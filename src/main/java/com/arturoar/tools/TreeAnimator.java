package com.arturoar.tools;

import java.util.ArrayList;
import java.util.List;

import com.arturoar.view.NodeView;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
//import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public class TreeAnimator {

    private static final Duration ANIMATION_DURATION = Duration.millis(2000);
    //private static Interpolator interpolator = Interpolator.EASE_IN;
    

    public static List<Transition>  parallelList = new ArrayList<>();

    public static List<Transition> transitions = new ArrayList<>();

 
    private TreeAnimator() {
        // Private constructor to prevent instantiation
    }

    public static Transition fadeNode(Node node, double from, double to) {
        FadeTransition fade = new FadeTransition(Duration.millis(ANIMATION_DURATION.toMillis() / 2), node);
        fade.setFromValue(from);
        fade.setToValue(to);
       // fade.setDelay(ANIMATION_DURATION);
        return fade;
    }

    public static Transition moveNode(Node node, double byX, double byY) {
        TranslateTransition translate = new TranslateTransition(ANIMATION_DURATION, node);
        translate.setByX(byX);
        translate.setByY(byY);
        return translate;
    }

    private static Transition scaleNode(Node node, double from, double to) {
        ScaleTransition scale = new ScaleTransition(ANIMATION_DURATION, node);
        scale.setFromX(from);
        scale.setToX(to);
        scale.setFromY(from);
        scale.setToY(to);
        return scale;
    }
    
    public static Transition colorTransition(javafx.scene.shape.Shape shape, Color fromColor, Color toColor) {
        FillTransition fillTransition = new FillTransition(ANIMATION_DURATION, shape);
        fillTransition.setFromValue(fromColor);
        fillTransition.setToValue(toColor);
        return fillTransition;
    }   

    public static Transition animateKeyCreation(NodeView nodeView) {

        Transition fadeIn = fadeNode(nodeView, 0, 1);
        Transition scaleUp = scaleNode(nodeView, 0.5, 1);

        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleUp);
        return parallelTransition;
    }

    public static Transition animateKeyDeletion(NodeView nodeView) {
        Transition fadeOut = fadeNode(nodeView, 1, 0);
        Transition scaleDown = scaleNode(nodeView, 1, 0.5);

        ParallelTransition parallelTransition = new ParallelTransition(fadeOut, scaleDown);
        return parallelTransition;
    }

    public static void addParallelTransition(List<Transition> transitions) {
        ParallelTransition parallelTransition = new ParallelTransition();
        for (Transition transition : transitions) {
            if (transition != null) {
                parallelTransition.getChildren().add(transition);
            }
        }
        TreeAnimator.transitions.add(parallelTransition);
    }

    public static void createParallelTransition() {
        if (parallelList.isEmpty()) return;
        ParallelTransition parallelTransition = new ParallelTransition();
        for (Transition transition : parallelList) {
            if (transition != null) {
                parallelTransition.getChildren().add(transition);
            }
        }
        TreeAnimator.transitions.add(parallelTransition);
        TreeAnimator.parallelList.clear();
    }

    public static void addTransitionToQueue(Transition transition) {
        if (transition != null) {
            TreeAnimator.transitions.add(transition);
        }
    }

    public static void addSequentialTransition(List<Transition> transitions) {
        SequentialTransition sequentialTransition = new SequentialTransition();
        for (Transition transition : transitions) {
            if (transition != null) {
                sequentialTransition.getChildren().add(transition);
            }
        }
        TreeAnimator.transitions.add(sequentialTransition);
    }

    public static void addParallelTransition(Transition transition) {
        if (transition != null) {
            TreeAnimator.parallelList.add(transition);
        }
    }

    public static void clearQueue() {
        TreeAnimator.transitions.clear();
    }

    public static void clearParallelList(){
        TreeAnimator.parallelList.clear();
    }

    public static void animateQueue() {
        SequentialTransition seqTransitions = new SequentialTransition();
        seqTransitions.getChildren().addAll(TreeAnimator.transitions);
        TreeAnimator.clearQueue();
        seqTransitions.play();
    }

}
