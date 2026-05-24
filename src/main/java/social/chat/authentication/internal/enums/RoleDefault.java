package social.chat.authentication.internal.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleDefault {
    ADMIN,
    USER;

    @JsonCreator
    public static RoleDefault forValue(String value) {
        return RoleDefault.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
