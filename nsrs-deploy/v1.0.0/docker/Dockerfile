# 使用多阶段构建来优化镜像大小
# 第一阶段：构建阶段
FROM maven:3.8.6-openjdk-8-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 文件
COPY pom.xml .
COPY nsrs-common/pom.xml nsrs-common/
COPY nsrs-framework/pom.xml nsrs-framework/
COPY nsrs-msisdn/pom.xml nsrs-msisdn/
COPY nsrs-simcard/pom.xml nsrs-simcard/
COPY nsrs-binding/pom.xml nsrs-binding/
COPY nsrs-boot/pom.xml nsrs-boot/

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY nsrs-common/src nsrs-common/src
COPY nsrs-framework/src nsrs-framework/src
COPY nsrs-msisdn/src nsrs-msisdn/src
COPY nsrs-simcard/src nsrs-simcard/src
COPY nsrs-binding/src nsrs-binding/src
COPY nsrs-boot/src nsrs-boot/src

# 构建应用
RUN mvn clean package -DskipTests -B

# 第二阶段：运行阶段
FROM openjdk:8-jre-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r nsrs && useradd -r -g nsrs nsrs

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/nsrs-boot/target/nsrs-boot-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R nsrs:nsrs /app

# 切换到应用用户
USER nsrs

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]