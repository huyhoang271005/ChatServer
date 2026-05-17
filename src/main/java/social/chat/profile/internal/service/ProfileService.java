package social.chat.profile.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthImp;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.exception.ConflictException;
import social.chat.exception.EntityNotFoundException;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.api.dto.ProfileDto;
import social.chat.profile.internal.entity.Profile;
import social.chat.profile.internal.mapper.ProfileMapper;
import social.chat.profile.internal.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    EmailService emailService;
    AuthImp authImp;

    @Transactional
    public Response<?> createProfile(Long userId, String fullName) {
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
        profileMapper.updateProfile(profileDto, profile);
        if(profileDto.getFullName() != null) {
            profile.setFullName(profileDto.getFullName());
        }
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }
}
