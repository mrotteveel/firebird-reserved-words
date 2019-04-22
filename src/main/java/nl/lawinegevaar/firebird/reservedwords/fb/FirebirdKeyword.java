package nl.lawinegevaar.firebird.reservedwords.fb;

import lombok.Value;

import java.math.BigDecimal;

@Value
class FirebirdKeyword {

    private String word;
    private BigDecimal firebirdVersion;
    private boolean reserved;
    
}
