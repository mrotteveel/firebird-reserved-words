package nl.lawinegevaar.firebird.reservedwords.fb;

import nl.lawinegevaar.firebird.reservedwords.KeywordProcessingException;

import java.util.function.Supplier;
import java.util.stream.Stream;

final class MergeFirebirdKeywords extends AbstractFirebirdKeywordLoader {

    private final Supplier<Stream<FirebirdKeyword>> keywordStreamSupplier;

    MergeFirebirdKeywords(Supplier<Stream<FirebirdKeyword>> keywordStreamSupplier) {
        this.keywordStreamSupplier = keywordStreamSupplier;
    }

    @Override
    String getStatement() {
        return "merge into FB_KEYWORD " +
                "using ( " +
                "  select " +
                "    cast(? as varchar(50)) as WORD, " +
                "    cast(? as numeric(2,1)) as FB_VERSION, " +
                "    cast(? as boolean) as RESERVED " +
                "  from RDB$DATABASE " +
                ") as SRC " +
                "on FB_KEYWORD.WORD = SRC.WORD and FB_KEYWORD.FB_VERSION = SRC.FB_VERSION " +
                "when matched and FB_KEYWORD.RESERVED <> SRC.RESERVED then " +
                "  update set RESERVED = SRC.RESERVED " +
                "when not matched then " +
                "  insert (WORD, FB_VERSION, RESERVED) values (SRC.WORD, SRC.FB_VERSION, SRC.RESERVED)";
    }

    @Override
    Stream<FirebirdKeyword> getKeywordStream() throws KeywordProcessingException {
        return keywordStreamSupplier.get();
    }

}
