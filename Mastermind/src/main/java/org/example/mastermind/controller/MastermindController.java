package org.example.mastermind.controller;

import org.example.mastermind.model.MastermindModel;
import org.example.mastermind.view.MastermindView;

public class MastermindController {

    private final MastermindModel model;
    private final MastermindView view;

    public MastermindController(MastermindModel model, MastermindView view) {
        this.model = model;
        this.view = view;

        view.setOnSubmit(event -> handleSubmit());
        view.setOnRestart(event -> startNewGame());

        startNewGame();
    }

    private void startNewGame() {
        model.startNewGame();
        view.resetBoard();
        view.updateRemainingAttempts(model.getRemainingAttempts());
        view.showInfo("Neues Spiel gestartet. Gib 4 Farben ein, z. B. R G B Y.");
        view.clearGuessInput();
        view.enableInput(true);
    }

    private void handleSubmit() {
        String input = view.getGuessInput();

        if (!model.isValidGuess(input)) {
            view.showError("Ungültige Eingabe. Erlaubt sind genau 4 Buchstaben aus R, G, B, Y, O, P.");
            return;
        }

        model.makeGuess(input);

        view.updateBoard(model.getHistory());
        view.updateRemainingAttempts(model.getRemainingAttempts());
        view.clearGuessInput();

        if (model.isWon()) {
            view.showWinMessage("Gewonnen! Du hast den geheimen Code geknackt.");
            view.enableInput(false);
            return;
        }

        if (model.isGameOver()) {
            view.showLoseMessage("Keine Versuche mehr. Geheimer Code: " + formatCode(model.getSecretCode()));
            view.enableInput(false);
            return;
        }

        view.showInfo("Versuch gespeichert. Verbleibende Versuche: " + model.getRemainingAttempts());
    }

    private String formatCode(String code) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < code.length(); i++) {
            builder.append(code.charAt(i));
            if (i < code.length() - 1) {
                builder.append(' ');
            }
        }

        return builder.toString();
    }
}