# SIM卡生命周期管理详解

## 概述

SIM卡（Subscriber Identity Module）生命周期管理是电信CRM系统的核心业务之一，涵盖了从SIM卡制造、分发、激活、使用到回收的完整流程。本文档详细阐述NSRS号卡资源管理系统中SIM卡生命周期的各个阶段及其管理策略。

## SIM卡生命周期阶段

### 1. 制造阶段（Manufacturing）
- **卡片制造**: 物理SIM卡的生产制造
- **密钥注入**: Ki密钥和算法的安全注入
- **个人化**: IMSI、ICCID等标识的写入
- **质量检测**: 卡片功能和安全性测试

### 2. 库存阶段（Inventory）
- **入库管理**: SIM卡批次入库和登记
- **库存分配**: 按渠道和区域分配库存
- **库存监控**: 实时监控库存水平
- **库存盘点**: 定期库存盘点和对账

### 3. 分发阶段（Distribution）
- **渠道分发**: 向各销售渠道分发SIM卡
- **物流跟踪**: 跟踪SIM卡的物流状态
- **签收确认**: 渠道签收确认
- **库存调拨**: 渠道间库存调拨

### 4. 销售阶段（Sales）
- **零售销售**: 向最终用户销售
- **实名登记**: 用户实名信息登记
- **套餐选择**: 用户选择资费套餐
- **销售记录**: 记录销售信息

### 5. 激活阶段（Activation）
- **首次开机**: 用户首次插卡开机
- **网络注册**: 向网络注册用户身份
- **服务开通**: 开通语音、数据等服务
- **计费激活**: 激活计费系统

### 6. 使用阶段（Active）
- **正常使用**: 用户正常使用各种服务
- **服务变更**: 套餐变更、增值服务等
- **状态监控**: 监控SIM卡使用状态
- **异常处理**: 处理异常使用情况

### 7. 暂停阶段（Suspended）
- **主动暂停**: 用户主动申请暂停服务
- **被动暂停**: 因欠费等原因被暂停
- **暂停期管理**: 暂停期间的状态管理
- **恢复服务**: 暂停后的服务恢复

### 8. 注销阶段（Deactivated）
- **用户注销**: 用户主动注销
- **系统注销**: 系统自动注销
- **数据保留**: 注销后数据保留策略
- **资源回收**: 号码等资源的回收

### 9. 回收阶段（Recycled）
- **物理回收**: 物理SIM卡的回收
- **数据清除**: 清除卡内敏感数据
- **资源重用**: 号码资源的重新分配
- **环保处理**: 废旧SIM卡的环保处理

## 数据模型设计

### SIM卡核心实体
```java
/**
 * SIM卡实体
 */
@Entity
@Table(name = "sim_card")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ICCID - SIM卡唯一标识
     */
    @Column(name = "iccid", unique = true, nullable = false, length = 20)
    private String iccid;
    
    /**
     * IMSI - 国际移动用户识别码
     */
    @Column(name = "imsi", unique = true, length = 15)
    private String imsi;
    
    /**
     * MSISDN - 手机号码
     */
    @Column(name = "msisdn", length = 15)
    private String msisdn;
    
    /**
     * Ki密钥（加密存储）
     */
    @Column(name = "ki_encrypted", length = 64)
    private String kiEncrypted;
    
    /**
     * SIM卡类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sim_type")
    private SimType simType;
    
    /**
     * SIM卡状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SimStatus status;
    
    /**
     * 生命周期阶段
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_stage")
    private LifecycleStage lifecycleStage;
    
    /**
     * 制造批次
     */
    @Column(name = "batch_number", length = 32)
    private String batchNumber;
    
    /**
     * 制造日期
     */
    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;
    
    /**
     * 激活日期
     */
    @Column(name = "activation_date")
    private LocalDateTime activationDate;
    
    /**
     * 注销日期
     */
    @Column(name = "deactivation_date")
    private LocalDateTime deactivationDate;
    
    /**
     * 当前用户ID
     */
    @Column(name = "current_user_id")
    private Long currentUserId;
    
    /**
     * 当前渠道ID
     */
    @Column(name = "current_channel_id")
    private Long currentChannelId;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    @Column(name = "remarks", length = 500)
    private String remarks;
}
```

