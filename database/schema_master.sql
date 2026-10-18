-- ==============================================================================
-- LANKA THREADS ERP - MASTER DATABASE SCHEMA
-- Location: src/main/resources/database/schema_master.sql
-- Database: MySQL 8.0+ / MariaDB (InnoDB, utf8mb4)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS lankathreads CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lankathreads;

-- ------------------------------------------------------------------------------
-- 1. SYSTEM USERS & AUTHENTICATION
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,                       -- 'FACTORY_MANAGER', 'PRODUCTION_SUPERVISOR', 'INVENTORY_OFFICER', 'SALES_MANAGER', 'SUPPLIER_OFFICER', 'CUSTOMER'
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    profile_photo VARCHAR(255),
    theme_preference VARCHAR(20) DEFAULT 'DARK',     -- 'DARK', 'LIGHT'
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 2. FACTORY WORKERS (Factory Manager & Supervisor Module)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_code VARCHAR(50) NOT NULL UNIQUE,         -- e.g. 'W-001'
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,                      -- 'Master Tailor', 'Cutting Specialist', 'Embroidery Tech'
    department VARCHAR(100) NOT NULL,                -- 'Assembly', 'Cutting', 'Finishing', 'Quality Control'
    hourly_rate DECIMAL(10, 2) NOT NULL DEFAULT 650.00,
    daily_target INT DEFAULT 50,
    active BOOLEAN DEFAULT TRUE,
    joined_date DATE
);

-- ------------------------------------------------------------------------------
-- 3. MASTER SUPPLIERS (Supplier Module)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,       -- e.g. 'SUP-001'
    name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255) NOT NULL,
    address VARCHAR(255) DEFAULT 'Colombo, Sri Lanka',
    fabric_type_supplied VARCHAR(100) DEFAULT 'Cotton',
    colour_supplied VARCHAR(100) DEFAULT 'Multi',
    quantity_supplied DOUBLE DEFAULT 0.0,
    price DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    po_number VARCHAR(50) DEFAULT 'PO-2026-001',
    expected_delivery_date DATE
);

-- ------------------------------------------------------------------------------
-- 4. RAW MATERIAL INTAKE & QUALITY INSPECTION
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS received_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id VARCHAR(50) NOT NULL UNIQUE,          -- e.g. 'MR-2026-001'
    po_number VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255),
    fabric_type VARCHAR(100) NOT NULL,               -- e.g. 'Pique Cotton'
    colour VARCHAR(100) NOT NULL,                    -- e.g. 'Navy Blue'
    quantity_received DOUBLE NOT NULL,               -- Weight in kg
    price DECIMAL(10, 2) NOT NULL,                   -- Total cost in LKR
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_INSPECTION', -- 'APPROVED', 'PENDING_INSPECTION', 'REJECTED'
    received_date DATE NOT NULL,
    expected_delivery_date DATE,
    roll_number VARCHAR(100) DEFAULT 'R-1001',
    lot_number VARCHAR(100) DEFAULT 'LOT-2002',
    notes VARCHAR(500),
    CONSTRAINT fk_received_supplier FOREIGN KEY (supplier_code) REFERENCES suppliers(supplier_code) ON DELETE CASCADE
);

-- ------------------------------------------------------------------------------
-- 5. ACTIVE RAW MATERIAL INVENTORY (Inventory Officer Module)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS raw_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(100) NOT NULL UNIQUE,
    lot_number VARCHAR(100) NOT NULL,
    fabric_type VARCHAR(100) NOT NULL,
    colour VARCHAR(100) NOT NULL,
    quantity_kg DOUBLE NOT NULL,
    reorder_level DOUBLE DEFAULT 100.0,
    unit_price DECIMAL(10, 2) NOT NULL,
    location_bin VARCHAR(50) DEFAULT 'Warehouse A',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------------------------
-- 6. POLO PRODUCTS & BILL OF MATERIALS (Sales Module)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,        -- e.g. 'POLO-001'
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'Polo Shirts',
    size VARCHAR(20) NOT NULL,                       -- 'S', 'M', 'L', 'XL', 'XXL'
    color VARCHAR(50) NOT NULL,                      -- 'Navy Blue', 'Classic White', 'Jet Black'
    price DECIMAL(10, 2) NOT NULL,                   -- Selling price in LKR
    stock_quantity INT NOT NULL DEFAULT 0,
    gsm INT DEFAULT 220,
    fabric_type VARCHAR(100) DEFAULT '100% Combed Pique Cotton',
    image_url VARCHAR(255) DEFAULT '/images/polo-hero.jpg',
    active BOOLEAN DEFAULT TRUE
);

-- ------------------------------------------------------------------------------
-- 7. AUDIT ACTIVITY LOGS
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS material_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id VARCHAR(50) NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,                     -- 'MATERIAL_RECEIVED', 'QUALITY_APPROVED', 'QUALITY_REJECTED', 'INVENTORY_RESTOCKED'
    details VARCHAR(1000) NOT NULL,
    fabric_type VARCHAR(100),
    colour VARCHAR(100),
    quantity DOUBLE,
    performed_by VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
