package com.ruoyi.myhealthy.secgoods.payment;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component @ConfigurationProperties(prefix="health.alipay")
public class AlipaySandboxProperties {
 private boolean enabled;private String gateway="",appId="",privateKey="",publicKey="",sellerId="",notifyUrl="",returnUrl="";
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getGateway(){return gateway;}public void setGateway(String v){gateway=v;}
 public String getAppId(){return appId;}public void setAppId(String v){appId=v;}
 public String getPrivateKey(){return privateKey;}public void setPrivateKey(String v){privateKey=v;}
 public String getPublicKey(){return publicKey;}public void setPublicKey(String v){publicKey=v;}
 public String getSellerId(){return sellerId;}public void setSellerId(String v){sellerId=v;}
 public String getNotifyUrl(){return notifyUrl;}public void setNotifyUrl(String v){notifyUrl=v;}
 public String getReturnUrl(){return returnUrl;}public void setReturnUrl(String v){returnUrl=v;}
}
