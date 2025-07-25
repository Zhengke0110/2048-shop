package fun.timu.shop.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Snowflake分布式ID生成器
 * 
 * ID结构 (64位):
 * 1位符号位(固定为0) + 41位时间戳 + 10位机器ID + 12位序列号
 * 
 * 特点：
 * - 趋势递增：大致按照时间递增
 * - 不重复：在分布式系统中保证ID不重复
 * - 高性能：本地生成，无需网络通信
 * - 高可用：不依赖第三方系统
 * 
 * @author zhengke
 */
@Slf4j
public class SnowflakeIdGenerator {
    
    /**
     * 起始时间戳 (2023-01-01 00:00:00)
     * 可以使用约69年 (2^41 / (365 * 24 * 3600 * 1000))
     */
    private static final long START_TIMESTAMP = 1672531200000L;
    
    /**
     * 机器ID位数
     */
    private static final long MACHINE_ID_BITS = 10L;
    
    /**
     * 序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;
    
    /**
     * 机器ID最大值 (1023)
     */
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    
    /**
     * 序列号最大值 (4095)
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    
    /**
     * 机器ID左移位数
     */
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    
    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    
    /**
     * 机器ID
     */
    private final long machineId;
    
    /**
     * 序列号
     */
    private long sequence = 0L;
    
    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;
    
    /**
     * 实例缓存，每个机器ID对应一个实例
     */
    private static final java.util.concurrent.ConcurrentHashMap<Long, SnowflakeIdGenerator> instances = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * 构造函数
     * 
     * @param machineId 机器ID (0-1023)
     */
    private SnowflakeIdGenerator(long machineId) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException(
                String.format("机器ID必须在0到%d之间", MAX_MACHINE_ID));
        }
        this.machineId = machineId;
        log.info("SnowflakeIdGenerator 初始化完成，机器ID: {}", machineId);
    }
    
    /**
     * 获取指定机器ID的实例
     * 
     * @param machineId 机器ID
     * @return SnowflakeIdGenerator实例
     */
    public static SnowflakeIdGenerator getInstance(long machineId) {
        // 先验证machineId的有效性
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException(
                String.format("机器ID必须在0到%d之间", MAX_MACHINE_ID));
        }
        
        return instances.computeIfAbsent(machineId, SnowflakeIdGenerator::new);
    }
    
    /**
     * 获取单例实例（使用默认机器ID）
     * 
     * @return SnowflakeIdGenerator实例
     */
    public static SnowflakeIdGenerator getInstance() {
        return getInstance(getDefaultMachineId());
    }
    
    /**
     * 生成下一个ID
     * 
     * @return 分布式ID
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();
        
        // 时间回拨检查
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                // 小幅度时间回拨，等待追赶
                try {
                    Thread.sleep(offset << 1);
                    timestamp = getCurrentTimestamp();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException(
                            String.format("时间回拨异常，拒绝生成ID。当前时间: %d, 上次时间: %d", 
                                timestamp, lastTimestamp));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待时间追赶被中断", e);
                }
            } else {
                throw new RuntimeException(
                    String.format("严重时间回拨，拒绝生成ID。回拨时间: %d毫秒", offset));
            }
        }
        
        // 同一毫秒内生成ID
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 序列号用完，等待下一毫秒
                timestamp = getNextTimestamp(lastTimestamp);
            }
        } else {
            // 新的毫秒，序列号重置
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        // 组装ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }
    
    /**
     * 获取当前时间戳
     * 
     * @return 当前时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 等待下一毫秒
     * 
     * @param lastTimestamp 上次时间戳
     * @return 下一毫秒时间戳
     */
    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
    
    /**
     * 获取默认机器ID
     * 基于本机网络地址生成
     * 
     * @return 机器ID
     */
    private static long getDefaultMachineId() {
        try {
            // 获取本机IP地址的后10位作为机器ID
            java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
            byte[] ipBytes = addr.getAddress();
            
            // 使用IP地址的后两个字节
            int machineId = 0;
            if (ipBytes.length >= 4) {
                machineId = ((ipBytes[ipBytes.length - 2] & 0xFF) << 8) 
                          | (ipBytes[ipBytes.length - 1] & 0xFF);
            } else if (ipBytes.length >= 2) {
                machineId = ((ipBytes[0] & 0xFF) << 8) | (ipBytes[1] & 0xFF);
            }
            
            return machineId & MAX_MACHINE_ID;
        } catch (Exception e) {
            log.warn("获取机器ID失败，使用随机ID", e);
            // 如果获取失败，使用随机数
            return (long) (Math.random() * MAX_MACHINE_ID);
        }
    }
    
    /**
     * 解析ID的各个组成部分（用于调试）
     * 
     * @param id 分布式ID
     * @return ID解析结果
     */
    public static IdInfo parseId(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + START_TIMESTAMP;
        long machineId = (id >> MACHINE_ID_SHIFT) & MAX_MACHINE_ID;
        long sequence = id & MAX_SEQUENCE;
        
        return new IdInfo(timestamp, machineId, sequence);
    }
    
    /**
     * ID解析结果
     */
    public static class IdInfo {
        private final long timestamp;
        private final long machineId;
        private final long sequence;
        
        public IdInfo(long timestamp, long machineId, long sequence) {
            this.timestamp = timestamp;
            this.machineId = machineId;
            this.sequence = sequence;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public long getMachineId() {
            return machineId;
        }
        
        public long getSequence() {
            return sequence;
        }
        
        @Override
        public String toString() {
            return String.format("IdInfo{timestamp=%d(%s), machineId=%d, sequence=%d}", 
                timestamp, new java.util.Date(timestamp), machineId, sequence);
        }
    }
}
