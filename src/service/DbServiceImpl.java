package service;

import app.TableController;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for work with database.
 * Implements DbService interface.
 */
public class DbServiceImpl implements DbService {
    private static DbServiceImpl dbServiceImpl;
    private ArrayList<String > addedColumns = new ArrayList<>();

    /** Default constructor. */
    public DbServiceImpl() {
    }

    /** Returns instance of current object. */
    public DbServiceImpl getInstance() {
        if (dbServiceImpl == null) {
            dbServiceImpl = new DbServiceImpl();
        }
        return dbServiceImpl;
    }

    /**
     * Returns list of all table names from database.
     *
     * @return ArrayList<String > of tables names.
     */
    @Override
    public ArrayList<String > getAllTablesNames() {
        ArrayList<String >  tablesNames = new ArrayList<>();
        try {
            Class.forName("org.h2.Driver");
            Connection connection = DriverManager.getConnection("jdbc:h2:~/test", "rsg", "");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES");
            while (resultSet.next()) {
                tablesNames.add(resultSet.getString("TABLE_NAME"));
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tablesNames;
    }

    /**
     * Write line from csv file to database in chosen table.
     *
     * @param tableName - String name of table.
     * @param lineOfCsvFile  - String line of csv file.
     */
    @Override
    public void writeLineToDb(final String tableName, final String lineOfCsvFile) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        String tableColumns = addedColumns.toString().replaceAll("\\[","").replaceAll("\\]","");
        String lineOfFile = lineOfCsvFile.replaceAll(",", ", ").substring(0, lineOfCsvFile.lastIndexOf(","));
        String [] dataFromLine = lineOfFile.split(",");
        String line = "";
        for (int i = 0; i < dataFromLine.length; i++) {
            if (dataFromLine[i].contains(".")) {
                line += "'" + dataFromLine[i].substring(0, dataFromLine[i].lastIndexOf(".")).trim() + "',";
            } else {
                line += "'" + dataFromLine[i].trim() + "',";
            }
        }
        line = line.substring(0, line.lastIndexOf(","));
        String sqlCommand = "INSERT INTO " + tableName +" ("+ tableColumns + ") VALUES (" + line + ")";
        SQLQuery query = session.createSQLQuery(sqlCommand);
        query.executeUpdate();
        session.getTransaction().commit();
    }

    /**
     * Returns list of headers from chosen table.
     *
     * @param tableName - String table name that chosen by user.
     *
     */
    public ArrayList<String > getTableHeaders(final String tableName) {
        ArrayList<String  > tableHeaders = new ArrayList<>();
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        String sqlCommand = "SHOW COLUMNS FROM " + tableName;
        SQLQuery query = session.createSQLQuery(sqlCommand);
        List<Object> result = query.list();
        for (Object i : result) {
            String name = null;
            if (i instanceof String)
                name = String.class.cast(i);
            if (Object[].class.cast(i)[0] instanceof String)
                if ((Object[].class.cast(i).length > 0))
                    if (i instanceof Object[]) {
                        name = String.class.cast(Object[].class.cast(i)[0]);
                    }
            tableHeaders.add(name);
        }
        session.getTransaction().commit();
        return tableHeaders;
    }

    /**
     * Rename chosen column in table in database.
     *
     * @param oldColumnName - String column name in database that will be renamed.
     * @param newColumnName - String new column name in text field.
     */
    public void renameColumnNameInDbTable(final String oldColumnName, final String newColumnName) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        String sqlCommand = "ALTER TABLE " + TableController.getTableName() + " ALTER " + oldColumnName + " RENAME TO " + newColumnName;
        SQLQuery query = session.createSQLQuery(sqlCommand);
        query.executeUpdate();
        session.getTransaction().commit();
    }

    /**
     * Add column to table in database.
     *
     * @param tableName - String table name that chosen by user.
     * @param columnName - String column name that is adding in chosen table.
     */
    public void addColumnToTable(final String tableName, final String columnName) {
        addedColumns.add(columnName);
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        String sqlCommand = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " VARCHAR (25) NULL";
        SQLQuery query = session.createSQLQuery(sqlCommand);
        query.executeUpdate();
        session.getTransaction().commit();
    }

    /**
     * Create table in database if it not exists.
     *
     * @param tableName - String table name that chosen by user.
     */
    public void createTable(final String tableName) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        String sqlCommand = "CREATE TABLE IF NOT EXISTS " + tableName + " (PrimaryKey INT NOT NULL AUTO_INCREMENT PRIMARY KEY)";
        SQLQuery query = session.createSQLQuery(sqlCommand);
        query.executeUpdate();
        session.getTransaction().commit();
    }
}
