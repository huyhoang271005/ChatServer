package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.api.dto.UserConversationDto;
import social.chat.conversation.internal.entity.Conversation;
import social.chat.conversation.internal.repository.ConversationRepository;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.ProfileInfo;
import social.chat.shared.common.ApplicationProperties;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.dto.ResponseList;
import social.chat.shared.websocket.DataDto;
import social.chat.shared.websocket.WebsocketEventType;
import social.chat.shared.websocket.WebsocketService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationMapper conversationMapper;
    ConversationRepository conversationRepository;
    WebsocketService websocketService;
    ConversationCache conversationCache;
    ProfileImp profileImp;
    ApplicationProperties applicationProperties;


    @Transactional
    public Response<ConversationDto> createConversation(Long myId, ConversationDto conversationDto) {
        Conversation conversation =  conversationMapper.toConversation(conversationDto);
        conversation.setGroup(conversationDto.getUserConversations().size() > 1);
        conversation.setCreatedBy(myId);
        if(!conversation.isGroup()){
            conversation.setConversationAvatar(null);
            conversation.setTitle(null);
        }
        conversation.addUserConversations(List.of(myId), ConversationRole.CREATOR);
        List<Long> userIds = conversationDto.getUserConversations()
                .stream()
                .map(UserConversationDto::getUserId)
                .toList();
        conversation.addUserConversations(userIds, ConversationRole.MEMBER);
        conversationDto = conversationMapper.toConversationDto(conversationRepository.save(conversation));
        if(!conversationDto.isGroup()){
            List<ProfileInfo> profileShortDtos = profileImp.getShortProfiles(List.of(myId));
            if(!profileShortDtos.isEmpty()){
                ProfileInfo profileShortDto = profileShortDtos.getFirst();
                conversationDto.setConversationAvatar(profileShortDto.avatarUrl());
                conversationDto.setTitle(profileShortDto.fullName());
            }

        }
        DataDto payload = DataDto.builder()
                .type(WebsocketEventType.NEW_CONVERSATION)
                .conversation(conversationDto)
                .build();
        websocketService.sendMessage(myId, payload);
        return Response.success(
                GlobalMessage.Success.CREATED,
                ConversationDto.builder()
                        .conversationId(conversationDto.getConversationId())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Response<ResponseList<ConversationDto>> getConversation(Long myId, Long lastId, Pageable pageable){
        Slice<Long> conversationIdSlice = conversationRepository.findConversationIdsByUserId(myId, lastId, pageable);
        List<ConversationDto> conversationDtos = conversationCache.getConversations(conversationIdSlice.getContent());
        List<Long> userIdsPrivateConversations = conversationDtos.stream()
                .filter(conversationDto -> !conversationDto.isGroup())
                .flatMap(conversationDto -> conversationDto.getUserConversations().stream()
                        .map(UserConversationDto::getUserId))
                .distinct()
                .toList();
        Map<Long, ProfileInfo> profileShortDtoMap = profileImp.getShortProfiles(userIdsPrivateConversations)
                .stream()
                .collect(Collectors.toMap(ProfileInfo::userId, Function.identity()));
        conversationDtos.forEach(conversationDto -> {
            Integer unreadMessage = conversationDto.getUserConversations()
                    .stream()
                    .filter(userConversationDto -> userConversationDto.getUserId().equals(myId))
                    .findAny()
                    .map(UserConversationDto::getUnreadMessage)
                    .orElse(0);
            conversationDto.setUnreadMessage(unreadMessage);
            conversationDto.getUserConversations()
                    .forEach(userConversationDto -> {
                        ProfileInfo profileInfo = profileShortDtoMap.get(userConversationDto.getUserId());
                        userConversationDto.setFullName(profileInfo.fullName());
                        userConversationDto.setAvatarUrl(profileInfo.avatarUrl());
                        userConversationDto.setUsername(profileInfo.username());
                    });
           if(!conversationDto.isGroup()){
               Long userId = conversationDto.getUserConversations()
                       .stream()
                       .filter(userConversationDto ->  !userConversationDto.getUserId()
                               .equals(myId))
                       .toList()
                       .getFirst()
                       .getUserId();
               ProfileInfo profileShortDto = profileShortDtoMap.get(userId);
               String avatarUrl = profileShortDto.avatarUrl() != null ?  profileShortDto.avatarUrl() :
                       applicationProperties.getUnknowUserUrl();
               conversationDto.setConversationAvatar(avatarUrl);
               conversationDto.setTitle(profileShortDto.fullName());
           }
        });
        return Response.success(
                GlobalMessage.Success.GET,
                new ResponseList<>(conversationDtos, conversationIdSlice.hasNext())
        );
    }
}
