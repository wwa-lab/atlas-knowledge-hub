import {
  answerReviewReasonLine,
  answerReuseHint,
  auditMetadataLabel,
  formatGraphEvidenceReference,
  formatMetricRatio,
  isSecretConfigured,
  memberRoleClass,
  memberRoleLabel,
  modelDetail,
  modelIcon,
  modelSecretLabel,
  modelTypeToCategory,
  mockFileStatusLabel,
  productGraphNodeType,
  productReviewStatus,
  secretStatusByKey,
  uniqueValues,
  wikiSourceTrace
} from './viewModels'
import type { ApiAskRun, ApiSecretStatus, ApiWikiPage } from '@/types'
import type { VueModelConfig } from './viewModels'

describe('frontend view model helpers', () => {
  it('formats Ask governance and metric labels without changing safe fallbacks', () => {
    const reviewedRun = {
      answerReviewReason: 'Approved for team reuse',
      answerReviewedBy: 'SME',
      answerReusable: true
    } as ApiAskRun
    const unreviewedRun = {
      answerReusable: false
    } as ApiAskRun

    expect(answerReviewReasonLine(reviewedRun)).toBe('Approved for team reuse · SME')
    expect(answerReviewReasonLine(unreviewedRun)).toBe('No reviewer reason recorded.')
    expect(answerReuseHint(reviewedRun)).toBe('Approved reusable knowledge.')
    expect(answerReuseHint(unreviewedRun)).toBe('Not approved reusable knowledge.')
    expect(formatMetricRatio(0.944)).toBe('94%')
    expect(formatMetricRatio(null)).toBe('n/a')
    expect(formatMetricRatio(Number.NaN)).toBe('n/a')
  })

  it('preserves Wiki and graph source trace display strings', () => {
    const page = {
      sourceRefs: [
        {
          type: 'SOURCE_CHUNK',
          id: 'chunk-p0',
          label: 'P0 Evidence',
          locator: 'page 4'
        }
      ],
      sourceDocumentIds: ['doc-p0'],
      markdownPath: 'wiki/p0.md'
    } as ApiWikiPage
    const fallbackPage = {
      sourceRefs: [],
      sourceDocumentIds: ['doc-fallback'],
      markdownPath: 'wiki/fallback.md'
    } as unknown as ApiWikiPage

    expect(wikiSourceTrace(page)).toBe('SOURCE_CHUNK: chunk-p0 · P0 Evidence / page 4')
    expect(wikiSourceTrace(fallbackPage)).toBe('sources doc-fallback / wiki/fallback.md')
    expect(
      formatGraphEvidenceReference({
        referenceType: 'WIKI_PAGE',
        wikiPageId: 'wiki-p0',
        label: 'P0 Wiki',
        section: 'Overview',
        reviewStatus: 'PUBLISHED',
        confidence: 0.96
      })
    ).toBe('Wiki page P0 Wiki · wiki-p0 · Overview · PUBLISHED · confidence 0.96')
    expect(
      formatGraphEvidenceReference({
        sourceChunkId: 'chunk-p0',
        sourceFile: 'samples/p0.md',
        section: 'Evidence',
        reviewStatus: 'APPROVED',
        confidence: 0.91
      })
    ).toBe('Source chunk chunk-p0 · samples/p0.md · Evidence · APPROVED · confidence 0.91')
  })

  it('maps graph and model labels deterministically', () => {
    expect(uniqueValues(['ENTITY', 'DOCUMENT', 'ENTITY'])).toEqual(['DOCUMENT', 'ENTITY'])
    expect(productReviewStatus('PUBLISHED')).toBe('PUBLISHED')
    expect(productReviewStatus('APPROVED')).toBe('APPROVED')
    expect(productReviewStatus('REVIEW_REQUIRED')).toBe('REVIEW_REQUIRED')
    expect(productGraphNodeType('WIKI_PAGE')).toBe('Wiki Page')
    expect(productGraphNodeType('SOURCE_CHUNK')).toBe('Document')
    expect(mockFileStatusLabel('OCR_REQUIRED')).toBe('OCR_REQUIRED')
    expect(memberRoleLabel('owner', [{ value: 'owner', label: '所有者' }])).toBe('所有者')
    expect(memberRoleLabel('viewer', [])).toBe('viewer')
    expect(memberRoleClass('admin')).toBe('role-admin')
    expect(auditMetadataLabel({ requestId: 'req-001', retry: false })).toBe(
      'requestId: req-001 · retry: false'
    )
    expect(auditMetadataLabel({})).toBe('metadata none')
    expect(modelTypeToCategory('EMBEDDING')).toBe('embedding')
    expect(modelIcon('chat')).toBe('□')
    expect(modelDetail({ category: 'vision' } as VueModelConfig)).toBe(' · 多模态')
  })

  it('keeps secret status labels masked and status-only', () => {
    const statuses: ApiSecretStatus[] = [
      {
        reference: {
          provider: 'deepseek',
          scope: 'MODEL_ADAPTER',
          key: 'credential',
          displayName: 'Credential'
        },
        status: 'ENV_CONFIGURED',
        source: 'environment',
        maskedLabel: 'configured',
        replaceable: true,
        removable: false
      }
    ]
    const model = {
      apiKeyStatus: 'not_configured',
      secretStatuses: statuses
    } as VueModelConfig

    expect(secretStatusByKey(statuses, 'credential')).toBe(statuses[0])
    expect(isSecretConfigured(statuses[0])).toBe(true)
    expect(modelSecretLabel(model)).toBe('环境已配置')
    const unconfiguredModel = {
      ...model,
      apiKeyStatus: 'not_configured',
      secretStatuses: []
    } as VueModelConfig
    expect(modelSecretLabel(unconfiguredModel)).toBe('未配置')
  })
})
