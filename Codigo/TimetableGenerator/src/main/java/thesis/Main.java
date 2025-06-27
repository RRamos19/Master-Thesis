package thesis;

import thesis.model.domain.ClassUnit;
import thesis.model.domain.DomainModel;
import thesis.model.domain.Timetable;
import thesis.model.mapper.ModelConverter;
import thesis.model.domain.exceptions.ParsingException;
import thesis.model.persistence.EntityModel;
import thesis.model.repository.DBHibernateManager;
import thesis.model.repository.DBManager;
import thesis.model.repository.HibernateUtils;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;
import thesis.service.initialsolutiongenerator.InitialSolutionGenerator;
import thesis.service.initialsolutiongenerator.MullerSolutionGenerator;
import thesis.service.solutionoptimizer.SimulatedAnnealing;

public class Main {
    public static void main(String[] args) {
        InputFileReader<DomainModel> inputFileReader = new ITCFormatParser();

        HibernateUtils.init("timetabling_db", "localhost", "5432", "postgres", "123");
        DBManager<EntityModel> dbHibernateManager = new DBHibernateManager();

        DomainModel dBData = null;
        try {
            dBData = ModelConverter.convertToDomain(dbHibernateManager.fetchData());
        } catch (Exception e) {
            System.out.println("Error while reading the data from the database. " + e.getMessage());
        }

        System.out.println(dBData);
        System.out.println();

        DomainModel data = null;
        try {
            data = inputFileReader.readFile("../../lums-sum17.xml");
            //data = inputFileReader.readFile("../../pu-d9-fal19.xml");

            System.out.println(data);
            System.out.println();
        } catch (ParsingException e) {
            System.out.println(e.getMessage());
        }

        assert(data != null);

        InitialSolutionGenerator<Timetable, ClassUnit> initialSolutionGen = new MullerSolutionGenerator(data);
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing(data, initialSolutionGen, 1000, 1000, 0.001, 100, 5);
        //Timetable generatedSolution = simulatedAnnealing.execute();

        //dbHibernateManager.storeData(dBData);

        //System.out.println("Saved in the database!");
    }
}
