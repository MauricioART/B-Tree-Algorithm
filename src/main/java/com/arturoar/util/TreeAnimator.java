package com.arturoar.util;

import java.util.ArrayList;
import java.util.List;

import com.arturoar.view.Edge;
import com.arturoar.view.KeyView;
import com.arturoar.view.NodeView;

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
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TreeAnimator {

    private static final Duration ANIMATION_DURATION = Duration.millis(600);
    private static Interpolator interpolator = Interpolator.EASE_IN;


    public static List<Transition>  parallelList = new ArrayList<>();
    public static List<Transition> transitionQueue = new ArrayList<>();
    public static List<Transition> traversalList = new ArrayList<>();

    private TreeAnimator() {
        // Private constructor to prevent instantiation
    }

    public static Transition fadeNode(Node node, double from, double to) {
        FadeTransition fade = new FadeTransition(Duration.millis(ANIMATION_DURATION.toMillis() * 0.4), node);
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setInterpolator(interpolator);
        return fade;
    }

    public static Transition moveNode(Node node, double byX, double byY) {
        
        TranslateTransition translate = new TranslateTransition(ANIMATION_DURATION, node);
        translate.setByX(byX);
        translate.setByY(byY);
        translate.setInterpolator(interpolator);
        return translate;
    }

    public static Transition moveNode(Node node, double byX, double byY , double multiplier) {
        
        TranslateTransition translate = new TranslateTransition(Duration.millis(ANIMATION_DURATION.toMillis() * multiplier), node);
        translate.setByX(byX);
        translate.setByY(byY);
        translate.setInterpolator(interpolator);
        return translate;
    }

    public static Transition scaleNode(Node node, double from, double to) {
        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, node);
        scale.setFromX(from);
        scale.setToX(to);
        scale.setFromY(from);
        scale.setToY(to);
        scale.setInterpolator(interpolator);
        return scale;
    }
    
    public static Transition colorTransition(javafx.scene.shape.Shape shape, Color fromColor, Color toColor) {
        FillTransition fillTransition = new FillTransition(ANIMATION_DURATION, shape);
        fillTransition.setFromValue(fromColor);
        fillTransition.setToValue(toColor);
        fillTransition.setInterpolator(interpolator);
        return fillTransition;
    }   

    public static SequentialTransition highlightNode(NodeView node) {
        Rectangle shape = node.getNodeShape();
        Color original = (Color) shape.getStroke();
        Color highlightColor = Color.web("#bca20eff"); // Amarillo dorado

        FadeTransition fadeIn = new FadeTransition(ANIMATION_DURATION.divide(2), shape);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(interpolator);
        StrokeTransition highlightStroke = new StrokeTransition(ANIMATION_DURATION.divide(2), shape, original, highlightColor);

        ParallelTransition highlight = new ParallelTransition(fadeIn, highlightStroke);

        FadeTransition fadeOut = new FadeTransition(ANIMATION_DURATION.divide(2), shape);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(interpolator);

        StrokeTransition restoreTransition = new StrokeTransition(ANIMATION_DURATION.divide(2), shape, highlightColor, original);

        ParallelTransition restore = new ParallelTransition(fadeOut, restoreTransition);

        SequentialTransition seq = new SequentialTransition(highlight, restore);
        return seq;
    }

    /*
    
    public static SequentialTransition highlightKeyView(KeyView keyView) {
        Rectangle rect = keyView.getKeyShape();
        Text text = keyView.getKeyLabel();

        Color originalStroke = (Color) rect.getStroke();
        Color highlightStroke = Color.web("#bca20eff"); // Amarillo dorado

        Color originalText = (Color) text.getFill();
        Color highlightText = Color.web("#FFF"); // Naranja fuerte

        // Transición para el borde
        FillTransition strokeHighlight = new FillTransition(ANIMATION_DURATION, rect);
        strokeHighlight.setFromValue(originalStroke);
        strokeHighlight.setToValue(highlightStroke);
        strokeHighlight.setInterpolator(interpolator);

        FillTransition strokeRestore = new FillTransition(ANIMATION_DURATION, rect);
        strokeRestore.setFromValue(highlightStroke);
        strokeRestore.setToValue(originalStroke);
        strokeRestore.setInterpolator(interpolator);

        // Transición para el texto
        Transition textHighlight = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
                setInterpolator(interpolator);
            }
            @Override
            protected void interpolate(double frac) {
                text.setFill(frac < 1.0 ? highlightText : originalText);
            }
        };

        Transition textRestore = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
                setInterpolator(interpolator);
            }
            @Override
            protected void interpolate(double frac) {
                text.setFill(frac < 1.0 ? originalText : highlightText);
            }
        };

        SequentialTransition seq = new SequentialTransition(
            strokeHighlight, textHighlight, strokeRestore, textRestore
        );
        return seq;
    }
    */
    public static SequentialTransition highlightKeyView(KeyView keyView) {
    Rectangle rect = keyView.getKeyShape();
    Text text = keyView.getKeyLabel();

    Color originalStroke = (Color) rect.getStroke();
    Color highlightStroke = Color.web("#bca20eff"); // Amarillo dorado

    Color originalText = (Color) text.getFill();
    Color highlightText = Color.web("#bca20eff"); // Blanco

    // Transiciones para el borde
    StrokeTransition strokeHighlight = new StrokeTransition(ANIMATION_DURATION, rect, originalStroke, highlightStroke);
    strokeHighlight.setInterpolator(interpolator);

    StrokeTransition strokeRestore = new StrokeTransition(ANIMATION_DURATION, rect, highlightStroke, originalStroke);
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

  
    public static SequentialTransition highlightEdge(Edge arrow) {

        Color originalStroke = (Color) arrow.getArrowColor();
        Color highlightStroke = Color.web("#bca20eff"); // Azul claro


        StrokeTransition highlightCurve = new StrokeTransition(ANIMATION_DURATION, arrow.getCurve(), originalStroke, highlightStroke);
        StrokeTransition highlightArrowHead = new StrokeTransition(ANIMATION_DURATION, arrow.getArrowHead(), originalStroke, highlightStroke);
        
        
        StrokeTransition restoreCurve = new StrokeTransition(ANIMATION_DURATION, arrow.getCurve(), highlightStroke, originalStroke);
        StrokeTransition restoreArrowHead = new StrokeTransition(ANIMATION_DURATION, arrow.getArrowHead(), highlightStroke, originalStroke);

        highlightCurve.setInterpolator(interpolator);
        restoreCurve.setInterpolator(interpolator);
        highlightArrowHead.setInterpolator(interpolator);
        restoreArrowHead.setInterpolator(interpolator);
        ParallelTransition highlight = new ParallelTransition(highlightCurve, highlightArrowHead);
        ParallelTransition restore = new ParallelTransition(restoreCurve, restoreArrowHead);

        return new SequentialTransition(highlight, restore);
    }

    public static void addParallelTransition(List<Transition> transitions) {
        ParallelTransition parallelTransition = new ParallelTransition();
        for (Transition transition : transitions) {
            if (transition != null) {
                parallelTransition.getChildren().add(transition);
            }
        }
        TreeAnimator.transitionQueue.add(parallelTransition);
    }

    public static void createParallelTransition() {
        if (parallelList.isEmpty()) return;
        ParallelTransition parallelTransition = new ParallelTransition();
        for (Transition transition : parallelList) {
            if (transition != null) {
                parallelTransition.getChildren().add(transition);
            }
        }
        TreeAnimator.transitionQueue.add(parallelTransition);
        TreeAnimator.parallelList.clear();
    }

    public static void addTransitionToQueue(Transition transition) {
        if (transition != null) {
            TreeAnimator.transitionQueue.add(transition);
        }
    }

    public static void addSequentialTransition(List<Transition> transitions) {
        SequentialTransition sequentialTransition = new SequentialTransition();
        for (Transition transition : transitions) {
            if (transition != null) {
                sequentialTransition.getChildren().add(transition);
            }
        }
        TreeAnimator.transitionQueue.add(sequentialTransition);
    }

    public static void addParallelTransition(Transition transition) {
        if (transition != null) {
            TreeAnimator.parallelList.add(transition);
        }
    }

    public static void clearQueue() {
        TreeAnimator.transitionQueue.clear();
    }

    public static void clearParallelList(){
        TreeAnimator.parallelList.clear();
    }

    public static void animateQueue() {
        SequentialTransition traversal = new SequentialTransition();
        traversal.getChildren().addAll(TreeAnimator.traversalList);
        SequentialTransition seqTransitions = new SequentialTransition();
        seqTransitions.getChildren().addAll(TreeAnimator.transitionQueue);
        TreeAnimator.clearQueue();
        traversal.setOnFinished(_ -> {
            TreeAnimator.traversalList.clear();
            seqTransitions.play();
        });
        traversal.play();
    }


    public static Transition moveArrowEnd(Edge arrow, double fromX, double toX) {
        Transition transition = new Transition() {
            {
                setCycleDuration(ANIMATION_DURATION);
                setInterpolator(interpolator);
            }
            @Override
            protected void interpolate(double frac) {
                double value = fromX + (toX - fromX) * frac;
                arrow.endXProperty().set(value);
            }
        };
        return transition;
    }

    public static void combineLastTransitionsOnQueue() {
        int size = TreeAnimator.transitionQueue.size();
        if (size < 2) return;

        Transition last = TreeAnimator.transitionQueue.get(size - 1);
        Transition secondLast = TreeAnimator.transitionQueue.get(size - 2);

        ParallelTransition combined = new ParallelTransition();
        combined.getChildren().addAll(secondLast, last);

        TreeAnimator.transitionQueue.remove(size - 1);
        TreeAnimator.transitionQueue.remove(size - 2);
        TreeAnimator.transitionQueue.add(combined);
    }
    

    public static void addListenerToLastTransition(Runnable onFinished) {
        int size = TreeAnimator.transitionQueue.size();
        if (size == 0) return;

        Transition last = TreeAnimator.transitionQueue.get(size - 1);
        last.setOnFinished(_ -> onFinished.run());
    }

    /**
     * Creates a smooth transition for any property using a custom Transition
     */
    public static Transition animateProperty(Property<Number> property, double fromValue, double toValue)  {
        return new Transition() {
            private final double startValue = fromValue;
            private final double endValue = toValue;
            
            {
                setCycleDuration(ANIMATION_DURATION);
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
    public static Transition animateProperty(DoubleProperty property, double fromValue, double toValue) {
        return animateProperty((Property<Number>)property, fromValue, toValue);
    }

    public static SequentialTransition animateTextChange(Text text, String newText) {
        SequentialTransition seqTransition = new SequentialTransition();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(ANIMATION_DURATION.toMillis()/2), text);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(interpolator);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(ANIMATION_DURATION.toMillis()/2), text);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(interpolator);

        fadeOut.setOnFinished(_ -> text.setText(newText));

        seqTransition.getChildren().addAll(fadeOut, fadeIn);
        return seqTransition;

    } 

    public static void addToTraversalList(Transition transition) {
        if (transition != null) {
            TreeAnimator.traversalList.add(transition);
        }
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
