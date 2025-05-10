package thesis;

import thesis.model.dbms.DBTimetableRepository;
import thesis.model.dbms.DBPostgreSQLManager;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;
import thesis.model.entities.StructuredTimetableData;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        InputFileReader<StructuredTimetableData> inputFileReader = new ITCFormatParser();

        //GraphicalInterface graphicalManager = new InterfaceGraficaSwing("Ferramenta para Geração de Horários");
        //IGJavaFX testeIG = new IGJavaFX();
        //testeIG.instantiateGUI("Ferramenta para Geração de Horários");

        DBTimetableRepository timetableRepository = new DBTimetableRepository(new DBPostgreSQLManager("timetabling_db"));
        StructuredTimetableData timetableData = new StructuredTimetableData();

        try {
            timetableRepository.connect("localhost", "5432", "postgres", "123");

            timetableData.mergeWithTimetable(timetableRepository.fetchTimetableData());
            System.out.println(timetableData);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println();

        StructuredTimetableData structuredTimetableData = inputFileReader.readFile("../lums-sum17.xml");

        timetableData.mergeWithTimetable(structuredTimetableData);
        System.out.println(timetableData);
        System.out.println();

        StructuredTimetableData solutionTimetableData = inputFileReader.readFile("../solution-agh-fis-spr17.xml");

        timetableData.mergeWithTimetable(solutionTimetableData);
        System.out.println(timetableData);

        try {
            timetableRepository.storeTimetableData(timetableData);

            if(timetableRepository.isConnected()) {
                timetableRepository.disconnect();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
