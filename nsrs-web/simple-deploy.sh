#!/bin/bash

# NSRS-Web 简化部署脚本
# 本地打包 -> 上传到远程Tomcat

set -e

# 颜色输出
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_step() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')] $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

echo -e "${BLUE}"
echo "========================================"
echo "          NSRS-Web 简化部署"
echo "========================================"
echo -e "${NC}"

# 配置参数
SERVER_IP="${1}"
SERVER_USER="${2}"
TOMCAT_PATH="/opt/tomcat/webapps"
APP_NAME="nsrs-web"

# 检查参数
if [ -z "$SERVER_IP" ] || [ -z "$SERVER_USER" ]; then
    echo "使用方法: ./simple-deploy.sh <服务器IP> <用户名>"
    echo "示例: ./simple-deploy.sh 192.168.1.100 root"
    exit 1
fi

# 检查必要工具
if ! command -v npm >/dev/null 2>&1; then
    print_error "npm 未安装，请先安装 Node.js"
    exit 1
fi

if ! command -v scp >/dev/null 2>&1; then
    print_error "scp 未安装，请先安装 OpenSSH 客户端"
    exit 1
fi

if ! command -v jar >/dev/null 2>&1; then
    print_error "jar 命令未找到，请先安装 JDK"
    exit 1
fi

print_step "[1/4] 清理旧文件..."
rm -rf dist
rm -f "${APP_NAME}.war"
print_success "清理完成"

print_step "[2/4] 安装依赖并构建项目..."
npm install
if [ $? -ne 0 ]; then
    print_error "依赖安装失败！"
    exit 1
fi

npm run build
if [ $? -ne 0 ]; then
    print_error "构建失败！"
    exit 1
fi
print_success "构建完成"

print_step "[3/4] 创建WAR包..."
# 创建WAR目录结构
mkdir -p war-temp
cp -r dist/* war-temp/

# 创建WEB-INF目录和web.xml
mkdir -p war-temp/WEB-INF
cat > war-temp/WEB-INF/web.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <display-name>NSRS-Web</display-name>

    <!-- SPA路由支持 -->
    <error-page>
        <error-code>404</error-code>
        <location>/index.html</location>
    </error-page>

    <!-- 静态资源缓存 -->
    <filter>
        <filter-name>CacheFilter</filter-name>
        <filter-class>org.apache.catalina.filters.ExpiresFilter</filter-class>
        <init-param>
            <param-name>ExpiresByType text/css</param-name>
            <param-value>access plus 1 year</param-value>
        </init-param>
        <init-param>
            <param-name>ExpiresByType application/javascript</param-name>
            <param-value>access plus 1 year</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>CacheFilter</filter-name>
        <url-pattern>*.css</url-pattern>
    </filter-mapping>
    <filter-mapping>
        <filter-name>CacheFilter</filter-name>
        <url-pattern>*.js</url-pattern>
    </filter-mapping>

</web-app>
EOF

# 打包成WAR文件
cd war-temp
jar -cf "../${APP_NAME}.war" *
cd ..
rm -rf war-temp
print_success "WAR包创建完成: ${APP_NAME}.war"

print_step "[4/4] 上传并部署到服务器..."
print_step "正在上传WAR文件到服务器..."
scp "${APP_NAME}.war" "${SERVER_USER}@${SERVER_IP}:~/"
if [ $? -ne 0 ]; then
    print_error "文件上传失败！请检查网络连接和SSH配置"
    exit 1
fi

print_step "正在部署到Tomcat..."
ssh "${SERVER_USER}@${SERVER_IP}" << EOF
    # 备份现有应用（如果存在）
    if [ -d "${TOMCAT_PATH}/${APP_NAME}" ]; then
        sudo cp -r "${TOMCAT_PATH}/${APP_NAME}" "${TOMCAT_PATH}/${APP_NAME}.backup.\$(date +%Y%m%d_%H%M%S)"
        echo "已备份现有应用"
    fi
    
    # 删除旧应用
    sudo rm -rf "${TOMCAT_PATH}/${APP_NAME}"
    
    # 部署新应用
    sudo mv "~/${APP_NAME}.war" "${TOMCAT_PATH}/"
    sudo chown tomcat:tomcat "${TOMCAT_PATH}/${APP_NAME}.war" 2>/dev/null || true
    
    echo "部署完成"
EOF

if [ $? -ne 0 ]; then
    print_error "部署失败！请检查服务器权限和Tomcat配置"
    exit 1
fi

echo
echo -e "${GREEN}"
echo "========================================"
echo "           部署成功完成！"
echo "========================================"
echo -e "${NC}"
echo "访问地址: http://${SERVER_IP}:8080/${APP_NAME}/"
echo
print_warning "如果无法访问，请检查:"
echo "1. Tomcat服务是否运行"
echo "2. 防火墙端口8080是否开放"
echo "3. 等待1-2分钟让Tomcat解压WAR文件"
echo

# 清理本地文件
rm -f "${APP_NAME}.war"
print_success "本地清理完成"

echo "部署脚本执行完毕！"