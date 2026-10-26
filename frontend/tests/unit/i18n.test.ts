import { describe, expect, it } from 'vitest';
import ar from '@/shared/i18n/messages/ar.json';
import en from '@/shared/i18n/messages/en.json';
import { errorMessageKey, ERROR_CODE_MAP } from '@/shared/i18n/error-messages';

describe('i18n Error Messages and Plural Integrity', () => {
  it('maps all defined error codes to non-empty translations in ar and en', () => {
    const codes = Object.keys(
      ERROR_CODE_MAP
    ) as (keyof typeof ERROR_CODE_MAP)[];

    for (const code of codes) {
      const key = errorMessageKey(code);
      const subKey = key.replace('errors.', '');

      const arMsg = (ar.errors as any)[subKey];
      const enMsg = (en.errors as any)[subKey];

      expect(arMsg).toBeDefined();
      expect(typeof arMsg).toBe('string');
      expect(arMsg.trim().length).toBeGreaterThan(0);

      expect(enMsg).toBeDefined();
      expect(typeof enMsg).toBe('string');
      expect(enMsg.trim().length).toBeGreaterThan(0);
    }
  });

  it('handles unknown error code fallback gracefully', () => {
    expect(errorMessageKey('UNKNOWN_SOMETHING')).toBe('errors.UNKNOWN');
    expect(errorMessageKey(null)).toBe('errors.UNKNOWN');
  });

  it('contains Arabic plural forms with all required ICU categories', () => {
    const arPlural = ar.common.cart.itemsCount;
    expect(arPlural).toContain('=0');
    expect(arPlural).toContain('one');
    expect(arPlural).toContain('two');
    expect(arPlural).toContain('few');
    expect(arPlural).toContain('many');
    expect(arPlural).toContain('other');
  });
});
