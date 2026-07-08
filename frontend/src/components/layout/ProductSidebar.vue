<script setup lang="ts">
import type { ProductView, SettingsPanel } from '@/domain/viewModels'

defineProps<{
  productView: ProductView
  canManageMembers: boolean
}>()

const emit = defineEmits<{
  showHome: []
  showChat: []
  openSettings: [panel: SettingsPanel]
}>()
</script>

<template>
  <aside class="atlas-sidebar" aria-label="Product navigation">
    <div class="atlas-brand"><span>A</span><strong>Atlas Knowledge Hub</strong></div>
    <button :class="{ active: productView === 'home' }" type="button" @click="emit('showHome')">
      □ 知识库
    </button>
    <button type="button">✦ 智能体</button>
    <button type="button">∞ 共享空间</button>
    <button :class="{ active: productView === 'chat' }" type="button" @click="emit('showChat')">
      ◱ 对话
    </button>
    <p>近7天</p>
    <span>外企职场常用语</span>
    <span>选择知识库内容</span>
    <span>用户问候或打招呼</span>
    <p>工作区设置</p>
    <button type="button" @click="emit('openSettings', 'spaceInfo')">◎ 空间信息</button>
    <button type="button" :disabled="!canManageMembers" @click="emit('openSettings', 'members')">
      ♙ 成员管理
    </button>
    <button type="button" @click="emit('openSettings', 'models')">⬡ 模型管理</button>
    <button type="button" @click="emit('openSettings', 'vector')">◎ 向量数据库引擎</button>
    <button type="button" @click="emit('openSettings', 'parser')">▧ 解析引擎</button>
    <button type="button" @click="emit('openSettings', 'storage')">▱ 存储引擎</button>
    <button type="button" @click="emit('openSettings', 'general')">⚙ 全部设置</button>
  </aside>
</template>
