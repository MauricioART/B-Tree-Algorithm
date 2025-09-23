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
import com.arturoar.util.TreeAnimator;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;

public class TreeView extends Group implements BPlusTreeObserver<Integer, String> {

    private final Double X_PADDING = 20.0;
    private final Double Y_PADDING = 50.0;

    private final Map<Key<Integer>, KeyView> keyToKeyView = new HashMap<>();
    private final Map<BPlusNode<Integer, String>, NodeView> nodeToNodeView = new HashMap<>();
    private final Map<BPlusNode<Integer, String>, Arrow> childrenToArrow = new HashMap<>();
    public List<List<NodeView>> treeLevels = new ArrayList<>();
    private List<Arrow> unshownArrows = new ArrayList<>();

    private DoubleProperty canvasWidth = new SimpleDoubleProperty();
    private DoubleProperty canvasHeight = new SimpleDoubleProperty();

    public TreeView(BPlusNode<Integer, String> root) {

        ChangeListener<Number> listener = new ChangeListener<>() {
            private boolean first = true;

            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldValue, Number newValue) {
                if (first) {
                    System.out.println("Canvas size initialized: " + newValue);
                    handleNewRoot(new BPlusTreeEvent.NewRoot<>(root));
                    first = false; 
                }
            }
        };

