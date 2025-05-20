package thesis.model.dbms;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DBPostgreSQLManager implements DBManager {
    private final int DEFAULT_TIMEOUT = 200;

    private String ip;
    private String port;
    private String user;
    private String password;
    private String db_name;
    private String jdbcPostgres = "jdbc:postgresql:";
    protected Connection connection;
    private DatabaseMetaData meta;

    public DBPostgreSQLManager(String db_name){
        this.db_name = db_name;
    }

    @Override
    public Connection connect(String ip, String port, String user, String password) throws SQLException {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;

        String finalUrl = jdbcPostgres + "//" + ip + ":" + port + "/" + db_name;

        connection = DriverManager.getConnection(finalUrl, user, password);

        meta = connection.getMetaData();

        return connection;
    }

    @Override
    public void disconnect() throws SQLException {
        connection.close();
        connection = null;
    }

    @Override
    public boolean isConnected() throws SQLException {
        if(connection == null) {
            return false;
        }

        return connection.isValid(DEFAULT_TIMEOUT);
    }

    @Override
    public void insert(String tableName, Map<String, Collection<?>> data, boolean updateConflicts) throws SQLException, IllegalArgumentException {
        // Verify if the data map is consistent
        if(!verifyDataLists(data)) {
            throw new IllegalArgumentException("Insertion in table " + tableName + " failed. Data map has an inconsistent number of rows");
        }

        int nDataRows = data.values().iterator().next().size();
        // If there is no data to insert the method ends
        if(nDataRows == 0) {
            return;
        }

        // Get the primary keys of the table
        ResultSet rs = meta.getPrimaryKeys(null, null, tableName);
        List<String> primaryKeyColumns = new ArrayList<>();
        while (rs.next()) {
            String pkName = rs.getString("COLUMN_NAME");
            if(pkName != null) {
                primaryKeyColumns.add(pkName);
            }
        }

        // Get the values needed to build the SQL query and build the query with placeholders
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        List<Object> values = new ArrayList<>();

        for (String columnName : data.keySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
            }
            columns.append(columnName);
        }

        // Construction of the query using placeholders
        // The placeholders will later be replaced by the prepared statement
        List<List<?>> collectionData = data.values().stream()
                .map(ArrayList::new)  // Garante acesso por índice
                .collect(Collectors.toList());

        // The value i corresponds to the number of the row
        // The value j corresponds to the number of the column
        for (int i = 0; i < nDataRows; i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("(");

            for (int j = 0; j < collectionData.size(); j++) {
                if (j > 0) {
                    placeholders.append(", ");
                }
                values.add(collectionData.get(j).get(i));
                placeholders.append("?");
            }

            placeholders.append(")");
        }

        StringBuilder stringToFormat = new StringBuilder("INSERT INTO %s (%s) VALUES %s");

        if(!primaryKeyColumns.isEmpty()) {
            stringToFormat.append(" ON CONFLICT (");

            // Add the primary key/s to the causes of the conflict
            for (int i = 0; i < primaryKeyColumns.size(); i++) {
                stringToFormat.append(primaryKeyColumns.get(i));
                if (i + 1 < primaryKeyColumns.size()) {
                    stringToFormat.append(" ,");
                }
            }
            stringToFormat.append(")");

            // Append the action to perform when there is a conflict
            if (updateConflicts && primaryKeyColumns.size() > 1) {
                stringToFormat.append(" DO UPDATE SET ");
                int colNumber = 0;
                for (String colName : data.keySet()) {
                    if (colNumber != 0) {
                        stringToFormat.append(", ");
                    }
                    stringToFormat.append(colName).append(" = EXCLUDED.").append(colName);
                    colNumber++;
                }
            } else {
                stringToFormat.append(" DO NOTHING");
            }
        }

        String sql = String.format(stringToFormat.toString(), tableName, columns, placeholders);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            //TODO: Apenas para debug. Remover quando não for mais necessário
            //System.out.println(stmt.toString());
            stmt.executeUpdate();
        }
    }

    @Override
    public Map<String, List<Object>> read(String tableName) throws SQLException {
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
    public void remove(String tableName, String condition) throws SQLException {
        // The table name must be checked to avoid SQL Injections
        isTableNameValid(tableName);
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
    private Map<String, List<Object>> convertResultSetToMap(ResultSet rs) throws SQLException {
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

    /**
     * Checks if the lists present in the data map are all of the same size
     * @param data
     * @return
     */
    private boolean verifyDataLists(Map<String, Collection<?>> data) {
        if (data.isEmpty()) return false;

        int expectedSize = -1;

        for (Collection<?> list : data.values()) {
            if (expectedSize == -1) {
                expectedSize = list.size();
            } else if (list.size() != expectedSize) {
                return false;
            }
        }

        return true;
    }
}