### 生命周期状态枚举
```java
/**
 * SIM卡生命周期阶段枚举
 */
public enum LifecycleStage {
    MANUFACTURING("制造中", "SIM卡正在制造过程中"),
    INVENTORY("库存中", "SIM卡在库存中等待分发"),
    DISTRIBUTION("分发中", "SIM卡正在分发给渠道"),
    SALES("销售中", "SIM卡在渠道等待销售"),
    ACTIVATION("激活中", "SIM卡正在激活过程中"),
    ACTIVE("使用中", "SIM卡正常使用中"),
    SUSPENDED("暂停中", "SIM卡服务已暂停"),
    DEACTIVATED("已注销", "SIM卡已注销"),
    RECYCLED("已回收", "SIM卡已回收处理");
    
    private final String description;
    private final String detail;
    
    LifecycleStage(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDetail() {
        return detail;
    }
}

/**
 * SIM卡状态枚举
 */
public enum SimStatus {
    NORMAL("正常"),
    LOCKED("锁定"),
    DAMAGED("损坏"),
    LOST("丢失"),
    STOLEN("被盗"),
    EXPIRED("过期");
    
    private final String description;
    
    SimStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * SIM卡类型枚举
 */
public enum SimType {
    STANDARD("标准SIM", "2FF"),
    MICRO("Micro SIM", "3FF"),
    NANO("Nano SIM", "4FF"),
    ESIM("eSIM", "嵌入式SIM"),
    MULTI_CUT("三合一", "可切割多种尺寸");
    
    private final String description;
    private final String specification;
    
    SimType(String description, String specification) {
        this.description = description;
        this.specification = specification;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getSpecification() {
        return specification;
    }
}
```

## 生命周期管理服务

