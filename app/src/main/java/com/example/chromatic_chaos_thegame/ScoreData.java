package com.example.chromatic_chaos_thegame;

public class ScoreData {
    private String name;
    private long score;

    public ScoreData(String name, long score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public long getScore() {
        return score;
    }

    @Override
    public String toString() {
        // Format, w jakim wynik pojawi się na liście
        return name + " - " + score + " pkt";
    }
}