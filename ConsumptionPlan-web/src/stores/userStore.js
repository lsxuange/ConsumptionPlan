import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref('')
  const avatar = ref('')

  function setUser(newToken, newUsername, newAvatar) {
    token.value = newToken
    username.value = newUsername
    avatar.value = newAvatar || ''
    localStorage.setItem('token', newToken)
  }

  function clearUser() {
    token.value = ''
    username.value = ''
    avatar.value = ''
    localStorage.removeItem('token')
  }

  return { token, username, avatar, setUser, clearUser }
})
