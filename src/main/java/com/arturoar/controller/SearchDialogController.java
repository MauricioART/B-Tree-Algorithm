package com.arturoar.controller;

import java.util.function.Consumer;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.Initializable;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

public class SearchDialogController implements Initializable {

    @FXML private MFXTextField keyField;
    @FXML private MFXButton searchBtn;
    @FXML private MFXButton cancelBtn;


    private Consumer<Integer> onSearchCallback;
    private Runnable onCancelRunnable;

    private Integer integerValue;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        // Inicialización del controlador

        searchBtn.setDisable(true);
        searchBtn.setOnAction(_->{
            if (onCancelRunnable != null){
                onCancelRunnable.run();
            }
            if (onSearchCallback != null){
                onSearchCallback.accept(integerValue);
            }
        });

        cancelBtn.setOnAction(_->{
            if (onCancelRunnable != null){
                onCancelRunnable.run();
            }
        });

        keyField.textProperty().addListener((_,_,currentText)->{
            try{

                integerValue =Integer.valueOf(currentText);
                searchBtn.setDisable(false);

            }catch(NumberFormatException e){

                searchBtn.setDisable(true);
            }
        });
    }
    
    public StringProperty keyFieldTextProperty(){
        return keyField.textProperty();
    }

    public void setOnSearchCallback(Consumer<Integer> onSearchCallback){
        this.onSearchCallback = onSearchCallback;
    }

    public void setOnCancelCallback(Runnable onCancelRunnable){
        this.onCancelRunnable = onCancelRunnable;
    }



    
    
}