package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.cache.FcmTokenCache;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Token;
import social.chat.authentication.internal.enums.TokenType;
import social.chat.authentication.internal.repository.DeviceRepository;
import social.chat.authentication.internal.repository.TokenRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.exception.UnprocessableException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    TokenRepository tokenRepository;
    DeviceRepository deviceRepository;
    FcmTokenCache fcmTokenCache;

    public Response<Boolean> notificationStatus(Long sessionId){
        return Response.success(
                GlobalMessage.Success.GET,
                tokenRepository.existsByDevice_DeviceIdAndTokenType(sessionId, TokenType.FCM_TOKEN)
        );
    }

    @Transactional
    public Response<Void> enableNotification(Long userId, Long deviceId, FirebaseLoginRequest firebaseLoginRequest,
                                             boolean enable){
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
        List<String> fcmCache = fcmTokenCache.getFcmTokenByUserIds(List.of(userId));
        if(enable){
            if(firebaseLoginRequest.fcmToken() == null) {
                throw new UnprocessableException(AuthenticationMessage.Notification.NEED_ENABLE);
            }
            Token token = tokenRepository.findByDeviceAndTokenType(device, TokenType.FCM_TOKEN)
                    .orElseGet(() -> Token.builder()
                            .device(device)
                            .tokenType(TokenType.FCM_TOKEN)
                            .build());
            token.setTokenValue(firebaseLoginRequest.fcmToken());
            tokenRepository.save(token);
            log.info("Enabled notification fcm");
            fcmCache.add(firebaseLoginRequest.fcmToken());
            fcmTokenCache.putFcmTokenByUserId(userId, fcmCache);
        }
        else {
            Integer tokenFcmDeleted = tokenRepository.deleteByDevice_DeviceIdAndTokenType(deviceId,
                    TokenType.FCM_TOKEN);
            log.info("Deleted {} fcm token", tokenFcmDeleted);
            fcmCache.remove(firebaseLoginRequest.fcmToken());
            fcmTokenCache.putFcmTokenByUserId(userId, fcmCache);
        }
        return Response.success(GlobalMessage.Success.UPDATED, null);
    }
}
