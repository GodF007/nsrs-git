package com.nsrs.busacc.config;

import io.grpc.protobuf.services.ProtoReflectionService;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC 反射服务配置
 * 启用 gRPC 服务反射，用于动态发现服务和方法
 */
@Configuration
public class GrpcReflectionConfig {

    /**
     * 配置 gRPC 服务器，添加反射服务
     * @return GrpcServerConfigurer
     */
    @Bean
    public GrpcServerConfigurer grpcServerConfigurer() {
        return serverBuilder -> {
            // 添加 gRPC 反射服务
            serverBuilder.addService(ProtoReflectionService.newInstance());
        };
    }
}