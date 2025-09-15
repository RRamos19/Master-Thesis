package thesis.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import thesis.controller.ControllerInterface;
import thesis.view.utils.AppIcons;

import java.io.IOException;

public class JavaFXView extends Application {
    private static final String APP_FXML_PATH = "/main_view.fxml";

    private static ControllerInterface staticController;
    private static String staticApplicationName;
    private static JavaFXController javaFXController;

    public static void launch(ControllerInterface controller, String applicationName) {
        staticController = controller;
        staticApplicationName = applicationName;

        Application.launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(APP_FXML_PATH));

        Parent root = loader.load();

        // After the controller is loaded pass the MVC controller to it
        Object genericView = loader.getController();
        if(genericView instanceof ViewInterface) {
            ViewInterface view = (ViewInterface) genericView;

            staticController.setView(view);
        }

        javaFXController = loader.getController();
        javaFXController.setPrimaryWindow(stage);

        Scene scene = new Scene(root);
        stage.setTitle(staticApplicationName);

        stage.getIcons().addAll(AppIcons.getAppIcons());

        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (javaFXController != null) {
            javaFXController.cleanup();
        }
    }
}
