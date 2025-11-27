package com.arturoar.util;

import java.util.ArrayList;
import java.util.List;

import com.arturoar.view.Edge;
import com.arturoar.view.KeyView;
import javafx.scene.paint.Paint;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class TreeAnimator {

    private final int BASE_DURATION = 300;
    private DoubleProperty animationSpeed = new SimpleDoubleProperty(1.0);
    private DoubleProperty animationDuration = new SimpleDoubleProperty();
    private BooleanProperty darkMode = new SimpleBooleanProperty();
    private Interpolator interpolator = Interpolator.EASE_BOTH;
    private BooleanProperty mute = new SimpleBooleanProperty(false);

    private Runnable globalFinishCallback = null;


    public List<Transition>  parallelList = new ArrayList<>();
    public List<Transition> transitionQueue = new ArrayList<>();
    public List<Transition> traversalList = new ArrayList<>();

    private List<Pair<Runnable, Integer>> scheduleTask = null;

    private static TreeAnimator instance = new TreeAnimator();
    

    public static TreeAnimator getInstance(){
        return instance;
    }
    
    
    private TreeAnimator() {
        // Private constructor to prevent instantiation
        animationDuration.bind(animationSpeed.multiply(BASE_DURATION));
        try {
            SoundType.loadAllSounds();
        } catch (Exception exception) {
            System.err.println("Error loading sounds: " + exception);
        }
    }

    public void setGlobalFinishCallback(Runnable callback) {
        this.globalFinishCallback = callback;
    }

    public Transition fadeNode(Node node, double from, double to, SoundType sound) {
       
        FadeTransition fade = new FadeTransition(Duration.millis(animationDuration.get()), node);
        fade.setInterpolator(Interpolator.EASE_IN);
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setInterpolator(interpolator);

        if (!mute.get() && sound != null){
            fade.statusProperty().addListener((_, oldStatus, newStatus)->{
                if (newStatus == Animation.Status.RUNNING && oldStatus == Animation.Status.STOPPED) {
                    if (sound != null) sound.play();
                }
            });
        }

        return fade;
    }

    public Transition moveNode(Node node, double byX, double byY, SoundType sound) {
        
        double distance = Math.sqrt(Math.pow(byX, 2) + Math.pow(byY,2));
        int duration = calculateDuration(distance);
        TranslateTransition translate = new TranslateTransition(Duration.millis(duration), node);
        translate.setByX(byX);
        translate.setByY(byY);
        translate.setInterpolator(interpolator);

        if (!mute.get() && sound != null){
            translate.statusProperty().addListener((_, oldStatus, newStatus)->{
                if (newStatus == Animation.Status.RUNNING && oldStatus == Animation.Status.STOPPED) {
                    if (sound != null) sound.play();
                }
            });
        }

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
    
    public Transition colorTransition(Duration duration, ObjectProperty<Paint> color, Paint fromColor, Paint toColor) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(color, fromColor)),
            new KeyFrame(duration, 
                new KeyValue(color, toColor))
        );        
        return new SequentialTransition(timeline);
    }   

    public PauseTransition pauseTransition(int miliseconds){
        return new PauseTransition(Duration.millis(miliseconds));
    }

    public SequentialTransition highlightKeyView(KeyView keyView, SoundType sound) {

        Paint highlightColor = Color.web("#bca20eff");
        Paint originalColor = keyView.colorProperty().get();

        Transition highlightTransition = colorTransition(Duration.millis(animationDuration.get()),keyView.colorProperty(), originalColor, highlightColor);

        if (!mute.get() && sound != null){
            highlightTransition.statusProperty().addListener((_, oldStatus, newStatus)->{
                if (newStatus == Animation.Status.RUNNING && oldStatus == Animation.Status.STOPPED) {
                    if (sound != null) sound.play();
                }
            });
        }

        Transition restoreTransition = colorTransition(Duration.millis(animationDuration.get()),keyView.colorProperty(), highlightColor, originalColor);

        return new SequentialTransition(highlightTransition, restoreTransition);
}

