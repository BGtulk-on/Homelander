package com.uktc.schoolInventory.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomLoginSuccessHandler successHandler;
    @Autowired
    private SessionUserFilter sessionUserFilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
//                .httpBasic(withDefaults())
                .formLogin(form -> form
                        .successHandler(successHandler) // Слагаме нашия нов Handler тук
                        .permitAll()
                )
                .addFilterAfter(sessionUserFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                );
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
    // Този Bean е задължителен за Baeldung метода!
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
