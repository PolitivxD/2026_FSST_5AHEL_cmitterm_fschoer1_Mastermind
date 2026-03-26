package org.example.mastermind.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MastermindModel {

    public static final int CODE_LENGTH = 4;
    public static final int MAX_ATTEMPTS = 10;

    private static final ShapeType[] ALLOWED_SHAPES = ShapeType.values();

    private final Random random = new Random();
    private final List<RoundEntry> history = new ArrayList<>();

    private List<ShapeType> secretCode;
    private int attemptsUsed;
    private boolean gameOver;
    private boolean won;

    public MastermindModel() {
        startNewGame();
    }

    public void startNewGame() {
        secretCode = generateSecretCode();
        attemptsUsed = 0;
        gameOver = false;
        won = false;
        history.clear();
    }

    private List<ShapeType> generateSecretCode() {
        List<ShapeType> code = new ArrayList<>();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(ALLOWED_SHAPES.length);
            code.add(ALLOWED_SHAPES[randomIndex]);
        }

        return code;
    }

    public boolean isValidGuess(List<ShapeType> guess) {
        if (guess == null) {
            return false;
        }

        if (guess.size() != CODE_LENGTH) {
            return false;
        }

        for (ShapeType shape : guess) {
            if (shape == null) {
                return false;
            }
        }

        return true;
    }

    public RoundEntry makeGuess(List<ShapeType> guess) {
        if (gameOver) {
            throw new IllegalStateException("Das Spiel ist bereits beendet.");
        }

        if (!isValidGuess(guess)) {
            throw new IllegalArgumentException("Ungültige Eingabe.");
        }

        List<ShapeType> guessCopy = new ArrayList<>(guess);
        Evaluation evaluation = evaluateGuess(guessCopy);

        RoundEntry entry = new RoundEntry(guessCopy, evaluation);
        history.add(entry);
        attemptsUsed++;

        if (evaluation.getExactMatches() == CODE_LENGTH) {
            won = true;
            gameOver = true;

        } else if (attemptsUsed >= MAX_ATTEMPTS) {
            gameOver = true;

        }

        return entry;
    }

    private Evaluation evaluateGuess(List<ShapeType> guess) {
        boolean[] secretUsed = new boolean[CODE_LENGTH];
        boolean[] guessUsed = new boolean[CODE_LENGTH];

        int exactMatches = 0;
        int shapeOnlyMatches = 0;

        for (int i = 0; i < CODE_LENGTH; i++) {
            if (guess.get(i) == secretCode.get(i)) {
                exactMatches++;
                secretUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        for (int i = 0; i < CODE_LENGTH; i++) {
            if (guessUsed[i]) {
                continue;

            }

            for (int j = 0; j < CODE_LENGTH; j++) {
                if (secretUsed[j]) {
                    continue;
                }

                if (guess.get(i) == secretCode.get(j)) {
                    shapeOnlyMatches++;
                    secretUsed[j] = true;
                    guessUsed[i] = true;
                    break;
                }
            }
        }

        return new Evaluation(exactMatches, shapeOnlyMatches);
    }

    public List<RoundEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getRemainingAttempts() {
        return MAX_ATTEMPTS - attemptsUsed;
    }

    public List<ShapeType> getSecretCode() {
        return Collections.unmodifiableList(secretCode);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }
    public int getAttemptsUsed() {
        return attemptsUsed;
    }
}