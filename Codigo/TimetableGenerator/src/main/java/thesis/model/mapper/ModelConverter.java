package thesis.model.mapper;

import thesis.model.domain.*;
import thesis.model.domain.restrictions.Restriction;
import thesis.model.domain.restrictions.RestrictionFactory;
import thesis.model.persistence.EntityModel;
import thesis.model.persistence.entities.*;

public class ModelConverter {
    public static DomainModel convertToDomain(EntityModel entityModel) throws Exception {
        DomainModel data = new DomainModel();

        // Set the optimization parameters and configurations of the timetable
        ConfigurationEntity configurationEntity = entityModel.getConfiguration();
        OptimizationParametersEntity optimizationParametersEntity = entityModel.getOptimization();

        data.setConfiguration(
                configurationEntity.getNumberDays(),
                configurationEntity.getNumberWeeks(),
                configurationEntity.getSlotsPerDay());

        data.setOptimizationParameters(
                optimizationParametersEntity.getTimeWeight(),
                optimizationParametersEntity.getRoomWeight(),
                optimizationParametersEntity.getDistributionWeight());

        // Add the rooms and the relations of distance and unavailabilities
        for(RoomEntity roomEntity : entityModel.getRooms()) {
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
        for(CourseEntity courseEntity : entityModel.getCourses()) {
            Course course = new Course(courseEntity.getName());
            data.addCourse(course);
            for(ConfigEntity configEntity : courseEntity.getConfigList()) {
                Config config = new Config(configEntity.getName());
                course.addConfig(config);
                data.addConfig(config);
                for(SubpartEntity subpartEntity : configEntity.getSubpartList()) {
                    Subpart subpart = new Subpart(subpartEntity.getName());
                    config.addSubpart(subpart);
                    data.addSubpart(subpart);
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
        for(TeacherEntity teacherEntity : entityModel.getTeachers()) {
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
        for(TimetableEntity timetableEntity : entityModel.getTimetables()) {
            Timetable timetable = new Timetable(timetableEntity.getProgram());
            data.addTimetable(timetable);
            for(ScheduledLessonEntity scheduledLessonEntity : timetableEntity.getScheduledLessonEntityList()) {
                ScheduledLesson scheduledLesson = new ScheduledLesson(
                        scheduledLessonEntity.getClassUnit().getName(),
                        scheduledLessonEntity.getRoom().getName(),
                        scheduledLessonEntity.getDays(),
                        scheduledLessonEntity.getWeeks(),
                        scheduledLessonEntity.getStartSlot(),
                        scheduledLessonEntity.getDuration());
                timetable.addScheduledLesson(scheduledLesson);
                for(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity : scheduledLessonEntity.getScheduledLessonTeacherList()) {
                    scheduledLesson.addTeacher(scheduledLessonTeacherEntity.getTeacher().getId());
                }
            }
        }

        for(RestrictionEntity restrictionEntity : entityModel.getRestrictions()) {
            for(ClassRestrictionEntity classRestriction : restrictionEntity.getClassRestrictionEntityList()) {
                Restriction restriction = RestrictionFactory.createRestriction(restrictionEntity.getName(), classRestriction.getPenalty(), classRestriction.getRequired());

                for(ClassUnitEntity classUnitEntity : entityModel.getClassUnits()) {
                    // TODO: Fazer em conjunto com a alteração da BD
                }
            }
        }

        return data;
    }
}
