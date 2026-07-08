import { ref } from 'vue'

export function useGlobalChat() {
  const selectedChatSpaces = ref(['IBM i Modernization', 'AI Engineering Playbook'])

  function toggleChatSpace(name: string) {
    selectedChatSpaces.value = selectedChatSpaces.value.includes(name)
      ? selectedChatSpaces.value.filter(space => space !== name)
      : [...selectedChatSpaces.value, name]
  }

  return {
    selectedChatSpaces,
    toggleChatSpace
  }
}
