package edu.uob;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HandleAlter implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        // Ensure a database is selected
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        // Validate command structure
        if (tokens.length != 5 || !tokens[1].equalsIgnoreCase("TABLE")) {
            return "[ERROR] Invalid ALTER TABLE syntax. Expected: ALTER TABLE <table> ADD/DROP <column>;";
        }

        if (SQLKeywords.isReservedKeyword(tokens[2])) {
            return "[ERROR] '" + tokens[2] + "' is a reserved keyword and cannot be used as a name.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[4])) {
            return "[ERROR] '" + tokens[4] + "' is a reserved keyword and cannot be used as a name.";
        }

        String tableName = tokens[2].toLowerCase();
        String alterationType = tokens[3].toUpperCase();
        String columnName = tokens[4];

        // Check if the table exists
        File tableFile = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase() + File.separator + tableName + ".tab");
        if (!tableFile.exists()) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        // Handle ALTER operation
        if (alterationType.equals("ADD")) {
            return addColumnToTable(tableFile, columnName);
        } else if (alterationType.equals("DROP")) {
            return dropColumnFromTable(tableFile, columnName);
        } else {
            return "[ERROR] Invalid ALTER type. Use ADD or DROP.";
        }
    }

    private String addColumnToTable(File tableFile, String newColumn) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            if (header == null) {
                return "[ERROR] Table structure invalid.";
            }

            // Check if column already exists
            String[] columns = header.split("\t");
            for (String col : columns) {
                if (col.equalsIgnoreCase(newColumn)) {
                    return "[ERROR] Column '" + newColumn + "' already exists.";
                }
            }

            // Append new column to the header
            header += "\t" + newColumn;
            lines.add(header);

            // Read and update each row (add empty value for new column)
            String row;
            while ((row = reader.readLine()) != null) {
                lines.add(row + "\t"); // Default value for new column
            }
        } catch (IOException e) {
            return "[ERROR] Failed to read table: " + e.getMessage();
        }

        // Write updated data back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            return "[ERROR] Failed to update table: " + e.getMessage();
        }

        return "[OK] Column '" + newColumn + "' added successfully.";
    }

    private String dropColumnFromTable(File tableFile, String columnToDrop) {
        List<String> lines = new ArrayList<>();
        int columnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            if (header == null) {
                return "[ERROR] Table structure invalid.";
            }

            // Find column index
            String[] columns = header.split("\t");
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equalsIgnoreCase(columnToDrop)) {
                    columnIndex = i;
                    break;
                }
            }

            // If column doesn't exist, return error
            if (columnIndex == -1) {
                return "[ERROR] Column '" + columnToDrop + "' does not exist.";
            }

            // Prevent dropping the 'id' column
            if (columns[columnIndex].equalsIgnoreCase("id")) {
                return "[ERROR] Cannot remove primary key column 'id'.";
            }

            // Remove column from header
            String newHeader = removeColumnFromRow(header, columnIndex);
            lines.add(newHeader);

            // Process each row
            String row;
            while ((row = reader.readLine()) != null) {
                lines.add(removeColumnFromRow(row, columnIndex));
            }
        } catch (IOException e) {
            return "[ERROR] Failed to read table: " + e.getMessage();
        }

        // Write updated data back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            return "[ERROR] Failed to update table: " + e.getMessage();
        }

        return "[OK] Column '" + columnToDrop + "' removed successfully.";
    }

    private String removeColumnFromRow(String row, int columnIndex) {
        String[] values = row.split("\t");
        List<String> newValues = new ArrayList<>();

        for (int i = 0; i < values.length; i++) {
            if (i != columnIndex) {
                newValues.add(values[i]);
            }
        }

        return String.join("\t", newValues);
    }
}
