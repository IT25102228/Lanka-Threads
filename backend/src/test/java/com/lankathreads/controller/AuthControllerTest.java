package com.lankathreads.controller;

import com.lankathreads.model.Customer;
import com.lankathreads.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Automatically rolls back database changes after each test!
@SuppressWarnings("null")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerService customerService;

    @Test
    public void testSupportFormSubmission() throws Exception {
        mockMvc.perform(post("/support")
                        .param("name", "Test User")
                        .param("email", "test@example.com")
                        .param("subject", "Test Subject")
                        .param("message", "Test Message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/support"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    public void testSuccessfulUserRegistration() throws Exception {
        String uniqueUsername = "newtestuser_" + System.currentTimeMillis();
        String uniqueEmail = "newtest_" + System.currentTimeMillis() + "@example.com";

        mockMvc.perform(post("/register")
                        .param("name", "Test Account")
                        .param("email", uniqueEmail)
                        .param("username", uniqueUsername)
                        .param("password", "securepassword")
                        .param("role", "CUSTOMER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        // Verify customer exists in the customers database table
        assertTrue(customerService.getCustomerByUsername(uniqueUsername).isPresent());
    }

    @Test
    public void testDuplicateUsernameRegistrationRejection() throws Exception {
        // Create an existing customer first
        String duplicateUsername = "existinguser_" + System.currentTimeMillis();
        Customer existingCustomer = new Customer();
        existingCustomer.setName("Existing User");
        existingCustomer.setEmail("existing@example.com");
        existingCustomer.setUsername(duplicateUsername);
        existingCustomer.setPassword("password");
        customerService.saveCustomer(existingCustomer);

        // Try registering with the same username
        mockMvc.perform(post("/register")
                        .param("name", "Another User")
                        .param("email", "another@example.com")
                        .param("username", duplicateUsername)
                        .param("password", "newpassword")
                        .param("role", "CUSTOMER"))
                .andExpect(status().isOk()) // returns register template (status 200 OK)
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(containsString("is already taken")));
    }

    @Test
    public void testDuplicateEmailRegistrationRejection() throws Exception {
        // Create an existing customer first
        String duplicateEmail = "existingemail_" + System.currentTimeMillis() + "@example.com";
        Customer existingCustomer = new Customer();
        existingCustomer.setName("Existing User");
        existingCustomer.setEmail(duplicateEmail);
        existingCustomer.setUsername("someuniqueusername");
        existingCustomer.setPassword("password");
        customerService.saveCustomer(existingCustomer);

        // Try registering with the same email
        mockMvc.perform(post("/register")
                        .param("name", "Another User")
                        .param("email", duplicateEmail)
                        .param("username", "anotherusername")
                        .param("password", "newpassword")
                        .param("role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(containsString("is already registered")));
    }
}
