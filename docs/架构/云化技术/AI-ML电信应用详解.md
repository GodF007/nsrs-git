# NSRS号卡资源管理系统 - AI/ML在电信领域应用详解

## 概述

AI/ML技术在电信领域的应用正在革命性地改变传统的运营模式，通过智能化的数据分析、预测建模和自动化决策，为NSRS号卡资源管理系统提供更加精准、高效的业务洞察和优化方案。

## AI/ML核心概念

### 机器学习模型类型枚举

```java
/**
 * 机器学习模型类型枚举
 * 定义不同的ML模型类别和应用场景
 */
public enum MLModelType {
    /**
     * 监督学习 - 分类
     */
    SUPERVISED_CLASSIFICATION("SUPERVISED_CLASSIFICATION", "监督学习-分类", "用于分类预测任务"),
    
    /**
     * 监督学习 - 回归
     */
    SUPERVISED_REGRESSION("SUPERVISED_REGRESSION", "监督学习-回归", "用于数值预测任务"),
    
    /**
     * 无监督学习 - 聚类
     */
    UNSUPERVISED_CLUSTERING("UNSUPERVISED_CLUSTERING", "无监督学习-聚类", "用于数据聚类分析"),
    
    /**
     * 无监督学习 - 异常检测
     */
    UNSUPERVISED_ANOMALY_DETECTION("UNSUPERVISED_ANOMALY_DETECTION", "无监督学习-异常检测", "用于异常行为识别"),
    
    /**
     * 强化学习
     */
    REINFORCEMENT_LEARNING("REINFORCEMENT_LEARNING", "强化学习", "用于策略优化和决策"),
    
    /**
     * 深度学习
     */
    DEEP_LEARNING("DEEP_LEARNING", "深度学习", "用于复杂模式识别"),
    
    /**
     * 时间序列预测
     */
    TIME_SERIES_FORECASTING("TIME_SERIES_FORECASTING", "时间序列预测", "用于时序数据预测"),
    
    /**
     * 自然语言处理
     */
    NATURAL_LANGUAGE_PROCESSING("NATURAL_LANGUAGE_PROCESSING", "自然语言处理", "用于文本分析和理解");
    
    private final String code;
    private final String description;
    private final String detail;
    
    MLModelType(String code, String description, String detail) {
        this.code = code;
        this.description = description;
        this.detail = detail;
    }
    
    // getters...
}
```

### 欺诈检测服务

```java
/**
 * 欺诈检测服务
 * 基于AI/ML技术实时检测欺诈行为
 */
@Service
@Slf4j
public class FraudDetectionService {
    
    @Autowired
    private MLModelManagementService modelManagementService;
    
    @Autowired
    private RealTimeEventProcessor eventProcessor;
    
    @Autowired
    private FraudAlertService alertService;
    
    /**
     * 实时欺诈检测
     * 
     * @param transactionEvent 交易事件
     * @return 欺诈检测结果
     */
    public FraudDetectionResult detectFraud(TransactionEvent transactionEvent) {
        log.debug("执行欺诈检测: imsi={}, type={}", 
                transactionEvent.getImsi(), transactionEvent.getTransactionType());
        
        try {
            // 1. 提取欺诈检测特征
            Map<String, Object> fraudFeatures = extractFraudFeatures(transactionEvent);
            
            // 2. 使用欺诈检测模型
            Long fraudModelId = getModelIdByScenario(AIApplicationScenario.FRAUD_DETECTION);
            ModelPredictionResult fraudResult = modelManagementService.predict(
                    fraudModelId, fraudFeatures, 
                    Map.of("imsi", transactionEvent.getImsi(), 
                           "transactionId", transactionEvent.getTransactionId()));
            
            // 3. 解析检测结果
            Map<String, Object> predictionData = JsonUtils.fromJson(
                    fraudResult.getPredictionResult(), Map.class);
            
            Double fraudScore = (Double) predictionData.get("fraud_score");
            String riskLevel = (String) predictionData.get("risk_level");
            List<String> fraudIndicators = (List<String>) predictionData.get("fraud_indicators");
            
            // 4. 生成检测结果
            FraudDetectionResult result = new FraudDetectionResult();
            result.setTransactionId(transactionEvent.getTransactionId());
            result.setImsi(transactionEvent.getImsi());
            result.setFraudScore(fraudScore);
            result.setRiskLevel(FraudRiskLevel.valueOf(riskLevel));
            result.setFraudIndicators(fraudIndicators);
            result.setConfidenceScore(fraudResult.getConfidenceScore());
            result.setDetectionTime(LocalDateTime.now());
            
            // 5. 风险评估和决策
            FraudDecision decision = makeFraudDecision(fraudScore, riskLevel);
            result.setDecision(decision);
            
            // 6. 触发相应的处理流程
            if (decision == FraudDecision.BLOCK || decision == FraudDecision.REVIEW) {
                handleHighRiskTransaction(transactionEvent, result);
            }
            
            log.info("欺诈检测完成: imsi={}, score={}, decision={}", 
                    transactionEvent.getImsi(), fraudScore, decision);
            
            return result;
            
        } catch (Exception e) {
            log.error("欺诈检测失败", e);
            throw new RuntimeException("欺诈检测失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量欺诈检测
     * 
     * @param timeRange 时间范围
     * @return 批量检测结果
     */
    public BatchFraudDetectionResult batchFraudDetection(TimeRange timeRange) {
        log.info("执行批量欺诈检测: timeRange={}", timeRange);
        
        try {
            // 1. 获取待检测的交易数据
            List<TransactionEvent> transactions = getTransactionsInTimeRange(timeRange);
            
            // 2. 批量特征提取
            List<Map<String, Object>> batchFeatures = transactions.stream()
                    .map(this::extractFraudFeatures)
                    .collect(Collectors.toList());
            
            // 3. 批量预测
            Long fraudModelId = getModelIdByScenario(AIApplicationScenario.FRAUD_DETECTION);
            List<FraudDetectionResult> results = new ArrayList<>();
            
            for (int i = 0; i < transactions.size(); i++) {
                TransactionEvent transaction = transactions.get(i);
                Map<String, Object> features = batchFeatures.get(i);
                
                ModelPredictionResult prediction = modelManagementService.predict(
                        fraudModelId, features, 
                        Map.of("batchDetection", true, "transactionId", transaction.getTransactionId()));
                
                // 解析结果
                Map<String, Object> predictionData = JsonUtils.fromJson(
                        prediction.getPredictionResult(), Map.class);
                
                FraudDetectionResult result = new FraudDetectionResult();
                result.setTransactionId(transaction.getTransactionId());
                result.setImsi(transaction.getImsi());
                result.setFraudScore((Double) predictionData.get("fraud_score"));
                result.setRiskLevel(FraudRiskLevel.valueOf((String) predictionData.get("risk_level")));
                result.setConfidenceScore(prediction.getConfidenceScore());
                result.setDetectionTime(LocalDateTime.now());
                
                results.add(result);
            }
            
            // 4. 生成批量检测报告
            BatchFraudDetectionResult batchResult = new BatchFraudDetectionResult();
            batchResult.setTimeRange(timeRange);
            batchResult.setTotalTransactions(transactions.size());
            batchResult.setDetectionResults(results);
            
            // 统计各风险级别的数量
            Map<FraudRiskLevel, Long> riskLevelCounts = results.stream()
                    .collect(Collectors.groupingBy(
                            FraudDetectionResult::getRiskLevel, Collectors.counting()));
            batchResult.setRiskLevelStatistics(riskLevelCounts);
            
            // 计算平均欺诈分数
            double avgFraudScore = results.stream()
                    .mapToDouble(FraudDetectionResult::getFraudScore)
                    .average().orElse(0.0);
            batchResult.setAverageFraudScore(avgFraudScore);
            
            batchResult.setDetectionCompletedAt(LocalDateTime.now());
            
            log.info("批量欺诈检测完成: total={}, avgScore={}", 
                    transactions.size(), avgFraudScore);
            
            return batchResult;
            
        } catch (Exception e) {
            log.error("批量欺诈检测失败", e);
            throw new RuntimeException("批量欺诈检测失败: " + e.getMessage());
        }
    }
    
    // 辅助方法实现...
    private Map<String, Object> extractFraudFeatures(TransactionEvent event) {
        Map<String, Object> features = new HashMap<>();
        
        // 交易基本特征
        features.put("transaction_amount", event.getAmount());
        features.put("transaction_type", event.getTransactionType());
        features.put("transaction_time", event.getTransactionTime());
        
        // 用户历史行为特征
        features.put("user_avg_transaction_amount", getUserAvgTransactionAmount(event.getImsi()));
        features.put("user_transaction_frequency", getUserTransactionFrequency(event.getImsi()));
        features.put("user_unusual_time_transactions", getUserUnusualTimeTransactions(event.getImsi()));
        
        // 地理位置特征
        features.put("location_risk_score", getLocationRiskScore(event.getLocation()));
        features.put("location_change_frequency", getLocationChangeFrequency(event.getImsi()));
        features.put("distance_from_usual_location", getDistanceFromUsualLocation(event.getImsi(), event.getLocation()));
        
        // 设备特征
        features.put("device_risk_score", getDeviceRiskScore(event.getDeviceInfo()));
        features.put("device_change_frequency", getDeviceChangeFrequency(event.getImsi()));
        
        // 网络特征
        features.put("network_anomaly_score", getNetworkAnomalyScore(event.getNetworkInfo()));
        features.put("connection_pattern_anomaly", getConnectionPatternAnomaly(event.getImsi()));
        
        return features;
    }
    
    private FraudDecision makeFraudDecision(Double fraudScore, String riskLevel) {
        if (fraudScore >= 0.9) {
            return FraudDecision.BLOCK;
        } else if (fraudScore >= 0.7) {
            return FraudDecision.REVIEW;
        } else if (fraudScore >= 0.5) {
            return FraudDecision.MONITOR;
        } else {
            return FraudDecision.ALLOW;
        }
    }
    
    private void handleHighRiskTransaction(TransactionEvent event, FraudDetectionResult result) {
        // 处理高风险交易
        if (result.getDecision() == FraudDecision.BLOCK) {
            // 阻止交易
            blockTransaction(event.getTransactionId());
            
            // 发送警报
            alertService.sendFraudAlert(result);
            
            // 冻结账户（如果需要）
            if (result.getFraudScore() >= 0.95) {
                freezeAccount(event.getImsi());
            }
        } else if (result.getDecision() == FraudDecision.REVIEW) {
            // 标记为待审核
            markForReview(event.getTransactionId(), result);
            
            // 通知风控团队
            alertService.notifyRiskTeam(result);
        }
    }
    
    // 其他辅助方法...
    private Double getUserAvgTransactionAmount(String imsi) {
        // 获取用户平均交易金额
        return 0.0;
    }
    
    private Integer getUserTransactionFrequency(String imsi) {
        // 获取用户交易频率
        return 0;
    }
    
    private Integer getUserUnusualTimeTransactions(String imsi) {
        // 获取用户异常时间交易次数
        return 0;
    }
    
    private Double getLocationRiskScore(String location) {
        // 获取位置风险评分
        return 0.0;
    }
    
    private Integer getLocationChangeFrequency(String imsi) {
        // 获取位置变化频率
        return 0;
    }
    
    private Double getDistanceFromUsualLocation(String imsi, String currentLocation) {
        // 获取与常用位置的距离
        return 0.0;
    }
    
    private Double getDeviceRiskScore(String deviceInfo) {
        // 获取设备风险评分
        return 0.0;
    }
    
    private Integer getDeviceChangeFrequency(String imsi) {
        // 获取设备变化频率
        return 0;
    }
    
    private Double getNetworkAnomalyScore(String networkInfo) {
        // 获取网络异常评分
        return 0.0;
    }
    
    private Double getConnectionPatternAnomaly(String imsi) {
        // 获取连接模式异常评分
        return 0.0;
    }
    
    private List<TransactionEvent> getTransactionsInTimeRange(TimeRange timeRange) {
        // 获取时间范围内的交易数据
        return new ArrayList<>();
    }
    
    private void blockTransaction(String transactionId) {
        // 阻止交易
        log.warn("阻止可疑交易: transactionId={}", transactionId);
    }
    
    private void freezeAccount(String imsi) {
        // 冻结账户
        log.warn("冻结可疑账户: imsi={}", imsi);
    }
    
    private void markForReview(String transactionId, FraudDetectionResult result) {
        // 标记为待审核
        log.info("标记交易待审核: transactionId={}, score={}", 
                transactionId, result.getFraudScore());
    }
    
    private Long getModelIdByScenario(AIApplicationScenario scenario) {
        // 根据应用场景获取模型ID
        return 3L; // 欺诈检测模型ID
    }
}
```

