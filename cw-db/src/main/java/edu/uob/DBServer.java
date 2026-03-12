package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;
    private final DatabaseState databaseState;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        this.databaseState = new DatabaseState(storageFolderPath);
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        // Step 1: Trim and sanitize input
        if (command == null || command.trim().isEmpty()) {
            return "[ERROR] Empty command received.";
        }
        command = command.trim();

        // Step 2: Ensure command ends with a semicolon
        if (!command.endsWith(";")) {
            return "[ERROR] Missing semicolon at the end of the command.";
        }
        command = command.substring(0, command.length() - 1).trim(); // Remove semicolon for processing

        // Step 3: Tokenize the command
        String[] tokens = command.split("\\s+"); // Split by whitespace
        if (tokens.length == 0) {
            return "[ERROR] Invalid command format.";
        }

        // Step 4: Identify the command type (first keyword)
        String action = tokens[0].toUpperCase(); // Case-insensitive command matching

        CommandHandler handler = switch (action) {
            case "USE" -> new HandleUseDatabase();
            case "CREATE" -> new HandleCreate();
            case "DROP" -> new HandleDrop();
            case "ALTER" -> new HandleAlter();
            case "INSERT" -> new HandleInsert();
            case "SELECT" -> new HandleSelect();
            case "UPDATE" -> new HandleUpdate();
            case "DELETE" -> new HandleDelete();
            case "JOIN" -> new HandleJoin();
            default -> null;
        };

        if (handler == null) {
            return "[ERROR] Invalid command format.";
        }

        return handler.execute(databaseState, tokens, command);

    }

    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
