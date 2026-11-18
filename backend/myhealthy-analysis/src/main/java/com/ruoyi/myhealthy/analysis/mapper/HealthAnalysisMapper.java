package com.ruoyi.myhealthy.analysis.mapper;
import java.util.List;
import com.ruoyi.myhealthy.analysis.domain.HealthAnalysis;
public interface HealthAnalysisMapper {
 List<HealthAnalysis> selectList(HealthAnalysis query);
 HealthAnalysis selectById(Long id);
 int insert(HealthAnalysis record);
 int update(HealthAnalysis record);
 int delete(Long id);
}
