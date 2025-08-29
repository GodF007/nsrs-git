# IMSI管理详解

## 概述

IMSI（International Mobile Subscriber Identity，国际移动用户识别码）是全球唯一标识移动用户的15位数字，是电信网络中最重要的用户标识之一。本文档详细介绍IMSI的结构、生命周期管理、业务应用和技术实现。

## IMSI结构解析

### 基本结构
```
IMSI = MCC + MNC + MSIN
总长度：15位数字
```

### 各部分详解

#### 1. MCC（Mobile Country Code）- 移动国家码
- **长度**: 3位数字
- **作用**: 标识移动用户所属的国家或地区
- **中国MCC**: 460
- **示例**: 
  - 460 - 中国
  - 310 - 美国
  - 440 - 日本

#### 2. MNC（Mobile Network Code）- 移动网络码
- **长度**: 2-3位数字
- **作用**: 标识移动用户所属的运营商
- **中国运营商MNC**:
  - 00, 02, 07, 08 - 中国移动
  - 01, 06, 09 - 中国联通
  - 03, 05, 11 - 中国电信

#### 3. MSIN（Mobile Subscriber Identification Number）- 移动用户识别号
- **长度**: 9-10位数字（总长度15位）
- **作用**: 在特定运营商网络内唯一标识用户
- **结构**: 通常包含HLR标识和用户序列号

### IMSI示例分析
```
IMSI: 460001234567890
├── MCC: 460 (中国)
├── MNC: 00 (中国移动)
└── MSIN: 1234567890 (用户标识)
```

## IMSI生命周期管理

### 1. IMSI分配阶段

