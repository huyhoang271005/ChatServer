package social.chat.profile.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.exception.ConflictException;
import social.chat.exception.EntityNotFoundException;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailResponse;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.internal.entity.Email;
import social.chat.profile.internal.entity.Profile;
import social.chat.profile.internal.repository.EmailRepository;
import social.chat.profile.internal.repository.ProfileRepository;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileLogicService implements ProfileImp {
    EmailRepository emailRepository;
    ProfileRepository profileRepository;

    @Override
    @Transactional
    public void deleteProfileAndEmails(List<Long> userIds) {
        profileRepository.deleteAllById(userIds);
        emailRepository.deleteByUserIdIn(userIds);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return emailRepository.existsByEmailName(email);
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
    public void verifiedEmail(String emailName) {
        Email email = emailRepository.findByEmailName(emailName)
                .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Email.NOT_EXITS));
        email.setVerified(true);
    }
}
