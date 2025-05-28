package com.example.chromatic_chaos_thegame;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardsActivity extends AppCompatActivity {

    private ListView scoresListView;
    private ScoresDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        scoresListView = findViewById(R.id.scoresListView);
        Button backButton = findViewById(R.id.backButton);
        TextView titleTextView = findViewById(R.id.leaderboardsTitle); // Upewnij się, że masz ten TextView w XML

        dataSource = new ScoresDataSource(this);
        dataSource.open();

        List<Long> scores = dataSource.getTopScores(10); // Pobierz top 10

        dataSource.close();

        List<String> scoreStrings = new ArrayList<>();
        int rank = 1;
        if (scores.isEmpty()) {
            scoreStrings.add("Brak wyników!");
        } else {
            for (Long score : scores) {
                scoreStrings.add(rank + ". " + score + " pkt");
                rank++;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, scoreStrings);
        scoresListView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish()); // Przycisk Wstecz zamyka aktywność
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
}