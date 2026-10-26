import { describe, expect, it } from 'vitest';
import {
  formatDate,
  formatDateTime,
  formatDiscountPercent,
  formatMoney,
  formatNumber,
} from './index';

describe('Format Utilities', () => {
  describe('formatMoney', () => {
    it('formats valid money string in Arabic locale without floating point loss', () => {
      const result = formatMoney('950.00', 'EGP', 'ar');
      expect(result).toContain('950.00');
      expect(result).toContain('ج.م');
    });

    it('formats valid money string in English locale', () => {
      const result = formatMoney('950.00', 'EGP', 'en');
      expect(result).toContain('950.00');
      expect(result).toContain('EGP');
    });

    it('formats large money values correctly with grouping separators', () => {
      const result = formatMoney('1234567.50', 'EGP', 'en');
      expect(result).toContain('1,234,567.50');
    });

    it('throws error in development for invalid amount strings (missing decimals or numbers)', () => {
      expect(() => formatMoney('950', 'EGP', 'ar')).toThrow(
        /Invalid amount format/
      );
      expect(() => formatMoney('950.5', 'EGP', 'ar')).toThrow(
        /Invalid amount format/
      );
      expect(() => formatMoney('abc', 'EGP', 'ar')).toThrow(
        /Invalid amount format/
      );
    });

    it('returns fallback string in production for invalid amount strings', () => {
      const originalEnv = process.env.NODE_ENV;
      process.env.NODE_ENV = 'production';
      try {
        expect(formatMoney('invalid', 'EGP', 'ar')).toBe('invalid EGP');
      } finally {
        process.env.NODE_ENV = originalEnv;
      }
    });
  });

  describe('formatDiscountPercent', () => {
    it('formats discount percentage correctly', () => {
      expect(formatDiscountPercent(20, 'en')).toBe('20%');
      expect(formatDiscountPercent('15', 'ar')).toContain('15');
    });
  });

  describe('formatNumber', () => {
    it('formats numbers with Latin digits in both Arabic and English', () => {
      expect(formatNumber(12345, 'ar')).toBe('12,345');
      expect(formatNumber(12345, 'en')).toBe('12,345');
    });
  });

  describe('formatDate & formatDateTime', () => {
    it('formats ISO-8601 date string in Africa/Cairo timezone', () => {
      // 2026-03-30T23:30:00Z is 2026-03-31T01:30:00 Cairo time (UTC+2)
      const dateStr = '2026-03-30T23:30:00Z';
      const formattedDate = formatDate(dateStr, 'en');
      expect(formattedDate).toContain('Mar 31, 2026');
    });

    it('formats ISO-8601 datetime string in Africa/Cairo timezone', () => {
      const dateStr = '2026-03-30T15:30:00Z';
      const formattedDateTime = formatDateTime(dateStr, 'en');
      expect(formattedDateTime).toContain('Mar 30, 2026');
      expect(formattedDateTime).toContain('5:30');
    });
  });
});
