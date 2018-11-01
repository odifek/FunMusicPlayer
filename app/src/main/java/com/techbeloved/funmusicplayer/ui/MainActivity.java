package com.techbeloved.funmusicplayer.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.techbeloved.funmusicplayer.R;
import com.techbeloved.funmusicplayer.client.MediaBrowserHelper;
import com.techbeloved.funmusicplayer.service.MusicService;
import com.techbeloved.funmusicplayer.service.contentcatalogs.MusicLibrary;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView mAlbumArt;
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private Button mPlayPauseBtn;

    private MediaBrowserHelper mMediaBrowserHelper;

    private boolean mIsPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent intent = new Intent(this, MusicListingActivity.class);
//        startActivity(intent);

        mTitleTextView = findViewById(R.id.song_title_text_view);
        mArtistTextView = findViewById(R.id.artist_text_view);
        mAlbumArt = findViewById(R.id.album_art_image_view);
        mPlayPauseBtn = findViewById(R.id.btn_play);

        final ClickListener clickListener = new ClickListener();
        findViewById(R.id.btn_prev).setOnClickListener(clickListener);
        mPlayPauseBtn.setOnClickListener(clickListener);
        findViewById(R.id.btn_next).setOnClickListener(clickListener);

        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaBrowserHelper.onStop();
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_prev:
                    mMediaBrowserHelper.getTransportControls().skipToPrevious();
                    break;
                case R.id.btn_play:
                    if (mIsPlaying) {
                        mMediaBrowserHelper.getTransportControls().pause();
                    } else {
                        mMediaBrowserHelper.getTransportControls().play();
                    }
                    break;
                case R.id.btn_next:
                    mMediaBrowserHelper.getTransportControls().skipToNext();
                    break;
            }
        }
    }

    /**
     * Customize the connection to our {@link androidx.media.MediaBrowserServiceCompat}
     * and implement our app specific desires.
     */
    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, MusicService.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            super.onConnected(mediaController);
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            final MediaControllerCompat mediaController = getMediaController();

            // Queue up all media items for this simple sample.
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                Log.i("MainActivity", "onChildrenLoaded: " + mediaItem.getMediaId() + " browseable? " + mediaItem.isBrowsable());
                mediaController.addQueueItem(mediaItem.getDescription());
            }

            // Call prepare now so pressing play just works
            mediaController.getTransportControls().prepare();
        }

    }

    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mIsPlaying = state != null
                    && state.getState() == PlaybackStateCompat.STATE_PLAYING;
            if (mIsPlaying) mPlayPauseBtn.setText(R.string.label_pause);
            else mPlayPauseBtn.setText(R.string.label_play);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            mTitleTextView.setText(
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mArtistTextView.setText(
                   metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mAlbumArt.setImageBitmap(MusicLibrary.getAlbumBitmap(MainActivity.this,
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    }
}
