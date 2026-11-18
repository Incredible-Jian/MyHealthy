package com.ruoyi.myhealthy.tuser.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
/** 患者档案. userId always links to sys_user.user_id. */
public class PatientProfile {
    private Long id;
    private Long userId;
    private String realName;
    private Integer age;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String phone;
    private Long practitionerId;
    public Long getId() { return id; }
    public void setId(Long value) { this.id=value; }
    public Long getUserId() { return userId; }
    public void setUserId(Long value) { this.userId=value; }
    public String getRealName() { return realName; }
    public void setRealName(String value) { this.realName=value; }
    public Integer getAge() { return age; }
    public void setAge(Integer value) { this.age=value; }
    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal value) { this.heightCm=value; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal value) { this.weightKg=value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone=value; }
    public Long getPractitionerId() { return practitionerId; }
    public void setPractitionerId(Long value) { this.practitionerId=value; }
}
