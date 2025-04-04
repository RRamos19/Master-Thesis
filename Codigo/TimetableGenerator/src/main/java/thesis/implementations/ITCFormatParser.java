package thesis.implementations;

import thesis.structures.TimetablingData;
import thesis.interfaces.InputFileReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ITCFormatParser implements InputFileReader {

    // Principal Tags
    private final String TEACHERS_TAG          = "teachers";
    private final String COURSES_TAG           = "courses";
    private final String ROOMS_TAG             = "rooms";
    private final String RESTRICTIONS_TAG      = "distributions";
    private final String OPTIMIZATION_TAG      = "optimization";

    // Secondary Tags
    private final String ROOM_TAG              = "room";
    private final String TEACHER_TAG           = "teacher";
    private final String COURSE_TAG            = "course";
    private final String CONFIG_TAG            = "config";
    private final String SUBPART_TAG           = "subpart";
    private final String RESTRICTION_TAG       = "distribution";

    // Configuration Tags
    private final String TRAVEL_TAG            = "travel";
    private final String UNAVAILABILITY_TAG    = "unavailable";
    private final String CLASS_TAG             = "class";
    private final String TIME_TAG              = "time";

    @Override
    public TimetablingData readFile(String filePath) {
        TimetablingData data = new TimetablingData();

        FileInputStream inputFile;
        InputStreamReader inputFileReader;

        try {
            inputFile = new FileInputStream(filePath);
            inputFileReader = new InputStreamReader(inputFile, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            // TODO: Alterar para mostrar uma mensagem de erro
            throw new RuntimeException(e);
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader eventReader;
        try {
            eventReader = factory.createXMLEventReader(inputFileReader);
        } catch (XMLStreamException e) {
            // TODO: Alterar para mostrar uma mensagem de erro
            throw new RuntimeException(e);
        }

        while (eventReader.hasNext()) {
            XMLEvent event;
            try {
                event = eventReader.nextEvent();

                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();

                        switch (qName) {
                            case OPTIMIZATION_TAG:
                                int timeWeight = 0, roomWeight = 0, distributionWeight = 0;

                                timeWeight = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("time")).getValue());
                                roomWeight = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("room")).getValue());
                                distributionWeight = Integer.parseInt(startElement.getAttributeByName(QName.valueOf("distribution")).getValue());

                                data.storeOptimization(timeWeight, roomWeight, distributionWeight);
                                break;
                            case ROOMS_TAG:
                                readRooms(eventReader);
                                break;
                            case RESTRICTIONS_TAG:
                                readRestrictions(eventReader);
                                break;
                            case COURSES_TAG:
                                readCourses(eventReader);
                                break;
                            case TEACHERS_TAG:
                                readTeachers(eventReader);
                                break;
                        }
                        break;
                }
            } catch (XMLStreamException e) {
                // TODO: Alterar para mostrar uma mensagem de erro
                throw new RuntimeException(e);
            }
        }

        try {
            eventReader.close();
            inputFileReader.close();
            inputFile.close();
        } catch (XMLStreamException xml_e) {
            // TODO: Alterar para mostrar uma mensagem de erro
            throw new RuntimeException(xml_e);
        } catch (IOException io_e) {
            // TODO: Alterar para mostrar uma mensagem de erro
            throw new RuntimeException(io_e);
        }

        return data;
    }

    /**
     * All the rooms should be read before encountering the termination tag
     * @param eventReader
     */
    private void readRooms(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case TRAVEL_TAG:
                            break;
                        case UNAVAILABILITY_TAG:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag das salas for encontrado a função termina
                    if (endElementName.equals(ROOMS_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * All the courses should be read before encountering the termination tag
     * @param eventReader
     */
    private void readCourses(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case COURSE_TAG:
                            break;
                        case CONFIG_TAG:
                            break;
                        case SUBPART_TAG:
                            break;
                        case CLASS_TAG:
                            break;
                        case ROOM_TAG:
                            break;
                        case TIME_TAG:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag dos cursos for encontrado a função termina
                    if (endElementName.equals(COURSES_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * All the teachers should be read before encountering the termination tag
     * @param eventReader
     */
    private void readTeachers(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case TEACHER_TAG:
                            break;
                        case UNAVAILABILITY_TAG:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag dos professores for encontrado a função termina
                    if (endElementName.equals(TEACHERS_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * All the restrictions should be read before encountering the termination tag
     * @param eventReader
     */
    private void readRestrictions(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case RESTRICTION_TAG:
                            break;
                        case CLASS_TAG:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag das restrições for encontrado a função termina
                    if (endElementName.equals(RESTRICTIONS_TAG)) {
                        return;
                    }
                    break;
            }
        }
    }
}
