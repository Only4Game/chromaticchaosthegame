package com.example.chromatic_chaos_thegame;

import android.content.DialogInterface; // <-- NOWY IMPORT
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog; // <-- NOWY IMPORT
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // <-- NOWY IMPORT

public class LeaderboardsActivity extends AppCompatActivity {

    private ListView scoresListView;
    private ScoresDataSource dataSource;
    private ArrayAdapter<String> adapter; // <-- ZMIANA NA POLE KLASY
    private List<String> scoreStrings; // <-- ZMIANA NA POLE KLASY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        scoresListView = findViewById(R.id.scoresListView);
        Button backButton = findViewById(R.id.backButton);
        Button clearScoresButton = findViewById(R.id.clearScoresButton); // <-- NOWE

        dataSource = new ScoresDataSource(this);
        dataSource.open();

        scoreStrings = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, scoreStrings);
        scoresListView.setAdapter(adapter);

        loadScores(); // <-- NOWA METODA DO ŁADOWANIA

        backButton.setOnClickListener(v -> finish());
        clearScoresButton.setOnClickListener(v -> showClearConfirmationDialog()); // <-- NOWY LISTENER
    }

    // NOWA METODA DO ŁADOWANIA/ODŚWIEŻANIA WYNIKÓW
    private void loadScores() {
        List<ScoreData> scores = dataSource.getTopScores(15); // Pobierz top 15

        scoreStrings.clear(); // Wyczyść starą listę

        int rank = 1;
        if (scores.isEmpty()) {
            scoreStrings.add("Brak wyników!");
        } else {
            for (ScoreData scoreData : scores) {
                // Używamy ScoreData.toString() lub formatujemy ręcznie
                scoreStrings.add(rank + ". " + scoreData.getName() + " - " + scoreData.getScore() + " pkt");
                rank++;
            }
        }
        adapter.notifyDataSetChanged(); // Odśwież widok listy
    }

    // NOWA METODA DO WYŚWIETLANIA OKNA POTWIERDZENIA
    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Potwierdzenie")
                .setMessage("Czy na pewno chcesz usunąć wszystkie zapisane wyniki?")
                .setPositiveButton("Tak", (dialog, which) -> {
                    dataSource.deleteAllScores(); // Usuń wyniki
                    loadScores(); // Odśwież listę
                })
                .setNegativeButton("Nie", null) // Nic nie rób, jeśli kliknięto "Nie"
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    protected void onResume() {
        dataSource.open();
        loadScores(); // Odświeżaj wyniki przy powrocie do aktywności
        super.onResume();
    }

    @Override
    protected void onPause() {
        dataSource.close();
        super.onPause();
    }
}