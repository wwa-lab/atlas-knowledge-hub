<script setup lang="ts">
import type {
  ProductSpaceCard,
  SpaceInfoDraft,
  SpaceInfoEditableField,
  SpaceInfoRow
} from '@/domain/viewModels'

const props = defineProps<{
  selectedProductSpace: ProductSpaceCard
  selectedSpaceInfoRows: SpaceInfoRow[]
  activeSpaceInfoEditField: SpaceInfoEditableField
  spaceInfoDraft: SpaceInfoDraft
  spaceInfoStatus: string
}>()

const emit = defineEmits<{
  close: []
  beginSpaceInfoEdit: [field: keyof SpaceInfoDraft]
  cancelSpaceInfoEdit: []
  saveSpaceInfoEdit: []
  updateSpaceInfoDraft: [draft: SpaceInfoDraft]
}>()

function updateDraft<K extends keyof SpaceInfoDraft>(key: K, value: SpaceInfoDraft[K]) {
  emit('updateSpaceInfoDraft', {
    ...props.spaceInfoDraft,
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
</script>

<template>
  <section class="atlas-space-info-panel" data-testid="vue-space-info-panel">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-admin-head" data-testid="vue-admin-panel">
      <div>
        <h1>空间信息</h1>
        <p>查看当前知识空间的详细配置与 mock 运营状态。</p>
      </div>
      <span>{{ selectedProductSpace.source === 'api' ? 'API-backed Space' : 'Mock Space' }}</span>
    </header>

    <section class="atlas-space-info-list" aria-label="空间信息">
      <article
        v-for="row in selectedSpaceInfoRows"
        :key="row.key"
        class="atlas-space-info-row"
        :data-testid="`vue-space-info-${row.key}`"
      >
        <div>
          <strong>{{ row.label }}</strong>
          <p>{{ row.note }}</p>
        </div>

        <div
          v-if="row.field && activeSpaceInfoEditField === row.field"
          class="atlas-space-info-edit"
        >
          <input
            v-if="row.field === 'name'"
            :value="spaceInfoDraft.name"
            data-testid="vue-space-info-name-input"
            aria-label="空间名称"
            @input="updateDraft('name', readInputValue($event))"
          />
          <textarea
            v-else
            :value="spaceInfoDraft.description"
            data-testid="vue-space-info-description-input"
            aria-label="空间描述"
            rows="2"
            @input="updateDraft('description', readInputValue($event))"
          ></textarea>
          <div class="atlas-space-info-actions">
            <button
              type="button"
              data-testid="vue-space-info-save"
              @click="emit('saveSpaceInfoEdit')"
            >
              保存
            </button>
            <button type="button" @click="emit('cancelSpaceInfoEdit')">取消</button>
          </div>
        </div>

        <div v-else class="atlas-space-info-value">
          <span :class="{ 'atlas-space-status': row.key === 'status' }">{{ row.value }}</span>
          <button
            v-if="row.field"
            type="button"
            :aria-label="`编辑${row.label}`"
            :title="`编辑${row.label}`"
            :data-testid="`vue-space-info-edit-${row.field}`"
            @click="emit('beginSpaceInfoEdit', row.field)"
          >
            ✎
          </button>
        </div>
      </article>
    </section>

    <p
      v-if="spaceInfoStatus"
      class="atlas-inline-success"
      data-testid="vue-space-info-save-status"
      role="status"
    >
      {{ spaceInfoStatus }}
    </p>

    <section class="atlas-admin-boundary">
      <strong>Production boundary</strong>
      <p>
        空间信息当前只更新 Vue mock 会话状态；真实空间元数据、存储额度、审计日志和权限校验必须由后端
        API 与 RBAC 控制。
      </p>
    </section>
  </section>
</template>
