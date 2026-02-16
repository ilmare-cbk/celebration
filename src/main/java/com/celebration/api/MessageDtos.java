package com.celebration.api;

import com.celebration.domain.Media;
import com.celebration.domain.OccasionType;
import com.celebration.domain.TemplateCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public class MessageDtos {

    public record UpdateMessageRequest(
            @NotBlank String title,
            @NotBlank @Size(max = 2000) String content,
            @NotNull TemplateCode templateCode,
            @NotNull Instant expiresAt
    ) {
    }

    public record CreateMessageResponse(
            String messageId,
            String editToken,
            String publicToken,
            Instant expiresAt
    ) {
    }

    public record UpdateMessageResponse(
            String messageId,
            Instant updatedAt
    ) {
    }

    public record PublicMessageResponse(
            String title,
            String content,
            TemplateCode templateCode,
            Occasion occasion,
            MediaPayload media,
            Instant expiresAt
    ) {
        public record Occasion(OccasionType type, String name) {}

        public record MediaPayload(List<ImagePayload> images, VoicePayload voice) {}

        public record ImagePayload(String url, Integer sortOrder) {}

        public record VoicePayload(String url) {}

        public static PublicMessageResponse from(com.celebration.domain.Message message) {
            List<ImagePayload> images = message.getMedia().stream()
                    .filter(media -> media.mediaType() == com.celebration.domain.MediaType.IMAGE)
                    .map(media -> new ImagePayload(media.url(), media.sortOrder()))
                    .toList();

            Media voice = message.voice();
            VoicePayload voicePayload = voice == null ? null : new VoicePayload(voice.url());

            return new PublicMessageResponse(
                    message.getTitle(),
                    message.getContent(),
                    message.getTemplateCode(),
                    new Occasion(message.getOccasionType(), message.getOccasionName()),
                    new MediaPayload(images, voicePayload),
                    message.getExpiresAt()
            );
        }
    }
}
