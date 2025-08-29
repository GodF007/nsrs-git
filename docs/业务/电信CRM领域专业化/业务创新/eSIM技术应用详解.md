# eSIM技术应用详解

## 概述

eSIM（Embedded SIM）是一种嵌入式SIM卡技术，它将传统的物理SIM卡功能集成到设备的芯片中，支持远程配置和管理。本文档详细阐述eSIM技术在NSRS号卡资源管理系统中的应用，包括eSIM配置文件管理、远程配置、多配置文件切换等核心功能。

## eSIM核心概念

### eSIM技术特性
- **远程配置**: 支持OTA（Over-The-Air）方式远程下载和配置
- **多配置文件**: 单个eSIM可存储多个运营商配置文件
- **动态切换**: 支持配置文件的动态启用和禁用
- **全球漫游**: 便于国际漫游和多运营商服务
- **安全可靠**: 基于PKI的端到端安全机制

### eSIM架构组件枚举
```java
/**
 * eSIM架构组件枚举
 */
public enum ESIMComponent {
    EID("eUICC标识符", "Embedded UICC Identifier", "唯一标识eSIM芯片"),
    SM_DP_PLUS("订阅管理数据准备+", "Subscription Manager Data Preparation+", "配置文件生成和加密"),
    SM_DS("订阅管理发现服务", "Subscription Manager Discovery Service", "发现和路由服务"),
    LPA("本地配置文件助手", "Local Profile Assistant", "设备端配置文件管理"),
    EUICC("嵌入式UICC", "Embedded Universal Integrated Circuit Card", "eSIM安全芯片"),
    ISD_R("发行者安全域根", "Issuer Security Domain Root", "根安全域"),
    ECASD("eUICC控制权限安全域", "eUICC Controlling Authority Security Domain", "控制权限管理"),
    ISD_P("发行者安全域配置文件", "Issuer Security Domain Profile", "配置文件安全域");
    
    private final String chineseName;
    private final String englishName;
    private final String description;
    
    ESIMComponent(String chineseName, String englishName, String description) {
        this.chineseName = chineseName;
        this.englishName = englishName;
        this.description = description;
    }
    
    public String getChineseName() { return chineseName; }
    public String getEnglishName() { return englishName; }
    public String getDescription() { return description; }
}

/**
 * eSIM配置文件状态枚举
 */
public enum ESIMProfileStatus {
    CREATED("已创建", "配置文件已生成，等待下载"),
    DOWNLOADED("已下载", "配置文件已下载到设备"),
    INSTALLED("已安装", "配置文件已安装到eSIM"),
    ENABLED("已启用", "配置文件当前处于激活状态"),
    DISABLED("已禁用", "配置文件已禁用但仍保留"),
    DELETED("已删除", "配置文件已从eSIM中删除"),
    ERROR("错误", "配置文件处理过程中出现错误");
    
    private final String chineseName;
    private final String description;
    
    ESIMProfileStatus(String chineseName, String description) {
        this.chineseName = chineseName;
        this.description = description;
    }
    
    public String getChineseName() { return chineseName; }
    public String getDescription() { return description; }
}

/**
 * eSIM应用场景枚举
 */
public enum ESIMApplicationScenario {
    CONSUMER_DEVICE("消费电子设备", "智能手机、平板电脑、智能手表"),
    IOT_DEVICE("物联网设备", "传感器、智能家居、工业设备"),
    AUTOMOTIVE("车联网", "车载通信、车辆追踪"),
    WEARABLE("可穿戴设备", "智能手表、健康监测设备"),
    LAPTOP("笔记本电脑", "移动办公设备"),
    INDUSTRIAL("工业应用", "工业自动化、远程监控"),
    GLOBAL_ROAMING("全球漫游", "国际旅行、跨境业务");
    
    private final String chineseName;
    private final String description;
    
    ESIMApplicationScenario(String chineseName, String description) {
        this.chineseName = chineseName;
        this.description = description;
    }
    
    public String getChineseName() { return chineseName; }
    public String getDescription() { return description; }
}
```

## eSIM核心实体

