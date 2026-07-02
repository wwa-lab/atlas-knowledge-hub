<script setup lang="ts">
const prototypeSrc = '/atlas-prototype.html'
</script>

<template>
  <main class="prototype-host" aria-label="Atlas Phase 1 prototype fidelity host">
    <iframe
      class="prototype-frame"
      title="Atlas Knowledge Hub Phase 1 Prototype"
      :src="prototypeSrc"
    />
  </main>
</template>
