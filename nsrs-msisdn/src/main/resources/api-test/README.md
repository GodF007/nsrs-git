# NSRS-MSISDN 模块 API 测试用例

本目录包含 nsrs-msisdn 模块所有 Controller 接口的测试用例，用于 ApiFox 工具进行自测。

## 测试用例文件列表

1. **NumberResourceController_TestCases.json** - 号码资源管理接口测试用例
   - 包含分页查询、跨表查询、新增、修改、删除、批量操作、状态管理等接口
   
2. **NumberSegmentController_TestCases.json** - 号码段管理接口测试用例
   - 包含分页查询、详情获取、新增、修改、删除、启用/禁用、列表查询等接口
   
3. **NumberLevelController_TestCases.json** - 号码级别管理接口测试用例
   - 包含分页查询、详情获取、新增、修改、删除、启用/禁用、列表查询等接口
   
4. **NumberPatternController_TestCases.json** - 号码模式管理接口测试用例
   - 包含分页查询、详情获取、新增、修改、删除、启用/禁用、验证等接口
   
5. **NumberApprovalController_TestCases.json** - 号码审批管理接口测试用例
   - 包含分页查询、申请、审批通过/拒绝、取消申请、详情获取等接口
   
6. **NumberOperationLogController_TestCases.json** - 号码操作日志接口测试用例
   - 包含分页查询、按号码ID查询、统计信息等接口
   
7. **HlrSwitchController_TestCases.json** - HLR/交换机管理接口测试用例
   - 包含分页查询、详情获取、新增、修改、删除、启用/禁用、列表查询等接口
   
8. **RegionController_TestCases.json** - 区域管理接口测试用例
   - 包含分页查询、详情获取、新增、修改、删除、启用/禁用、树形结构等接口

## 使用说明

1. 将对应的 JSON 文件导入到 ApiFox 工具中
2. 根据实际环境修改基础 URL
3. 根据需要调整请求参数
4. 执行测试用例进行接口验证

## 注意事项

- 所有测试用例中的示例数据仅供参考，请根据实际业务需求调整
- 部分接口需要先创建相关数据才能正常测试
- 请确保测试环境数据库已正确初始化