### 核心生命周期管理服务
```java
/**
 * SIM卡生命周期管理服务
 */
@Service
@Slf4j
public class SimLifecycleService {
    
    @Autowired
    private SimCardRepository simCardRepository;
    
    @Autowired
    private LifecycleEventRepository lifecycleEventRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * 创建SIM卡（制造阶段）
     */
    @Transactional
    public SimCard createSimCard(SimCardCreationRequest request) {
        log.info("Creating SIM card with ICCID: {}", request.getIccid());
        
        try {
            // 1. 验证ICCID唯一性
            validateIccidUniqueness(request.getIccid());
            
            // 2. 创建SIM卡实体
            SimCard simCard = SimCard.builder()
                .iccid(request.getIccid())
                .imsi(request.getImsi())
                .kiEncrypted(encryptKi(request.getKi()))
                .simType(request.getSimType())
                .status(SimStatus.NORMAL)
                .lifecycleStage(LifecycleStage.MANUFACTURING)
                .batchNumber(request.getBatchNumber())
                .manufactureDate(LocalDate.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
            
            // 3. 保存SIM卡
            simCard = simCardRepository.save(simCard);
            
            // 4. 记录生命周期事件
            recordLifecycleEvent(simCard.getId(), LifecycleStage.MANUFACTURING, 
                "SIM card created in manufacturing stage");
            
            // 5. 记录审计日志
            auditService.recordSimCardCreation(simCard);
            
            log.info("SIM card created successfully: {}", request.getIccid());
            return simCard;
            
        } catch (Exception e) {
            log.error("Failed to create SIM card: {}", request.getIccid(), e);
            throw new SimCardCreationException("Failed to create SIM card", e);
        }
    }
    
    /**
     * 转移到库存阶段
     */
    @Transactional
    public void moveToInventory(Long simCardId, InventoryRequest request) {
        log.info("Moving SIM card to inventory: {}", simCardId);
        
        SimCard simCard = getSimCardById(simCardId);
        validateStageTransition(simCard.getLifecycleStage(), LifecycleStage.INVENTORY);
        
        // 更新生命周期阶段
        simCard.setLifecycleStage(LifecycleStage.INVENTORY);
        simCard.setUpdateTime(LocalDateTime.now());
        
        simCardRepository.save(simCard);
        
        // 记录生命周期事件
        recordLifecycleEvent(simCardId, LifecycleStage.INVENTORY, 
            "SIM card moved to inventory");
        
        // 更新库存系统
        updateInventorySystem(simCard, request);
        
        log.info("SIM card moved to inventory successfully: {}", simCardId);
    }
    
    /**
     * 分发到渠道
     */
    @Transactional
    public void distributeToChannel(Long simCardId, DistributionRequest request) {
        log.info("Distributing SIM card to channel: {} -> {}", 
            simCardId, request.getChannelId());
        
        SimCard simCard = getSimCardById(simCardId);
        validateStageTransition(simCard.getLifecycleStage(), LifecycleStage.DISTRIBUTION);
        
        // 更新生命周期阶段和渠道信息
        simCard.setLifecycleStage(LifecycleStage.DISTRIBUTION);
        simCard.setCurrentChannelId(request.getChannelId());
        simCard.setUpdateTime(LocalDateTime.now());
        
        simCardRepository.save(simCard);
        
        // 记录生命周期事件
        recordLifecycleEvent(simCardId, LifecycleStage.DISTRIBUTION, 
            "SIM card distributed to channel: " + request.getChannelId());
        
        // 通知渠道
        notificationService.notifyChannelDistribution(request.getChannelId(), simCard);
        
        log.info("SIM card distributed successfully: {}", simCardId);
    }
    
    /**
     * 销售给用户
     */
    @Transactional
    public void sellToUser(Long simCardId, SalesRequest request) {
        log.info("Selling SIM card to user: {} -> {}", 
            simCardId, request.getUserId());
        
        SimCard simCard = getSimCardById(simCardId);
        validateStageTransition(simCard.getLifecycleStage(), LifecycleStage.SALES);
        
        // 更新生命周期阶段和用户信息
        simCard.setLifecycleStage(LifecycleStage.SALES);
        simCard.setCurrentUserId(request.getUserId());
        simCard.setMsisdn(request.getMsisdn());
        simCard.setUpdateTime(LocalDateTime.now());
        
        simCardRepository.save(simCard);
        
        // 记录生命周期事件
        recordLifecycleEvent(simCardId, LifecycleStage.SALES, 
            "SIM card sold to user: " + request.getUserId());
        
        // 创建用户SIM卡绑定关系
        createUserSimBinding(simCard, request);
        
        log.info("SIM card sold successfully: {}", simCardId);
    }
    
    /**
     * 激活SIM卡
     */
    @Transactional
    public void activateSimCard(Long simCardId, ActivationRequest request) {
        log.info("Activating SIM card: {}", simCardId);
        
        SimCard simCard = getSimCardById(simCardId);
        validateStageTransition(simCard.getLifecycleStage(), LifecycleStage.ACTIVATION);
        
        try {
            // 1. 更新生命周期阶段
            simCard.setLifecycleStage(LifecycleStage.ACTIVATION);
            simCard.setActivationDate(LocalDateTime.now());
            simCard.setUpdateTime(LocalDateTime.now());
            
            simCardRepository.save(simCard);
            
            // 2. 执行网络激活
            executeNetworkActivation(simCard, request);
            
            // 3. 开通服务
            activateServices(simCard, request);
            
            // 4. 转移到使用阶段
            simCard.setLifecycleStage(LifecycleStage.ACTIVE);
            simCardRepository.save(simCard);
            
            // 5. 记录生命周期事件
            recordLifecycleEvent(simCardId, LifecycleStage.ACTIVE, 
                "SIM card activated successfully");
            
            // 6. 通知相关系统
            notificationService.notifySimCardActivation(simCard);
            
            log.info("SIM card activated successfully: {}", simCardId);
            
        } catch (Exception e) {
            log.error("Failed to activate SIM card: {}", simCardId, e);
            
            // 激活失败，回滚状态
            simCard.setLifecycleStage(LifecycleStage.SALES);
            simCard.setActivationDate(null);
            simCardRepository.save(simCard);
            
            throw new SimCardActivationException("Failed to activate SIM card", e);
        }
    }
    
    /**
     * 暂停SIM卡服务
     */
    @Transactional
    public void suspendSimCard(Long simCardId, SuspensionRequest request) {
        log.info("Suspending SIM card: {}, reason: {}", 
            simCardId, request.getReason());
        
        SimCard simCard = getSimCardById(simCardId);
        validateStageTransition(simCard.getLifecycleStage(), LifecycleStage.SUSPENDED);
        
        // 更新生命周期阶段
        simCard.setLifecycleStage(LifecycleStage.SUSPENDED);
        simCard.setUpdateTime(LocalDateTime.now());
        
        simCardRepository.save(simCard);
        
        // 执行网络暂停
        executeNetworkSuspension(simCard, request);
        
        // 记录生命周期事件
        recordLifecycleEvent(simCardId, LifecycleStage.SUSPENDED, 
            "SIM card suspended: " + request.getReason());
        
        // 通知用户
        notificationService.notifySimCardSuspension(simCard, request.getReason());
        
        log.info("SIM card suspended successfully: {}", simCardId);
    }
    
    /**
     * 恢复SIM卡服务
     */
    @Transactional
    public void resumeSimCard(Long simCardId, ResumptionRequest request) {
        log.info("Resuming SIM card: {}", simCardId);
        
        SimCard simCard = getSimCardById(simCardId);
        
        if (simCard.getLifecycleStage() != LifecycleStage.SUSPENDED) {
            throw new IllegalStateException("SIM card is not in suspended state");
        }
        
        // 更新生命周期阶段
        simCard.setLifecycleStage(LifecycleStage.ACTIVE);
        simCard.setUpdateTime(LocalDateTime.now());
        
        simCardRepository.save(simCard);
        
        // 执行网络恢复
        executeNetworkResumption(simCard, request);
        
        // 记录生命周期事件
        recordLifecycleEvent(simCardId, LifecycleStage.ACTIVE, 
            "SIM card resumed from suspension");
        
        // 通知用户
        notificationService.notifySimCardResumption(simCard);
        
        log.info("SIM card resumed successfully: {}", simCardId);
    }
    
    /**
     * 注销SIM卡
     */
    @Transactional
    public void deactivateSimCard(Long simCardId, DeactivationRequest request) {
        log.info("Deactivating SIM card: {}, reason: {}", 
            simCardId, request.getReason());
        
        SimCard simCard = getSimCardById(simCardId);
        validateStageTransition(simCard.getLifecycleStage(), LifecycleStage.DEACTIVATED);
        
        // 更新生命周期阶段
        simCard.setLifecycleStage(LifecycleStage.DEACTIVATED);
        simCard.setDeactivationDate(LocalDateTime.now());
        simCard.setUpdateTime(LocalDateTime.now());
        
        simCardRepository.save(simCard);
        
        // 执行网络注销
        executeNetworkDeactivation(simCard, request);
        
        // 回收号码资源
        recycleNumberResources(simCard);
        
        // 记录生命周期事件
        recordLifecycleEvent(simCardId, LifecycleStage.DEACTIVATED, 
            "SIM card deactivated: " + request.getReason());
        
        // 通知相关系统
        notificationService.notifySimCardDeactivation(simCard);
        
        log.info("SIM card deactivated successfully: {}", simCardId);
    }
    
    /**
     * 回收SIM卡
     */
    @Transactional
    public void recycleSimCard(Long simCardId, RecyclingRequest request) {
        log.info("Recycling SIM card: {}", simCardId);
        
        SimCard simCard = getSimCardById(simCardId);
        validateStageTransition(simCard.getLifecycleStage(), LifecycleStage.RECYCLED);
        
        // 更新生命周期阶段
        simCard.setLifecycleStage(LifecycleStage.RECYCLED);
        simCard.setUpdateTime(LocalDateTime.now());
        
        simCardRepository.save(simCard);
        
        // 清除敏感数据
        clearSensitiveData(simCard);
        
        // 记录生命周期事件
        recordLifecycleEvent(simCardId, LifecycleStage.RECYCLED, 
            "SIM card recycled");
        
        // 环保处理记录
        recordEnvironmentalProcessing(simCard, request);
        
        log.info("SIM card recycled successfully: {}", simCardId);
    }
    
    // 辅助方法
    private SimCard getSimCardById(Long simCardId) {
        return simCardRepository.findById(simCardId)
            .orElseThrow(() -> new SimCardNotFoundException("SIM card not found: " + simCardId));
    }
    
    private void validateIccidUniqueness(String iccid) {
        if (simCardRepository.existsByIccid(iccid)) {
            throw new DuplicateIccidException("ICCID already exists: " + iccid);
        }
    }
    
    private void validateStageTransition(LifecycleStage currentStage, LifecycleStage targetStage) {
        if (!isValidTransition(currentStage, targetStage)) {
            throw new InvalidStageTransitionException(
                String.format("Invalid stage transition from %s to %s", 
                    currentStage, targetStage));
        }
    }
    
    private boolean isValidTransition(LifecycleStage from, LifecycleStage to) {
        // 定义有效的状态转换规则
        switch (from) {
            case MANUFACTURING:
                return to == LifecycleStage.INVENTORY;
            case INVENTORY:
                return to == LifecycleStage.DISTRIBUTION;
            case DISTRIBUTION:
                return to == LifecycleStage.SALES;
            case SALES:
                return to == LifecycleStage.ACTIVATION;
            case ACTIVATION:
                return to == LifecycleStage.ACTIVE;
            case ACTIVE:
                return to == LifecycleStage.SUSPENDED || to == LifecycleStage.DEACTIVATED;
            case SUSPENDED:
                return to == LifecycleStage.ACTIVE || to == LifecycleStage.DEACTIVATED;
            case DEACTIVATED:
                return to == LifecycleStage.RECYCLED;
            case RECYCLED:
                return false; // 回收后不能再转换
            default:
                return false;
        }
    }
    
    private void recordLifecycleEvent(Long simCardId, LifecycleStage stage, String description) {
        LifecycleEvent event = LifecycleEvent.builder()
            .simCardId(simCardId)
            .stage(stage)
            .description(description)
            .eventTime(LocalDateTime.now())
            .build();
        
        lifecycleEventRepository.save(event);
    }
    
    private String encryptKi(String ki) {
        // 实现Ki密钥的加密存储
        // 这里应该使用强加密算法
        return "encrypted_" + ki; // 示例实现
    }
    
    private void updateInventorySystem(SimCard simCard, InventoryRequest request) {
        // 更新库存系统
        log.debug("Updating inventory system for SIM card: {}", simCard.getIccid());
    }
    
    private void createUserSimBinding(SimCard simCard, SalesRequest request) {
        // 创建用户与SIM卡的绑定关系
        log.debug("Creating user-SIM binding: {} -> {}", 
            request.getUserId(), simCard.getIccid());
    }
    
    private void executeNetworkActivation(SimCard simCard, ActivationRequest request) {
        // 执行网络层面的激活操作
        log.debug("Executing network activation for SIM card: {}", simCard.getIccid());
    }
    
    private void activateServices(SimCard simCard, ActivationRequest request) {
        // 开通各种服务
        log.debug("Activating services for SIM card: {}", simCard.getIccid());
    }
    
    private void executeNetworkSuspension(SimCard simCard, SuspensionRequest request) {
        // 执行网络层面的暂停操作
        log.debug("Executing network suspension for SIM card: {}", simCard.getIccid());
    }
    
    private void executeNetworkResumption(SimCard simCard, ResumptionRequest request) {
        // 执行网络层面的恢复操作
        log.debug("Executing network resumption for SIM card: {}", simCard.getIccid());
    }
    
    private void executeNetworkDeactivation(SimCard simCard, DeactivationRequest request) {
        // 执行网络层面的注销操作
        log.debug("Executing network deactivation for SIM card: {}", simCard.getIccid());
    }
    
    private void recycleNumberResources(SimCard simCard) {
        // 回收号码资源
        if (StringUtils.isNotEmpty(simCard.getMsisdn())) {
            log.debug("Recycling number resource: {}", simCard.getMsisdn());
            // 调用号码回收服务
        }
    }
    
    private void clearSensitiveData(SimCard simCard) {
        // 清除敏感数据
        simCard.setKiEncrypted(null);
        simCard.setCurrentUserId(null);
        simCardRepository.save(simCard);
    }
    
    private void recordEnvironmentalProcessing(SimCard simCard, RecyclingRequest request) {
        // 记录环保处理信息
        log.debug("Recording environmental processing for SIM card: {}", simCard.getIccid());
    }
}
```

