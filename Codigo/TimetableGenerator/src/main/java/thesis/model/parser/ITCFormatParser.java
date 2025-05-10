package thesis.model.parser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import thesis.model.entities.*;

public class ITCFormatParser implements InputFileReader<StructuredTimetableData> {

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
    public StructuredTimetableData readFile(String filePath) {
        StructuredTimetableData data = new StructuredTimetableData();

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
                        case PROBLEM_TAG:
                            String nrDaysString = getAttributeValue(startElement, "nrDays");
                            String slotsPerDayString = getAttributeValue(startElement, "slotsPerDay");
                            String nrWeeksString = getAttributeValue(startElement, "nrWeeks");

                            int nrDays = nrDaysString != null ? Integer.parseInt(nrDaysString) : 0;
                            int slotsPerDay = slotsPerDayString != null ? Integer.parseInt(slotsPerDayString) : 0;
                            int nrWeeks = nrWeeksString != null ? Integer.parseInt(nrWeeksString) : 0;

                            data.storeConfiguration(nrDays, slotsPerDay, nrWeeks);
                            break;
                        case OPTIMIZATION_TAG:
                            String timeWeightString = getAttributeValue(startElement, "time");
                            String roomWeightString = getAttributeValue(startElement, "room");
                            String distributionWeightString = getAttributeValue(startElement, "distribution");

                            int timeWeight = timeWeightString != null ? Integer.parseInt(timeWeightString) : 0;
                            int roomWeight = roomWeightString != null ? Integer.parseInt(roomWeightString) : 0;
                            int distributionWeight = distributionWeightString != null ? Integer.parseInt(distributionWeightString) : 0;

                            data.storeOptimization(timeWeight, roomWeight, distributionWeight);
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
                        case SOLUTION_TAG:
                            String solutionName = getAttributeValue(startElement, "name");

                            readSolution(eventReader, data, solutionName);
                            break;
                    }
                }
            } catch (XMLStreamException e) {
                throw new RuntimeException("Error while processing the XML file. Make sure the file is in UTF-8 format.");
            }
        }

        try {
            eventReader.close();
            inputFileReader.close();
            inputFile.close();
        } catch (XMLStreamException xml_e) {
            throw new RuntimeException("Error while closing the file");
        } catch (IOException io_e) {
            throw new RuntimeException("Error while closing the file");
        }

        return data;
    }

    /**
     * All the assigned classes should be read before encountering the termination tag
     */
    private void readSolution(XMLEventReader eventReader, StructuredTimetableData data, String solutionName) throws XMLStreamException {
        Timetable timetable = new Timetable(solutionName);

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case CLASS_TAG:
                            String classId = getAttributeValue(startElement, "id");
                            String roomId = getAttributeValue(startElement, "room");
                            String days = getAttributeValue(startElement, "days");
                            String startString = getAttributeValue(startElement, "start");
                            String lengthString = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");

                            int start = startString != null ? Integer.parseInt(startString) : 0;
                            int length = lengthString != null ? Integer.parseInt(lengthString) : 0;

                            timetable.storeAssignedClass(classId, roomId, days, start, length, weeks);
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    switch (endElementName){
                        case SOLUTION_TAG:
                            // If the end of the tag solution is found the function ends and the timetable is stored

                            data.storeTimetable(timetable);

                            return;
                    }
                    break;
            }
        }
    }

    /**
     * All the rooms should be read before encountering the termination tag
     */
    private void readRooms(XMLEventReader eventReader, StructuredTimetableData data) throws XMLStreamException {
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
                            String roomId = getAttributeValue(startElement, "id");

                            room = new Room(roomId);
                            break;
                        case TRAVEL_TAG:
                            if(room == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a travel tag before a room tag in line " + event.getLocation().getLineNumber());
                            }

                            String travelRoomId = getAttributeValue(startElement, "room");
                            String travelPenaltyString = getAttributeValue(startElement, "value");

                            int travelPenalty = travelPenaltyString != null ? Integer.parseInt(travelPenaltyString) : 0;

                            room.addTravel(travelRoomId, travelPenalty);
                            break;
                        case UNAVAILABILITY_TAG:
                            if(room == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is an unavailability tag before a room tag in line " + event.getLocation().getLineNumber());
                            }

                            String days = getAttributeValue(startElement, "days");
                            String startString = getAttributeValue(startElement, "start");
                            String lengthString = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");

                            int start = startString != null ? Integer.parseInt(startString) : 0;
                            int length = lengthString != null ? Integer.parseInt(lengthString) : 0;

                            Time unavail = new Time(days, start, length, weeks);
                            room.addUnavailability(unavail);
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
                                data.storeRoom(room);
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
    private void readCourses(XMLEventReader eventReader, StructuredTimetableData data) throws XMLStreamException {
        Course course = null;
        Config config = null;
        Subpart subpart = null;
        TimetableClass cls = null;

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
                            break;
                        case CONFIG_TAG:
                            String configId = getAttributeValue(startElement, "id");

                            config = new Config(configId);
                            break;
                        case SUBPART_TAG:
                            String subpartId = getAttributeValue(startElement, "id");

                            subpart = new Subpart(subpartId);
                            break;
                        case CLASS_TAG:
                            if(subpart == null) {
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a class tag that appears before a subpart tag in line " + event.getLocation().getLineNumber());
                            }
                            String classId = getAttributeValue(startElement, "id");

                            cls = new TimetableClass(classId);
                            subpart.addClass(cls);

                            break;
                        case ROOM_TAG:
                            if(cls == null) {
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a room tag that appears before a class tag in line " + event.getLocation().getLineNumber());
                            }
                            String roomId = getAttributeValue(startElement, "id");
                            String roomPenaltyString = getAttributeValue(startElement, "penalty");

                            int roomPenalty = roomPenaltyString != null ? Integer.parseInt(roomPenaltyString) : 0;

                            cls.addRoom(roomId, roomPenalty);
                            break;
                        case TIME_TAG:
                            if(cls == null) {
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a time tag that appears before a class tag in line " + event.getLocation().getLineNumber());
                            }

                            String days = getAttributeValue(startElement, "days");
                            String startString = getAttributeValue(startElement, "start");
                            String lengthString = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");
                            String timePenaltyString = getAttributeValue(startElement, "penalty");

                            int start = startString != null ? Integer.parseInt(startString) : 0;
                            int length = lengthString != null ? Integer.parseInt(lengthString) : 0;
                            int timePenalty = timePenaltyString != null ? Integer.parseInt(timePenaltyString) : 0;

                            Time time = new Time(days, start, length, weeks);

                            cls.addTime(time, timePenalty);
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

                            if(course != null) {
                                data.storeCourse(course);
                                course = null;
                            }

                            break;
                        case CONFIG_TAG:
                            // If the end of the tag of a config is found
                            // its necessary to add said config to the course

                            if(course != null && config != null) {
                                course.addConfig(config);
                                config = null;
                            }

                            break;
                        case SUBPART_TAG:
                            // If the end of the tag of a subpart is found
                            // its necessary to add said subpart to the config

                            if(config != null && subpart != null) {
                                config.addSubpart(subpart);
                                subpart = null;
                            }

                            break;
                        case CLASS_TAG:
                            // If the end of the tag of a class is found
                            // its necessary to add said class to the subpart

                            if(subpart != null && cls != null) {
                                subpart.addClass(cls);
                                cls = null;
                            }

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
    private void readTeachers(XMLEventReader eventReader, StructuredTimetableData data) throws XMLStreamException {
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
                            String teacherName = getAttributeValue(startElement,"name");;

                            int teacherId = teacherIdString != null ? Integer.parseInt(teacherIdString) : 0;

                            teacher = new Teacher(teacherId, teacherName);
                            break;
                        case UNAVAILABILITY_TAG:
                            if(teacher == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is an unavailability tag before a teacher tag in line " + event.getLocation().getLineNumber());
                            }

                            String days = getAttributeValue(startElement,"days");
                            String startString = getAttributeValue(startElement, "start");
                            String lengthString = getAttributeValue(startElement, "length");
                            String weeks = getAttributeValue(startElement, "weeks");

                            int start = startString != null ? Integer.parseInt(startString) : 0;
                            int length = lengthString != null ? Integer.parseInt(lengthString) : 0;

                            Time unavail = new Time(days, start, length, weeks);

                            teacher.addUnavailability(unavail);
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

                            if(teacher != null) {
                                data.storeTeacher(teacher);
                                teacher = null;
                            }

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
    private void readRestrictions(XMLEventReader eventReader, StructuredTimetableData data) throws XMLStreamException {
        Distribution distribution = null;

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case DISTRIBUTION_TAG:
                            String distType = getAttributeValue(startElement, "type");
                            String distRequired = getAttributeValue(startElement, "required");

                            distribution = new Distribution(distType, Boolean.parseBoolean(distRequired));
                            break;
                        case CLASS_TAG:
                            if(distribution == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a class tag before a distribution tag in line " + event.getLocation().getLineNumber());
                            }
                            String classId = getAttributeValue(startElement, "id");

                            distribution.addClassId(classId);
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

                            if(distribution != null){
                                data.storeDistribution(distribution);
                                distribution = null;
                            }

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
