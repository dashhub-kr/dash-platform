import { ref, onMounted } from 'vue'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const user = ref(null)
const authChecked = ref(false)

export function useAuth() {
  const refresh = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/me`, { credentials: 'include' })
      if (res.ok) user.value = await res.json()
      else user.value = null
    } catch (e) {
      user.value = null
    } finally {
      authChecked.value = true
    }
  }

  onMounted(() => {
    // initial fetch
    if (!authChecked.value) refresh()
  })

  return { user, authChecked, refresh }
}
