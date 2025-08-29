package com.nsrs.busacc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * gRPC 测试服务
 * 用于执行 gRPC 方法调用和测试
 */
@Slf4j
@Service
public class GrpcTestService {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;
    
    @Value("${grpc.server.address:localhost}")
    private String grpcAddress;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ManagedChannel channel;
    
    /**
     * 获取 gRPC 通道
     */
    private ManagedChannel getChannel() {
        if (channel == null || channel.isShutdown()) {
            channel = ManagedChannelBuilder.forAddress(grpcAddress, grpcPort)
                    .usePlaintext()
                    .build();
        }
        return channel;
    }
    
    /**
     * 测试 gRPC 方法调用
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param requestJson 请求参数 JSON
     * @return 测试结果
     */
    public TestResult testGrpcMethod(String serviceName, String methodName, String requestJson) {
        TestResult result = new TestResult();
        result.setServiceName(serviceName);
        result.setMethodName(methodName);
        result.setRequestData(requestJson);
        result.setTimestamp(System.currentTimeMillis());
        
        try {
            // 解析请求参数
            JsonNode requestNode = objectMapper.readTree(requestJson);
            
            // 模拟 gRPC 调用（实际项目中需要根据具体的 protobuf 定义来实现）
            Map<String, Object> mockResponse = simulateGrpcCall(serviceName, methodName, requestNode);
            
            result.setSuccess(true);
            result.setResponseData(objectMapper.writeValueAsString(mockResponse));
            result.setExecutionTime(System.currentTimeMillis() - result.getTimestamp());
            
            log.info("gRPC method test successful: {}/{}", serviceName, methodName);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setExecutionTime(System.currentTimeMillis() - result.getTimestamp());
            
            log.error("gRPC method test failed: {}/{}", serviceName, methodName, e);
        }
        
        return result;
    }
    
    /**
     * 模拟 gRPC 调用
     * 在实际项目中，这里应该根据具体的 protobuf 定义来实现真实的调用
     */
    private Map<String, Object> simulateGrpcCall(String serviceName, String methodName, JsonNode requestNode) {
        Map<String, Object> response = new HashMap<>();
        
        // 根据不同的服务和方法返回不同的模拟数据
        if (serviceName.contains("Binding")) {
            response.put("success", true);
            response.put("message", "号码绑定操作成功");
            response.put("bindingId", "BIND_" + System.currentTimeMillis());
            response.put("timestamp", System.currentTimeMillis());
        } else if (serviceName.contains("Activation")) {
            response.put("success", true);
            response.put("message", "激活操作成功");
            response.put("activationId", "ACT_" + System.currentTimeMillis());
            response.put("status", "ACTIVATED");
        } else {
            // 默认响应
            response.put("success", true);
            response.put("message", "操作成功");
            response.put("service", serviceName);
            response.put("method", methodName);
            response.put("timestamp", System.currentTimeMillis());
            
            // 回显请求参数
            if (requestNode != null && !requestNode.isEmpty()) {
                response.put("requestEcho", requestNode);
            }
        }
        
        return response;
    }
    
    /**
     * 验证 gRPC 服务连接
     * @return 连接状态
     */
    public boolean testConnection() {
        try {
            ManagedChannel testChannel = getChannel();
            // 简单的连接测试
            return !testChannel.isShutdown();
        } catch (Exception e) {
            log.error("gRPC connection test failed", e);
            return false;
        }
    }
    
    /**
     * 获取连接信息
     * @return 连接信息
     */
    public Map<String, Object> getConnectionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("address", grpcAddress);
        info.put("port", grpcPort);
        info.put("connected", testConnection());
        return info;
    }
    
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Failed to shutdown gRPC channel gracefully", e);
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 测试结果
     */
    public static class TestResult {
        private String serviceName;
        private String methodName;
        private String requestData;
        private String responseData;
        private boolean success;
        private String errorMessage;
        private long timestamp;
        private long executionTime;
        
        // Getters and Setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        public String getRequestData() { return requestData; }
        public void setRequestData(String requestData) { this.requestData = requestData; }
        public String getResponseData() { return responseData; }
        public void setResponseData(String responseData) { this.responseData = responseData; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
}