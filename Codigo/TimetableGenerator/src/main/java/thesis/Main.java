package thesis;

import thesis.model.domain.DomainModel;
import thesis.model.persistence.EntityModel;
import thesis.model.repository.DBHibernateManager;
import thesis.model.repository.DBManager;
import thesis.model.repository.HibernateUtils;
import thesis.model.parser.ITCFormatParser;
import thesis.model.parser.InputFileReader;

public class Main {
    public static void main(String[] args) {
        InputFileReader<DomainModel> inputFileReader = new ITCFormatParser();

        HibernateUtils.init("timetabling_db", "localhost", "5432", "postgres", "123");
        DBManager<EntityModel> dbHibernateManager = new DBHibernateManager();

        EntityModel dBData = dbHibernateManager.fetchData();

        System.out.println(dBData);
        System.out.println();

        DomainModel data = inputFileReader.readFile("../lums-sum17.xml");

        System.out.println(data);

        dbHibernateManager.storeData(dBData);

        System.out.println("Saved in the database!");
    }
}
