import { test, expect } from '@playwright/test';

test.describe('Smoke Tests', () => {
  test('loads home page and checks title and heading', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/كلاسيك ستور|Classic Store/);
    await expect(page.locator('h1')).toHaveText(
      /أهلاً بك في كلاسيك ستور|Welcome to Classic Store/
    );

    const htmlElement = page.locator('html');
    await expect(htmlElement).toHaveAttribute('lang', 'ar');
    await expect(htmlElement).toHaveAttribute('dir', 'rtl');
  });

  test('loads health check API endpoint', async ({ request }) => {
    const response = await request.get('/api/health');
    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body).toEqual({ status: 'ok' });
    expect(response.headers()['cache-control']).toContain('no-store');
  });
});
