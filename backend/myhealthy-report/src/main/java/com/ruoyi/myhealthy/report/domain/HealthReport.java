package com.ruoyi.myhealthy.report.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 健康报告. userId always links to sys_user.user_id. */
public class HealthReport {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String source;
    private String status;
    private Long reviewerId;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date reviewedAt;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title=value; }
    public String getContent() { return content; }
    public void setContent(String value) { this.content=value; }
    public String getSource() { return source; }
    public void setSource(String value) { this.source=value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status=value; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long value) { this.reviewerId=value; }
    public Date getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Date value) { this.reviewedAt=value; }
}
