package io.metersphere.plan.mapper;

import io.metersphere.functional.dto.FunctionalCaseModuleCountDTO;
import io.metersphere.functional.dto.ProjectOptionDTO;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.dto.ApiCaseModuleDTO;
import io.metersphere.plan.dto.ApiScenarioModuleDTO;
import io.metersphere.plan.dto.ResourceSelectParam;
import io.metersphere.plan.dto.TestPlanCaseRunResultCount;
import io.metersphere.plan.dto.request.TestPlanApiScenarioModuleRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.plan.dto.response.TestPlanApiScenarioPageResponse;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanApiScenarioMapper {

    long updatePos(@Param("id") String id, @Param("pos") long pos);

    List<String> selectIdByTestPlanIdOrderByPos(String testPlanId);

    Long getMaxPosByRangeId(String rangeId);

    List<String> getIdByParam(ResourceSelectParam resourceSelectParam);

    DropNode selectDragInfoById(String id);

    DropNode selectNodeByPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<TestPlanCaseRunResultCount> selectCaseExecResultCount(String testPlanId);

    List<TestPlanApiScenario> selectByTestPlanIdAndNotDeleted(String testPlanId);

    List<TestPlanApiScenarioPageResponse> relateApiScenarioList(@Param("request") TestPlanApiScenarioRequest request, @Param("deleted") boolean deleted);

    List<FunctionalCaseModuleCountDTO> countModuleIdByRequest(@Param("request") TestPlanApiScenarioModuleRequest request, @Param("deleted") boolean deleted);

    long caseCount(@Param("request") TestPlanApiScenarioModuleRequest request, @Param("deleted") boolean deleted);

    List<String> selectIdByProjectIdAndTestPlanId(@Param("projectId") String projectId, @Param("testPlanId") String testPlanId);

    List<ModuleCountDTO> collectionCountByRequest(@Param("testPlanId") String testPlanId);

    List<ProjectOptionDTO> selectRootIdByTestPlanId(@Param("testPlanId") String testPlanId);

    List<ApiScenarioModuleDTO> selectBaseByProjectIdAndTestPlanId(@Param("testPlanId") String testPlanId);
}
