package com.techbeloved.funmusicplayer.service;

import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Listener to provide state updates from MediaPlayerAdapter (the media player)
 * to {@link MusicService} (the service that holds our {@link android.support.v4.media.session.MediaSessionCompat}
 */
public abstract class PlaybackInfoListener {

    public abstract void onPlaybackStateChange(PlaybackStateCompat state);

    public void onPlaybackCompleted() {

    }
}
