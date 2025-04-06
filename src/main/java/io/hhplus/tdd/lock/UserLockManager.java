package io.hhplus.tdd.lock;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class UserLockManager {
    private ConcurrentHashMap<Long, ReentrantLock> userLockMap = new ConcurrentHashMap<>();

    public ReentrantLock getUserLock(Long userId) {
        return userLockMap.computeIfAbsent(userId, lock -> new ReentrantLock(true));
    }

}
