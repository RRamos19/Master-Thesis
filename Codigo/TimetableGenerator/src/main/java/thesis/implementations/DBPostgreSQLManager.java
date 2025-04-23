package thesis.implementations;

import thesis.interfaces.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBPostgreSQLManager implements DBManager {
    private String ip;
    private String port;
    private String user;
    private String password;
    private String db_name;
    private String jdbcPostgres = "jdbc:postgresql:";
    private Connection connection;
    private DatabaseMetaData meta;

    public DBPostgreSQLManager(String db_name){
        this.db_name = db_name;
    }

    @Override
    public void connect(String ip, String port, String user, String password) throws SQLException {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;

        String finalUrl = jdbcPostgres + "//" + ip + ":" + port + "/" + db_name;

        connection = DriverManager.getConnection(finalUrl, user, password);

        meta = connection.getMetaData();
    }

    @Override
    public void disconnect() throws SQLException{
        connection.close();
    }

    @Override
    public void insert(String tableName, List<Object> data) throws SQLException{
        // The table name must be checked to avoid SQL Injections
        isTableNameValid(tableName);

        StringBuilder sqlQuery = new StringBuilder("INSERT INTO ").append(tableName).append(" (");

        ResultSet rsColumns = meta.getColumns(null, "public", tableName, null);

        int numberColumns = 0;
        while (rsColumns.next()) {
            String columnName = rsColumns.getString("COLUMN_NAME");
            sqlQuery.append(columnName);
            if (!rsColumns.isLast()) {
                sqlQuery.append(", ");
            }
            numberColumns++;
        }
        sqlQuery.append(") VALUES (");

        for(int i=0; i<numberColumns; i++){
            sqlQuery.append("?");
            if(i + 1 < numberColumns){
                sqlQuery.append(", ");
            }
        }
        sqlQuery.append(")");

        System.out.println(sqlQuery);

        PreparedStatement stmt = connection.prepareStatement(sqlQuery.toString());

        int objectIndex = 1;
        for (Object o : data) {
            stmt.setObject(objectIndex, o);
            objectIndex++;
        }

        System.out.println(stmt);

        int result = stmt.executeUpdate();
    }

    @Override
    public Map<String, List<Object>> read(String tableName) throws SQLException{
        // The table name must be checked to avoid SQL Injections
        isTableNameValid(tableName);

        String sqlQuery = "SELECT * FROM " + tableName;

        PreparedStatement stmt = connection.prepareStatement(sqlQuery);

        ResultSet rs = stmt.executeQuery();

        Map<String, List<Object>> results = convertResultSetToMap(rs);

        stmt.close();

        return results;
    }

    @Override
    public void update(String tableName, Map<String, Object> data, String condition) throws SQLException{

    }

    @Override
    public void remove(String tableName, String condition) throws SQLException{

    }

    /**
     * Checks if the table name only contains letters to avoid SQL Injections
     * @param tableName Name of the table present in the database
     *
     */
    private void isTableNameValid(String tableName) {
        if(!tableName.matches("^[a-zA-Z0-9_]+$")){
            throw new IllegalArgumentException("The table name provided should only contain either letters, numbers or underscores");
        }
    }

    /**
     * Converts the result of a query into a Map where the keys are the names of the columns and the values are lists of all the tuples
     * @param rs Result of the SQL query
     * @return Map where the keys are the names of the columns and the values are lists of all the tuples
     * @throws SQLException
     */
    private Map<String, List<Object>> convertResultSetToMap(ResultSet rs) throws SQLException{
        // collect column names
        List<String> columnNames = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(rsmd.getColumnLabel(i+1));
        }

        // Prepare the Map. For each possible key an ArrayList is created
        Map<String, List<Object>> resultMap = new HashMap<>();
        for(String columnName : columnNames) {
            resultMap.computeIfAbsent(columnName, k -> new ArrayList<>());
        }

        // For each line the values present in the columns are placed in a Map with the column name as the key
        while (rs.next()) {
            for (int i = 0; i < columnCount; i++) {
                String columnName = columnNames.get(i);

                List<Object> resultList = resultMap.get(columnName);

                resultList.add(rs.getObject(i+1));
            }
        }

        return resultMap;
    }
}
