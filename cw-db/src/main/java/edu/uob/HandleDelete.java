package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleDelete implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected.";
        }

        if (tokens.length < 4 || !tokens[1].equalsIgnoreCase("FROM")) {
            return "[ERROR] Invalid DELETE syntax.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[2])) {
            return "[ERROR] '" + tokens[2] + "' is a reserved keyword and cannot be used as a name.";
        }

        String normalizedCommand = command.replaceAll("(?i)DELETE", "DELETE")
                .replaceAll("(?i)FROM", "FROM")
                .replaceAll("(?i)WHERE", "WHERE");

        String tableName = tokens[2].toLowerCase();
        return deleteFromTable(databaseState, tokens, normalizedCommand);
    }

    private String deleteFromTable(DatabaseState databaseState, String[] tokens, String command) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        if (tokens.length < 4 || !tokens[1].equalsIgnoreCase("FROM") || !command.contains("WHERE")) {
            return "[ERROR] Invalid DELETE syntax. Expected: DELETE FROM <table> WHERE <condition>;";
        }

        // Extract table name
        String tableName = tokens[2].toLowerCase();
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

        // Extract WHERE condition
        String whereCondition = command.substring(command.indexOf("WHERE") + 6).trim();

        // Delete matching rows
        int deleteCount = 0;
        List<String> updatedTableData = new ArrayList<>();
        updatedTableData.add(header); // Keep the header row

        for (int i = 1; i < tableData.size(); i++) {
            String[] rowValues = tableData.get(i).split("\t");
            if (!EvaluateCondition.evaluateCondition(whereCondition, columns, rowValues)) {
                updatedTableData.add(tableData.get(i)); // Keep rows that don't match the condition
            } else {
                deleteCount++; // Count deleted rows
            }
        }

        // Write updated table back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            for (String line : updatedTableData) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            return "[ERROR] Failed to update table: " + e.getMessage();
        }

        return "[OK] " + deleteCount + " row(s) deleted.";
    }
}
