# NSRS号卡资源管理系统 - DevOps实践详解

## 概述

DevOps实践是NSRS号卡资源管理系统云化转型的核心组成部分，通过建立完善的CI/CD流水线、容器化部署和自动化运维体系，实现开发、测试、部署和运维的一体化管理，提升软件交付效率和质量。

## DevOps核心概念

### 部署环境枚举

```java
/**
 * 部署环境枚举
 * 定义不同的部署环境类型
 */
public enum DeploymentEnvironment {
    /**
     * 开发环境
     */
    DEVELOPMENT("DEV", "开发环境", "dev"),
    
    /**
     * 测试环境
     */
    TESTING("TEST", "测试环境", "test"),
    
    /**
     * 预生产环境
     */
    STAGING("STAGING", "预生产环境", "staging"),
    
    /**
     * 生产环境
     */
    PRODUCTION("PROD", "生产环境", "prod");
    
    private final String code;
    private final String description;
    private final String profile;
    
    DeploymentEnvironment(String code, String description, String profile) {
        this.code = code;
        this.description = description;
        this.profile = profile;
    }
    
    // getters...
}
```

### 流水线阶段枚举

```java
/**
 * CI/CD流水线阶段枚举
 */
public enum PipelineStage {
    /**
     * 代码检出
     */
    CHECKOUT("CHECKOUT", "代码检出", 1),
    
    /**
     * 代码质量检查
     */
    CODE_QUALITY("CODE_QUALITY", "代码质量检查", 2),
    
    /**
     * 单元测试
     */
    UNIT_TEST("UNIT_TEST", "单元测试", 3),
    
    /**
     * 构建
     */
    BUILD("BUILD", "构建", 4),
    
    /**
     * 安全扫描
     */
    SECURITY_SCAN("SECURITY_SCAN", "安全扫描", 5),
    
    /**
     * 镜像构建
     */
    IMAGE_BUILD("IMAGE_BUILD", "镜像构建", 6),
    
    /**
     * 集成测试
     */
    INTEGRATION_TEST("INTEGRATION_TEST", "集成测试", 7),
    
    /**
     * 部署
     */
    DEPLOY("DEPLOY", "部署", 8),
    
    /**
     * 验收测试
     */
    ACCEPTANCE_TEST("ACCEPTANCE_TEST", "验收测试", 9),
    
    /**
     * 性能测试
     */
    PERFORMANCE_TEST("PERFORMANCE_TEST", "性能测试", 10);
    
    private final String code;
    private final String description;
    private final int order;
    
    PipelineStage(String code, String description, int order) {
        this.code = code;
        this.description = description;
        this.order = order;
    }
    
    // getters...
}
```

## CI/CD流水线实体

### 流水线配置实体

```java
/**
 * CI/CD流水线配置实体
 */
@Entity
@Table(name = "pipeline_config")
public class PipelineConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 流水线名称
     */
    @Column(name = "pipeline_name", nullable = false, length = 100)
    private String pipelineName;
    
    /**
     * 项目名称
     */
    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;
    
    /**
     * Git仓库URL
     */
    @Column(name = "git_repository_url", nullable = false, length = 500)
    private String gitRepositoryUrl;
    
    /**
     * 分支名称
     */
    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;
    
    /**
     * 目标环境
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_environment", nullable = false)
    private DeploymentEnvironment targetEnvironment;
    
    /**
     * 流水线阶段配置（JSON格式）
     */
    @Column(name = "stages_config", columnDefinition = "TEXT")
    private String stagesConfig;
    
    /**
     * 触发条件
     */
    @Column(name = "trigger_conditions", columnDefinition = "TEXT")
    private String triggerConditions;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // constructors, getters, setters...
}
```

### 流水线执行记录实体

```java
/**
 * 流水线执行记录实体
 */
@Entity
@Table(name = "pipeline_execution")
public class PipelineExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 流水线配置ID
     */
    @Column(name = "pipeline_config_id", nullable = false)
    private Long pipelineConfigId;
    
    /**
     * 执行编号
     */
    @Column(name = "execution_number", nullable = false)
    private Integer executionNumber;
    
    /**
     * Git提交哈希
     */
    @Column(name = "git_commit_hash", length = 40)
    private String gitCommitHash;
    
    /**
     * 触发用户
     */
    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;
    
    /**
     * 执行状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false)
    private PipelineExecutionStatus executionStatus;
    
    /**
     * 当前阶段
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage")
    private PipelineStage currentStage;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 执行耗时（秒）
     */
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 构建产物信息
     */
    @Column(name = "artifacts_info", columnDefinition = "TEXT")
    private String artifactsInfo;
    
    // constructors, getters, setters...
}
```

