package thesis.view.managers.windows;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Arrays;

public class ExceptionMessageWindow {
    private final static int PREF_WIDTH = 620;

    private final Alert exceptionAlert;
    private final TextArea stacktraceMessage;

    public ExceptionMessageWindow() {
        exceptionAlert = new Alert(Alert.AlertType.ERROR);
        exceptionAlert.setTitle("Exception Message");
        exceptionAlert.setHeaderText("An error occurred!");

        exceptionAlert.getDialogPane().setPrefWidth(PREF_WIDTH);

        stacktraceMessage = new TextArea();
        stacktraceMessage.setEditable(false);
        stacktraceMessage.setWrapText(true);

        stacktraceMessage.setMaxWidth(Double.MAX_VALUE);
        stacktraceMessage.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(stacktraceMessage, Priority.ALWAYS);
        GridPane.setHgrow(stacktraceMessage, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label("Details of the exception:"), 0, 0);
        expContent.add(stacktraceMessage, 0, 1);

        exceptionAlert.getDialogPane().setExpandableContent(expContent);
    }

    public Alert getExceptionAlert() {
        return exceptionAlert;
    }

    public void setMessages(Throwable e) {
        exceptionAlert.setContentText(e.getMessage());
        stacktraceMessage.setText(Arrays.toString(e.getStackTrace()));
    }
}
