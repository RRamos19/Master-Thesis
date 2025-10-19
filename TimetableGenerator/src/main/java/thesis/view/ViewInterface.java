package thesis.view;

import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import thesis.controller.Controller;
import thesis.view.viewobjects.ViewModel;

public interface ViewInterface {
    Parent getRoot();

    // Getters for the UI components
    Pane getApplicationPrincipalPane();
    Pane getDragAndDropPane();
    TextField getIpField();
    TextField getPortField();
    TextField getUsernameField();
    PasswordField getPasswordField();
    TreeView<Controller.TableType> getTreeView();
    Button getConnectBtn();
    VBox getTaskContainer();
    ListView<HBox> getProgressContainer();
    ChoiceBox<String> getProgramsChoiceBox();
    TableView<ViewModel> getTableView();
    Button getReoptimizeButton();
    Button getRemoveButton();
}
