package tese.implementacoes;

import tese.estruturas.DadosAgendamento;
import tese.interfaces.LeitorFicheiros;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class InterpretadorITC implements LeitorFicheiros {

    // Estados possíveis na máquina de estados da leitura do ficheiro
    private enum EstadoLeitura{
        Inicio,
        Salas,
        Cursos,
        Restricoes,
        Professores
    }

    @Override
    public DadosAgendamento lerFicheiro(String caminhoFicheiro) {
        EstadoLeitura estadoLeitura = EstadoLeitura.Inicio;

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
            } catch (XMLStreamException e) {
                // TODO: Alterar para mostrar uma mensagem de erro
                throw new RuntimeException(e);
            }

            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement startElement = event.asStartElement();
                    String qName = startElement.getName().getLocalPart();

                    switch (qName) {
                        case "optimization":
                            // TODO: Guardar valores de otimização
                            break;
                        case "rooms":
                            estadoLeitura = EstadoLeitura.Salas;
                            break;
                        case "distributions":
                            estadoLeitura = EstadoLeitura.Restricoes;
                            break;
                        case "courses":
                            estadoLeitura = EstadoLeitura.Cursos;
                            break;
                        case "teachers":
                            estadoLeitura = EstadoLeitura.Professores;
                            break;
                        case "room":
                            break;
                        case "distribution":
                            if(estadoLeitura != EstadoLeitura.Restricoes) {
                                // TODO: Erro na leitura !?
                                continue;
                            }
                            break;
                        case "teacher":
                            if(estadoLeitura != EstadoLeitura.Professores) {
                                // TODO: Erro na leitura !?
                                continue;
                            }
                            break;
                        case "travel":
                            break;
                        case "unavailable":
                            break;
                        case "time":
                            break;
                        case "course":
                            if(estadoLeitura != EstadoLeitura.Cursos) {
                                // TODO: Erro na leitura !?
                                continue;
                            }
                            break;
                        case "config":
                            if(estadoLeitura != EstadoLeitura.Cursos) {
                                // TODO: Erro na leitura !?
                                continue;
                            }
                            break;
                        case "subpart":
                            if(estadoLeitura != EstadoLeitura.Cursos) {
                                // TODO: Erro na leitura !?
                                continue;
                            }
                            break;
                        case "class":
                            break;
                    }

                    System.out.println("qName: " + qName);
                    System.out.println("Estado de leitura: " + estadoLeitura);

                    break;
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
}
