package com.example.chromatic_chaos_thegame;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText; // <-- NOWY IMPORT
import android.widget.TextView;
import android.widget.Toast; // <-- NOWY IMPORT
import androidx.appcompat.app.AppCompatActivity;

public class GameOverActivity extends AppCompatActivity {

    private ScoresDataSource dataSource;
    private EditText playerNameEditText; // <-- NOWE
    private Button saveScoreButton; // <-- NOWE
    private long finalScore; // <-- NOWE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        dataSource = new ScoresDataSource(this);
        dataSource.open();

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        Button restartButton = findViewById(R.id.restartButton);
        Button menuButton = findViewById(R.id.menuButton);
        playerNameEditText = findViewById(R.id.playerNameEditText); // <-- NOWE
        saveScoreButton = findViewById(R.id.saveScoreButton); // <-- NOWE

        finalScore = getIntent().getLongExtra("FINAL_SCORE", 0);
        scoreTextView.setText("Wynik: " + finalScore);

        // Ukryj pole i przycisk, jeśli wynik to 0
        if (finalScore <= 0) {
            playerNameEditText.setVisibility(android.view.View.GONE);
            saveScoreButton.setVisibility(android.view.View.GONE);
        }

        // Zamiast zapisywać od razu, robimy to po kliknięciu przycisku
        saveScoreButton.setOnClickListener(v -> saveScore());

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

    // NOWA METODA ZAPISU
    private void saveScore() {
        String playerName = playerNameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(playerName)) {
            playerName = "Anonim"; // Domyślna nazwa, jeśli pole jest puste
        }

        if (finalScore > 0) {
            dataSource.addScore(playerName, finalScore);
            Toast.makeText(this, "Wynik zapisany!", Toast.LENGTH_SHORT).show();
            // Wyłącz pole i przycisk po zapisie
            playerNameEditText.setEnabled(false);
            saveScoreButton.setEnabled(false);
        }
    }

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