# 3GPP标准详解

## 概述

3GPP（3rd Generation Partnership Project）是制定全球移动通信标准的国际标准化组织，负责制定从3G到5G及未来移动通信技术的技术规范。本文档详细阐述3GPP标准体系及其在NSRS号卡资源管理系统中的应用实践。

## 3GPP组织架构

### 组织成员
- **ARIB** (日本): Association of Radio Industries and Businesses
- **ATIS** (北美): Alliance for Telecommunications Industry Solutions
- **CCSA** (中国): China Communications Standards Association
- **ETSI** (欧洲): European Telecommunications Standards Institute
- **TSDSI** (印度): Telecommunications Standards Development Society, India
- **TTA** (韩国): Telecommunications Technology Association
- **TTC** (日本): Telecommunication Technology Committee

### 工作组结构
- **TSG RAN** (Radio Access Network): 无线接入网
- **TSG SA** (Service and System Aspects): 服务与系统
- **TSG CT** (Core Network and Terminals): 核心网与终端

## 3GPP标准体系

### Release版本演进
```java
/**
 * 3GPP Release版本枚举
 */
public enum ThreeGppRelease {
    REL_99("Release 99", "3G UMTS基础版本", 1999),
    REL_4("Release 4", "增强3G功能", 2001),
    REL_5("Release 5", "IMS引入", 2002),
    REL_6("Release 6", "HSPA", 2004),
    REL_7("Release 7", "HSPA+", 2007),
    REL_8("Release 8", "LTE", 2008),
    REL_9("Release 9", "LTE增强", 2009),
    REL_10("Release 10", "LTE-Advanced", 2011),
    REL_11("Release 11", "LTE-A增强", 2012),
    REL_12("Release 12", "载波聚合增强", 2014),
    REL_13("Release 13", "LTE-A Pro", 2015),
    REL_14("Release 14", "5G NR基础", 2017),
    REL_15("Release 15", "5G Phase 1", 2018),
    REL_16("Release 16", "5G Phase 2", 2020),
    REL_17("Release 17", "5G增强", 2022),
    REL_18("Release 18", "5G-Advanced", 2024);
    
    private final String name;
    private final String description;
    private final int year;
    
    ThreeGppRelease(String name, String description, int year) {
        this.name = name;
        this.description = description;
        this.year = year;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getYear() { return year; }
}
```

## 核心技术规范

### 1. 网络架构规范 (TS 23.xxx)

