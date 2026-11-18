package com.ruoyi.web.controller.health;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.myhealthy.mylog.domain.CollectionLog;
import com.ruoyi.myhealthy.mylog.service.CollectionLogService;
@RestController @RequestMapping("/health/mylog")
public class CollectionLogController extends BaseController {
 private final CollectionLogService service;
 public CollectionLogController(CollectionLogService service) { this.service=service; }
 @GetMapping("/list") public TableDataInfo list(CollectionLog query) { startPage(); return getDataTable(service.list(query)); }
 @GetMapping("/{id}") public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }
 @PostMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult create(@RequestBody CollectionLog value) { return toAjax(service.create(value)); }
 @PutMapping @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult update(@RequestBody CollectionLog value) { return toAjax(service.update(value)); }
 @DeleteMapping("/{id}") @PreAuthorize("@ss.hasRole('admin') or @ss.hasRole('healthadmin')") public AjaxResult delete(@PathVariable Long id) { return toAjax(service.delete(id)); }
}
