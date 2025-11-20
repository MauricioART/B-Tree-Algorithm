package com.arturoar.view;

import java.util.function.Consumer;

import com.arturoar.util.TreeAnimator;

import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

public class KeyView extends Group {

    private final Paint LIGHT_COLOR = Color.web("#008e9b");
    private final Paint DARK_COLOR = Color.web("#ffffffff");
    
    private Rectangle nodeShape;
    private Text keyLabel;
    private Label dataLabel;
    private Integer key;
    private double currentXOrigin;
    private double currentYOrigin;
    private double newXOrigin;
    private double newYOrigin;
    private DoubleProperty widthProperty;
    private Double height;
    private boolean isNew;
    private final Double paddingX = 8.0;
    private final Double paddingY = 4.0;
    private NodeView node;
    private NodeView newNode;
    private ObjectProperty<Paint> colorProperty = new SimpleObjectProperty<>(LIGHT_COLOR);
    private BooleanProperty darkModeProperty = new SimpleBooleanProperty(false);

    private Consumer<Integer> onWidthChangeCallback;

    public KeyView(Integer key) {
        this.key = key;
        keyLabel = new Text(this.key.toString());
        //keyLabel.setFont(Font.font(13));
        widthProperty = new SimpleDoubleProperty (keyLabel.getLayoutBounds().getWidth() + 2 * paddingX);
        height = keyLabel.getLayoutBounds().getHeight() + 2 * paddingY;
        isNew = true;
        setupNode();
        darkModeProperty.addListener((_,_,newVal)->{
            if (newVal){
                colorProperty.set(DARK_COLOR);
                if(dataLabel != null) {
                    dataLabel.setTextFill(DARK_COLOR);
                }
            }else{
                colorProperty.set(LIGHT_COLOR);
                if (dataLabel != null){
                    dataLabel.setTextFill(Color.BLACK);
                } 
            }
        });
        
    }
    public KeyView(Integer key, String nodeData ) {
        this(key);
        dataLabel = new Label(nodeData);
        
        // Configuración básica del Label
        dataLabel.setMinHeight(22.0);
        dataLabel.setMaxHeight(22.0);
        dataLabel.setMinWidth(this.height);
        dataLabel.setMaxWidth(200.0);
        dataLabel.setAlignment(Pos.BASELINE_LEFT);
        dataLabel.setWrapText(true);
        dataLabel.setStyle("-fx-padding: 0 5 0 5;"); // Espaciado interno

        // LIMPIA cualquier transformación previa
        dataLabel.getTransforms().clear();

        // Aplicar rotación -90° desde la esquina superior izquierda
        Rotate rotate = new Rotate(90, 0, 0);
        dataLabel.getTransforms().add(rotate);
        

        // Posicionamiento ABSOLUTO en el contenedor padre
        dataLabel.setLayoutX(0);  // Alineado al borde izquierdo del contenedor
        dataLabel.setLayoutY(dataLabel.getHeight() + this.height + 10);  // 5 unidades por debajo del contenedor
        dataLabel.translateXProperty().bind(widthProperty.divide(2).add(11.0));
        dataLabel.getStyleClass().add("rotated-label-modern");

        

        getChildren().add(dataLabel);
    }


    private void setupNode(){
        
        nodeShape = new Rectangle(this.widthProperty.get(), this.height);
        nodeShape.setStrokeWidth(1);
        
        nodeShape.strokeProperty().bind(colorProperty);
        nodeShape.fillProperty().bind(Bindings.createObjectBinding(() -> {
            Paint originalColor = colorProperty.get();
            if (originalColor instanceof Color) {
                Color color = (Color) originalColor;
                return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.2);
            }
            return originalColor;
        }, colorProperty));

        nodeShape.setStrokeWidth(2);      
        nodeShape.widthProperty().bind(widthProperty);
        nodeShape.setArcWidth(10.0);
        nodeShape.setArcHeight(10.0);   

        nodeShape.getStyleClass().clear();
        nodeShape.getStyleClass().add("keyview");
        
        keyLabel.setX(paddingX);
        keyLabel.setY(this.height - (2.0 * paddingY));
        keyLabel.fillProperty().bind(colorProperty);

        
        getChildren().addAll(this.nodeShape, this.keyLabel);

        ChangeListener<String> strListener = (_, _, newValue) -> {
            this.setKey(Integer.valueOf(newValue));

        };

        keyLabel.layoutBoundsProperty().addListener((_, oldVal, newVal)->{
            double oldWidth = oldVal.getWidth() + 2 * paddingX;
            double newWidth = newVal.getWidth() + 2 * paddingX;
            Transition widthChangeTransition = TreeAnimator.getInstance().animateProperty(widthProperty, oldWidth, newWidth);
            TreeAnimator.getInstance().addParallelTransition(widthChangeTransition);
            node.onWidthChange(this, newWidth - oldWidth);
            if (onWidthChangeCallback != null){
                int level = node.getLevel();
                onWidthChangeCallback.accept(level);
            }

            TreeAnimator.getInstance().createParallelTransition();
            TreeAnimator.getInstance().animateQueue(null);

            
        });

        this.keyLabel.textProperty().addListener(strListener);
    }

    public void setOnWidthChangeCallback(Consumer<Integer> updateLevel){
        onWidthChangeCallback = updateLevel;
    }

    public NodeView getNode() {
        return this.node;
    }

    public void setNode(NodeView node) {
        this.node = node;
    }

    public void setNewNode(NodeView newNode) {
        this.newNode = newNode;
    }   

    public void updateNode() {
        this.node = this.newNode;
    }
   
    public double getNewXOrigin(){
        return this.newXOrigin;
    }

    public double getNewYOrigin(){
        return this.newYOrigin;
    }   

    public void setNewOriginX(Double newOriginX) {
        this.newXOrigin = newOriginX;
    }

    public void setNewOriginY(Double newOriginY) {
        this.newYOrigin = newOriginY;
    }

    public double getCurrentXOrigin() {
        return this.currentXOrigin;
    }

    public double getCurrentYOrigin() {
        return this.currentYOrigin;
    }

    public void setCurrentXOrigin(Double currentXOrigin) {
        this.currentXOrigin = currentXOrigin;
    }

    public void setCurrentYOrigin(Double currentYOrigin) {
        this.currentYOrigin = currentYOrigin;
    } 

    public double getDeltaX(){
        double deltaX  = this.newXOrigin - this.currentXOrigin;
        return deltaX;
    }

    public double getDeltaY(){
        double deltaY  = this.newYOrigin - this.currentYOrigin;
        return deltaY;
    }
    
    public boolean isNew() {
        return isNew;
    }

    public Double getHeight() {
        return this.height;
    }
    public Double getWidthProperty() {
        return this.widthProperty.get();
    }
    public Integer getKey() {
        return this.key;
    }
    public Text getKeyLabel() {
        return this.keyLabel;
    }
    public void setKey(Integer key) {
        this.key = key;
    }
  
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public Rectangle getKeyShape() {
        return this.nodeShape;
    }

    public DoubleProperty widthProperty(){
        return widthProperty;
    }

    public ObjectProperty<Paint> colorProperty(){
        return colorProperty;
    }

    public Label getData(){
        return dataLabel;
    }

    public BooleanProperty darkModeProperty() { return darkModeProperty; }
}

