package com.ruoyi.common.utils;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.exception.ServiceException;
public final class HealthAccess {
 private HealthAccess(){}
 public static boolean admin(){return SecurityUtils.isAdmin(SecurityUtils.getUserId())||SecurityUtils.hasRole("healthadmin");}
 public static void require(Long user){
  if(user==null)throw new ServiceException("缺少患者编号");
  if(admin()||user.equals(SecurityUtils.getUserId()))return;
  if(SecurityUtils.hasRole("healthyperson")){
   JdbcTemplate db=SpringUtils.getBean(JdbcTemplate.class);
   Integer n=db.queryForObject("SELECT COUNT(*) FROM t_patient_profile WHERE user_id=? AND practitioner_id=?",Integer.class,user,SecurityUtils.getUserId());
   if(n!=null&&n>0)return;
  }
  throw new ServiceException("无权访问该患者");
 }
}
