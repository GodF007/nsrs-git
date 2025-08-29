# Docker容器化部署详解

## 概述

NSRS号卡资源管理系统采用Docker容器化部署方案，实现应用的标准化打包、快速部署和弹性扩缩容。本文档详细阐述了从单体应用到微服务的完整容器化部署策略。

### 容器化目标

- **环境一致性**: 开发、测试、生产环境完全一致
- **快速部署**: 秒级启动，分钟级部署
- **弹性扩缩**: 根据负载自动扩缩容
- **资源隔离**: 应用间资源完全隔离
- **版本管理**: 支持灰度发布和快速回滚

## 核心枚举定义

### 1. 部署环境类型

```java
/**
 * 部署环境类型枚举
 */
public enum DeploymentEnvironment {
    DEVELOPMENT("开发环境", "dev", "开发测试使用"),
    TESTING("测试环境", "test", "功能测试验证"),
    STAGING("预发环境", "staging", "生产前验证"),
    PRODUCTION("生产环境", "prod", "正式生产使用"),
    DISASTER_RECOVERY("容灾环境", "dr", "灾备切换使用");
    
    private final String name;
    private final String code;
    private final String description;
    
    DeploymentEnvironment(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }
    
    // getters...
}
```

### 2. 容器状态枚举

```java
/**
 * 容器状态枚举
 */
public enum ContainerStatus {
    CREATING("创建中", "容器正在创建"),
    RUNNING("运行中", "容器正常运行"),
    STOPPED("已停止", "容器已停止运行"),
    RESTARTING("重启中", "容器正在重启"),
    REMOVING("删除中", "容器正在删除"),
    PAUSED("已暂停", "容器已暂停"),
    EXITED("已退出", "容器已退出"),
    DEAD("已死亡", "容器进程异常终止");
    
    private final String description;
    private final String detail;
    
    ContainerStatus(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }
    
    // getters...
}
```

### 3. 部署策略枚举

```java
/**
 * 部署策略枚举
 */
public enum DeploymentStrategy {
    RECREATE("重建部署", "停止旧版本，启动新版本"),
    ROLLING_UPDATE("滚动更新", "逐步替换旧版本实例"),
    BLUE_GREEN("蓝绿部署", "并行运行两个版本"),
    CANARY("金丝雀部署", "小流量验证新版本"),
    A_B_TESTING("A/B测试", "同时运行多个版本");
    
    private final String name;
    private final String description;
    
    DeploymentStrategy(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // getters...
}
```

## 核心实体设计

### 1. 容器配置实体

```java
/**
 * 容器配置实体
 */
@Entity
@Table(name = "container_config")
public class ContainerConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "service_name", nullable = false)
    private String serviceName;
    
    @Column(name = "image_name", nullable = false)
    private String imageName;
    
    @Column(name = "image_tag")
    private String imageTag;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "environment")
    private DeploymentEnvironment environment;
    
    @Column(name = "cpu_limit")
    private String cpuLimit; // 如: "1000m"
    
    @Column(name = "memory_limit")
    private String memoryLimit; // 如: "2Gi"
    
    @Column(name = "cpu_request")
    private String cpuRequest;
    
    @Column(name = "memory_request")
    private String memoryRequest;
    
    @Column(name = "replica_count")
    private Integer replicaCount;
    
    @Column(name = "port_mappings", columnDefinition = "JSON")
    private String portMappings;
    
    @Column(name = "environment_variables", columnDefinition = "JSON")
    private String environmentVariables;
    
    @Column(name = "volume_mounts", columnDefinition = "JSON")
    private String volumeMounts;
    
    @Column(name = "health_check_config", columnDefinition = "JSON")
    private String healthCheckConfig;
    
    @Column(name = "network_config", columnDefinition = "JSON")
    private String networkConfig;
    
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // constructors, getters, setters...
}
```

### 2. 部署记录实体

```java
/**
 * 部署记录实体
 */
@Entity
@Table(name = "deployment_record")
public class DeploymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "deployment_id", unique = true)
    private String deploymentId;
    
    @Column(name = "service_name")
    private String serviceName;
    
    @Column(name = "image_name")
    private String imageName;
    
    @Column(name = "image_tag")
    private String imageTag;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "environment")
    private DeploymentEnvironment environment;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "deployment_strategy")
    private DeploymentStrategy deploymentStrategy;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "status")
    private String status; // PENDING, RUNNING, SUCCESS, FAILED, ROLLBACK
    
    @Column(name = "deployed_by")
    private String deployedBy;
    
    @Column(name = "rollback_version")
    private String rollbackVersion;
    
    @Column(name = "deployment_logs", columnDefinition = "TEXT")
    private String deploymentLogs;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "resource_usage", columnDefinition = "JSON")
    private String resourceUsage;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    // constructors, getters, setters...
}
```

