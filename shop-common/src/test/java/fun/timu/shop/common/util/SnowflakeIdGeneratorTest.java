package fun.timu.shop.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SnowflakeIdGenerator 测试类
 * 
 * @author zhengke
 */
public class SnowflakeIdGeneratorTest {

    @Test
    public void testSingleThreadGeneration() {
        SnowflakeIdGenerator generator = SnowflakeIdGenerator.getInstance(1);
        
        // 生成100个ID
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 100; i++) {
            long id = generator.nextId();
            assertTrue(id > 0, "ID应该大于0");
            assertTrue(ids.add(id), "ID应该唯一");
        }
        
        assertEquals(100, ids.size(), "应该生成100个唯一ID");
    }

    @Test
    public void testMultiThreadGeneration() throws InterruptedException {
        SnowflakeIdGenerator generator = SnowflakeIdGenerator.getInstance(2);
        
        int threadCount = 10;
        int idsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Long> allIds = ConcurrentHashMap.newKeySet();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        long id = generator.nextId();
                        assertTrue(id > 0, "ID应该大于0");
                        assertTrue(allIds.add(id), "ID应该唯一: " + id);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertEquals(threadCount * idsPerThread, allIds.size(), 
            "应该生成" + (threadCount * idsPerThread) + "个唯一ID");
    }

    @Test
    public void testIdParsing() {
        SnowflakeIdGenerator generator = SnowflakeIdGenerator.getInstance(3);
        
        long id = generator.nextId();
        SnowflakeIdGenerator.IdInfo info = SnowflakeIdGenerator.parseId(id);
        
        assertNotNull(info, "ID解析结果不应为空");
        assertEquals(3, info.getMachineId(), "机器ID应该为3");
        assertTrue(info.getTimestamp() > 0, "时间戳应该大于0");
        assertTrue(info.getSequence() >= 0, "序列号应该大于等于0");
        
        System.out.println("生成的ID: " + id);
        System.out.println("解析结果: " + info);
    }

    @RepeatedTest(5)
    public void testIdIncrement() {
        SnowflakeIdGenerator generator = SnowflakeIdGenerator.getInstance(4);
        
        long previousId = 0;
        for (int i = 0; i < 100; i++) {
            long currentId = generator.nextId();
            assertTrue(currentId > previousId, 
                "当前ID(" + currentId + ")应该大于前一个ID(" + previousId + ")");
            previousId = currentId;
        }
    }

    @Test
    public void testDifferentMachineIds() {
        SnowflakeIdGenerator generator1 = SnowflakeIdGenerator.getInstance(1);
        SnowflakeIdGenerator generator2 = SnowflakeIdGenerator.getInstance(2);
        
        long id1 = generator1.nextId();
        long id2 = generator2.nextId();
        
        SnowflakeIdGenerator.IdInfo info1 = SnowflakeIdGenerator.parseId(id1);
        SnowflakeIdGenerator.IdInfo info2 = SnowflakeIdGenerator.parseId(id2);
        
        assertEquals(1, info1.getMachineId());
        assertEquals(2, info2.getMachineId());
        assertNotEquals(id1, id2, "不同机器ID生成的ID应该不同");
    }

    @Test
    public void testInvalidMachineId() {
        // 测试机器ID超出范围
        assertThrows(IllegalArgumentException.class, () -> {
            SnowflakeIdGenerator.getInstance(-1);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            SnowflakeIdGenerator.getInstance(1024);
        });
    }

    @Test
    public void testPerformance() {
        SnowflakeIdGenerator generator = SnowflakeIdGenerator.getInstance(5);
        
        int count = 100000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            generator.nextId();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("生成" + count + "个ID耗时: " + duration + "ms");
        System.out.println("平均每秒生成ID数: " + (count * 1000 / duration));
        
        assertTrue(duration < 10000, "生成10万个ID应该在10秒内完成");
    }
}
