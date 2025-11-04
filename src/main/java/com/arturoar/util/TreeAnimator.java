package com.arturoar.util;

import java.util.ArrayList;
import java.util.List;

import com.arturoar.view.Edge;
import com.arturoar.view.KeyView;
import com.arturoar.view.LeafLinkEdge;
import com.arturoar.view.TreeEdge;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.StrokeTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TreeAnimator {

    private final int BASE_DURATION = 300;
    private DoubleProperty animationSpeed = new SimpleDoubleProperty(1.0);
    private DoubleProperty animationDuration = new SimpleDoubleProperty();
    private Interpolator interpolator = Interpolator.EASE_BOTH;


    public List<Transition>  parallelList = new ArrayList<>();
    public List<Transition> transitionQueue = new ArrayList<>();
    public List<Transition> traversalList = new ArrayList<>();
    
    private static TreeAnimator instance = new TreeAnimator();
    
    public static TreeAnimator getInstance(){
        return instance;
    }
    
    
    private TreeAnimator() {
        // Private constructor to prevent instantiation
        animationDuration.bind(animationSpeed.multiply(BASE_DURATION));
    }

    public Transition fadeNode(Node node, double from, double to) {
        FadeTransition fade = new FadeTransition(Duration.millis(animationDuration.get()), node);
        fade.setInterpolator(Interpolator.EASE_IN);
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setInterpolator(interpolator);
        return fade;
    }

    public Transition moveNode(Node node, double byX, double byY) {
        
        double distance = Math.sqrt(Math.pow(byX, 2) + Math.pow(byY,2));
        int duration = calculateDuration(distance);
        TranslateTransition translate = new TranslateTransition(Duration.millis(duration), node);
        translate.setByX(byX);
        translate.setByY(byY);
        translate.setInterpolator(interpolator);
        return translate;
    }

    public Transition moveNode(Node node, double byX, double byY , double multiplier) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(animationDuration.get() * multiplier), node);
        translate.setByX(byX);
        translate.setByY(byY);
        translate.setInterpolator(interpolator);
        return translate;
    }

    private int calculateDuration(double delta){
        //double normalizedDelta = Math.log1p(delta); // log(1 + delta)
        double normalizedDelta = Math.sqrt(Math.abs(delta));
        double duration = animationDuration.get() * normalizedDelta;
        
        // Limitar duración máxima y mínima
        duration = Math.max(animationDuration.get(), Math.min(1.4 * animationDuration.get(), duration)); // entre 200ms y 2000ms

        return (int) duration;
    }

    public Transition scaleNode(Node node, double from, double to) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(animationDuration.get()), node);
        scale.setFromX(from);
        scale.setToX(to);
        scale.setFromY(from);
        scale.setToY(to);
        scale.setInterpolator(interpolator);
        return scale;
    }
    
    public Transition colorTransition(javafx.scene.shape.Shape shape, Color fromColor, Color toColor) {
        FillTransition fillTransition = new FillTransition(Duration.millis(animationDuration.get()), shape);
        fillTransition.setFromValue(fromColor);
        fillTransition.setToValue(toColor);
        fillTransition.setInterpolator(interpolator);
        return fillTransition;
    }   

    public SequentialTransition highlightKeyView(KeyView keyView) {
        Rectangle rect = keyView.getKeyShape();
        Text text = keyView.getKeyLabel();

        Color originalStroke = (Color) rect.getStroke();
        Color highlightStroke = Color.web("#bca20eff"); // Amarillo dorado

        Color originalText = (Color) text.getFill();
        Color highlightText = Color.web("#bca20eff"); // Blanco

        // Transiciones para el borde
        StrokeTransition strokeHighlight = new StrokeTransition(Duration.millis(animationDuration.get()), rect, originalStroke, highlightStroke);
        strokeHighlight.setInterpolator(interpolator);

        StrokeTransition strokeRestore = new StrokeTransition(Duration.millis(animationDuration.get()), rect, highlightStroke, originalStroke);
        strokeRestore.setInterpolator(interpolator);

        // 🔹 Transiciones para el texto (usando FillTransition en lugar de Transition manual)
        FillTransition textHighlight = new FillTransition(Duration.millis(250), text, originalText, highlightText);
        textHighlight.setInterpolator(interpolator);

        FillTransition textRestore = new FillTransition(Duration.millis(250), text, highlightText, originalText);
        textRestore.setInterpolator(interpolator);

        // 🔹 Agrupar en paralelo
        ParallelTransition highlightPhase = new ParallelTransition(strokeHighlight, textHighlight);
        ParallelTransition restorePhase = new ParallelTransition(strokeRestore, textRestore);

        // 🔹 Secuencia completa
        return new SequentialTransition(highlightPhase, restorePhase);
    }

  
    public SequentialTransition highlightEdge(Edge edge) {

        Shape body;
        if (edge instanceof TreeEdge ) {
            body = ((TreeEdge) edge).getBody();
        }else {
            body = ((LeafLinkEdge) edge).getBody();
        }
        Color originalStroke = (Color) edge.getColor();
        Color highlightStroke = Color.web("#bca20eff"); // Azul claro


        StrokeTransition highlightCurve = new StrokeTransition(Duration.millis(animationDuration.get()), body, originalStroke, highlightStroke);
        FillTransition highlightArrowHead = new FillTransition(Duration.millis(animationDuration.get()), edge.getHead(), originalStroke, highlightStroke);


        StrokeTransition restoreCurve = new StrokeTransition(Duration.millis(animationDuration.get()), body, highlightStroke, originalStroke);
        FillTransition restoreArrowHead = new FillTransition(Duration.millis(animationDuration.get()), edge.getHead(), highlightStroke, originalStroke);

        highlightCurve.setInterpolator(interpolator);
        restoreCurve.setInterpolator(interpolator);
        highlightArrowHead.setInterpolator(interpolator);
        restoreArrowHead.setInterpolator(interpolator);
        ParallelTransition highlight = new ParallelTransition(highlightCurve, highlightArrowHead);
        ParallelTransition restore = new ParallelTransition(restoreCurve, restoreArrowHead);

        return new SequentialTransition(highlight, restore);
    }

    public void addParallelTransition(List<Transition> transitions) {
        ParallelTransition parallelTransition = new ParallelTransition();
        for (Transition transition : transitions) {
            if (transition != null) {
                parallelTransition.getChildren().add(transition);
            }
        }
        transitionQueue.add(parallelTransition);
    }

    public void createParallelTransition() {
        if (parallelList.isEmpty()) return;
        ParallelTransition parallelTransition = new ParallelTransition();
        for (Transition transition : parallelList) {
            if (transition != null) {
                parallelTransition.getChildren().add(transition);
            }
        }
        transitionQueue.add(parallelTransition);
        parallelList.clear();
    }

    public void addTransitionToQueue(Transition transition) {
        if (transition != null) {
            transitionQueue.add(transition);
        }
    }

    public void addSequentialTransition(List<Transition> transitions) {
        SequentialTransition sequentialTransition = new SequentialTransition();
        for (Transition transition : transitions) {
            if (transition != null) {
                sequentialTransition.getChildren().add(transition);
            }
        }
        transitionQueue.add(sequentialTransition);
    }

    public void addParallelTransition(Transition transition) {
        if (transition != null) {
            parallelList.add(transition);
        }
    }

    public void clearQueue() {
        transitionQueue.clear();
    }

    public void clearParallelList(){
        parallelList.clear();
    }

    public void animateQueue() {
        SequentialTransition traversal = new SequentialTransition();
        traversal.getChildren().addAll(traversalList);
        SequentialTransition seqTransitions = new SequentialTransition();
        seqTransitions.getChildren().addAll(transitionQueue);
        clearQueue();
        traversal.setOnFinished(_ -> {
            traversalList.clear();
            seqTransitions.play();
        });
        traversal.play();
    }



    public void combineLastsTransitionsOnQueue(int numOfTransitions) {
        int size = transitionQueue.size();
        if (size < numOfTransitions) return;

        Transition lastTransition, secondLast;
        for (int i = 1; i < numOfTransitions; i++){
            lastTransition = transitionQueue.removeLast();
            secondLast = transitionQueue.removeLast();
            ParallelTransition combinedTransitions = new ParallelTransition(lastTransition,secondLast);
            transitionQueue.add(combinedTransitions);
        }

    }
    

    public void addListenerToLastTransition(Runnable onFinished) {
        int size = transitionQueue.size();
        if (size == 0) return;

        Transition last = transitionQueue.get(size - 1);
        last.setOnFinished(_ -> onFinished.run());
    }

    /**
     * Creates a smooth transition for any property using a custom Transition
     */
    public Transition animateProperty(Property<Number> property, double fromValue, double toValue)  {
        return new Transition() {
            private final double startValue = fromValue;
            private final double endValue = toValue;
            int duration = calculateDuration(toValue - fromValue);
            {
                setCycleDuration(Duration.millis(duration));
                //setCycleDuration(ANIMATION_DURATION);
                // Set initial value
                property.setValue(fromValue);
            }
            
            @Override
            protected void interpolate(double frac) {
                double value = startValue + (endValue - startValue) * frac;
                property.setValue(value);
            }
        };
    }
    
    /**
     * Overload for DoubleProperty specifically
     */
    public Transition animateProperty(DoubleProperty property, double fromValue, double toValue) {
        return animateProperty((Property<Number>)property, fromValue, toValue);
    }

    public SequentialTransition animateTextChange(Text text, String newText) {
        SequentialTransition seqTransition = new SequentialTransition();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(animationDuration.get()/2), text);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(interpolator);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(animationDuration.get()/2), text);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(interpolator);

        fadeOut.setOnFinished(_ -> text.setText(newText));

        seqTransition.getChildren().addAll(fadeOut, fadeIn);
        return seqTransition;

    } 

    public void addToTraversalList(Transition transition) {
        if (transition != null) {
            traversalList.add(transition);
        }
    }

    public DoubleProperty getAnimationSpeedProperty() {
        return animationSpeed;
    }

    public void setSpeedAnimation(double newSpeed) {
        this.animationSpeed.set(newSpeed);
    }

    public enum Theme {
        DAY, NIGHT
    }

    public static class Palette {
        public final Color nodeHighlight;
        public final Color keyHighlight;
        public final Color arrowHighlight;
        public final Color nodeStroke;
        public final Color keyStroke;
        public final Color arrowStroke;

        public Palette(Color nodeHighlight, Color keyHighlight, Color arrowHighlight,
                       Color nodeStroke, Color keyStroke, Color arrowStroke) {
            this.nodeHighlight = nodeHighlight;
            this.keyHighlight = keyHighlight;
            this.arrowHighlight = arrowHighlight;
            this.nodeStroke = nodeStroke;
            this.keyStroke = keyStroke;
            this.arrowStroke = arrowStroke;
        }
    }

    public static final Palette DAY_PALETTE = new Palette(
        Color.web("#FFD700"), // nodeHighlight (gold)
        Color.web("#FFFACD"), // keyHighlight (lemon chiffon)
        Color.web("#00BFFF"), // arrowHighlight (deep sky blue)
        Color.web("#333333"), // nodeStroke
        Color.web("#333333"), // keyStroke
        Color.web("#333333")  // arrowStroke
    );

    public static final Palette NIGHT_PALETTE = new Palette(
        Color.web("#FF8C00"), // nodeHighlight (dark orange)
        Color.web("#FF4500"), // keyHighlight (orange red)
        Color.web("#1E90FF"), // arrowHighlight (dodger blue)
        Color.web("#CCCCCC"), // nodeStroke
        Color.web("#CCCCCC"), // keyStroke
        Color.web("#CCCCCC")  // arrowStroke
    );

    private static Palette currentPalette = DAY_PALETTE;

    public static void setTheme(Theme theme) {
        currentPalette = (theme == Theme.DAY) ? DAY_PALETTE : NIGHT_PALETTE;
    }

    public static Palette getPalette() {
        return currentPalette;
    }

}