#### 分配策略
```java
/**
 * IMSI分配服务
 */
@Service
@Slf4j
public class ImsiAllocationService {
    
    @Autowired
    private ImsiPoolRepository imsiPoolRepository;
    
    @Autowired
    private ImsiAllocationRepository allocationRepository;
    
    /**
     * 分配IMSI
     */
    @Transactional
    public String allocateImsi(ImsiAllocationRequest request) {
        log.info("Starting IMSI allocation for operator: {}", request.getOperatorCode());
        
        try {
            // 1. 验证分配请求
            validateAllocationRequest(request);
            
            // 2. 从IMSI池中获取可用IMSI
            String imsi = getAvailableImsi(request.getOperatorCode(), request.getRegion());
            
            // 3. 记录分配信息
            recordAllocation(imsi, request);
            
            // 4. 更新IMSI状态
            updateImsiStatus(imsi, ImsiStatus.ALLOCATED);
            
            log.info("IMSI allocated successfully: {}", maskImsi(imsi));
            return imsi;
            
        } catch (Exception e) {
            log.error("IMSI allocation failed", e);
            throw new ImsiAllocationException("IMSI allocation failed", e);
        }
    }
    
    /**
     * 验证分配请求
     */
    private void validateAllocationRequest(ImsiAllocationRequest request) {
        if (StringUtils.isEmpty(request.getOperatorCode())) {
            throw new IllegalArgumentException("Operator code is required");
        }
        
        if (!isValidOperatorCode(request.getOperatorCode())) {
            throw new IllegalArgumentException("Invalid operator code: " + request.getOperatorCode());
        }
        
        if (StringUtils.isEmpty(request.getRegion())) {
            throw new IllegalArgumentException("Region is required");
        }
    }
    
    /**
     * 获取可用IMSI
     */
    private String getAvailableImsi(String operatorCode, String region) {
        // 构建IMSI前缀
        String imsiPrefix = buildImsiPrefix(operatorCode, region);
        
        // 从IMSI池中查找可用IMSI
        Optional<ImsiPool> availableImsi = imsiPoolRepository
            .findFirstByPrefixAndStatusOrderByCreatedTimeAsc(imsiPrefix, ImsiStatus.AVAILABLE);
        
        if (availableImsi.isPresent()) {
            return availableImsi.get().getImsi();
        }
        
        // 如果池中没有可用IMSI，生成新的IMSI
        return generateNewImsi(imsiPrefix);
    }
    
    /**
     * 构建IMSI前缀
     */
    private String buildImsiPrefix(String operatorCode, String region) {
        String mcc = "460"; // 中国
        String mnc = getMncByOperatorCode(operatorCode);
        String hlrCode = getHlrCodeByRegion(region);
        
        return mcc + mnc + hlrCode;
    }
    
    /**
     * 生成新IMSI
     */
    private String generateNewImsi(String prefix) {
        // 获取下一个序列号
        long sequence = getNextImsiSequence(prefix);
        
        // 构建完整IMSI
        String imsi = prefix + String.format("%06d", sequence);
        
        // 验证IMSI唯一性
        if (imsiExists(imsi)) {
            throw new ImsiGenerationException("Generated IMSI already exists: " + maskImsi(imsi));
        }
        
        // 保存到IMSI池
        saveToImsiPool(imsi, prefix);
        
        return imsi;
    }
    
    /**
     * 记录分配信息
     */
    private void recordAllocation(String imsi, ImsiAllocationRequest request) {
        ImsiAllocation allocation = ImsiAllocation.builder()
            .imsi(imsi)
            .operatorCode(request.getOperatorCode())
            .region(request.getRegion())
            .allocatedBy(request.getAllocatedBy())
            .allocatedTime(LocalDateTime.now())
            .purpose(request.getPurpose())
            .build();
        
        allocationRepository.save(allocation);
    }
    
    /**
     * 更新IMSI状态
     */
    private void updateImsiStatus(String imsi, ImsiStatus status) {
        imsiPoolRepository.updateStatusByImsi(imsi, status, LocalDateTime.now());
    }
    
    /**
     * IMSI脱敏显示
     */
    private String maskImsi(String imsi) {
        if (StringUtils.isEmpty(imsi) || imsi.length() < 10) {
            return imsi;
        }
        return imsi.substring(0, 6) + "*****" + imsi.substring(11);
    }
    
    // 其他辅助方法...
    private String getMncByOperatorCode(String operatorCode) {
        Map<String, String> operatorMncMap = Map.of(
            "CMCC", "00",  // 中国移动
            "CUCC", "01",  // 中国联通
            "CTCC", "03"   // 中国电信
        );
        return operatorMncMap.getOrDefault(operatorCode, "00");
    }
    
    private String getHlrCodeByRegion(String region) {
        // 根据地区返回HLR编码
        Map<String, String> regionHlrMap = Map.of(
            "BJ", "01",    // 北京
            "SH", "02",    // 上海
            "GZ", "03",    // 广州
            "SZ", "04"     // 深圳
        );
        return regionHlrMap.getOrDefault(region, "01");
    }
    
    private boolean isValidOperatorCode(String operatorCode) {
        return Set.of("CMCC", "CUCC", "CTCC").contains(operatorCode);
    }
    
    private long getNextImsiSequence(String prefix) {
        // 从数据库获取下一个序列号
        return imsiPoolRepository.getNextSequenceByPrefix(prefix);
    }
    
    private boolean imsiExists(String imsi) {
        return imsiPoolRepository.existsByImsi(imsi);
    }
    
    private void saveToImsiPool(String imsi, String prefix) {
        ImsiPool pool = ImsiPool.builder()
            .imsi(imsi)
            .prefix(prefix)
            .status(ImsiStatus.AVAILABLE)
            .createdTime(LocalDateTime.now())
            .build();
        
        imsiPoolRepository.save(pool);
    }
}
```

### 2. IMSI激活阶段

