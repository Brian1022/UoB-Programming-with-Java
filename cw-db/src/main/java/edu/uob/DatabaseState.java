package edu.uob;

public class DatabaseState {
    private String storageFolderPath;
    private String currentDatabase = null;

    public DatabaseState(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public String getStorageFolderPath() {
        return storageFolderPath;
    }

    public void setStorageFolderPath(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    public void setCurrentDatabase(String currentDatabase) {
        this.currentDatabase = currentDatabase;
    }
}

