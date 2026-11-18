package com.ruoyi.web.controller.health;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.*;
@Configuration @EnableScheduling
public class HealthEvents {
 private final JdbcTemplate db;private final RabbitTemplate rabbit;
 public HealthEvents(JdbcTemplate db,RabbitTemplate rabbit){this.db=db;this.rabbit=rabbit;}
 @Bean public org.springframework.amqp.core.Queue healthQueue(){return new org.springframework.amqp.core.Queue("myhealthy.events",true);}
 @Scheduled(fixedDelay=3000) public void publish(){
  for(Map<String,Object> e:db.queryForList("SELECT * FROM t_event_outbox WHERE sent=0 LIMIT 100")){
   try{rabbit.invoke(ops->{ops.convertAndSend("","myhealthy.events",String.valueOf(e.get("id")));ops.waitForConfirmsOrDie(5000);return null;});db.update("UPDATE t_event_outbox SET sent=1 WHERE id=?",e.get("id"));}catch(Exception ex){return;}
  }
 }
 @RabbitListener(queues="myhealthy.events") public void consume(String eventId){db.update("INSERT IGNORE INTO t_event_receipt(id,received_at) VALUES(?,NOW())",eventId);}
}
