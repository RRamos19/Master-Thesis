package thesis;

import thesis.abstractions.DBTimetableRepository;
import thesis.implementations.*;
import thesis.interfaces.*;
import thesis.structures.StructuredTimetableData;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        InputFileReader<StructuredTimetableData> inputFileReader = new ITCFormatParser();

        //GraphicalInterface graphicalManager = new InterfaceGraficaSwing("Ferramenta para Geração de Horários");
        //IGJavaFX testeIG = new IGJavaFX();
        //testeIG.instantiateGUI("Ferramenta para Geração de Horários");

        DBManager dbManager = new DBPostgreSQLManager("timetabling_db");
        DBTimetableRepository timetableRepository = null;
        StructuredTimetableData timetableData = null;
        try {
            timetableRepository = new DBTimetableRepository(dbManager);

            timetableRepository.connect("localhost", "5432", "postgres", "123");

            timetableData = timetableRepository.fetchTimetableData();
            System.out.println(timetableData);
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e);
            timetableRepository = null;
        }

        StructuredTimetableData structuredTimetableData = inputFileReader.readFile("../lums-sum17.xml");
        System.out.println(structuredTimetableData);
        System.out.println();

        StructuredTimetableData solutionTimetableData = inputFileReader.readFile("../solution-agh-fis-spr17.xml");

        if(timetableData != null) {
            timetableData.mergeWithTimetable(structuredTimetableData);
            System.out.println(timetableData);
            System.out.println();
        } else {
            timetableData = structuredTimetableData;
        }

        timetableData.mergeWithTimetable(solutionTimetableData);
        System.out.println(timetableData);

        try {
            if(timetableRepository != null) {
                timetableRepository.disconnect();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
