package com.lankathreads.repository;

import com.lankathreads.model.Order;
import com.lankathreads.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);
    java.util.Optional<Order> findByOrderCode(String orderCode);
}

