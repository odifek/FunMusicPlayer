package com.techbeloved.funmusicplayer.service.model;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import java.time.Year;

public class MediaCursorHandler implements CursorHandler<MediaMetadataCompat> {
    @Override
    public MediaMetadataCompat handle(Cursor cursor) {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        // Try to handle all media info logic here
        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE);
        String title = cursor.getString(titleIndex);
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);

        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
        String artist = cursor.getString(artistIndex);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist);

        int filePathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        String filePath = cursor.getString(filePathIndex);
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, filePath);
        // Use the file name as the key
        String mediaId = filePath.split("/", filePath.lastIndexOf("/"))[1];
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId);

        int albumIndex = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM);
        String album = cursor.getString(albumIndex);
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album);

        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);

        int year = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.YEAR));
        builder.putString(MediaMetadataCompat.METADATA_KEY_DATE, String.valueOf(year));

        return builder.build();
    }

    private boolean indexExists(int indexTitle) {
        return indexTitle != -1;
    }
}
