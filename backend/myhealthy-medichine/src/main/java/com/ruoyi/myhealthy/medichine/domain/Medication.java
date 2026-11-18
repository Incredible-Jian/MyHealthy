package com.ruoyi.myhealthy.medichine.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 用药记录. userId always links to sys_user.user_id. */
public class Medication {
    private Long id;
    private Long userId;
    private String medicineName;
    private String dosage;
    private String frequency;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date startDate;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date endDate;
    private String notes;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String value) { this.medicineName=value; }
    public String getDosage() { return dosage; }
    public void setDosage(String value) { this.dosage=value; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String value) { this.frequency=value; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date value) { this.startDate=value; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date value) { this.endDate=value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { this.notes=value; }
}
