package social.chat.profile.api.dto;

import social.chat.profile.internal.entity.Profile;

import java.io.Serializable;

/**
 * Projection for {@link Profile}
 */
public record ProfileInfo(
        Long userId,

        String fullName,

        String username,

        String avatarUrl
) implements Serializable {}