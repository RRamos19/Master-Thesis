package thesis.model.parser;

import thesis.model.domain.*;
import thesis.model.domain.constraints.ConstraintFactory;
import thesis.model.domain.exceptions.CheckedIllegalArgumentException;
import thesis.model.domain.exceptions.ParsingException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ITCFormatParser implements InputFileReader<DomainModel> {

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

    // Configuration Tags
    private static final String TRAVEL_TAG            = "travel";
    private static final String UNAVAILABILITY_TAG    = "unavailable";
    private static final String CLASS_TAG             = "class";
    private static final String TIME_TAG              = "time";

    @Override
    public DomainModel readFile(String filePath) throws ParsingException {
        DomainModel data = new DomainModel();

        FileInputStream inputFile;
        InputStreamReader inputFileReader;

        try {
            inputFile = new FileInputStream(filePath);
            inputFileReader = new InputStreamReader(inputFile, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error, the file was not found");
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader eventReader;
        try {
            eventReader = factory.createXMLEventReader(inputFileReader);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Error while processing the XML file. Make sure the file is in UTF-8 format.");
        }

        while (eventReader.hasNext()) {
            XMLEvent event;
            try {
                event = eventReader.nextEvent();

                if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    StartElement startElement = event.asStartElement();
                    String qName = startElement.getName().getLocalPart();

                    switch (qName) {
                        case SOLUTION_TAG:
                            String solutionName = getAttributeValue(startElement, "name");

                            if (solutionName == null) {
                                throw new ParsingException(event.getLocation(), "A name must be specified in the solution tag");
                            }

                            data.setProblemName(solutionName);

                            readSolution(eventReader, data);
                            break;
                        case PROBLEM_TAG:
                            String programName = getAttributeValue(startElement, "name");
                            String nrDaysString = getAttributeValue(startElement, "nrDays");
                            String slotsPerDayString = getAttributeValue(startElement, "slotsPerDay");
                            String nrWeeksString = getAttributeValue(startElement, "nrWeeks");

                            byte nrDays = nrDaysString != null ? Byte.parseByte(nrDaysString) : 7;                  // Default 7 days
                            int slotsPerDay = slotsPerDayString != null ? Integer.parseInt(slotsPerDayString) : 16; // Default 1h:30m for each slot
                            int nrWeeks = nrWeeksString != null ? Integer.parseInt(nrWeeksString) : 9;              // Default 9 weeks

                            List<String> problemTagErrors = new ArrayList<>();
                            if (programName == null) problemTagErrors.add("name must be specified");
                            if (nrDays < 1) problemTagErrors.add("nrDays >= 1");
                            if (slotsPerDay <= 1) problemTagErrors.add("slotsPerDay > 1");
                            if (nrWeeks < 1) problemTagErrors.add("nrWeeks >= 1");
                            if (!problemTagErrors.isEmpty()) {
                                String errorMessage = "The following conditions must be followed: " + String.join(", ", problemTagErrors);
                                throw new ParsingException(event.getLocation(), errorMessage);
                            }

                            data.setProblemName(programName);

                            data.setConfiguration(nrDays, nrWeeks, slotsPerDay);
                            break;
                        case OPTIMIZATION_TAG:
                            String timeWeightString = getAttributeValue(startElement, "time");
                            String roomWeightString = getAttributeValue(startElement, "room");
                            String distributionWeightString = getAttributeValue(startElement, "distribution");

                            short timeWeight = timeWeightString != null ? Short.parseShort(timeWeightString) : 1;                         // Default 1
                            short roomWeight = roomWeightString != null ? Short.parseShort(roomWeightString) : 1;                         // Default 1
                            short distributionWeight = distributionWeightString != null ? Short.parseShort(distributionWeightString) : 1; // Default 1

                            List<String> optimizationTagErrors = new ArrayList<>();
                            if (timeWeight < 1) optimizationTagErrors.add("time >= 1");
                            if (roomWeight < 1) optimizationTagErrors.add("room >= 1");
                            if (distributionWeight < 1) optimizationTagErrors.add("distribution >= 1");
                            if (!optimizationTagErrors.isEmpty()) {
                                String errorMessage = "The following conditions must be followed: " + String.join(", ", optimizationTagErrors);
                                throw new ParsingException(event.getLocation(), errorMessage);
                            }

                            data.setOptimizationParameters(timeWeight, roomWeight, distributionWeight);
                            break;
                        case ROOMS_TAG:
                            readRooms(eventReader, data);
                            break;
                        case DISTRIBUTIONS_TAG:
                            readRestrictions(eventReader, data);
                            break;
                        case COURSES_TAG:
                            readCourses(eventReader, data);
                            break;
                        case TEACHERS_TAG:
                            readTeachers(eventReader, data);
                            break;
                    }
                }
            } catch (XMLStreamException e) {
                throw new ParsingException("Error while processing the XML file. Make sure the file is in UTF-8 format.");
            }
        }

        try {
            eventReader.close();
            inputFileReader.close();
            inputFile.close();
        } catch (XMLStreamException | IOException xml_e) {
            throw new ParsingException("Error while closing the file");
        }

        data.verifyValidity();

        return data;
    }

    /**
     * All the scheduled lessons should be read before encountering the termination tag
     */
    private void readSolution(XMLEventReader eventReader, DomainModel data) throws XMLStreamException, ParsingException {
        Timetable timetable = new Timetable(data.getProblemName());
        data.addTimetable(timetable);

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    if (startElementName.equals(CLASS_TAG)) {
                        String classId = getAttributeValue(startElement, "id");
                        String roomId = getAttributeValue(startElement, "room");
                        String days = getAttributeValue(startElement, "days");
                        String startString = getAttributeValue(startElement, "start");
                        String lengthString = getAttributeValue(startElement, "length");
                        String weeks = getAttributeValue(startElement, "weeks");

                        int start = startString != null ? Integer.parseInt(startString) : 0;
                        int length = lengthString != null ? Integer.parseInt(lengthString) : 0;

                        try {
                            ScheduledLesson scheduledLesson = new ScheduledLesson(data, classId, roomId, days, weeks, start, length);

                            timetable.addScheduledLesson(scheduledLesson);
                        } catch (CheckedIllegalArgumentException e) {
                            throw new ParsingException(event.getLocation(), e.getMessage());
                        }
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    if (endElementName.equals(SOLUTION_TAG)) {
                        // If the end of the tag solution is found the function ends
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * All the rooms should be read before encountering the termination tag
     */
    private void readRooms(XMLEventReader eventReader, DomainModel data) throws XMLStreamException, ParsingException {
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

                            room = new Room(roomName);
                            break;
                        case TRAVEL_TAG:
                            if(room == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a travel tag before a room tag");
                            }

                            String travelRoomId = getAttributeValue(startElement, "room");
                            String travelPenaltyString = getAttributeValue(startElement, "value");

                            int travelPenalty = travelPenaltyString != null ? Integer.parseInt(travelPenaltyString) : 0;

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

                            int start = startString != null ? Integer.parseInt(startString) : 0;
                            int length = lengthString != null ? Integer.parseInt(lengthString) : 0;

                            try {
                                room.addUnavailability(days, weeks, start, length);
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
     * All the courses should be read before encountering the termination tag
     */
    private void readCourses(XMLEventReader eventReader, DomainModel data) throws XMLStreamException, ParsingException {
        Course course = null;
        Config config = null;
        Subpart subpart = null;
        ClassUnit cls = null;

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

                            course = new Course(courseId);
                            data.addCourse(course);

                            break;
                        case CONFIG_TAG:
                            if(course == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a config tag before a course tag");
                            }

                            String configId = getAttributeValue(startElement, "id");

                            config = new Config(configId);
                            course.addConfig(config);
                            data.addConfig(config);

                            break;
                        case SUBPART_TAG:
                            if(config == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a subpart tag before a config tag");
                            }
                            String subpartId = getAttributeValue(startElement, "id");

                            subpart = new Subpart(subpartId);
                            config.addSubpart(subpart);
                            data.addSubpart(subpart);

                            break;
                        case CLASS_TAG:
                            if(subpart == null) {
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is a class tag before a subpart tag");
                            }
                            String classId = getAttributeValue(startElement, "id");
                            String parentClassId = getAttributeValue(startElement, "parent");

                            cls = new ClassUnit(data, classId);
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
                            String startString = getAttributeValue(startElement, "start");
                            String lengthString = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");
                            String timePenaltyString = getAttributeValue(startElement, "penalty");

                            int start = startString != null ? Integer.parseInt(startString) : 0;
                            int length = lengthString != null ? Integer.parseInt(lengthString) : 0;
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
     * All the teachers should be read before encountering the termination tag
     */
    private void readTeachers(XMLEventReader eventReader, DomainModel data) throws XMLStreamException, ParsingException {
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

                            break;
                        case UNAVAILABILITY_TAG:
                            if(teacher == null){
                                // Only happens if the file isn't structured correctly
                                throw new ParsingException(event.getLocation(), "There is an unavailability tag before a teacher tag");
                            }

                            String days = getAttributeValue(startElement,"days");
                            String startString = getAttributeValue(startElement, "start");
                            String lengthString = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");

                            int start = startString != null ? Integer.parseInt(startString) : 0;
                            int length = lengthString != null ? Integer.parseInt(lengthString) : 0;

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
     * All the restrictions should be read before encountering the termination tag
     */
    private void readRestrictions(XMLEventReader eventReader, DomainModel data) throws XMLStreamException, ParsingException {
        Constraint constraint = null;

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
                                constraint = ConstraintFactory.createConstraint(restricType, restrictionPenalty, restrictionRequired);

                                data.addConstraint(constraint);
                            } catch (Exception e) {
                                // TODO: fazer log do erro (provavelmente deve-se ignorar as restricoes que não são suportadas)
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
