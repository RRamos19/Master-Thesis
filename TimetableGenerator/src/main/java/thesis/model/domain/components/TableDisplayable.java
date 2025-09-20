package thesis.model.domain.components;

import java.util.List;

public interface TableDisplayable {
    String getTableName();
    List<String> getColumnNames();
    List<Object> getColumnValues();
    boolean isOptimizable();
    boolean isRemovable();
}
