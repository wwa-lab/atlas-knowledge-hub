<script setup lang="ts">
import { memberRoleClass, memberRoleLabel } from '@/domain/viewModels'
import type { GeneralOption, MemberRole, PendingInvitation, SpaceMember } from '@/domain/viewModels'

defineProps<{
  pendingInvitationCount: number
  pendingInvitations: PendingInvitation[]
  spaceMembers: SpaceMember[]
  filteredSpaceMembers: SpaceMember[]
  memberSearch: string
  canManageMembers: boolean
  memberActionStatus: string
  memberRoleOptions: Array<GeneralOption<MemberRole>>
}>()

const emit = defineEmits<{
  close: []
  updateMemberSearch: [query: string]
  noteAuditEntry: []
  inviteMockMember: []
  copyInviteMockLink: []
  handleMemberRoleChange: [memberId: string, role: MemberRole]
  removeSpaceMember: [memberId: string]
}>()

function readInputValue(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? target.value : ''
}

function readMemberRole(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? (target.value as MemberRole) : 'viewer'
}
</script>

<template>
  <section class="atlas-member-settings" data-testid="vue-member-manager">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-member-head" data-testid="vue-admin-panel">
      <div>
        <h1>
          成员管理
          <span title="当前为 mock RBAC 说明">ⓘ</span>
          <button class="atlas-text-link" type="button" @click="emit('noteAuditEntry')">
            审计日志
          </button>
        </h1>
        <p>
          邀请伙伴加入当前空间并分配角色。只有 Owner/Admin 后续才能新增或移除成员。
          <a href="#" aria-label="了解 RBAC">了解 RBAC ↗</a>
        </p>
      </div>
    </header>

    <section class="atlas-member-block" aria-label="待接受的邀请">
      <header class="atlas-member-section-head">
        <div>
          <h2>
            待接受的邀请 <span>{{ pendingInvitationCount }}</span>
          </h2>
          <p>发出后等待对方在站内确认。7 天未响应将自动过期。</p>
        </div>
      </header>
      <div v-if="pendingInvitationCount === 0" class="atlas-member-empty">暂无待接受的邀请。</div>
      <div v-else class="atlas-member-invite-list">
        <article v-for="invite in pendingInvitations" :key="invite.id">
          <strong>{{ invite.email }}</strong>
          <span
            >{{ memberRoleLabel(invite.role, memberRoleOptions) }} · {{ invite.invitedAt }}</span
          >
          <small>邀请人：{{ invite.inviter }}</small>
        </article>
      </div>
    </section>

    <section class="atlas-member-block" aria-label="空间成员">
      <header class="atlas-member-toolbar">
        <div>
          <h2>
            空间成员 <span>{{ spaceMembers.length }}</span>
          </h2>
        </div>
        <div class="atlas-member-actions">
          <label class="atlas-member-search">
            <span>搜索成员</span>
            <input
              :value="memberSearch"
              data-testid="vue-member-search"
              placeholder="按姓名或邮箱搜索"
              @input="emit('updateMemberSearch', readInputValue($event))"
            />
          </label>
          <button
            class="atlas-icon-button"
            data-testid="vue-member-invite"
            type="button"
            aria-label="邀请成员"
            title="邀请成员"
            :disabled="!canManageMembers"
            @click="emit('inviteMockMember')"
          >
            +人
          </button>
          <button
            class="atlas-icon-button"
            data-testid="vue-member-copy-link"
            type="button"
            aria-label="复制邀请链接"
            title="复制邀请链接"
            :disabled="!canManageMembers"
            @click="emit('copyInviteMockLink')"
          >
            ⌁
          </button>
        </div>
      </header>

      <div class="atlas-member-table-wrap">
        <table class="atlas-member-table">
          <thead>
            <tr>
              <th>姓名与邮箱</th>
              <th>角色</th>
              <th>加入时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="member in filteredSpaceMembers" :key="member.id">
              <td>
                <strong>{{ member.name }}</strong>
                <span>{{ member.email }}</span>
              </td>
              <td>
                <span
                  v-if="!member.removable"
                  class="atlas-role-badge"
                  :class="memberRoleClass(member.role)"
                >
                  {{ memberRoleLabel(member.role, memberRoleOptions) }}
                </span>
                <select
                  v-else
                  :value="member.role"
                  :aria-label="`${member.name} 角色`"
                  @change="emit('handleMemberRoleChange', member.id, readMemberRole($event))"
                >
                  <option
                    v-for="option in memberRoleOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
              </td>
              <td>{{ member.joinedAt }}</td>
              <td>
                <button
                  class="atlas-member-remove"
                  type="button"
                  :disabled="!member.removable"
                  :aria-label="`移除 ${member.name}`"
                  @click="emit('removeSpaceMember', member.id)"
                >
                  移除
                </button>
              </td>
            </tr>
            <tr v-if="filteredSpaceMembers.length === 0">
              <td colspan="4">没有匹配的成员。</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p
        v-if="memberActionStatus"
        class="atlas-inline-success"
        data-testid="vue-member-status"
        role="status"
      >
        {{ memberActionStatus }}
      </p>
    </section>

    <section class="atlas-admin-boundary">
      <strong>Production boundary</strong>
      <p>
        当前成员管理为 mock-only：不发送邀请、不提交真实公司域名、不保存真实账号、 不执行生产
        RBAC，后续权限与审计必须由后端强制执行。
      </p>
    </section>
  </section>
</template>
