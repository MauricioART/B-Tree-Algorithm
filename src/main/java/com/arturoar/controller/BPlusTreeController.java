package com.arturoar.controller;

import java.util.List;

import com.arturoar.exceptions.BPlusTreeEmptyException;
import com.arturoar.model.BPlusLeafNode;
import com.arturoar.model.BPlusTree;
import com.arturoar.model.Key;
import com.arturoar.util.BPlusTraversalResult;
import com.arturoar.util.SoundType;
import com.arturoar.util.TreeAnimator;
import com.arturoar.util.TreeViewTransformer;
import com.arturoar.view.TreeView;

import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class BPlusTreeController {

    private static final PseudoClass LIGHT = PseudoClass.getPseudoClass("light");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private enum Operation { SEARCH, INSERT, DELETE }

    @FXML private BorderPane rootPane;
    @FXML private Pane canvas;
    @FXML private VBox emptyState;
    @FXML private VBox operationPane;
    @FXML private VBox valuePane;
    @FXML private VBox resultPane;
    @FXML private VBox settingsPane;
    @FXML private HBox toolbar;
    @FXML private TextField keyField;
    @FXML private TextField valueField;
    @FXML private Button submitButton;
    @FXML private ToggleButton searchButton;
    @FXML private ToggleButton insertButton;
    @FXML private ToggleButton deleteButton;
    @FXML private ToggleButton settingsButton;
    @FXML private ComboBox<Integer> orderChoice;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;
    @FXML private Label treeInfoLabel;
    @FXML private Label keyCountLabel;
    @FXML private Label resultTitle;
    @FXML private Label resultDetail;
    @FXML private CheckBox traversalToggle;
    @FXML private CheckBox soundToggle;
    @FXML private CheckBox lightThemeToggle;

    private final Stage stage;
    private final TreeAnimator animator = TreeAnimator.getInstance();
    private Operation operation = Operation.SEARCH;
    private BPlusTree<Integer, String> tree;
    private TreeView treeView;
    private TreeViewTransformer transformer;

    public BPlusTreeController(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        for (VBox pane : List.of(operationPane, valuePane, resultPane, settingsPane)) {
            pane.managedProperty().bind(pane.visibleProperty());
        }
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(canvas.widthProperty());
        clip.heightProperty().bind(canvas.heightProperty());
        canvas.setClip(clip);
        addGrid();

        animator.animationSpeedProperty().bind(Bindings.divide(1.0, speedSlider.valueProperty()));
        animator.muteProperty().bind(soundToggle.selectedProperty().not());
        speedLabel.textProperty().bind(speedSlider.valueProperty().asString("Velocidad · %.1fx"));
        lightThemeToggle.selectedProperty().addListener((_, _, light) ->
                rootPane.pseudoClassStateChanged(LIGHT, light));

        orderChoice.getItems().addAll(4, 5, 6, 7, 8, 9, 10);
        orderChoice.setValue(4);
        initializeTree(4);
        selectOperation(Operation.SEARCH, "Buscar");
    }

    private void addGrid() {
        Path grid = new Path();
        grid.getStyleClass().add("board-grid");
        grid.setManaged(false);
        grid.setMouseTransparent(true);
        canvas.getChildren().add(grid);
        canvas.layoutBoundsProperty().addListener((_, _, bounds) -> {
            grid.getElements().clear();
            double width = bounds.getWidth();
            double height = bounds.getHeight();
            for (double x = 20.5; x < width; x += 20) {
                grid.getElements().addAll(new MoveTo(x, 0), new LineTo(x, height));
            }
            for (double y = 20.5; y < height; y += 20) {
                grid.getElements().addAll(new MoveTo(0, y), new LineTo(width, y));
            }
        });
    }

    private void initializeTree(int order) {
        if (treeView != null) {
            tree.removeObserver(treeView);
            treeView.canvasWidthProperty().unbind();
            treeView.canvasHeightProperty().unbind();
            treeView.darkModeProperty().unbind();
            canvas.getChildren().remove(treeView);
        }

        tree = new BPlusTree<>(order);
        treeView = new TreeView();
        treeView.setManaged(false);
        tree.addObserver(treeView);
        transformer = new TreeViewTransformer(canvas, treeView);
        treeView.darkModeProperty().bind(lightThemeToggle.selectedProperty().not());
        treeView.canvasWidthProperty().bind(canvas.widthProperty());
        treeView.canvasHeightProperty().bind(canvas.heightProperty());
        canvas.getChildren().add(treeView);

        emptyState.visibleProperty().bind(tree.emptyProperty());
        treeInfoLabel.textProperty().bind(treeView.depthProperty().asString("Orden " + order + " · %d niveles"));
        keyCountLabel.textProperty().bind(treeView.widthProperty().asString("%d claves"));
        showResult("Sin selección", "Busca una clave para consultar su valor.", false);
    }

    @FXML private void showSearch() { selectOperation(Operation.SEARCH, "Buscar"); }
    @FXML private void showInsert() { selectOperation(Operation.INSERT, "Insertar"); }
    @FXML private void showDelete() { selectOperation(Operation.DELETE, "Borrar"); }
    @FXML private void resetViewport() { transformer.resetView(); }

    private void selectOperation(Operation selected, String label) {
        operation = selected;
        searchButton.setSelected(selected == Operation.SEARCH);
        insertButton.setSelected(selected == Operation.INSERT);
        deleteButton.setSelected(selected == Operation.DELETE);
        settingsButton.setSelected(false);
        operationPane.setVisible(true);
        resultPane.setVisible(true);
        settingsPane.setVisible(false);
        valuePane.setVisible(selected == Operation.INSERT);
        submitButton.setText(label);
        keyField.requestFocus();
    }

    @FXML
    private void showSettings() {
        if (settingsPane.isVisible()) {
            switch (operation) {
                case SEARCH -> showSearch();
                case INSERT -> showInsert();
                case DELETE -> showDelete();
            }
            return;
        }
        searchButton.setSelected(false);
        insertButton.setSelected(false);
        deleteButton.setSelected(false);
        settingsButton.setSelected(true);
        operationPane.setVisible(false);
        resultPane.setVisible(false);
        settingsPane.setVisible(true);
    }

    @FXML
    private void changeOrder() {
        Integer order = orderChoice.getValue();
        if (tree == null || order == null || order == tree.getM()) return;
        if (!tree.emptyProperty().get()) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "Cambiar el orden vaciará el árbol actual.", ButtonType.OK, ButtonType.CANCEL);
            confirmation.setTitle("Cambiar orden");
            confirmation.setHeaderText(null);
            confirmation.initOwner(stage);
            confirmation.getDialogPane().getStylesheets().setAll(rootPane.getStylesheets());
            confirmation.getDialogPane().getStyleClass().add("terminal");
            confirmation.getDialogPane().pseudoClassStateChanged(LIGHT, lightThemeToggle.isSelected());
            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                orderChoice.setValue(tree.getM());
                return;
            }
        }
        initializeTree(order);
    }

    @FXML
    private void submitOperation() {
        if (operationPane.isDisabled()) return;
        int key;
        try {
            key = Integer.parseInt(keyField.getText().strip());
        } catch (NumberFormatException exception) {
            showResult("Clave no válida", "Escribe un entero de 32 bits.", true);
            return;
        }

        toolbar.setDisable(true);
        operationPane.setDisable(true);
        treeView.clearSelection();
        try {
            List<Key<Integer>> visited = switch (operation) {
                case SEARCH -> search(key);
                case INSERT -> insert(key);
                case DELETE -> remove(key);
            };
            if (traversalToggle.isSelected()) {
                treeView.animateTraversal(visited);
            }
        } catch (BPlusTreeEmptyException exception) {
            showResult("Árbol vacío", "Inserta una clave para empezar.", true);
        }

        animator.setGlobalFinishCallback(() -> {
            treeView.updateKeyViews();
            toolbar.setDisable(false);
            operationPane.setDisable(false);
            keyField.selectAll();
            keyField.requestFocus();
        });
        animator.animateQueue();
    }

    private List<Key<Integer>> search(int key) throws BPlusTreeEmptyException {
        var result = tree.search(key);
        BPlusLeafNode<Integer, String> leaf = result.getResult();
        if (leaf == null) {
            showResult("Clave " + key + " ausente", "No hay un valor asociado.", true);
        } else {
            String value = leaf.getData(Integer.valueOf(key));
            showResult("Clave " + key + " encontrada",
                    "Hoja " + leaf.getKeyValues() + "\nValor · " + value, false);
            treeView.highlightResult(result.getVisitedKeys().getLast());
        }
        return result.getVisitedKeys();
    }

    private List<Key<Integer>> insert(int key) throws BPlusTreeEmptyException {
        String value = valueField.getText();
        BPlusTraversalResult<Boolean, Integer, String> result = tree.insert(key, value);
        if (result.getResult()) {
            showResult("Clave " + key + " insertada", "Valor · " + value, false);
            valueField.clear();
        } else {
            showResult("La clave " + key + " ya existe", "El valor original se conserva.", true);
        }
        return result.getVisitedKeys();
    }

    private List<Key<Integer>> remove(int key) throws BPlusTreeEmptyException {
        var result = tree.remove(key);
        if (result.getResult() == null) {
            showResult("Clave " + key + " ausente", "No se eliminó ninguna entrada.", true);
        } else {
            showResult("Clave " + key + " borrada", "", false);
        }
        return result.getVisitedKeys();
    }

    private void showResult(String title, String detail, boolean error) {
        resultTitle.setText(title);
        resultTitle.pseudoClassStateChanged(ERROR, error);
        resultDetail.setText(detail);
        if (error && soundToggle.isSelected()) {
            SoundType.ERROR.play();
        }
    }
}
