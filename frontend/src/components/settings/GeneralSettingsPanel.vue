<script setup lang="ts">
import type {
  GeneralCodeFont,
  GeneralFontSize,
  GeneralInterfaceFont,
  GeneralLanguage,
  GeneralOption,
  GeneralSettingsState,
  GeneralThemeMode
} from '@/domain/viewModels'

const props = defineProps<{
  generalSettings: GeneralSettingsState
  languageOptions: Array<GeneralOption<GeneralLanguage>>
  themeOptions: Array<GeneralOption<GeneralThemeMode>>
  interfaceFontOptions: Array<GeneralOption<GeneralInterfaceFont>>
  codeFontOptions: Array<GeneralOption<GeneralCodeFont>>
  fontSizeOptions: Array<GeneralOption<GeneralFontSize>>
}>()

const emit = defineEmits<{
  close: []
  updateGeneralSettings: [settings: GeneralSettingsState]
}>()

function updateSettings<K extends keyof GeneralSettingsState>(
  key: K,
  value: GeneralSettingsState[K]
) {
  emit('updateGeneralSettings', {
    ...props.generalSettings,
    [key]: value
  })
}

function readSelectValue<T extends string>(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? (target.value as T) : ('' as T)
}
</script>

<template>
  <section class="atlas-general-settings">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-general-head" data-testid="vue-admin-panel">
      <h1>常规设置</h1>
      <p>配置语言、外观等基础选项</p>
    </header>

    <div class="atlas-general-form">
      <section class="atlas-general-row">
        <div>
          <h2>语言</h2>
          <p>选择界面显示语言</p>
        </div>
        <select
          :value="generalSettings.language"
          data-testid="vue-general-language"
          aria-label="语言"
          @change="updateSettings('language', readSelectValue<GeneralLanguage>($event))"
        >
          <option v-for="option in languageOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </section>

      <section class="atlas-general-row">
        <div>
          <h2>主题模式</h2>
          <p>选择界面的显示主题，支持跟随系统自动切换</p>
        </div>
        <select
          :value="generalSettings.themeMode"
          data-testid="vue-general-theme"
          aria-label="主题模式"
          @change="updateSettings('themeMode', readSelectValue<GeneralThemeMode>($event))"
        >
          <option v-for="option in themeOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </section>

      <section class="atlas-general-row">
        <div>
          <h2>界面字体</h2>
          <p>用于菜单、正文、按钮等界面大部分文字的字体</p>
        </div>
        <div class="atlas-general-control-stack">
          <select
            :value="generalSettings.interfaceFont"
            data-testid="vue-general-interface-font"
            aria-label="界面字体"
            @change="updateSettings('interfaceFont', readSelectValue<GeneralInterfaceFont>($event))"
          >
            <option
              v-for="option in interfaceFontOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </option>
          </select>
          <p class="atlas-font-preview">示例 Sample 字体 Font - Aa Gg Oo 0123</p>
        </div>
      </section>

      <section class="atlas-general-row">
        <div>
          <h2>代码字体</h2>
          <p>用于代码块、终端命令、API 密钥、文件路径等技术内容</p>
        </div>
        <div class="atlas-general-control-stack">
          <select
            :value="generalSettings.codeFont"
            data-testid="vue-general-code-font"
            aria-label="代码字体"
            @change="updateSettings('codeFont', readSelectValue<GeneralCodeFont>($event))"
          >
            <option v-for="option in codeFontOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
          <code class="atlas-code-preview">const source_trace = 'chunk-001'</code>
        </div>
      </section>

      <section class="atlas-general-row">
        <div>
          <h2>字体大小</h2>
          <p>整体缩放界面文字、图标、间距等</p>
        </div>
        <div class="atlas-size-segment" role="group" aria-label="字体大小">
          <button
            v-for="option in fontSizeOptions"
            :key="option.value"
            :class="{ active: generalSettings.fontSize === option.value }"
            type="button"
            :data-testid="`vue-general-font-size-${option.value}`"
            @click="updateSettings('fontSize', option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </section>

      <section class="atlas-general-row">
        <div>
          <h2>开启记忆功能</h2>
          <p>开启后，系统将记录对话历史，并在后续对话中自动回忆相关内容</p>
        </div>
        <button
          class="atlas-switch"
          :class="{ active: generalSettings.memoryEnabled }"
          type="button"
          role="switch"
          :aria-checked="generalSettings.memoryEnabled"
          data-testid="vue-general-memory"
          @click="updateSettings('memoryEnabled', !generalSettings.memoryEnabled)"
        >
          <span></span>
        </button>
      </section>
    </div>

    <section class="atlas-admin-boundary atlas-general-boundary">
      <strong>Production boundary</strong>
      <p>
        常规偏好仅作用于当前 Vue mock 会话；不写入真实账号配置、不执行生产 RBAC、
        不保存真实密钥，也不会展示私有 endpoint 或本地绝对路径。
      </p>
    </section>
  </section>
</template>
