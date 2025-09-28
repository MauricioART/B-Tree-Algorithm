package com.arturoar.util;

import java.util.ArrayList;
import java.util.List;

import com.arturoar.view.Arrow;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TreeAnimator {

    private static final Duration ANIMATION_DURATION = Duration.millis(800);
    private static Interpolator interpolator = Interpolator.EASE_BOTH;

    public static List<Transition>  parallelList = new ArrayList<>();
    public static List<Transition> transitionQueue = new ArrayList<>();

    private TreeAnimator() {
        // Private constructor to prevent instantiation
    }

    public static Transition fadeNode(Node node, double from, double to) {
        FadeTransition fade = new FadeTransition(Duration.millis(ANIMATION_DURATION.toMillis()/4), node);
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
        SequentialTransition seqTransitions = new SequentialTransition();
        seqTransitions.getChildren().addAll(TreeAnimator.transitionQueue);
        TreeAnimator.clearQueue();
        seqTransitions.play();
    }

    public static Transition moveArrowEnd(Arrow arrow, double fromX, double toX) {
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

}
