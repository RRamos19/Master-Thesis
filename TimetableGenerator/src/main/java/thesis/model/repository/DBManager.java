package thesis.model.repository;

public interface DBManager<T> {
    /**
     * Fetches all the data present in the database
     * @return An aggregation of the data present in the database
     */
    T fetchData();

    /**
     * Inserts the data provided into the database. The insertion should be done in a transaction to avoid inconsistencies
     * @param data Class that contains all of the data to be inserted
     */
    void storeData(T data);
}
