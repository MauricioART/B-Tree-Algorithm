package com.arturoar.view;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

import com.arturoar.util.TreeAnimator;

public abstract class NodeView extends Group {
    
    protected DoubleProperty width = new SimpleDoubleProperty();
    protected DoubleProperty height = new SimpleDoubleProperty();
   
    protected DoubleProperty xOrigin = new SimpleDoubleProperty();
    protected DoubleProperty yOrigin = new SimpleDoubleProperty();

    protected DoubleProperty animatedWidth = new SimpleDoubleProperty(0.0);
    protected DoubleProperty centerX = new SimpleDoubleProperty(0.0);

    protected List<KeyView> keys;

    public NodeView() {
        this.width.setValue(0.0);
        this.keys = new ArrayList<>();    
    }
    
    public NodeView(Double x, Double y) {
        this.setXOrigin(x);
        this.setYOrigin(y);
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.width.set(0.0);
        this.keys = new ArrayList<>();

        // Bind centerX to smoothly follow the animated width
        this.centerX.bind(this.translateXProperty().add(this.animatedWidth.divide(2)));
        
        // Animate width changes
        this.width.addListener((_, oldVal, newVal) -> {
            Transition widthTransition = TreeAnimator.animateProperty(
                this.animatedWidth, oldVal.doubleValue(), newVal.doubleValue());
            TreeAnimator.addParallelTransition(widthTransition);
        });

    }

    public int getNumberOfKeys(){
        return this.keys.size();
    }

    public KeyView getKey(int index){
        return this.keys.get(index);
    }

    public KeyView getLast(){
        return this.keys.getLast();
    }
    
    public int getKeyIndex( KeyView keyView) {
        return this.keys.indexOf(keyView);
    }

    public void appendKey(KeyView newNodeView){
        this.keys.add(newNodeView);
    }

    public int insert(KeyView newKeyView){
        // Adjust height if this is the first key
        if (this.keys.isEmpty()) {
            this.height.set(newKeyView.getHeight());
        }
        
        // Find the correct position to insert the new key
        int position = 0;
        double xOrigin = this.getXOrigin();
        double yOrigin = this.getYOrigin();
        
        for (KeyView key : this.keys) {
            if (key.getKey() < newKeyView.getKey()) {
                xOrigin += key.getWidth();
                position++;
            }else { break; }
        }
        this.keys.add(position, newKeyView);
        
        // Set the position of the new key
        if (newKeyView.isNew()){
            newKeyView.setTranslateY(yOrigin);
            newKeyView.setTranslateX( xOrigin);
            newKeyView.setCurrentXOrigin(xOrigin);
            newKeyView.setCurrentYOrigin(yOrigin);
            newKeyView.setNewOriginY(yOrigin);
        }else{
            newKeyView.setNewOriginY(yOrigin);
        }

        // Update the new origin for all keys after the inserted key
        for (int i = position; i < this.keys.size(); i++) {
            KeyView key = this.keys.get(i);
            key.setNewOriginX(xOrigin);
            xOrigin += key.getWidth();
        }

        // Update the width of the node view
        this.width.set(this.width.get() + newKeyView.getWidth());

        return position;
    }

    public int remove(KeyView key) {

        int position = this.keys.indexOf(key);

        if (position != -1) {
            this.keys.remove(position);

            // Update the width of the node view
            this.width.set(this.width.get() - key.getWidth());

            // Update the new origin for all keys after the removed key
            for (int i = position; i < this.keys.size(); i++) {
                KeyView k = this.keys.get(i);
                k.setNewOriginX(k.getNewXOrigin() - key.getWidth());
            }
        }

        return position;
    }

        

    public KeyView removeKey(Integer key) {
        KeyView removedKey = this.keys.stream()
                                    .filter(n -> n.getKey().equals(key))
                                    .findFirst()
                                    .orElse(null);

        if (removedKey != null) {
            this.keys.remove(removedKey);
            this.width.set(this.width.get() - removedKey.getWidth());
            return removedKey;
        }
        return null;
    }

    
    public void updateLayout(double deltaX) {
      
        this.keys.forEach(key -> {
            double byX = key.getDeltaX() + deltaX;

            if (byX == 0.0) return;

            // Create a transition for the key movement
            Transition movingKey = TreeAnimator.moveNode(key, byX, 0.0);
            TreeAnimator.addParallelTransition(movingKey);
            // Update the new origin for the key
            key.setNewOriginX(key.getNewXOrigin() + deltaX);
            key.setCurrentXOrigin(key.getNewXOrigin());

        });

        this.setXOrigin(getXOrigin() + deltaX);
    }
 
   
    public Double getWidth() {
        return this.width.get();
    }

    public Double getHeight(){
        return this.height.get();
    }
   

    public DoubleProperty widthProperty() {
        return this.width;
    }

    public DoubleProperty heightProperty() {
        return this.height;
    }

     
    public Double getXOrigin() {
        return this.xOrigin.get();
    }
    
    public void setXOrigin(Double x) {
        this.xOrigin.set(x);
    }

    public Double getYOrigin() {
        return this.yOrigin.get();
    }

    public void setYOrigin(Double y) {
        this.yOrigin.set(y);
    }

    public DoubleProperty xOriginProperty() {
        return this.xOrigin;
    }

    public DoubleProperty yOriginProperty() {
        return this.yOrigin;
    }
    
    public DoubleProperty centerXProperty() {
        return centerX;
    }
    
    public Double getCenterX() {
        return centerX.get();
    }

}