<script setup lang="ts">
import type { ApiInfoState, SafeErrorPreview } from '@/domain/viewModels'

defineProps<{
  apiInfo: ApiInfoState
  apiKeyDisplayValue: string
  safeErrorPreviews: SafeErrorPreview[]
}>()

const emit = defineEmits<{
  close: []
  toggleApiKeyVisibility: []
  copyApiInfoValue: [successMessage: string]
  refreshApiKey: []
  openApiDocumentation: []
}>()
</script>

<template>
  <section class="atlas-api-info-panel" data-testid="vue-api-info-panel">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-admin-head" data-testid="vue-admin-panel">
      <div>
        <h1>API 信息</h1>
        <p>查看和管理 Atlas API 调用信息，密钥默认为脱敏显示。</p>
      </div>
      <span>Mock-safe API</span>
    </header>

    <section class="atlas-api-info-list">
      <article class="atlas-api-info-row">
        <div>
          <strong>API Key</strong>
          <p>用于 API 调用的密钥，请妥善保管；当前仅展示 mock key 状态。</p>
        </div>
        <div class="atlas-api-control">
          <input
            :key="apiKeyDisplayValue"
            :value="apiKeyDisplayValue"
            data-testid="vue-api-key-value"
            readonly
            aria-label="API Key"
          />
          <div class="atlas-api-icon-row">
            <button
              data-testid="vue-api-key-reveal"
              type="button"
              aria-label="确认 API Key 保持隐藏"
              title="确认 API Key 保持隐藏"
              @click="emit('toggleApiKeyVisibility')"
            >
              ◉
            </button>
            <button
              data-testid="vue-api-key-copy"
              type="button"
              aria-label="复制 API Key"
              title="复制 API Key"
              @click="emit('copyApiInfoValue', 'API Key 已复制')"
            >
              ⧉
            </button>
            <button
              data-testid="vue-api-key-refresh"
              type="button"
              aria-label="刷新 API Key"
              title="刷新 API Key"
              @click="emit('refreshApiKey')"
            >
              ↻
            </button>
          </div>
        </div>
      </article>

      <article class="atlas-api-info-row">
        <div>
          <strong>API 地址</strong>
          <p>REST API 的基础路径，请求时在末尾拼接具体接口路径。</p>
        </div>
        <div class="atlas-api-control">
          <input
            :value="apiInfo.baseUrl"
            data-testid="vue-api-base-url"
            readonly
            aria-label="API 地址"
          />
          <div class="atlas-api-icon-row">
            <button
              data-testid="vue-api-base-copy"
              type="button"
              aria-label="复制 API 地址"
              title="复制 API 地址"
              @click="emit('copyApiInfoValue', 'API 地址已复制')"
            >
              ⧉
            </button>
          </div>
        </div>
      </article>

      <article class="atlas-api-info-row">
        <div>
          <strong>API 文档</strong>
          <p>查看完整的 API 调用文档和示例。</p>
        </div>
        <div class="atlas-api-doc-actions">
          <a
            :href="apiInfo.docsPath"
            data-testid="vue-api-doc-link"
            @click.prevent="emit('openApiDocumentation')"
          >
            打开文档 ↗
          </a>
        </div>
      </article>
    </section>

    <section class="atlas-api-safe-errors" data-testid="vue-safe-error-states">
      <header>
        <strong>Safe Error States</strong>
        <span>Atlas API contract</span>
      </header>
      <div>
        <article
          v-for="state in safeErrorPreviews"
          :key="state.code"
          :data-testid="`vue-safe-error-state-${state.code}`"
        >
          <strong>{{ state.code }}</strong>
          <span>{{ state.title }}</span>
          <p>{{ state.description }}</p>
        </article>
      </div>
    </section>

    <p
      v-if="apiInfo.status"
      class="atlas-inline-success"
      data-testid="vue-api-info-status"
      role="status"
    >
      {{ apiInfo.status }}
    </p>

    <section class="atlas-admin-boundary">
      <strong>Production boundary</strong>
      <p>
        API Key 仍是前端 mock 信息；真实密钥必须由后端生成、脱敏返回并审计刷新。
        当前界面不会连接外部 provider、不会保存明文 token，也不会展示公司内网 endpoint。
      </p>
    </section>
  </section>
</template>
