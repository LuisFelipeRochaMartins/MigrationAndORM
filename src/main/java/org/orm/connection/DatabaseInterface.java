package org.orm.connection;

import java.sql.Connection;

public interface DatabaseInterface {

    Connection  connect();
}
