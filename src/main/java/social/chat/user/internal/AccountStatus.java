package social.chat.user.internal;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    PENDING_PROFILE,
    BLOCKED;

    @JsonCreator
    public static AccountStatus fromValue(String text) {
        return AccountStatus.valueOf(text.toUpperCase());
    }
}
