package thesis.model.mapper;

import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.domain.components.constraints.ConstraintFactory;
import thesis.model.persistence.EntityRepository;
import thesis.model.persistence.entities.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelConverter {
    public static InMemoryRepository convertToDomain(EntityRepository entityRepository) throws Exception {
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
            Room room = RoomFactory.createRoom(roomEntity.getName());

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

            data.addRoom(room);
        }

        // Add the courses, configs, subparts, classes and respective teachers
        for(CourseEntity courseEntity : entityRepository.getCourses()) {
            Course course = new Course(courseEntity.getName());
            for(ConfigEntity configEntity : courseEntity.getConfigList()) {
                Config config = new Config(configEntity.getName());
                for(SubpartEntity subpartEntity : configEntity.getSubpartList()) {
                    Subpart subpart = new Subpart(subpartEntity.getName());
                    for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnits()) {
                        ClassUnit classUnit = new ClassUnit(classUnitEntity.getName());
                        for(TeacherEntity teacherEntity : classUnitEntity.getTeacherEntityClassList()) {
                            classUnit.addTeacher(teacherEntity.getId());
                        }

                        subpart.addClassUnit(classUnit);
                        data.addClassUnit(classUnit);
                    }
                    config.addSubpart(subpart);
                }
                course.addConfig(config);
            }
            data.addCourse(course);
        }

        // Add the teachers and theirs unavailabilities
        for(TeacherEntity teacherEntity : entityRepository.getTeachers()) {
            Teacher teacher = new Teacher(teacherEntity.getId(), teacherEntity.getName());

            for(TeacherUnavailabilityEntity teacherUnavailabilityEntity : teacherEntity.getTeacherUnavailabilityEntityList()) {
                teacher.addUnavailability(
                        teacherUnavailabilityEntity.getDays(),
                        teacherUnavailabilityEntity.getWeeks(),
                        teacherUnavailabilityEntity.getStartSlot(),
                        teacherUnavailabilityEntity.getDuration());
            }

            data.addTeacher(teacher);
        }

        // Add the timetables, scheduled lessons and respective teachers
        for(TimetableEntity timetableEntity : entityRepository.getTimetables()) {
            Timetable timetable = new Timetable(data.getProgramName());

            for(ScheduledLessonEntity scheduledLessonEntity : timetableEntity.getScheduledLessonEntityList()) {
                ScheduledLesson scheduledLesson = new ScheduledLesson(
                        scheduledLessonEntity.getClassUnit().getName(),
                        scheduledLessonEntity.getRoom().getName(),
                        scheduledLessonEntity.getDays(),
                        scheduledLessonEntity.getWeeks(),
                        scheduledLessonEntity.getStartSlot(),
                        scheduledLessonEntity.getDuration());

                for(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity : scheduledLessonEntity.getScheduledLessonTeacherList()) {
                    scheduledLesson.addTeacherId(scheduledLessonTeacherEntity.getTeacher().getId());
                }

                scheduledLesson.bindModel(data);
                timetable.addScheduledLesson(scheduledLesson);
            }
            data.addTimetable(timetable);
        }

        for(ConstraintEntity constraintEntity : entityRepository.getConstraintEntities()) {
            Constraint constraint = ConstraintFactory.createConstraint(constraintEntity.getConstraintTypeEntity().getName(), constraintEntity.getFirstParameter(), constraintEntity.getSecondParameter(), constraintEntity.getPenalty(), constraintEntity.getRequired(), data.getTimetableConfiguration());

            // Add the classes
            for(ClassUnitEntity classUnitEntity : constraintEntity.getClassRestrictionEntityList()) {
                constraint.addClassUnitId(classUnitEntity.getName());
            }

            data.addConstraint(constraint);
        }

        return data;
    }

    public static EntityRepository convertToEntity(InMemoryRepository inMemoryRepository) {
        EntityRepository data = new EntityRepository();

        TimetableConfiguration timetableConfiguration = inMemoryRepository.getTimetableConfiguration();

        ProgramEntity programEntity = new ProgramEntity(inMemoryRepository.getProgramName(),
                timetableConfiguration.getNumDays(), timetableConfiguration.getNumWeeks(), timetableConfiguration.getSlotsPerDay(),
                timetableConfiguration.getTimeWeight(), timetableConfiguration.getRoomWeight(), timetableConfiguration.getDistribWeight());

        data.storeProgram(programEntity);

        for(Course course : inMemoryRepository.getCourses()) {
            CourseEntity courseEntity = new CourseEntity(programEntity, course.getCourseId());

            for(Config config : course.getConfigList()) {
                ConfigEntity configEntity = new ConfigEntity(courseEntity, config.getConfigId());

                for(Subpart subpart : config.getSubpartList()) {
                    SubpartEntity subpartEntity = new SubpartEntity(configEntity, subpart.getSubpartId());

                    for(ClassUnit classUnit : subpart.getClassUnitList()) {
                        ClassUnitEntity classUnitEntity = new ClassUnitEntity(subpartEntity, classUnit.getClassId());
                    }
                }
            }

            data.storeCourse(courseEntity);
        }

        for(Teacher teacher : inMemoryRepository.getTeachers()) {
            TeacherEntity teacherEntity = new TeacherEntity(teacher.getId(), teacher.getName());

            for(Time unavailability : teacher.getTeacherUnavailabilities()) {
                teacherEntity.addUnavailability(new TeacherUnavailabilityEntity(teacherEntity,
                        unavailability.getLength(), unavailability.getStartSlot(), unavailability.getDays(), unavailability.getWeeks()));
            }

            data.storeTeacher(teacherEntity);
        }

        // Add the rooms and unavailabilities
        for(Room room : inMemoryRepository.getRooms()) {
            RoomEntity roomEntity = new RoomEntity(room.getRoomId());

            for(Time unavailability : room.getRoomUnavailabilities()) {
                RoomUnavailabilityEntity roomUnavailabilityEntity = new RoomUnavailabilityEntity(roomEntity,
                        unavailability.getDays(), unavailability.getWeeks(), unavailability.getStartSlot(), unavailability.getLength());
            }

            data.storeRoom(roomEntity);
        }

        // Add the room distances
        //TODO: may add multiple distances. Example room1 -> room2 and room2 -> room1
        for(Room room : inMemoryRepository.getRooms()) {
            RoomEntity room1 = data.getRoom(room.getRoomId());

            for(Map.Entry<String, Integer> roomDistance : room.getRoomDistances().entrySet()) {
                RoomEntity room2 = data.getRoom(roomDistance.getKey());

                RoomDistanceEntity roomDistanceEntity = new RoomDistanceEntity(room1, room2, roomDistance.getValue());
            }
        }

        for(Timetable timetable : inMemoryRepository.getTimetableList()) {
            TimetableEntity timetableEntity = new TimetableEntity(programEntity);

            for(ScheduledLesson scheduledLesson : timetable.getScheduledLessonList()) {
                ClassUnitEntity classUnitEntity = data.getClassUnit(scheduledLesson.getClassId());
                RoomEntity roomEntity = data.getRoom(scheduledLesson.getRoomId());
                ScheduledLessonEntity scheduledLessonEntity = new ScheduledLessonEntity(timetableEntity, classUnitEntity, roomEntity, scheduledLesson.getDays(), scheduledLesson.getWeeks(), scheduledLesson.getStartSlot(), scheduledLesson.getLength());
            }

            data.storeTimetable(timetableEntity);
        }

        return data;
    }
}
