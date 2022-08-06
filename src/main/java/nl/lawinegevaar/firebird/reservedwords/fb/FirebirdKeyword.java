package nl.lawinegevaar.firebird.reservedwords.fb;

import java.math.BigDecimal;

record FirebirdKeyword(String word, BigDecimal firebirdVersion, boolean reserved) { }