### 3. 容器实例实体

```java
/**
 * 容器实例实体
 */
@Entity
@Table(name = "container_instance")
public class ContainerInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "container_id", unique = true)
    private String containerId;
    
    @Column(name = "container_name")
    private String containerName;
    
    @Column(name = "service_name")
    private String serviceName;
    
    @Column(name = "image_name")
    private String imageName;
    
    @Column(name = "image_tag")
    private String imageTag;
    
    @Column(name = "host_ip")
    private String hostIp;
    
    @Column(name = "container_ip")
    private String containerIp;
    
    @Column(name = "port_mappings", columnDefinition = "JSON")
    private String portMappings;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContainerStatus status;
    
    @Column(name = "cpu_usage")
    private Double cpuUsage;
    
    @Column(name = "memory_usage")
    private Double memoryUsage;
    
    @Column(name = "network_io", columnDefinition = "JSON")
    private String networkIo;
    
    @Column(name = "disk_io", columnDefinition = "JSON")
    private String diskIo;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;
    
    @Column(name = "health_status")
    private String healthStatus; // HEALTHY, UNHEALTHY, UNKNOWN
    
    @Column(name = "restart_count")
    private Integer restartCount;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // constructors, getters, setters...
}
```

## Docker容器化服务实现

### 1. 容器管理服务

