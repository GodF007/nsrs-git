# MSISDN管理详解

## 概述

MSISDN（Mobile Station International Subscriber Directory Number，移动台国际用户目录号码）是移动通信网络中用于标识移动用户的电话号码，也就是我们常说的手机号码。本文档详细介绍MSISDN的结构、分配策略、生命周期管理和业务应用。

## MSISDN结构解析

### 基本结构
```
MSISDN = CC + NDC + SN
总长度：最多15位数字
```

### 各部分详解

#### 1. CC（Country Code）- 国家码
- **长度**: 1-3位数字
- **作用**: 标识号码所属的国家或地区
- **中国CC**: 86
- **示例**:
  - 86 - 中国
  - 1 - 美国/加拿大
  - 44 - 英国
  - 81 - 日本

#### 2. NDC（National Destination Code）- 国内目的地码
- **长度**: 2-3位数字
- **作用**: 标识运营商和地区
- **中国移动网络码**:
  - 130-139, 147, 150-152, 157-159, 178, 182-184, 187-188, 198 - 中国移动
  - 130-132, 145, 155-156, 166, 175-176, 185-186 - 中国联通
  - 133-134, 149, 153, 173-174, 177, 180-181, 189, 199 - 中国电信

#### 3. SN（Subscriber Number）- 用户号码
- **长度**: 剩余位数
- **作用**: 在特定运营商网络内唯一标识用户
- **结构**: 通常包含地区码和用户序列号

### MSISDN示例分析
```
MSISDN: 8613812345678
├── CC: 86 (中国)
├── NDC: 138 (中国移动)
└── SN: 12345678 (用户号码)
```

## MSISDN分配管理

### 1. 号码资源池管理

