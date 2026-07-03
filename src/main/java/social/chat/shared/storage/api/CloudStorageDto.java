package social.chat.shared.storage.api;

public record CloudStorageDto (
        String originalFileName,
        String imageUrl,
        String contentType,
        String contentDisposition,
        String presignedUrl
) {}
