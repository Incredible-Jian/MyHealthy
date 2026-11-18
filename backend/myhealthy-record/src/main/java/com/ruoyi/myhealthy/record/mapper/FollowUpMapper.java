package com.ruoyi.myhealthy.record.mapper;
import java.util.List;
import com.ruoyi.myhealthy.record.domain.FollowUp;
public interface FollowUpMapper {
 List<FollowUp> selectList(FollowUp query);
 FollowUp selectById(Long id);
 int insert(FollowUp record);
 int update(FollowUp record);
 int delete(Long id);
}
