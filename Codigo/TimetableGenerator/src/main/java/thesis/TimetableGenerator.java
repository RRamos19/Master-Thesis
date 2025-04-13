package thesis;

import thesis.implementations.*;
import thesis.interfaces.*;
import thesis.structures.TimetablingData;

public class TimetableGenerator {
    public static void main(String[] args) {
        InputFileReader<TimetablingData> inputFileReader = new ITCFormatParser();
        //GraphicalInterface graphicalManager = new InterfaceGraficaSwing("Ferramenta de Geração de Horários");
        //IGJavaFX testeIG = new IGJavaFX();
        //testeIG.iniciarInterface("Ferramenta de Geração de Horários");
        HeuristicAlgorithm heuristicAlgorithm = new SimulatedAnnealing(null, 100, 0.99);
        //DBManager dbManager = new DBPostgreSQLManager("timetabling_db");

        TimetablingData timetablingData = inputFileReader.readFile("../lums-sum17.xml");
        System.out.println(timetablingData);

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