### 网络优化服务

```java
/**
 * 网络优化服务
 * 基于AI/ML技术进行网络性能优化
 */
@Service
@Slf4j
public class NetworkOptimizationService {
    
    @Autowired
    private MLModelManagementService modelManagementService;
    
    @Autowired
    private NetworkMonitoringService networkMonitoringService;
    
    @Autowired
    private NetworkConfigurationService configurationService;
    
    /**
     * 网络性能预测
     * 
     * @param networkId 网络ID
     * @param predictionTimeRange 预测时间范围
     * @return 网络性能预测结果
     */
    public NetworkPerformancePrediction predictNetworkPerformance(String networkId, 
                                                                 TimeRange predictionTimeRange) {
        log.info("预测网络性能: networkId={}, timeRange={}", networkId, predictionTimeRange);
        
        try {
            // 1. 收集网络历史数据
            NetworkHistoricalData historicalData = networkMonitoringService
                    .getHistoricalData(networkId, predictionTimeRange);
            
            // 2. 提取网络特征
            Map<String, Object> networkFeatures = extractNetworkFeatures(historicalData);
            
            // 3. 使用网络优化模型进行预测
            Long networkModelId = getModelIdByScenario(AIApplicationScenario.NETWORK_OPTIMIZATION);
            ModelPredictionResult predictionResult = modelManagementService.predict(
                    networkModelId, networkFeatures, 
                    Map.of("networkId", networkId, "predictionType", "performance"));
            
            // 4. 解析预测结果
            Map<String, Object> predictionData = JsonUtils.fromJson(
                    predictionResult.getPredictionResult(), Map.class);
            
            // 5. 生成预测报告
            NetworkPerformancePrediction prediction = new NetworkPerformancePrediction();
            prediction.setNetworkId(networkId);
            prediction.setPredictionTimeRange(predictionTimeRange);
            prediction.setPredictedThroughput((Double) predictionData.get("predicted_throughput"));
            prediction.setPredictedLatency((Double) predictionData.get("predicted_latency"));
            prediction.setPredictedPacketLoss((Double) predictionData.get("predicted_packet_loss"));
            prediction.setPredictedCongestionLevel((String) predictionData.get("predicted_congestion_level"));
            prediction.setConfidenceScore(predictionResult.getConfidenceScore());
            prediction.setPredictionTime(LocalDateTime.now());
            
            // 6. 生成优化建议
            List<String> optimizationRecommendations = generateOptimizationRecommendations(
                    networkFeatures, predictionData);
            prediction.setOptimizationRecommendations(optimizationRecommendations);
            
            log.info("网络性能预测完成: networkId={}, throughput={}, latency={}", 
                    networkId, prediction.getPredictedThroughput(), prediction.getPredictedLatency());
            
            return prediction;
            
        } catch (Exception e) {
            log.error("网络性能预测失败", e);
            throw new RuntimeException("网络性能预测失败: " + e.getMessage());
        }
    }
    
    /**
     * 自动网络优化
     * 
     * @param networkId 网络ID
     * @return 优化结果
     */
    public NetworkOptimizationResult autoOptimizeNetwork(String networkId) {
        log.info("执行自动网络优化: networkId={}", networkId);
        
        try {
            // 1. 获取当前网络状态
            NetworkStatus currentStatus = networkMonitoringService.getCurrentStatus(networkId);
            
            // 2. 分析网络问题
            List<NetworkIssue> issues = analyzeNetworkIssues(currentStatus);
            
            // 3. 生成优化策略
            List<OptimizationStrategy> strategies = generateOptimizationStrategies(issues);
            
            // 4. 执行优化操作
            List<OptimizationAction> executedActions = new ArrayList<>();
            for (OptimizationStrategy strategy : strategies) {
                OptimizationAction action = executeOptimizationStrategy(networkId, strategy);
                executedActions.add(action);
            }
            
            // 5. 验证优化效果
            NetworkStatus optimizedStatus = networkMonitoringService.getCurrentStatus(networkId);
            OptimizationEffectiveness effectiveness = evaluateOptimizationEffectiveness(
                    currentStatus, optimizedStatus);
            
            // 6. 生成优化结果
            NetworkOptimizationResult result = new NetworkOptimizationResult();
            result.setNetworkId(networkId);
            result.setOriginalStatus(currentStatus);
            result.setOptimizedStatus(optimizedStatus);
            result.setIdentifiedIssues(issues);
            result.setExecutedActions(executedActions);
            result.setEffectiveness(effectiveness);
            result.setOptimizationTime(LocalDateTime.now());
            
            log.info("自动网络优化完成: networkId={}, improvement={}", 
                    networkId, effectiveness.getOverallImprovement());
            
            return result;
            
        } catch (Exception e) {
            log.error("自动网络优化失败", e);
            throw new RuntimeException("自动网络优化失败: " + e.getMessage());
        }
    }
    
    /**
     * 负载均衡优化
     * 
     * @param clusterNetworkIds 集群网络ID列表
     * @return 负载均衡优化结果
     */
    public LoadBalancingOptimizationResult optimizeLoadBalancing(List<String> clusterNetworkIds) {
        log.info("执行负载均衡优化: networks={}", clusterNetworkIds);
        
        try {
            // 1. 收集集群网络状态
            Map<String, NetworkStatus> clusterStatus = new HashMap<>();
            for (String networkId : clusterNetworkIds) {
                NetworkStatus status = networkMonitoringService.getCurrentStatus(networkId);
                clusterStatus.put(networkId, status);
            }
            
            // 2. 分析负载分布
            LoadDistributionAnalysis loadAnalysis = analyzeLoadDistribution(clusterStatus);
            
            // 3. 使用强化学习模型优化负载分配
            Map<String, Object> loadFeatures = extractLoadBalancingFeatures(clusterStatus);
            Long rlModelId = getModelIdByScenario(AIApplicationScenario.NETWORK_OPTIMIZATION);
            
            ModelPredictionResult optimizationResult = modelManagementService.predict(
                    rlModelId, loadFeatures, 
                    Map.of("optimizationType", "loadBalancing", "clusterSize", clusterNetworkIds.size()));
            
            // 4. 解析优化策略
            Map<String, Object> strategyData = JsonUtils.fromJson(
                    optimizationResult.getPredictionResult(), Map.class);
            
            Map<String, Double> newLoadDistribution = (Map<String, Double>) strategyData.get("load_distribution");
            
            // 5. 应用新的负载分配
            Map<String, LoadBalancingAction> appliedActions = new HashMap<>();
            for (String networkId : clusterNetworkIds) {
                Double newLoadRatio = newLoadDistribution.get(networkId);
                if (newLoadRatio != null) {
                    LoadBalancingAction action = configurationService
                            .updateLoadBalancingWeight(networkId, newLoadRatio);
                    appliedActions.put(networkId, action);
                }
            }
            
            // 6. 验证优化效果
            Thread.sleep(5000); // 等待配置生效
            Map<String, NetworkStatus> optimizedStatus = new HashMap<>();
            for (String networkId : clusterNetworkIds) {
                NetworkStatus status = networkMonitoringService.getCurrentStatus(networkId);
                optimizedStatus.put(networkId, status);
            }
            
            LoadDistributionAnalysis optimizedAnalysis = analyzeLoadDistribution(optimizedStatus);
            
            // 7. 生成优化结果
            LoadBalancingOptimizationResult result = new LoadBalancingOptimizationResult();
            result.setClusterNetworkIds(clusterNetworkIds);
            result.setOriginalLoadAnalysis(loadAnalysis);
            result.setOptimizedLoadAnalysis(optimizedAnalysis);
            result.setAppliedActions(appliedActions);
            result.setConfidenceScore(optimizationResult.getConfidenceScore());
            result.setOptimizationTime(LocalDateTime.now());
            
            // 计算改进指标
            double loadBalanceImprovement = calculateLoadBalanceImprovement(
                    loadAnalysis, optimizedAnalysis);
            result.setLoadBalanceImprovement(loadBalanceImprovement);
            
            log.info("负载均衡优化完成: improvement={}", loadBalanceImprovement);
            
            return result;
            
        } catch (Exception e) {
            log.error("负载均衡优化失败", e);
            throw new RuntimeException("负载均衡优化失败: " + e.getMessage());
        }
    }
    
    // 辅助方法实现...
    private Map<String, Object> extractNetworkFeatures(NetworkHistoricalData historicalData) {
        Map<String, Object> features = new HashMap<>();
        
        // 流量特征
        features.put("avg_throughput", historicalData.getAverageThroughput());
        features.put("peak_throughput", historicalData.getPeakThroughput());
        features.put("throughput_variance", historicalData.getThroughputVariance());
        
        // 延迟特征
        features.put("avg_latency", historicalData.getAverageLatency());
        features.put("max_latency", historicalData.getMaxLatency());
        features.put("latency_jitter", historicalData.getLatencyJitter());
        
        // 丢包特征
        features.put("packet_loss_rate", historicalData.getPacketLossRate());
        features.put("error_rate", historicalData.getErrorRate());
        
        // 连接特征
        features.put("active_connections", historicalData.getActiveConnections());
        features.put("connection_establishment_rate", historicalData.getConnectionEstablishmentRate());
        
        // 资源利用率特征
        features.put("cpu_utilization", historicalData.getCpuUtilization());
        features.put("memory_utilization", historicalData.getMemoryUtilization());
        features.put("bandwidth_utilization", historicalData.getBandwidthUtilization());
        
        return features;
    }
    
    private List<String> generateOptimizationRecommendations(Map<String, Object> features, 
                                                            Map<String, Object> predictions) {
        List<String> recommendations = new ArrayList<>();
        
        Double predictedThroughput = (Double) predictions.get("predicted_throughput");
        Double predictedLatency = (Double) predictions.get("predicted_latency");
        String congestionLevel = (String) predictions.get("predicted_congestion_level");
        
        if (predictedThroughput < 100.0) {
            recommendations.add("建议增加带宽容量");
        }
        
        if (predictedLatency > 50.0) {
            recommendations.add("建议优化路由配置");
        }
        
        if ("HIGH".equals(congestionLevel)) {
            recommendations.add("建议启用流量控制");
            recommendations.add("建议增加负载均衡节点");
        }
        
        return recommendations;
    }
    
    private List<NetworkIssue> analyzeNetworkIssues(NetworkStatus status) {
        List<NetworkIssue> issues = new ArrayList<>();
        
        if (status.getThroughput() < status.getExpectedThroughput() * 0.8) {
            issues.add(new NetworkIssue("LOW_THROUGHPUT", "吞吐量低于预期", 
                    NetworkIssueSeverity.MEDIUM));
        }
        
        if (status.getLatency() > 100.0) {
            issues.add(new NetworkIssue("HIGH_LATENCY", "延迟过高", 
                    NetworkIssueSeverity.HIGH));
        }
        
        if (status.getPacketLossRate() > 0.01) {
            issues.add(new NetworkIssue("PACKET_LOSS", "丢包率过高", 
                    NetworkIssueSeverity.HIGH));
        }
        
        if (status.getCpuUtilization() > 0.9) {
            issues.add(new NetworkIssue("HIGH_CPU", "CPU使用率过高", 
                    NetworkIssueSeverity.MEDIUM));
        }
        
        return issues;
    }
    
    private List<OptimizationStrategy> generateOptimizationStrategies(List<NetworkIssue> issues) {
        List<OptimizationStrategy> strategies = new ArrayList<>();
        
        for (NetworkIssue issue : issues) {
            switch (issue.getType()) {
                case "LOW_THROUGHPUT":
                    strategies.add(new OptimizationStrategy("INCREASE_BANDWIDTH", 
                            "增加带宽", OptimizationPriority.HIGH));
                    break;
                case "HIGH_LATENCY":
                    strategies.add(new OptimizationStrategy("OPTIMIZE_ROUTING", 
                            "优化路由", OptimizationPriority.HIGH));
                    break;
                case "PACKET_LOSS":
                    strategies.add(new OptimizationStrategy("ADJUST_BUFFER_SIZE", 
                            "调整缓冲区大小", OptimizationPriority.MEDIUM));
                    break;
                case "HIGH_CPU":
                    strategies.add(new OptimizationStrategy("SCALE_OUT", 
                            "横向扩展", OptimizationPriority.MEDIUM));
                    break;
            }
        }
        
        return strategies;
    }
    
    private OptimizationAction executeOptimizationStrategy(String networkId, 
                                                          OptimizationStrategy strategy) {
        log.info("执行优化策略: networkId={}, strategy={}", networkId, strategy.getType());
        
        OptimizationAction action = new OptimizationAction();
        action.setNetworkId(networkId);
        action.setStrategyType(strategy.getType());
        action.setExecutionTime(LocalDateTime.now());
        
        try {
            switch (strategy.getType()) {
                case "INCREASE_BANDWIDTH":
                    configurationService.increaseBandwidth(networkId, 1.2);
                    action.setStatus(OptimizationActionStatus.SUCCESS);
                    action.setDescription("带宽增加20%");
                    break;
                case "OPTIMIZE_ROUTING":
                    configurationService.optimizeRouting(networkId);
                    action.setStatus(OptimizationActionStatus.SUCCESS);
                    action.setDescription("路由表已优化");
                    break;
                case "ADJUST_BUFFER_SIZE":
                    configurationService.adjustBufferSize(networkId, 2048);
                    action.setStatus(OptimizationActionStatus.SUCCESS);
                    action.setDescription("缓冲区大小调整为2048KB");
                    break;
                case "SCALE_OUT":
                    configurationService.scaleOut(networkId, 1);
                    action.setStatus(OptimizationActionStatus.SUCCESS);
                    action.setDescription("增加1个处理节点");
                    break;
                default:
                    action.setStatus(OptimizationActionStatus.SKIPPED);
                    action.setDescription("未知的优化策略");
            }
        } catch (Exception e) {
            action.setStatus(OptimizationActionStatus.FAILED);
            action.setErrorMessage(e.getMessage());
            log.error("执行优化策略失败: networkId={}, strategy={}", 
                    networkId, strategy.getType(), e);
        }
        
        return action;
    }
    
    private OptimizationEffectiveness evaluateOptimizationEffectiveness(NetworkStatus before, 
                                                                       NetworkStatus after) {
        OptimizationEffectiveness effectiveness = new OptimizationEffectiveness();
        
        // 计算吞吐量改进
        double throughputImprovement = (after.getThroughput() - before.getThroughput()) / 
                before.getThroughput() * 100;
        effectiveness.setThroughputImprovement(throughputImprovement);
        
        // 计算延迟改进
        double latencyImprovement = (before.getLatency() - after.getLatency()) / 
                before.getLatency() * 100;
        effectiveness.setLatencyImprovement(latencyImprovement);
        
        // 计算丢包率改进
        double packetLossImprovement = (before.getPacketLossRate() - after.getPacketLossRate()) / 
                before.getPacketLossRate() * 100;
        effectiveness.setPacketLossImprovement(packetLossImprovement);
        
        // 计算总体改进
        double overallImprovement = (throughputImprovement + latencyImprovement + packetLossImprovement) / 3;
        effectiveness.setOverallImprovement(overallImprovement);
        
        return effectiveness;
    }
    
    private LoadDistributionAnalysis analyzeLoadDistribution(Map<String, NetworkStatus> clusterStatus) {
        LoadDistributionAnalysis analysis = new LoadDistributionAnalysis();
        
        List<Double> loads = clusterStatus.values().stream()
                .map(NetworkStatus::getCpuUtilization)
                .collect(Collectors.toList());
        
        // 计算负载统计
        double avgLoad = loads.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double maxLoad = loads.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double minLoad = loads.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        
        analysis.setAverageLoad(avgLoad);
        analysis.setMaxLoad(maxLoad);
        analysis.setMinLoad(minLoad);
        analysis.setLoadVariance(maxLoad - minLoad);
        
        // 计算负载均衡指数（值越接近1表示负载越均衡）
        double loadBalanceIndex = minLoad / maxLoad;
        analysis.setLoadBalanceIndex(loadBalanceIndex);
        
        return analysis;
    }
    
    private Map<String, Object> extractLoadBalancingFeatures(Map<String, NetworkStatus> clusterStatus) {
        Map<String, Object> features = new HashMap<>();
        
        // 集群整体特征
        features.put("cluster_size", clusterStatus.size());
        
        List<Double> cpuUtilizations = clusterStatus.values().stream()
                .map(NetworkStatus::getCpuUtilization)
                .collect(Collectors.toList());
        
        features.put("avg_cpu_utilization", 
                cpuUtilizations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
        features.put("max_cpu_utilization", 
                cpuUtilizations.stream().mapToDouble(Double::doubleValue).max().orElse(0.0));
        features.put("min_cpu_utilization", 
                cpuUtilizations.stream().mapToDouble(Double::doubleValue).min().orElse(0.0));
        
        // 各节点详细特征
        int nodeIndex = 0;
        for (Map.Entry<String, NetworkStatus> entry : clusterStatus.entrySet()) {
            String prefix = "node_" + nodeIndex + "_";
            NetworkStatus status = entry.getValue();
            
            features.put(prefix + "cpu_utilization", status.getCpuUtilization());
            features.put(prefix + "memory_utilization", status.getMemoryUtilization());
            features.put(prefix + "throughput", status.getThroughput());
            features.put(prefix + "active_connections", status.getActiveConnections());
            
            nodeIndex++;
        }
        
        return features;
    }
    
    private double calculateLoadBalanceImprovement(LoadDistributionAnalysis before, 
                                                 LoadDistributionAnalysis after) {
        // 负载均衡改进 = (优化后的负载均衡指数 - 优化前的负载均衡指数) / 优化前的负载均衡指数 * 100
        return (after.getLoadBalanceIndex() - before.getLoadBalanceIndex()) / 
                before.getLoadBalanceIndex() * 100;
    }
    
    private Long getModelIdByScenario(AIApplicationScenario scenario) {
        // 根据应用场景获取模型ID
        return 4L; // 网络优化模型ID
    }
}
```

