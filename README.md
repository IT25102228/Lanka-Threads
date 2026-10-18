# 🧵 Lanka Threads – Integrated Textile & Garment ERP System

A full-stack **Enterprise Resource Planning (ERP)** and **e-commerce** web platform built for garment and textile manufacturing facilities in Sri Lanka. It connects factory management, production, inventory, suppliers, sales and online customers in a single role-based system.

![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Templates-005F0F?logo=thymeleaf)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4-38BDF8?logo=tailwindcss)
![SQL Server](https://img.shields.io/badge/SQL%20Server-MySQL%20%7C%20H2-CC2927?logo=microsoftsqlserver)

---

## ✨ Features

- 🔐 **Role-based authentication** with Spring Security (6 user roles, custom login redirects)
- 🏭 **Factory management** – worker registration, efficiency overview
- 🧵 **Production tracking** – daily production logs, cutting/stitching lines, machine schedules
- 📦 **Inventory control** – fabric stock, roll & lot tracking, low-stock alerts
- 🚚 **Supplier management** – vendor directory, material intake, supplier scorecards
- 💰 **Sales** – product catalogue, pricing, order fulfilment, customer records
- 🛒 **Customer e-commerce** – shop, cart, checkout and order tracking
- 👤 **User profiles** – profile photo upload, preferences, password change

---

## 👥 System Roles & Demo Accounts

Demo accounts are created automatically on first run by `DatabaseSeeder`.

| Role | Username | Password | Responsibilities |
|---|---|---|---|
| **Factory Manager** | `manager` | `pass` | Operations overview, worker management, factory efficiency |
| **Production Supervisor** | `supervisor` | `pass` | Production logs, cutting/stitching lines, machine scheduling |
| **Inventory Officer** | `inventory` | `pass` | Fabric stock, roll & lot tracking, reorder thresholds |
| **Sales Manager** | `sales` | `pass` | Product catalogue, pricing, orders, customer relations |
| **Supplier Officer** | `supplier` | `pass` | Vendor directory, fabric intake inspection, purchase orders |
| **Customer** | `customer` | `pass` | Shopping, cart, checkout, order tracking |

> ⚠️ These credentials are for local development/demo only. Change them before any real deployment.

---

## 🧩 Design Patterns Used

| Pattern | Where |
|---|---|
| **Strategy** | `InventoryStrategy` (Standard / Bulk), `SupplierEvaluationStrategy` (Delivery Punctuality / Quality Compliance / Composite Scorecard) |
| **Observer** | `OrderEventPublisher` → `CustomerNotifier`, `OrderStatusLogger`, `StockRestorationObserver`; `ProductStockEventPublisher` → `LowStockAlertObserver`, `StockAuditLogObserver` |
| **Builder** | `ProductionLogBuilder` |
| **Singleton** | `WorkerRegistrationManager` |

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA, Hibernate, Bean Validation |
| **Frontend** | Thymeleaf, Tailwind CSS 4, Vanilla JavaScript, HTML5 |
| **Database** | Microsoft SQL Server (default) · MySQL / MariaDB · H2 (embedded) |
| **Build & Tools** | Maven Wrapper, Node.js (Tailwind CLI), Playwright, JUnit 5 |

---

## 📁 Project Structure

```text
Lanka Threads Garment/
├── backend/                         # Spring Boot application
│   ├── pom.xml                      # Maven dependencies & build config
│   ├── mvnw.cmd / .mvn/             # Maven wrapper
│   ├── run.bat                      # Backend launcher (Windows)
│   ├── scripts/                     # Helper / verification scripts
│   └── src/
│       ├── main/java/com/lankathreads/
│       │   ├── SystemApplication.java   # Application entry point
│       │   ├── config/              # Security, MVC & database seeder
│       │   ├── controller/          # Web controllers (one per role/module)
│       │   ├── model/               # JPA entities
│       │   ├── repository/          # Spring Data repositories
│       │   ├── service/             # Business logic
│       │   ├── pattern/             # Strategy / Observer / Builder / Singleton
│       │   ├── security/            # Custom user details & login handler
│       │   └── util/                # File upload & validation helpers
│       ├── main/resources/
│       │   └── application.properties
│       └── test/java/com/lankathreads/  # Unit & integration tests
├── frontend/
│   ├── templates/                   # Thymeleaf views (customer, manager, sales, ...)
│   ├── static/                      # CSS, JS and images
│   ├── tailwind/input.css           # Tailwind source
│   ├── e2e/                         # Playwright end-to-end tests
│   └── playwright.config.js
├── database/
│   ├── schema_master.sql            # Master database schema
│   └── schema_supplier_system.sql   # Supplier subsystem schema
├── data/                            # Local H2 database files (git-ignored)
├── package.json                     # Tailwind CSS & Playwright dev dependencies
├── run.bat                          # One-click launcher
└── README.md
```

---

## ✅ Prerequisites

- **Java 17+** (e.g. `winget install EclipseAdoptium.Temurin.17.JDK`)
- **One database**: SQL Server (default), MySQL/MariaDB, or nothing at all if you use H2
- **Node.js 18+** – only needed to rebuild Tailwind CSS or run Playwright tests

---

## 🏁 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/IT25102228/lanka_threads.git
cd lanka_threads
```

### 2. Configure the database

Edit `backend/src/main/resources/application.properties`.

**SQL Server (default)** – create a database `lankathreads_db` and a login `lanka_user`, then set the password via environment variable:

```powershell
$env:DB_PASSWORD = "your-sql-server-password"
```

**MySQL / XAMPP** – comment out the SQL Server block and uncomment the MySQL block. The database is created automatically.

**H2 (zero configuration)** – comment out the SQL Server block and uncomment the H2 block. Data is stored in `data/`, and the console is available at `http://localhost:8080/h2-console`.

> Tables are created automatically by Hibernate (`ddl-auto=update`). The SQL scripts in `database/` are provided for reference and manual setup.

### 3. Run the application

**Option A – One-click (Windows):** double-click `run.bat` in the project root.

**Option B – Command line:**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Then open **http://localhost:8080** and log in with one of the demo accounts above.

---

## 🎨 Rebuilding Tailwind CSS

Only needed if you change `frontend/tailwind/input.css` or add new Tailwind classes:

```bash
npm install
npm run build:css
```

---

## 🧪 Running Tests

**Backend unit & integration tests**

```powershell
cd backend
.\mvnw.cmd test
```

**Playwright end-to-end tests** (application must be running)

```bash
npm install
npx playwright install
npx playwright test --config=frontend/playwright.config.js
```

---

## 📄 License

This project was developed for academic purposes at **SLIIT**.
