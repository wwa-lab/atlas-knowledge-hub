import { mount } from '@vue/test-utils'
import App from './App.vue'

describe('Atlas Phase 1 prototype fidelity host', () => {
  it('embeds the accepted prototype fixture', () => {
    const wrapper = mount(App)
    const frame = wrapper.get('iframe')

    expect(frame.attributes('src')).toBe('/atlas-prototype.html')
    expect(frame.attributes('title')).toBe('Atlas Knowledge Hub Phase 1 Prototype')
  })
})
