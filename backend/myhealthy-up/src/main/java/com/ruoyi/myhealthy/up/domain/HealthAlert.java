package com.ruoyi.myhealthy.up.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 异常预警. userId always links to sys_user.user_id. */
public class HealthAlert {
    private Long id;
    private Long userId;
    private String measurementType;
    private Long measurementId;
    private String severity;
    private String message;
    private String status;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String value) { this.measurementType=value; }
    public Long getMeasurementId() { return measurementId; }
    public void setMeasurementId(Long value) { this.measurementId=value; }
    public String getSeverity() { return severity; }
    public void setSeverity(String value) { this.severity=value; }
    public String getMessage() { return message; }
    public void setMessage(String value) { this.message=value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status=value; }
}
