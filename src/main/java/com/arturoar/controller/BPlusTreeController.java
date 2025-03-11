package com.arturoar.controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import com.arturoar.model.BPlusLeafNode;
import com.arturoar.model.BPlusNode;
import com.arturoar.model.BPlusPage;
import com.arturoar.model.BPlusTraversalResult;
import com.arturoar.model.BPlusTree;
import com.arturoar.model.BPlusTreeObserver;
import com.arturoar.view.BPlusPageView;
import com.arturoar.view.Arrow;
import com.arturoar.view.BPlusNodeView;

//import com.arturoar.view.BPlusPageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.util.Duration;

public class BPlusTreeController implements BPlusTreeObserver{

    
    private static final double SCALE_DELTA = 1.1; 
    private double xSpacing = 20.0;
    private double ySpacing = 80.0;
    private int animationSpeed = 2000;

    private int B = 3;

    @FXML private Pane canvas;
    @FXML private Button insertBtn;
    @FXML private Button removeBtn;
    @FXML private Button searchBtn;
    @FXML private Button homeBtn;


    private Scale scaleTransform;
    private Translate translateTransform;
    
    private BPlusTree tree;
    private ArrayList<ArrayList<BPlusPageView>> lastTreeSnapshot;
    private ArrayDeque<ArrayList<ArrayList<BPlusPageView>>> treeSnapshots;
    private HashMap<BPlusNode,BPlusNodeView> nodeToNodeView;
    private HashSet<Arrow> edges;

    private Double centerX;
    private Double centerY;
    private Double rootYPosition;
    private Double treeHeight;
    private Double treeWidth;


    private double zoomFactor = 1.0;
    private double lastMouseX, lastMouseY;
    private Group treeGroup;
    
    public BPlusTreeController() {

        this.tree = new BPlusTree(this.B);
        this.tree.addObserver(this);
        this.treeGroup = new Group();
        this.rootYPosition = 20.0;
        this.nodeToNodeView = new HashMap<BPlusNode,BPlusNodeView>();
        this.treeSnapshots = new ArrayDeque<>();
        this.edges = new HashSet<>();
    }

    @FXML
    public void initialize() {
    
        this.centerX = this.canvas.getWidth() / 2;
        this.centerY = this.canvas.getHeight() / 2;


        scaleTransform = new Scale(1, 1, 0, 0); 
        translateTransform = new Translate();

        this.treeGroup.getTransforms().addAll(scaleTransform, translateTransform);

        this.canvas.getChildren().add(treeGroup);

        this.canvas.setOnScroll(this::handleZoom);
        this.canvas.setOnMousePressed(this::handleMousePressed);
        this.canvas.setOnMouseDragged(this::handleMouseDragged);

        this.homeBtn.setOnAction(e->goHome());

        this.removeBtn.setOnAction(e -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Remove Node");
            dialog.setHeaderText("Insert the key:");
            ButtonType insertButtonType = new ButtonType("Insert", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);


            TextField textField = new TextField();
            textField.setPromptText("Key");
            dialog.getDialogPane().setContent(textField);

            // Obtener el resultado cuando el usuario presiona "Aceptar"
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == insertButtonType) {
                    return textField.getText();
                }
                return null;
            });

            // Mostrar el diálogo y obtener la entrada del usuario
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(key -> {
                if (this.remove(Integer.parseInt(key)).getResult() == null){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Removal Error");
                    alert.setHeaderText("The structure does not contain " + key + " key.");
                    // Mostrar la alerta
                    alert.showAndWait();
                }else{
                    updateView();
                }
            });
        });
        this.insertBtn.setOnAction(e -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("New Node");
            dialog.setHeaderText("Insert the new key:");
            ButtonType insertButtonType = new ButtonType("Insert", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);


            TextField keyField = new TextField();
            keyField.setPromptText("New key");
            TextField dataField = new TextField();
            dataField.setPromptText("Data");
            VBox vbox = new VBox();
            vbox.getChildren().addAll(keyField, dataField);
            dialog.getDialogPane().setContent(vbox);

            // Obtener el resultado cuando el usuario presiona "Aceptar"
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == insertButtonType) {
                    return keyField.getText() + "," + dataField.getText();
                }
                return null;
            });

            // Mostrar el diálogo y obtener la entrada del usuario
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(input -> {
                String[] values = input.split(",");
                if (!this.insert(Integer.parseInt(values[0]), values[1])){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Insertion Error");
                    alert.setHeaderText("Key already exists");
                    // Mostrar la alerta
                    alert.showAndWait();
                }else{
                    this.treeSnapshots.push(this.takeTreeSnapshot());
                    //updateView();
                    animate();
                    this.tree.mostrarArbol();
                    //this.renderTree();
                }

            });
        });

        this.searchBtn.setOnAction(e -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Searching Node");
            dialog.setHeaderText("Insert the key:");
            ButtonType insertButtonType = new ButtonType("Insert", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(insertButtonType, ButtonType.CANCEL);


            TextField textField = new TextField();
            textField.setPromptText("Key");
            dialog.getDialogPane().setContent(textField);

            // Obtener el resultado cuando el usuario presiona "Aceptar"
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == insertButtonType) {
                    return textField.getText();
                }
                return null;
            });

            // Mostrar el diálogo y obtener la entrada del usuario
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(key -> {
                BPlusTraversalResult<BPlusLeafNode> res = this.search(Integer.parseInt(key));
                if ( res.getResult() == null){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Removal Error");
                    alert.setHeaderText("The structure does not contain " + key + " key.");
                    // Mostrar la alerta
                    alert.showAndWait();
                }else{
                    
                    updateView();
                }
            });
        });
    }

    private void animateSearching(ArrayList<BPlusNodeView> nodes){

    }

    private void setupTreeLayout(ArrayList<ArrayList<BPlusPageView>> tree){
        int treeDepth = tree.size() - 1;
        Double maxWidth = tree.get(treeDepth).stream()
                                                .mapToDouble(page -> page.getWidth()) 
                                                .sum();

        //Double maxHeight = tree.size() * tree.get(treeDepth).get(0).getHeight() + (tree.size() + 1) * this.xSpacing;

        double yacc = 0.0;

        for (ArrayList<BPlusPageView> level: tree){
            yacc += ySpacing;
            int numOfSpaces = level.size() + 1;
            double totalPageSpace = level.stream()
                                            .mapToDouble(page -> page.getWidth())
                                            .sum();
            
            double spacing =  (this.canvas.getWidth() - maxWidth) / (level.size() + 1) ;
            double acc = 0.0;
            for (BPlusPageView page: level){
                acc += spacing;
                page.setOrigin(acc, yacc);
                acc += page.getWidth();
            } 
            yacc += level.get(0).getHeight();
        }

        for (int i = 0; i < tree.size() - 1; i++){
            for (BPlusPageView page : tree.get(i)){
                if (!page.getIsLeaf()){
                    for (int j = 0; j < page.getEdges().size(); j++){
                        double endX = tree.get(i+1).get(j).getPageMiddleX();
                        double endY = tree.get(i+1).get(0).getOriginY();
                        page.getEdges().get(j).setEnd(endX, endY);
                    }
                }
            }
        }

    }

    private void animate() {
        animateSnapshotRecursively();
    }

