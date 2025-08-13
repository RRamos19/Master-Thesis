package thesis;

import thesis.controller.Controller;
import thesis.controller.ControllerInterface;
import thesis.model.Model;
import thesis.model.ModelInterface;
import thesis.model.domain.DataRepository;
import thesis.view.JavaFXView;

public class Main {
    public static void main(String[] args) {
        ControllerInterface controller = new Controller();

        ModelInterface<DataRepository> model = new Model();
        controller.setModel(model);

        JavaFXView.launch(controller, "Timetable Generator");
    }
}
