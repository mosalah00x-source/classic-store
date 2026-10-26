import 'server-only';
import { z } from 'zod';

const envSchema = z.object({
  API_BASE_URL: z.string().url('API_BASE_URL must be a valid URL'),
  SESSION_COOKIE_NAME: z.string().min(1, 'SESSION_COOKIE_NAME is required'),
  NEXT_PUBLIC_SITE_URL: z
    .string()
    .url('NEXT_PUBLIC_SITE_URL must be a valid URL'),
  IMAGE_HOSTS: z.string().optional().default(''),
});

const _env = envSchema.safeParse(process.env);

if (!_env.success) {
  const formattedErrors = JSON.stringify(_env.error.format(), null, 2);
  console.error('Invalid environment variables:\n', formattedErrors);
  throw new Error('Invalid environment variables configured.');
}

export const env = _env.data;
