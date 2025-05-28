package com.example.chromatic_chaos_thegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    private ScoresDataSource dataSource; // NOWY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        dataSource = new ScoresDataSource(this); // NOWY
        dataSource.open(); // NOWY

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        Button restartButton = findViewById(R.id.restartButton);
        Button menuButton = findViewById(R.id.menuButton);

        long finalScore = getIntent().getLongExtra("FINAL_SCORE", 0);
        scoreTextView.setText("Wynik: " + finalScore);

        // ZAPISZ WYNIK (tylko jeśli jest większy od 0)
        if (finalScore > 0) {
            dataSource.addScore(finalScore);
        }

        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // NOWE METODY cyklu życia do zarządzania bazą
    @Override
    protected void onResume() {
        dataSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        dataSource.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        dataSource.close();
        super.onDestroy();
    }
}