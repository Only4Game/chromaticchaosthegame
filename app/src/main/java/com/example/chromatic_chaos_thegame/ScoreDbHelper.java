package com.example.chromatic_chaos_thegame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ScoreDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2; // <-- ZWIĘKSZ WERSJĘ DO 2
    public static final String DATABASE_NAME = "ChromatycznyChaos.db";

    public static class ScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "scores";
        public static final String COLUMN_NAME_PLAYER = "player_name"; // <-- NOWA KOLUMNA
        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ScoreEntry.TABLE_NAME + " (" +
                    ScoreEntry._ID + " INTEGER PRIMARY KEY," +
                    ScoreEntry.COLUMN_NAME_PLAYER + " TEXT," + // <-- DODAJ TYP KOLUMNY
                    ScoreEntry.COLUMN_NAME_SCORE + " INTEGER," +
                    ScoreEntry.COLUMN_NAME_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ScoreEntry.TABLE_NAME;

    public ScoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Usuwa starą tabelę i tworzy nową przy aktualizacji
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}