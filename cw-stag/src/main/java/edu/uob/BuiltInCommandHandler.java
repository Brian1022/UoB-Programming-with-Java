package edu.uob;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BuiltInCommandHandler {
    CommandValidator validator = new CommandValidator();
    Set<String> builtInKeywords = Set.of("look", "inv", "inventory", "get", "drop", "goto", "health");

    public boolean isBuiltInCommand(String command) {
        String cmd = command.toLowerCase();
        return cmd.equals("look") || cmd.equals("inv") || cmd.equals("inventory") ||
                cmd.equals("get") || cmd.equals("drop") || cmd.equals("goto") ||
                cmd.equals("health");
    }

    public String executeBuiltInCommand(List<String> tokens, GamePlayer player, GameWorld world, GamePlayerState state) {
        tokens = validator.filterBuiltInCommandTokens(tokens, world, builtInKeywords);
        String command = this.containsBuiltInKeywords(tokens);

        if (command.equals("look")) {
            return this.handleLook(player, state);
        } else if (command.equals("inv") || command.equals("inventory")) {
            return this.handleInventory(player);
        } else if (command.equals("get")) {
            return this.handleGet(tokens, player);
        } else if (command.equals("drop")) {
            return this.handleDrop(tokens, player);
        } else if (command.equals("goto")) {
            return this.handleGoto(tokens, player, state);
        } else if (command.equals("health")) {
            return this.handleHealth(player);
        } else {
            return "Unknown built-in command.";
        }
    }

    private String handleLook(GamePlayer player, GamePlayerState state) {
        GameLocation location = player.getCurrentLocation();
        StringBuilder sb = new StringBuilder();
        sb.append("You are in ");
        sb.append(location.getName());
        sb.append(".\n");
        sb.append(location.getDescription());
        sb.append("\n");

        Set<GameEntity> contents = location.getAllEntities();
        if (!contents.isEmpty()) {
            sb.append("You can see: \n");
            for (Iterator<GameEntity> it = contents.iterator(); it.hasNext();) {
                GameEntity e = it.next();
                sb.append(" - ");
                sb.append(e.getName());
                sb.append(" (");
                sb.append(e.getDescription());
                sb.append(")\n");
            }
        }

        Set<GameLocation> paths = location.getConnectedLocations();
        if (!paths.isEmpty()) {
            sb.append("Paths from here: \n");
            for (Iterator<GameLocation> it = paths.iterator(); it.hasNext();) {
                GameLocation l = it.next();
                sb.append(" - ");
                sb.append(l.getName());
                sb.append("\n");
            }
        }

        boolean hasOthers = false;
        for (GamePlayer other : state.getAllPlayers().values()) {
            if (!other.getName().equalsIgnoreCase(player.getName()) &&
                    other.getCurrentLocation().equals(location)) {
                if (!hasOthers) {
                    sb.append("Other players here:\n");
                    hasOthers = true;
                }
                sb.append(" - ");
                sb.append(other.getName());
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String handleInventory(GamePlayer player) {
        Set<GameArtefact> inv = player.getInventory();
        if (inv.isEmpty()) {
            return "You are not carrying anything.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("You are carrying: \n");
        for (Iterator<GameArtefact> it = inv.iterator(); it.hasNext();) {
            GameArtefact a = it.next();
            sb.append(" - ");
            sb.append(a.getName());
            sb.append(" (");
            sb.append(a.getDescription());
            sb.append(")\n");
        }
        return sb.toString();
    }

    private String handleGet(List<String> tokens, GamePlayer player) {
        if (tokens.size() != 2) {
            return "Specify what to get.";
        }

        Set<String> tokenSet = new HashSet<>();
        for (String t : tokens) {
            tokenSet.add(t.toLowerCase());
        }

        GameArtefact found = null;
        for (GameEntity entity : player.getCurrentLocation().getAllEntities()) {
            if (entity instanceof GameArtefact && tokenSet.contains(entity.getName().toLowerCase())) {
                found = (GameArtefact) entity;
                break;
            }
        }

        if (found != null) {
            player.addToInventory(found);
            player.getCurrentLocation().removeEntity(found);
            player.addToInventory((GameArtefact) found);
            player.getCurrentLocation().removeEntity(found);
            StringBuilder sb = new StringBuilder();
            sb.append("You picked up the ");
            sb.append(found.getName());
            sb.append(".");
            return sb.toString();
        }
        return "You can't take that.";
    }

    private String handleDrop(List<String> tokens, GamePlayer player) {
        if (tokens.size() != 2) {
            return "Specify what to drop.";
        }

        Set<String> tokenSet = new HashSet<>();
        for (String token : tokens) {
            tokenSet.add(token.toLowerCase());
        }

        GameArtefact dropItem = null;
        for (GameArtefact item : player.getInventory()) {
            if (tokenSet.contains(item.getName().toLowerCase())) {
                dropItem = item;
                break;
            }
        }

        if (dropItem != null) {
            player.removeFromInventory(dropItem);
            player.getCurrentLocation().addEntity(dropItem);
            StringBuilder sb = new StringBuilder();
            sb.append("You dropped the ");
            sb.append(dropItem.getName());
            sb.append(".");
            return sb.toString();
        }
        return "You are not carrying that item.";
    }

    private String handleGoto(List<String> tokens, GamePlayer player, GamePlayerState state) {
        if (tokens.size() != 2) {
            return "Specify where to go.";
        }

        Set<String> tokenSet = new HashSet<>();
        for (String token : tokens) {
            tokenSet.add(token.toLowerCase());
        }

        for (GameLocation connected : player.getCurrentLocation().getConnectedLocations()) {
            if (tokenSet.contains(connected.getName().toLowerCase())) {
                player.setCurrentLocation(connected);
                return this.handleLook(player, state);
            }
        }
        return "You can't go there from here.";
    }

    private String handleHealth(GamePlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append("Your current health: ");
        sb.append(player.getHealth());
        return sb.toString();
    }

    public String containsBuiltInKeywords(List<String> tokens) {
        for (String token : tokens) {
            String lower = token.toLowerCase();
            if (builtInKeywords.contains(lower)) {
                return lower;
            }
        }
        return null;
    }

    public Set<String> getBuiltInCommands() {
        return Set.of("look", "inv", "inventory", "get", "drop", "goto", "health");
    }
}
