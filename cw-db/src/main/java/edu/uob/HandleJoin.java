package edu.uob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleJoin implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected.";
        }

        if (tokens.length < 8 || !tokens[2].equalsIgnoreCase("AND") || !tokens[4].equalsIgnoreCase("ON") || !tokens[6].equalsIgnoreCase("AND")) {
            return "[ERROR] Invalid JOIN syntax.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[1])) {
            return "[ERROR] '" + tokens[1] + "' is a reserved keyword and cannot be used as a name.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[3])) {
            return "[ERROR] '" + tokens[3] + "' is a reserved keyword and cannot be used as a name.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[5])) {
            return "[ERROR] '" + tokens[5] + "' is a reserved keyword and cannot be used as a name.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[7])) {
            return "[ERROR] '" + tokens[7] + "' is a reserved keyword and cannot be used as a name.";
        }

        //String table1 = tokens[1].toLowerCase();
        //String table2 = tokens[3].toLowerCase();
        return joinTables(databaseState, tokens);
    }

    private String joinTables(DatabaseState databaseState, String[] tokens) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        if (tokens.length < 8 || !tokens[2].equalsIgnoreCase("AND") || !tokens[4].equalsIgnoreCase("ON")) {
            return "[ERROR] Invalid JOIN syntax. Expected: JOIN <table1> AND <table2> ON <column1> AND <column2>;";
        }

        // Extract table names and join attributes
        String table1 = tokens[1].toLowerCase();
        String table2 = tokens[3].toLowerCase();
        String column1 = tokens[5];
        String column2 = tokens[7];

        // Load both tables
        List<String[]> table1Data = loadTable(databaseState, table1);
        List<String[]> table2Data = loadTable(databaseState, table2);
        if (table1Data == null || table2Data == null) {
            return "[ERROR] One or both tables do not exist.";
        }

        // Extract column headers
        List<String> table1Columns = new ArrayList<>(Arrays.asList(table1Data.get(0)));
        List<String> table2Columns = new ArrayList<>(Arrays.asList(table2Data.get(0)));

        // Identify column indexes
        int columnIndex1 = table1Columns.indexOf(column1);
        int columnIndex2 = table2Columns.indexOf(column2);
        if (columnIndex1 == -1 || columnIndex2 == -1) {
            return "[ERROR] Join column(s) not found.";
        }

        // Construct the joined table header (new unique id, plus prefixed columns)
        List<String> joinedColumns = new ArrayList<>();
        joinedColumns.add("id"); // New unique ID column

        // Add table1 columns (excluding 'id' and join column)
        for (String col : table1Columns) {
            if (!col.equalsIgnoreCase("id") && !col.equals(column1)) {
                joinedColumns.add(table1 + "." + col);
            }
        }

        // Add table2 columns (excluding 'id' and join column)
        for (String col : table2Columns) {
            if (!col.equalsIgnoreCase("id") && !col.equals(column2)) {
                joinedColumns.add(table2 + "." + col);
            }
        }

        // Perform the join operation
        List<String[]> joinedData = new ArrayList<>();
        joinedData.add(joinedColumns.toArray(new String[0]));

        int newId = 1; // Start new unique ID count

        for (int i = 1; i < table1Data.size(); i++) {
            for (int j = 1; j < table2Data.size(); j++) {
                if (table1Data.get(i)[columnIndex1].equals(table2Data.get(j)[columnIndex2])) {
                    List<String> newRow = new ArrayList<>();
                    newRow.add(String.valueOf(newId++)); // Assign new unique ID

                    // Add table1 data (excluding ID and join column)
                    for (int k = 0; k < table1Data.get(i).length; k++) {
                        if (!table1Columns.get(k).equalsIgnoreCase("id") && k != columnIndex1) {
                            newRow.add(table1Data.get(i)[k]);
                        }
                    }

                    // Add table2 data (excluding ID and join column)
                    for (int k = 0; k < table2Data.get(j).length; k++) {
                        if (!table2Columns.get(k).equalsIgnoreCase("id") && k != columnIndex2) {
                            newRow.add(table2Data.get(j)[k]);
                        }
                    }

                    joinedData.add(newRow.toArray(new String[0]));
                }
            }
        }

        // Return formatted result
        return formatTableOutput(joinedData);
    }

    private List<String[]> loadTable(DatabaseState databaseState, String tableName) {
        File tableFile = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase() + File.separator + tableName + ".tab");

        if (!tableFile.exists()) {
            return null;
        }

        List<String[]> tableData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tableData.add(line.split("\t"));
            }
        } catch (IOException e) {
            return null;
        }

        return tableData;
    }

    private String formatTableOutput(List<String[]> tableData) {
        StringBuilder output = new StringBuilder("[OK]\n");
        for (String[] row : tableData) {
            output.append(String.join("\t", row)).append("\n");
        }
        return output.toString().trim();
    }
}
