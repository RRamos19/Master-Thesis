package thesis;

import thesis.implementations.*;
import thesis.interfaces.*;
import thesis.structures.StructuredTimetablingData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class TimetableGenerator {
    public static void main(String[] args) {
        InputFileReader<StructuredTimetablingData> inputFileReader = new ITCFormatParser();
        //GraphicalInterface graphicalManager = new InterfaceGraficaSwing("Ferramenta para Geração de Horários");
        //IGJavaFX testeIG = new IGJavaFX();
        //testeIG.instantiateGUI("Ferramenta para Geração de Horários");
        DBManager dbManager = new DBPostgreSQLManager("timetabling_db");

        StructuredTimetablingData structuredTimetablingData = inputFileReader.readFile("../lums-sum17.xml");
        System.out.println(structuredTimetablingData);

        try {
            dbManager.connect("localhost", "5432", "postgres", "123");
            HashMap<String, ArrayList<Object>> teste = (HashMap<String, ArrayList<Object>>) dbManager.read("room");

            System.out.println(teste);
            dbManager.disconnect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

//        Map<String, ArrayList<Object>> mapa = null;

//        try {
//            gestorBD.conectar("localhost", "5432", "postgres", "123");
//            System.out.println("Conectado");
//
//            mapa = gestorBD.lerTabela("professor", "");
//            System.out.println("Leitura");
//
//            gestorBD.desconectar();
//            System.out.println("Desconexão");
//        } catch (SQLException e) {
//            System.out.println("Erro a ler os dados SQL");
//            e.printStackTrace();
//        }
//
//        if(mapa != null){
//            System.out.println(mapa.entrySet());
//        }
    }
}
