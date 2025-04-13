package thesis.structures;

import java.util.ArrayList;

public class Teacher {
    private int teacherId;
    private String teacherName;
    private ArrayList<Time> unavails = new ArrayList<>();

    public Teacher(int teacherId, String teacherName){
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    public int getId(){
        return teacherId;
    }

    public void addUnavailability(Time unavail){
        unavails.add(unavail);
    }
}
