package com.ruoyi.web.controller.health;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.myhealthy.tuser.domain.PatientProfile;
import com.ruoyi.myhealthy.tuser.service.PatientProfileService;
@RestController @RequestMapping("/health/tuser")
public class PatientProfileController extends BaseController {
 private final PatientProfileService service;
 public PatientProfileController(PatientProfileService service) { this.service=service; }
 @GetMapping("/list") public TableDataInfo list(PatientProfile query) { startPage(); return getDataTable(service.list(query)); }
 @GetMapping("/{id}") public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }
 @PostMapping  public AjaxResult create(@RequestBody PatientProfile value) { return toAjax(service.create(value)); }
 @PutMapping  public AjaxResult update(@RequestBody PatientProfile value) { return toAjax(service.update(value)); }
 @DeleteMapping("/{id}") @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult delete(@PathVariable Long id) { return toAjax(service.delete(id)); }
}
