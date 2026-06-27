package social.chat.message.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.message.api.dto.ReactorCacheDto;
import social.chat.message.internal.MessageMapper;
import social.chat.message.internal.cronjob.ReactorCronjobProperties;
import social.chat.message.internal.repository.MessageRepository;
import social.chat.message.internal.repository.ReactorRepository;
import social.chat.shared.cache.SafeCacheExecutor;
import social.chat.shared.common.GlobalParamName;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.REACTION)
public class ReactorCache {
    Set<Long> reactorIdsPending = ConcurrentHashMap.newKeySet();
    ReactorCronjobProperties reactorCronjobProperties;
    SafeCacheExecutor safeCacheExecutor;
    ReactorRepository reactorRepository;
    MessageMapper messageMapper;
    Lock reactorLock = new ReentrantLock();
    MessageRepository messageRepository;

    public Optional<List<ReactorCacheDto>> getReactorsCache(Collection<Long> reactorIds){
        return safeCacheExecutor.getCacheByIds(reactorIds, GlobalParamName.CacheName.REACTION,
                ReactorCacheDto.class, reactorLock, finalMissId ->
                        reactorRepository.findAllById(finalMissId)
                        .stream()
                        .map(messageMapper::toReactorDto)
                        .toList(), ReactorCacheDto::getReactorId);
    }

    public Optional<ReactorCacheDto> getReactorCacheByMessageIdAndUserId(Long userId,
                                                                         Long messageId){
        return safeCacheExecutor.getCacheWithSupplier(GlobalParamName.CacheName.REACTION,
                ReactorCacheDto.class, reactorLock, reactorIdsPending, reactorCacheDto ->
                reactorCacheDto.getUserId().equals(userId) && reactorCacheDto.getMessageId()
                        .equals(messageId), () -> reactorRepository.
                findByUserIdAndMessage_MessageId(userId, messageId)
                        .map(messageMapper::toReactorDto)
                        .orElse(null), ReactorCacheDto::getReactorId);
    }

    @CachePut(key = "#reactorId")
    public ReactorCacheDto updateReactor(Long reactorId, ReactorCacheDto reactorCacheDto){
        if(reactorId == null) return null;
        reactorIdsPending.add(reactorId);
        return reactorCacheDto;
    }

    @CacheEvict(key = "#reactorId")
    public void deleteReactor(Long reactorId){}

    public List<Long> getPendingReactorIds() {
        return safeCacheExecutor.getBatchPendingIds(reactorIdsPending,
                reactorCronjobProperties.getBatchSize());
    }

    public Collection<Long> getAllPendingReactorIds() {
        return safeCacheExecutor.getAllPendingIds(reactorIdsPending);
    }

    public List<ReactorCacheDto> getReactorsByMessageId(Collection<Long> reactorIds, Long messageId) {
        Collection<Long> finalReactorIds = safeCacheExecutor.getIdsByFKId(reactorIds,
                reactorIdsPending, GlobalParamName.CacheName.REACTION,
                ReactorCacheDto.class, reactorDto -> reactorDto.getMessageId()
                        .equals(messageId));
        return getReactorsCache(finalReactorIds)
                .orElse(List.of());
    }

    @Transactional
    public void saveData(Collection<Long> reactorIds){
        if(reactorIds.isEmpty()) {
            log.info("No reactor need save");
            return;
        }
        safeCacheExecutor.saveDataWithIds(reactorIds, reactorIdsPending, longs ->
                getReactorsCache(longs)
                        .orElse(List.of()), reactorDtos -> {
            Integer dataSave = reactorRepository.saveAll(reactorDtos
                    .stream()
                    .map(reactorCacheDto -> messageMapper.toReactor(reactorCacheDto,
                            messageRepository))
                    .toList()).size();
            log.info("Saved {} reactors", dataSave);
        });
    }
}
