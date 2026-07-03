package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.shared.storage.api.events.CloudStorageDeleteEvent;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.api.dto.MemberDto;
import social.chat.conversation.api.dto.UserConversationDto;
import social.chat.conversation.internal.entity.Conversation;
import social.chat.conversation.internal.entity.UserConversation;
import social.chat.conversation.internal.repository.ConversationRepository;
import social.chat.conversation.internal.repository.UserConversationRepository;
import social.chat.message.api.MessageImp;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.api.dto.MessageType;
import social.chat.message.api.events.DeleteMessageEvent;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.ProfileInfo;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.dto.ResponseList;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.websocket.WebsocketEventType;
import social.chat.shared.websocket.WebsocketService;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationMapper conversationMapper;
    ConversationRepository conversationRepository;
    UserConversationRepository userConversationRepository;
    WebsocketService websocketService;
    ConversationCache conversationCache;
    ProfileImp profileImp;
    MessageImp messageImp;
    ApplicationEventPublisher applicationEventPublisher;

    private ConversationDto checkRole(Long userId, Long conversationId, ConversationPermission permission) {
        ConversationDto conversationDtoCurrent = conversationCache
                .getConversationsCache(List.of(conversationId))
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.NOT_EXISTS))
                .getFirst();
        ConversationRole role = conversationDtoCurrent.getUserConversations()
                .stream()
                .filter(userConversationDto -> userConversationDto.getUserId()
                        .equals(userId))
                .findAny()
                .orElseThrow(() -> new ConflictException(ConversationMessage.USER_NOT_IN))
                .getConversationRole();
        if(!role.roleCheck(permission)){
            throw new ConflictException(ConversationMessage.FORBIDDEN);
        }
        return conversationDtoCurrent;
    }

    private String getStringChangeMember(String myName, List<String> namesChange) {
        String names = String.join(", ", namesChange);
        return "%s {%s}".formatted(myName, names);
    }

    @Transactional
    public Response<ConversationDto> createConversation(Long myId, ConversationDto conversationDto) {
        Conversation conversation =  conversationMapper.toConversation(conversationDto);
        conversation.setGroup(conversationDto.getUserConversations().size() > 1);
        conversation.setCreatedBy(myId);
        Instant now = Instant.now();
        conversation.setUpdatedAt(now);
        conversation.setCreatedAt(now);
        conversation.setUserConversations(null);
        conversation.setRolesCanChat(Arrays.stream(ConversationRole
                .values()).toList());
        List<Long> userIdsInConversation = new ArrayList<>();
        userIdsInConversation.add(myId);
        userIdsInConversation.addAll(conversationDto.getUserConversations()
                .stream()
                .map(UserConversationDto::getUserId)
                .toList());
        if(!conversation.isGroup()){
            conversation.setConversationAvatarUrl(null);
            conversation.setTitle(null);
            if(conversationRepository.existsByGroupAndAllUsers(false,
                    userIdsInConversation)) {
                throw new ConflictException(ConversationMessage.EXISTS);
            }
        } else {
            if(conversation.getTitle() == null){
                List<ProfileInfo> profileInfos = profileImp.getShortProfiles(userIdsInConversation);
                conversation.setTitle(profileInfos
                        .stream()
                        .limit(5)
                        .map(ProfileInfo::fullName)
                        .collect(Collectors.joining(", ")));
            }
        }
        conversationRepository.save(conversation);
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
                conversationDto.setConversationAvatarUrl(profileShortDto.avatarUrl());
            }

        }

        conversationCache.updateConversation(conversationDto.getConversationId(), conversationDto);
        websocketService.sendMessageToConversation(myId, null, WebsocketEventType.UPDATE_CONVERSATION,
                conversationDto.getConversationId(), null);
        return Response.success(
                GlobalMessage.Success.CREATED,
                ConversationDto.builder()
                        .conversationId(conversationDto.getConversationId())
                        .build()
        );
    }

    public Response<Void> updateConversation(Long userId, ConversationDto conversationDto){
        ConversationDto conversationDto1 = checkRole(userId, conversationDto.getConversationId(),
                ConversationPermission.UPDATE_CONVERSATION);

        Instant updatedAt = Instant.now();

        conversationMapper.updateConversation(conversationDto, conversationDto1);
        conversationDto1.setUpdatedAt(updatedAt);

        conversationCache.updateConversation(conversationDto1.getConversationId(), conversationDto1);
        websocketService.sendMessageToConversation(userId, null, WebsocketEventType.UPDATE_CONVERSATION,
                conversationDto1.getConversationId(), null);
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }

    public Response<ResponseList<ConversationDto>> getConversations(Long myId, Long lastId, Pageable pageable){
        Slice<Long> conversationIdSlice = conversationRepository.findConversationIdsByUserId(myId, lastId, pageable);
        List<ConversationDto> conversationDtos = conversationCache.getConversationsByUserId(conversationIdSlice
                .getContent(), myId);
        return Response.success(
                GlobalMessage.Success.GET,
                new ResponseList<>(conversationDtos, conversationIdSlice.hasNext())
        );
    }

    public Response<ConversationDto> getConversation(Long userId, Long conversationId){
        ConversationDto conversationDto = conversationCache.getConversationsCache(List.of(conversationId))
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.NOT_EXISTS))
                .getFirst();
        conversationDto.getUserConversations()
                .stream().map(UserConversationDto::getUserId)
                .filter(id -> id.equals(userId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.USER_NOT_IN));
        return Response.success(
                GlobalMessage.Success.GET,
                conversationDto
        );
    }

    @Transactional
    public Response<Void> addMember(Long userId, MemberDto memberDto){
        ConversationDto conversationDto = checkRole(userId, memberDto.conversationId(), ConversationPermission.ADD_MEMBER);

        Conversation conversation = conversationRepository.findById(memberDto.conversationId())
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.NOT_EXISTS));

        List<Long> userIdsExist = conversationDto.getUserConversations()
                .stream()
                .map(UserConversationDto::getUserId)
                .filter(id -> memberDto.userIds().contains(id))
                .toList();
        List<Long> allUserIds = new ArrayList<>(memberDto.userIds());
        allUserIds.add(userId);
        Map<Long, ProfileInfo> profileInfoMap = profileImp.getShortProfiles(allUserIds)
                .stream()
                .collect(Collectors.toMap(ProfileInfo::userId, Function.identity()));
        String nameConversation = allUserIds.stream()
                .map(userIdAdd -> profileInfoMap.get(userIdAdd).fullName())
                .collect(Collectors.joining(", "));
        if(!userIdsExist.isEmpty()){
            String nameExists = userIdsExist.stream()
                    .map(id -> profileInfoMap.get(id).fullName())
                    .collect(Collectors.joining(", "));
            throw new ConflictException(ConversationMessage.Member.EXISTS, nameExists);
        }
        List<UserConversation> userConversations = conversation.addUserConversations(memberDto.userIds(),
                ConversationRole.MEMBER);
        userConversationRepository.saveAll(userConversations);
        conversationDto.setGroup(true);
        conversationDto.setTitle(conversationDto.getTitle() == null ?
                nameConversation : conversationDto.getTitle());
        conversationDto.setUpdatedAt(Instant.now());
        conversationDto.getUserConversations()
                        .addAll(userConversations
                                .stream()
                                .filter(userConversation -> memberDto.userIds().contains(userConversation.getUserId()))
                                .map(userConversation -> {
                                    UserConversationDto userConversationDto = conversationMapper.toUserConversationDto(userConversation);
                                    ProfileInfo profileInfo = profileInfoMap.get(userConversationDto.getUserId());
                                    userConversationDto.setFullName(profileInfo.fullName());
                                    userConversationDto.setUsername(profileInfo.username());
                                    userConversationDto.setAvatarUrl(profileInfo.avatarUrl());
                                    return userConversationDto;
                                })
                                .toList());

        conversationCache.updateConversation(conversationDto.getConversationId(), conversationDto);
        //Send event update conversation
        websocketService.sendMessageToConversation(userId, null, WebsocketEventType.UPDATE_CONVERSATION,
                conversationDto.getConversationId(), null);
        //Send event new member join conversation
        List<String> nameAdded = memberDto.userIds()
                .stream()
                .map(id -> profileInfoMap.get(id).fullName())
                .toList();
        MessageDto messageDto = MessageDto.builder()
                .text(getStringChangeMember(profileInfoMap.get(userId)
                        .fullName(), nameAdded))
                .conversationId(memberDto.conversationId())
                .type(MessageType.ADD_MEMBER)
                .build();
        messageImp.sendMessage(userId, null, messageDto);
        return Response.success(
                GlobalMessage.Success.CREATED,
                null
        );
    }

    @Transactional
    public Response<Void> removeMember(Long userId, Long conversationId, Long userRemoveId){
        ConversationDto conversationDto = checkRole(userId, conversationId, ConversationPermission.REMOVE_MEMBER);

        int memberCount = conversationDto.getUserConversations().size();
        if(memberCount < 3){
            throw new ConflictException(ConversationMessage.Member.TOO_LOW);
        }
        Map<Long, ProfileInfo> profileInfoMap = profileImp.getShortProfiles(List.of(userRemoveId, userId))
                .stream()
                .collect(Collectors.toMap(ProfileInfo::userId, Function.identity()));
        UserConversationDto userConversationDto = conversationDto.getUserConversations()
                .stream()
                .filter(ucd -> ucd.getUserId()
                        .equals(userRemoveId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.Member.NOT_EXISTS,
                        profileInfoMap.get(userId).fullName()));
        if(userConversationDto.getConversationRole() == ConversationRole.CREATOR) {
            throw new ConflictException(ConversationMessage.Member.EXISTS);
        }
        userConversationRepository.deleteById(userConversationDto.getUserConversationId());
        conversationDto.setUpdatedAt(Instant.now());
        conversationDto.getUserConversations().remove(userConversationDto);
        if(memberCount == 3){
            conversationDto.setGroup(false);
            if(conversationDto.getConversationAvatarUrl() != null){
                applicationEventPublisher.publishEvent(new CloudStorageDeleteEvent(List.of(conversationDto
                        .getConversationAvatarUrl())));
            }
            conversationDto.setConversationAvatarUrl(null);
            conversationDto.setTitle(null);
        }
        conversationCache.updateConversation(conversationDto.getConversationId(), conversationDto);
        websocketService.sendMessageToUser(userId, userRemoveId, null,
                WebsocketEventType.DELETE_CONVERSATION,
                conversationDto);
        websocketService.sendMessageToConversation(userId, null,
                WebsocketEventType.UPDATE_CONVERSATION, conversationId, null);
        MessageDto messageDto = MessageDto.builder()
                .type(MessageType.REMOVE_MEMBER)
                .conversationId(conversationId)
                .text(getStringChangeMember(profileInfoMap.get(userId).fullName(),
                        List.of(profileInfoMap.get(userRemoveId).fullName())))
                .build();
        messageImp.sendMessage(userId, null, messageDto);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }

    public Response<Void> changeConversationRole(Long myId, Long userId, Long conversationId,
                                                 UserConversationDto userConversationDto){
        ConversationDto conversationDto = checkRole(myId, conversationId,
                ConversationPermission.CHANGE_ROLE_MEMBER);
        conversationDto.getUserConversations()
                .stream()
                .filter(userConversationDto1 -> userConversationDto1.getUserId()
                        .equals(userId))
                .findAny()
                .ifPresent(userConversationDto1 -> {
                    UserConversation userConversation = userConversationRepository
                            .findById(userConversationDto1.getUserConversationId())
                            .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.USER_NOT_IN));
                    userConversation.setConversationRole(userConversationDto.getConversationRole());
                    userConversationRepository.save(userConversation);
                    userConversationDto1.setConversationRole(userConversationDto.getConversationRole());
                    conversationCache.updateConversation(conversationId, conversationDto);
                });
        websocketService.sendMessageToConversation(myId, null,
                WebsocketEventType.UPDATE_CONVERSATION, conversationId, null);
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }


    @Transactional
    public Response<Void> closeConversation(Long userId, Long conversationId){
        checkRole(userId, conversationId, ConversationPermission.DISBAND_CONVERSATION);
        conversationCache.getConversationsCache(List.of(conversationId))
                .ifPresent(conversationDtos -> {
                    conversationRepository.deleteById(conversationId);
                    websocketService.sendMessageToConversation(userId, null, WebsocketEventType
                            .DELETE_CONVERSATION, conversationId, null);
                    conversationCache.deleteConversation(conversationId);
                    applicationEventPublisher.publishEvent(new DeleteMessageEvent(conversationId));
                });
        return Response.success(
                GlobalMessage.Success.DELETED,
                null);
    }

    private void changeToRoleCreated(ConversationDto conversationDto){
        List<UserConversationDto> userConversationDtos = conversationDto.getUserConversations();
        Optional<UserConversationDto> creatorNext = userConversationDtos
                .stream()
                .filter(userConversationDto -> userConversationDto.getConversationRole() ==
                        ConversationRole.ADMIN)
                .min(Comparator.comparing(UserConversationDto::getJoinedAt));
        if(creatorNext.isEmpty())
            creatorNext = userConversationDtos
                    .stream()
                    .min(Comparator.comparing(UserConversationDto::getJoinedAt));
        creatorNext.ifPresent(userConversationDto ->{
            userConversationDto.setConversationRole(ConversationRole.CREATOR);
            userConversationRepository.save(conversationMapper.toUserConversation(userConversationDto));
        });
    }

    @Transactional
    public Response<Void> leaveConversation(Long userId, Long conversationId){
        ConversationDto conversationDto = conversationCache.getConversationsCache(List.of(conversationId))
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.NOT_EXISTS))
                .getFirst();
        UserConversationDto myUserconversationDto = conversationDto.getUserConversations()
                .stream().filter(userConversationDto -> userConversationDto.getUserId()
                        .equals(userId))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.USER_NOT_IN));
        if(myUserconversationDto.getConversationRole() == ConversationRole.CREATOR){
            changeToRoleCreated(conversationDto);
        }
        userConversationRepository.deleteById(myUserconversationDto.getUserConversationId());
        conversationDto.getUserConversations().remove(myUserconversationDto);
        websocketService.sendMessageToConversation(userId, null,
                WebsocketEventType.UPDATE_CONVERSATION, conversationId, null);
        ProfileInfo profileInfo = profileImp.getShortProfiles(List.of(userId))
                .getFirst();
        MessageDto messageDto = MessageDto.builder()
                .text(profileInfo.fullName())
                .type(MessageType.LEAVED)
                .conversationId(conversationId)
                .build();
        messageImp.sendMessage(userId, null, messageDto);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }


}
