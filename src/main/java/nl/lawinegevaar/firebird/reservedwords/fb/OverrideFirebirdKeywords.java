package nl.lawinegevaar.firebird.reservedwords.fb;

import lombok.extern.slf4j.Slf4j;
import nl.lawinegevaar.firebird.reservedwords.KeywordLoader;
import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
final class OverrideFirebirdKeywords implements KeywordLoader {

    private final BigDecimal firebirdVersion;
    private final String keywordsFilePath;
    private final boolean markAsReserved;

    OverrideFirebirdKeywords(BigDecimal firebirdVersion, String keywordsFilePath, boolean markAsReserved) {
        this.firebirdVersion = firebirdVersion;
        this.keywordsFilePath = keywordsFilePath;
        this.markAsReserved = markAsReserved;
    }

    @Override
    public void loadKeywords(DataSource dataSource) throws KeywordProcessingException {
        log.info("Merging keywords for Firebird {} from file '{}' as {}", firebirdVersion, keywordsFilePath,
                markAsReserved ? "reserved" : "non-reserved");
        new MergeFirebirdKeywords(this::getKeywordStream).loadKeywords(dataSource);
    }

    @SuppressWarnings("resource")
    private Stream<FirebirdKeyword> getKeywordStream() throws KeywordProcessingException {
        try {
            return Files.lines(Path.of(keywordsFilePath), StandardCharsets.ISO_8859_1)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(this::toFirebirdKeyword);
        } catch (IOException e) {
            throw new KeywordProcessingException("Keywords file not found or other IO error", e);
        }
    }

    private FirebirdKeyword toFirebirdKeyword(String keywordString) {
        return new FirebirdKeyword(keywordString, firebirdVersion, markAsReserved);
    }
}
