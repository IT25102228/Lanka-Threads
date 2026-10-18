const http = require('http');
const querystring = require('querystring');

const BASE_URL = 'http://localhost:8080';

// Helper class for maintaining user session (cookies)
class HttpClient {
    constructor() {
        this.cookies = {};
    }

    setCookieHeader(res) {
        const setCookie = res.headers['set-cookie'];
        if (setCookie) {
            setCookie.forEach(cookieStr => {
                const parts = cookieStr.split(';')[0].split('=');
                if (parts.length === 2) {
                    this.cookies[parts[0].trim()] = parts[1].trim();
                }
            });
        }
    }

    getCookieHeader() {
        return Object.entries(this.cookies)
            .map(([k, v]) => `${k}=${v}`)
            .join('; ');
    }

    request(method, path, data = null, headers = {}) {
        return new Promise((resolve, reject) => {
            const url = new URL(path, BASE_URL);
            let postData = '';

            const reqHeaders = {
                ...headers,
                'User-Agent': 'Mozilla/5.0 (Node Verification Agent)',
            };

            const cookieStr = this.getCookieHeader();
            if (cookieStr) {
                reqHeaders['Cookie'] = cookieStr;
            }

            if (data) {
                if (typeof data === 'object') {
                    postData = querystring.stringify(data);
                    reqHeaders['Content-Type'] = 'application/x-www-form-urlencoded';
                } else {
                    postData = data;
                }
                reqHeaders['Content-Length'] = Buffer.byteLength(postData);
            }

            const options = {
                hostname: url.hostname,
                port: url.port,
                path: url.pathname + url.search,
                method: method,
                headers: reqHeaders,
            };

            const req = http.request(options, (res) => {
                this.setCookieHeader(res);
                let body = '';
                res.on('data', chunk => body += chunk);
                res.on('end', () => {
                    resolve({
                        statusCode: res.statusCode,
                        headers: res.headers,
                        body: body,
                        location: res.headers['location']
                    });
                });
            });

            req.on('error', (e) => reject(e));

            if (postData) {
                req.write(postData);
            }
            req.end();
        });
    }

    async get(path) {
        return this.request('GET', path);
    }

    async post(path, data = null, headers = {}) {
        return this.request('POST', path, data, headers);
    }

    async login(username, password) {
        // Step 1: GET /login
        await this.get('/login');
        // Step 2: POST credentials to /login
        const res = await this.post('/login', { username, password });
        return res;
    }
}

