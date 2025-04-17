package thesis.implementations;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import thesis.interfaces.InputFileReader;
import thesis.structures.*;
import thesis.structures.Class;

public class ITCFormatParser implements InputFileReader<StructuredTimetablingData> {

    // Principal Tags
    private static final String TEACHERS_TAG          = "teachers";
    private static final String COURSES_TAG           = "courses";
    private static final String ROOMS_TAG             = "rooms";
    private static final String DISTRIBUTIONS_TAG     = "distributions";
    private static final String OPTIMIZATION_TAG      = "optimization";
    private static final String PROBLEM_TAG           = "problem";

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
    public StructuredTimetablingData readFile(String filePath) {
        StructuredTimetablingData data = new StructuredTimetablingData();

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
                            int nrDays = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("nrDays")).getValue());
                            int slotsPerDay = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("slotsPerDay")).getValue());
                            int nrWeeks = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("nrWeeks")).getValue());

                            data.storeTimetableConfiguration(nrDays, slotsPerDay, nrWeeks);
                            break;
                        case OPTIMIZATION_TAG:
                            int timeWeight = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("time")).getValue());
                            int roomWeight = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("room")).getValue());
                            int distributionWeight = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("distribution")).getValue());

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
     * All the rooms should be read before encountering the termination tag
     */
    private void readRooms(XMLEventReader eventReader, StructuredTimetablingData data) throws XMLStreamException {
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
                            String roomId = startElement.getAttributeByName(QName.valueOf("id")).getValue();

                            room = new Room(roomId);
                            break;
                        case TRAVEL_TAG:
                            String travelRoomId = startElement.getAttributeByName(QName.valueOf("room")).getValue();
                            int travelPenalization = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("value")).getValue());

                            if(room == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a travel tag before a room tag in line " + event.getLocation().getLineNumber());
                            }

                            room.addTravel(travelRoomId, travelPenalization);
                            break;
                        case UNAVAILABILITY_TAG:
                            String days = startElement.getAttributeByName(QName.valueOf("days")).getValue();
                            int start = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("start")).getValue());
                            int length = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("length")).getValue());
                            String weeks = startElement.getAttributeByName(QName.valueOf("weeks")).getValue();

                            if(room == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is an unavailability tag before a room tag in line " + event.getLocation().getLineNumber());
                            }

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
    private void readCourses(XMLEventReader eventReader, StructuredTimetablingData data) throws XMLStreamException {
        Course course = null;
        Config config = null;
        Subpart subpart = null;
        Class cls = null;

        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case COURSE_TAG:
                            String courseId = startElement.getAttributeByName(QName.valueOf("id")).getValue();

                            course = new Course(courseId);
                            break;
                        case CONFIG_TAG:
                            String configId = startElement.getAttributeByName(QName.valueOf("id")).getValue();

                            config = new Config(configId);
                            break;
                        case SUBPART_TAG:
                            String subpartId = startElement.getAttributeByName(QName.valueOf("id")).getValue();

                            subpart = new Subpart(subpartId);
                            break;
                        case CLASS_TAG:
                            if(subpart == null) {
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a class tag that appears before a subpart tag in line " + event.getLocation().getLineNumber());
                            }
                            String classId = startElement.getAttributeByName(QName.valueOf("id")).getValue();

                            cls = new Class(classId);
                            subpart.addClass(cls);

                            break;
                        case ROOM_TAG:
                            if(cls == null) {
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a room tag that appears before a class tag in line " + event.getLocation().getLineNumber());
                            }
                            String roomId = startElement.getAttributeByName(QName.valueOf("id")).getValue();
                            int roomPenalty = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("penalty")).getValue());

                            cls.addRoom(roomId, roomPenalty);
                            break;
                        case TIME_TAG:
                            if(cls == null) {
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a time tag that appears before a class tag in line " + event.getLocation().getLineNumber());
                            }

                            String days = startElement.getAttributeByName(QName.valueOf("days")).getValue();
                            int start = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("start")).getValue());
                            int length = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("length")).getValue());
                            String weeks = startElement.getAttributeByName(QName.valueOf("weeks")).getValue();

                            Time time = new Time(days, start, length, weeks);
                            int timePenalty = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("penalty")).getValue());
                            
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
    private void readTeachers(XMLEventReader eventReader, StructuredTimetablingData data) throws XMLStreamException {
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
                            int teacherId = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("id")).getValue());
                            String teacherName = startElement.getAttributeByName(QName.valueOf("name")).getValue();

                            teacher = new Teacher(teacherId, teacherName);
                            break;
                        case UNAVAILABILITY_TAG:
                            if(teacher == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is an unavailability tag before a teacher tag in line " + event.getLocation().getLineNumber());
                            }

                            String days = startElement.getAttributeByName(QName.valueOf("days")).getValue();
                            int start = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("start")).getValue());
                            int length = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("length")).getValue());
                            String weeks = startElement.getAttributeByName(QName.valueOf("weeks")).getValue();

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
    private void readRestrictions(XMLEventReader eventReader, StructuredTimetablingData data) throws XMLStreamException {
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
                            String distType = startElement.getAttributeByName(QName.valueOf("type")).getValue();
                            Boolean distRequired = Boolean.valueOf(startElement.getAttributeByName(QName.valueOf("required")).getValue());

                            distribution = new Distribution(distType, distRequired);
                            break;
                        case CLASS_TAG:
                            if(distribution == null){
                                // Only happens if the file isn't structured correctly
                                throw new RuntimeException("There is a class tag before a distribution tag in line " + event.getLocation().getLineNumber());
                            }
                            int classId = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("id")).getValue());
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
}
