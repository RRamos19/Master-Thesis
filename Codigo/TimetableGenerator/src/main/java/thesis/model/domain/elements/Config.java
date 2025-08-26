package thesis.model.domain.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Config implements TableDisplayable {
    private final String configId;
    private final List<Subpart> subpartList = new ArrayList<>();

    public Config(String configId) {
        this.configId = configId;
    }

    public void addSubpart(Subpart subpart) {
        subpartList.add(subpart);
    }

    public String getConfigId() {
        return configId;
    }

    public List<Subpart> getSubpartList() {
        return Collections.unmodifiableList(subpartList);
    }

    public List<String> getColumnNames() {
        return List.of("Id", "Nº of Subparts");
    }

    public List<Object> getColumnValues() {
        return List.of(configId, subpartList.size());
    }
}
