package com.lankathreads.security;

import com.lankathreads.model.Role;
import com.lankathreads.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private CustomUserDetails createMockUserDetails(Role role) {
        User user = new User();
        user.setUsername("mock_" + role.name().toLowerCase());
        user.setPassword("password");
        user.setRole(role);
        user.setName("Mock " + role.name());
        user.setEmail(role.name().toLowerCase() + "@mock.com");
        return new CustomUserDetails(user);
    }

    @Test
    public void testPublicUrls() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/support"))
                .andExpect(status().isOk());
    }

    @Test
    public void testManagerUrlProtectedForAnonymous() throws Exception {
        mockMvc.perform(get("/manager/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testManagerUrlProtectedForCustomer() throws Exception {
        CustomUserDetails customerDetails = createMockUserDetails(Role.CUSTOMER);
        mockMvc.perform(get("/manager/dashboard").with(java.util.Objects.requireNonNull(user(customerDetails))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testManagerUrlAllowedForManager() throws Exception {
        CustomUserDetails managerDetails = createMockUserDetails(Role.FACTORY_MANAGER);
        mockMvc.perform(get("/manager/dashboard").with(java.util.Objects.requireNonNull(user(managerDetails))))
                .andExpect(status().isOk());
    }

    @Test
    public void testCustomerUrlAllowedForCustomer() throws Exception {
        CustomUserDetails customerDetails = createMockUserDetails(Role.CUSTOMER);
        mockMvc.perform(get("/customer/shop").with(java.util.Objects.requireNonNull(user(customerDetails))))
                .andExpect(status().isOk());
    }
}
