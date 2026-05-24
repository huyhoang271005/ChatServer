package social.chat.profile.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthImp;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.cloudinary.api.CloudinaryImp;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.exception.ConflictException;
import social.chat.exception.EntityNotFoundException;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.api.dto.ProfileDto;
import social.chat.profile.internal.entity.Profile;
import social.chat.profile.internal.mapper.ProfileMapper;
import social.chat.profile.internal.repository.ProfileRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    AuthImp authImp;
    CloudinaryImp cloudinaryImp;

    @Transactional
    public Response<TokenDto> createProfile(Long userId, String fullName) {
        authImp.checkUser(userId);
        if(profileRepository.existsById(userId)){
            throw new ConflictException(ProfileMessage.Profile.EXITS);
        }
        Profile profile = Profile.builder()
                .userId(userId)
                .fullName(fullName)
                .build();
        profileRepository.save(profile);
        return Response.success(
                GlobalMessage.Success.CREATED,
                authImp.generateToken(userId, Long.MIN_VALUE)
        );
    }

    @Transactional
    public Response<Void> updateProfile(Long userId, ProfileDto profileDto) {
        Profile profile = profileRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Profile.NOT_EXITS));
        if(profileDto.getFullName() != null) {
            profile.setFullName(profileDto.getFullName());
        }
        if( profileDto.getAvatarId() != null && !profileDto.getAvatarId().equals(profile.getAvatarId())) {
            if(!cloudinaryImp.deleteImage(profile.getAvatarId())){
                log.error("Delete image {} failed", profile.getAvatarId());
                throw new ConflictException(GlobalMessage.Error.INTERNAL);
            }
        }
        profileMapper.updateProfile(profileDto, profile);
        authImp.updateAccountStatusFromPendingToActive(profile.getUserId());
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }
}
