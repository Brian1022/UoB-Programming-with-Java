package edu.uob;

import java.util.Set;

public class SQLKeywords {
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "SELECT", "FROM", "WHERE", "AND", "OR", "INSERT", "INTO", "VALUES",
            "UPDATE", "SET", "DELETE", "CREATE", "TABLE", "DATABASE", "DROP",
            "ALTER", "JOIN", "ON", "USE", "LIKE", "TRUE", "FALSE", "NULL"
    );

    public static boolean isReservedKeyword(String word) {
        return RESERVED_KEYWORDS.contains(word.toUpperCase());
    }
}
