package com.ruoyi.myhealthy.analysis.service;
import java.util.List;
import com.ruoyi.common.utils.HealthAccess;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.ruoyi.myhealthy.analysis.domain.HealthAnalysis;
import com.ruoyi.myhealthy.analysis.mapper.HealthAnalysisMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
@Service
public class HealthAnalysisService {
 private final HealthAnalysisMapper mapper;
 public HealthAnalysisService(HealthAnalysisMapper mapper) { this.mapper=mapper; }
 private boolean admin() { return SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("healthadmin"); }
 public List<HealthAnalysis> list(HealthAnalysis query) { if(query.getUserId()==null) { if(!admin()) query.setUserId(SecurityUtils.getUserId()); } else HealthAccess.require(query.getUserId()); return mapper.selectList(query); }
 public HealthAnalysis detail(Long id) { HealthAnalysis value=mapper.selectById(id); if(value==null) throw new ServiceException("记录不存在"); HealthAccess.require(value.getUserId()); return value; }
 public int create(HealthAnalysis value) { if(value.getUserId()==null) value.setUserId(SecurityUtils.getUserId()); HealthAccess.require(value.getUserId()); value.setId(null); return mapper.insert(value); }
 public int update(HealthAnalysis value) { throw new ServiceException("采集数据及系统生成记录不可手工修改"); }
 public int delete(Long id) { if(!admin()) throw new ServiceException("仅管理员可删除记录"); detail(id); return mapper.delete(id); }
}
