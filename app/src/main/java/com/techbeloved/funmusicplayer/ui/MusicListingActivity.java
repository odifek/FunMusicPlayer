package com.techbeloved.funmusicplayer.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.techbeloved.funmusicplayer.R;
import com.techbeloved.funmusicplayer.ui.adapter.MediaListingAdapter;

import java.util.ArrayList;
import java.util.List;

public class MusicListingActivity extends AppCompatActivity {

    private static final String TAG = MusicListingActivity.class.getSimpleName();

    private static final Uri MEDIA_EXTERNAL_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final Uri MEDIA_INTERNAL_URI = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

    private RecyclerView mRecyclerView;
    private MediaListingAdapter mAdapter;

    private List<AudioMedia> mMediaList;

    private Disposable mResultDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_listing);

        mAdapter = new MediaListingAdapter(this);
        mRecyclerView = findViewById(R.id.recyclerview_media_listing);
        mRecyclerView.setAdapter(mAdapter);
        mMediaList = new ArrayList<>();

        mResultDisposable = getMediaFilesInBackground(MEDIA_EXTERNAL_URI, new MediaCursorHandler())
                .subscribe(mediaMetadataCompats -> {
                    Log.i(TAG, "onCreate: Did we get any data?");
                    mMediaList.clear();
                    for (MediaMetadataCompat media :
                            mediaMetadataCompats) {
                        String title = media.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
                        String artist = media.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                        String filePath = media.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
                        String albumArtPath = media.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
                        mMediaList.add(new AudioMedia(title, artist, filePath, albumArtPath));
                        Log.i(TAG, "onCreate: albumArt " + albumArtPath );
                    }
                    mAdapter.setMediaList(mMediaList);
                    Log.i(TAG, "how many media files?: " + mMediaList.size());
                }, error -> {
                    Log.i(TAG, "onCreate: Some error encountered: " + error.getMessage());
                });
    }

    private <T> Single<List<T>> getMediaFilesInBackground(final Uri uri, final CursorHandler<T> cursorHandler) {

        return Observable.create((ObservableOnSubscribe<T>) emitter -> {
            Cursor cursor = MusicListingActivity.this.getContentResolver().query(uri,
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
                .flatMap((Function<T, ObservableSource<T>>) data -> {
//                MediaMetadataCompat currentMedia = (MediaMetadataCompat) data;
            return insertAlbumInfo(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, data, cursorHandler);
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
     * @param <T>
     * @return
     */
    private <T> Observable <T> insertAlbumInfo(final Uri uri, T data, final CursorHandler<T> cursorHandler) {
        MediaMetadataCompat currentMedia = (MediaMetadataCompat) data;
        String album = currentMedia.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        Log.i(TAG, "insertAlbumInfo: " + album);
        String selection = MediaStore.Audio.Albums.ALBUM + "=?";
        return Observable.create((ObservableEmitter<T> emitter) -> {
            Cursor cursor = MusicListingActivity.this.getContentResolver().query(uri,
                    null,
                    selection,
                    new String[]{album},
                    null);
            Log.i(TAG, "insertAlbumInfo: did we get any thing from albums?");
            if (cursor != null && cursor.getCount() > 0) {
                Log.i(TAG, "insertAlbumInfo: yes");
                cursor.moveToFirst();
                MediaMetadataCompat albumData = (MediaMetadataCompat) cursorHandler.handle(cursor);
                MediaMetadataCompat resultMeta = rebuildMetaWithAlbumInfo(currentMedia, albumData);
                emitter.onNext((T) resultMeta);

                cursor.close();
            } else {
                // Just return the original media metadata
//                emitter.onError(new Throwable("Error: no album info available!"));
                Log.i(TAG, "insertAlbumInfo: no");
                emitter.onNext(data);

            }
            emitter.onComplete(); // Has to be called inorder that the outer observable can complete
        });
//                .subscribeOn(Schedulers.io());
//                .observeOn(AndroidSchedulers.mainThread());
    }

    private MediaMetadataCompat rebuildMetaWithAlbumInfo(MediaMetadataCompat currentMedia, MediaMetadataCompat albumData) {
        return new MediaMetadataCompat.Builder(currentMedia)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumData.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mResultDisposable.dispose();
    }
}
