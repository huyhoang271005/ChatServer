package social.chat.profile.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.profile.api.dto.EmailDto;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.internal.entity.Email;
import social.chat.profile.internal.mapper.EmailMapper;
import social.chat.profile.internal.repository.EmailRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.user.api.UserImp;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class EmailService {
    EmailRepository emailRepository;
    EmailMapper emailMapper;
    UserImp userImp;

    @Transactional(readOnly = true)
    public Response<List<EmailDto>> getEmails(Long userId) {
        return Response.success(
                GlobalMessage.Success.GET,
                emailRepository.findByUserId(userId)
                        .stream()
                        .map(emailMapper::toEmailDto)
                        .toList()
        );
    }

    @Transactional
    public Response<EmailDto> createEmail(EmailDto emailDto, Long userId) {
        Email email = emailRepository.save(Email.builder()
                .emailName(emailDto.emailName())
                .userId(userId)
                .verified(false)
                .build());
        userImp.updateAccountStatusToInactive(userId);
        return Response.success(
                GlobalMessage.Success.CREATED,
                emailMapper.toEmailDto(email)
        );
    }

    @Transactional
    public Response<Void> deleteEmail(Long emailId){
        Email email = emailRepository.findById(emailId)
                .orElseThrow(() -> new EntityNotFoundException(ProfileMessage.Email.NOT_EXITS));
        List<Email> emails = emailRepository.findByUserId(email.getUserId());
        if(emails.size() < 2){
            throw new ConflictException(ProfileMessage.Email.ONLY_ONE);
        }
        emailRepository.delete(email);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }
}
