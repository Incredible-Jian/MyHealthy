package com.ruoyi.web.controller.health;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;

/** Teaching simulators: output is explicitly synthetic, never a medical diagnosis. */
@RestController @RequestMapping("/health/workflow")
public class HealthWorkflowController {
 private final JdbcTemplate db;
 private static final Map<String,String> TABLES=new LinkedHashMap<>();
 static { TABLES.put("pressure","t_blood_pressure"); TABLES.put("glucose","t_blood_glucose"); TABLES.put("lipid","t_blood_lipid"); TABLES.put("ecg","t_ecg_record"); }
 public HealthWorkflowController(JdbcTemplate db) { this.db=db; }
 private long me(){return SecurityUtils.getUserId();}
 private boolean staff(){return SecurityUtils.isAdmin(me()) || SecurityUtils.hasRole("healthadmin") || SecurityUtils.hasRole("healthyperson");}
 private void access(long user){
  if(user==me() || SecurityUtils.isAdmin(me()) || SecurityUtils.hasRole("healthadmin")) return;
  if(SecurityUtils.hasRole("healthyperson") && db.queryForObject("SELECT COUNT(*) FROM t_patient_profile WHERE user_id=? AND practitioner_id=?",Integer.class,user,me())>0) return;
  throw new ServiceException("无权访问该患者");
 }
 private double number(Map<String,Object> body,String key,double min,double max){
  try { double n=Double.parseDouble(String.valueOf(body.get(key))); if(!Double.isFinite(n)||n<min||n>max) throw new Exception(); return n; }
  catch(Exception e){throw new ServiceException(key+" 参数不合法");}
 }
 @GetMapping("/dashboard") public AjaxResult dashboard(@RequestParam(required=false) Long userId){
  long u=userId==null?me():userId; access(u); Map<String,Object> result=new LinkedHashMap<>();
  TABLES.forEach((kind,table)->{ List<Map<String,Object>> list=db.queryForList("SELECT * FROM "+table+" WHERE user_id=? ORDER BY measure_time DESC,id DESC LIMIT 1",u); result.put(kind,list.isEmpty()?null:list.get(0)); });
  result.put("alerts",db.queryForList("SELECT * FROM t_health_alert WHERE user_id=? ORDER BY id DESC LIMIT 10",u));
  result.put("profile",db.queryForList("SELECT * FROM t_patient_profile WHERE user_id=?",u));
  return AjaxResult.success(result);
 }
 @GetMapping("/trends") public AjaxResult trends(@RequestParam String kind,@RequestParam(required=false) Long userId){
  if(!TABLES.containsKey(kind)) throw new ServiceException("未知测量类型"); long u=userId==null?me():userId; access(u);
  return AjaxResult.success(db.queryForList("SELECT * FROM "+TABLES.get(kind)+" WHERE user_id=? AND measure_time >= DATE_SUB(NOW(),INTERVAL 10 DAY) ORDER BY measure_time,id",u));
 }
 @PostMapping("/simulate/{kind}") @Transactional public AjaxResult simulate(@PathVariable String kind,@RequestBody Map<String,Object> body){
  if(!TABLES.containsKey(kind)) throw new ServiceException("未知测量类型");
  double age=number(body,"age",1,120), height=number(body,"heightCm",50,250), weight=number(body,"weightKg",10,350);
  double bmi=weight/Math.pow(height/100,2); long u=me(); Date now=new Date();
  List<Map<String,Object>> devices=db.queryForList("SELECT id FROM t_health_device WHERE user_id=? AND device_type=? ORDER BY id LIMIT 1",u,kind);
  if(devices.isEmpty()) db.update("INSERT INTO t_health_device(user_id,device_name,device_type,serial_number,status) VALUES (?,?,?,?,?)",u,"教学模拟设备",kind,"SIM-"+u+"-"+kind,"active");
  Long device=db.queryForObject("SELECT id FROM t_health_device WHERE user_id=? AND device_type=? ORDER BY id LIMIT 1",Long.class,u,kind);
  Map<String,Object> result=new LinkedHashMap<>(); result.put("source","local_simulation"); result.put("bmi",Math.round(bmi*10)/10.0);
  result.put("message","教学模拟数据，不用于诊断"); boolean abnormal=false;
  if(kind.equals("pressure")){
   int systolic=(int)Math.round(108+age*.25+Math.max(0,bmi-23)*1.2),diastolic=70+(int)(Math.max(0,bmi-23)*.7),hr=72;
   db.update("INSERT INTO t_blood_pressure(user_id,device_id,measure_time,systolic,diastolic,heart_rate) VALUES(?,?,?,?,?,?)",u,device,now,systolic,diastolic,hr);
   result.put("systolic",systolic);result.put("diastolic",diastolic);result.put("heartRate",hr); abnormal=systolic>=140||diastolic>=90;
  }else if(kind.equals("glucose")){
   double g=Math.round((4.7+Math.max(0,bmi-23)*.12)*10)/10.0; String meal=String.valueOf(body.getOrDefault("mealState","fasting"));
   if(!meal.equals("fasting")&&!meal.equals("postmeal")) throw new ServiceException("请选择空腹或餐后"); if(meal.equals("postmeal")) g+=1.5;
   db.update("INSERT INTO t_blood_glucose(user_id,device_id,measure_time,glucose,meal_state) VALUES(?,?,?,?,?)",u,device,now,g,meal);
   result.put("glucose",g);result.put("mealState",meal);abnormal=g>7;
  }else if(kind.equals("lipid")){
   double tc=Math.round((4.2+Math.max(0,bmi-23)*.12)*100)/100.0,tg=1.2,hdl=1.3,ldl=Math.round((tc-1.8)*100)/100.0;
   db.update("INSERT INTO t_blood_lipid(user_id,device_id,measure_time,total_cholesterol,triglycerides,hdl,ldl) VALUES(?,?,?,?,?,?,?)",u,device,now,tc,tg,hdl,ldl);
   result.put("totalCholesterol",tc);result.put("triglycerides",tg);result.put("hdl",hdl);result.put("ldl",ldl);abnormal=tc>5.2;
  }else{
   int duration=(int)number(body,"durationSeconds",60,60); String waveform="[0,0.1,0,-0.15,1,-0.3,0,0.2,0]";
   db.update("INSERT INTO t_ecg_record(user_id,device_id,measure_time,heart_rate,duration_seconds,waveform) VALUES(?,?,?,?,?,?)",u,device,now,72,duration,waveform);
   result.put("heartRate",72);result.put("durationSeconds",duration);result.put("waveform",Arrays.asList(0,.1,0,-.15,1,-.3,0,.2,0));
  }
  if(abnormal) db.update("INSERT INTO t_health_alert(user_id,measurement_type,severity,message,status) VALUES(?,?,?,?,?)",u,kind,"demo","教学规则触发的模拟预警，请勿作为诊断","new");
  db.update("INSERT INTO t_collection_log(user_id,device_id,collected_at,status,message) VALUES(?,?,?,?,?)",u,device,now,"success","local_simulation: "+kind);
  return AjaxResult.success(result);
 }
 @PostMapping("/sports") public AjaxResult sports(@RequestBody Map<String,Object> body){
  int steps=(int)number(body,"steps",0,86400),duration=(int)number(body,"durationSeconds",0,86400);
  if(steps>duration+1) throw new ServiceException("步数超过一秒一步的模拟范围");
  db.update("INSERT INTO t_sports_record(user_id,measure_time,steps,duration_seconds) VALUES(?,NOW(),?,?)",me(),steps,duration);return AjaxResult.success();
 }
 @GetMapping("/patients") public AjaxResult patients(){
  if(!staff()) throw new ServiceException("需要健康师权限");
  return AjaxResult.success(SecurityUtils.isAdmin(me())||SecurityUtils.hasRole("healthadmin")?db.queryForList("SELECT * FROM t_patient_profile"):db.queryForList("SELECT * FROM t_patient_profile WHERE practitioner_id=?",me()));
 }
 @PostMapping("/report") @Transactional public AjaxResult report(){
  long u=me(); StringBuilder text=new StringBuilder("本报告由本地教学规则汇总，未调用大模型，不作为医疗建议。\n");
  TABLES.forEach((kind,table)->{List<Map<String,Object>> rows=db.queryForList("SELECT * FROM "+table+" WHERE user_id=? ORDER BY measure_time DESC,id DESC LIMIT 1",u);text.append(kind).append(": ").append(rows.isEmpty()?"暂无测量":rows.get(0).toString()).append("\n");});
  db.update("INSERT INTO t_health_report(user_id,title,content,source,status) VALUES(?,?,?,?,?)",u,"健康指标汇总",text.toString(),"local_rules","draft");return AjaxResult.success();
 }
 @GetMapping("/reports") public AjaxResult reports(@RequestParam(required=false) Long userId){
  long u=userId==null?me():userId; access(u);return AjaxResult.success(db.queryForList("SELECT * FROM t_health_report WHERE user_id=? ORDER BY id DESC",u));
 }
 @PostMapping("/report/{id}/review") @Transactional public AjaxResult review(@PathVariable Long id,@RequestBody Map<String,Object> body){
  if(!staff()) throw new ServiceException("需要健康师权限");
  List<Map<String,Object>> rows=db.queryForList("SELECT * FROM t_health_report WHERE id=? FOR UPDATE",id);if(rows.isEmpty()) throw new ServiceException("报告不存在");
  long u=((Number)rows.get(0).get("user_id")).longValue(); access(u);String content=String.valueOf(body.getOrDefault("content",rows.get(0).get("content")));if(content.trim().isEmpty()||content.length()>20000) throw new ServiceException("报告内容长度不合法");
  db.update("UPDATE t_health_report SET content=?,status='approved',reviewer_id=?,reviewed_at=NOW() WHERE id=?",content,me(),id);return AjaxResult.success();
 }
}
