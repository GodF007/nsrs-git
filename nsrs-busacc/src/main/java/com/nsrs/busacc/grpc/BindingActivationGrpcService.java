package com.nsrs.busacc.grpc;

// import com.nsrs.busacc.grpc.BindingActivationServiceGrpc;
// import com.nsrs.busacc.grpc.UnbindRequest;
// import com.nsrs.busacc.grpc.UnbindResponse;
import com.nsrs.busacc.service.BindingActivationService;
import com.nsrs.common.model.CommonResult;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Date;

/**
 * 绑定激活GRPC服务实现
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class BindingActivationGrpcService extends BindingActivationServiceGrpc.BindingActivationServiceImplBase {
    
    private final BindingActivationService bindingActivationService;
    
    /**
     * 绑定激活号码和IMSI
     */
    @Override
    public void bindAndActivate(com.nsrs.busacc.grpc.BindingActivationRequest request,
                               StreamObserver<com.nsrs.busacc.grpc.BindingActivationResponse> responseObserver) {
        
        log.info("GRPC received binding activation request for number: {}, imsi: {}, iccid: {}", 
                request.getNumber(), request.getImsi(), request.getIccid());
        
        // 参数验证
        if (!StringUtils.hasText(request.getNumber())) {
            sendErrorResponse(responseObserver, "Number cannot be empty");
            return;
        }
        if (!StringUtils.hasText(request.getImsi())) {
            sendErrorResponse(responseObserver, "IMSI cannot be empty");
            return;
        }
        if (!StringUtils.hasText(request.getIccid())) {
            sendErrorResponse(responseObserver, "ICCID cannot be empty");
            return;
        }
        if (request.getOperatorUserId() <= 0) {
            sendErrorResponse(responseObserver, "Operator user ID must be greater than 0");
            return;
        }
        
        try {
            // 转换GRPC请求为业务DTO
            com.nsrs.busacc.dto.BindingActivationRequest businessRequest = convertToBusinessRequest(request);
            
            // 调用业务服务
            CommonResult<com.nsrs.busacc.dto.BindingActivationResponse> result = 
                    bindingActivationService.bindAndActivate(businessRequest);
            
            // 转换业务响应为GRPC响应
            com.nsrs.busacc.grpc.BindingActivationResponse grpcResponse = convertToGrpcResponse(result);
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            if (result.getSuccess()) {
                log.info("GRPC binding activation completed successfully for number: {}", request.getNumber());
            } else {
                log.warn("GRPC binding activation failed for number: {}, reason: {}", 
                        request.getNumber(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("GRPC exception occurred during binding activation for number: {}", 
                    request.getNumber(), e);
            
            com.nsrs.busacc.grpc.BindingActivationResponse errorResponse = 
                    com.nsrs.busacc.grpc.BindingActivationResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Binding and activation failed: " + e.getMessage())
                            .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 根据号码解绑
     */
    @Override
    public void unbindByNumber(UnbindRequest request,
                              StreamObserver<UnbindResponse> responseObserver) {
        
        log.info("GRPC received unbinding request for number: {}, operatorUserId: {}", 
                request.getNumber(), request.getOperatorUserId());
        
        // 参数验证
        if (!StringUtils.hasText(request.getNumber())) {
            sendUnbindErrorResponse(responseObserver, "Number cannot be empty");
            return;
        }
        if (request.getOperatorUserId() <= 0) {
            sendUnbindErrorResponse(responseObserver, "Operator user ID must be greater than 0");
            return;
        }
        
        try {
            // 调用业务服务
            CommonResult<Void> result = bindingActivationService.unbindByNumber(
                    request.getNumber(), 
                    request.getOperatorUserId(), 
                    StringUtils.hasText(request.getRemark()) ? request.getRemark() : null
            );
            
            // 构建GRPC响应
            UnbindResponse grpcResponse = UnbindResponse.newBuilder()
                    .setSuccess(result.getSuccess())
                    .setMessage(result.getMessage() != null ? result.getMessage() : "")
                    .build();
            
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            if (result.getSuccess()) {
                log.info("GRPC unbinding completed successfully for number: {}", request.getNumber());
            } else {
                log.warn("GRPC unbinding failed for number: {}, reason: {}", 
                        request.getNumber(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("GRPC exception occurred during unbinding for number: {}", request.getNumber(), e);
            
            UnbindResponse errorResponse = UnbindResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Unbinding failed: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 转换GRPC请求为业务DTO
     */
    private com.nsrs.busacc.dto.BindingActivationRequest convertToBusinessRequest(
            com.nsrs.busacc.grpc.BindingActivationRequest grpcRequest) {
        
        com.nsrs.busacc.dto.BindingActivationRequest businessRequest = 
                new com.nsrs.busacc.dto.BindingActivationRequest();
        
        businessRequest.setNumber(grpcRequest.getNumber());
        businessRequest.setImsi(grpcRequest.getImsi());
        businessRequest.setIccid(grpcRequest.getIccid());
        
        if (grpcRequest.getOrderId() > 0) {
            businessRequest.setOrderId(grpcRequest.getOrderId());
        }
        
        if (grpcRequest.getBindingType() > 0) {
            businessRequest.setBindingType(grpcRequest.getBindingType());
        }
        
        businessRequest.setOperatorUserId(grpcRequest.getOperatorUserId());
        
        if (StringUtils.hasText(grpcRequest.getRemark())) {
            businessRequest.setRemark(grpcRequest.getRemark());
        }
        
        return businessRequest;
    }
    
    /**
     * 转换业务响应为GRPC响应
     */
    private com.nsrs.busacc.grpc.BindingActivationResponse convertToGrpcResponse(
            CommonResult<com.nsrs.busacc.dto.BindingActivationResponse> businessResult) {
        
        com.nsrs.busacc.grpc.BindingActivationResponse.Builder responseBuilder = 
                com.nsrs.busacc.grpc.BindingActivationResponse.newBuilder();
        
        responseBuilder.setSuccess(businessResult.getSuccess());
        
        if (businessResult.getMessage() != null) {
            responseBuilder.setMessage(businessResult.getMessage());
        }
        
        if (businessResult.getSuccess() && businessResult.getData() != null) {
            com.nsrs.busacc.dto.BindingActivationResponse businessResponse = businessResult.getData();
            
            if (businessResponse.getBindingId() != null) {
                responseBuilder.setBindingId(businessResponse.getBindingId());
            }
            
            if (StringUtils.hasText(businessResponse.getNumber())) {
                responseBuilder.setNumber(businessResponse.getNumber());
            }
            
            if (StringUtils.hasText(businessResponse.getImsi())) {
                responseBuilder.setImsi(businessResponse.getImsi());
            }
            
            if (StringUtils.hasText(businessResponse.getIccid())) {
                responseBuilder.setIccid(businessResponse.getIccid());
            }
            
            if (businessResponse.getBindingStatus() != null) {
                responseBuilder.setBindingStatus(businessResponse.getBindingStatus());
            }
            
            if (businessResponse.getBindingTime() != null) {
                responseBuilder.setBindingTime(convertDateToTimestamp(businessResponse.getBindingTime()));
            }
        }
        
        return responseBuilder.build();
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
     * 发送绑定激活错误响应
     */
    private void sendErrorResponse(StreamObserver<com.nsrs.busacc.grpc.BindingActivationResponse> responseObserver, String message) {
        com.nsrs.busacc.grpc.BindingActivationResponse errorResponse = 
                com.nsrs.busacc.grpc.BindingActivationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(message)
                        .build();
        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
        log.warn("GRPC binding activation parameter validation failed: {}", message);
    }
    
    /**
     * 发送解绑错误响应
     */
    private void sendUnbindErrorResponse(StreamObserver<UnbindResponse> responseObserver, String message) {
        UnbindResponse errorResponse = UnbindResponse.newBuilder()
                .setSuccess(false)
                .setMessage(message)
                .build();
        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
        log.warn("GRPC unbind parameter validation failed: {}", message);
    }
}