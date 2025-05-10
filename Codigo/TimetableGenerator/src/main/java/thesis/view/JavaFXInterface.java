package thesis.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFXInterface extends Application {
    private static String applicationName;

    public static void instantiateGUI(String name) {
        applicationName = name;
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Cliente:");
        Button button = new Button("Botão teste");

        button.setOnAction(e -> {
            try {
                label.setText("Teste botão!");
            } catch (Exception ex) {
                label.setText("Erro no teste");
            }
        });

        VBox vbox = new VBox(label, button);
        Scene scene = new Scene(vbox, 400, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle(applicationName);
        primaryStage.show();
    }
}
