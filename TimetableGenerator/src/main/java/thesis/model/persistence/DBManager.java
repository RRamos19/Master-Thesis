package thesis.model.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface DBManager<T> {
    /**
     * Fetches all the data present in the database
     * @return An aggregation of the data present in the database
     */
    Collection<T> fetchData(Map<String, T> programsToFetch);

    /**
     * Inserts the data provided into the database. The insertion should be done in a transaction to avoid inconsistencies
     * @param data Class that contains all the data to be inserted
     */
    void storeData(Map<String, T> data);

    void removeTimetable(String programName, UUID timetableId);

    void removeProgram(String programName);
}
