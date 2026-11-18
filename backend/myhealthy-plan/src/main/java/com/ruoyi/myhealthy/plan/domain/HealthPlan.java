package com.ruoyi.myhealthy.plan.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 健康计划. userId always links to sys_user.user_id. */
public class HealthPlan {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date startDate;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date endDate;
    private String status;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title=value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content=value; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date value) { this.startDate=value; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date value) { this.endDate=value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status=value; }
}
