package com.ruoyi.myhealthy.report.mapper;
import java.util.List;
import com.ruoyi.myhealthy.report.domain.HealthReport;
public interface HealthReportMapper {
 List<HealthReport> selectList(HealthReport query);
 HealthReport selectById(Long id);
 int insert(HealthReport record);
 int update(HealthReport record);
 int delete(Long id);
}
