package com.arivubuilds.ai.mvc.memory;

import com.arivubuilds.ai.mvc.model.ChatTurn;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConversationMemoryStore {

    private final int maxTokenBudget;
    private final ConcurrentHashMap<String, Deque<ChatTurn>> sessions = new ConcurrentHashMap<>();

    public ConversationMemoryStore(
            @Value("${ai.mvc.memory-token-budget:240}") int maxTokenBudget) {
        this.maxTokenBudget = maxTokenBudget;
    }

    public List<ChatTurn> loadWindow(String sessionId) {
        Deque<ChatTurn> turns = sessions.get(sessionId);
        if (turns == null) {
            return List.of();
        }
        synchronized (turns) {
            return List.copyOf(turns);
        }
    }

    public void appendTurn(String sessionId, ChatTurn turn) {
        Deque<ChatTurn> turns = sessions.computeIfAbsent(sessionId, id -> new ArrayDeque<>());
        synchronized (turns) {
            turns.addLast(turn);
            int totalTokens = turns.stream().mapToInt(ChatTurn::estimatedTokens).sum();
            while (totalTokens > maxTokenBudget && turns.size() > 1) {
                ChatTurn evicted = turns.removeFirst();
                totalTokens -= evicted.estimatedTokens();
            }
        }
    }

    public int currentWindowTokens(String sessionId) {
        return loadWindow(sessionId).stream().mapToInt(ChatTurn::estimatedTokens).sum();
    }

    public void clearAll() {
        sessions.clear();
    }

    public String formatHistory(List<ChatTurn> history) {
        if (history.isEmpty()) {
            return "(no prior turns)";
        }
        List<String> lines = new ArrayList<>(history.size());
        for (ChatTurn turn : history) {
            lines.add(turn.role() + ": " + turn.content());
        }
        return String.join("\n", lines);
    }
}
