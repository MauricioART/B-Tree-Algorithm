package com.arturoar.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import com.arturoar.model.BPlusNode;
import com.arturoar.model.BPlusTraversalResult;
import com.arturoar.model.BPlusInternalNode;
import com.arturoar.model.BPlusLeafNode;
import com.arturoar.model.BPlusTree;
import com.arturoar.model.BPlusTreeEvent;
import com.arturoar.model.BPlusTreeObserver;
import com.arturoar.model.Key;
import com.arturoar.view.BPlusNodeView;
import com.arturoar.view.Arrow;
import com.arturoar.view.BPlusKeyView;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.MouseEvent;
import javafx.animation.ParallelTransition;
import javafx.animation.Transition;
import javafx.fxml.FXML;


public class BPlusTreeController implements BPlusTreeObserver<Integer,String>{

    private static final double SCALE_DELTA = 1.1;
    private double xSpacing = 20.0;
    private double ySpacing = 80.0;
    private int B = 3;

    @FXML private Pane canvas;
    @FXML private Button insertBtn, removeBtn, searchBtn, homeBtn;

    private Scale scaleTransform;
    private Translate translateTransform;
    private BPlusTree<Integer, String> tree;
    private ArrayList<ArrayList<BPlusNodeView>> treeLevels;
    private HashMap<Key<Integer>, BPlusKeyView> keyToKeyView;
    private HashMap<BPlusNode<Integer, String>, BPlusNodeView> nodeToNodeView;
    private HashMap<BPlusNode<Integer, String>, Arrow> childrenToArrow;
    private ArrayDeque<ArrayList<BPlusNodeView>> snapshots;
    private Group treeGroup;
    private double zoomFactor = 1.0, lastMouseX, lastMouseY;
   

    private Double centerX;
    private Double centerY;
    private Double rootYPosition;
    private Double treeHeight;
    private Double treeWidth;

    
    public BPlusTreeController() {
        this.tree = new BPlusTree<>(this.B);
        BPlusNodeView root = new BPlusNodeView(true);
        this.treeGroup = new Group();
        this.keyToKeyView = new HashMap<>();
        this.nodeToNodeView = new HashMap<>();
        this.childrenToArrow = new HashMap<>();
        this.snapshots = new ArrayDeque<>(); 
        this.nodeToNodeView.put(this.tree.getRoot(), root);
        this.treeLevels = new ArrayList<>();
        this.tree.addObserver(this);
    }

    @FXML
    public void initialize() {
        this.scaleTransform = new Scale(1, 1);
        this.translateTransform = new Translate();
        this.treeGroup.getTransforms().addAll(scaleTransform, translateTransform);
        this.canvas.getChildren().add(treeGroup);
        this.canvas.setOnScroll(this::handleZoom);
        this.canvas.setOnMousePressed(this::handleMousePressed);
        this.canvas.setOnMouseDragged(this::handleMouseDragged);
        this.homeBtn.setOnAction(e -> goHome());
        setupButtonActions();
    }

    private void setupButtonActions() {
        insertBtn.setOnAction(e -> handleInsert());
        removeBtn.setOnAction(e -> handleRemove());
        searchBtn.setOnAction(e -> handleSearch());
    }

