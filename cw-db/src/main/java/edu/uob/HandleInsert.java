package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HandleInsert implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        if (tokens.length < 4 || !tokens[1].equalsIgnoreCase("INTO") || !tokens[3].equalsIgnoreCase("VALUES")) {
            return "[ERROR] Invalid INSERT syntax.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[2])) {
            return "[ERROR] '" + tokens[2] + "' is a reserved keyword and cannot be used as a name.";
        }

        String tableName = tokens[2].toLowerCase();
        String normalizedCommand = command.replaceAll("(?i)VALUES","VALUES");
        String valuesPart = normalizedCommand.substring(normalizedCommand.indexOf("VALUES") + 7).trim();

        return insertIntoTable(databaseState, tableName, valuesPart);
    }

    private String insertIntoTable(DatabaseState databaseState, String tableName, String valuesPart) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        // Convert table name to lowercase
        tableName = tableName.toLowerCase();

        // Define the table file path
        File tableFile = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase() + File.separator + tableName + ".tab");
        File tableFileMaxID = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase() + File.separator + "maxid.tab");

        // Check if the table exists
        if (!tableFile.exists()) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        List<String> columns;
        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine(); // Read column names
            if (header == null) {
                return "[ERROR] Table '" + tableName + "' has no structure.";
            }
            columns = Arrays.asList(header.split("\t"));
        } catch (IOException e) {
            return "[ERROR] Failed to read table: " + e.getMessage();
        }

        // Parse values from VALUES(...)
        if (!valuesPart.startsWith("(") || !valuesPart.endsWith(")")) {
            return "[ERROR] Invalid INSERT syntax. Expected VALUES(...).";
        }

        valuesPart = valuesPart.substring(1, valuesPart.length() - 1).trim(); // Remove parentheses
        String[] values = valuesPart.split(",");

        // Ensure correct number of values (excluding the 'id' column)
        if (values.length != columns.size() - 1) {
            return "[ERROR] Incorrect number of values. Expected " + (columns.size() - 1) + " but got " + values.length + ".";
        }

        for (String value : values) {
            value = value.trim();
            if (!isValidValue(value)) {
                return "[ERROR] Invalid value format: " + value;
            }
        }

        // Clean values and ensure they are valid
        List<String> cleanedValues = new ArrayList<>();
        for (String value : values) {
            cleanedValues.add(value.trim());
        }

        // Generate unique ID for new row
        int newId = generateNewId(databaseState, tableFileMaxID, tableName);

        // Create new row
        String newRow = newId + "\t" + String.join("\t", cleanedValues);

        // Append to the table file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile, true))) {
            writer.write(newRow);
            writer.newLine();
        } catch (IOException e) {
            return "[ERROR] Failed to insert data: " + e.getMessage();
        }

        return "[OK] 1 row inserted.";
    }

    private int generateNewId(DatabaseState databaseState, File tableFileMaxID, String tableName) {
        int maxId = 0;
        CommandHandler findCurrentMaxID= new HandleSelect();
        CommandHandler updateMaxID = new HandleUpdate();

        try {
            String commandSelectMatchTable = "SELECT maxid FROM maxid WHERE tablename == '" + tableName + "'";
            String[] tokensSelectMatchTable = commandSelectMatchTable.split("\\s+");
            String id = findCurrentMaxID.execute(databaseState, tokensSelectMatchTable, commandSelectMatchTable);
            int currentmaxId = Integer.parseInt(id.substring(id.indexOf("id") + 3));
            maxId = currentmaxId;

            String commandUpdateMaxID = "UPDATE maxid SET maxid = " + (currentmaxId + 1) + " WHERE tablename == '" + tableName + "'";
            String[] tokensUpdateMaxID = commandUpdateMaxID.split("\\s+");
            updateMaxID.execute(databaseState, tokensUpdateMaxID, commandUpdateMaxID);
        } catch (Exception e) {
            return 1; // Default ID if file cannot be read
        }
        return maxId + 1;

    }

    private boolean isValidValue(String value) {
        return value.equals("TRUE") ||
                value.equals("FALSE") ||
                value.equals("NULL") ||
                (value.startsWith("'") && value.endsWith("'")) ||
                value.matches("-?\\d+(\\.\\d+)?");
    }
}