```java
/**
 * 容器管理服务
 */
@Service
@Slf4j
public class ContainerManagementService {
    
    @Autowired
    private ContainerConfigRepository configRepository;
    
    @Autowired
    private ContainerInstanceRepository instanceRepository;
    
    @Autowired
    private DeploymentRecordRepository deploymentRepository;
    
    private final DockerClient dockerClient;
    
    public ContainerManagementService() {
        this.dockerClient = DockerClientBuilder.getInstance().build();
    }
    
    /**
     * 创建容器
     */
    public String createContainer(ContainerConfig config) {
        try {
            log.info("开始创建容器: {}", config.getServiceName());
            
            // 1. 构建容器创建配置
            CreateContainerCmd createCmd = dockerClient.createContainerCmd(config.getImageName() + ":" + config.getImageTag())
                .withName(generateContainerName(config.getServiceName()))
                .withHostConfig(buildHostConfig(config))
                .withEnv(parseEnvironmentVariables(config.getEnvironmentVariables()))
                .withExposedPorts(parseExposedPorts(config.getPortMappings()))
                .withLabels(buildLabels(config));
            
            // 2. 创建容器
            CreateContainerResponse response = createCmd.exec();
            String containerId = response.getId();
            
            // 3. 记录容器实例
            ContainerInstance instance = new ContainerInstance();
            instance.setContainerId(containerId);
            instance.setContainerName(generateContainerName(config.getServiceName()));
            instance.setServiceName(config.getServiceName());
            instance.setImageName(config.getImageName());
            instance.setImageTag(config.getImageTag());
            instance.setStatus(ContainerStatus.CREATING);
            instance.setRestartCount(0);
            instance.setCreatedTime(LocalDateTime.now());
            instanceRepository.save(instance);
            
            log.info("容器创建成功: {} -> {}", config.getServiceName(), containerId);
            return containerId;
            
        } catch (Exception e) {
            log.error("创建容器失败: {}", config.getServiceName(), e);
            throw new RuntimeException("创建容器失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动容器
     */
    public void startContainer(String containerId) {
        try {
            log.info("启动容器: {}", containerId);
            
            dockerClient.startContainerCmd(containerId).exec();
            
            // 更新容器状态
            ContainerInstance instance = instanceRepository.findByContainerId(containerId)
                .orElseThrow(() -> new IllegalArgumentException("容器实例不存在"));
            instance.setStatus(ContainerStatus.RUNNING);
            instance.setStartTime(LocalDateTime.now());
            instance.setUpdatedTime(LocalDateTime.now());
            instanceRepository.save(instance);
            
            log.info("容器启动成功: {}", containerId);
            
        } catch (Exception e) {
            log.error("启动容器失败: {}", containerId, e);
            throw new RuntimeException("启动容器失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止容器
     */
    public void stopContainer(String containerId) {
        try {
            log.info("停止容器: {}", containerId);
            
            dockerClient.stopContainerCmd(containerId)
                .withTimeout(30) // 30秒超时
                .exec();
            
            // 更新容器状态
            ContainerInstance instance = instanceRepository.findByContainerId(containerId)
                .orElseThrow(() -> new IllegalArgumentException("容器实例不存在"));
            instance.setStatus(ContainerStatus.STOPPED);
            instance.setUpdatedTime(LocalDateTime.now());
            instanceRepository.save(instance);
            
            log.info("容器停止成功: {}", containerId);
            
        } catch (Exception e) {
            log.error("停止容器失败: {}", containerId, e);
            throw new RuntimeException("停止容器失败: " + e.getMessage());
        }
    }
    
    /**
     * 重启容器
     */
    public void restartContainer(String containerId) {
        try {
            log.info("重启容器: {}", containerId);
            
            dockerClient.restartContainerCmd(containerId)
                .withtTimeout(30)
                .exec();
            
            // 更新容器状态
            ContainerInstance instance = instanceRepository.findByContainerId(containerId)
                .orElseThrow(() -> new IllegalArgumentException("容器实例不存在"));
            instance.setStatus(ContainerStatus.RUNNING);
            instance.setRestartCount(instance.getRestartCount() + 1);
            instance.setStartTime(LocalDateTime.now());
            instance.setUpdatedTime(LocalDateTime.now());
            instanceRepository.save(instance);
            
            log.info("容器重启成功: {}", containerId);
            
        } catch (Exception e) {
            log.error("重启容器失败: {}", containerId, e);
            throw new RuntimeException("重启容器失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除容器
     */
    public void removeContainer(String containerId, boolean force) {
        try {
            log.info("删除容器: {}, force: {}", containerId, force);
            
            dockerClient.removeContainerCmd(containerId)
                .withForce(force)
                .exec();
            
            // 删除容器实例记录
            instanceRepository.deleteByContainerId(containerId);
            
            log.info("容器删除成功: {}", containerId);
            
        } catch (Exception e) {
            log.error("删除容器失败: {}", containerId, e);
            throw new RuntimeException("删除容器失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取容器状态
     */
    public ContainerStats getContainerStats(String containerId) {
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            
            ContainerStats stats = new ContainerStats();
            stats.setContainerId(containerId);
            stats.setStatus(response.getState().getStatus());
            stats.setStartedAt(response.getState().getStartedAt());
            stats.setFinishedAt(response.getState().getFinishedAt());
            stats.setRestartCount(response.getRestartCount());
            
            // 获取资源使用情况
            Statistics resourceStats = dockerClient.statsCmd(containerId).exec(
                new ResultCallback.Adapter<Statistics>() {
                    @Override
                    public void onNext(Statistics statistics) {
                        // 处理统计信息
                    }
                }
            );
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取容器状态失败: {}", containerId, e);
            throw new RuntimeException("获取容器状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 监控容器健康状态
     */
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void monitorContainerHealth() {
        try {
            List<ContainerInstance> runningContainers = instanceRepository.findByStatus(ContainerStatus.RUNNING);
            
            for (ContainerInstance instance : runningContainers) {
                try {
                    // 检查容器是否还在运行
                    InspectContainerResponse response = dockerClient.inspectContainerCmd(instance.getContainerId()).exec();
                    
                    String currentStatus = response.getState().getStatus();
                    ContainerStatus newStatus = mapDockerStatusToContainerStatus(currentStatus);
                    
                    if (!newStatus.equals(instance.getStatus())) {
                        log.warn("容器状态变化: {} {} -> {}", instance.getContainerId(), instance.getStatus(), newStatus);
                        instance.setStatus(newStatus);
                        instance.setUpdatedTime(LocalDateTime.now());
                        instanceRepository.save(instance);
                    }
                    
                    // 更新健康检查时间
                    instance.setLastHealthCheck(LocalDateTime.now());
                    instance.setHealthStatus("HEALTHY");
                    instanceRepository.save(instance);
                    
                } catch (Exception e) {
                    log.error("健康检查失败: {}", instance.getContainerId(), e);
                    instance.setHealthStatus("UNHEALTHY");
                    instance.setLastHealthCheck(LocalDateTime.now());
                    instanceRepository.save(instance);
                }
            }
            
        } catch (Exception e) {
            log.error("容器健康监控失败", e);
        }
    }
    
    // 辅助方法
    private String generateContainerName(String serviceName) {
        return serviceName + "-" + System.currentTimeMillis();
    }
    
    private HostConfig buildHostConfig(ContainerConfig config) {
        HostConfig hostConfig = HostConfig.newHostConfig();
        
        // 资源限制
        if (config.getMemoryLimit() != null) {
            hostConfig.withMemory(parseMemoryLimit(config.getMemoryLimit()));
        }
        
        if (config.getCpuLimit() != null) {
            hostConfig.withCpuQuota(parseCpuLimit(config.getCpuLimit()));
        }
        
        // 端口映射
        if (config.getPortMappings() != null) {
            hostConfig.withPortBindings(parsePortBindings(config.getPortMappings()));
        }
        
        // 卷挂载
        if (config.getVolumeMounts() != null) {
            hostConfig.withBinds(parseVolumeBinds(config.getVolumeMounts()));
        }
        
        // 重启策略
        hostConfig.withRestartPolicy(RestartPolicy.unlessStoppedRestart());
        
        return hostConfig;
    }
    
    private String[] parseEnvironmentVariables(String envVarsJson) {
        if (envVarsJson == null) return new String[0];
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> envMap = mapper.readValue(envVarsJson, Map.class);
            
            return envMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
        } catch (Exception e) {
            log.error("解析环境变量失败", e);
            return new String[0];
        }
    }
    
    private ExposedPort[] parseExposedPorts(String portMappingsJson) {
        if (portMappingsJson == null) return new ExposedPort[0];
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> portMappings = mapper.readValue(portMappingsJson, List.class);
            
            return portMappings.stream()
                .map(mapping -> ExposedPort.tcp((Integer) mapping.get("containerPort")))
                .toArray(ExposedPort[]::new);
        } catch (Exception e) {
            log.error("解析端口映射失败", e);
            return new ExposedPort[0];
        }
    }
    
    private Map<String, String> buildLabels(ContainerConfig config) {
        Map<String, String> labels = new HashMap<>();
        labels.put("service", config.getServiceName());
        labels.put("environment", config.getEnvironment().getCode());
        labels.put("managed-by", "nsrs-container-service");
        labels.put("created-at", LocalDateTime.now().toString());
        return labels;
    }
    
    private Long parseMemoryLimit(String memoryLimit) {
        // 解析内存限制，如 "2Gi" -> 2147483648L
        if (memoryLimit.endsWith("Gi")) {
            return Long.parseLong(memoryLimit.replace("Gi", "")) * 1024 * 1024 * 1024;
        } else if (memoryLimit.endsWith("Mi")) {
            return Long.parseLong(memoryLimit.replace("Mi", "")) * 1024 * 1024;
        }
        return Long.parseLong(memoryLimit);
    }
    
    private Long parseCpuLimit(String cpuLimit) {
        // 解析CPU限制，如 "1000m" -> 100000L (微秒)
        if (cpuLimit.endsWith("m")) {
            return Long.parseLong(cpuLimit.replace("m", "")) * 1000;
        }
        return Long.parseLong(cpuLimit) * 1000000;
    }
    
    private PortBinding[] parsePortBindings(String portMappingsJson) {
        if (portMappingsJson == null) return new PortBinding[0];
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> portMappings = mapper.readValue(portMappingsJson, List.class);
            
            return portMappings.stream()
                .map(mapping -> {
                    ExposedPort exposedPort = ExposedPort.tcp((Integer) mapping.get("containerPort"));
                    Ports.Binding binding = Ports.Binding.bindPort((Integer) mapping.get("hostPort"));
                    return new PortBinding(binding, exposedPort);
                })
                .toArray(PortBinding[]::new);
        } catch (Exception e) {
            log.error("解析端口绑定失败", e);
            return new PortBinding[0];
        }
    }
    
    private Bind[] parseVolumeBinds(String volumeMountsJson) {
        if (volumeMountsJson == null) return new Bind[0];
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> volumeMounts = mapper.readValue(volumeMountsJson, List.class);
            
            return volumeMounts.stream()
                .map(mount -> new Bind(mount.get("hostPath"), new Volume(mount.get("containerPath"))))
                .toArray(Bind[]::new);
        } catch (Exception e) {
            log.error("解析卷挂载失败", e);
            return new Bind[0];
        }
    }
    
    private ContainerStatus mapDockerStatusToContainerStatus(String dockerStatus) {
        switch (dockerStatus.toLowerCase()) {
            case "running": return ContainerStatus.RUNNING;
            case "exited": return ContainerStatus.EXITED;
            case "paused": return ContainerStatus.PAUSED;
            case "restarting": return ContainerStatus.RESTARTING;
            case "removing": return ContainerStatus.REMOVING;
            case "dead": return ContainerStatus.DEAD;
            default: return ContainerStatus.STOPPED;
        }
    }
}
```

