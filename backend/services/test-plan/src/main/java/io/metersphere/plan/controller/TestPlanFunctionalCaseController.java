package io.metersphere.plan.controller;

import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.request.ReviewFunctionalCasePageRequest;
import io.metersphere.plan.constants.TestPlanResourceConfig;
import io.metersphere.plan.dto.request.ResourceSortRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.request.TestPlanCaseRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.dto.response.TestPlanCasePageResponse;
import io.metersphere.plan.dto.response.TestPlanResourceSortResponse;
import io.metersphere.plan.service.TestPlanFunctionalCaseService;
import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "测试计划功能用例")
@RequestMapping("/test-plan/functional/case")
public class TestPlanFunctionalCaseController {

    @Resource
    private TestPlanManagementService testPlanManagementService;
    @Resource
    private TestPlanFunctionalCaseService testPlanFunctionalCaseService;

    @PostMapping(value = "/association")
    @Operation(summary = "测试计划功能用例-关联功能用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_ASSOCIATION)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanAssociationResponse association(@Validated @RequestBody TestPlanAssociationRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        return testPlanFunctionalCaseService.association(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/association", HttpMethodConstants.POST.name()));
    }

    @PostMapping(value = "/sort")
    @Operation(summary = "测试计划功能用例-关联功能用例")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public TestPlanResourceSortResponse sortNode(@Validated @RequestBody ResourceSortRequest request) {
        testPlanManagementService.checkModuleIsOpen(request.getTestPlanId(), TestPlanResourceConfig.CHECK_TYPE_TEST_PLAN, Collections.singletonList(TestPlanResourceConfig.CONFIG_TEST_PLAN_FUNCTIONAL_CASE));
        return testPlanFunctionalCaseService.sortNode(request, new LogInsertModule(SessionUtils.getUserId(), "/test-plan/functional/case/sort", HttpMethodConstants.POST.name()));
    }


    @PostMapping("/page")
    @Operation(summary = "测试计划-已关联功能用例分页查询")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Pager<List<TestPlanCasePageResponse>> page(@Validated @RequestBody TestPlanCaseRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, testPlanFunctionalCaseService.getFunctionalCasePage(request, false));
    }


    @GetMapping("/tree/{testPlanId}")
    @Operation(summary = "测试计划-已关联功能用例列表模块树")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#testPlanId", resourceType = "test_plan")
    public List<BaseTreeNode> getTree(@PathVariable String testPlanId) {
        return testPlanFunctionalCaseService.getTree(testPlanId);
    }

    @PostMapping("/module/count")
    @Operation(summary = "测试计划-已关联功能用例模块数量")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#request.getTestPlanId()", resourceType = "test_plan")
    public Map<String, Long> moduleCount(@Validated @RequestBody TestPlanCaseRequest request) {
        return testPlanFunctionalCaseService.moduleCount(request);
    }
}
