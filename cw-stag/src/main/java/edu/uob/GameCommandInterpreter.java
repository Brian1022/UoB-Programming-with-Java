package edu.uob;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameCommandInterpreter {

    public static List<String> extractTokens(String command) {
        return Arrays.stream(command.toLowerCase().split("\\s+"))
                .collect(Collectors.toList());
    }

    public static String extractUsername(String rawInput) {
        int colonIndex = rawInput.indexOf(":");
        if (colonIndex == -1) return null;
        return rawInput.substring(0, colonIndex).trim();
    }

    public static String extractCommandBody(String rawInput) {
        int colonIndex = rawInput.indexOf(":");
        if (colonIndex == -1) return "";
        return rawInput.substring(colonIndex + 1).trim();
    }

    public static boolean isValidUsername(String username) {
        return username.matches("[A-Za-z\\-' ]+");
    }
}