### eSIM设备实体
```java
/**
 * eSIM设备实体
 */
@Entity
@Table(name = "esim_device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESIMDevice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deviceId;
    
    @Column(unique = true, nullable = false)
    private String eid; // eUICC标识符
    
    @Column(nullable = false)
    private String deviceModel; // 设备型号
    
    @Column(nullable = false)
    private String manufacturer; // 制造商
    
    @Enumerated(EnumType.STRING)
    private ESIMApplicationScenario applicationScenario; // 应用场景
    
    @Column(nullable = false)
    private String euiccVersion; // eUICC版本
    
    private String platformType; // 平台类型（Android、iOS等）
    private String osVersion; // 操作系统版本
    
    // 设备状态
    @Enumerated(EnumType.STRING)
    private ESIMDeviceStatus status; // 设备状态
    
    private String customerId; // 客户ID
    private String customerName; // 客户名称
    
    // 安全信息
    private String rootCertificate; // 根证书
    private String deviceCertificate; // 设备证书
    private String publicKey; // 公钥
    
    // 配置文件容量
    private Integer maxProfiles; // 最大配置文件数量
    private Integer currentProfileCount; // 当前配置文件数量
    
    // 时间信息
    private LocalDateTime registrationTime; // 注册时间
    private LocalDateTime lastContactTime; // 最后联系时间
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    
    // 位置信息
    private String lastKnownLocation; // 最后已知位置
    private String timeZone; // 时区
}

/**
 * eSIM配置文件实体
 */
@Entity
@Table(name = "esim_profile")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESIMProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;
    
    @Column(unique = true, nullable = false)
    private String iccid; // 配置文件ICCID
    
    @Column(nullable = false)
    private String eid; // 关联的eSIM设备EID
    
    @Column(unique = true, nullable = false)
    private String profileIdentifier; // 配置文件标识符
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ESIMProfileStatus status; // 配置文件状态
    
    // 运营商信息
    private String operatorId; // 运营商ID
    private String operatorName; // 运营商名称
    private String mcc; // 移动国家码
    private String mnc; // 移动网络码
    
    // 订阅信息
    private String imsi; // IMSI
    private String msisdn; // 手机号码
    private String ki; // 认证密钥
    private String opc; // 运营商变量
    
    // 配置文件元数据
    private String profileName; // 配置文件名称
    private String profileNickname; // 配置文件昵称
    private String serviceProviderName; // 服务提供商名称
    private String profileClass; // 配置文件类别
    
    // 策略规则
    private Boolean isDeletable; // 是否可删除
    private Boolean isDisableable; // 是否可禁用
    private String policyRules; // 策略规则JSON
    
    // 下载信息
    private String activationCode; // 激活码
    private String smDpPlusAddress; // SM-DP+服务器地址
    private String confirmationCode; // 确认码
    
    // 时间信息
    private LocalDateTime creationTime; // 创建时间
    private LocalDateTime downloadTime; // 下载时间
    private LocalDateTime installationTime; // 安装时间
    private LocalDateTime enableTime; // 启用时间
    private LocalDateTime disableTime; // 禁用时间
    private LocalDateTime deleteTime; // 删除时间
    private LocalDateTime expirationTime; // 过期时间
    
    // 使用统计
    private Long dataUsed; // 已使用流量
    private Integer connectionCount; // 连接次数
    private LocalDateTime lastUsedTime; // 最后使用时间
}

/**
 * eSIM设备状态枚举
 */
public enum ESIMDeviceStatus {
    REGISTERED("已注册", "设备已注册到系统"),
    ACTIVE("活跃", "设备正常工作"),
    INACTIVE("非活跃", "设备长时间未联系"),
    SUSPENDED("暂停", "设备被暂停服务"),
    BLOCKED("阻止", "设备被阻止访问"),
    LOST("丢失", "设备丢失或被盗");
    
    private final String chineseName;
    private final String description;
    
    ESIMDeviceStatus(String chineseName, String description) {
        this.chineseName = chineseName;
        this.description = description;
    }
    
    public String getChineseName() { return chineseName; }
    public String getDescription() { return description; }
}
```

## eSIM管理服务

