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
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TreeAnimator {

    private static final Duration ANIMATION_DURATION = Duration.millis(500);
    private static Interpolator interpolator = Interpolator.EASE_IN;

    public static List<Transition>  parallelList = new ArrayList<>();
    public static List<Transition> transitionQueue = new ArrayList<>();

    private TreeAnimator() {
        // Private constructor to prevent instantiation
    }

    public static Transition fadeNode(Node node, double from, double to) {
        FadeTransition fade = new FadeTransition(Duration.millis(ANIMATION_DURATION.toMillis()), node);
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setInterpolator(interpolator);
        return fade;
    }

    public static Transition moveNode(Node node, double byX, double byY) {
        TranslateTransition translate = new TranslateTransition(Duration.millis(ANIMATION_DURATION.toMillis() / 2), node);
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

    

}
