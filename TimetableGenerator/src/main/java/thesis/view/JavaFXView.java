package thesis.view;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import thesis.controller.Controller;
import thesis.controller.ControllerInterface;
import thesis.view.viewobjects.ViewModel;

import java.io.IOException;

public class JavaFXView implements ViewInterface {
    private static final String APP_FXML_PATH = "/view/main_view.fxml";

    private Parent root;
    private ControllerInterface controller;

    // UI Components
    @FXML private Pane applicationPrincipalPane;
    @FXML private Pane dragAndDropPane;
    @FXML private MenuItem importITCBtn;
    @FXML private MenuItem exportDataITCBtn;
    @FXML private MenuItem exportDataCSVBtn;
    @FXML private MenuItem exportSolutionsITCBtn;
    @FXML private MenuItem exportSolutionsPNGBtn;
    @FXML private MenuItem exportSolutionsPDFBtn;
    @FXML private MenuItem configurationBtn;
    @FXML private MenuItem instructionsBtn;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TreeView<Controller.TableType> treeView;
    @FXML private Button connectBtn;
    @FXML private VBox taskContainer;
    @FXML private ListView<HBox> progressContainer;
    @FXML private ChoiceBox<String> programsChoiceBox;
    @FXML private TableView<ViewModel> tableView;
    @FXML private Button reoptimizeButton;
    @FXML private Button removeButton;
    @FXML private Button generateSolutionBtn;

    public JavaFXView(ControllerInterface controller) {
        this.controller = controller;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(APP_FXML_PATH));
            loader.setController(this);
            root = loader.load();

            // Bind the buttons to the events
            connectBtn.setOnAction(e -> controller.connectEvent());
            importITCBtn.setOnAction(e -> controller.importDataITCEvent());
            exportDataITCBtn.setOnAction(e -> controller.exportDataITCEvent());
            exportDataCSVBtn.setOnAction(e -> controller.exportDataCSVEvent());
            exportSolutionsITCBtn.setOnAction(e -> controller.exportSolutionsITCEvent());
            exportSolutionsPNGBtn.setOnAction(e -> controller.exportSolutionsPNGEvent());
            exportSolutionsPDFBtn.setOnAction(e -> controller.exportSolutionsPDFEvent());
            configurationBtn.setOnAction(e -> controller.configMenuEvent());
            instructionsBtn.setOnAction(e -> controller.showInstructionsMenuEvent());
            applicationPrincipalPane.setOnDragOver(e -> controller.dragOverEvent(e));
            applicationPrincipalPane.setOnDragEntered(e -> controller.dragEnteredEvent(e));
            applicationPrincipalPane.setOnDragExited(e -> controller.dragExitedEvent(e));
            applicationPrincipalPane.setOnDragDropped(e -> controller.dragDroppedEvent(e));
            reoptimizeButton.setOnAction(e -> controller.reoptimizeSolutionEvent());
            removeButton.setOnAction(e -> controller.removeTableInstanceEvent());
            generateSolutionBtn.setOnAction(e -> controller.generateSolutionEvent());
            tableView.setOnMouseClicked(e -> controller.tableViewMouseClickedEvent(e));
            programsChoiceBox.getSelectionModel().selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> controller.changeProgramChoiceEvent(newValue));

            // Update the table based on the category chosen on the left menu
            treeView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldItem, newItem) -> controller.updateTableView(newItem));

            // Update the window size if the number of tasks change
            progressContainer.getItems().addListener((ListChangeListener<HBox>) c -> controller.progressContainterResizeEvent());

            // Only show the taskContainer if there are any tasks
            taskContainer.managedProperty().bind(taskContainer.visibleProperty());
            taskContainer.visibleProperty().bind(
                    Bindings.isNotEmpty(progressContainer.getItems())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pane getApplicationPrincipalPane() {
        return applicationPrincipalPane;
    }

    @Override
    public Pane getDragAndDropPane() {
        return dragAndDropPane;
    }

    @Override
    public TextField getIpField() {
        return ipField;
    }

    @Override
    public TextField getPortField() {
        return portField;
    }

    @Override
    public TextField getUsernameField() {
        return usernameField;
    }

    @Override
    public PasswordField getPasswordField() {
        return passwordField;
    }

    @Override
    public TreeView<Controller.TableType> getTreeView() {
        return treeView;
    }

    @Override
    public Button getConnectBtn() {
        return connectBtn;
    }

    @Override
    public VBox getTaskContainer() {
        return taskContainer;
    }

    @Override
    public ListView<HBox> getProgressContainer() {
        return progressContainer;
    }

    @Override
    public ChoiceBox<String> getProgramsChoiceBox() {
        return programsChoiceBox;
    }

    @Override
    public TableView<ViewModel> getTableView() {
        return tableView;
    }

    @Override
    public Button getReoptimizeButton() {
        return reoptimizeButton;
    }

    @Override
    public Button getRemoveButton() {
        return removeButton;
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    public void stop() {
        controller.cleanup();
    }
}