## Demo脚本示例

### 机器学习模型训练Demo

```bash
#!/bin/bash
# ML模型训练部署脚本

echo "=== NSRS AI/ML模型训练部署 ==="

# 1. 启动MLflow服务
echo "启动MLflow服务..."
docker run -d --name mlflow-server \
  -p 5000:5000 \
  -v $(pwd)/mlruns:/mlflow/mlruns \
  mlflow/mlflow:latest \
  mlflow server --host 0.0.0.0 --port 5000

# 2. 启动Jupyter Notebook
echo "启动Jupyter Notebook..."
docker run -d --name jupyter-ml \
  -p 8888:8888 \
  -v $(pwd)/notebooks:/home/jovyan/work \
  -e JUPYTER_ENABLE_LAB=yes \
  jupyter/tensorflow-notebook:latest

# 3. 启动Redis（用于特征存储）
echo "启动Redis特征存储..."
docker run -d --name redis-features \
  -p 6379:6379 \
  redis:latest redis-server --appendonly yes

# 4. 启动PostgreSQL（用于模型元数据）
echo "启动PostgreSQL数据库..."
docker run -d --name postgres-ml \
  -p 5432:5432 \
  -e POSTGRES_DB=ml_metadata \
  -e POSTGRES_USER=mluser \
  -e POSTGRES_PASSWORD=mlpass123 \
  -v postgres_ml_data:/var/lib/postgresql/data \
  postgres:13

# 5. 等待服务启动
echo "等待服务启动..."
sleep 30

# 6. 初始化数据库
echo "初始化ML元数据库..."
docker exec postgres-ml psql -U mluser -d ml_metadata -c "
CREATE TABLE IF NOT EXISTS ml_models (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    model_type VARCHAR(100) NOT NULL,
    application_scenario VARCHAR(100) NOT NULL,
    algorithm VARCHAR(100) NOT NULL,
    training_dataset VARCHAR(255),
    feature_config TEXT,
    hyperparameters TEXT,
    performance_metrics TEXT,
    deployment_config TEXT,
    status VARCHAR(50) DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS training_jobs (
    id SERIAL PRIMARY KEY,
    model_id INTEGER REFERENCES ml_models(id),
    job_name VARCHAR(255) NOT NULL,
    training_config TEXT,
    data_source_config TEXT,
    compute_resource_config TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prediction_results (
    id SERIAL PRIMARY KEY,
    model_id INTEGER REFERENCES ml_models(id),
    prediction_request_id VARCHAR(255) NOT NULL,
    input_features TEXT NOT NULL,
    prediction_result TEXT NOT NULL,
    confidence_score DECIMAL(5,4),
    prediction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
"

# 7. 创建训练数据生成器
echo "创建训练数据生成器..."
cat > generate_training_data.py << 'EOF'
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import json
import random

def generate_user_behavior_data(num_records=10000):
    """生成用户行为分析训练数据"""
    data = []
    
    for i in range(num_records):
        # 基础用户信息
        imsi = f"46000{random.randint(100000000, 999999999)}"
        
        # 通话行为特征
        call_frequency = random.randint(0, 50)  # 每日通话次数
        call_duration_avg = random.uniform(30, 300)  # 平均通话时长
        call_time_variance = random.uniform(0.1, 2.0)  # 通话时间方差
        
        # 数据使用特征
        data_usage_mb = random.uniform(0, 10240)  # 每日数据使用量MB
        app_usage_diversity = random.randint(1, 20)  # 使用应用种类数
        peak_hour_usage_ratio = random.uniform(0.1, 0.8)  # 高峰时段使用比例
        
        # 位置行为特征
        location_diversity = random.randint(1, 10)  # 位置多样性
        roaming_frequency = random.randint(0, 5)  # 漫游频率
        home_area_ratio = random.uniform(0.5, 0.95)  # 归属地使用比例
        
        # 消费行为特征
        monthly_spend = random.uniform(10, 500)  # 月消费金额
        recharge_frequency = random.randint(0, 10)  # 充值频率
        service_usage_variety = random.randint(1, 15)  # 服务使用种类
        
        # 生成标签（用户流失风险）
        # 基于特征计算流失概率
        churn_score = (
            (50 - call_frequency) * 0.02 +
            (5000 - data_usage_mb) * 0.00005 +
            (10 - location_diversity) * 0.05 +
            (100 - monthly_spend) * 0.005 +
            random.uniform(-0.2, 0.2)  # 添加随机噪声
        )
        churn_risk = 1 if churn_score > 0.5 else 0
        
        data.append({
            'imsi': imsi,
            'call_frequency': call_frequency,
            'call_duration_avg': call_duration_avg,
            'call_time_variance': call_time_variance,
            'data_usage_mb': data_usage_mb,
            'app_usage_diversity': app_usage_diversity,
            'peak_hour_usage_ratio': peak_hour_usage_ratio,
            'location_diversity': location_diversity,
            'roaming_frequency': roaming_frequency,
            'home_area_ratio': home_area_ratio,
            'monthly_spend': monthly_spend,
            'recharge_frequency': recharge_frequency,
            'service_usage_variety': service_usage_variety,
            'churn_risk': churn_risk
        })
    
    return pd.DataFrame(data)

def generate_fraud_detection_data(num_records=10000):
    """生成欺诈检测训练数据"""
    data = []
    
    for i in range(num_records):
        # 交易基本信息
        transaction_id = f"TXN{random.randint(100000, 999999)}"
        imsi = f"46000{random.randint(100000000, 999999999)}"
        transaction_amount = random.uniform(1, 1000)
        transaction_type = random.choice(['RECHARGE', 'SERVICE_FEE', 'DATA_PACKAGE', 'ROAMING_FEE'])
        
        # 用户历史行为特征
        user_avg_amount = random.uniform(10, 200)
        user_transaction_freq = random.randint(1, 30)
        unusual_time_transactions = random.randint(0, 5)
        
        # 地理位置特征
        location_risk_score = random.uniform(0, 1)
        location_change_freq = random.randint(0, 10)
        distance_from_usual = random.uniform(0, 1000)
        
        # 设备特征
        device_risk_score = random.uniform(0, 1)
        device_change_freq = random.randint(0, 5)
        
        # 网络特征
        network_anomaly_score = random.uniform(0, 1)
        connection_pattern_anomaly = random.uniform(0, 1)
        
        # 生成欺诈标签
        fraud_score = (
            (transaction_amount / user_avg_amount - 1) * 0.3 +
            location_risk_score * 0.2 +
            device_risk_score * 0.2 +
            network_anomaly_score * 0.15 +
            (unusual_time_transactions / 10) * 0.15 +
            random.uniform(-0.1, 0.1)  # 随机噪声
        )
        is_fraud = 1 if fraud_score > 0.7 else 0
        
        data.append({
            'transaction_id': transaction_id,
            'imsi': imsi,
            'transaction_amount': transaction_amount,
            'transaction_type': transaction_type,
            'user_avg_amount': user_avg_amount,
            'user_transaction_freq': user_transaction_freq,
            'unusual_time_transactions': unusual_time_transactions,
            'location_risk_score': location_risk_score,
            'location_change_freq': location_change_freq,
            'distance_from_usual': distance_from_usual,
            'device_risk_score': device_risk_score,
            'device_change_freq': device_change_freq,
            'network_anomaly_score': network_anomaly_score,
            'connection_pattern_anomaly': connection_pattern_anomaly,
            'is_fraud': is_fraud
        })
    
    return pd.DataFrame(data)

def generate_network_optimization_data(num_records=5000):
    """生成网络优化训练数据"""
    data = []
    
    for i in range(num_records):
        # 网络基础指标
        network_id = f"NET{random.randint(1000, 9999)}"
        avg_throughput = random.uniform(50, 500)
        peak_throughput = avg_throughput * random.uniform(1.2, 2.0)
        avg_latency = random.uniform(10, 200)
        packet_loss_rate = random.uniform(0, 0.05)
        
        # 资源利用率
        cpu_utilization = random.uniform(0.1, 0.95)
        memory_utilization = random.uniform(0.2, 0.9)
        bandwidth_utilization = random.uniform(0.3, 0.95)
        
        # 连接信息
        active_connections = random.randint(100, 10000)
        connection_establishment_rate = random.uniform(10, 1000)
        
        # 预测目标（网络性能等级）
        performance_score = (
            (avg_throughput / 500) * 0.3 +
            (1 - avg_latency / 200) * 0.3 +
            (1 - packet_loss_rate / 0.05) * 0.2 +
            (1 - cpu_utilization) * 0.1 +
            (1 - bandwidth_utilization) * 0.1
        )
        
        if performance_score >= 0.8:
            performance_level = 'EXCELLENT'
        elif performance_score >= 0.6:
            performance_level = 'GOOD'
        elif performance_score >= 0.4:
            performance_level = 'FAIR'
        else:
            performance_level = 'POOR'
        
        data.append({
            'network_id': network_id,
            'avg_throughput': avg_throughput,
            'peak_throughput': peak_throughput,
            'avg_latency': avg_latency,
            'packet_loss_rate': packet_loss_rate,
            'cpu_utilization': cpu_utilization,
            'memory_utilization': memory_utilization,
            'bandwidth_utilization': bandwidth_utilization,
            'active_connections': active_connections,
            'connection_establishment_rate': connection_establishment_rate,
            'performance_level': performance_level
        })
    
    return pd.DataFrame(data)

if __name__ == "__main__":
    print("生成训练数据...")
    
    # 生成用户行为分析数据
    user_behavior_df = generate_user_behavior_data(10000)
    user_behavior_df.to_csv('user_behavior_training_data.csv', index=False)
    print(f"用户行为分析数据已生成: {len(user_behavior_df)} 条记录")
    
    # 生成欺诈检测数据
    fraud_detection_df = generate_fraud_detection_data(10000)
    fraud_detection_df.to_csv('fraud_detection_training_data.csv', index=False)
    print(f"欺诈检测数据已生成: {len(fraud_detection_df)} 条记录")
    
    # 生成网络优化数据
    network_optimization_df = generate_network_optimization_data(5000)
    network_optimization_df.to_csv('network_optimization_training_data.csv', index=False)
    print(f"网络优化数据已生成: {len(network_optimization_df)} 条记录")
    
    print("所有训练数据生成完成！")
EOF

# 8. 运行数据生成器
echo "生成训练数据..."
python generate_training_data.py

# 9. 创建模型训练脚本
echo "创建模型训练脚本..."
cat > train_models.py << 'EOF'
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score
import joblib
import mlflow
import mlflow.sklearn
from datetime import datetime
import warnings
warnings.filterwarnings('ignore')

# 设置MLflow跟踪URI
mlflow.set_tracking_uri("http://localhost:5000")

def train_user_behavior_model():
    """训练用户行为分析模型"""
    print("训练用户行为分析模型...")
    
    # 加载数据
    df = pd.read_csv('user_behavior_training_data.csv')
    
    # 特征工程
    feature_columns = [
        'call_frequency', 'call_duration_avg', 'call_time_variance',
        'data_usage_mb', 'app_usage_diversity', 'peak_hour_usage_ratio',
        'location_diversity', 'roaming_frequency', 'home_area_ratio',
        'monthly_spend', 'recharge_frequency', 'service_usage_variety'
    ]
    
    X = df[feature_columns]
    y = df['churn_risk']
    
    # 数据分割
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    # 特征标准化
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    
    # 开始MLflow实验
    with mlflow.start_run(run_name="user_behavior_analysis"):
        # 训练模型
        model = GradientBoostingClassifier(
            n_estimators=100,
            learning_rate=0.1,
            max_depth=6,
            random_state=42
        )
        
        model.fit(X_train_scaled, y_train)
        
        # 预测和评估
        y_pred = model.predict(X_test_scaled)
        y_pred_proba = model.predict_proba(X_test_scaled)[:, 1]
        
        auc_score = roc_auc_score(y_test, y_pred_proba)
        
        # 记录参数和指标
        mlflow.log_param("model_type", "GradientBoostingClassifier")
        mlflow.log_param("n_estimators", 100)
        mlflow.log_param("learning_rate", 0.1)
        mlflow.log_param("max_depth", 6)
        mlflow.log_metric("auc_score", auc_score)
        
        # 保存模型
        mlflow.sklearn.log_model(model, "model")
        joblib.dump(scaler, 'user_behavior_scaler.pkl')
        mlflow.log_artifact('user_behavior_scaler.pkl')
        
        print(f"用户行为分析模型训练完成，AUC: {auc_score:.4f}")
        print(classification_report(y_test, y_pred))

def train_fraud_detection_model():
    """训练欺诈检测模型"""
    print("训练欺诈检测模型...")
    
    # 加载数据
    df = pd.read_csv('fraud_detection_training_data.csv')
    
    # 特征工程
    feature_columns = [
        'transaction_amount', 'user_avg_amount', 'user_transaction_freq',
        'unusual_time_transactions', 'location_risk_score', 'location_change_freq',
        'distance_from_usual', 'device_risk_score', 'device_change_freq',
        'network_anomaly_score', 'connection_pattern_anomaly'
    ]
    
    # 处理分类特征
    le = LabelEncoder()
    df['transaction_type_encoded'] = le.fit_transform(df['transaction_type'])
    feature_columns.append('transaction_type_encoded')
    
    X = df[feature_columns]
    y = df['is_fraud']
    
    # 数据分割
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    # 特征标准化
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    
    # 开始MLflow实验
    with mlflow.start_run(run_name="fraud_detection"):
        # 训练模型
        model = RandomForestClassifier(
            n_estimators=200,
            max_depth=10,
            min_samples_split=5,
            min_samples_leaf=2,
            random_state=42
        )
        
        model.fit(X_train_scaled, y_train)
        
        # 预测和评估
        y_pred = model.predict(X_test_scaled)
        y_pred_proba = model.predict_proba(X_test_scaled)[:, 1]
        
        auc_score = roc_auc_score(y_test, y_pred_proba)
        
        # 记录参数和指标
        mlflow.log_param("model_type", "RandomForestClassifier")
        mlflow.log_param("n_estimators", 200)
        mlflow.log_param("max_depth", 10)
        mlflow.log_metric("auc_score", auc_score)
        
        # 保存模型
        mlflow.sklearn.log_model(model, "model")
        joblib.dump(scaler, 'fraud_detection_scaler.pkl')
        joblib.dump(le, 'transaction_type_encoder.pkl')
        mlflow.log_artifact('fraud_detection_scaler.pkl')
        mlflow.log_artifact('transaction_type_encoder.pkl')
        
        print(f"欺诈检测模型训练完成，AUC: {auc_score:.4f}")
        print(classification_report(y_test, y_pred))

def train_network_optimization_model():
    """训练网络优化模型"""
    print("训练网络优化模型...")
    
    # 加载数据
    df = pd.read_csv('network_optimization_training_data.csv')
    
    # 特征工程
    feature_columns = [
        'avg_throughput', 'peak_throughput', 'avg_latency', 'packet_loss_rate',
        'cpu_utilization', 'memory_utilization', 'bandwidth_utilization',
        'active_connections', 'connection_establishment_rate'
    ]
    
    X = df[feature_columns]
    y = df['performance_level']
    
    # 标签编码
    le = LabelEncoder()
    y_encoded = le.fit_transform(y)
    
    # 数据分割
    X_train, X_test, y_train, y_test = train_test_split(X, y_encoded, test_size=0.2, random_state=42)
    
    # 特征标准化
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)
    
    # 开始MLflow实验
    with mlflow.start_run(run_name="network_optimization"):
        # 训练模型
        model = GradientBoostingClassifier(
            n_estimators=150,
            learning_rate=0.1,
            max_depth=8,
            random_state=42
        )
        
        model.fit(X_train_scaled, y_train)
        
        # 预测和评估
        y_pred = model.predict(X_test_scaled)
        accuracy = (y_pred == y_test).mean()
        
        # 记录参数和指标
        mlflow.log_param("model_type", "GradientBoostingClassifier")
        mlflow.log_param("n_estimators", 150)
        mlflow.log_param("learning_rate", 0.1)
        mlflow.log_param("max_depth", 8)
        mlflow.log_metric("accuracy", accuracy)
        
        # 保存模型
        mlflow.sklearn.log_model(model, "model")
        joblib.dump(scaler, 'network_optimization_scaler.pkl')
        joblib.dump(le, 'performance_level_encoder.pkl')
        mlflow.log_artifact('network_optimization_scaler.pkl')
        mlflow.log_artifact('performance_level_encoder.pkl')
        
        print(f"网络优化模型训练完成，准确率: {accuracy:.4f}")
        print(f"类别: {le.classes_}")

if __name__ == "__main__":
    print("开始模型训练...")
    
    # 创建MLflow实验
    mlflow.set_experiment("NSRS_AI_ML_Models")
    
    # 训练各个模型
    train_user_behavior_model()
    train_fraud_detection_model()
    train_network_optimization_model()
    
    print("所有模型训练完成！")
    print("请访问 http://localhost:5000 查看MLflow UI")
EOF

# 10. 运行模型训练
echo "开始模型训练..."
python train_models.py

echo "=== ML模型训练部署完成 ==="
echo "服务访问地址:"
echo "- MLflow UI: http://localhost:5000"
echo "- Jupyter Notebook: http://localhost:8888"
echo "- Redis: localhost:6379"
echo "- PostgreSQL: localhost:5432"
```

