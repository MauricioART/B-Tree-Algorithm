package com.arturoar.controller;

import java.util.function.Consumer;

//import com.jfoenix.controls.JFXButton;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import javafx.scene.control.Button;

public class InsertDialogController implements Initializable{

    @FXML private TextField keyField;
    @FXML private TextField valueField;
    @FXML private Button insertBtn;
    @FXML private Button cancelBtn;

    private Consumer<Pair<Integer, String>> onInsertCallback;
    private Runnable onCancelRunnable;

    private Integer integerValue;
    
    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        // Inicialización del controlador
        insertBtn.setDisable(true);
        insertBtn.setOnAction(_->{
            if (onCancelRunnable != null){
                onCancelRunnable.run();
            }
            if (onInsertCallback != null){
                onInsertCallback.accept(new Pair<>(integerValue, valueField.getText()));
            }
        });

        cancelBtn.setOnAction(_->{
            if (onCancelRunnable != null){
                keyField.setText("");
                valueField.setText("");
                onCancelRunnable.run();
            }
        });
        keyField.textProperty().addListener((_,_,currentText)->{
            try{
                integerValue =Integer.valueOf(currentText);
                insertBtn.setDisable(false);
                valueField.setText("Data " + currentText);
            }catch(NumberFormatException e){
                insertBtn.setDisable(true);
            }
        });

        valueField.focusedProperty().addListener(_->{
            valueField.setText("");
        });
        
    }

    public StringProperty keyFieldTextProperty(){
        return keyField.textProperty();
    }
    
    public StringProperty valueFieldTextProperty(){
        return valueField.textProperty();
    }

    public void setOnInsertCallback(Consumer<Pair<Integer, String>> onInsertCallback){
        this.onInsertCallback = onInsertCallback;
    }

   
    public void setOnCancelCallback(Runnable onCancelRunnable){
        this.onCancelRunnable = onCancelRunnable;
    }
    
}

