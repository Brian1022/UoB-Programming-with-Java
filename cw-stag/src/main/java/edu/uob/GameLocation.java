package edu.uob;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameLocation extends GameEntity {

    private final Set<GameArtefact> artefacts;
    private final Set<GameFurniture> furniture;
    private final Set<GameCharacter> characters;
    private final Map<String, GameLocation> connectedLocations;

    public GameLocation(String name, String description) {
        super(name, description);
        this.artefacts = new HashSet<>();
        this.furniture = new HashSet<>();
        this.characters = new HashSet<>();
        this.connectedLocations = new HashMap<>();
    }

    public void connectTo(GameLocation location) {
        this.connectedLocations.put(location.getName().toLowerCase(), location);
    }

    public Set<String> getPathNames() {
        return connectedLocations.keySet();
    }

    public Set<GameLocation> getConnectedLocations() {
        return new HashSet<GameLocation>(connectedLocations.values());
    }

    public boolean hasEntity(GameEntity entity) {
        return artefacts.contains(entity) || furniture.contains(entity) || characters.contains(entity);
    }

    public void removeEntity(GameEntity entity) {
        artefacts.remove(entity);
        furniture.remove(entity);
        characters.remove(entity);
    }

    public void addEntity(GameEntity entity) {
        if (entity instanceof GameArtefact) {
            artefacts.add((GameArtefact) entity);
        } else if (entity instanceof GameFurniture) {
            furniture.add((GameFurniture) entity);
        } else if (entity instanceof GameCharacter) {
            characters.add((GameCharacter) entity);
        }
    }

    public Set<GameEntity> getAllEntities() {
        Set<GameEntity> all = new HashSet<GameEntity>();
        all.addAll(this.artefacts);
        all.addAll(this.furniture);
        all.addAll(this.characters);
        return all;
    }

    public GameEntity getEntityByName(String name) {
        for (GameArtefact artefact : this.artefacts) {
            if (artefact.getName().equalsIgnoreCase(name)) {
                return artefact;
            }
        }
        for (GameFurniture item : this.furniture) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        for (GameCharacter character : this.characters) {
            if (character.getName().equalsIgnoreCase(name)) {
                return character;
            }
        }
        return null;
    }
}
