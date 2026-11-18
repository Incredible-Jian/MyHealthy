package com.ruoyi.myhealthy.lipid.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 血脂测量. userId always links to sys_user.user_id. */
public class BloodLipid {
    private Long id;
    private Long userId;
    private Long deviceId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date measureTime;
    private BigDecimal totalCholesterol;
    private BigDecimal triglycerides;
    private BigDecimal hdl;
    private BigDecimal ldl;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { this.deviceId=value; }
    public Date getMeasureTime() { return measureTime; }
    public void setMeasureTime(Date value) { this.measureTime=value; }
    public BigDecimal getTotalCholesterol() { return totalCholesterol; }
    public void setTotalCholesterol(BigDecimal value) { this.totalCholesterol=value; }
    public BigDecimal getTriglycerides() { return triglycerides; }
    public void setTriglycerides(BigDecimal value) { this.triglycerides=value; }
    public BigDecimal getHdl() { return hdl; }
    public void setHdl(BigDecimal value) { this.hdl=value; }
    public BigDecimal getLdl() { return ldl; }
    public void setLdl(BigDecimal value) { this.ldl=value; }
}
