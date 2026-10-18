-- ==============================================================================
-- LANKA THREADS ERP - SUPPLIER & MATERIAL INTAKE SUBSYSTEM SCHEMA
-- Location: src/main/resources/database/schema_supplier_system.sql
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. MASTER SUPPLIERS TABLE (Master vendor profiles)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,       -- e.g. 'SUP-001'
    name VARCHAR(255) NOT NULL,                      -- e.g. 'Apex Textiles Lanka'
    contact_info VARCHAR(255) NOT NULL,              -- e.g. '0112345678 / sales@apex.lk'
    address VARCHAR(255) DEFAULT 'Colombo, Sri Lanka',
    fabric_type_supplied VARCHAR(100) DEFAULT 'Cotton',
    colour_supplied VARCHAR(100) DEFAULT 'Multi',
    quantity_supplied DOUBLE DEFAULT 0.0,
    price DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',    -- 'ACTIVE', 'INACTIVE'
    po_number VARCHAR(50) DEFAULT 'PO-2026-001',
    expected_delivery_date DATE
);

-- ------------------------------------------------------------------------------
-- 2. RECEIVED MATERIALS TABLE (Shipment deliveries with QA Approval Status)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS received_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id VARCHAR(50) NOT NULL UNIQUE,          -- e.g. 'MR-2026-001'
    po_number VARCHAR(50) NOT NULL,                  -- e.g. 'PO-2026-001'
    supplier_code VARCHAR(50) NOT NULL,              -- Links to suppliers(supplier_code)
    supplier_name VARCHAR(255) NOT NULL,             -- Supplier snapshot name
    contact_info VARCHAR(255),
    fabric_type VARCHAR(100) NOT NULL,               -- e.g. 'Pique Cotton'
    colour VARCHAR(100) NOT NULL,                    -- e.g. 'Navy Blue'
    quantity_received DOUBLE NOT NULL,               -- e.g. 500.0 (kg)
    price DECIMAL(10, 2) NOT NULL,                   -- e.g. 150000.00 (LKR)
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_INSPECTION', 
    -- QA Status Values:
    --   'APPROVED'           -> Material passed QA & Restocked into Inventory
    --   'PENDING_INSPECTION' -> Awaiting lab / physical quality check
    --   'REJECTED'           -> Defective / returned to supplier
    received_date DATE NOT NULL,
    expected_delivery_date DATE,
    roll_number VARCHAR(100) DEFAULT 'R-1001',
    lot_number VARCHAR(100) DEFAULT 'LOT-2002',
    notes VARCHAR(500),
    CONSTRAINT fk_received_supplier FOREIGN KEY (supplier_code) REFERENCES suppliers(supplier_code) ON DELETE CASCADE
);

-- ------------------------------------------------------------------------------
-- 3. AUDIT LOGS TABLE (Material receiving and QA inspection logs)
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

-- ==============================================================================
-- SAMPLE SEED DATA
-- ==============================================================================

-- Seed Suppliers
INSERT INTO suppliers (supplier_code, name, contact_info, address, status) VALUES
('SUP-001', 'Apex Textiles Lanka', '0112345678 / sales@apex.lk', 'Biyagama EPZ, Sri Lanka', 'ACTIVE'),
('SUP-002', 'Lanka Knit Fab Mills', '0779876543 / info@lankaknit.lk', 'Katunayake EPZ, Sri Lanka', 'ACTIVE'),
('SUP-003', 'Ceylon Cotton Mills', '0334567890 / orders@ceyloncotton.com', 'Horana Industrial Zone, Sri Lanka', 'ACTIVE');

-- Seed Received Materials with QA Approval Statuses
INSERT INTO received_materials (receipt_id, po_number, supplier_code, supplier_name, contact_info, fabric_type, colour, quantity_received, price, status, received_date, roll_number, lot_number) VALUES
('MR-2026-001', 'PO-2026-001', 'SUP-001', 'Apex Textiles Lanka', '0112345678 / sales@apex.lk', 'Pique Cotton', 'Navy Blue', 300.0, 90000.00, 'APPROVED', '2026-09-10', 'R-1001', 'LOT-901'),
('MR-2026-002', 'PO-2026-002', 'SUP-002', 'Lanka Knit Fab Mills', '0779876543 / info@lankaknit.lk', 'Single Jersey Cotton', 'White', 250.0, 70000.00, 'APPROVED', '2026-09-11', 'R-1002', 'LOT-902'),
('MR-2026-003', 'PO-2026-003', 'SUP-003', 'Ceylon Cotton Mills', '0334567890 / orders@ceyloncotton.com', 'Interlock Knit', 'Black', 400.0, 130000.00, 'PENDING_INSPECTION', '2026-09-12', 'R-1003', 'LOT-903');
