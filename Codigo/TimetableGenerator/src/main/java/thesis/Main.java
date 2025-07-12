package thesis;

import thesis.model.domain.DomainModel;
import thesis.model.domain.ScheduledLesson;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;
import thesis.model.exporter.DataExporter;
import thesis.model.exporter.TimetableDataExporter;
import thesis.model.domain.exceptions.ParsingException;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        InputFileReader<DomainModel> inputFileReader = new ITCFormatParser();
        DataExporter<DomainModel> dataExporter = new TimetableDataExporter();

//        DBManager<EntityModel> dbHibernateManager = new DBHibernateManager("timetabling_db", "localhost", "5432", "postgres", "123");
//
//        DomainModel dBData = null;
//        try {
//            dBData = ModelConverter.convertToDomain(dbHibernateManager.fetchData());
//        } catch (Exception e) {
//            System.out.println("Error while reading the data from the database. " + e.getMessage());
//        }
//
//        System.out.println(dBData);
//        System.out.println();

        DomainModel data = null;
        try {
            //data = inputFileReader.readFile("../../tg-spr18.xml");
            //data = inputFileReader.readFile("../../lums-spr18.xml");
            //data = inputFileReader.readFile("../../bet-sum18.xml");
            data = inputFileReader.readFile("../../pu-llr-spr07.xml");

            System.out.println(data);
            System.out.println();
        } catch (ParsingException e) {
            throw new RuntimeException(e.getMessage());
        }

        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(data, null, 1000, 0.001, 100, 5);
        //Timetable generatedSolution = simulatedAnnealing.execute();

        try {
            dataExporter.exportToITC(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //dbHibernateManager.storeData(dBData);

        //System.out.println("Saved in the database!");
    }
}
