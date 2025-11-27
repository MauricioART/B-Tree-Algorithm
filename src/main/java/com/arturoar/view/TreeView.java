package com.arturoar.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arturoar.model.BPlusInnerNode;
import com.arturoar.model.BPlusLeafNode;
import com.arturoar.model.BPlusNode;
import com.arturoar.model.Key;
import com.arturoar.util.BPlusTreeEvent;
import com.arturoar.util.BPlusTreeObserver;
import com.arturoar.util.SoundType;
import com.arturoar.util.TreeAnimator;

import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;

public class TreeView extends Group implements BPlusTreeObserver<Integer, String> {

    private final Double X_PADDING = 20.0;
    private final Double Y_PADDING = 50.0;
    private final Double MIN_NODE_SPACING = 15.0;
    private final Double MAX_NODE_SPACING = 50.0;

    private final Map<Key<Integer>, KeyView> keyToKeyView = new HashMap<>();
    private final Map<BPlusNode<Integer, String>, NodeView> nodeToNodeView = new HashMap<>();
    private final Map<NodeView, Edge> childrenToEdge = new HashMap<>();
    private List<List<NodeView>> treeLevels = new ArrayList<>();
    private List<Edge> unshownEdges = new ArrayList<>();
    private List<KeyView> borrowedKeys = new ArrayList<>();

    private DoubleProperty canvasWidth = new SimpleDoubleProperty();
    private DoubleProperty canvasHeight = new SimpleDoubleProperty();
    private BooleanProperty darkModeProperty = new SimpleBooleanProperty();
    private TreeAnimator animator;

    private IntegerProperty depthProperty = new SimpleIntegerProperty(0);
    private IntegerProperty widthProperty = new SimpleIntegerProperty(0);

    private DoubleProperty scaleProperty = new SimpleDoubleProperty(1.0);


    private boolean isBorrowingFromInnerNode = false;
    

