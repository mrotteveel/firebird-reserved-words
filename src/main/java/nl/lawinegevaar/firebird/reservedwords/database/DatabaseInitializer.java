package nl.lawinegevaar.firebird.reservedwords.database;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DatabaseInitializer {

    private static final int LATEST_VERSION = 3;
    private final DatabaseInfo databaseInfo;

    public DatabaseInitializer(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }

    public void initializeDatabase() {
        createDatabase();

        try (Connection connection = databaseInfo.getDataSource().getConnection()) {
            migrateDatabase(connection);
        } catch (SQLException e) {
            throw new DatabaseInitializationFailureException("Unable to connect to database", e);
        }
    }

    private void migrateDatabase(Connection connection) {
        try {
            int dbVersion = findDatabaseVersion(connection);
            log.info("Found database version: {}", dbVersion);
            connection.setAutoCommit(false);

            switch (dbVersion) {
                case 0:
                    new MigrationStep(1, connection,
//@formatter:off
                            "alter database set default character set UTF8",
                            "commit",
                            "create table dbversion ( " +
                            "    version integer constraint pk_dbversion primary key, " +
                            "    migration_date timestamp default current_timestamp not null " +
                            ")",
                            // force commit so version update succeeds
                            "commit"
//@formatter:on
                    ).migrate();

                case 1:
                    new MigrationStep(2, connection,
//@formatter:off
                            "create table SQL_KEYWORD (" +
                            "    WORD varchar(30) not null, " +
                            "    SQL_VERSION smallint not null, " +
                            "    RESERVED boolean not null, " +
                            "    constraint PK_SQL_KEYWORD primary key (WORD, SQL_VERSION)" +
                            ")",
                            "comment on column SQL_KEYWORD.RESERVED is 'true: reserved keyword, false: non-reserved keyword'",
                            "create index IDX_SQL_KEYWORD_VERSION_WORD on SQL_KEYWORD (SQL_VERSION, WORD)",
                            "create table FB_KEYWORD (" +
                            "    WORD varchar(30) not null, " +
                            "    FB_VERSION NUMERIC(2,1) not null, " +
                            "    RESERVED boolean, " +
                            "    constraint PK_FB_KEYWORD primary key (WORD, FB_VERSION)" +
                            ")",
                            "comment on column FB_KEYWORD.RESERVED is 'true: reserved keyword, false: non-reserved keyword'",
                            "create index IDX_FB_KEYWORD_VERSION_WORD on FB_KEYWORD (FB_VERSION, WORD)"
//@formatter:on
                    ).migrate();
                case 2:
                    new MigrationStep(3, connection,
                            "alter table SQL_KEYWORD drop constraint PK_SQL_KEYWORD",
                            "alter table SQL_KEYWORD alter WORD type varchar(50)",
                            "alter table SQL_KEYWORD add constraint PK_SQL_KEYWORD primary key (WORD, SQL_VERSION)",
                            "alter table FB_KEYWORD drop constraint PK_FB_KEYWORD",
                            "alter table FB_KEYWORD alter WORD type varchar(50)",
                            "alter table FB_KEYWORD add constraint PK_FB_KEYWORD primary key (WORD, FB_VERSION)"
                    ).migrate();
                case LATEST_VERSION:
                    // current nothing to do
                    break;
                default:
                    log.warn("Unknown or unexpected database version: {}", dbVersion);
            }

        } catch (SQLException e) {
            throw new DatabaseInitializationFailureException("Failure to migrate database", e);
        }
    }

    private int findDatabaseVersion(Connection connection) {
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet rs = metadata.getTables(null, null, "DBVERSION", null)) {
                if (!rs.next()) {
                    return 0;
                }
            }
            // DBVERSION exists
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("select max(VERSION) as VERSION from DBVERSION")) {
                if (rs.next()) {
                    return rs.getInt("VERSION");
                } else {
                    throw new DatabaseInitializationFailureException("No versions in DBVERSION");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseInitializationFailureException("Unable to determine database version", e);
        }
    }

    private void createDatabase() {
        try {
            databaseInfo.createDatabaseIfNecessary();
        } catch (SQLException e) {
            throw new DatabaseInitializationFailureException("Failed to create database", e);
        }
    }

    @Slf4j
    static class MigrationStep {

        private final int version;
        private final Connection connection;
        private final String[] statements;

        MigrationStep(int version, Connection connection, String... statements) {
            this.version = version;
            this.connection = connection;
            this.statements = statements;
        }

        void migrate() {
            log.info("Migrating to version {}", version);
            try (Statement statement = connection.createStatement()) {
                for (String statementString : statements) {
                    // Special consideration to force commits, will break transactionality of migration
                    if (statementString.equalsIgnoreCase("commit")) {
                        connection.commit();
                        continue;
                    }
                    statement.execute(statementString);
                }

                try (PreparedStatement pstmt = connection.prepareStatement(
                        "insert into dbversion(version, migration_date) values (?, current_timestamp)")) {
                    pstmt.setInt(1, version);
                    pstmt.execute();
                }

                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (SQLException e2) {
                    log.error("Failed to rollback step {}", version, e2);
                }
                throw new DatabaseInitializationFailureException("Failed in step " + version, e);
            }
            log.info("Migration to version {} completed", version);
        }
    }
}

