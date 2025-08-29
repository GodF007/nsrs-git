package com.nsrs.busacc.grpc;

import com.nsrs.busacc.grpc.NumberSelectionServiceGrpc;
import com.nsrs.busacc.service.NumberSelectionService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 号码选择GRPC服务实现
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class NumberSelectionGrpcService extends NumberSelectionServiceGrpc.NumberSelectionServiceImplBase {
    
    private final NumberSelectionService numberSelectionService;
    
    /**
     * 根据号段查询号码
     */
    @Override
    public void selectByPrefix(com.nsrs.busacc.grpc.NumberSelectionRequest request,
                              StreamObserver<com.nsrs.busacc.grpc.NumberSelectionResponse> responseObserver) {
        
        log.info("GRPC select numbers by prefix: {}, poolSize: {}, numberType: {}", 
                request.getNumberPrefix(), request.getPoolSize(), request.getNumberType());
        
        // 参数验证
        if (!StringUtils.hasText(request.getNumberPrefix())) {
            sendErrorResponse(responseObserver, "Number prefix cannot be empty");
            return;
        }
        if (request.getPoolSize() <= 0) {
            sendErrorResponse(responseObserver, "Pool size must be greater than 0");
            return;
        }
        if (request.getPoolSize() > 1000) {
            sendErrorResponse(responseObserver, "Pool size cannot exceed 1000");
            return;
        }
        
        try {
            // 转换GRPC请求为业务DTO
            com.nsrs.busacc.dto.NumberSelectionRequest businessRequest = convertToBusinessRequest(request);
            
            // 调用业务服务
            com.nsrs.busacc.dto.NumberSelectionResponse businessResponse = 
                    numberSelectionService.selectNumbers(businessRequest);
            
            // 转换业务响应为GRPC响应
            com.nsrs.busacc.grpc.NumberSelectionResponse grpcResponse = convertToGrpcResponse(businessResponse);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            if (businessResponse.getSuccess()) {
                log.info("GRPC number selection by prefix completed successfully, returned {} numbers", 
                        businessResponse.getTotalCount());
            } else {
                log.warn("GRPC number selection by prefix failed: {}", businessResponse.getMessage());
            }
            
        } catch (Exception e) {
            log.error("GRPC exception occurred during number selection by prefix", e);
            
            com.nsrs.busacc.grpc.NumberSelectionResponse errorResponse = 
                    com.nsrs.busacc.grpc.NumberSelectionResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Number selection failed: " + e.getMessage())
                            .setTotalCount(0)
                            .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 随机获取号码池
     */
    @Override
    public void getRandomPool(com.nsrs.busacc.grpc.NumberSelectionRequest request,
                             StreamObserver<com.nsrs.busacc.grpc.NumberSelectionResponse> responseObserver) {
        
        log.info("GRPC get random number pool: poolSize: {}, numberType: {}", 
                request.getPoolSize(), request.getNumberType());
        
        // 参数验证
        if (request.getPoolSize() <= 0) {
            sendErrorResponse(responseObserver, "Pool size must be greater than 0");
            return;
        }
        if (request.getPoolSize() > 1000) {
            sendErrorResponse(responseObserver, "Pool size cannot exceed 1000");
            return;
        }
        
        try {
            // 转换GRPC请求为业务DTO
            com.nsrs.busacc.dto.NumberSelectionRequest businessRequest = convertToBusinessRequest(request);
            
            // 调用业务服务
            com.nsrs.busacc.dto.NumberSelectionResponse businessResponse = 
                    numberSelectionService.getRandomPool(businessRequest);
            
            // 转换业务响应为GRPC响应
            com.nsrs.busacc.grpc.NumberSelectionResponse grpcResponse = convertToGrpcResponse(businessResponse);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            if (businessResponse.getSuccess()) {
                log.info("GRPC random number pool completed successfully, returned {} numbers", 
                        businessResponse.getTotalCount());
            } else {
                log.warn("GRPC random number pool failed: {}", businessResponse.getMessage());
            }
            
        } catch (Exception e) {
            log.error("GRPC exception occurred during random number pool selection", e);
            
            com.nsrs.busacc.grpc.NumberSelectionResponse errorResponse = 
                    com.nsrs.busacc.grpc.NumberSelectionResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Get random pool failed: " + e.getMessage())
                            .setTotalCount(0)
                            .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 转换GRPC请求为业务DTO
     */
    private com.nsrs.busacc.dto.NumberSelectionRequest convertToBusinessRequest(
            com.nsrs.busacc.grpc.NumberSelectionRequest grpcRequest) {
        
        com.nsrs.busacc.dto.NumberSelectionRequest businessRequest = 
                new com.nsrs.busacc.dto.NumberSelectionRequest();
        
        if (StringUtils.hasText(grpcRequest.getNumberPrefix())) {
            businessRequest.setNumberPrefix(grpcRequest.getNumberPrefix());
        }
        
        if (grpcRequest.getPoolSize() > 0) {
            businessRequest.setPoolSize(grpcRequest.getPoolSize());
        }
        
        if (grpcRequest.getNumberType() > 0) {
            businessRequest.setNumberType(grpcRequest.getNumberType());
        }
        
        if (grpcRequest.getStatusFilterCount() > 0) {
            Integer[] statusFilter = new Integer[grpcRequest.getStatusFilterCount()];
            for (int i = 0; i < grpcRequest.getStatusFilterCount(); i++) {
                statusFilter[i] = grpcRequest.getStatusFilter(i);
            }
            businessRequest.setStatusFilter(statusFilter);
        }
        
        return businessRequest;
    }
    
    /**
     * 转换业务响应为GRPC响应
     */
    private com.nsrs.busacc.grpc.NumberSelectionResponse convertToGrpcResponse(
            com.nsrs.busacc.dto.NumberSelectionResponse businessResponse) {
        
        com.nsrs.busacc.grpc.NumberSelectionResponse.Builder responseBuilder = 
                com.nsrs.busacc.grpc.NumberSelectionResponse.newBuilder();
        
        responseBuilder.setSuccess(businessResponse.getSuccess() != null ? businessResponse.getSuccess() : false);
        
        if (StringUtils.hasText(businessResponse.getMessage())) {
            responseBuilder.setMessage(businessResponse.getMessage());
        }
        
        if (businessResponse.getTotalCount() != null) {
            responseBuilder.setTotalCount(businessResponse.getTotalCount());
        }
        
        if (StringUtils.hasText(businessResponse.getNumberPrefix())) {
            responseBuilder.setNumberPrefix(businessResponse.getNumberPrefix());
        }
        
        // 转换号码列表
        if (businessResponse.getNumbers() != null && !businessResponse.getNumbers().isEmpty()) {
            List<com.nsrs.busacc.grpc.NumberInfo> grpcNumbers = new ArrayList<>();
            
            for (com.nsrs.busacc.dto.NumberSelectionResponse.NumberInfo businessNumber : businessResponse.getNumbers()) {
                com.nsrs.busacc.grpc.NumberInfo grpcNumber = convertToGrpcNumberInfo(businessNumber);
                grpcNumbers.add(grpcNumber);
            }
            
            responseBuilder.addAllNumbers(grpcNumbers);
        }
        
        return responseBuilder.build();
    }
    
    /**
     * 转换号码信息为GRPC格式
     */
    private com.nsrs.busacc.grpc.NumberInfo convertToGrpcNumberInfo(
            com.nsrs.busacc.dto.NumberSelectionResponse.NumberInfo businessNumber) {
        
        com.nsrs.busacc.grpc.NumberInfo.Builder numberBuilder = 
                com.nsrs.busacc.grpc.NumberInfo.newBuilder();
        
        if (businessNumber.getNumberId() != null) {
            numberBuilder.setNumberId(businessNumber.getNumberId());
        }
        
        if (StringUtils.hasText(businessNumber.getNumber())) {
            numberBuilder.setNumber(businessNumber.getNumber());
        }
        
        if (businessNumber.getNumberType() != null) {
            numberBuilder.setNumberType(businessNumber.getNumberType());
        }
        
        if (businessNumber.getStatus() != null) {
            numberBuilder.setStatus(businessNumber.getStatus());
        }
        
        if (businessNumber.getCharge() != null) {
            numberBuilder.setCharge(businessNumber.getCharge().toString());
        }
        
        if (businessNumber.getSegmentId() != null) {
            numberBuilder.setSegmentId(businessNumber.getSegmentId());
        }
        
        if (businessNumber.getLevelId() != null) {
            numberBuilder.setLevelId(businessNumber.getLevelId());
        }
        
        return numberBuilder.build();
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(StreamObserver<com.nsrs.busacc.grpc.NumberSelectionResponse> responseObserver, String errorMessage) {
        com.nsrs.busacc.grpc.NumberSelectionResponse errorResponse = 
                com.nsrs.busacc.grpc.NumberSelectionResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(errorMessage)
                        .setTotalCount(0)
                        .build();
        
        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
        
        log.warn("GRPC parameter validation failed: {}", errorMessage);
    }
}