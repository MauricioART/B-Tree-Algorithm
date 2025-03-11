
package com.arturoar.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;


public class BPlusTreeUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(BPlusTreeUI.class.getResource("vista.fxml"));
        Parent root = fxmlLoader.load();
        //BPlusTreeController controller = fxmlLoader.getController();
        Scene scene = new Scene(root);
        primaryStage.setResizable(true);
        primaryStage.setTitle(STYLESHEET_CASPIAN);
        primaryStage.setTitle("B+ Tree");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}