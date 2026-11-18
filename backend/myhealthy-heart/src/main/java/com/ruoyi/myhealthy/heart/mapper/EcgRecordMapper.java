package com.ruoyi.myhealthy.heart.mapper;
import java.util.List;
import com.ruoyi.myhealthy.heart.domain.EcgRecord;
public interface EcgRecordMapper {
 List<EcgRecord> selectList(EcgRecord query);
 EcgRecord selectById(Long id);
 int insert(EcgRecord record);
 int update(EcgRecord record);
 int delete(Long id);
}
