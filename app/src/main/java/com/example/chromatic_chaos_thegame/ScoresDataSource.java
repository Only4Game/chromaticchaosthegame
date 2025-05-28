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

    public void addScore(long score) {
        ContentValues values = new ContentValues();
        values.put(ScoreDbHelper.ScoreEntry.COLUMN_NAME_SCORE, score);

        database.insert(ScoreDbHelper.ScoreEntry.TABLE_NAME, null, values);
    }

    // Pobiera top 10 wyników
    public List<Long> getTopScores(int limit) {
        List<Long> scores = new ArrayList<>();

        Cursor cursor = database.query(
                ScoreDbHelper.ScoreEntry.TABLE_NAME,
                allColumns,
                null,
                null,
                null,
                null,
                ScoreDbHelper.ScoreEntry.COLUMN_NAME_SCORE + " DESC", // Sortuj malejąco
                String.valueOf(limit) // Limit wyników
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long score = cursor.getLong(cursor.getColumnIndexOrThrow(ScoreDbHelper.ScoreEntry.COLUMN_NAME_SCORE));
            scores.add(score);
            cursor.moveToNext();
        }
        cursor.close();
        return scores;
    }
}
