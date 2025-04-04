package thesis.interfaces;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public interface DBManager {
    void connect(String ip, String port, String user, String password) throws SQLException;
    void disconnect() throws SQLException;
    void create(String tableName, Map<String, Object> dados) throws SQLException;
    Map<String, ArrayList<Object>> read(String tableName, String condition) throws SQLException;
    void update(String tableName, Map<String, Object> data, String condition) throws SQLException;
    void remove(String tableName, String condition) throws SQLException;
}
