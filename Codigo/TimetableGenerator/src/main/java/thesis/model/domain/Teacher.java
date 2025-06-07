package thesis.model.domain;

import java.util.ArrayList;
import java.util.List;

public class Teacher {
    private final int id;
    private final String name;

    private final List<Time> teacherUnavailabilities = new ArrayList<>();

    public Teacher(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Time> getTeacherUnavailabilities() {
        return teacherUnavailabilities;
    }

    public void addUnavailability(String days, String weeks, int startSlot, int length) {
        teacherUnavailabilities.add(new Time(days, weeks, startSlot, length));
    }
}