private void animateSnapshotRecursively() {
    if (!this.treeSnapshots.isEmpty()) {
        ArrayList<ArrayList<BPlusPageView>> snapshot = this.treeSnapshots.pop();
        this.setupTreeLayout(snapshot);  // Configura las posiciones de los nodos

        // Llamamos a animateTransitions() para realizar las animaciones
        animateTransitions();

        // Usamos PauseTransition para esperar un tiempo antes de continuar
        PauseTransition pause = new PauseTransition(Duration.seconds(10));  // Esperar 1 segundo
        pause.setOnFinished(event -> {
            // Llamamos recursivamente para el siguiente snapshot
            animateSnapshotRecursively();
        });

        pause.play();  // Inicia el tiempo de espera
    }
}

    private void animateTransitions(){
        ParallelTransition parallelTransition = new ParallelTransition();
        ArrayList<Transition> transitions = new ArrayList<>();
        for (BPlusNode node: this.nodeToNodeView.keySet()){
            BPlusNodeView nodeView = this.nodeToNodeView.get(node);
            if (!this.treeGroup.getChildren().contains(nodeView)){
                this.treeGroup.getChildren().add(nodeView);
            }

            if (nodeView.getOriginX() != null && nodeView.getOriginY() != null){
                if (nodeView.getOriginX().equals(nodeView.getNewOriginX()) || nodeView.getOriginY().equals(nodeView.getNewOriginY())) {
                    TranslateTransition translation = new TranslateTransition();
                    translation.setDuration(new javafx.util.Duration(this.animationSpeed)); 
                    translation.setNode(nodeView);  
                    translation.setFromX(nodeView.getOriginX()); 
                    translation.setToX(nodeView.getNewOriginX()); 
                    translation.setFromY(nodeView.getOriginY());   
                    translation.setToY(nodeView.getNewOriginY());
                    transitions.add(translation);
                    nodeView.updatePosition();
                    }
            }else{
                FadeTransition fading = new FadeTransition();
                fading.setDuration(new javafx.util.Duration(this.animationSpeed));
                fading.setNode(nodeView);
                fading.setFromValue(0.0);
                fading.setToValue(1.0);
                fading.setCycleCount(1);
                transitions.add(fading);
                nodeView.updatePosition();

            }
            
        }
        parallelTransition.getChildren().addAll(transitions);
        parallelTransition.play();

    }

    private ArrayList<ArrayList<BPlusPageView>> takeTreeSnapshot(){
        ArrayList<ArrayList<BPlusPageView>> snapshot = new ArrayList<ArrayList<BPlusPageView>>();
        ArrayDeque<BPlusPage> pages = new ArrayDeque<>();
        pages.push(this.tree.getRoot());
        int level;
        while(!pages.isEmpty()){
            BPlusPage currentPage = pages.pop();
            pages.addAll(currentPage.getChildren());
            BPlusPageView newPageView = new BPlusPageView(currentPage.isLeaf());
            level = currentPage.getLevel();
            try{ 
                snapshot.get(level).add(newPageView);
            }catch(IndexOutOfBoundsException e){
                ArrayList<BPlusPageView> levelPages = new ArrayList<>();
                levelPages.add(newPageView);
                snapshot.add(levelPages);
            }
            if (currentPage.isLeaf()){
                for (BPlusLeafNode node : currentPage.getNodes()){
                    if (nodeToNodeView.get(node) == null){
                        BPlusNodeView newNodeView = new BPlusNodeView(node.getKey(), node.getData());
                        nodeToNodeView.put(node, newNodeView);
                        newPageView.insertNode(newNodeView);
                    }else{
                        newPageView.insertNode(nodeToNodeView.get(node));
                    }
                }
            }else{
                for (BPlusNode node : currentPage.getKeys()){
                    if (nodeToNodeView.get(node) == null){
                        BPlusNodeView newNodeView = new BPlusNodeView(node.getKey());
                        nodeToNodeView.put(node, newNodeView);
                        newPageView.insertNode(newNodeView);   
                    }else{
                        newPageView.insertNode(nodeToNodeView.get(node));
                    }
                }
            }
            
        }
        return snapshot;
    }


    private void goHome() {
        scaleTransform.setX(1);
        scaleTransform.setY(1);
        this.zoomFactor = 1;
    }
    private void handleZoom(ScrollEvent event) {

        scaleTransform.setPivotX(event.getX()); 
        scaleTransform.setPivotY(event.getY());

        double zoomFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
        System.err.println("Zoom factor: " + this.zoomFactor);
                
        this.zoomFactor *= zoomFactor;
        if (this.zoomFactor > 0.4 && this.zoomFactor < 2) {
            scaleTransform.setX(scaleTransform.getX() * zoomFactor);
            scaleTransform.setY(scaleTransform.getY() * zoomFactor);
        }
        if (this.zoomFactor < 0.4) {
            this.zoomFactor = 0.4;
        }
        if (this.zoomFactor > 2) {
            this.zoomFactor = 2;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        ((Pane) event.getSource()).setCursor(Cursor.CLOSED_HAND);
        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        System.err.println("Mouse pressed at: " + lastMouseX + ", " + lastMouseY);
    }

    private void handleMouseDragged(MouseEvent event) {
        double deltaX = event.getSceneX() - lastMouseX;
        double deltaY = event.getSceneY() - lastMouseY;

        translateTransform.setX(translateTransform.getX() + deltaX );
        translateTransform.setY(translateTransform.getY() + deltaY );

        lastMouseX = event.getSceneX();
        lastMouseY = event.getSceneY();
        
        ((Pane) event.getSource()).setCursor(Cursor.OPEN_HAND);
    }

    
    public boolean insert(int key,String data) {
       BPlusTraversalResult<ArrayList<BPlusNode>> resultado = this.tree.insertNode(key, data);
       System.out.println(resultado.getResult());
       return !resultado.getResult().isEmpty();
    }
    public BPlusTraversalResult<ArrayList<BPlusNode>> remove(int key) {
        return this.tree.removeNode(key);
    }
    public BPlusTraversalResult<BPlusLeafNode> search(int key) {
        return this.tree.searchKeyIterativo(key);
    }

    
    public void renderTree(){
        ArrayDeque<BPlusPage> queue = new ArrayDeque<>();
        
        queue.add(this.tree.getRoot());

        while(queue.peek() != null){
            BPlusPage current = queue.poll();

            for (BPlusPage child : current.getChildren()) {
                queue.add(child);
            } 

            BPlusPageView pageView = new BPlusPageView(20.0, 80.0,current.isLeaf());
            for (BPlusLeafNode node : current.getNodes()) {
                pageView.insertNodeOld(node.getKey(),node.getData());
            }
            this.treeGroup.getChildren().add(pageView);
            for (BPlusPage child : current.getChildren()) {
                queue.add(child);
            }
        }
    } 

    private void updateView() {
        this.tree.mostrarArbol();
        //System.out.println(this.tree);
        this.treeGroup.getChildren().clear();
        this.renderTree();
    }

    @Override
    public void onNodeSplit() {
        this.treeSnapshots.add(this.takeTreeSnapshot());
    }


}