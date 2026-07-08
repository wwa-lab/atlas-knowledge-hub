<script setup lang="ts">
import type { SettingsSurface } from '@/domain/viewModels'

defineProps<{
  settingsSurface: SettingsSurface
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <section class="atlas-settings-placeholder">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-admin-head" data-testid="vue-admin-panel">
      <div>
        <h1>{{ settingsSurface.title }}</h1>
        <p>{{ settingsSurface.summary }}</p>
      </div>
      <span>{{ settingsSurface.status }}</span>
    </header>
    <section class="atlas-admin-grid">
      <article v-for="row in settingsSurface.rows" :key="row.label">
        <strong>{{ row.label }}</strong>
        <span>{{ row.value }}</span>
        <p>{{ row.note }}</p>
      </article>
    </section>
    <section class="atlas-admin-boundary">
      <strong>Production boundary</strong>
      <p>
        当前面板只展示 mock-safe 配置状态；不会保存真实密钥、不会调用外部 provider、 不执行生产
        RBAC，也不会展示私有 endpoint 或本地绝对路径。
      </p>
    </section>
  </section>
</template>
