package social.chat.message.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.modulith.NamedInterface;

@NamedInterface
public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE;

    @JsonCreator
    public static MessageType forValue(String value) {
        return MessageType.valueOf(value.toUpperCase());
    }
}
