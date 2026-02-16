package com.celebration.infra.jpa;

import com.celebration.domain.Media;
import com.celebration.domain.Message;
import com.celebration.infra.MessageRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaMessageRepository implements MessageRepository {

    private final SpringDataMessageJpaRepository repository;

    public JpaMessageRepository(SpringDataMessageJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Message save(Message message) {
        MessageJpaEntity saved = repository.save(toEntity(message));
        return toDomain(saved);
    }

    @Override
    public Optional<Message> findById(String messageId) {
        return repository.findById(messageId).map(this::toDomain);
    }

    @Override
    public Optional<Message> findByPublicToken(String publicToken) {
        return repository.findByPublicToken(publicToken).map(this::toDomain);
    }

    private MessageJpaEntity toEntity(Message message) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setMessageId(message.getMessageId());
        entity.setPublicToken(message.getPublicToken());
        entity.setEditToken(message.getEditToken());
        entity.setTitle(message.getTitle());
        entity.setContent(message.getContent());
        entity.setOccasionType(message.getOccasionType());
        entity.setOccasionName(message.getOccasionName());
        entity.setTemplateCode(message.getTemplateCode());
        entity.setExpiresAt(message.getExpiresAt());
        entity.setCreatedAt(message.getCreatedAt());
        entity.setUpdatedAt(message.getUpdatedAt());
        entity.replaceMedia(message.getMedia().stream().map(this::toEntity).toList());
        return entity;
    }

    private MediaJpaEntity toEntity(Media media) {
        MediaJpaEntity entity = new MediaJpaEntity();
        entity.setMediaId(media.mediaId());
        entity.setMediaType(media.mediaType());
        entity.setFileName(media.fileName());
        entity.setFileSizeBytes(media.fileSizeBytes());
        entity.setMimeType(media.mimeType());
        entity.setUrl(media.url());
        entity.setSortOrder(media.sortOrder());
        return entity;
    }

    private Message toDomain(MessageJpaEntity entity) {
        List<Media> media = entity.getMedia().stream()
                .map(this::toDomain)
                .toList();

        return new Message(
                entity.getMessageId(),
                entity.getPublicToken(),
                entity.getEditToken(),
                entity.getTitle(),
                entity.getContent(),
                entity.getOccasionType(),
                entity.getOccasionName(),
                entity.getTemplateCode(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                media
        );
    }

    private Media toDomain(MediaJpaEntity entity) {
        return new Media(
                entity.getMediaId(),
                entity.getMediaType(),
                entity.getFileName(),
                entity.getFileSizeBytes(),
                entity.getMimeType(),
                entity.getUrl(),
                entity.getSortOrder()
        );
    }
}
