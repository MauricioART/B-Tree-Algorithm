
package com.arturoar.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;


public class BPlusTreeUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

       
        FXMLLoader fxmlLoader = new FXMLLoader(BPlusTreeUI.class.getResource("fxml/BPlusTreeView.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);

        
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        //scene.getStylesheets().add(getClass().getResource("css/styles.css").toExternalForm());
         // Aplicar clipping para redondear la ventana completa
        //Rectangle clip = new Rectangle(scene.getWidth(), scene.getHeight());
        //clip.setArcWidth(40); // Radio de las esquinas
        //clip.setArcHeight(40);
       // root.setClip(clip);
        
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