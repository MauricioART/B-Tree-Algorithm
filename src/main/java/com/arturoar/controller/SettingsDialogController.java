package com.arturoar.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import org.kordamp.ikonli.coreui.CoreUiFree;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import io.github.palexdev.materialfx.controls.MFXSlider;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;




public class SettingsDialogController {
    @FXML private MFXSlider mSlider;
    @FXML private Label mLabel;
    @FXML private MFXToggleButton traversalToggle;
    @FXML private MFXSlider speedSlider;
    @FXML private MFXToggleButton themeToggle;
    @FXML private Label speedLabel;
    @FXML private MFXToggleButton soundToggle;

    private FontIcon traversalToggleIcon;
    private FontIcon soundToggleIcon;
    private FontIcon themeToggleIcon;

    Runnable onMSliderChange;
    
    @FXML
    public void initialize() {
        setupMSlider();
        setupThemeToggle();
        setupSpeedSlider();
        setupTraversalToggle();
        setupSoundToggle();
    }
    
    public void setOnMSliderChange(Runnable onMSliderChange){
        mSlider.setOnMouseReleased(_ -> { 
            System.out.println("Done"); 
            onMSliderChange.run();
        });
    }
    private void setupThemeToggle() {
        // Lógica para inicializar el toggle de tema
        themeToggleIcon = new FontIcon(FontAwesomeSolid.LIGHTBULB);
        themeToggle.setGraphic(themeToggleIcon);
        themeToggle.setSelected(false);

        themeToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            
            if (isSelected) {

                themeToggleIcon.setIconColor(Color.WHITE);
                traversalToggleIcon.setIconColor(Color.WHITE);
                soundToggleIcon.setIconColor(Color.WHITE);
                
                themeToggleIcon.setIconCode(FontAwesomeSolid.MOON);
            } else {
                themeToggleIcon.setIconColor(Color.BLACK);
                traversalToggleIcon.setIconColor(Color.BLACK);
                soundToggleIcon.setIconColor(Color.BLACK);
                themeToggleIcon.setIconCode(FontAwesomeSolid.LIGHTBULB);
                
            }
        });
    }

    private void setupMSlider(){
        mSlider.setMax(10);
        mSlider.setMin(4);
        mSlider.setDecimalPrecision(0);
        mSlider.setValue(7);
        mLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%d", (int)mSlider.getValue()), mSlider.valueProperty()));
    }

    private void setupSpeedSlider() {
        speedSlider.setValue(1.0);
        speedSlider.setMin(0.50);
        speedSlider.setMax(2.0);
        speedSlider.setDecimalPrecision(1);
        speedLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%.2fx", speedSlider.getValue()),speedSlider.valueProperty()));
    }


    private void setupTraversalToggle() {
        // Lógica para inicializar el toggle de recorrido
        traversalToggleIcon = new FontIcon(FontAwesomeSolid.PLAY_CIRCLE);
        traversalToggle.setSelected(true);
        traversalToggle.setGraphic(traversalToggleIcon);
        traversalToggle.selectedProperty().addListener((_,_,newVal)->{
            if (newVal){
                traversalToggleIcon.setIconCode(FontAwesomeSolid.PLAY_CIRCLE);
            }else{
                traversalToggleIcon.setIconCode(FontAwesomeSolid.STOP_CIRCLE);
            }
        });
    }

    private void setupSoundToggle(){
        soundToggleIcon = new FontIcon(FontAwesomeSolid.VOLUME_UP);

        soundToggle.setSelected(true);
        soundToggle.setGraphic(soundToggleIcon);

        soundToggle.selectedProperty().addListener((_,_,isSelected)->{
            if (isSelected){
                soundToggleIcon.setIconCode(FontAwesomeSolid.VOLUME_UP);
            }else{
                soundToggleIcon.setIconCode(FontAwesomeSolid.VOLUME_MUTE);
            }
        });
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

    public BooleanProperty soundToggleProperty(){
        return soundToggle.selectedProperty();
    }

    public MFXSlider getMSlider(){
        return mSlider;
    }

}