package com.ruoyi.myhealthy.secgoods.payment;
import java.util.*;
import java.math.BigDecimal;
import java.net.URI;
import com.alipay.api.*;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.internal.util.AlipaySignature;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.common.exception.ServiceException;

/** Public-key RSA2 sandbox only. GET pageExecute signs a redirect URL locally. */
@Service public class AlipaySandboxService {
 private final AlipaySandboxProperties config;private final JdbcTemplate db;private final TransactionTemplate tx;
 public AlipaySandboxService(AlipaySandboxProperties config,JdbcTemplate db,TransactionTemplate tx){this.config=config;this.db=db;this.tx=tx;}
 private boolean text(String s){return s!=null&&!s.trim().isEmpty();}
 private boolean sandboxGateway(String value){
  try{URI uri=URI.create(value);return "https".equals(uri.getScheme())&&uri.getUserInfo()==null&&(uri.getPort()==-1||uri.getPort()==443)&&"/gateway.do".equals(uri.getPath())&&uri.getQuery()==null&&uri.getFragment()==null&&Arrays.asList("openapi-sandbox.dl.alipaydev.com","openapi.alipaydev.com").contains(uri.getHost());}catch(Exception e){return false;}
 }
 private boolean callbackUrl(String value){try{URI uri=URI.create(value);return Arrays.asList("http","https").contains(uri.getScheme())&&text(uri.getHost())&&uri.getUserInfo()==null&&uri.getFragment()==null;}catch(Exception e){return false;}}
 public boolean configured(){return config.isEnabled()&&sandboxGateway(config.getGateway())&&text(config.getAppId())&&text(config.getSellerId())&&text(config.getPrivateKey())&&text(config.getPublicKey())&&callbackUrl(config.getNotifyUrl())&&callbackUrl(config.getReturnUrl());}
 public String checkoutUrl(Map<String,Object> order){
  if(!configured())throw new ServiceException("支付宝沙箱尚未配置完整，请按手册填写本地配置");
  if(!"RESERVED".equals(order.get("status")))throw new ServiceException("当前订单不可结算");
  try{
   AlipayClient client=new DefaultAlipayClient(config.getGateway(),config.getAppId(),config.getPrivateKey(),"json","UTF-8",config.getPublicKey(),"RSA2");
   AlipayTradePagePayRequest request=new AlipayTradePagePayRequest();request.setNotifyUrl(config.getNotifyUrl());request.setReturnUrl(config.getReturnUrl());
   AlipayTradePagePayModel model=new AlipayTradePagePayModel();model.setOutTradeNo(String.valueOf(order.get("id")));model.setTotalAmount(new BigDecimal(String.valueOf(order.get("amount"))).setScale(2).toPlainString());model.setSubject("MyHealthy课程沙箱优惠券");model.setProductCode("FAST_INSTANT_TRADE_PAY");
   // Respect the existing reservation deadline; initiating checkout does not extend it.
   Object expiry=order.get("expires_at");String expiryText=String.valueOf(expiry).replace('T',' ');if(expiryText.length()>=19)model.setTimeExpire(expiryText.substring(0,19));
   request.setBizModel(model);AlipayTradePagePayResponse response=client.pageExecute(request,"GET");String url=response.getBody();
   URI target=URI.create(url);URI gateway=URI.create(config.getGateway());if(!target.getHost().equals(gateway.getHost())||!"https".equals(target.getScheme()))throw new ServiceException("沙箱跳转地址不合法");
   return url;
  }catch(ServiceException e){throw e;}catch(Exception e){throw new ServiceException("沙箱支付参数生成失败，请检查本地RSA2密钥和配置");}
 }
 public boolean notify(Map<String,String> original){
  if(!configured())return false;Map<String,String> params=new HashMap<>(original);
  try{
   if(!"RSA2".equals(params.get("sign_type"))||!text(params.get("sign")))return false;
   if(!AlipaySignature.rsaCheckV1(new HashMap<>(params),config.getPublicKey(),"UTF-8","RSA2"))return false;
   if(!config.getAppId().equals(params.get("app_id"))||!config.getSellerId().equals(params.get("seller_id")))return false;
   if(!Arrays.asList("TRADE_SUCCESS","TRADE_FINISHED").contains(params.get("trade_status")))return false;
   String orderId=params.get("out_trade_no"),tradeNo=params.get("trade_no");
   if(orderId==null||!orderId.matches("[A-Za-z0-9_-]{1,64}")||tradeNo==null||!tradeNo.matches("[A-Za-z0-9_-]{1,100}"))return false;
   BigDecimal notifiedAmount=new BigDecimal(params.get("total_amount"));if(notifiedAmount.signum()<0)return false;
   return Boolean.TRUE.equals(tx.execute(transaction->{
    List<Map<String,Object>> orders=db.queryForList("SELECT * FROM t_seckill_order WHERE id=? FOR UPDATE",orderId);if(orders.isEmpty())return false;Map<String,Object> order=orders.get(0);
    if(new BigDecimal(String.valueOf(order.get("amount"))).compareTo(notifiedAmount)!=0)return false;
    List<Map<String,Object>> paid=db.queryForList("SELECT * FROM t_seckill_payment WHERE order_id=?",orderId);
    if(!paid.isEmpty())return "PAID".equals(order.get("status"))&&tradeNo.equals(paid.get(0).get("trade_no"))&&"ALIPAY_SANDBOX".equals(paid.get(0).get("provider"));
    if(!"RESERVED".equals(order.get("status")))return false;
    if(Boolean.TRUE.equals(db.queryForObject("SELECT expires_at<=NOW() FROM t_seckill_order WHERE id=?",Boolean.class,orderId)))return false;
    // A conflicting transaction number causes rollback instead of marking the order paid.
    db.update("INSERT INTO t_seckill_payment(order_id,trade_no,amount,provider,paid_at) VALUES(?,?,?,'ALIPAY_SANDBOX',NOW())",orderId,tradeNo,notifiedAmount);
    if(db.update("UPDATE t_seckill_order SET status='PAID',paid_at=NOW() WHERE id=? AND status='RESERVED'",orderId)!=1)throw new ServiceException("订单状态冲突");
    db.update("INSERT INTO t_event_outbox(id,event_type,payload) VALUES(?,?,?)",UUID.randomUUID().toString(),"PAID_ALIPAY_SANDBOX",orderId);return true;
   }));
  }catch(Exception e){return false;}
 }
}
