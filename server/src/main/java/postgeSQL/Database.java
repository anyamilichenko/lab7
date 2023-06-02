package postgeSQL;

import org.apache.logging.log4j.core.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class for holding database tables for working with them
 */
public class Database {
    private final UsersTable usersTable;
    private final DragonTable dragonTable;
    private final Logger logger;

    public Database(Connection connection, Logger logger) {
        this.dragonTable = new DragonTable(connection);
        this.usersTable = new UsersTable(connection);
        this.logger = logger;

        try {
            initTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initTables() throws SQLException {
        dragonTable.init();
        usersTable.init();
    }

    public DragonTable getDragonTable() {
        return dragonTable;
    }

    public UsersTable getUsersTable() {
        return usersTable;
    }
}