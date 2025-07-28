package fun.timu.shop.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的分布式锁工具类
 *
 * @author zhengke
 */
@Slf4j
public class DistributedLock {

    private final StringRedisTemplate redisTemplate;
    private final String lockKey;
    private final String lockValue;
    private final long expireTime;
    private final TimeUnit timeUnit;

    /**
     * Lua脚本：释放锁
     */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    /**
     * 构造分布式锁
     *
     * @param redisTemplate Redis模板
     * @param lockKey       锁的key
     * @param expireTime    锁的过期时间
     * @param timeUnit      时间单位
     */
    public DistributedLock(StringRedisTemplate redisTemplate, String lockKey, long expireTime, TimeUnit timeUnit) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString();
        this.expireTime = expireTime;
        this.timeUnit = timeUnit;
    }

    /**
     * 尝试获取锁
     *
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock() {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, timeUnit);
            boolean lockResult = Boolean.TRUE.equals(result);

            if (lockResult) {
                log.debug("获取分布式锁成功: key={}, value={}, expireTime={}{}", 
                         lockKey, lockValue, expireTime, timeUnit.name().toLowerCase());
            } else {
                log.debug("获取分布式锁失败，锁已被占用: key={}", lockKey);
            }

            return lockResult;
        } catch (Exception e) {
            log.error("获取分布式锁异常: key={}, 可能是Redis连接问题", lockKey, e);
            // Redis异常时不要无限重试，直接返回false
            return false;
        }
    }

    /**
     * 尝试获取锁，带重试
     *
     * @param retryTimes    重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock(int retryTimes, long retryInterval) {
        for (int i = 0; i <= retryTimes; i++) {
            if (tryLock()) {
                return true;
            }

            if (i < retryTimes) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("获取分布式锁重试被中断: key={}", lockKey);
                    return false;
                }
            }
        }

        log.warn("获取分布式锁失败，重试{}次后放弃: key={}", retryTimes, lockKey);
        return false;
    }

    /**
     * 尝试获取锁，带超时和重试
     * 优化版本：使用指数退避算法减少重试压力
     *
     * @param waitTime      等待时间
     * @param retryInterval 重试间隔（毫秒）
     * @param timeUnit      时间单位
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock(long waitTime, long retryInterval, TimeUnit timeUnit) {
        long waitTimeMs = timeUnit.toMillis(waitTime);
        long endTime = System.currentTimeMillis() + waitTimeMs;
        int attempts = 0;
        long currentInterval = retryInterval;
        
        log.debug("开始尝试获取分布式锁: key={}, waitTime={}ms, retryInterval={}ms", 
                 lockKey, waitTimeMs, retryInterval);
        
        while (System.currentTimeMillis() < endTime) {
            attempts++;
            if (tryLock()) {
                log.debug("获取分布式锁成功: key={}, attempts={}", lockKey, attempts);
                return true;
            }
            
            // 指数退避算法：随着重试次数增加，延迟时间增加
            if (attempts > 20) {
                currentInterval = Math.min(retryInterval * 2, 200); // 最大200ms
            }
            
            try {
                Thread.sleep(currentInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("获取分布式锁重试被中断: key={}, attempts={}", lockKey, attempts);
                return false;
            }
        }
        
        log.warn("获取分布式锁超时失败: key={}, waitTime={}ms, attempts={}", lockKey, waitTimeMs, attempts);
        return false;
    }

    /**
     * 释放锁
     *
     * @return true-释放成功，false-释放失败
     */
    public boolean unlock() {
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);

            Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);
            boolean unlockResult = Long.valueOf(1).equals(result);

            if (unlockResult) {
                log.debug("释放分布式锁成功: key={}, value={}", lockKey, lockValue);
            } else {
                log.warn("释放分布式锁失败，锁可能已过期或被其他线程释放: key={}, value={}", lockKey, lockValue);
            }

            return unlockResult;
        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}, value={}", lockKey, lockValue, e);
            return false;
        }
    }

    /**
     * 执行带锁的操作
     *
     * @param action 要执行的操作
     * @param <T>    返回值类型
     * @return 操作结果
     * @throws LockException 获取锁失败异常
     */
    public <T> T executeWithLock(LockAction<T> action) throws Exception {
        if (!tryLock()) {
            throw new LockException("获取分布式锁失败: " + lockKey);
        }

        try {
            return action.execute();
        } finally {
            unlock();
        }
    }

    /**
     * 执行带锁的操作（带重试）
     *
     * @param action        要执行的操作
     * @param retryTimes    重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param <T>           返回值类型
     * @return 操作结果
     * @throws LockException 获取锁失败异常
     */
    public <T> T executeWithLock(LockAction<T> action, int retryTimes, long retryInterval) throws LockException {
        if (!tryLock(retryTimes, retryInterval)) {
            throw new LockException("获取分布式锁失败: " + lockKey);
        }

        try {
            return action.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unlock();
        }
    }

    /**
     * 执行带锁的操作（带等待时间）
     *
     * @param action        要执行的操作
     * @param waitTime      等待时间
     * @param retryInterval 重试间隔（毫秒）
     * @param timeUnit      时间单位
     * @param <T>           返回值类型
     * @return 操作结果
     * @throws LockException 获取锁失败异常
     */
    public <T> T executeWithLock(LockAction<T> action, long waitTime, long retryInterval, TimeUnit timeUnit) throws LockException {
        long startTime = System.currentTimeMillis();
        
        if (!tryLock(waitTime, retryInterval, timeUnit)) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("获取分布式锁失败: key={}, waitTime={}ms, actualWaitTime={}ms", 
                    lockKey, timeUnit.toMillis(waitTime), duration);
            throw new LockException("获取分布式锁失败: " + lockKey);
        }

        long lockAcquiredTime = System.currentTimeMillis();
        log.debug("获取分布式锁耗时: key={}, duration={}ms", lockKey, lockAcquiredTime - startTime);

        try {
            return action.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            long businessDuration = System.currentTimeMillis() - lockAcquiredTime;
            unlock();
            log.debug("分布式锁业务执行耗时: key={}, duration={}ms", lockKey, businessDuration);
        }
    }

    /**
     * 锁操作接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface LockAction<T> {
        T execute() throws Exception;
    }

    /**
     * 锁异常
     */
    public static class LockException extends Exception {
        public LockException(String message) {
            super(message);
        }

        public LockException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 检查锁是否存在
     *
     * @return true-锁存在，false-锁不存在
     */
    public boolean isLocked() {
        try {
            String value = redisTemplate.opsForValue().get(lockKey);
            return value != null;
        } catch (Exception e) {
            log.error("检查锁状态异常: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 检查当前锁的持有者
     *
     * @return 锁的值，如果锁不存在返回null
     */
    public String getLockHolder() {
        try {
            return redisTemplate.opsForValue().get(lockKey);
        } catch (Exception e) {
            log.error("获取锁持有者异常: key={}", lockKey, e);
            return null;
        }
    }

    /**
     * 检查当前线程是否持有锁
     *
     * @return true-当前线程持有锁，false-不持有
     */
    public boolean isLockedByCurrentThread() {
        try {
            String currentHolder = redisTemplate.opsForValue().get(lockKey);
            return lockValue.equals(currentHolder);
        } catch (Exception e) {
            log.error("检查锁持有状态异常: key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 获取锁的剩余过期时间
     *
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示锁不存在
     */
    public long getLockTtl() {
        try {
            return redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("获取锁TTL异常: key={}", lockKey, e);
            return -2;
        }
    }
}
