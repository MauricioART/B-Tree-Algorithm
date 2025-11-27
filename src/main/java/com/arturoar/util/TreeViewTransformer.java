package com.arturoar.util;

import com.arturoar.view.TreeView;

import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.scene.layout.Pane;

/**
 * Maneja transformaciones de zoom y pan para TreeView en un sistema tipo AutoCAD
 * donde TreeView representa un espacio de trabajo virtual y Canvas el viewport
 */
public class TreeViewTransformer {
    private final Pane viewport;
    private final TreeView treeView;
    
    private final Scale scaleTransform;
    private final Scale autoFitScaleTransform;
    private final Translate translateTransform;
    
    private double minScale = 1;   // 30% del tamaño original
    private double maxScale = 1;   // 300% del tamaño original
    private double currentScale = 1.0;
    
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;
    
    public TreeViewTransformer(Pane viewport, TreeView treeView) {
        this.viewport = viewport;
        this.treeView = treeView;
        
        this.scaleTransform = new Scale(1.0, 1.0);
        this.autoFitScaleTransform = new Scale(1.0, 1.0);
        this.translateTransform = new Translate(0, 0);


        ChangeListener<Number> autoFitListener = (_, oldVal, newVal) -> {
            maxScale = 1 / newVal.doubleValue();
            Transition autoFitX = TreeAnimator.getInstance().animateProperty(autoFitScaleTransform.xProperty(),oldVal.doubleValue(), newVal.doubleValue());
            Transition autoFitY = TreeAnimator.getInstance().animateProperty(autoFitScaleTransform.yProperty(),oldVal.doubleValue(), newVal.doubleValue());
            TreeAnimator.getInstance().addParallelTransition(autoFitX);
            TreeAnimator.getInstance().addParallelTransition(autoFitY);
        };
        
        this.treeView.scaleProperty().addListener(autoFitListener);

        initialize();
    }
    
    private void initialize() {
        // Aplicar transformaciones al TreeView
        treeView.getTransforms().addAll(autoFitScaleTransform, scaleTransform, translateTransform);
        
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        // Zoom con rueda del mouse
        viewport.setOnScroll(this::handleScroll);
        
        // Pan con click derecho
        viewport.setOnMousePressed(this::handleMousePressed);
        viewport.setOnMouseDragged(this::handleMouseDragged);
        viewport.setOnMouseReleased(this::handleMouseReleased);
    }
    
