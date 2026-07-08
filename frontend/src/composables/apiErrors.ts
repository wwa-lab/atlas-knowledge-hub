import { ApiError } from '@/api'

export function safeApiErrorMessage(error: ApiError) {
  const reference = error.correlationId ? ` Reference ${error.correlationId}.` : ''
  const retryAfter =
    error.code === 'RATE_LIMITED' && error.retryAfterSeconds != null
      ? ` Retry after ${error.retryAfterSeconds}s.`
      : ''
  return `${error.message}${retryAfter}${reference}`
}

export function safeError(error: unknown, fallback: string) {
  if (error instanceof ApiError) {
    return safeApiErrorMessage(error)
  }
  return error instanceof Error ? error.message : fallback
}

export function isPermissionDeniedError(error: unknown, message: string) {
  if (error instanceof ApiError) {
    return error.code === 'PERMISSION_DENIED' || error.status === 403
  }
  return message.toLowerCase().includes('permission denied')
}
