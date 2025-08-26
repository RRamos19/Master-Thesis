package thesis.model.domain.elements;

import java.util.List;

public interface TableDisplayable {
    List<String> getColumnNames();
    List<Object> getColumnValues();
}
