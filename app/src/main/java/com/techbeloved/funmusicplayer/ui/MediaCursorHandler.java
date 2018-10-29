package com.techbeloved.funmusicplayer.ui;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

public class MediaCursorHandler implements CursorHandler<MediaMetadataCompat> {
    @Override
    public MediaMetadataCompat handle(Cursor cursor) {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        // Try to handle all media info logic here
        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE);
        if (indexExists(titleIndex)) {
            String title = cursor.getString(titleIndex);
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        }

        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
        if (indexExists(artistIndex)) {
            String artist = cursor.getString(artistIndex);
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);
        }

        int filePathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        if (indexExists(filePathIndex)) {
            String filePath = cursor.getString(filePathIndex);
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, filePath);
        }

        int albumArtIndex = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART);
        if (indexExists(albumArtIndex)) {
            String albumArtPath = cursor.getString(albumArtIndex);
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtPath);
        }

        int albumIndex = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
        if (indexExists(albumIndex)) {
            String album = cursor.getString(albumIndex);
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);
        }

//        String albumArtPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART))


        return builder
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtPath)
                .build();
    }

    private boolean indexExists(int indexTitle) {
        return indexTitle != -1;
    }
}
