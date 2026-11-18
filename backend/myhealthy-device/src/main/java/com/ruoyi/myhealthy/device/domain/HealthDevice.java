package com.ruoyi.myhealthy.device.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 健康设备. userId always links to sys_user.user_id. */
public class HealthDevice {
    private Long id;
    private Long userId;
    private String deviceName;
    private String deviceType;
    private String serialNumber;
    private String status;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String value) { this.deviceName=value; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String value) { this.deviceType=value; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String value) { this.serialNumber=value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status=value; }
}
