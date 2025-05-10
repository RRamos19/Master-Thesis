package thesis.model.dbms.mapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMapper {
    private final int columnCount;

    private final List<String> columnNames = new ArrayList<>();
    private final List<Object> data = new ArrayList<>();

    public ResultSetMapper(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        columnCount = rsmd.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(rsmd.getColumnLabel(i+1));
        }

        // Store everything in a single list
        // The retrieval of the data is computed in get() using the row number and the columnCount
        while (resultSet.next()) {
            for (int i = 0; i < columnCount; i++) {
                data.add(resultSet.getObject(i+1));
            }
        }
    }

    public List<Object> get(int row) {
        List<Object> result = new ArrayList<>();

        // Every value present in a single row is stored in a list
        for(int i=0; i<columnCount; i++) {
            result.add(data.get(row*columnCount+i));
        }

        return result;
    }
}
