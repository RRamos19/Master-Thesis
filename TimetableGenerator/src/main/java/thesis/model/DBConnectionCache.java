package thesis.model;

import thesis.model.domain.InMemoryRepository;
import thesis.model.exceptions.DatabaseException;
import thesis.model.persistence.DBHibernateManager;
import thesis.model.persistence.DBManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBConnectionCache {
    private static final String DB_NAME = "timetabling_db";
    private final Map<List<String>, DBManager<InMemoryRepository>> dbManagerCacheMap = new HashMap<>();

    public DBManager<InMemoryRepository> connectToDatabase(String ip, String port, String userName, String password) throws DatabaseException {
        List<String> key = List.of(ip, port);
        if (dbManagerCacheMap.containsKey(key)) return dbManagerCacheMap.get(key);

        DBManager<InMemoryRepository> dbManager = new DBHibernateManager(DB_NAME, ip, port, userName, password);
        dbManagerCacheMap.put(key, dbManager);

        return dbManager;
    }

    public void clearCache(String ip, String port) {
        dbManagerCacheMap.remove(List.of(ip, port));
    }

    public void clearCache() {
        dbManagerCacheMap.clear();
    }
}
