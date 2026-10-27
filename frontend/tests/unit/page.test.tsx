import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import HomePage from '@/app/[locale]/page';
import '@testing-library/jest-dom';

vi.mock('next-intl/server', () => ({
  setRequestLocale: vi.fn(),
  getTranslations: vi.fn().mockImplementation(async (ns: string) => {
    return (key: string) => {
      if (ns === 'home') {
        if (key === 'title') return 'أهلاً بك في كلاسيك ستور';
        if (key === 'tagline') return 'وجهتك الأولى للتسوق الإلكتروني الأنيق في مصر';
      }
      return key;
    };
  }),
}));

vi.mock('next-intl', () => ({
  NextIntlClientProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  useTranslations: () => (key: string) => key,
}));

describe('HomePage Localized Smoke Test', () => {
  it('renders translated home title', async () => {
    const pageComponent = await HomePage({
      params: Promise.resolve({ locale: 'ar' }),
    });
    render(pageComponent);
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(
      'أهلاً بك في كلاسيك ستور'
    );
  });
});
