package com.ruoyi.web.controller.health;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.StringReader;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
class CozeStreamReaderTest {
 private final ObjectMapper json=new ObjectMapper();
 private Map<String,Object> parse(String s)throws Exception{return CozeStreamReader.read(new StringReader(s),json);}
 @Test void returnsOnlyCompletedPublicAnswer()throws Exception{
  String input="event: conversation.message.delta\ndata: {\"type\":\"answer\",\"content\":\"partial\"}\n\nevent: conversation.message.completed\ndata: {\"type\":\"tool_response\",\"content\":\"private tool output\"}\n\nevent: conversation.message.completed\ndata: {\"type\":\"answer\",\"content\":\"教学回答\"}\n\nevent: conversation.message.completed\ndata: {\"type\":\"follow_up\",\"content\":\"如何查看记录？\"}\n\nevent: conversation.chat.completed\ndata: {}\n\nevent: done\ndata: [DONE]\n\n";
  Map<String,Object> result=parse(input);assertEquals("教学回答",result.get("answer"));assertEquals("coze",result.get("source"));assertEquals(Collections.singletonList("如何查看记录？"),result.get("followups"));
 }
 @Test void incompleteOrFailedStreamsDoNotBecomeSuccess(){
  assertThrows(ServiceException.class,()->parse("event: conversation.message.completed\ndata: {\"type\":\"answer\",\"content\":\"partial\"}\n\n"));
  assertThrows(ServiceException.class,()->parse("event: conversation.chat.failed\ndata: {}\n\n"));
 }
 @Test void rejectsJsonErrorAndMissingAnswer(){
  assertThrows(ServiceException.class,()->parse("{\"code\":4100}\n"));
  assertThrows(ServiceException.class,()->parse("event: conversation.chat.completed\ndata: {}\n\n"));
 }
 @Test void keepsModelAuthoredFollowupsWhenInline()throws Exception{
  String message=json.writeValueAsString(new HashMap<String,String>(){{put("type","answer");put("content","这是教学回答。\n相关追问：\n1. 如何查看测量记录？\n2. 如何联系健康师？");}});
  Map<String,Object> result=parse("event: conversation.message.completed\ndata: "+message+"\n\nevent: conversation.chat.completed\ndata: {}\n\n");
  assertEquals(Arrays.asList("如何查看测量记录？","如何联系健康师？"),result.get("followups"));
 }
}
