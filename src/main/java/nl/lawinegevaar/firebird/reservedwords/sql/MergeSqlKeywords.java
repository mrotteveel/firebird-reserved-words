package nl.lawinegevaar.firebird.reservedwords.sql;

import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import java.util.function.Supplier;
import java.util.stream.Stream;

final class MergeSqlKeywords extends AbstractSqlKeywordLoader {

    private final Supplier<Stream<SqlKeyword>> keywordStreamSupplier;

    MergeSqlKeywords(Supplier<Stream<SqlKeyword>> keywordStreamSupplier) {
        this.keywordStreamSupplier = keywordStreamSupplier;
    }

    @Override
    String getStatement() {
        return """
                merge into SQL_KEYWORD
                using (
                  select
                    cast(? as varchar(50)) as WORD,
                    cast(? as smallint) as SQL_VERSION,
                    cast(? as boolean) as RESERVED
                  from RDB$DATABASE
                ) as SRC
                on SQL_KEYWORD.WORD = SRC.WORD and SQL_KEYWORD.SQL_VERSION = SRC.SQL_VERSION
                when matched and SQL_KEYWORD.RESERVED <> SRC.RESERVED then
                  update set RESERVED = SRC.RESERVED
                when not matched then
                  insert (WORD, SQL_VERSION, RESERVED) values (SRC.WORD, SRC.SQL_VERSION, SRC.RESERVED)
                """;
    }

    @Override
    Stream<SqlKeyword> getKeywordStream() throws KeywordProcessingException {
        return keywordStreamSupplier.get();
    }

}
