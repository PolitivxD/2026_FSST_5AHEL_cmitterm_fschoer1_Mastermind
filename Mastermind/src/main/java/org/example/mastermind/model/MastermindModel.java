package org.example.mastermind.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MastermindModel {

    public static final int CODE_LENGTH = 4;
    public static final int MAX_ATTEMPTS = 10;

    private static final char[] ALLOWED_COLORS = {'R', 'G', 'B', 'Y', 'O', 'P'};

    private final Random random = new Random();
    private final List<RoundEntry> history = new ArrayList<>();

    private String secretCode;
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

    private String generateSecretCode() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(ALLOWED_COLORS.length);
            code.append(ALLOWED_COLORS[randomIndex]);
        }

        return code.toString();
    }

    public String normalizeGuess(String input) {
        if (input == null) {
            return "";
        }

        return input.toUpperCase().replaceAll("\\s+", "");
    }

    public boolean isValidGuess(String input) {
        String guess = normalizeGuess(input);

        if (guess.length() != CODE_LENGTH) {
            return false;
        }

        for (int i = 0; i < guess.length(); i++) {
            if (!isAllowedColor(guess.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean isAllowedColor(char c) {
        for (char allowedColor : ALLOWED_COLORS) {
            if (allowedColor == c) {
                return true;
            }
        }
        return false;
    }

    public RoundEntry makeGuess(String input) {
        if (gameOver) {
            throw new IllegalStateException("Das Spiel ist bereits beendet.");
        }

        if (!isValidGuess(input)) {
            throw new IllegalArgumentException("Ungültige Eingabe.");
        }

        String guess = normalizeGuess(input);
        Evaluation evaluation = evaluateGuess(guess);

        RoundEntry entry = new RoundEntry(guess, evaluation);
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

    private Evaluation evaluateGuess(String guess) {
        boolean[] secretUsed = new boolean[CODE_LENGTH];
        boolean[] guessUsed = new boolean[CODE_LENGTH];

        int exactMatches = 0;
        int colorOnlyMatches = 0;

        for (int i = 0; i < CODE_LENGTH; i++) {
            if (guess.charAt(i) == secretCode.charAt(i)) {
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

                if (guess.charAt(i) == secretCode.charAt(j)) {
                    colorOnlyMatches++;
                    secretUsed[j] = true;
                    guessUsed[i] = true;
                    break;
                }
            }
        }

        return new Evaluation(exactMatches, colorOnlyMatches);
    }

    public List<RoundEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public int getRemainingAttempts() {
        return MAX_ATTEMPTS - attemptsUsed;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }
}