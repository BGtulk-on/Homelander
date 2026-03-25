package com.uktc.schoolInventory.controllers.user;

import com.uktc.schoolInventory.controllers.user.Permission;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Role {

    USER(Collections.emptySet()),
    SUPERUSER(
           Set.of(
                  Permission.SUPERUSER_READ,
                   Permission.SUPERUSER_UPDATE,
                   Permission.SUPERUSER_DELETE,
                   Permission.SUPERUSER_CREATE,
                   Permission.ADMIN_READ,
                   Permission.ADMIN_UPDATE,
                   Permission.ADMIN_DELETE,
                   Permission.ADMIN_CREATE
           )
    ),

    ADMIN(
            Set.of(
                    Permission.ADMIN_READ,
                    Permission.ADMIN_UPDATE,
                    Permission.ADMIN_DELETE,
                    Permission.ADMIN_CREATE
            )
    )
    ;

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities (){
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }}
