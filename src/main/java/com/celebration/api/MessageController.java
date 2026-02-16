package com.celebration.api;

import com.celebration.domain.Message;
import com.celebration.domain.OccasionType;
import com.celebration.domain.TemplateCode;
import com.celebration.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDtos.CreateMessageResponse createMessage(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam OccasionType occasionType,
            @RequestParam(required = false) String occasionName,
            @RequestParam TemplateCode templateCode,
            @RequestParam(required = false) Instant expiresAt,
            @RequestPart(required = false) List<MultipartFile> images,
            @RequestPart(required = false) MultipartFile voice
    ) {
        return messageService.create(title, content, occasionType, occasionName, templateCode, expiresAt, images, voice);
    }

    @PutMapping("/{messageId}")
    public MessageDtos.UpdateMessageResponse updateMessage(
            @PathVariable String messageId,
            @RequestHeader("X-Edit-Token") String editToken,
            @Valid @org.springframework.web.bind.annotation.RequestBody MessageDtos.UpdateMessageRequest request
    ) {
        return messageService.update(messageId, editToken, request);
    }

    @GetMapping("/public/{publicToken}")
    public MessageDtos.PublicMessageResponse getPublicMessage(@PathVariable String publicToken) {
        Message message = messageService.getByPublicToken(publicToken);
        return MessageDtos.PublicMessageResponse.from(message);
    }

    @PostMapping(path = "/{messageId}/images", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addImages(@PathVariable String messageId,
                          @RequestHeader("X-Edit-Token") String editToken,
                          @RequestPart List<MultipartFile> images) {
        messageService.addImages(messageId, editToken, images);
    }

    @PostMapping(path = "/{messageId}/voice", consumes = {"multipart/form-data"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertVoice(@PathVariable String messageId,
                            @RequestHeader("X-Edit-Token") String editToken,
                            @RequestPart MultipartFile voice) {
        messageService.upsertVoice(messageId, editToken, voice);
    }

    @DeleteMapping("/{messageId}/media/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedia(@PathVariable String messageId,
                            @PathVariable String mediaId,
                            @RequestHeader("X-Edit-Token") String editToken) {
        messageService.deleteMedia(messageId, mediaId, editToken);
    }
}
