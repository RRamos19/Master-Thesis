package thesis.model.domain.components;

import thesis.model.exceptions.CheckedIllegalArgumentException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Teacher {
    private final int id;
    private final String name;

    private final List<Time> teacherUnavailabilities = new ArrayList<>();
    private final List<String> teacherClassList = new ArrayList<>();

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
        return Collections.unmodifiableList(teacherUnavailabilities);
    }

    public List<String> getTeacherClassList() {
        return Collections.unmodifiableList(teacherClassList);
    }

    public void addUnavailability(String days, String weeks, short startSlot, short length) throws CheckedIllegalArgumentException {
        teacherUnavailabilities.add(TimeFactory.create(days, weeks, startSlot, length));
    }

    public void addUnavailability(short days, int weeks, short startSlot, short length) throws CheckedIllegalArgumentException {
        teacherUnavailabilities.add(TimeFactory.create(days, weeks, startSlot, length));
    }

    public void addClassUnit(String classId) {
        teacherClassList.add(classId);
    }
}
