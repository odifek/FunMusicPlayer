package com.techbeloved.funmusicplayer.service.model;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

/**
 * Takes care of querying the albums table and extracting some useful album information for a given audio
 */
public class AlbumCursorHandler implements CursorHandler<MediaMetadataCompat> {
    @Override
    public MediaMetadataCompat handle(Cursor cursor) {

        String albumArtPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART));
        String albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ARTIST));
        // Other album only information can be handled here

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, albumArtist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtPath)
                .build();
    }
}
