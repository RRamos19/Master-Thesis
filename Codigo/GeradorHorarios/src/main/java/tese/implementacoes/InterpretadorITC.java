package tese.implementacoes;

import tese.estruturas.DadosAgendamento;
import tese.interfaces.LeitorFicheiros;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class InterpretadorITC implements LeitorFicheiros {

    // Tags principais
    private final String TAG_PROFESSORES       = "teachers";
    private final String TAG_CURSOS            = "courses";
    private final String TAG_SALAS             = "rooms";
    private final String TAG_RESTRICOES        = "distributions";
    private final String TAG_OTIMIZACAO        = "optimization";

    // Tags secundárias
    private final String TAG_SALA              = "room";
    private final String TAG_PROFESSOR         = "teacher";
    private final String TAG_CURSO             = "course";
    private final String TAG_CONFIG            = "config";
    private final String TAG_SUBPARTE          = "subpart";
    private final String TAG_RESTRICAO         = "distribution";

    // Tags de configuração
    private final String TAG_DISTANCIA         = "travel";
    private final String TAG_INDISPONIBILIDADE = "unavailable";
    private final String TAG_DISCIPLINA        = "class";
    private final String TAG_TEMPO             = "time";

    @Override
    public DadosAgendamento lerFicheiro(String caminhoFicheiro) {
        DadosAgendamento dados = new DadosAgendamento();

        FileInputStream ficheiro;
        InputStreamReader leitorFicheiro;

        try {
            ficheiro = new FileInputStream(caminhoFicheiro);
            leitorFicheiro = new InputStreamReader(ficheiro, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            // TODO: Alterar para mostrar uma mensagem de erro
            throw new RuntimeException(e);
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader eventReader;
        try {
            eventReader = factory.createXMLEventReader(leitorFicheiro);
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
                            case TAG_OTIMIZACAO:
                                // TODO: Guardar valores de otimização
                                break;
                            case TAG_SALAS:
                                lerSalas(eventReader);
                                break;
                            case TAG_RESTRICOES:
                                lerRestricoes(eventReader);
                                break;
                            case TAG_CURSOS:
                                lerCursos(eventReader);
                                break;
                            case TAG_PROFESSORES:
                                lerProfessores(eventReader);
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
            leitorFicheiro.close();
            ficheiro.close();
        } catch (XMLStreamException xml_e) {
            // TODO: Alterar para mostrar uma mensagem de erro
            throw new RuntimeException(xml_e);
        } catch (IOException io_e) {
            // TODO: Alterar para mostrar uma mensagem de erro
            throw new RuntimeException(io_e);
        }

        return dados;
    }

    /**
     * Deve ser feita a leitura de todas as salas até se encontrar o terminador da tag
     * @param eventReader
     */
    private void lerSalas(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case TAG_DISTANCIA:
                            break;
                        case TAG_INDISPONIBILIDADE:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag das salas for encontrado a função termina
                    if (endElementName.equals(TAG_SALAS)) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * Deve ser feita a leitura de todos os cursos até se encontrar o terminador da tag
     * @param eventReader
     */
    private void lerCursos(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case TAG_CURSO:
                            break;
                        case TAG_CONFIG:
                            break;
                        case TAG_SUBPARTE:
                            break;
                        case TAG_DISCIPLINA:
                            break;
                        case TAG_SALA:
                            break;
                        case TAG_TEMPO:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag dos cursos for encontrado a função termina
                    if (endElementName.equals(TAG_CURSOS)) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * Deve ser feita a leitura de todos os professores até se encontrar o terminador da tag
     * @param eventReader
     */
    private void lerProfessores(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case TAG_PROFESSOR:
                            break;
                        case TAG_INDISPONIBILIDADE:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag dos professores for encontrado a função termina
                    if (endElementName.equals(TAG_PROFESSORES)) {
                        return;
                    }
                    break;
            }
        }
    }

    /**
     * Deve ser feita a leitura de todas as restrições até se encontrar o terminador da tag
     * @param eventReader
     */
    private void lerRestricoes(XMLEventReader eventReader) throws XMLStreamException {
        while (eventReader.hasNext()) {
            XMLEvent event;
            event = eventReader.nextEvent();

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();

                    switch (startElementName) {
                        case TAG_RESTRICAO:
                            break;
                        case TAG_DISCIPLINA:
                            break;
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    EndElement endElement = event.asEndElement();
                    String endElementName = endElement.getName().getLocalPart();

                    // Se o terminador da tag das restrições for encontrado a função termina
                    if (endElementName.equals(TAG_RESTRICOES)) {
                        return;
                    }
                    break;
            }
        }
    }
}
