package com.cabpool.matcher.service;

import com.cabpool.common.enums.Direction;
import com.cabpool.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPoolService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private static final String POOL_KEY_PREFIX = "cabpool:active_pool:";
    private static final String LOCK_KEY_PREFIX = "cabpool:lock:";

    /**
     * Atomic Lua script for safe lock release.
     * Guarantees that a worker only deletes the lock if it still holds the exact lockOwner token.
     * Prevents race condition where lock expires, worker B acquires it, and worker A's slow release deletes B's lock!
     */
    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final DefaultRedisScript<Long> unlockScript = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);

    public void addToActivePool(RideRequestedEvent event) {
        String key = buildPoolKey(event.getVenue(), event.getDirection(), event.getDropZone());
        redisTemplate.opsForHash().put(key, event.getRequestId().toString(), event);
        redisTemplate.expire(key, Duration.ofHours(4));
        log.info("Added request {} to Redis candidate pool '{}'", event.getRequestId(), key);
    }

    public List<RideRequestedEvent> getCandidates(String venue, Direction direction, String dropZone) {
        String key = buildPoolKey(venue, direction, dropZone);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<RideRequestedEvent> list = new ArrayList<>();
        for (Object value : entries.values()) {
            if (value instanceof RideRequestedEvent event) {
                list.add(event);
            } else if (value != null) {
                try {
                    RideRequestedEvent event = objectMapper.convertValue(value, RideRequestedEvent.class);
                    list.add(event);
                } catch (Exception e) {
                    log.error("Failed to deserialize candidate from Redis: {}", e.getMessage());
                }
            }
        }
        return list;
    }

    public void removeFromActivePool(String venue, Direction direction, String dropZone, List<UUID> requestIds) {
        String key = buildPoolKey(venue, direction, dropZone);
        Object[] hashKeys = requestIds.stream().map(UUID::toString).toArray();
        redisTemplate.opsForHash().delete(key, hashKeys);
        log.info("Removed requests {} from Redis pool '{}'", requestIds, key);
    }

    /**
     * Atomically acquires distributed locks on all candidate IDs using SET key owner NX EX ttl.
     */
    public boolean acquireDistributedLocks(List<UUID> requestIds, String lockOwner, long ttlSeconds) {
        List<String> successfullyLockedKeys = new ArrayList<>();

        for (UUID id : requestIds) {
            String lockKey = LOCK_KEY_PREFIX + id;
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockOwner, ttlSeconds, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(acquired)) {
                successfullyLockedKeys.add(lockKey);
            } else {
                log.warn("Failed to acquire distributed lock for request {}, safely rolling back held locks", id);
                releaseLocksByKeysSafely(successfullyLockedKeys, lockOwner);
                return false;
            }
        }
        return true;
    }

    /**
     * Safely releases distributed locks using the atomic Lua script.
     */
    public void releaseDistributedLocks(List<UUID> requestIds, String lockOwner) {
        List<String> keys = requestIds.stream().map(id -> LOCK_KEY_PREFIX + id).toList();
        releaseLocksByKeysSafely(keys, lockOwner);
    }

    private void releaseLocksByKeysSafely(List<String> lockKeys, String lockOwner) {
        for (String key : lockKeys) {
            try {
                Long result = redisTemplate.execute(unlockScript, Collections.singletonList(key), lockOwner);
                if (result != null && result == 1L) {
                    log.debug("Successfully and atomically released lock key {}", key);
                } else {
                    log.warn("Lock key {} was not released (already expired or held by another owner)", key);
                }
            } catch (Exception e) {
                log.error("Failed to release lock key {}: {}", key, e.getMessage());
            }
        }
    }

    private String buildPoolKey(String venue, Direction direction, String dropZone) {
        return POOL_KEY_PREFIX + venue.toUpperCase() + ":" + direction.name() + ":" + dropZone.toUpperCase();
    }
}
