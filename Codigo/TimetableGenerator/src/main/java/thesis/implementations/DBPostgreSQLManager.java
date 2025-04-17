package thesis.implementations;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
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

    /**
     * Disconnects the connection to the database
     * @throws SQLException
     */
    public void disconnect() throws SQLException{
        connection.close();
    }

    public void create(String tableName, Map<String, Object> data) throws SQLException{
        
    }

    /**
     * Reads every instance and every column of the provided table name
     * @param tableName Name of the table present in the database
     * @return A map that contains the instances present on the table present in the database
     * @throws SQLException
     */
    public Map<String, ArrayList<Object>> read(String tableName) throws SQLException{
        return read(tableName, null);
    }

    /**
     * Checks if the table name only contains letters to avoid SQL Injections
     * @param tableName Name of the table present in the database
     * @return True if tableName only contains letters, False otherwise
     */
    public boolean isTableNameValid(String tableName) {
        return tableName.matches("^[a-zA-Z]+$");
    }

    /**
     * Reads every instance and every column of the provided table name considering the conditions present
     * @param tableName Name of the table present in the database
     * @param conditions List of conditions to apply on the query
     * @return A map that contains the instances present on the table present in the database
     * @throws SQLException
     */
    public Map<String, ArrayList<Object>> read(String tableName, List<String> conditions) throws SQLException{
        // By using preparedStatement and inserting the parameters where the character ? is
        // the possibility of SQL Injection is avoided

        if(!isTableNameValid(tableName)){
            throw new RuntimeException("The table name provided can only contain letters");
        }

        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ").append(tableName);

//        if(conditions != null){
//            // The inclusion of WHERE 1=1 eases the logic of inclusion of the rest of the conditions
//            sqlQuery.append("WHERE 1=1");
//
//            // Adds the conditions dinamically
//            for (String entry : conditions) {
//                sqlQuery.append(" ").append(entry);
//            }
//        }

        PreparedStatement stmt = connection.prepareStatement(sqlQuery.toString());

//        if(conditions != null) {
//            // Defines the conditions dinamically
//            int paramIndex = 1;
//            for (Map.Entry<String, String> entry : conditions.entrySet()) {
//                stmt.setString(paramIndex++, entry.getValue());
//            }
//        }

        ResultSet rs = stmt.executeQuery();

        Map<String, ArrayList<Object>> results = convertResultSetToMap(rs);

        rs.close();
        stmt.close();

        return results;
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
