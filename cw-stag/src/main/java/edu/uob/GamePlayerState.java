package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class GamePlayerState {

    private final Map<String, GamePlayer> players;
    private final GameLocation startLocation;

    public GamePlayerState(GameLocation startLocation) {
        this.players = new HashMap<>();
        this.startLocation = startLocation;
    }

    public GamePlayer getOrCreatePlayer(String username) {
        String key = username.toLowerCase(); // Delete to limit the case-insensitive
        if (!players.containsKey(key)) {
            GamePlayer newPlayer = new GamePlayer(username, startLocation);
            players.put(key, newPlayer);
        }
        return players.get(key);
    }

    public Map<String, GamePlayer> getAllPlayers() {
        return players;
    }
}
