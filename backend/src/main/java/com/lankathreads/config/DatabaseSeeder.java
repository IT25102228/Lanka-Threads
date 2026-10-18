package com.lankathreads.config;

import com.lankathreads.model.*;
import com.lankathreads.repository.*;
import com.lankathreads.model.*;
import com.lankathreads.repository.*;
import com.lankathreads.model.*;
import com.lankathreads.repository.*;
import com.lankathreads.model.*;
import com.lankathreads.repository.*;
import com.lankathreads.model.*;
import com.lankathreads.repository.*;
import com.lankathreads.model.*;
import com.lankathreads.repository.*;
import com.lankathreads.model.*;
import com.lankathreads.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ReceivedMaterialRepository receivedMaterialRepository;

    @Autowired
    private MaterialLogRepository materialLogRepository;

    @Autowired
    private ProductionLogRepository productionLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 0. Clean legacy FKs safely if any exist
        if (jdbcTemplate != null) {
            try {
                List<String> oldFks = jdbcTemplate.queryForList(
                        "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders' AND (REFERENCED_TABLE_NAME = 'users' OR CONSTRAINT_NAME = 'FKsjfs85qf6vmcurlx43cnc16gy')",
                        String.class
                );
                for (String fk : oldFks) {
                    try {
                        jdbcTemplate.execute("ALTER TABLE orders DROP FOREIGN KEY " + fk);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        // 1. Seed System Role Users with default password "pass"
        String defaultPassword = passwordEncoder.encode("pass");

        seedOrUpdateUser("manager", defaultPassword, "Factory Manager", "manager@lankathreads.com", Role.FACTORY_MANAGER);
        User supervisorUser = seedOrUpdateUser("supervisor", defaultPassword, "Production Supervisor", "supervisor@lankathreads.com", Role.PRODUCTION_SUPERVISOR);
        seedOrUpdateUser("inventory", defaultPassword, "Inventory Officer", "inventory@lankathreads.com", Role.INVENTORY_OFFICER);
        seedOrUpdateUser("sales", defaultPassword, "Sales Manager", "sales@lankathreads.com", Role.SALES_MANAGER);
        seedOrUpdateUser("supplier", defaultPassword, "Supplier Officer", "supplier@lankathreads.com", Role.SUPPLIER_OFFICER);

        System.out.println("✅ All system role users verified (manager, supervisor, inventory, sales, supplier). Password: pass");

        // 1.1 Seed Demo Customer into customers table
        seedOrUpdateCustomer("customer", defaultPassword, "John Customer", "customer@gmail.com", "CUST-10001");
        System.out.println("✅ Demo Customer verified in customers table. Password: pass");

        // 2. Seed Clean Products if empty
        if (productRepository.count() == 0) {
            List<String> colors = Arrays.asList("Red", "Blue", "Green", "Black", "White", "Yellow", "Navy Blue", "Pink", "Heather Grey", "Orange");
            List<String> sizes = Arrays.asList("XS", "S", "M", "L", "XL", "2XL");

            for (int i = 0; i < colors.size(); i++) {
                String color = colors.get(i);
                String size = sizes.get(i % sizes.size());
                Product product = new Product();
                product.setName("Men's Premium Polo");
                product.setCategory("Men");
                product.setColour(color);
                product.setSize(size);
                product.setPrice(new BigDecimal("1200.00"));
                product.setFabricCost(new BigDecimal("400.00"));
                product.setLaborCost(new BigDecimal("250.00"));
                product.setOverheadCost(new BigDecimal("150.00"));
                product.setStockQuantity(50 + (i * 5));
                productRepository.save(product);
            }

            for (int i = 0; i < colors.size(); i++) {
                String color = colors.get(i);
                String size = sizes.get((i + 1) % sizes.size());
                Product product = new Product();
                product.setName("Women's Slim Fit Polo");
                product.setCategory("Women");
                product.setColour(color);
                product.setSize(size);
                product.setPrice(new BigDecimal("1100.00"));
                product.setFabricCost(new BigDecimal("380.00"));
                product.setLaborCost(new BigDecimal("240.00"));
                product.setOverheadCost(new BigDecimal("140.00"));
                product.setStockQuantity(40 + (i * 6));
                productRepository.save(product);
            }

            for (int i = 0; i < colors.size(); i++) {
                String color = colors.get(i);
                String size = sizes.get((i + 2) % sizes.size());
                Product product = new Product();
                product.setName("Kids Active Polo");
                product.setCategory("Children");
                product.setColour(color);
                product.setSize(size);
                product.setPrice(new BigDecimal("800.00"));
                product.setFabricCost(new BigDecimal("280.00"));
                product.setLaborCost(new BigDecimal("180.00"));
                product.setOverheadCost(new BigDecimal("100.00"));
                product.setStockQuantity(60 + (i * 4));
                productRepository.save(product);
            }
            System.out.println("✅ 30 Default Products seeded with 10 colors and XS-2XL sizes.");
        }

        // 3. Seed Default Workers if empty
        if (workerRepository.count() == 0) {
            Worker w1 = new Worker();
            w1.setWorkerId("W001");
            w1.setName("Amal Silva");
            w1.setGender("Male");
            w1.setMobileNumber("+94 77 123 4567");
            w1.setCategoryAssigned("Men");
            w1.setActiveProductionLine("Line E");
            w1.setShift("Morning");
            w1.setSkill("Cutting");
            w1.setStatus("Active");
            workerRepository.save(w1);

            Worker w2 = new Worker();
            w2.setWorkerId("W002");
            w2.setName("Kamal Perera");
            w2.setGender("Male");
            w2.setMobileNumber("+94 77 123 4567");
            w2.setCategoryAssigned("Women");
            w2.setActiveProductionLine("Line C");
            w2.setShift("Morning");
            w2.setSkill("Ironing");
            w2.setStatus("Active");
            workerRepository.save(w2);

            Worker w3 = new Worker();
            w3.setWorkerId("W003");
            w3.setName("Nimal Fernando");
            w3.setGender("Male");
            w3.setMobileNumber("+94 77 987 6543");
            w3.setCategoryAssigned("Children");
            w3.setActiveProductionLine("Line B");
            w3.setShift("Evening");
            w3.setSkill("Sewing");
            w3.setStatus("Active");
            workerRepository.save(w3);

            Worker w4 = new Worker();
            w4.setWorkerId("W004");
            w4.setName("Sunil Jayasinghe");
            w4.setGender("Male");
            w4.setMobileNumber("+94 77 234 5678");
            w4.setCategoryAssigned("Men");
            w4.setActiveProductionLine("Line E");
            w4.setShift("Evening");
            w4.setSkill("Packing");
            w4.setStatus("Active");
            workerRepository.save(w4);

            Worker w5 = new Worker();
            w5.setWorkerId("W005");
            w5.setName("Priyanthi Kumari");
            w5.setGender("Female");
            w5.setMobileNumber("+94 77 567 8901");
            w5.setCategoryAssigned("Women");
            w5.setActiveProductionLine("Line C");
            w5.setShift("Morning");
            w5.setSkill("Checking");
            w5.setStatus("Active");
            workerRepository.save(w5);

            Worker w6 = new Worker();
            w6.setWorkerId("W006");
            w6.setName("Nimali Silva");
            w6.setGender("Female");
            w6.setMobileNumber("+94 77 678 9012");
            w6.setCategoryAssigned("Women");
            w6.setActiveProductionLine("Line B");
            w6.setShift("Morning");
            w6.setSkill("Checking");
            w6.setStatus("Active");
            workerRepository.save(w6);
            System.out.println("✅ 6 Default Worker profiles seeded.");
        }

        // 4. Seed Inventory if empty
        if (inventoryRepository.count() == 0) {
            Inventory i1 = new Inventory();
            i1.setFabricType("Pique Cotton");
            i1.setColour("Navy Blue");
            i1.setQuantityAvailable(120.0);
            i1.setReorderLevel(150.0);
            i1.setRollNumber("R-9011");
            i1.setLotNumber("LOT-201");
            i1.setRequisitionLine("Line E");
            i1.setCategory("Men");
            inventoryRepository.save(i1);

            Inventory i2 = new Inventory();
            i2.setFabricType("Single Jersey Cotton");
            i2.setColour("White");
            i2.setQuantityAvailable(350.0);
            i2.setReorderLevel(100.0);
            i2.setRollNumber("R-9012");
            i2.setLotNumber("LOT-202");
            i2.setRequisitionLine("None");
            i2.setCategory("Women");
            inventoryRepository.save(i2);

            Inventory i3 = new Inventory();
            i3.setFabricType("Polyester Cotton Blend");
            i3.setColour("Red");
            i3.setQuantityAvailable(45.0);
            i3.setReorderLevel(50.0);
            i3.setRollNumber("R-9013");
            i3.setLotNumber("LOT-203");
            i3.setRequisitionLine("Line B");
            i3.setCategory("Men");
            inventoryRepository.save(i3);

            Inventory i4 = new Inventory();
            i4.setFabricType("Pique Cotton");
            i4.setColour("Black");
            i4.setQuantityAvailable(80.0);
            i4.setReorderLevel(100.0);
            i4.setRollNumber("R-9014");
            i4.setLotNumber("LOT-204");
            i4.setRequisitionLine("Line C");
            i4.setCategory("Children");
            inventoryRepository.save(i4);

            Inventory i5 = new Inventory();
            i5.setFabricType("Interlock Knit");
            i5.setColour("Pink");
            i5.setQuantityAvailable(250.0);
            i5.setReorderLevel(80.0);
            i5.setRollNumber("R-9015");
            i5.setLotNumber("LOT-205");
            i5.setRequisitionLine("None");
            i5.setCategory("Women");
            inventoryRepository.save(i5);
            System.out.println("✅ Default Fabric inventory seeded.");
        }

        // 5. Seed Master Suppliers, Received Materials, and Audit Logs if empty
        if (supplierRepository.count() == 0) {
            Supplier s1 = new Supplier();
            s1.setSupplierCode("SUP-001");
            s1.setName("Lanka Textiles Ltd");
            s1.setContactInfo("0112345678 / sales@lankatex.com");
            s1.setAddress("Colombo 03, Sri Lanka");
            s1.setFabricTypeSupplied("Pique Cotton");
            s1.setColourSupplied("Navy Blue");
            s1.setQuantitySupplied(500.0);
            s1.setPrice(new BigDecimal("150000.00"));
            s1.setStatus("ACTIVE");
            s1.setPoNumber("PO-2026-001");
            s1.setExpectedDeliveryDate(LocalDate.now().plusDays(2));
            supplierRepository.save(s1);

            Supplier s2 = new Supplier();
            s2.setSupplierCode("SUP-002");
            s2.setName("Textile Garment Hub");
            s2.setContactInfo("0118765432 / info@texhub.lk");
            s2.setAddress("Katunayake EPZ, Sri Lanka");
            s2.setFabricTypeSupplied("Single Jersey Cotton");
            s2.setColourSupplied("White");
            s2.setQuantitySupplied(800.0);
            s2.setPrice(new BigDecimal("200000.00"));
            s2.setStatus("ACTIVE");
            s2.setPoNumber("PO-2026-002");
            s2.setExpectedDeliveryDate(LocalDate.now().plusDays(5));
            supplierRepository.save(s2);

            Supplier s3 = new Supplier();
            s3.setSupplierCode("SUP-003");
            s3.setName("Drape Fabric Suppliers");
            s3.setContactInfo("0331234567 / sales@drape.lk");
            s3.setAddress("Biyagama EPZ, Sri Lanka");
            s3.setFabricTypeSupplied("Polyester Cotton Blend");
            s3.setColourSupplied("Red");
            s3.setQuantitySupplied(300.0);
            s3.setPrice(new BigDecimal("90000.00"));
            s3.setStatus("ACTIVE");
            s3.setPoNumber("PO-2026-003");
            s3.setExpectedDeliveryDate(LocalDate.now().plusDays(10));
            supplierRepository.save(s3);
            System.out.println("✅ Master Suppliers seeded.");
        }

        if (receivedMaterialRepository.count() == 0) {
            ReceivedMaterial m1 = new ReceivedMaterial();
            m1.setReceiptId("MR-2026-001");
            m1.setPoNumber("PO-2026-001");
            m1.setSupplierCode("SUP-001");
            m1.setSupplierName("Lanka Textiles Ltd");
            m1.setContactInfo("0112345678 / sales@lankatex.com");
            m1.setFabricType("Pique Cotton");
            m1.setColour("Navy Blue");
            m1.setQuantityReceived(500.0);
            m1.setPrice(new BigDecimal("150000.00"));
            m1.setStatus("APPROVED");
            m1.setReceivedDate(LocalDate.now().minusDays(3));
            m1.setExpectedDeliveryDate(LocalDate.now().plusDays(2));
            m1.setRollNumber("R-9011");
            m1.setLotNumber("LOT-201");
            m1.setNotes("First quality batch inspected and passed");
            receivedMaterialRepository.save(m1);

            ReceivedMaterial m2 = new ReceivedMaterial();
            m2.setReceiptId("MR-2026-002");
            m2.setPoNumber("PO-2026-002");
            m2.setSupplierCode("SUP-002");
            m2.setSupplierName("Textile Garment Hub");
            m2.setContactInfo("0118765432 / info@texhub.lk");
            m2.setFabricType("Single Jersey Cotton");
            m2.setColour("White");
            m2.setQuantityReceived(800.0);
            m2.setPrice(new BigDecimal("200000.00"));
            m2.setStatus("APPROVED");
            m2.setReceivedDate(LocalDate.now().minusDays(2));
            m2.setExpectedDeliveryDate(LocalDate.now().plusDays(5));
            m2.setRollNumber("R-9012");
            m2.setLotNumber("LOT-202");
            m2.setNotes("Clean knit fabric, approved for Line B");
            receivedMaterialRepository.save(m2);

            ReceivedMaterial m3 = new ReceivedMaterial();
            m3.setReceiptId("MR-2026-003");
            m3.setPoNumber("PO-2026-003");
            m3.setSupplierCode("SUP-003");
            m3.setSupplierName("Drape Fabric Suppliers");
            m3.setContactInfo("0331234567 / sales@drape.lk");
            m3.setFabricType("Polyester Cotton Blend");
            m3.setColour("Red");
            m3.setQuantityReceived(300.0);
            m3.setPrice(new BigDecimal("90000.00"));
            m3.setStatus("PENDING_INSPECTION");
            m3.setReceivedDate(LocalDate.now().minusDays(1));
            m3.setExpectedDeliveryDate(LocalDate.now().plusDays(10));
            m3.setRollNumber("R-9013");
            m3.setLotNumber("LOT-203");
            m3.setNotes("Awaiting lab shrinkage test verification");
            receivedMaterialRepository.save(m3);
            System.out.println("✅ Received Materials seeded.");
        }

        if (materialLogRepository.count() == 0) {
            MaterialLog l1 = new MaterialLog("MR-2026-001", "SUP-001", "Lanka Textiles Ltd", "MATERIAL_RECEIVED",
                    "Initial intake of 500kg Pique Cotton (Navy Blue)", "Pique Cotton", "Navy Blue", 500.0, "Supplier Coordination Officer");
            materialLogRepository.save(l1);

            MaterialLog l2 = new MaterialLog("MR-2026-001", "SUP-001", "Lanka Textiles Ltd", "QUALITY_APPROVED",
                    "Quality Inspection APPROVED. GSM and color fastness verified.", "Pique Cotton", "Navy Blue", 500.0, "QA Inspector");
            materialLogRepository.save(l2);

            MaterialLog l3 = new MaterialLog("MR-2026-002", "SUP-002", "Textile Garment Hub", "MATERIAL_RECEIVED",
                    "Intake of 800kg Single Jersey Cotton (White)", "Single Jersey Cotton", "White", 800.0, "Supplier Coordination Officer");
            materialLogRepository.save(l3);

            MaterialLog l4 = new MaterialLog("MR-2026-002", "SUP-002", "Textile Garment Hub", "QUALITY_APPROVED",
                    "Quality Inspection APPROVED. Clean roll certification verified.", "Single Jersey Cotton", "White", 800.0, "QA Inspector");
            materialLogRepository.save(l4);

            MaterialLog l5 = new MaterialLog("MR-2026-003", "SUP-003", "Drape Fabric Suppliers", "MATERIAL_RECEIVED",
                    "Intake of 300kg Polyester Cotton Blend (Red) - Flagged for inspection", "Polyester Cotton Blend", "Red", 300.0, "Supplier Coordination Officer");
            materialLogRepository.save(l5);
            System.out.println("✅ Material Receiving Logs seeded.");
        }

        // 6. Seed Production Logs if empty
        if (productionLogRepository.count() == 0 && supervisorUser != null) {
            ProductionLog pl1 = new ProductionLog();
            pl1.setDate(LocalDate.now().minusDays(3));
            pl1.setCategory("Men");
            pl1.setQuantityProduced(120);
            pl1.setTargetQuantity(150);
            pl1.setWipStage("Cutting");
            pl1.setDowntimeMinutes(15);
            pl1.setDowntimeReason("Needle Breakage");
            pl1.setProductionLine("Line E");
            pl1.setSupervisor(supervisorUser);
            productionLogRepository.save(pl1);

            ProductionLog pl2 = new ProductionLog();
            pl2.setDate(LocalDate.now().minusDays(2));
            pl2.setCategory("Women");
            pl2.setQuantityProduced(95);
            pl2.setTargetQuantity(120);
            pl2.setWipStage("Sewing");
            pl2.setDowntimeMinutes(30);
            pl2.setDowntimeReason("Thread Snapping");
            pl2.setProductionLine("Line B");
            pl2.setSupervisor(supervisorUser);
            productionLogRepository.save(pl2);

            ProductionLog pl3 = new ProductionLog();
            pl3.setDate(LocalDate.now().minusDays(1));
            pl3.setCategory("Children");
            pl3.setQuantityProduced(70);
            pl3.setTargetQuantity(100);
            pl3.setWipStage("Ironing");
            pl3.setDowntimeMinutes(0);
            pl3.setDowntimeReason("None");
            pl3.setProductionLine("Line C");
            pl3.setSupervisor(supervisorUser);
            productionLogRepository.save(pl3);

            ProductionLog pl4 = new ProductionLog();
            pl4.setDate(LocalDate.now());
            pl4.setCategory("Men");
            pl4.setQuantityProduced(150);
            pl4.setTargetQuantity(200);
            pl4.setWipStage("Packaging");
            pl4.setDowntimeMinutes(45);
            pl4.setDowntimeReason("Motor Repair");
            pl4.setProductionLine("Line E");
            pl4.setSupervisor(supervisorUser);
            productionLogRepository.save(pl4);
            System.out.println("✅ Production logs seeded.");
        }

        System.out.println("🚀 Database initialization complete. Lanka Threads is ready.");
    }

    private User seedOrUpdateUser(String username, String encodedPassword, String name, String email, Role role) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(role);
            user.setName(name);
            user.setEmail(email);
            user.setPassword(encodedPassword);
            return userRepository.save(user);
        } else {
            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setName(name);
            user.setEmail(email);
            user.setRole(role);
            return userRepository.save(user);
        }
    }

    private Customer seedOrUpdateCustomer(String username, String encodedPassword, String name, String email, String customerCode) {
        var custOpt = customerRepository.findByUsername(username);
        if (custOpt.isPresent()) {
            Customer c = custOpt.get();
            c.setName(name);
            c.setEmail(email);
            c.setCustomerCode(customerCode);
            c.setPassword(encodedPassword);
            return customerRepository.save(c);
        } else {
            Customer customer = new Customer();
            customer.setUsername(username);
            customer.setPassword(encodedPassword);
            customer.setName(name);
            customer.setEmail(email);
            customer.setCustomerCode(customerCode);
            return customerRepository.save(customer);
        }
    }
}