## 生命周期事件记录

### 生命周期事件实体
```java
/**
 * 生命周期事件实体
 */
@Entity
@Table(name = "lifecycle_event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LifecycleEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * SIM卡ID
     */
    @Column(name = "sim_card_id", nullable = false)
    private Long simCardId;
    
    /**
     * 生命周期阶段
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    private LifecycleStage stage;
    
    /**
     * 事件描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 事件时间
     */
    @Column(name = "event_time")
    private LocalDateTime eventTime;
    
    /**
     * 操作人员
     */
    @Column(name = "operator", length = 100)
    private String operator;
    
    /**
     * 操作系统
     */
    @Column(name = "system_source", length = 100)
    private String systemSource;
    
    /**
     * 扩展属性（JSON格式）
     */
    @Column(name = "extended_attributes", columnDefinition = "TEXT")
    private String extendedAttributes;
}
```

## 生命周期监控和报告

### 生命周期监控服务
```java
/**
 * SIM卡生命周期监控服务
 */
@Service
@Slf4j
public class LifecycleMonitoringService {
    
    @Autowired
    private SimCardRepository simCardRepository;
    
    @Autowired
    private LifecycleEventRepository lifecycleEventRepository;
    
    /**
     * 获取生命周期统计报告
     */
    public LifecycleStatisticsReport getLifecycleStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("Generating lifecycle statistics report from {} to {}", startDate, endDate);
        
        // 统计各阶段SIM卡数量
        Map<LifecycleStage, Long> stageDistribution = simCardRepository
            .findAll()
            .stream()
            .collect(Collectors.groupingBy(
                SimCard::getLifecycleStage,
                Collectors.counting()
            ));
        
        // 统计激活率
        long totalSold = simCardRepository.countByLifecycleStageIn(
            Arrays.asList(LifecycleStage.SALES, LifecycleStage.ACTIVATION, 
                         LifecycleStage.ACTIVE, LifecycleStage.SUSPENDED, 
                         LifecycleStage.DEACTIVATED));
        
        long totalActivated = simCardRepository.countByLifecycleStageIn(
            Arrays.asList(LifecycleStage.ACTIVE, LifecycleStage.SUSPENDED, 
                         LifecycleStage.DEACTIVATED));
        
        double activationRate = totalSold > 0 ? (double) totalActivated / totalSold * 100 : 0;
        
        // 统计平均激活时间
        Double avgActivationDays = simCardRepository.calculateAverageActivationDays();
        
        return LifecycleStatisticsReport.builder()
            .reportDate(LocalDate.now())
            .startDate(startDate)
            .endDate(endDate)
            .stageDistribution(stageDistribution)
            .activationRate(activationRate)
            .averageActivationDays(avgActivationDays != null ? avgActivationDays : 0.0)
            .totalSimCards(simCardRepository.count())
            .build();
    }
    
    /**
     * 监控异常SIM卡
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void monitorAbnormalSimCards() {
        log.debug("Monitoring abnormal SIM cards");
        
        // 1. 检查长时间未激活的SIM卡
        checkLongTimeUnactivatedCards();
        
        // 2. 检查异常状态的SIM卡
        checkAbnormalStatusCards();
        
        // 3. 检查生命周期异常的SIM卡
        checkLifecycleAnomalies();
    }
    
    private void checkLongTimeUnactivatedCards() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        
        List<SimCard> unactivatedCards = simCardRepository
            .findByLifecycleStageAndCreateTimeBefore(
                LifecycleStage.SALES, threshold);
        
        if (!unactivatedCards.isEmpty()) {
            log.warn("Found {} SIM cards not activated for more than 30 days", 
                unactivatedCards.size());
            
            // 发送告警
            sendUnactivatedCardsAlert(unactivatedCards);
        }
    }
    
    private void checkAbnormalStatusCards() {
        List<SimCard> abnormalCards = simCardRepository
            .findByStatusIn(Arrays.asList(SimStatus.DAMAGED, SimStatus.LOST, SimStatus.STOLEN));
        
        if (!abnormalCards.isEmpty()) {
            log.info("Found {} SIM cards with abnormal status", abnormalCards.size());
            
            // 处理异常状态的SIM卡
            processAbnormalStatusCards(abnormalCards);
        }
    }
    
    private void checkLifecycleAnomalies() {
        // 检查生命周期状态异常的SIM卡
        // 例如：激活时间过长、状态转换异常等
        
        List<SimCard> activatingCards = simCardRepository
            .findByLifecycleStageAndUpdateTimeBefore(
                LifecycleStage.ACTIVATION, 
                LocalDateTime.now().minusHours(1));
        
        if (!activatingCards.isEmpty()) {
            log.warn("Found {} SIM cards stuck in activation stage", 
                activatingCards.size());
            
            // 处理激活异常的SIM卡
            processActivationAnomalies(activatingCards);
        }
    }
    
    private void sendUnactivatedCardsAlert(List<SimCard> unactivatedCards) {
        // 发送未激活SIM卡告警
        log.debug("Sending unactivated cards alert for {} cards", unactivatedCards.size());
    }
    
    private void processAbnormalStatusCards(List<SimCard> abnormalCards) {
        // 处理异常状态的SIM卡
        for (SimCard card : abnormalCards) {
            log.debug("Processing abnormal status card: {} - {}", 
                card.getIccid(), card.getStatus());
        }
    }
    
    private void processActivationAnomalies(List<SimCard> activatingCards) {
        // 处理激活异常的SIM卡
        for (SimCard card : activatingCards) {
            log.debug("Processing activation anomaly for card: {}", card.getIccid());
            
            // 可以尝试重新激活或标记为异常
        }
    }
}
```

