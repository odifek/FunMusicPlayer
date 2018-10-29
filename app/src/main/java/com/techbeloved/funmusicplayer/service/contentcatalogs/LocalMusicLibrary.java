package com.techbeloved.funmusicplayer.service.contentcatalogs;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class LocalMusicLibrary {

    private static final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();
    private static final HashMap<String, String> musicFileName = new HashMap<>();

    public static String getRoot() {
        return "root";
    }

    public static String getMusicFilename(String mediaId) {
        return musicFileName.containsKey(mediaId) ? musicFileName.get(mediaId) : null;
    }

    public static List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        for (MediaMetadataCompat metadata: music.values()) {
            result.add(
                    new MediaBrowserCompat.MediaItem(
                            metadata.getDescription(),
                            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        return result;
    }

    public static MediaMetadataCompat getMetadata(String mediaId) {
        MediaMetadataCompat mediaMetadata = music.get(mediaId);
        return mediaMetadata;
    }

    private static void insertMetadata(MediaMetadataCompat metadataCompat) {
        music.put(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID), metadataCompat);
    }
}
