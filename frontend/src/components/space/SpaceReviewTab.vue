<script setup lang="ts">
import type { ProductBatchMetrics, ProductProcessingIssue } from '@/domain/viewModels'
import type { ApiDeadLetterEntry, ApiReviewQueueItem } from '@/types'

defineProps<{
  apiBlockedReviewCount: number
  apiReadyToPublishCount: number
  manualUrlReviewRequiredCount: number
  productBatchMetrics: ProductBatchMetrics
  reviewQueueCards: ApiReviewQueueItem[]
  apiProcessingIssues: ProductProcessingIssue[]
  canApprove: boolean
  isReviewing: boolean
  deadLetterEntries: ApiDeadLetterEntry[]
  isLoadingDeadLetters: boolean
  deadLetterError: string
  selectedDeadLetterEntry: ApiDeadLetterEntry | null
  isRetryingDeadLetter: boolean
  isAcknowledgingDeadLetter: boolean
  productProcessingIssues: ProductProcessingIssue[]
}>()

const emit = defineEmits<{
  approveSelectedFile: []
  loadDeadLetters: []
  selectDeadLetterEntry: [entryId: string]
  retrySelectedDeadLetter: []
  acknowledgeSelectedDeadLetter: []
}>()
</script>

<template>
  <div class="atlas-review-layout">
    <section class="atlas-processing-overview" data-testid="vue-processing-center">
      <article>
        <strong>{{ apiBlockedReviewCount }}</strong
        ><span>API blocked queues</span>
      </article>
      <article>
        <strong>{{ apiReadyToPublishCount }}</strong
        ><span>API ready to publish</span>
      </article>
      <article data-testid="vue-processing-manual-url-count">
        <strong>{{ manualUrlReviewRequiredCount }}</strong
        ><span>manual URL review</span>
      </article>
      <article>
        <strong>{{ productBatchMetrics.total }}</strong
        ><span>total documents</span>
      </article>
      <article>
        <strong>{{ productBatchMetrics.failed }}</strong
        ><span>parse failures</span>
      </article>
      <article>
        <strong>{{ productBatchMetrics.ocr }}</strong
        ><span>OCR required</span>
      </article>
      <article>
        <strong>{{ productBatchMetrics.lowConfidence }}</strong
        ><span>low confidence</span>
      </article>
      <article><strong>2</strong><span>missing source_trace</span></article>
      <article>
        <strong>{{ productBatchMetrics.approved }}</strong
        ><span>ready to publish</span>
      </article>
    </section>
    <section class="atlas-api-review-queues" data-testid="vue-api-review-queues">
      <header>
        <h2>API review queues</h2>
        <span>{{ reviewQueueCards.length > 0 ? 'ApiEnvelope connected' : 'No API queues' }}</span>
        <button
          data-testid="vue-api-approve-file"
          type="button"
          :disabled="!canApprove || isReviewing"
          @click="emit('approveSelectedFile')"
        >
          {{ isReviewing ? 'Approving...' : 'Approve API file' }}
        </button>
      </header>
      <article v-for="issue in apiProcessingIssues" :key="issue.id">
        <div>
          <strong>{{ issue.label }}</strong>
          <span>{{ issue.type }} · {{ issue.source }}</span>
        </div>
        <span>{{ issue.count }}</span>
        <span>{{ issue.status }}</span>
        <button type="button">{{ issue.action }}</button>
      </article>
    </section>
    <section class="atlas-api-review-queues" data-testid="vue-dead-letter-ops">
      <header>
        <h2>Worker recovery</h2>
        <span>
          {{
            deadLetterEntries.length > 0
              ? `${deadLetterEntries.length} dead-letter entries`
              : 'No dead letters'
          }}
        </span>
        <button
          type="button"
          :disabled="isLoadingDeadLetters"
          data-testid="vue-refresh-dead-letters"
          @click="emit('loadDeadLetters')"
        >
          {{ isLoadingDeadLetters ? 'Refreshing...' : 'Refresh' }}
        </button>
      </header>
      <p v-if="deadLetterError" class="atlas-inline-warning">{{ deadLetterError }}</p>
      <article
        v-for="entry in deadLetterEntries"
        :key="entry.id"
        data-testid="vue-dead-letter-entry"
      >
        <div>
          <strong>{{ entry.subjectType }} · {{ entry.subjectId }}</strong>
          <span>{{ entry.jobType }} · {{ entry.safeErrorCategory }}</span>
        </div>
        <span>{{ entry.attemptSummary }}</span>
        <span>{{ entry.status }}</span>
        <button type="button" @click="emit('selectDeadLetterEntry', entry.id)">Inspect</button>
      </article>
      <div
        v-if="selectedDeadLetterEntry"
        class="atlas-api-detail"
        data-testid="vue-dead-letter-detail"
      >
        <header>
          <div>
            <h3>{{ selectedDeadLetterEntry.safeErrorCode }}</h3>
            <p>
              {{ selectedDeadLetterEntry.safeErrorMessage }} ·
              {{ selectedDeadLetterEntry.job.status }}
            </p>
          </div>
          <span>{{ selectedDeadLetterEntry.status }}</span>
        </header>
        <dl>
          <div>
            <dt>Attempts</dt>
            <dd>
              {{ selectedDeadLetterEntry.job.attemptCount }}/{{
                selectedDeadLetterEntry.job.maxAttempts
              }}
            </dd>
          </div>
          <div>
            <dt>Retry delay</dt>
            <dd>
              {{
                selectedDeadLetterEntry.job.retryDelaySeconds === null
                  ? 'terminal'
                  : `${selectedDeadLetterEntry.job.retryDelaySeconds}s`
              }}
            </dd>
          </div>
          <div>
            <dt>Review</dt>
            <dd>{{ selectedDeadLetterEntry.reviewEligible ? 'eligible' : 'blocked' }}</dd>
          </div>
        </dl>
        <p data-testid="vue-dead-letter-trace">
          source_trace:
          {{
            Object.entries(selectedDeadLetterEntry.sourceTrace)
              .map(([key, value]) => `${key}=${value}`)
              .join(' · ')
          }}
        </p>
        <ul>
          <li
            v-for="attempt in selectedDeadLetterEntry.attempts"
            :key="attempt.id"
            data-testid="vue-dead-letter-attempt"
          >
            #{{ attempt.attemptNumber }} · {{ attempt.status }} · {{ attempt.safeErrorCategory }} ·
            {{ attempt.safeErrorMessage }}
          </li>
        </ul>
        <footer>
          <button
            type="button"
            :disabled="isRetryingDeadLetter || selectedDeadLetterEntry.status !== 'OPEN'"
            data-testid="vue-retry-dead-letter"
            @click="emit('retrySelectedDeadLetter')"
          >
            {{ isRetryingDeadLetter ? 'Retrying...' : 'Retry safely' }}
          </button>
          <button
            type="button"
            :disabled="isAcknowledgingDeadLetter || selectedDeadLetterEntry.status !== 'OPEN'"
            data-testid="vue-ack-dead-letter"
            @click="emit('acknowledgeSelectedDeadLetter')"
          >
            {{ isAcknowledgingDeadLetter ? 'Acknowledging...' : 'Acknowledge' }}
          </button>
        </footer>
      </div>
    </section>
    <section class="atlas-processing-queues">
      <article
        v-for="issue in productProcessingIssues"
        :key="issue.id"
        data-testid="vue-processing-issue"
      >
        <div>
          <strong>{{ issue.label }}</strong>
          <span>{{ issue.type }} · {{ issue.source }}</span>
        </div>
        <span>{{ issue.count }}</span>
        <span>{{ issue.status }}</span>
        <button type="button">{{ issue.action }}</button>
      </article>
    </section>
  </div>
</template>
