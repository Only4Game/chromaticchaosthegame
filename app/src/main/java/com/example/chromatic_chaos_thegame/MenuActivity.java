package com.example.chromatic_chaos_thegame;

import android.content.Intent;

import android.os.Bundle;

import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);



        Button playButton = findViewById(R.id.playButton);
        Button leaderboardsButton = findViewById(R.id.leaderboardsButton); // NOWY

        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, GameActivity.class);
            startActivity(intent);
        });

        // NOWY LISTENER
        leaderboardsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, LeaderboardsActivity.class);
            startActivity(intent);
        });
    }
}