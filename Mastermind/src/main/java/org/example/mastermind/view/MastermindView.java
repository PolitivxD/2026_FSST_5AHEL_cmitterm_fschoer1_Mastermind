package org.example.mastermind.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.mastermind.model.Evaluation;
import org.example.mastermind.model.RoundEntry;

import java.util.ArrayList;
import java.util.List;

public class MastermindView extends BorderPane {

    private final Label remainingAttemptsLabel = new Label();
    private final Label messageLabel = new Label();

    private final TextField guessField = new TextField();
    private final Button submitButton = new Button("Versuch prüfen");
    private final Button restartButton = new Button("Neues Spiel");

    private final GridPane boardGrid = new GridPane();

    private final List<HBox> guessRows = new ArrayList<>();
    private final List<HBox> feedbackRows = new ArrayList<>();

    public MastermindView() {
        createLayout();
    }

    private void createLayout() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Mastermind");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Label colorsLabel = new Label("Farben: R = Rot, G = Grün, B = Blau, Y = Gelb, O = Orange, P = Pink");
        Label inputInfoLabel = new Label("Eingabe: RGBY oder R G B Y");

        messageLabel.setWrapText(true);
        messageLabel.setMinHeight(30);

        VBox topBox = new VBox(8, titleLabel, remainingAttemptsLabel, colorsLabel, inputInfoLabel, messageLabel);
        topBox.setPadding(new Insets(0, 0, 20, 0));

        createBoard();

        Label inputLabel = new Label("Code:");
        guessField.setPromptText("z. B. R G B Y");
        guessField.setPrefWidth(180);

        HBox bottomBox = new HBox(10, inputLabel, guessField, submitButton, restartButton);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        setTop(topBox);
        setCenter(boardGrid);
        setBottom(bottomBox);
    }

    private void createBoard() {
        boardGrid.setHgap(20);
        boardGrid.setVgap(12);
        boardGrid.setAlignment(Pos.TOP_LEFT);

        boardGrid.add(createHeaderLabel("Nr."), 0, 0);
        boardGrid.add(createHeaderLabel("Versuch"), 1, 0);
        boardGrid.add(createHeaderLabel("Auswertung"), 2, 0);

        for (int i = 0; i < 10; i++) {
            Label numberLabel = new Label((i + 1) + ".");
            numberLabel.setStyle("-fx-font-weight: bold;");

            HBox guessBox = new HBox(8);
            guessBox.setAlignment(Pos.CENTER_LEFT);

            HBox feedbackBox = new HBox(6);
            feedbackBox.setAlignment(Pos.CENTER_LEFT);

            guessRows.add(guessBox);
            feedbackRows.add(feedbackBox);

            fillGuessRowWithPlaceholders(guessBox);
            fillFeedbackRowWithPlaceholders(feedbackBox);

            boardGrid.add(numberLabel, 0, i + 1);
            boardGrid.add(guessBox, 1, i + 1);
            boardGrid.add(feedbackBox, 2, i + 1);
        }
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return label;
    }

    private void fillGuessRowWithPlaceholders(HBox box) {
        box.getChildren().clear();

        for (int i = 0; i < 4; i++) {
            box.getChildren().add(createGuessCircle(' ', Color.LIGHTGRAY));
        }
    }

    private void fillFeedbackRowWithPlaceholders(HBox box) {
        box.getChildren().clear();

        for (int i = 0; i < 4; i++) {
            box.getChildren().add(createEmptyFeedbackPeg());
        }
    }

    private Node createGuessCircle(char letter, Color color) {
        Circle circle = new Circle(17);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);

        Label label = new Label(letter == ' ' ? "" : String.valueOf(letter));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        return new StackPane(circle, label);
    }

    private Node createBlackFeedbackPeg() {
        Circle circle = new Circle(7);
        circle.setFill(Color.BLACK);
        circle.setStroke(Color.BLACK);
        return circle;
    }

    private Node createWhiteFeedbackPeg() {
        Circle circle = new Circle(7);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        return circle;
    }

    private Node createEmptyFeedbackPeg() {
        Circle circle = new Circle(7);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.LIGHTGRAY);
        return circle;
    }

    private Color getColorForCode(char code) {
        return switch (code) {
            case 'R' -> Color.RED;
            case 'G' -> Color.LIMEGREEN;
            case 'B' -> Color.DODGERBLUE;
            case 'Y' -> Color.GOLD;
            case 'O' -> Color.ORANGE;
            case 'P' -> Color.HOTPINK;
            default -> Color.LIGHTGRAY;
        };
    }

    public void updateBoard(List<RoundEntry> history) {
        for (int row = 0; row < 10; row++) {
            if (row < history.size()) {
                RoundEntry entry = history.get(row);
                updateGuessRow(row, entry.getGuess());
                updateFeedbackRow(row, entry.getEvaluation());
            } else {
                fillGuessRowWithPlaceholders(guessRows.get(row));
                fillFeedbackRowWithPlaceholders(feedbackRows.get(row));
            }
        }
    }

    private void updateGuessRow(int rowIndex, String guess) {
        HBox row = guessRows.get(rowIndex);
        row.getChildren().clear();

        for (int i = 0; i < guess.length(); i++) {
            char code = guess.charAt(i);
            row.getChildren().add(createGuessCircle(code, getColorForCode(code)));
        }
    }

    private void updateFeedbackRow(int rowIndex, Evaluation evaluation) {
        HBox row = feedbackRows.get(rowIndex);
        row.getChildren().clear();

        int exact = evaluation.getExactMatches();
        int colorOnly = evaluation.getColorOnlyMatches();
        int empty = 4 - exact - colorOnly;

        for (int i = 0; i < exact; i++) {
            row.getChildren().add(createBlackFeedbackPeg());
        }

        for (int i = 0; i < colorOnly; i++) {
            row.getChildren().add(createWhiteFeedbackPeg());
        }

        for (int i = 0; i < empty; i++) {
            row.getChildren().add(createEmptyFeedbackPeg());
        }
    }

    public void resetBoard() {
        for (int i = 0; i < 10; i++) {
            fillGuessRowWithPlaceholders(guessRows.get(i));
            fillFeedbackRowWithPlaceholders(feedbackRows.get(i));
        }
    }

    public void updateRemainingAttempts(int remainingAttempts) {
        remainingAttemptsLabel.setText("Verbleibende Versuche: " + remainingAttempts);
    }

    public void showInfo(String message) {
        messageLabel.setStyle("-fx-text-fill: #00695c; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    public void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #b71c1c; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    public void showWinMessage(String message) {
        messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    public void showLoseMessage(String message) {
        messageLabel.setStyle("-fx-text-fill: #8e24aa; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    public String getGuessInput() {
        return guessField.getText();
    }

    public void clearGuessInput() {
        guessField.clear();
    }

    public void enableInput(boolean enabled) {
        guessField.setDisable(!enabled);
        submitButton.setDisable(!enabled);

        if (enabled) {
            guessField.requestFocus();
        }
    }

    public void setOnSubmit(EventHandler<ActionEvent> handler) {
        submitButton.setOnAction(handler);
        guessField.setOnAction(handler);
    }

    public void setOnRestart(EventHandler<ActionEvent> handler) {
        restartButton.setOnAction(handler);
    }
}