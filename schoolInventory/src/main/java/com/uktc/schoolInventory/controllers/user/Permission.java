package com.uktc.schoolInventory.controllers.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    SUPERUSER_READ("superuser:read"),
    SUPERUSER_UPDATE("superuser:update"),
    SUPERUSER_DELETE("superuser:delete"),
    SUPERUSER_CREATE("superuser:create"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),
    ADMIN_CREATE("admin:create")

    ;

    @Getter
    private final String permission;
}
