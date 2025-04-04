package thesis.structures;

import java.util.ArrayList;

public class TimetablingData {
    private ArrayList<Class> classes = new ArrayList<>();
    private ArrayList<Teacher> teachers = new ArrayList<>();
    private ArrayList<Restriction> restrictions = new ArrayList<>();
    private ArrayList<Timetable> timetables = new ArrayList<>();

    // Optimization data
    int timeWeight = 0, roomWeight = 0, distributionWeight = 0;

    public TimetablingData() {}

    public void storeOptimization(int timeWeight, int roomWeight, int distributionWeight) {
        this.timeWeight = timeWeight;
        this.roomWeight = roomWeight;
        this.distributionWeight = distributionWeight;
    }
}
