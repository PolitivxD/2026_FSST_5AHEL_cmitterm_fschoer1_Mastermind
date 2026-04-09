package org.example.mastermind.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.mastermind.model.Evaluation;
import org.example.mastermind.model.RoundEntry;
import org.example.mastermind.model.ShapeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MastermindView extends BorderPane {

    public void showDifficultySelectionDialog() {
    }

    @FunctionalInterface
    public interface GuessReorderHandler {
        void handle(int fromIndex, int toIndex);
    }

    @FunctionalInterface
    public interface ShapeDropToGuessHandler {
        void handle(ShapeType shapeType, int targetIndex);
    }

    private final Label remainingAttemptsLabel = new Label();
    private final Label messageLabel = new Label();

    private final Button descriptionButton = new Button("Erklärung / Beschreibung");
    private final Button submitButton = new Button("Versuch prüfen");
    private final Button restartButton = new Button("Neues Spiel");
    private final Button clearCurrentGuessButton = new Button("Eingabe leeren");
    private final Button removeLastButton = new Button("Letzte Form löschen");

    private final Button easyButton = new Button("EASY");
    private final Button middleButton = new Button("MIDDLE");
    private final Button hardButton = new Button("HARD");

    private final HBox schwierigkeitsbox = new HBox(10);
    private final Label schwierigkeitslabel = new Label("Schwierigkeitsgrad:");

    private final GridPane boardGrid = new GridPane();

    private final TableView<ScoreEntry> scoreboardTable = new TableView<>();



    private final List<HBox> guessRows = new ArrayList<>();
    private final List<HBox> feedbackRows = new ArrayList<>();

    private final List<Button> shapeButtons = new ArrayList<>();
    private final List<StackPane> currentGuessSlots = new ArrayList<>();
    private List<ShapeType> currentGuessState = new ArrayList<>();

    private Consumer<ShapeType> shapeSelectedHandler;
    private GuessReorderHandler guessReorderHandler;
    private ShapeDropToGuessHandler shapeDropToGuessHandler;

    public MastermindView() {
        createLayout();
    }

    private void createLayout() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Mastermind");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        HBox titleBox = new HBox(12, titleLabel, descriptionButton);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        messageLabel.setWrapText(true);
        messageLabel.setMinHeight(28);

        VBox topBox = new VBox(10, titleBox, remainingAttemptsLabel, messageLabel);
        topBox.setPadding(new Insets(0, 0, 20, 0));

        createBoard();

        VBox bottomBox = new VBox(
                16,
                setSchwierigkeitsbox(actionEvent -> setOnMouseClicked(event -> showDifficultySelectionDialog())),
                createShapeSelectionBox(),
                createCurrentGuessBox(),
                createButtonBox()
        );
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        setTop(topBox);
        setCenter(boardGrid);
        setRight(createScoreboardBox());
        setBottom(bottomBox);
    }

    private VBox createScoreboardBox() {
        Label scoreboardLabel = new Label("Scoreboard");
        scoreboardLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableColumn<ScoreEntry, Integer> rankColumn = new TableColumn<>("Platz");
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));

        TableColumn<ScoreEntry, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ScoreEntry, Integer> attemptsColumn = new TableColumn<>("Versuche");
        attemptsColumn.setCellValueFactory(new PropertyValueFactory<>("attempts"));

        scoreboardTable.getColumns().clear();
        scoreboardTable.getColumns().add(rankColumn);
        scoreboardTable.getColumns().add(nameColumn);
        scoreboardTable.getColumns().add(attemptsColumn);

        scoreboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scoreboardTable.setPrefWidth(300);
        scoreboardTable.setPrefHeight(420);

        VBox scoreboardBox = new VBox(10, scoreboardLabel, scoreboardTable);
        scoreboardBox.setPadding(new Insets(0, 0, 0, 20));

        return scoreboardBox;
    }

    public void updateScoreboard(List<ScoreEntry> scores) {
        scoreboardTable.getItems().setAll(scores);
    }

    public void showDescriptionDialog() {
        Stage dialog = new Stage();

        if (getScene() != null && getScene().getWindow() != null) {
            dialog.initOwner(getScene().getWindow());
        }

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Spielbeschreibung");

        Label line1 = new Label("• Der Computer erzeugt einen geheimen Code aus 4 Formen.");
        Label line2 = new Label("• Verfügbare Formen: Kreis, Rechteck, Dreieck, Raute, Stern, Sechseck.");
        Label line3 = new Label("• Formen dürfen sich wiederholen.");
        Label line4 = new Label("• Du hast 10 Versuche.");
        Label line5 = new Label("• Schwarzer Punkt: richtige Form an richtiger Position.");
        Label line6 = new Label("• Weißer Punkt: richtige Form, aber falsche Position.");
        Label line7 = new Label("• Formen können angeklickt oder per Drag & Drop in den Prüfbereich gezogen werden.");
        Label line8 = new Label("• Im Prüfbereich können Formen vor dem Prüfen noch per Drag & Drop umgeordnet werden.");

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setOnAction(event -> dialog.close());

        VBox root = new VBox(10, line1, line2, line3, line4, line5, line6, line7, line8, okButton);
        root.setPadding(new Insets(18));
        root.setAlignment(Pos.TOP_LEFT);

        Scene scene = new Scene(root, 620, 290);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private VBox createShapeSelectionBox() {
        Label selectionLabel = new Label("Form auswählen:");
        selectionLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        FlowPane buttonPane = new FlowPane();
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);

        for (ShapeType shapeType : ShapeType.values()) {
            Button button = createShapeButton(shapeType);
            shapeButtons.add(button);
            buttonPane.getChildren().add(button);
        }

        Label hint = new Label("Klick = nächste freie Stelle füllen | Ziehen = direkt in einen Zielplatz ziehen");

        return new VBox(8, selectionLabel, buttonPane, hint);
    }

    private Button createShapeButton(ShapeType shapeType) {
        Button button = new Button();
        button.setPrefSize(90, 70);
        button.setGraphic(createShapeGraphic(shapeType, 34));
        button.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #bdbdbd;");

        button.setOnAction(event -> {
            if (shapeSelectedHandler != null) {
                shapeSelectedHandler.accept(shapeType);
            }
        });

        button.setOnDragDetected(event -> {
            Dragboard dragboard = button.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString("PALETTE:" + shapeType.name());
            dragboard.setContent(content);

            WritableImage image = button.snapshot(new SnapshotParameters(), null);
            dragboard.setDragView(image, image.getWidth() / 2, image.getHeight() / 2);

            event.consume();
        });

        return button;
    }

    private VBox createCurrentGuessBox() {
        Label guessLabel = new Label("Prüfbereich:");
        guessLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        HBox slotBox = new HBox(12);
        slotBox.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < 4; i++) {
            StackPane slot = createCurrentGuessSlot(i);
            currentGuessSlots.add(slot);
            slotBox.getChildren().add(slot);
        }

        Label hintLabel = new Label("Du kannst Formen direkt aus der Auswahl hier hineinziehen oder vorhandene Formen umordnen.");

        return new VBox(8, guessLabel, slotBox, hintLabel);
    }

    private StackPane createCurrentGuessSlot(int slotIndex) {
        StackPane slot = new StackPane();
        slot.setPrefSize(80, 80);
        slot.setMinSize(80, 80);
        slot.setMaxSize(80, 80);
        slot.setStyle("-fx-border-color: #616161; -fx-border-width: 2; -fx-background-color: #fafafa;");

        slot.setOnDragDetected(event -> {
            if (slotIndex >= currentGuessState.size()) {
                return;
            }

            if (currentGuessState.get(slotIndex) == null) {
                return;
            }

            Dragboard dragboard = slot.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("SLOT:" + slotIndex);
            dragboard.setContent(content);

            WritableImage image = slot.snapshot(new SnapshotParameters(), null);
            dragboard.setDragView(image, image.getWidth() / 2, image.getHeight() / 2);

            slot.setOpacity(0.35);
            event.consume();
        });

        slot.setOnDragDone(event -> slot.setOpacity(1.0));

        slot.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString()) {
                String value = dragboard.getString();

                if (value.startsWith("PALETTE:") || value.startsWith("SLOT:")) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            }

            event.consume();
        });

        slot.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString()) {
                String value = dragboard.getString();

                if (value.startsWith("PALETTE:")) {
                    String shapeName = value.substring("PALETTE:".length());
                    ShapeType shapeType = ShapeType.valueOf(shapeName);

                    if (shapeDropToGuessHandler != null) {
                        shapeDropToGuessHandler.handle(shapeType, slotIndex);
                        success = true;
                    }
                } else if (value.startsWith("SLOT:")) {
                    int fromIndex = Integer.parseInt(value.substring("SLOT:".length()));

                    if (guessReorderHandler != null) {
                        guessReorderHandler.handle(fromIndex, slotIndex);
                        success = true;
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        return slot;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10, submitButton, clearCurrentGuessButton, removeLastButton, restartButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        return buttonBox;
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
            box.getChildren().add(createBoardGuessPlaceholder());
        }
    }

    private Node createBoardGuessPlaceholder() {
        StackPane placeholder = new StackPane();
        placeholder.setPrefSize(54, 54);
        placeholder.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
        return placeholder;
    }

    private void fillFeedbackRowWithPlaceholders(HBox box) {
        box.getChildren().clear();

        for (int i = 0; i < 4; i++) {
            box.getChildren().add(createEmptyFeedbackPeg());
        }
    }

    public void renderCurrentGuess(List<ShapeType> guess) {
        currentGuessState = new ArrayList<>(guess);

        for (int i = 0; i < currentGuessSlots.size(); i++) {
            StackPane slot = currentGuessSlots.get(i);
            slot.getChildren().clear();
            slot.setOpacity(1.0);

            ShapeType shapeType = guess.get(i);

            if (shapeType == null) {
                Label placeholder = new Label(String.valueOf(i + 1));
                placeholder.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 18px; -fx-font-weight: bold;");
                slot.getChildren().add(placeholder);
            } else {
                slot.getChildren().add(createShapeGraphic(shapeType, 42));
            }
        }
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

    private void updateGuessRow(int rowIndex, List<ShapeType> guess) {
        HBox row = guessRows.get(rowIndex);
        row.getChildren().clear();

        for (ShapeType shapeType : guess) {
            StackPane cell = new StackPane();
            cell.setPrefSize(54, 54);
            cell.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
            cell.getChildren().add(createShapeGraphic(shapeType, 28));
            row.getChildren().add(cell);
        }
    }

    private void updateFeedbackRow(int rowIndex, Evaluation evaluation) {
        HBox row = feedbackRows.get(rowIndex);
        row.getChildren().clear();

        int exact = evaluation.getExactMatches();
        int shapeOnly = evaluation.getShapeOnlyMatches();
        int empty = 4 - exact - shapeOnly;

        for (int i = 0; i < exact; i++) {
            row.getChildren().add(createBlackFeedbackPeg());
        }

        for (int i = 0; i < shapeOnly; i++) {
            row.getChildren().add(createWhiteFeedbackPeg());
        }

        for (int i = 0; i < empty; i++) {
            row.getChildren().add(createEmptyFeedbackPeg());
        }
    }

    private Node createShapeGraphic(ShapeType shapeType, double size) {
        Shape shape = switch (shapeType) {
            case CIRCLE -> createCircleShape(size);
            case RECTANGLE -> createRectangleShape(size);
            case TRIANGLE -> createTriangleShape(size);
            case DIAMOND -> createDiamondShape(size);
            case STAR -> createStarShape(size);
            case HEXAGON -> createHexagonShape(size);
            case ELLIPSE -> createEllipseShape(size);
            case LINE -> createLineShape(size);
        };

        shape.setFill(Color.web("#7ec8e3"));
        shape.setStroke(Color.BLACK);
        shape.setStrokeWidth(2);

        StackPane pane = new StackPane(shape);
        pane.setPrefSize(size + 14, size + 14);
        return pane;
    }

    private Shape createCircleShape(double size) {
        return new Circle(size * 0.32);
    }

    private Shape createRectangleShape(double size) {
        Rectangle rectangle = new Rectangle(size * 0.72, size * 0.48);
        rectangle.setArcWidth(8);
        rectangle.setArcHeight(8);
        return rectangle;
    }

    private Shape createLineShape(double size) {

      Line line = new Line(size * -0.36, size * 0.36, size * 0.36, size * -0.36);
      line.setStrokeWidth(4);

        return line;
    }

    private Shape createEllipseShape(double size) {

        Ellipse ellipse = new Ellipse(size * 0.52, size * 0.38);
        ellipse.setFill(Color.BLUE);

        return ellipse;
    }

    private Shape createTriangleShape(double size) {
        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(
                0.0, -size * 0.38,
                -size * 0.36, size * 0.28,
                size * 0.36, size * 0.28
        );
        return triangle;
    }

    private Shape createDiamondShape(double size) {
        Polygon diamond = new Polygon();
        diamond.getPoints().addAll(
                0.0, -size * 0.38,
                size * 0.30, 0.0,
                0.0, size * 0.38,
                -size * 0.30, 0.0
        );
        return diamond;
    }

    private Shape createHexagonShape(double size) {
        Polygon hexagon = new Polygon();
        double radius = size * 0.34;

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            hexagon.getPoints().add(radius * Math.cos(angle));
            hexagon.getPoints().add(radius * Math.sin(angle));
        }

        return hexagon;
    }

    private Shape createStarShape(double size) {
        Polygon star = new Polygon();

        double outerRadius = size * 0.34;
        double innerRadius = size * 0.15;

        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = Math.toRadians(-90 + i * 36);
            star.getPoints().add(radius * Math.cos(angle));
            star.getPoints().add(radius * Math.sin(angle));
        }

        return star;
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

    public void resetBoard() {
        for (int i = 0; i < 10; i++) {
            fillGuessRowWithPlaceholders(guessRows.get(i));
            fillFeedbackRowWithPlaceholders(feedbackRows.get(i));
        }
    }

    public void updateRemainingAttempts(int remainingAttempts) {
        remainingAttemptsLabel.setText("Verbleibende Versuche: " + remainingAttempts);
        remainingAttemptsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
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

    public void enableInput(boolean enabled) {
        submitButton.setDisable(!enabled);
        clearCurrentGuessButton.setDisable(!enabled);
        removeLastButton.setDisable(!enabled);

        for (Button button : shapeButtons) {
            button.setDisable(!enabled);
        }
    }

    public HBox setSchwierigkeitsbox(EventHandler<ActionEvent> handler) {
        easyButton.setOnAction(handler);
        middleButton.setOnAction(handler);
        hardButton.setOnAction(handler);

        schwierigkeitsbox.getChildren().addAll(schwierigkeitslabel, easyButton, middleButton, hardButton);
        schwierigkeitsbox.setAlignment(Pos.CENTER_LEFT);
        schwierigkeitsbox.setPadding(new Insets(0, 0, 12, 0));


        return schwierigkeitsbox;
    }

    public int setSchwierigkeitsbox2() {
        int i=4;
        if(easyButton.isPressed()){
            i=4;
        }else if(middleButton.isPressed()){
            i=5;
        }else if(hardButton.isPressed()){
            i=6;
        }
        return i;
    }

    public void setOnShapeSelected(Consumer<ShapeType> handler) {
        this.shapeSelectedHandler = handler;
    }

    public void setOnShapeDroppedToGuess(ShapeDropToGuessHandler handler) {
        this.shapeDropToGuessHandler = handler;
    }

    public void setOnGuessReordered(GuessReorderHandler handler) {
        this.guessReorderHandler = handler;
    }

    public void setOnSubmit(EventHandler<ActionEvent> handler) {
        submitButton.setOnAction(handler);
    }

    public void setOnRestart(EventHandler<ActionEvent> handler) {
        restartButton.setOnAction(handler);
    }

    public void setOnClearCurrentGuess(EventHandler<ActionEvent> handler) {
        clearCurrentGuessButton.setOnAction(handler);
    }

    public void setOnRemoveLast(EventHandler<ActionEvent> handler) {
        removeLastButton.setOnAction(handler);
    }

    public void setOnShowDescription(EventHandler<ActionEvent> handler) {
        descriptionButton.setOnAction(handler);
    }

    public static class ScoreEntry {
        private final int rank;
        private final String name;
        private final int attempts;

        public ScoreEntry(int rank, String name, int attempts) {
            this.rank = rank;
            this.name = name;
            this.attempts = attempts;
        }

        public int getRank() {
            return rank;
        }

        public String getName() {
            return name;
        }

        public int getAttempts() {
            return attempts;
        }
    }
}