### 2. 部署管理服务

```java
/**
 * 部署管理服务
 */
@Service
@Slf4j
public class DeploymentManagementService {
    
    @Autowired
    private ContainerManagementService containerService;
    
    @Autowired
    private ContainerConfigRepository configRepository;
    
    @Autowired
    private DeploymentRecordRepository deploymentRepository;
    
    @Autowired
    private ContainerInstanceRepository instanceRepository;
    
    /**
     * 执行部署
     */
    public String deploy(String serviceName, String imageTag, DeploymentStrategy strategy, String deployedBy) {
        String deploymentId = UUID.randomUUID().toString();
        
        try {
            log.info("开始部署服务: {} -> {}, 策略: {}", serviceName, imageTag, strategy);
            
            // 1. 获取服务配置
            ContainerConfig config = configRepository.findByServiceName(serviceName)
                .orElseThrow(() -> new IllegalArgumentException("服务配置不存在: " + serviceName));
            
            // 2. 创建部署记录
            DeploymentRecord record = createDeploymentRecord(deploymentId, config, imageTag, strategy, deployedBy);
            deploymentRepository.save(record);
            
            // 3. 根据策略执行部署
            switch (strategy) {
                case RECREATE:
                    executeRecreateDeployment(config, imageTag, record);
                    break;
                case ROLLING_UPDATE:
                    executeRollingUpdateDeployment(config, imageTag, record);
                    break;
                case BLUE_GREEN:
                    executeBlueGreenDeployment(config, imageTag, record);
                    break;
                case CANARY:
                    executeCanaryDeployment(config, imageTag, record);
                    break;
                default:
                    throw new UnsupportedOperationException("不支持的部署策略: " + strategy);
            }
            
            // 4. 更新部署状态
            record.setStatus("SUCCESS");
            record.setEndTime(LocalDateTime.now());
            deploymentRepository.save(record);
            
            log.info("部署完成: {}", deploymentId);
            return deploymentId;
            
        } catch (Exception e) {
            log.error("部署失败: {}", deploymentId, e);
            
            // 更新部署状态为失败
            DeploymentRecord record = deploymentRepository.findByDeploymentId(deploymentId).orElse(null);
            if (record != null) {
                record.setStatus("FAILED");
                record.setErrorMessage(e.getMessage());
                record.setEndTime(LocalDateTime.now());
                deploymentRepository.save(record);
            }
            
            throw new RuntimeException("部署失败: " + e.getMessage());
        }
    }
    
    /**
     * 重建部署策略
     */
    private void executeRecreateDeployment(ContainerConfig config, String imageTag, DeploymentRecord record) {
        log.info("执行重建部署: {}", config.getServiceName());
        
        // 1. 停止并删除旧容器
        List<ContainerInstance> oldInstances = instanceRepository.findByServiceNameAndStatus(
            config.getServiceName(), ContainerStatus.RUNNING);
        
        for (ContainerInstance instance : oldInstances) {
            containerService.stopContainer(instance.getContainerId());
            containerService.removeContainer(instance.getContainerId(), true);
        }
        
        // 2. 创建新容器
        config.setImageTag(imageTag);
        for (int i = 0; i < config.getReplicaCount(); i++) {
            String containerId = containerService.createContainer(config);
            containerService.startContainer(containerId);
        }
        
        record.setDeploymentLogs("重建部署完成，创建了 " + config.getReplicaCount() + " 个新实例");
    }
    
    /**
     * 滚动更新部署策略
     */
    private void executeRollingUpdateDeployment(ContainerConfig config, String imageTag, DeploymentRecord record) {
        log.info("执行滚动更新部署: {}", config.getServiceName());
        
        List<ContainerInstance> oldInstances = instanceRepository.findByServiceNameAndStatus(
            config.getServiceName(), ContainerStatus.RUNNING);
        
        StringBuilder logs = new StringBuilder();
        config.setImageTag(imageTag);
        
        // 逐个替换实例
        for (int i = 0; i < oldInstances.size(); i++) {
            ContainerInstance oldInstance = oldInstances.get(i);
            
            // 创建新实例
            String newContainerId = containerService.createContainer(config);
            containerService.startContainer(newContainerId);
            
            // 等待新实例就绪
            waitForContainerReady(newContainerId, 60);
            
            // 停止旧实例
            containerService.stopContainer(oldInstance.getContainerId());
            containerService.removeContainer(oldInstance.getContainerId(), true);
            
            logs.append(String.format("替换实例 %d/%d: %s -> %s\n", 
                i + 1, oldInstances.size(), oldInstance.getContainerId(), newContainerId));
            
            // 间隔时间，避免同时替换过多实例
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        record.setDeploymentLogs(logs.toString());
    }
    
    /**
     * 蓝绿部署策略
     */
    private void executeBlueGreenDeployment(ContainerConfig config, String imageTag, DeploymentRecord record) {
        log.info("执行蓝绿部署: {}", config.getServiceName());
        
        // 1. 创建绿色环境（新版本）
        config.setImageTag(imageTag);
        List<String> greenContainers = new ArrayList<>();
        
        for (int i = 0; i < config.getReplicaCount(); i++) {
            String containerId = containerService.createContainer(config);
            containerService.startContainer(containerId);
            greenContainers.add(containerId);
        }
        
        // 2. 等待绿色环境就绪
        for (String containerId : greenContainers) {
            waitForContainerReady(containerId, 120);
        }
        
        // 3. 切换流量到绿色环境（这里需要配合负载均衡器）
        switchTrafficToGreenEnvironment(config.getServiceName(), greenContainers);
        
        // 4. 停止蓝色环境（旧版本）
        List<ContainerInstance> blueInstances = instanceRepository.findByServiceNameAndStatus(
            config.getServiceName(), ContainerStatus.RUNNING);
        
        for (ContainerInstance instance : blueInstances) {
            if (!greenContainers.contains(instance.getContainerId())) {
                containerService.stopContainer(instance.getContainerId());
                containerService.removeContainer(instance.getContainerId(), true);
            }
        }
        
        record.setDeploymentLogs("蓝绿部署完成，绿色环境实例数: " + greenContainers.size());
    }
    
    /**
     * 金丝雀部署策略
     */
    private void executeCanaryDeployment(ContainerConfig config, String imageTag, DeploymentRecord record) {
        log.info("执行金丝雀部署: {}", config.getServiceName());
        
        // 1. 计算金丝雀实例数（10%流量）
        int totalInstances = config.getReplicaCount();
        int canaryInstances = Math.max(1, totalInstances / 10);
        
        // 2. 创建金丝雀实例
        config.setImageTag(imageTag);
        List<String> canaryContainers = new ArrayList<>();
        
        for (int i = 0; i < canaryInstances; i++) {
            String containerId = containerService.createContainer(config);
            containerService.startContainer(containerId);
            canaryContainers.add(containerId);
        }
        
        // 3. 配置流量分发（10%到金丝雀版本）
        configureCanaryTraffic(config.getServiceName(), canaryContainers, 10);
        
        record.setDeploymentLogs("金丝雀部署完成，金丝雀实例数: " + canaryInstances + "，流量比例: 10%");
    }
    
    /**
     * 回滚部署
     */
    public String rollback(String serviceName, String targetVersion, String rolledBackBy) {
        String rollbackId = UUID.randomUUID().toString();
        
        try {
            log.info("开始回滚服务: {} -> {}", serviceName, targetVersion);
            
            // 1. 获取目标版本的部署记录
            DeploymentRecord targetRecord = deploymentRepository
                .findByServiceNameAndImageTagAndStatus(serviceName, targetVersion, "SUCCESS")
                .orElseThrow(() -> new IllegalArgumentException("目标版本不存在或部署失败: " + targetVersion));
            
            // 2. 创建回滚记录
            DeploymentRecord rollbackRecord = new DeploymentRecord();
            rollbackRecord.setDeploymentId(rollbackId);
            rollbackRecord.setServiceName(serviceName);
            rollbackRecord.setImageName(targetRecord.getImageName());
            rollbackRecord.setImageTag(targetVersion);
            rollbackRecord.setEnvironment(targetRecord.getEnvironment());
            rollbackRecord.setDeploymentStrategy(DeploymentStrategy.ROLLING_UPDATE);
            rollbackRecord.setStartTime(LocalDateTime.now());
            rollbackRecord.setStatus("RUNNING");
            rollbackRecord.setDeployedBy(rolledBackBy);
            rollbackRecord.setRollbackVersion(targetVersion);
            rollbackRecord.setCreatedTime(LocalDateTime.now());
            deploymentRepository.save(rollbackRecord);
            
            // 3. 执行回滚（使用滚动更新策略）
            ContainerConfig config = configRepository.findByServiceName(serviceName)
                .orElseThrow(() -> new IllegalArgumentException("服务配置不存在: " + serviceName));
            
            executeRollingUpdateDeployment(config, targetVersion, rollbackRecord);
            
            // 4. 更新回滚状态
            rollbackRecord.setStatus("SUCCESS");
            rollbackRecord.setEndTime(LocalDateTime.now());
            deploymentRepository.save(rollbackRecord);
            
            log.info("回滚完成: {}", rollbackId);
            return rollbackId;
            
        } catch (Exception e) {
            log.error("回滚失败: {}", rollbackId, e);
            throw new RuntimeException("回滚失败: " + e.getMessage());
        }
    }
    
    // 辅助方法
    private DeploymentRecord createDeploymentRecord(String deploymentId, ContainerConfig config, 
                                                   String imageTag, DeploymentStrategy strategy, String deployedBy) {
        DeploymentRecord record = new DeploymentRecord();
        record.setDeploymentId(deploymentId);
        record.setServiceName(config.getServiceName());
        record.setImageName(config.getImageName());
        record.setImageTag(imageTag);
        record.setEnvironment(config.getEnvironment());
        record.setDeploymentStrategy(strategy);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("RUNNING");
        record.setDeployedBy(deployedBy);
        record.setCreatedTime(LocalDateTime.now());
        return record;
    }
    
    private void waitForContainerReady(String containerId, int timeoutSeconds) {
        int waited = 0;
        while (waited < timeoutSeconds) {
            try {
                ContainerStats stats = containerService.getContainerStats(containerId);
                if ("running".equals(stats.getStatus())) {
                    // 额外等待应用启动
                    Thread.sleep(5000);
                    return;
                }
                Thread.sleep(1000);
                waited++;
            } catch (Exception e) {
                log.warn("等待容器就绪时出错: {}", containerId, e);
                try {
                    Thread.sleep(1000);
                    waited++;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new RuntimeException("容器启动超时: " + containerId);
    }
    
    private void switchTrafficToGreenEnvironment(String serviceName, List<String> greenContainers) {
        // 这里需要配合负载均衡器（如Nginx、HAProxy等）实现流量切换
        log.info("切换流量到绿色环境: {} -> {}", serviceName, greenContainers);
        // 实际实现需要调用负载均衡器API
    }
    
    private void configureCanaryTraffic(String serviceName, List<String> canaryContainers, int percentage) {
        // 配置金丝雀流量分发
        log.info("配置金丝雀流量: {} -> {}%, 实例: {}", serviceName, percentage, canaryContainers);
        // 实际实现需要配置负载均衡器的权重分配
    }
}
```

