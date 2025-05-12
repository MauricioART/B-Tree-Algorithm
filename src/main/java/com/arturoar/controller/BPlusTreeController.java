package com.arturoar.controller;

import com.arturoar.model.*;
import com.arturoar.view.*;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BPlusTreeController implements BPlusTreeObserver<Integer, String> {

    private static final double SCALE_DELTA = 1.1;
    private static final double DEFAULT_X_SPACING = 20.0;
    private static final double DEFAULT_Y_SPACING = 80.0;
    private static final int DEFAULT_BRANCHING_FACTOR = 3;

    @FXML private Pane canvas;
    @FXML private Button insertBtn;
    @FXML private Button removeBtn;
    @FXML private Button searchBtn;
    @FXML private Button homeBtn;

    private final Scale scaleTransform = new Scale(1, 1);
    private final Translate translateTransform = new Translate();
    private final BPlusTree<Integer, String> tree;
    private final Group treeGroup = new Group();
    private final Map<Key<Integer>, BPlusKeyView> keyToKeyView = new HashMap<>();
    private final Map<BPlusNode<Integer, String>, BPlusNodeView> nodeToNodeView = new HashMap<>();
    private final Map<BPlusNode<Integer, String>, Arrow> childrenToArrow = new HashMap<>();
    private final ArrayDeque<List<BPlusNodeView>> snapshots = new ArrayDeque<>();
    private final List<List<BPlusNodeView>> treeLevels = new ArrayList<>();
    
    private double xSpacing = DEFAULT_X_SPACING;
    private double ySpacing = DEFAULT_Y_SPACING;
    private int branchingFactor = DEFAULT_BRANCHING_FACTOR;
    private double zoomFactor = 1.0;
    private double lastMouseX;
    private double lastMouseY;
    private Double centerX;
    private Double centerY;

    private Timeline currentAnimation;

    public BPlusTreeController() {
        this.tree = new BPlusTree<>(this.branchingFactor);
        BPlusNodeView rootView = new BPlusNodeView(true);
        this.nodeToNodeView.put(this.tree.getRoot(), rootView);
        this.tree.addObserver(this);
    }

    @FXML
    public void initialize() {
        setupTransforms();
        setupCanvas();
        setupButtonActions();
    }

    private void setupTransforms() {
        this.treeGroup.getTransforms().addAll(scaleTransform, translateTransform);
    }

    private void setupCanvas() {
        this.canvas.getChildren().add(treeGroup);
        this.canvas.setOnScroll(this::handleZoom);
        this.canvas.setOnMousePressed(this::handleMousePressed);
        this.canvas.setOnMouseDragged(this::handleMouseDragged);
    }

    private void setupButtonActions() {
        this.homeBtn.setOnAction(e -> resetView());
        this.insertBtn.setOnAction(e -> handleInsert());
        this.removeBtn.setOnAction(e -> handleRemove());
        this.searchBtn.setOnAction(e -> handleSearch());
    }

    private void handleInsert() {
        Dialog<String[]> dialog = createKeyValueDialog("New Node", "Insert the new key:", true);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (!insert(key, values[1])) {
                    showAlert(Alert.AlertType.WARNING, "Insertion Error", "Key already exists");
                }
                this.tree.showTree();
            } catch (NumberFormatException e) {
                showInputErrorAlert("Please enter a valid integer key.");
            }
        });
    }

    private void handleRemove() {
        Dialog<String[]> dialog = createKeyValueDialog("Remove Node", "Insert the key:", false);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (remove(key).getResult() == null) {
                    showAlert(Alert.AlertType.ERROR, "Removal Error", 
                            "The structure does not contain key " + key);
                }
            } catch (NumberFormatException e) {
                showInputErrorAlert("Please enter a valid integer key.");
            }
        });
    }

    private void handleSearch() {
        Dialog<String[]> dialog = createKeyValueDialog("Searching Node", "Insert the key:", false);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (search(key).getResult() == null) {
                    showAlert(Alert.AlertType.ERROR, "Search Error", 
                            "The structure does not contain key " + key);
                }
            } catch (NumberFormatException e) {
                showInputErrorAlert("Please enter a valid integer key.");
            }
        });
    }

    private Dialog<String[]> createKeyValueDialog(String title, String header, boolean includeDataField) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        
        ButtonType confirmButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);
        
        TextField keyField = new TextField();
        keyField.setPromptText("Key");
        VBox vbox = new VBox(keyField);
        
        if (includeDataField) {
            TextField dataField = new TextField();
            dataField.setPromptText("Data");
            vbox.getChildren().add(dataField);
        }
        
        dialog.getDialogPane().setContent(vbox);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButton) {
                String[] result = new String[includeDataField ? 2 : 1];
                result[0] = keyField.getText();
                if (includeDataField) {
                    result[1] = ((TextField) vbox.getChildren().get(1)).getText();
                }
                return result;
            }
            return null;
        });
        
        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void showInputErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, "Invalid Input", message);
    }

    private void handleZoom(ScrollEvent event) {
        double scaleFactor = event.getDeltaY() > 0 ? SCALE_DELTA : 1 / SCALE_DELTA;
        scaleTransform.setX(scaleTransform.getX() * scaleFactor);
        scaleTransform.setY(scaleTransform.getY() * scaleFactor);
        zoomFactor *= scaleFactor;
    }

    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getSceneX() - lastMouseX;
        double deltaY = event.getSceneY() - lastMouseY;
        
        translateTransform.setX(translateTransform.getX() + deltaX);
        translateTransform.setY(translateTransform.getY() + deltaY);
        
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    private void resetView() {
        scaleTransform.setX(1);
        scaleTransform.setY(1);
        translateTransform.setX(0);
        translateTransform.setY(0);
        zoomFactor = 1.0;
    }

    public boolean insert(Integer key, String data) {
        BPlusTraversalResult<Boolean, Integer> result = this.tree.insert(key, data);
        return result.getResult();
    }

    public BPlusTraversalResult<String, Integer> remove(Integer key) {
        return this.tree.remove(key);
    }

    public BPlusTraversalResult<String, Integer> search(Integer key) {
        return this.tree.search(key);
    }

     private void animateSearching(List<BPlusKeyView> nodes) {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        Timeline timeline = new Timeline();
        Duration duration = Duration.millis(300);
        
        // Highlight nodes in sequence
        for (int i = 0; i < nodes.size(); i++) {
            BPlusKeyView node = nodes.get(i);
            
            // Create highlight animation
            KeyFrame highlightFrame = new KeyFrame(
                duration.multiply(i),
                new KeyValue(node.fillColorProperty(), Color.YELLOW)
            );
            
            // Create unhighlight animation (unless it's the last node)
            if (i < nodes.size() - 1) {
                KeyFrame unhighlightFrame = new KeyFrame(
                    duration.multiply(i + 0.8),
                    new KeyValue(node.fillColorProperty(), Color.WHITE)
                );
                timeline.getKeyFrames().addAll(highlightFrame, unhighlightFrame);
            } else {
                timeline.getKeyFrames().add(highlightFrame);
            }
        }
        
        currentAnimation = timeline;
        timeline.play();
    }

    private void animateTransitions() {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        Timeline timeline = new Timeline();
        Duration duration = Duration.millis(500);

        keyToKeyView.forEach((key, nodeView) -> {
            if (!treeGroup.getChildren().contains(nodeView)) {
                treeGroup.getChildren().add(nodeView);
            }

            if (nodeView.getOriginX() != null && nodeView.getOriginY() != null) {
                // Animate position changes
                KeyValue xValue = new KeyValue(
                    nodeView.translateXProperty(),
                    nodeView.getNewOriginX() - nodeView.getOriginX()
                );
                KeyValue yValue = new KeyValue(
                    nodeView.translateYProperty(),
                    nodeView.getNewOriginY() - nodeView.getOriginY()
                );
                timeline.getKeyFrames().add(new KeyFrame(duration, xValue, yValue));
            } else {
                // Animate fade-in for new nodes
                nodeView.setOpacity(0);
                timeline.getKeyFrames().add(new KeyFrame(duration, 
                    new KeyValue(nodeView.opacityProperty(), 1)));
            }
        });

        // Add callback to update positions after animation completes
        timeline.setOnFinished(e -> {
            keyToKeyView.values().forEach(BPlusKeyView::updatePosition);
        });

        currentAnimation = timeline;
        timeline.play();
    }
