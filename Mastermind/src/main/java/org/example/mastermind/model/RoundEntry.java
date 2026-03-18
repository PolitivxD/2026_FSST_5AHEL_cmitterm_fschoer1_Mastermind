package org.example.mastermind.model;

public class RoundEntry {

    private final String guess;
    private final Evaluation evaluation;

    public RoundEntry(String guess, Evaluation evaluation) {
        this.guess = guess;
        this.evaluation = evaluation;
    }

    public String getGuess() {
        return guess;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }
}