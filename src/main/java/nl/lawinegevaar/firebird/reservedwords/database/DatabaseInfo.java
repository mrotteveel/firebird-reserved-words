package nl.lawinegevaar.firebird.reservedwords.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.PageSizeConstants;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Builder
@Getter(value = AccessLevel.PRIVATE)
@Slf4j
public class DatabaseInfo {

    @NonNull private final String hostname;
    private final int port;
    @NonNull private final String databaseName;
    @NonNull private final String user;
    @NonNull private final String password;
    @Getter(lazy = true, value = AccessLevel.PUBLIC)
    private final DataSource dataSource = initDataSource();

    /**
     * Creates the database if it doesn't already exist.
     *
     * @throws SQLException For failures to create the database
     */
    void createDatabaseIfNecessary() throws SQLException {
        try {
            FBManager fbManager = getFbManager();
            // No need for UTF8, as all we store is plain ASCII
            fbManager.setDefaultCharacterSet("win1252");
            // As forceCreate is false, will not do anything if db already exists
            fbManager.createDatabase(databaseName, user, password);
            fbManager.stop();
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            log.warn("ignored exception", e);
        }
    }

    private DataSource initDataSource() {
        var fbDataSource = new FBSimpleDataSource();
        fbDataSource.setDatabase(getDatabase());
        fbDataSource.setUserName(user);
        fbDataSource.setPassword(password);
        fbDataSource.setCharSet("utf-8");
        var config = new HikariConfig();
        config.setDataSource(fbDataSource);
        return new HikariDataSource(config);
    }

    private FBManager getFbManager() throws Exception {
        var fbManager = new FBManager();
        fbManager.setServer(hostname);
        fbManager.setPort(port != 0 ? port : 3050);
        fbManager.setPageSize(PageSizeConstants.SIZE_16K);
        fbManager.start();
        return fbManager;
    }

    private String getDatabase() {
        return String.format("//%s:%d/%s", hostname, (port != 0 ? port : 3050), databaseName);
    }

    public static DatabaseInfo createDatabaseInfo() {
        return createDatabaseInfo(readDatabaseConfiguration());
    }

    private static DatabaseInfo createDatabaseInfo(Properties properties) {
        return DatabaseInfo.builder()
                .hostname(properties.getProperty("db.hostname", "localhost"))
                .port(intValue(properties.getProperty("db.port"), 3050))
                .databaseName(properties.getProperty("db.databaseName", "fb_reservedwords.fdb"))
                .user(properties.getProperty("db.user", "sysdba"))
                .password(properties.getProperty("db.password", "masterkey"))
                .build();
    }

    private static int intValue(String intString, int defaultValue) {
        if (intString == null || intString.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Properties readDatabaseConfiguration() {
        try (InputStream is = DatabaseInfo.class.getResourceAsStream("/database.properties")) {
            var props = new Properties();
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to access database.properties", e);
        }
    }
}
