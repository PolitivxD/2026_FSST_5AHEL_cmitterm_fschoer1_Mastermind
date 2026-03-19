# 2026_FSST_5AHEL_cmitterm_fschoer1_Mastermind


## Promt: ##
Gut so, bitte arbeite genau die Aufgabenstellung ab und gib mir den gesammten Code. Verwende leichtere Funktionen bzw schreibe das Programm verständlich.

## Ausgabe: ##
Ja, Christoph. Hier ist alles **1:1 zum Reinkopieren** für **deine Struktur** mit

* `org.example.mastermind`
* `controller`
* `model`
* `view`

Vorher noch kurz:

Lösche diese alten Dateien:

* `HelloApplication.java`
* `HelloController.java`
* `hello-view.fxml`

---

# 1) `src/main/java/module-info.java`

```java
module org.example.mastermind {
    requires javafx.controls;

    exports org.example.mastermind;
}
```

---

# 2) `src/main/java/org/example/mastermind/MainApp.java`

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

        Scene scene = new Scene(view, 900, 650);

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

# 3) `src/main/java/org/example/mastermind/controller/MastermindController.java`

```java
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
```

---

# 4) `src/main/java/org/example/mastermind/model/Evaluation.java`

```java
package org.example.mastermind.model;

public class Evaluation {

    private final int exactMatches;
    private final int colorOnlyMatches;

    public Evaluation(int exactMatches, int colorOnlyMatches) {
        this.exactMatches = exactMatches;
        this.colorOnlyMatches = colorOnlyMatches;
    }

    public int getExactMatches() {
        return exactMatches;
    }

    public int getColorOnlyMatches() {
        return colorOnlyMatches;
    }
}
```

---

# 5) `src/main/java/org/example/mastermind/model/RoundEntry.java`

```java
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
```

---

# 6) `src/main/java/org/example/mastermind/model/MastermindModel.java`

```java
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
```

---

# 7) `src/main/java/org/example/mastermind/view/MastermindView.java`

```java
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
```

---

# 8) `pom.xml`

Falls dein `pom.xml` noch auf `HelloApplication` zeigt, ersetze ihn komplett durch das hier:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>mastermind</artifactId>
    <version>1.0-SNAPSHOT</version>
    <name>Mastermind</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.2</junit.version>
        <javafx.version>21.0.2</javafx.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
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
                <configuration>
                    <mainClass>org.example.mastermind/org.example.mastermind.MainApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

# Danach

1. Alte Hello-Dateien löschen
2. Alles oben einfügen
3. Projekt neu laden
4. Starten über `MainApp` oder Maven

Wenn beim Einfügen irgendwo rot unterstrichen wird, schick mir einfach den Fehler oder Screenshot.


------

## Promt: ##
Erkläre mir kurz was jede einzelnde Datei Macht

## Antwort: ##
Ja, Christoph.

## `MainApp.java`

Startet das ganze Programm.

* erstellt **Model**
* erstellt **View**
* erstellt **Controller**
* öffnet das JavaFX-Fenster

Also: **Einstiegspunkt des Programms**

---

## `MastermindController.java`

Steuert den Ablauf des Spiels.

* reagiert auf Button-Klicks
* holt die Eingabe aus der View
* lässt das Model prüfen und auswerten
* sagt der View, was angezeigt werden soll
* prüft Gewinn oder Spielende

Also: **Verbindung zwischen View und Model**

---

## `MastermindModel.java`

Enthält die komplette Spiellogik.

* erzeugt den geheimen Farbcode
* prüft, ob eine Eingabe gültig ist
* wertet einen Versuch aus
* zählt die benutzten und übrigen Versuche
* merkt sich, ob gewonnen wurde oder das Spiel vorbei ist

Also: **hier passiert die eigentliche Logik**

---

## `Evaluation.java`

Speichert das Ergebnis eines Versuchs.

* wie viele Farben **richtig und an richtiger Stelle** sind
* wie viele Farben **richtig, aber an falscher Stelle** sind

