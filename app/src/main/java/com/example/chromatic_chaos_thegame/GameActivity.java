package com.example.chromatic_chaos_thegame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements com.example.chromatic_chaos_thegame.GameView.GameManager {

    private com.example.chromatic_chaos_thegame.GameView gameView;
    private long currentScore = 0;

    private final int[] buttonColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.gameView);
        gameView.setGameManager(this);

//        Button buttonRed = findViewById(R.id.buttonRed);
//        Button buttonGreen = findViewById(R.id.buttonGreen);
//        Button buttonBlue = findViewById(R.id.buttonBlue);
//        Button buttonYellow = findViewById(R.id.buttonYellow);
//
//        buttonRed.setBackgroundColor(Color.RED);
//        buttonGreen.setBackgroundColor(Color.GREEN);
//        buttonBlue.setBackgroundColor(Color.BLUE);
//        buttonYellow.setBackgroundColor(Color.YELLOW);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // Optional: Make fullscreen immersive
        // getWindow().getDecorView().setSystemUiVisibility(
        //        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
        //        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
        //        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentScore = 0; // Reset score on resume for simplicity
        // gameView.resume(); // GameView handles its thread start/stop via surface methods
    }

    @Override
    public void onGameOver(long finalScore) {
        Intent intent = new Intent(this, com.example.chromatic_chaos_thegame.GameOverActivity.class);
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
