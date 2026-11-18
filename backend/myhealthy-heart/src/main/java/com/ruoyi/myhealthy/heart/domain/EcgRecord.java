package com.ruoyi.myhealthy.heart.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 心电测量. userId always links to sys_user.user_id. */
public class EcgRecord {
    private Long id;
    private Long userId;
    private Long deviceId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date measureTime;
    private Integer heartRate;
    private Integer durationSeconds;
    private String waveform;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { this.deviceId=value; }
    public Date getMeasureTime() { return measureTime; }
    public void setMeasureTime(Date value) { this.measureTime=value; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer value) { this.heartRate=value; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer value) { this.durationSeconds=value; }
    public String getWaveform() { return waveform; }
    public void setWaveform(String value) { this.waveform=value; }
}
