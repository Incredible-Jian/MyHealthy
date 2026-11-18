package com.ruoyi.myhealthy.plan.service;
import java.util.List;
import com.ruoyi.common.utils.HealthAccess;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.ruoyi.myhealthy.plan.domain.HealthPlan;
import com.ruoyi.myhealthy.plan.mapper.HealthPlanMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
@Service
public class HealthPlanService {
 private final HealthPlanMapper mapper;
 public HealthPlanService(HealthPlanMapper mapper) { this.mapper=mapper; }
 private boolean admin() { return SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("healthadmin"); }
 public List<HealthPlan> list(HealthPlan query) { if(query.getUserId()==null) { if(!admin()) query.setUserId(SecurityUtils.getUserId()); } else HealthAccess.require(query.getUserId()); return mapper.selectList(query); }
 public HealthPlan detail(Long id) { HealthPlan value=mapper.selectById(id); if(value==null) throw new ServiceException("记录不存在"); HealthAccess.require(value.getUserId()); return value; }
 public int create(HealthPlan value) { if(value.getUserId()==null) value.setUserId(SecurityUtils.getUserId()); HealthAccess.require(value.getUserId()); value.setId(null); return mapper.insert(value); }
 public int update(HealthPlan value) { if(value.getId()==null) throw new ServiceException("缺少记录ID"); HealthPlan previous=detail(value.getId()); value.setUserId(previous.getUserId()); return mapper.update(value); }
 public int delete(Long id) { if(!admin()) throw new ServiceException("仅管理员可删除记录"); detail(id); return mapper.delete(id); }
}
