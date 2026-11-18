package com.ruoyi.myhealthy.analysis.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 健康分析. userId always links to sys_user.user_id. */
public class HealthAnalysis {
    private Long id;
    private Long userId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date periodStart;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date periodEnd;
    private String summary;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public Date getPeriodStart() { return periodStart; }
    public void setPeriodStart(Date value) { this.periodStart=value; }
    public Date getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Date value) { this.periodEnd=value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { this.summary=value; }
}
