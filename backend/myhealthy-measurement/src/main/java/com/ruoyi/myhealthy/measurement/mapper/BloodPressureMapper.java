package com.ruoyi.myhealthy.measurement.mapper;
import java.util.List;
import com.ruoyi.myhealthy.measurement.domain.BloodPressure;
public interface BloodPressureMapper {
 List<BloodPressure> selectList(BloodPressure query);
 BloodPressure selectById(Long id);
 int insert(BloodPressure record);
 int update(BloodPressure record);
 int delete(Long id);
}
