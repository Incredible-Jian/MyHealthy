package com.ruoyi.web.controller.health;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.myhealthy.plan.domain.HealthPlan;
import com.ruoyi.myhealthy.plan.service.HealthPlanService;
@RestController @RequestMapping("/health/plan")
public class HealthPlanController extends BaseController {
 private final HealthPlanService service;
 public HealthPlanController(HealthPlanService service) { this.service=service; }
 @GetMapping("/list") public TableDataInfo list(HealthPlan query) { startPage(); return getDataTable(service.list(query)); }
 @GetMapping("/{id}") public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }
 @PostMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin') or @ss.hasRole('healthyperson')") public AjaxResult create(@RequestBody HealthPlan value) { return toAjax(service.create(value)); }
 @PutMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin') or @ss.hasRole('healthyperson')") public AjaxResult update(@RequestBody HealthPlan value) { return toAjax(service.update(value)); }
 @DeleteMapping("/{id}") @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult delete(@PathVariable Long id) { return toAjax(service.delete(id)); }
}
