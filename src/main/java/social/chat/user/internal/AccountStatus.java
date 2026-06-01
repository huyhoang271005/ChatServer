package social.chat.user.internal;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    BANNED,
    PENDING_PROFILE,
    LOCKED;

    @JsonCreator
    public static AccountStatus fromValue(String text) {
        return AccountStatus.valueOf(text.toUpperCase());
    }
}