    private void handleScroll(ScrollEvent event) {
        double zoomFactor = event.getDeltaY() > 0 ? 1.05 : 0.95;
        zoomToPoint(zoomFactor, event.getX(), event.getY());
        event.consume();
    }
    
    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            isDragging = true;
            event.consume();
        }
        System.out.println("Pressed at: (" + event.getX() + ", " + event.getY() + ")");
        System.out.println("Current translation: (" + translateTransform.getX()/currentScale + ", " + translateTransform.getY()/currentScale + ")");
        System.out.println("Current scale: " + currentScale);
        Point2D worldPoint = viewportToWorld2(event.getX(), event.getY());
        System.out.println("World coords : (" + worldPoint.getX() + ", " + worldPoint.getY()+ ")");
    }
    
    private void handleMouseDragged(MouseEvent event) {
        if (isDragging && event.getButton() == MouseButton.PRIMARY && treeView.scaleProperty().get() < 1.0) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;
            
            pan(deltaX, deltaY);
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            event.consume();
        }
    }
    
    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            isDragging = false;
            event.consume();
        }
    }
    
    /**
     * Zoom aplicado a un punto específico del viewport (anclado al cursor)
     */
    public void zoomToPoint(double scaleFactor, double viewportX, double viewportY) {
        Point2D worldPoint = viewportToWorld(viewportX, viewportY);
        
        double newScale = currentScale * scaleFactor;
        newScale = Math.max(minScale, Math.min(maxScale, newScale));
        
        if (newScale != currentScale) {
            
            currentScale = newScale;
            scaleTransform.setPivotX(worldPoint.getX());
            scaleTransform.setPivotY(worldPoint.getY());
            
            scaleTransform.setX(currentScale);
            scaleTransform.setY(currentScale);

        }
        enforcePanBounds();
    }
    
    /**
     * Desplazamiento del viewport
     */
    public void pan(double deltaX, double deltaY) {
        double deltaXScaled = deltaX / currentScale;
        double deltaYScaled = deltaY / currentScale;
        translateTransform.setX(translateTransform.getX() + deltaXScaled);
        translateTransform.setY(translateTransform.getY() + deltaYScaled);
        enforcePanBounds();
    }
    
    /**
     * Ajustar zoom para que todo el árbol sea visible
     */
    public void zoomToFit() {
        // Implementación basada en las dimensiones del TreeView
        double treeWidth = treeView.getBoundsInLocal().getWidth();
        double treeHeight = treeView.getBoundsInLocal().getHeight();
        
        double scaleX = viewport.getWidth() / treeWidth;
        double scaleY = viewport.getHeight() / treeHeight;
        
        double newScale = Math.min(scaleX, scaleY) * 0.9; // 10% de margen
        
        zoomToPoint(newScale / currentScale, viewport.getWidth() / 2, viewport.getHeight() / 2);
    }
    
    /**
     * Resetear a zoom 1:1
     */
    public void resetZoom() {
        zoomToPoint(1.0 / currentScale, viewport.getWidth() / 2, viewport.getHeight() / 2);
    }

    /*
     * Resetear vista a estado inicial
     */
    public void resetView() {
        resetZoom();
        translateTransform.setX(0);
        translateTransform.setY(0);
    }
    /**
     * Convertir coordenadas del viewport a coordenadas mundiales (TreeView)
     */
    public Point2D viewportToWorld(double viewportX, double viewportY) {
        double worldX = (viewportX - translateTransform.getX()); // / currentScale;
        double worldY = (viewportY - translateTransform.getY()); // / currentScale;
        return new Point2D(worldX, worldY);
    }
    
    /**
     * Convertir coordenadas mundiales (TreeView) a coordenadas del viewport
     */
    public Point2D worldToViewport(double worldX, double worldY) {
        return worldToViewport(worldX, worldY, currentScale);
    }
    
    private Point2D worldToViewport(double worldX, double worldY, double scale) {
        double viewportX = (worldX * scale) + translateTransform.getX();
        double viewportY = (worldY * scale) + translateTransform.getY();
        return new Point2D(viewportX, viewportY);
    }

   

    public Point2D viewportToWorld2(double viewportX, double viewportY) {
        // 1. Aplicar inversa de Translate (primero en orden)
        double x = viewportX - translateTransform.getX();
        double y = viewportY - translateTransform.getY();
        
        // 2. Aplicar inversa de Scale (segundo en orden)
        double px = scaleTransform.getPivotX();
        double py = scaleTransform.getPivotY();


        x = (x + (scaleTransform.getX() - 1) * px) / scaleTransform.getX();
        y = (y + (scaleTransform.getY() - 1) * py) / scaleTransform.getY();
        
        // 3. Aplicar inversa de AutoFit (tercero en orden)
        x = x / autoFitScaleTransform.getX();
        y = y / autoFitScaleTransform.getY();
        
        return new Point2D(x, y);
    }

    
    /**
     * Aplicar límites de desplazamiento para no salirse del contenido
     */
    /*private void enforcePanBounds() {
        double treeWidth = treeView.getBoundsInLocal().getWidth() * getTotalScale();
        double treeHeight = treeView.getBoundsInLocal().getHeight() * getTotalScale();
        
        double viewportWidth = viewport.getWidth();
        double viewportHeight = viewport.getHeight();

        double maxXPan = (treeWidth - viewportWidth)/2;
        double maxYPan = (treeHeight - viewportHeight)/2;
        
        //double clampedX = Math.max(minX, Math.min(maxX, translateTransform.getX()));
        //double clampedY = Math.max(minY, Math.min(maxY, translateTransform.getY()));

        double clampedX = 0;
        double clampedY = 0;

        if (translateTransform.getX() < maxXPan && translateTransform.getX() > -maxXPan) {
            clampedX = translateTransform.getX();
        } else if ( translateTransform.getX() < 0){
            clampedX = -maxXPan;
        } else{
            clampedX = maxXPan;
        }
        if (translateTransform.getY() < maxYPan && translateTransform.getY() > -maxYPan) {
            clampedY = translateTransform.getY();
        } else if (translateTransform.getY() < 0){
            clampedY = -maxYPan;
        }else {
            clampedY = maxYPan;
        }
        
        translateTransform.setX(clampedX);
        translateTransform.setY(clampedY);

    }*/
    private void enforcePanBounds() {
    // 1. Calcular dimensiones VISUALES del árbol (con escala aplicada)
    double treeWidth = treeView.getBoundsInLocal().getWidth() * currentScale * autoFitScaleTransform.getX();
    double treeHeight = treeView.getBoundsInLocal().getHeight() * currentScale * autoFitScaleTransform.getY();
    
    // 2. Obtener dimensiones del viewport
    double viewportWidth = viewport.getWidth();
    double viewportHeight = viewport.getHeight();
    
    // 3. Obtener traslación actual
    double currentX = translateTransform.getX();
    double currentY = translateTransform.getY();
    
    // 4. LÓGICA CORREGIDA para eje X:
    if (treeWidth <= viewportWidth) {
        // Árbol más pequeño: permitir desplazamiento para CENTRARLO
        // Límites: desde 0 (pegado a la izquierda) hasta (viewportWidth - treeWidth) (pegado a la derecha)
        double minX = -(viewportWidth - treeWidth)/2;
        double maxX = (viewportWidth - treeWidth)/2;
        translateTransform.setX(Math.max(minX, Math.min(maxX, currentX)));
    } else {
        // Árbol más grande: permitir desplazamiento para NAVEGAR
        // Límites: desde -(treeWidth - viewportWidth) (extremo izquierdo) hasta 0 (extremo derecho)
        double minX = -(treeWidth - viewportWidth);
        double maxX = 0;
        translateTransform.setX(Math.max(minX, Math.min(maxX, currentX)));
    }
    
    // 5. LÓGICA CORREGIDA para eje Y (misma lógica que X):
    if (treeHeight <= viewportHeight) {
        double minY = 0;
        double maxY = viewportHeight - treeHeight;
        translateTransform.setY(Math.max(minY, Math.min(maxY, currentY)));
    } else {
        double minY = -(treeHeight - viewportHeight);
        double maxY = 0;
        translateTransform.setY(Math.max(minY, Math.min(maxY, currentY)));
    }
}


    private double getTotalScale() {
        return autoFitScaleTransform.getX() * scaleTransform.getX();
    }
    
    // Getters y setters
    public void setZoomLimits(double minScale, double maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;
    }
    
    public double getCurrentScale() {
        return currentScale;
    }
    
    public Point2D getCurrentTranslation() {
        return new Point2D(translateTransform.getX(), translateTransform.getY());
    }
}