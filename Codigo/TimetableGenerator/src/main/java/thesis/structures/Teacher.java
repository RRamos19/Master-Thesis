package thesis.structures;

import javafx.util.Pair;

import java.util.List;
import java.util.ArrayList;

public class Teacher {
    private final int teacherId;
    private final String teacherName;
    private final List<Time> unavails = new ArrayList<>();
    private final List<Pair<String, String>> classes = new ArrayList<>();

    public Teacher(int teacherId, String teacherName) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    public int getId() {
        return teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void addUnavailability(Time unavail) {
        unavails.add(unavail);
    }

    public void addClass(String classId, String subjectId){
        classes.add(new Pair<>(classId, subjectId));
    }
}
