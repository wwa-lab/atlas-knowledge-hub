<script setup lang="ts">
import ApiInfoSettingsPanel from '@/components/settings/ApiInfoSettingsPanel.vue'
import AuditLogSettingsPanel from '@/components/settings/AuditLogSettingsPanel.vue'
import GeneralSettingsPanel from '@/components/settings/GeneralSettingsPanel.vue'
import MembersSettingsPanel from '@/components/settings/MembersSettingsPanel.vue'
import MessageSettingsPanel from '@/components/settings/MessageSettingsPanel.vue'
import ModelEditor from '@/components/settings/ModelEditor.vue'
import ModelsSettingsPanel from '@/components/settings/ModelsSettingsPanel.vue'
import PlaceholderSettingsPanelView from '@/components/settings/PlaceholderSettingsPanel.vue'
import ProfileSettingsPanel from '@/components/settings/ProfileSettingsPanel.vue'
import SettingsModal from '@/components/settings/SettingsModal.vue'
import SpaceInfoSettingsPanel from '@/components/settings/SpaceInfoSettingsPanel.vue'
import type { useSettings } from '@/composables/useSettings'
import type { ProductSpaceCard } from '@/domain/viewModels'

type SettingsState = ReturnType<typeof useSettings>

defineProps<{
  settings: SettingsState
  selectedProductSpace: ProductSpaceCard
  canReadGovernance: boolean
  canManageMembers: boolean
}>()
</script>

<template>
  <SettingsModal
    v-if="settings.settingsOpen.value"
    :settings-panel="settings.settingsPanel.value"
    :can-read-governance="canReadGovernance"
    @close="settings.closeSettings"
    @select-panel="settings.setSettingsPanel"
    @open-panel="settings.openSettings"
  >
    <GeneralSettingsPanel
      v-if="settings.settingsPanel.value === 'general'"
      :general-settings="settings.generalSettings.value"
      :language-options="settings.languageOptions"
      :theme-options="settings.themeOptions"
      :interface-font-options="settings.interfaceFontOptions"
      :code-font-options="settings.codeFontOptions"
      :font-size-options="settings.fontSizeOptions"
      @close="settings.closeSettings"
      @update-general-settings="settings.updateGeneralSettings"
    />
    <ProfileSettingsPanel
      v-else-if="settings.settingsPanel.value === 'profile'"
      :account-profile-rows="settings.accountProfileRows"
      @close="settings.closeSettings"
    />
    <SpaceInfoSettingsPanel
      v-else-if="settings.settingsPanel.value === 'spaceInfo'"
      :selected-product-space="selectedProductSpace"
      :selected-space-info-rows="settings.selectedSpaceInfoRows.value"
      :active-space-info-edit-field="settings.activeSpaceInfoEditField.value"
      :space-info-draft="settings.spaceInfoDraft.value"
      :space-info-status="settings.spaceInfoStatus.value"
      @close="settings.closeSettings"
      @begin-space-info-edit="settings.beginSpaceInfoEdit"
      @cancel-space-info-edit="settings.cancelSpaceInfoEdit"
      @save-space-info-edit="settings.saveSpaceInfoEdit"
      @update-space-info-draft="settings.updateSpaceInfoDraft"
    />
    <MembersSettingsPanel
      v-else-if="settings.settingsPanel.value === 'members'"
      :pending-invitation-count="settings.pendingInvitationCount.value"
      :pending-invitations="settings.pendingInvitations.value"
      :space-members="settings.spaceMembers.value"
      :filtered-space-members="settings.filteredSpaceMembers.value"
      :member-search="settings.memberSearch.value"
      :can-manage-members="canManageMembers"
      :member-action-status="settings.memberActionStatus.value"
      :member-role-options="settings.memberRoleOptions"
      @close="settings.closeSettings"
      @update-member-search="settings.updateMemberSearch"
      @note-audit-entry="settings.noteAuditEntry"
      @invite-mock-member="settings.inviteMockMember"
      @copy-invite-mock-link="settings.copyInviteMockLink"
      @handle-member-role-change="settings.handleMemberRoleChange"
      @remove-space-member="settings.removeSpaceMember"
    />
    <AuditLogSettingsPanel
      v-else-if="settings.settingsPanel.value === 'audit'"
      :selected-product-space="selectedProductSpace"
      :audit-summary="settings.auditSummary.value"
      :audit-events="settings.auditEvents.value"
      :is-loading-audit-events="settings.isLoadingAuditEvents.value"
      :audit-error="settings.auditError.value"
      @close="settings.closeSettings"
    />
    <ApiInfoSettingsPanel
      v-else-if="settings.settingsPanel.value === 'api'"
      :api-info="settings.apiInfo.value"
      :api-key-display-value="settings.apiKeyDisplayValue.value"
      :safe-error-previews="settings.safeErrorPreviews"
      @close="settings.closeSettings"
      @toggle-api-key-visibility="settings.toggleApiKeyVisibility"
      @copy-api-info-value="settings.copyApiInfoValue"
      @refresh-api-key="settings.refreshApiKey"
      @open-api-documentation="settings.openApiDocumentation"
    />
    <MessageSettingsPanel
      v-else-if="settings.settingsPanel.value === 'messages'"
      :message-index-enabled="settings.messageIndexEnabled.value"
      :message-embedding-model="settings.messageEmbeddingModel.value"
      :message-embedding-options="settings.messageEmbeddingOptions"
      :message-index-configured="settings.messageIndexConfigured.value"
      :message-index-stats="settings.messageIndexStats.value"
      @close="settings.closeSettings"
      @toggle-message-indexing="settings.toggleMessageIndexing"
      @update-message-embedding-model="settings.updateMessageEmbeddingModel"
    />
    <PlaceholderSettingsPanelView
      v-else-if="settings.currentSettingsSurface.value"
      :settings-surface="settings.currentSettingsSurface.value"
      @close="settings.closeSettings"
    />
    <ModelsSettingsPanel
      v-else
      :is-model-add-menu-open="settings.isModelAddMenuOpen.value"
      :addable-model-types="settings.addableModelTypes"
      :model-api-status="settings.modelApiStatus.value"
      :model-save-status="settings.modelSaveStatus.value"
      :model-api-error="settings.modelApiError.value"
      :model-categories="settings.modelCategories"
      :active-model-category="settings.activeModelCategory.value"
      :models="settings.models.value"
      :visible-models="settings.visibleModels.value"
      @close="settings.closeSettings"
      @toggle-model-add-menu="settings.toggleModelAddMenu"
      @update-active-model-category="settings.updateActiveModelCategory"
      @open-model-editor="settings.openModelEditor"
    />
  </SettingsModal>

  <ModelEditor
    v-if="settings.modelDraft.value"
    :model-draft="settings.modelDraft.value"
    :model-provider-options="settings.modelProviderOptions"
    :thinking-options="settings.thinkingOptions"
    :api-key-input="settings.apiKeyInput.value"
    :model-test-status="settings.modelTestStatus.value"
    @close="settings.closeModelEditor"
    @update-model-draft="settings.updateModelDraft"
    @update-api-key-input="settings.updateApiKeyInput"
    @set-model-source="settings.setModelSource"
    @start-api-key-replace="settings.startApiKeyReplace"
    @remove-api-key="settings.removeApiKey"
    @confirm-api-key-replace="settings.confirmApiKeyReplace"
    @cancel-api-key-replace="settings.cancelApiKeyReplace"
    @toggle-model-multimodal="settings.toggleModelMultimodal"
    @test-model-connection="settings.testModelConnection"
    @save-model-editor="settings.saveModelEditor"
  />
</template>
