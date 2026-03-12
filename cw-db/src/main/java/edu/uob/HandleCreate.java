package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HandleCreate implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (tokens.length < 3) {
            return "[ERROR] Invalid CREATE syntax.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[2])) {
            return "[ERROR] '" + tokens[2] + "' is a reserved keyword and cannot be used as a name.";
        }

        if (tokens[1].equalsIgnoreCase("DATABASE")) {
            return createDatabase(databaseState, tokens[2]);
        } else if (tokens[1].equalsIgnoreCase("TABLE")) {
            return createTable(databaseState, tokens, command);
        }

        return "[ERROR] Invalid CREATE command.";
    }

    public String createDatabase(DatabaseState databaseState, String databaseName) {
        // Ensure database names are case-insensitive
        databaseName = databaseName.toLowerCase();

        // Define the database folder path
        File databaseDir = new File(databaseState.getStorageFolderPath(), databaseName);

        // Check if database already exists
        if (databaseDir.exists()) {
            return "[ERROR] Database '" + databaseName + "' already exists.";
        }

        // Attempt to create the database directory
        if (databaseDir.mkdir()) {
            databaseState.setCurrentDatabase(databaseName);
            String commandCreateIDTable = "CREATE TABLE maxid (tablename, maxid)";
            createTable(databaseState, commandCreateIDTable.split("\\s+"), commandCreateIDTable);
            databaseState.setCurrentDatabase(null);
            return "[OK] Database '" + databaseName + "' created successfully.";
        } else {
            return "[ERROR] Failed to create database '" + databaseName + "'.";
        }
    }

    public String createTable(DatabaseState databaseState, String[] tokens, String command) {
        // Ensure a database is selected
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        // Validate syntax (must be at least "CREATE TABLE <table>")
        if (tokens.length < 3) {
            return "[ERROR] Invalid CREATE TABLE syntax. Expected: CREATE TABLE <name> (columns);";
        }

        String tableName = tokens[2].toLowerCase();  // Convert table name to lowercase
        File tableFile = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase() + File.separator + tableName + ".tab");

        // Check if table already exists
        if (tableFile.exists()) {
            return "[ERROR] Table '" + tableName + "' already exists.";
        }

        // Check if the table definition includes column definitions
        String columnDefinition = "";
        if (tokens.length > 3) {
            if (!tokens[3].startsWith("(")) {
                return "[ERROR] Invalid Column Definitions.";
            } else {
                columnDefinition = command.substring(command.indexOf("(") + 1, command.lastIndexOf(")")).trim();
            }
        }

        // Parse and validate the column names
        List<String> columns = new ArrayList<>();
        columns.add("id"); // Always include 'id' as the primary key
        if (!columnDefinition.isEmpty()) {
            String[] columnNames = columnDefinition.split(",");
            for (String col : columnNames) {
                col = col.trim();
                if (col.isEmpty() || columns.contains(col.toLowerCase())) {
                    return "[ERROR] Invalid or duplicate column name: '" + col + "'.";
                }
                if (SQLKeywords.isReservedKeyword(col)) {
                    return "[ERROR] '" + col + "' is a reserved keyword and cannot be used as a name.";
                }
                columns.add(col);
            }
        }

        // Create table file and write the header row
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            writer.write(String.join("\t", columns)); // Tab-separated column names
            writer.newLine();
        } catch (IOException e) {
            return "[ERROR] Failed to create table '" + tableName + "': " + e.getMessage();
        }

        CommandHandler insertTablenameToMaxID = new HandleInsert();
        String commandinsertTablenameToMaxID = "INSERT INTO maxid VALUES ('" + tableName + "', 0)";
        String[] tokensinsertTablenameToMaxID = commandinsertTablenameToMaxID.split("\\s+");
        insertTablenameToMaxID.execute(databaseState, tokensinsertTablenameToMaxID, commandinsertTablenameToMaxID);

        return "[OK] Table '" + tableName + "' created successfully.";
    }
}
