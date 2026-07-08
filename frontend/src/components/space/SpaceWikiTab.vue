<script setup lang="ts">
import type { ProductWikiPage } from '@/domain/viewModels'
import type { ApiWikiPageIssue } from '@/types'

defineProps<{
  visibleProductWikiPages: ProductWikiPage[]
  selectedProductWikiPage: ProductWikiPage
  selectedProductWikiPageIssues: ApiWikiPageIssue[]
  canPublish: boolean
  isPublishing: boolean
}>()

const emit = defineEmits<{
  selectWikiPage: [pageId: string]
  publishSelectedFile: []
}>()
</script>

<template>
  <div class="atlas-wiki-layout">
    <aside data-testid="vue-wiki-index">
      <input placeholder="搜索 Wiki 页面..." />
      <button
        v-for="page in visibleProductWikiPages"
        :key="page.id"
        :class="{ active: selectedProductWikiPage.id === page.id }"
        type="button"
        @click="emit('selectWikiPage', page.id)"
      >
        <strong>{{ page.title }}</strong>
        <span>{{ page.reviewStatus }} · confidence {{ page.confidence.toFixed(2) }}</span>
      </button>
    </aside>
    <article data-testid="vue-wiki-page">
      <header class="atlas-wiki-head">
        <div>
          <h2>{{ selectedProductWikiPage.title }}</h2>
          <p>{{ selectedProductWikiPage.slug }} · {{ selectedProductWikiPage.owner }}</p>
        </div>
        <span>{{ selectedProductWikiPage.reviewStatus }}</span>
        <button
          data-testid="vue-api-publish-file"
          type="button"
          :disabled="!canPublish || isPublishing"
          @click="emit('publishSelectedFile')"
        >
          {{ isPublishing ? 'Publishing...' : 'Publish API Wiki' }}
        </button>
      </header>
      <div class="atlas-wiki-meta">
        <span>type {{ selectedProductWikiPage.pageType }}</span>
        <span>version {{ selectedProductWikiPage.version }}</span>
        <span>source {{ selectedProductWikiPage.sourceMode }}</span>
        <span>refresh {{ selectedProductWikiPage.refreshPolicy }}</span>
        <span>confidence {{ selectedProductWikiPage.confidence.toFixed(2) }}</span>
        <span>updated {{ selectedProductWikiPage.updatedAt }}</span>
        <span>
          aliases
          {{
            selectedProductWikiPage.aliases.length > 0
              ? selectedProductWikiPage.aliases.join(', ')
              : 'none'
          }}
        </span>
        <span>
          links in {{ selectedProductWikiPage.inLinks.length }} / out
          {{ selectedProductWikiPage.outLinks.length }}
        </span>
        <span>wiki warnings {{ selectedProductWikiPageIssues.length }}</span>
        <span>source_trace: {{ selectedProductWikiPage.sourceTrace }}</span>
        <span>
          chunk_refs
          {{
            selectedProductWikiPage.chunkRefs.length > 0
              ? selectedProductWikiPage.chunkRefs.join('; ')
              : 'none'
          }}
        </span>
      </div>
      <section
        v-if="selectedProductWikiPageIssues.length > 0"
        data-testid="vue-wiki-issues"
        class="atlas-wiki-section"
      >
        <h3>Wiki quality warnings</h3>
        <div v-for="issue in selectedProductWikiPageIssues" :key="issue.id">
          <strong>{{ issue.issueType }}</strong>
          <span>{{ issue.severity }} · {{ issue.status }} · {{ issue.message }}</span>
        </div>
      </section>
      <nav class="atlas-entity-links" aria-label="Wiki entity links">
        <button v-for="entity in selectedProductWikiPage.entities" :key="entity" type="button">
          {{ entity }}
        </button>
      </nav>
      <section
        v-for="section in selectedProductWikiPage.sections"
        :key="section.title"
        class="atlas-wiki-section"
      >
        <h3>{{ section.title }}</h3>
        <p>{{ section.body }}</p>
        <div>
          <strong>source_trace</strong>
          <span>{{ section.sourceTrace }}</span>
          <span>
            {{ section.reviewStatus }} · confidence
            {{ section.confidence.toFixed(2) }}
          </span>
        </div>
      </section>
    </article>
  </div>
</template>
