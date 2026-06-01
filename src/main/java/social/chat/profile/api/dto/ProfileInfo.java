package social.chat.profile.api.dto;

import social.chat.profile.internal.entity.Profile;

/**
 * Projection for {@link Profile}
 */
public interface ProfileInfo {
    Long getUserId();

    String getFullName();

    String getUsername();

    String getAvatarUrl();
}