#### 激活流程
```java
/**
 * IMSI激活服务
 */
@Service
@Slf4j
public class ImsiActivationService {
    
    @Autowired
    private SimCardRepository simCardRepository;
    
    @Autowired
    private HlrIntegrationService hlrIntegrationService;
    
    @Autowired
    private AucIntegrationService aucIntegrationService;
    
    /**
     * 激活IMSI
     */
    @Transactional
    public void activateImsi(ImsiActivationRequest request) {
        log.info("Starting IMSI activation: {}", maskImsi(request.getImsi()));
        
        try {
            // 1. 验证IMSI状态
            validateImsiForActivation(request.getImsi());
            
            // 2. 生成认证密钥
            AuthenticationKeys keys = generateAuthenticationKeys(request.getImsi());
            
            // 3. 写入HLR/HSS
            writeToHlr(request.getImsi(), keys, request.getServiceProfile());
            
            // 4. 写入AuC
            writeToAuc(request.getImsi(), keys);
            
            // 5. 更新SIM卡状态
            updateSimCardStatus(request.getImsi(), SimCardStatus.ACTIVE);
            
            // 6. 记录激活日志
            recordActivationLog(request);
            
            log.info("IMSI activation completed: {}", maskImsi(request.getImsi()));
            
        } catch (Exception e) {
            log.error("IMSI activation failed: {}", maskImsi(request.getImsi()), e);
            throw new ImsiActivationException("IMSI activation failed", e);
        }
    }
    
    /**
     * 验证IMSI激活条件
     */
    private void validateImsiForActivation(String imsi) {
        // 验证IMSI格式
        if (!isValidImsiFormat(imsi)) {
            throw new IllegalArgumentException("Invalid IMSI format: " + maskImsi(imsi));
        }
        
        // 查询SIM卡信息
        Optional<SimCard> simCard = simCardRepository.findByImsi(imsi);
        if (simCard.isEmpty()) {
            throw new SimCardNotFoundException("SIM card not found for IMSI: " + maskImsi(imsi));
        }
        
        // 验证SIM卡状态
        SimCardStatus currentStatus = simCard.get().getStatus();
        if (currentStatus != SimCardStatus.ALLOCATED && currentStatus != SimCardStatus.INACTIVE) {
            throw new IllegalStateException(
                String.format("SIM card status %s is not valid for activation: %s", 
                    currentStatus, maskImsi(imsi)));
        }
    }
    
    /**
     * 生成认证密钥
     */
    private AuthenticationKeys generateAuthenticationKeys(String imsi) {
        // 生成Ki（认证密钥）
        String ki = generateKi();
        
        // 生成OPc（运营商变量）
        String opc = generateOpc(imsi, ki);
        
        // 生成ADM密钥
        String adm = generateAdmKey();
        
        return AuthenticationKeys.builder()
            .imsi(imsi)
            .ki(ki)
            .opc(opc)
            .adm(adm)
            .algorithm("Milenage")
            .createdTime(LocalDateTime.now())
            .build();
    }
    
    /**
     * 写入HLR/HSS
     */
    private void writeToHlr(String imsi, AuthenticationKeys keys, ServiceProfile profile) {
        HlrSubscriberData subscriberData = HlrSubscriberData.builder()
            .imsi(imsi)
            .ki(keys.getKi())
            .opc(keys.getOpc())
            .serviceProfile(profile)
            .subscriptionData(buildSubscriptionData(profile))
            .build();
        
        hlrIntegrationService.createSubscriber(subscriberData);
    }
    
    /**
     * 写入AuC
     */
    private void writeToAuc(String imsi, AuthenticationKeys keys) {
        AucSubscriberData aucData = AucSubscriberData.builder()
            .imsi(imsi)
            .ki(keys.getKi())
            .opc(keys.getOpc())
            .algorithm(keys.getAlgorithm())
            .build();
        
        aucIntegrationService.createSubscriber(aucData);
    }
    
    /**
     * 更新SIM卡状态
     */
    private void updateSimCardStatus(String imsi, SimCardStatus status) {
        simCardRepository.updateStatusByImsi(imsi, status, LocalDateTime.now());
    }
    
    /**
     * 记录激活日志
     */
    private void recordActivationLog(ImsiActivationRequest request) {
        ImsiActivationLog log = ImsiActivationLog.builder()
            .imsi(request.getImsi())
            .activatedBy(request.getActivatedBy())
            .activationTime(LocalDateTime.now())
            .serviceProfile(request.getServiceProfile())
            .result("SUCCESS")
            .build();
        
        // 保存激活日志
        // activationLogRepository.save(log);
    }
    
    // 辅助方法实现...
    private boolean isValidImsiFormat(String imsi) {
        return imsi != null && imsi.matches("\\d{15}") && imsi.startsWith("460");
    }
    
    private String generateKi() {
        // 生成128位随机密钥
        SecureRandom random = new SecureRandom();
        byte[] kiBytes = new byte[16];
        random.nextBytes(kiBytes);
        return bytesToHex(kiBytes);
    }
    
    private String generateOpc(String imsi, String ki) {
        // 根据运营商密钥OP和Ki生成OPc
        String op = getOperatorKey(); // 运营商密钥
        return calculateOpc(op, ki);
    }
    
    private String generateAdmKey() {
        // 生成ADM管理密钥
        SecureRandom random = new SecureRandom();
        byte[] admBytes = new byte[8];
        random.nextBytes(admBytes);
        return bytesToHex(admBytes);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    
    private String getOperatorKey() {
        // 返回运营商密钥（实际应用中应从安全存储中获取）
        return "11111111111111111111111111111111";
    }
    
    private String calculateOpc(String op, String ki) {
        // 实现OPc计算算法（简化版本）
        // 实际应用中应使用标准的Milenage算法
        return "22222222222222222222222222222222";
    }
    
    private SubscriptionData buildSubscriptionData(ServiceProfile profile) {
        return SubscriptionData.builder()
            .accessRestriction(profile.getAccessRestriction())
            .subscriberStatus("ACTIVE")
            .networkAccessMode("BOTH")
            .roamingRestriction(profile.getRoamingRestriction())
            .build();
    }
    
    private String maskImsi(String imsi) {
        if (StringUtils.isEmpty(imsi) || imsi.length() < 10) {
            return imsi;
        }
        return imsi.substring(0, 6) + "*****" + imsi.substring(11);
    }
}
```