```java
/**
 * MSISDN资源池管理服务
 */
@Service
@Slf4j
public class MsisdnPoolService {
    
    @Autowired
    private MsisdnPoolRepository poolRepository;
    
    @Autowired
    private MsisdnAllocationRepository allocationRepository;
    
    @Autowired
    private NumberPortabilityService portabilityService;
    
    /**
     * 初始化号码段
     */
    @Transactional
    public void initializeNumberRange(NumberRangeRequest request) {
        log.info("Initializing number range: {} - {}", 
            request.getStartNumber(), request.getEndNumber());
        
        try {
            // 1. 验证号码段
            validateNumberRange(request);
            
            // 2. 检查号码段冲突
            checkRangeConflict(request);
            
            // 3. 生成号码池
            generateNumberPool(request);
            
            // 4. 记录号码段信息
            recordNumberRange(request);
            
            log.info("Number range initialized successfully: {} - {}", 
                request.getStartNumber(), request.getEndNumber());
            
        } catch (Exception e) {
            log.error("Failed to initialize number range", e);
            throw new NumberRangeInitializationException("Failed to initialize number range", e);
        }
    }
    
    /**
     * 分配MSISDN
     */
    @Transactional
    public String allocateMsisdn(MsisdnAllocationRequest request) {
        log.info("Starting MSISDN allocation for operator: {}, region: {}", 
            request.getOperatorCode(), request.getRegion());
        
        try {
            // 1. 验证分配请求
            validateAllocationRequest(request);
            
            // 2. 选择号码
            String msisdn = selectAvailableNumber(request);
            
            // 3. 记录分配信息
            recordAllocation(msisdn, request);
            
            // 4. 更新号码状态
            updateNumberStatus(msisdn, NumberStatus.ALLOCATED);
            
            log.info("MSISDN allocated successfully: {}", maskMsisdn(msisdn));
            return msisdn;
            
        } catch (Exception e) {
            log.error("MSISDN allocation failed", e);
            throw new MsisdnAllocationException("MSISDN allocation failed", e);
        }
    }
    
    /**
     * 批量分配MSISDN
     */
    @Transactional
    public List<String> batchAllocateMsisdn(BatchAllocationRequest request) {
        log.info("Starting batch MSISDN allocation, count: {}", request.getCount());
        
        List<String> allocatedNumbers = new ArrayList<>();
        
        try {
            // 1. 验证批量分配请求
            validateBatchAllocationRequest(request);
            
            // 2. 检查可用号码数量
            checkAvailableNumberCount(request);
            
            // 3. 批量分配
            for (int i = 0; i < request.getCount(); i++) {
                MsisdnAllocationRequest singleRequest = MsisdnAllocationRequest.builder()
                    .operatorCode(request.getOperatorCode())
                    .region(request.getRegion())
                    .numberType(request.getNumberType())
                    .allocatedBy(request.getAllocatedBy())
                    .purpose(request.getPurpose())
                    .build();
                
                String msisdn = allocateMsisdn(singleRequest);
                allocatedNumbers.add(msisdn);
            }
            
            log.info("Batch MSISDN allocation completed, allocated: {}", allocatedNumbers.size());
            return allocatedNumbers;
            
        } catch (Exception e) {
            log.error("Batch MSISDN allocation failed", e);
            // 回滚已分配的号码
            rollbackAllocatedNumbers(allocatedNumbers);
            throw new BatchAllocationException("Batch MSISDN allocation failed", e);
        }
    }
    
    /**
     * 选择可用号码
     */
    private String selectAvailableNumber(MsisdnAllocationRequest request) {
        // 1. 根据偏好选择号码
        if (request.getNumberPreference() != null) {
            String preferredNumber = selectPreferredNumber(request);
            if (preferredNumber != null) {
                return preferredNumber;
            }
        }
        
        // 2. 根据号码类型选择
        switch (request.getNumberType()) {
            case GOLDEN:
                return selectGoldenNumber(request);
            case SILVER:
                return selectSilverNumber(request);
            case REGULAR:
                return selectRegularNumber(request);
            default:
                return selectRegularNumber(request);
        }
    }
    
    /**
     * 选择靓号
     */
    private String selectGoldenNumber(MsisdnAllocationRequest request) {
        // 查找靓号池中的可用号码
        List<MsisdnPool> goldenNumbers = poolRepository.findAvailableGoldenNumbers(
            request.getOperatorCode(), request.getRegion());
        
        if (goldenNumbers.isEmpty()) {
            throw new NoAvailableNumberException("No golden numbers available");
        }
        
        // 选择评分最高的靓号
        return goldenNumbers.stream()
            .max(Comparator.comparing(MsisdnPool::getGoldenScore))
            .map(MsisdnPool::getMsisdn)
            .orElseThrow(() -> new NoAvailableNumberException("No golden numbers available"));
    }
    
    /**
     * 选择银号
     */
    private String selectSilverNumber(MsisdnAllocationRequest request) {
        List<MsisdnPool> silverNumbers = poolRepository.findAvailableSilverNumbers(
            request.getOperatorCode(), request.getRegion());
        
        if (silverNumbers.isEmpty()) {
            throw new NoAvailableNumberException("No silver numbers available");
        }
        
        return silverNumbers.get(0).getMsisdn();
    }
    
    /**
     * 选择普通号码
     */
    private String selectRegularNumber(MsisdnAllocationRequest request) {
        Optional<MsisdnPool> regularNumber = poolRepository
            .findFirstAvailableRegularNumber(request.getOperatorCode(), request.getRegion());
        
        return regularNumber
            .map(MsisdnPool::getMsisdn)
            .orElseThrow(() -> new NoAvailableNumberException("No regular numbers available"));
    }
    
    /**
     * 选择偏好号码
     */
    private String selectPreferredNumber(MsisdnAllocationRequest request) {
        NumberPreference preference = request.getNumberPreference();
        
        // 根据偏好条件查找号码
        List<MsisdnPool> candidates = poolRepository.findByPreference(
            request.getOperatorCode(),
            request.getRegion(),
            preference.getContainsDigits(),
            preference.getExcludeDigits(),
            preference.getPattern()
        );
        
        return candidates.isEmpty() ? null : candidates.get(0).getMsisdn();
    }
    
    /**
     * 验证号码段
     */
    private void validateNumberRange(NumberRangeRequest request) {
        if (StringUtils.isEmpty(request.getStartNumber()) || 
            StringUtils.isEmpty(request.getEndNumber())) {
            throw new IllegalArgumentException("Start and end numbers are required");
        }
        
        if (!isValidMsisdnFormat(request.getStartNumber()) || 
            !isValidMsisdnFormat(request.getEndNumber())) {
            throw new IllegalArgumentException("Invalid MSISDN format");
        }
        
        if (request.getStartNumber().compareTo(request.getEndNumber()) >= 0) {
            throw new IllegalArgumentException("Start number must be less than end number");
        }
    }
    
    /**
     * 检查号码段冲突
     */
    private void checkRangeConflict(NumberRangeRequest request) {
        boolean hasConflict = poolRepository.existsRangeConflict(
            request.getStartNumber(), request.getEndNumber());
        
        if (hasConflict) {
            throw new NumberRangeConflictException(
                "Number range conflicts with existing ranges");
        }
    }
    
    /**
     * 生成号码池
     */
    private void generateNumberPool(NumberRangeRequest request) {
        String startNumber = request.getStartNumber();
        String endNumber = request.getEndNumber();
        
        long start = Long.parseLong(startNumber);
        long end = Long.parseLong(endNumber);
        
        List<MsisdnPool> poolEntries = new ArrayList<>();
        
        for (long number = start; number <= end; number++) {
            String msisdn = String.valueOf(number);
            
            // 分析号码特征
            NumberCharacteristics characteristics = analyzeNumberCharacteristics(msisdn);
            
            MsisdnPool poolEntry = MsisdnPool.builder()
                .msisdn(msisdn)
                .operatorCode(request.getOperatorCode())
                .region(request.getRegion())
                .numberType(characteristics.getNumberType())
                .goldenScore(characteristics.getGoldenScore())
                .status(NumberStatus.AVAILABLE)
                .createdTime(LocalDateTime.now())
                .build();
            
            poolEntries.add(poolEntry);
            
            // 批量保存，避免内存溢出
            if (poolEntries.size() >= 1000) {
                poolRepository.saveAll(poolEntries);
                poolEntries.clear();
            }
        }
        
        // 保存剩余的号码
        if (!poolEntries.isEmpty()) {
            poolRepository.saveAll(poolEntries);
        }
    }
    
    /**
     * 分析号码特征
     */
    private NumberCharacteristics analyzeNumberCharacteristics(String msisdn) {
        NumberCharacteristics.NumberCharacteristicsBuilder builder = 
            NumberCharacteristics.builder();
        
        // 提取后8位进行分析
        String suffix = msisdn.substring(msisdn.length() - 8);
        
        int goldenScore = 0;
        NumberType numberType = NumberType.REGULAR;
        
        // 分析连号
        int consecutiveCount = getMaxConsecutiveCount(suffix);
        if (consecutiveCount >= 4) {
            goldenScore += consecutiveCount * 10;
            numberType = NumberType.GOLDEN;
        } else if (consecutiveCount >= 3) {
            goldenScore += consecutiveCount * 5;
            numberType = NumberType.SILVER;
        }
        
        // 分析重复数字
        int repeatCount = getMaxRepeatCount(suffix);
        if (repeatCount >= 4) {
            goldenScore += repeatCount * 8;
            numberType = NumberType.GOLDEN;
        } else if (repeatCount >= 3) {
            goldenScore += repeatCount * 4;
            if (numberType == NumberType.REGULAR) {
                numberType = NumberType.SILVER;
            }
        }
        
        // 分析特殊模式
        if (hasSpecialPattern(suffix)) {
            goldenScore += 20;
            numberType = NumberType.GOLDEN;
        }
        
        // 分析吉利数字
        goldenScore += getLuckyNumberScore(suffix);
        
        return builder
            .numberType(numberType)
            .goldenScore(goldenScore)
            .consecutiveCount(consecutiveCount)
            .repeatCount(repeatCount)
            .build();
    }
    
    /**
     * 获取最大连续数字个数
     */
    private int getMaxConsecutiveCount(String number) {
        int maxCount = 1;
        int currentCount = 1;
        
        for (int i = 1; i < number.length(); i++) {
            int current = Character.getNumericValue(number.charAt(i));
            int previous = Character.getNumericValue(number.charAt(i - 1));
            
            if (current == previous + 1 || current == previous - 1) {
                currentCount++;
                maxCount = Math.max(maxCount, currentCount);
            } else {
                currentCount = 1;
            }
        }
        
        return maxCount;
    }
    
    /**
     * 获取最大重复数字个数
     */
    private int getMaxRepeatCount(String number) {
        Map<Character, Integer> digitCount = new HashMap<>();
        
        for (char digit : number.toCharArray()) {
            digitCount.put(digit, digitCount.getOrDefault(digit, 0) + 1);
        }
        
        return digitCount.values().stream().mapToInt(Integer::intValue).max().orElse(1);
    }
    
    /**
     * 检查特殊模式
     */
    private boolean hasSpecialPattern(String number) {
        // ABAB模式
        if (number.length() >= 4) {
            String ab = number.substring(0, 2);
            String cd = number.substring(2, 4);
            if (ab.equals(cd)) {
                return true;
            }
        }
        
        // ABCD模式（递增或递减）
        if (isSequential(number)) {
            return true;
        }
        
        // 回文模式
        if (isPalindrome(number)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取吉利数字评分
     */
    private int getLuckyNumberScore(String number) {
        int score = 0;
        
        // 统计吉利数字
        long count6 = number.chars().filter(ch -> ch == '6').count();
        long count8 = number.chars().filter(ch -> ch == '8').count();
        long count9 = number.chars().filter(ch -> ch == '9').count();
        
        score += count6 * 2; // 6代表顺利
        score += count8 * 3; // 8代表发财
        score += count9 * 2; // 9代表长久
        
        // 扣除不吉利数字
        long count4 = number.chars().filter(ch -> ch == '4').count();
        score -= count4 * 2; // 4代表不吉利
        
        return Math.max(0, score);
    }
    
    /**
     * 检查是否为序列号码
     */
    private boolean isSequential(String number) {
        if (number.length() < 3) {
            return false;
        }
        
        // 检查递增
        boolean ascending = true;
        for (int i = 1; i < number.length(); i++) {
            int current = Character.getNumericValue(number.charAt(i));
            int previous = Character.getNumericValue(number.charAt(i - 1));
            if (current != previous + 1) {
                ascending = false;
                break;
            }
        }
        
        // 检查递减
        boolean descending = true;
        for (int i = 1; i < number.length(); i++) {
            int current = Character.getNumericValue(number.charAt(i));
            int previous = Character.getNumericValue(number.charAt(i - 1));
            if (current != previous - 1) {
                descending = false;
                break;
            }
        }
        
        return ascending || descending;
    }
    
    /**
     * 检查是否为回文
     */
    private boolean isPalindrome(String number) {
        int left = 0;
        int right = number.length() - 1;
        
        while (left < right) {
            if (number.charAt(left) != number.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        
        return true;
    }
    
    /**
     * 记录分配信息
     */
    private void recordAllocation(String msisdn, MsisdnAllocationRequest request) {
        MsisdnAllocation allocation = MsisdnAllocation.builder()
            .msisdn(msisdn)
            .operatorCode(request.getOperatorCode())
            .region(request.getRegion())
            .numberType(request.getNumberType())
            .allocatedBy(request.getAllocatedBy())
            .allocatedTime(LocalDateTime.now())
            .purpose(request.getPurpose())
            .build();
        
        allocationRepository.save(allocation);
    }
    
    /**
     * 更新号码状态
     */
    private void updateNumberStatus(String msisdn, NumberStatus status) {
        poolRepository.updateStatusByMsisdn(msisdn, status, LocalDateTime.now());
    }
    
    /**
     * 回滚已分配的号码
     */
    private void rollbackAllocatedNumbers(List<String> allocatedNumbers) {
        for (String msisdn : allocatedNumbers) {
            try {
                updateNumberStatus(msisdn, NumberStatus.AVAILABLE);
                allocationRepository.deleteByMsisdn(msisdn);
            } catch (Exception e) {
                log.error("Failed to rollback allocated number: {}", maskMsisdn(msisdn), e);
            }
        }
    }
    
    // 其他辅助方法...
    private void validateAllocationRequest(MsisdnAllocationRequest request) {
        if (StringUtils.isEmpty(request.getOperatorCode())) {
            throw new IllegalArgumentException("Operator code is required");
        }
        
        if (StringUtils.isEmpty(request.getRegion())) {
            throw new IllegalArgumentException("Region is required");
        }
        
        if (request.getNumberType() == null) {
            throw new IllegalArgumentException("Number type is required");
        }
    }
    
    private void validateBatchAllocationRequest(BatchAllocationRequest request) {
        if (request.getCount() <= 0 || request.getCount() > 10000) {
            throw new IllegalArgumentException("Invalid allocation count");
        }
        
        validateAllocationRequest(request);
    }
    
    private void checkAvailableNumberCount(BatchAllocationRequest request) {
        long availableCount = poolRepository.countAvailableNumbers(
            request.getOperatorCode(), request.getRegion(), request.getNumberType());
        
        if (availableCount < request.getCount()) {
            throw new InsufficientNumberException(
                String.format("Insufficient numbers available. Required: %d, Available: %d", 
                    request.getCount(), availableCount));
        }
    }
    
    private void recordNumberRange(NumberRangeRequest request) {
        NumberRange range = NumberRange.builder()
            .startNumber(request.getStartNumber())
            .endNumber(request.getEndNumber())
            .operatorCode(request.getOperatorCode())
            .region(request.getRegion())
            .createdBy(request.getCreatedBy())
            .createdTime(LocalDateTime.now())
            .build();
        
        numberRangeRepository.save(range);
    }
    
    private boolean isValidMsisdnFormat(String msisdn) {
        return msisdn != null && msisdn.matches("\\d{11,15}") && msisdn.startsWith("86");
    }
    
    private String maskMsisdn(String msisdn) {
        if (StringUtils.isEmpty(msisdn) || msisdn.length() < 7) {
            return msisdn;
        }
        return msisdn.substring(0, 3) + "****" + msisdn.substring(7);
    }
}
```

