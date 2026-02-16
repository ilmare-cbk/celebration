package com.celebration.infra;

import com.celebration.domain.Message;

import java.util.Optional;

public interface MessageRepository {

    Message save(Message message);

    Optional<Message> findById(String messageId);

    Optional<Message> findByPublicToken(String publicToken);
}
