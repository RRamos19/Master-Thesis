package thesis.view;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TableViewFactory {
    private TableViewFactory() {}

    public static <T> TableView<T> createGenericTableView(ObservableList<T> data, List<Pair<String, String>> columns) {
        TableView<T> tableView = new TableView<>();

        for (Pair<String, String> column : columns) {
            TableColumn<T, ?> tableColumn = new TableColumn<>(column.getKey());
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(column.getValue()));
            tableView.getColumns().add(tableColumn);
        }

        tableView.setItems(data);
        return tableView;
    }
}
