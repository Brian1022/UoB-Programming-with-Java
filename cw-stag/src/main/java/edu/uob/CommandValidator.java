package edu.uob;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandValidator {

    public List<String> filterBuiltInCommandTokens(List<String> rawTokens, GameWorld world, Set<String> builtInKeywords) {
        Set<String> validWords = new HashSet<>();

        validWords.addAll(builtInKeywords);
        validWords.addAll(world.getAllEntityNames());
        validWords.addAll(world.getAllLocationNames());

        Set<String> filteredSet = new HashSet<>();
        for (String token : rawTokens) {
            String lower = token.toLowerCase();
            if (validWords.contains(lower)) {
                filteredSet.add(lower);
            }
        }

        return new java.util.LinkedList<>(filteredSet);
    }

    public List<String> filterCustomActionTokens(List<String> rawTokens, GameWorld world, List<GameAction> actions) {
        Set<String> validWords = new HashSet<>();

        for (GameAction action : actions) {
            validWords.addAll(action.getTriggers());
        }
        validWords.addAll(world.getAllEntityNames());
        validWords.addAll(world.getAllLocationNames());

        Set<String> filteredSet = new HashSet<>();
        for (String token : rawTokens) {
            String lower = token.toLowerCase();
            if (validWords.contains(lower)) {
                filteredSet.add(lower);
            }
        }

        return new java.util.LinkedList<>(filteredSet);
    }

    public boolean isCommandAmbiguous(List<GameAction> actions, List<String> tokens) {
        int validMatches = 0;
        Iterator<GameAction> it = actions.iterator();

        while (it.hasNext()) {
            GameAction action = it.next();
            if (this.containsAnyTrigger(tokens, action.getTriggers()) && this.matchesSubjects(tokens, action.getSubjects())) {
                validMatches = validMatches + 1;
                if (validMatches > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCommandTooVague(List<String> tokens, Set<String> triggers) {
        if (tokens.size() < 2) {
            return true;
        }
        if (!containsAnyTrigger(tokens, triggers)) {
            return true;
        }
        return false;
    }

    public boolean containsExtraneousEntities(List<String> tokens, Set<String> subjects, Set<String> allEntities) {
        Iterator<String> it = tokens.iterator();
        while (it.hasNext()) {
            String word = it.next();
            if (allEntities.contains(word)) {
                if (!subjects.contains(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsAnyTrigger(List<String> tokens, Set<String> triggers) {
        Iterator<String> tokenIt = tokens.iterator();
        while (tokenIt.hasNext()) {
            String token = tokenIt.next();
            Iterator<String> triggerIt = triggers.iterator();
            while (triggerIt.hasNext()) {
                String trigger = triggerIt.next();
                if (token.equalsIgnoreCase(trigger)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesSubjects(List<String> tokens, Set<String> subjects) {
        Set<String> matched = new HashSet<String>();
        Iterator<String> subjectIt = subjects.iterator();
        while (subjectIt.hasNext()) {
            String subject = subjectIt.next();
            Iterator<String> tokenIt = tokens.iterator();
            while (tokenIt.hasNext()) {
                String token = tokenIt.next();
                if (subject.equalsIgnoreCase(token)) {
                    matched.add(subject);
                    break;
                }
            }
        }
        return matched.size() == subjects.size();
    }
}
