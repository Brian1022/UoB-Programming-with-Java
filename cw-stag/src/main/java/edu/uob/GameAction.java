package edu.uob;

import java.util.Set;

public class GameAction {

    private final Set<String> triggers;
    private final Set<String> subjects;
    private final Set<String> consumed;
    private final Set<String> produced;
    private final String narration;

    public GameAction(Set<String> triggers, Set<String> subjects, Set<String> consumed, Set<String> produced, String narration) {
        this.triggers = triggers;
        this.subjects = subjects;
        this.consumed = consumed;
        this.produced = produced;
        this.narration = narration;
    }

    public Set<String> getTriggers() {
        return triggers;
    }

    public Set<String> getSubjects() {
        return subjects;
    }

    public Set<String> getConsumedEntities() {
        return consumed;
    }

    public Set<String> getProducedEntities() {
        return produced;
    }

    public String getNarration() {
        return narration;
    }
}