public SequentialTransition highlightData(Label data, SoundType sound) {
    Paint highlightColor = Color.web("#bca20eff");
    Paint originalColor = data.getTextFill();
    
    // Transición de color
    Transition highlightText = colorTransition(Duration.millis(800), data.textFillProperty(), originalColor, highlightColor);
    
   
    Transition moveDownData = moveNode(data, 0, 10, 0.5);
    Transition moveUpData = moveNode(data, 0, -10, 0.5);
    

    // Sonido
    if (!mute.get() && sound != null) {
        highlightText.statusProperty().addListener((_, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.RUNNING && oldStatus == Animation.Status.STOPPED) {
                sound.play();
            }
        });
    }
    
    Transition pause = new PauseTransition(Duration.millis(100));
    Transition restoreText = colorTransition(Duration.millis(800), data.textFillProperty(), highlightColor, originalColor);
    
    return new SequentialTransition(
        new ParallelTransition(highlightText, moveDownData),
        pause,
        new ParallelTransition(restoreText, moveUpData)
    );
}

   


    public SequentialTransition highlightEdge(Edge edge, SoundType sound) {


        Paint originalColor = edge.colorProperty().get();

        Paint highlighColor = Color.web("#bca20eff");

        Transition highlightTransition = colorTransition(Duration.millis(animationDuration.get()),edge.colorProperty(), originalColor, highlighColor);

         if (!mute.get() && sound != null){
            highlightTransition.statusProperty().addListener((_, oldStatus, newStatus)->{
                if (newStatus == Animation.Status.RUNNING && oldStatus == Animation.Status.STOPPED) {
                    if (sound != null) sound.play();
                }
            });
        }

        Transition restoreTransition = colorTransition(Duration.millis(animationDuration.get()),edge.colorProperty(), highlighColor, originalColor);

        return new SequentialTransition(highlightTransition, restoreTransition);
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
        addTransitionToQueue(parallelTransition);
        parallelList.clear();
    }

    public void addTransitionToQueue(Transition transition) {
        if (transition != null) {
            transitionQueue.add(transition);
        }
        if (scheduleTask != null && !scheduleTask.isEmpty()) {
            List<Pair<Runnable, Integer>> tasksToRemove = new ArrayList<>();
            for (Pair<Runnable, Integer> taskPair : scheduleTask) {
                Runnable task = taskPair.getKey();
                int delay = taskPair.getValue();
                delay = delay - 1;

                if (delay <= 0) {
                    addListenerToLastTransition(() -> task.run());
                    tasksToRemove.add(taskPair);
                } else {
                    taskPair = new Pair<>(task, delay);
                }
            }
            scheduleTask.removeAll(tasksToRemove);
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
        traversalList.clear();

        SequentialTransition seqTransitions = new SequentialTransition();
        seqTransitions.getChildren().addAll(transitionQueue);
        clearQueue();

         seqTransitions.setOnFinished(_ -> {
            if (globalFinishCallback != null) {
                globalFinishCallback.run(); // ¡Botones habilitados aquí!
                globalFinishCallback = null; // Limpiar el estado
            }
        });
        
        traversal.setOnFinished(_ -> {
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
    if (size == 0) {
        
        return;
    }

    Transition last = transitionQueue.get(size - 1);
    

    final EventHandler<ActionEvent> currentHandler = last.getOnFinished();
    
   
    last.setOnFinished(event -> {
        if (currentHandler != null) {
            currentHandler.handle(event);  
        }
        onFinished.run();                  
    });
}

    public void addScheduleTask(Runnable task, int delayTransitions) {
        if (scheduleTask == null) {
            scheduleTask = new ArrayList<>();
        }
        scheduleTask.add(new Pair<>(task, delayTransitions));
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

    public DoubleProperty animationSpeedProperty() {
        return animationSpeed;
    }

    public void setSpeedAnimation(double newSpeed) {
        this.animationSpeed.set(newSpeed);
    }

    public BooleanProperty darkModeProperty(){
        return darkMode;
    }

    public BooleanProperty muteProperty(){
        return mute;
    }

    public class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
    
    public long getTotalDuration(){
        long totalDuration = 0;
        for (Transition transition : transitionQueue) {
            totalDuration += transition.getCycleDuration().toMillis();
        }
        return totalDuration;
    }
     
}