package com.ruoyi.web.controller.health;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import com.ruoyi.myhealthy.secgoods.payment.AlipaySandboxService;
@RestController public class AlipayNotificationController {
 private final AlipaySandboxService alipay;public AlipayNotificationController(AlipaySandboxService alipay){this.alipay=alipay;}
 @PostMapping(value="/health/alipay/notify",consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE,produces=MediaType.TEXT_PLAIN_VALUE)
 public String notify(HttpServletRequest request){Map<String,String> params=new HashMap<>();for(Map.Entry<String,String[]> e:request.getParameterMap().entrySet()){if(e.getValue()==null||e.getValue().length!=1)return "failure";params.put(e.getKey(),e.getValue()[0]);}return alipay.notify(params)?"success":"failure";}
}
