package thesis.model.mapper;

import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.domain.components.constraints.ConstraintFactory;
import thesis.model.persistence.EntityRepository;
import thesis.model.persistence.entities.*;

public class ModelConverter {
    public static InMemoryRepository convertToDataRepository(EntityRepository entityRepository) throws Exception {
        InMemoryRepository data = new DataRepository(entityRepository.getProgramName());

        ProgramEntity programEntity = entityRepository.getProgramEntity();

        data.setConfiguration(
                programEntity.getNumberDays(),
                programEntity.getNumberWeeks(),
                programEntity.getSlotsPerDay());

        data.setOptimizationParameters(
                programEntity.getTimeWeight(),
                programEntity.getRoomWeight(),
                programEntity.getDistributionWeight());

        // Add the rooms and the relations of distance and unavailabilities
        for(RoomEntity roomEntity : entityRepository.getRooms()) {
            Room room = new Room(roomEntity.getName());
            data.addRoom(room);
            for(RoomDistanceEntity roomDistanceEntity : roomEntity.getRoom1DistanceList()) {
                room.addRoomDistance(roomDistanceEntity.getRoom2().getName(), roomDistanceEntity.getDistance());
            }
            for(RoomUnavailabilityEntity roomUnavailabilityEntity : roomEntity.getRoomUnavailabilityList()) {
                room.addUnavailability(
                        roomUnavailabilityEntity.getDays(),
                        roomUnavailabilityEntity.getWeeks(),
                        roomUnavailabilityEntity.getStartSlot(),
                        roomUnavailabilityEntity.getDuration());
            }
        }

        // Add the courses, configs, subparts, classes and respective teachers
        for(CourseEntity courseEntity : entityRepository.getCourses()) {
            Course course = new Course(courseEntity.getName());
            data.addCourse(course);
            for(ConfigEntity configEntity : courseEntity.getConfigList()) {
                Config config = new Config(configEntity.getName());
                course.addConfig(config);
                for(SubpartEntity subpartEntity : configEntity.getSubpartList()) {
                    Subpart subpart = new Subpart(subpartEntity.getName());
                    config.addSubpart(subpart);
                    for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnits()) {
                        ClassUnit classUnit = new ClassUnit(classUnitEntity.getName());
                        subpart.addClassUnit(classUnit);
                        data.addClassUnit(classUnit);
                        for(TeacherEntity teacherEntity : classUnitEntity.getTeacherEntityClassList()) {
                            classUnit.addTeacher(teacherEntity.getId());
                        }
                    }
                }
            }
        }

        // Add the teachers and theirs unavailabilities
        for(TeacherEntity teacherEntity : entityRepository.getTeachers()) {
            Teacher teacher = new Teacher(teacherEntity.getId(), teacherEntity.getName());
            data.addTeacher(teacher);
            for(TeacherUnavailabilityEntity teacherUnavailabilityEntity : teacherEntity.getTeacherUnavailabilityEntityList()) {
                teacher.addUnavailability(
                        teacherUnavailabilityEntity.getDays(),
                        teacherUnavailabilityEntity.getWeeks(),
                        teacherUnavailabilityEntity.getStartSlot(),
                        teacherUnavailabilityEntity.getDuration());
            }
        }

        // Add the timetables, scheduled lessons and respective teachers
        for(TimetableEntity timetableEntity : entityRepository.getTimetables()) {
            Timetable timetable = new Timetable(data.getProgramName());
            data.addTimetable(timetable);
            for(ScheduledLessonEntity scheduledLessonEntity : timetableEntity.getScheduledLessonEntityList()) {
                ScheduledLesson scheduledLesson = new ScheduledLesson(
                        scheduledLessonEntity.getClassUnit().getName(),
                        scheduledLessonEntity.getRoom().getName(),
                        scheduledLessonEntity.getDays(),
                        scheduledLessonEntity.getWeeks(),
                        scheduledLessonEntity.getStartSlot(),
                        scheduledLessonEntity.getDuration());
                //scheduledLesson.bindModel(data);
                timetable.addScheduledLesson(scheduledLesson);
                for(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity : scheduledLessonEntity.getScheduledLessonTeacherList()) {
                    scheduledLesson.addTeacherId(scheduledLessonTeacherEntity.getTeacher().getId());
                }
            }
        }

        for(ConstraintTypeEntity constraintTypeEntity : entityRepository.getConstraintTypes()) {
            for(ConstraintEntity constraintEntity : constraintTypeEntity.getConstraintEntityList()) {
                Constraint constraint = ConstraintFactory.createConstraint(constraintEntity.getConstraintTypeEntity().getName(), constraintEntity.getPenalty(), constraintEntity.getRequired(), data.getTimetableConfiguration());
                data.addConstraint(constraint);

                for (ClassConstraintEntity classRestriction : constraintEntity.getClassRestrictionEntityList()) {

                    for (ClassUnitEntity classUnitEntity : entityRepository.getClassUnits()) {
                        // TODO: complete
                    }
                }
            }
        }

        return data;
    }
}
