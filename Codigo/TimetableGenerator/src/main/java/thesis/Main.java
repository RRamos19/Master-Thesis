package thesis;

import thesis.controller.Controller;
import thesis.controller.ControllerInterface;
import thesis.model.Model;
import thesis.model.ModelInterface;
import thesis.model.exporter.DataExporter;
import thesis.model.exporter.TimetableDataExporter;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;
import thesis.view.JavaFXView;

public class Main {
    public static void main(String[] args) {
        ControllerInterface controller = new Controller();

        DataExporter dataExporter = new TimetableDataExporter();
        InputFileReader inputReader = new ITCFormatParser();
        ModelInterface model = new Model(inputReader, dataExporter);
        controller.setModel(model);

        JavaFXView.launch(controller, "Timetable Generator");
    }
}
