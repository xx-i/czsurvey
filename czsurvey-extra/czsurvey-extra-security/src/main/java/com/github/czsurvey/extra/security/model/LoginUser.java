package com.github.czsurvey.extra.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author YanYu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private Long id;

    private String username;

    private String password;

    private Boolean enabled;

    private Set<String> roles;

    private Set<String> authorities;

    /**
     * 获取当前登录用户
     */
    public static LoginUser me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new InsufficientAuthenticationException("Full authentication is required to access this resource");
    }

    /**
     * 是否有某个角色
     */
    public boolean hasThisRole(String roleCode) {
        return roles.contains(roleCode);
    }

    /**
     * 是否有某个权限
     */
    public boolean hasThisAuthority(String authority) {
        return authorities.contains(authority);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(authorities)
            .map(e -> e.stream().map(SimpleGrantedAuthority::new).toList())
            .orElse(Collections.emptyList());
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
