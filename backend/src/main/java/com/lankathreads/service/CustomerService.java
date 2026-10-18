package com.lankathreads.service;

import com.lankathreads.model.Customer;
import com.lankathreads.model.Order;
import com.lankathreads.repository.CustomerRepository;
import com.lankathreads.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing registered Customers, unique customer codes, and customer account cleanup.
 */
@SuppressWarnings("null")
@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Saves a new customer, automatically encrypting password and generating a unique Customer Code.
     */
    public Customer saveCustomer(Customer customer) {
        // Step 1: Encrypt raw password if not already BCrypt-hashed
        if (customer.getPassword() != null && !customer.getPassword().startsWith("$2a$")) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        }
        // Step 2: Auto-assign customer code (e.g. CUST-10452) if missing
        if (customer.getCustomerCode() == null || customer.getCustomerCode().isBlank()) {
            customer.setCustomerCode(generateUniqueCustomerCode());
        }
        // Step 3: Save to database
        return customerRepository.save(customer);
    }

    /**
     * Generates a unique customer identifier in format "CUST-XXXXX".
     */
    public String generateUniqueCustomerCode() {
        String code;
        do {
            int randomNum = (int) (Math.random() * 90000 + 10000); // 5-digit number
            code = "CUST-" + randomNum;
        } while (customerRepository.findByCustomerCode(code).isPresent());
        return code;
    }

    /**
     * Retrieves all registered customers.
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Finds a customer by username.
     */
    public Optional<Customer> getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    /**
     * Finds a customer by email address.
     */
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    /**
     * Finds a customer by either username or email (used during login).
     */
    public Optional<Customer> getCustomerByUsernameOrEmail(String identifier) {
        return customerRepository.findByUsernameOrEmail(identifier, identifier);
    }

    /**
     * Finds a customer by customer code.
     */
    public Optional<Customer> getCustomerByCode(String code) {
        return customerRepository.findByCustomerCode(code);
    }

    /**
     * Finds a customer by database primary key ID.
     */
    @Nullable
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    /**
     * Saves changes to an existing customer.
     */
    public Customer saveExistingCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    /**
     * Deletes a customer account and any associated customer orders.
     */
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer != null) {
            List<Order> orders = orderRepository.findByCustomerOrderByOrderDateDesc(customer);
            if (orders != null && !orders.isEmpty()) {
                orderRepository.deleteAll(orders);
            }
            customerRepository.delete(customer);
        }
    }
}
