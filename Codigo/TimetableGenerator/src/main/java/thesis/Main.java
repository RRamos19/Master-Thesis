package thesis;

import thesis.model.domain.DomainModel;
import thesis.model.domain.Timetable;
import thesis.model.exporter.DataExporter;
import thesis.model.exporter.TimetableDataExporter;
import thesis.model.domain.exceptions.ParsingException;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;
import thesis.solver.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.solver.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.solver.solutionoptimizer.SimulatedAnnealing;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        InputFileReader<DomainModel> inputFileReader = new ITCFormatParser();
        DataExporter<DomainModel> dataExporter = new TimetableDataExporter();

        DomainModel data = null;
        try {
            //data = inputFileReader.readFile("../../tg-spr18.xml");
            //data = inputFileReader.readFile("../../lums-spr18.xml");
            data = inputFileReader.readFile("../../bet-sum18.xml");
            //data = inputFileReader.readFile("../../pu-llr-spr07.xml");
            //data = inputFileReader.readFile("../../pu-llr-spr07 - Copy.xml");

            System.out.println(data);
            System.out.println();
        } catch (ParsingException e) {
            throw new RuntimeException(e.getMessage());
        }

        long start = System.currentTimeMillis();
        InitialSolutionGenerator<Timetable> initialSolutionGen = new MullerSolutionGenerator(data);
        Timetable initialSolution = initialSolutionGen.generate(null);
        System.out.println("Initial solution time: " + (System.currentTimeMillis() - start)/1000 + "s");

        System.out.println();
        System.out.println("Initial Cost = " + initialSolution.cost());
        System.out.println();

        long optimizedStart = System.currentTimeMillis();
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(initialSolution, 1000, 0.001, 100, 5);
        Timetable generatedSolution = simulatedAnnealing.execute();
        generatedSolution.setRuntime((System.currentTimeMillis() - start)/1000);
        System.out.println("Optimized solution time: " + (System.currentTimeMillis() - optimizedStart)/1000 + "s");

        System.out.println();
        System.out.println("Final Cost = " + generatedSolution.cost());
        System.out.println();

        data.addTimetable(generatedSolution);

        try {
            dataExporter.exportSolutionsToITC(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //dbHibernateManager.storeData(dBData);
        //System.out.println("Saved in the database!");
    }
}
