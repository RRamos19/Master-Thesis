package tese;

import tese.implementacoes.*;
import tese.interfaces.*;

public class GeradorHorarios {
    public static void main(String[] args) {
        LeitorFicheiros leitorFicheiros = new InterpretadorITC();
        //GestorInterfaceGrafica gestorIG = new InterfaceGraficaSwing("Ferramenta de Geração de Horários");
        AlgoritmoHeuristico algoritmoH = new SimulatedAnnealing(null, 100, 0.99);
        GestorBaseDados gestorBD = new GestorBDPostgreSQL("bd_agendamento");

        //IGJavaFX testeIG = new IGJavaFX();
        //testeIG.iniciarInterface("Ferramenta de Geração de Horários");

        leitorFicheiros.lerFicheiro("C:\\Users\\Ricardo\\Desktop\\lums-sum17.xml");

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
