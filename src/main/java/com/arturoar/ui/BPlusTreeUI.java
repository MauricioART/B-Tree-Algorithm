
package com.arturoar.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BPlusTreeUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(BPlusTreeUI.class.getResource("vista.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        primaryStage.setTitle("B+ Tree");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}