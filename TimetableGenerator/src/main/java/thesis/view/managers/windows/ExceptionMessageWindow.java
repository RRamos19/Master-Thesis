package thesis.view.managers.windows;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import thesis.view.utils.AppIcons;
import thesis.view.utils.Defaults;

import java.util.Arrays;

public class ExceptionMessageWindow {
    private final Stage exceptionStage;
    private final Label exceptionMessage;
    private final TextArea stacktraceMessage;

    public ExceptionMessageWindow(Window primaryWindow) {
        exceptionStage = new Stage();
        exceptionStage.getIcons().addAll(AppIcons.getAppIcons());
        exceptionStage.initStyle(StageStyle.DECORATED);
        exceptionStage.initOwner(primaryWindow);
        exceptionStage.initModality(Modality.APPLICATION_MODAL);
        exceptionStage.setTitle("Exception message");

        exceptionMessage = new Label();
        exceptionMessage.setWrapText(true);

        stacktraceMessage = new TextArea();
        stacktraceMessage.setEditable(false);
        stacktraceMessage.setWrapText(true);

        // Area where the stacktrace will be displayed (starts hidden)
        TitledPane titledPane = new TitledPane("Show stracktrace", stacktraceMessage);
        titledPane.setExpanded(false);
        titledPane.setAnimated(false);

        // Dynamically resize the window when the stacktrace is expanded
        titledPane.expandedProperty().addListener((obs, oldV, newV) ->
                Platform.runLater(exceptionStage::sizeToScene)
        );

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(event -> exceptionStage.close());
        HBox hbox = new HBox(closeBtn);
        hbox.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(10, exceptionMessage, titledPane, hbox);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);

        exceptionStage.setScene(new Scene(box));
    }

    public Stage getExceptionStage() {
        return exceptionStage;
    }

    public void setMessages(Exception e) {
        exceptionMessage.setText(e.getMessage());

        stacktraceMessage.setText(Arrays.toString(e.getStackTrace()));
    }
}
