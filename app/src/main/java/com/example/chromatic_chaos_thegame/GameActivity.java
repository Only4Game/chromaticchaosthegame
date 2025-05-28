package com.example.chromatic_chaos_thegame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity implements GameView.GameManager {

    private GameView gameView;
    private long currentScore = 0;
    private int[] buttonColors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW}; // Początkowe kolory
    private Button buttonRed, buttonGreen, buttonBlue, buttonYellow; // Przechowuj referencje

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // Upewnij się, że używasz activity_game.xml z przyciskami!

        gameView = findViewById(R.id.gameView);
        // Sprawdź, czy gameView nie jest null! Jeśli jest, to błąd w R.layout.activity_game
        if (gameView != null) {
            gameView.setGameManager(this); // To powinno teraz działać
        } else {
            android.util.Log.e("GameActivity", "GameView not found! Check R.id.gameView in activity_game.xml");
            Toast.makeText(this, "Błąd inicjalizacji widoku gry!", Toast.LENGTH_LONG).show();
            finish(); // Zakończ aktywność, jeśli widok gry nie działa
            return;
        }


        buttonRed = findViewById(R.id.buttonRed);
        buttonGreen = findViewById(R.id.buttonGreen);
        buttonBlue = findViewById(R.id.buttonBlue);
        buttonYellow = findViewById(R.id.buttonYellow);

        // Sprawdź, czy przyciski nie są null
        if (buttonRed == null || buttonGreen == null || buttonBlue == null || buttonYellow == null) {
            android.util.Log.e("GameActivity", "One or more buttons not found! Check IDs in activity_game.xml");
            Toast.makeText(this, "Błąd inicjalizacji przycisków!", Toast.LENGTH_LONG).show();
            // Możesz zdecydować, czy kończyć grę, czy grać bez przycisków
        } else {
            updateButtonColors(); // Ustaw początkowe kolory i listenery
        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void updateButtonColors() {
        if (buttonRed == null) return; // Dodatkowe zabezpieczenie

        buttonRed.setBackgroundColor(buttonColors[0]);
        buttonGreen.setBackgroundColor(buttonColors[1]);
        buttonBlue.setBackgroundColor(buttonColors[2]);
        buttonYellow.setBackgroundColor(buttonColors[3]);

        buttonRed.setTextColor(isColorDark(buttonColors[0]) ? Color.WHITE : Color.BLACK);
        buttonGreen.setTextColor(isColorDark(buttonColors[1]) ? Color.WHITE : Color.BLACK);
        buttonBlue.setTextColor(isColorDark(buttonColors[2]) ? Color.WHITE : Color.BLACK);
        buttonYellow.setTextColor(isColorDark(buttonColors[3]) ? Color.WHITE : Color.BLACK);

        buttonRed.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[0]));
        buttonGreen.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[1]));
        buttonBlue.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[2]));
        buttonYellow.setOnClickListener(v -> gameView.changePlatformColor(buttonColors[3]));
    }

    private boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness >= 0.5;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pause(); // Użyj gameView.pause() (public)
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentScore = 0;
        if (gameView != null) gameView.resume();
    }

    @Override
    public void onGameOver(long finalScore) {
        // Przekaż wynik do GameOverActivity
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("FINAL_SCORE", finalScore);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void addScore(int points) {
        // Jeśli punkty są ujemne, to znaczy, że GameView chce zresetować wynik
        if (points < 0 && currentScore > 0) {
            currentScore = 0;
        } else if (points > 0) {
            currentScore += points;
        }
    }

    @Override
    public long getScore() {
        return currentScore;
    }

    @Override
    public void onPaletteChanged(int[] newColors) {
        if (newColors != null && newColors.length == 4) {
            this.buttonColors = newColors;
            runOnUiThread(this::updateButtonColors);
        }
    }
}