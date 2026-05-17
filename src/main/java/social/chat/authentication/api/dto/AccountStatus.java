package social.chat.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED;

    @JsonCreator
    public static AccountStatus fromValue(String text) {
        return AccountStatus.valueOf(text.toUpperCase());
    }
}
