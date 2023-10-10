package org.example.connection;

public enum DatabaseCommands {
    
    SELECT(" SELECT * FROM "),
    CREATE_TABLE_IF_NOT_EXISTS(" CREATE TABLE IF NOT EXISTS "),
    DROP_TABLE_IF_EXISTS(" DROP TABLE IF EXISTS "),
    ALTER_TABLE(" ALTER TABLE "),
    ADD_CONSTRAINT(" ADD CONSTRAINT "),
    PRIMARY_KEY(" PRIMARY KEY "),
    INSERT_INTO(" INSERT INTO "),
    SELECT_COUNT(" SELECT COUNT(*) FROM "),
    DELETE(" DELETE FROM "),
    UPDATE(" UPDATE ");

    public final String query;


    private DatabaseCommands(String query) {
        this.query = query;
    }
}
