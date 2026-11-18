package com.ruoyi.myhealthy.secgoods.service;
import org.springframework.stereotype.Service;
import com.ruoyi.myhealthy.secgoods.payment.AlipaySandboxService;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;

/** Redis admission and durable SQL reservation. SQL is the final stock authority. */
@Service
public class SeckillService {
 private final JdbcTemplate db;private final StringRedisTemplate redis;private final TransactionTemplate tx;private final AlipaySandboxService alipay;
 @Value("${health.demo-pay.enabled:false}") private boolean demoPay;
 private static final DefaultRedisScript<Long> TAKE=new DefaultRedisScript<>("if redis.call('exists',KEYS[2])==1 then return -2 end; local n=tonumber(redis.call('get',KEYS[1]) or '0'); if n<=0 then return -1 end; redis.call('decr',KEYS[1]);redis.call('set',KEYS[2],ARGV[1]);return 1",Long.class);
 private static final DefaultRedisScript<Long> GIVE=new DefaultRedisScript<>("if redis.call('get',KEYS[2])~=ARGV[1] then return 0 end;redis.call('del',KEYS[2]);redis.call('incr',KEYS[1]);return 1",Long.class);
 public SeckillService(JdbcTemplate db,StringRedisTemplate redis,TransactionTemplate tx,AlipaySandboxService alipay){this.db=db;this.redis=redis;this.tx=tx;this.alipay=alipay;}
 private String stock(long g){return "health:stock:"+g;}private String owner(long g,long u){return "health:reservation:"+g+":"+u;}
 private void event(String type,String id){db.update("INSERT INTO t_event_outbox(id,event_type,payload) VALUES(?,?,?)",UUID.randomUUID().toString(),type,id);}
 public AjaxResult goods(){return AjaxResult.success(db.queryForList("SELECT * FROM t_seckill_goods ORDER BY id"));}
 public AjaxResult orders(){return AjaxResult.success(db.queryForList("SELECT o.*,g.title FROM t_seckill_order o JOIN t_seckill_goods g ON g.id=o.goods_id WHERE user_id=? ORDER BY created_at DESC",SecurityUtils.getUserId()));}
 public AjaxResult reserve(long goodsId){
  long u=SecurityUtils.getUserId();String id=UUID.randomUUID().toString();
  return tx.execute(t->{
   List<Map<String,Object>> gs=db.queryForList("SELECT * FROM t_seckill_goods WHERE id=? AND start_time<=NOW() AND end_time>NOW() FOR UPDATE",goodsId);
   if(gs.isEmpty()) throw new ServiceException("商品不存在或不在活动时间");Map<String,Object> g=gs.get(0);
   Integer existing=db.queryForObject("SELECT COUNT(*) FROM t_seckill_order WHERE user_id=? AND goods_id=? AND status IN ('RESERVED','PAID')",Integer.class,u,goodsId);
   if(existing>0) throw new ServiceException("该商品已经抢购，请查看订单");
   Integer cancels=db.queryForObject("SELECT COUNT(*) FROM t_seckill_order WHERE user_id=? AND goods_id=? AND status='CANCELLED'",Integer.class,u,goodsId);
   if(cancels>=3) throw new ServiceException("该商品已退单三次，不可再次抢购");
   int n=((Number)g.get("stock")).intValue();if(n<=0) throw new ServiceException("库存不足");
   redis.opsForValue().setIfAbsent(stock(goodsId),String.valueOf(n));
   Long taken=redis.execute(TAKE,Arrays.asList(stock(goodsId),owner(goodsId,u)),id);
   if(!Long.valueOf(1).equals(taken)) throw new ServiceException("库存不足或重复请求");
   TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){public void afterCompletion(int status){if(status!=STATUS_COMMITTED) redis.execute(GIVE,Arrays.asList(stock(goodsId),owner(goodsId,u)),id);}});
   if(db.update("UPDATE t_seckill_goods SET stock=stock-1 WHERE id=? AND stock>0",goodsId)!=1) throw new ServiceException("库存不足");
   String name=SecurityUtils.getLoginUser().getUser().getNickName(),phone=SecurityUtils.getLoginUser().getUser().getPhonenumber();
   db.update("INSERT INTO t_seckill_order(id,user_id,goods_id,amount,status,created_at,expires_at,user_name,user_phone) VALUES(?,?,?,?,'RESERVED',NOW(),DATE_ADD(NOW(),INTERVAL 30 MINUTE),?,?)",id,u,goodsId,g.get("price"),name,phone);
   event("RESERVED",id);return AjaxResult.success("预留成功",id);
  });
 }
 private Map<String,Object> locked(String id,boolean checkOwner){
  List<Map<String,Object>> os=db.queryForList("SELECT * FROM t_seckill_order WHERE id=? FOR UPDATE",id);if(os.isEmpty())throw new ServiceException("订单不存在");Map<String,Object> o=os.get(0);
  if(checkOwner&&((Number)o.get("user_id")).longValue()!=SecurityUtils.getUserId())throw new ServiceException("无权访问订单");return o;
 }
 private boolean expired(Map<String,Object> o){return Boolean.TRUE.equals(db.queryForObject("SELECT expires_at<=NOW() FROM t_seckill_order WHERE id=?",Boolean.class,o.get("id")));}
 private void release(Map<String,Object> o,String state){
  String id=(String)o.get("id");long g=((Number)o.get("goods_id")).longValue(),u=((Number)o.get("user_id")).longValue();
  if(!"RESERVED".equals(o.get("status")))return;
  db.update("UPDATE t_seckill_order SET status=? WHERE id=? AND status='RESERVED'",state,id);
  db.update("UPDATE t_seckill_goods SET stock=stock+1 WHERE id=?",g);event(state,id);
  TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){public void afterCommit(){redis.execute(GIVE,Arrays.asList(stock(g),owner(g,u)),id);}});
 }
 public AjaxResult cancel(String id){return tx.execute(t->{Map<String,Object> o=locked(id,true);if("PAID".equals(o.get("status")))throw new ServiceException("已付款订单不可退单");release(o,"CANCELLED");return AjaxResult.success();});}
 public AjaxResult checkout(String id){return tx.execute(t->{Map<String,Object> order=locked(id,true);if(expired(order))throw new ServiceException("订单已超时");Map<String,Object> result=new LinkedHashMap<>();result.put("checkoutUrl",alipay.checkoutUrl(order));result.put("provider","ALIPAY_SANDBOX");return AjaxResult.success(result);});}
 public AjaxResult paymentConfiguration(){Map<String,Object> result=new LinkedHashMap<>();result.put("sandboxConfigured",alipay.configured());result.put("demoEnabled",demoPay);return AjaxResult.success(result);}
 public AjaxResult paymentStatus(String id){List<Map<String,Object>> result=db.queryForList("SELECT o.id,o.status,o.amount,o.expires_at,o.paid_at,p.provider,p.trade_no FROM t_seckill_order o LEFT JOIN t_seckill_payment p ON p.order_id=o.id WHERE o.id=? AND o.user_id=?",id,SecurityUtils.getUserId());if(result.isEmpty())throw new ServiceException("订单不存在或无权访问");return AjaxResult.success(result.get(0));}
 public AjaxResult demo(String id){
  if(!demoPay)throw new ServiceException("本地演示结算未启用");
  return tx.execute(t->{Map<String,Object> o=locked(id,true);if("PAID".equals(o.get("status")))return AjaxResult.success("已完成本地演示结算");if(!"RESERVED".equals(o.get("status")))throw new ServiceException("订单状态不可结算");
   if(expired(o))throw new ServiceException("订单已超时");
   db.update("UPDATE t_seckill_order SET status='PAID',paid_at=NOW() WHERE id=?",id);
   db.update("INSERT INTO t_seckill_payment(order_id,trade_no,amount,provider,paid_at) VALUES(?,?,?,'LOCAL_DEMO',NOW())",id,"DEMO-"+id,o.get("amount"));event("PAID_LOCAL_DEMO",id);return AjaxResult.success("本地演示结算完成，未发生真实付款");});
 }
 @Scheduled(fixedDelay=15000) public void expire(){
  List<String> ids=db.queryForList("SELECT id FROM t_seckill_order WHERE status='RESERVED' AND expires_at<=NOW() LIMIT 100",String.class);
  for(String id:ids)tx.execute(t->{Map<String,Object> o=locked(id,false);if(expired(o))release(o,"EXPIRED");return null;});
 }
}
