package nl.lawinegevaar.firebird.reservedwords.sql;

import lombok.extern.slf4j.Slf4j;
import nl.lawinegevaar.firebird.reservedwords.KeywordLoader;
import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
public class SqlKeywordsFromSource implements KeywordLoader {

    private final int sqlVersion;
    private final String keywordsFilePath;
    private final boolean markAsReserved;

    SqlKeywordsFromSource(int sqlVersion, String keywordsFilePath, boolean markAsReserved) {
        this.sqlVersion = sqlVersion;
        this.keywordsFilePath = keywordsFilePath;
        this.markAsReserved = markAsReserved;
    }

    @Override
    public void loadKeywords(DataSource dataSource) throws KeywordProcessingException {
        log.info("Merging keywords for SQL {} from file '{}' as {}", sqlVersion, keywordsFilePath,
                markAsReserved ? "reserved" : "non-reserved");
        new MergeSqlKeywords(this::getKeywordStream).loadKeywords(dataSource);
    }

    private Stream<SqlKeyword> getKeywordStream() throws KeywordProcessingException {
        try {
            return Files.lines(Path.of(keywordsFilePath), StandardCharsets.ISO_8859_1)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(this::toSqlKeyword);
        } catch (IOException e) {
            throw new KeywordProcessingException("Keywords file not found or other IO error", e);
        }
    }

    private SqlKeyword toSqlKeyword(String keywordString) {
        return new SqlKeyword(keywordString, sqlVersion, markAsReserved);
    }

}
