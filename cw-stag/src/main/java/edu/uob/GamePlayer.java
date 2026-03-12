package edu.uob;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GamePlayer extends GameEntity {

    private GameLocation currentLocation;
    private final Set<GameArtefact> inventory;
    private int health;

    public GamePlayer(String name, GameLocation startingLocation) {
        super(name, GamePlayer.createPlayerDescription(name));
        this.currentLocation = startingLocation;
        this.inventory = new HashSet<GameArtefact>();
        this.health = 3;
    }

    private static String createPlayerDescription(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("Player: ");
        sb.append(name);
        return sb.toString();
    }

    public GameLocation getCurrentLocation() {
        return this.currentLocation;
    }

    public void setCurrentLocation(GameLocation newLocation) {
        this.currentLocation = newLocation;
    }

    public Set<GameArtefact> getInventory() {
        return this.inventory;
    }

    public void addToInventory(GameArtefact artefact) {
        this.inventory.add(artefact);
    }

    public void removeFromInventory(GameArtefact artefact) {
        this.inventory.remove(artefact);
    }

    public int getHealth() {
        return this.health;
    }

    public void increaseHealth() {
        if (this.health < 3) {
            this.health = this.health + 1;
        }
    }

    public void decreaseHealth() {
        if (this.health > 0) {
            this.health = this.health - 1;
        }
    }

    public boolean isDead() {
        if (this.health == 0) {
            return true;
        }
        return false;
    }

    public void resetHealth() {
        this.health = 3;
    }

    public void dropAllItemsToLocation(GameLocation location) {
        Iterator<GameArtefact> items = this.inventory.iterator();
        while (items.hasNext()) {
            GameArtefact item = items.next();
            location.addEntity(item);
        }
        this.inventory.clear();
    }
}
