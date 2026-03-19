package org.example.mastermind.model;

public class Evaluation {

    private final int exactMatches;
    private final int shapeOnlyMatches;

    public Evaluation(int exactMatches, int shapeOnlyMatches) {
        this.exactMatches = exactMatches;
        this.shapeOnlyMatches = shapeOnlyMatches;
    }

    public int getExactMatches() {
        return exactMatches;
    }

    public int getShapeOnlyMatches() {
        return shapeOnlyMatches;
    }
}