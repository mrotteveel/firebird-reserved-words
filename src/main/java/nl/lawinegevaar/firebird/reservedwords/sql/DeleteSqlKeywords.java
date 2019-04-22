package nl.lawinegevaar.firebird.reservedwords.sql;

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
final class DeleteSqlKeywords {

    private final int sqlVersion;
    private final String keywordsFilePath;

    DeleteSqlKeywords(int sqlVersion, String keywordsFilePath) {
        this.sqlVersion = sqlVersion;
        this.keywordsFilePath = keywordsFilePath;
    }

    void deleteKeywords(DataSource dataSource) throws KeywordProcessingException {
        log.info("Deleting keywords for SQL version {} from file '{}'", sqlVersion, keywordsFilePath);
        try (Stream<String> keywordStream = getKeywordStream();
             Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "delete from SQL_KEYWORD where WORD = ? and SQL_VERSION = ?")) {

                keywordStream.forEach(keyword -> {
                    try {
                        pstmt.setString(1, keyword);
                        pstmt.setInt(2, sqlVersion);
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
