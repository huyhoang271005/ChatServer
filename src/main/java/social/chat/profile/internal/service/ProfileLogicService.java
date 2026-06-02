package social.chat.profile.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailResponse;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.internal.entity.Email;
import social.chat.profile.internal.entity.Profile;
import social.chat.profile.internal.repository.EmailRepository;
import social.chat.profile.internal.repository.ProfileRepository;
import social.chat.user.api.UserImp;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileLogicService implements ProfileImp {
    EmailRepository emailRepository;
    ProfileRepository profileRepository;
    UserImp userImp;

    @Override
    @Transactional(readOnly = true)
    public boolean existsEmailByEmailName(String email) {
        return emailRepository.existsByEmailName(email);
    }

    @Override
    public boolean existsProfileByUserId(Long userId) {
        return profileRepository.existsById(userId);
    }

    @Override
    @Transactional
    public void createEmail(String emailName, Long userId, boolean isVerified) {
        emailRepository.save(Email.builder()
                .emailName(emailName)
                .verified(isVerified)
                .userId(userId)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public String getFullName(Long userId) {
        Profile profile = profileRepository.findById(userId)
                .orElse(null);
        return profile != null ? profile.getFullName() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public EmailResponse getUserByEmail(String emailName) {
        Email email = emailRepository.findByEmailName(emailName)
                .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Email.NOT_EXITS));
        return EmailResponse.builder()
                .userId(email.getUserId())
                .emailId(email.getEmailId())
                .verified(email.getVerified())
                .build();
    }

    @Override
    @Transactional
    public void verifiedEmail(Long emailId) {
        Email email = emailRepository.findById(emailId)
                .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Email.NOT_EXITS));
        if(email.getVerified()){
            throw new ConflictException(ProfileMessage.Email.VERIFIED);
        }
        email.setVerified(true);
    }

    @Override
    @Transactional
    public Long getUserIdByEmail(String emailName) {
        Email email = emailRepository.findByEmailName(emailName)
                .orElseGet(() -> emailRepository.save(Email.builder()
                                .userId(userImp.getAndCreateUser())
                                .emailName(emailName)
                                .verified(true)
                        .build()));
        return email.getUserId();

    }

    @Override
    @Transactional(readOnly = true)
    public boolean getUpdated(Long userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Profile.NOT_EXITS));
        return profile.getUpdated();
    }

    @Override
    @Transactional
    public void deleteEmailAndProfileByUserIds(List<Long> userIds) {
        profileRepository.deleteAllById(userIds);
        emailRepository.deleteByUserIdIn(userIds);
    }
}
