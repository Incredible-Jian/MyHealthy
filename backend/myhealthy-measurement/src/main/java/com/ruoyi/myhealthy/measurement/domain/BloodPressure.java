package com.ruoyi.myhealthy.measurement.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 血压测量. userId always links to sys_user.user_id. */
public class BloodPressure {
    private Long id;
    private Long userId;
    private Long deviceId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date measureTime;
    private Integer systolic;
    private Integer diastolic;
    private Integer heartRate;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { this.deviceId=value; }
    public Date getMeasureTime() { return measureTime; }
    public void setMeasureTime(Date value) { this.measureTime=value; }
    public Integer getSystolic() { return systolic; }
    public void setSystolic(Integer value) { this.systolic=value; }
    public Integer getDiastolic() { return diastolic; }
    public void setDiastolic(Integer value) { this.diastolic=value; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer value) { this.heartRate=value; }
}