/*
    private void animateTransitions() {
        ParallelTransition parallelTransition = new ParallelTransition();

        keyToKeyView.forEach((key, nodeView) -> {
            if (!treeGroup.getChildren().contains(nodeView)) {
                treeGroup.getChildren().add(nodeView);
            }

            Transition animation;
            if (nodeView.getOriginX() != null && nodeView.getOriginY() != null) {
                animation = TreeAnimator.createTranslate(
                    nodeView, 
                    nodeView.getOriginX(), nodeView.getNewOriginX(),
                    nodeView.getOriginY(), nodeView.getNewOriginY()
                );
            } else {
                animation = TreeAnimator.createFading(nodeView, 0.0, 1.0);
            }

            parallelTransition.getChildren().add(animation);
            nodeView.updatePosition();
        });

        parallelTransition.play();
    }
         */


    private void animateNodeSplit(BPlusNode<Integer, String> originalNode, BPlusNode<Integer, String> newNode) {
        if (currentAnimation != null) {
        currentAnimation.stop();
        }

        Timeline timeline = new Timeline();
        Duration duration = Duration.millis(800);

        BPlusNodeView originalView = nodeToNodeView.get(originalNode);
        BPlusNodeView newView = nodeToNodeView.get(newNode);

        // Animate original node shrinking
        KeyValue originalWidth = new KeyValue(
        originalView.widthProperty(), 
        originalView.getWidth() / 2
        );

        // Animate new node appearing
        newView.setOpacity(0);
        KeyValue newOpacity = new KeyValue(newView.opacityProperty(), 1);

        // Add keyframes
        timeline.getKeyFrames().addAll(
        new KeyFrame(duration, originalWidth, newOpacity)
        );

        currentAnimation = timeline;
        timeline.play();
        }

    private void updateTreeLayout() {
        double yacc = 0.0;

        for (List<BPlusNodeView> level : this.treeLevels) {
            yacc += ySpacing;
            int numOfSpaces = level.size() + 1;
            double totalNodeSpace = level.stream()
                .mapToDouble(BPlusNodeView::getWidth)
                .sum() + numOfSpaces * xSpacing;
            
            double acc = this.centerX - totalNodeSpace / 2;
            for (BPlusNodeView node : level) {
                acc += xSpacing;
                node.setOrigin(acc, yacc);
                acc += node.getWidth();
            } 
            yacc += level.get(0).getHeight();
        }

        for (int i = 0; i < this.treeLevels.size() - 1; i++) {
            for (BPlusNodeView node : this.treeLevels.get(i)) {
                if (!node.getIsLeaf()) {
                    for (int j = 0; j < node.getEdges().size(); j++) {
                        double endX = this.treeLevels.get(i+1).get(j).getPageMiddleX();
                        double endY = this.treeLevels.get(i+1).get(0).getOriginY();
                        node.getEdges().get(j).setEnd(endX, endY);
                    }
                }
            }
        }
    }

    @Override
    public void onTreeChanged(BPlusTreeEvent<Integer, String> event) {
        switch (event.getType()) {
            case NODE_CREATED:
                handleNodeCreated(event);
                break;
            case NODE_DELETED:
                handleNodeDeleted(event);
                break;
            case KEY_INSERTED:
                handleKeyInserted(event);
                break;
            case KEY_REMOVED:
                handleKeyRemoved(event);
                break;
            case KEY_BORROWED:
                handleKeyBorrowed(event);
                break;
            case CHILDNODE_BORROWED:
                handleChildNodeBorrowed(event);
                break;
            case CHILDNODE_DELETED:
                handleChildNodeDeleted(event);
                break;
            case CHILDNODE_CREATED:
                handleChildNodeCreated(event);
                break;
        }

        // Use timeline for layout updates
        Timeline layoutTimeline = new Timeline(
            new KeyFrame(Duration.millis(10), e -> updateTreeLayout())
        );
        layoutTimeline.play();
    }

    private void handleNodeCreated(BPlusTreeEvent<Integer, String> event) {
        BPlusNodeView newNode = new BPlusNodeView(event.getAffectedNode().isLeaf());
        this.nodeToNodeView.put(event.getAffectedNode(), newNode);
        this.treeGroup.getChildren().add(newNode);
        
        if (event.getAffectedNode().getParent() != null) {
            int levelIndex = event.getAffectedNode().getLevel();
            int nodeChildIndex = event.getAffectedNode().getChildrenIndex();
            BPlusNode<Integer, String> sibling;
            int newNodeIndex;
            
            if (nodeChildIndex == event.getAffectedNode().getParent().getChildren().size() - 1) {
                sibling = event.getAffectedNode().getParent().getChild(nodeChildIndex - 1);
                newNodeIndex = this.treeLevels.get(levelIndex).indexOf(nodeToNodeView.get(sibling)) + 1;
            } else {
                sibling = event.getAffectedNode().getParent().getChild(nodeChildIndex + 1);
                newNodeIndex = this.treeLevels.get(levelIndex).indexOf(nodeToNodeView.get(sibling));
            }
            
            this.treeLevels.get(levelIndex).add(newNodeIndex, newNode);
        } else {
            List<BPlusNodeView> newLevel = new ArrayList<>();
            newLevel.add(newNode);
            this.treeLevels.add(newLevel);
        }
    }

    private void handleNodeDeleted(BPlusTreeEvent<Integer, String> event) {
        BPlusNodeView deletedNode = nodeToNodeView.remove(event.getAffectedNode());
        this.treeLevels.get(event.getAffectedNode().getLevel()).remove(deletedNode);
        this.treeGroup.getChildren().remove(deletedNode);
    }

    private void handleKeyInserted(BPlusTreeEvent<Integer, String> event) {
        BPlusKeyView newKey = new BPlusKeyView(event.getAffectedKey().key);
        this.keyToKeyView.put(event.getAffectedKey(), newKey);
        this.nodeToNodeView.get(event.getAffectedNode()).insert(newKey);
    }

    private void handleKeyRemoved(BPlusTreeEvent<Integer, String> event) {
        BPlusKeyView removedKey = keyToKeyView.remove(event.getAffectedKey());
        BPlusNodeView node = nodeToNodeView.get(event.getAffectedNode());
        node.removeNode(removedKey.getKey());
    }

    private void handleKeyBorrowed(BPlusTreeEvent<Integer, String> event) {
        BPlusKeyView borrowedKey = keyToKeyView.get(event.getAffectedKey());
        BPlusNodeView lendingNode = nodeToNodeView.get(event.getLendingNode());
        BPlusNodeView borrowingNode = nodeToNodeView.get(event.getBorrowingNode());
        
        lendingNode.removeNode(borrowedKey.getKey());
        borrowingNode.insert(borrowedKey);
    }

    private void handleChildNodeBorrowed(BPlusTreeEvent<Integer, String> event) {
        BPlusNodeView borrowingNodeView = nodeToNodeView.get(event.getBorrowingNode());
        BPlusNodeView lendingNodeView = nodeToNodeView.get(event.getLendingNode());
        Arrow arrow = childrenToArrow.get(event.getChildrenNode());
        
        lendingNodeView.getEdges().remove(arrow);
        
        int childIndex = findChildIndex(event.getLendingNode(), event.getChildrenNode());
        borrowingNodeView.getEdges().add(childIndex, arrow);
    }

    private void handleChildNodeDeleted(BPlusTreeEvent<Integer, String> event) {
        BPlusNodeView affectedNode = nodeToNodeView.get(event.getAffectedNode());
        Arrow deletedArrow = childrenToArrow.remove(event.getChildrenNode());
        affectedNode.getEdges().remove(deletedArrow);
    }

    private void handleChildNodeCreated(BPlusTreeEvent<Integer, String> event) {
        Arrow newChildNode = new Arrow();
        childrenToArrow.put(event.getChildrenNode(), newChildNode);
        nodeToNodeView.get(event.getAffectedNode()).getEdges().add(newChildNode);
    }

    private int findChildIndex(BPlusNode<Integer, String> parent, BPlusNode<Integer, String> child) {
        for (int i = 0; i < ((BPlusInternalNode<Integer, String>) parent).getChildren().size(); i++) {
            if (((BPlusInternalNode<Integer, String>) parent).getChild(i) == child) {
                return i;
            }
        }
        return 0;
    }
}