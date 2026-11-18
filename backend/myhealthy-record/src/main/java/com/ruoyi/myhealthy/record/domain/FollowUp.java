package com.ruoyi.myhealthy.record.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 随访记录. userId always links to sys_user.user_id. */
public class FollowUp {
    private Long id;
    private Long userId;
    private Long practitionerId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date visitTime;
    private String content;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date nextVisitTime;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Long getPractitionerId() { return practitionerId; }
    public void setPractitionerId(Long value) { this.practitionerId=value; }
    public Date getVisitTime() { return visitTime; }
    public void setVisitTime(Date value) { this.visitTime=value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content=value; }
    public Date getNextVisitTime() { return nextVisitTime; }
    public void setNextVisitTime(Date value) { this.nextVisitTime=value; }
}