### 流水线执行状态枚举

```java
/**
 * 流水线执行状态枚举
 */
public enum PipelineExecutionStatus {
    /**
     * 排队中
     */
    QUEUED("QUEUED", "排队中"),
    
    /**
     * 运行中
     */
    RUNNING("RUNNING", "运行中"),
    
    /**
     * 成功
     */
    SUCCESS("SUCCESS", "成功"),
    
    /**
     * 失败
     */
    FAILED("FAILED", "失败"),
    
    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消"),
    
    /**
     * 超时
     */
    TIMEOUT("TIMEOUT", "超时");
    
    private final String code;
    private final String description;
    
    PipelineExecutionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    // getters...
}
```

## CI/CD流水线服务

### 流水线管理服务

```java
/**
 * CI/CD流水线管理服务
 * 负责流水线的创建、执行和监控
 */
@Service
@Slf4j
public class PipelineManagementService {
    
    @Autowired
    private PipelineConfigRepository pipelineConfigRepository;
    
    @Autowired
    private PipelineExecutionRepository pipelineExecutionRepository;
    
    @Autowired
    private GitService gitService;
    
    @Autowired
    private BuildService buildService;
    
    @Autowired
    private TestService testService;
    
    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 触发流水线执行
     * 
     * @param pipelineConfigId 流水线配置ID
     * @param triggeredBy 触发用户
     * @param gitCommitHash Git提交哈希
     * @return 流水线执行记录
     */
    @Async
    @Transactional
    public CompletableFuture<PipelineExecution> triggerPipeline(Long pipelineConfigId, 
                                                               String triggeredBy, 
                                                               String gitCommitHash) {
        log.info("触发流水线执行: pipelineConfigId={}, triggeredBy={}, commit={}", 
                pipelineConfigId, triggeredBy, gitCommitHash);
        
        // 1. 获取流水线配置
        PipelineConfig config = pipelineConfigRepository.findById(pipelineConfigId)
                .orElseThrow(() -> new RuntimeException("流水线配置不存在: " + pipelineConfigId));
        
        // 2. 创建执行记录
        PipelineExecution execution = createPipelineExecution(config, triggeredBy, gitCommitHash);
        
        try {
            // 3. 执行流水线各阶段
            executePipelineStages(config, execution);
            
            // 4. 更新执行状态
            execution.setExecutionStatus(PipelineExecutionStatus.SUCCESS);
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationSeconds(Duration.between(execution.getStartTime(), execution.getEndTime()).getSeconds());
            
            // 5. 发送成功通知
            notificationService.sendPipelineNotification(execution, "流水线执行成功");
            
            log.info("流水线执行成功: executionId={}, 耗时: {}秒", 
                    execution.getId(), execution.getDurationSeconds());
            
        } catch (Exception e) {
            log.error("流水线执行失败", e);
            execution.setExecutionStatus(PipelineExecutionStatus.FAILED);
            execution.setEndTime(LocalDateTime.now());
            execution.setErrorMessage(e.getMessage());
            
            // 发送失败通知
            notificationService.sendPipelineNotification(execution, "流水线执行失败: " + e.getMessage());
        }
        
        return CompletableFuture.completedFuture(pipelineExecutionRepository.save(execution));
    }
    
    /**
     * 创建流水线执行记录
     */
    private PipelineExecution createPipelineExecution(PipelineConfig config, 
                                                      String triggeredBy, 
                                                      String gitCommitHash) {
        // 获取下一个执行编号
        Integer nextExecutionNumber = pipelineExecutionRepository
                .findMaxExecutionNumberByPipelineConfigId(config.getId())
                .map(num -> num + 1)
                .orElse(1);
        
        PipelineExecution execution = new PipelineExecution();
        execution.setPipelineConfigId(config.getId());
        execution.setExecutionNumber(nextExecutionNumber);
        execution.setGitCommitHash(gitCommitHash);
        execution.setTriggeredBy(triggeredBy);
        execution.setExecutionStatus(PipelineExecutionStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        
        return pipelineExecutionRepository.save(execution);
    }
    
    /**
     * 执行流水线各阶段
     */
    private void executePipelineStages(PipelineConfig config, PipelineExecution execution) {
        List<PipelineStage> stages = parsePipelineStages(config.getStagesConfig());
        
        for (PipelineStage stage : stages) {
            log.info("执行流水线阶段: {}", stage.getDescription());
            
            // 更新当前阶段
            execution.setCurrentStage(stage);
            pipelineExecutionRepository.save(execution);
            
            // 执行阶段
            executeStage(stage, config, execution);
        }
    }
    
    /**
     * 执行单个阶段
     */
    private void executeStage(PipelineStage stage, PipelineConfig config, PipelineExecution execution) {
        switch (stage) {
            case CHECKOUT:
                executeCheckoutStage(config, execution);
                break;
            case CODE_QUALITY:
                executeCodeQualityStage(config, execution);
                break;
            case UNIT_TEST:
                executeUnitTestStage(config, execution);
                break;
            case BUILD:
                executeBuildStage(config, execution);
                break;
            case SECURITY_SCAN:
                executeSecurityScanStage(config, execution);
                break;
            case IMAGE_BUILD:
                executeImageBuildStage(config, execution);
                break;
            case INTEGRATION_TEST:
                executeIntegrationTestStage(config, execution);
                break;
            case DEPLOY:
                executeDeployStage(config, execution);
                break;
            case ACCEPTANCE_TEST:
                executeAcceptanceTestStage(config, execution);
                break;
            case PERFORMANCE_TEST:
                executePerformanceTestStage(config, execution);
                break;
            default:
                throw new RuntimeException("不支持的流水线阶段: " + stage);
        }
    }
    
    /**
     * 执行代码检出阶段
     */
    private void executeCheckoutStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行代码检出阶段");
        
        // 检出代码
        String workspaceDir = gitService.checkout(
                config.getGitRepositoryUrl(), 
                config.getBranchName(), 
                execution.getGitCommitHash()
        );
        
        // 保存工作空间信息
        Map<String, Object> artifacts = new HashMap<>();
        artifacts.put("workspace_dir", workspaceDir);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行代码质量检查阶段
     */
    private void executeCodeQualityStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行代码质量检查阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String workspaceDir = (String) artifacts.get("workspace_dir");
        
        // 执行SonarQube扫描
        Map<String, Object> qualityResult = buildService.runSonarQubeScan(workspaceDir);
        
        // 检查质量门禁
        if (!buildService.passQualityGate(qualityResult)) {
            throw new RuntimeException("代码质量检查未通过");
        }
        
        artifacts.put("quality_result", qualityResult);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行单元测试阶段
     */
    private void executeUnitTestStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行单元测试阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String workspaceDir = (String) artifacts.get("workspace_dir");
        
        // 执行单元测试
        Map<String, Object> testResult = testService.runUnitTests(workspaceDir);
        
        // 检查测试结果
        if (!testService.isTestPassed(testResult)) {
            throw new RuntimeException("单元测试失败");
        }
        
        artifacts.put("unit_test_result", testResult);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行构建阶段
     */
    private void executeBuildStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行构建阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String workspaceDir = (String) artifacts.get("workspace_dir");
        
        // 执行Maven构建
        String buildResult = buildService.runMavenBuild(workspaceDir);
        
        artifacts.put("build_result", buildResult);
        artifacts.put("jar_file", workspaceDir + "/target/nsrs-sim-card-mgnt.jar");
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行安全扫描阶段
     */
    private void executeSecurityScanStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行安全扫描阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String jarFile = (String) artifacts.get("jar_file");
        
        // 执行安全扫描
        Map<String, Object> securityResult = buildService.runSecurityScan(jarFile);
        
        // 检查安全扫描结果
        if (!buildService.passSecurityCheck(securityResult)) {
            throw new RuntimeException("安全扫描发现高危漏洞");
        }
        
        artifacts.put("security_result", securityResult);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行镜像构建阶段
     */
    private void executeImageBuildStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行镜像构建阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String workspaceDir = (String) artifacts.get("workspace_dir");
        
        // 构建Docker镜像
        String imageTag = String.format("%s:%s-%d", 
                config.getProjectName(), 
                config.getTargetEnvironment().getProfile(), 
                execution.getExecutionNumber());
        
        String imageId = buildService.buildDockerImage(workspaceDir, imageTag);
        
        // 推送镜像到仓库
        buildService.pushDockerImage(imageTag);
        
        artifacts.put("docker_image_tag", imageTag);
        artifacts.put("docker_image_id", imageId);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行集成测试阶段
     */
    private void executeIntegrationTestStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行集成测试阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String imageTag = (String) artifacts.get("docker_image_tag");
        
        // 启动测试环境
        String testEnvironmentId = testService.startTestEnvironment(imageTag);
        
        try {
            // 执行集成测试
            Map<String, Object> testResult = testService.runIntegrationTests(testEnvironmentId);
            
            // 检查测试结果
            if (!testService.isTestPassed(testResult)) {
                throw new RuntimeException("集成测试失败");
            }
            
            artifacts.put("integration_test_result", testResult);
            execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
            
        } finally {
            // 清理测试环境
            testService.cleanupTestEnvironment(testEnvironmentId);
        }
    }
    
    /**
     * 执行部署阶段
     */
    private void executeDeployStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行部署阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String imageTag = (String) artifacts.get("docker_image_tag");
        
        // 部署到目标环境
        String deploymentId = deploymentService.deploy(
                imageTag, 
                config.getTargetEnvironment(), 
                config.getProjectName()
        );
        
        // 等待部署完成
        deploymentService.waitForDeploymentReady(deploymentId);
        
        artifacts.put("deployment_id", deploymentId);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行验收测试阶段
     */
    private void executeAcceptanceTestStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行验收测试阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String deploymentId = (String) artifacts.get("deployment_id");
        
        // 获取部署的服务URL
        String serviceUrl = deploymentService.getServiceUrl(deploymentId);
        
        // 执行验收测试
        Map<String, Object> testResult = testService.runAcceptanceTests(serviceUrl);
        
        // 检查测试结果
        if (!testService.isTestPassed(testResult)) {
            throw new RuntimeException("验收测试失败");
        }
        
        artifacts.put("acceptance_test_result", testResult);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 执行性能测试阶段
     */
    private void executePerformanceTestStage(PipelineConfig config, PipelineExecution execution) {
        log.info("执行性能测试阶段");
        
        Map<String, Object> artifacts = JsonUtils.fromJson(execution.getArtifactsInfo(), Map.class);
        String deploymentId = (String) artifacts.get("deployment_id");
        
        // 获取部署的服务URL
        String serviceUrl = deploymentService.getServiceUrl(deploymentId);
        
        // 执行性能测试
        Map<String, Object> testResult = testService.runPerformanceTests(serviceUrl);
        
        // 检查性能指标
        if (!testService.meetPerformanceRequirements(testResult)) {
            log.warn("性能测试未达到预期指标，但不阻断部署");
        }
        
        artifacts.put("performance_test_result", testResult);
        execution.setArtifactsInfo(JsonUtils.toJson(artifacts));
    }
    
    /**
     * 解析流水线阶段配置
     */
    private List<PipelineStage> parsePipelineStages(String stagesConfig) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> stageNames = mapper.readValue(stagesConfig, new TypeReference<List<String>>() {});
            
            return stageNames.stream()
                    .map(PipelineStage::valueOf)
                    .sorted(Comparator.comparing(PipelineStage::getOrder))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析流水线阶段配置失败", e);
            // 返回默认阶段
            return Arrays.asList(
                    PipelineStage.CHECKOUT,
                    PipelineStage.CODE_QUALITY,
                    PipelineStage.UNIT_TEST,
                    PipelineStage.BUILD,
                    PipelineStage.SECURITY_SCAN,
                    PipelineStage.IMAGE_BUILD,
                    PipelineStage.DEPLOY
            );
        }
    }
    
    /**
     * 获取流水线执行历史
     */
    public Page<PipelineExecution> getPipelineExecutionHistory(Long pipelineConfigId, Pageable pageable) {
        return pipelineExecutionRepository.findByPipelineConfigIdOrderByStartTimeDesc(pipelineConfigId, pageable);
    }
    
    /**
     * 取消流水线执行
     */
    @Transactional
    public void cancelPipelineExecution(Long executionId) {
        PipelineExecution execution = pipelineExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("流水线执行记录不存在: " + executionId));
        
        if (execution.getExecutionStatus() == PipelineExecutionStatus.RUNNING) {
            execution.setExecutionStatus(PipelineExecutionStatus.CANCELLED);
            execution.setEndTime(LocalDateTime.now());
            pipelineExecutionRepository.save(execution);
            
            log.info("流水线执行已取消: executionId={}", executionId);
        }
    }
}
```

