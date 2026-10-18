package com.lankathreads.security;

import com.lankathreads.model.User;
import com.lankathreads.repository.UserRepository;
import com.lankathreads.model.Customer;
import com.lankathreads.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Custom UserDetailsService that supports dual-table login:
 * 1. Checks internal staff 'users' table (Manager, Supervisor, Inventory, Sales, Supplier)
 * 2. If not found, checks the 'customers' table (Customer portal)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Step 1: Check internal staff users table
        Optional<User> user = userRepository.findByUsernameOrEmail(username, username);
        if (user.isPresent()) {
            return new CustomUserDetails(user.get());
        }

        // Step 2: Check customer portal users table
        Optional<Customer> customer = customerRepository.findByUsernameOrEmail(username, username);
        if (customer.isPresent()) {
            return new CustomUserDetails(customer.get());
        }

        // Step 3: If not found in either table, reject login
        throw new UsernameNotFoundException("User not found with username or email: " + username);
    }
}
