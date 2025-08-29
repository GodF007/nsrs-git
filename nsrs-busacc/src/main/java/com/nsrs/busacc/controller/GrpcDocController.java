package com.nsrs.busacc.controller;

import com.nsrs.busacc.service.GrpcDocumentationService;
import com.nsrs.busacc.service.GrpcTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * gRPC 文档控制器
 * 提供类似 Swagger 的 gRPC 接口文档功能
 * 同时处理API接口和页面路由
 */
@Tag(name = "gRPC 文档", description = "gRPC 接口文档管理")
@Controller
@RequestMapping("/busacc")
public class GrpcDocController {

    @Autowired
    private GrpcDocumentationService grpcDocumentationService;
    
    @Autowired
    private GrpcTestService grpcTestService;

    /**
     * gRPC 文档首页
     * @param model 模型
     * @return 文档页面
     */
    @GetMapping("/grpc-doc")
    public String index(Model model) {
        try {
            List<GrpcDocumentationService.ServiceInfo> services = grpcDocumentationService.getAllServices();
            model.addAttribute("services", services);
        } catch (Exception e) {
            model.addAttribute("services", new ArrayList<>());
            model.addAttribute("error", "获取服务列表失败: " + e.getMessage());
        }
        return "grpc-doc/index";
    }

    /**
     * 服务详情页面
     * @param serviceName 服务名称
     * @param model 模型
     * @return 服务详情页面
     */
    @GetMapping("/grpc-doc/service/{serviceName}")
    public String serviceDetail(@PathVariable String serviceName, Model model) {
        try {
            GrpcDocumentationService.ServiceInfo service = grpcDocumentationService.getService(serviceName);
            model.addAttribute("service", service);
        } catch (Exception e) {
            model.addAttribute("service", null);
            model.addAttribute("error", "获取服务详情失败: " + e.getMessage());
        }
        return "grpc-doc/service-detail";
    }

    /**
     * 获取所有 gRPC 服务信息
     * @return 服务信息列表
     */
    @Operation(summary = "获取所有 gRPC 服务")
    @GetMapping("/api/grpc-doc/services")
    @ResponseBody
    public List<GrpcDocumentationService.ServiceInfo> getAllServices() {
        return grpcDocumentationService.getAllServices();
    }

    /**
     * 获取指定服务信息
     * @param serviceName 服务名称
     * @return 服务信息
     */
    @Operation(summary = "获取指定 gRPC 服务信息")
    @GetMapping("/api/grpc-doc/services/{serviceName}")
    @ResponseBody
    public GrpcDocumentationService.ServiceInfo getService(@PathVariable String serviceName) {
        return grpcDocumentationService.getService(serviceName);
    }


    
    /**
     * 测试 gRPC 方法
     * @param serviceName 服务名称
     * @param methodName 方法名称
     * @param requestData 请求数据
     * @return 测试结果
     */
    @Operation(summary = "测试 gRPC 方法")
    @PostMapping("/api/grpc-doc/test/{serviceName}/{methodName}")
    @ResponseBody
    public ResponseEntity<GrpcTestService.TestResult> testMethod(
            @PathVariable String serviceName,
            @PathVariable String methodName,
            @RequestBody Map<String, Object> requestData) {
        
        try {
            String requestJson = requestData.get("request").toString();
            GrpcTestService.TestResult result = grpcTestService.testGrpcMethod(serviceName, methodName, requestJson);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            GrpcTestService.TestResult errorResult = new GrpcTestService.TestResult();
            errorResult.setServiceName(serviceName);
            errorResult.setMethodName(methodName);
            errorResult.setSuccess(false);
            errorResult.setErrorMessage("测试失败: " + e.getMessage());
            errorResult.setTimestamp(System.currentTimeMillis());
            return ResponseEntity.ok(errorResult);
        }
    }
    
    /**
     * 获取 gRPC 连接信息
     * @return 连接信息
     */
    @Operation(summary = "获取 gRPC 连接信息")
    @GetMapping("/api/grpc-doc/connection")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConnectionInfo() {
        Map<String, Object> connectionInfo = grpcTestService.getConnectionInfo();
        return ResponseEntity.ok(connectionInfo);
    }
    
    /**
     * 测试 gRPC 连接
     * @return 连接测试结果
     */
    @Operation(summary = "测试 gRPC 连接")
    @GetMapping("/api/grpc-doc/test-connection")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testConnection() {
        boolean connected = grpcTestService.testConnection();
        Map<String, Object> result = new HashMap<>();
        result.put("connected", connected);
        result.put("message", connected ? "连接成功" : "连接失败");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }
}