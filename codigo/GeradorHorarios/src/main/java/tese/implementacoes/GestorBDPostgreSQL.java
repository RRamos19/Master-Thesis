package tese.implementacoes;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tese.interfaces.GestorBaseDados;

public class GestorBDPostgreSQL implements GestorBaseDados {
    private String url;
    private String user;
    private String password;
    private String nome_db_padrao = "agendamento_bd";
    private String jdbcPostgres = "jdbc:postgresql:";
    private Connection conexao;

    public void conectar(String url, String user, String password) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;

        String finalUrl = jdbcPostgres + "//" + url + "/" + nome_db_padrao;

        conexao = DriverManager.getConnection(finalUrl, user, password);
    }

    public void desconectar() throws SQLException{
        conexao.close();
    }

    public void criar(String nomeTabela, Map<String, Object> dados) throws SQLException{

    }

    public Map<String, ArrayList<Object>> lerTabela(String nomeTabela, String condicao) throws SQLException{
        // TODO: Falta verificar o nomeTabela e condicao para evitar SQLInjection

        String sqlQuery = "SELECT * FROM " + nomeTabela;
        if(!condicao.isEmpty()){
            sqlQuery += " WHERE " + condicao;
        }

        PreparedStatement stmt = conexao.prepareStatement(sqlQuery);
        ResultSet rs = stmt.executeQuery();

        Map<String, ArrayList<Object>> mapa = conversaoResultSetParaMap(rs);

        rs.close();
        stmt.close();

        return mapa;
    }

    public void atualizar(String nomeTabela, Map<String, Object> dados, String condicao) throws SQLException{

    }

    public void remover(String nomeTabela, String condicao) throws SQLException{

    }

    private Map<String, ArrayList<Object>> conversaoResultSetParaMap(ResultSet rs) throws SQLException{
        // collect column names
        ArrayList<String> columnNames = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(rsmd.getColumnLabel(i+1));
        }

        HashMap<String, ArrayList<Object>> mapa = new HashMap<>();

        while (rs.next()) {
            for (int i = 0; i < columnCount; i++) {
                String columnName = columnNames.get(i);

                ArrayList<Object> list = mapa.get(columnName);
                if(list == null){
                    list = new ArrayList<>();
                }

                list.add(rs.getObject(i+1));

                mapa.put(columnName, list);
            }
        }

        return mapa;
    }
}
