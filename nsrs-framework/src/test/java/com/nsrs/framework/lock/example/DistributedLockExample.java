package com.nsrs.framework.lock.example;

import com.nsrs.framework.lock.annotation.DistributedLockable;
import com.nsrs.framework.lock.DistributedLockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分布式锁使用示例
 *
 * @author NSRS
 */
@Service
public class DistributedLockExample {

    @Autowired
    private DistributedLockUtil lockUtil;

    private final AtomicInteger counter = new AtomicInteger(0);

    /**
 * 示例1：使用注解方式实现分布式锁
     * 适用于简单的锁定场景
     */
    @DistributedLockable(
        key = "user:update:#{#userId}",
        timeout = 5,
        expireTime = 30,
        errorMessage = "用户信息正在更新中，请稍后重试"
    )
    public String updateUserInfo(String userId, String newInfo) {
        // 模拟用户信息更新
        System.out.println("开始更新用户 " + userId + " 的信息");
        
        try {
            // 模拟数据库操作
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("用户 " + userId + " 信息更新完成");
        return "更新成功";
    }

    /**
     * 示例2：使用工具类方式实现分布式锁
     * 适用于需要更灵活控制的场景
     */
    public String processOrder(String orderId) {
        String lockKey = "order:process:" + orderId;
        
        return lockUtil.executeWithLockOrFallback(
            lockKey,
            3, // 等待3秒
            60, // 锁定60秒
            TimeUnit.SECONDS,
            () -> {
                // 主要业务逻辑：处理订单
                System.out.println("开始处理订单: " + orderId);
                
                // 模拟复杂的订单处理逻辑
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("订单处理完成: " + orderId);
                return "订单 " + orderId + " 处理成功";
            },
            () -> {
                // 降级逻辑：无法获取锁时的处理
                System.out.println("订单 " + orderId + " 正在处理中，请稍后查询");
                return "订单正在处理中，请稍后查询";
            }
        );
    }

    /**
     * 示例3：库存扣减场景
     * 使用分布式锁防止超卖
     */
    public boolean deductStock(String productId, int quantity) {
        String lockKey = "stock:" + productId;
        
        Boolean result = lockUtil.executeWithLock(
            lockKey,
            1, // 等待1秒
            10, // 锁定10秒
            TimeUnit.SECONDS,
            () -> {
                // 查询当前库存
                int currentStock = getCurrentStock(productId);
                
                if (currentStock >= quantity) {
                    // 扣减库存
                    updateStock(productId, currentStock - quantity);
                    System.out.println("商品 " + productId + " 库存扣减成功，扣减数量: " + quantity);
                    return true;
                } else {
                    System.out.println("商品 " + productId + " 库存不足，当前库存: " + currentStock + "，需要: " + quantity);
                    return false;
                }
            }
        );
        
        return result != null && result;
    }

    /**
     * 示例4：计数器场景
     * 使用分布式锁保证计数的准确性
     */
    public int incrementCounter(String counterId) {
        String lockKey = "counter:" + counterId;
        
        Integer result = lockUtil.executeWithLock(
            lockKey,
            2, // 等待2秒
            5, // 锁定5秒
            TimeUnit.SECONDS,
            () -> {
                // 获取当前计数
                int currentCount = getCounterValue(counterId);
                
                // 递增计数
                int newCount = currentCount + 1;
                setCounterValue(counterId, newCount);
                
                System.out.println("计数器 " + counterId + " 递增，当前值: " + newCount);
                return newCount;
            }
        );
        
        return result != null ? result : -1;
    }

    /**
     * 示例5：批量操作场景
     * 使用分布式锁确保批量操作的原子性
     */
    @DistributedLockable(
        key = "batch:operation:#{#batchId}",
        timeout = 10,
        expireTime = 120,
        errorMessage = "批量操作正在进行中"
    )
    public String batchOperation(String batchId, String[] items) {
        System.out.println("开始批量操作: " + batchId);
        
        int successCount = 0;
        int failCount = 0;
        
        for (String item : items) {
            try {
                // 模拟处理每个项目
                processItem(item);
                successCount++;
                
                // 模拟处理时间
                Thread.sleep(100);
            } catch (Exception e) {
                failCount++;
                System.err.println("处理项目失败: " + item + ", 错误: " + e.getMessage());
            }
        }
        
        String result = String.format("批量操作完成，成功: %d, 失败: %d", successCount, failCount);
        System.out.println(result);
        return result;
    }

    /**
     * 示例6：检查锁状态
     */
    public void checkLockStatus(String lockKey) {
        boolean isLocked = lockUtil.isLocked(lockKey);
        System.out.println("锁 " + lockKey + " 状态: " + (isLocked ? "已锁定" : "未锁定"));
        
        if (isLocked) {
            long expireTime = lockUtil.getExpireTime(lockKey);
            System.out.println("锁 " + lockKey + " 剩余时间: " + expireTime + " 秒");
        }
    }

    /**
     * 示例7：强制释放锁
     * 注意：这个操作需要谨慎使用
     */
    public void forceReleaseLock(String lockKey) {
        boolean released = lockUtil.forceUnlock(lockKey);
        System.out.println("强制释放锁 " + lockKey + ": " + (released ? "成功" : "失败"));
    }

    // 模拟方法
    private int getCurrentStock(String productId) {
        // 模拟从数据库查询库存
        return 100;
    }

    private void updateStock(String productId, int newStock) {
        // 模拟更新数据库库存
        System.out.println("更新商品 " + productId + " 库存为: " + newStock);
    }

    private int getCounterValue(String counterId) {
        // 模拟从缓存或数据库获取计数值
        return counter.get();
    }

    private void setCounterValue(String counterId, int value) {
        // 模拟设置计数值到缓存或数据库
        counter.set(value);
    }

    private void processItem(String item) throws Exception {
        // 模拟处理单个项目
        if ("error".equals(item)) {
            throw new Exception("模拟处理错误");
        }
        System.out.println("处理项目: " + item);
    }
}