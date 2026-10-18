package com.lankathreads.repository;

import com.lankathreads.model.CartItem;
import com.lankathreads.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCustomer(Customer customer);

    List<CartItem> findByCustomerId(Long customerId);

    Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.customer = :customer")
    void deleteByCustomer(@Param("customer") Customer customer);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.customer.id = :customerId AND c.product.id = :productId")
    void deleteByCustomerIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);
}