### 3. IMSI使用阶段

#### 状态监控
```java
/**
 * IMSI状态监控服务
 */
@Service
@Slf4j
public class ImsiMonitoringService {
    
    @Autowired
    private ImsiUsageRepository usageRepository;
    
    @Autowired
    private NetworkEventRepository eventRepository;
    
    /**
     * 记录IMSI使用事件
     */
    public void recordUsageEvent(ImsiUsageEvent event) {
        try {
            // 验证事件数据
            validateUsageEvent(event);
            
            // 保存使用记录
            saveUsageRecord(event);
            
            // 更新统计信息
            updateUsageStatistics(event);
            
            // 检查异常使用模式
            checkAbnormalUsage(event);
            
        } catch (Exception e) {
            log.error("Failed to record IMSI usage event", e);
        }
    }
    
    /**
     * 获取IMSI使用统计
     */
    public ImsiUsageStatistics getUsageStatistics(String imsi, LocalDate startDate, LocalDate endDate) {
        List<ImsiUsageRecord> records = usageRepository
            .findByImsiAndDateBetween(imsi, startDate, endDate);
        
        return calculateStatistics(records);
    }
    
    /**
     * 检查IMSI状态
     */
    public ImsiStatusInfo checkImsiStatus(String imsi) {
        // 查询基本信息
        Optional<SimCard> simCard = simCardRepository.findByImsi(imsi);
        if (simCard.isEmpty()) {
            throw new SimCardNotFoundException("SIM card not found: " + maskImsi(imsi));
        }
        
        // 查询网络状态
        NetworkStatus networkStatus = queryNetworkStatus(imsi);
        
        // 查询最近使用记录
        List<ImsiUsageRecord> recentUsage = usageRepository
            .findTop10ByImsiOrderByEventTimeDesc(imsi);
        
        return ImsiStatusInfo.builder()
            .imsi(imsi)
            .simCardStatus(simCard.get().getStatus())
            .networkStatus(networkStatus)
            .lastUsageTime(getLastUsageTime(recentUsage))
            .usageCount24h(getUsageCount24h(imsi))
            .currentLocation(getCurrentLocation(imsi))
            .build();
    }
    
    /**
     * IMSI异常检测
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void detectAbnormalImsi() {
        log.info("Starting IMSI abnormal detection");
        
        try {
            // 检测高频使用IMSI
            detectHighFrequencyUsage();
            
            // 检测异地登录IMSI
            detectRemoteLogin();
            
            // 检测长时间未使用IMSI
            detectInactiveImsi();
            
            // 检测异常认证失败
            detectAuthenticationFailures();
            
        } catch (Exception e) {
            log.error("IMSI abnormal detection failed", e);
        }
    }
    
    private void validateUsageEvent(ImsiUsageEvent event) {
        if (StringUtils.isEmpty(event.getImsi())) {
            throw new IllegalArgumentException("IMSI is required");
        }
        
        if (event.getEventTime() == null) {
            throw new IllegalArgumentException("Event time is required");
        }
        
        if (StringUtils.isEmpty(event.getEventType())) {
            throw new IllegalArgumentException("Event type is required");
        }
    }
    
    private void saveUsageRecord(ImsiUsageEvent event) {
        ImsiUsageRecord record = ImsiUsageRecord.builder()
            .imsi(event.getImsi())
            .eventType(event.getEventType())
            .eventTime(event.getEventTime())
            .cellId(event.getCellId())
            .lac(event.getLac())
            .dataVolume(event.getDataVolume())
            .duration(event.getDuration())
            .build();
        
        usageRepository.save(record);
    }
    
    private void updateUsageStatistics(ImsiUsageEvent event) {
        // 更新日统计
        updateDailyStatistics(event);
        
        // 更新月统计
        updateMonthlyStatistics(event);
    }
    
    private void checkAbnormalUsage(ImsiUsageEvent event) {
        // 检查是否为异常使用模式
        if (isAbnormalUsagePattern(event)) {
            createAbnormalUsageAlert(event);
        }
    }
    
    private boolean isAbnormalUsagePattern(ImsiUsageEvent event) {
        // 检查高频使用
        if (isHighFrequencyUsage(event.getImsi())) {
            return true;
        }
        
        // 检查异地使用
        if (isRemoteUsage(event)) {
            return true;
        }
        
        // 检查大流量使用
        if (isHighVolumeUsage(event)) {
            return true;
        }
        
        return false;
    }
    
    // 其他辅助方法实现...
    private ImsiUsageStatistics calculateStatistics(List<ImsiUsageRecord> records) {
        // 计算使用统计信息
        return ImsiUsageStatistics.builder()
            .totalEvents(records.size())
            .totalDataVolume(records.stream().mapToLong(ImsiUsageRecord::getDataVolume).sum())
            .totalDuration(records.stream().mapToLong(ImsiUsageRecord::getDuration).sum())
            .uniqueCells(records.stream().map(ImsiUsageRecord::getCellId).distinct().count())
            .build();
    }
    
    private NetworkStatus queryNetworkStatus(String imsi) {
        // 查询网络状态（简化实现）
        return NetworkStatus.ONLINE;
    }
    
    private LocalDateTime getLastUsageTime(List<ImsiUsageRecord> records) {
        return records.isEmpty() ? null : records.get(0).getEventTime();
    }
    
    private long getUsageCount24h(String imsi) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return usageRepository.countByImsiAndEventTimeAfter(imsi, since);
    }
    
    private String getCurrentLocation(String imsi) {
        // 获取当前位置信息
        return "Unknown";
    }
    
    private void detectHighFrequencyUsage() {
        // 检测高频使用IMSI
    }
    
    private void detectRemoteLogin() {
        // 检测异地登录IMSI
    }
    
    private void detectInactiveImsi() {
        // 检测长时间未使用IMSI
    }
    
    private void detectAuthenticationFailures() {
        // 检测异常认证失败
    }
    
    private void updateDailyStatistics(ImsiUsageEvent event) {
        // 更新日统计
    }
    
    private void updateMonthlyStatistics(ImsiUsageEvent event) {
        // 更新月统计
    }
    
    private boolean isHighFrequencyUsage(String imsi) {
        // 检查是否为高频使用
        return false;
    }
    
    private boolean isRemoteUsage(ImsiUsageEvent event) {
        // 检查是否为异地使用
        return false;
    }
    
    private boolean isHighVolumeUsage(ImsiUsageEvent event) {
        // 检查是否为大流量使用
        return event.getDataVolume() > 1024 * 1024 * 1024; // 1GB
    }
    
    private void createAbnormalUsageAlert(ImsiUsageEvent event) {
        // 创建异常使用告警
        log.warn("Abnormal usage detected for IMSI: {}", maskImsi(event.getImsi()));
    }
    
    private String maskImsi(String imsi) {
        if (StringUtils.isEmpty(imsi) || imsi.length() < 10) {
            return imsi;
        }
        return imsi.substring(0, 6) + "*****" + imsi.substring(11);
    }
}
```

