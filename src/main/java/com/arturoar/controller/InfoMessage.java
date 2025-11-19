package com.arturoar.controller;


import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.paint.*;
public class InfoMessage extends VBox {

    private MFXFontIcon icon;
    private Label message;

    public InfoMessage(){
        this.icon = new MFXFontIcon();
        this.icon.setSize(35);
        this.message = new Label();
        this.message.setFont(Font.font(20));
        this.setBackground(Background.fill((Paint)Color.web("rgba(181, 200, 218, 1)")));
        this.setPadding(new Insets(15, 30, 15, 30));
        this.setBorder(new Border(new BorderStroke(
            Color.BLACK,                    // Color
            BorderStrokeStyle.SOLID,        // Estilo
            CornerRadii.EMPTY,              // Esquinas
            BorderWidths.DEFAULT            // Ancho
        )));
        

        getChildren().addAll(icon, message);
        setLayout();
    }

    public void setLayout(){

        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
    }

    public void setIcon(String description){
        this.icon.setDescription(description);
    }

    public void setMessage(String message){
        this.message.setText(message);
    }
}