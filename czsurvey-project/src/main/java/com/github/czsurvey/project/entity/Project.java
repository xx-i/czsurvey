package com.github.czsurvey.project.entity;

import com.github.czsurvey.project.entity.audit.DateAudit;
import com.github.czsurvey.project.entity.enums.ProjectType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "t_project")
public class Project extends DateAudit {

    @Id
    @GenericGenerator(name = "snowflake", strategy = "com.github.czsurvey.extra.data.generator.SnowflakeIdGenerator")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    private String name;

    private Long parentId;

    @Enumerated(EnumType.STRING)
    private ProjectType ownerType;

    private Long ownerId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "is_deleted")
    private Boolean deleted;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project project)) return false;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static String entityName() {
        return "project";
    }
}
