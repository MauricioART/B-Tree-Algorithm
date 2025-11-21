package com.arturoar.controller;


import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.coreui.CoreUiFree;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class InfoMessage extends VBox {

    private FontIcon icon;
    private Label message;

    public InfoMessage(){
        this.icon = new FontIcon();
        this.icon.setIconSize(30);
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

    public void setIcon(Ikon ikon){
        this.icon.setIconCode(ikon);
    }

    public void setMessage(String message){
        this.message.setText(message);
    }
}