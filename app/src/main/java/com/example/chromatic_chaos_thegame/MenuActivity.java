package com.example.chromatic_chaos_thegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chromatic_chaos_thegame.GameActivity;
import com.example.chromatic_chaos_thegame.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button playButton = findViewById(R.id.playButton);

        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            startActivity(intent);
            // finish(); // Optional: close menu when game starts
        });
    }
}