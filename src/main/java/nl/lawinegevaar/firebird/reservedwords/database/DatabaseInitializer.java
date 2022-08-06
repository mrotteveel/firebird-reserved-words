package nl.lawinegevaar.firebird.reservedwords.database;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;

import java.sql.*;

@Slf4j
public class DatabaseInitializer {

    private final DatabaseInfo databaseInfo;

    public DatabaseInitializer(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }

    public void initializeDatabase() {
        createDatabase();

        Flyway flyway = Flyway.configure()
                .dataSource(databaseInfo.getDataSource())
                .locations("classpath:/db/migrations")
                .load();
        flyway.migrate();
    }

    private void createDatabase() {
        try {
            databaseInfo.createDatabaseIfNecessary();
        } catch (SQLException e) {
            throw new DatabaseInitializationFailureException("Failed to create database", e);
        }
    }

}

