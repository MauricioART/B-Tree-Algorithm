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
    

    public void appendKey(KeyView newNodeView){
        this.keys.add(newNodeView);
    }

    public int insert(KeyView newKeyView){
        int position = 0;
        double xOrigin = 0.0;

        if (this.keys.isEmpty()) {
            this.height.set(newKeyView.getHeight());
        }
        for (KeyView key : this.keys) {
            if (key.getKey() < newKeyView.getKey()) {
                xOrigin += key.getWidth();
                position++;
            }else { break; }
        }
        this.keys.add(position, newKeyView);

        
        //newKeyView.setNewOriginY(this.translateYProperty().get()); if the key is the inserted key set translateX property instead of newOriginX

        for (int i = position; i < this.keys.size(); i++) {
            KeyView key = this.keys.get(i);
            if (key.equals(newKeyView)) key.setTranslateX(xOrigin);
            key.setNewOriginX(xOrigin);
            xOrigin += key.getWidth();
        }

        this.width.set(this.width.get() + newKeyView.getWidth());

        this.getChildren().add(newKeyView);

        return position;
    }

    public KeyView removeNode(Integer key) {
        KeyView node = this.keys.stream()
                                    .filter(n -> n.getKey().equals(key))
                                    .findFirst()
                                    .orElse(null);
        if (node != null) {
            this.keys.remove(node);
            this.width.set(this.width.get() - node.getWidth());
            return node;
        }
        return null;
    }

    
    public void updateLayout() {
        /*
        for (KeyView key : this.keys){
            double byX = key.getDeltaX();
            if (byX == 0.0) return;
            Transition movingKey = TreeAnimator.moveNode(key, key.getDeltaX(), 0.0);
            movingKey.setOnFinished(_ -> onFinished(key));
            TreeAnimator.addParallelTransition(movingKey);
        }
    */ 
        this.keys.forEach(key -> {
            double byX = key.getDeltaX();
            if (byX == 0.0) return;
            Transition movingKey = TreeAnimator.moveNode(key, key.getDeltaX(), 0.0);
            movingKey.setOnFinished(_ -> onFinished(key));
            TreeAnimator.addParallelTransition(movingKey);

        });
    }
 
    private void onFinished(KeyView key) {
        System.out.println("Finished animation for key: " + key.getKey() + "\t" + "Current translate X: " + key.getTranslateX() + "\t" + "New origin X: " + key.getNewOriginX());
        key.setNewOriginX(key.getTranslateX());
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


}