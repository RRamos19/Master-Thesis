package thesis.structures;

import java.util.ArrayList;

public class Distribution {
    private String type;
    private Boolean required;
    private ArrayList<Integer> involvedClasses = new ArrayList<>();

    public Distribution(String type, Boolean required){
        this.type = type;
        this.required = required;
    }

    public void addClassId(int classId){
        involvedClasses.add(classId);
    }

    public String getType() {
        return type;
    }

    public Boolean getRequired() {
        return required;
    }
}
