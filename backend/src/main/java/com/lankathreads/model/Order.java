package com.lankathreads.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = true)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column
    private String shippingAddress;

    @Column
    private String phone;

    @Column(name = "order_code", unique = true)
    private String orderCode;

    @Column(name = "payment_method")
    private String paymentMethod; // Credit / Debit Card, Direct Bank Transfer, KOKO Installments, Cash on Delivery

    @Column(name = "payment_reference")
    private String paymentReference; // Card ending digits, bank ref number, etc.

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    // Helper: get OrderStatus enum from string field
    @Transient
    public OrderStatus getOrderStatus() {
        return OrderStatus.fromString(status);
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.status = orderStatus.getDisplayName();
    }

    // Helper: check if cancellable by customer
    @Transient
    public boolean isCancellableByCustomer() {
        return getOrderStatus().isCancellableByCustomer();
    }

    // Helper: get step number for progress display
    @Transient
    public int getStatusStep() {
        return getOrderStatus().getStepNumber();
    }
}
