import { describe, expect, it, vi } from 'vitest';
import {
  formatMoney,
  formatDiscountPercent,
  formatNumber,
  formatDate,
  formatDateTime,
  pickLocalized,
} from '@/shared/lib/format';
import { errorMessageKey, KNOWN_ERROR_CODES } from '@/shared/i18n/error-codes';

describe('formatMoney Utility', () => {
  it('formats monetary string correctly in Arabic locale', () => {
    const result = formatMoney('712.50', 'EGP', 'ar');
    expect(result).toContain('712.50');
    expect(result).toContain('EGP');
  });

  it('formats monetary string correctly in English locale', () => {
    const result = formatMoney('712.50', 'EGP', 'en');
    expect(result).toBe('EGP 712.50');
  });

  it('handles large monetary amounts without rounding or precision loss', () => {
    const result = formatMoney('98765432101234.56', 'EGP', 'en');
    expect(result).toContain('98,765,432,101,234.56');
  });

  it('throws in dev/test environment for invalid money strings', () => {
    expect(() => formatMoney('712.5')).toThrow(
      '[formatMoney] Invalid money amount string'
    );
    expect(() => formatMoney('100')).toThrow(
      '[formatMoney] Invalid money amount string'
    );
    expect(() => formatMoney('abc')).toThrow(
      '[formatMoney] Invalid money amount string'
    );
  });

  it('renders safe fallback in production environment for invalid inputs', () => {
    vi.stubEnv('NODE_ENV', 'production');
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    try {
      const result = formatMoney('invalid', 'EGP', 'ar');
      expect(result).toBe('0.00 EGP');
      expect(consoleSpy).toHaveBeenCalled();
    } finally {
      vi.unstubAllEnvs();
      consoleSpy.mockRestore();
    }
  });
});

describe('formatDate & formatDateTime Utilities', () => {
  it('formats date using Africa/Cairo timezone (UTC midnight edge case)', () => {
    // 2026-03-30T23:30:00Z in UTC is 2026-03-31 01:30:00 in Africa/Cairo (UTC+2)
    const dateFormattedEn = formatDate('2026-03-30T23:30:00Z', 'en');
    expect(dateFormattedEn).toContain('March 31, 2026');

    const dateTimeFormattedEn = formatDateTime('2026-03-30T23:30:00Z', 'en');
    expect(dateTimeFormattedEn).toContain('March 31, 2026');
    expect(dateTimeFormattedEn).toContain('01:30');
  });

  it('throws in dev/test for invalid date strings', () => {
    expect(() => formatDate('not-a-date', 'en')).toThrow(
      '[formatDate] Invalid ISO date string'
    );
  });
});

describe('pickLocalized Helper', () => {
  const camelEntity = {
    titleAr: 'قميص قطني',
    titleEn: 'Cotton Shirt',
    descriptionAr: '',
    descriptionEn: 'Shirt description',
  };

  const snakeEntity = {
    name_ar: 'تصنيف رئيسي',
    name_en: 'Main Category',
  };

  it('returns primary locale value when present', () => {
    expect(pickLocalized(camelEntity, 'title', 'ar')).toBe('قميص قطني');
    expect(pickLocalized(camelEntity, 'title', 'en')).toBe('Cotton Shirt');
    expect(pickLocalized(snakeEntity, 'name', 'ar')).toBe('تصنيف رئيسي');
  });

  it('falls back to alternate locale when primary locale value is empty', () => {
    expect(pickLocalized(camelEntity, 'description', 'ar')).toBe(
      'Shirt description'
    );
  });

  it('throws in dev/test when both localized values are missing', () => {
    const emptyEntity = { titleAr: '', titleEn: '' };
    expect(() => pickLocalized(emptyEntity, 'title', 'ar')).toThrow(
      '[pickLocalized] Missing localized values'
    );
  });
});

describe('errorMessageKey Utility', () => {
  it('maps all spec and handoff error codes correctly', () => {
    KNOWN_ERROR_CODES.forEach((code) => {
      expect(errorMessageKey(code)).toBe(`errors.${code}`);
    });
  });

  it('returns errors.UNKNOWN fallback for invalid or unmapped code', () => {
    expect(errorMessageKey('SOMETHING_RANDOM')).toBe('errors.UNKNOWN');
    expect(errorMessageKey(null)).toBe('errors.UNKNOWN');
  });
});

describe('formatNumber & formatDiscountPercent Utilities', () => {
  it('formats percentage correctly', () => {
    expect(formatDiscountPercent(15, 'ar')).toBe('%15');
    expect(formatDiscountPercent(15, 'en')).toBe('15%');
  });

  it('formats numbers with Latin digits', () => {
    expect(formatNumber(1250, 'ar')).toBe('1,250');
    expect(formatNumber(1250, 'en')).toBe('1,250');
  });
});
