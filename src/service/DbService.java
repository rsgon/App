package service;

import java.util.List;

/**
 * Interface for work with database.
 */
public interface DbService {
    List<String > getAllTablesNames();
    DbService getInstance();
    List<String > getTableHeaders(final String tableName);
    void writeLineToDb(final String tableName, final String lineDataOfCsvFile);
    void renameColumnNameInDbTable(final String oldColumnName, final String newColumnName);
    void addColumnToTable(final String tableName, final String columnName);
    void createTable(final String tableName);
}