#### TS 23.002 - 网络架构
```java
/**
 * 3GPP网络架构组件
 */
@Component
@Slf4j
public class NetworkArchitectureService {
    
    /**
     * 网络功能实体枚举
     */
    public enum NetworkFunction {
        // 2G/3G网络功能
        MSC("Mobile Switching Center", "移动交换中心"),
        VLR("Visitor Location Register", "拜访位置寄存器"),
        HLR("Home Location Register", "归属位置寄存器"),
        AUC("Authentication Center", "鉴权中心"),
        EIR("Equipment Identity Register", "设备身份寄存器"),
        
        // 4G LTE网络功能
        MME("Mobility Management Entity", "移动性管理实体"),
        SGW("Serving Gateway", "服务网关"),
        PGW("PDN Gateway", "分组数据网网关"),
        HSS("Home Subscriber Server", "归属用户服务器"),
        PCRF("Policy and Charging Rules Function", "策略与计费规则功能"),
        
        // 5G网络功能
        AMF("Access and Mobility Management Function", "接入与移动性管理功能"),
        SMF("Session Management Function", "会话管理功能"),
        UPF("User Plane Function", "用户面功能"),
        UDM("Unified Data Management", "统一数据管理"),
        UDR("Unified Data Repository", "统一数据存储"),
        AUSF("Authentication Server Function", "认证服务器功能"),
        NSSF("Network Slice Selection Function", "网络切片选择功能"),
        PCF("Policy Control Function", "策略控制功能");
        
        private final String fullName;
        private final String chineseName;
        
        NetworkFunction(String fullName, String chineseName) {
            this.fullName = fullName;
            this.chineseName = chineseName;
        }
        
        public String getFullName() { return fullName; }
        public String getChineseName() { return chineseName; }
    }
    
    /**
     * 获取网络架构信息
     */
    public NetworkArchitectureInfo getNetworkArchitecture(ThreeGppRelease release) {
        log.info("Getting network architecture for release: {}", release.getName());
        
        switch (release) {
            case REL_8:
            case REL_9:
            case REL_10:
            case REL_11:
            case REL_12:
            case REL_13:
                return getLteArchitecture();
            case REL_15:
            case REL_16:
            case REL_17:
            case REL_18:
                return getFiveGArchitecture();
            default:
                return getLegacyArchitecture();
        }
    }
    
    private NetworkArchitectureInfo getLteArchitecture() {
        return NetworkArchitectureInfo.builder()
            .release("LTE/EPC")
            .coreNetworkFunctions(Arrays.asList(
                NetworkFunction.MME,
                NetworkFunction.SGW,
                NetworkFunction.PGW,
                NetworkFunction.HSS,
                NetworkFunction.PCRF
            ))
            .interfaces(Arrays.asList(
                "S1-MME", "S1-U", "S5/S8", "S6a", "S11", "Gx", "Gy"
            ))
            .description("LTE Evolved Packet Core架构")
            .build();
    }
    
    private NetworkArchitectureInfo getFiveGArchitecture() {
        return NetworkArchitectureInfo.builder()
            .release("5G Core")
            .coreNetworkFunctions(Arrays.asList(
                NetworkFunction.AMF,
                NetworkFunction.SMF,
                NetworkFunction.UPF,
                NetworkFunction.UDM,
                NetworkFunction.UDR,
                NetworkFunction.AUSF,
                NetworkFunction.NSSF,
                NetworkFunction.PCF
            ))
            .interfaces(Arrays.asList(
                "N1", "N2", "N3", "N4", "N6", "N8", "N10", "N11", "N12", "N15"
            ))
            .description("5G Service Based Architecture")
            .build();
    }
    
    private NetworkArchitectureInfo getLegacyArchitecture() {
        return NetworkArchitectureInfo.builder()
            .release("2G/3G")
            .coreNetworkFunctions(Arrays.asList(
                NetworkFunction.MSC,
                NetworkFunction.VLR,
                NetworkFunction.HLR,
                NetworkFunction.AUC,
                NetworkFunction.EIR
            ))
            .interfaces(Arrays.asList(
                "A", "Abis", "B", "C", "D", "E", "F", "G"
            ))
            .description("传统电路交换网络架构")
            .build();
    }
}
```

### 2. 用户数据管理规范 (TS 29.xxx)