### 核心管理服务
```java
/**
 * eSIM管理服务
 */
@Service
@Slf4j
public class ESIMManagementService {
    
    @Autowired
    private ESIMDeviceRepository deviceRepository;
    
    @Autowired
    private ESIMProfileRepository profileRepository;
    
    @Autowired
    private SMDPPlusService smDpPlusService;
    
    @Autowired
    private CertificateService certificateService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 注册eSIM设备
     */
    @Transactional
    public ESIMDevice registerDevice(ESIMDeviceRegistrationRequest request) {
        log.info("Registering eSIM device with EID: {}", request.getEid());
        
        // 验证EID格式
        validateEID(request.getEid());
        
        // 检查设备是否已注册
        if (deviceRepository.existsByEid(request.getEid())) {
            throw new DeviceAlreadyRegisteredException(
                "Device already registered: " + request.getEid());
        }
        
        // 验证设备证书
        validateDeviceCertificate(request.getDeviceCertificate());
        
        ESIMDevice device = ESIMDevice.builder()
            .eid(request.getEid())
            .deviceModel(request.getDeviceModel())
            .manufacturer(request.getManufacturer())
            .applicationScenario(request.getApplicationScenario())
            .euiccVersion(request.getEuiccVersion())
            .platformType(request.getPlatformType())
            .osVersion(request.getOsVersion())
            .status(ESIMDeviceStatus.REGISTERED)
            .customerId(request.getCustomerId())
            .customerName(request.getCustomerName())
            .deviceCertificate(request.getDeviceCertificate())
            .publicKey(request.getPublicKey())
            .maxProfiles(request.getMaxProfiles())
            .currentProfileCount(0)
            .registrationTime(LocalDateTime.now())
            .lastContactTime(LocalDateTime.now())
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();
        
        ESIMDevice savedDevice = deviceRepository.save(device);
        
        // 生成根证书
        generateRootCertificate(savedDevice);
        
        log.info("eSIM device registered successfully: {}", request.getEid());
        return savedDevice;
    }
    
    /**
     * 创建eSIM配置文件
     */
    @Transactional
    public ESIMProfile createProfile(ESIMProfileCreationRequest request) {
        log.info("Creating eSIM profile for EID: {}, operator: {}", 
            request.getEid(), request.getOperatorId());
        
        // 验证设备存在且可用
        ESIMDevice device = deviceRepository.findByEid(request.getEid())
            .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + request.getEid()));
        
        if (device.getStatus() != ESIMDeviceStatus.REGISTERED && 
            device.getStatus() != ESIMDeviceStatus.ACTIVE) {
            throw new InvalidDeviceStatusException(
                "Device not available for profile creation: " + device.getStatus());
        }
        
        // 检查配置文件容量
        if (device.getCurrentProfileCount() >= device.getMaxProfiles()) {
            throw new ProfileCapacityExceededException(
                "Device profile capacity exceeded: " + device.getCurrentProfileCount());
        }
        
        // 生成配置文件标识符
        String profileIdentifier = generateProfileIdentifier();
        String iccid = generateICCID(request.getOperatorId());
        
        ESIMProfile profile = ESIMProfile.builder()
            .iccid(iccid)
            .eid(request.getEid())
            .profileIdentifier(profileIdentifier)
            .status(ESIMProfileStatus.CREATED)
            .operatorId(request.getOperatorId())
            .operatorName(request.getOperatorName())
            .mcc(request.getMcc())
            .mnc(request.getMnc())
            .imsi(generateIMSI(request.getMcc(), request.getMnc()))
            .msisdn(request.getMsisdn())
            .profileName(request.getProfileName())
            .profileNickname(request.getProfileNickname())
            .serviceProviderName(request.getServiceProviderName())
            .profileClass(request.getProfileClass())
            .isDeletable(request.getIsDeletable())
            .isDisableable(request.getIsDisableable())
            .policyRules(request.getPolicyRules())
            .creationTime(LocalDateTime.now())
            .expirationTime(request.getExpirationTime())
            .dataUsed(0L)
            .connectionCount(0)
            .build();
        
        // 生成安全密钥
        generateSecurityKeys(profile);
        
        // 生成激活码
        generateActivationCode(profile);
        
        ESIMProfile savedProfile = profileRepository.save(profile);
        
        // 更新设备配置文件计数
        device.setCurrentProfileCount(device.getCurrentProfileCount() + 1);
        device.setUpdateTime(LocalDateTime.now());
        deviceRepository.save(device);
        
        // 通知SM-DP+服务器
        notifyProfileCreation(savedProfile);
        
        log.info("eSIM profile created successfully: {}", profileIdentifier);
        return savedProfile;
    }
    
    /**
     * 下载配置文件到设备
     */
    @Transactional
    public ESIMProfile downloadProfile(String activationCode, String confirmationCode) {
        log.info("Downloading eSIM profile with activation code: {}", activationCode);
        
        ESIMProfile profile = profileRepository.findByActivationCode(activationCode)
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found for activation code: " + activationCode));
        
        // 验证确认码
        if (profile.getConfirmationCode() != null && 
            !profile.getConfirmationCode().equals(confirmationCode)) {
            throw new InvalidConfirmationCodeException("Invalid confirmation code");
        }
        
        // 验证配置文件状态
        if (profile.getStatus() != ESIMProfileStatus.CREATED) {
            throw new InvalidProfileStatusException(
                "Profile cannot be downloaded in current status: " + profile.getStatus());
        }
        
        // 执行下载流程
        try {
            // 调用SM-DP+服务进行配置文件下载
            smDpPlusService.downloadProfile(profile);
            
            // 更新配置文件状态
            profile.setStatus(ESIMProfileStatus.DOWNLOADED);
            profile.setDownloadTime(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to download profile: {}", profile.getProfileIdentifier(), e);
            profile.setStatus(ESIMProfileStatus.ERROR);
            throw new ProfileDownloadException("Profile download failed", e);
        }
        
        ESIMProfile savedProfile = profileRepository.save(profile);
        
        // 发送下载完成通知
        notificationService.sendProfileDownloadNotification(savedProfile);
        
        log.info("eSIM profile downloaded successfully: {}", profile.getProfileIdentifier());
        return savedProfile;
    }
    
    /**
     * 安装配置文件
     */
    @Transactional
    public ESIMProfile installProfile(String profileIdentifier) {
        log.info("Installing eSIM profile: {}", profileIdentifier);
        
        ESIMProfile profile = profileRepository.findByProfileIdentifier(profileIdentifier)
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found: " + profileIdentifier));
        
        if (profile.getStatus() != ESIMProfileStatus.DOWNLOADED) {
            throw new InvalidProfileStatusException(
                "Profile cannot be installed in current status: " + profile.getStatus());
        }
        
        try {
            // 执行安装流程
            smDpPlusService.installProfile(profile);
            
            // 更新配置文件状态
            profile.setStatus(ESIMProfileStatus.INSTALLED);
            profile.setInstallationTime(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to install profile: {}", profileIdentifier, e);
            profile.setStatus(ESIMProfileStatus.ERROR);
            throw new ProfileInstallationException("Profile installation failed", e);
        }
        
        ESIMProfile savedProfile = profileRepository.save(profile);
        
        // 发送安装完成通知
        notificationService.sendProfileInstallationNotification(savedProfile);
        
        log.info("eSIM profile installed successfully: {}", profileIdentifier);
        return savedProfile;
    }
    
    /**
     * 启用配置文件
     */
    @Transactional
    public ESIMProfile enableProfile(String profileIdentifier) {
        log.info("Enabling eSIM profile: {}", profileIdentifier);
        
        ESIMProfile profile = profileRepository.findByProfileIdentifier(profileIdentifier)
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found: " + profileIdentifier));
        
        if (profile.getStatus() != ESIMProfileStatus.INSTALLED && 
            profile.getStatus() != ESIMProfileStatus.DISABLED) {
            throw new InvalidProfileStatusException(
                "Profile cannot be enabled in current status: " + profile.getStatus());
        }
        
        // 禁用同一设备上的其他活跃配置文件
        disableOtherActiveProfiles(profile.getEid(), profileIdentifier);
        
        try {
            // 执行启用流程
            smDpPlusService.enableProfile(profile);
            
            // 更新配置文件状态
            profile.setStatus(ESIMProfileStatus.ENABLED);
            profile.setEnableTime(LocalDateTime.now());
            profile.setLastUsedTime(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to enable profile: {}", profileIdentifier, e);
            profile.setStatus(ESIMProfileStatus.ERROR);
            throw new ProfileEnableException("Profile enable failed", e);
        }
        
        ESIMProfile savedProfile = profileRepository.save(profile);
        
        // 更新设备状态
        updateDeviceStatus(profile.getEid(), ESIMDeviceStatus.ACTIVE);
        
        // 发送启用完成通知
        notificationService.sendProfileEnableNotification(savedProfile);
        
        log.info("eSIM profile enabled successfully: {}", profileIdentifier);
        return savedProfile;
    }
    
    /**
     * 禁用配置文件
     */
    @Transactional
    public ESIMProfile disableProfile(String profileIdentifier) {
        log.info("Disabling eSIM profile: {}", profileIdentifier);
        
        ESIMProfile profile = profileRepository.findByProfileIdentifier(profileIdentifier)
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found: " + profileIdentifier));
        
        if (profile.getStatus() != ESIMProfileStatus.ENABLED) {
            throw new InvalidProfileStatusException(
                "Profile cannot be disabled in current status: " + profile.getStatus());
        }
        
        // 检查是否可禁用
        if (!profile.getIsDisableable()) {
            throw new ProfileNotDisableableException(
                "Profile is not disableable: " + profileIdentifier);
        }
        
        try {
            // 执行禁用流程
            smDpPlusService.disableProfile(profile);
            
            // 更新配置文件状态
            profile.setStatus(ESIMProfileStatus.DISABLED);
            profile.setDisableTime(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to disable profile: {}", profileIdentifier, e);
            profile.setStatus(ESIMProfileStatus.ERROR);
            throw new ProfileDisableException("Profile disable failed", e);
        }
        
        ESIMProfile savedProfile = profileRepository.save(profile);
        
        // 发送禁用完成通知
        notificationService.sendProfileDisableNotification(savedProfile);
        
        log.info("eSIM profile disabled successfully: {}", profileIdentifier);
        return savedProfile;
    }
    
    /**
     * 删除配置文件
     */
    @Transactional
    public void deleteProfile(String profileIdentifier) {
        log.info("Deleting eSIM profile: {}", profileIdentifier);
        
        ESIMProfile profile = profileRepository.findByProfileIdentifier(profileIdentifier)
            .orElseThrow(() -> new ProfileNotFoundException(
                "Profile not found: " + profileIdentifier));
        
        // 检查是否可删除
        if (!profile.getIsDeletable()) {
            throw new ProfileNotDeletableException(
                "Profile is not deletable: " + profileIdentifier);
        }
        
        // 如果配置文件处于启用状态，先禁用
        if (profile.getStatus() == ESIMProfileStatus.ENABLED) {
            disableProfile(profileIdentifier);
        }
        
        try {
            // 执行删除流程
            smDpPlusService.deleteProfile(profile);
            
            // 更新配置文件状态
            profile.setStatus(ESIMProfileStatus.DELETED);
            profile.setDeleteTime(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to delete profile: {}", profileIdentifier, e);
            profile.setStatus(ESIMProfileStatus.ERROR);
            throw new ProfileDeleteException("Profile delete failed", e);
        }
        
        profileRepository.save(profile);
        
        // 更新设备配置文件计数
        ESIMDevice device = deviceRepository.findByEid(profile.getEid()).orElse(null);
        if (device != null) {
            device.setCurrentProfileCount(Math.max(0, device.getCurrentProfileCount() - 1));
            device.setUpdateTime(LocalDateTime.now());
            deviceRepository.save(device);
        }
        
        // 发送删除完成通知
        notificationService.sendProfileDeleteNotification(profile);
        
        log.info("eSIM profile deleted successfully: {}", profileIdentifier);
    }
    
    // 辅助方法
    private void validateEID(String eid) {
        if (eid == null || eid.length() != 32) {
            throw new InvalidEIDException("Invalid EID format: " + eid);
        }
    }
    
    private void validateDeviceCertificate(String certificate) {
        // 验证设备证书有效性
        if (!certificateService.validateCertificate(certificate)) {
            throw new InvalidCertificateException("Invalid device certificate");
        }
    }
    
    private void generateRootCertificate(ESIMDevice device) {
        String rootCert = certificateService.generateRootCertificate(device.getEid());
        device.setRootCertificate(rootCert);
        deviceRepository.save(device);
    }
    
    private String generateProfileIdentifier() {
        return "PROF_" + System.currentTimeMillis() + "_" + 
               String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
    
    private String generateICCID(String operatorId) {
        return "89860" + operatorId + System.currentTimeMillis() % 1000000000L;
    }
    
    private String generateIMSI(String mcc, String mnc) {
        return mcc + mnc + String.format("%010d", ThreadLocalRandom.current().nextLong(10000000000L));
    }
    
    private void generateSecurityKeys(ESIMProfile profile) {
        // 生成Ki和OPc
        profile.setKi(generateRandomKey(128));
        profile.setOpc(generateRandomKey(128));
    }
    
    private void generateActivationCode(ESIMProfile profile) {
        String activationCode = "1$" + profile.getSmDpPlusAddress() + "$" + 
                               profile.getProfileIdentifier();
        if (profile.getConfirmationCode() != null) {
            activationCode += "$" + profile.getConfirmationCode();
        }
        profile.setActivationCode(activationCode);
    }
    
    private String generateRandomKey(int bits) {
        byte[] key = new byte[bits / 8];
        new SecureRandom().nextBytes(key);
        return bytesToHex(key);
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString().toUpperCase();
    }
    
    private void notifyProfileCreation(ESIMProfile profile) {
        // 通知SM-DP+服务器配置文件已创建
        smDpPlusService.notifyProfileCreated(profile);
    }
    
    private void disableOtherActiveProfiles(String eid, String currentProfileId) {
        List<ESIMProfile> activeProfiles = profileRepository
            .findByEidAndStatus(eid, ESIMProfileStatus.ENABLED);
        
        for (ESIMProfile profile : activeProfiles) {
            if (!profile.getProfileIdentifier().equals(currentProfileId)) {
                profile.setStatus(ESIMProfileStatus.DISABLED);
                profile.setDisableTime(LocalDateTime.now());
                profileRepository.save(profile);
            }
        }
    }
    
    private void updateDeviceStatus(String eid, ESIMDeviceStatus status) {
        deviceRepository.findByEid(eid).ifPresent(device -> {
            device.setStatus(status);
            device.setLastContactTime(LocalDateTime.now());
            device.setUpdateTime(LocalDateTime.now());
            deviceRepository.save(device);
        });
    }
}
```

