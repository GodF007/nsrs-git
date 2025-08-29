#!/bin/bash

# NSRS Docker镜像构建脚本
# 作用：自动化构建NSRS应用的Docker镜像
# 说明：需要先编译生成NSRS.jar文件，然后构建Docker镜像

set -e  # 遇到错误立即退出

echo "开始构建NSRS Docker镜像..."

# 配置变量
IMAGE_NAME="nsrs"
IMAGE_TAG="1.0.0"
REGISTRY="10.21.1.210:5000"  # 根据实际镜像仓库地址修改
FULL_IMAGE_NAME="$REGISTRY/$IMAGE_NAME:$IMAGE_TAG"

# 项目根目录（假设脚本在nsrs-deploy/simple-deploy目录下）
PROJECT_ROOT="$(cd "$(dirname "$0")/../../" && pwd)"
DEPLOY_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "项目根目录: $PROJECT_ROOT"
echo "部署目录: $DEPLOY_DIR"
echo "镜像名称: $FULL_IMAGE_NAME"

# 检查Docker是否可用
if ! command -v docker &> /dev/null; then
    echo "错误: Docker命令未找到，请确保已安装Docker"
    exit 1
fi

# 检查Maven是否可用
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven命令未找到，请确保已安装Maven"
    exit 1
fi

echo "✓ Docker和Maven环境检查通过"

# 1. 编译项目
echo "1. 编译NSRS项目..."
cd "$PROJECT_ROOT"

# 清理并编译项目
echo "执行Maven编译..."
mvn clean package -DskipTests -Dmaven.test.skip=true

if [ $? -ne 0 ]; then
    echo "错误: Maven编译失败"
    exit 1
fi

echo "✓ 项目编译完成"

# 2. 查找生成的JAR文件
echo "2. 查找生成的JAR文件..."
JAR_FILE=$(find "$PROJECT_ROOT" -name "*.jar" -not -path "*/target/original-*" -not -path "*/target/*-sources.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "错误: 未找到编译生成的JAR文件"
    echo "请检查Maven编译是否成功"
    exit 1
fi

echo "找到JAR文件: $JAR_FILE"

# 3. 复制JAR文件到构建目录
echo "3. 准备构建文件..."
cp "$JAR_FILE" "$DEPLOY_DIR/NSRS.jar"

if [ ! -f "$DEPLOY_DIR/NSRS.jar" ]; then
    echo "错误: JAR文件复制失败"
    exit 1
fi

echo "✓ JAR文件准备完成"

# 4. 构建Docker镜像
echo "4. 构建Docker镜像..."
cd "$DEPLOY_DIR"

# 检查Dockerfile是否存在
if [ ! -f "Dockerfile" ]; then
    echo "错误: Dockerfile不存在"
    exit 1
fi

echo "开始构建镜像: $FULL_IMAGE_NAME"
docker build -t "$FULL_IMAGE_NAME" .

if [ $? -ne 0 ]; then
    echo "错误: Docker镜像构建失败"
    exit 1
fi

echo "✓ Docker镜像构建完成"

# 5. 推送镜像到仓库（可选）
read -p "是否推送镜像到仓库? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "5. 推送镜像到仓库..."
    docker push "$FULL_IMAGE_NAME"
    
    if [ $? -eq 0 ]; then
        echo "✓ 镜像推送完成"
    else
        echo "警告: 镜像推送失败，请检查仓库连接和权限"
    fi
else
    echo "跳过镜像推送"
fi

# 6. 清理临时文件
echo "6. 清理临时文件..."
rm -f "$DEPLOY_DIR/NSRS.jar"
echo "✓ 清理完成"

# 7. 显示构建结果
echo "\n=== 构建完成 ==="
echo "镜像名称: $FULL_IMAGE_NAME"
echo "镜像大小: $(docker images $FULL_IMAGE_NAME --format 'table {{.Size}}' | tail -1)"
echo "\n可用命令:"
echo "查看镜像: docker images $FULL_IMAGE_NAME"
echo "运行容器: docker run -p 8080:8080 $FULL_IMAGE_NAME"
echo "部署到K8s: 更新deployment.yml中的镜像地址后执行部署脚本"

echo "\n🎉 NSRS Docker镜像构建完成！"
echo "\n下一步:"
echo "1. 更新 k8s/deployment.yml 中的镜像地址为: $FULL_IMAGE_NAME"
echo "2. 执行部署脚本: ./scripts/deploy.sh"