        this.canvasWidth.addListener(listener);
        this.canvasHeight.addListener(listener);

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

    }

    private void handleNewRoot(BPlusTreeEvent<Integer, String> event) {
        // Implementation for handling new root if needed
        
        BPlusTreeEvent.NewRoot<Integer,String> e = (BPlusTreeEvent.NewRoot<Integer,String>) event;


        if (nodeToNodeView.containsKey(e.getNewRoot())) {
            this.treeLevels.removeFirst();
        }else{
            NodeView newNode = NodeFactory.createNode(e.getNewRoot().isLeaf(), this.canvasWidth.get()/2, Y_PADDING);
            this.nodeToNodeView.put(e.getNewRoot(), newNode);
            //this.getChildren().add(newNode);
            List<NodeView> newLevel = new ArrayList<>();
            newLevel.add(newNode);
            this.treeLevels.addFirst(newLevel);   
        }

        updateYLayout();
        TreeAnimator.combineLastTransitionsOnQueue();
    }
    
    private void handleNodeSplit(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.NodeSplit<Integer,String> e = (BPlusTreeEvent.NodeSplit<Integer,String>) event;
        NodeView splitNode = this.nodeToNodeView.get(e.getNode());
        KeyView middleKey = keyToKeyView.get(e.getMiddleKey());
        NodeView newNode = NodeFactory.createNode(e.getNewNode().isLeaf(), middleKey.getTranslateX(), splitNode.getYOrigin());
        this.nodeToNodeView.put(e.getNewNode(), newNode);

        // Inserting new NodeView in the correct position

            int levelIndex = e.getNewNode().getLevel();
            int nodeChildIndex = this.treeLevels.get(levelIndex).indexOf(splitNode) + 1;

            this.treeLevels.get(levelIndex).add(nodeChildIndex, newNode);

            if (e.getNode().isLeaf()) {
                LeafNodeView leafNode = (LeafNodeView) newNode;
                if (nodeChildIndex < this.treeLevels.get(levelIndex).size() - 1){
                    LeafNodeView nextLeaf = (LeafNodeView) this.treeLevels.get(levelIndex).get(nodeChildIndex + 1);
                    leafNode.nextLeaf.endYProperty().bind(nextLeaf.yOriginProperty().add(nextLeaf.getHeight()/2));

                    if (nodeChildIndex != 0){
                        LeafNodeView prevNode = (LeafNodeView) this.treeLevels.get(levelIndex).get(nodeChildIndex - 1);
                        prevNode.nextLeaf.endYProperty().unbind();
                        prevNode.nextLeaf.endYProperty().bind(leafNode.yOriginProperty().add(leafNode.getHeight()/2));
                    }
                }
            } else{
                
                int childIndex = splitNode.getKeyIndex(middleKey);

                Arrow prevArrow = ((InnerNodeView) splitNode).getEdges().get(childIndex);
                KeyView prevKey = ((InnerNodeView) splitNode).keys.get(childIndex - 1);
                prevArrow.originXProperty().unbind();
                prevArrow.originYProperty().unbind();
                prevArrow.originXProperty().bind(prevKey.translateXProperty().add(prevKey.getWidth()));
                prevArrow.originYProperty().bind(prevKey.translateYProperty().add(prevKey.getHeight()));

                Arrow nextArrow = ((InnerNodeView) splitNode).getEdges().get(childIndex + 1);
                KeyView nextKey = ((InnerNodeView) splitNode).keys.get(childIndex + 1);
                nextArrow.originXProperty().bind(nextKey.translateXProperty());
                nextArrow.originYProperty().bind(nextKey.translateYProperty().add(nextKey.getHeight()));

                Arrow splitNodeLastEdge = ((InnerNodeView) splitNode).getEdges().getLast();
                KeyView lastKey = ((InnerNodeView) splitNode).keys.getLast();
                splitNodeLastEdge.originXProperty().unbind();
                splitNodeLastEdge.originYProperty().unbind();
                splitNodeLastEdge.originXProperty().bind(lastKey.translateXProperty().add(middleKey.getWidth()));
                splitNodeLastEdge.originYProperty().bind(lastKey.translateYProperty().add(middleKey.getHeight()));


            }

    }


    private void handleNodeDeleted(BPlusTreeEvent<Integer, String> event) {

        BPlusTreeEvent.NodeDeleted<Integer,String> e = (BPlusTreeEvent.NodeDeleted<Integer,String>) event;
        NodeView deletedNode = nodeToNodeView.remove(e.getNode());
        this.treeLevels.get(e.getNode().getLevel()).remove(deletedNode);

        //Transition keyDeletion = TreeAnimator.animateKeyDeletion(deletedNode);
        //TreeAnimator.addTransitionToQueue(keyDeletion);
        //keyDeletion.setOnFinished(_ -> this.getChildren().remove(deletedNode));
        updateYLayout();
    }

    private void handleKeyInserted(BPlusTreeEvent<Integer, String> event) {

        KeyView newKey = createKeyView(event);

        BPlusTreeEvent.KeyInserted<Integer,String> e = (BPlusTreeEvent.KeyInserted<Integer,String>) event;

        NodeView affectedNodeView = nodeToNodeView.get(e.getNode());
        
        //Register new key view
        this.keyToKeyView.put(e.getKey(), newKey);
        int position = affectedNodeView.insert(newKey);

        if (!e.getNode().isLeaf()){
            reasignnArrowOrigins((InnerNodeView) affectedNodeView, position, newKey);
        }

        this.getChildren().add(newKey);
        newKey.setIsNew(false);

        // Animate insertion
        int level = e.getNode().getLevel();
        updateLevelLayout(level);
        TreeAnimator.addTransitionToQueue(TreeAnimator.fadeNode(newKey,0,1));
        if (!this.unshownArrows.isEmpty()){
            showArrows();
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

        KeyView removedKey = keyToKeyView.remove(e.getKey());
        NodeView node = nodeToNodeView.get(e.getNode());
        node.remove(removedKey);

        
        Transition keyFading = TreeAnimator.fadeNode(removedKey, 1, 0);
        TreeAnimator.addTransitionToQueue(keyFading);
        keyFading.setOnFinished(_ -> this.getChildren().remove(removedKey));    
        
        updateLevelLayout(e.getNode().getLevel());

    }

    private void handleKeyBorrowed(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.KeyBorrowed<Integer,String> e = (BPlusTreeEvent.KeyBorrowed<Integer,String>) event;
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
        }
        //FADE IN ARROWS IN QUEUE

        final int position = tempPosition;
        final KeyView borrowedKey = tempBorrowedKey;

        if (lendingLevel == borrowingLevel) {
            updateLevelLayout(lendingLevel);
        } else {
            updateLevelLayout(lendingLevel);
            updateLevelLayout(borrowingLevel);
            TreeAnimator.combineLastTransitionsOnQueue();
            updateYLayout();
            TreeAnimator.combineLastTransitionsOnQueue();
            if (position != -1 && borrowedKey != null) {
                TreeAnimator.addListenerToLastTransition(() -> reasignnArrowOrigins((InnerNodeView) borrowingNode, position, borrowedKey));
            }
            showArrows();
        }
    }

    private void reasignnArrowOrigins(InnerNodeView nodeView, int position, KeyView borrowedKey) {
        Arrow prevArrow = nodeView.getEdge(position); 
        prevArrow.originXProperty().unbind();
        prevArrow.originYProperty().unbind();
        prevArrow.originXProperty().bind(borrowedKey.translateXProperty());
        prevArrow.originYProperty().bind(borrowedKey.translateYProperty().add(borrowedKey.getHeight()));
    
        Arrow nextArrow = nodeView.getEdge(position + 1);
        if (position < nodeView.getNumberOfKeys() - 1) {
            KeyView nextKeyView = nodeView.getKey(position + 1);
            nextArrow.originXProperty().bind(nextKeyView.translateXProperty());
            nextArrow.originYProperty().bind(nextKeyView.translateYProperty().add(nextKeyView.getHeight()));
        }else{
            KeyView lastKey = nodeView.getLast();
            nextArrow.originXProperty().bind(lastKey.translateXProperty().add(lastKey.getWidth()));
            nextArrow.originYProperty().bind(lastKey.translateYProperty().add(lastKey.getHeight()));
        }
    }
 
    private void handleChildNodeBorrowed(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.ChildNodeBorrowed<Integer,String> e = (BPlusTreeEvent.ChildNodeBorrowed<Integer,String>) event;
        NodeView borrowingNodeView = nodeToNodeView.get(e.getBorrowingNode());
        NodeView lendingNodeView = nodeToNodeView.get(e.getLendingNode());
        Arrow arrow = childrenToArrow.get(e.getChildNode());

        if (!e.getLendingNode().isLeaf()){
           ( (InnerNodeView) lendingNodeView).getEdges().remove(arrow);
        }

        int childIndex = findChildIndex(e.getLendingNode(), e.getChildNode());
        ((InnerNodeView)borrowingNodeView).getEdges().add(childIndex, arrow);
    }

    private void handleChildNodeDeleted(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.ChildNodeDeleted<Integer,String> e = (BPlusTreeEvent.ChildNodeDeleted<Integer,String>) event;
        NodeView affectedNode = nodeToNodeView.get(e.getParent());
        Arrow deletedArrow = childrenToArrow.remove(e.getChild());

        ((InnerNodeView)affectedNode).getEdges().remove(deletedArrow);
    }

    private void handleChildNodeCreated(BPlusTreeEvent<Integer, String> event) {
        BPlusTreeEvent.ChildNodeCreated<Integer,String> e = (BPlusTreeEvent.ChildNodeCreated<Integer,String>) event;
        Arrow newChildEdge = new Arrow();
        newChildEdge.setOpacity(0.0);
        NodeView nodeView = nodeToNodeView.get(e.getParent());
        NodeView childNodeView = nodeToNodeView.get(e.getChild());
        int childIndex = findChildIndex(e.getParent(), e.getChild());

        ((InnerNodeView)nodeView).getEdges().add(childIndex,newChildEdge);
        
        //Bind end of Arrow to ChildNodeView
        newChildEdge.endXProperty().bind(childNodeView.centerXProperty());
        newChildEdge.endYProperty().bind(childNodeView.translateYProperty());

        childrenToArrow.put(e.getChild(), newChildEdge);
        this.getChildren().add(newChildEdge);
        this.unshownArrows.add(newChildEdge);
    }

    private int findChildIndex(BPlusNode<Integer, String> parent, BPlusNode<Integer, String> child) {
        for (int i = 0; i < ((BPlusInnerNode<Integer, String>) parent).getChildren().size(); i++) {
            if (((BPlusInnerNode<Integer, String>) parent).getChild(i) == child) {
                return i;
            }
        }
        return 0;
    }
    
    private void updateLevelLayout(int level){

        if (level < 0 || level >= this.treeLevels.size()) {
            return; 
        }

        double totalWidth = this.treeLevels.get(level).stream()
                                                        .mapToDouble(NodeView::getWidth)
                                                        .sum() ;

        double nodeSpacing = (this.canvasWidth.get() - totalWidth - 2 * X_PADDING) / (this.treeLevels.get(level).size() + 1);

        double startX = X_PADDING + nodeSpacing;

        for (NodeView node : this.treeLevels.get(level)) {
            double deltaX = startX - node.xOriginProperty().get() ;
            TreeAnimator.addParallelTransition(TreeAnimator.moveNode(node, deltaX, 0));
            node.updateLayout(deltaX);
            startX += node.getWidth() + nodeSpacing;
        }
        TreeAnimator.createParallelTransition();

    }


    private void updateYLayout(){

        for (int i = 0; i < this.treeLevels.size(); i++) {

            double startY = Y_PADDING * (2*i + 1);

            for (NodeView node : this.treeLevels.get(i)) {
                double deltaY = startY - node.yOriginProperty().get();
                node.setYOrigin(startY);
                TreeAnimator.addParallelTransition(TreeAnimator.moveNode(node, 0, deltaY));
                if (!node.keys.isEmpty()) {
                    node.keys.forEach(key -> {
                        double byY = key.getDeltaY() + deltaY;
                        Transition movingKey = TreeAnimator.moveNode(key, 0.0, byY);
                        key.setNewOriginY(key.getNewYOrigin() + deltaY);
                        key.setCurrentYOrigin(key.getNewYOrigin());
                        TreeAnimator.addParallelTransition(movingKey);
                    });
                }
            }
            startY += Y_PADDING;
        }
        TreeAnimator.createParallelTransition();

    }

    public void setCanvasSize(double width, double height) {
        this.canvasWidth.set(width);
        this.canvasHeight.set(height);
    }

    private  void showArrows(){
        for (Arrow arrow : this.unshownArrows) {
            Transition fadeIn = TreeAnimator.fadeNode(arrow, 0, 1);
            TreeAnimator.addParallelTransition(fadeIn);
        }
        this.unshownArrows.clear();
        TreeAnimator.createParallelTransition();
    }

    
    public DoubleProperty canvasWidthProperty() { return canvasWidth; }

    public DoubleProperty canvasHeightProperty() { return canvasHeight; }

    public Double getCanvasWidth() { return canvasWidth.get(); }

    public Double getCanvasHeight() { return canvasHeight.get(); }



}
