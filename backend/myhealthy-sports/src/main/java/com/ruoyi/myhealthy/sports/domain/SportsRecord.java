package com.ruoyi.myhealthy.sports.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 运动记录. userId always links to sys_user.user_id. */
public class SportsRecord {
    private Long id;
    private Long userId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date measureTime;
    private Integer steps;
    private Integer durationSeconds;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Date getMeasureTime() { return measureTime; }
    public void setMeasureTime(Date value) { this.measureTime=value; }
    public Integer getSteps() { return steps; }
    public void setSteps(Integer value) { this.steps=value; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer value) { this.durationSeconds=value; }
}
