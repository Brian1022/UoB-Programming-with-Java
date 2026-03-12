package edu.uob;

public interface CommandHandler {
    String execute(DatabaseState databaseState, String[] tokens, String command);
}
