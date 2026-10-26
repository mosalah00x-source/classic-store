import { describe, expect, it, vi } from 'vitest';
import { pickLocalized } from './pick-localized';

describe('pickLocalized', () => {
  const sampleProduct = {
    titleAr: 'قميص قطني',
    titleEn: 'Cotton Shirt',
    descriptionAr: 'وصف المنتج',
    descriptionEn: '',
  };

  it('returns requested locale value when available', () => {
    expect(pickLocalized(sampleProduct, 'title', 'ar')).toBe('قميص قطني');
    expect(pickLocalized(sampleProduct, 'title', 'en')).toBe('Cotton Shirt');
  });

  it('falls back to alternate locale when primary locale value is empty string', () => {
    expect(pickLocalized(sampleProduct, 'description', 'en')).toBe(
      'وصف المنتج'
    );
  });

  it('throws error in dev/test environment when both localized fields are missing or empty', () => {
    const emptyEntity = { titleAr: '', titleEn: '  ' };
    expect(() => pickLocalized(emptyEntity, 'title', 'ar')).toThrow(
      /Both localized fields/
    );
  });

  it('logs console.error and returns empty string in production when both are missing', () => {
    const originalEnv = process.env.NODE_ENV;
    process.env.NODE_ENV = 'production';
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    try {
      const emptyEntity = { titleAr: '', titleEn: '' };
      expect(pickLocalized(emptyEntity, 'title', 'ar')).toBe('');
      expect(consoleSpy).toHaveBeenCalledWith(
        expect.stringContaining('Missing localized text for baseKey "title"')
      );
    } finally {
      process.env.NODE_ENV = originalEnv;
      consoleSpy.mockRestore();
    }
  });
});