## 最佳实践总结

### 1. 状态管理
- **状态一致性**: 确保SIM卡状态在各个系统间的一致性
- **状态转换控制**: 严格控制生命周期状态的转换规则
- **异常状态处理**: 建立完善的异常状态处理机制
- **状态回滚**: 支持在异常情况下的状态回滚

### 2. 数据安全
- **敏感数据加密**: 对Ki密钥等敏感数据进行加密存储
- **访问控制**: 严格控制SIM卡数据的访问权限
- **数据脱敏**: 在日志和报告中对敏感数据进行脱敏
- **数据清除**: 在回收阶段彻底清除敏感数据

### 3. 监控告警
- **实时监控**: 实时监控SIM卡的生命周期状态
- **异常检测**: 自动检测生命周期异常情况
- **性能监控**: 监控生命周期操作的性能指标
- **业务监控**: 监控激活率、注销率等业务指标

### 4. 流程优化
- **自动化流程**: 尽可能自动化生命周期管理流程
- **批量处理**: 支持SIM卡的批量生命周期操作
- **流程可视化**: 提供生命周期流程的可视化展示
- **流程审计**: 完整记录生命周期操作的审计轨迹

### 5. 性能优化
- **索引优化**: 在关键字段上建立合适的索引
- **分库分表**: 根据业务需要进行分库分表
- **缓存策略**: 对热点数据进行缓存
- **异步处理**: 对耗时操作采用异步处理

通过以上SIM卡生命周期管理的详细实现，可以确保NSRS号卡资源管理系统在SIM卡全生命周期管理方面达到电信行业的专业水准，满足复杂的业务需求和监管要求。