### 2. 号码激活管理

```java
/**
 * MSISDN激活服务
 */
@Service
@Slf4j
public class MsisdnActivationService {
    
    @Autowired
    private SimCardRepository simCardRepository;
    
    @Autowired
    private MsisdnPoolRepository poolRepository;
    
    @Autowired
    private HlrIntegrationService hlrIntegrationService;
    
    @Autowired
    private NumberPortabilityService portabilityService;
    
    /**
     * 激活MSISDN
     */
    @Transactional
    public void activateMsisdn(MsisdnActivationRequest request) {
        log.info("Starting MSISDN activation: {}", maskMsisdn(request.getMsisdn()));
        
        try {
            // 1. 验证激活条件
            validateActivationConditions(request);
            
            // 2. 检查号码可用性
            checkNumberAvailability(request.getMsisdn());
            
            // 3. 绑定IMSI和MSISDN
            bindImsiMsisdn(request.getImsi(), request.getMsisdn());
            
            // 4. 写入HLR
            writeToHlr(request);
            
            // 5. 更新号码状态
            updateNumberStatus(request.getMsisdn(), NumberStatus.ACTIVE);
            
            // 6. 记录激活日志
            recordActivationLog(request);
            
            log.info("MSISDN activation completed: {}", maskMsisdn(request.getMsisdn()));
            
        } catch (Exception e) {
            log.error("MSISDN activation failed: {}", maskMsisdn(request.getMsisdn()), e);
            throw new MsisdnActivationException("MSISDN activation failed", e);
        }
    }
    
    /**
     * 停用MSISDN
     */
    @Transactional
    public void deactivateMsisdn(MsisdnDeactivationRequest request) {
        log.info("Starting MSISDN deactivation: {}", maskMsisdn(request.getMsisdn()));
        
        try {
            // 1. 验证停用条件
            validateDeactivationConditions(request);
            
            // 2. 检查业务状态
            checkBusinessStatus(request.getMsisdn());
            
            // 3. 从HLR删除路由信息
            removeFromHlr(request.getMsisdn());
            
            // 4. 解绑IMSI和MSISDN
            unbindImsiMsisdn(request.getMsisdn());
            
            // 5. 更新号码状态
            updateNumberStatus(request.getMsisdn(), NumberStatus.INACTIVE);
            
            // 6. 记录停用日志
            recordDeactivationLog(request);
            
            log.info("MSISDN deactivation completed: {}", maskMsisdn(request.getMsisdn()));
            
        } catch (Exception e) {
            log.error("MSISDN deactivation failed: {}", maskMsisdn(request.getMsisdn()), e);
            throw new MsisdnDeactivationException("MSISDN deactivation failed", e);
        }
    }
    
    /**
     * 号码携带
     */
    @Transactional
    public void portNumber(NumberPortabilityRequest request) {
        log.info("Starting number portability: {} from {} to {}", 
            maskMsisdn(request.getMsisdn()), 
            request.getFromOperator(), 
            request.getToOperator());
        
        try {
            // 1. 验证携带条件
            validatePortabilityConditions(request);
            
            // 2. 检查携带资格
            checkPortabilityEligibility(request);
            
            // 3. 执行号码携带
            executeNumberPortability(request);
            
            // 4. 更新路由信息
            updateRoutingInfo(request);
            
            // 5. 记录携带日志
            recordPortabilityLog(request);
            
            log.info("Number portability completed: {}", maskMsisdn(request.getMsisdn()));
            
        } catch (Exception e) {
            log.error("Number portability failed: {}", maskMsisdn(request.getMsisdn()), e);
            throw new NumberPortabilityException("Number portability failed", e);
        }
    }
    
    /**
     * 验证激活条件
     */
    private void validateActivationConditions(MsisdnActivationRequest request) {
        // 验证MSISDN格式
        if (!isValidMsisdnFormat(request.getMsisdn())) {
            throw new IllegalArgumentException("Invalid MSISDN format: " + 
                maskMsisdn(request.getMsisdn()));
        }
        
        // 验证IMSI格式
        if (!isValidImsiFormat(request.getImsi())) {
            throw new IllegalArgumentException("Invalid IMSI format");
        }
        
        // 检查SIM卡状态
        Optional<SimCard> simCard = simCardRepository.findByImsi(request.getImsi());
        if (simCard.isEmpty()) {
            throw new SimCardNotFoundException("SIM card not found for IMSI");
        }
        
        if (simCard.get().getStatus() != SimCardStatus.ALLOCATED) {
            throw new IllegalStateException("SIM card is not in allocated status");
        }
    }
    
    /**
     * 检查号码可用性
     */
    private void checkNumberAvailability(String msisdn) {
        Optional<MsisdnPool> numberPool = poolRepository.findByMsisdn(msisdn);
        
        if (numberPool.isEmpty()) {
            throw new NumberNotFoundException("Number not found in pool: " + maskMsisdn(msisdn));
        }
        
        if (numberPool.get().getStatus() != NumberStatus.ALLOCATED) {
            throw new IllegalStateException(
                String.format("Number status %s is not valid for activation: %s", 
                    numberPool.get().getStatus(), maskMsisdn(msisdn)));
        }
    }
    
    /**
     * 绑定IMSI和MSISDN
     */
    private void bindImsiMsisdn(String imsi, String msisdn) {
        // 更新SIM卡信息
        simCardRepository.updateMsisdnByImsi(imsi, msisdn, LocalDateTime.now());
        
        // 创建绑定记录
        ImsiMsisdnBinding binding = ImsiMsisdnBinding.builder()
            .imsi(imsi)
            .msisdn(msisdn)
            .bindingTime(LocalDateTime.now())
            .status(BindingStatus.ACTIVE)
            .build();
        
        bindingRepository.save(binding);
    }
    
    /**
     * 写入HLR
     */
    private void writeToHlr(MsisdnActivationRequest request) {
        HlrRoutingInfo routingInfo = HlrRoutingInfo.builder()
            .msisdn(request.getMsisdn())
            .imsi(request.getImsi())
            .operatorCode(request.getOperatorCode())
            .serviceProfile(request.getServiceProfile())
            .routingNumber(generateRoutingNumber(request.getMsisdn()))
            .build();
        
        hlrIntegrationService.createRoutingInfo(routingInfo);
    }
    
    /**
     * 解绑IMSI和MSISDN
     */
    private void unbindImsiMsisdn(String msisdn) {
        // 查找绑定记录
        Optional<ImsiMsisdnBinding> binding = bindingRepository.findByMsisdn(msisdn);
        
        if (binding.isPresent()) {
            // 更新绑定状态
            binding.get().setStatus(BindingStatus.INACTIVE);
            binding.get().setUnbindingTime(LocalDateTime.now());
            bindingRepository.save(binding.get());
            
            // 清除SIM卡中的MSISDN
            simCardRepository.clearMsisdnByImsi(binding.get().getImsi());
        }
    }
    
    /**
     * 从HLR删除路由信息
     */
    private void removeFromHlr(String msisdn) {
        try {
            hlrIntegrationService.deleteRoutingInfo(msisdn);
        } catch (Exception e) {
            log.error("Failed to remove routing info from HLR: {}", maskMsisdn(msisdn), e);
            throw new HlrIntegrationException("Failed to remove routing info from HLR", e);
        }
    }
    
    /**
     * 执行号码携带
     */
    private void executeNumberPortability(NumberPortabilityRequest request) {
        // 调用号码携带服务
        portabilityService.executePortability(request);
        
        // 更新号码归属
        poolRepository.updateOperatorByMsisdn(
            request.getMsisdn(), 
            request.getToOperator(), 
            LocalDateTime.now()
        );
    }
    
    /**
     * 更新路由信息
     */
    private void updateRoutingInfo(NumberPortabilityRequest request) {
        HlrRoutingInfo routingInfo = HlrRoutingInfo.builder()
            .msisdn(request.getMsisdn())
            .operatorCode(request.getToOperator())
            .routingNumber(generateRoutingNumber(request.getMsisdn()))
            .portedFlag(true)
            .portingDate(LocalDateTime.now())
            .build();
        
        hlrIntegrationService.updateRoutingInfo(routingInfo);
    }
    
    /**
     * 生成路由号码
     */
    private String generateRoutingNumber(String msisdn) {
        // 根据MSISDN生成路由号码
        // 这里简化处理，实际应根据运营商规则生成
        return "86" + msisdn.substring(2);
    }
    
    /**
     * 记录激活日志
     */
    private void recordActivationLog(MsisdnActivationRequest request) {
        MsisdnActivationLog log = MsisdnActivationLog.builder()
            .msisdn(request.getMsisdn())
            .imsi(request.getImsi())
            .activatedBy(request.getActivatedBy())
            .activationTime(LocalDateTime.now())
            .result("SUCCESS")
            .build();
        
        activationLogRepository.save(log);
    }
    
    /**
     * 记录停用日志
     */
    private void recordDeactivationLog(MsisdnDeactivationRequest request) {
        MsisdnDeactivationLog log = MsisdnDeactivationLog.builder()
            .msisdn(request.getMsisdn())
            .deactivatedBy(request.getDeactivatedBy())
            .deactivationTime(LocalDateTime.now())
            .reason(request.getReason())
            .result("SUCCESS")
            .build();
        
        deactivationLogRepository.save(log);
    }
    
    /**
     * 记录携带日志
     */
    private void recordPortabilityLog(NumberPortabilityRequest request) {
        NumberPortabilityLog log = NumberPortabilityLog.builder()
            .msisdn(request.getMsisdn())
            .fromOperator(request.getFromOperator())
            .toOperator(request.getToOperator())
            .portingTime(LocalDateTime.now())
            .requestedBy(request.getRequestedBy())
            .result("SUCCESS")
            .build();
        
        portabilityLogRepository.save(log);
    }
    
    // 其他辅助方法...
    private void validateDeactivationConditions(MsisdnDeactivationRequest request) {
        if (!isValidMsisdnFormat(request.getMsisdn())) {
            throw new IllegalArgumentException("Invalid MSISDN format");
        }
        
        if (StringUtils.isEmpty(request.getReason())) {
            throw new IllegalArgumentException("Deactivation reason is required");
        }
    }
    
    private void checkBusinessStatus(String msisdn) {
        // 检查是否有未结清费用
        if (hasOutstandingCharges(msisdn)) {
            throw new IllegalStateException("Cannot deactivate MSISDN with outstanding charges");
        }
        
        // 检查是否有活跃业务
        if (hasActiveServices(msisdn)) {
            throw new IllegalStateException("Cannot deactivate MSISDN with active services");
        }
    }
    
    private void validatePortabilityConditions(NumberPortabilityRequest request) {
        if (!isValidMsisdnFormat(request.getMsisdn())) {
            throw new IllegalArgumentException("Invalid MSISDN format");
        }
        
        if (StringUtils.isEmpty(request.getFromOperator()) || 
            StringUtils.isEmpty(request.getToOperator())) {
            throw new IllegalArgumentException("From and to operators are required");
        }
        
        if (request.getFromOperator().equals(request.getToOperator())) {
            throw new IllegalArgumentException("From and to operators cannot be the same");
        }
    }
    
    private void checkPortabilityEligibility(NumberPortabilityRequest request) {
        // 检查号码是否符合携带条件
        if (!isPortabilityEligible(request.getMsisdn())) {
            throw new IllegalStateException("Number is not eligible for portability");
        }
        
        // 检查冷却期
        if (!hasPortabilityCoolingPeriodPassed(request.getMsisdn())) {
            throw new IllegalStateException("Portability cooling period has not passed");
        }
    }
    
    private boolean isValidMsisdnFormat(String msisdn) {
        return msisdn != null && msisdn.matches("\\d{11,15}");
    }
    
    private boolean isValidImsiFormat(String imsi) {
        return imsi != null && imsi.matches("\\d{15}");
    }
    
    private boolean hasOutstandingCharges(String msisdn) {
        // 调用计费系统检查
        return false;
    }
    
    private boolean hasActiveServices(String msisdn) {
        // 检查活跃业务
        return false;
    }
    
    private boolean isPortabilityEligible(String msisdn) {
        // 检查携带资格
        return true;
    }
    
    private boolean hasPortabilityCoolingPeriodPassed(String msisdn) {
        // 检查携带冷却期
        return true;
    }
    
    private String maskMsisdn(String msisdn) {
        if (StringUtils.isEmpty(msisdn) || msisdn.length() < 7) {
            return msisdn;
        }
        return msisdn.substring(0, 3) + "****" + msisdn.substring(7);
    }
}
```

