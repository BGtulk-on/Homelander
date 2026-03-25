package com.uktc.schoolInventory.models;

import com.uktc.schoolInventory.controllers.user.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;

@Entity
@Table(name = "users") // Свързва се с таблицата users в SQL [cite: 58]
@Data // Lombok автоматично прави getter/setter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = true)
    private Role role = Role.USER;

    @Column(name = "approved")
    private Boolean approved = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Role getRole() {
        return role != null ? role : Role.USER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRole().getAuthorities();
    }

    @Override public String getPassword() {return passwordHash; }
    @Override public String getUsername() {return email; }
    @Override public boolean isAccountNonExpired() {return true; }
    @Override public boolean isAccountNonLocked() {return true; }
    @Override public boolean isCredentialsNonExpired() {return true; }
    @Override public boolean isEnabled() {return true; }
}