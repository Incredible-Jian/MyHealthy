package com.ruoyi.myhealthy.mylog.service;
import java.util.List;
import com.ruoyi.common.utils.HealthAccess;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.ruoyi.myhealthy.mylog.domain.CollectionLog;
import com.ruoyi.myhealthy.mylog.mapper.CollectionLogMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
@Service
public class CollectionLogService {
 private final CollectionLogMapper mapper;
 public CollectionLogService(CollectionLogMapper mapper) { this.mapper=mapper; }
 private boolean admin() { return SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("healthadmin"); }
 public List<CollectionLog> list(CollectionLog query) { if(query.getUserId()==null) { if(!admin()) query.setUserId(SecurityUtils.getUserId()); } else HealthAccess.require(query.getUserId()); return mapper.selectList(query); }
 public CollectionLog detail(Long id) { CollectionLog value=mapper.selectById(id); if(value==null) throw new ServiceException("记录不存在"); HealthAccess.require(value.getUserId()); return value; }
 public int create(CollectionLog value) { if(value.getUserId()==null) value.setUserId(SecurityUtils.getUserId()); HealthAccess.require(value.getUserId()); value.setId(null); return mapper.insert(value); }
 public int update(CollectionLog value) { throw new ServiceException("采集数据及系统生成记录不可手工修改"); }
 public int delete(Long id) { if(!admin()) throw new ServiceException("仅管理员可删除记录"); detail(id); return mapper.delete(id); }
}
