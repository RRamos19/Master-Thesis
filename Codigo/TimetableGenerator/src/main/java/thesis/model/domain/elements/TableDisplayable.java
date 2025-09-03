package thesis.model.domain.elements;

import java.util.List;

public interface TableDisplayable {
    String getTableName();
    List<String> getColumnNames();
    List<Object> getColumnValues();
}
