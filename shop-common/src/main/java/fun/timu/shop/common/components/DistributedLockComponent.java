package fun.timu.shop.common.components;

import fun.timu.shop.common.util.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁组件
 *
 * @author zhengke
 */
@Slf4j
@Component
public class DistributedLockComponent {

    private final StringRedisTemplate redisTemplate;

    /**
     * 默认锁过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 30;

    /**
     * 默认重试次数
     */
    private static final int DEFAULT_RETRY_TIMES = 3;

    /**
     * 默认重试间隔（毫秒）
     */
    private static final long DEFAULT_RETRY_INTERVAL = 100;

    public DistributedLockComponent(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建分布式锁
     *
     * @param lockKey 锁的key
     * @return 分布式锁实例
     */
    public DistributedLock createLock(String lockKey) {
        return createLock(lockKey, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 创建分布式锁
     *
     * @param lockKey    锁的key
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return 分布式锁实例
     */
    public DistributedLock createLock(String lockKey, long expireTime, TimeUnit timeUnit) {
        return new DistributedLock(redisTemplate, lockKey, expireTime, timeUnit);
    }

    /**
     * 执行带锁的操作
     *
     * @param lockKey 锁的key
     * @param action  要执行的操作
     * @param <T>     返回值类型
     * @return 操作结果
     * @throws DistributedLock.LockException 获取锁失败异常
     */
    public <T> T executeWithLock(String lockKey, DistributedLock.LockAction<T> action) throws DistributedLock.LockException {
        DistributedLock lock = createLock(lockKey);
        return lock.executeWithLock(action, DEFAULT_RETRY_TIMES, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * 执行带锁的操作（自定义参数）
     *
     * @param lockKey       锁的key
     * @param action        要执行的操作
     * @param expireTime    锁过期时间
     * @param timeUnit      时间单位
     * @param retryTimes    重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param <T>           返回值类型
     * @return 操作结果
     * @throws DistributedLock.LockException 获取锁失败异常
     */
    public <T> T executeWithLock(String lockKey, DistributedLock.LockAction<T> action, long expireTime, TimeUnit timeUnit, int retryTimes, long retryInterval) throws DistributedLock.LockException {
        DistributedLock lock = createLock(lockKey, expireTime, timeUnit);
        return lock.executeWithLock(action, retryTimes, retryInterval);
    }

    /**
     * 执行带锁的操作（兼容Supplier接口）
     * 为了兼容现有的Supplier<T>接口调用
     *
     * @param lockKey   锁的key
     * @param waitTime  等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit  时间单位
     * @param business  业务逻辑
     * @param <T>       返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, 
                                TimeUnit timeUnit, java.util.function.Supplier<T> business) {
        try {
            return executeWithLock(lockKey, () -> business.get(), leaseTime, timeUnit, DEFAULT_RETRY_TIMES, DEFAULT_RETRY_INTERVAL);
        } catch (DistributedLock.LockException e) {
            throw new RuntimeException("获取锁失败，请稍后重试", e);
        }
    }

    /**
     * 生成用户地址锁的key
     *
     * @param userId 用户ID
     * @return 锁的key
     */
    public String generateUserAddressLockKey(Long userId) {
        return "lock:user:address:" + userId;
    }

    /**
     * 生成默认地址锁的key
     *
     * @param userId 用户ID
     * @return 锁的key
     */
    public String generateDefaultAddressLockKey(Long userId) {
        return "lock:user:default_address:" + userId;
    }
}
