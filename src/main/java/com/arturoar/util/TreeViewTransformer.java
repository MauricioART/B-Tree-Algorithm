package com.arturoar.util;

import com.arturoar.view.TreeView;

import javafx.geometry.Bounds;
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
    private double maxScale = 3;
    private double currentScale = 1.0;
    
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;
    
    public TreeViewTransformer(Pane viewport, TreeView treeView) {
        this.viewport = viewport;
        this.treeView = treeView;
        
        this.scaleTransform = new Scale(1.0, 1.0);
        this.autoFitScaleTransform = new Scale(1.0, 1.0);
        this.translateTransform = new Translate(0, 0);


        autoFitScaleTransform.xProperty().bind(treeView.scaleProperty());
        autoFitScaleTransform.yProperty().bind(treeView.scaleProperty());
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
    }
    
    private void handleMouseDragged(MouseEvent event) {
        if (isDragging && event.isPrimaryButtonDown()) {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;
            
            pan(deltaX, deltaY);
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            event.consume();
        }
    }
    
    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            isDragging = false;
            event.consume();
        }
    }
    
    /**
     * Zoom aplicado a un punto específico del viewport (anclado al cursor)
     */
    public void zoomToPoint(double scaleFactor, double viewportX, double viewportY) {
        double newScale = Math.max(minScale, Math.min(maxScale, currentScale * scaleFactor));
        if (newScale == currentScale) return;

        Point2D worldPoint = treeView.parentToLocal(viewportX, viewportY);
        currentScale = newScale;
        scaleTransform.setX(currentScale);
        scaleTransform.setY(currentScale);
        Point2D moved = treeView.localToParent(worldPoint);
        pan(viewportX - moved.getX(), viewportY - moved.getY());
    }
    
    /**
     * Desplazamiento del viewport
     */
    public void pan(double deltaX, double deltaY) {
        double deltaXScaled = deltaX / getTotalScale();
        double deltaYScaled = deltaY / getTotalScale();
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
        return treeView.parentToLocal(viewportX, viewportY);
    }

    public Point2D worldToViewport(double worldX, double worldY) {
        return treeView.localToParent(worldX, worldY);
    }

    public Point2D viewportToWorld2(double viewportX, double viewportY) {
        return viewportToWorld(viewportX, viewportY);
    }

    private void enforcePanBounds() {
        Bounds bounds = treeView.getBoundsInParent();
        if (bounds.isEmpty() || viewport.getWidth() <= 0 || viewport.getHeight() <= 0) return;

        double scale = getTotalScale();
        double correctionX = panCorrection(bounds.getMinX(), bounds.getMaxX(), viewport.getWidth());
        double correctionY = panCorrection(bounds.getMinY(), bounds.getMaxY(), viewport.getHeight());
        translateTransform.setX(translateTransform.getX() + correctionX / scale);
        translateTransform.setY(translateTransform.getY() + correctionY / scale);
    }

    private double panCorrection(double min, double max, double viewportSize) {
        if (max - min > viewportSize) {
            return min > 0 ? -min : Math.max(0, viewportSize - max);
        }
        return min < 0 ? -min : Math.min(0, viewportSize - max);
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