#### TS 29.002 - MAP协议
```java
/**
 * MAP (Mobile Application Part) 协议服务
 */
@Service
@Slf4j
public class MapProtocolService {
    
    /**
     * MAP操作类型枚举
     */
    public enum MapOperation {
        // 位置管理
        UPDATE_LOCATION("updateLocation", "位置更新"),
        CANCEL_LOCATION("cancelLocation", "位置取消"),
        PURGE_MS("purgeMS", "清除移动台"),
        
        // 用户数据管理
        INSERT_SUBSCRIBER_DATA("insertSubscriberData", "插入用户数据"),
        DELETE_SUBSCRIBER_DATA("deleteSubscriberData", "删除用户数据"),
        
        // 鉴权管理
        SEND_AUTHENTICATION_INFO("sendAuthenticationInfo", "发送鉴权信息"),
        
        // 呼叫处理
        PROVIDE_ROAMING_NUMBER("provideRoamingNumber", "提供漫游号码"),
        SEND_ROUTING_INFO("sendRoutingInfo", "发送路由信息"),
        
        // 短信服务
        SEND_ROUTING_INFO_FOR_SM("sendRoutingInfoForSM", "发送短信路由信息"),
        MT_FORWARD_SM("mt-forwardSM", "移动终接短信转发"),
        MO_FORWARD_SM("mo-forwardSM", "移动发起短信转发");
        
        private final String operationName;
        private final String description;
        
        MapOperation(String operationName, String description) {
            this.operationName = operationName;
            this.description = description;
        }
        
        public String getOperationName() { return operationName; }
        public String getDescription() { return description; }
    }
    
    /**
     * 处理MAP消息
     */
    public MapResponse processMapMessage(MapRequest request) {
        log.info("Processing MAP operation: {}", request.getOperation());
        
        try {
            switch (request.getOperation()) {
                case UPDATE_LOCATION:
                    return handleUpdateLocation(request);
                case SEND_AUTHENTICATION_INFO:
                    return handleSendAuthenticationInfo(request);
                case INSERT_SUBSCRIBER_DATA:
                    return handleInsertSubscriberData(request);
                default:
                    throw new UnsupportedOperationException(
                        "Unsupported MAP operation: " + request.getOperation());
            }
        } catch (Exception e) {
            log.error("Failed to process MAP operation: {}", request.getOperation(), e);
            return MapResponse.error("MAP_SYSTEM_FAILURE", e.getMessage());
        }
    }
    
    private MapResponse handleUpdateLocation(MapRequest request) {
        // 处理位置更新请求
        String imsi = request.getParameter("imsi");
        String vlrNumber = request.getParameter("vlrNumber");
        
        log.debug("Handling location update for IMSI: {}, VLR: {}", 
            maskImsi(imsi), vlrNumber);
        
        // 验证IMSI
        if (!isValidImsi(imsi)) {
            return MapResponse.error("UNKNOWN_SUBSCRIBER", "Invalid IMSI");
        }
        
        // 更新位置信息
        updateSubscriberLocation(imsi, vlrNumber);
        
        // 返回用户数据
        SubscriberData subscriberData = getSubscriberData(imsi);
        
        return MapResponse.success("UPDATE_LOCATION_ACK", subscriberData);
    }
    
    private MapResponse handleSendAuthenticationInfo(MapRequest request) {
        // 处理鉴权信息请求
        String imsi = request.getParameter("imsi");
        int numberOfRequestedVectors = Integer.parseInt(
            request.getParameter("numberOfRequestedVectors"));
        
        log.debug("Handling authentication info request for IMSI: {}, vectors: {}", 
            maskImsi(imsi), numberOfRequestedVectors);
        
        // 生成鉴权向量
        List<AuthenticationVector> vectors = generateAuthenticationVectors(
            imsi, numberOfRequestedVectors);
        
        return MapResponse.success("SEND_AUTHENTICATION_INFO_ACK", vectors);
    }
    
    private MapResponse handleInsertSubscriberData(MapRequest request) {
        // 处理插入用户数据请求
        String imsi = request.getParameter("imsi");
        SubscriberData subscriberData = (SubscriberData) request.getParameter("subscriberData");
        
        log.debug("Handling insert subscriber data for IMSI: {}", maskImsi(imsi));
        
        // 插入/更新用户数据
        insertSubscriberData(imsi, subscriberData);
        
        return MapResponse.success("INSERT_SUBSCRIBER_DATA_ACK", null);
    }
    
    // 辅助方法
    private boolean isValidImsi(String imsi) {
        return imsi != null && imsi.matches("\\d{15}");
    }
    
    private void updateSubscriberLocation(String imsi, String vlrNumber) {
        // 更新用户位置信息到HLR/HSS
        log.debug("Updating location for IMSI: {} to VLR: {}", 
            maskImsi(imsi), vlrNumber);
    }
    
    private SubscriberData getSubscriberData(String imsi) {
        // 从HLR/HSS获取用户数据
        return SubscriberData.builder()
            .imsi(imsi)
            .msisdn("86138****1234") // 脱敏显示
            .serviceProfile(getServiceProfile(imsi))
            .build();
    }
    
    private ServiceProfile getServiceProfile(String imsi) {
        // 获取用户服务配置
        return ServiceProfile.builder()
            .voiceService(true)
            .dataService(true)
            .smsService(true)
            .roamingAllowed(true)
            .build();
    }
    
    private List<AuthenticationVector> generateAuthenticationVectors(String imsi, int count) {
        // 生成鉴权向量
        List<AuthenticationVector> vectors = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            AuthenticationVector vector = AuthenticationVector.builder()
                .rand(generateRand())
                .sres(generateSres())
                .kc(generateKc())
                .build();
            vectors.add(vector);
        }
        
        return vectors;
    }
    
    private void insertSubscriberData(String imsi, SubscriberData data) {
        // 插入用户数据到VLR
        log.debug("Inserting subscriber data for IMSI: {}", maskImsi(imsi));
    }
    
    private String generateRand() {
        // 生成随机数RAND
        return "1234567890ABCDEF1234567890ABCDEF";
    }
    
    private String generateSres() {
        // 生成签名响应SRES
        return "12345678";
    }
    
    private String generateKc() {
        // 生成密钥Kc
        return "1234567890ABCDEF";
    }
    
    private String maskImsi(String imsi) {
        if (imsi == null || imsi.length() < 10) {
            return imsi;
        }
        return imsi.substring(0, 6) + "****" + imsi.substring(10);
    }
}
```