### 3. 号码回收管理

```java
/**
 * MSISDN回收服务
 */
@Service
@Slf4j
public class MsisdnRecyclingService {
    
    @Autowired
    private MsisdnPoolRepository poolRepository;
    
    @Autowired
    private MsisdnRecyclingRepository recyclingRepository;
    
    @Autowired
    private HlrIntegrationService hlrIntegrationService;
    
    /**
     * 回收MSISDN
     */
    @Transactional
    public void recycleMsisdn(MsisdnRecyclingRequest request) {
        log.info("Starting MSISDN recycling: {}", maskMsisdn(request.getMsisdn()));
        
        try {
            // 1. 验证回收条件
            validateRecyclingConditions(request);
            
            // 2. 清理HLR路由信息
            clearHlrRoutingInfo(request.getMsisdn());
            
            // 3. 清理绑定关系
            clearBindingRelations(request.getMsisdn());
            
            // 4. 更新号码状态
            updateNumberStatus(request.getMsisdn(), NumberStatus.RECYCLED);
            
            // 5. 记录回收信息
            recordRecycling(request);
            
            // 6. 加入回收池
            addToRecyclingPool(request.getMsisdn());
            
            log.info("MSISDN recycling completed: {}", maskMsisdn(request.getMsisdn()));
            
        } catch (Exception e) {
            log.error("MSISDN recycling failed: {}", maskMsisdn(request.getMsisdn()), e);
            throw new MsisdnRecyclingException("MSISDN recycling failed", e);
        }
    }
    
    /**
     * 自动回收过期号码
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void autoRecycleExpiredNumbers() {
        log.info("Starting automatic MSISDN recycling");
        
        try {
            // 查找需要回收的号码
            List<String> expiredNumbers = findExpiredNumbers();
            
            if (!expiredNumbers.isEmpty()) {
                log.info("Found {} expired MSISDN for recycling", expiredNumbers.size());
                
                // 批量回收
                batchRecycleMsisdn(expiredNumbers, "SYSTEM", "Auto recycling expired numbers");
            }
            
        } catch (Exception e) {
            log.error("Automatic MSISDN recycling failed", e);
        }
    }
    
    /**
     * 批量回收MSISDN
     */
    @Transactional
    public void batchRecycleMsisdn(List<String> msisdnList, String recycledBy, String reason) {
        log.info("Starting batch MSISDN recycling, count: {}", msisdnList.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (String msisdn : msisdnList) {
            try {
                MsisdnRecyclingRequest request = MsisdnRecyclingRequest.builder()
                    .msisdn(msisdn)
                    .recycledBy(recycledBy)
                    .reason(reason)
                    .build();
                
                recycleMsisdn(request);
                successCount++;
                
            } catch (Exception e) {
                log.error("Failed to recycle MSISDN: {}", maskMsisdn(msisdn), e);
                failureCount++;
            }
        }
        
        log.info("Batch MSISDN recycling completed. Success: {}, Failure: {}", 
            successCount, failureCount);
    }
    
    /**
     * 号码重新分配
     */
    @Transactional
    public void reallocateRecycledNumber(NumberReallocationRequest request) {
        log.info("Starting number reallocation: {}", maskMsisdn(request.getMsisdn()));
        
        try {
            // 1. 验证重新分配条件
            validateReallocationConditions(request);
            
            // 2. 检查冷却期
            checkCoolingPeriod(request.getMsisdn());
            
            // 3. 清理历史数据
            cleanHistoryData(request.getMsisdn());
            
            // 4. 重置号码状态
            resetNumberStatus(request.getMsisdn());
            
            // 5. 记录重新分配
            recordReallocation(request);
            
            log.info("Number reallocation completed: {}", maskMsisdn(request.getMsisdn()));
            
        } catch (Exception e) {
            log.error("Number reallocation failed: {}", maskMsisdn(request.getMsisdn()), e);
            throw new NumberReallocationException("Number reallocation failed", e);
        }
    }
    
    /**
     * 验证回收条件
     */
    private void validateRecyclingConditions(MsisdnRecyclingRequest request) {
        // 查询号码信息
        Optional<MsisdnPool> numberPool = poolRepository.findByMsisdn(request.getMsisdn());
        if (numberPool.isEmpty()) {
            throw new NumberNotFoundException("Number not found: " + maskMsisdn(request.getMsisdn()));
        }
        
        MsisdnPool pool = numberPool.get();
        
        // 检查状态是否允许回收
        if (!isRecyclableStatus(pool.getStatus())) {
            throw new IllegalStateException(
                String.format("Number status %s is not recyclable: %s", 
                    pool.getStatus(), maskMsisdn(request.getMsisdn())));
        }
        
        // 检查是否有未结清费用
        if (hasOutstandingCharges(request.getMsisdn())) {
            throw new IllegalStateException(
                "Cannot recycle number with outstanding charges: " + 
                maskMsisdn(request.getMsisdn()));
        }
        
        // 检查冷却期
        if (!hasRecyclingCoolingPeriodPassed(pool)) {
            throw new IllegalStateException(
                "Recycling cooling period has not passed: " + 
                maskMsisdn(request.getMsisdn()));
        }
    }
    
    /**
     * 清理HLR路由信息
     */
    private void clearHlrRoutingInfo(String msisdn) {
        try {
            hlrIntegrationService.deleteRoutingInfo(msisdn);
            log.info("HLR routing info cleared for MSISDN: {}", maskMsisdn(msisdn));
        } catch (Exception e) {
            log.error("Failed to clear HLR routing info: {}", maskMsisdn(msisdn), e);
            throw new HlrIntegrationException("Failed to clear HLR routing info", e);
        }
    }
    
    /**
     * 清理绑定关系
     */
    private void clearBindingRelations(String msisdn) {
        // 清理IMSI-MSISDN绑定
        bindingRepository.deleteByMsisdn(msisdn);
        
        // 清理SIM卡中的MSISDN
        simCardRepository.clearMsisdnByMsisdn(msisdn);
        
        log.info("Binding relations cleared for MSISDN: {}", maskMsisdn(msisdn));
    }
    
    /**
     * 加入回收池
     */
    private void addToRecyclingPool(String msisdn) {
        MsisdnRecyclingPool pool = MsisdnRecyclingPool.builder()
            .msisdn(msisdn)
            .recyclingTime(LocalDateTime.now())
            .availableTime(LocalDateTime.now().plusMonths(6)) // 6个月后可重新分配
            .status(RecyclingStatus.IN_POOL)
            .build();
        
        recyclingPoolRepository.save(pool);
    }
    
    /**
     * 查找过期号码
     */
    private List<String> findExpiredNumbers() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusMonths(12); // 12个月未使用
        
        return poolRepository.findExpiredNumbers(
            Set.of(NumberStatus.INACTIVE, NumberStatus.SUSPENDED),
            expiredBefore
        );
    }
    
    /**
     * 验证重新分配条件
     */
    private void validateReallocationConditions(NumberReallocationRequest request) {
        Optional<MsisdnRecyclingPool> recyclingPool = 
            recyclingPoolRepository.findByMsisdn(request.getMsisdn());
        
        if (recyclingPool.isEmpty()) {
            throw new NumberNotFoundException(
                "Number not found in recycling pool: " + maskMsisdn(request.getMsisdn()));
        }
        
        if (recyclingPool.get().getStatus() != RecyclingStatus.IN_POOL) {
            throw new IllegalStateException(
                "Number is not available for reallocation: " + maskMsisdn(request.getMsisdn()));
        }
    }
    
    /**
     * 检查冷却期
     */
    private void checkCoolingPeriod(String msisdn) {
        Optional<MsisdnRecyclingPool> recyclingPool = 
            recyclingPoolRepository.findByMsisdn(msisdn);
        
        if (recyclingPool.isPresent()) {
            LocalDateTime availableTime = recyclingPool.get().getAvailableTime();
            if (LocalDateTime.now().isBefore(availableTime)) {
                throw new IllegalStateException(
                    "Number is still in cooling period: " + maskMsisdn(msisdn));
            }
        }
    }
    
    /**
     * 清理历史数据
     */
    private void cleanHistoryData(String msisdn) {
        // 清理使用记录（保留必要的审计数据）
        usageRecordRepository.archiveByMsisdn(msisdn);
        
        // 清理临时数据
        temporaryDataRepository.deleteByMsisdn(msisdn);
        
        log.info("History data cleaned for MSISDN: {}", maskMsisdn(msisdn));
    }
    
    /**
     * 重置号码状态
     */
    private void resetNumberStatus(String msisdn) {
        // 更新号码池状态
        poolRepository.updateStatusByMsisdn(msisdn, NumberStatus.AVAILABLE, LocalDateTime.now());
        
        // 从回收池中移除
        recyclingPoolRepository.deleteByMsisdn(msisdn);
        
        log.info("Number status reset for MSISDN: {}", maskMsisdn(msisdn));
    }
    
    /**
     * 记录回收信息
     */
    private void recordRecycling(MsisdnRecyclingRequest request) {
        MsisdnRecyclingRecord record = MsisdnRecyclingRecord.builder()
            .msisdn(request.getMsisdn())
            .recycledBy(request.getRecycledBy())
            .recyclingTime(LocalDateTime.now())
            .reason(request.getReason())
            .status("SUCCESS")
            .build();
        
        recyclingRepository.save(record);
    }
    
    /**
     * 记录重新分配
     */
    private void recordReallocation(NumberReallocationRequest request) {
        NumberReallocationRecord record = NumberReallocationRecord.builder()
            .msisdn(request.getMsisdn())
            .reallocatedBy(request.getReallocatedBy())
            .reallocationTime(LocalDateTime.now())
            .reason(request.getReason())
            .status("SUCCESS")
            .build();
        
        reallocationRepository.save(record);
    }
    
    /**
     * 检查状态是否可回收
     */
    private boolean isRecyclableStatus(NumberStatus status) {
        return Set.of(
            NumberStatus.INACTIVE,
            NumberStatus.SUSPENDED,
            NumberStatus.CANCELLED
        ).contains(status);
    }
    
    /**
     * 检查回收冷却期是否已过
     */
    private boolean hasRecyclingCoolingPeriodPassed(MsisdnPool pool) {
        if (pool.getLastUsageTime() == null) {
            return true;
        }
        
        LocalDateTime coolingPeriodEnd = pool.getLastUsageTime().plusMonths(6); // 6个月冷却期
        return LocalDateTime.now().isAfter(coolingPeriodEnd);
    }
    
    private boolean hasOutstandingCharges(String msisdn) {
        // 调用计费系统检查未结清费用
        return false;
    }
    
    private String maskMsisdn(String msisdn) {
        if (StringUtils.isEmpty(msisdn) || msisdn.length() < 7) {
            return msisdn;
        }
        return msisdn.substring(0, 3) + "****" + msisdn.substring(7);
    }
}
```

