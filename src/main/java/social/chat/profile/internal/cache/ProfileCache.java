package social.chat.profile.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.profile.api.dto.ProfileShortDto;
import social.chat.profile.internal.mapper.ProfileMapper;
import social.chat.profile.internal.repository.ProfileRepository;
import social.chat.shared.common.ApplicationProperties;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileCache {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    CacheManager cacheManager;
    ApplicationProperties applicationProperties;

    @Transactional
    public List<ProfileShortDto> getShortProfileByUserIds(List<Long> userIds) {
        if(userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        List<ProfileShortDto> result = new ArrayList<>();
        List<Long> missingUserIds = new ArrayList<>();

        Cache profileCache = cacheManager.getCache("profile");

        for(Long userId : userIds) {
            if(profileCache != null) {
                ProfileShortDto profileShortDto = profileCache.get(userId, ProfileShortDto.class);
                if(profileShortDto != null) {
                    result.add(profileShortDto);
                }
                else  {
                    missingUserIds.add(userId);
                }
            }
        }

        if(!missingUserIds.isEmpty()) {
            profileRepository.getProfileInfo(missingUserIds)
                    .stream()
                    .map(profileInfo -> {
                        ProfileShortDto profileShortDto = profileMapper.toProfileShortDto(profileInfo);
                        if(profileShortDto.getAvatarUrl() == null){
                            profileShortDto.setAvatarUrl(applicationProperties.getUnknowUserUrl());
                        }
                        return profileShortDto;
                    })
                    .forEach(profileShortDto -> {
                        profileCache.put(profileShortDto.getUserId(), profileShortDto);
                        result.add(profileShortDto);
                    });
        }
        return result;
    }

    @CacheEvict(cacheNames = "profile", key = "#userId")
    public void deleteShortProfile(Long userId) {}
}