## Docker Compose配置

### 1. NSRS应用完整部署

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: nsrs-mysql
    environment:
      MYSQL_ROOT_PASSWORD: nsrs123456
      MYSQL_DATABASE: nsrs
      MYSQL_USER: nsrs
      MYSQL_PASSWORD: nsrs123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    networks:
      - nsrs-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: nsrs-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
      - ./redis/redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf
    networks:
      - nsrs-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  # NSRS应用
  nsrs-app:
    build:
      context: .
      dockerfile: Dockerfile
    image: nsrs/sim-card-mgnt:latest
    container_name: nsrs-app
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/nsrs?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: nsrs
      SPRING_DATASOURCE_PASSWORD: nsrs123
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      JAVA_OPTS: -Xmx1g -Xms512m
    ports:
      - "8080:8080"
      - "8081:8081" # 管理端口
    volumes:
      - app_logs:/app/logs
      - app_data:/app/data
    networks:
      - nsrs-network
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  # Nginx负载均衡
  nginx:
    image: nginx:alpine
    container_name: nsrs-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./ssl:/etc/nginx/ssl
    networks:
      - nsrs-network
    depends_on:
      - nsrs-app
    restart: unless-stopped

  # Prometheus监控
  prometheus:
    image: prom/prometheus:latest
    container_name: nsrs-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    networks:
      - nsrs-network
    restart: unless-stopped

  # Grafana可视化
  grafana:
    image: grafana/grafana:latest
    container_name: nsrs-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin123
    volumes:
      - grafana_data:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    networks:
      - nsrs-network
    depends_on:
      - prometheus
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:
  app_logs:
  app_data:
  prometheus_data:
  grafana_data:

