package social.chat.profile.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import social.chat.profile.api.dto.ProfileInfo;
import social.chat.profile.internal.repository.ProfileRepository;
import social.chat.shared.common.ApplicationProperties;
import social.chat.shared.common.GlobalParamName;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.USER_SHORT_PROFILE)
public class ProfileCache {
    ProfileRepository profileRepository;
    CacheManager cacheManager;
    ApplicationProperties applicationProperties;

    public List<ProfileInfo> getShortProfileByUserIds(List<Long> userIds) {
        Cache profileCache = cacheManager.getCache(GlobalParamName.CacheName.USER_SHORT_PROFILE);

        List<ProfileInfo> result = new ArrayList<>();

        if(userIds.isEmpty() || profileCache == null) return result;

        List<Long> missingUserIds = new ArrayList<>();

        for(Long userId : userIds) {
            ProfileInfo profileInfo = profileCache.get(userId, ProfileInfo.class);
            if(profileInfo != null) {
                result.add(profileInfo);
            }
            else  {
                missingUserIds.add(userId);
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

    @CacheEvict(key = "#userId")
    public void deleteShortProfile(Long userId) {
        log.info("Deleted cache short profile for user {}", userId);
    }
}
