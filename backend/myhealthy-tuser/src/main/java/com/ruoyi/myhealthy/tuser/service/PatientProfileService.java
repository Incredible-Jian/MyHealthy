package com.ruoyi.myhealthy.tuser.service;
import java.util.List;
import com.ruoyi.common.utils.HealthAccess;
import java.util.Objects;
import org.springframework.stereotype.Service;
import com.ruoyi.myhealthy.tuser.domain.PatientProfile;
import com.ruoyi.myhealthy.tuser.mapper.PatientProfileMapper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
@Service
public class PatientProfileService {
 private final PatientProfileMapper mapper;
 public PatientProfileService(PatientProfileMapper mapper) { this.mapper=mapper; }
 private boolean admin() { return SecurityUtils.isAdmin(SecurityUtils.getUserId()) || SecurityUtils.hasRole("healthadmin"); }
 public List<PatientProfile> list(PatientProfile query) { if(query.getUserId()==null) { if(!admin()) query.setUserId(SecurityUtils.getUserId()); } else HealthAccess.require(query.getUserId()); return mapper.selectList(query); }
 public PatientProfile detail(Long id) { PatientProfile value=mapper.selectById(id); if(value==null) throw new ServiceException("记录不存在"); HealthAccess.require(value.getUserId()); return value; }
 public int create(PatientProfile value) { if(value.getUserId()==null) value.setUserId(SecurityUtils.getUserId()); HealthAccess.require(value.getUserId()); if(!admin()) { value.setPractitionerId(null); PatientProfile q=new PatientProfile(); q.setUserId(value.getUserId()); if(!mapper.selectList(q).isEmpty()) throw new ServiceException("个人档案已存在，请编辑原档案"); } value.setId(null); return mapper.insert(value); }
 public int update(PatientProfile value) { if(value.getId()==null) throw new ServiceException("缺少记录ID"); PatientProfile previous=detail(value.getId()); value.setUserId(previous.getUserId()); if(!admin()) value.setPractitionerId(previous.getPractitionerId()); return mapper.update(value); }
 public int delete(Long id) { if(!admin()) throw new ServiceException("仅管理员可删除记录"); detail(id); return mapper.delete(id); }
}
