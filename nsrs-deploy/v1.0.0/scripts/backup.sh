#!/bin/bash

# NSRS 备份脚本
# 用于备份数据库和配置文件

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
NAMESPACE="nsrs"
DB_NAME="mysql"
BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d-%H%M%S)
BACKUP_PREFIX="nsrs-backup-$DATE"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "NSRS 备份脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help          显示此帮助信息"
    echo "  -d, --dir DIR       指定备份目录 (默认: ./backups)"
    echo "  -n, --namespace NS  指定命名空间 (默认: nsrs)"
    echo "  --db-only          仅备份数据库"
    echo "  --config-only      仅备份配置"
    echo "  --list             列出现有备份"
    echo "  --restore FILE     恢复指定的备份文件"
    echo ""
    echo "示例:"
    echo "  $0                 # 完整备份"
    echo "  $0 --db-only       # 仅备份数据库"
    echo "  $0 --config-only   # 仅备份配置"
    echo "  $0 --list          # 列出备份"
    echo "  $0 --restore nsrs-backup-20250127-120000-db.sql  # 恢复数据库"
}

# 创建备份目录
create_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        log_info "创建备份目录: $BACKUP_DIR"
    fi
}

# 检查 kubectl 连接
check_kubectl() {
    if ! kubectl cluster-info &>/dev/null; then
        log_error "无法连接到 Kubernetes 集群"
        exit 1
    fi
}

# 检查命名空间
check_namespace() {
    if ! kubectl get namespace $NAMESPACE &>/dev/null; then
        log_error "命名空间 $NAMESPACE 不存在"
        exit 1
    fi
}

# 备份数据库
backup_database() {
    log_info "开始备份数据库..."
    
    # 获取数据库 Pod
    DB_POD=$(kubectl get pods -n $NAMESPACE -l app=$DB_NAME --no-headers | head -1 | awk '{print $1}' 2>/dev/null || echo "")
    if [ -z "$DB_POD" ]; then
        log_error "未找到数据库 Pod"
        return 1
    fi
    
    # 获取数据库密码
    DB_PASSWORD=$(kubectl get secret nsrs-secret -n $NAMESPACE -o jsonpath='{.data.MYSQL_ROOT_PASSWORD}' | base64 -d 2>/dev/null || echo "root123")
    
    # 执行备份
    DB_BACKUP_FILE="$BACKUP_DIR/${BACKUP_PREFIX}-db.sql"
    log_info "备份数据库到: $DB_BACKUP_FILE"
    
    if kubectl exec -n $NAMESPACE $DB_POD -- mysqldump -u root -p$DB_PASSWORD --single-transaction --routines --triggers nsrs > "$DB_BACKUP_FILE" 2>/dev/null; then
        # 压缩备份文件
        gzip "$DB_BACKUP_FILE"
        DB_BACKUP_FILE="${DB_BACKUP_FILE}.gz"
        
        # 获取文件大小
        BACKUP_SIZE=$(du -h "$DB_BACKUP_FILE" | cut -f1)
        log_success "数据库备份完成: $DB_BACKUP_FILE (大小: $BACKUP_SIZE)"
        
        # 验证备份文件
        if [ -s "$DB_BACKUP_FILE" ]; then
            log_success "备份文件验证通过"
        else
            log_error "备份文件为空或损坏"
            return 1
        fi
    else
        log_error "数据库备份失败"
        return 1
    fi
}

# 备份配置文件
backup_config() {
    log_info "开始备份配置文件..."
    
    CONFIG_BACKUP_FILE="$BACKUP_DIR/${BACKUP_PREFIX}-config.yaml"
    log_info "备份配置到: $CONFIG_BACKUP_FILE"
    
    # 备份所有 Kubernetes 资源
    if kubectl get all,configmap,secret,pvc,ingress,hpa,servicemonitor,networkpolicy -n $NAMESPACE -o yaml > "$CONFIG_BACKUP_FILE" 2>/dev/null; then
        # 压缩配置文件
        gzip "$CONFIG_BACKUP_FILE"
        CONFIG_BACKUP_FILE="${CONFIG_BACKUP_FILE}.gz"
        
        # 获取文件大小
        BACKUP_SIZE=$(du -h "$CONFIG_BACKUP_FILE" | cut -f1)
        log_success "配置备份完成: $CONFIG_BACKUP_FILE (大小: $BACKUP_SIZE)"
    else
        log_error "配置备份失败"
        return 1
    fi
}