    private void handleInsert() {
        Dialog<String[]> dialog = createDialog("New Node", "Insert the new key:", true);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (!this.insert(key, values[1])) {
                    showAlert(Alert.AlertType.WARNING, "Insertion Error", "Key already exists");
                }
                this.tree.showTree();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid integer key.");
            }
        });
    }

    private void handleRemove() {
        Dialog<String[]> dialog = createDialog("Remove Node", "Insert the key:", false);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (this.remove(key).getResult() == null) {
                    showAlert(Alert.AlertType.ERROR, "Removal Error", "The structure does not contain key " + key);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid integer key.");
            }
        });
    }

    private void handleSearch() {
        Dialog<String[]> dialog = createDialog("Searching Node", "Insert the key:", false);
        dialog.showAndWait().ifPresent(values -> {
            try {
                int key = Integer.parseInt(values[0]);
                if (this.search(key).getResult() == null) {
                    showAlert(Alert.AlertType.ERROR, "Search Error", "The structure does not contain key " + key);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid integer key.");
            }
        });
    }

    private Dialog<String[]> createDialog(String title, String header, boolean includeDataField) {
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
                return new String[]{keyField.getText(), includeDataField ? ((TextField) vbox.getChildren().get(vbox.getChildren().size() - 1)).getText() : ""};
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

    private void handleZoom(ScrollEvent event) {
        double scaleFactor = event.getDeltaY() > 0 ? SCALE_DELTA : 1 / SCALE_DELTA;
        scaleTransform.setX(scaleTransform.getX() * scaleFactor);
        scaleTransform.setY(scaleTransform.getY() * scaleFactor);
    }

    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        translateTransform.setX(translateTransform.getX() + event.getSceneX() - lastMouseX);
        translateTransform.setY(translateTransform.getY() + event.getSceneY() - lastMouseY);
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
    }

    private void goHome() {
        scaleTransform.setX(1);
        scaleTransform.setY(1);
        translateTransform.setX(0);
        translateTransform.setY(0);
    }

    

    
    public boolean insert(Integer key,String data) {
        BPlusTraversalResult<Boolean,Integer> result = this.tree.insert(key, data);
        System.out.println(result.getResult());
        return result.getResult();
     }
     public BPlusTraversalResult<String,Integer> remove(Integer key) {
        BPlusTraversalResult<String,Integer> result =  this.tree.remove(key);
        return result;
     }
     public BPlusTraversalResult<String,Integer> search(Integer key) {
        BPlusTraversalResult<String,Integer> result =  this.tree.search(key);
        return result;
     }

    private void animateSearching(ArrayList<BPlusKeyView> nodes){
        // TODO: Implement searching animation 
    }

   
    private void animateTransitions() {
        ParallelTransition parallelTransition = new ParallelTransition();

        for (Key<Integer> node : keyToKeyView.keySet()) {
            BPlusKeyView nodeView = keyToKeyView.get(node);
            
            if (!treeGroup.getChildren().contains(nodeView)) {
                treeGroup.getChildren().add(nodeView);
            }

            Transition animation;
            if (nodeView.getOriginX() != null && nodeView.getOriginY() != null) {
                animation = TreeAnimator.createTranslate(nodeView, nodeView.getOriginX(), nodeView.getNewOriginX(),
                                                        nodeView.getOriginY(), nodeView.getNewOriginY());
            } else {
                animation = TreeAnimator.createFading(nodeView, 0.0, 1.0);
            }

            parallelTransition.getChildren().add(animation);
            nodeView.updatePosition();
        }

        parallelTransition.play();
    }


    private void updateTreeLayout(){

        //Double lastLevelNodeWidth = this.treeLevels.getLast().stream()
        //                                        .mapToDouble(page -> page.getWidth()) 
        //                                        .sum();
        double yacc = 0.0;

        for (ArrayList<BPlusNodeView> level: this.treeLevels){
            yacc += ySpacing;
            int numOfSpaces = level.size() + 1;
            double totalNodeSpace = level.stream()
                                            .mapToDouble(node -> node.getWidth())
                                            .sum() + numOfSpaces * xSpacing;
            
            //double spacing =  (this.canvas.getWidth() - lastLevelNodeWidth) / (level.size() + 1) ;
            double acc = this.centerX - totalNodeSpace / 2;
            for (BPlusNodeView node: level){
                acc += xSpacing;
                node.setOrigin(acc, yacc);
                acc += node.getWidth();
            } 
            yacc += level.get(0).getHeight();
        }

        for (int i = 0; i < this.treeLevels.size() - 1; i++){
            for (BPlusNodeView node : this.treeLevels.get(i)){
                if (!node.getIsLeaf()){
                    for (int j = 0; j < node.getEdges().size(); j++){
                        double endX = this.treeLevels.get(i+1).get(j).getPageMiddleX();
                        double endY = this.treeLevels.get(i+1).get(0).getOriginY();
                        node.getEdges().get(j).setEnd(endX, endY);
                    }
                }
            }
        }

    }

    @Override
    public void onTreeChanged(BPlusTreeEvent<Integer, String> e) {
        switch (e.getType()) {
            case NODE_CREATED:
                BPlusNodeView newNode = new BPlusNodeView(e.getAffectedNode().isLeaf());
                this.nodeToNodeView.put(e.getAffectedNode(), newNode);
                this.treeGroup.getChildren().add(newNode);
                if (e.getAffectedNode().getParent() != null) {
                    int levelIndex = e.getAffectedNode().getLevel();
                    int nodeChildIndex = e.getAffectedNode().getChildrenIndex();
                    BPlusNode<Integer, String> sibling;
                    int newNodeIndex;
                    if (nodeChildIndex == e.getAffectedNode().getParent().getChildren().size() - 1){
                        sibling = e.getAffectedNode().getParent().getChild(nodeChildIndex - 1);
                        newNodeIndex = this.treeLevels.get(levelIndex).indexOf(nodeToNodeView.get(sibling)) + 1;
                    }else {
                        sibling = e.getAffectedNode().getParent().getChild(nodeChildIndex + 1);
                        newNodeIndex = this.treeLevels.get(levelIndex).indexOf(nodeToNodeView.get(sibling));
                    }
                    this.treeLevels.get(levelIndex).add(newNodeIndex, newNode);
                }else{
                    ArrayList<BPlusNodeView> newLevel = new ArrayList<>();
                    newLevel.add(newNode);
                    this.treeLevels.add(newLevel);
                }
                break;

            case NODE_DELETED:
                BPlusNodeView deletedNode = nodeToNodeView.get(e.getAffectedNode());
                this.treeLevels.get(e.getAffectedNode().getLevel()).remove(deletedNode);
                break;

            case KEY_INSERTED:
                BPlusKeyView newKey = new BPlusKeyView(e.getAffectedKey().key);
                this.keyToKeyView.put(e.getAffectedKey(), newKey);
                this.nodeToNodeView.get(e.getAffectedNode()).insert(newKey);
                break;

            case KEY_REMOVED:
                BPlusKeyView removedKey = keyToKeyView.get(e.getAffectedKey());
                BPlusNodeView node = nodeToNodeView.get(e.getAffectedNode());
                node.removeNode(removedKey.getKey());

                break;

            case KEY_BORROWED:
                BPlusKeyView borrowedKey = keyToKeyView.get(e.getAffectedKey());
                BPlusNodeView lenderNode = nodeToNodeView.get(e.getAffectedNode());
                BPlusNodeView borrowerNode = nodeToNodeView.get(e.getAffectedNode2());
                lenderNode.removeNode(borrowedKey.getKey());
                borrowerNode.insert(borrowedKey);
                break;

        }
        updateTreeLayout();
    }

}