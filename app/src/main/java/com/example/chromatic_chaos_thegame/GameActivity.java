package com.example.chromatic_chaos_thegame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements GameView.GameManager {

    private GameView gameView;
    private long currentScore = 0;

    private final int[] buttonColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        gameView.setGameManager(this);

        // ODKOMENTUJ I DODAJ KOD PRZYCISKÓW
        Button buttonRed = findViewById(R.id.buttonRed);
        Button buttonGreen = findViewById(R.id.buttonGreen);
        Button buttonBlue = findViewById(R.id.buttonBlue);
        Button buttonYellow = findViewById(R.id.buttonYellow);

        buttonRed.setBackgroundColor(Color.RED);
        buttonGreen.setBackgroundColor(Color.GREEN);
        buttonBlue.setBackgroundColor(Color.BLUE);
        buttonYellow.setBackgroundColor(Color.YELLOW);

        buttonRed.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[0]));
        buttonGreen.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[1]));
        buttonBlue.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[2]));
        buttonYellow.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[3]));
        // KONIEC KODU PRZYCISKÓW

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentScore = 0;
        // gameView.resume();
    }

    @Override
    public void onGameOver(long finalScore) {
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("FINAL_SCORE", finalScore);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void addScore(int points) {
        currentScore += points;
    }

    @Override
    public long getScore() {
        return currentScore;
    }
}