### 实时预测服务Demo

```bash
#!/bin/bash
# 实时预测服务部署脚本

echo "=== NSRS 实时预测服务部署 ==="

# 1. 启动Kafka（用于实时数据流）
echo "启动Kafka服务..."
docker run -d --name zookeeper \
  -p 2181:2181 \
  confluentinc/cp-zookeeper:latest \
  bash -c "export ZOOKEEPER_CLIENT_PORT=2181 && /etc/confluent/docker/run"

sleep 10

docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest

# 2. 启动模型服务API
echo "创建模型服务API..."
cat > model_service_api.py << 'EOF'
from flask import Flask, request, jsonify
import joblib
import numpy as np
import pandas as pd
from datetime import datetime
import json
import redis
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 连接Redis
redis_client = redis.Redis(host='localhost', port=6379, db=0)

# 加载模型和预处理器
try:
    # 用户行为分析模型
    user_behavior_model = joblib.load('user_behavior_model.pkl')
    user_behavior_scaler = joblib.load('user_behavior_scaler.pkl')
    
    # 欺诈检测模型
    fraud_detection_model = joblib.load('fraud_detection_model.pkl')
    fraud_detection_scaler = joblib.load('fraud_detection_scaler.pkl')
    transaction_type_encoder = joblib.load('transaction_type_encoder.pkl')
    
    # 网络优化模型
    network_optimization_model = joblib.load('network_optimization_model.pkl')
    network_optimization_scaler = joblib.load('network_optimization_scaler.pkl')
    performance_level_encoder = joblib.load('performance_level_encoder.pkl')
    
    logger.info("所有模型加载成功")
except Exception as e:
    logger.error(f"模型加载失败: {e}")

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({"status": "healthy", "timestamp": datetime.now().isoformat()})

@app.route('/predict/user_behavior', methods=['POST'])
def predict_user_behavior():
    """用户行为分析预测"""
    try:
        data = request.json
        
        # 提取特征
        features = [
            data['call_frequency'],
            data['call_duration_avg'],
            data['call_time_variance'],
            data['data_usage_mb'],
            data['app_usage_diversity'],
            data['peak_hour_usage_ratio'],
            data['location_diversity'],
            data['roaming_frequency'],
            data['home_area_ratio'],
            data['monthly_spend'],
            data['recharge_frequency'],
            data['service_usage_variety']
        ]
        
        # 特征标准化
        features_scaled = user_behavior_scaler.transform([features])
        
        # 预测
        prediction = user_behavior_model.predict(features_scaled)[0]
        prediction_proba = user_behavior_model.predict_proba(features_scaled)[0]
        
        # 生成结果
        result = {
            "prediction_request_id": f"UB_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}",
            "imsi": data.get('imsi', ''),
            "churn_risk": int(prediction),
            "churn_probability": float(prediction_proba[1]),
            "confidence_score": float(max(prediction_proba)),
            "prediction_time": datetime.now().isoformat(),
            "risk_level": "HIGH" if prediction_proba[1] > 0.7 else "MEDIUM" if prediction_proba[1] > 0.3 else "LOW"
        }
        
        # 缓存结果
        redis_client.setex(
            f"user_behavior_prediction:{result['prediction_request_id']}",
            3600,  # 1小时过期
            json.dumps(result)
        )
        
        logger.info(f"用户行为预测完成: {result['prediction_request_id']}")
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"用户行为预测失败: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/predict/fraud_detection', methods=['POST'])
def predict_fraud_detection():
    """欺诈检测预测"""
    try:
        data = request.json
        
        # 处理分类特征
        transaction_type_encoded = transaction_type_encoder.transform([data['transaction_type']])[0]
        
        # 提取特征
        features = [
            data['transaction_amount'],
            data['user_avg_amount'],
            data['user_transaction_freq'],
            data['unusual_time_transactions'],
            data['location_risk_score'],
            data['location_change_freq'],
            data['distance_from_usual'],
            data['device_risk_score'],
            data['device_change_freq'],
            data['network_anomaly_score'],
            data['connection_pattern_anomaly'],
            transaction_type_encoded
        ]
        
        # 特征标准化
        features_scaled = fraud_detection_scaler.transform([features])
        
        # 预测
        prediction = fraud_detection_model.predict(features_scaled)[0]
        prediction_proba = fraud_detection_model.predict_proba(features_scaled)[0]
        
        # 生成结果
        fraud_score = float(prediction_proba[1])
        
        if fraud_score >= 0.9:
            decision = "BLOCK"
            risk_level = "CRITICAL"
        elif fraud_score >= 0.7:
            decision = "REVIEW"
            risk_level = "HIGH"
        elif fraud_score >= 0.5:
            decision = "MONITOR"
            risk_level = "MEDIUM"
        else:
            decision = "ALLOW"
            risk_level = "LOW"
        
        result = {
            "prediction_request_id": f"FD_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}",
            "transaction_id": data.get('transaction_id', ''),
            "imsi": data.get('imsi', ''),
            "is_fraud": int(prediction),
            "fraud_score": fraud_score,
            "confidence_score": float(max(prediction_proba)),
            "risk_level": risk_level,
            "decision": decision,
            "prediction_time": datetime.now().isoformat()
        }
        
        # 缓存结果
        redis_client.setex(
            f"fraud_detection_prediction:{result['prediction_request_id']}",
            3600,  # 1小时过期
            json.dumps(result)
        )
        
        logger.info(f"欺诈检测预测完成: {result['prediction_request_id']}")
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"欺诈检测预测失败: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/predict/network_optimization', methods=['POST'])
def predict_network_optimization():
    """网络优化预测"""
    try:
        data = request.json
        
        # 提取特征
        features = [
            data['avg_throughput'],
            data['peak_throughput'],
            data['avg_latency'],
            data['packet_loss_rate'],
            data['cpu_utilization'],
            data['memory_utilization'],
            data['bandwidth_utilization'],
            data['active_connections'],
            data['connection_establishment_rate']
        ]
        
        # 特征标准化
        features_scaled = network_optimization_scaler.transform([features])
        
        # 预测
        prediction = network_optimization_model.predict(features_scaled)[0]
        prediction_proba = network_optimization_model.predict_proba(features_scaled)[0]
        
        # 解码预测结果
        performance_level = performance_level_encoder.inverse_transform([prediction])[0]
        
        # 生成优化建议
        recommendations = []
        if data['avg_throughput'] < 100:
            recommendations.append("建议增加带宽容量")
        if data['avg_latency'] > 50:
            recommendations.append("建议优化路由配置")
        if data['cpu_utilization'] > 0.8:
            recommendations.append("建议增加计算资源")
        if data['packet_loss_rate'] > 0.01:
            recommendations.append("建议检查网络设备")
        
        result = {
            "prediction_request_id": f"NO_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}",
            "network_id": data.get('network_id', ''),
            "performance_level": performance_level,
            "confidence_score": float(max(prediction_proba)),
            "optimization_recommendations": recommendations,
            "prediction_time": datetime.now().isoformat()
        }
        
        # 缓存结果
        redis_client.setex(
            f"network_optimization_prediction:{result['prediction_request_id']}",
            3600,  # 1小时过期
            json.dumps(result)
        )
        
        logger.info(f"网络优化预测完成: {result['prediction_request_id']}")
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"网络优化预测失败: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/prediction/<prediction_id>', methods=['GET'])
def get_prediction_result(prediction_id):
    """获取预测结果"""
    try:
        # 从Redis获取结果
        for prefix in ['user_behavior_prediction', 'fraud_detection_prediction', 'network_optimization_prediction']:
            key = f"{prefix}:{prediction_id}"
            result = redis_client.get(key)
            if result:
                return jsonify(json.loads(result))
        
        return jsonify({"error": "预测结果未找到"}), 404
        
    except Exception as e:
        logger.error(f"获取预测结果失败: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
EOF

# 3. 启动模型服务
echo "启动模型服务API..."
python model_service_api.py &

# 4. 创建实时数据生成器
echo "创建实时数据生成器..."
cat > real_time_data_generator.py << 'EOF'
import json
import random
import time
from datetime import datetime
from kafka import KafkaProducer
import requests
import threading

# Kafka生产者
producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

def generate_user_behavior_events():
    """生成用户行为事件"""
    while True:
        try:
            event = {
                "event_type": "user_behavior",
                "imsi": f"46000{random.randint(100000000, 999999999)}",
                "call_frequency": random.randint(0, 50),
                "call_duration_avg": random.uniform(30, 300),
                "call_time_variance": random.uniform(0.1, 2.0),
                "data_usage_mb": random.uniform(0, 10240),
                "app_usage_diversity": random.randint(1, 20),
                "peak_hour_usage_ratio": random.uniform(0.1, 0.8),
                "location_diversity": random.randint(1, 10),
                "roaming_frequency": random.randint(0, 5),
                "home_area_ratio": random.uniform(0.5, 0.95),
                "monthly_spend": random.uniform(10, 500),
                "recharge_frequency": random.randint(0, 10),
                "service_usage_variety": random.randint(1, 15),
                "timestamp": datetime.now().isoformat()
            }
            
            # 发送到Kafka
            producer.send('user_behavior_events', event)
            
            # 调用预测API
            response = requests.post('http://localhost:5001/predict/user_behavior', json=event)
            if response.status_code == 200:
                result = response.json()
                print(f"用户行为预测: IMSI={event['imsi']}, 流失风险={result['risk_level']}")
            
            time.sleep(random.uniform(1, 3))
            
        except Exception as e:
            print(f"用户行为事件生成失败: {e}")
            time.sleep(5)

def generate_fraud_detection_events():
    """生成欺诈检测事件"""
    while True:
        try:
            event = {
                "event_type": "transaction",
                "transaction_id": f"TXN{random.randint(100000, 999999)}",
                "imsi": f"46000{random.randint(100000000, 999999999)}",
                "transaction_amount": random.uniform(1, 1000),
                "transaction_type": random.choice(['RECHARGE', 'SERVICE_FEE', 'DATA_PACKAGE', 'ROAMING_FEE']),
                "user_avg_amount": random.uniform(10, 200),
                "user_transaction_freq": random.randint(1, 30),
                "unusual_time_transactions": random.randint(0, 5),
                "location_risk_score": random.uniform(0, 1),
                "location_change_freq": random.randint(0, 10),
                "distance_from_usual": random.uniform(0, 1000),
                "device_risk_score": random.uniform(0, 1),
                "device_change_freq": random.randint(0, 5),
                "network_anomaly_score": random.uniform(0, 1),
                "connection_pattern_anomaly": random.uniform(0, 1),
                "timestamp": datetime.now().isoformat()
            }
            
            # 发送到Kafka
            producer.send('fraud_detection_events', event)
            
            # 调用预测API
            response = requests.post('http://localhost:5001/predict/fraud_detection', json=event)
            if response.status_code == 200:
                result = response.json()
                print(f"欺诈检测: TXN={event['transaction_id']}, 决策={result['decision']}, 风险={result['risk_level']}")
            
            time.sleep(random.uniform(0.5, 2))
            
        except Exception as e:
            print(f"欺诈检测事件生成失败: {e}")
            time.sleep(5)

def generate_network_optimization_events():
    """生成网络优化事件"""
    while True:
        try:
            event = {
                "event_type": "network_metrics",
                "network_id": f"NET{random.randint(1000, 9999)}",
                "avg_throughput": random.uniform(50, 500),
                "peak_throughput": random.uniform(100, 800),
                "avg_latency": random.uniform(10, 200),
                "packet_loss_rate": random.uniform(0, 0.05),
                "cpu_utilization": random.uniform(0.1, 0.95),
                "memory_utilization": random.uniform(0.2, 0.9),
                "bandwidth_utilization": random.uniform(0.3, 0.95),
                "active_connections": random.randint(100, 10000),
                "connection_establishment_rate": random.uniform(10, 1000),
                "timestamp": datetime.now().isoformat()
            }
            
            # 发送到Kafka
            producer.send('network_optimization_events', event)
            
            # 调用预测API
            response = requests.post('http://localhost:5001/predict/network_optimization', json=event)
            if response.status_code == 200:
                result = response.json()
                print(f"网络优化: NET={event['network_id']}, 性能等级={result['performance_level']}")
            
            time.sleep(random.uniform(2, 5))
            
        except Exception as e:
            print(f"网络优化事件生成失败: {e}")
            time.sleep(5)

if __name__ == "__main__":
    print("启动实时数据生成器...")
    
    # 启动多个线程生成不同类型的事件
    threading.Thread(target=generate_user_behavior_events, daemon=True).start()
    threading.Thread(target=generate_fraud_detection_events, daemon=True).start()
    threading.Thread(target=generate_network_optimization_events, daemon=True).start()
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("停止数据生成器")
EOF

# 5. 等待服务启动
echo "等待服务启动..."
sleep 30

# 6. 创建Kafka主题
echo "创建Kafka主题..."
docker exec kafka kafka-topics --create --topic user_behavior_events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec kafka kafka-topics --create --topic fraud_detection_events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
docker exec kafka kafka-topics --create --topic network_optimization_events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 7. 启动实时数据生成器
echo "启动实时数据生成器..."
python real_time_data_generator.py &

echo "=== 实时预测服务部署完成 ==="
echo "服务访问地址:"
echo "- 模型预测API: http://localhost:5001"
echo "- Kafka: localhost:9092"
echo "- 健康检查: http://localhost:5001/health"
echo ""
echo "API使用示例:"
echo "curl -X POST http://localhost:5001/predict/user_behavior -H 'Content-Type: application/json' -d '{...}'"
echo "curl -X POST http://localhost:5001/predict/fraud_detection -H 'Content-Type: application/json' -d '{...}'"
echo "curl -X POST http://localhost:5001/predict/network_optimization -H 'Content-Type: application/json' -d '{...}'"
```

