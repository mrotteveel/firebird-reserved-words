package nl.lawinegevaar.firebird.reservedwords.sql;

import lombok.Value;

import java.math.BigDecimal;

@Value
class SqlKeyword {

    private String word;
    private int sqlVersion;
    private boolean reserved;
    
}
