package com.arturoar.controller;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

//import com.jfoenix.controls.JFXSlider;
//import com.jfoenix.controls.JFXToggleButton;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;




public class SettingsDialogController {
    @FXML private Slider mSlider;
    @FXML private Label mLabel;
    @FXML private ToggleButton traversalToggle;
    @FXML private Slider speedSlider;
    @FXML private ToggleButton themeToggle;
    @FXML private Label speedLabel;

    
    @FXML
    public void initialize() {
        setupMSlider();
        setupThemeToggle();
        setupSpeedSlider();
        setupTraversalToggle();
    }
    
    private void setupThemeToggle() {
        // Lógica para inicializar el toggle de tema
        FontIcon lightbulbIcon = new FontIcon(FontAwesomeSolid.LIGHTBULB);
        themeToggle.setGraphic(lightbulbIcon);
        themeToggle.setSelected(false);
        //themeToggle.setColors(Color.LIGHTGRAY, Color.FLORALWHITE);

        themeToggle.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                // Cambiar a tema oscuro
                FontIcon moonIcon = new FontIcon(FontAwesomeSolid.MOON);
                moonIcon.setFill(Color.WHITE);
                themeToggle.setGraphic(moonIcon);
            } else {
                // Cambiar a tema claro
                themeToggle.setGraphic(lightbulbIcon);
                
            }
        });
    }

    private void setupMSlider(){
        mSlider.setMax(10);
        mSlider.setMin(4);
        mSlider.setValue(4);
        mLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%d", (int)mSlider.getValue()), mSlider.valueProperty()));
    }

    private void setupSpeedSlider() {
        speedSlider.setValue(1.0);
        speedSlider.setMin(0.25);
        speedSlider.setMax(1.5);
        speedLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%.2fx", speedSlider.getValue()),speedSlider.valueProperty()));
    }


    private void setupTraversalToggle() {
        // Lógica para inicializar el toggle de recorrido
        traversalToggle.setSelected(true);
        traversalToggle.setText("Enable");
        traversalToggle.selectedProperty().addListener((_,_,newVal)->{
            if (newVal){
                traversalToggle.setText("Enable");
            }else{
                traversalToggle.setText("Disable");
            }
        });
        //traversalToggle.setColors(Color.ANTIQUEWHITE, Color.BEIGE);
    }


    public DoubleProperty speedSliderValueProperty(){
        return speedSlider.valueProperty();
    }

    public DoubleProperty mProperty(){
        return mSlider.valueProperty();
    }

    public BooleanProperty traversalToggleProperty(){
        return traversalToggle.selectedProperty();
    }

    public BooleanProperty themeToggleProperty(){
        return themeToggle.selectedProperty();
    }
}