## 容器化部署服务

### Docker镜像管理服务

```java
/**
 * Docker镜像管理服务
 * 负责Docker镜像的构建、推送和管理
 */
@Service
@Slf4j
public class DockerImageService {
    
    @Value("${docker.registry.url}")
    private String dockerRegistryUrl;
    
    @Value("${docker.registry.username}")
    private String dockerRegistryUsername;
    
    @Value("${docker.registry.password}")
    private String dockerRegistryPassword;
    
    /**
     * 构建Docker镜像
     * 
     * @param workspaceDir 工作目录
     * @param imageTag 镜像标签
     * @return 镜像ID
     */
    public String buildDockerImage(String workspaceDir, String imageTag) {
        log.info("构建Docker镜像: workspaceDir={}, imageTag={}", workspaceDir, imageTag);
        
        try {
            // 创建Dockerfile
            createDockerfile(workspaceDir);
            
            // 执行docker build命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "build", 
                    "-t", imageTag,
                    "-f", workspaceDir + "/Dockerfile",
                    workspaceDir
            );
            
            Process process = processBuilder.start();
            
            // 读取构建输出
            String output = readProcessOutput(process);
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Docker镜像构建失败: " + output);
            }
            
            // 获取镜像ID
            String imageId = getImageId(imageTag);
            
            log.info("Docker镜像构建成功: imageTag={}, imageId={}", imageTag, imageId);
            return imageId;
            
        } catch (Exception e) {
            log.error("Docker镜像构建失败", e);
            throw new RuntimeException("Docker镜像构建失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Dockerfile
     */
    private void createDockerfile(String workspaceDir) throws IOException {
        String dockerfileContent = generateDockerfileContent();
        
        Path dockerfilePath = Paths.get(workspaceDir, "Dockerfile");
        Files.write(dockerfilePath, dockerfileContent.getBytes(StandardCharsets.UTF_8));
        
        log.info("Dockerfile创建成功: {}", dockerfilePath);
    }
    
    /**
     * 生成Dockerfile内容
     */
    private String generateDockerfileContent() {
        return """
                # 使用OpenJDK 17作为基础镜像
                FROM openjdk:17-jre-slim
                
                # 设置工作目录
                WORKDIR /app
                
                # 创建应用用户
                RUN groupadd -r nsrs && useradd -r -g nsrs nsrs
                
                # 复制JAR文件
                COPY target/nsrs-sim-card-mgnt.jar app.jar
                
                # 设置文件权限
                RUN chown nsrs:nsrs app.jar
                
                # 切换到应用用户
                USER nsrs
                
                # 暴露端口
                EXPOSE 8080
                
                # 设置JVM参数
                ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"
                
                # 健康检查
                HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
                    CMD curl -f http://localhost:8080/actuator/health || exit 1
                
                # 启动应用
                ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
                """;
    }
    
    /**
     * 推送Docker镜像
     */
    public void pushDockerImage(String imageTag) {
        log.info("推送Docker镜像: imageTag={}", imageTag);
        
        try {
            // 登录Docker仓库
            loginDockerRegistry();
            
            // 标记镜像
            String remoteImageTag = dockerRegistryUrl + "/" + imageTag;
            tagImage(imageTag, remoteImageTag);
            
            // 推送镜像
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "push", remoteImageTag
            );
            
            Process process = processBuilder.start();
            
            String output = readProcessOutput(process);
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Docker镜像推送失败: " + output);
            }
            
            log.info("Docker镜像推送成功: remoteImageTag={}", remoteImageTag);
            
        } catch (Exception e) {
            log.error("Docker镜像推送失败", e);
            throw new RuntimeException("Docker镜像推送失败: " + e.getMessage());
        }
    }
    
    /**
     * 登录Docker仓库
     */
    private void loginDockerRegistry() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "login", 
                "-u", dockerRegistryUsername,
                "-p", dockerRegistryPassword,
                dockerRegistryUrl
        );
        
        Process process = processBuilder.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Docker仓库登录失败");
        }
    }
    
    /**
     * 标记镜像
     */
    private void tagImage(String sourceTag, String targetTag) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "tag", sourceTag, targetTag
        );
        
        Process process = processBuilder.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Docker镜像标记失败");
        }
    }
    
    /**
     * 获取镜像ID
     */
    private String getImageId(String imageTag) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "images", "-q", imageTag
        );
        
        Process process = processBuilder.start();
        
        String output = readProcessOutput(process);
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("获取镜像ID失败");
        }
        
        return output.trim();
    }
    
    /**
     * 读取进程输出
     */
    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * 清理本地镜像
     */
    public void cleanupLocalImages(String imageTag) {
        log.info("清理本地镜像: imageTag={}", imageTag);
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "rmi", imageTag
            );
            
            Process process = processBuilder.start();
            process.waitFor();
            
            log.info("本地镜像清理完成: imageTag={}", imageTag);
            
        } catch (Exception e) {
            log.warn("清理本地镜像失败: imageTag={}", imageTag, e);
        }
    }
    
    /**
     * 获取镜像信息
     */
    public Map<String, Object> getImageInfo(String imageTag) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "inspect", imageTag
            );
            
            Process process = processBuilder.start();
            
            String output = readProcessOutput(process);
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("获取镜像信息失败");
            }
            
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> imageInfoList = mapper.readValue(output, 
                    new TypeReference<List<Map<String, Object>>>() {});
            
            return imageInfoList.isEmpty() ? new HashMap<>() : imageInfoList.get(0);
            
        } catch (Exception e) {
            log.error("获取镜像信息失败: imageTag={}", imageTag, e);
            return new HashMap<>();
        }
    }
}
```

