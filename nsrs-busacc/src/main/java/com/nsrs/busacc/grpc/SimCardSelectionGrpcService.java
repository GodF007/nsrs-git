package com.nsrs.busacc.grpc;

import com.nsrs.busacc.service.SimCardSelectionService;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SIM卡选择GRPC服务实现
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class SimCardSelectionGrpcService extends SimCardSelectionServiceGrpc.SimCardSelectionServiceImplBase {
    
    private final SimCardSelectionService simCardSelectionService;
    
    /**
     * 选择SIM卡（统一接口）
     */
    @Override
    public void selectSimCards(com.nsrs.busacc.grpc.SimCardSelectionRequest request,
                              StreamObserver<com.nsrs.busacc.grpc.SimCardSelectionResponse> responseObserver) {
        
        if (StringUtils.hasText(request.getIccidSuffix())) {
            log.info("GRPC SimCard selection by suffix: suffix={}, poolSize={}, dataType={}", 
                    request.getIccidSuffix(), request.getPoolSize(), request.getDataType());
        } else {
            log.info("GRPC SimCard selection: poolSize={}, dataType={}", 
                    request.getPoolSize(), request.getDataType());
        }
        
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
            com.nsrs.busacc.dto.SimCardSelectionRequest businessRequest = convertToBusinessRequest(request);
            
            // 调用业务服务
            com.nsrs.busacc.dto.SimCardSelectionResponse businessResponse = 
                    simCardSelectionService.selectSimCards(businessRequest);
            
            // 转换业务响应为GRPC响应
            com.nsrs.busacc.grpc.SimCardSelectionResponse grpcResponse = convertToGrpcResponse(businessResponse);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            if (businessResponse.getSuccess()) {
                log.info("GRPC sim card selection completed successfully, returned {} sim cards", 
                        businessResponse.getTotalCount());
            } else {
                log.warn("GRPC sim card selection failed: {}", businessResponse.getMessage());
            }
            
        } catch (Exception e) {
            log.error("GRPC exception occurred during sim card selection", e);
            
            com.nsrs.busacc.grpc.SimCardSelectionResponse errorResponse = 
                    com.nsrs.busacc.grpc.SimCardSelectionResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Failed to select SIM cards: " + e.getMessage())
                            .setTotalCount(0)
                            .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 随机获取SIM卡池
     */
    @Override
    public void getRandomPool(com.nsrs.busacc.grpc.SimCardSelectionRequest request,
                             StreamObserver<com.nsrs.busacc.grpc.SimCardSelectionResponse> responseObserver) {
        
        log.info("GRPC random sim card selection: poolSize={}, dataType={}", 
                request.getPoolSize(), request.getDataType());
        
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
            com.nsrs.busacc.dto.SimCardSelectionRequest businessRequest = convertToBusinessRequest(request);
            
            // 调用业务服务
            com.nsrs.busacc.dto.SimCardSelectionResponse businessResponse = 
                    simCardSelectionService.getRandomPool(businessRequest);
            
            // 转换业务响应为GRPC响应
            com.nsrs.busacc.grpc.SimCardSelectionResponse grpcResponse = convertToGrpcResponse(businessResponse);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            if (businessResponse.getSuccess()) {
                log.info("GRPC random sim card pool completed successfully, returned {} sim cards", 
                        businessResponse.getTotalCount());
            } else {
                log.warn("GRPC random sim card pool failed: {}", businessResponse.getMessage());
            }
            
        } catch (Exception e) {
            log.error("GRPC exception occurred during random sim card pool selection", e);
            
            com.nsrs.busacc.grpc.SimCardSelectionResponse errorResponse = 
                    com.nsrs.busacc.grpc.SimCardSelectionResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Failed to get random SIM card pool: " + e.getMessage())
                            .setTotalCount(0)
                            .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 转换GRPC请求为业务DTO
     */
    private com.nsrs.busacc.dto.SimCardSelectionRequest convertToBusinessRequest(
            com.nsrs.busacc.grpc.SimCardSelectionRequest grpcRequest) {
        
        com.nsrs.busacc.dto.SimCardSelectionRequest businessRequest = 
                new com.nsrs.busacc.dto.SimCardSelectionRequest();
        
        if (StringUtils.hasText(grpcRequest.getIccidSuffix())) {
            businessRequest.setIccidSuffix(grpcRequest.getIccidSuffix());
        }
        
        if (grpcRequest.getPoolSize() > 0) {
            businessRequest.setPoolSize(grpcRequest.getPoolSize());
        }
        
        if (grpcRequest.getDataType() > 0) {
            businessRequest.setDataType(grpcRequest.getDataType());
        }
        
        if (grpcRequest.getStatusFilterCount() > 0) {
            Integer[] statusFilter = new Integer[grpcRequest.getStatusFilterCount()];
            for (int i = 0; i < grpcRequest.getStatusFilterCount(); i++) {
                statusFilter[i] = grpcRequest.getStatusFilter(i);
            }
            businessRequest.setStatusFilter(statusFilter);
        }
        
        if (grpcRequest.getSupplierId() > 0) {
            businessRequest.setSupplierId(grpcRequest.getSupplierId());
        }
        
        if (grpcRequest.getOrganizationId() > 0) {
            businessRequest.setOrganizationId(grpcRequest.getOrganizationId());
        }
        
        if (grpcRequest.getBatchId() > 0) {
            businessRequest.setBatchId(grpcRequest.getBatchId());
        }
        
        return businessRequest;
    }
    
    /**
     * 转换业务响应为GRPC响应
     */
    private com.nsrs.busacc.grpc.SimCardSelectionResponse convertToGrpcResponse(
            com.nsrs.busacc.dto.SimCardSelectionResponse businessResponse) {
        
        com.nsrs.busacc.grpc.SimCardSelectionResponse.Builder responseBuilder = 
                com.nsrs.busacc.grpc.SimCardSelectionResponse.newBuilder();
        
        responseBuilder.setSuccess(businessResponse.getSuccess() != null ? businessResponse.getSuccess() : false);
        
        if (StringUtils.hasText(businessResponse.getMessage())) {
            responseBuilder.setMessage(businessResponse.getMessage());
        }
        
        if (businessResponse.getTotalCount() != null) {
            responseBuilder.setTotalCount(businessResponse.getTotalCount());
        }
        
        if (StringUtils.hasText(businessResponse.getIccidSuffix())) {
            responseBuilder.setIccidSuffix(businessResponse.getIccidSuffix());
        }
        
        // 转换SIM卡列表
        if (businessResponse.getSimCards() != null && !businessResponse.getSimCards().isEmpty()) {
            List<com.nsrs.busacc.grpc.SimCardInfo> grpcSimCards = new ArrayList<>();
            
            for (com.nsrs.busacc.dto.SimCardSelectionResponse.SimCardInfo businessSimCard : businessResponse.getSimCards()) {
                com.nsrs.busacc.grpc.SimCardInfo grpcSimCard = convertToGrpcSimCardInfo(businessSimCard);
                grpcSimCards.add(grpcSimCard);
            }
            
            responseBuilder.addAllSimCards(grpcSimCards);
        }
        
        return responseBuilder.build();
    }
    
    /**
     * 转换SIM卡信息为GRPC格式
     */
    private com.nsrs.busacc.grpc.SimCardInfo convertToGrpcSimCardInfo(
            com.nsrs.busacc.dto.SimCardSelectionResponse.SimCardInfo businessSimCard) {
        
        com.nsrs.busacc.grpc.SimCardInfo.Builder simCardBuilder = 
                com.nsrs.busacc.grpc.SimCardInfo.newBuilder();
        
        if (businessSimCard.getCardId() != null) {
            simCardBuilder.setCardId(businessSimCard.getCardId());
        }
        
        if (StringUtils.hasText(businessSimCard.getIccid())) {
            simCardBuilder.setIccid(businessSimCard.getIccid());
        }
        
        if (StringUtils.hasText(businessSimCard.getImsi())) {
            simCardBuilder.setImsi(businessSimCard.getImsi());
        }
        
        if (businessSimCard.getDataType() != null) {
            simCardBuilder.setDataType(businessSimCard.getDataType());
        }
        
        if (businessSimCard.getStatus() != null) {
            simCardBuilder.setStatus(businessSimCard.getStatus());
        }
        
        if (businessSimCard.getBatchId() != null) {
            simCardBuilder.setBatchId(businessSimCard.getBatchId());
        }
        
        if (businessSimCard.getSupplierId() != null) {
            simCardBuilder.setSupplierId(businessSimCard.getSupplierId());
        }
        
        if (businessSimCard.getOrganizationId() != null) {
            simCardBuilder.setOrganizationId(businessSimCard.getOrganizationId());
        }
        
        if (businessSimCard.getCreateTime() != null) {
            simCardBuilder.setCreateTime(convertDateToTimestamp(businessSimCard.getCreateTime()));
        }
        
        if (StringUtils.hasText(businessSimCard.getRemark())) {
            simCardBuilder.setRemark(businessSimCard.getRemark());
        }
        
        return simCardBuilder.build();
    }
    
    /**
     * 转换Date为Timestamp
     */
    private Timestamp convertDateToTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        
        Instant instant = date.toInstant();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(StreamObserver<com.nsrs.busacc.grpc.SimCardSelectionResponse> responseObserver, String message) {
        com.nsrs.busacc.grpc.SimCardSelectionResponse errorResponse = 
                com.nsrs.busacc.grpc.SimCardSelectionResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(message)
                        .setTotalCount(0)
                        .build();
        
        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
    }
}