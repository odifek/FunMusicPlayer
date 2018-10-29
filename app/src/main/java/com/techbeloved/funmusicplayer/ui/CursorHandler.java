package com.techbeloved.funmusicplayer.ui;

import android.database.Cursor;

interface CursorHandler<T> {
    T handle(Cursor cursor);
}
