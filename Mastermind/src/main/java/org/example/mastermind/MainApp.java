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

        Scene scene = new Scene(view, 710, 900);

        stage.setTitle("Mastermind");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}