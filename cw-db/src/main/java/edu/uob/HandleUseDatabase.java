package edu.uob;

import java.io.File;

public class HandleUseDatabase implements CommandHandler {
    public String execute(DatabaseState databaseState, String[] tokens, String command) {
        if (tokens.length != 2) {
            return "[ERROR] Incorrect syntax for USE command. Expected: USE <database>;";
        }

        if (SQLKeywords.isReservedKeyword(tokens[1])) {
            return "[ERROR] '" + tokens[1] + "' is a reserved keyword and cannot be used as a name.";
        }

        String databaseName = tokens[1].toLowerCase();
        File databaseDir = new File(databaseState.getStorageFolderPath(), databaseName);

        if (!databaseDir.exists() || !databaseDir.isDirectory()) {
            return "[ERROR] Database '" + databaseName + "' does not exist.";
        }

        databaseState.setCurrentDatabase(databaseName); // Track the current active database
        return "[OK] Using database '" + databaseName + "'";
    }
}
