package social.chat.shared.websocket;

import com.google.firebase.messaging.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.message.api.dto.MessageType;
import social.chat.shared.common.ApplicationProperties;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.common.ResponseTranslationAdvice;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebsocketService {
    WebsocketProperties websocketProperties;
    SimpMessagingTemplate simpMessagingTemplate;
    AuthenticationImp authenticationImp;
    ApplicationProperties applicationProperties;
    ResponseTranslationAdvice responseTranslationAdvice;

    public void sendMessage(List<Long> userIds, Long myUserId, DataDto dataDto) {
        userIds.forEach(userId ->{
            String topicSubscribe = websocketProperties.getBrokerPaths()
                    .getFirst() + "/users." + userId;
            simpMessagingTemplate.convertAndSend(topicSubscribe, dataDto);
            log.info("Send message to topic {}", topicSubscribe);
        });
        List<String> fcmTokens = authenticationImp.getFcmTokenByUserIds(userIds
                .stream()
                .filter(userId -> !userId.equals(myUserId))
                .toList());
        if(fcmTokens != null && !fcmTokens.isEmpty()) {
            String link = "%s/#home?conversationId=%s"
                    .formatted(applicationProperties.getFrontendUrl(),
                            dataDto.getConversation().getConversationId());
            String icon = dataDto.getConversation().getConversationAvatar() == null ?
                    applicationProperties.getUnknowUserUrl() : dataDto.getConversation().getConversationAvatar();
            String image = dataDto.getMessage().getType() ==  MessageType.IMAGE ?
                    dataDto.getMessage().getText() : null;
            String body = switch (dataDto.getMessage().getType()) {
                case IMAGE -> responseTranslationAdvice.getString(GlobalMessage.Message.IMAGE);
                case VIDEO -> responseTranslationAdvice.getString(GlobalMessage.Message.VIDEO);
                case FILE -> responseTranslationAdvice.getString(GlobalMessage.Message.FILE);
                case AUDIO -> responseTranslationAdvice.getString(GlobalMessage.Message.AUDIO);
                default -> dataDto.getMessage().getText();
            };
            var multicastMessageBuild = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .putData("title", dataDto.getConversation().getTitle())
                    .putData("body", dataDto.getMessage().getText())
                    .putData("link", link);
            if(image != null) {
                multicastMessageBuild.putData("image", image);
            }
            if(icon != null) {
                multicastMessageBuild.putData("icon", icon);
            }
            try {
                FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessageBuild.build());
            } catch (FirebaseMessagingException e) {
                log.error(e.getMessage());
            }
        }
    }
}
