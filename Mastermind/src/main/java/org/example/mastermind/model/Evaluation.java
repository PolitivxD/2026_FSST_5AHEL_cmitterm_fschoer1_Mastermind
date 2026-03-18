package org.example.mastermind.model;

public class Evaluation {

    private final int exactMatches;
    private final int colorOnlyMatches;

    public Evaluation(int exactMatches, int colorOnlyMatches) {
        this.exactMatches = exactMatches;
        this.colorOnlyMatches = colorOnlyMatches;
    }

    public int getExactMatches() {
        return exactMatches;
    }

    public int getColorOnlyMatches() {
        return colorOnlyMatches;
    }
}