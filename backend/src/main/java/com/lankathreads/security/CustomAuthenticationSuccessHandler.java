package com.lankathreads.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * Handles post-login redirection based on the authenticated user's role.
 * Directs each user directly to their respective department dashboard or shop.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Redirect based on user role
        if (roles.contains("ROLE_FACTORY_MANAGER")) {
            response.sendRedirect("/manager/dashboard");
        } else if (roles.contains("ROLE_PRODUCTION_SUPERVISOR")) {
            response.sendRedirect("/supervisor/dashboard");
        } else if (roles.contains("ROLE_INVENTORY_OFFICER")) {
            response.sendRedirect("/inventory/dashboard");
        } else if (roles.contains("ROLE_SALES_MANAGER")) {
            response.sendRedirect("/sales/dashboard");
        } else if (roles.contains("ROLE_SUPPLIER_OFFICER")) {
            response.sendRedirect("/supplier/dashboard");
        } else if (roles.contains("ROLE_CUSTOMER")) {
            response.sendRedirect("/customer/shop");
        } else {
            response.sendRedirect("/");
        }
    }
}