## SM-DP+服务集成

### SM-DP+服务接口
```java
/**
 * SM-DP+服务接口
 */
@Service
@Slf4j
public class SMDPPlusService {
    
    @Value("${esim.smdp.plus.endpoint}")
    private String smDpPlusEndpoint;
    
    @Value("${esim.smdp.plus.certificate}")
    private String smDpPlusCertificate;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private CryptographyService cryptographyService;
    
    /**
     * 通知配置文件创建
     */
    public void notifyProfileCreated(ESIMProfile profile) {
        log.info("Notifying SM-DP+ about profile creation: {}", profile.getProfileIdentifier());
        
        try {
            SMDPProfileCreationRequest request = SMDPProfileCreationRequest.builder()
                .profileIdentifier(profile.getProfileIdentifier())
                .iccid(profile.getIccid())
                .eid(profile.getEid())
                .operatorId(profile.getOperatorId())
                .imsi(profile.getImsi())
                .ki(profile.getKi())
                .opc(profile.getOpc())
                .policyRules(profile.getPolicyRules())
                .build();
            
            // 加密请求数据
            String encryptedRequest = cryptographyService.encrypt(request, smDpPlusCertificate);
            
            // 发送到SM-DP+服务器
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Admin-Protocol", "gsma-rsp-v2.0");
            
            HttpEntity<String> entity = new HttpEntity<>(encryptedRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                smDpPlusEndpoint + "/profiles", entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Profile creation notification sent successfully");
            } else {
                log.error("Failed to notify SM-DP+ about profile creation: {}", 
                    response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error notifying SM-DP+ about profile creation", e);
            throw new SMDPCommunicationException("Failed to communicate with SM-DP+", e);
        }
    }
    
    /**
     * 下载配置文件
     */
    public void downloadProfile(ESIMProfile profile) {
        log.info("Initiating profile download via SM-DP+: {}", profile.getProfileIdentifier());
        
        try {
            SMDPDownloadRequest request = SMDPDownloadRequest.builder()
                .eid(profile.getEid())
                .profileIdentifier(profile.getProfileIdentifier())
                .activationCode(profile.getActivationCode())
                .build();
            
            // 发送下载请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Admin-Protocol", "gsma-rsp-v2.0");
            
            HttpEntity<SMDPDownloadRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SMDPDownloadResponse> response = restTemplate.postForEntity(
                smDpPlusEndpoint + "/download", entity, SMDPDownloadResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                SMDPDownloadResponse downloadResponse = response.getBody();
                if (downloadResponse != null && "SUCCESS".equals(downloadResponse.getStatus())) {
                    log.info("Profile download initiated successfully");
                } else {
                    throw new ProfileDownloadException(
                        "Profile download failed: " + downloadResponse.getErrorMessage());
                }
            } else {
                throw new ProfileDownloadException(
                    "SM-DP+ download request failed: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error downloading profile via SM-DP+", e);
            throw new ProfileDownloadException("Failed to download profile", e);
        }
    }
    
    /**
     * 安装配置文件
     */
    public void installProfile(ESIMProfile profile) {
        log.info("Installing profile via SM-DP+: {}", profile.getProfileIdentifier());
        
        try {
            SMDPInstallRequest request = SMDPInstallRequest.builder()
                .eid(profile.getEid())
                .profileIdentifier(profile.getProfileIdentifier())
                .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Admin-Protocol", "gsma-rsp-v2.0");
            
            HttpEntity<SMDPInstallRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SMDPInstallResponse> response = restTemplate.postForEntity(
                smDpPlusEndpoint + "/install", entity, SMDPInstallResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                SMDPInstallResponse installResponse = response.getBody();
                if (installResponse != null && "SUCCESS".equals(installResponse.getStatus())) {
                    log.info("Profile installation completed successfully");
                } else {
                    throw new ProfileInstallationException(
                        "Profile installation failed: " + installResponse.getErrorMessage());
                }
            } else {
                throw new ProfileInstallationException(
                    "SM-DP+ install request failed: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error installing profile via SM-DP+", e);
            throw new ProfileInstallationException("Failed to install profile", e);
        }
    }
    
    /**
     * 启用配置文件
     */
    public void enableProfile(ESIMProfile profile) {
        log.info("Enabling profile via SM-DP+: {}", profile.getProfileIdentifier());
        
        try {
            SMDPEnableRequest request = SMDPEnableRequest.builder()
                .eid(profile.getEid())
                .profileIdentifier(profile.getProfileIdentifier())
                .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Admin-Protocol", "gsma-rsp-v2.0");
            
            HttpEntity<SMDPEnableRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SMDPEnableResponse> response = restTemplate.postForEntity(
                smDpPlusEndpoint + "/enable", entity, SMDPEnableResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                SMDPEnableResponse enableResponse = response.getBody();
                if (enableResponse != null && "SUCCESS".equals(enableResponse.getStatus())) {
                    log.info("Profile enabled successfully");
                } else {
                    throw new ProfileEnableException(
                        "Profile enable failed: " + enableResponse.getErrorMessage());
                }
            } else {
                throw new ProfileEnableException(
                    "SM-DP+ enable request failed: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error enabling profile via SM-DP+", e);
            throw new ProfileEnableException("Failed to enable profile", e);
        }
    }
    
    /**
     * 禁用配置文件
     */
    public void disableProfile(ESIMProfile profile) {
        log.info("Disabling profile via SM-DP+: {}", profile.getProfileIdentifier());
        
        try {
            SMDPDisableRequest request = SMDPDisableRequest.builder()
                .eid(profile.getEid())
                .profileIdentifier(profile.getProfileIdentifier())
                .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Admin-Protocol", "gsma-rsp-v2.0");
            
            HttpEntity<SMDPDisableRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SMDPDisableResponse> response = restTemplate.postForEntity(
                smDpPlusEndpoint + "/disable", entity, SMDPDisableResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                SMDPDisableResponse disableResponse = response.getBody();
                if (disableResponse != null && "SUCCESS".equals(disableResponse.getStatus())) {
                    log.info("Profile disabled successfully");
                } else {
                    throw new ProfileDisableException(
                        "Profile disable failed: " + disableResponse.getErrorMessage());
                }
            } else {
                throw new ProfileDisableException(
                    "SM-DP+ disable request failed: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error disabling profile via SM-DP+", e);
            throw new ProfileDisableException("Failed to disable profile", e);
        }
    }
    
    /**
     * 删除配置文件
     */
    public void deleteProfile(ESIMProfile profile) {
        log.info("Deleting profile via SM-DP+: {}", profile.getProfileIdentifier());
        
        try {
            SMDPDeleteRequest request = SMDPDeleteRequest.builder()
                .eid(profile.getEid())
                .profileIdentifier(profile.getProfileIdentifier())
                .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Admin-Protocol", "gsma-rsp-v2.0");
            
            HttpEntity<SMDPDeleteRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SMDPDeleteResponse> response = restTemplate.exchange(
                smDpPlusEndpoint + "/profiles/" + profile.getProfileIdentifier(),
                HttpMethod.DELETE, entity, SMDPDeleteResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                SMDPDeleteResponse deleteResponse = response.getBody();
                if (deleteResponse != null && "SUCCESS".equals(deleteResponse.getStatus())) {
                    log.info("Profile deleted successfully");
                } else {
                    throw new ProfileDeleteException(
                        "Profile delete failed: " + deleteResponse.getErrorMessage());
                }
            } else {
                throw new ProfileDeleteException(
                    "SM-DP+ delete request failed: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error deleting profile via SM-DP+", e);
            throw new ProfileDeleteException("Failed to delete profile", e);
        }
    }
}
```

