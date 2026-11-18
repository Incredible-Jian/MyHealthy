package com.ruoyi.web.controller.health;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
@RestController @RequestMapping("/health/ai") public class AiController {
 private final CozeAdapter coze;private final JdbcTemplate db;
 public AiController(CozeAdapter coze,JdbcTemplate db){this.coze=coze;this.db=db;}
 @GetMapping("/status") public AjaxResult status(){Map<String,Object>r=new LinkedHashMap<>();r.put("configured",coze.configured());r.put("source",coze.configured()?"coze":"not_configured");return AjaxResult.success(r);}
 @PostMapping("/consult") public AjaxResult consult(@RequestBody Map<String,String>body){String q=body.getOrDefault("question","").trim();if(q.isEmpty()||q.length()>2000)throw new ServiceException("问题长度须为1至2000字");
  if(!coze.configured()){Map<String,Object>r=new LinkedHashMap<>();r.put("source","local_help");r.put("answer","当前未配置外部AI。本地帮助：可在首页进行模拟测量，在记录页查看教学数据，或在在线健康咨询中联系已分配的健康师。此回复是固定操作说明，不是模型生成内容。");r.put("followups",Arrays.asList("如何查看测量记录？","如何申请在线问询？","如何生成健康报告？"));return AjaxResult.success(r);}
  return AjaxResult.success(coze.ask("consultation-bot-id","课程教学场景，所有信息均非真实患者病历。当前系统支持本地四类模拟、记录趋势、档案随访、草稿审核、排队咨询与教学商城；不支持真实医疗设备采集或体检资料上传。请勿虚构功能、给出诊断或处方。结尾提供两个相关追问。用户问题："+q));
 }
 @PostMapping("/measurement/{kind}") public AjaxResult measurement(@PathVariable String kind,@RequestBody Map<String,Object>body){
  Map<String,String>keys=new HashMap<>();keys.put("pressure","blood-pressure-bot-id");keys.put("glucose","glucose-bot-id");keys.put("lipid","lipid-bot-id");keys.put("ecg","ecg-bot-id");if(!keys.containsKey(kind))throw new ServiceException("未知测量类型");
  Map<String,Object> safe=new LinkedHashMap<>();for(String key:Arrays.asList("age","heightCm","weightKg","mealState"))if(body.containsKey(key))safe.put(key,body.get(key));
  double[][] ranges={{1,120},{50,250},{10,350}};int index=0;
  for(String key:Arrays.asList("age","heightCm","weightKg")){
   try{double value=Double.parseDouble(String.valueOf(safe.get(key)));if(!Double.isFinite(value)||value<ranges[index][0]||value>ranges[index][1])throw new Exception();safe.put(key,value);}
   catch(Exception e){throw new ServiceException(key+" 参数不合法");}index++;
  }
  if(safe.containsKey("mealState")&&!Arrays.asList("fasting","postmeal").contains(String.valueOf(safe.get("mealState"))))throw new ServiceException("请选择空腹或餐后");
  return AjaxResult.success(coze.ask(keys.get(kind),"仅使用教学样本。返回相关模拟指标的JSON及样本匹配说明，不得作为诊断："+safe.toString()));
 }
 @PostMapping("/report") public AjaxResult report(){
  if(!coze.configured())throw new ServiceException("扣子尚未配置，可使用本地教学汇总报告");
  long u=SecurityUtils.getUserId();Map<String,Object>data=new LinkedHashMap<>();
  Map<String,String>tables=new LinkedHashMap<>();tables.put("pressure","t_blood_pressure");tables.put("glucose","t_blood_glucose");tables.put("lipid","t_blood_lipid");tables.put("ecg","t_ecg_record");
  tables.forEach((k,t)->{List<Map<String,Object>>rows=db.queryForList("SELECT * FROM "+t+" WHERE user_id=? ORDER BY measure_time DESC,id DESC LIMIT 1",u);if(!rows.isEmpty()){Map<String,Object>r=rows.get(0);r.remove("user_id");r.remove("device_id");r.remove("id");data.put(k,r);}});
  Map<String,Object> answer=coze.ask("report-bot-id","请对以下本地教学模拟指标生成待健康师审核的汇总报告，缺少的数据明确说明，不诊断、不建议调整用药："+data.toString());
  db.update("INSERT INTO t_health_report(user_id,title,content,source,status) VALUES(?,?,?,?,?)",u,"AI教学健康报告",answer.get("answer"),"coze","draft");return AjaxResult.success(answer);
 }
}
