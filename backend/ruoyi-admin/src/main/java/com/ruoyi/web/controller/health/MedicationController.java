package com.ruoyi.web.controller.health;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.myhealthy.medichine.domain.Medication;
import com.ruoyi.myhealthy.medichine.service.MedicationService;
@RestController @RequestMapping("/health/medichine")
public class MedicationController extends BaseController {
 private final MedicationService service;
 public MedicationController(MedicationService service) { this.service=service; }
 @GetMapping("/list") public TableDataInfo list(Medication query) { startPage(); return getDataTable(service.list(query)); }
 @GetMapping("/{id}") public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }
 @PostMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin') or @ss.hasRole('healthyperson') or @ss.hasRole('illman')") public AjaxResult create(@RequestBody Medication value) { return toAjax(service.create(value)); }
 @PutMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin') or @ss.hasRole('healthyperson') or @ss.hasRole('illman')") public AjaxResult update(@RequestBody Medication value) { return toAjax(service.update(value)); }
 @DeleteMapping("/{id}") @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult delete(@PathVariable Long id) { return toAjax(service.delete(id)); }
}
