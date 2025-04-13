package thesis.structures;

public class Timetable {
    private String timetableId;

    public Timetable(String timetableId){
        this.timetableId = timetableId;
    }

    public String getId(){
        return timetableId;
    }
}
