package org.orm.connection;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class PostgresSQL implements DatabaseInterface {

    private static PostgresSQL postgresSQL;

    private PostgresSQL () {

    }

    public static synchronized PostgresSQL getInstance() {
        if (postgresSQL == null) {
            postgresSQL = new PostgresSQL();
        }
        return postgresSQL;
    }

    /**
     * Returns the Connection.
     *
     * @return Connection
     */
    public Connection connect() {
        try {
            return DriverManager.getConnection(getDatabaseURL(), getUser(), getPassword());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the environment variable for database URL
     *
     * @return String
     */
    private String getDatabaseURL() {
        return Dotenv.load().get("database_name");
    }

    /**
     * Get the environment variable for database User.
     *
     * @return String
     */
    private String getUser() {
        return Dotenv.load().get("database_user");
    }

    /**
     * Get the environment  variable for the database password
     *
     * @return String
     */
    private String getPassword() {
        return Dotenv.load().get("database_pswd");
    }

    public void executeQuery(String query) {
        try (Connection connection = connect()) {

            try(Statement statement = connection.createStatement()) {
                statement.execute(query);
                System.out.println(query);
            }
        } catch (SQLException e) {
            System.out.println(query);
            throw new RuntimeException(e);
        }
    }

    public String createTableIfNotExists(String query) {
        return DatabaseCommands.CREATE_TABLE_IF_NOT_EXISTS.query + query + ")";
    }

    public String dropTableIfExists(String tablename) {
        return DatabaseCommands.DROP_TABLE_IF_EXISTS.query + tablename;
    }

    public String alterTableAddPrimaryKeys(String tableName, String fields) {
        return DatabaseCommands.ALTER_TABLE.query + tableName +
                DatabaseCommands.ADD_CONSTRAINT.query + " PK_" + tableName +
                DatabaseCommands.PRIMARY_KEY.query + " (" + fields + ")";
    }

    public String save (String query) {
        return DatabaseCommands.INSERT_INTO.query + query;
    }

    public ResultSet getAllFromSql(String tableName) {
        try (Connection connection = connect()) {

            PreparedStatement statement = connection.prepareStatement(DatabaseCommands.SELECT.query + tableName);
            return statement.executeQuery();

        } catch (SQLException e) {
            System.out.println(DatabaseCommands.SELECT.query + tableName);
            throw new RuntimeException(e);
        }
    }

    public ResultSet getModelFromSql(String tableName, String[] fieldsName, Object[] values) {
        StringBuilder sb = new StringBuilder();

        sb.append(DatabaseCommands.SELECT.query);
        sb.append(tableName);

        sb.append(where(fieldsName, values));

        try (Connection connection = connect()) {
            return connection.prepareStatement(sb.toString()).executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a Where clause.
     * @param fieldsName String[]
     * @param values     Object[]
     * @return           String
     */
    public String where(String[] fieldsName, Object[] values) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < fieldsName.length; i++) {
            boolean addedWhere = false;
            if (i == 0 && values[0] != null) {
                sb.append(" WHERE ").append(fieldsName[0]).append(" = ").append(values[0]);
                addedWhere = true;
            }

            if (i != 0 && values[i] != null && !addedWhere) {
                sb.append(" WHERE ").append(fieldsName[i]).append(" = ").append(values[i]);
                addedWhere = true;
            }

            if (i != 0 && values[i] != null && addedWhere) {
                sb.append("\nAND ").append(fieldsName[i]).append(" = ").append(values[i]);
            }
        }
        return sb.toString();
    }

    public ResultSet existsByPrimaryKey(String tableName, String[] fieldNames, Object[] values) {
        StringBuilder sb = new StringBuilder();

        sb.append(DatabaseCommands.SELECT_COUNT.query).append(tableName);

        sb.append(where(fieldNames, values));

        try (Connection connection = connect()) {
            return connection.prepareStatement(sb.toString()).executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(String tableName, String[] fieldNames, Object[] values) {
        String sb = DatabaseCommands.DELETE.query + tableName +
                where(fieldNames, values);

        executeQuery(sb);
        return true;
    }

    public boolean update(String tableName, String[] fieldsToChange, Object[] values, String[] PKFields, Object[] PKValues) {
        StringBuilder sb = new StringBuilder();
        sb.append(DatabaseCommands.UPDATE.query).append(tableName);

        sb.append(set(fieldsToChange, values));
        sb.append(where(PKFields, PKValues));

        if (sb.toString().contains("SET")) {
            executeQuery(sb.toString());
            return true;
        }
        return false;
    }

    private String set(String[] fieldsToChange, Object[] values) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SET ");

        for (int i = 0; i < fieldsToChange.length; i++) {
            if (values[i] != null) {
                sb.append(fieldsToChange[i]).append(" = ").append(values[i]).append(", ");
            }
        }
        if (sb.toString().contains(",")) {
            int index = sb.lastIndexOf(",");
            sb.deleteCharAt(index);
        } else {
            return null;
        }
        return sb.toString();
    }
}
