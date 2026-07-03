declare module 'jsdom' {
  export class JSDOM {
    window: Window & typeof globalThis
    constructor(html?: string, options?: Record<string, unknown>)
  }
}

interface ImportMetaEnv {
  readonly VITE_ATLAS_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
