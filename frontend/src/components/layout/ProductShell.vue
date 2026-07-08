<script setup lang="ts">
import ProductSidebar from './ProductSidebar.vue'
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
  <main
    class="atlas-product-shell"
    data-testid="vue-product-page"
    aria-label="Atlas Knowledge Hub product page"
  >
    <ProductSidebar
      :product-view="productView"
      :can-manage-members="canManageMembers"
      @show-home="emit('showHome')"
      @show-chat="emit('showChat')"
      @open-settings="panel => emit('openSettings', panel)"
    />

    <section class="atlas-product-main">
      <slot />
    </section>
  </main>
</template>
