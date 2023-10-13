package org.example.migration;

import java.util.HashMap;
import java.util.Map;

public class DatabaseTypes {
    public static final Map<String, String> types = new HashMap<>() {{
        put("String"   , "VARCHAR "     );
        put("Integer"  , "INT "         );
        put("Float"    , "FLOAT "       );
        put("Double"   , "DOUBLE "      );
        put("boolean"  , "BOOLEAN "     );
        put("LocalDate", "Date "        );
        put("Long"     , "BIGINT "      );
        put("Short"    , "SMALLINT "    );
    }};
}
