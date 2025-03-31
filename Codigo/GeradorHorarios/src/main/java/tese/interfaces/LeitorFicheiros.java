package tese.interfaces;

import tese.estruturas.DadosAgendamento;

public interface LeitorFicheiros<T> {
    T lerFicheiro(String caminhoFicheiro);
}