### 3. 安全规范 (TS 33.xxx)

#### TS 33.102 - 3G安全架构
```java
/**
 * 3GPP安全服务
 */
@Service
@Slf4j
public class ThreeGppSecurityService {
    
    /**
     * 安全算法枚举
     */
    public enum SecurityAlgorithm {
        // 加密算法
        A5_0("A5/0", "无加密", AlgorithmType.ENCRYPTION),
        A5_1("A5/1", "GSM加密算法1", AlgorithmType.ENCRYPTION),
        A5_2("A5/2", "GSM加密算法2", AlgorithmType.ENCRYPTION),
        A5_3("A5/3", "KASUMI加密", AlgorithmType.ENCRYPTION),
        A5_4("A5/4", "SNOW 3G加密", AlgorithmType.ENCRYPTION),
        
        // 完整性算法
        IA1("IA1", "KASUMI完整性", AlgorithmType.INTEGRITY),
        IA2("IA2", "SNOW 3G完整性", AlgorithmType.INTEGRITY),
        IA3("IA3", "AES完整性", AlgorithmType.INTEGRITY),
        
        // 5G算法
        NEA0("NEA0", "5G无加密", AlgorithmType.ENCRYPTION),
        NEA1("NEA1", "5G SNOW加密", AlgorithmType.ENCRYPTION),
        NEA2("NEA2", "5G AES加密", AlgorithmType.ENCRYPTION),
        NEA3("NEA3", "5G ZUC加密", AlgorithmType.ENCRYPTION),
        
        NIA0("NIA0", "5G无完整性", AlgorithmType.INTEGRITY),
        NIA1("NIA1", "5G SNOW完整性", AlgorithmType.INTEGRITY),
        NIA2("NIA2", "5G AES完整性", AlgorithmType.INTEGRITY),
        NIA3("NIA3", "5G ZUC完整性", AlgorithmType.INTEGRITY);
        
        private final String name;
        private final String description;
        private final AlgorithmType type;
        
        SecurityAlgorithm(String name, String description, AlgorithmType type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public AlgorithmType getType() { return type; }
    }
    
    public enum AlgorithmType {
        ENCRYPTION("加密"),
        INTEGRITY("完整性"),
        AUTHENTICATION("鉴权");
        
        private final String description;
        
        AlgorithmType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * 执行AKA鉴权
     */
    public AkaAuthenticationResult performAkaAuthentication(
            String imsi, String servingNetworkName) {
        log.info("Performing AKA authentication for IMSI: {}", maskImsi(imsi));
        
        try {
            // 1. 获取用户密钥
            String k = getUserSecretKey(imsi);
            String opc = getOperatorVariant(imsi);
            
            // 2. 生成随机数
            String rand = generateSecureRandom();
            
            // 3. 计算鉴权向量
            AkaVector akaVector = computeAkaVector(k, opc, rand, servingNetworkName);
            
            // 4. 存储鉴权上下文
            storeAuthenticationContext(imsi, akaVector);
            
            log.info("AKA authentication completed for IMSI: {}", maskImsi(imsi));
            
            return AkaAuthenticationResult.builder()
                .success(true)
                .rand(akaVector.getRand())
                .autn(akaVector.getAutn())
                .xres(akaVector.getXres())
                .kasme(akaVector.getKasme())
                .build();
                
        } catch (Exception e) {
            log.error("AKA authentication failed for IMSI: {}", maskImsi(imsi), e);
            return AkaAuthenticationResult.builder()
                .success(false)
                .errorCode("AUTHENTICATION_FAILURE")
                .errorMessage(e.getMessage())
                .build();
        }
    }
    
    /**
     * 验证鉴权响应
     */
    public boolean verifyAuthenticationResponse(String imsi, String sres) {
        log.debug("Verifying authentication response for IMSI: {}", maskImsi(imsi));
        
        try {
            // 获取存储的鉴权上下文
            AuthenticationContext context = getAuthenticationContext(imsi);
            
            if (context == null) {
                log.warn("No authentication context found for IMSI: {}", maskImsi(imsi));
                return false;
            }
            
            // 验证SRES
            boolean isValid = context.getExpectedSres().equals(sres);
            
            if (isValid) {
                log.info("Authentication response verified successfully for IMSI: {}", 
                    maskImsi(imsi));
                
                // 清除鉴权上下文
                clearAuthenticationContext(imsi);
            } else {
                log.warn("Authentication response verification failed for IMSI: {}", 
                    maskImsi(imsi));
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Failed to verify authentication response for IMSI: {}", 
                maskImsi(imsi), e);
            return false;
        }
    }
    
    /**
     * 生成密钥层次结构
     */
    public KeyHierarchy generateKeyHierarchy(String kasme, String nasCount) {
        log.debug("Generating key hierarchy from KASME");
        
        // 生成NAS密钥
        String knasInt = deriveKey(kasme, "NAS_INT", nasCount);
        String knasEnc = deriveKey(kasme, "NAS_ENC", nasCount);
        
        // 生成RRC密钥
        String krrcInt = deriveKey(kasme, "RRC_INT", nasCount);
        String krrcEnc = deriveKey(kasme, "RRC_ENC", nasCount);
        
        // 生成UP密钥
        String kupEnc = deriveKey(kasme, "UP_ENC", nasCount);
        
        return KeyHierarchy.builder()
            .kasme(kasme)
            .knasInt(knasInt)
            .knasEnc(knasEnc)
            .krrcInt(krrcInt)
            .krrcEnc(krrcEnc)
            .kupEnc(kupEnc)
            .build();
    }
    
    /**
     * 选择安全算法
     */
    public SecurityAlgorithmSelection selectSecurityAlgorithms(
            List<SecurityAlgorithm> ueCapabilities,
            List<SecurityAlgorithm> networkPreferences) {
        log.debug("Selecting security algorithms");
        
        // 选择加密算法
        SecurityAlgorithm selectedEncryption = selectAlgorithm(
            ueCapabilities, networkPreferences, AlgorithmType.ENCRYPTION);
        
        // 选择完整性算法
        SecurityAlgorithm selectedIntegrity = selectAlgorithm(
            ueCapabilities, networkPreferences, AlgorithmType.INTEGRITY);
        
        return SecurityAlgorithmSelection.builder()
            .encryptionAlgorithm(selectedEncryption)
            .integrityAlgorithm(selectedIntegrity)
            .build();
    }
    
    // 辅助方法
    private String getUserSecretKey(String imsi) {
        // 从HSS/AuC获取用户密钥K
        // 实际实现中应该从安全的密钥管理系统获取
        return "0123456789ABCDEF0123456789ABCDEF";
    }
    
    private String getOperatorVariant(String imsi) {
        // 获取运营商变量OPc
        return "FEDCBA9876543210FEDCBA9876543210";
    }
    
    private String generateSecureRandom() {
        // 生成安全随机数RAND
        SecureRandom random = new SecureRandom();
        byte[] randBytes = new byte[16];
        random.nextBytes(randBytes);
        return bytesToHex(randBytes);
    }
    
    private AkaVector computeAkaVector(String k, String opc, String rand, String snn) {
        // 计算AKA鉴权向量
        // 这里是简化实现，实际应该使用标准的AKA算法
        
        String res = computeRes(k, rand);
        String ck = computeCk(k, rand);
        String ik = computeIk(k, rand);
        String ak = computeAk(k, rand);
        String mac = computeMac(k, rand, snn);
        
        String autn = generateAutn(ak, mac);
        String xres = res; // 在网络侧，XRES = RES
        String kasme = computeKasme(ck, ik, snn);
        
        return AkaVector.builder()
            .rand(rand)
            .res(res)
            .ck(ck)
            .ik(ik)
            .ak(ak)
            .autn(autn)
            .xres(xres)
            .kasme(kasme)
            .build();
    }
    
    private String computeRes(String k, String rand) {
        // 计算RES
        return "12345678";
    }
    
    private String computeCk(String k, String rand) {
        // 计算密钥CK
        return "0123456789ABCDEF0123456789ABCDEF";
    }
    
    private String computeIk(String k, String rand) {
        // 计算完整性密钥IK
        return "FEDCBA9876543210FEDCBA9876543210";
    }
    
    private String computeAk(String k, String rand) {
        // 计算匿名密钥AK
        return "123456";
    }
    
    private String computeMac(String k, String rand, String snn) {
        // 计算消息认证码MAC
        return "12345678";
    }
    
    private String generateAutn(String ak, String mac) {
        // 生成AUTN
        String sqn = "000000000001"; // 序列号
        String amf = "8000"; // 鉴权管理域
        return sqn + amf + mac;
    }
    
    private String computeKasme(String ck, String ik, String snn) {
        // 计算KASME
        return deriveKey(ck + ik, "KASME", snn);
    }
    
    private String deriveKey(String baseKey, String keyType, String context) {
        // 密钥派生函数
        // 实际实现应该使用KDF (Key Derivation Function)
        return "DERIVED_" + keyType + "_" + context.hashCode();
    }
    
    private SecurityAlgorithm selectAlgorithm(
            List<SecurityAlgorithm> ueCapabilities,
            List<SecurityAlgorithm> networkPreferences,
            AlgorithmType type) {
        
        // 按网络偏好顺序选择UE支持的算法
        for (SecurityAlgorithm preferred : networkPreferences) {
            if (preferred.getType() == type && ueCapabilities.contains(preferred)) {
                return preferred;
            }
        }
        
        // 如果没有匹配的，选择第一个UE支持的同类型算法
        return ueCapabilities.stream()
            .filter(alg -> alg.getType() == type)
            .findFirst()
            .orElse(null);
    }
    
    private void storeAuthenticationContext(String imsi, AkaVector vector) {
        // 存储鉴权上下文
        log.debug("Storing authentication context for IMSI: {}", maskImsi(imsi));
    }
    
    private AuthenticationContext getAuthenticationContext(String imsi) {
        // 获取鉴权上下文
        return AuthenticationContext.builder()
            .imsi(imsi)
            .expectedSres("12345678")
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private void clearAuthenticationContext(String imsi) {
        // 清除鉴权上下文
        log.debug("Clearing authentication context for IMSI: {}", maskImsi(imsi));
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
    
    private String maskImsi(String imsi) {
        if (imsi == null || imsi.length() < 10) {
            return imsi;
        }
        return imsi.substring(0, 6) + "****" + imsi.substring(10);
    }
}
```

