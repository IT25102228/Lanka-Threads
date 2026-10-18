const { test, expect } = require('@playwright/test');

test.describe('Lanka Threads Auth & Support Flow', () => {

  test('Verify Login Page Loads and Theme Toggle Works', async ({ page }) => {
    await page.goto('/login');
    await expect(page).toHaveTitle(/Login - Lanka Threads/i);

    // Get initial background class on html tag
    const htmlElement = page.locator('html');
    const hasInitialLight = await htmlElement.evaluate((el) => el.classList.contains('light-mode'));

    // Find theme toggle button
    const themeBtn = page.locator('button[onclick*="toggleLoginTheme"]');
    await themeBtn.click();
    await page.waitForTimeout(500); // Allow transition animation

    // Expect class list to be toggled
    const isLightAfterClick = await htmlElement.evaluate((el) => el.classList.contains('light-mode'));
    expect(isLightAfterClick).not.toBe(hasInitialLight);
  });

  test('User Registration and Login Flow', async ({ page }) => {
    const timestamp = Date.now();
    const testUsername = `user_${timestamp}`;
    const testEmail = `user_${timestamp}@test.com`;

    // Go to registration page
    await page.goto('/register');
    await page.fill('input[name="name"]', 'QA Test Customer');
    await page.fill('input[name="email"]', testEmail);
    await page.fill('input[name="username"]', testUsername);
    await page.fill('input[name="password"]', 'password123');
    await page.selectOption('select[name="role"]', 'CUSTOMER');

    // Submit form
    await page.click('button[type="submit"]');

    // Registration should redirect back to login page with registered query parameter
    await page.waitForURL('**/login?registered');
    
    // Check if the registered message is displayed
    const successMsg = page.locator('div[th\\:if*="registered"], :text("Account created successfully")');
    await expect(successMsg).toBeVisible();

    // Attempt to login using the registered user credentials
    await page.fill('input[name="username"]', testUsername);
    await page.fill('input[name="password"]', 'password123');
    await page.click('button[type="submit"]');

    // Successful login of customer role redirects to /customer/shop
    await page.waitForURL('**/customer/shop');
    await expect(page).toHaveTitle(/Shop - Lanka Threads/i);
  });

  test('Support Form Submission Validation', async ({ page }) => {
    await page.goto('/support');
    
    // Fill the support ticket form fields
    await page.fill('input[name="name"]', 'John Customer Support');
    await page.fill('input[name="email"]', 'john_support@test.com');
    await page.selectOption('select[name="subject"]', 'General Inquiry');
    await page.fill('textarea[name="message"]', 'Hello, do you offer customized logo printing on Polo T-shirts?');

    // Submit support form
    await page.click('button[type="submit"]');

    // Submission should redirect back to support page and present success message
    await page.waitForURL('**/support');
    const successAlert = page.locator('div[th\\:if*="successMessage"], :text("Your support ticket")');
    await expect(successAlert).toBeVisible();
  });
});