## 数据模型设计

### 核心实体类
```java
/**
 * MSISDN池实体
 */
@Entity
@Table(name = "msisdn_pool")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MsisdnPool {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "msisdn", unique = true, nullable = false, length = 15)
    private String msisdn;
    
    @Column(name = "operator_code", nullable = false, length = 10)
    private String operatorCode;
    
    @Column(name = "region", nullable = false, length = 10)
    private String region;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "number_type", nullable = false)
    private NumberType numberType;
    
    @Column(name = "golden_score")
    private Integer goldenScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NumberStatus status;
    
    @Column(name = "allocated_time")
    private LocalDateTime allocatedTime;
    
    @Column(name = "last_usage_time")
    private LocalDateTime lastUsageTime;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

/**
 * IMSI-MSISDN绑定关系
 */
@Entity
@Table(name = "imsi_msisdn_binding")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImsiMsisdnBinding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "imsi", nullable = false, length = 15)
    private String imsi;
    
    @Column(name = "msisdn", nullable = false, length = 15)
    private String msisdn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BindingStatus status;
    
    @Column(name = "binding_time", nullable = false)
    private LocalDateTime bindingTime;
    
    @Column(name = "unbinding_time")
    private LocalDateTime unbindingTime;
}

/**
 * 号码特征分析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberCharacteristics {
    
    private NumberType numberType;
    private Integer goldenScore;
    private Integer consecutiveCount;
    private Integer repeatCount;
    private Boolean hasSpecialPattern;
    private Integer luckyScore;
}
```