## 最佳实践总结

### 1. 模型开发最佳实践

#### 数据质量管理
- **数据清洗**: 处理缺失值、异常值和重复数据
- **特征工程**: 创建有意义的特征，提高模型性能
- **数据平衡**: 处理类别不平衡问题，使用SMOTE等技术
- **数据版本控制**: 使用DVC等工具管理数据版本

#### 模型训练优化
- **交叉验证**: 使用K折交叉验证评估模型性能
- **超参数调优**: 使用网格搜索或贝叶斯优化
- **模型集成**: 结合多个模型提高预测准确性
- **正则化**: 防止过拟合，提高模型泛化能力

#### 模型评估指标
- **分类任务**: 准确率、精确率、召回率、F1分数、AUC-ROC
- **回归任务**: MAE、MSE、RMSE、R²
- **业务指标**: 结合具体业务场景定义评估指标

### 2. 模型部署最佳实践

#### 模型版本管理
- **模型注册**: 使用MLflow Model Registry管理模型版本
- **A/B测试**: 对比新旧模型性能
- **灰度发布**: 逐步替换生产环境中的模型
- **回滚机制**: 快速回滚到稳定版本

#### 服务架构设计
- **微服务架构**: 将不同模型部署为独立服务
- **负载均衡**: 使用Nginx或云负载均衡器
- **容器化部署**: 使用Docker和Kubernetes
- **服务监控**: 监控服务健康状态和性能指标

