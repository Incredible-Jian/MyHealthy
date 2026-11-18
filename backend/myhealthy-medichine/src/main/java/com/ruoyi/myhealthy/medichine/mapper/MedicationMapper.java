package com.ruoyi.myhealthy.medichine.mapper;
import java.util.List;
import com.ruoyi.myhealthy.medichine.domain.Medication;
public interface MedicationMapper {
 List<Medication> selectList(Medication query);
 Medication selectById(Long id);
 int insert(Medication record);
 int update(Medication record);
 int delete(Long id);
}
