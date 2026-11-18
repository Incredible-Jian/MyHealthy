package com.ruoyi.myhealthy.plan.mapper;
import java.util.List;
import com.ruoyi.myhealthy.plan.domain.HealthPlan;
public interface HealthPlanMapper {
 List<HealthPlan> selectList(HealthPlan query);
 HealthPlan selectById(Long id);
 int insert(HealthPlan record);
 int update(HealthPlan record);
 int delete(Long id);
}
