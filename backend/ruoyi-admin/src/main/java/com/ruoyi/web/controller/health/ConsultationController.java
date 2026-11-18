package com.ruoyi.web.controller.health;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
@RestController @RequestMapping("/health/chat")
public class ConsultationController {
 private final JdbcTemplate db;private final ChatSocket sockets;
 public ConsultationController(JdbcTemplate db,ChatSocket sockets){this.db=db;this.sockets=sockets;}
 private long me(){return SecurityUtils.getUserId();}private boolean doctor(){return SecurityUtils.hasRole("healthyperson");}
 private Map<String,Object> consultation(long id){List<Map<String,Object>>rs=db.queryForList("SELECT * FROM t_consultation WHERE id=?",id);if(rs.isEmpty())throw new ServiceException("咨询不存在");Map<String,Object> r=rs.get(0);if(((Number)r.get("user_id")).longValue()!=me()&&((Number)r.get("practitioner_id")).longValue()!=me())throw new ServiceException("无权访问咨询");return r;}
 @GetMapping("/ticket") public AjaxResult ticket(){return AjaxResult.success("success",sockets.ticket(me()));}
 @GetMapping("/queue") public AjaxResult queue(){
  List<Map<String,Object>> rows=doctor()?db.queryForList("SELECT c.*,u.nick_name FROM t_consultation c JOIN sys_user u ON u.user_id=c.user_id WHERE practitioner_id=? AND c.status IN ('WAITING','ACTIVE') ORDER BY (c.status='ACTIVE') DESC,c.created_at,c.id",me()):db.queryForList("SELECT c.*,u.nick_name FROM t_consultation c JOIN sys_user u ON u.user_id=c.practitioner_id WHERE c.user_id=? ORDER BY c.id DESC LIMIT 10",me());
  for(Map<String,Object> r:rows){long doc=((Number)r.get("practitioner_id")).longValue();r.put("practitionerOnline",sockets.online(doc));r.put("ahead",db.queryForObject("SELECT COUNT(*) FROM t_consultation WHERE practitioner_id=? AND status IN ('WAITING','ACTIVE') AND id<?",Integer.class,doc,r.get("id")));}return AjaxResult.success(rows);
 }
 @PostMapping("/request") @Transactional public AjaxResult request(){
  if(doctor())throw new ServiceException("健康师请在工作台接诊");
  db.queryForList("SELECT user_id FROM sys_user WHERE user_id=? FOR UPDATE",me());
  if(db.queryForObject("SELECT COUNT(*) FROM t_consultation WHERE user_id=? AND status IN ('WAITING','ACTIVE')",Integer.class,me())>0)throw new ServiceException("已有排队或进行中的咨询");
  List<Long> docs=db.queryForList("SELECT practitioner_id FROM t_patient_profile WHERE user_id=? AND practitioner_id IS NOT NULL LIMIT 1",Long.class,me());
  if(docs.isEmpty())throw new ServiceException("请先由管理员分配健康师");long d=docs.get(0);
  db.update("INSERT INTO t_consultation(user_id,practitioner_id,status,created_at) VALUES(?,?,'WAITING',NOW())",me(),d);sockets.notifyUser(d);return AjaxResult.success();
 }
 @PostMapping("/{id}/accept") @Transactional public AjaxResult accept(@PathVariable long id){
  if(!doctor())throw new ServiceException("需要健康师权限");db.queryForList("SELECT user_id FROM sys_user WHERE user_id=? FOR UPDATE",me());Map<String,Object> r=consultation(id);
  if(((Number)r.get("practitioner_id")).longValue()!=me())throw new ServiceException("非本人的接诊队列");
  if(db.queryForObject("SELECT COUNT(*) FROM t_consultation WHERE practitioner_id=? AND status='ACTIVE'",Integer.class,me())>0)throw new ServiceException("请先结束当前问诊");
  Long next=db.queryForObject("SELECT MIN(id) FROM t_consultation WHERE practitioner_id=? AND status='WAITING'",Long.class,me());if(next==null||next!=id)throw new ServiceException("请按排队顺序接诊");
  db.update("UPDATE t_consultation SET status='ACTIVE',started_at=NOW() WHERE id=? AND status='WAITING'",id);sockets.notifyUser(((Number)r.get("user_id")).longValue());return AjaxResult.success();
 }
 @PostMapping("/{id}/close") @Transactional public AjaxResult close(@PathVariable long id){Map<String,Object>r=consultation(id);db.update("UPDATE t_consultation SET status='CLOSED',closed_at=NOW() WHERE id=?",id);sockets.notifyUser(((Number)r.get("user_id")).longValue());sockets.notifyUser(((Number)r.get("practitioner_id")).longValue());return AjaxResult.success();}
 @GetMapping("/{id}/messages") public AjaxResult messages(@PathVariable long id){consultation(id);return AjaxResult.success(db.queryForList("SELECT * FROM t_chat_message WHERE consultation_id=? ORDER BY id LIMIT 1000",id));}
 @PostMapping("/{id}/messages") @Transactional public AjaxResult send(@PathVariable long id,@RequestBody Map<String,String>body){Map<String,Object>r=consultation(id);if(!"ACTIVE".equals(r.get("status")))throw new ServiceException("当前未在接诊状态");String content=body.getOrDefault("content","").trim();if(content.isEmpty()||content.length()>2000)throw new ServiceException("消息长度须为1至2000字");db.update("INSERT INTO t_chat_message(consultation_id,sender_id,content,created_at) VALUES(?,?,?,NOW())",id,me(),content);sockets.notifyUser(((Number)r.get("user_id")).longValue());sockets.notifyUser(((Number)r.get("practitioner_id")).longValue());return AjaxResult.success();}
 @GetMapping("/knowledge") public AjaxResult knowledge(){return AjaxResult.success(db.queryForList("SELECT * FROM t_health_article ORDER BY id"));}
}