### 4. IMSI回收阶段

#### 回收策略
```java
/**
 * IMSI回收服务
 */
@Service
@Slf4j
public class ImsiRecyclingService {
    
    @Autowired
    private SimCardRepository simCardRepository;
    
    @Autowired
    private ImsiRecyclingRepository recyclingRepository;
    
    @Autowired
    private HlrIntegrationService hlrIntegrationService;
    
    /**
     * 回收IMSI
     */
    @Transactional
    public void recycleImsi(ImsiRecyclingRequest request) {
        log.info("Starting IMSI recycling: {}", maskImsi(request.getImsi()));
        
        try {
            // 1. 验证回收条件
            validateRecyclingConditions(request.getImsi());
            
            // 2. 从HLR/HSS删除用户数据
            removeFromHlr(request.getImsi());
            
            // 3. 清理认证数据
            clearAuthenticationData(request.getImsi());
            
            // 4. 更新SIM卡状态
            updateSimCardStatus(request.getImsi(), SimCardStatus.RECYCLED);
            
            // 5. 记录回收信息
            recordRecycling(request);
            
            // 6. 加入回收池
            addToRecyclingPool(request.getImsi());
            
            log.info("IMSI recycling completed: {}", maskImsi(request.getImsi()));
            
        } catch (Exception e) {
            log.error("IMSI recycling failed: {}", maskImsi(request.getImsi()), e);
            throw new ImsiRecyclingException("IMSI recycling failed", e);
        }
    }
    
    /**
     * 批量回收IMSI
     */
    @Transactional
    public void batchRecycleImsi(List<String> imsiList, String recycledBy, String reason) {
        log.info("Starting batch IMSI recycling, count: {}", imsiList.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (String imsi : imsiList) {
            try {
                ImsiRecyclingRequest request = ImsiRecyclingRequest.builder()
                    .imsi(imsi)
                    .recycledBy(recycledBy)
                    .reason(reason)
                    .build();
                
                recycleImsi(request);
                successCount++;
                
            } catch (Exception e) {
                log.error("Failed to recycle IMSI: {}", maskImsi(imsi), e);
                failureCount++;
            }
        }
        
        log.info("Batch IMSI recycling completed. Success: {}, Failure: {}", 
            successCount, failureCount);
    }
    
    /**
     * 自动回收过期IMSI
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void autoRecycleExpiredImsi() {
        log.info("Starting automatic IMSI recycling");
        
        try {
            // 查找需要回收的IMSI
            List<String> expiredImsiList = findExpiredImsi();
            
            if (!expiredImsiList.isEmpty()) {
                log.info("Found {} expired IMSI for recycling", expiredImsiList.size());
                
                // 批量回收
                batchRecycleImsi(expiredImsiList, "SYSTEM", "Auto recycling expired IMSI");
            }
            
        } catch (Exception e) {
            log.error("Automatic IMSI recycling failed", e);
        }
    }
    
    /**
     * 验证回收条件
     */
    private void validateRecyclingConditions(String imsi) {
        // 查询SIM卡信息
        Optional<SimCard> simCard = simCardRepository.findByImsi(imsi);
        if (simCard.isEmpty()) {
            throw new SimCardNotFoundException("SIM card not found: " + maskImsi(imsi));
        }
        
        SimCard card = simCard.get();
        
        // 检查状态是否允许回收
        if (!isRecyclableStatus(card.getStatus())) {
            throw new IllegalStateException(
                String.format("SIM card status %s is not recyclable: %s", 
                    card.getStatus(), maskImsi(imsi)));
        }
        
        // 检查是否有未结清费用
        if (hasOutstandingCharges(imsi)) {
            throw new IllegalStateException(
                "Cannot recycle IMSI with outstanding charges: " + maskImsi(imsi));
        }
        
        // 检查冷却期
        if (!hasCoolingPeriodPassed(card)) {
            throw new IllegalStateException(
                "Cooling period has not passed for IMSI: " + maskImsi(imsi));
        }
    }
    
    /**
     * 从HLR删除用户数据
     */
    private void removeFromHlr(String imsi) {
        try {
            hlrIntegrationService.deleteSubscriber(imsi);
            log.info("Subscriber data removed from HLR: {}", maskImsi(imsi));
        } catch (Exception e) {
            log.error("Failed to remove subscriber from HLR: {}", maskImsi(imsi), e);
            throw new HlrIntegrationException("Failed to remove subscriber from HLR", e);
        }
    }
    
    /**
     * 清理认证数据
     */
    private void clearAuthenticationData(String imsi) {
        try {
            // 从AuC删除认证数据
            aucIntegrationService.deleteSubscriber(imsi);
            
            // 清理本地认证密钥
            authenticationKeyRepository.deleteByImsi(imsi);
            
            log.info("Authentication data cleared for IMSI: {}", maskImsi(imsi));
        } catch (Exception e) {
            log.error("Failed to clear authentication data: {}", maskImsi(imsi), e);
            throw new AuthenticationDataException("Failed to clear authentication data", e);
        }
    }
    
    /**
     * 记录回收信息
     */
    private void recordRecycling(ImsiRecyclingRequest request) {
        ImsiRecyclingRecord record = ImsiRecyclingRecord.builder()
            .imsi(request.getImsi())
            .recycledBy(request.getRecycledBy())
            .recyclingTime(LocalDateTime.now())
            .reason(request.getReason())
            .status("SUCCESS")
            .build();
        
        recyclingRepository.save(record);
    }
    
    /**
     * 加入回收池
     */
    private void addToRecyclingPool(String imsi) {
        ImsiRecyclingPool pool = ImsiRecyclingPool.builder()
            .imsi(imsi)
            .recyclingTime(LocalDateTime.now())
            .availableTime(LocalDateTime.now().plusMonths(6)) // 6个月后可重新分配
            .status(RecyclingStatus.IN_POOL)
            .build();
        
        recyclingPoolRepository.save(pool);
    }
    
    /**
     * 查找过期IMSI
     */
    private List<String> findExpiredImsi() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusMonths(12); // 12个月未使用
        
        return simCardRepository.findExpiredImsi(
            Set.of(SimCardStatus.INACTIVE, SimCardStatus.SUSPENDED),
            expiredBefore
        );
    }
    
    /**
     * 检查状态是否可回收
     */
    private boolean isRecyclableStatus(SimCardStatus status) {
        return Set.of(
            SimCardStatus.INACTIVE,
            SimCardStatus.SUSPENDED,
            SimCardStatus.CANCELLED
        ).contains(status);
    }
    
    /**
     * 检查是否有未结清费用
     */
    private boolean hasOutstandingCharges(String imsi) {
        // 调用计费系统检查未结清费用
        // 这里简化处理
        return false;
    }
    
    /**
     * 检查冷却期是否已过
     */
    private boolean hasCoolingPeriodPassed(SimCard card) {
        if (card.getLastUsageTime() == null) {
            return true;
        }
        
        LocalDateTime coolingPeriodEnd = card.getLastUsageTime().plusMonths(3); // 3个月冷却期
        return LocalDateTime.now().isAfter(coolingPeriodEnd);
    }
    
    private String maskImsi(String imsi) {
        if (StringUtils.isEmpty(imsi) || imsi.length() < 10) {
            return imsi;
        }
        return imsi.substring(0, 6) + "*****" + imsi.substring(11);
    }
}
```

