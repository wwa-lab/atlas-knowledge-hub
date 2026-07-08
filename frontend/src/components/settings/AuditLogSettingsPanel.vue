<script setup lang="ts">
import { auditMetadataLabel } from '@/domain/viewModels'
import type { AuditSummary, ProductSpaceCard } from '@/domain/viewModels'
import type { ApiAuditEvent } from '@/types'

defineProps<{
  selectedProductSpace: ProductSpaceCard
  auditSummary: AuditSummary
  auditEvents: ApiAuditEvent[]
  isLoadingAuditEvents: boolean
  auditError: string
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <section class="atlas-audit-log-panel" data-testid="vue-audit-log-panel">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-admin-head" data-testid="vue-admin-panel">
      <div>
        <h1>审计日志</h1>
        <p>{{ selectedProductSpace.name }} · {{ auditSummary.total }} events</p>
      </div>
      <span>Governance read</span>
    </header>

    <section class="atlas-admin-grid">
      <article>
        <strong>{{ auditSummary.security }}</strong>
        <span>SECURITY</span>
      </article>
      <article>
        <strong>{{ auditSummary.denied }}</strong>
        <span>DENIED</span>
      </article>
      <article>
        <strong>{{ auditEvents[0]?.createdAt ?? 'n/a' }}</strong>
        <span>latest</span>
      </article>
    </section>

    <section class="atlas-audit-list" aria-label="审计事件">
      <p v-if="isLoadingAuditEvents" role="status">Loading audit events</p>
      <p v-else-if="auditError" class="atlas-inline-success" role="status">
        {{ auditError }}
      </p>
      <article v-for="event in auditEvents" :key="event.id" class="atlas-audit-event">
        <header>
          <strong>{{ event.action }}</strong>
          <span>{{ event.category }} · {{ event.result }} · {{ event.severity }}</span>
        </header>
        <p>{{ event.safeSummary }}</p>
        <dl>
          <div>
            <dt>actor</dt>
            <dd>{{ event.actorDisplay }} · {{ event.actorUserId ?? 'anonymous' }}</dd>
          </div>
          <div>
            <dt>target</dt>
            <dd>{{ event.targetType }} · {{ event.targetId }}</dd>
          </div>
          <div>
            <dt>metadata</dt>
            <dd>{{ auditMetadataLabel(event.metadata) }}</dd>
          </div>
        </dl>
      </article>
      <p v-if="!isLoadingAuditEvents && !auditError && auditEvents.length === 0">
        No audit events.
      </p>
    </section>

    <section class="atlas-admin-boundary">
      <strong>Production boundary</strong>
      <p>
        审计事件只显示后端返回的安全摘要与 allow-listed
        metadata；原始正文、prompt、密钥和私有路径不进入此面板。
      </p>
    </section>
  </section>
</template>
