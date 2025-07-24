package thesis.model.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Config {
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
}
