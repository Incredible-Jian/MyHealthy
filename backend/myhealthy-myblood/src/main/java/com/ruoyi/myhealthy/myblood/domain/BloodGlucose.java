package com.ruoyi.myhealthy.myblood.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 血糖测量. userId always links to sys_user.user_id. */
public class BloodGlucose {
    private Long id;
    private Long userId;
    private Long deviceId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date measureTime;
    private BigDecimal glucose;
    private String mealState;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { this.deviceId=value; }
    public Date getMeasureTime() { return measureTime; }
    public void setMeasureTime(Date value) { this.measureTime=value; }
    public BigDecimal getGlucose() { return glucose; }
    public void setGlucose(BigDecimal value) { this.glucose=value; }
    public String getMealState() { return mealState; }
    public void setMealState(String value) { this.mealState=value; }
}
