
package com.arturoar.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;


public class BPlusTreeUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/view.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("css/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("css/theme.css").toExternalForm());
        
        
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