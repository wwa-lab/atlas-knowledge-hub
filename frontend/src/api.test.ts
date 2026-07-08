import { afterEach, describe, expect, it, vi } from 'vitest'

describe('api base URL configuration', () => {
  afterEach(() => {
    vi.unstubAllEnvs()
    vi.resetModules()
  })

  it('normalizes an explicit full-stack API base URL', async () => {
    vi.stubEnv('VITE_ATLAS_API_BASE_URL', 'http://127.0.0.1:8080/')
    vi.resetModules()

    const api = await import('./api')

    expect(api.apiBaseUrl()).toBe('http://127.0.0.1:8080')
  })

  it('supports explicit mock mode without a backend fetch', async () => {
    vi.stubEnv('VITE_ATLAS_API_BASE_URL', 'mock')
    vi.resetModules()

    const api = await import('./api')

    expect(api.apiBaseUrl()).toBe('mock')
    await expect(api.listSpaces()).rejects.toMatchObject({
      code: 'SAFE_SYSTEM_ERROR',
      message: 'Atlas API disabled for mock dev mode.',
      status: 503
    })
  })
})
