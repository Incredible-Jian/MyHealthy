package com.ruoyi.myhealthy.up.mapper;
import java.util.List;
import com.ruoyi.myhealthy.up.domain.HealthAlert;
public interface HealthAlertMapper {
 List<HealthAlert> selectList(HealthAlert query);
 HealthAlert selectById(Long id);
 int insert(HealthAlert record);
 int update(HealthAlert record);
 int delete(Long id);
}
