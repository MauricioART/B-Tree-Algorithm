package com.arturoar.view;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;
import java.util.List;

import com.arturoar.util.TreeAnimator;

public abstract class NodeView {
    
    protected DoubleProperty width = new SimpleDoubleProperty();
    protected DoubleProperty height = new SimpleDoubleProperty();
    protected DoubleProperty translateX = new SimpleDoubleProperty();
    protected DoubleProperty translateY = new SimpleDoubleProperty();

    protected List<KeyView> keys;

    public NodeView() {
        this.width.setValue(0.0);
        this.keys = new ArrayList<>();    
    }
    
    public NodeView(Double x, Double y) {
        this.setTranslateX(x);
        this.setTranslateY(y);
        this.width.set(0.0);
        this.keys = new ArrayList<>();
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
        double xOrigin = this.getTranslateX();
        double yOrigin = this.getTranslateY();
        
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

        this.setTranslateX(getTranslateX() + deltaX);
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

    public Double getTranslateX() {
        return this.translateX.get();
    }

    public void setTranslateX(Double x) {
        this.translateX.set(x);
    }

    public Double getTranslateY() {
        return this.translateY.get();
    }

    public void setTranslateY(Double y) {
        this.translateY.set(y);
    }

    public DoubleProperty translateXProperty() {
        return this.translateX;
    }

    public DoubleProperty translateYProperty() {
        return this.translateY;
    }


}