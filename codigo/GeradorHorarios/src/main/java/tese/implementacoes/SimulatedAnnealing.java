package tese.implementacoes;

import tese.estruturas.Horario;
import tese.interfaces.AlgoritmoHeuristico;

public class SimulatedAnnealing implements AlgoritmoHeuristico<Horario> {
    Horario solucao_inicial;
    int temperatura_inicial;
    double taxa_arrefecimento;

    public SimulatedAnnealing(Horario solucao_inicial, int temperatura_inicial, double taxa_arrefecimento){
        this.solucao_inicial = solucao_inicial;
        this.temperatura_inicial = temperatura_inicial;
        this.taxa_arrefecimento = taxa_arrefecimento;

    }

    public Horario executar(){
        return null;
    }

    private int funcao_custo(){
        return 0;
    }

    private Horario funcao_vizinhanca(){
        return null;
    }
}
