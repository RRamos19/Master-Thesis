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

    private final Parent root;
    private final ControllerInterface controller;

    // UI Components
    @FXML private Pane applicationPrincipalPane;
    @FXML private Pane dragAndDropPane;
    @FXML private MenuItem importDataBtn;
    @FXML private MenuItem exportDataITCBtn;
    @FXML private MenuItem exportDataXLSXBtn;
    @FXML private MenuItem exportSolutionITCBtn;
    @FXML private MenuItem exportSolutionPNGBtn;
    @FXML private MenuItem exportSolutionPDFBtn;
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
    @FXML private Button generateSolutionButton;
    @FXML private Button programRemoveButton;
    @FXML private Label lastSyncLabel;

    public JavaFXView(ControllerInterface controller) {
        this.controller = controller;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(APP_FXML_PATH));
            loader.setController(this);
            root = loader.load();

            // Bind the buttons to the events
            connectBtn.setOnAction(e -> controller.connectEvent());
            importDataBtn.setOnAction(e -> controller.importDataEvent());
            exportDataITCBtn.setOnAction(e -> controller.exportDataITCEvent());
            exportDataXLSXBtn.setOnAction(e -> controller.exportDataXLSXEvent());
            exportSolutionITCBtn.setOnAction(e -> controller.exportSolutionITCEvent());
            exportSolutionPNGBtn.setOnAction(e -> controller.exportSolutionPNGEvent());
            exportSolutionPDFBtn.setOnAction(e -> controller.exportSolutionPDFEvent());
            configurationBtn.setOnAction(e -> controller.configMenuEvent());
            instructionsBtn.setOnAction(e -> controller.showInstructionsMenuEvent());
            applicationPrincipalPane.setOnDragOver(e -> controller.dragOverEvent(e));
            applicationPrincipalPane.setOnDragEntered(e -> controller.dragEnteredEvent(e));
            applicationPrincipalPane.setOnDragExited(e -> controller.dragExitedEvent(e));
            applicationPrincipalPane.setOnDragDropped(e -> controller.dragDroppedEvent(e));
            reoptimizeButton.setOnAction(e -> controller.reoptimizeSolutionEvent());
            removeButton.setOnAction(e -> controller.removeTableInstanceEvent());
            generateSolutionButton.setOnAction(e -> controller.generateSolutionEvent());
            tableView.setOnMouseClicked(e -> controller.tableViewMouseClickedEvent(e));
            programRemoveButton.setOnAction(e -> controller.removeProgramEvent());
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

            // Bind the label with the string that contains the last update date
            lastSyncLabel.textProperty().bind(controller.getLastSyncStringProperty());
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
}
