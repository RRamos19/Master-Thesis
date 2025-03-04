package tese;

import java.sql.Driver;
import tese.implementacoes.*;
import tese.interfaces.*;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

public class GeradorHorarios {
    public static void main(String[] args) {
        GestorFicheiros gestorFicheiros = new InterpretadorITC();
        GestorInterfaceGrafica gestorIG = new InterfaceGraficaSwing("Ferramenta de Geração de Horários");
        AlgoritmoHeuristico algoritmoH = new SimulatedAnnealing(null, 100, 0.99);
        GestorBaseDados gestorBD = new GestorBDPostgreSQL();

        Map<String, ArrayList<Object>> mapa = null;

        try {
            gestorBD.conectar("127.0.0.1:5432", "postgres", "123");
            System.out.println("Conectado");

            mapa = gestorBD.lerTabela("professor", "");
            System.out.println("Leitura");

            gestorBD.desconectar();
            System.out.println("Desconexão");
        } catch (SQLException e) {
            System.out.println("Erro a ler os dados SQL");
            e.printStackTrace();
        }

        if(mapa != null){
            System.out.println(mapa.entrySet());
        }
    }
}
