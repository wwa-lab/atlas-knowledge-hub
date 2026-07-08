<script setup lang="ts">
import { modelIcon, modelSecretLabel } from '@/domain/viewModels'
import type { ModelSource, ThinkingOption, VueModelDraft } from '@/domain/viewModels'

const props = defineProps<{
  modelDraft: VueModelDraft
  modelProviderOptions: string[]
  thinkingOptions: ThinkingOption[]
  apiKeyInput: string
  modelTestStatus: string
}>()

const emit = defineEmits<{
  close: []
  updateModelDraft: [draft: VueModelDraft]
  updateApiKeyInput: [value: string]
  setModelSource: [source: ModelSource]
  startApiKeyReplace: []
  removeApiKey: []
  confirmApiKeyReplace: []
  cancelApiKeyReplace: []
  toggleModelMultimodal: []
  testModelConnection: []
  saveModelEditor: []
}>()

function updateDraft<K extends keyof VueModelDraft>(key: K, value: VueModelDraft[K]) {
  emit('updateModelDraft', {
    ...props.modelDraft,
    [key]: value
  })
}

function readInputValue(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? target.value : ''
}

function readThinkingFormat(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string'
    ? (target.value as VueModelDraft['thinkingFormat'])
    : 'none'
}
</script>

<template>
  <div class="vue-editor-scrim" @click="emit('close')"></div>
  <aside
    class="vue-model-editor"
    data-testid="vue-model-editor"
    role="dialog"
    aria-modal="true"
    aria-label="编辑模型"
  >
    <header class="vue-editor-head">
      <span class="vue-model-icon">{{ modelIcon(modelDraft.category) }}</span>
      <div>
        <h2>编辑模型</h2>
        <p>配置用于对话的大语言模型</p>
      </div>
    </header>

    <div class="vue-editor-body">
      <section>
        <h3>模型来源</h3>
        <div class="vue-source-toggle">
          <button
            :class="{ active: modelDraft.source === 'Ollama' }"
            type="button"
            @click="emit('setModelSource', 'Ollama')"
          >
            Ollama
          </button>
          <button
            :class="{ active: modelDraft.source === 'API' }"
            type="button"
            @click="emit('setModelSource', 'API')"
          >
            API
          </button>
        </div>
      </section>

      <section>
        <h3>接入配置</h3>
        <label>
          服务商
          <select
            :value="modelDraft.provider"
            data-testid="vue-model-provider"
            @change="updateDraft('provider', readInputValue($event))"
          >
            <option v-for="provider in modelProviderOptions" :key="provider" :value="provider">
              {{ provider }}
            </option>
          </select>
        </label>
        <label class="required">
          模型名称
          <input
            :value="modelDraft.name"
            data-testid="vue-model-name"
            @input="updateDraft('name', readInputValue($event))"
          />
        </label>
        <label>
          显示名称（可选）
          <input
            :value="modelDraft.displayName"
            data-testid="vue-model-display-name"
            @input="updateDraft('displayName', readInputValue($event))"
          />
          <small>仅用于界面展示，实际调用仍使用上面的模型名称。</small>
        </label>
        <label class="required">
          Base URL
          <input
            :value="modelDraft.baseUrl"
            data-testid="vue-model-base-url"
            @input="updateDraft('baseUrl', readInputValue($event))"
          />
        </label>
        <div class="vue-key-field">
          <strong>API Key（可选）</strong>
          <div class="vue-key-row">
            <span
              :class="{ empty: modelDraft.apiKeyStatus !== 'configured' }"
              data-testid="vue-key-status"
            >
              {{ modelSecretLabel(modelDraft) }}
            </span>
            <span>
              <button
                data-testid="vue-key-replace"
                type="button"
                @click="emit('startApiKeyReplace')"
              >
                更换
              </button>
              <button class="danger" type="button" @click="emit('removeApiKey')">移除</button>
            </span>
          </div>
          <div v-if="modelDraft.apiKeyEditing" class="vue-key-editor">
            <input
              :value="apiKeyInput"
              data-testid="vue-key-input"
              type="password"
              autocomplete="off"
              placeholder="输入新的 API Key（不会在原型中保存明文）"
              @input="emit('updateApiKeyInput', readInputValue($event))"
            />
            <button
              data-testid="vue-key-confirm"
              type="button"
              @click="emit('confirmApiKeyReplace')"
            >
              确认
            </button>
            <button type="button" @click="emit('cancelApiKeyReplace')">取消</button>
          </div>
          <small>出于安全考虑，API Key 保存后将不再显示，仅显示配置状态。</small>
        </div>
      </section>

      <section>
        <h3>高级选项</h3>
        <div class="vue-switch-row">
          <strong>支持视觉/多模态</strong>
          <button
            class="vue-switch"
            :class="{ active: modelDraft.supportsMultimodal }"
            type="button"
            @click="emit('toggleModelMultimodal')"
          ></button>
        </div>
        <label>
          思考模式参数格式
          <select
            :value="modelDraft.thinkingFormat"
            data-testid="vue-model-thinking"
            @change="updateDraft('thinkingFormat', readThinkingFormat($event))"
          >
            <option v-for="option in thinkingOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
        </label>
      </section>
    </div>

    <footer class="vue-editor-foot">
      <button
        class="secondary"
        data-testid="vue-test-model"
        type="button"
        @click="emit('testModelConnection')"
      >
        测试连接
      </button>
      <small v-if="modelTestStatus" data-testid="vue-model-test-status">{{
        modelTestStatus
      }}</small>
      <span>
        <button class="secondary" type="button" @click="emit('close')">取消</button>
        <button data-testid="vue-save-model" type="button" @click="emit('saveModelEditor')">
          保存
        </button>
      </span>
    </footer>
  </aside>
</template>
