package social.chat.user.api.dto;

import social.chat.user.internal.AccountStatus;

import java.time.Instant;

public record ExtendUser (
    Long userId,
    AccountStatus accountStatus,
    Long roleId,
    Instant expireAt
){}
