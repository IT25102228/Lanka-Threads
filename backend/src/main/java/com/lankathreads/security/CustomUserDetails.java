package com.lankathreads.security;

import com.lankathreads.model.Role;
import com.lankathreads.model.User;
import com.lankathreads.model.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation for Spring Security.
 * Adapts either a factory staff 'User' or an online 'Customer' to Spring Security's UserDetails.
 */
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Customer customer;
    private final boolean isCustomer;

    /**
     * Constructor for Factory Staff Users (Manager, Supervisor, Inventory, Sales, Supplier).
     */
    public CustomUserDetails(User user) {
        this.user = user;
        this.customer = null;
        this.isCustomer = false;
    }

    /**
     * Constructor for Online Customer Users.
     * Also initializes an adapter User object for template compatibility.
     */
    public CustomUserDetails(Customer customer) {
        this.customer = customer;
        this.isCustomer = true;
        
        // Create an adapter User so Thymeleaf profile templates work seamlessly
        User u = new User();
        u.setId(customer.getId());
        u.setUsername(customer.getUsername());
        u.setPassword(customer.getPassword());
        u.setName(customer.getName());
        u.setEmail(customer.getEmail());
        u.setRole(Role.CUSTOMER);
        u.setProfilePhoto(customer.getProfilePhoto());
        u.setThemePreference(customer.getThemePreference());
        this.user = u;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = isCustomer ? Role.CUSTOMER : user.getRole();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return isCustomer ? customer.getPassword() : user.getPassword();
    }

    @Override
    public String getUsername() {
        return isCustomer ? customer.getUsername() : user.getUsername();
    }

    public User getUser() {
        return user;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isCustomer() {
        return isCustomer;
    }

    public void setThemePreference(String theme) {
        if (user != null) {
            user.setThemePreference(theme);
        }
        if (customer != null) {
            customer.setThemePreference(theme);
        }
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
