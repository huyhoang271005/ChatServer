package social.chat.user.api.dto;

import org.springframework.modulith.NamedInterface;
import social.chat.user.internal.AccountStatus;

import java.time.Instant;

@NamedInterface
public record UserCacheDto (
    Long roleId,
    AccountStatus accountStatus,
    Instant expireAt
){}
