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
