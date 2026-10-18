package com.lankathreads.repository;

import com.lankathreads.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsernameOrEmail(String username, String email);
    Optional<Customer> findByCustomerCode(String customerCode);
}
