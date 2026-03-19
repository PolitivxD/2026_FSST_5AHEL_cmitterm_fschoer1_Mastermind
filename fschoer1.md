# Promt: #
So passt. Es hat jetzt eine erweiterung von Professor gegeben:
-Der Teil mit der Beschreibung des Spiels soll wegklappbar sein.
-Die Buchstaben sollen durch Formen ersetzt werden (Rechteck, Dreieck, ...). Mann kann diese jetzt nicht mehr eingeben sondern einzeln auswählen und diese dann bevor man die eingabe prüft noch mit drag und drop verschieben.

# Ausgabe: #
Ja, Christoph — hier ist die **komplette neue Version** mit:

* **TitledPane** für die wegklappbare Spielbeschreibung
* **Formen statt Buchstaben**
* **Formen einzeln auswählen**
* **4 Eingabeslots**
* **Drag & Drop zum Verschieben**
* weiterhin sauber nach **MVC**

Wichtig: Es gibt jetzt **eine zusätzliche Datei**:

* `ShapeType.java`

---

# Projektstruktur

```text
src
└─ main
   ├─ java
   │  ├─ module-info.java
   │  └─ org
   │     └─ example
   │        └─ mastermind
   │           ├─ MainApp.java
   │           ├─ controller
   │           │  └─ MastermindController.java
   │           ├─ model
   │           │  ├─ Evaluation.java
   │           │  ├─ MastermindModel.java
   │           │  ├─ RoundEntry.java
   │           │  └─ ShapeType.java
   │           └─ view
   │              └─ MastermindView.java
   └─ resources
```

---

# 1) `module-info.java`

```java
module org.example.mastermind {
    requires javafx.controls;

    exports org.example.mastermind;
}
```

---

# 2) `MainApp.java`

