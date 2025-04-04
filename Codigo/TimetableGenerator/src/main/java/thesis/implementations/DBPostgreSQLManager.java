package thesis.implementations;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import thesis.interfaces.DBManager;

public class DBPostgreSQLManager implements DBManager {
    private String ip;
    private String port;
    private String user;
    private String password;
    private String db_name;
    private String jdbcPostgres = "jdbc:postgresql:";
    private Connection connection;

    public DBPostgreSQLManager(String db_name){
        this.db_name = db_name;
    }

    public void connect(String ip, String port, String user, String password) throws SQLException {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;

        String finalUrl = jdbcPostgres + "//" + ip + ":" + port + "/" + db_name;

        connection = DriverManager.getConnection(finalUrl, user, password);
    }

    public void disconnect() throws SQLException{
        connection.close();
    }

    public void create(String tableName, Map<String, Object> data) throws SQLException{

    }

    public Map<String, ArrayList<Object>> read(String tableName, String condition) throws SQLException{
        // TODO: Falta verificar o nomeTabela e condicao para evitar SQLInjection

        String sqlQuery = "SELECT * FROM " + tableName;
        if(!condition.isEmpty()){
            sqlQuery += " WHERE " + condition;
        }

        PreparedStatement stmt = connection.prepareStatement(sqlQuery);
        ResultSet rs = stmt.executeQuery();

        Map<String, ArrayList<Object>> mapa = convertResultSetToMap(rs);

        rs.close();
        stmt.close();

        return mapa;
    }

    public void update(String tableName, Map<String, Object> data, String condition) throws SQLException{

    }

    public void remove(String tableName, String condition) throws SQLException{

    }

    private Map<String, ArrayList<Object>> convertResultSetToMap(ResultSet rs) throws SQLException{
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