# 备份应用日志
backup_logs() {
    log_info "开始备份应用日志..."
    
    LOGS_BACKUP_DIR="$BACKUP_DIR/${BACKUP_PREFIX}-logs"
    mkdir -p "$LOGS_BACKUP_DIR"
    
    # 备份应用日志
    APP_PODS=$(kubectl get pods -n $NAMESPACE -l app=nsrs-app --no-headers | awk '{print $1}' 2>/dev/null || echo "")
    if [ -n "$APP_PODS" ]; then
        echo "$APP_PODS" | while read POD_NAME; do
            if [ -n "$POD_NAME" ]; then
                log_info "备份 Pod $POD_NAME 的日志..."
                kubectl logs $POD_NAME -n $NAMESPACE > "$LOGS_BACKUP_DIR/${POD_NAME}.log" 2>/dev/null || true
                
                # 备份上一个容器的日志 (如果存在)
                kubectl logs $POD_NAME -n $NAMESPACE --previous > "$LOGS_BACKUP_DIR/${POD_NAME}-previous.log" 2>/dev/null || true
            fi
        done
    fi
    
    # 备份数据库日志
    DB_PODS=$(kubectl get pods -n $NAMESPACE -l app=$DB_NAME --no-headers | awk '{print $1}' 2>/dev/null || echo "")
    if [ -n "$DB_PODS" ]; then
        echo "$DB_PODS" | while read POD_NAME; do
            if [ -n "$POD_NAME" ]; then
                log_info "备份 Pod $POD_NAME 的日志..."
                kubectl logs $POD_NAME -n $NAMESPACE > "$LOGS_BACKUP_DIR/${POD_NAME}.log" 2>/dev/null || true
            fi
        done
    fi
    
    # 压缩日志目录
    if [ -d "$LOGS_BACKUP_DIR" ] && [ "$(ls -A $LOGS_BACKUP_DIR)" ]; then
        tar -czf "${LOGS_BACKUP_DIR}.tar.gz" -C "$BACKUP_DIR" "$(basename $LOGS_BACKUP_DIR)"
        rm -rf "$LOGS_BACKUP_DIR"
        
        BACKUP_SIZE=$(du -h "${LOGS_BACKUP_DIR}.tar.gz" | cut -f1)
        log_success "日志备份完成: ${LOGS_BACKUP_DIR}.tar.gz (大小: $BACKUP_SIZE)"
    else
        log_warning "没有找到日志文件"
        rm -rf "$LOGS_BACKUP_DIR"
    fi
}

# 列出现有备份
list_backups() {
    log_info "现有备份文件:"
    
    if [ ! -d "$BACKUP_DIR" ]; then
        log_warning "备份目录不存在: $BACKUP_DIR"
        return 0
    fi
    
    echo "备份目录: $BACKUP_DIR"
    echo "----------------------------------------"
    
    # 按时间排序显示备份文件
    find "$BACKUP_DIR" -name "nsrs-backup-*" -type f | sort | while read file; do
        if [ -f "$file" ]; then
            SIZE=$(du -h "$file" | cut -f1)
            MTIME=$(stat -c %y "$file" 2>/dev/null || stat -f %Sm "$file" 2>/dev/null || echo "未知")
            echo "$(basename "$file") - $SIZE - $MTIME"
        fi
    done
    
    echo "----------------------------------------"
    
    # 显示总大小
    TOTAL_SIZE=$(du -sh "$BACKUP_DIR" 2>/dev/null | cut -f1 || echo "未知")
    echo "总大小: $TOTAL_SIZE"
}

# 恢复数据库
restore_database() {
    local RESTORE_FILE="$1"
    
    if [ ! -f "$RESTORE_FILE" ]; then
        log_error "备份文件不存在: $RESTORE_FILE"
        return 1
    fi
    
    log_info "开始恢复数据库: $RESTORE_FILE"
    
    # 获取数据库 Pod
    DB_POD=$(kubectl get pods -n $NAMESPACE -l app=$DB_NAME --no-headers | head -1 | awk '{print $1}' 2>/dev/null || echo "")
    if [ -z "$DB_POD" ]; then
        log_error "未找到数据库 Pod"
        return 1
    fi
    
    # 获取数据库密码
    DB_PASSWORD=$(kubectl get secret nsrs-secret -n $NAMESPACE -o jsonpath='{.data.MYSQL_ROOT_PASSWORD}' | base64 -d 2>/dev/null || echo "root123")
    
    # 确认恢复操作
    echo -n "确认要恢复数据库吗？这将覆盖现有数据 [y/N]: "
    read -r CONFIRM
    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
        log_info "恢复操作已取消"
        return 0
    fi
    
    # 解压备份文件 (如果需要)
    TEMP_FILE="$RESTORE_FILE"
    if [[ "$RESTORE_FILE" == *.gz ]]; then
        TEMP_FILE="/tmp/$(basename "$RESTORE_FILE" .gz)"
        gunzip -c "$RESTORE_FILE" > "$TEMP_FILE"
    fi
    
    # 执行恢复
    if kubectl exec -i -n $NAMESPACE $DB_POD -- mysql -u root -p$DB_PASSWORD nsrs < "$TEMP_FILE" 2>/dev/null; then
        log_success "数据库恢复完成"
    else
        log_error "数据库恢复失败"
        return 1
    fi
    
    # 清理临时文件
    if [ "$TEMP_FILE" != "$RESTORE_FILE" ]; then
        rm -f "$TEMP_FILE"
    fi
}

