package edu.uob;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameWorld {

    private final Map<String, GameLocation> locations;
    private final Map<String, GameEntity> allEntities;
    private GameLocation storeroom;
    private GameLocation startLocation;

    public GameWorld(GameLocation startLocation, GameLocation storeroom) {
        this.locations = new HashMap<>();
        this.allEntities = new HashMap<>();
        this.startLocation = startLocation;
        this.storeroom = storeroom;
    }

    public void setStartLocation(GameLocation location) {
        this.startLocation = location;
    }

    public void setStoreroom(GameLocation storeroom) {
        this.storeroom = storeroom;
    }

    public void addLocation(GameLocation location) {
        this.locations.put(location.getName().toLowerCase(), location);
    }

    public void registerEntity(GameEntity entity) {
        this.allEntities.put(entity.getName().toLowerCase(), entity);
    }

    public GameLocation getLocationByName(String name) {
        return this.locations.get(name.toLowerCase());
    }

    public GameEntity getEntityByName(String name) {
        return this.allEntities.get(name.toLowerCase());
    }

    public boolean isEntityAvailableToPlayer(String name, GamePlayer player) {
        GameEntity entity = this.getEntityByName(name);
        return entity != null && (
                player.getInventory().contains(entity) ||
                        player.getCurrentLocation().hasEntity(entity)
        );
    }

    public GameLocation getStartLocation() {
        return this.startLocation;
    }

    public GameLocation getStoreroom() {
        return this.storeroom;
    }

    public Set<String> getAllEntityNames() {
        return new HashSet<>(allEntities.keySet());
    }

    public Set<String> getAllLocationNames() {
        return new HashSet<>(this.locations.keySet());
    }

}
