package com.arturoar.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class TreeAnimator {
    private static final Duration ANIMATION_DURATION = Duration.millis(2000);

    public static Transition createFading(Node node, double from, double to) {
        FadeTransition fade = new FadeTransition(ANIMATION_DURATION, node);
        fade.setFromValue(from);
        fade.setToValue(to);
        return fade;
    }

    public static Transition createTranslate(Node node, double fromX, double toX, double fromY, double toY) {
        TranslateTransition translate = new TranslateTransition(ANIMATION_DURATION, node);
        translate.setFromX(fromX);
        translate.setToX(toX);
        translate.setFromY(fromY);
        translate.setToY(toY);
        return translate;
    }
}
