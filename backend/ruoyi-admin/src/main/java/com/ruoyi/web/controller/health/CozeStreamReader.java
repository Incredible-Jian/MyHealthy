package com.ruoyi.web.controller.health;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.ruoyi.common.exception.ServiceException;

/** Reads only completed public answers; deltas, reasoning and tool output stay private. */
public final class CozeStreamReader {
 private CozeStreamReader(){}
 public static Map<String,Object> read(Reader input,ObjectMapper json) throws IOException {
  BufferedReader reader=new BufferedReader(input);String event="",line;StringBuilder data=new StringBuilder();
  List<String> answers=new ArrayList<>(),followups=new ArrayList<>();boolean complete=false;
  long deadline=System.nanoTime()+45_000_000_000L;
  while((line=reader.readLine())!=null){
   if(System.nanoTime()>deadline)throw new ServiceException("扣子响应超时，请稍后重试");
   if(line.length()>1_000_000)throw new ServiceException("扣子响应内容过大");
   if(line.startsWith("event:")){event=line.substring(6).trim();continue;}
   if(line.startsWith("data:")){if(data.length()>0)data.append('\n');data.append(line.substring(5).trim());continue;}
   if(line.startsWith("{"))throw new ServiceException("扣子未返回有效事件流，请检查配置和权限");
   if(!line.isEmpty()||data.length()==0)continue;
   if("error".equals(event)||"conversation.chat.failed".equals(event)||"conversation.chat.requires_action".equals(event))throw new ServiceException("扣子对话未完成，请检查平台调试结果");
   if("conversation.chat.completed".equals(event))complete=true;
   if("conversation.message.completed".equals(event)){
    JsonNode message=json.readTree(data.toString());String content=message.path("content").asText("");
    if("answer".equals(message.path("type").asText())&&!content.trim().isEmpty())answers.add(content);
    if("follow_up".equals(message.path("type").asText())&&!content.trim().isEmpty())followups.add(content);
   }
   data.setLength(0);if("done".equals(event))break;event="";
  }
  if(!complete||answers.isEmpty())throw new ServiceException("智能体未返回完整答案，请稍后重试");
  if(followups.isEmpty()){
   String answer=String.join("\n",answers);int start=answer.indexOf("相关追问");
   if(start>=0){java.util.regex.Matcher questions=java.util.regex.Pattern.compile("(?m)^\\s*(?:\\d+[.、]|[-*])\\s*([^\\n？?]{3,}[？?])\\s*$").matcher(answer.substring(start));while(questions.find()&&followups.size()<3)followups.add(questions.group(1));}
  }
  Map<String,Object> result=new LinkedHashMap<>();result.put("source","coze");result.put("answer",String.join("\n",answers));result.put("followups",followups);return result;
 }
}
