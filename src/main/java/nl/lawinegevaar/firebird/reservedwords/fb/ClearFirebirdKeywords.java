package nl.lawinegevaar.firebird.reservedwords.fb;

import lombok.extern.slf4j.Slf4j;
import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
final class ClearFirebirdKeywords {

    private final BigDecimal firebirdVersion;

    ClearFirebirdKeywords(BigDecimal firebirdVersion) {
        this.firebirdVersion = firebirdVersion;
    }

    void clearKeywords(DataSource dataSource) {
        log.info("Deleting all keywords for version {}", firebirdVersion);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "delete from FB_KEYWORD where FB_VERSION = ?")) {
            preparedStatement.setBigDecimal(1, firebirdVersion);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new KeywordProcessingException("Data-access exception", e);
        }
    }

}
