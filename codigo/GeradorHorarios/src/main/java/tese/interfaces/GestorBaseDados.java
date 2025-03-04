package tese.interfaces;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public interface GestorBaseDados {
    void conectar(String url, String user, String password) throws SQLException;
    void desconectar() throws SQLException;
    void criar(String nomeTabela, Map<String, Object> dados) throws SQLException;
    Map<String, ArrayList<Object>> lerTabela(String nomeTabela, String condicao) throws SQLException;
    void atualizar(String nomeTabela, Map<String, Object> dados, String condicao) throws SQLException;
    void remover(String nomeTabela, String condicao) throws SQLException;
}
