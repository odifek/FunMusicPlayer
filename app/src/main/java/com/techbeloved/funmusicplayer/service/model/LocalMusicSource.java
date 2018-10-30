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

    private <T> Single<List<T>> getMediaFilesInBackground(final Uri uri, final CursorHandler<T> cursorHandler) {

        return Observable.create((ObservableOnSubscribe<T>) emitter -> {
            Cursor cursor = mContext.getContentResolver().query(uri,
                    null,
                    null,
                    null,
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
                .flatMap((Function<T, ObservableSource<T>>) data
                        -> getAlbumInfo(data, cursorHandler))
                .toList();
    }

    /**
     * Queries the MediaStore Albums table for the album information of the given media
     *
     * @param <T>           is mostly {@link MediaMetadataCompat} or something like that
     * @param data          represents the original metadata that we want to add more info to
     * @param cursorHandler iterates through the cursor retrieving desired information
     * @return
     */
    private <T> Observable<T> getAlbumInfo(T data, final CursorHandler<T> cursorHandler) {
        MediaMetadataCompat currentMedia = (MediaMetadataCompat) data;
        String album = currentMedia.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        String selection = MediaStore.Audio.Albums.ALBUM + "=?";
        return Observable.create((ObservableEmitter<T> emitter) -> {
            Cursor cursor = mContext.getContentResolver().query(LocalMusicSource.CONTENT_URI_ALBUMS,
                    null,
                    selection,
                    new String[]{album},
                    null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                MediaMetadataCompat albumData = (MediaMetadataCompat) cursorHandler.handle(cursor);
                MediaMetadataCompat resultMeta = rebuildMetaWithAlbumInfo(currentMedia, albumData);
                emitter.onNext((T) resultMeta);

                cursor.close();
            } else {
                // Just return the original media metadata
                emitter.onNext(data);

            }
            emitter.onComplete(); // Has to be called inorder that the outer observable can complete
        });
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
