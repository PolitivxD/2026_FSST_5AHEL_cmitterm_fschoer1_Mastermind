package org.example.mastermind.model;

import javafx.scene.control.TextInputDialog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class Leaderboard {

    public Leaderboard (int attemptsUsed)  {
        String name = getUsername();

        if (name != null && !name.isEmpty()) {
            // Hier können Sie den Namen und die Anzahl der Versuche in Ihrem Leaderboard speichern
            System.out.println("Spieler: " + name + ", Versuche: " + attemptsUsed);
        } else {
            System.out.println("Kein Name eingegeben. Spieler wird nicht zum Leaderboard hinzugefügt.");
        }

        writescoresinfile(name, attemptsUsed);

    }

    private String getUsername (){
        String name = null;

        name = String.valueOf(Panel());

        return name;
    }
    public String Panel() {
        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Eingabefeld");
        dialog.setHeaderText("Benutzername eingeben");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();

        return result.orElse("");
    }
    public void writescoresinfile(String name, int attemptsUsed) {

        BufferedWriter out = null;

        try {
            FileWriter fstream = new FileWriter("Scores.txt", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            out.write("Spieler:" + name + ",Versuche:" + attemptsUsed + "\n");
            out.close();
        }

        catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }


}

