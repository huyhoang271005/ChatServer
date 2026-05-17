package social.chat.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleDefault {
    ADMIN,
    USER,
    PENDING_PROFILE;

    @JsonCreator
    public static RoleDefault forValue(String value) {
        return RoleDefault.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
