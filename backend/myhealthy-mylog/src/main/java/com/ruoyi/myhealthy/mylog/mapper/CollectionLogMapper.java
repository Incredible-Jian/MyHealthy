package com.ruoyi.myhealthy.mylog.mapper;
import java.util.List;
import com.ruoyi.myhealthy.mylog.domain.CollectionLog;
public interface CollectionLogMapper {
 List<CollectionLog> selectList(CollectionLog query);
 CollectionLog selectById(Long id);
 int insert(CollectionLog record);
 int update(CollectionLog record);
 int delete(Long id);
}
