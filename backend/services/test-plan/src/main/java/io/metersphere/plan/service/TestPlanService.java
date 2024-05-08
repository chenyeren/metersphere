package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanCopyRequest;
import io.metersphere.plan.dto.request.TestPlanCreateRequest;
import io.metersphere.plan.dto.request.TestPlanUpdateRequest;
import io.metersphere.plan.dto.response.TestPlanCountResponse;
import io.metersphere.plan.dto.response.TestPlanDetailResponse;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.utils.TestPlanXPackFactory;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestPlanModule;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.BatchProcessUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private TestPlanLogService testPlanLogService;
    @Resource
    private TestPlanModuleMapper testPlanModuleMapper;
    @Resource
    private TestPlanFollowerMapper testPlanFollowerMapper;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionCaseService;

    public void checkModule(String moduleId) {
        if (!StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID)) {
            TestPlanModuleExample example = new TestPlanModuleExample();
            example.createCriteria().andIdEqualTo(moduleId);
            if (testPlanModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("module.not.exist"));
            }
        }
    }


    /**
     * 创建测试计划
     *
     * @param testPlanCreateRequest
     * @param operator
     * @param requestUrl
     * @param requestMethod
     * @return
     */
    public String add(TestPlanCreateRequest testPlanCreateRequest, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = savePlanDTO(testPlanCreateRequest, operator, null);
        testPlanLogService.saveAddLog(testPlan, operator, requestUrl, requestMethod);
        return testPlan.getId();
    }


    /**
     * 保存数据
     *
     * @param createOrCopyRequest
     * @param operator
     * @param id                  复制的计划/计划组id 判断新增还是复制
     */
    private TestPlan savePlanDTO(TestPlanCreateRequest createOrCopyRequest, String operator, String id) {
        //检查模块的合法性
        this.checkModule(createOrCopyRequest.getModuleId());

        TestPlan createTestPlan = new TestPlan();
        BeanUtils.copyBean(createTestPlan, createOrCopyRequest);
        this.validateTestPlan(createTestPlan);

        createTestPlan.setId(IDGenerator.nextStr());
        long operateTime = System.currentTimeMillis();
        createTestPlan.setNum(NumGenerator.nextNum(createOrCopyRequest.getProjectId(), ApplicationNumScope.TEST_PLAN));
        createTestPlan.setCreateUser(operator);
        createTestPlan.setUpdateUser(operator);
        createTestPlan.setCreateTime(operateTime);
        createTestPlan.setUpdateTime(operateTime);
        createTestPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_PREPARED);

        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(createTestPlan.getId());
        testPlanConfig.setAutomaticStatusUpdate(createOrCopyRequest.isAutomaticStatusUpdate());
        testPlanConfig.setRepeatCase(createOrCopyRequest.isRepeatCase());
        testPlanConfig.setPassThreshold(createOrCopyRequest.getPassThreshold());
        testPlanConfig.setTestPlanning(createOrCopyRequest.isTestPlanning());

        if (StringUtils.isBlank(id)) {
            handleAssociateCase(createOrCopyRequest, createTestPlan);
        } else {
            //复制
            handleCopy(createTestPlan, id);
        }

        testPlanMapper.insert(createTestPlan);
        testPlanConfigMapper.insert(testPlanConfig);
        return createTestPlan;
    }

    /**
     * 处理关联的用例
     *
     * @param request
     * @return
     */
    private void handleAssociateCase(TestPlanCreateRequest request, TestPlan testPlan) {
        //关联的功能用例
        handleFunctionalCase(request.getBaseAssociateCaseRequest().getFunctionalSelectIds(), testPlan);
        //TODO 关联接口用例/接口场景用例

    }

    /**
     * 关联的功能用例
     *
     * @param functionalSelectIds
     */
    private void handleFunctionalCase(List<String> functionalSelectIds, TestPlan testPlan) {
        if (CollectionUtils.isNotEmpty(functionalSelectIds)) {
            TestPlanResourceAssociationParam associationParam = new TestPlanResourceAssociationParam(functionalSelectIds, testPlan.getProjectId(), testPlan.getId(), testPlan.getNum(), testPlan.getCreateUser());
            testPlanFunctionCaseService.saveTestPlanResource(associationParam);
        }
    }

    private void validateTestPlan(TestPlan testPlan) {
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN) && !StringUtils.equals(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan group = testPlanMapper.selectByPrimaryKey(testPlan.getGroupId());
            testPlan.setModuleId(group.getModuleId());
        }
        TestPlanExample example = new TestPlanExample();
        if (StringUtils.isBlank(testPlan.getId())) {
            //新建 校验
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId()).andModuleIdEqualTo(testPlan.getModuleId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        }else{
            //更新 校验
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId()).andIdNotEqualTo(testPlan.getId()).andModuleIdEqualTo(testPlan.getModuleId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        }
    }


    /**
     * 删除测试计划
     *
     * @param id
     * @param operator
     * @param requestUrl
     * @param requestMethod
     */
    public void delete(String id, String operator, String requestUrl, String requestMethod) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            this.deleteGroupByList(Collections.singletonList(testPlan.getId()));
        } else {
            testPlanMapper.deleteByPrimaryKey(id);
            //级联删除
            this.cascadeDeleteTestPlanIds(Collections.singletonList(id));
        }
        //记录日志
        testPlanLogService.saveDeleteLog(testPlan, operator, requestUrl, requestMethod);
    }

    private void deleteByList(List<String> testPlanIds) {
        if (CollectionUtils.isNotEmpty(testPlanIds)) {
            BatchProcessUtils.consumerByString(testPlanIds, (deleteIds) -> {
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andIdIn(deleteIds);
                testPlanMapper.deleteByExample(testPlanExample);
                //级联删除
                this.cascadeDeleteTestPlanIds(deleteIds);
            });
        }
    }

    private void deleteGroupByList(List<String> testPlanGroupIds) {
        if (CollectionUtils.isNotEmpty(testPlanGroupIds)) {
            BatchProcessUtils.consumerByString(testPlanGroupIds, (deleteGroupIds) -> {
                TestPlanExample testPlanExample = new TestPlanExample();
                testPlanExample.createCriteria().andIdIn(deleteGroupIds);
                testPlanMapper.deleteByExample(testPlanExample);
                //级联删除
                this.cascadeDeleteTestPlanIds(deleteGroupIds);
                //更新组下的数据
                extTestPlanMapper.updateDefaultGroupId(deleteGroupIds);
            });
        }
    }


    /**
     * 批量删除测试计划
     *
     * @param request
     * @param operator
     * @param requestUrl
     * @param requestMethod
     */
    public void batchDelete(TestPlanBatchProcessRequest request, String operator, String requestUrl, String requestMethod) {
        List<String> deleteIdList = request.getSelectIds();
        if (request.isSelectAll()) {
            deleteIdList = extTestPlanMapper.selectIdByConditions(request);
        }
        if (CollectionUtils.isEmpty(deleteIdList)) {
            return;
        }
        List<TestPlan> deleteTestPlanList = extTestPlanMapper.selectBaseInfoByIds(deleteIdList);
        if (CollectionUtils.isNotEmpty(deleteTestPlanList)) {
            List<String> testPlanGroupList = new ArrayList<>();
            List<String> testPlanIdList = new ArrayList<>();
            for (TestPlan testPlan : deleteTestPlanList) {
                if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                    testPlanGroupList.add(testPlan.getId());
                } else {
                    testPlanIdList.add(testPlan.getId());
                }
            }
            this.deleteByList(deleteIdList);
            this.deleteGroupByList(testPlanGroupList);
            //记录日志
            testPlanLogService.saveBatchDeleteLog(deleteTestPlanList, operator, requestUrl, requestMethod);
        }
    }


    private void cascadeDeleteTestPlanIds(List<String> testPlanIds) {
        //删除当前计划对应的资源
        Map<String, TestPlanResourceService> subTypes = CommonBeanFactory.getBeansOfType(TestPlanResourceService.class);
        subTypes.forEach((k, t) -> {
            t.deleteBatchByTestPlanId(testPlanIds);
        });
        //删除测试计划配置
        TestPlanConfigExample configExample = new TestPlanConfigExample();
        configExample.createCriteria().andTestPlanIdIn(testPlanIds);
        testPlanConfigMapper.deleteByExample(configExample);

        TestPlanFollowerExample testPlanFollowerExample = new TestPlanFollowerExample();
        testPlanFollowerExample.createCriteria().andTestPlanIdIn(testPlanIds);
        testPlanFollowerMapper.deleteByExample(testPlanFollowerExample);
        /*
        todo
            删除计划定时任务  执行相关配置
         */

    }


    public TestPlanCountResponse getCount(String id) {
        TestPlanCountResponse response = new TestPlanCountResponse();
        response.setId(id);
        /*
        todo  统计：测试进度、通过率、用例数、Bug数量（这些比较慢的查询，是否需要另开接口查询)
         */

        response.setFunctionalCaseCount(0);
        response.setApiCaseCount(0);
        response.setApiScenarioCount(0);
        response.setPassRate("3.14%");
        response.setTestProgress("15.92%");
        return response;
    }


    /**
     * 更新测试计划
     *
     * @param request
     * @param userId
     * @param requestUrl
     * @param requestMethod
     * @return
     */
    public String update(TestPlanUpdateRequest request, String userId, String requestUrl, String requestMethod) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getId());
        if (!ObjectUtils.allNull(request.getName(), request.getModuleId(), request.getTags(), request.getPlannedEndTime(), request.getPlannedStartTime(), request.getDescription(), request.getTestPlanGroupId())) {
            TestPlan updateTestPlan = new TestPlan();
            updateTestPlan.setId(request.getId());
            if (StringUtils.isNotBlank(request.getModuleId())) {
                //检查模块的合法性
                this.checkModule(request.getModuleId());
                updateTestPlan.setModuleId(request.getModuleId());
            }
            if (StringUtils.isNotBlank(request.getName())) {
                updateTestPlan.setName(request.getName());
                updateTestPlan.setProjectId(testPlan.getProjectId());
                this.validateTestPlan(updateTestPlan);
            }
            if (CollectionUtils.isNotEmpty(request.getTags())) {
                updateTestPlan.setTags(new ArrayList<>(request.getTags()));
            }
            updateTestPlan.setPlannedStartTime(request.getPlannedStartTime());
            updateTestPlan.setPlannedEndTime(request.getPlannedEndTime());
            updateTestPlan.setDescription(request.getDescription());
            updateTestPlan.setGroupId(request.getTestPlanGroupId());
            updateTestPlan.setType(testPlan.getType());
            testPlanMapper.updateByPrimaryKeySelective(updateTestPlan);
        }

        if (!ObjectUtils.allNull(request.getAutomaticStatusUpdate(), request.getRepeatCase(), request.getPassThreshold(), request.getTestPlanning())) {
            TestPlanConfig testPlanConfig = new TestPlanConfig();
            testPlanConfig.setTestPlanId(request.getId());
            testPlanConfig.setAutomaticStatusUpdate(request.getAutomaticStatusUpdate());
            testPlanConfig.setRepeatCase(request.getRepeatCase());
            testPlanConfig.setPassThreshold(request.getPassThreshold());
            testPlanConfig.setTestPlanning(request.getTestPlanning());
            testPlanConfigMapper.updateByPrimaryKeySelective(testPlanConfig);
        }
        testPlanLogService.saveUpdateLog(testPlan, testPlanMapper.selectByPrimaryKey(request.getId()), testPlan.getProjectId(), userId, requestUrl, requestMethod);
        return request.getId();
    }


    /**
     * 关注/取消关注 测试计划
     *
     * @param testPlanId
     * @param userId
     */
    public void editFollower(String testPlanId, String userId) {
        TestPlanFollowerExample example = new TestPlanFollowerExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId).andUserIdEqualTo(userId);
        if (testPlanFollowerMapper.countByExample(example) > 0) {
            testPlanFollowerMapper.deleteByPrimaryKey(testPlanId, userId);
        } else {
            TestPlanFollower testPlanFollower = new TestPlanFollower();
            testPlanFollower.setTestPlanId(testPlanId);
            testPlanFollower.setUserId(userId);
            testPlanFollowerMapper.insert(testPlanFollower);
        }
    }


    /**
     * 测试计划归档
     *
     * @param id
     * @param userId
     */
    public void archived(String id, String userId) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        if (StringUtils.equalsAnyIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //测试计划组归档
            updateGroupStatus(testPlan.getId(), userId);
        } else {
            //测试计划
            testPlan.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            testPlan.setUpdateUser(userId);
            testPlan.setUpdateTime(System.currentTimeMillis());
            testPlanMapper.updateByPrimaryKeySelective(testPlan);
        }

    }

    /**
     * 测试计划组归档
     *
     * @param id
     * @param userId
     */
    private void updateGroupStatus(String id, String userId) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andGroupIdEqualTo(id);
        List<TestPlan> testPlanList = testPlanMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(testPlanList)) {
            throw new MSException(Translator.get("test_plan.group.not_plan"));
        }
        List<String> ids = testPlanList.stream().filter(item -> StringUtils.equalsIgnoreCase(item.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_COMPLETED)).map(TestPlan::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            throw new MSException(Translator.get("test_plan.group.not_plan"));
        }
        extTestPlanMapper.batchUpdateStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED, userId, System.currentTimeMillis(), ids);
    }

    /**
     * 复制测试计划
     *
     * @param request
     * @param userId
     * @return
     */
    public TestPlan copy(TestPlanCopyRequest request, String userId) {
        TestPlan testPlan = savePlanDTO(request, userId, request.getTestPlanId());
        return testPlan;
    }


    /**
     * 处理复制
     *
     * @param testPlan
     * @param id
     */
    private void handleCopy(TestPlan testPlan, String id) {
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            //计划组
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andGroupIdEqualTo(id);
            List<TestPlan> testPlans = testPlanMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(testPlans)) {
                return;
            }
            List<String> ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
            doHandleAssociateCase(ids, testPlan);
        } else {
            //计划
            doHandleAssociateCase(Arrays.asList(id), testPlan);
        }

    }

    /**
     * 处理复制 关联用例数据
     *
     * @param ids
     */
    private void doHandleAssociateCase(List<String> ids, TestPlan testPlan) {
        testPlanFunctionCaseService.saveTestPlanByPlanId(ids, testPlan);
        //TODO 复制关联接口用例/接口场景用例

    }


    /**
     * 获取单个测试计划或测试计划组详情（用于编辑）
     *
     * @param id
     * @return
     */
    public TestPlanDetailResponse detail(String id) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(id);
        TestPlanDetailResponse response = new TestPlanDetailResponse();
        TestPlanModule testPlanModule = testPlanModuleMapper.selectByPrimaryKey(testPlan.getModuleId());
        //计划组只有几个参数
        response.setName(testPlan.getName());
        response.setTags(testPlan.getTags());
        response.setModuleId(testPlan.getModuleId());
        response.setModuleName(testPlanModule.getName());
        response.setDescription(testPlan.getDescription());
        if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            //计划的 其他参数
            getGroupName(response, testPlan);
            response.setPlannedStartTime(testPlan.getPlannedStartTime());
            response.setPlannedEndTime(testPlan.getPlannedEndTime());
            getOtherConfig(response, testPlan);
        }
        return response;
    }

    private void getOtherConfig(TestPlanDetailResponse response, TestPlan testPlan) {
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        response.setAutomaticStatusUpdate(testPlanConfig.getAutomaticStatusUpdate());
        response.setRepeatCase(testPlanConfig.getRepeatCase());
        response.setPassThreshold(testPlanConfig.getPassThreshold());
        response.setTestPlanning(testPlanConfig.getTestPlanning());
    }

    private void getGroupName(TestPlanDetailResponse response, TestPlan testPlan) {
        if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan group = testPlanMapper.selectByPrimaryKey(testPlan.getGroupId());
            response.setGroupId(testPlan.getGroupId());
            response.setGroupName(group.getName());
        }
    }
}
