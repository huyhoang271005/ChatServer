package social.chat.profile.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.cloudinary.api.events.CloudinaryRegisteredEvent;
import social.chat.profile.api.dto.ProfileShortDto;
import social.chat.profile.internal.cache.ProfileCache;
import social.chat.profile.internal.repository.EmailRepository;
import social.chat.shared.common.ApplicationProperties;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.dto.ResponseList;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.api.dto.ProfileDto;
import social.chat.profile.internal.entity.Profile;
import social.chat.profile.internal.mapper.ProfileMapper;
import social.chat.profile.internal.repository.ProfileRepository;
import social.chat.user.api.UserImp;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    UserImp userImp;
    AuthenticationImp authenticationImp;
    ApplicationEventPublisher applicationEventPublisher;
    EmailRepository emailRepository;
    ProfileCache profileCache;
    ApplicationProperties applicationProperties;

    @Transactional
    public Response<TokenDto> createProfile(Long userId, String fullName, Long deviceId) {
        userImp.checkUser(userId);
        if(profileRepository.existsById(userId)){
            throw new ConflictException(ProfileMessage.Profile.EXITS);
        }
        Profile profile = Profile.builder()
                .userId(userId)
                .fullName(fullName)
                .updated(false)
                .build();
        profileRepository.save(profile);
        return Response.success(
                GlobalMessage.Success.CREATED,
                authenticationImp.generateToken(userId, deviceId, null)
        );
    }

    @Transactional
    public Response<Void> updateProfile(Long userId, ProfileDto profileDto) {
        Profile profile = profileRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Profile.NOT_EXITS));
        if(profileDto.getFullName() != null) {
            profile.setFullName(profileDto.getFullName());
        }
        if( profile.getAvatarId() != null && !profileDto.getAvatarId().equals(profile.getAvatarId())) {
            CloudinaryRegisteredEvent event = new CloudinaryRegisteredEvent(List.of(profile.getAvatarId()));
            applicationEventPublisher.publishEvent(event);
        }
        if(profileRepository.existsByUsernameAndUserIdNot(profileDto.getUsername(), userId)){
            throw new ConflictException(ProfileMessage.Profile.USERNAME_EXISTS, profileDto.getUsername());
        }
        profileMapper.updateProfile(profileDto, profile);
        profile.setUpdated(true);
        profileCache.deleteShortProfile(userId);
        userImp.updateAccountStatusFromPendingToActive(profile.getUserId());
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<ProfileDto> getProfile(Long userId) {
        ProfileDto profileDto = profileMapper.toProfileDTO(profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Profile.NOT_EXITS)));
        if(profileDto.getAvatarId() == null) {
            profileDto.setAvatarUrl(applicationProperties.getUnknowUserUrl());
        }
        return Response.success(
                GlobalMessage.Success.GET,
                profileDto
        );
    }

    @Transactional
    public Response<ProfileShortDto> getProfileShort(Long userId) {
        List<ProfileShortDto> profileShortDtos = profileCache.getShortProfileByUserIds(List.of(userId));
        return Response.success(
                GlobalMessage.Success.GET,
                profileShortDtos.getFirst()
        );
    }

    @Transactional(readOnly = true)
    public Response<ResponseList<ProfileShortDto>> getProfiles(Long lastId, String emailName, Pageable pageable) {
        Slice<Long> userSlice = emailRepository.findUserIdsByEmailName(emailName, lastId, pageable);
        var profiles = profileRepository.getProfileInfo(userSlice.getContent());
        return Response.success(
                GlobalMessage.Success.GET,
                new ResponseList<>(
                        profiles
                                .stream()
                                .map(profileInfo -> {
                                    ProfileShortDto profileShortDto = profileMapper.toProfileShortDto(profileInfo);
                                    if(profileShortDto.getAvatarUrl() == null){
                                        profileShortDto.setAvatarUrl(applicationProperties.getUnknowUserUrl());
                                    }
                                    return profileShortDto;
                                })
                                .toList(),
                        userSlice.hasNext()
                )
        );
    }
}
