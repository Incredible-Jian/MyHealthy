package com.ruoyi.myhealthy.mylog.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 采集日志. userId always links to sys_user.user_id. */
public class CollectionLog {
    private Long id;
    private Long userId;
    private Long deviceId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date collectedAt;
    private String status;
    private String message;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long value) { this.deviceId=value; }
    public Date getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Date value) { this.collectedAt=value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status=value; }
    public String getMessage() { return message; }
    public void setMessage(String value) { this.message=value; }
}
