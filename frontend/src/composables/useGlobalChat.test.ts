import { describe, expect, it } from 'vitest'
import { useGlobalChat } from '@/composables/useGlobalChat'

describe('useGlobalChat', () => {
  it('toggles selected chat spaces with immutable array updates', () => {
    const state = useGlobalChat()
    const initial = state.selectedChatSpaces.value

    state.toggleChatSpace('IBM i Modernization')
    expect(state.selectedChatSpaces.value).not.toBe(initial)
    expect(state.selectedChatSpaces.value).not.toContain('IBM i Modernization')

    state.toggleChatSpace('Claims Ops Hub')
    expect(state.selectedChatSpaces.value).toContain('Claims Ops Hub')
  })
})
