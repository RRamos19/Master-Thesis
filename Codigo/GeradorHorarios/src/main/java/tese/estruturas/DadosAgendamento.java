package tese.estruturas;

import java.util.ArrayList;

public class DadosAgendamento {
    private ArrayList<Disciplina> disciplinas = new ArrayList<>();
    private ArrayList<Professor> professores = new ArrayList<>();
    private ArrayList<Restricao> restricoes = new ArrayList<>();
    private ArrayList<Horario> horarios = new ArrayList<>();

    // Dados de otimização
    int pesoTempo = 0, pesoSala = 0, pesoDistribuicao = 0;

    public DadosAgendamento() {}

    public void armazenarOtimizacao(int pesoTempo, int pesoSala, int pesoDistribuicao) {
        this.pesoTempo = pesoTempo;
        this.pesoSala = pesoSala;
        this.pesoDistribuicao = pesoDistribuicao;
    }
}
