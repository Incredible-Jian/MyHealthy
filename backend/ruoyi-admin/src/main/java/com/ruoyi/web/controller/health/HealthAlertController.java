package com.ruoyi.web.controller.health;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.myhealthy.up.domain.HealthAlert;
import com.ruoyi.myhealthy.up.service.HealthAlertService;
@RestController @RequestMapping("/health/up")
public class HealthAlertController extends BaseController {
 private final HealthAlertService service;
 public HealthAlertController(HealthAlertService service) { this.service=service; }
 @GetMapping("/list") public TableDataInfo list(HealthAlert query) { startPage(); return getDataTable(service.list(query)); }
 @GetMapping("/{id}") public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }
 @PostMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult create(@RequestBody HealthAlert value) { return toAjax(service.create(value)); }
 @PutMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult update(@RequestBody HealthAlert value) { return toAjax(service.update(value)); }
 @DeleteMapping("/{id}") @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult delete(@PathVariable Long id) { return toAjax(service.delete(id)); }
}
