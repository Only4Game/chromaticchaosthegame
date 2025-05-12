package com.example.chromatic_chaos_thegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chromatic_chaos_thegame.R;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R .layout.activity_game_over);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        Button restartButton = findViewById(R.id.restartButton);
        Button menuButton = findViewById(R.id.menuButton);

        long finalScore = getIntent().getLongExtra("FINAL_SCORE", 0);
        scoreTextView.setText("Wynik: " + finalScore);

        restartButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, com.example.chromatic_chaos_thegame.MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
