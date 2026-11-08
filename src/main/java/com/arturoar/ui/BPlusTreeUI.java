package com.arturoar.ui;

import com.arturoar.controller.BPlusTreeController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class BPlusTreeUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("appIcon.png")));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/BPlusTreeView.fxml"));
        loader.setControllerFactory(type -> new BPlusTreeController(stage));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Explorador B+");
        stage.setMinWidth(780);
        stage.setMinHeight(475);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
