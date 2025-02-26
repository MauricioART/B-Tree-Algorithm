
package com.arturoar.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;



public class BPlusTreeUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
       // FXMLLoader fxmlLoader = new FXMLLoader(BPlusTreeUI.class.getResource("vista.fxml"));
       // Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        primaryStage.setTitle("B+ Tree");
       // primaryStage.setScene(scene);
        primaryStage.setResizable(false);
       // primaryStage.show();
       Group root = new Group();
       Scene scene = new Scene(root,900,600);

       final Canvas canvas = new Canvas(250,250);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.AQUAMARINE);
        gc.fillRect(75,75,100,100);
        gc.setFont(new Font(STYLESHEET_CASPIAN, 20));
        gc.fillText("Hola jnijwednoni4fn", 70, 75);
        Line linea = new Line();
        linea.setStartX(0.0f);
        linea.setStartY(0.0f);
        linea.setEndX(100.0f);
        linea.setEndY(300.0f);
        gc.strokeLine(0,0,300,100);  
        //Text texto = new Text(10, 50, "Hola mundo");
        
        root.getChildren().add(canvas);
       

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}