### 枚举定义
```java
/**
 * 号码类型枚举
 */
public enum NumberType {
    GOLDEN("靓号"),
    SILVER("银号"),
    REGULAR("普通号");
    
    private final String description;
    
    NumberType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * 号码状态枚举
 */
public enum NumberStatus {
    AVAILABLE("可用"),
    ALLOCATED("已分配"),
    ACTIVE("激活"),
    INACTIVE("未激活"),
    SUSPENDED("暂停"),
    CANCELLED("注销"),
    RECYCLED("已回收"),
    PORTED("已携带");
    
    private final String description;
    
    NumberStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * 绑定状态枚举
 */
public enum BindingStatus {
    ACTIVE("激活"),
    INACTIVE("未激活");
    
    private final String description;
    
    BindingStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

## 号码携带管理

### 号码携带服务实现
```java
/**
 * 号码携带服务
 */
@Service
@Slf4j
public class NumberPortabilityService {
    
    @Autowired
    private PortabilityRepository portabilityRepository;
    
    @Autowired
    private NpdbIntegrationService npdbIntegrationService;
    
    /**
     * 执行号码携带
     */
    @Transactional
    public void executePortability(NumberPortabilityRequest request) {
        log.info("Executing number portability: {} from {} to {}", 
            maskMsisdn(request.getMsisdn()), 
            request.getFromOperator(), 
            request.getToOperator());
        
        try {
            // 1. 创建携带记录
            createPortabilityRecord(request);
            
            // 2. 更新NPDB
            updateNpdb(request);
            
            // 3. 通知相关系统
            notifyPortabilityCompletion(request);
            
            log.info("Number portability executed successfully: {}", 
                maskMsisdn(request.getMsisdn()));
            
        } catch (Exception e) {
            log.error("Number portability execution failed: {}", 
                maskMsisdn(request.getMsisdn()), e);
            throw new PortabilityExecutionException("Number portability execution failed", e);
        }
    }
    