networks:
  nsrs-network:
    driver: bridge
```

### 2. Dockerfile优化

```dockerfile
# 多阶段构建Dockerfile
FROM openjdk:11-jdk-slim as builder

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖（利用Docker缓存）
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 运行时镜像
FROM openjdk:11-jre-slim

# 安装必要工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r nsrs && useradd -r -g nsrs nsrs

# 设置工作目录
WORKDIR /app

# 复制应用JAR
COPY --from=builder /app/target/nsrs-boot.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R nsrs:nsrs /app

# 切换到应用用户
USER nsrs

# 暴露端口
EXPOSE 8080 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 容器化部署脚本

### 1. 一键部署脚本

```bash
#!/bin/bash

# NSRS容器化部署脚本

set -e

echo "开始NSRS容器化部署..."

# 配置变量
APP_NAME="nsrs"
APP_VERSION="1.0.0"
ENVIRONMENT="production"
REGISTRY="registry.nsrs.com"

# 1. 构建应用镜像
echo "构建应用镜像..."
docker build -t ${REGISTRY}/${APP_NAME}:${APP_VERSION} .
docker tag ${REGISTRY}/${APP_NAME}:${APP_VERSION} ${REGISTRY}/${APP_NAME}:latest

# 2. 推送镜像到仓库
echo "推送镜像到仓库..."
docker push ${REGISTRY}/${APP_NAME}:${APP_VERSION}
docker push ${REGISTRY}/${APP_NAME}:latest

# 3. 创建网络
echo "创建Docker网络..."
docker network create nsrs-network || true

# 4. 启动基础服务
echo "启动基础服务..."
docker-compose -f docker-compose.base.yml up -d

# 等待基础服务就绪
echo "等待基础服务就绪..."
sleep 30

# 5. 启动应用服务
echo "启动应用服务..."
docker-compose -f docker-compose.app.yml up -d

# 6. 健康检查
echo "执行健康检查..."
for i in {1..30}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "应用启动成功！"
        break
    fi
    echo "等待应用启动... ($i/30)"
    sleep 10
done

# 7. 显示部署信息
echo "部署完成！"
echo "应用访问地址: http://localhost:8080"
echo "监控面板: http://localhost:3000 (admin/admin123)"
echo "Prometheus: http://localhost:9090"

# 8. 显示容器状态
echo "容器状态:"
docker-compose ps
```

