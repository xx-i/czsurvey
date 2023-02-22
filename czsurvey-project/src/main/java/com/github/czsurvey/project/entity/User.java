package com.github.czsurvey.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.czsurvey.project.entity.audit.DateAudit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

/**
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "t_user")
public class User extends DateAudit {
    @Id
    @GenericGenerator(name = "snowflake", strategy = "com.github.czsurvey.extra.data.generator.SnowflakeIdGenerator")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @JsonIgnore
    private String password;

    private String nickname;

    private String realName;

    private String avatar;

    private String phone;

    private String email;

    private String wxOpenid;

    @Column(name = "is_enabled")
    private Boolean enabled;

    @ToString.Exclude
    @OneToMany(mappedBy = "userId")
    private Set<Role> roles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
