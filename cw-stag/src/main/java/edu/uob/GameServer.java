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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;

    private final GameWorld world;
    private final List<GameAction> actions;
    private final GamePlayerState playerState;
    private final BuiltInCommandHandler builtInHandler;
    private final CustomActionExecutor customExecutor;

    public static void main(String[] args) throws IOException {
        StringBuilder sbdot = new StringBuilder();
        sbdot.append("config").append(File.separator).append("extended-entities.dot");
        StringBuilder sbxml = new StringBuilder();
        sbxml.append("config").append(File.separator).append("extended-actions.xml");
        File entitiesFile = Paths.get(sbdot.toString()).toAbsolutePath().toFile();
        File actionsFile = Paths.get(sbxml.toString()).toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        GameEntityParser entityParser = new GameEntityParser(entitiesFile);
        GameActionParser actionParser = new GameActionParser(actionsFile);
        this.world = entityParser.parse();
        this.actions = actionParser.parse();
        this.playerState = new GamePlayerState(this.world.getStartLocation());
        this.builtInHandler = new BuiltInCommandHandler();
        this.customExecutor = new CustomActionExecutor(this.world, this.playerState);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        String username = GameCommandInterpreter.extractUsername(command);
        String raw = GameCommandInterpreter.extractCommandBody(command);

        if (username == null || !GameCommandInterpreter.isValidUsername(username)) {
            return "Invalid or missing username.";
        }

        GamePlayer player = this.playerState.getOrCreatePlayer(username);
        List<String> tokens = GameCommandInterpreter.extractTokens(raw);

        if (tokens.isEmpty()) {
            return "Command is empty or invalid.";
        }

        Set<String> triggerSet = new HashSet<>();
        triggerSet.addAll(this.builtInHandler.getBuiltInCommands());
        for (GameAction action : this.actions) {
            triggerSet.addAll(action.getTriggers());
        }

        Set<String> foundTriggers = new HashSet<>();
        for (String token : tokens) {
            String lower = token.toLowerCase();
            if (triggerSet.contains(lower)) {
                foundTriggers.add(lower);
            }
        }

        if (foundTriggers.isEmpty()) {
            return "No valid command trigger found.";
        }
        if (foundTriggers.size() > 1) {
            return "Command is ambiguous – more than one possible action trigger detected.";
        }

        String trigger = foundTriggers.iterator().next();
        if (this.builtInHandler.isBuiltInCommand(trigger)) {
            return this.builtInHandler.executeBuiltInCommand(tokens, player, this.world, this.playerState);
        } else {
            return this.customExecutor.tryExecuteAction(this.actions, tokens, player);
        }
    }


    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            StringBuilder sbport = new StringBuilder();
            sbport.append("Server listening on port ").append(portNumber);
            System.out.println(sbport.toString());
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                StringBuilder sbic = new StringBuilder();
                sbic.append("Received message from ").append(incomingCommand);
                System.out.println(sbic.toString());
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                StringBuilder sbet = new StringBuilder();
                sbet.append("\n" ).append(END_OF_TRANSMISSION).append("\n");
                writer.write(sbet.toString());
                writer.flush();
            }
        }
    }
}
