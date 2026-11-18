package com.ruoyi.myhealthy.device.service;
import java.util.List;
import com.ruoyi.common.utils.HealthAccess;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.ruoyi.myhealthy.device.domain.HealthDevice;
import com.ruoyi.myhealthy.device.mapper.HealthDeviceMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
@Service
public class HealthDeviceService {
 private final HealthDeviceMapper mapper;
 public HealthDeviceService(HealthDeviceMapper mapper) { this.mapper=mapper; }
 private boolean admin() { return SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("healthadmin"); }
 public List<HealthDevice> list(HealthDevice query) { if(query.getUserId()==null) { if(!admin()) query.setUserId(SecurityUtils.getUserId()); } else HealthAccess.require(query.getUserId()); return mapper.selectList(query); }
 public HealthDevice detail(Long id) { HealthDevice value=mapper.selectById(id); if(value==null) throw new ServiceException("记录不存在"); HealthAccess.require(value.getUserId()); return value; }
 public int create(HealthDevice value) { if(value.getUserId()==null) value.setUserId(SecurityUtils.getUserId()); HealthAccess.require(value.getUserId()); value.setId(null); return mapper.insert(value); }
 public int update(HealthDevice value) { if(value.getId()==null) throw new ServiceException("缺少记录ID"); HealthDevice previous=detail(value.getId()); value.setUserId(previous.getUserId()); return mapper.update(value); }
 public int delete(Long id) { if(!admin()) throw new ServiceException("仅管理员可删除记录"); detail(id); return mapper.delete(id); }
}