    /**
     * 查询号码归属
     */
    public NumberOwnershipInfo queryNumberOwnership(String msisdn) {
        try {
            // 查询NPDB
            NpdbQueryResult result = npdbIntegrationService.queryNumber(msisdn);
            
            return NumberOwnershipInfo.builder()
                .msisdn(msisdn)
                .currentOperator(result.getCurrentOperator())
                .originalOperator(result.getOriginalOperator())
                .isPorted(result.isPorted())
                .portingDate(result.getPortingDate())
                .routingNumber(result.getRoutingNumber())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to query number ownership: {}", maskMsisdn(msisdn), e);
            throw new NumberOwnershipQueryException("Failed to query number ownership", e);
        }
    }
    
    private void createPortabilityRecord(NumberPortabilityRequest request) {
        NumberPortabilityRecord record = NumberPortabilityRecord.builder()
            .msisdn(request.getMsisdn())
            .fromOperator(request.getFromOperator())
            .toOperator(request.getToOperator())
            .requestTime(LocalDateTime.now())
            .status(PortabilityStatus.IN_PROGRESS)
            .build();
        
        portabilityRepository.save(record);
    }
    
    private void updateNpdb(NumberPortabilityRequest request) {
        NpdbUpdateRequest updateRequest = NpdbUpdateRequest.builder()
            .msisdn(request.getMsisdn())
            .newOperator(request.getToOperator())
            .routingNumber(generateRoutingNumber(request.getMsisdn()))
            .effectiveTime(LocalDateTime.now())
            .build();
        
        npdbIntegrationService.updateNumberPortability(updateRequest);
    }
    
    private void notifyPortabilityCompletion(NumberPortabilityRequest request) {
        // 通知相关系统号码携带完成
        // 这里可以发送消息到消息队列或调用其他服务
    }
    
    private String generateRoutingNumber(String msisdn) {
        // 生成路由号码
        return "86" + msisdn.substring(2);
    }
    
    private String maskMsisdn(String msisdn) {
        if (StringUtils.isEmpty(msisdn) || msisdn.length() < 7) {
            return msisdn;
        }
        return msisdn.substring(0, 3) + "****" + msisdn.substring(7);
    }
}
```

## 号码质量评估

### 靓号评分算法
```java
/**
 * 号码质量评估服务
 */
@Service
@Slf4j
public class NumberQualityService {
    
    /**
     * 评估号码质量
     */
    public NumberQualityScore evaluateNumberQuality(String msisdn) {
        log.debug("Evaluating number quality: {}", maskMsisdn(msisdn));
        
        // 提取后8位进行评估
        String suffix = msisdn.substring(msisdn.length() - 8);
        
        NumberQualityScore.NumberQualityScoreBuilder scoreBuilder = 
            NumberQualityScore.builder().msisdn(msisdn);
        
        int totalScore = 0;
        
        // 1. 连号评分
        int consecutiveScore = evaluateConsecutiveDigits(suffix);
        totalScore += consecutiveScore;
        scoreBuilder.consecutiveScore(consecutiveScore);
        
        // 2. 重复数字评分
        int repeatScore = evaluateRepeatDigits(suffix);
        totalScore += repeatScore;
        scoreBuilder.repeatScore(repeatScore);
        
        // 3. 特殊模式评分
        int patternScore = evaluateSpecialPatterns(suffix);
        totalScore += patternScore;
        scoreBuilder.patternScore(patternScore);
        
        // 4. 吉利数字评分
        int luckyScore = evaluateLuckyDigits(suffix);
        totalScore += luckyScore;
        scoreBuilder.luckyScore(luckyScore);
        
        // 5. 易记性评分
        int memoryScore = evaluateMemorability(suffix);
        totalScore += memoryScore;
        scoreBuilder.memoryScore(memoryScore);
        
        // 6. 商业价值评分
        int commercialScore = evaluateCommercialValue(suffix);
        totalScore += commercialScore;
        scoreBuilder.commercialScore(commercialScore);
        
        // 确定号码等级
        NumberGrade grade = determineNumberGrade(totalScore);
        
        return scoreBuilder
            .totalScore(totalScore)
            .grade(grade)
            .evaluationTime(LocalDateTime.now())
            .build();
    }
    
    /**
     * 评估连号
     */
    private int evaluateConsecutiveDigits(String number) {
        int maxConsecutive = getMaxConsecutiveLength(number);
        
        switch (maxConsecutive) {
            case 8: return 100; // 全连号
            case 7: return 80;
            case 6: return 60;
            case 5: return 40;
            case 4: return 25;
            case 3: return 10;
            default: return 0;
        }
    }
    
    /**
     * 评估重复数字
     */
    private int evaluateRepeatDigits(String number) {
        Map<Character, Integer> digitCount = new HashMap<>();
        for (char c : number.toCharArray()) {
            digitCount.put(c, digitCount.getOrDefault(c, 0) + 1);
        }
        
        int maxRepeat = digitCount.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        
        switch (maxRepeat) {
            case 8: return 90; // 全相同
            case 7: return 70;
            case 6: return 50;
            case 5: return 35;
            case 4: return 20;
            case 3: return 8;
            default: return 0;
        }
    }
    
    /**
     * 评估特殊模式
     */
    private int evaluateSpecialPatterns(String number) {
        int score = 0;
        
        // AABB模式
        if (hasAABBPattern(number)) {
            score += 15;
        }
        
        // ABAB模式
        if (hasABABPattern(number)) {
            score += 20;
        }
        
        // ABCD模式（递增递减）
        if (hasSequentialPattern(number)) {
            score += 25;
        }
        
        // 回文模式
        if (isPalindrome(number)) {
            score += 30;
        }
        
        // 对称模式
        if (hasSymmetricPattern(number)) {
            score += 18;
        }
        
        return score;
    }
    
