package social.chat.shared.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.*;
import java.util.stream.Collectors;

@NamedInterface
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SafeCacheExecutor {
    CacheManager cacheManager;

    private<T> List<Long> checkMissIds(Collection<Long> ids, Cache cache, Class<T> classType, List<T> result){
        List<Long> missIds = new ArrayList<>();
        for(Long id : ids){
            if(id == null) continue;
            T obj = cache.get(id, classType);
            if(obj == null){
                missIds.add(id);
            }
            else {
                result.add(obj);
            }
        }
        return missIds;
    }


    public<T> Optional<List<T>> getCacheByIds(Collection<Long> ids, String cacheName,
                                              Class<T> classType, Lock lock,
                                              Function<List<Long>, List<T>> dbLoader,
                                              Function<T, Long> getKey) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache == null){
            return Optional.empty();
        }
        List<T> result = new ArrayList<>();
        List<Long> missingIds = checkMissIds(ids, cache, classType, result);
        if(!missingIds.isEmpty()){
            lock.lock();
            try {
                List<Long> finalMissIds = checkMissIds(missingIds, cache, classType, result);
                if(!finalMissIds.isEmpty()){
                    List<T> responseDb = dbLoader.apply(finalMissIds);
                    responseDb.forEach(t -> cache.put(getKey.apply(t), t));
                    result.addAll(responseDb);
                }
            }
            finally {
                lock.unlock();
            }
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    private<T> Optional<T> checkCacheExists(Set<Long> idsPending, Cache cache, Class<T> classType,
                                            Predicate<T> predicate){
        List<T> listObj = new ArrayList<>();
        for(Long id : idsPending){
            T obj = cache.get(id, classType);
            if(obj != null){
                listObj.add(obj);
            }
        }
        return listObj.stream()
                .filter(predicate)
                .findAny();
    }

    public<T> Optional<T> getCacheWithSupplier(String cacheName, Class<T> classType, Lock lock,
                                                Set<Long> idsPending, Predicate<T> predicateCache,
                                               Supplier<T> dbLoader, Function<T, Long> getKey) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache == null){
            return Optional.empty();
        }
        T obj = checkCacheExists(idsPending, cache, classType, predicateCache)
                .orElseGet(() -> {
                    lock.lock();
                    try {
                        T dbLoad = checkCacheExists(idsPending, cache, classType, predicateCache)
                                .orElseGet(dbLoader);
                        if(dbLoad != null) cache.put(getKey.apply(dbLoad), dbLoad);
                        return dbLoad;
                    }
                    finally {
                        lock.unlock();
                    }
                });
        return obj != null ? Optional.of(obj) : Optional.empty();
    }

    public List<Long> getBatchPendingIds(Set<Long> pendingIds, int batchSize) {
        if(pendingIds.isEmpty()){
            return List.of();
        }
        Set<Long> batch = pendingIds.stream()
                .limit(batchSize)
                .collect(Collectors.toSet());

        pendingIds.removeAll(batch);

        return batch.stream().toList();
    }

    public Collection<Long> getAllPendingIds(Set<Long> pendingIds) {
        return pendingIds;
    }

    public<T> Collection<Long> getIdsByFKId(Collection<Long> ids, Set<Long> pendingIds,
                                            String cacheName, Class<T> classType, Predicate<T> filter) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache == null) return List.of();
        Set<Long> result = new HashSet<>(ids);
        pendingIds.forEach(id -> {
                    T obj = cache.get(id, classType);
                    if(obj != null) {
                        if(filter.test(obj)) {
                            result.add(id);
                        }
                    }
                });
        return result;
    }

    public<T> void saveDataWithIds(Collection<Long> idsNeedSave, Set<Long> pendingIds,
                                Function<Collection<Long>, Optional<List<T>>> getCache, Consumer<List<T>> consumer) {
        if(idsNeedSave == null || idsNeedSave.isEmpty()){
            return;
        }
        try {
            getCache.apply(idsNeedSave).ifPresent(consumer);
        } catch (Exception ex) {
            log.error(ex.toString());
            pendingIds.addAll(idsNeedSave);
        }
    }
}
