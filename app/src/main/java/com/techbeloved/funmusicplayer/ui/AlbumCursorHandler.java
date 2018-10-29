package com.techbeloved.funmusicplayer.ui;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

public class AlbumCursorHandler implements  CursorHandler<MediaMetadataCompat> {
    @Override
    public MediaMetadataCompat handle(Cursor cursor) {

        String albumTitle = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM));
        String albumArtPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART));
        // Other album only information can be handled here

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtPath)
                .build();
    }
}
