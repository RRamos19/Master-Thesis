package thesis.service;

import thesis.model.domain.*;
import thesis.model.domain.restrictions.Restriction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The solution generator presented is based on Tomáš Müller's and Roman Barták's paper.
 * Source - Interactive Timetabling https://www.unitime.org/papers/it02_ctp.pdf
 */
public class MullerSolutionGenerator implements InitialSolutionGenerator<Timetable, ClassUnit> {
    private boolean interruptAlgorithm = false;
    private final Map<String, Integer> classRemovals = new HashMap<>();     // ClassId : nrRemovals from timetable
    private final DomainModel data;
    private final int slotsPerDay;
    private final int numDays;

    public MullerSolutionGenerator(DomainModel data) {
        TimetableConfiguration ttConf = data.getTimetableConfiguration();
        this.slotsPerDay = ttConf.getSlotsPerDay();
        this.numDays = ttConf.getNumDays();
        this.data = data;
    }

    public Timetable generate(List<ClassUnit> unscheduled, int maxIterations) {
        Timetable schedule = new Timetable();

        int iter = 0;
        while(!unscheduled.isEmpty() && iter < maxIterations && !interruptAlgorithm) {
            iter++;
            ClassUnit activity = activitySelection(schedule, unscheduled);
            unscheduled.remove(activity);
            ScheduledLesson location = locationSelection(schedule, activity);
            // TODO: por fazer
            // schedule.addScheduledLesson(location);
        }

        return schedule;
    }

    private ClassUnit activitySelection(Timetable schedule, List<ClassUnit> unscheduled) {
        int weightRemovals = 1, weightDepend = 1, weightPlaces = 1, weightPlacesNoConflict = 1;
        ClassUnit bestCandidate = null;
        Integer valueBestCandidate = null;

        for(ClassUnit cls : unscheduled) {
            int clsValue = -classRemovals.getOrDefault(cls.getClassId(), 0)*weightRemovals +
                    -weightDepend*getNumberOfDependencies(cls)+
                    weightPlaces*getNumberOfPlaces(schedule, cls)+
                    weightPlacesNoConflict*getNumberOfPlacesNoConflict(schedule, cls);

            if(valueBestCandidate == null || clsValue < valueBestCandidate) {
                valueBestCandidate = clsValue;
                bestCandidate = cls;
            }
        }

        return bestCandidate;
    }


    /**
     * Count the number of dependencies of a class unit. A dependency consists of a relation between lectures. Said relation can be in time or in space
     * @param cls Class unit of which dependencies are to be counted
     * @return The count of dependencies of the class unit provided
     */
    private int getNumberOfDependencies(ClassUnit cls) {
        int count = 0;
        for(Restriction r : cls.getRestrictionList()) {
            switch (r.getType()) {
                case "SameStart":
                case "SameTime":
                case "DifferentTime":
                case "SameDays":
                case "DifferentDays":
                case "SameWeeks":
                case "DifferentWeeks":
                case "Overlap":
                case "NotOverlap":
                case "SameRoom":
                case "DifferentRoom":
                case "SameAttendees":
                case "Precedence":
                    count++;
                    break;
            }
        }

        return count;
    }


    private int getNumberOfPlaces(Timetable schedule, ClassUnit cls) {
        int numberOfPlaces = slotsPerDay*numDays-1; //TODO: confirmar se está certo
        for(ScheduledLesson scheduledLesson : schedule.getScheduledLessonList()) {
            ClassUnit scheduledClass = data.getClassUnit(scheduledLesson.getClassId());

            for(Restriction r : scheduledClass.getRestrictionList()) {
                r.evaluate(schedule);
                //TODO: verificar cada restrição e remover do numberOfPlaces caso seja required

            }
        }

        return numberOfPlaces;
    }


    private int getNumberOfPlacesNoConflict(Timetable schedule, ClassUnit cls) {
        int numberOfPlacesNoConflict = slotsPerDay*numDays-1; //TODO: confirmar se está certo
        for(ScheduledLesson scheduledLesson : schedule.getScheduledLessonList()) {
            numberOfPlacesNoConflict -= scheduledLesson.getLength();
            //TODO: confirmar se basta isto
        }

        return numberOfPlacesNoConflict;
    }

    private ScheduledLesson locationSelection(Timetable schedule, ClassUnit activity) {
        // Timetable.AssignedClass assignedClass = new Timetable.AssignedClass();
        //TODO: por implementar
        return null;
    }

    private void stopAlgorithm() {
        interruptAlgorithm = true;
    }
}
