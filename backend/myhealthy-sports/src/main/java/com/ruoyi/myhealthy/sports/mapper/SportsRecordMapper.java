package com.ruoyi.myhealthy.sports.mapper;
import java.util.List;
import com.ruoyi.myhealthy.sports.domain.SportsRecord;
public interface SportsRecordMapper {
 List<SportsRecord> selectList(SportsRecord query);
 SportsRecord selectById(Long id);
 int insert(SportsRecord record);
 int update(SportsRecord record);
 int delete(Long id);
}
