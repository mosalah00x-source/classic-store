import { expect, test } from '@playwright/test';

test.describe('i18n & Localization', () => {
  test('/ar sets lang=ar, dir=rtl, and localized title', async ({ page }) => {
    await page.goto('/ar');
    const html = page.locator('html');
    await expect(html).toHaveAttribute('lang', 'ar');
    await expect(html).toHaveAttribute('dir', 'rtl');
    await expect(page.locator('[data-testid="page-title"]')).toHaveText(
      'مرحباً بكم في كلاسيك ستور'
    );
  });

  test('/en sets lang=en, dir=ltr, and localized title', async ({ page }) => {
    await page.goto('/en');
    const html = page.locator('html');
    await expect(html).toHaveAttribute('lang', 'en');
    await expect(html).toHaveAttribute('dir', 'ltr');
    await expect(page.locator('[data-testid="page-title"]')).toHaveText(
      'Welcome to ClassicStore'
    );
  });

  test('root / redirects to /ar', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL(/\/ar$/);
  });

  test('unknown locale returns 404', async ({ page }) => {
    const response = await page.goto('/fr');
    expect(response?.status()).toBe(404);
  });

  test('/api/health is untouched by locale routing', async ({ request }) => {
    const response = await request.get('/api/health');
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toEqual({ status: 'ok' });
  });

  test('raw server HTML contains correct lang and dir attributes without JavaScript', async ({
    request,
  }) => {
    const resAr = await request.get('/ar');
    const textAr = await resAr.text();
    expect(textAr).toMatch(/<html[^>]*lang="ar"[^>]*dir="rtl"/);

    const resEn = await request.get('/en');
    const textEn = await resEn.text();
    expect(textEn).toMatch(/<html[^>]*lang="en"[^>]*dir="ltr"/);
  });
});
