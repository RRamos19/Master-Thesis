package thesis;

import org.hibernate.Session;
import org.hibernate.Transaction;
import thesis.model.dbms.HibernateUtil;
import thesis.model.entities.Room;
import thesis.model.entities.RoomUnavailability;

public class Main {
    public static void main(String[] args) {
        //InputFileReader<StructuredTimetableData> inputFileReader = new ITCFormatParser();

        //GraphicalInterface graphicalManager = new InterfaceGraficaSwing("Ferramenta para Geração de Horários");
        //IGJavaFX testeIG = new IGJavaFX();
        //testeIG.instantiateGUI("Ferramenta para Geração de Horários");

        HibernateUtil.init("timetabling_db", "localhost", "5432", "postgres", "123");
        //DBTimetableRepository timetableRepository = new DBTimetableRepository("timetabling_db");

        Room room = new Room("teste");
        RoomUnavailability roomUnavailability = new RoomUnavailability(room, "0100000", "1111111111111111", 10, 50);
        room.addRoomUnavailability(roomUnavailability);

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(room);
            session.persist(roomUnavailability);
            tx.commit();
        }
    }
}
