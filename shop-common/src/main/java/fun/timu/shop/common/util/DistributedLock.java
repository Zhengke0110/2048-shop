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
                log.debug("获取分布式锁成功: key={}, value={}", lockKey, lockValue);
            } else {
                log.debug("获取分布式锁失败: key={}", lockKey);
            }

            return lockResult;
        } catch (Exception e) {
            log.error("获取分布式锁异常: key={}", lockKey, e);
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
                log.warn("释放分布式锁失败，锁可能已过期或被其他线程释放: key={}", lockKey);
            }

            return unlockResult;
        } catch (Exception e) {
            log.error("释放分布式锁异常: key={}", lockKey, e);
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
}