## Demo脚本示例

### eSIM管理Demo
```bash
#!/bin/bash
# eSIM管理Demo脚本

echo "=== eSIM管理Demo ==="

# 1. 注册eSIM设备
echo "1. 注册eSIM设备..."
curl -X POST http://localhost:8080/api/v1/esim/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "eid": "89049032000000000000000000000001",
    "deviceModel": "iPhone 14 Pro",
    "manufacturer": "Apple",
    "applicationScenario": "CONSUMER_DEVICE",
    "euiccVersion": "2.1",
    "platformType": "iOS",
    "osVersion": "16.0",
    "customerId": "CUSTOMER_001",
    "customerName": "张三",
    "deviceCertificate": "-----BEGIN CERTIFICATE-----\nMIIC...\n-----END CERTIFICATE-----",
    "publicKey": "-----BEGIN PUBLIC KEY-----\nMIIB...\n-----END PUBLIC KEY-----",
    "maxProfiles": 8
  }'

# 2. 创建eSIM配置文件
echo "\n2. 创建eSIM配置文件..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/create \
  -H "Content-Type: application/json" \
  -d '{
    "eid": "89049032000000000000000000000001",
    "operatorId": "CMCC",
    "operatorName": "中国移动",
    "mcc": "460",
    "mnc": "00",
    "msisdn": "13800138000",
    "profileName": "中国移动主卡",
    "profileNickname": "移动卡",
    "serviceProviderName": "中国移动通信集团",
    "profileClass": "operational",
    "isDeletable": true,
    "isDisableable": true,
    "policyRules": "{\"roaming\": true, \"dataLimit\": 10240}",
    "expirationTime": "2025-12-31T23:59:59"
  }'

# 3. 下载配置文件
echo "\n3. 下载配置文件..."
activation_code="1\$smdp.example.com\$PROF_1234567890_0001"
curl -X POST http://localhost:8080/api/v1/esim/profiles/download \
  -H "Content-Type: application/json" \
  -d "{
    \"activationCode\": \"${activation_code}\",
    \"confirmationCode\": \"123456\"
  }"

# 4. 安装配置文件
echo "\n4. 安装配置文件..."
profile_id="PROF_1234567890_0001"
curl -X POST http://localhost:8080/api/v1/esim/profiles/${profile_id}/install

# 5. 启用配置文件
echo "\n5. 启用配置文件..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/${profile_id}/enable

# 6. 查询设备配置文件列表
echo "\n6. 查询设备配置文件列表..."
eid="89049032000000000000000000000001"
curl -X GET "http://localhost:8080/api/v1/esim/devices/${eid}/profiles"

# 7. 创建第二个配置文件（联通）
echo "\n7. 创建第二个配置文件（联通）..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/create \
  -H "Content-Type: application/json" \
  -d '{
    "eid": "89049032000000000000000000000001",
    "operatorId": "CUCC",
    "operatorName": "中国联通",
    "mcc": "460",
    "mnc": "01",
    "msisdn": "13100131000",
    "profileName": "中国联通副卡",
    "profileNickname": "联通卡",
    "serviceProviderName": "中国联合网络通信集团",
    "profileClass": "operational",
    "isDeletable": true,
    "isDisableable": true,
    "policyRules": "{\"roaming\": false, \"dataLimit\": 5120}",
    "expirationTime": "2025-12-31T23:59:59"
  }'

# 8. 切换到联通配置文件
echo "\n8. 切换到联通配置文件..."
# 先禁用移动配置文件
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_1234567890_0001/disable
# 启用联通配置文件
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_1234567890_0002/enable

# 9. 查询配置文件使用统计
echo "\n9. 查询配置文件使用统计..."
curl -X GET "http://localhost:8080/api/v1/esim/profiles/PROF_1234567890_0002/statistics"

echo "\neSIM管理Demo完成！"
```

