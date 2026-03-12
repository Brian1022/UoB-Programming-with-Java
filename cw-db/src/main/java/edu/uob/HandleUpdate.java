package edu.uob;

import java.io.*;
import java.util.*;

public class HandleUpdate implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected.";
        }

        if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("SET")) {
            return "[ERROR] Invalid UPDATE syntax.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[1])) {
            return "[ERROR] '" + tokens[1] + "' is a reserved keyword and cannot be used as a name.";
        }

        String normalizedCommand = command.replaceAll("(?i)UPDATE", "UPDATE")
                .replaceAll("(?i)SET", "SET")
                .replaceAll("(?i)WHERE", "WHERE");

        String tableName = tokens[1].toLowerCase();
        return updateTable(databaseState, tokens, normalizedCommand);
    }

    private String updateTable(DatabaseState databaseState, String[] tokens, String command) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("SET") || !command.contains("WHERE")) {
            return "[ERROR] Invalid UPDATE syntax. Expected: UPDATE <table> SET <column=value> WHERE <condition>;";
        }

        // Extract table name
        //String tableName = tokens[1].toLowerCase();
        String[] splitCommand = command.split("\\s+SET\\s+", 2);
        if (splitCommand.length < 2) {
            return "[ERROR] Invalid SELECT syntax.";
        }
        String tableName = splitCommand[0].replaceFirst("UPDATE", "").trim();
        String remainingPart = splitCommand[1];

        File tableFile = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase() + File.separator + tableName + ".tab");

        // Check if table exists
        if (!tableFile.exists()) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        List<String> tableData = new ArrayList<>();
        String header;

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            header = reader.readLine(); // Read column names
            if (header == null) {
                return "[ERROR] Table '" + tableName + "' is empty.";
            }
            tableData.add(header);

            String row;
            while ((row = reader.readLine()) != null) {
                tableData.add(row);
            }
        } catch (IOException e) {
            return "[ERROR] Failed to read table: " + e.getMessage();
        }

        // Parse column names
        List<String> columns = Arrays.asList(header.split("\t"));

        // Extract SET clause
        //String[] splitCommand = command.split("\\s+FROM\\s+", 2);
        String setClause;
        String whereCondition;
        String[] tableAndCondition = remainingPart.split("\\s+WHERE\\s+", 2);
        setClause = tableAndCondition[0].trim();
        whereCondition = tableAndCondition[1].trim();

        Map<String, String> updates = parseSetClause(setClause, columns);
        if (updates == null) {
            return "[ERROR] Invalid SET clause or Invalid value format.";
        }

        // Update matching rows
        int updateCount = 0;
        for (int i = 1; i < tableData.size(); i++) {
            String[] rowValues = tableData.get(i).split("\t");
            if (EvaluateCondition.evaluateCondition(whereCondition, columns, rowValues)) {
                for (Map.Entry<String, String> entry : updates.entrySet()) {
                    int colIndex = columns.indexOf(entry.getKey());
                    if (colIndex != -1) {
                        rowValues[colIndex] = entry.getValue();
                    }
                }
                tableData.set(i, String.join("\t", rowValues));
                updateCount++;
            }
        }

        // Write updated table back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            for (String line : tableData) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            return "[ERROR] Failed to update table: " + e.getMessage();
        }

        return "[OK] " + updateCount + " row(s) updated.";
    }

    private Map<String, String> parseSetClause(String setClause, List<String> columns) {
        Map<String, String> updates = new HashMap<>();

        String[] pairs = setClause.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=");
            if (keyValue.length != 2) {
                return null; // Invalid format
            }

            String columnName = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (!columns.contains(columnName)) {
                return null; // Invalid column
            }

            if (!isValidValue(value)) {
                return null;
            }

            updates.put(columnName, value);
        }

        return updates;
    }

    private boolean isValidValue(String value) {
        return value.equals("TRUE") ||
                value.equals("FALSE") ||
                value.equals("NULL") ||
                (value.startsWith("'") && value.endsWith("'")) ||
                value.matches("-?\\d+(\\.\\d+)?");
    }
}
