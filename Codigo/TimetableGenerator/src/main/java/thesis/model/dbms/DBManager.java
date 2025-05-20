package thesis.model.dbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
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
     * Disconnects the connection to the database. Doesn't do anything about ongoing transactions
     * @throws SQLException if a database access error occurs
     */
    void disconnect() throws SQLException;

    /**
     * Checks if the system is still connected to the database
     * @return True if the system is still connected, false otherwise
     * @throws SQLException Should never happen. The only way the exception is thrown is if the timeout is negative, which it never is
     */
    boolean isConnected() throws SQLException;

    /**
     * Inserts the data provided into the table specified. The insertion is not done in a transaction
     * @param tableName Name of the table present in the database
     * @param data data that is to be inserted into the table. The map should have the name of the column as key and list of objects as values
     * @throws SQLException
     */
    void insert(String tableName, Map<String, Collection<?>> data, boolean updateConflicts) throws SQLException;

    /**
     * Reads every instance and every column of the provided table name
     * @param tableName Name of the table present in the database
     * @return A map that contains the instances present on the table present in the database
     * @throws SQLException
     */
    Map<String, List<Object>> read(String tableName) throws SQLException;

    void remove(String tableName, String condition) throws SQLException;
}
