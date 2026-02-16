package com.celebration.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Message {

    private final String messageId;
    private final String publicToken;
    private final String editToken;
    private String title;
    private String content;
    private OccasionType occasionType;
    private String occasionName;
    private TemplateCode templateCode;
    private Instant expiresAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<Media> media;

    public Message(String messageId,
                   String publicToken,
                   String editToken,
                   String title,
                   String content,
                   OccasionType occasionType,
                   String occasionName,
                   TemplateCode templateCode,
                   Instant expiresAt,
                   Instant createdAt,
                   Instant updatedAt,
                   List<Media> media) {
        this.messageId = messageId;
        this.publicToken = publicToken;
        this.editToken = editToken;
        this.title = title;
        this.content = content;
        this.occasionType = occasionType;
        this.occasionName = occasionName;
        this.templateCode = templateCode;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.media = new ArrayList<>(media);
    }

    public String getMessageId() { return messageId; }
    public String getPublicToken() { return publicToken; }
    public String getEditToken() { return editToken; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public OccasionType getOccasionType() { return occasionType; }
    public String getOccasionName() { return occasionName; }
    public TemplateCode getTemplateCode() { return templateCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Media> getMedia() { return List.copyOf(media); }

    public void update(String title, String content, TemplateCode templateCode, Instant expiresAt, Instant now) {
        this.title = title;
        this.content = content;
        this.templateCode = templateCode;
        this.expiresAt = expiresAt;
        this.updatedAt = now;
    }

    public void replaceVoice(Media voice) {
        media.removeIf(m -> m.mediaType() == MediaType.VOICE);
        media.add(voice);
    }

    public void addImage(Media image) {
        media.add(image);
    }

    public int imageCount() {
        return (int) media.stream().filter(m -> m.mediaType() == MediaType.IMAGE).count();
    }

    public long totalUploadSize() {
        return media.stream().mapToLong(Media::fileSizeBytes).sum();
    }

    public Media voice() {
        return media.stream().filter(m -> m.mediaType() == MediaType.VOICE).findFirst().orElse(null);
    }
}
