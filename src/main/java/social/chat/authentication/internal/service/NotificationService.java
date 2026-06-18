package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.entity.Token;
import social.chat.authentication.internal.enums.TokenType;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.authentication.internal.repository.TokenRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.exception.UnprocessableException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    TokenRepository tokenRepository;
    SessionRepository sessionRepository;

    @Transactional
    public Response<Boolean> notificationStatus(Long sessionId){
        return Response.success(
                GlobalMessage.Success.GET,
                tokenRepository.existsBySession_SessionIdAndTokenType(sessionId, TokenType.FCM_TOKEN)
        );
    }

    @Transactional
    public Response<Void> enableNotification(Long sessionId, FirebaseLoginRequest firebaseLoginRequest,
                                             boolean enable){
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
        if(enable){
            if(firebaseLoginRequest.fcmToken() == null) {
                throw new UnprocessableException(AuthenticationMessage.Notification.NEED_ENABLE);
            }
            tokenRepository.findBySessionAndTokenType(session, TokenType.FCM_TOKEN)
                    .orElseGet(() -> tokenRepository.save(Token.builder()
                            .tokenValue(firebaseLoginRequest.fcmToken())
                            .session(session)
                            .tokenType(TokenType.FCM_TOKEN)
                            .build()));
            log.info("Enabled notification fcm");
        }
        else {
            Integer tokenFcmDeleted = tokenRepository.deleteBySession_SessionIdAndTokenType(sessionId, TokenType.FCM_TOKEN);
            log.info("Deleted {} fcm token", tokenFcmDeleted);
        }
        return Response.success(GlobalMessage.Success.UPDATED, null);
    }
}
