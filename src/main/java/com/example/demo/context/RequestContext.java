package com.example.demo.context;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.LocalDateTime;
import java.util.Collection;

@Component
@RequestScope
@Getter
public class RequestContext implements Context {
    private LocalDateTime created;
    private String userId;
    private Collection roles;

    public LocalDateTime getCreated() {

        return created;
    }

    public String getUserId() {
        return userId;
    }


    public Collection getRoles() {
        return roles;
    }

    public RequestContext() {
        SecurityContext context = SecurityContextHolder.getContext();
        created = LocalDateTime.now();
        userId = context.getAuthentication().getCredentials().toString();
        roles = context.getAuthentication().getAuthorities();
    }

    public RequestContext(LocalDateTime created, String userId, Collection roleUser) {
        SecurityContext context = SecurityContextHolder.getContext();
        this.created = created;
        this.userId = userId;
        this.roles = roleUser;
    }

    public boolean hasRole(String role) {

        return getRoles().contains(new SimpleGrantedAuthority(role));
    }
}
