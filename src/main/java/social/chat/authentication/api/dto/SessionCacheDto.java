package social.chat.authentication.api.dto;

public record SessionCacheDto (
    boolean revoked,
    String ipAddress
){}
