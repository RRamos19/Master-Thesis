package thesis.model.dbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DBManager {

    /**
     * Establishes a connection to a database
     * @param ip IP of the instance of the database
     * @param port Port of the instance of the database
     * @param user Username used to establish the connection
     * @param password Password used to establish the connection
     * @throws SQLException
     */
    Connection connect(String ip, String port, String user, String password) throws SQLException;

    /**
     * Disconnects the connection to the database
     * @throws SQLException
     */
    void disconnect() throws SQLException;

    /**
     * Inserts the data provided into the table specified
     * @param tableName Name of the table present in the database
     * @param data data that is to be inserted into the table
     * @throws SQLException
     */
    void insert(String tableName, Map<String, List<?>> data, boolean updateConflicts) throws SQLException;

    /**
     * Reads every instance and every column of the provided table name
     * @param tableName Name of the table present in the database
     * @return A map that contains the instances present on the table present in the database
     * @throws SQLException
     */
    Map<String, List<Object>> read(String tableName) throws SQLException;

    void remove(String tableName, String condition) throws SQLException;
}
