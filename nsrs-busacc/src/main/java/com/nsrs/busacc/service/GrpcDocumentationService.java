package com.nsrs.busacc.service;

import com.google.protobuf.Descriptors;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.MethodDescriptor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * gRPC 文档服务
 * 通过反射获取 gRPC 服务信息，生成文档数据
 */
@Slf4j
@Service
public class GrpcDocumentationService {

    @Autowired(required = false)
    private Server grpcServer;
    
    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    @PostConstruct
    public void init() {
        loadServiceInfoFromContext();
        if (grpcServer != null) {
            loadServiceInfoFromServer();
        }
    }

    /**
     * 从 ApplicationContext 加载服务信息
     */
    private void loadServiceInfoFromContext() {
        try {
            // 获取所有标注了 @GrpcService 的 Bean
            Map<String, Object> grpcServices = applicationContext.getBeansWithAnnotation(GrpcService.class);
            
            for (Map.Entry<String, Object> entry : grpcServices.entrySet()) {
                Object serviceBean = entry.getValue();
                GrpcService grpcServiceAnnotation = serviceBean.getClass().getAnnotation(GrpcService.class);
                
                if (grpcServiceAnnotation != null) {
                    String serviceName = extractServiceName(serviceBean.getClass());
                    
                    ServiceInfo serviceInfo = new ServiceInfo();
                    serviceInfo.setName(serviceName);
                    serviceInfo.setFullName(serviceName);
                    serviceInfo.setDescription("gRPC 服务: " + serviceName);
                    
                    // 获取方法信息（从类的方法中推断）
                    List<MethodInfo> methods = extractMethodsFromClass(serviceBean.getClass());
                    serviceInfo.setMethods(methods);
                    
                    serviceInfoMap.put(serviceName, serviceInfo);
                    log.info("Loaded gRPC service from context: {} with {} methods", serviceName, methods.size());
                }
            }
        } catch (Exception e) {
            log.error("Failed to load gRPC service info from context", e);
        }
    }
    
    /**
     * 从 gRPC Server 加载服务信息
     */
    private void loadServiceInfoFromServer() {
        try {
            List<ServerServiceDefinition> services = grpcServer.getServices();
            for (ServerServiceDefinition service : services) {
                String serviceName = service.getServiceDescriptor().getName();
                
                // 跳过反射服务本身
                if (serviceName.contains("grpc.reflection")) {
                    continue;
                }
                
                ServiceInfo existingService = serviceInfoMap.get(serviceName);
                if (existingService == null) {
                    existingService = new ServiceInfo();
                    existingService.setName(serviceName);
                    existingService.setFullName(serviceName);
                    serviceInfoMap.put(serviceName, existingService);
                }
                
                // 获取方法信息
                List<MethodInfo> methods = service.getServiceDescriptor().getMethods().stream()
                    .map(methodDescriptor -> {
                        MethodInfo methodInfo = new MethodInfo();
                        methodInfo.setName(methodDescriptor.getFullMethodName());
                        methodInfo.setType(methodDescriptor.getType().toString());
                        return methodInfo;
                    })
                    .collect(Collectors.toList());
                
                existingService.setMethods(methods);
                
                log.info("Updated gRPC service from server: {} with {} methods", serviceName, methods.size());
            }
        } catch (Exception e) {
            log.error("Failed to load gRPC service info from server", e);
        }
    }
    
    /**
     * 从服务类名提取服务名称
     */
    private String extractServiceName(Class<?> serviceClass) {
        String className = serviceClass.getSimpleName();
        if (className.endsWith("Impl")) {
            className = className.substring(0, className.length() - 4);
        }
        if (className.endsWith("Service")) {
            className = className.substring(0, className.length() - 7);
        }
        return className;
    }
    
    /**
     * 从服务类中提取方法信息
     */
    private List<MethodInfo> extractMethodsFromClass(Class<?> serviceClass) {
        List<MethodInfo> methods = new ArrayList<>();
        
        // 获取所有公共方法
        java.lang.reflect.Method[] classMethods = serviceClass.getDeclaredMethods();
        for (java.lang.reflect.Method method : classMethods) {
            // 跳过非公共方法和继承的方法
            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers()) ||
                method.getDeclaringClass() == Object.class) {
                continue;
            }
            
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setName(method.getName());
            methodInfo.setType("UNARY"); // 默认类型
            methodInfo.setDescription("gRPC 方法: " + method.getName());
            
            // 获取参数和返回类型
            if (method.getParameterCount() > 0) {
                methodInfo.setInputType(method.getParameterTypes()[0].getSimpleName());
            }
            methodInfo.setOutputType(method.getReturnType().getSimpleName());
            
            methods.add(methodInfo);
        }
        
        return methods;
    }

    /**
     * 获取所有服务信息
     * @return 服务信息列表
     */
    public List<ServiceInfo> getAllServices() {
        return new ArrayList<>(serviceInfoMap.values());
    }

    /**
     * 获取指定服务信息
     * @param serviceName 服务名称
     * @return 服务信息
     */
    public ServiceInfo getService(String serviceName) {
        return serviceInfoMap.get(serviceName);
    }

    /**
     * 服务信息
     */
    public static class ServiceInfo {
        private String name;
        private String fullName;
        private String description;
        private List<MethodInfo> methods = new ArrayList<>();

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<MethodInfo> getMethods() { return methods; }
        public void setMethods(List<MethodInfo> methods) { this.methods = methods; }
    }

    /**
     * 方法信息
     */
    public static class MethodInfo {
        private String name;
        private String type;
        private String description;
        private String inputType;
        private String outputType;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getInputType() { return inputType; }
        public void setInputType(String inputType) { this.inputType = inputType; }
        public String getOutputType() { return outputType; }
        public void setOutputType(String outputType) { this.outputType = outputType; }
    }
}