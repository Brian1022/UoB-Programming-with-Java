package edu.uob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleSelect implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected.";
        }

        // Convert entire command to uppercase for keyword parsing (keeps identifiers unchanged)**
        String normalizedCommand = command.replaceAll("(?i)SELECT", "SELECT")
                .replaceAll("(?i)FROM", "FROM")
                .replaceAll("(?i)WHERE", "WHERE");

        if (!normalizedCommand.contains("FROM")) {
            return "[ERROR] Invalid SELECT syntax: missing FROM.";
        }

        return selectFromTable(databaseState, normalizedCommand);
    }

    private String selectFromTable(DatabaseState databaseState, String command) {
        // Find `FROM` keyword correctly**
        String[] splitCommand = command.split("\\s+FROM\\s+", 2);
        if (splitCommand.length < 2) {
            return "[ERROR] Invalid SELECT syntax.";
        }

        String columnPart = splitCommand[0].replaceFirst("SELECT", "").trim();
        String remainingPart = splitCommand[1];

        // Handle WHERE clause properly**
        String tableName;
        String whereClause = null;
        if (remainingPart.contains(" WHERE ")) {
            String[] tableAndCondition = remainingPart.split("\\s+WHERE\\s+", 2);
            tableName = tableAndCondition[0].trim().toLowerCase();
            whereClause = tableAndCondition[1].trim();
        } else {
            tableName = remainingPart.trim().toLowerCase();
        }

        if (SQLKeywords.isReservedKeyword(tableName)) {
            return "[ERROR] '" + tableName + "' is a reserved keyword and cannot be used as a name.";
        }

        File tableFile = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase(), tableName + ".tab");

        if (!tableFile.exists()) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        List<String[]> tableData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tableData.add(line.split("\t"));
            }
        } catch (IOException e) {
            return "[ERROR] Failed to read table: " + e.getMessage();
        }

        if (tableData.isEmpty()) {
            return "[ERROR] Table '" + tableName + "' is empty.";
        }

        List<String> columns = Arrays.asList(tableData.get(0)); // Column headers
        List<Integer> selectedIndexes = new ArrayList<>();

        // Extract selected columns correctly**
        if (columnPart.equals("*")) {
            for (int i = 0; i < columns.size(); i++) {
                selectedIndexes.add(i);
            }
        } else {
            String[] requestedColumns = columnPart.split(",");
            for (String col : requestedColumns) {
                col = col.trim();
                if (!columns.contains(col)) {
                    return "[ERROR] Column '" + col + "' does not exist.";
                }
                selectedIndexes.add(columns.indexOf(col));
            }
        }

        // Filter rows if WHERE clause exists**
        List<String[]> resultRows = new ArrayList<>();
        resultRows.add(selectedIndexes.stream().map(columns::get).toArray(String[]::new));

        for (int i = 1; i < tableData.size(); i++) {
            String[] rowValues = tableData.get(i);

            if (whereClause != null && !EvaluateCondition.evaluateCondition(whereClause, columns, rowValues)) {
                continue;
            }

            resultRows.add(selectedIndexes.stream().map(index -> rowValues[index]).toArray(String[]::new));
        }

        return formatTableOutput(resultRows);
    }

    private String formatTableOutput(List<String[]> tableData) {
        StringBuilder output = new StringBuilder("[OK]\n");
        for (String[] row : tableData) {
            output.append(String.join("\t", row)).append("\n");
        }
        return output.toString().trim();
    }
}
