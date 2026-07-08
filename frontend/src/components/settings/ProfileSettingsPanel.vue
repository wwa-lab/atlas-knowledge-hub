<script setup lang="ts">
import type { AccountProfileRow } from '@/domain/viewModels'

defineProps<{
  accountProfileRows: AccountProfileRow[]
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <section class="atlas-account-settings" data-testid="vue-user-info-panel">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-admin-head" data-testid="vue-admin-panel">
      <div>
        <h1>用户信息</h1>
        <p>查看当前 mock 账号资料、空间身份和最近活动。</p>
      </div>
      <span>Mock account</span>
    </header>

    <section class="atlas-profile-card">
      <span class="atlas-profile-avatar" aria-hidden="true">L</span>
      <div>
        <strong>leo</strong>
        <p>Atlas Delivery · Workspace Owner</p>
      </div>
    </section>

    <section class="atlas-admin-grid">
      <article v-for="row in accountProfileRows" :key="row.key" :data-testid="row.testId">
        <strong>{{ row.label }}</strong>
        <span>{{ row.value }}</span>
        <p>{{ row.note }}</p>
      </article>
    </section>

    <section class="atlas-admin-boundary">
      <strong>Production boundary</strong>
      <p>
        用户信息只用于当前 Vue mock 演示；不连接真实身份系统、不保存个人资料、
        不展示真实组织目录，也不执行生产 RBAC。
      </p>
    </section>
  </section>
</template>
