package com.celebration.infra.jpa;

import com.celebration.domain.OccasionType;
import com.celebration.domain.TemplateCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
public class MessageJpaEntity {

    @Id
    @Column(name = "message_id", nullable = false, updatable = false, length = 32)
    private String messageId;

    @Column(name = "public_token", nullable = false, unique = true, updatable = false, length = 64)
    private String publicToken;

    @Column(name = "edit_token", nullable = false, updatable = false, length = 64)
    private String editToken;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "occasion_type", nullable = false, length = 32)
    private OccasionType occasionType;

    @Column(name = "occasion_name")
    private String occasionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_code", nullable = false, length = 32)
    private TemplateCode templateCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MediaJpaEntity> media = new ArrayList<>();

    protected MessageJpaEntity() {
    }

    public void replaceMedia(List<MediaJpaEntity> newMedia) {
        media.clear();
        for (MediaJpaEntity mediaJpaEntity : newMedia) {
            mediaJpaEntity.setMessage(this);
            media.add(mediaJpaEntity);
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getPublicToken() {
        return publicToken;
    }

    public void setPublicToken(String publicToken) {
        this.publicToken = publicToken;
    }

    public String getEditToken() {
        return editToken;
    }

    public void setEditToken(String editToken) {
        this.editToken = editToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OccasionType getOccasionType() {
        return occasionType;
    }

    public void setOccasionType(OccasionType occasionType) {
        this.occasionType = occasionType;
    }

    public String getOccasionName() {
        return occasionName;
    }

    public void setOccasionName(String occasionName) {
        this.occasionName = occasionName;
    }

    public TemplateCode getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(TemplateCode templateCode) {
        this.templateCode = templateCode;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MediaJpaEntity> getMedia() {
        return media;
    }
}
