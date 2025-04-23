package thesis.structures;

public class Timetable {
    private final String timetableId;

    public Timetable(String timetableId){
        this.timetableId = timetableId;
    }

    public String getId(){
        return timetableId;
    }
}