Also: **ein kleines Ergebnis-Objekt für die Rückmeldung**

---

## `RoundEntry.java`

Speichert **einen kompletten Versuch**.

* den eingegebenen Code
* die zugehörige Auswertung

Also: **eine Zeile der Spielhistorie**

---

## `MastermindView.java`

Ist die grafische Oberfläche.

* zeigt das Spielfeld an
* zeigt frühere Versuche
* zeigt die Auswertung
* zeigt verbleibende Versuche
* enthält Eingabefeld und Buttons
* gibt Meldungen aus

Also: **alles, was der Spieler sieht und bedient**

---

## `module-info.java`

Legt fest, welche JavaFX-Module verwendet werden.

Bei dir:

* `javafx.controls`

Also: **Modulbeschreibung des Projekts**

---

## `pom.xml`

Ist die Maven-Projektdatei.

* verwaltet Bibliotheken
* bindet JavaFX ein
* legt Java-Version fest
* sagt Maven, welche Hauptklasse gestartet werden soll

Also: **Projekt- und Build-Konfiguration**

---

## Ganz kurz zusammengefasst

* **MainApp** → startet alles
* **Controller** → steuert das Spiel
* **Model** → enthält die Logik
* **Evaluation** → Ergebnis eines Tipps
* **RoundEntry** → ein Versuch mit Auswertung
* **View** → Oberfläche
* **module-info** → Java-Module
* **pom.xml** → Maven-Einstellungen

Wenn du willst, erkläre ich dir als Nächstes noch **den Ablauf vom Programm Schritt für Schritt**, also was beim Starten und bei einem Klick genau passiert.



------

--------


## Promt: ##
Neue Aufgaben:
Erklärung/Beschreibung mit einem Butten ein zusätzliches Fenster öffnen. Weiterspielbar nur wenn das Fenster mit OK wieder geschlossen worden ist.
Drag und Drop soll nicht nucr in der AUsgabe hin und her schiebbar sein sondern auch von der auswahl heraus in den prüfbereich. Bei der Drag und Drop soll das Element sichbar verschoben werden. (Also das das Element meiner Maus nachfahrt)
Die grauen spielfelderumrandungen sollen durchsichtig sein.

## Ausgabe: ##
Ja, Christoph — dafür musst du **nur 2 Dateien ändern**:

* `MastermindController.java`
* `MastermindView.java`

**Alle anderen Dateien bleiben wie in deiner letzten funktionierenden Version.**
`pom.xml`, `module-info.java`, `MainApp.java`, `MastermindModel.java`, `Evaluation.java`, `RoundEntry.java`, `ShapeType.java` bleiben unverändert.

---

## 1) `src/main/java/org/example/mastermind/controller/MastermindController.java`

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

        view.showError("Es sind bereits 4 Formen ausgewählt. Prüfe die Eingabe oder leere sie zuerst.");
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
```

---

## 2) `src/main/java/org/example/mastermind/view/MastermindView.java`

```java
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    private final GridPane boardGrid = new GridPane();

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
                createShapeSelectionBox(),
                createCurrentGuessBox(),
                createButtonBox()
        );
        bottomBox.setPadding(new Insets(20, 0, 0, 0));

        setTop(topBox);
        setCenter(boardGrid);
        setBottom(bottomBox);
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
}
```

---

## Was jetzt neu erfüllt ist

* **Beschreibung nicht mehr als TitledPane**, sondern per **Button**
* öffnet ein **zusätzliches modales Fenster**
* Spiel ist erst wieder bedienbar, wenn mit **OK** geschlossen wurde
* **Drag & Drop direkt aus der Auswahl in den Prüfbereich**
* **Drag-Vorschau folgt sichtbar der Maus**
* **Drag & Drop im Prüfbereich untereinander** bleibt erhalten
* die **grauen Umrandungen** im Spielfeld sind jetzt **transparent**

Wenn beim Einfügen etwas rot wird, schick mir nur den **ersten Fehler**, dann korrigiere ich ihn direkt.

