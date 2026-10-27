export const KNOWN_ERROR_CODES = [
  'VALIDATION_ERROR',
  'EMAIL_OR_PHONE_REQUIRED',
  'INVALID_CREDENTIALS',
  'INVALID_TOKEN',
  'FORBIDDEN',
  'NOT_FOUND',
  'EMAIL_ALREADY_REGISTERED',
  'PHONE_ALREADY_REGISTERED',
  'BUSINESS_RULE_VIOLATION',
  'NO_ELIGIBLE_WAREHOUSE',
  'RATE_LIMITED',
  'INTERNAL_ERROR',
] as const;

export type KnownErrorCode = (typeof KNOWN_ERROR_CODES)[number];

export type ErrorMessageKey = `errors.${KnownErrorCode | 'UNKNOWN'}`;

export function errorMessageKey(code?: string | null): ErrorMessageKey {
  if (
    code &&
    (KNOWN_ERROR_CODES as readonly string[]).includes(code as KnownErrorCode)
  ) {
    return `errors.${code as KnownErrorCode}`;
  }
  return 'errors.UNKNOWN';
}
