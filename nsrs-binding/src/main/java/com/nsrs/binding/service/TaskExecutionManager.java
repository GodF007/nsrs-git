package com.nsrs.binding.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * 任务执行管理器
 * 用于管理正在执行的异步任务，支持任务的停止和状态查询
 */
@Slf4j
@Component
public class TaskExecutionManager {
    
    /**
     * 存储正在执行的任务
     * Key: 任务ID, Value: Future对象
     */
    private final ConcurrentHashMap<Long, Future<?>> runningTasks = new ConcurrentHashMap<>();
    
    /**
     * 存储任务的中断标志
     * Key: 任务ID, Value: 是否被中断
     */
    private final ConcurrentHashMap<Long, Boolean> interruptFlags = new ConcurrentHashMap<>();
    
    /**
     * 注册正在执行的任务
     * 
     * @param taskId 任务ID
     * @param future Future对象
     */
    public void registerTask(Long taskId, Future<?> future) {
        runningTasks.put(taskId, future);
        interruptFlags.put(taskId, false);
        log.info("Task registered: {}", taskId);
    }
    
    /**
     * 停止指定的任务
     * 
     * @param taskId 任务ID
     * @return 是否成功停止
     */
    public boolean stopTask(Long taskId) {
        Future<?> future = runningTasks.get(taskId);
        if (future != null) {
            // 设置中断标志
            interruptFlags.put(taskId, true);
            
            // 尝试取消任务
            boolean cancelled = future.cancel(true);
            
            if (cancelled) {
                log.info("Task successfully stopped: {}", taskId);
            } else {
                log.warn("Failed to stop task or task already completed: {}", taskId);
            }
            
            return cancelled;
        }
        
        log.warn("Task not found in running tasks: {}", taskId);
        return false;
    }
    
    /**
     * 检查任务是否被中断
     * 
     * @param taskId 任务ID
     * @return 是否被中断
     */
    public boolean isTaskInterrupted(Long taskId) {
        return interruptFlags.getOrDefault(taskId, false);
    }
    
    /**
     * 任务完成时清理资源
     * 
     * @param taskId 任务ID
     */
    public void taskCompleted(Long taskId) {
        runningTasks.remove(taskId);
        interruptFlags.remove(taskId);
        log.info("Task completed and cleaned up: {}", taskId);
    }
    
    /**
     * 检查任务是否正在运行
     * 
     * @param taskId 任务ID
     * @return 是否正在运行
     */
    public boolean isTaskRunning(Long taskId) {
        Future<?> future = runningTasks.get(taskId);
        return future != null && !future.isDone();
    }
    
    /**
     * 获取正在运行的任务数量
     * 
     * @return 正在运行的任务数量
     */
    public int getRunningTaskCount() {
        return runningTasks.size();
    }
}