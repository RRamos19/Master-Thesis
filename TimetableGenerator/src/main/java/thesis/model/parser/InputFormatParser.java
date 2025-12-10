package thesis.model.parser;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import thesis.model.XLSXFormat;
import thesis.model.domain.DataRepository;
import thesis.model.domain.InMemoryRepository;
import thesis.model.domain.components.*;
import thesis.model.domain.components.constraints.ConstraintFactory;
import thesis.model.exceptions.CheckedIllegalArgumentException;
import thesis.model.exceptions.InvalidConfigurationException;
import thesis.model.exceptions.ParsingException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InputFormatParser implements InputFileReader {
    private static final DateTimeFormatter timeStampUTC12Formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // Principal Tags
    private static final String TEACHERS_TAG          = "teachers";
    private static final String COURSES_TAG           = "courses";
    private static final String ROOMS_TAG             = "rooms";
    private static final String DISTRIBUTIONS_TAG     = "distributions";
    private static final String OPTIMIZATION_TAG      = "optimization";
    private static final String PROBLEM_TAG           = "problem";
    private static final String SOLUTION_TAG          = "solution";

    // Secondary Tags
    private static final String ROOM_TAG              = "room";
    private static final String TEACHER_TAG           = "teacher";
    private static final String COURSE_TAG            = "course";
    private static final String CONFIG_TAG            = "config";
    private static final String SUBPART_TAG           = "subpart";
    private static final String DISTRIBUTION_TAG      = "distribution";

    // Tags with no descendants
    private static final String TRAVEL_TAG            = "travel";
    private static final String UNAVAILABILITY_TAG    = "unavailable";
    private static final String CLASS_TAG             = "class";
    private static final String TIME_TAG              = "time";

    /**
     * This interface was created to allow the throw of certain exceptions, which should be caught by the method callers.
     * @param <T> Type which implements the Marker Interface. It is used to categorize the various types of classes which result from the parsing of files.
     */
    @FunctionalInterface
    private interface XmlProcessor<T extends XmlResult> {
        T apply(XMLEventReader reader) throws ParsingException, InvalidConfigurationException;
    }

    /**
     * This method is a generalization of the parsing of files. No matter which file is read the process is the same.
     * @param file File that is to be read
     * @param parser Functional interface made to reduce code repetition and to allow throws of exceptions
     * @return Either a Timetable or a InMemoryRepository
     * @throws ParsingException Exception related to the parsing of the file
     * @throws InvalidConfigurationException Exception related to the configuration
     */
    private <T extends XmlResult> T processXmlFile(File file, XmlProcessor<T> parser) throws ParsingException, InvalidConfigurationException {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(bis);

            try {
                return parser.apply(reader);
            } finally {
                try {
                    reader.close();
                } catch (XMLStreamException ignore) {}
                bis.close();
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error, the file was not found", e);
        } catch (IOException e) {
            throw new ParsingException("Error while closing the file", e);
        } catch (XMLStreamException e) {
            throw new ParsingException("Error while processing the XML file. The file may be malformed", e);
        }
    }

    private String cellString(DataFormatter formatter, Row row, int col) {
        String result = formatter.formatCellValue(row.getCell(col));
        return result.isEmpty() ? null : result;
    }

    private int cellInt(DataFormatter formatter, Row row, int col) {
        return Integer.parseInt(formatter.formatCellValue(row.getCell(col)));
    }

    private long cellLong(DataFormatter formatter, Row row, int col) {
        return Long.parseLong(formatter.formatCellValue(row.getCell(col)));
    }

    private boolean cellBool(DataFormatter formatter, Row row, int col) {
        return Boolean.parseBoolean(formatter.formatCellValue(row.getCell(col)));
    }

    private InMemoryRepository readXLSXFile(File file) throws IOException, ParsingException, CheckedIllegalArgumentException, InvalidConfigurationException {
        DataFormatter formatter = new DataFormatter();
        try(XSSFWorkbook workbook = new XSSFWorkbook(file)) {
            InMemoryRepository repository = new DataRepository();

            for (XLSXFormat.SHEET_NAMES sheetName : XLSXFormat.FORMAT_PER_SHEET.keySet()) {
                XSSFSheet sheet = workbook.getSheet(sheetName.label);
                if (sheet == null) continue;

                // Ignore header
                int first = sheet.getFirstRowNum() + 1;
                int last  = sheet.getLastRowNum();

                switch (sheetName) {
                    case Configuration:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String programName = cellString(formatter, row, 0);
                            short numDays = (short) cellInt(formatter, row, 1);
                            short numSlots = (short) cellInt(formatter, row, 2);
                            int numWeeks = cellInt(formatter, row, 3);
                            short timeWeight = (short) cellInt(formatter, row, 4);
                            short roomWeight = (short) cellInt(formatter, row, 5);
                            short distributionWeight = (short) cellInt(formatter, row, 6);

                            repository.setProgramName(programName);
                            repository.setConfiguration(numDays, numWeeks, numSlots);
                            repository.setOptimizationParameters(timeWeight, roomWeight, distributionWeight);
                        }
                        break;
                    case Classes:
                        Map<String, Config> configMap = new HashMap<>();
                        Map<String, Subpart> subpartMap = new HashMap<>();
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String courseId = cellString(formatter, row, 0);
                            String configId = cellString(formatter, row, 1);
                            String subpartId = cellString(formatter, row, 2);
                            String classUnitId = cellString(formatter, row, 3);
                            String parentClassUnitId = cellString(formatter, row, 4);

                            Course course = repository.getCourse(courseId);
                            if(course == null) {
                                course = new Course(courseId);
                                repository.addCourse(course);
                            }

                            Config config = configMap.get(configId);
                            if(config == null) {
                                config = new Config(configId);
                                configMap.put(configId, config);
                                course.addConfig(config);
                            }

                            Subpart subpart = subpartMap.get(subpartId);
                            if(subpart == null) {
                                subpart = new Subpart(subpartId);
                                subpartMap.put(subpartId, subpart);
                                config.addSubpart(subpart);
                            }

                            ClassUnit cls = new ClassUnit(classUnitId);
                            cls.setParentClassId(parentClassUnitId);
                            subpart.addClassUnit(cls);

                            repository.addClassUnit(cls);
                        }

                        break;
                    case Class_Times:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String classUnitId = cellString(formatter, row, 0);
                            short startSlot = (short) cellInt(formatter, row, 1);
                            short length = (short) cellInt(formatter, row, 2);
                            short days = (short) cellInt(formatter, row, 3);
                            int weeks = cellInt(formatter, row, 4);
                            int penalty = cellInt(formatter, row, 5);

                            ClassUnit cls = repository.getClassUnit(classUnitId);
                            if(cls == null) {
                                throw new ParsingException("Class Unit " + classUnitId + " found in class times wasn't defined as a class");
                            }
                            cls.addClassTime(days, weeks, startSlot, length, penalty);
                        }
                        break;
                    case Class_Rooms:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String classUnitId = cellString(formatter, row, 0);
                            String roomId = cellString(formatter, row, 1);
                            int penalty = cellInt(formatter, row, 2);

                            ClassUnit cls = repository.getClassUnit(classUnitId);
                            if(cls == null) {
                                throw new ParsingException("Class Unit " + classUnitId + " found in class rooms wasn't defined as a class");
                            }
                            cls.addRoom(roomId, penalty);
                        }
                        break;
                    case Class_Teachers:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String classUnitId = cellString(formatter, row, 0);
                            int teacherId = cellInt(formatter, row, 1);

                            ClassUnit cls = repository.getClassUnit(classUnitId);
                            if(cls == null) {
                                throw new ParsingException("Class Unit " + classUnitId + " found in class teachers wasn't defined as a class");
                            }
                            Teacher teacher = repository.getTeacher(teacherId);
                            if(teacher == null) {
                                throw new ParsingException("Teacher with id " + teacherId + " found in class teachers wasn't defined as a Teacher");
                            }

                            cls.addTeacher(teacherId);
                            teacher.addClassUnit(classUnitId);
                        }
                        break;
                    case Constraints:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            int constraintId = cellInt(formatter, row, 0);
                            String constraintType = cellString(formatter, row, 1);
                            int penalty = cellInt(formatter, row, 2);
                            boolean required = cellBool(formatter, row, 3);
                            String classList = cellString(formatter, row, 4);

                            Constraint constraint = ConstraintFactory.createConstraint(
                                constraintId,
                                constraintType,
                                penalty,
                                required,
                                repository.getTimetableConfiguration());

                            for(String classId : classList.split(",")) {
                                ClassUnit cls = repository.getClassUnit(classId);
                                if(cls == null) {
                                    throw new ParsingException("Class Unit " + classId + " found in constraints wasn't defined as a class");
                                }
                                cls.addConstraint(constraint);
                                constraint.addClassUnitId(classId);
                            }

                            repository.addConstraint(constraint);
                        }
                        break;
                    case Teachers:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            int teacherId = cellInt(formatter, row, 0);
                            String teacherName = cellString(formatter, row, 1);

                            Teacher teacher = new Teacher(teacherId, teacherName);
                            repository.addTeacher(teacher);
                        }
                        break;
                    case Teacher_Unavailabilities:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            int teacherId = cellInt(formatter, row, 0);
                            short startSlot = (short) cellInt(formatter, row, 1);
                            short length = (short) cellInt(formatter, row, 2);
                            short days = (short) cellInt(formatter, row, 3);
                            int weeks = cellInt(formatter, row, 4);

                            Teacher teacher = repository.getTeacher(teacherId);
                            if(teacher == null) {
                                throw new ParsingException("Teacher with id " + teacherId + " found in teacher unavailabilities wasn't defined as a Teacher");
                            }
                            teacher.addUnavailability(days, weeks, startSlot, length);
                        }
                        break;
                    case Rooms:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String roomId = cellString(formatter, row, 0);

                            Room room = RoomFastIdFactory.createRoom(roomId);
                            repository.addRoom(room);
                        }
                        break;
                    case Room_Unavailabilities:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String roomId = cellString(formatter, row, 0);
                            short startSlot = (short) cellInt(formatter, row, 1);
                            short length = (short) cellInt(formatter, row, 2);
                            short days = (short) cellInt(formatter, row, 3);
                            int weeks = cellInt(formatter, row, 4);

                            Room room = repository.getRoom(roomId);
                            if(room == null) {
                                throw new ParsingException("Room with id " + roomId + " found in room unavailabilities wasn't defined as a Room");
                            }
                            room.addUnavailability(days, weeks, startSlot, length);
                        }
                        break;
                    case Room_Distances:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String room1Id = cellString(formatter, row, 0);
                            String room2Id = cellString(formatter, row, 1);
                            int distance = cellInt(formatter, row, 2);

                            Room room1 = repository.getRoom(room1Id);
                            if(room1 == null) {
                                throw new ParsingException("Room 1 with id " + room1Id + " found in room distances wasn't defined as a Room");
                            }

                            room1.addRoomDistance(room2Id, distance);
                        }
                        break;
                    case Solutions:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String solutionId = cellString(formatter, row, 0);
                            long runtime = cellLong(formatter, row, 1);
                            String dateOfCreation = cellString(formatter, row, 2);

                            if(solutionId == null) {
                                throw new ParsingException("Solution Id is null");
                            }
                            if(dateOfCreation == null) {
                                throw new ParsingException("Date of Creation of the solution " + solutionId + " is null");
                            }

                            Timetable timetable = new Timetable(
                                UUID.fromString(solutionId),
                                repository.getProgramName(),
                                LocalDateTime.from(timeStampUTC12Formatter.parse(dateOfCreation)));

                            timetable.setRuntime(runtime);

                            repository.addTimetable(timetable);
                        }
                        break;
                    case Scheduled_Lessons:
                        for(int i = first; i <= last; i++) {
                            Row row = sheet.getRow(i);
                            String solutionId = cellString(formatter, row, 0);
                            String classId = cellString(formatter, row, 1);
                            String roomId = cellString(formatter, row, 2);
                            String teacherIds = cellString(formatter, row, 3);
                            short startSlot = (short) cellInt(formatter, row, 4);
                            short length = (short) cellInt(formatter, row, 5);
                            short days = (short) cellInt(formatter, row, 6);
                            int weeks = cellInt(formatter, row, 7);

                            if(solutionId == null) {
                                throw new ParsingException("Solution Id found in scheduled lessons is null");
                            }

                            Timetable timetable = repository.getTimetable(UUID.fromString(solutionId));
                            if(timetable == null) {
                                throw new ParsingException("Timetable with id " + solutionId + " found in scheduled lessons wasn't defined as a Timetable");
                            }

                            ScheduledLesson scheduledLesson = new ScheduledLesson(classId, roomId, days, weeks, startSlot, length);

                            if(teacherIds != null) {
                                List<Integer> teacherList = new ArrayList<>();
                                for (String teacherIdString : teacherIds.split(",")) {
                                    teacherList.add(Integer.parseInt(teacherIdString));
                                }

                                for(int teacherId : teacherList) {
                                    scheduledLesson.addTeacherId(teacherId);
                                }
                            }

                            timetable.addScheduledLesson(scheduledLesson);
                        }
                        break;
                }
            }

            repository.verifyValidity();

            // Set the last updated at timestamp
            repository.setLastUpdatedAt();

            return repository;
        } catch (InvalidFormatException ignored) {
        // This is impossible because this method is only called if this exception doesn't happen in the isXMLFile method
        }

        return null;
    }

    /**
     * This method verifies if the file given is a solution or a problem and parses it.
     * @param file File that is to be read
     * @return Either a Timetable or a InMemoryRepository
     * @throws ParsingException Exception related to the parsing of the file
     * @throws InvalidConfigurationException Exception related to the configuration
     */
    public XmlResult readXmlFile(File file) throws ParsingException, InvalidConfigurationException {
        return processXmlFile(file, reader -> {
            try {
                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();
                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();

                        if (qName.equals(SOLUTION_TAG)) {
                            return parseSolution(reader, startElement, event);
                        } else if (qName.equals(PROBLEM_TAG)) {
                            return parseProblem(reader, startElement, event);
                        }
                    }
                }
                throw new ParsingException("The XML file does not contain a valid <solution> or <problem> root element");
            } catch (XMLStreamException e) {
                throw new ParsingException("Error while processing the XML file. The file may be malformed", e);
            }
        });
    }

    public boolean isXMLFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(bis);
        } catch (XMLStreamException e) {
            return false;
        }

        return true;
    }

    public boolean isXLSXFile(File file) throws IOException {
        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(file)) {

        } catch (RuntimeException | InvalidFormatException e) {
            return false;
        }

        return true;
    }

    @Override
    public XmlResult readFile(File file) throws ParsingException, InvalidConfigurationException, IOException, CheckedIllegalArgumentException {
        if(isXLSXFile(file)) {
            return readXLSXFile(file);
        } else if(isXMLFile(file)) {
            return readXmlFile(file);
        } else {
            throw new ParsingException("Input file type is not supported");
        }
    }

    /**
     * Parses the whole solution file.
     * @param reader Reader of the xml file
     * @param startElement Starting tag of the solution
     * @param event Contains the event that was read to check if the file is a problem or a solution
     * @return Timetable instance filled with the data present in the file
     * @throws ParsingException Exception related to the parsing of the file
     * @throws XMLStreamException Exception related to the xml file processing
     */
    private Timetable parseSolution(XMLEventReader reader, StartElement startElement, XMLEvent event) throws ParsingException, XMLStreamException {
        Timetable timetable = new Timetable();

        String solutionName = getAttributeValue(startElement, "name");
        String dateOfCreationString = getAttributeValue(startElement, "timeStampUTC12");
        String runtimeString = getAttributeValue(startElement, "runtime");

        LocalDateTime dateOfCreation = dateOfCreationString != null
                ? LocalDateTime.from(timeStampUTC12Formatter.parse(dateOfCreationString)) : null;
        int runtime = runtimeString != null ? Integer.parseInt(runtimeString) : 0;

        if (solutionName == null) {
            throw new ParsingException(event.getLocation(), "A name must be specified in the solution tag");
        }

        timetable.setProgramName(solutionName);
        timetable.setRuntime(runtime);
        if (dateOfCreation != null) timetable.setDateOfCreation(dateOfCreation);

        readScheduledLessons(reader, timetable);

        return timetable;
    }

    /**
     * Reads all the scheduled lessons before encountering the termination tag
     */
    private void readScheduledLessons(XMLEventReader eventReader, Timetable timetable) throws XMLStreamException, ParsingException {
        Map<String, ScheduledLesson> scheduledLessonMap = new HashMap<>();
        Integer teacherId = null;

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch(startElementName) {
                        case CLASS_TAG:
                            if(teacherId == null) {
                                String classId = getAttributeValue(startElement, "id");
                                String roomId = getAttributeValue(startElement, "room");
                                String days = getAttributeValue(startElement, "days");
                                String start = getAttributeValue(startElement, "start");
                                String weeks = getAttributeValue(startElement, "weeks");

                                try {
                                    ScheduledLesson scheduledLesson = new ScheduledLesson(classId, roomId, days, weeks, start, "0");
                                    scheduledLessonMap.put(scheduledLesson.getClassId(), scheduledLesson);
                                    timetable.addScheduledLesson(scheduledLesson);
                                } catch (CheckedIllegalArgumentException e) {
                                    throw new ParsingException(event.getLocation(), e.getMessage());
                                }
                            } else {
                                String classId = getAttributeValue(startElement, "id");

                                if(classId != null) {
                                    ScheduledLesson scheduledLesson = scheduledLessonMap.get(classId);
                                    if(scheduledLesson != null) {
                                        scheduledLesson.addTeacherId(teacherId);
                                    }
                                }
                            }
                            break;
                        case TEACHER_TAG:
                            String teacherIdString = getAttributeValue(startElement, "id");

                            try{
                                if(teacherIdString != null) {
                                    teacherId = Integer.parseInt(teacherIdString);
                                }
                            } catch (NumberFormatException e) {
                                throw new ParsingException(event.getLocation(), e.getMessage());
                            }
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    switch(endElementName) {
                        case TEACHER_TAG:
                            teacherId = null;
                            break;
                        case SOLUTION_TAG:
                            break;
                    }
                    if (endElementName.equals(SOLUTION_TAG)) {
                        // If the end of the tag solution is found the function ends
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * Parses the whole problem file.
     * @param reader Reader of the xml file
     * @param startElement Starting tag of the problem
     * @param event Contains the event that was read to check if the file is a problem or a solution
     * @return InMemoryRepository instance filled with the data present in the file
     * @throws ParsingException Exception related to the parsing of the file
     * @throws InvalidConfigurationException Exception related to the configuration
     * @throws XMLStreamException Exception related to the xml file processing
     */
    private InMemoryRepository parseProblem(XMLEventReader reader, StartElement startElement, XMLEvent event) throws ParsingException, InvalidConfigurationException, XMLStreamException {
        InMemoryRepository data = new DataRepository();

        String programName = getAttributeValue(startElement, "name");
        String nrDaysString = getAttributeValue(startElement, "nrDays");
        String slotsPerDayString = getAttributeValue(startElement, "slotsPerDay");
        String nrWeeksString = getAttributeValue(startElement, "nrWeeks");

        short nrDays;
        short slotsPerDay;
        int nrWeeks;
        try {
            nrDays = nrDaysString != null ? Short.parseShort(nrDaysString) : 0;
            slotsPerDay = slotsPerDayString != null ? Short.parseShort(slotsPerDayString) : 0;
            nrWeeks = nrWeeksString != null ? Integer.parseInt(nrWeeksString) : 0;
        } catch (NumberFormatException e) {
            throw new ParsingException(event.getLocation(), e.getMessage());
        }

        List<String> problemTagErrors = new ArrayList<>();
        if (programName == null) problemTagErrors.add("Program name must be specified");
        if (nrDays == 0) problemTagErrors.add("nrDays >= 1");
        if (slotsPerDay <= 1) problemTagErrors.add("slotsPerDay > 1");
        if (nrWeeks == 0) problemTagErrors.add("nrWeeks >= 1");
        if (!problemTagErrors.isEmpty()) {
            String errorMessage = "The following conditions must be followed:\n" + String.join("\n", problemTagErrors);
            throw new ParsingException(event.getLocation(), errorMessage);
        }

        data.setProgramName(programName);
        data.setConfiguration(nrDays, nrWeeks, slotsPerDay);

        while (reader.hasNext()) {
            try {
                XMLEvent subEvent = reader.nextEvent();
                if (subEvent.isStartElement()) {
                    StartElement subStart = subEvent.asStartElement();
                    String qName = subStart.getName().getLocalPart();

                    switch (qName) {
                        case OPTIMIZATION_TAG:
                            String timeWeightString = getAttributeValue(subStart, "time");
                            String roomWeightString = getAttributeValue(subStart, "room");
                            String distributionWeightString = getAttributeValue(subStart, "distribution");

                            short timeWeight;
                            short roomWeight;
                            short distributionWeight;
                            try {
                                timeWeight = timeWeightString != null ? Short.parseShort(timeWeightString) : 0;
                                roomWeight = roomWeightString != null ? Short.parseShort(roomWeightString) : 0;
                                distributionWeight = distributionWeightString != null ? Short.parseShort(distributionWeightString) : 0;
                            } catch (NumberFormatException e) {
                                throw new ParsingException(subEvent.getLocation(), e.getMessage());
                            }

                            List<String> optimizationTagErrors = new ArrayList<>();
                            if (timeWeight < 1) optimizationTagErrors.add("time >= 1");
                            if (roomWeight < 1) optimizationTagErrors.add("room >= 1");
                            if (distributionWeight < 1) optimizationTagErrors.add("distribution >= 1");
                            if (!optimizationTagErrors.isEmpty()) {
                                String errorMessage = "The following conditions must be followed:\n" + String.join("\n", optimizationTagErrors);
                                throw new ParsingException(subEvent.getLocation(), errorMessage);
                            }

                            data.setOptimizationParameters(timeWeight, roomWeight, distributionWeight);
                            break;
                        case ROOMS_TAG:
                            readRooms(reader, data);
                            break;
                        case DISTRIBUTIONS_TAG:
                            readRestrictions(reader, data);
                            break;
                        case COURSES_TAG:
                            readCourses(reader, data);
                            break;
                        case TEACHERS_TAG:
                            readTeachers(reader, data);
                            break;
                    }
                }
            } catch (XMLStreamException e) {
                throw new ParsingException(e.getLocation(), "Error while processing the XML file. The file may be malformed");
            }
        }

        data.verifyValidity();

        // Set the last updated at timestamp
        data.setLastUpdatedAt();

        return data;
    }

    /**
     * Reads all the rooms data before encountering the termination tag
     */
    private void readRooms(XMLEventReader eventReader, InMemoryRepository data) throws XMLStreamException, ParsingException {
        Room room = null;

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case ROOM_TAG:
                            String roomName = getAttributeValue(startElement, "id");

                            room = RoomFastIdFactory.createRoom(roomName);
                            break;
                        case TRAVEL_TAG:
                            if(room == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a travel tag before a room tag");
                            }

                            String travelRoomId = getAttributeValue(startElement, "room");
                            String travelPenaltyString = getAttributeValue(startElement, "value");

                            int travelPenalty;
                            try {
                                travelPenalty = travelPenaltyString != null ? Integer.parseInt(travelPenaltyString) : 0;
                            } catch (NumberFormatException e) {
                                throw new ParsingException(event.getLocation(), e.getMessage());
                            }

                            room.addRoomDistance(travelRoomId, travelPenalty);

                            break;
                        case UNAVAILABILITY_TAG:
                            if(room == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is an unavailability tag before a room tag");
                            }

                            String days = getAttributeValue(startElement, "days");
                            String startString = getAttributeValue(startElement, "start");
                            String lengthString = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");

                            try {
                                room.addUnavailability(days, weeks, startString, lengthString);
                            } catch (Exception e) {
                                throw new ParsingException(event.getLocation(), e.getMessage());
                            }

                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    switch (endElementName){
                        case ROOM_TAG:
                            // If the end of the tag of a room is found
                            // its necessary to add said room to the data

                            if(room != null) {
                                data.addRoom(room);
                                room = null;
                            }

                            break;
                        case ROOMS_TAG:
                            // If the end of the tag of rooms is found the function ends
                            return;
                    }
                    break;
            }
        }
    }

    /**
     * Reads all the courses, configs, subparts and classes before encountering the courses termination tag
     */
    private void readCourses(XMLEventReader eventReader, InMemoryRepository data) throws XMLStreamException, ParsingException {
        Course course = null;
        Config config = null;
        Subpart subpart = null;
        ClassUnit cls = null;

        // Used to verify if there are no equal ids in the same category (course, config, subpart or class)
        Set<String> courseIds = new HashSet<>();
        Set<String> configIds = new HashSet<>();
        Set<String> subpartIds = new HashSet<>();
        Set<String> classUnitIds = new HashSet<>();

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case COURSE_TAG:
                            String courseId = getAttributeValue(startElement, "id");

                            if(courseIds.contains(courseId)) {
                                throw new ParsingException(event.getLocation(), "The id " + courseId + " is repeated in multiple courses");
                            }
                            courseIds.add(courseId);

                            course = new Course(courseId);
                            data.addCourse(course);

                            break;
                        case CONFIG_TAG:
                            if(course == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a config tag before a course tag");
                            }

                            String configId = getAttributeValue(startElement, "id");

                            if(configIds.contains(configId)) {
                                throw new ParsingException(event.getLocation(), "The id " + configId + " is repeated in multiple configs");
                            }
                            configIds.add(configId);

                            config = new Config(configId);
                            course.addConfig(config);

                            break;
                        case SUBPART_TAG:
                            if(config == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a subpart tag before a config tag");
                            }
                            String subpartId = getAttributeValue(startElement, "id");

                            if(subpartIds.contains(subpartId)) {
                                throw new ParsingException(event.getLocation(), "The id " + subpartId + " is repeated in multiple subparts");
                            }
                            subpartIds.add(subpartId);

                            subpart = new Subpart(subpartId);
                            config.addSubpart(subpart);

                            break;
                        case CLASS_TAG:
                            if(subpart == null) {
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a class tag before a subpart tag");
                            }
                            String classId = getAttributeValue(startElement, "id");
                            String parentClassId = getAttributeValue(startElement, "parent");

                            if(classUnitIds.contains(classId)) {
                                throw new ParsingException(event.getLocation(), "The id " + classId + " is repeated in multiple classes");
                            }
                            classUnitIds.add(classId);

                            cls = new ClassUnit(classId);
                            data.addClassUnit(cls);

                            if(parentClassId != null) {
                                cls.setParentClassId(parentClassId);
                            }

                            subpart.addClassUnit(cls);
                            break;
                        case ROOM_TAG:
                            if(cls == null) {
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a room tag that appears before a class tag");
                            }
                            String roomId = getAttributeValue(startElement, "id");
                            String roomPenaltyString = getAttributeValue(startElement, "penalty");

                            int roomPenalty = roomPenaltyString != null ? Integer.parseInt(roomPenaltyString) : 0;

                            cls.addRoom(roomId, roomPenalty);

                            break;
                        case TIME_TAG:
                            if(cls == null) {
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a time tag that appears before a class tag");
                            }

                            String days = getAttributeValue(startElement, "days");
                            String start = getAttributeValue(startElement, "start");
                            String length = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");
                            String timePenaltyString = getAttributeValue(startElement, "penalty");

                            int timePenalty = timePenaltyString != null ? Integer.parseInt(timePenaltyString) : 0;

                            try {
                                cls.addClassTime(days, weeks, start, length, timePenalty);
                            } catch (Exception e) {
                                throw new ParsingException(event.getLocation(), e.getMessage());
                            }
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    switch(endElementName){
                        case COURSE_TAG:
                            // If the end of the tag of a course is found
                            // its necessary to add said course to the data

                            course = null;

                            break;
                        case CONFIG_TAG:
                            // If the end of the tag of a config is found
                            // its necessary to add said config to the course

                            config = null;

                            break;
                        case SUBPART_TAG:
                            // If the end of the tag of a subpart is found
                            // its necessary to add said subpart to the config

                            subpart = null;

                            break;
                        case CLASS_TAG:
                            // If the end of the tag of a class is found
                            // its necessary to add said class to the subpart

                            cls = null;

                            break;
                        case COURSES_TAG:
                            // If the courses tag terminator is found the function ends
                            return;
                    }

                    break;
            }
        }
    }

    /**
     * Reads all the teachers data before encountering the termination tag
     */
    private void readTeachers(XMLEventReader eventReader, InMemoryRepository data) throws XMLStreamException, ParsingException {
        Teacher teacher = null;

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case TEACHER_TAG:
                            String teacherIdString = getAttributeValue(startElement,"id");
                            String teacherName = getAttributeValue(startElement,"name");

                            int teacherId = teacherIdString != null ? Integer.parseInt(teacherIdString) : 0;

                            teacher = new Teacher(teacherId, teacherName);
                            data.addTeacher(teacher);

                            break;
                        case CLASS_TAG:
                            if(teacher == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a class tag before a teacher tag");
                            }
                            String classId = getAttributeValue(startElement, "id");

                            ClassUnit cls = data.getClassUnit(classId);
                            if(cls == null) {
                                throw new ParsingException(event.getLocation(), "The class " + classId + " wasn't defined previously");
                            }
                            cls.addTeacher(teacher.getId());
                            teacher.addClassUnit(classId);

                            break;
                        case UNAVAILABILITY_TAG:
                            if(teacher == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is an unavailability tag before a teacher tag");
                            }

                            String days = getAttributeValue(startElement,"days");
                            String start = getAttributeValue(startElement, "start");
                            String length = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");

                            try {
                                teacher.addUnavailability(days, weeks, start, length);
                            } catch (CheckedIllegalArgumentException e) {
                                throw new ParsingException(event.getLocation(), e.getMessage());
                            }

                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    switch(endElementName){
                        case TEACHER_TAG:
                            // If the end of the tag of a teacher is found
                            // its necessary to add said teacher to the data

                            teacher = null;

                            break;
                        case TEACHERS_TAG:
                            // If the teachers tag terminator is found the function ends
                            return;
                    }

                    break;
            }
        }
    }

    /**
     * Reads all the restrictions data before encountering the termination tag
     */
    private void readRestrictions(XMLEventReader eventReader, InMemoryRepository data) throws XMLStreamException, ParsingException {
        Constraint constraint = null;
        int id = 0; // Consists of the index of the restriction read from the file

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case DISTRIBUTION_TAG:
                            String restricType = getAttributeValue(startElement, "type");
                            String restricRequired = getAttributeValue(startElement, "required");
                            String restricPenalty = getAttributeValue(startElement, "penalty");

                            boolean restrictionRequired = Boolean.parseBoolean(restricRequired);
                            Integer restrictionPenalty = restricPenalty != null ? Integer.parseInt(restricPenalty) : null;

                            if(restricType == null) {
                                throw new ParsingException(event.getLocation(), "The restriction specified has no type");
                            }

                            try {
                                constraint = ConstraintFactory.createConstraint(id++, restricType, restrictionPenalty, restrictionRequired, data.getTimetableConfiguration());

                                data.addConstraint(constraint);
                            } catch (Exception e) {
                                throw new ParsingException(event.getLocation(), e.getMessage());
                            }
                            break;
                        case CLASS_TAG:
                            if(constraint == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a class tag before a distribution tag");
                            }
                            String classId = getAttributeValue(startElement, "id");

                            constraint.addClassUnitId(classId);

                            ClassUnit cls = data.getClassUnit(classId);
                            if(cls == null) {
                                throw new ParsingException(event.getLocation(), "The class id provided doesn't exist");
                            }
                            cls.addConstraint(constraint);

                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    switch(endElementName){
                        case DISTRIBUTION_TAG:
                            // If the end of the tag of a distribution is found
                            // its necessary to add said distribution to the data

                            constraint = null;

                            break;
                        case DISTRIBUTIONS_TAG:
                            // If the distributions tag terminator is found the function ends
                            return;
                    }

                    break;
            }
        }
    }

    /**
     * Auxiliary method that returns the value of the provided attribute. If the attribute isn't found the return is null.
     * @param element Tag to which the attribute value will be extracted if possible
     * @param attrName Name of the attribute to be extracted
     * @return Value of the attribute provided or null if the attribute isn't found.
     */
    private String getAttributeValue(StartElement element, String attrName) {
        Attribute attr = element.getAttributeByName(QName.valueOf(attrName));

        return attr != null ? attr.getValue() : null;
    }
}