## 接口规范实现

### Diameter协议实现 (TS 29.272)
```java
/**
 * Diameter协议服务 (S6a接口)
 */
@Service
@Slf4j
public class DiameterS6aService {
    
    /**
     * Diameter命令代码枚举
     */
    public enum DiameterCommand {
        ULR(316, "Update-Location-Request", "位置更新请求"),
        ULA(316, "Update-Location-Answer", "位置更新应答"),
        AIR(318, "Authentication-Information-Request", "鉴权信息请求"),
        AIA(318, "Authentication-Information-Answer", "鉴权信息应答"),
        PUR(321, "Purge-UE-Request", "清除UE请求"),
        PUA(321, "Purge-UE-Answer", "清除UE应答"),
        IDR(319, "Insert-Subscriber-Data-Request", "插入用户数据请求"),
        IDA(319, "Insert-Subscriber-Data-Answer", "插入用户数据应答");
        
        private final int code;
        private final String name;
        private final String description;
        
        DiameterCommand(int code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }
        
        public int getCode() { return code; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    /**
     * 处理位置更新请求
     */
    public DiameterMessage handleUpdateLocationRequest(DiameterMessage request) {
        log.info("Handling Update-Location-Request");
        
        try {
            // 解析请求参数
            String imsi = request.getAvpValue("User-Name");
            String visitedPlmnId = request.getAvpValue("Visited-PLMN-Id");
            String ratType = request.getAvpValue("RAT-Type");
            String ulrFlags = request.getAvpValue("ULR-Flags");
            
            log.debug("ULR for IMSI: {}, Visited-PLMN: {}, RAT: {}", 
                maskImsi(imsi), visitedPlmnId, ratType);
            
            // 验证用户
            if (!isValidSubscriber(imsi)) {
                return createErrorResponse(request, 5001, "DIAMETER_ERROR_USER_UNKNOWN");
            }
            
            // 检查漫游权限
            if (!isRoamingAllowed(imsi, visitedPlmnId)) {
                return createErrorResponse(request, 5004, "DIAMETER_ERROR_ROAMING_NOT_ALLOWED");
            }
            
            // 更新位置信息
            updateSubscriberLocation(imsi, visitedPlmnId);
            
            // 获取用户数据
            SubscriptionData subscriptionData = getSubscriptionData(imsi);
            
            // 构造应答消息
            DiameterMessage response = DiameterMessage.builder()
                .commandCode(DiameterCommand.ULA.getCode())
                .isRequest(false)
                .sessionId(request.getSessionId())
                .build();
            
            // 添加AVP
            response.addAvp("Result-Code", "2001"); // DIAMETER_SUCCESS
            response.addAvp("Subscription-Data", subscriptionData);
            response.addAvp("ULA-Flags", "1");
            
            log.info("ULA sent successfully for IMSI: {}", maskImsi(imsi));
            return response;
            
        } catch (Exception e) {
            log.error("Failed to handle ULR", e);
            return createErrorResponse(request, 5012, "DIAMETER_UNABLE_TO_COMPLY");
        }
    }
    
    /**
     * 处理鉴权信息请求
     */
    public DiameterMessage handleAuthenticationInformationRequest(DiameterMessage request) {
        log.info("Handling Authentication-Information-Request");
        
        try {
            // 解析请求参数
            String imsi = request.getAvpValue("User-Name");
            String visitedPlmnId = request.getAvpValue("Visited-PLMN-Id");
            String numOfRequestedVectors = request.getAvpValue("Requested-EUTRAN-Authentication-Info");
            
            log.debug("AIR for IMSI: {}, Vectors: {}", 
                maskImsi(imsi), numOfRequestedVectors);
            
            // 验证用户
            if (!isValidSubscriber(imsi)) {
                return createErrorResponse(request, 5001, "DIAMETER_ERROR_USER_UNKNOWN");
            }
            
            // 生成鉴权向量
            int vectorCount = Integer.parseInt(numOfRequestedVectors);
            List<EutranVector> authVectors = generateEutranVectors(imsi, vectorCount);
            
            // 构造应答消息
            DiameterMessage response = DiameterMessage.builder()
                .commandCode(DiameterCommand.AIA.getCode())
                .isRequest(false)
                .sessionId(request.getSessionId())
                .build();
            
            // 添加AVP
            response.addAvp("Result-Code", "2001"); // DIAMETER_SUCCESS
            response.addAvp("Authentication-Info", authVectors);
            
            log.info("AIA sent successfully for IMSI: {}", maskImsi(imsi));
            return response;
            
        } catch (Exception e) {
            log.error("Failed to handle AIR", e);
            return createErrorResponse(request, 5012, "DIAMETER_UNABLE_TO_COMPLY");
        }
    }
    
    /**
     * 处理清除UE请求
     */
    public DiameterMessage handlePurgeUeRequest(DiameterMessage request) {
        log.info("Handling Purge-UE-Request");
        
        try {
            String imsi = request.getAvpValue("User-Name");
            
            log.debug("PUR for IMSI: {}", maskImsi(imsi));
            
            // 清除UE上下文
            purgeUeContext(imsi);
            
            // 构造应答消息
            DiameterMessage response = DiameterMessage.builder()
                .commandCode(DiameterCommand.PUA.getCode())
                .isRequest(false)
                .sessionId(request.getSessionId())
                .build();
            
            response.addAvp("Result-Code", "2001"); // DIAMETER_SUCCESS
            response.addAvp("PUA-Flags", "0");
            
            log.info("PUA sent successfully for IMSI: {}", maskImsi(imsi));
            return response;
            
        } catch (Exception e) {
            log.error("Failed to handle PUR", e);
            return createErrorResponse(request, 5012, "DIAMETER_UNABLE_TO_COMPLY");
        }
    }
    
    // 辅助方法
    private boolean isValidSubscriber(String imsi) {
        // 验证用户是否存在
        return imsi != null && imsi.matches("\\d{15}");
    }
    
    private boolean isRoamingAllowed(String imsi, String visitedPlmnId) {
        // 检查漫游权限
        // 实际实现中应该检查用户的漫游配置
        return true;
    }
    
    private void updateSubscriberLocation(String imsi, String visitedPlmnId) {
        // 更新用户位置信息
        log.debug("Updating location for IMSI: {} to PLMN: {}", 
            maskImsi(imsi), visitedPlmnId);
    }
    
    private SubscriptionData getSubscriptionData(String imsi) {
        // 获取用户签约数据
        return SubscriptionData.builder()
            .imsi(imsi)
            .msisdn("86138****1234")
            .accessRestrictionData(0)
            .subscriberStatus("SERVICE_GRANTED")
            .networkAccessMode("PACKET_AND_CIRCUIT")
            .apnConfigurationProfile(getApnConfigurationProfile(imsi))
            .build();
    }
    
    private ApnConfigurationProfile getApnConfigurationProfile(String imsi) {
        // 获取APN配置
        return ApnConfigurationProfile.builder()
            .contextIdentifier(1)
            .allApnConfigurationsIncludedIndicator(true)
            .apnConfigurations(Arrays.asList(
                ApnConfiguration.builder()
                    .contextIdentifier(1)
                    .serviceSelection("internet")
                    .pdnType("IPv4")
                    .build()
            ))
            .build();
    }
    
    private List<EutranVector> generateEutranVectors(String imsi, int count) {
        // 生成E-UTRAN鉴权向量
        List<EutranVector> vectors = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            EutranVector vector = EutranVector.builder()
                .rand(generateRand())
                .xres(generateXres())
                .autn(generateAutn())
                .kasme(generateKasme())
                .build();
            vectors.add(vector);
        }
        
        return vectors;
    }
    
    private void purgeUeContext(String imsi) {
        // 清除UE上下文
        log.debug("Purging UE context for IMSI: {}", maskImsi(imsi));
    }
    
    private DiameterMessage createErrorResponse(DiameterMessage request, 
                                               int resultCode, String errorMessage) {
        DiameterMessage response = DiameterMessage.builder()
            .commandCode(request.getCommandCode())
            .isRequest(false)
            .sessionId(request.getSessionId())
            .build();
        
        response.addAvp("Result-Code", String.valueOf(resultCode));
        response.addAvp("Error-Message", errorMessage);
        
        return response;
    }
    
    private String generateRand() {
        return "1234567890ABCDEF1234567890ABCDEF";
    }
    
    private String generateXres() {
        return "12345678";
    }
    
    private String generateAutn() {
        return "000000000001800012345678";
    }
    
    private String generateKasme() {
        return "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
    }
    
    private String maskImsi(String imsi) {
        if (imsi == null || imsi.length() < 10) {
            return imsi;
        }
        return imsi.substring(0, 6) + "****" + imsi.substring(10);
    }
}
```

