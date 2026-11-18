package com.ruoyi.myhealthy.secgoods.payment;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import java.security.*;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.alipay.api.internal.util.AlipaySignature;
public class AlipaySandboxServiceTest {
 private AlipaySandboxProperties config;private AlipaySandboxService service;private JdbcTemplate db;private String privateKey;
 @Before public void setup() throws Exception {
  KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);KeyPair keys=generator.generateKeyPair();
  privateKey=Base64.getEncoder().encodeToString(keys.getPrivate().getEncoded());
  config=new AlipaySandboxProperties();config.setEnabled(true);config.setGateway("https://openapi-sandbox.dl.alipaydev.com/gateway.do");config.setAppId("testapp");config.setSellerId("testseller");config.setPrivateKey(privateKey);config.setPublicKey(Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()));config.setNotifyUrl("http://localhost/health/alipay/notify");config.setReturnUrl("http://localhost/health-payment-return");
  DriverManagerDataSource ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");db=new JdbcTemplate(ds);
  db.execute("CREATE TABLE t_seckill_order(id varchar(64) primary key,amount decimal(10,2),status varchar(20),expires_at timestamp,paid_at timestamp)");
  db.execute("CREATE TABLE t_seckill_payment(order_id varchar(64) unique,trade_no varchar(100) unique,amount decimal(10,2),provider varchar(30),paid_at timestamp)");
  db.execute("CREATE TABLE t_event_outbox(id varchar(64),event_type varchar(50),payload varchar(64))");
  service=new AlipaySandboxService(config,db,new TransactionTemplate(new DataSourceTransactionManager(ds)));order("order1","RESERVED",true);
 }
 private void order(String id,String status,boolean future){db.update("INSERT INTO t_seckill_order(id,amount,status,expires_at) VALUES(?,?,?,?)",id,new BigDecimal("9.90"),status,new java.sql.Timestamp(System.currentTimeMillis()+(future?600000:-600000)));}
 private Map<String,String> callback(String id,String trade,String amount,String app,String seller,String status) throws Exception {
  Map<String,String> p=new HashMap<>();p.put("out_trade_no",id);p.put("trade_no",trade);p.put("total_amount",amount);p.put("app_id",app);p.put("seller_id",seller);p.put("trade_status",status);
  String content=AlipaySignature.getSignCheckContentV1(new HashMap<>(p));p.put("sign",AlipaySignature.rsaSign(content,privateKey,"UTF-8","RSA2"));p.put("sign_type","RSA2");return p;
 }
 private Map<String,String> valid() throws Exception{return callback("order1","trade1","9.90","testapp","testseller","TRADE_SUCCESS");}
 private int count(String table){return db.queryForObject("SELECT COUNT(*) FROM "+table,Integer.class);}
 @Test public void signedCheckoutStaysInSandbox(){String url=service.checkoutUrl(db.queryForMap("SELECT * FROM t_seckill_order WHERE id='order1'"));assertTrue(url.startsWith(config.getGateway()+"?"));assertTrue(url.contains("sign="));assertTrue(url.contains("order1"));assertTrue(url.contains("9.90"));}
 @Test public void disabledAndProductionGatewayBlocked(){config.setEnabled(false);assertFalse(service.configured());assertFalse(service.notify(Collections.emptyMap()));config.setEnabled(true);config.setGateway("https://openapi.alipay.com/gateway.do");assertFalse(service.configured());try{service.checkoutUrl(Collections.emptyMap());fail();}catch(com.ruoyi.common.exception.ServiceException expected){}}
 @Test public void authenticCallbackIsIdempotent() throws Exception{Map<String,String> p=valid();assertTrue(service.notify(p));assertTrue(service.notify(p));assertEquals("PAID",db.queryForObject("SELECT status FROM t_seckill_order",String.class));assertEquals(1,count("t_seckill_payment"));assertEquals(1,count("t_event_outbox"));}
 @Test public void tamperingRejected() throws Exception{Map<String,String> p=valid();p.put("total_amount","0.01");assertFalse(service.notify(p));assertEquals(0,count("t_seckill_payment"));}
 @Test public void signedBusinessMismatchesRejected() throws Exception{
  assertFalse(service.notify(callback("order1","trade1","8.90","testapp","testseller","TRADE_SUCCESS")));
  assertFalse(service.notify(callback("order1","trade1","9.90","other","testseller","TRADE_SUCCESS")));
  assertFalse(service.notify(callback("order1","trade1","9.90","testapp","other","TRADE_SUCCESS")));
  assertFalse(service.notify(callback("order1","trade1","9.90","testapp","testseller","WAIT_BUYER_PAY")));assertEquals(0,count("t_seckill_payment"));
 }
 @Test public void expiredAndCancelledRemainUnpaid() throws Exception{db.update("UPDATE t_seckill_order SET expires_at=?",new java.sql.Timestamp(System.currentTimeMillis()-60000));assertFalse(service.notify(valid()));db.update("UPDATE t_seckill_order SET status='CANCELLED'");assertFalse(service.notify(valid()));assertEquals(0,count("t_seckill_payment"));assertEquals(0,count("t_event_outbox"));}
 @Test public void duplicateTradeRollsBackOtherOrder() throws Exception{assertTrue(service.notify(valid()));order("order2","RESERVED",true);assertFalse(service.notify(callback("order2","trade1","9.90","testapp","testseller","TRADE_SUCCESS")));assertEquals("RESERVED",db.queryForObject("SELECT status FROM t_seckill_order WHERE id='order2'",String.class));assertEquals(1,count("t_event_outbox"));}
}
