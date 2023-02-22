package com.github.czsurvey.project.entity.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.MappedSuperclass;

/**
 * @author YanYu
 */
@Getter
@Setter
@MappedSuperclass
public class UserDateAudit extends DateAudit {

    @CreatedBy
    @JsonIgnore
    private Long createBy;

    @LastModifiedBy
    @JsonIgnore
    private Long updateBy;
}
