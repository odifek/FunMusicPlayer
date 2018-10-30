package com.techbeloved.funmusicplayer.service.model;

import android.database.Cursor;

public interface CursorHandler<T> {
    T handle(Cursor cursor);
}
