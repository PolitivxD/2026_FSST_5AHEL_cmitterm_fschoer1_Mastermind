package org.example.mastermind.controller;

import org.example.mastermind.model.MastermindModel;
import org.example.mastermind.model.ShapeType;
import org.example.mastermind.view.MastermindView;

import java.util.ArrayList;
import java.util.List;

public class MastermindController {

    private final MastermindModel model;
    private final MastermindView view;

    private final List<ShapeType> currentGuess = new ArrayList<>();

    public MastermindController(MastermindModel model, MastermindView view) {
        this.model = model;
        this.view = view;

        view.setOnShapeSelected(this::handleShapeSelected);
        view.setOnShapeDroppedToGuess(this::handleShapeDroppedToGuess);
        view.setOnGuessReordered(this::handleGuessReordered);
        view.setOnSubmit(event -> handleSubmit());
        view.setOnRestart(event -> startNewGame());
        view.setOnClearCurrentGuess(event -> clearCurrentGuess());
        view.setOnRemoveLast(event -> removeLastShape());
        view.setOnShowDescription(event -> view.showDescriptionDialog());

        startNewGame();
    }

    private void startNewGame() {
        model.startNewGame();
        resetCurrentGuess();

        view.resetBoard();
        view.renderCurrentGuess(currentGuess);
        view.updateRemainingAttempts(model.getRemainingAttempts());
        view.showInfo("Neues Spiel gestartet. Wähle 4 Formen aus oder ziehe sie direkt in den Prüfbereich.");
        view.enableInput(true);
    }

    private void resetCurrentGuess() {
        currentGuess.clear();

        for (int i = 0; i < MastermindModel.CODE_LENGTH; i++) {
            currentGuess.add(null);
        }

    }

    private void handleShapeSelected(ShapeType shapeType) {
        for (int i = 0; i < currentGuess.size(); i++) {
            if (currentGuess.get(i) == null) {
                currentGuess.set(i, shapeType);
                view.renderCurrentGuess(currentGuess);
                return;
            }
        }

        view.showError("Es sind bereits 4 Formen ausgewählt. Prüfen sie die Eingabe oder leere sie zuerst.");
    }

    private void handleShapeDroppedToGuess(ShapeType shapeType, int targetIndex) {
        if (targetIndex < 0 || targetIndex >= currentGuess.size()) {
            return;

        }

        currentGuess.set(targetIndex, shapeType);
        view.renderCurrentGuess(currentGuess);
    }

    private void handleGuessReordered(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
            return;
        }

        if (fromIndex < 0 || fromIndex >= currentGuess.size()) {
            return;
        }

        if (toIndex < 0 || toIndex >= currentGuess.size()) {
            return;
        }

        ShapeType fromShape = currentGuess.get(fromIndex);
        ShapeType toShape = currentGuess.get(toIndex);

        if (fromShape == null) {
            return;
        }

        currentGuess.set(toIndex, fromShape);
        currentGuess.set(fromIndex, toShape);

        view.renderCurrentGuess(currentGuess);
    }

    private void clearCurrentGuess() {
        resetCurrentGuess();
        view.renderCurrentGuess(currentGuess);
        view.showInfo("Die aktuelle Eingabe wurde geleert.");
    }

    private void removeLastShape() {
        for (int i = currentGuess.size() - 1; i >= 0; i--) {
            if (currentGuess.get(i) != null) {
                currentGuess.set(i, null);
                view.renderCurrentGuess(currentGuess);
                return;
            }
        }

        view.showInfo("Es ist noch keine Form ausgewählt.");
    }

    private void handleSubmit() {
        if (!model.isValidGuess(currentGuess)) {
            view.showError("Die Eingabe ist noch nicht vollständig. Bitte wähle 4 Formen aus.");
            return;
        }

        model.makeGuess(currentGuess);

        view.updateBoard(model.getHistory());
        view.updateRemainingAttempts(model.getRemainingAttempts());

        resetCurrentGuess();
        view.renderCurrentGuess(currentGuess);

        if (model.isWon()) {
            view.showWinMessage("Gewonnen! Du hast den geheimen Code richtig erraten.");
            view.enableInput(false);
            return;
        }

        if (model.isGameOver()) {
            view.showLoseMessage("Keine Versuche mehr. Geheimer Code: " + formatSecretCode(model.getSecretCode()));
            view.enableInput(false);
            return;
        }

        view.showInfo("Versuch gespeichert. Verbleibende Versuche: " + model.getRemainingAttempts());
    }

    private String formatSecretCode(List<ShapeType> secretCode) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < secretCode.size(); i++) {
            builder.append(secretCode.get(i).getDisplayName());

            if (i < secretCode.size() - 1) {
                builder.append(" - ");
            }
        }

        return builder.toString();
    }
}