package com.techbeloved.funmusicplayer.service.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.techbeloved.funmusicplayer.ui.AudioMedia;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LocalMusicSource implements MusicProviderSource {

    private static final String TAG = LocalMusicSource.class.getSimpleName();

    private static final Uri CONTENT_URI_ALBUMS = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    private static final Uri CONTENT_URI_AUDIO = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private Context mContext;

    private List<MediaMetadataCompat> mData = new ArrayList<>();

    public LocalMusicSource(Context context) {
        mContext = context;

        Disposable subscribe = getMediaFilesInBackground(CONTENT_URI_AUDIO, new MediaCursorHandler())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(mediaMetadataCompats -> {
                    mData.clear();
                    mData.addAll(mediaMetadataCompats);
                }, error -> {
                    Log.e(TAG, "onCreate: Some error encountered: " + error.getMessage());
                });

    }

    private  Single<List<MediaMetadataCompat>> getMediaFilesInBackground(final Uri uri, final CursorHandler<MediaMetadataCompat> cursorHandler) {

        return Observable.create((ObservableOnSubscribe<MediaMetadataCompat>) emitter -> {
            String selection = MediaStore.Audio.AudioColumns.IS_MUSIC + ">?";
            Cursor cursor = mContext.getContentResolver().query(uri,
                    null,
                    selection,
                    new String[]{String.valueOf(0)},
                    MediaStore.Audio.AudioColumns.TITLE);

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    // We want to get the album information here
                    emitter.onNext(cursorHandler.handle(cursor));
                }
                cursor.close();
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable("Error: no Item retrieved"));
            }
        })
                .flatMap((Function<MediaMetadataCompat, ObservableSource<MediaMetadataCompat>>) data -> {
//                MediaMetadataCompat currentMedia = (MediaMetadataCompat) data;
            return insertAlbumInfo(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, data, new AlbumCursorHandler());
        })
                .toList()

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Queries the MediaStore Albums table for the album information of the given media
     * @param uri
     * @param data
     * @param cursorHandler
     * @return
     */
    private Observable <MediaMetadataCompat> insertAlbumInfo(final Uri uri, MediaMetadataCompat data, final CursorHandler<MediaMetadataCompat> cursorHandler) {
        String album = data.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        Log.i(TAG, "insertAlbumInfo: " + album);
        String selection = null;
        String[] selectionArgs = null;
        if (album != null) {
            selection = MediaStore.Audio.Albums.ALBUM + "=?";
            selectionArgs = new String[]{album};
        }
        String finalSelection = selection;
        String[] finalSelectionArgs = selectionArgs;
        return Observable.create((ObservableEmitter<MediaMetadataCompat> emitter) -> {
            Cursor cursor = mContext.getContentResolver().query(uri,
                    null,
                    finalSelection,
                    finalSelectionArgs,
                    null);
//            Log.i(TAG, "insertAlbumInfo: did we get any thing from albums?");
            if (cursor != null && cursor.getCount() > 0) {
//                Log.i(TAG, "insertAlbumInfo: yes");
                cursor.moveToFirst();
                MediaMetadataCompat albumData = cursorHandler.handle(cursor);
                MediaMetadataCompat resultMeta = rebuildMetaWithAlbumInfo(data, albumData);
                emitter.onNext(resultMeta);

                cursor.close();
            } else {
                // Just return the original media metadata
//                emitter.onError(new Throwable("Error: no album info available!"));
//                Log.i(TAG, "insertAlbumInfo: no");
                emitter.onNext(data);

            }
            emitter.onComplete(); // Has to be called inorder that the outer observable can complete
        });
//                .subscribeOn(Schedulers.io());
//                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Handles the adding of the album info
     *
     * @param currentMedia the original media
     * @param albumData    represents the album info
     * @return a {@link MediaMetadataCompat} with the new info
     */
    private MediaMetadataCompat rebuildMetaWithAlbumInfo(MediaMetadataCompat currentMedia, MediaMetadataCompat albumData) {
        return new MediaMetadataCompat.Builder(currentMedia)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumData.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                .build();
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        return mData.iterator();
    }
}