### 全球漫游场景Demo
```bash
#!/bin/bash
# eSIM全球漫游场景Demo脚本

echo "=== eSIM全球漫游场景Demo ==="

# 1. 注册支持全球漫游的eSIM设备
echo "1. 注册全球漫游eSIM设备..."
curl -X POST http://localhost:8080/api/v1/esim/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "eid": "89049032000000000000000000000002",
    "deviceModel": "Samsung Galaxy S23",
    "manufacturer": "Samsung",
    "applicationScenario": "GLOBAL_ROAMING",
    "euiccVersion": "2.1",
    "platformType": "Android",
    "osVersion": "13.0",
    "customerId": "ROAMING_CUSTOMER_001",
    "customerName": "商务旅行者",
    "deviceCertificate": "-----BEGIN CERTIFICATE-----\nMIIC...\n-----END CERTIFICATE-----",
    "publicKey": "-----BEGIN PUBLIC KEY-----\nMIIB...\n-----END PUBLIC KEY-----",
    "maxProfiles": 10
  }'

# 2. 创建国内主卡配置文件
echo "\n2. 创建国内主卡配置文件..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/create \
  -H "Content-Type: application/json" \
  -d '{
    "eid": "89049032000000000000000000000002",
    "operatorId": "CMCC",
    "operatorName": "中国移动",
    "mcc": "460",
    "mnc": "00",
    "msisdn": "13900139000",
    "profileName": "中国移动国内卡",
    "profileNickname": "国内主卡",
    "serviceProviderName": "中国移动通信集团",
    "profileClass": "operational",
    "isDeletable": false,
    "isDisableable": true,
    "policyRules": "{\"roaming\": true, \"dataLimit\": 20480, \"voiceEnabled\": true}",
    "expirationTime": "2025-12-31T23:59:59"
  }'

# 3. 创建美国漫游配置文件
echo "\n3. 创建美国漫游配置文件..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/create \
  -H "Content-Type: application/json" \
  -d '{
    "eid": "89049032000000000000000000000002",
    "operatorId": "VERIZON",
    "operatorName": "Verizon Wireless",
    "mcc": "311",
    "mnc": "480",
    "msisdn": "+12125551234",
    "profileName": "Verizon USA Roaming",
    "profileNickname": "美国卡",
    "serviceProviderName": "Verizon Wireless",
    "profileClass": "operational",
    "isDeletable": true,
    "isDisableable": true,
    "policyRules": "{\"roaming\": false, \"dataLimit\": 5120, \"validDays\": 30}",
    "expirationTime": "2024-03-31T23:59:59"
  }'

# 4. 创建欧洲漫游配置文件
echo "\n4. 创建欧洲漫游配置文件..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/create \
  -H "Content-Type: application/json" \
  -d '{
    "eid": "89049032000000000000000000000002",
    "operatorId": "VODAFONE",
    "operatorName": "Vodafone Europe",
    "mcc": "234",
    "mnc": "15",
    "msisdn": "+447700900123",
    "profileName": "Vodafone Europe Roaming",
    "profileNickname": "欧洲卡",
    "serviceProviderName": "Vodafone Group",
    "profileClass": "operational",
    "isDeletable": true,
    "isDisableable": true,
    "policyRules": "{\"roaming\": true, \"dataLimit\": 10240, \"validDays\": 15}",
    "expirationTime": "2024-02-29T23:59:59"
  }'

# 5. 模拟旅行场景 - 启用国内卡
echo "\n5. 启用国内主卡..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_DOMESTIC_001/enable

# 6. 模拟到达美国 - 切换到美国卡
echo "\n6. 到达美国，切换到美国漫游卡..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_DOMESTIC_001/disable
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_USA_001/enable

# 7. 模拟到达欧洲 - 切换到欧洲卡
echo "\n7. 到达欧洲，切换到欧洲漫游卡..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_USA_001/disable
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_EUROPE_001/enable

# 8. 查询漫游使用统计
echo "\n8. 查询漫游使用统计..."
curl -X GET "http://localhost:8080/api/v1/esim/devices/89049032000000000000000000000002/roaming-statistics"

# 9. 回国后切换回国内卡
echo "\n9. 回国后切换回国内主卡..."
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_EUROPE_001/disable
curl -X POST http://localhost:8080/api/v1/esim/profiles/PROF_DOMESTIC_001/enable

# 10. 删除临时漫游配置文件
echo "\n10. 删除过期的漫游配置文件..."
curl -X DELETE http://localhost:8080/api/v1/esim/profiles/PROF_EUROPE_001

echo "\neSIM全球漫游场景Demo完成！"
```

