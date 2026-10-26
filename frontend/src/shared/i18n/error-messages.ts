export const ERROR_CODE_MAP = {
  VALIDATION_ERROR: 'errors.VALIDATION_ERROR',
  EMAIL_OR_PHONE_REQUIRED: 'errors.EMAIL_OR_PHONE_REQUIRED',
  INVALID_CREDENTIALS: 'errors.INVALID_CREDENTIALS',
  INVALID_TOKEN: 'errors.INVALID_TOKEN',
  FORBIDDEN: 'errors.FORBIDDEN',
  NOT_FOUND: 'errors.NOT_FOUND',
  EMAIL_ALREADY_REGISTERED: 'errors.EMAIL_ALREADY_REGISTERED',
  PHONE_ALREADY_REGISTERED: 'errors.PHONE_ALREADY_REGISTERED',
  BUSINESS_RULE_VIOLATION: 'errors.BUSINESS_RULE_VIOLATION',
  NO_ELIGIBLE_WAREHOUSE: 'errors.NO_ELIGIBLE_WAREHOUSE',
  RATE_LIMITED: 'errors.RATE_LIMITED',
  INTERNAL_ERROR: 'errors.INTERNAL_ERROR',
  UNKNOWN: 'errors.UNKNOWN',
} as const;

export type ApiErrorCode = keyof typeof ERROR_CODE_MAP;

export type ErrorMessageKey = (typeof ERROR_CODE_MAP)[ApiErrorCode];

/**
 * Returns the typed i18n message key corresponding to an API error code.
 * Falls back to 'errors.UNKNOWN' if the code is unknown or missing.
 */
export function errorMessageKey(code?: string | null): ErrorMessageKey {
  if (code && code in ERROR_CODE_MAP) {
    return ERROR_CODE_MAP[code as ApiErrorCode];
  }
  return ERROR_CODE_MAP.UNKNOWN;
}
