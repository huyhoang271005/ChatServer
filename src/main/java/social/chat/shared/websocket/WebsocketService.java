package social.chat.shared.websocket;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import social.chat.authentication.api.FcmTokenImp;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.api.dto.UserConversationDto;
import social.chat.message.api.MessageCacheImp;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.api.dto.MessageType;
import social.chat.shared.common.ApplicationProperties;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.common.ResponseTranslationAdvice;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebsocketService {
    WebsocketProperties websocketProperties;
    SimpMessagingTemplate simpMessagingTemplate;
    FcmTokenImp fcmTokenImp;
    ApplicationProperties applicationProperties;
    ResponseTranslationAdvice responseTranslationAdvice;
    ConversationImp conversationImp;
    MessageCacheImp messageCacheImp;

    public void sendMessageToConversation(Long myUserId, String clientMsgId, WebsocketEventType type,
                                          Long conversationId, Long messageId) {
        ConversationDto conversationDto = conversationImp.getConversations(List.of(conversationId))
                .getFirst();
        List<UserConversationDto> userConversationDtos = conversationDto.getUserConversations();
        List<Long> userIds = userConversationDtos
                        .stream()
                                .map(UserConversationDto::getUserId)
                                        .toList();
        MessageDto messageDto;
        if(messageId == null) {
            messageDto = null;
        }
        else {
            List<MessageDto> list = messageCacheImp.getMessagesCache(List.of(messageId))
                    .orElse(null);
            if(list == null) {
                messageDto = null;
            }
            else {
                messageDto = list.getFirst();
            }
        }
        if(type == WebsocketEventType.SEEN_MESSAGE){
            if(messageDto == null) return;
            userIds = userIds.stream()
                    .filter(userId -> userId.equals(messageDto.getSenderId()))
                    .toList();
        }
        boolean conversationNotNull = type == WebsocketEventType.UPDATE_CONVERSATION  ||
                type == WebsocketEventType.TYPING || type == WebsocketEventType.UNTYPING ;
        userIds.forEach(userId -> {
            var data = conversationNotNull ? conversationDto : messageDto;
            if(type == WebsocketEventType.TYPING || type == WebsocketEventType.UNTYPING) {
                data = conversationId;
            }
            sendMessageToUser(myUserId, userId, clientMsgId, type, data);
        });
        log.info("List user id need send message {}", userIds);
        //Send notification
        if(type == WebsocketEventType.NEW_MESSAGE || type == WebsocketEventType.REVOKE_MESSAGE) {
            List<String> fcmTokens = fcmTokenImp.getFcmTokenByUserIds(userIds
                    .stream()
                    .filter(userId -> !userId.equals(myUserId))
                    .toList());
            Optional<UserConversationDto> myProfileInfo = userConversationDtos
                    .stream()
                    .filter(userConversationDto1 -> userConversationDto1
                            .getUserId().equals(myUserId))
                    .findAny();
            String myAvatar = myProfileInfo
                    .map(UserConversationDto::getAvatarUrl)
                    .orElseGet(applicationProperties::getUnknowUserUrl);

            if(fcmTokens != null && !fcmTokens.isEmpty()) {
                log.info("Found fcm token start sending message");
                String link = "%s/#home?conversationId=%s"
                        .formatted(applicationProperties.getFrontendUrl(),
                                conversationDto.getConversationId());
                String icon = conversationDto.isGroup() ?
                        conversationDto.getConversationAvatarUrl() : myAvatar;
                String image = null;
                String body = "REVOKE_MESSAGE";
                if(messageDto == null) return;
                if(messageDto.getType() != null && (messageDto.getRevoked() == null ||
                        !messageDto.getRevoked())) {
                    image = messageDto.getType() ==  MessageType.IMAGE ?
                            messageDto.getText() : null;
                    body = switch (messageDto.getType()) {
                        case IMAGE -> responseTranslationAdvice.getString(GlobalMessage.Message.IMAGE);
                        case VIDEO -> responseTranslationAdvice.getString(GlobalMessage.Message.VIDEO);
                        case FILE -> responseTranslationAdvice.getString(GlobalMessage.Message.FILE);
                        case AUDIO -> responseTranslationAdvice.getString(GlobalMessage.Message.AUDIO);
                        default -> messageDto.getText();
                    };
                }
                String title = conversationDto.isGroup() ? conversationDto.getTitle() :
                        myProfileInfo.map(UserConversationDto::getFullName).orElse(null);
                var multicastMessageBuild = MulticastMessage.builder()
                        .addAllTokens(fcmTokens)
                        .putData("body", body)
                        .putData("link", link)
                        .putData("senderId", myUserId.toString())
                        .putData("messageType", messageDto.getType().name())
                        .putData("tag", conversationId.toString());
                if(icon != null) {
                    multicastMessageBuild.putData("icon", icon);
                }
                if(title != null) {
                    multicastMessageBuild.putData("title", conversationDto.getTitle());
                }
                if(image != null) {
                    multicastMessageBuild.putData("image", image);
                }
                if(messageDto.getMessageId() != null) {
                    multicastMessageBuild.putData("messageId", messageDto
                            .getMessageId().toString());
                }
                try {
                    FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessageBuild.build());
                    log.info("Sent {} fcm message", fcmTokens.size());
                } catch (FirebaseMessagingException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public<T> void sendMessageToUser(Long myUserId, Long toUser, String clientMsgId, WebsocketEventType type,
                                  T data) {
        if(data == null) return;
        DataDto<T> dataDto = new DataDto<>(type, myUserId, clientMsgId, data);
        String topicSubscribe = "%s/stream".formatted(websocketProperties.getUserPath());
        simpMessagingTemplate.convertAndSendToUser(toUser.toString(), topicSubscribe, dataDto);
    }
}
