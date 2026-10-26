import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { z } from 'zod';

const envSchema = z.object({
  API_BASE_URL: z.string().url('API_BASE_URL must be a valid URL'),
  SESSION_COOKIE_NAME: z.string().min(1, 'SESSION_COOKIE_NAME is required'),
  NEXT_PUBLIC_SITE_URL: z
    .string()
    .url('NEXT_PUBLIC_SITE_URL must be a valid URL'),
  IMAGE_HOSTS: z.string().optional().default(''),
});

describe('Environment Variables Schema Unit Tests', () => {
  const originalEnv = process.env;

  beforeEach(() => {
    vi.resetModules();
    process.env = { ...originalEnv };
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  it('validates correct environment variables', () => {
    const validConfig = {
      API_BASE_URL: 'http://localhost:8080/api/v1',
      SESSION_COOKIE_NAME: 'classicstore_session',
      NEXT_PUBLIC_SITE_URL: 'http://localhost:3000',
      IMAGE_HOSTS: 'cdn.example.com',
    };

    const parsed = envSchema.safeParse(validConfig);
    expect(parsed.success).toBe(true);
    if (parsed.success) {
      expect(parsed.data.API_BASE_URL).toBe('http://localhost:8080/api/v1');
      expect(parsed.data.SESSION_COOKIE_NAME).toBe('classicstore_session');
      expect(parsed.data.NEXT_PUBLIC_SITE_URL).toBe('http://localhost:3000');
      expect(parsed.data.IMAGE_HOSTS).toBe('cdn.example.com');
    }
  });

  it('fails fast on invalid API_BASE_URL', () => {
    const invalidConfig = {
      API_BASE_URL: 'not-a-url',
      SESSION_COOKIE_NAME: 'classicstore_session',
      NEXT_PUBLIC_SITE_URL: 'http://localhost:3000',
    };

    const parsed = envSchema.safeParse(invalidConfig);
    expect(parsed.success).toBe(false);
  });

  it('fails fast on missing SESSION_COOKIE_NAME', () => {
    const invalidConfig = {
      API_BASE_URL: 'http://localhost:8080/api/v1',
      SESSION_COOKIE_NAME: '',
      NEXT_PUBLIC_SITE_URL: 'http://localhost:3000',
    };

    const parsed = envSchema.safeParse(invalidConfig);
    expect(parsed.success).toBe(false);
  });

  it('prevents client-side imports via server-only package', async () => {
    process.env.API_BASE_URL = 'http://localhost:8080/api/v1';
    process.env.SESSION_COOKIE_NAME = 'classicstore_session';
    process.env.NEXT_PUBLIC_SITE_URL = 'http://localhost:3000';
    process.env.IMAGE_HOSTS = '';

    await expect(import('@/shared/config/env')).rejects.toThrow(
      'This module cannot be imported from a Client Component module'
    );
  });
});
