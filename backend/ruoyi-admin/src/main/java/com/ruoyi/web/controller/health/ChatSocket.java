package com.ruoyi.web.controller.health;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.*;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration @EnableWebSocket
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {
 private static class Ticket{long user,expiry;Ticket(long u){user=u;expiry=System.currentTimeMillis()+30000;}}
 private final Map<String,Ticket> tickets=new ConcurrentHashMap<>();
 private final Map<Long,Set<WebSocketSession>> sessions=new ConcurrentHashMap<>();
 public String ticket(long u){tickets.entrySet().removeIf(e->e.getValue().expiry<System.currentTimeMillis());String t=UUID.randomUUID().toString();tickets.put(t,new Ticket(u));return t;}
 public boolean online(long u){Set<WebSocketSession> s=sessions.get(u);return s!=null&&s.stream().anyMatch(WebSocketSession::isOpen);}
 public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){registry.addHandler(this,"/health/chat/socket").addInterceptors(new HandshakeInterceptor(){
  public boolean beforeHandshake(ServerHttpRequest request,ServerHttpResponse response,WebSocketHandler handler,Map<String,Object> attrs){String key=UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("ticket");Ticket t=key==null?null:tickets.remove(key);if(t==null||t.expiry<System.currentTimeMillis())return false;attrs.put("user",t.user);return true;}
  public void afterHandshake(ServerHttpRequest req,ServerHttpResponse res,WebSocketHandler handler,Exception e){}
 }).setAllowedOrigins("http://localhost:18082","http://127.0.0.1:18082");}
 public void afterConnectionEstablished(WebSocketSession session){long u=(Long)session.getAttributes().get("user");sessions.computeIfAbsent(u,k->ConcurrentHashMap.newKeySet()).add(session);}
 public void afterConnectionClosed(WebSocketSession session,CloseStatus status){long u=(Long)session.getAttributes().get("user");Set<WebSocketSession>s=sessions.get(u);if(s!=null){s.remove(session);if(s.isEmpty())sessions.remove(u);}}
 protected void handleTextMessage(WebSocketSession session,TextMessage message)throws Exception{session.sendMessage(new TextMessage("{\"type\":\"heartbeat\"}"));}
 public void notifyUser(long u){Set<WebSocketSession>s=sessions.get(u);if(s!=null)for(WebSocketSession ws:s)try{synchronized(ws){if(ws.isOpen())ws.sendMessage(new TextMessage("{\"type\":\"refresh\"}"));}}catch(Exception ignored){}}
}