    /**
     * 评估吉利数字
     */
    private int evaluateLuckyDigits(String number) {
        int score = 0;
        
        // 统计各数字出现次数
        long count0 = number.chars().filter(ch -> ch == '0').count();
        long count1 = number.chars().filter(ch -> ch == '1').count();
        long count2 = number.chars().filter(ch -> ch == '2').count();
        long count3 = number.chars().filter(ch -> ch == '3').count();
        long count4 = number.chars().filter(ch -> ch == '4').count();
        long count5 = number.chars().filter(ch -> ch == '5').count();
        long count6 = number.chars().filter(ch -> ch == '6').count();
        long count7 = number.chars().filter(ch -> ch == '7').count();
        long count8 = number.chars().filter(ch -> ch == '8').count();
        long count9 = number.chars().filter(ch -> ch == '9').count();
        
        // 吉利数字加分
        score += count6 * 3; // 6代表顺利
        score += count8 * 5; // 8代表发财
        score += count9 * 3; // 9代表长久
        score += count1 * 2; // 1代表第一
        score += count5 * 2; // 5代表我
        
        // 不吉利数字扣分
        score -= count4 * 3; // 4代表死
        score -= count7 * 1; // 7在某些地区不吉利
        
        // 特殊组合
        if (number.contains("168")) score += 10; // 一路发
        if (number.contains("888")) score += 15; // 发发发
        if (number.contains("666")) score += 12; // 六六六
        if (number.contains("999")) score += 10; // 久久久
        if (number.contains("520")) score += 8;  // 我爱你
        if (number.contains("1314")) score += 8; // 一生一世
        
        return Math.max(0, score);
    }
    
    /**
     * 评估易记性
     */
    private int evaluateMemorability(String number) {
        int score = 0;
        
        // 数字变化少的更容易记忆
        Set<Character> uniqueDigits = new HashSet<>();
        for (char c : number.toCharArray()) {
            uniqueDigits.add(c);
        }
        
        int uniqueCount = uniqueDigits.size();
        switch (uniqueCount) {
            case 1: score += 20; // 全相同
            case 2: score += 15; // 只有两种数字
            case 3: score += 10; // 三种数字
            case 4: score += 5;  // 四种数字
            default: score += 0;
        }
        
        // 规律性加分
        if (hasRhythm(number)) {
            score += 8;
        }
        
        return score;
    }
    
    /**
     * 评估商业价值
     */
    private int evaluateCommercialValue(String number) {
        int score = 0;
        
        // 商业相关数字组合
        if (number.contains("888")) score += 20; // 发财
        if (number.contains("168")) score += 15; // 一路发
        if (number.contains("518")) score += 10; // 我要发
        if (number.contains("678")) score += 8;  // 顺发
        if (number.contains("789")) score += 8;  // 顺序
        
        // 尾号价值
        char lastDigit = number.charAt(number.length() - 1);
        switch (lastDigit) {
            case '8': score += 10; break;
            case '6': score += 8; break;
            case '9': score += 6; break;
            case '0': score += 4; break;
            case '4': score -= 5; break;
        }
        
        return score;
    }
    
    /**
     * 确定号码等级
     */
    private NumberGrade determineNumberGrade(int totalScore) {
        if (totalScore >= 200) {
            return NumberGrade.DIAMOND; // 钻石级
        } else if (totalScore >= 150) {
            return NumberGrade.PLATINUM; // 白金级
        } else if (totalScore >= 100) {
            return NumberGrade.GOLD; // 黄金级
        } else if (totalScore >= 60) {
            return NumberGrade.SILVER; // 白银级
        } else if (totalScore >= 30) {
            return NumberGrade.BRONZE; // 青铜级
        } else {
            return NumberGrade.REGULAR; // 普通级
        }
    }
    
    // 辅助方法实现
    private int getMaxConsecutiveLength(String number) {
        int maxLength = 1;
        int currentLength = 1;
        
        for (int i = 1; i < number.length(); i++) {
            int current = Character.getNumericValue(number.charAt(i));
            int previous = Character.getNumericValue(number.charAt(i - 1));
            
            if (Math.abs(current - previous) == 1) {
                currentLength++;
                maxLength = Math.max(maxLength, currentLength);
            } else {
                currentLength = 1;
            }
        }
        
        return maxLength;
    }
    
    private boolean hasAABBPattern(String number) {
        for (int i = 0; i < number.length() - 3; i++) {
            if (number.charAt(i) == number.charAt(i + 1) && 
                number.charAt(i + 2) == number.charAt(i + 3) &&
                number.charAt(i) != number.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasABABPattern(String number) {
        for (int i = 0; i < number.length() - 3; i++) {
            if (number.charAt(i) == number.charAt(i + 2) && 
                number.charAt(i + 1) == number.charAt(i + 3) &&
                number.charAt(i) != number.charAt(i + 1)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasSequentialPattern(String number) {
        // 检查递增或递减序列
        boolean ascending = true;
        boolean descending = true;
        
        for (int i = 1; i < number.length(); i++) {
            int current = Character.getNumericValue(number.charAt(i));
            int previous = Character.getNumericValue(number.charAt(i - 1));
            
            if (current != previous + 1) {
                ascending = false;
            }
            if (current != previous - 1) {
                descending = false;
            }
        }
        
        return ascending || descending;
    }
    
    private boolean isPalindrome(String number) {
        int left = 0;
        int right = number.length() - 1;
        
        while (left < right) {
            if (number.charAt(left) != number.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        
        return true;
    }
    
    private boolean hasSymmetricPattern(String number) {
        // 检查对称模式，如1234321
        int len = number.length();
        int mid = len / 2;
        
        for (int i = 0; i < mid; i++) {
            if (number.charAt(i) != number.charAt(len - 1 - i)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean hasRhythm(String number) {
        // 检查是否有节奏感，如12121212
        if (number.length() < 4) return false;
        
        String pattern = number.substring(0, 2);
        for (int i = 2; i < number.length(); i += 2) {
            if (i + 1 < number.length()) {
                String current = number.substring(i, i + 2);
                if (!pattern.equals(current)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private String maskMsisdn(String msisdn) {
        if (StringUtils.isEmpty(msisdn) || msisdn.length() < 7) {
            return msisdn;
        }
        return msisdn.substring(0, 3) + "****" + msisdn.substring(7);
    }
}
```

## 最佳实践总结

### 1. 安全管理
- **数据脱敏**: 在日志和界面中对MSISDN进行脱敏处理
- **访问控制**: 严格控制号码资源的访问和操作权限
- **审计追踪**: 记录所有号码操作的详细审计日志
- **加密存储**: 对敏感的号码数据进行加密存储

### 2. 性能优化
- **索引优化**: 在MSISDN字段上建立合适的索引
- **分库分表**: 根据号码段进行分库分表
- **缓存策略**: 对热点号码数据进行缓存
- **批量处理**: 支持号码的批量分配和回收

### 3. 业务规范
- **号码分级**: 建立完善的号码分级体系
- **回收策略**: 制定合理的号码回收和重用策略
- **携带管理**: 规范号码携带流程和数据同步
- **质量评估**: 建立科学的号码质量评估体系

### 4. 监控告警
- **资源监控**: 监控号码池的容量和使用率
- **异常检测**: 检测异常的号码使用模式
- **性能监控**: 监控号码相关操作的性能指标
- **业务监控**: 监控号码携带和激活的成功率

通过以上MSISDN管理的详细实现，可以确保NSRS号卡资源管理系统在号码资源管理方面达到电信行业的专业水准，满足复杂的业务需求和监管要求。