## 最佳实践总结

### 1. 标准合规性
- **严格遵循3GPP规范**: 确保所有实现都符合相应的3GPP技术规范
- **版本兼容性**: 支持多个3GPP Release版本的兼容性
- **互操作性**: 确保与其他厂商设备的互操作性
- **标准更新跟踪**: 及时跟踪和应用3GPP标准的更新

### 2. 安全实现
- **密钥管理**: 实现安全的密钥生成、存储和管理
- **算法选择**: 支持多种安全算法并正确选择
- **鉴权流程**: 严格按照3GPP规范实现鉴权流程
- **安全审计**: 记录所有安全相关操作的审计日志

### 3. 协议实现
- **消息格式**: 严格按照协议规范定义消息格式
- **错误处理**: 正确处理各种协议错误情况
- **性能优化**: 优化协议处理性能
- **协议测试**: 进行充分的协议一致性测试

### 4. 系统集成
- **接口标准化**: 使用标准化的接口进行系统集成
- **数据一致性**: 确保跨系统的数据一致性
- **监控告警**: 实现全面的系统监控和告警
- **容错处理**: 实现系统的容错和恢复机制

通过深入理解和正确实现3GPP标准，NSRS号卡资源管理系统能够确保与全球电信网络的兼容性和互操作性，满足电信行业的专业要求。