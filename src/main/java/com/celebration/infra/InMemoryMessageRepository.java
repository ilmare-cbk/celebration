package com.celebration.infra;

import com.celebration.domain.Message;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMessageRepository {

    private final Map<String, Message> byId = new ConcurrentHashMap<>();
    private final Map<String, String> byPublicToken = new ConcurrentHashMap<>();

    public void save(Message message) {
        byId.put(message.getMessageId(), message);
        byPublicToken.put(message.getPublicToken(), message.getMessageId());
    }

    public Optional<Message> findById(String messageId) {
        return Optional.ofNullable(byId.get(messageId));
    }

    public Optional<Message> findByPublicToken(String publicToken) {
        String id = byPublicToken.get(publicToken);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }
}
