# SIM卡库存告警系统

## 概述

SIM卡库存告警系统提供了SIM卡库存水平的自动监控和通知功能。它包括告警配置、自动告警生成以及通过电子邮件和短信发送通知。

## 功能特性

- **告警配置**：为不同SIM卡类型配置低/高库存阈值
- **自动监控**：定时任务自动检查库存水平
- **告警生成**：当库存超过配置的阈值时生成告警
- **通知发送**：通过电子邮件和短信发送通知
- **告警管理**：查看和管理告警历史和通知状态
- **手动触发**：提供手动检查告警和处理通知的API接口

## 系统组件

### 1. 告警配置 (`sim_card_inventory_alert`)
- 为不同SIM卡规格配置告警规则
- 设置低/高库存阈值
- 配置通知接收人（邮箱和手机号）
- 启用/禁用告警规则

### 2. 告警日志 (`sim_card_inventory_alert_log`)
- 记录所有生成的告警
- 跟踪通知状态（待发送、已发送、发送失败）
- 存储告警详情和时间戳

### 3. 服务层
- `SimCardInventoryAlertService`：管理告警配置和生成告警
- `SimCardInventoryAlertLogService`：管理告警日志和历史记录
- `SimCardAlertNotificationService`：处理通知发送

### 4. 定时任务
- **库存检查**：每30分钟运行一次，检查库存并生成告警
- **通知处理**：每5分钟运行一次，处理待发送的通知
- **失败重试**：每小时运行一次，重试发送失败的通知

## API接口

### 告警配置管理
```
GET    /api/simcard/inventory-alert/page          # 分页获取告警配置
POST   /api/simcard/inventory-alert              # 创建新的告警配置
PUT    /api/simcard/inventory-alert/{id}         # 更新告警配置
DELETE /api/simcard/inventory-alert/{id}         # 删除告警配置
GET    /api/simcard/inventory-alert/{id}         # 获取告警配置详情
```

### 告警日志管理
```
GET    /api/simcard/inventory-alert-log/page     # 分页获取告警日志
GET    /api/simcard/inventory-alert-log/{id}     # 获取告警日志详情
PUT    /api/simcard/inventory-alert-log/{id}/notify-status/{status}  # 更新通知状态
```

### 手动告警操作
```
POST   /api/simcard/alert/trigger-check          # 手动触发告警检查
POST   /api/simcard/alert/trigger-notification   # 手动触发通知处理
POST   /api/simcard/alert/process-pending        # 仅处理待发送通知
POST   /api/simcard/alert/retry-failed           # 仅重试失败通知
POST   /api/simcard/alert/check-inventory        # 仅检查库存并生成告警
GET    /api/simcard/alert/status                 # 获取告警系统状态
```

## 配置

### 数据库表

