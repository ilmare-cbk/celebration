package com.celebration.service;

import com.celebration.api.MessageDtos;
import com.celebration.domain.Media;
import com.celebration.domain.MediaType;
import com.celebration.domain.Message;
import com.celebration.domain.OccasionType;
import com.celebration.domain.TemplateCode;
import com.celebration.infra.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class MessageService {

    private static final long IMAGE_MAX_SIZE = 3L * 1024 * 1024;
    private static final long VOICE_MAX_SIZE = 5L * 1024 * 1024;
    private static final long TOTAL_MAX_SIZE = 14L * 1024 * 1024;

    private final MessageRepository repository;
    private static final Clock clock = Clock.systemUTC();

    MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public MessageDtos.CreateMessageResponse create(String title,
                                                    String content,
                                                    OccasionType occasionType,
                                                    String occasionName,
                                                    TemplateCode templateCode,
                                                    Instant expiresAt,
                                                    List<MultipartFile> images,
                                                    MultipartFile voice) {
        validateText(title, content, occasionType, occasionName);

        Instant now = Instant.now(clock);
        Instant effectiveExpiresAt = expiresAt == null ? now.plus(7, ChronoUnit.DAYS) : expiresAt;
        if (!effectiveExpiresAt.isAfter(now)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "expiresAt must be future");
        }

        List<Media> media = validateAndBuildMedia(images, voice, 0);

        Message message = new Message(
                "msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                "pub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 18),
                "ed_" + UUID.randomUUID().toString().replace("-", "").substring(0, 18),
                title,
                content,
                occasionType,
                occasionName,
                templateCode,
                effectiveExpiresAt,
                now,
                now,
                media
        );

        repository.save(message);
        return new MessageDtos.CreateMessageResponse(
                message.getMessageId(),
                message.getEditToken(),
                message.getPublicToken(),
                message.getExpiresAt()
        );
    }

    public MessageDtos.UpdateMessageResponse update(String messageId, String editToken, MessageDtos.UpdateMessageRequest request) {
        Message message = findMessage(messageId);
        verifyEditToken(message, editToken);
        verifyNotExpired(message);

        Instant now = Instant.now(clock);
        if (!request.expiresAt().isAfter(now)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "expiresAt must be future");
        }

        message.update(request.title(), request.content(), request.templateCode(), request.expiresAt(), now);
        repository.save(message);
        return new MessageDtos.UpdateMessageResponse(message.getMessageId(), message.getUpdatedAt());
    }

    public Message getByPublicToken(String publicToken) {
        Message message = repository.findByPublicToken(publicToken)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "message not found"));

        if (!message.getExpiresAt().isAfter(Instant.now(clock))) {
            throw new AppException(ErrorCode.LINK_EXPIRED, HttpStatus.GONE, "link expired");
        }
        return message;
    }

    public void addImages(String messageId, String editToken, List<MultipartFile> images) {
        Message message = findMessage(messageId);
        verifyEditToken(message, editToken);
        verifyNotExpired(message);

        List<MultipartFile> normalizedImages = Objects.requireNonNullElse(images, List.of());
        validateAndBuildMedia(normalizedImages, null, message.totalUploadSize());

        int nextSort = message.imageCount() + 1;
        for (MultipartFile image : normalizedImages) {
            if (image.isEmpty()) {
                continue;
            }
            message.addImage(toMedia(MediaType.IMAGE, image, nextSort++));
        }

        repository.save(message);
    }

    public void upsertVoice(String messageId, String editToken, MultipartFile voice) {
        Message message = findMessage(messageId);
        verifyEditToken(message, editToken);
        verifyNotExpired(message);

        long sizeWithoutExistingVoice = message.totalUploadSize() - (message.voice() == null ? 0 : message.voice().fileSizeBytes());
        validateAndBuildMedia(List.of(), voice, sizeWithoutExistingVoice);
        if (voice != null && !voice.isEmpty()) {
            message.replaceVoice(toMedia(MediaType.VOICE, voice, null));
            repository.save(message);
        }
    }

    public void deleteMedia(String messageId, String mediaId, String editToken) {
        Message message = findMessage(messageId);
        verifyEditToken(message, editToken);
        verifyNotExpired(message);

        boolean removed = message.removeMedia(mediaId, Instant.now(clock));
        if (!removed) {
            throw new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "media not found");
        }

        repository.save(message);
    }

    private Message findMessage(String messageId) {
        return repository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "message not found"));
    }

    private void verifyEditToken(Message message, String editToken) {
        if (!StringUtils.hasText(editToken) || !message.getEditToken().equals(editToken)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_EDIT_TOKEN, HttpStatus.UNAUTHORIZED, "invalid edit token");
        }
    }

    private void verifyNotExpired(Message message) {
        if (!message.getExpiresAt().isAfter(Instant.now(clock))) {
            throw new AppException(ErrorCode.MESSAGE_EXPIRED, HttpStatus.FORBIDDEN, "message expired");
        }
    }

    private void validateText(String title, String content, OccasionType occasionType, String occasionName) {
        if (!StringUtils.hasText(title)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "title is required");
        }
        if (!StringUtils.hasText(content) || content.length() > 2000) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "content is invalid");
        }
        if (occasionType == OccasionType.CUSTOM && !StringUtils.hasText(occasionName)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "occasionName is required for CUSTOM");
        }
    }

    private List<Media> validateAndBuildMedia(List<MultipartFile> images, MultipartFile voice, long existingSizeBytes) {
        List<MultipartFile> normalizedImages = Optional.ofNullable(images)
                .orElseGet(List::of)
                .stream()
                .filter(file -> !file.isEmpty())
                .toList();

        if (normalizedImages.size() > 3) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "images can be up to 3");
        }

        long total = existingSizeBytes;
        List<Media> result = new ArrayList<>();
        int sortOrder = 1;
        for (MultipartFile image : normalizedImages) {
            if (image.getSize() > IMAGE_MAX_SIZE) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "image file size exceeds 3MB");
            }
            total += image.getSize();
            result.add(toMedia(MediaType.IMAGE, image, sortOrder++));
        }

        if (voice != null && !voice.isEmpty()) {
            if (voice.getSize() > VOICE_MAX_SIZE) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "voice file size exceeds 5MB");
            }
            total += voice.getSize();
            result.add(toMedia(MediaType.VOICE, voice, null));
        }

        if (total > TOTAL_MAX_SIZE) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "total upload size exceeds 14MB");
        }

        return result;
    }

    private Media toMedia(MediaType type, MultipartFile file, Integer sortOrder) {
        return new Media(
                "media_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                type,
                file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename(),
                file.getSize(),
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                "/uploads/" + UUID.randomUUID().toString().replace("-", "") + "/" + file.getOriginalFilename(),
                sortOrder
        );
    }
}