    public TreeView() {

        ChangeListener<Number> listener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldValue, Number newValue) {
                for (int level = 0; level < treeLevels.size(); level++) {
                    updateLevelLayout(level);
                }
                animator.combineLastsTransitionsOnQueue(treeLevels.size());
                animator.animateQueue();
            }

        };

        this.canvasWidth.addListener(listener);
        this.canvasHeight.addListener(listener);
        this.animator = TreeAnimator.getInstance();
        
    }

    @Override
    public void onTreeChanged(BPlusTreeEvent<Integer, String> event) {

        switch (event.getType()) {
            case NEW_ROOT:
                handleNewRoot(event);
                break;
            case NODE_SPLIT:
                handleNodeSplit(event);
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
            case NODE_BORROWED:
                handleChildNodeBorrowed(event);
                break;
            case NODE_DELETED:
                handleChildNodeDeleted(event);
                break;
            case NODE_CREATED:
                handleChildNodeCreated(event);
                break;
            case KEY_CHANGED:
                handleKeyChanged(event);
                break;
            case KEY_BORROWED_FROM_INNER_NODE:
                isBorrowingFromInnerNode = true;
                break;
        }

    }

    private void handleNewRoot(BPlusTreeEvent<Integer, String> event) {
        
        BPlusTreeEvent.NewRoot<Integer,String> e = (BPlusTreeEvent.NewRoot<Integer,String>) event;

        if (nodeToNodeView.containsKey(e.getNewRoot())) {
            NodeView root = this.treeLevels.removeFirst().getFirst();
            for(Edge edge : ((InnerNodeView)root).getEdges()){
                animator.addTransitionToQueue(animator.fadeNode(edge, 1, 0, null));
                animator.combineLastsTransitionsOnQueue(2);
            }
            updateYLayout();
            animator.addListenerToLastTransition(()->{ depthProperty.set(treeLevels.size());});
            
        }else{
            NodeView newNode = NodeFactory.createNode(e.getNewRoot().isLeaf(), this.canvasWidth.get()/2, Y_PADDING);
            if (newNode instanceof LeafNodeView){
                Edge nextLeaf = ((LeafNodeView)newNode).nextLeaf;
                nextLeaf.darkModeProperty().bind(darkModeProperty);
            }
            this.nodeToNodeView.put(e.getNewRoot(), newNode);
            newNode.levelProperty().bind(e.getNewRoot().levelProperty());
            this.getChildren().add(0,newNode);
            List<NodeView> newLevel = new ArrayList<>();
            newLevel.add(newNode);
            this.treeLevels.addFirst(newLevel);   
            if (nodeToNodeView.size() > 1){
                updateYLayout();
            }
            animator.combineLastsTransitionsOnQueue(2);
            animator.addScheduleTask(() -> {
                depthProperty.set(treeLevels.size());
            },0);
        }
    }
    
    
    private void handleNodeSplit(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.NodeSplit<Integer,String> e = (BPlusTreeEvent.NodeSplit<Integer,String>) event;
        NodeView splitNode = this.nodeToNodeView.get(e.getNode());
        KeyView middleKey = keyToKeyView.get(e.getMiddleKey());
        NodeView newNode = NodeFactory.createNode(e.getNewNode().isLeaf(), middleKey.getTranslateX(), splitNode.getYOrigin());
        if (newNode instanceof LeafNodeView){
            Edge nextLeaf = ((LeafNodeView)newNode).nextLeaf;
            nextLeaf.darkModeProperty().bind(darkModeProperty);
        }
        newNode.levelProperty().bind(e.getNewNode().levelProperty());
        this.nodeToNodeView.put(e.getNewNode(), newNode);
        this.getChildren().add(newNode);

        // Inserting new NodeView in the correct position

            int levelIndex = e.getNewNode().getLevel();
            int newNodeIndex = this.treeLevels.get(levelIndex).indexOf(splitNode) + 1;

            this.treeLevels.get(levelIndex).add(newNodeIndex, newNode);

            if (e.getNode().isLeaf()) {
                LeafNodeView newLeafNode = (LeafNodeView) newNode;
                LeafNodeView previousNode = (LeafNodeView) splitNode;
                if (newNodeIndex < this.treeLevels.get(levelIndex).size() - 1){
                    LeafLinkEdge newNodeEdge = (LeafLinkEdge) newLeafNode.nextLeaf;
                    LeafLinkEdge previousNodeEdge = (LeafLinkEdge) previousNode.nextLeaf;
                    
                    previousNodeEdge.originXProperty().unbind();
                    previousNodeEdge.originYProperty().unbind();
                    previousNodeEdge.originXProperty().bind(splitNode.getLast().translateXProperty().add(splitNode.getLast().widthProperty()));
                    previousNodeEdge.originYProperty().bind(splitNode.getLast().translateYProperty().add(splitNode.getLast().getHeight()/2));
                    previousNode.setNextLeaf(newNodeEdge);
                    
                    animator.addScheduleTask(()->{ 
                        newLeafNode.setNextLeaf(previousNodeEdge);
                    },  1);

                    
                }
            
                previousNode.nextLeaf.endXProperty().bind(newLeafNode.translateXProperty());
                previousNode.nextLeaf.endYProperty().bind(newLeafNode.translateYProperty().add(newLeafNode.heightProperty().divide(2)));
                unshownEdges.add(previousNode.nextLeaf);
                getChildren().add(previousNode.nextLeaf);

            } else{
                
                int childIndex = splitNode.getKeyIndex(middleKey);

                Edge prevArrow = ((InnerNodeView) splitNode).getEdges().get(childIndex);
                KeyView prevKey = ((InnerNodeView) splitNode).keys.get(childIndex - 1);
                prevArrow.originXProperty().unbind();
                prevArrow.originYProperty().unbind();
                prevArrow.originXProperty().bind(prevKey.translateXProperty().add(prevKey.widthProperty()));
                prevArrow.originYProperty().bind(prevKey.translateYProperty().add(prevKey.getHeight()));

                /*Edge nextArrow = ((InnerNodeView) splitNode).getEdges().get(childIndex + 1);
                KeyView nextKey = ((InnerNodeView) splitNode).keys.get(childIndex + 1);
                nextArrow.originXProperty().bind(nextKey.translateXProperty());
                nextArrow.originYProperty().bind(nextKey.translateYProperty().add(nextKey.getHeight()));*/

                Edge splitNodeLastEdge = ((InnerNodeView) splitNode).getEdges().getLast();
                KeyView lastKey = ((InnerNodeView) splitNode).keys.getLast();
                splitNodeLastEdge.originXProperty().unbind();
                splitNodeLastEdge.originYProperty().unbind();
                splitNodeLastEdge.originXProperty().bind(lastKey.translateXProperty().add(lastKey.widthProperty()));
                splitNodeLastEdge.originYProperty().bind(lastKey.translateYProperty().add(lastKey.getHeight()));

            }

    }


    private void   updateEdges(BPlusNode<Integer,String> node) {
        NodeView deletedNode = nodeToNodeView.get(node);
        int nodeIndex = this.treeLevels.get(deletedNode.getLevel()).indexOf(deletedNode);
        this.treeLevels.get(deletedNode.getLevel()).remove(nodeIndex);
        updateLevelLayout(deletedNode.getLevel());

        if (node.isLeaf()){
            LeafNodeView deletedLeaf = (LeafNodeView) deletedNode;
            int deletedNodeLevel = deletedNode.getLevel();

            if (deletedNodeLevel > 0){
                if (nodeIndex == this.treeLevels.get(deletedNodeLevel).size() ){
                    LeafNodeView previousNode = (LeafNodeView) this.treeLevels.get(deletedNodeLevel).get(nodeIndex - 1);
                    Transition edgeFadeOut = animator.fadeNode(previousNode.nextLeaf, 1, 0, null);
                    edgeFadeOut.setOnFinished(_ -> {
                        this.getChildren().remove(previousNode.nextLeaf);
                    });
                    animator.addTransitionToQueue(edgeFadeOut);
                    animator.combineLastsTransitionsOnQueue(2);
                }else{

                    Transition edgeFadeOut = animator.fadeNode(deletedLeaf.nextLeaf, 1, 0, null);
                    edgeFadeOut.setOnFinished(_ -> {
                        this.getChildren().remove(deletedLeaf.nextLeaf);
                    });
                    animator.addTransitionToQueue(edgeFadeOut);
                    animator.combineLastsTransitionsOnQueue(2);

                    LeafNodeView previousNode = (LeafNodeView) this.treeLevels.get(deletedNodeLevel).get(nodeIndex - 1);
                    LeafNodeView nextNode = (LeafNodeView) this.treeLevels.get(deletedNodeLevel).get(nodeIndex);
                    
                    
                    previousNode.nextLeaf.endXProperty().unbind();
                    previousNode.nextLeaf.endYProperty().unbind();

                    double fromX = previousNode.nextLeaf.endXProperty().get();
                    double toX = nextNode.getKey(0).getNewXOrigin();

                    Transition endPointTranslation = animator.animateProperty(previousNode.nextLeaf.endXProperty(), fromX, toX);
                    endPointTranslation.setOnFinished(_ -> {
                        previousNode.nextLeaf.endXProperty().unbind();
                        previousNode.nextLeaf.endYProperty().unbind();
                        previousNode.nextLeaf.endXProperty().bind(nextNode.translateXProperty());
                        previousNode.nextLeaf.endYProperty().bind(nextNode.translateYProperty().add(nextNode.getHeight()/2));
    
                    });
                    animator.addTransitionToQueue(endPointTranslation);
                    animator.combineLastsTransitionsOnQueue(2);
                }
            }
       
        }
        getChildren().remove(deletedNode);
        treeLevels.get(deletedNode.getLevel()).remove(deletedNode);
        animator.combineLastsTransitionsOnQueue(2);
    }

    private void handleKeyInserted(BPlusTreeEvent<Integer, String> event) {

        KeyView newKey = createKeyView(event);

        newKey.setOnWidthChangeCallback(level ->{updateLevelLayout(level);});
        newKey.darkModeProperty().bind(darkModeProperty);

        BPlusTreeEvent.KeyInserted<Integer,String> e = (BPlusTreeEvent.KeyInserted<Integer,String>) event;

        NodeView affectedNodeView = nodeToNodeView.get(e.getNode());
        
        //Register new key view
        this.keyToKeyView.put(e.getKey(), newKey);
        int position = affectedNodeView.insert(newKey);

        if (!e.getNode().isLeaf()){
            reassignEdgesOriginsFromKeyInserted2((InnerNodeView) affectedNodeView, position, newKey);
        }

        this.getChildren().add(newKey);
        newKey.setIsNew(false);

        // Animate insertion
        int level = e.getNode().getLevel();
        updateLevelLayout(level);
        
        Transition keyFadeIn = animator.fadeNode(newKey,0,1,null);
        keyFadeIn.setOnFinished(_->{
            if (e.getNode().isLeaf()){

                widthProperty.set(widthProperty.get() + 1);
            }
        });
        animator.addTransitionToQueue(keyFadeIn);
        
        if (!this.unshownEdges.isEmpty()){
            showEdges();
        }

    }

    private KeyView createKeyView(BPlusTreeEvent<Integer, String> event){
        BPlusTreeEvent.KeyInserted<Integer,String> e = (BPlusTreeEvent.KeyInserted<Integer,String>) event;
        KeyView newKey;
        if (e.getNode().isLeaf()){
           newKey = new KeyView(e.getKey().key, String.valueOf(((BPlusLeafNode<Integer, String>)e.getNode()).getData(e.getKey().getKey())));
        }else {
            newKey = new KeyView(e.getKey().key);
        }
        newKey.setOpacity(0);
        return newKey;
    }

    private void handleKeyRemoved(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.KeyRemoved<Integer,String> e = (BPlusTreeEvent.KeyRemoved<Integer,String>) event;

        KeyView removedKey = keyToKeyView.get(e.getKey());
        NodeView node = nodeToNodeView.get(e.getNode());
        int removeKeyIndex = node.remove(removedKey);

        
        Transition keyFading = animator.fadeNode(removedKey, 1, 0, SoundType.FADE_OUT);
        animator.addTransitionToQueue(keyFading);
        keyFading.setOnFinished(_ -> {
            this.getChildren().remove(removedKey);
            keyToKeyView.remove(e.getKey());
            if(removedKey.getNode() instanceof LeafNodeView){
                widthProperty.set(widthProperty.get() - 1);
            }
        }
        );
        updateLevelLayout(e.getNode().getLevel());


        if (!e.getNode().isLeaf() && e.getNode().size() > 0){
            if (removeKeyIndex < e.getNode().size() ){
                KeyView nextKey = node.getKey(removeKeyIndex);
                Edge prevEdge = ((InnerNodeView)node).getEdge(removeKeyIndex);

                prevEdge.originXProperty().unbind();
                prevEdge.originYProperty().unbind();

                Transition readjustOrigin = animator.animateProperty(prevEdge.originXProperty(), prevEdge.originXProperty().get(), nextKey.getNewXOrigin());
                readjustOrigin.setOnFinished(_ -> { 
                    prevEdge.originXProperty().bind(nextKey.translateXProperty());
                    prevEdge.originYProperty().bind(nextKey.translateYProperty().add(nextKey.getHeight()));
                });
                
                animator.addTransitionToQueue(readjustOrigin);
                animator.combineLastsTransitionsOnQueue(2 );
            
            }else{
                KeyView lastKey = node.getLast();
                Edge lastEdge = ((InnerNodeView)node).getEdge(removeKeyIndex);
                lastEdge.originXProperty().unbind();
                lastEdge.originYProperty().unbind();
                lastEdge.originXProperty().bind(lastKey.translateXProperty().add(lastKey.widthProperty()));
                lastEdge.originYProperty().bind(lastKey.translateYProperty().add(lastKey.getHeight()));


            }

        }

        animator.combineLastsTransitionsOnQueue(2);
        
    }

    private void  handleKeyBorrowed(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.KeyBorrowed<Integer,String> e = (BPlusTreeEvent.KeyBorrowed<Integer,String>) event;
        
        if (e.getBorrowedKeys().isEmpty()) return;

        NodeView lendingNode = nodeToNodeView.get(e.getLendingNode());
        NodeView borrowingNode = nodeToNodeView.get(e.getBorrowingNode());
        int lendingLevel = e.getLendingNode().getLevel();
        int borrowingLevel = e.getBorrowingNode().getLevel();

        int tempPosition = -1;
        KeyView tempBorrowedKey = null;

        for (Key<Integer> key : e.getBorrowedKeys()) {
            tempBorrowedKey = keyToKeyView.get(key);
            lendingNode.remove(tempBorrowedKey);
            tempPosition = borrowingNode.insert(tempBorrowedKey);
            borrowedKeys.add(tempBorrowedKey);
            tempBorrowedKey.toFront();

            if (isBorrowingFromInnerNode){
                reassignEdgesOriginDynamically(tempBorrowedKey, borrowingNode, lendingNode);
                isBorrowingFromInnerNode = false;
            }
        }

        final int position = tempPosition;
        final KeyView borrowedKey = tempBorrowedKey;

        if (lendingLevel == borrowingLevel) {
            updateLevelLayout(lendingLevel);
        } else {

            // Borrow from different levels due to inner node split
            updateLevelLayout(lendingLevel);
            updateLevelLayout(borrowingLevel);
            updateYLayout();
            animator.combineLastsTransitionsOnQueue(3);

            if (position != -1 && borrowedKey != null) {
                Edge prevEdge = ((InnerNodeView)borrowingNode).getEdge(position); 
                Edge nextEdge = ((InnerNodeView)borrowingNode).getEdge(position + 1);
                final KeyView nextKeyView = (position < borrowingNode.getNumberOfKeys() - 1) ? borrowingNode.getKey(position + 1) : null;
                animator.addListenerToLastTransition(() -> reassignEdgesOriginsFromKeyInserted(prevEdge, borrowedKey, nextEdge, nextKeyView));
            }

            showEdges();
        }
    }

    private void reassignEdgesOriginDynamically(KeyView borrowedKey,NodeView borrowingNode, NodeView lendingNode){

        int borrowingIndex = treeLevels.get(borrowingNode.getLevel()).indexOf(borrowingNode);
        int lendingIndex = treeLevels.get(lendingNode.getLevel()).indexOf(lendingNode);
        
        /*if (lendingIndex < borrowingIndex){

        }else{}*/

        ChangeListener<Number> posListener = (obs, _, newVal) -> {
            if (obs instanceof DoubleProperty prop) {
                // Acceder al bean que contiene la propiedad
                Object bean = prop.getBean();
                if (bean instanceof KeyView movingKey) {
                    if (lendingIndex < borrowingIndex){
                        int lendingNodeSize = ((InnerNodeView) lendingNode).size();
                        Edge BKnextEdge = ((InnerNodeView) borrowingNode).getEdges().get(0);
                        Edge BKprevEdge = ((InnerNodeView) lendingNode).getEdges().get(lendingNodeSize);
                        Edge BKnewNextEdge = ((InnerNodeView) borrowingNode).getEdges().get(1);

                            BKprevEdge.originXProperty().unbind();
                            BKprevEdge.originYProperty().unbind();
                            KeyView prevKey = lendingNode.getKey(lendingNodeSize - 1);
                            BKprevEdge.originXProperty().bind(prevKey.translateXProperty().add(prevKey.widthProperty()));
                            BKprevEdge.originYProperty().bind(prevKey.translateYProperty().add(prevKey.getHeight()));
                            BKnextEdge.originXProperty().unbind();
                                                        
                        if (Math.abs( (double) newVal - BKnextEdge.getOriginX()) < 5){
                            BKnextEdge.originXProperty().bind(movingKey.translateXProperty());
                        }
                        
                        if (Math.abs((double) newVal + movingKey.getWidthProperty() - BKnewNextEdge.getOriginX()) < 5){                                   
                            BKnewNextEdge.originXProperty().unbind();
                            BKnewNextEdge.originYProperty().unbind();
                            BKnewNextEdge.originXProperty().bind(movingKey.translateXProperty().add(movingKey.widthProperty()));
                            BKnewNextEdge.originYProperty().bind(movingKey.translateYProperty().add(movingKey.getHeight()));
                        }
                        
                    }else{ 

                        Edge BKnextEdge = ((InnerNodeView) lendingNode).getEdges().get(1);
                        Edge BKprevEdge = ((InnerNodeView) lendingNode).getEdges().get(0);
                        Edge BKnewPrevEdge = ((InnerNodeView) borrowingNode).getEdges().getLast();
                        
                        BKnextEdge.originXProperty().unbind();
                        BKnextEdge.originYProperty().unbind();
                        
                        KeyView nextKey = lendingNode.getKey(1);
                        BKnextEdge.originXProperty().bind(nextKey.translateXProperty().add(nextKey.widthProperty()));
                        BKnextEdge.originYProperty().bind(nextKey.translateYProperty().add(nextKey.getHeight()));
                        
                        BKprevEdge.originXProperty().unbind();
                        if (Math.abs((double) newVal + movingKey.getWidthProperty() - BKprevEdge.getOriginX()) < 0.5){
                            BKprevEdge.originYProperty().bind(movingKey.translateXProperty().add(movingKey.widthProperty()));
                        }
                        
                        if (Math.abs((double) newVal - BKnewPrevEdge.getOriginX()) < 0.5){
                            
                            BKnewPrevEdge.originXProperty().unbind();
                            BKnewPrevEdge.originYProperty().unbind();
                            BKnewPrevEdge.originXProperty().bind(movingKey.translateXProperty());
                            BKnewPrevEdge.originYProperty().bind(movingKey.translateYProperty().add(movingKey.getHeight()));
                        }
                    }
                }
            }
        };
        borrowedKey.translateXProperty().addListener(posListener);
    
    }

    private void reassignEdgesOriginsFromKeyInserted2(InnerNodeView nodeView, int position, KeyView insertedKey) {
        Edge prevArrow = nodeView.getEdge(position); 
        prevArrow.originXProperty().unbind();
        prevArrow.originYProperty().unbind();
        prevArrow.originXProperty().bind(insertedKey.translateXProperty());
        prevArrow.originYProperty().bind(insertedKey.translateYProperty().add(insertedKey.getHeight()));
    
        Edge nextArrow = nodeView.getEdge(position + 1);
        if (position < nodeView.getNumberOfKeys() - 1) {
            KeyView nextKeyView = nodeView.getKey(position + 1);
            nextArrow.originXProperty().bind(nextKeyView.translateXProperty());
            nextArrow.originYProperty().bind(nextKeyView.translateYProperty().add(nextKeyView.getHeight()));
        }else{
            KeyView lastKey = nodeView.getLast();
            nextArrow.originXProperty().bind(lastKey.translateXProperty().add(lastKey.widthProperty()));
            nextArrow.originYProperty().bind(lastKey.translateYProperty().add(lastKey.getHeight()));
        }
    }
 
    private void reassignEdgesOriginsFromKeyInserted(Edge prevEdge, KeyView insertedKey, Edge nextEdge,  KeyView nextKeyView) {

        prevEdge.originXProperty().unbind();
        prevEdge.originYProperty().unbind();
        prevEdge.originXProperty().bind(insertedKey.translateXProperty());
        prevEdge.originYProperty().bind(insertedKey.translateYProperty().add(insertedKey.getHeight()));
    
        if (nextKeyView != null) {
            nextEdge.originXProperty().bind(nextKeyView.translateXProperty());
            nextEdge.originYProperty().bind(nextKeyView.translateYProperty().add(nextKeyView.getHeight()));
        }else{
            nextEdge.originXProperty().bind(insertedKey.translateXProperty().add(insertedKey.widthProperty()));
            nextEdge.originYProperty().bind(insertedKey.translateYProperty().add(insertedKey.getHeight()));
        }
    }
 
    private void handleChildNodeBorrowed(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.NodeBorrowed<Integer,String> e = (BPlusTreeEvent.NodeBorrowed<Integer,String>) event;
        NodeView borrowingNodeView = nodeToNodeView.get(e.getBorrowingNode());
        NodeView lendingNodeView = nodeToNodeView.get(e.getLendingNode());
        Edge arrow = childrenToEdge.get(nodeToNodeView.get(e.getChildNode()));


        ( (InnerNodeView) lendingNodeView).getEdges().remove(arrow);

        int lendingIndex = treeLevels.get(lendingNodeView.getLevel()).indexOf(lendingNodeView);
        int borrowingIndex = treeLevels.get(borrowingNodeView.getLevel()).indexOf(borrowingNodeView);

        if ( lendingIndex < borrowingIndex ){
            ((InnerNodeView) borrowingNodeView).getEdges().addFirst(arrow);
        }else{
            ((InnerNodeView) borrowingNodeView).getEdges().addLast(arrow);
        }

    }

    private void handleChildNodeDeleted(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.NodeDeleted<Integer,String> e = (BPlusTreeEvent.NodeDeleted<Integer,String>) event;
        if ( e.getParent() == null){
            nodeToNodeView.remove(e.getChild());
            treeLevels.removeFirst();
            depthProperty.set(0);
            return;
        }
        NodeView affectedNode = nodeToNodeView.get(e.getParent());
        NodeView deletedNode = nodeToNodeView.get(e.getChild());
        Edge edge = childrenToEdge.remove(deletedNode);

        Transition edgeFadeOut = animator.fadeNode(edge, 1, 0, SoundType.FADE_OUT);
        animator.addTransitionToQueue(edgeFadeOut);
        animator.combineLastsTransitionsOnQueue(2);
        edgeFadeOut.setOnFinished(_ -> {
            ((InnerNodeView)affectedNode).getEdges().remove(edge);
            this.getChildren().remove(edge);
            nodeToNodeView.remove(e.getChild());
        });        

        updateEdges(e.getChild());

    }

    private void handleChildNodeCreated(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.NodeCreated<Integer,String> e = (BPlusTreeEvent.NodeCreated<Integer,String>) event;
        Edge newChildEdge = new TreeEdge();
        newChildEdge.setOpacity(0.0);
        newChildEdge.darkModeProperty().bind(darkModeProperty);
        NodeView nodeView = nodeToNodeView.get(e.getParent());
        NodeView childNodeView = nodeToNodeView.get(e.getChild());
        int childIndex = findChildIndex(e.getParent(), e.getChild());

        ((InnerNodeView)nodeView).getEdges().add(childIndex,newChildEdge);
        
        //Bind end of Arrow to ChildNodeView
        newChildEdge.endXProperty().bind(childNodeView.centerXProperty());
        newChildEdge.endYProperty().bind(childNodeView.translateYProperty());

        childrenToEdge.put(childNodeView, newChildEdge);
        this.getChildren().add(newChildEdge);
        this.unshownEdges.add(newChildEdge);
    }

    private void handleKeyChanged(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.KeyChanged<Integer,String> e = (BPlusTreeEvent.KeyChanged<Integer,String>) event;
        KeyView changedKeyView = keyToKeyView.get(e.getKey());
        animator.addTransitionToQueue(animator.animateTextChange(changedKeyView.getKeyLabel(), String.valueOf(e.getNewKey())));
    }

    private int findChildIndex(BPlusNode<Integer, String> parent, BPlusNode<Integer, String> child) {
        for (int i = 0; i < ((BPlusInnerNode<Integer, String>) parent).getChildren().size(); i++) {
            if (((BPlusInnerNode<Integer, String>) parent).getChild(i) == child) {
                return i;
            }
        }
        return 0;
    }
    
    public void updateLevelLayout(int level){

        if (level < 0 || level >= treeLevels.size()) {
            return; 
        }

        double nodesTotalWidth = this.treeLevels.get(level).stream()
                                                        .mapToDouble(NodeView::getWidth)
                                                        .sum() ;

        
        double nodeSpacing = ((canvasWidth.get()/scaleProperty.get()) - nodesTotalWidth - 2 * X_PADDING) / (treeLevels.get(level).size() + 1);

        if (nodeSpacing < MIN_NODE_SPACING){

            double blankSpaceNeeded = MIN_NODE_SPACING * (treeLevels.get(level).size() + 1) + 2 * X_PADDING;
            scaleProperty.set( canvasWidth.get() / (blankSpaceNeeded + nodesTotalWidth) );
            nodeSpacing = MIN_NODE_SPACING;
            //TO DO: Scale down the tree view

        }else if (nodeSpacing > MAX_NODE_SPACING){
            //TODO: Scale up the tree view
            //nodeSpacing = MAX_NODE_SPACING;
        }

        double startX = X_PADDING + nodeSpacing;

    
        for (NodeView node : this.treeLevels.get(level)) {

            double deltaX = startX - node.xOriginProperty().get() ;
            if ( nodeToNodeView.size() == 1 && node.keys.size() == 1){
                animator.addParallelTransition(animator.moveNode(node, deltaX, 0, 0.1));
            }else{
                animator.addParallelTransition(animator.moveNode(node, deltaX, 0,null));
            }
            node.updateLayout(deltaX);
            startX += node.getWidth() + nodeSpacing;
        }
        animator.createParallelTransition();

    }


    private void updateYLayout(){

        for (int i = 0; i < this.treeLevels.size(); i++) {

            double startY = Y_PADDING * (2*i + 1);

            for (NodeView node : this.treeLevels.get(i)) {
                double deltaY = startY - node.yOriginProperty().get();
                node.setYOrigin(startY);
                animator.addParallelTransition(animator.moveNode(node, 0, deltaY, null));
                if (!node.keys.isEmpty()) {
                    node.keys.forEach(key -> {
                        double byY = key.getDeltaY() + deltaY;
                        Transition movingKey = animator.moveNode(key, 0.0, byY,null);
                        key.setNewOriginY(key.getNewYOrigin() + deltaY);
                        key.setCurrentYOrigin(key.getNewYOrigin());
                        animator.addParallelTransition(movingKey);
                    });
                }
            }
            startY += Y_PADDING;
        }
        animator.createParallelTransition();

    }

    public void setCanvasSize(double width, double height) {
        this.canvasWidth.set(width);
        this.canvasHeight.set(height);
    }

    private  void showEdges(){
        for (Edge edges : this.unshownEdges) {
            Transition fadeIn = animator.fadeNode(edges, 0, 1, null);
            animator.addParallelTransition(fadeIn);
        }
        this.unshownEdges.clear();
        animator.createParallelTransition();
    }

    public void animateTraversal(List<Key<Integer>> visitedKeys) {
        
        if (visitedKeys.size() == 0) return;

        NodeView currentNode = null;

        while( !visitedKeys.isEmpty()){

            KeyView currentKey = keyToKeyView.get(visitedKeys.remove(0));
            
            if (currentNode == null) {
                currentNode = currentKey.getNode();
            }else{
                if (currentNode != currentKey.getNode()){
                    currentNode = currentKey.getNode();
                    animator.addToTraversalList(animator.highlightEdge(childrenToEdge.get(currentNode),SoundType.HIGHLIGHTING));
                }
            }

            animator.addToTraversalList(animator.highlightKeyView(currentKey,SoundType.HIGHLIGHTING));
        }
    }

    public void updateKeyViews(){
        for (KeyView keyView : borrowedKeys) {
            keyView.updateNode();
        }
    }

    public KeyView getKeyView(Key<Integer> key){
        return keyToKeyView.get(key);
    }
    
    public DoubleProperty canvasWidthProperty() { return canvasWidth; }

    public DoubleProperty canvasHeightProperty() { return canvasHeight; }

    public Double getCanvasWidth() { return canvasWidth.get(); }

    public Double getCanvasHeight() { return canvasHeight.get(); }

    public DoubleProperty scaleProperty() { return scaleProperty; }

    public IntegerProperty depthProperty() { return depthProperty; }

    public IntegerProperty widthProperty() { return widthProperty; }


    public BooleanProperty darkModeProperty() { return darkModeProperty; }


}
