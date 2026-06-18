package social.chat.authentication.api.dto;


public record DeviceDto (
    String deviceName,
    String deviceType,
    String userAgent
){}