# 恢复配置
restore_config() {
    local RESTORE_FILE="$1"
    
    if [ ! -f "$RESTORE_FILE" ]; then
        log_error "配置文件不存在: $RESTORE_FILE"
        return 1
    fi
    
    log_info "开始恢复配置: $RESTORE_FILE"
    
    # 确认恢复操作
    echo -n "确认要恢复配置吗？这将覆盖现有配置 [y/N]: "
    read -r CONFIRM
    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
        log_info "恢复操作已取消"
        return 0
    fi
    
    # 解压配置文件 (如果需要)
    TEMP_FILE="$RESTORE_FILE"
    if [[ "$RESTORE_FILE" == *.gz ]]; then
        TEMP_FILE="/tmp/$(basename "$RESTORE_FILE" .gz)"
        gunzip -c "$RESTORE_FILE" > "$TEMP_FILE"
    fi
    
    # 执行恢复
    if kubectl apply -f "$TEMP_FILE" 2>/dev/null; then
        log_success "配置恢复完成"
    else
        log_error "配置恢复失败"
        return 1
    fi
    
    # 清理临时文件
    if [ "$TEMP_FILE" != "$RESTORE_FILE" ]; then
        rm -f "$TEMP_FILE"
    fi
}

# 清理旧备份
cleanup_old_backups() {
    local KEEP_DAYS=${1:-7}  # 默认保留 7 天
    
    log_info "清理 $KEEP_DAYS 天前的备份文件..."
    
    if [ ! -d "$BACKUP_DIR" ]; then
        log_warning "备份目录不存在: $BACKUP_DIR"
        return 0
    fi
    
    # 查找并删除旧文件
    DELETED_COUNT=0
    find "$BACKUP_DIR" -name "nsrs-backup-*" -type f -mtime +$KEEP_DAYS | while read file; do
        if [ -f "$file" ]; then
            log_info "删除旧备份: $(basename "$file")"
            rm -f "$file"
            DELETED_COUNT=$((DELETED_COUNT + 1))
        fi
    done
    
    if [ $DELETED_COUNT -gt 0 ]; then
        log_success "已删除 $DELETED_COUNT 个旧备份文件"
    else
        log_info "没有需要清理的旧备份文件"
    fi
}

# 主函数
main() {
    local DB_ONLY=false
    local CONFIG_ONLY=false
    local LIST_BACKUPS=false
    local RESTORE_FILE=""
    local CLEANUP_DAYS=""
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -d|--dir)
                BACKUP_DIR="$2"
                shift 2
                ;;
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            --db-only)
                DB_ONLY=true
                shift
                ;;
            --config-only)
                CONFIG_ONLY=true
                shift
                ;;
            --list)
                LIST_BACKUPS=true
                shift
                ;;
            --restore)
                RESTORE_FILE="$2"
                shift 2
                ;;
            --cleanup)
                CLEANUP_DAYS="$2"
                shift 2
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    echo "==========================================="
    echo "         NSRS 备份工具"
    echo "==========================================="
    echo "时间: $(date)"
    echo "命名空间: $NAMESPACE"
    echo "备份目录: $BACKUP_DIR"
    echo "==========================================="
    echo
    
    # 列出备份
    if [ "$LIST_BACKUPS" = true ]; then
        list_backups
        exit 0
    fi
    
    # 恢复备份
    if [ -n "$RESTORE_FILE" ]; then
        check_kubectl
        check_namespace
        
        if [[ "$RESTORE_FILE" == *"-db.sql"* ]]; then
            restore_database "$RESTORE_FILE"
        elif [[ "$RESTORE_FILE" == *"-config.yaml"* ]]; then
            restore_config "$RESTORE_FILE"
        else
            log_error "无法识别备份文件类型: $RESTORE_FILE"
            exit 1
        fi
        exit 0
    fi
    
    # 清理旧备份
    if [ -n "$CLEANUP_DAYS" ]; then
        cleanup_old_backups "$CLEANUP_DAYS"
        exit 0
    fi
    
    # 执行备份
    check_kubectl
    check_namespace
    create_backup_dir
    
    if [ "$CONFIG_ONLY" = true ]; then
        backup_config
    elif [ "$DB_ONLY" = true ]; then
        backup_database
    else
        # 完整备份
        backup_database
        backup_config
        backup_logs
    fi
    
    echo
    echo "==========================================="
    echo "         备份完成"
    echo "==========================================="
    
    # 显示备份文件
    list_backups
}

# 执行主函数
main "$@"