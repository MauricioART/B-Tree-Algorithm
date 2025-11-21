package com.arturoar.controller;

import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class RemoveDialogController implements Initializable {

    @FXML private MFXTextField keyField;
    @FXML private MFXButton removeBtn;
    @FXML private MFXButton cancelBtn;

    private Consumer<Integer> onRemoveCallback;
    private Runnable onCancelRunnable;

    private Integer integerValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicialización del controlador
        removeBtn.setDisable(true);
        removeBtn.setOnAction(_->{
            if (onCancelRunnable != null){
                onCancelRunnable.run();
            }
            if (onRemoveCallback != null){
                onRemoveCallback.accept(integerValue);
            }
            keyField.setText("");
        });

        cancelBtn.setOnAction(_->{
            if (onCancelRunnable != null){
                onCancelRunnable.run();
            }
            
            keyField.setText("");
        });
        keyField.textProperty().addListener((_,_,currentText)->{
            try{

                integerValue =Integer.valueOf(currentText);
                removeBtn.setDisable(false);

            }catch(NumberFormatException e){

                removeBtn.setDisable(true);
            }
        });
    }
    
    public StringProperty keyFieldTextProperty(){
        return keyField.textProperty();
    }

    public void setOnRemoveCallback(Consumer<Integer> onRemoveCallback){
        this.onRemoveCallback = onRemoveCallback;
    }

   
    public void setOnCancelCallback(Runnable onCancelRunnable){
        this.onCancelRunnable = onCancelRunnable;
    }


}
