package org.example.mastermind.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundEntry {

    private final List<ShapeType> guess;
    private final Evaluation evaluation;

    public RoundEntry(List<ShapeType> guess, Evaluation evaluation) {
        this.guess = new ArrayList<>(guess);
        this.evaluation = evaluation;
    }

    public List<ShapeType> getGuess() {
        return Collections.unmodifiableList(guess);
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }
}