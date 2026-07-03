import {
  countBlockedReviewItems,
  countGraphNodesByType,
  filterModels,
  getPublishedWikiPages,
  getPublishReadyCount,
  getReviewQueues,
  getSpaceById
} from './atlasMock'

describe('atlas mock data', () => {
  it('finds the accepted IBM i Modernization demo space', () => {
    expect(getSpaceById('ibm-i')?.name).toBe('IBM i Modernization')
  })

  it('filters model cards by category without mutating the source list', () => {
    const allModels = filterModels('all')
    const embeddingModels = filterModels('embedding')

    expect(allModels.length).toBeGreaterThan(embeddingModels.length)
    expect(embeddingModels).toEqual(
      expect.arrayContaining([expect.objectContaining({ category: 'embedding' })])
    )
  })

  it('counts graph node types for the legend', () => {
    expect(countGraphNodesByType()).toMatchObject({
      Entity: 1,
      Concept: 1,
      Document: 1,
      'Review Required': 1,
      'Wiki Page': 1
    })
  })

  it('keeps review publish queues API-shaped and separates ready content', () => {
    expect(getReviewQueues()).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ type: 'MISSING_SOURCE_TRACE', publishBlocked: true }),
        expect.objectContaining({ type: 'READY_TO_PUBLISH', publishBlocked: false })
      ])
    )
    expect(getReviewQueues()).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          type: 'MISSING_SOURCE_TRACE',
          representativeItems: expect.arrayContaining([
            expect.objectContaining({ fileId: 'file-missing-trace-001', hasSourceTrace: false })
          ])
        })
      ])
    )
    const queues = getReviewQueues()
    queues[0].representativeItems[0].fileId = 'mutated'
    expect(getReviewQueues()[0].representativeItems[0].fileId).not.toBe('mutated')
    expect(getPublishReadyCount()).toBe(12579)
    expect(countBlockedReviewItems()).toBeGreaterThan(0)
    expect(countBlockedReviewItems()).not.toBe(getPublishReadyCount())
  })

  it('returns cloned published wiki metadata with traceable source documents', () => {
    const pages = getPublishedWikiPages('ibm-i')
    pages[0].sourceDocumentIds.push('mutated')

    expect(pages).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          id: 'wiki-file-001',
          reviewStatus: 'PUBLISHED',
          markdownPath: 'generated/md/BRD.md',
          sourceDocumentIds: expect.arrayContaining(['file-001'])
        })
      ])
    )
    expect(getPublishedWikiPages('ibm-i')[0].sourceDocumentIds).not.toContain('mutated')
  })
})