#### 性能优化
- **模型压缩**: 使用量化、剪枝等技术减小模型大小
- **批量预测**: 提高吞吐量
- **缓存策略**: 缓存频繁请求的预测结果
- **异步处理**: 使用消息队列处理大量预测请求

### 3. 数据安全与隐私保护

#### 数据脱敏
- **敏感信息加密**: 对IMSI、手机号等敏感信息加密存储
- **数据匿名化**: 移除或替换可识别个人身份的信息
- **差分隐私**: 在数据分析中添加噪声保护隐私

#### 访问控制
- **身份认证**: 使用JWT或OAuth2进行API认证
- **权限管理**: 基于角色的访问控制(RBAC)
- **审计日志**: 记录所有数据访问和模型调用

### 4. 运维监控最佳实践

#### 模型性能监控
- **预测准确性**: 持续监控模型预测准确性
- **数据漂移检测**: 监控输入数据分布变化
- **模型漂移检测**: 监控模型性能下降
- **业务指标监控**: 监控模型对业务KPI的影响

#### 系统监控
- **资源使用**: 监控CPU、内存、存储使用情况
- **响应时间**: 监控API响应时间和吞吐量
- **错误率**: 监控系统错误率和异常
- **依赖服务**: 监控数据库、消息队列等依赖服务

#### 告警机制
- **阈值告警**: 设置关键指标阈值告警
- **异常检测**: 使用统计方法检测异常
- **多渠道通知**: 邮件、短信、钉钉等多渠道告警
- **告警升级**: 根据严重程度自动升级告警

### 5. 持续改进策略

#### 模型迭代
- **定期重训练**: 根据新数据定期重新训练模型
- **在线学习**: 使用增量学习技术实时更新模型
- **反馈循环**: 收集预测结果反馈改进模型
- **特征更新**: 根据业务变化更新特征工程

#### 技术演进
- **新算法探索**: 跟踪最新的机器学习算法和技术
- **框架升级**: 及时升级机器学习框架和工具
- **硬件优化**: 利用GPU、TPU等硬件加速
- **云服务集成**: 利用云平台的AI/ML服务

### AI应用场景枚举

```java
/**
 * AI应用场景枚举
 * 定义电信领域的AI应用场景
 */
public enum AIApplicationScenario {
    /**
     * 用户行为分析
     */
    USER_BEHAVIOR_ANALYSIS("USER_BEHAVIOR_ANALYSIS", "用户行为分析", "分析用户使用模式和偏好"),
    
    /**
     * 欺诈检测
     */
    FRAUD_DETECTION("FRAUD_DETECTION", "欺诈检测", "识别和防范欺诈行为"),
    
    /**
     * 网络优化
     */
    NETWORK_OPTIMIZATION("NETWORK_OPTIMIZATION", "网络优化", "优化网络性能和资源配置"),
    
    /**
     * 预测性维护
     */
    PREDICTIVE_MAINTENANCE("PREDICTIVE_MAINTENANCE", "预测性维护", "预测设备故障和维护需求"),
    
    /**
     * 客户流失预测
     */
    CHURN_PREDICTION("CHURN_PREDICTION", "客户流失预测", "预测客户流失风险"),
    
    /**
     * 个性化推荐
     */
    PERSONALIZED_RECOMMENDATION("PERSONALIZED_RECOMMENDATION", "个性化推荐", "提供个性化服务推荐"),
    
    /**
     * 智能客服
     */
    INTELLIGENT_CUSTOMER_SERVICE("INTELLIGENT_CUSTOMER_SERVICE", "智能客服", "自动化客户服务和支持"),
    
    /**
     * 资源需求预测
     */
    RESOURCE_DEMAND_FORECASTING("RESOURCE_DEMAND_FORECASTING", "资源需求预测", "预测资源使用需求");
    
    private final String code;
    private final String description;
    private final String detail;
    
    AIApplicationScenario(String code, String description, String detail) {
        this.code = code;
        this.description = description;
        this.detail = detail;
    }
    
    // getters...
}
```

### 模型状态枚举

```java
/**
 * 模型状态枚举
 * 定义ML模型的生命周期状态
 */
public enum ModelStatus {
    /**
     * 开发中
     */
    DEVELOPING("DEVELOPING", "开发中"),
    
    /**
     * 训练中
     */
    TRAINING("TRAINING", "训练中"),
    
    /**
     * 验证中
     */
    VALIDATING("VALIDATING", "验证中"),
    
    /**
     * 已部署
     */
    DEPLOYED("DEPLOYED", "已部署"),
    
    /**
     * 运行中
     */
    RUNNING("RUNNING", "运行中"),
    
    /**
     * 已暂停
     */
    PAUSED("PAUSED", "已暂停"),
    
    /**
     * 已停用
     */
    DEPRECATED("DEPRECATED", "已停用"),
    
    /**
     * 失败
     */
    FAILED("FAILED", "失败");
    
    private final String code;
    private final String description;
    
    ModelStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    // getters...
}
```

## AI/ML模型管理

### ML模型实体

```java
/**
 * ML模型实体
 * 定义机器学习模型的基本信息和配置
 */
@Entity
@Table(name = "ml_model")
public class MLModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模型名称
     */
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;
    
    /**
     * 模型版本
     */
    @Column(name = "model_version", nullable = false, length = 20)
    private String modelVersion;
    
    /**
     * 模型描述
     */
    @Column(name = "model_description", length = 500)
    private String modelDescription;
    
    /**
     * 模型类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false)
    private MLModelType modelType;
    
    /**
     * 应用场景
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "application_scenario", nullable = false)
    private AIApplicationScenario applicationScenario;
    
    /**
     * 模型状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "model_status", nullable = false)
    private ModelStatus modelStatus;
    
    /**
     * 模型算法
     */
    @Column(name = "algorithm", length = 100)
    private String algorithm;
    
    /**
     * 训练数据集
     */
    @Column(name = "training_dataset", length = 200)
    private String trainingDataset;
    
    /**
     * 特征配置（JSON格式）
     */
    @Column(name = "feature_config", columnDefinition = "TEXT")
    private String featureConfig;
    
    /**
     * 超参数配置（JSON格式）
     */
    @Column(name = "hyperparameters", columnDefinition = "TEXT")
    private String hyperparameters;
    
    /**
     * 模型性能指标（JSON格式）
     */
    @Column(name = "performance_metrics", columnDefinition = "TEXT")
    private String performanceMetrics;
    
    /**
     * 模型文件路径
     */
    @Column(name = "model_file_path", length = 500)
    private String modelFilePath;
    
    /**
     * 部署配置（JSON格式）
     */
    @Column(name = "deployment_config", columnDefinition = "TEXT")
    private String deploymentConfig;
    
    /**
     * 训练开始时间
     */
    @Column(name = "training_started_at")
    private LocalDateTime trainingStartedAt;
    
    /**
     * 训练完成时间
     */
    @Column(name = "training_completed_at")
    private LocalDateTime trainingCompletedAt;
    
    /**
     * 部署时间
     */
    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;
    
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
    
    /**
     * 创建者
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    // constructors, getters, setters...
}
```

### 模型训练任务实体

```java
/**
 * 模型训练任务实体
 * 定义ML模型训练任务的配置和状态
 */
@Entity
@Table(name = "model_training_job")
public class ModelTrainingJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的模型ID
     */
    @Column(name = "model_id", nullable = false)
    private Long modelId;
    
    /**
     * 任务名称
     */
    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;
    
    /**
     * 训练配置（JSON格式）
     */
    @Column(name = "training_config", columnDefinition = "TEXT")
    private String trainingConfig;
    
    /**
     * 数据源配置
     */
    @Column(name = "data_source_config", columnDefinition = "TEXT")
    private String dataSourceConfig;
    
    /**
     * 计算资源配置
     */
    @Column(name = "compute_resource_config", columnDefinition = "TEXT")
    private String computeResourceConfig;
    
    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false)
    private ModelStatus jobStatus;
    
    /**
     * 训练进度（百分比）
     */
    @Column(name = "training_progress")
    private Integer trainingProgress;
    
    /**
     * 训练日志
     */
    @Column(name = "training_logs", columnDefinition = "TEXT")
    private String trainingLogs;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 开始时间
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    /**
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
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

### 模型预测结果实体

```java
/**
 * 模型预测结果实体
 * 存储ML模型的预测结果和相关信息
 */
@Entity
@Table(name = "model_prediction_result")
public class ModelPredictionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模型ID
     */
    @Column(name = "model_id", nullable = false)
    private Long modelId;
    
    /**
     * 预测请求ID
     */
    @Column(name = "prediction_request_id", length = 100)
    private String predictionRequestId;
    
    /**
     * 输入特征（JSON格式）
     */
    @Column(name = "input_features", columnDefinition = "TEXT")
    private String inputFeatures;
    
    /**
     * 预测结果（JSON格式）
     */
    @Column(name = "prediction_result", columnDefinition = "TEXT")
    private String predictionResult;
    
    /**
     * 置信度
     */
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    /**
     * 预测时间
     */
    @Column(name = "prediction_time")
    private LocalDateTime predictionTime;
    
    /**
     * 处理耗时（毫秒）
     */
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    /**
     * 业务上下文（JSON格式）
     */
    @Column(name = "business_context", columnDefinition = "TEXT")
    private String businessContext;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // constructors, getters, setters...
}
```

## AI/ML服务实现

### ML模型管理服务

```java
/**
 * ML模型管理服务
 * 负责机器学习模型的生命周期管理
 */
@Service
@Slf4j
public class MLModelManagementService {
    
    @Autowired
    private MLModelRepository mlModelRepository;
    
    @Autowired
    private ModelTrainingJobRepository trainingJobRepository;
    
    @Autowired
    private ModelPredictionResultRepository predictionResultRepository;
    
    @Autowired
    private MLPipelineService mlPipelineService;
    
    @Autowired
    private ModelDeploymentService deploymentService;
    
    @Autowired
    private FeatureStoreService featureStoreService;
    