#### sim_card_inventory_alert（告警配置表）
```sql
CREATE TABLE sim_card_inventory_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spec_name VARCHAR(100) NOT NULL COMMENT 'SIM卡规格名称',
    low_threshold INT NOT NULL COMMENT '低库存阈值',
    high_threshold INT NOT NULL COMMENT '高库存阈值',
    notification_emails TEXT COMMENT '通知邮箱地址（逗号分隔）',
    notification_phones TEXT COMMENT '通知手机号码（逗号分隔）',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用告警',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### sim_card_inventory_alert_log（告警日志表）
```sql
CREATE TABLE sim_card_inventory_alert_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_id BIGINT NOT NULL COMMENT '告警配置ID',
    spec_name VARCHAR(100) NOT NULL COMMENT 'SIM卡规格名称',
    current_inventory INT NOT NULL COMMENT '当前库存数量',
    threshold_value INT NOT NULL COMMENT '触发的阈值',
    alert_type VARCHAR(20) NOT NULL COMMENT '告警类型：LOW或HIGH',
    alert_message TEXT NOT NULL COMMENT '告警消息内容',
    notification_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '通知状态：PENDING、SENT、FAILED',
    notification_error TEXT COMMENT '通知失败时的错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES sim_card_inventory_alert(id)
);
```

### 告警类型
- **Low Inventory Alert (Type 1)**: Triggered when inventory count is below threshold
- **High Inventory Alert (Type 2)**: Triggered when inventory count is above threshold

### Notification Status
- **Pending (0)**: Notification not yet sent
- **Sent (1)**: Notification successfully sent
- **Failed (2)**: Notification failed to send

### Alert Status
- **Inactive (0)**: Alert configuration is disabled
- **Active (1)**: Alert configuration is enabled

## Usage Examples

### 1. Create Alert Configuration
```json
{
  "alertName": "Low SIM Card Alert",
  "specId": 1,
  "alertType": 1,
  "threshold": 100,
  "notifyEmails": "admin@example.com,manager@example.com",
  "notifyPhones": "13800138000,13900139000",
  "isActive": 1,
  "remark": "Alert when SIM card count is below 100"
}
```

### 2. Manual Alert Check
```bash
curl -X POST http://localhost:8080/nsrs/api/simcard/alert/trigger-check
```

### 3. Process Pending Notifications
```bash
curl -X POST http://localhost:8080/nsrs/api/simcard/alert/process-pending
```

## Scheduled Task Configuration

The system uses Spring's `@Scheduled` annotation for automatic task execution:

- **Alert Check**: `@Scheduled(fixedRate = 30 * 60 * 1000)` - Every 30 minutes
- **Notification Processing**: `@Scheduled(fixedRate = 5 * 60 * 1000)` - Every 5 minutes
- **Retry Failed**: `@Scheduled(fixedRate = 60 * 60 * 1000)` - Every hour

## Constants and Configuration

All system constants are defined in `AlertConstant.java`:

```java
// Alert Types
ALERT_TYPE_LOW_INVENTORY = 1
ALERT_TYPE_HIGH_INVENTORY = 2

// Alert Status
ALERT_STATUS_INACTIVE = 0
ALERT_STATUS_ACTIVE = 1

// Notification Status
NOTIFY_STATUS_PENDING = 0
NOTIFY_STATUS_SENT = 1
NOTIFY_STATUS_FAILED = 2
```

## Logging

The system uses SLF4J for logging with English log messages:

- Alert check start/completion
- Alert generation events
- Notification sending status
- Error handling and troubleshooting

## Error Handling

- All exceptions are properly caught and logged
- Failed notifications are marked with appropriate status
- Retry mechanism for failed notifications
- Graceful degradation when external services are unavailable

## Integration Notes

### Email Service Integration
To enable email notifications, implement the email sending logic in `SimCardAlertNotificationServiceImpl.sendEmailNotification()`:

```java
// TODO: Implement actual email sending logic
// Example: emailService.sendEmail(emailArray, subject, content);
```

### SMS Service Integration
To enable SMS notifications, implement the SMS sending logic in `SimCardAlertNotificationServiceImpl.sendSmsNotification()`:

```java
// TODO: Implement actual SMS sending logic
// Example: smsService.sendSms(phoneArray, content);
```

## Monitoring and Maintenance

1. **Monitor Alert Logs**: Regularly check alert logs for system health
2. **Review Failed Notifications**: Investigate and resolve failed notification issues
3. **Adjust Thresholds**: Update alert thresholds based on business requirements
4. **Performance Monitoring**: Monitor scheduled task execution times
5. **Database Maintenance**: Archive old alert logs as needed

## Security Considerations

- All API endpoints should be secured with appropriate authentication
- Sensitive notification data (emails, phone numbers) should be encrypted
- Access to alert configuration should be restricted to authorized users
- Audit logging for alert configuration changes

## Troubleshooting

### Common Issues

1. **Alerts Not Generated**
   - Check if alert configurations are active
   - Verify scheduled tasks are running
   - Check inventory data availability

2. **Notifications Not Sent**
   - Verify email/SMS service configuration
   - Check notification status in alert logs
   - Review error logs for service issues

3. **Performance Issues**
   - Monitor scheduled task execution times
   - Optimize database queries if needed
   - Consider adjusting task frequencies

### Debug Commands

```bash
# Check alert system status
curl http://localhost:8080/nsrs/api/simcard/alert/status

# Manual alert check
curl -X POST http://localhost:8080/nsrs/api/simcard/alert/trigger-check

# Process pending notifications
curl -X POST http://localhost:8080/nsrs/api/simcard/alert/process-pending
```