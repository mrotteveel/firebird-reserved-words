package nl.lawinegevaar.firebird.reservedwords.sql;

import lombok.extern.slf4j.Slf4j;
import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
final class ClearSqlKeywords {

    private final int sqlVersion;

    ClearSqlKeywords(int sqlVersion) {
        this.sqlVersion = sqlVersion;
    }

    void clearKeywords(DataSource dataSource) {
        log.info("Deleting all keywords for version {}", sqlVersion);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "delete from SQL_KEYWORD where SQL_VERSION = ?")) {
            preparedStatement.setInt(1, sqlVersion);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new KeywordProcessingException("Data-access exception", e);
        }
    }

}