## 数据模型设计

### 核心实体类
```java
/**
 * IMSI池实体
 */
@Entity
@Table(name = "imsi_pool")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImsiPool {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "imsi", unique = true, nullable = false, length = 15)
    private String imsi;
    
    @Column(name = "prefix", nullable = false, length = 10)
    private String prefix;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImsiStatus status;
    
    @Column(name = "allocated_time")
    private LocalDateTime allocatedTime;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

/**
 * IMSI分配记录
 */
@Entity
@Table(name = "imsi_allocation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImsiAllocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "imsi", nullable = false, length = 15)
    private String imsi;
    
    @Column(name = "operator_code", nullable = false, length = 10)
    private String operatorCode;
    
    @Column(name = "region", nullable = false, length = 10)
    private String region;
    
    @Column(name = "allocated_by", nullable = false, length = 50)
    private String allocatedBy;
    
    @Column(name = "allocated_time", nullable = false)
    private LocalDateTime allocatedTime;
    
    @Column(name = "purpose", length = 100)
    private String purpose;
}

/**
 * 认证密钥
 */
@Entity
@Table(name = "authentication_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationKeys {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "imsi", unique = true, nullable = false, length = 15)
    private String imsi;
    
    @Column(name = "ki", nullable = false, length = 32)
    private String ki;
    
    @Column(name = "opc", nullable = false, length = 32)
    private String opc;
    
    @Column(name = "adm", length = 16)
    private String adm;
    
    @Column(name = "algorithm", nullable = false, length = 20)
    private String algorithm;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

/**
 * IMSI使用记录
 */
@Entity
@Table(name = "imsi_usage_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImsiUsageRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "imsi", nullable = false, length = 15)
    private String imsi;
    
    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;
    
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
    
    @Column(name = "cell_id", length = 20)
    private String cellId;
    
    @Column(name = "lac", length = 10)
    private String lac;
    
    @Column(name = "data_volume")
    private Long dataVolume;
    
    @Column(name = "duration")
    private Long duration;
}
```