## 最佳实践总结

### 1. eSIM设备管理
- **设备注册**: 严格验证设备EID和证书
- **容量管理**: 合理规划配置文件存储容量
- **状态监控**: 实时监控设备连接状态
- **安全认证**: 建立完善的设备认证机制

### 2. 配置文件生命周期
- **创建管理**: 规范配置文件创建流程
- **下载控制**: 确保配置文件安全下载
- **安装验证**: 验证配置文件正确安装
- **状态切换**: 支持配置文件灵活切换

### 3. SM-DP+集成
- **协议遵循**: 严格遵循GSMA RSP规范
- **安全通信**: 确保与SM-DP+的安全通信
- **错误处理**: 建立完善的错误处理机制
- **性能优化**: 优化与SM-DP+的交互性能

### 4. 安全保障措施
- **端到端加密**: 确保配置文件传输安全
- **证书管理**: 建立完整的证书管理体系
- **访问控制**: 实施严格的访问控制策略
- **审计追踪**: 记录所有关键操作的审计日志

### 5. 用户体验优化
- **简化流程**: 简化配置文件下载和安装流程
- **智能切换**: 支持基于位置的智能配置文件切换
- **状态提示**: 提供清晰的配置文件状态提示
- **故障恢复**: 建立快速的故障恢复机制

通过实施以上eSIM技术应用方案，NSRS号卡资源管理系统能够为用户提供灵活、安全、便捷的eSIM服务，推动移动通信技术的创新发展。