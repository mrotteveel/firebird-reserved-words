package nl.lawinegevaar.firebird.reservedwords.fb;

import lombok.extern.slf4j.Slf4j;
import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Stream;

@Slf4j
final class DeleteFirebirdKeywords {

    private final BigDecimal firebirdVersion;
    private final String keywordsFilePath;

    DeleteFirebirdKeywords(BigDecimal firebirdVersion, String keywordsFilePath) {
        this.firebirdVersion = firebirdVersion;
        this.keywordsFilePath = keywordsFilePath;
    }

    void deleteKeywords(DataSource dataSource) throws KeywordProcessingException {
        log.info("Deleting keywords for version {} from file '{}'", firebirdVersion, keywordsFilePath);
        try (Stream<String> keywordStream = getKeywordStream();
             Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "delete from FB_KEYWORD where WORD = ? and FB_VERSION = ?")) {

                keywordStream.forEach(keyword -> {
                    try {
                        pstmt.setString(1, keyword);
                        pstmt.setBigDecimal(2, firebirdVersion);
                        pstmt.addBatch();
                    } catch (SQLException e) {
                        throw new KeywordProcessingException("Could not delete keyword: " + keyword, e);
                    }
                });
                pstmt.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new KeywordProcessingException("Data-access exception", e);
        }
    }

    private Stream<String> getKeywordStream() throws KeywordProcessingException {
        try {
            return Files.lines(Path.of(keywordsFilePath), StandardCharsets.ISO_8859_1)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty());
        } catch (IOException e) {
            throw new KeywordProcessingException("Keywords file not found or other IO error", e);
        }
    }
}
