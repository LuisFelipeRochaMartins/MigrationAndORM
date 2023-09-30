package org.example.connection;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
import io.github.cdimascio.dotenv.Dotenv;

import javax.xml.transform.Result;
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
            System.out.println(DatabaseCommands.SELECT.query + tableName);
            return statement.executeQuery();

        } catch (SQLException e) {
            System.out.println(DatabaseCommands.SELECT.query + tableName);
            throw new RuntimeException(e);
        }
    }

}
