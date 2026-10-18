package com.lankathreads.config;

import com.lankathreads.security.CustomAuthenticationSuccessHandler;
import com.lankathreads.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security Configuration.
 * Configures password encryption, authentication providers, and role-based URL access.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    /**
     * 1. Password Encoder Bean:
     * Uses BCrypt strong hashing algorithm to securely hash and verify passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 2. Authentication Manager Bean:
     * Connects Spring Security to our CustomUserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    /**
     * 3. Security Filter Chain:
     * Defines URL authorization rules, login page, and logout behavior.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for simplified form and AJAX submissions
            .csrf(csrf -> csrf.disable())
            // Allow H2 database console frames
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            
            // Define access permissions per URL pattern
            .authorizeHttpRequests(authz -> authz
                // Public URLs (accessible by everyone without logging in)
                .requestMatchers("/login", "/register", "/support", "/support/**", "/css/**", "/js/**", "/images/**", "/h2-console/**", "/uploads/**").permitAll()
                
                // Role-restricted URLs
                .requestMatchers("/manager/**").hasRole("FACTORY_MANAGER")
                .requestMatchers("/supervisor/**").hasRole("PRODUCTION_SUPERVISOR")
                .requestMatchers("/inventory/**").hasRole("INVENTORY_OFFICER")
                .requestMatchers("/sales/**").hasRole("SALES_MANAGER")
                .requestMatchers("/supplier/**").hasRole("SUPPLIER_OFFICER")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                
                // All other URLs require authentication
                .anyRequest().authenticated()
            )
            
            // Form Login configuration
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler) // Redirects user based on their specific role
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
