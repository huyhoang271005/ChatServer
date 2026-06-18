package social.chat.shared.websocket;

import com.google.firebase.messaging.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.conversation.api.dto.UserConversationDto;
import social.chat.message.api.dto.MessageType;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.ProfileInfo;
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
    ProfileImp profileImp;

    public void sendMessage(Long myUserId, DataDto dataDto) {
        List<Long> userIds = dataDto.getConversation().getUserConversations()
                        .stream()
                                .map(UserConversationDto::getUserId)
                                        .toList();
        dataDto.setSenderId(myUserId);
        if(dataDto.getType() == WebsocketEventType.SEEN_MESSAGE){
            userIds = userIds.stream()
                    .filter(userId -> userId.equals(dataDto.getMessage().getSenderId()))
                    .toList();
        }
        userIds.forEach(userId ->{
            String topicSubscribe = websocketProperties.getBrokerPaths()
                    .getFirst() + "/users." + userId;
            simpMessagingTemplate.convertAndSend(topicSubscribe, dataDto);
            log.info("Send message to topic {} with type {}", topicSubscribe, dataDto.getType());
        });
        if(dataDto.getType() == WebsocketEventType.NEW_MESSAGE || dataDto.getType() == WebsocketEventType.REVOKE_MESSAGE) {
            List<String> fcmTokens = authenticationImp.getFcmTokenByUserIds(userIds
                    .stream()
                    .filter(userId -> !userId.equals(myUserId))
                    .toList());
            ProfileInfo profileInfo = profileImp.getShortProfiles(List.of(myUserId)).getFirst();
            if(fcmTokens != null && !fcmTokens.isEmpty()) {
                log.info("Found fcm token start sending message");
                String link = "%s/#home?conversationId=%s"
                        .formatted(applicationProperties.getFrontendUrl(),
                                dataDto.getConversation().getConversationId());
                String icon = dataDto.getConversation().isGroup() ?
                        dataDto.getConversation().getConversationAvatar() : profileInfo.avatarUrl();
                String image = null;
                String body = "REVOKE_MESSAGE";
                if(dataDto.getMessage().getType() != null) {
                    image = dataDto.getMessage().getType() ==  MessageType.IMAGE ?
                            dataDto.getMessage().getText() : null;
                    body = switch (dataDto.getMessage().getType()) {
                        case IMAGE -> responseTranslationAdvice.getString(GlobalMessage.Message.IMAGE);
                        case VIDEO -> responseTranslationAdvice.getString(GlobalMessage.Message.VIDEO);
                        case FILE -> responseTranslationAdvice.getString(GlobalMessage.Message.FILE);
                        case AUDIO -> responseTranslationAdvice.getString(GlobalMessage.Message.AUDIO);
                        default -> dataDto.getMessage().getText();
                    };
                }
                var multicastMessageBuild = MulticastMessage.builder()
                        .addAllTokens(fcmTokens)
                        .putData("title", dataDto.getConversation().getTitle())
                        .putData("body", body)
                        .putData("icon", icon)
                        .putData("link", link)
                        .putData("messageType", dataDto.getType().name());
                if(image != null) {
                    multicastMessageBuild.putData("image", image);
                }
                if(dataDto.getMessage() != null && dataDto.getMessage().getMessageId() != null) {
                    multicastMessageBuild.putData("messageId", dataDto.getMessage()
                            .getMessageId().toString());
                }
                try {
                    FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessageBuild.build());
                    log.info("Sent fcm message");
                } catch (FirebaseMessagingException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
