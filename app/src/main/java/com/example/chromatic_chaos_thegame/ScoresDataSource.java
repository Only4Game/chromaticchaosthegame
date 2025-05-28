package com.example.chromatic_chaos_thegame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class ScoresDataSource {

    private SQLiteDatabase database;
    private ScoreDbHelper dbHelper;
    private String[] allColumns = {
            ScoreDbHelper.ScoreEntry._ID,
            ScoreDbHelper.ScoreEntry.COLUMN_NAME_PLAYER, // <-- DODANA KOLUMNA
            ScoreDbHelper.ScoreEntry.COLUMN_NAME_SCORE,
            ScoreDbHelper.ScoreEntry.COLUMN_NAME_TIMESTAMP
    };

    public ScoresDataSource(Context context) {
        dbHelper = new ScoreDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // ZMODYFIKOWANA METODA DODAWANIA WYNIKU
    public void addScore(String name, long score) {
        ContentValues values = new ContentValues();
        values.put(ScoreDbHelper.ScoreEntry.COLUMN_NAME_PLAYER, name);
        values.put(ScoreDbHelper.ScoreEntry.COLUMN_NAME_SCORE, score);

        database.insert(ScoreDbHelper.ScoreEntry.TABLE_NAME, null, values);
    }

    // ZMODYFIKOWANA METODA POBIERANIA WYNIKÓW
    public List<ScoreData> getTopScores(int limit) {
        List<ScoreData> scores = new ArrayList<>();

        Cursor cursor = database.query(
                ScoreDbHelper.ScoreEntry.TABLE_NAME,
                allColumns,
                null,
                null,
                null,
                null,
                ScoreDbHelper.ScoreEntry.COLUMN_NAME_SCORE + " DESC",
                String.valueOf(limit)
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ScoreDbHelper.ScoreEntry.COLUMN_NAME_PLAYER));
            long score = cursor.getLong(cursor.getColumnIndexOrThrow(ScoreDbHelper.ScoreEntry.COLUMN_NAME_SCORE));
            scores.add(new ScoreData(name, score)); // <-- UŻYWAMY ScoreData
            cursor.moveToNext();
        }
        cursor.close();
        return scores;
    }

    // NOWA METODA DO USUWANIA WYNIKÓW
    public void deleteAllScores() {
        database.delete(ScoreDbHelper.ScoreEntry.TABLE_NAME, null, null);
    }
}