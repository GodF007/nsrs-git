# 使用官方的OpenJDK基础镜像
FROM 10.21.1.210:5000/openjdk:8-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制JAR文件
COPY NSRS.jar /app/NSRS.jar

# 定义环境变量
ENV JAVA_OPTS="-Duser.timezone=Asia/Shanghai -Xmx2048m -Xms512m"

ENTRYPOINT ["sh", "-c", "sleep 2 && java $JAVA_OPTS -jar /app/NSRS.jar --spring.config.location=/app/application-sharding.yml"]

EXPOSE 8088 8089