    /**
     * 创建新的ML模型
     * 
     * @param modelConfig 模型配置
     * @return 创建的模型
     */
    @Transactional
    public MLModel createModel(MLModelConfig modelConfig) {
        log.info("创建ML模型: {}", modelConfig.getModelName());
        
        try {
            // 1. 验证模型配置
            validateModelConfig(modelConfig);
            
            // 2. 创建模型实体
            MLModel model = new MLModel();
            model.setModelName(modelConfig.getModelName());
            model.setModelVersion("1.0.0");
            model.setModelDescription(modelConfig.getModelDescription());
            model.setModelType(modelConfig.getModelType());
            model.setApplicationScenario(modelConfig.getApplicationScenario());
            model.setModelStatus(ModelStatus.DEVELOPING);
            model.setAlgorithm(modelConfig.getAlgorithm());
            model.setTrainingDataset(modelConfig.getTrainingDataset());
            model.setFeatureConfig(JsonUtils.toJson(modelConfig.getFeatureConfig()));
            model.setHyperparameters(JsonUtils.toJson(modelConfig.getHyperparameters()));
            model.setCreatedAt(LocalDateTime.now());
            model.setCreatedBy(modelConfig.getCreatedBy());
            
            model = mlModelRepository.save(model);
            
            // 3. 初始化特征存储
            featureStoreService.initializeFeatureStore(model.getId(), modelConfig.getFeatureConfig());
            
            log.info("ML模型创建成功: id={}, name={}", model.getId(), model.getModelName());
            return model;
            
        } catch (Exception e) {
            log.error("创建ML模型失败", e);
            throw new RuntimeException("创建ML模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动模型训练
     * 
     * @param modelId 模型ID
     * @param trainingConfig 训练配置
     * @return 训练任务
     */
    @Transactional
    public ModelTrainingJob startTraining(Long modelId, ModelTrainingConfig trainingConfig) {
        log.info("启动模型训练: modelId={}", modelId);
        
        try {
            // 1. 获取模型信息
            MLModel model = mlModelRepository.findById(modelId)
                    .orElseThrow(() -> new RuntimeException("模型不存在: " + modelId));
            
            // 2. 验证模型状态
            if (model.getModelStatus() != ModelStatus.DEVELOPING) {
                throw new RuntimeException("模型状态不允许训练: " + model.getModelStatus());
            }
            
            // 3. 创建训练任务
            ModelTrainingJob trainingJob = new ModelTrainingJob();
            trainingJob.setModelId(modelId);
            trainingJob.setJobName("training-" + model.getModelName() + "-" + System.currentTimeMillis());
            trainingJob.setTrainingConfig(JsonUtils.toJson(trainingConfig));
            trainingJob.setDataSourceConfig(JsonUtils.toJson(trainingConfig.getDataSourceConfig()));
            trainingJob.setComputeResourceConfig(JsonUtils.toJson(trainingConfig.getComputeResourceConfig()));
            trainingJob.setJobStatus(ModelStatus.TRAINING);
            trainingJob.setTrainingProgress(0);
            trainingJob.setStartedAt(LocalDateTime.now());
            trainingJob.setCreatedAt(LocalDateTime.now());
            
            trainingJob = trainingJobRepository.save(trainingJob);
            
            // 4. 更新模型状态
            model.setModelStatus(ModelStatus.TRAINING);
            model.setTrainingStartedAt(LocalDateTime.now());
            model.setUpdatedAt(LocalDateTime.now());
            mlModelRepository.save(model);
            
            // 5. 提交训练任务到ML Pipeline
            mlPipelineService.submitTrainingJob(trainingJob);
            
            log.info("模型训练启动成功: jobId={}, modelId={}", trainingJob.getId(), modelId);
            return trainingJob;
            
        } catch (Exception e) {
            log.error("启动模型训练失败", e);
            throw new RuntimeException("启动模型训练失败: " + e.getMessage());
        }
    }
    
    /**
     * 部署模型
     * 
     * @param modelId 模型ID
     * @param deploymentConfig 部署配置
     * @return 部署结果
     */
    @Transactional
    public ModelDeploymentResult deployModel(Long modelId, ModelDeploymentConfig deploymentConfig) {
        log.info("部署模型: modelId={}", modelId);
        
        try {
            // 1. 获取模型信息
            MLModel model = mlModelRepository.findById(modelId)
                    .orElseThrow(() -> new RuntimeException("模型不存在: " + modelId));
            
            // 2. 验证模型状态
            if (model.getModelStatus() != ModelStatus.VALIDATING) {
                throw new RuntimeException("模型状态不允许部署: " + model.getModelStatus());
            }
            
            // 3. 执行模型部署
            ModelDeploymentResult deploymentResult = deploymentService.deployModel(model, deploymentConfig);
            
            if (deploymentResult.isSuccess()) {
                // 4. 更新模型状态
                model.setModelStatus(ModelStatus.DEPLOYED);
                model.setDeploymentConfig(JsonUtils.toJson(deploymentConfig));
                model.setDeployedAt(LocalDateTime.now());
                model.setUpdatedAt(LocalDateTime.now());
                mlModelRepository.save(model);
                
                log.info("模型部署成功: modelId={}, endpoint={}", 
                        modelId, deploymentResult.getEndpoint());
            } else {
                log.error("模型部署失败: modelId={}, error={}", 
                        modelId, deploymentResult.getErrorMessage());
            }
            
            return deploymentResult;
            
        } catch (Exception e) {
            log.error("部署模型失败", e);
            throw new RuntimeException("部署模型失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行模型预测
     * 
     * @param modelId 模型ID
     * @param inputFeatures 输入特征
     * @param businessContext 业务上下文
     * @return 预测结果
     */
    public ModelPredictionResult predict(Long modelId, 
                                       Map<String, Object> inputFeatures,
                                       Map<String, Object> businessContext) {
        log.debug("执行模型预测: modelId={}", modelId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 获取模型信息
            MLModel model = mlModelRepository.findById(modelId)
                    .orElseThrow(() -> new RuntimeException("模型不存在: " + modelId));
            
            // 2. 验证模型状态
            if (model.getModelStatus() != ModelStatus.DEPLOYED && 
                model.getModelStatus() != ModelStatus.RUNNING) {
                throw new RuntimeException("模型状态不允许预测: " + model.getModelStatus());
            }
            
            // 3. 特征预处理
            Map<String, Object> processedFeatures = featureStoreService
                    .preprocessFeatures(model.getId(), inputFeatures);
            
            // 4. 执行预测
            PredictionResponse predictionResponse = deploymentService
                    .predict(model, processedFeatures);
            
            // 5. 创建预测结果记录
            ModelPredictionResult result = new ModelPredictionResult();
            result.setModelId(modelId);
            result.setPredictionRequestId(UUID.randomUUID().toString());
            result.setInputFeatures(JsonUtils.toJson(inputFeatures));
            result.setPredictionResult(JsonUtils.toJson(predictionResponse.getResult()));
            result.setConfidenceScore(predictionResponse.getConfidenceScore());
            result.setPredictionTime(LocalDateTime.now());
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            result.setBusinessContext(JsonUtils.toJson(businessContext));
            result.setCreatedAt(LocalDateTime.now());
            
            result = predictionResultRepository.save(result);
            
            log.debug("模型预测完成: modelId={}, confidence={}, time={}ms", 
                    modelId, predictionResponse.getConfidenceScore(), 
                    result.getProcessingTimeMs());
            
            return result;
            
        } catch (Exception e) {
            log.error("执行模型预测失败", e);
            throw new RuntimeException("执行模型预测失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取模型性能指标
     * 
     * @param modelId 模型ID
     * @param timeRange 时间范围
     * @return 性能指标
     */
    public ModelPerformanceMetrics getModelPerformance(Long modelId, TimeRange timeRange) {
        log.info("获取模型性能指标: modelId={}, timeRange={}", modelId, timeRange);
        
        try {
            // 1. 获取预测结果数据
            List<ModelPredictionResult> predictions = predictionResultRepository
                    .findByModelIdAndPredictionTimeBetween(
                            modelId, timeRange.getStartTime(), timeRange.getEndTime());
            
            // 2. 计算性能指标
            ModelPerformanceMetrics metrics = new ModelPerformanceMetrics();
            metrics.setModelId(modelId);
            metrics.setTimeRange(timeRange);
            metrics.setTotalPredictions(predictions.size());
            
            if (!predictions.isEmpty()) {
                // 平均置信度
                double avgConfidence = predictions.stream()
                        .filter(p -> p.getConfidenceScore() != null)
                        .mapToDouble(ModelPredictionResult::getConfidenceScore)
                        .average()
                        .orElse(0.0);
                metrics.setAverageConfidence(avgConfidence);
                
                // 平均响应时间
                double avgResponseTime = predictions.stream()
                        .filter(p -> p.getProcessingTimeMs() != null)
                        .mapToLong(ModelPredictionResult::getProcessingTimeMs)
                        .average()
                        .orElse(0.0);
                metrics.setAverageResponseTime(avgResponseTime);
                
                // 吞吐量（每秒预测数）
                long timeRangeSeconds = Duration.between(
                        timeRange.getStartTime(), timeRange.getEndTime()).getSeconds();
                double throughput = timeRangeSeconds > 0 ? 
                        (double) predictions.size() / timeRangeSeconds : 0.0;
                metrics.setThroughput(throughput);
                
                // 错误率（低置信度预测比例）
                long lowConfidencePredictions = predictions.stream()
                        .filter(p -> p.getConfidenceScore() != null && p.getConfidenceScore() < 0.7)
                        .count();
                double errorRate = (double) lowConfidencePredictions / predictions.size();
                metrics.setErrorRate(errorRate);
            }
            
            return metrics;
            
        } catch (Exception e) {
            log.error("获取模型性能指标失败", e);
            throw new RuntimeException("获取模型性能指标失败: " + e.getMessage());
        }
    }
    
    /**
     * 模型版本管理
     * 
     * @param modelId 模型ID
     * @param newVersion 新版本号
     * @return 新版本模型
     */
    @Transactional
    public MLModel createModelVersion(Long modelId, String newVersion) {
        log.info("创建模型版本: modelId={}, version={}", modelId, newVersion);
        
        try {
            // 1. 获取原模型
            MLModel originalModel = mlModelRepository.findById(modelId)
                    .orElseThrow(() -> new RuntimeException("模型不存在: " + modelId));
            
            // 2. 创建新版本模型
            MLModel newVersionModel = new MLModel();
            BeanUtils.copyProperties(originalModel, newVersionModel);
            newVersionModel.setId(null);
            newVersionModel.setModelVersion(newVersion);
            newVersionModel.setModelStatus(ModelStatus.DEVELOPING);
            newVersionModel.setTrainingStartedAt(null);
            newVersionModel.setTrainingCompletedAt(null);
            newVersionModel.setDeployedAt(null);
            newVersionModel.setCreatedAt(LocalDateTime.now());
            newVersionModel.setUpdatedAt(LocalDateTime.now());
            
            newVersionModel = mlModelRepository.save(newVersionModel);
            
            log.info("模型版本创建成功: originalId={}, newId={}, version={}", 
                    modelId, newVersionModel.getId(), newVersion);
            
            return newVersionModel;
            
        } catch (Exception e) {
            log.error("创建模型版本失败", e);
            throw new RuntimeException("创建模型版本失败: " + e.getMessage());
        }
    }
    
    /**
     * 模型A/B测试
     * 
     * @param modelAId 模型A的ID
     * @param modelBId 模型B的ID
     * @param testConfig A/B测试配置
     * @return A/B测试结果
     */
    public ABTestResult performABTest(Long modelAId, Long modelBId, ABTestConfig testConfig) {
        log.info("执行模型A/B测试: modelA={}, modelB={}", modelAId, modelBId);
        
        try {
            // 1. 获取测试数据
            List<Map<String, Object>> testData = featureStoreService
                    .getTestDataset(testConfig.getTestDatasetName());
            
            // 2. 执行A/B测试
            List<ModelPredictionResult> resultsA = new ArrayList<>();
            List<ModelPredictionResult> resultsB = new ArrayList<>();
            
            for (Map<String, Object> testCase : testData) {
                // 模型A预测
                ModelPredictionResult resultA = predict(modelAId, testCase, 
                        Map.of("abTest", "modelA"));
                resultsA.add(resultA);
                
                // 模型B预测
                ModelPredictionResult resultB = predict(modelBId, testCase, 
                        Map.of("abTest", "modelB"));
                resultsB.add(resultB);
            }
            
            // 3. 计算A/B测试指标
            ABTestResult testResult = new ABTestResult();
            testResult.setModelAId(modelAId);
            testResult.setModelBId(modelBId);
            testResult.setTestConfig(testConfig);
            
            // 计算平均置信度
            double avgConfidenceA = resultsA.stream()
                    .mapToDouble(ModelPredictionResult::getConfidenceScore)
                    .average().orElse(0.0);
            double avgConfidenceB = resultsB.stream()
                    .mapToDouble(ModelPredictionResult::getConfidenceScore)
                    .average().orElse(0.0);
            
            testResult.setModelAAvgConfidence(avgConfidenceA);
            testResult.setModelBAvgConfidence(avgConfidenceB);
            
            // 计算平均响应时间
            double avgResponseTimeA = resultsA.stream()
                    .mapToLong(ModelPredictionResult::getProcessingTimeMs)
                    .average().orElse(0.0);
            double avgResponseTimeB = resultsB.stream()
                    .mapToLong(ModelPredictionResult::getProcessingTimeMs)
                    .average().orElse(0.0);
            
            testResult.setModelAAvgResponseTime(avgResponseTimeA);
            testResult.setModelBAvgResponseTime(avgResponseTimeB);
            
            // 确定获胜模型
            if (avgConfidenceA > avgConfidenceB) {
                testResult.setWinnerModelId(modelAId);
                testResult.setConfidenceImprovement(avgConfidenceA - avgConfidenceB);
            } else {
                testResult.setWinnerModelId(modelBId);
                testResult.setConfidenceImprovement(avgConfidenceB - avgConfidenceA);
            }
            
            testResult.setTestCompletedAt(LocalDateTime.now());
            
            log.info("A/B测试完成: winner={}, improvement={}", 
                    testResult.getWinnerModelId(), testResult.getConfidenceImprovement());
            
            return testResult;
            
        } catch (Exception e) {
            log.error("执行模型A/B测试失败", e);
            throw new RuntimeException("执行模型A/B测试失败: " + e.getMessage());
        }
    }
    
    // 辅助方法实现...
    private void validateModelConfig(MLModelConfig config) {
        if (config.getModelName() == null || config.getModelName().trim().isEmpty()) {
            throw new RuntimeException("模型名称不能为空");
        }
        
        if (config.getModelType() == null) {
            throw new RuntimeException("模型类型不能为空");
        }
        
        if (config.getApplicationScenario() == null) {
            throw new RuntimeException("应用场景不能为空");
        }
        
        if (config.getAlgorithm() == null || config.getAlgorithm().trim().isEmpty()) {
            throw new RuntimeException("算法不能为空");
        }
    }
}
```

## 电信AI应用场景实现

### 用户行为分析服务

```java
/**
 * 用户行为分析服务
 * 基于AI/ML技术分析用户行为模式
 */
@Service
@Slf4j
public class UserBehaviorAnalysisService {
    
    @Autowired
    private MLModelManagementService modelManagementService;
    
    @Autowired
    private FeatureStoreService featureStoreService;
    
    @Autowired
    private SimCardUsageRepository usageRepository;
    
    /**
     * 分析用户行为模式
     * 
     * @param imsi 用户IMSI
     * @param analysisTimeRange 分析时间范围
     * @return 行为分析结果
     */
    public UserBehaviorAnalysisResult analyzeUserBehavior(String imsi, TimeRange analysisTimeRange) {
        log.info("分析用户行为: imsi={}, timeRange={}", imsi, analysisTimeRange);
        
        try {
            // 1. 提取用户行为特征
            Map<String, Object> behaviorFeatures = extractBehaviorFeatures(imsi, analysisTimeRange);
            
            // 2. 使用聚类模型进行行为分类
            Long clusteringModelId = getModelIdByScenario(AIApplicationScenario.USER_BEHAVIOR_ANALYSIS);
            ModelPredictionResult clusterResult = modelManagementService.predict(
                    clusteringModelId, behaviorFeatures, Map.of("imsi", imsi));
            
            // 3. 解析聚类结果
            Map<String, Object> clusterData = JsonUtils.fromJson(
                    clusterResult.getPredictionResult(), Map.class);
            String behaviorCluster = (String) clusterData.get("cluster");
            
            // 4. 生成行为分析报告
            UserBehaviorAnalysisResult result = new UserBehaviorAnalysisResult();
            result.setImsi(imsi);
            result.setAnalysisTimeRange(analysisTimeRange);
            result.setBehaviorCluster(behaviorCluster);
            result.setBehaviorFeatures(behaviorFeatures);
            result.setConfidenceScore(clusterResult.getConfidenceScore());
            
            // 5. 生成行为洞察
            result.setBehaviorInsights(generateBehaviorInsights(behaviorFeatures, behaviorCluster));
            
            // 6. 生成个性化推荐
            result.setPersonalizedRecommendations(generatePersonalizedRecommendations(
                    imsi, behaviorCluster, behaviorFeatures));
            
            result.setAnalysisTime(LocalDateTime.now());
            
            log.info("用户行为分析完成: imsi={}, cluster={}, confidence={}", 
                    imsi, behaviorCluster, clusterResult.getConfidenceScore());
            
            return result;
            
        } catch (Exception e) {
            log.error("分析用户行为失败", e);
            throw new RuntimeException("分析用户行为失败: " + e.getMessage());
        }
    }
    
    /**
     * 预测用户流失风险
     * 
     * @param imsi 用户IMSI
     * @return 流失风险预测结果
     */
    public ChurnPredictionResult predictChurnRisk(String imsi) {
        log.info("预测用户流失风险: imsi={}", imsi);
        
        try {
            // 1. 提取流失预测特征
            Map<String, Object> churnFeatures = extractChurnFeatures(imsi);
            
            // 2. 使用流失预测模型
            Long churnModelId = getModelIdByScenario(AIApplicationScenario.CHURN_PREDICTION);
            ModelPredictionResult churnResult = modelManagementService.predict(
                    churnModelId, churnFeatures, Map.of("imsi", imsi));
            
            // 3. 解析预测结果
            Map<String, Object> predictionData = JsonUtils.fromJson(
                    churnResult.getPredictionResult(), Map.class);
            
            Double churnProbability = (Double) predictionData.get("churn_probability");
            String riskLevel = (String) predictionData.get("risk_level");
            
            // 4. 生成预测结果
            ChurnPredictionResult result = new ChurnPredictionResult();
            result.setImsi(imsi);
            result.setChurnProbability(churnProbability);
            result.setRiskLevel(ChurnRiskLevel.valueOf(riskLevel));
            result.setConfidenceScore(churnResult.getConfidenceScore());
            result.setChurnFeatures(churnFeatures);
            
            // 5. 生成风险因子分析
            result.setRiskFactors(analyzeChurnRiskFactors(churnFeatures, churnProbability));
            
            // 6. 生成挽留建议
            result.setRetentionRecommendations(generateRetentionRecommendations(
                    imsi, churnProbability, riskLevel));
            
            result.setPredictionTime(LocalDateTime.now());
            
            log.info("用户流失风险预测完成: imsi={}, probability={}, risk={}", 
                    imsi, churnProbability, riskLevel);
            
            return result;
            
        } catch (Exception e) {
            log.error("预测用户流失风险失败", e);
            throw new RuntimeException("预测用户流失风险失败: " + e.getMessage());
        }
    }
    
    // 辅助方法实现...
    private Map<String, Object> extractBehaviorFeatures(String imsi, TimeRange timeRange) {
        // 实现行为特征提取逻辑
        Map<String, Object> features = new HashMap<>();
        
        // 通话行为特征
        features.put("avg_call_duration", calculateAvgCallDuration(imsi, timeRange));
        features.put("call_frequency", calculateCallFrequency(imsi, timeRange));
        features.put("peak_hour_usage", calculatePeakHourUsage(imsi, timeRange));
        
        // 数据使用特征
        features.put("avg_data_usage", calculateAvgDataUsage(imsi, timeRange));
        features.put("data_usage_pattern", analyzeDataUsagePattern(imsi, timeRange));
        
        // 位置行为特征
        features.put("location_diversity", calculateLocationDiversity(imsi, timeRange));
        features.put("roaming_frequency", calculateRoamingFrequency(imsi, timeRange));
        
        // 网络偏好特征
        features.put("network_type_preference", analyzeNetworkTypePreference(imsi, timeRange));
        
        return features;
    }
    
    private Map<String, Object> extractChurnFeatures(String imsi) {
        // 实现流失预测特征提取逻辑
        Map<String, Object> features = new HashMap<>();
        
        // 使用趋势特征
        features.put("usage_trend_30d", calculateUsageTrend(imsi, 30));
        features.put("usage_trend_90d", calculateUsageTrend(imsi, 90));
        
        // 账单和支付特征
        features.put("avg_monthly_bill", calculateAvgMonthlyBill(imsi));
        features.put("payment_delay_frequency", calculatePaymentDelayFrequency(imsi));
        
        // 客服交互特征
        features.put("customer_service_calls", getCustomerServiceCalls(imsi));
        features.put("complaint_frequency", getComplaintFrequency(imsi));
        
        // 竞争对手活动特征
        features.put("competitor_activity_score", getCompetitorActivityScore(imsi));
        
        return features;
    }
    
    private Long getModelIdByScenario(AIApplicationScenario scenario) {
        // 根据应用场景获取对应的模型ID
        // 这里简化实现，实际应该从配置或数据库中获取
        switch (scenario) {
            case USER_BEHAVIOR_ANALYSIS:
                return 1L;
            case CHURN_PREDICTION:
                return 2L;
            case FRAUD_DETECTION:
                return 3L;
            default:
                throw new RuntimeException("不支持的应用场景: " + scenario);
        }
    }
    
    private List<String> generateBehaviorInsights(Map<String, Object> features, String cluster) {
        // 生成行为洞察
        List<String> insights = new ArrayList<>();
        
        switch (cluster) {
            case "heavy_user":
                insights.add("用户属于重度使用者，数据使用量较高");
                insights.add("建议推荐大流量套餐");
                break;
            case "business_user":
                insights.add("用户具有商务用户特征，通话频率较高");
                insights.add("建议推荐商务套餐");
                break;
            case "casual_user":
                insights.add("用户属于轻度使用者，使用量较低");
                insights.add("建议推荐经济型套餐");
                break;
            default:
                insights.add("用户行为模式需要进一步分析");
        }
        
        return insights;
    }
    
    private List<String> generatePersonalizedRecommendations(String imsi, String cluster, Map<String, Object> features) {
        // 生成个性化推荐
        List<String> recommendations = new ArrayList<>();
        
        // 基于聚类结果和特征生成推荐
        if ("heavy_user".equals(cluster)) {
            recommendations.add("推荐升级到无限流量套餐");
            recommendations.add("推荐5G网络服务");
        } else if ("business_user".equals(cluster)) {
            recommendations.add("推荐商务通话套餐");
            recommendations.add("推荐国际漫游服务");
        }
        
        return recommendations;
    }
    
    private List<String> analyzeChurnRiskFactors(Map<String, Object> features, Double churnProbability) {
        // 分析流失风险因子
        List<String> riskFactors = new ArrayList<>();
        
        if (churnProbability > 0.7) {
            riskFactors.add("使用量显著下降");
            riskFactors.add("客服投诉增加");
            riskFactors.add("支付延迟频繁");
        } else if (churnProbability > 0.4) {
            riskFactors.add("使用模式发生变化");
            riskFactors.add("竞争对手活动活跃");
        }
        
        return riskFactors;
    }
    
    private List<String> generateRetentionRecommendations(String imsi, Double churnProbability, String riskLevel) {
        // 生成挽留建议
        List<String> recommendations = new ArrayList<>();
        
        if (churnProbability > 0.7) {
            recommendations.add("立即安排客户经理联系");
            recommendations.add("提供专属优惠套餐");
            recommendations.add("赠送增值服务");
        } else if (churnProbability > 0.4) {
            recommendations.add("发送关怀短信");
            recommendations.add("推荐更适合的套餐");
        }
        
        return recommendations;
    }
    
    // 其他辅助计算方法...
    private Double calculateAvgCallDuration(String imsi, TimeRange timeRange) {
        // 计算平均通话时长
        return 0.0;
    }
    
    private Integer calculateCallFrequency(String imsi, TimeRange timeRange) {
        // 计算通话频率
        return 0;
    }
    
    private Double calculatePeakHourUsage(String imsi, TimeRange timeRange) {
        // 计算高峰时段使用量
        return 0.0;
    }
    
    private Double calculateAvgDataUsage(String imsi, TimeRange timeRange) {
        // 计算平均数据使用量
        return 0.0;
    }
    
    private String analyzeDataUsagePattern(String imsi, TimeRange timeRange) {
        // 分析数据使用模式
        return "normal";
    }
    
    private Integer calculateLocationDiversity(String imsi, TimeRange timeRange) {
        // 计算位置多样性
        return 0;
    }
    
    private Integer calculateRoamingFrequency(String imsi, TimeRange timeRange) {
        // 计算漫游频率
        return 0;
    }
    
    private String analyzeNetworkTypePreference(String imsi, TimeRange timeRange) {
        // 分析网络类型偏好
        return "4G";
    }
    
    private Double calculateUsageTrend(String imsi, int days) {
        // 计算使用趋势
        return 0.0;
    }
    
    private Double calculateAvgMonthlyBill(String imsi) {
        // 计算平均月账单
        return 0.0;
    }
    
    private Integer calculatePaymentDelayFrequency(String imsi) {
        // 计算支付延迟频率
        return 0;
    }
    
    private Integer getCustomerServiceCalls(String imsi) {
        // 获取客服通话次数
        return 0;
    }
    
    private Integer getComplaintFrequency(String imsi) {
        // 获取投诉频率
        return 0;
    }
    
    private Double getCompetitorActivityScore(String imsi) {
        // 获取竞争对手活动评分
        return 0.0;
    }
}
```