### 2. 滚动更新脚本

```bash
#!/bin/bash

# NSRS滚动更新脚本

set -e

APP_NAME="nsrs"
NEW_VERSION=$1
REGISTRY="registry.nsrs.com"

if [ -z "$NEW_VERSION" ]; then
    echo "使用方法: $0 <新版本号>"
    exit 1
fi

echo "开始滚动更新到版本: $NEW_VERSION"

# 1. 拉取新镜像
echo "拉取新镜像..."
docker pull ${REGISTRY}/${APP_NAME}:${NEW_VERSION}

# 2. 获取当前运行的容器
CURRENT_CONTAINERS=$(docker ps --filter "label=service=${APP_NAME}" --format "{{.ID}}")
CONTAINER_COUNT=$(echo $CURRENT_CONTAINERS | wc -w)

echo "当前运行容器数: $CONTAINER_COUNT"

# 3. 逐个替换容器
for container_id in $CURRENT_CONTAINERS; do
    echo "替换容器: $container_id"
    
    # 启动新容器
    NEW_CONTAINER=$(docker run -d \
        --network nsrs-network \
        --label service=${APP_NAME} \
        --label version=${NEW_VERSION} \
        -e SPRING_PROFILES_ACTIVE=docker \
        ${REGISTRY}/${APP_NAME}:${NEW_VERSION})
    
    echo "新容器启动: $NEW_CONTAINER"
    
    # 等待新容器就绪
    echo "等待新容器就绪..."
    for i in {1..30}; do
        if docker exec $NEW_CONTAINER curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
            echo "新容器就绪"
            break
        fi
        sleep 5
    done
    
    # 停止旧容器
    echo "停止旧容器: $container_id"
    docker stop $container_id
    docker rm $container_id
    
    echo "容器替换完成，等待5秒..."
    sleep 5
done

echo "滚动更新完成！"
echo "新版本容器:"
docker ps --filter "label=service=${APP_NAME}"
```

