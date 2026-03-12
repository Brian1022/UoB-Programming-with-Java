package edu.uob;

import java.io.File;

public class HandleDrop implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (tokens.length < 3) {
            return "[ERROR] Invalid DROP syntax.";
        }

        if (SQLKeywords.isReservedKeyword(tokens[2])) {
            return "[ERROR] '" + tokens[2] + "' is a reserved keyword and cannot be used as a name.";
        }

        if (tokens[1].equalsIgnoreCase("DATABASE")) {
            return dropDatabase(databaseState, tokens[2]);
        } else if (tokens[1].equalsIgnoreCase("TABLE")) {
            return dropTable(databaseState, tokens[2]);
        }

        return "[ERROR] Invalid DROP command.";
    }

    private String dropDatabase(DatabaseState databaseState, String databaseName) {
        // Convert to lowercase for case insensitivity
        databaseName = databaseName.toLowerCase();

        // Define the database folder path
        File databaseDir = new File(databaseState.getStorageFolderPath(), databaseName);

        // Check if the database exists
        if (!databaseDir.exists() || !databaseDir.isDirectory()) {
            return "[ERROR] Database '" + databaseName + "' does not exist.";
        }

        // Ensure we are not dropping the currently selected database
        if (databaseName.equals(databaseState.getCurrentDatabase())) {
            databaseState.setCurrentDatabase(null);  // Unset the current database
        }

        // Recursively delete the database folder and all its contents
        if (deleteDirectory(databaseDir)) {
            return "[OK] Database '" + databaseName + "' dropped successfully.";
        } else {
            return "[ERROR] Failed to drop database '" + databaseName + "'.";
        }
    }

    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return dir.delete();
    }

    private String dropTable(DatabaseState databaseState, String tableName) {
        if (databaseState.getCurrentDatabase() == null) {
            return "[ERROR] No database selected. Use 'USE <database>;' first.";
        }

        // Convert to lowercase for case insensitivity
        tableName = tableName.toLowerCase();

        // Define the path to the table file
        File tableFile = new File(databaseState.getStorageFolderPath() + File.separator + databaseState.getCurrentDatabase() + File.separator + tableName + ".tab");

        // Check if the table exists
        if (!tableFile.exists()) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        // Attempt to delete the table file
        if (tableFile.delete()) {
            return "[OK] Table '" + tableName + "' dropped successfully.";
        } else {
            return "[ERROR] Failed to drop table '" + tableName + "'.";
        }
    }
}