```java
package org.example.mastermind;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.mastermind.controller.MastermindController;
import org.example.mastermind.model.MastermindModel;
import org.example.mastermind.view.MastermindView;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        MastermindModel model = new MastermindModel();
        MastermindView view = new MastermindView();
        new MastermindController(model, view);

        Scene scene = new Scene(view, 1000, 780);

        stage.setTitle("Mastermind");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

---

# 3) `controller/MastermindController.java`

```java
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
        view.setOnGuessReordered(this::handleGuessReordered);
        view.setOnSubmit(event -> handleSubmit());
        view.setOnRestart(event -> startNewGame());
        view.setOnClearCurrentGuess(event -> clearCurrentGuess());
        view.setOnRemoveLast(event -> removeLastShape());

        startNewGame();
    }

    private void startNewGame() {
        model.startNewGame();
        resetCurrentGuess();

        view.resetBoard();
        view.renderCurrentGuess(currentGuess);
        view.updateRemainingAttempts(model.getRemainingAttempts());
        view.showInfo("Neues Spiel gestartet. Wähle 4 Formen aus und verschiebe sie bei Bedarf per Drag & Drop.");
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

        view.showError("Es sind bereits 4 Formen ausgewählt. Prüfe die Eingabe oder leere sie zuerst.");
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
```

---

# 4) `model/ShapeType.java`

```java
package org.example.mastermind.model;

public enum ShapeType {
    CIRCLE("Kreis"),
    RECTANGLE("Rechteck"),
    TRIANGLE("Dreieck"),
    DIAMOND("Raute"),
    STAR("Stern"),
    HEXAGON("Sechseck");

    private final String displayName;

    ShapeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

---

# 5) `model/Evaluation.java`

```java
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
```

---

# 6) `model/RoundEntry.java`

```java
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
```

---

# 7) `model/MastermindModel.java`

```java
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
}
```

---

# 8) `view/MastermindView.java`

```java
package org.example.mastermind.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.example.mastermind.model.Evaluation;
import org.example.mastermind.model.RoundEntry;
import org.example.mastermind.model.ShapeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MastermindView extends BorderPane {

    @FunctionalInterface
    public interface GuessReorderHandler {
        void handle(int fromIndex, int toIndex);
    }

    private final Label remainingAttemptsLabel = new Label();
    private final Label messageLabel = new Label();

    private final Button submitButton = new Button("Versuch prüfen");
    private final Button restartButton = new Button("Neues Spiel");
    private final Button clearCurrentGuessButton = new Button("Eingabe leeren");
    private final Button removeLastButton = new Button("Letzte Form löschen");

    private final GridPane boardGrid = new GridPane();

    private final List<HBox> guessRows = new ArrayList<>();
    private final List<HBox> feedbackRows = new ArrayList<>();

    private final List<Button> shapeButtons = new ArrayList<>();
    private final List<StackPane> currentGuessSlots = new ArrayList<>();
    private List<ShapeType> currentGuessState = new ArrayList<>();

    private Consumer<ShapeType> shapeSelectedHandler;
    private GuessReorderHandler guessReorderHandler;

    public MastermindView() {
        createLayout();
    }

    private void createLayout() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Mastermind");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        TitledPane descriptionPane = new TitledPane("Spielbeschreibung", createDescriptionBox());
        descriptionPane.setExpanded(false);

        messageLabel.setWrapText(true);
        messageLabel.setMinHeight(28);

        VBox topBox = new VBox(10, titleLabel, remainingAttemptsLabel, descriptionPane, messageLabel);
        topBox.setPadding(new Insets(0, 0, 20, 0));

        createBoard();

        VBox bottomBox = new VBox(
                16,
                createShapeSelectionBox(),
                createCurrentGuessBox(),
                createButtonBox()
        );
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        setTop(topBox);
        setCenter(boardGrid);
        setBottom(bottomBox);
    }

    private VBox createDescriptionBox() {
        Label line1 = new Label("• Der Computer erzeugt einen geheimen Code aus 4 Formen.");
        Label line2 = new Label("• Verfügbare Formen: Kreis, Rechteck, Dreieck, Raute, Stern, Sechseck.");
        Label line3 = new Label("• Formen dürfen sich wiederholen.");
        Label line4 = new Label("• Du hast 10 Versuche.");
        Label line5 = new Label("• Schwarzer Punkt: richtige Form an richtiger Position.");
        Label line6 = new Label("• Weißer Punkt: richtige Form, aber falsche Position.");
        Label line7 = new Label("• Wähle unten 4 Formen aus und verschiebe sie bei Bedarf per Drag & Drop.");

        VBox box = new VBox(6, line1, line2, line3, line4, line5, line6, line7);
        box.setPadding(new Insets(8));
        return box;
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

        return new VBox(8, selectionLabel, buttonPane);
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

        return button;
    }

    private VBox createCurrentGuessBox() {
        Label guessLabel = new Label("Aktuelle Eingabe (per Drag & Drop verschiebbar):");
        guessLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        HBox slotBox = new HBox(12);
        slotBox.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < 4; i++) {
            StackPane slot = createCurrentGuessSlot(i);
            currentGuessSlots.add(slot);
            slotBox.getChildren().add(slot);
        }

        Label hintLabel = new Label("Zuerst 4 Formen auswählen, danach kannst du sie noch umordnen.");

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
            content.putString(String.valueOf(slotIndex));
            dragboard.setContent(content);

            event.consume();
        });

        slot.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        slot.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString() && guessReorderHandler != null) {
                int fromIndex = Integer.parseInt(dragboard.getString());
                guessReorderHandler.handle(fromIndex, slotIndex);
                success = true;
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
        placeholder.setStyle("-fx-border-color: #c7c7c7; -fx-background-color: #fafafa;");
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
            cell.setStyle("-fx-border-color: #bdbdbd; -fx-background-color: #fcfcfc;");
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

    public void setOnShapeSelected(Consumer<ShapeType> handler) {
        this.shapeSelectedHandler = handler;
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
}
```

---

# 9) `pom.xml`

Deine korrigierte `pom.xml` kann so bleiben:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>Mastermind</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Mastermind</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.0</junit.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21</version>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <executions>
                    <execution>
                        <id>default-cli</id>
                        <configuration>
                            <mainClass>org.example.mastermind/org.example.mastermind.MainApp</mainClass>
                            <launcher>app</launcher>
                            <jlinkZipName>app</jlinkZipName>
                            <jlinkImageName>app</jlinkImageName>
                            <noManPages>true</noManPages>
                            <stripDebug>true</stripDebug>
                            <noHeaderFiles>true</noHeaderFiles>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Wichtig

Du brauchst jetzt **neu**:

* `ShapeType.java`

Und diese alten Dateien brauchst du weiterhin **nicht**:

* `HelloApplication.java`
* `HelloController.java`
* `hello-view.fxml`

---

## Falls etwas rot wird

Dann schick mir einfach den **ersten roten Fehler** oder einen Screenshot davon.