async function runTests() {
    console.log('===============================================================');
    console.log('   LANKA THREADS - 6 USERS, ALL FUNCTIONS & THEMES TEST SUITE  ');
    console.log('===============================================================\n');

    let passed = 0;
    let failed = 0;

    function assert(condition, message) {
        if (condition) {
            console.log(`  [PASS] ${message}`);
            passed++;
        } else {
            console.error(`  [FAIL] ${message}`);
            failed++;
        }
    }

    // -------------------------------------------------------------
    // 1. PUBLIC AUTH & SUPPORT TESTS
    // -------------------------------------------------------------
    console.log('\n--- 1. Public Authentication & Support Flow ---');
    const pubClient = new HttpClient();
    
    // Login page
    const loginPage = await pubClient.get('/login');
    assert(loginPage.statusCode === 200 && loginPage.body.includes('Lanka Threads'), 'Login page loads successfully');

    // Support Form
    const supportRes = await pubClient.post('/support', {
        name: 'Jane Doe',
        email: 'janedoe@example.com',
        subject: 'Wholesale Inquiry',
        message: 'Looking for bulk polo shirts'
    });
    assert(supportRes.statusCode === 302 && supportRes.location.includes('/support'), 'Support ticket submission succeeds and redirects');

    // Registration of a new customer
    const newCustUser = 'test_cust_' + Date.now();
    const newCustEmail = newCustUser + '@test.com';
    const regRes = await pubClient.post('/register', {
        name: 'Auto Test User',
        email: newCustEmail,
        username: newCustUser,
        password: 'pass',
        role: 'CUSTOMER'
    });
    assert(regRes.statusCode === 302 && regRes.location.includes('/login?registered'), 'New customer registration succeeds');

    // Verify login with newly registered user
    const newCustClient = new HttpClient();
    const newLoginRes = await newCustClient.login(newCustUser, 'pass');
    assert(newLoginRes.statusCode === 302 && newLoginRes.location.includes('/customer/shop'), 'Newly registered user login succeeds and redirects to /customer/shop');

    // -------------------------------------------------------------
    // 2. FACTORY MANAGER ROLE (user: manager)
    // -------------------------------------------------------------
    console.log('\n--- 2. Factory Manager Role Tests (manager) ---');
    const managerClient = new HttpClient();
    const mgrLogin = await managerClient.login('manager', 'pass');
    assert(mgrLogin.statusCode === 302 && mgrLogin.location.includes('/manager/dashboard'), 'Manager logs in and redirects to /manager/dashboard');

    const mgrDash = await managerClient.get('/manager/dashboard');
    assert(mgrDash.statusCode === 200 && mgrDash.body.includes('Workforce Analytics') || mgrDash.body.includes('Factory'), 'Manager dashboard loads with metrics');

    // Theme toggle (Dark <-> Light)
    const mgrThemeToggle1 = await managerClient.post('/profile/preferences/toggle');
    const mgrThemeJson1 = JSON.parse(mgrThemeToggle1.body);
    assert(mgrThemeJson1.success === true, `Manager toggled theme to ${mgrThemeJson1.theme}`);
    const mgrThemeToggle2 = await managerClient.post('/profile/preferences/toggle');
    const mgrThemeJson2 = JSON.parse(mgrThemeToggle2.body);
    assert(mgrThemeJson2.success === true, `Manager toggled theme back to ${mgrThemeJson2.theme}`);

    // Worker Management
    const workersPage = await managerClient.get('/manager/workers');
    assert(workersPage.statusCode === 200, 'Manager workers page loads');

    // Add new worker
    const addWorkerRes = await managerClient.post('/manager/workers/add', {
        name: 'Test Tailor Amal',
        gender: 'Male',
        mobileNumber: '+94 77 123 4567',
        categoryAssigned: 'Men',
        activeProductionLine: 'Line A',
        shift: 'Morning',
        skill: 'Sewing',
        status: 'Active'
    });
    assert(addWorkerRes.statusCode === 302, 'Manager successfully added new worker');

    // -------------------------------------------------------------
    // 3. PRODUCTION SUPERVISOR ROLE (user: supervisor)
    // -------------------------------------------------------------
    console.log('\n--- 3. Production Supervisor Role Tests (supervisor) ---');
    const supervisorClient = new HttpClient();
    const supLogin = await supervisorClient.login('supervisor', 'pass');
    assert(supLogin.statusCode === 302 && supLogin.location.includes('/supervisor/dashboard'), 'Supervisor logs in and redirects to /supervisor/dashboard');

    const supDash = await supervisorClient.get('/supervisor/dashboard');
    assert(supDash.statusCode === 200, 'Supervisor dashboard loads successfully');

    // Theme Toggle for Supervisor
    const supThemeRes = await supervisorClient.post('/profile/preferences/toggle');
    assert(JSON.parse(supThemeRes.body).success === true, 'Supervisor theme toggle works');

    // Shift Schedule
    const schedPage = await supervisorClient.get('/supervisor/schedule');
    assert(schedPage.statusCode === 200, 'Supervisor shift schedule page loads');

    // Daily Production Logs
    const prodLogPage = await supervisorClient.get('/supervisor/production-log');
    assert(prodLogPage.statusCode === 200, 'Supervisor production log page loads');

    // Add Production Log
    const addLogRes = await supervisorClient.post('/supervisor/production-log/add', {
        productionLine: 'Line A',
        category: 'Men',
        quantityProduced: 120,
        targetQuantity: 150,
        wipStage: 'Sewing',
        downtimeMinutes: 15,
        downtimeReason: 'Thread reel replacement'
    });
    assert(addLogRes.statusCode === 302, 'Supervisor successfully logged daily production output');

    // -------------------------------------------------------------
    // 4. INVENTORY OFFICER ROLE (user: inventory)
    // -------------------------------------------------------------
    console.log('\n--- 4. Inventory Officer Role Tests (inventory) ---');
    const inventoryClient = new HttpClient();
    const invLogin = await inventoryClient.login('inventory', 'pass');
    assert(invLogin.statusCode === 302 && invLogin.location.includes('/inventory/dashboard'), 'Inventory Officer logs in and redirects to /inventory/dashboard');

    const invDash = await inventoryClient.get('/inventory/dashboard');
    assert(invDash.statusCode === 200, 'Inventory dashboard loads successfully');

    // Theme Toggle for Inventory
    const invThemeRes = await inventoryClient.post('/profile/preferences/toggle');
    assert(JSON.parse(invThemeRes.body).success === true, 'Inventory theme toggle works');

    // Manage stock rolls
    const invManage = await inventoryClient.get('/inventory/manage');
    assert(invManage.statusCode === 200, 'Inventory stock manage page loads');

    // Add fabric stock roll
    const addRollRes = await inventoryClient.post('/inventory/add', {
        fabricType: 'Single Jersey 100% Cotton',
        colour: 'Royal Blue',
        quantityAvailable: 450.0,
        reorderLevel: 100.0,
        lotNumber: 'LOT-9901',
        rollNumber: 'R-9901',
        status: 'Available',
        requisitionLine: 'Line A'
    });
    assert(addRollRes.statusCode === 302, 'Inventory officer added fabric stock roll');

    // -------------------------------------------------------------
    // 5. SALES MANAGER ROLE (user: sales)
    // -------------------------------------------------------------
    console.log('\n--- 5. Sales Manager Role Tests (sales) ---');
    const salesClient = new HttpClient();
    const salesLogin = await salesClient.login('sales', 'pass');
    assert(salesLogin.statusCode === 302 && salesLogin.location.includes('/sales/dashboard'), 'Sales Manager logs in and redirects to /sales/dashboard');

    const salesDash = await salesClient.get('/sales/dashboard');
    assert(salesDash.statusCode === 200, 'Sales dashboard loads successfully');

    // Theme Toggle for Sales
    const salesThemeRes = await salesClient.post('/profile/preferences/toggle');
    assert(JSON.parse(salesThemeRes.body).success === true, 'Sales Manager theme toggle works');

    // Products catalog
    const salesProducts = await salesClient.get('/sales/products');
    assert(salesProducts.statusCode === 200, 'Sales products catalog loads');

    // Add new product
    const addProdRes = await salesClient.post('/sales/products/add', {
        name: 'Executive Polo Navy Gold',
        category: 'Men',
        size: 'L',
        colour: 'Navy Blue',
        price: '2500.00',
        stockQuantity: 100,
        fabricCost: '800.00',
        laborCost: '400.00',
        overheadCost: '300.00'
    });
    assert(addProdRes.statusCode === 302, 'Sales Manager added new product with BOM breakdown');

    // View orders & customers
    const salesOrders = await salesClient.get('/sales/orders');
    assert(salesOrders.statusCode === 200, 'Sales orders management page loads');

    const salesCust = await salesClient.get('/sales/customers');
    assert(salesCust.statusCode === 200, 'Sales customer directory page loads');

    // -------------------------------------------------------------
    // 6. SUPPLIER COORDINATION OFFICER ROLE (user: supplier)
    // -------------------------------------------------------------
    console.log('\n--- 6. Supplier Coordination Officer Role Tests (supplier) ---');
    const supplierClient = new HttpClient();
    const suppLogin = await supplierClient.login('supplier', 'pass');
    assert(suppLogin.statusCode === 302 && suppLogin.location.includes('/supplier/dashboard'), 'Supplier Officer logs in and redirects to /supplier/dashboard');

    const suppDash = await supplierClient.get('/supplier/dashboard');
    assert(suppDash.statusCode === 200, 'Supplier dashboard loads successfully');

    // Theme Toggle for Supplier
    const suppThemeRes = await supplierClient.post('/profile/preferences/toggle');
    assert(JSON.parse(suppThemeRes.body).success === true, 'Supplier theme toggle works');

    // Receive material shipment
    const suppManage = await supplierClient.get('/supplier/manage');
    assert(suppManage.statusCode === 200, 'Supplier manage/intake page loads');

    const receiveRes = await supplierClient.post('/supplier/receive', {
        supplierName: 'TexPro Global Lanka',
        supplierCode: 'SUP-001',
        contactInfo: '0112345678',
        fabricType: 'Single Jersey 100% Cotton',
        colour: 'Emerald Green',
        quantityReceived: 300.0,
        price: '1800.00'
    });
    assert(receiveRes.statusCode === 302, 'Supplier Officer received material shipment and logged PO');

    // Suppliers directory
    const suppDir = await supplierClient.get('/supplier/suppliers');
    assert(suppDir.statusCode === 200, 'Master supplier directory loads');

    // -------------------------------------------------------------
    // 7. ONLINE CUSTOMER ROLE (user: customer)
    // -------------------------------------------------------------
    console.log('\n--- 7. Online Customer Role Tests (customer) ---');
    const customerClient = new HttpClient();
    const custLogin = await customerClient.login('customer', 'pass');
    assert(custLogin.statusCode === 302 && custLogin.location.includes('/customer/shop'), 'Customer logs in and redirects to /customer/shop');

    // Shop catalog with filters
    const shopAll = await customerClient.get('/customer/shop');
    assert(shopAll.statusCode === 200 && shopAll.body.includes('Polo'), 'Customer shop catalog displays products');

    const shopFiltered = await customerClient.get('/customer/shop?category=Men&colour=Red&size=M');
    assert(shopFiltered.statusCode === 200, 'Customer search filter (category=Men, colour=Red, size=M) works');

    // Theme Toggle for Customer
    const custThemeRes = await customerClient.post('/profile/preferences/toggle');
    assert(JSON.parse(custThemeRes.body).success === true, 'Customer theme toggle works');

    // Cart operations: Add to cart
    const addCartRes = await customerClient.post('/customer/cart/add', {
        productId: 1,
        quantity: 2
    });
    assert(addCartRes.statusCode === 302, 'Customer added product to cart');

    const viewCartRes = await customerClient.get('/customer/cart');
    assert(viewCartRes.statusCode === 200, 'Customer cart page loads with item subtotal');

    // Direct Buy Now and Checkout
    const buyNowRes = await customerClient.post('/customer/buy-now', {
        productId: 1,
        quantity: 1
    });
    assert(buyNowRes.statusCode === 302 && buyNowRes.location.includes('/customer/checkout/direct'), 'Customer Buy Now directs immediately to direct checkout');

    // Checkout with Credit/Debit Card
    const checkoutRes = await customerClient.post('/customer/checkout', {
        directCheckout: 'true',
        shippingAddress: 'No 45, Temple Road, Colombo 03',
        phone: '+94 77 123 4567',
        paymentMethod: 'Credit / Debit Card',
        cardNumber: '4532123456789012',
        cardExpiry: '12/28',
        cardCvv: '789'
    });
    assert(checkoutRes.statusCode === 302 && checkoutRes.location.includes('/customer/orders'), 'Customer placed order and was redirected to /customer/orders');

    const ordersPage = await customerClient.get('/customer/orders');
    assert(ordersPage.statusCode === 200 && ordersPage.body.includes('ORD-'), 'Customer orders history lists placed order');

    // -------------------------------------------------------------
    // 8. PROFILE, PREFERENCES & PASSWORD TESTS FOR ALL USERS
    // -------------------------------------------------------------
    console.log('\n--- 8. Profile & Preferences Verification across Roles ---');
    const profRes = await managerClient.get('/profile');
    assert(profRes.statusCode === 200 && profRes.body.includes('Factory Manager'), 'Profile page displays user role accurately');

    const prefFormRes = await managerClient.post('/profile/preferences', {
        darkMode: 'true'
    });
    assert(prefFormRes.statusCode === 302, 'Profile preferences form submission saved theme');

    // SUMMARY
    console.log('\n===============================================================');
    console.log(`   TOTAL TESTS: ${passed + failed} | PASSED: ${passed} | FAILED: ${failed}   `);
    console.log('===============================================================');

    if (failed > 0) {
        process.exit(1);
    }
}

runTests().catch(err => {
    console.error('Test execution encountered error:', err);
    process.exit(1);
});
