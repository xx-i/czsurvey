package com.github.czsurvey.project.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "t_role")
@IdClass(RolePrimaryKey.class)
public class Role {

    @Id
    private Long userId;

    @Id
    private String role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role1)) return false;
        return Objects.equals(userId, role1.userId) && Objects.equals(role, role1.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, role);
    }
}
