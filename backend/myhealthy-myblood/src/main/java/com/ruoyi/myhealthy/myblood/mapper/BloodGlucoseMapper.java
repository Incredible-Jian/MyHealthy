package com.ruoyi.myhealthy.myblood.mapper;
import java.util.List;
import com.ruoyi.myhealthy.myblood.domain.BloodGlucose;
public interface BloodGlucoseMapper {
 List<BloodGlucose> selectList(BloodGlucose query);
 BloodGlucose selectById(Long id);
 int insert(BloodGlucose record);
 int update(BloodGlucose record);
 int delete(Long id);
}
