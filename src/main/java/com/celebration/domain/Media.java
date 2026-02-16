package com.celebration.domain;

public record Media(
        String mediaId,
        MediaType mediaType,
        String fileName,
        long fileSizeBytes,
        String mimeType,
        String url,
        Integer sortOrder
) {
}
