package com.ruoyi.web.controller.health;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.myhealthy.secgoods.service.SeckillService;
@RestController @RequestMapping("/health/shop") public class ShopController{
 private final SeckillService service;public ShopController(SeckillService service){this.service=service;}
 @GetMapping("/payment-configuration") public AjaxResult paymentConfiguration(){return service.paymentConfiguration();}
 @GetMapping("/orders/{id}/payment-status") public AjaxResult paymentStatus(@PathVariable String id){return service.paymentStatus(id);}
 @GetMapping("/goods") public AjaxResult goods(){return service.goods();}
 @GetMapping("/orders") public AjaxResult orders(){return service.orders();}
 @PostMapping("/reserve/{goodsId}") public AjaxResult reserve(@PathVariable long goodsId){return service.reserve(goodsId);}
 @PostMapping("/orders/{id}/cancel") public AjaxResult cancel(@PathVariable String id){return service.cancel(id);}
 @PostMapping("/orders/{id}/checkout") public AjaxResult checkout(@PathVariable String id){return service.checkout(id);}
 @PostMapping("/orders/{id}/demo-pay") public AjaxResult demo(@PathVariable String id){return service.demo(id);}
}