### 3. 回滚脚本

```bash
#!/bin/bash

# NSRS回滚脚本

set -e

APP_NAME="nsrs"
TARGET_VERSION=$1
REGISTRY="registry.nsrs.com"

if [ -z "$TARGET_VERSION" ]; then
    echo "使用方法: $0 <目标版本号>"
    exit 1
fi

echo "开始回滚到版本: $TARGET_VERSION"

# 1. 检查目标版本镜像是否存在
if ! docker pull ${REGISTRY}/${APP_NAME}:${TARGET_VERSION}; then
    echo "错误: 目标版本镜像不存在"
    exit 1
fi

# 2. 备份当前版本信息
CURRENT_VERSION=$(docker ps --filter "label=service=${APP_NAME}" --format "{{.Label \"version\"}}" | head -1)
echo "当前版本: $CURRENT_VERSION"
echo "目标版本: $TARGET_VERSION"

# 3. 确认回滚
read -p "确认回滚? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "回滚取消"
    exit 1
fi

# 4. 执行回滚（使用滚动更新方式）
echo "执行回滚..."
./rolling-update.sh $TARGET_VERSION

echo "回滚完成！"
echo "如需再次回滚到 $CURRENT_VERSION，请执行:"
echo "./rollback.sh $CURRENT_VERSION"
```

## 容器化最佳实践

### 1. 镜像优化
- **多阶段构建**: 减少镜像大小
- **基础镜像选择**: 使用官方轻量级镜像
- **层缓存优化**: 合理安排Dockerfile指令顺序
- **安全扫描**: 定期扫描镜像漏洞

### 2. 容器配置
- **资源限制**: 设置CPU和内存限制
- **健康检查**: 配置应用健康检查
- **日志管理**: 统一日志收集和存储
- **环境变量**: 外部化配置管理

### 3. 部署策略
- **滚动更新**: 零停机部署
- **蓝绿部署**: 快速切换和回滚
- **金丝雀发布**: 渐进式发布验证
- **自动回滚**: 异常时自动回滚

### 4. 监控运维
- **容器监控**: 实时监控容器状态
- **日志聚合**: 集中化日志管理
- **告警机制**: 及时发现和处理问题
- **备份恢复**: 数据和配置备份策略

通过以上容器化部署方案，NSRS系统可以实现标准化、自动化的部署流程，提高部署效率和系统可靠性。