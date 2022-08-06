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
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
final class FirebirdKeywordsFromSource implements KeywordLoader {

    private final BigDecimal firebirdVersion;
    private final String keywordsFilePath;

    FirebirdKeywordsFromSource(BigDecimal firebirdVersion, String keywordsFilePath) {
        this.firebirdVersion = firebirdVersion;
        this.keywordsFilePath = keywordsFilePath;
    }

    @Override
    public void loadKeywords(DataSource dataSource) throws KeywordProcessingException {
        log.info("Merging keywords from Firebird {} source file '{}'", firebirdVersion, keywordsFilePath);
        new MergeFirebirdKeywords(this::getKeywordStream).loadKeywords(dataSource);
    }

    @SuppressWarnings("resource")
    private Stream<FirebirdKeyword> getKeywordStream() throws KeywordProcessingException {
        try {
            return Files.lines(Path.of(keywordsFilePath), StandardCharsets.ISO_8859_1)
                    .map(getKeywordFunction())
                    .filter(Objects::nonNull);
        } catch (IOException e) {
            throw new KeywordProcessingException("Keywords file not found or other IO error", e);
        }
    }

    private Function<String, FirebirdKeyword> getKeywordFunction() {
        if (new BigDecimal("1.5").compareTo(firebirdVersion) >= 0) {
            return new Firebird15KeywordParser();
        } else if (new BigDecimal("4.0").compareTo(firebirdVersion) <= 0) {
            return new Firebird40KeywordParser();
        } else {
            return new Firebird20_30KeywordParser();
        }
    }

    private class Firebird15KeywordParser implements Function<String, FirebirdKeyword> {

        // eg {BASENAME, "BASE_NAME", 1},
        private final Pattern KEYWORD_PATTERN = Pattern.compile("\\{[^,]+,\\s*\"([^\"]+)\",\\s*[12]}");

        @Override
        public FirebirdKeyword apply(String s) {
            Matcher matcher = KEYWORD_PATTERN.matcher(s);
            if (matcher.find()) {
                String word = matcher.group(1);
                return new FirebirdKeyword(word, firebirdVersion, true);
            }
            return null;
        }
    }

    private class Firebird20_30KeywordParser implements Function<String, FirebirdKeyword> {

        // eg {ABS, "ABS", 2, false},
        private final Pattern KEYWORD_PATTERN = Pattern.compile(
                "\\{[^,]+,\\s*\"([^\"]+)\",\\s*[12],\\s*(\\w+)}");

        @Override
        public FirebirdKeyword apply(String s) {
            Matcher matcher = KEYWORD_PATTERN.matcher(s);
            if (matcher.find()) {
                String word = matcher.group(1);
                boolean reserved = !Boolean.parseBoolean(matcher.group(2));
                return new FirebirdKeyword(word, firebirdVersion, reserved);
            }
            return null;
        }
    }

    private class Firebird40KeywordParser implements Function<String, FirebirdKeyword> {

        // eg {TOK_ABS, "ABS", true},
        private final Pattern KEYWORD_PATTERN = Pattern.compile(
                "\\{[^,]+,\\s*\"([^\"]+)\",\\s*(true|false)}");

        @Override
        public FirebirdKeyword apply(String s) {
            Matcher matcher = KEYWORD_PATTERN.matcher(s);
            if (matcher.find()) {
                String word = matcher.group(1);
                boolean reserved = !Boolean.parseBoolean(matcher.group(2));
                return new FirebirdKeyword(word, firebirdVersion, reserved);
            }
            return null;
        }
    }


}
