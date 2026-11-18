package com.ruoyi.myhealthy.tuser.mapper;
import java.util.List;
import com.ruoyi.myhealthy.tuser.domain.PatientProfile;
public interface PatientProfileMapper {
 List<PatientProfile> selectList(PatientProfile query);
 PatientProfile selectById(Long id);
 int insert(PatientProfile record);
 int update(PatientProfile record);
 int delete(Long id);
}
