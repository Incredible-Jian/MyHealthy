package com.ruoyi.web.controller.health;
import java.util.*;
import java.time.Duration;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.*;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/** Coze v3 SSE by default; optional legacy polling. Never logs tokens. */
@Service public class CozeAdapter {
 private final Environment env;private final RestTemplate http;private final ObjectMapper json=new ObjectMapper();
 public CozeAdapter(Environment env,RestTemplateBuilder builder){this.env=env;http=builder.setConnectTimeout(Duration.ofSeconds(5)).setReadTimeout(Duration.ofSeconds(15)).build();}
 public boolean configured(){return env.getProperty("health.coze.enabled",Boolean.class,false)&&!env.getProperty("health.coze.token","").trim().isEmpty();}
 private JsonNode send(String path,HttpMethod method,Object body)throws Exception{
  HttpHeaders h=new HttpHeaders();h.setBearerAuth(env.getProperty("health.coze.token"));h.setContentType(MediaType.APPLICATION_JSON);
  ResponseEntity<String> response=http.exchange("https://api.coze.cn"+path,method,new HttpEntity<>(body,h),String.class);
  JsonNode result=json.readTree(response.getBody());if(result.path("code").asInt(-1)!=0)throw new ServiceException("扣子请求未成功，请检查本地配置与权限");return result.path("data");
 }
 public synchronized Map<String,Object> ask(String botKey,String text){
  if(!configured())throw new ServiceException("扣子尚未配置");String bot=env.getProperty("health.coze."+botKey,"");if(!bot.matches("[0-9]+"))throw new ServiceException("该智能体ID尚未配置");
  try{
   boolean stream=!"polling".equals(env.getProperty("health.coze.mode","stream"));
   Map<String,Object> body=new LinkedHashMap<>();body.put("bot_id",bot);body.put("user_id","course-"+SecurityUtils.getUserId());body.put("stream",stream);body.put("auto_save_history",true);
   Map<String,Object> message=new LinkedHashMap<>();message.put("role","user");message.put("content",text);message.put("content_type","text");body.put("additional_messages",Collections.singletonList(message));
   if(stream)return http.execute("https://api.coze.cn/v3/chat",HttpMethod.POST,request->{
    request.getHeaders().setBearerAuth(env.getProperty("health.coze.token"));request.getHeaders().setContentType(MediaType.APPLICATION_JSON);request.getHeaders().setAccept(Collections.singletonList(MediaType.TEXT_EVENT_STREAM));json.writeValue(request.getBody(),body);
   },response->CozeStreamReader.read(new InputStreamReader(response.getBody(),StandardCharsets.UTF_8),json));
   JsonNode chat=send("/v3/chat",HttpMethod.POST,body);String cid=chat.path("conversation_id").asText(),id=chat.path("id").asText();
   if(!cid.matches("[0-9]+")||!id.matches("[0-9]+"))throw new ServiceException("扣子返回的会话标识不合法");
   long deadline=System.currentTimeMillis()+30000;
   while(!chat.path("status").asText().equals("completed")){
    if(System.currentTimeMillis()>deadline)throw new ServiceException("扣子响应超时，请稍后重试");
    if(Arrays.asList("failed","cancelled","requires_action").contains(chat.path("status").asText()))throw new ServiceException("智能体未完成，请检查平台调试结果");
    Thread.sleep(1000);chat=send("/v3/chat/retrieve?conversation_id="+cid+"&chat_id="+id,HttpMethod.GET,null);
   }
   JsonNode messages=send("/v3/chat/message/list?conversation_id="+cid+"&chat_id="+id,HttpMethod.GET,null);List<String> answers=new ArrayList<>(),followups=new ArrayList<>();
   for(JsonNode m:messages){if(m.path("type").asText().equals("answer"))answers.add(m.path("content").asText());if(m.path("type").asText().equals("follow_up"))followups.add(m.path("content").asText());}
   if(answers.isEmpty())throw new ServiceException("智能体未返回可展示的答案");Map<String,Object> out=new LinkedHashMap<>();out.put("source","coze");out.put("answer",String.join("\n",answers));out.put("followups",followups);return out;
  }catch(ServiceException e){throw e;}catch(InterruptedException e){Thread.currentThread().interrupt();throw new ServiceException("请求已中断");}catch(Exception e){throw new ServiceException("扣子调用失败，请检查网络与本地配置");}
 }
}
