package com.arturoar.controller;


import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class InfoMessage extends VBox {

    private MFXFontIcon icon;
    private Label message;

    public InfoMessage(){
        this.icon = new MFXFontIcon();
        this.icon.setSize(35);
        this.icon.getStyleClass().add("image");
        this.message = new Label();
        this.message.setFont(Font.font(20));
        this.message.getStyleClass().add("image");
        
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