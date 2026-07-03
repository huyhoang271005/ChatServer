package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.api.dto.UserConversationDto;
import social.chat.conversation.internal.cronjob.ConversationCronjobProperties;
import social.chat.conversation.internal.repository.ConversationRepository;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.ProfileInfo;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.cache.SafeCacheExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.CONVERSATION)
public class ConversationCache {
    ConversationRepository conversationRepository;
    ConversationMapper conversationMapper;
    Set<Long> conversationPending = ConcurrentHashMap.newKeySet();
    ConversationCronjobProperties conversationCronjobProperties;
    ProfileImp profileImp;
    SafeCacheExecutor safeCacheExecutor;
    Lock conversationLock = new ReentrantLock();

    public Optional<List<ConversationDto>> getConversationsCache(Collection<Long> conversationIds) {
       return safeCacheExecutor.getCacheByIds(
                conversationIds, GlobalParamName.CacheName.CONVERSATION,
                ConversationDto.class, conversationLock, missIds -> {
                    List<ConversationDto> conversationDtos = conversationRepository
                            .findByConversationIds(missIds)
                            .stream()
                            .map(conversationMapper::toConversationDto)
                            .toList();
                    List<Long> userIds = conversationDtos
                            .stream()
                            .flatMap(conversationDto -> conversationDto.getUserConversations().stream())
                            .map(UserConversationDto::getUserId)
                            .distinct()
                            .toList();
                    Map<Long, ProfileInfo> profileInfos = profileImp.getShortProfiles(userIds)
                            .stream()
                            .collect(Collectors.toMap(ProfileInfo::userId, Function.identity()));
                    conversationDtos.stream()
                            .flatMap(conversationDto -> conversationDto.getUserConversations().stream())
                            .forEach(userConversationDto -> {
                                ProfileInfo profileInfo = profileInfos.get(userConversationDto.getUserId());
                                userConversationDto.setUsername(profileInfo.username());
                                userConversationDto.setAvatarUrl(profileInfo.avatarUrl());
                                userConversationDto.setFullName(profileInfo.fullName());
                            });
                    log.info("Added cache for {} conversations", missIds.size());
                    return conversationDtos;
                }, ConversationDto::getConversationId
        );
    }

    public List<ConversationDto> getConversationsByUserId(Collection<Long> conversationIds, Long userId) {
        Collection<Long> finalConversationsIds = safeCacheExecutor.getIdsByFKId(
                conversationIds, conversationPending, GlobalParamName.CacheName.CONVERSATION,
                ConversationDto.class, conversationDto -> conversationDto
                        .getUserConversations()
                        .stream().anyMatch(userConversationDto ->
                                userConversationDto.getUserId().equals(userId))
        );
        return getConversationsCache(finalConversationsIds.stream().toList())
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(ConversationDto::getUpdatedAt).reversed())
                .toList();

    }

    @CachePut(key = "#conversationId")
    @Transactional
    public ConversationDto updateConversation(Long conversationId, ConversationDto conversationDto) {
        if(conversationId == null) return null;
        conversationPending.add(conversationId);
        return conversationDto;
    }

    @CacheEvict(key = "#conversationId")
    public void deleteConversation(Long conversationId) {
        log.info("Deleting conversation cache {}", conversationId);
    }

    public List<Long> getPendingConversationIds() {
        return safeCacheExecutor.getBatchPendingIds(conversationPending,
                conversationCronjobProperties.getBatchSize());
    }

    public Collection<Long> getAllPendingConversationIds() {
        return safeCacheExecutor.getAllPendingIds(conversationPending);
    }

    @Transactional
    public void saveData(Collection<Long> conversationIds) {
        if(conversationIds.isEmpty()){
            log.info("No conversations need save");
            return;
        }
        safeCacheExecutor.saveDataWithIds(conversationIds, conversationPending,
                this::getConversationsCache, conversationDtos -> {
            Integer dataSave = conversationRepository.saveAll(conversationDtos
                    .stream()
                    .map(conversationMapper::toConversation)
                    .toList()).size();
            log.info("Saved {} conversations", dataSave);
                });
    }


}
