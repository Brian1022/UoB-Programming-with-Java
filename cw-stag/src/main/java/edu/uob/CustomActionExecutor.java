package edu.uob;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CustomActionExecutor {

    private final GameWorld world;
    private final GamePlayerState state;
    private final CommandValidator validator;

    public CustomActionExecutor(GameWorld world, GamePlayerState state) {
        this.world = world;
        this.state = state;
        this.validator = new CommandValidator();
    }

    public String tryExecuteAction(List<GameAction> actions, List<String> tokens, GamePlayer player) {
        Set<String> allTriggers = this.collectAllTriggers(actions);
        tokens = validator.filterCustomActionTokens(tokens, world, actions);

        if (this.validator.isCommandTooVague(tokens, allTriggers)) {
            return "Your command is too vague to interpret.";
        }

        if (this.validator.isCommandAmbiguous(actions, tokens)) {
            return "There is more than one possible action – please clarify your command.";
        }

        Iterator<GameAction> actionIterator = actions.iterator();
        while (actionIterator.hasNext()) {
            GameAction action = actionIterator.next();

            if (!this.containsAny(tokens, action.getTriggers())) {
                continue;
            }

            if (!this.containsAny(tokens, action.getSubjects())) {
                continue;
            }

            if (this.validator.containsExtraneousEntities(tokens, action.getSubjects(), this.world.getAllEntityNames())) {
                return "Command includes extraneous or inappropriate entities.";
            }

            if (!this.allSubjectsAreAvailable(action.getSubjects(), player)) {
                return "You are missing some required entities to perform this action.";
            }

            this.consumeEntities(action.getConsumedEntities(), player);
            this.produceEntities(action.getProducedEntities(), player);
            this.updateHealth(action, player);

            return action.getNarration();
        }

        return "No matching custom action could be found.";
    }

    private Set<String> collectAllTriggers(List<GameAction> actions) {
        Set<String> all = new HashSet<String>();
        Iterator<GameAction> it = actions.iterator();
        while (it.hasNext()) {
            GameAction action = it.next();
            all.addAll(action.getTriggers());
        }
        return all;
    }

    private boolean containsAny(List<String> tokens, Set<String> keywords) {
        Iterator<String> tokenIt = tokens.iterator();
        while (tokenIt.hasNext()) {
            String token = tokenIt.next();
            Iterator<String> keyIt = keywords.iterator();
            while (keyIt.hasNext()) {
                String keyword = keyIt.next();
                if (token.equalsIgnoreCase(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allSubjectsAreAvailable(Set<String> subjects, GamePlayer player) {
        Iterator<String> subjectIterator = subjects.iterator();
        while (subjectIterator.hasNext()) {
            String name = subjectIterator.next();
            if (!this.world.isEntityAvailableToPlayer(name, player)) {
                return false;
            }
        }
        return true;
    }

    private void consumeEntities(Set<String> consumed, GamePlayer player) {
        GameLocation storeroom = this.world.getStoreroom();
        Iterator<String> iterator = consumed.iterator();

        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name.equals("health")) {
                player.decreaseHealth();
                continue;
            }
            GameEntity entity = this.world.getEntityByName(name);
            if (entity instanceof GameArtefact && player.getInventory().contains(entity)) {
                player.removeFromInventory((GameArtefact) entity);
                storeroom.addEntity(entity);
            } else if (player.getCurrentLocation().hasEntity(entity)) {
                player.getCurrentLocation().removeEntity(entity);
                storeroom.addEntity(entity);
            } else if (entity instanceof GameLocation) {
                player.getCurrentLocation().getPathNames().remove(name);
            }
        }
    }

    private void produceEntities(Set<String> produced, GamePlayer player) {
        GameLocation current = player.getCurrentLocation();
        GameLocation storeroom = this.world.getStoreroom();
        Iterator<String> iterator = produced.iterator();

        while (iterator.hasNext()) {
            String name = iterator.next();
            if (name.equals("health")) {
                player.increaseHealth();
                continue;
            }
            GameEntity entity = this.world.getEntityByName(name);
            if (entity != null) {
                current.addEntity(entity);
                storeroom.removeEntity(entity);
            } else {
                GameLocation newLocation = this.world.getLocationByName(name);
                if (newLocation != null) {
                    current.connectTo(newLocation);
                }
            }
        }
    }

    private void updateHealth(GameAction action, GamePlayer player) {
        if (player.isDead()) {
            player.dropAllItemsToLocation(player.getCurrentLocation());
            player.setCurrentLocation(this.world.getStartLocation());
            player.resetHealth();
        }
    }
}