### 枚举定义
```java
/**
 * IMSI状态枚举
 */
public enum ImsiStatus {
    AVAILABLE("可用"),
    ALLOCATED("已分配"),
    ACTIVE("激活"),
    INACTIVE("未激活"),
    SUSPENDED("暂停"),
    CANCELLED("注销"),
    RECYCLED("已回收");
    
    private final String description;
    
    ImsiStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * 网络状态枚举
 */
public enum NetworkStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    ROAMING("漫游"),
    UNKNOWN("未知");
    
    private final String description;
    
    NetworkStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

## 最佳实践

### 1. 安全管理
- **密钥保护**: Ki、OPc等认证密钥必须加密存储
- **访问控制**: 严格控制IMSI相关数据的访问权限
- **审计日志**: 记录所有IMSI操作的详细日志
- **数据脱敏**: 在日志和界面中对IMSI进行脱敏处理

### 2. 性能优化
- **索引优化**: 在IMSI字段上建立合适的索引
- **分库分表**: 根据IMSI前缀进行分库分表
- **缓存策略**: 对热点IMSI数据进行缓存
- **批量处理**: 支持IMSI的批量操作

### 3. 监控告警
- **使用监控**: 监控IMSI的使用情况和异常模式
- **容量告警**: 监控IMSI池的容量和使用率
- **安全告警**: 检测IMSI的异常使用和安全威胁
- **性能监控**: 监控IMSI相关操作的性能指标

### 4. 合规要求
- **数据保护**: 遵循GDPR等数据保护法规
- **运营商规范**: 符合电信运营商的管理规范
- **国际标准**: 遵循3GPP等国际标准
- **安全认证**: 通过相关的安全认证

通过以上IMSI管理的详细实现，可以确保NSRS号卡资源管理系统在IMSI的全生命周期管理中达到专业水准，满足电信行业的严格要求。