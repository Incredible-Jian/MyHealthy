package com.ruoyi.myhealthy.device.mapper;
import java.util.List;
import com.ruoyi.myhealthy.device.domain.HealthDevice;
public interface HealthDeviceMapper {
 List<HealthDevice> selectList(HealthDevice query);
 HealthDevice selectById(Long id);
 int insert(HealthDevice record);
 int update(HealthDevice record);
 int delete(Long id);
}
