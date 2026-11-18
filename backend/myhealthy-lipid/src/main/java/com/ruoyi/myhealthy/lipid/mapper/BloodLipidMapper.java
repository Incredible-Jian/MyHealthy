package com.ruoyi.myhealthy.lipid.mapper;
import java.util.List;
import com.ruoyi.myhealthy.lipid.domain.BloodLipid;
public interface BloodLipidMapper {
 List<BloodLipid> selectList(BloodLipid query);
 BloodLipid selectById(Long id);
 int insert(BloodLipid record);
 int update(BloodLipid record);
 int delete(Long id);
}
