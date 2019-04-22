package nl.lawinegevaar.firebird.reservedwords;

import javax.sql.DataSource;

public interface KeywordLoader {

    void loadKeywords(DataSource dataSource) throws KeywordProcessingException;

}
