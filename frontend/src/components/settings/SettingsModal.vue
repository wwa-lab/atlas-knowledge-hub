<script setup lang="ts">
import type { SettingsPanel } from '@/domain/viewModels'

defineProps<{
  settingsPanel: SettingsPanel
  canReadGovernance: boolean
}>()

const emit = defineEmits<{
  close: []
  selectPanel: [panel: SettingsPanel]
  openPanel: [panel: SettingsPanel]
}>()
</script>

<template>
  <section class="atlas-settings-modal" role="dialog" aria-modal="true">
    <div class="atlas-settings-window">
      <aside class="vue-settings-rail" aria-label="Settings navigation">
        <h2>设置</h2>
        <p>账户</p>
        <button
          :class="{ active: settingsPanel === 'general' }"
          type="button"
          @click="emit('selectPanel', 'general')"
        >
          常规设置
        </button>
        <button
          :class="{ active: settingsPanel === 'profile' }"
          type="button"
          @click="emit('selectPanel', 'profile')"
        >
          用户信息
        </button>
        <button
          :class="{ active: settingsPanel === 'api' }"
          type="button"
          @click="emit('selectPanel', 'api')"
        >
          API 信息
        </button>
        <p>空间</p>
        <button
          :class="{ active: settingsPanel === 'spaceInfo' }"
          type="button"
          @click="emit('selectPanel', 'spaceInfo')"
        >
          空间信息
        </button>
        <button
          :class="{ active: settingsPanel === 'members' }"
          type="button"
          @click="emit('selectPanel', 'members')"
        >
          成员管理
        </button>
        <button
          v-if="canReadGovernance"
          :class="{ active: settingsPanel === 'audit' }"
          type="button"
          @click="emit('openPanel', 'audit')"
        >
          审计日志
        </button>
        <button
          :class="{ active: settingsPanel === 'messages' }"
          type="button"
          @click="emit('selectPanel', 'messages')"
        >
          消息管理
        </button>
        <button
          :class="{ active: settingsPanel === 'registration' }"
          type="button"
          @click="emit('selectPanel', 'registration')"
        >
          注册配置
        </button>
        <p>模型</p>
        <button
          :class="{ active: settingsPanel === 'models' }"
          type="button"
          @click="emit('selectPanel', 'models')"
        >
          模型管理
        </button>
        <p>数据与扩展</p>
        <button
          :class="{ active: settingsPanel === 'vector' }"
          type="button"
          @click="emit('selectPanel', 'vector')"
        >
          向量数据库引擎
        </button>
        <button
          :class="{ active: settingsPanel === 'parser' }"
          type="button"
          @click="emit('selectPanel', 'parser')"
        >
          解析引擎
        </button>
        <button
          :class="{ active: settingsPanel === 'storage' }"
          type="button"
          @click="emit('selectPanel', 'storage')"
        >
          存储引擎
        </button>
      </aside>

      <slot />
    </div>
  </section>
</template>