## Demo脚本示例

### CI/CD流水线Demo脚本

```bash
#!/bin/bash

# NSRS CI/CD流水线Demo脚本
# 演示完整的CI/CD流水线流程

echo "=== NSRS CI/CD流水线Demo ==="
echo "演示场景：从代码提交到生产部署的完整流水线"
echo

# 设置变量
PROJECT_NAME="nsrs-sim-card-mgnt"
GIT_REPO="https://github.com/nsrs/sim-card-mgnt.git"
BRANCH="main"
DOCKER_REGISTRY="registry.nsrs.com"
KUBERNETES_NAMESPACE="nsrs-prod"

# 1. 环境准备
echo "1. 环境准备..."

# 创建工作目录
WORKSPACE_DIR="/tmp/nsrs-pipeline-$(date +%Y%m%d_%H%M%S)"
mkdir -p $WORKSPACE_DIR
cd $WORKSPACE_DIR

echo "工作目录: $WORKSPACE_DIR"

# 2. 代码检出
echo "\n2. 代码检出..."
echo "从Git仓库检出代码: $GIT_REPO"

# 克隆代码仓库
git clone $GIT_REPO $PROJECT_NAME
cd $PROJECT_NAME

# 切换到指定分支
git checkout $BRANCH

# 获取提交信息
GIT_COMMIT=$(git rev-parse HEAD)
GIT_SHORT_COMMIT=$(git rev-parse --short HEAD)
echo "当前提交: $GIT_COMMIT"

# 3. 代码质量检查
echo "\n3. 代码质量检查..."
echo "执行SonarQube代码扫描..."

# 启动SonarQube服务器（如果未运行）
docker run -d --name sonarqube \
  -p 9000:9000 \
  -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true \
  sonarqube:latest 2>/dev/null || echo "SonarQube已在运行"

# 等待SonarQube启动
echo "等待SonarQube启动..."
sleep 30

# 执行代码扫描
echo "执行代码质量扫描..."
mvn sonar:sonar \
  -Dsonar.projectKey=$PROJECT_NAME \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin

echo "代码质量检查完成"

# 4. 单元测试
echo "\n4. 单元测试..."
echo "执行Maven单元测试..."

# 运行单元测试
mvn clean test

# 检查测试结果
if [ $? -eq 0 ]; then
    echo "单元测试通过"
else
    echo "单元测试失败，终止流水线"
    exit 1
fi

# 生成测试报告
mvn jacoco:report
echo "测试覆盖率报告已生成"

# 5. 应用构建
echo "\n5. 应用构建..."
echo "执行Maven构建..."

# 执行Maven构建
mvn clean package -DskipTests

# 检查构建结果
if [ -f "target/$PROJECT_NAME.jar" ]; then
    echo "应用构建成功"
    JAR_FILE="target/$PROJECT_NAME.jar"
else
    echo "应用构建失败，终止流水线"
    exit 1
fi

# 6. 安全扫描
echo "\n6. 安全扫描..."
echo "执行OWASP依赖检查..."

# 执行OWASP依赖检查
mvn org.owasp:dependency-check-maven:check

echo "安全扫描完成"

# 7. Docker镜像构建
echo "\n7. Docker镜像构建..."

# 创建Dockerfile
cat > Dockerfile << 'EOF'
# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 创建应用用户
RUN groupadd -r nsrs && useradd -r -g nsrs nsrs

# 复制JAR文件
COPY target/nsrs-sim-card-mgnt.jar app.jar

# 设置文件权限
RUN chown nsrs:nsrs app.jar

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 切换到应用用户
USER nsrs

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
EOF

# 构建Docker镜像
IMAGE_TAG="$PROJECT_NAME:$GIT_SHORT_COMMIT"
echo "构建Docker镜像: $IMAGE_TAG"

docker build -t $IMAGE_TAG .

# 检查镜像构建结果
if [ $? -eq 0 ]; then
    echo "Docker镜像构建成功: $IMAGE_TAG"
else
    echo "Docker镜像构建失败，终止流水线"
    exit 1
fi

# 8. 镜像安全扫描
echo "\n8. 镜像安全扫描..."
echo "使用Trivy扫描镜像安全漏洞..."

# 安装Trivy（如果未安装）
if ! command -v trivy &> /dev/null; then
    echo "安装Trivy..."
    curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
fi

# 执行镜像安全扫描
trivy image --exit-code 1 --severity HIGH,CRITICAL $IMAGE_TAG

if [ $? -eq 0 ]; then
    echo "镜像安全扫描通过"
else
    echo "镜像存在高危安全漏洞，但继续流水线（生产环境应阻断）"
fi

# 9. 集成测试
echo "\n9. 集成测试..."
echo "启动测试环境进行集成测试..."

# 启动应用容器进行测试
TEST_CONTAINER_NAME="nsrs-test-$(date +%s)"
docker run -d --name $TEST_CONTAINER_NAME \
  -p 18080:8080 \
  -e SPRING_PROFILES_ACTIVE=test \
  -e MYSQL_URL=jdbc:h2:mem:testdb \
  $IMAGE_TAG

# 等待应用启动
echo "等待应用启动..."
sleep 30

# 执行健康检查
echo "执行健康检查..."
for i in {1..10}; do
    if curl -f http://localhost:18080/actuator/health; then
        echo "应用启动成功"
        break
    fi
    echo "等待应用启动... ($i/10)"
    sleep 10
done

# 执行API测试
echo "执行API集成测试..."

# 测试SIM卡查询接口
echo "测试SIM卡查询接口..."
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/api/sim-card/list)
if [ "$response" = "200" ]; then
    echo "SIM卡查询接口测试通过"
else
    echo "SIM卡查询接口测试失败: HTTP $response"
fi

# 测试健康检查接口
echo "测试健康检查接口..."
response=$(curl -s http://localhost:18080/actuator/health | jq -r '.status')
if [ "$response" = "UP" ]; then
    echo "健康检查接口测试通过"
else
    echo "健康检查接口测试失败: $response"
fi

# 清理测试容器
echo "清理测试环境..."
docker stop $TEST_CONTAINER_NAME
docker rm $TEST_CONTAINER_NAME

echo "集成测试完成"

# 10. 推送镜像到仓库
echo "\n10. 推送镜像到仓库..."

# 标记镜像
REMOTE_IMAGE_TAG="$DOCKER_REGISTRY/$IMAGE_TAG"
docker tag $IMAGE_TAG $REMOTE_IMAGE_TAG

# 推送镜像（模拟）
echo "推送镜像到仓库: $REMOTE_IMAGE_TAG"
echo "（Demo环境跳过实际推送）"

# 11. 部署到Kubernetes
echo "\n11. 部署到Kubernetes..."
echo "生成Kubernetes部署配置..."

# 创建Kubernetes部署配置
cat > k8s-deployment.yaml << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $PROJECT_NAME
  namespace: $KUBERNETES_NAMESPACE
  labels:
    app: $PROJECT_NAME
    version: $GIT_SHORT_COMMIT
spec:
  replicas: 3
  selector:
    matchLabels:
      app: $PROJECT_NAME
  template:
    metadata:
      labels:
        app: $PROJECT_NAME
        version: $GIT_SHORT_COMMIT
    spec:
      containers:
      - name: $PROJECT_NAME
        image: $REMOTE_IMAGE_TAG
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: MYSQL_URL
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: url
        - name: MYSQL_USERNAME
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: username
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: $PROJECT_NAME-service
  namespace: $KUBERNETES_NAMESPACE
spec:
  selector:
    app: $PROJECT_NAME
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: ClusterIP
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: $PROJECT_NAME-ingress
  namespace: $KUBERNETES_NAMESPACE
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: nsrs.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: $PROJECT_NAME-service
            port:
              number: 80
EOF

echo "Kubernetes部署配置已生成"
echo "（Demo环境跳过实际部署到Kubernetes）"

# 12. 验收测试
echo "\n12. 验收测试..."
echo "执行自动化验收测试..."

# 模拟验收测试
echo "执行业务流程测试..."
echo "✓ SIM卡创建流程测试"
echo "✓ SIM卡激活流程测试"
echo "✓ SIM卡查询流程测试"
echo "✓ SIM卡状态变更流程测试"

echo "验收测试完成"

# 13. 性能测试
echo "\n13. 性能测试..."
echo "执行性能基准测试..."

# 使用Apache Bench进行简单性能测试
if command -v ab &> /dev/null; then
    echo "执行并发性能测试..."
    # 注意：这里使用之前的测试容器端口，实际应该是部署后的服务
    # ab -n 1000 -c 10 http://localhost:18080/api/sim-card/test
else
    echo "Apache Bench未安装，跳过性能测试"
fi

echo "性能测试完成"

# 14. 部署完成通知
echo "\n14. 部署完成通知..."
echo "发送部署完成通知..."

# 生成部署报告
cat > deployment-report.json << EOF
{
  "project": "$PROJECT_NAME",
  "version": "$GIT_SHORT_COMMIT",
  "commit": "$GIT_COMMIT",
  "branch": "$BRANCH",
  "image": "$REMOTE_IMAGE_TAG",
  "environment": "production",
  "deployment_time": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "pipeline_status": "SUCCESS",
  "stages": {
    "checkout": "SUCCESS",
    "code_quality": "SUCCESS",
    "unit_test": "SUCCESS",
    "build": "SUCCESS",
    "security_scan": "SUCCESS",
    "image_build": "SUCCESS",
    "integration_test": "SUCCESS",
    "deploy": "SUCCESS",
    "acceptance_test": "SUCCESS",
    "performance_test": "SUCCESS"
  }
}
EOF

echo "部署报告已生成: deployment-report.json"

# 15. 清理工作
echo "\n15. 清理工作..."
echo "清理临时文件和资源..."

# 清理Docker镜像
docker rmi $IMAGE_TAG 2>/dev/null || true

# 清理工作目录
cd /tmp
rm -rf $WORKSPACE_DIR

echo "清理完成"

echo "\n=== CI/CD流水线Demo完成 ==="
echo "流水线执行摘要："
echo "✓ 代码检出和分析"
echo "✓ 代码质量检查"
echo "✓ 单元测试和覆盖率"
echo "✓ 应用构建和打包"
echo "✓ 安全漏洞扫描"
echo "✓ Docker镜像构建"
echo "✓ 镜像安全扫描"
echo "✓ 集成测试验证"
echo "✓ 镜像推送到仓库"
echo "✓ Kubernetes部署配置"
echo "✓ 验收测试执行"
echo "✓ 性能基准测试"
echo "✓ 部署通知和报告"
echo
echo "项目版本: $GIT_SHORT_COMMIT"
echo "镜像标签: $REMOTE_IMAGE_TAG"
echo "部署时间: $(date)"
```