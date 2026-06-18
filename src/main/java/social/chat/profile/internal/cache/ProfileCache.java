package social.chat.profile.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.profile.api.dto.ProfileInfo;
import social.chat.profile.internal.repository.ProfileRepository;
import social.chat.shared.common.ApplicationProperties;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileCache {
    ProfileRepository profileRepository;
    CacheManager cacheManager;
    ApplicationProperties applicationProperties;

    @Transactional
    public List<ProfileInfo> getShortProfileByUserIds(List<Long> userIds) {
        if(userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        List<ProfileInfo> result = new ArrayList<>();
        List<Long> missingUserIds = new ArrayList<>();

        Cache profileCache = cacheManager.getCache("profile");

        for(Long userId : userIds) {
            if(profileCache != null) {
                ProfileInfo profileShortDto = profileCache.get(userId, ProfileInfo.class);
                if(profileShortDto != null) {
                    result.add(profileShortDto);
                }
                else  {
                    missingUserIds.add(userId);
                }
            }
        }

        if(!missingUserIds.isEmpty()) {
            profileRepository.getProfileInfo(missingUserIds, applicationProperties.getUnknowUserUrl())
                    .forEach(profileInfo -> {
                        profileCache.put(profileInfo.userId(), profileInfo);
                        result.add(profileInfo);
                    });
        }
        return result;
    }

    @CacheEvict(cacheNames = "profile", key = "#userId")
    public void deleteShortProfile(Long userId) {}
}
