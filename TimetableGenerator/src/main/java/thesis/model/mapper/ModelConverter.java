package thesis.model.mapper;

import org.hibernate.Session;
import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.domain.components.constraints.ConstraintFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.persistence.repository.EntityRepository;
import thesis.model.persistence.repository.entities.*;
import thesis.model.persistence.repository.entities.utils.ConstraintTypeEntityFactory;
import thesis.model.persistence.repository.entities.utils.TimeBlockEntityFactory;

import java.util.HashMap;
import java.util.Map;

public class ModelConverter {
    public static InMemoryRepository convertToDomain(EntityRepository entityRepository) throws CheckedIllegalArgumentException, InvalidConfigurationException {
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

        data.setLastUpdatedAt(programEntity.getLastUpdatedAt());

        // Add the rooms and the relations of distance and unavailabilities
        for(RoomEntity roomEntity : entityRepository.getRooms()) {
            Room room = RoomFastIdFactory.createRoom(roomEntity.getName());

            for(RoomDistanceEntity roomDistanceEntity : roomEntity.getRoom1DistanceSet()) {
                room.addRoomDistance(roomDistanceEntity.getRoom2().getName(), roomDistanceEntity.getDistance());
            }

            for(TimeBlockEntity roomUnavailabilityEntity : roomEntity.getRoomUnavailabilityList()) {
                room.addUnavailability(
                        roomUnavailabilityEntity.getDays(),
                        roomUnavailabilityEntity.getWeeks(),
                        roomUnavailabilityEntity.getStartSlot(),
                        roomUnavailabilityEntity.getDuration());
            }

            data.addRoom(room);
        }

        Map<ConstraintEntity, Constraint> constraintMap = new HashMap<>();
        for(ConstraintEntity constraintEntity : entityRepository.getConstraintEntities()) {
            Constraint constraint = ConstraintFactory.createConstraint(
                    constraintEntity.getConstraintTypeEntity().getName(),
                    constraintEntity.getFirstParameter(),
                    constraintEntity.getSecondParameter(),
                    constraintEntity.getPenalty(),
                    constraintEntity.getRequired(),
                    data.getTimetableConfiguration());

            data.addConstraint(constraint);

            constraintMap.put(constraintEntity, constraint);
        }

        // Add the courses, configs, subparts, classes and respective teachers
        for(CourseEntity courseEntity : entityRepository.getCourses()) {
            Course course = new Course(courseEntity.getName());
            for(ConfigEntity configEntity : courseEntity.getConfigSet()) {
                Config config = new Config(configEntity.getName());
                for(SubpartEntity subpartEntity : configEntity.getSubpartSet()) {
                    Subpart subpart = new Subpart(subpartEntity.getName());
                    for(ClassUnitEntity classUnitEntity : subpartEntity.getClassUnitSet()) {
                        ClassUnit classUnit = new ClassUnit(classUnitEntity.getName());
                        for(TeacherClassEntity teacherClassEntity : classUnitEntity.getTeacherClassEntitySet()) {
                            classUnit.addTeacher(teacherClassEntity.getTeacherEntity().getId());
                        }
                        for(ClassConstraintEntity classConstraintEntity : classUnitEntity.getClassConstraintEntitySet()) {
                            classUnit.addConstraint(constraintMap.get(classConstraintEntity.getConstraintEntity()));
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

            for(TeacherUnavailabilityEntity teacherUnavailabilityEntity : teacherEntity.getTeacherUnavailabilityEntitySet()) {
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
                TimeBlockEntity timeBlockEntity = scheduledLessonEntity.getTimeBlockEntity();
                ScheduledLesson scheduledLesson = new ScheduledLesson(
                        scheduledLessonEntity.getClassUnitEntity().getName(),
                        scheduledLessonEntity.getRoomEntity().getName(),
                        timeBlockEntity.getDays(),
                        timeBlockEntity.getWeeks(),
                        timeBlockEntity.getStartSlot(),
                        timeBlockEntity.getDuration());

                for(ScheduledLessonTeacherEntity scheduledLessonTeacherEntity : scheduledLessonEntity.getScheduledLessonTeacherList()) {
                    scheduledLesson.addTeacherId(scheduledLessonTeacherEntity.getTeacher().getId());
                }

                scheduledLesson.bindModel(data);
                timetable.addScheduledLesson(scheduledLesson);
            }
            data.addTimetable(timetable);
        }

        return data;
    }

    private static TimeBlockEntity getTimeBlockEntityFromTime(TimeBlockEntityFactory timeBlockEntityFactory, Time time) {
        return timeBlockEntityFactory.getOrCreate(time.getStartSlot(), time.getLength(), time.getDays(), time.getWeeks());
    }

    public static EntityRepository convertToEntity(Session session, InMemoryRepository inMemoryRepository) {
        EntityRepository data = new EntityRepository();

        TimeBlockEntityFactory timeBlockEntityFactory = new TimeBlockEntityFactory(session);
        ConstraintTypeEntityFactory constraintTypeEntityFactory = new ConstraintTypeEntityFactory(session);

        TimetableConfiguration timetableConfiguration = inMemoryRepository.getTimetableConfiguration();

        ProgramEntity programEntity = new ProgramEntity(inMemoryRepository.getProgramName(),
            timetableConfiguration.getNumDays(), timetableConfiguration.getNumWeeks(), timetableConfiguration.getSlotsPerDay(),
            timetableConfiguration.getTimeWeight(), timetableConfiguration.getRoomWeight(), timetableConfiguration.getDistribWeight(), inMemoryRepository.getLastUpdatedAt());

        data.storeProgram(programEntity);

        for (Teacher teacher : inMemoryRepository.getTeachers()) {
            TeacherEntity teacherEntity = new TeacherEntity(teacher.getId(), teacher.getName());

            for (Time unavailability : teacher.getTeacherUnavailabilities()) {
                new TeacherUnavailabilityEntity(teacherEntity,
                    getTimeBlockEntityFromTime(timeBlockEntityFactory, unavailability));
            }

            data.storeTeacher(teacherEntity);
        }

        // Add the rooms and unavailabilities
        for (Room room : inMemoryRepository.getRooms()) {
            RoomEntity roomEntity = new RoomEntity(room.getRoomId());

            for (Time unavailability : room.getRoomUnavailabilities()) {
                roomEntity.addRoomUnavailability(getTimeBlockEntityFromTime(timeBlockEntityFactory, unavailability));
            }

            data.storeRoom(roomEntity);
        }

        // Add the room distances
        //TODO: may add multiple equal distances. Example room1 -> room2 and room2 -> room1
        for (Room room : inMemoryRepository.getRooms()) {
            RoomEntity room1 = data.getRoom(room.getRoomId());

            for (Map.Entry<String, Integer> roomDistance : room.getRoomDistances().entrySet()) {
                RoomEntity room2 = data.getRoom(roomDistance.getKey());

                if(room2 == null) continue;

                new RoomDistanceEntity(room1, room2, roomDistance.getValue());
            }
        }

        Map<Constraint, ConstraintEntity> constraintMap = new HashMap<>();
        for(Constraint constraint : inMemoryRepository.getConstraints()) {
            ConstraintTypeEntity constraintTypeEntity =
                    constraintTypeEntityFactory.getConstraintType(constraint.getType());

            ConstraintEntity constraintEntity = new ConstraintEntity(constraintTypeEntity,
                    constraint.getFirstParameter(), constraint.getSecondParameter(),
                    constraint.getPenalty(), constraint.getRequired());

            constraintMap.put(constraint, constraintEntity);
        }

        for (Course course : inMemoryRepository.getCourses()) {
            CourseEntity courseEntity = new CourseEntity(programEntity, course.getCourseId());

            for (Config config : course.getConfigList()) {
                ConfigEntity configEntity = new ConfigEntity(courseEntity, config.getConfigId());

                for (Subpart subpart : config.getSubpartList()) {
                    SubpartEntity subpartEntity = new SubpartEntity(configEntity, subpart.getSubpartId());

                    for (ClassUnit classUnit : subpart.getClassUnitList()) {
                        ClassUnitEntity classUnitEntity = new ClassUnitEntity(subpartEntity, classUnit.getClassId());

                        for(Map.Entry<String, Integer> roomPenalty : classUnit.getClassRoomPenalties().entrySet()) {
                            RoomEntity roomEntity = data.getRoom(roomPenalty.getKey());
                            new ClassRoomEntity(classUnitEntity, roomEntity, roomPenalty.getValue());
                        }

                        for(Map.Entry<Time, Integer> timePenalty : classUnit.getClassTimePenalties().entrySet()) {
                            Time time = timePenalty.getKey();

                            new ClassTimeEntity(classUnitEntity,
                                getTimeBlockEntityFromTime(timeBlockEntityFactory, time), timePenalty.getValue());
                        }

                        for(Constraint constraint : classUnit.getConstraintList()) {
                            ConstraintEntity constraintEntity = constraintMap.get(constraint);

                            if(constraintEntity != null) {
                                new ClassConstraintEntity(classUnitEntity, constraintEntity);
                            } else {
                                System.out.println("ConstraintEntity null!!");
                            }
                        }
                    }
                }
            }

            data.storeCourse(courseEntity);
        }

        for (Timetable timetable : inMemoryRepository.getTimetableList()) {
            TimetableEntity timetableEntity = new TimetableEntity(programEntity, timetable.getDateOfCreation());

            for (ScheduledLesson scheduledLesson : timetable.getScheduledLessonList()) {
                ClassUnitEntity classUnitEntity = data.getClassUnit(scheduledLesson.getClassId());
                RoomEntity roomEntity = data.getRoom(scheduledLesson.getRoomId());

                new ScheduledLessonEntity(timetableEntity, classUnitEntity, roomEntity, timeBlockEntityFactory.getOrCreate(scheduledLesson.getStartSlot(), scheduledLesson.getLength(), scheduledLesson.getDays(), scheduledLesson.getWeeks()));
            }

            data.storeTimetable(timetableEntity);
        }

        return data;
    }
}
