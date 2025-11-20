<template>
  <header v-if="visible" class="border-b border-white/10 bg-slate-950 sticky top-0 z-50">
    <div class="container mx-auto px-6 h-16 relative">
      <!-- 좌측 -->
      <div class="absolute left-0 inset-y-0 flex items-center">
        <div class="flex items-center gap-4 justify-start">
          <template v-if="user">
            <div class="flex items-center gap-2 group cursor-pointer" @click="goToDashboard" aria-label="대시보드로 이동">
              <div class="p-1.5 rounded-lg bg-indigo-500/10 group-hover:bg-indigo-500/20 transition-colors">
                <ClipboardList :size="20" class="text-indigo-400" />
              </div>
              <span class="text-sm font-semibold text-white">대시보드</span>
            </div>
          </template>
          <template v-else>
            <div class="flex items-center gap-2 group cursor-pointer" @click="goHome">
              <div class="p-1.5 rounded-lg bg-indigo-500/10 group-hover:bg-indigo-500/20 transition-colors">
                <Code2 :size="24" class="text-indigo-400" />
              </div>
              <span class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">DASH</span>
            </div>
          </template>
        </div>
      </div>

      <!-- 중앙 (탭 전용, 부모가 제어) -->
      <div class="absolute left-1/2 top-0 transform -translate-x-1/2 h-full flex items-center justify-center pointer-events-none">
        <nav v-if="authChecked && !user" class="hidden sm:flex items-center gap-6 pointer-events-auto">
          <button @click.prevent="$emit('scroll','hero')" class="text-sm text-slate-300 hover:text-white transition text-center">홈</button>
          <button @click.prevent="$emit('scroll','features')" class="text-sm text-slate-300 hover:text-white transition text-center">주요 기능</button>
          <button @click.prevent="$emit('scroll','core')" class="text-sm text-slate-300 hover:text-white transition text-center">핵심 기능</button>
        </nav>
      </div>

      <!-- 우측 -->
      <div class="absolute right-0 inset-y-0 flex items-center">
        <div class="flex items-center gap-4 justify-end">
          <template v-if="user">
            <div class="flex items-center gap-3 min-w-0 relative" ref="profileRef">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-9 h-9 rounded-full bg-indigo-600/20 flex items-center justify-center text-indigo-300 font-semibold">{{ userInitial }}</div>
                <div class="flex flex-col leading-tight min-w-0">
                  <span class="text-xs text-slate-300">반갑습니다,</span>
                  <span class="text-sm text-white font-semibold max-w-[140px] sm:max-w-[180px] block truncate">{{ user.name || user.email }}</span>
                </div>
              </div>

              <button @click.stop="toggleProfileMenu" :aria-expanded="profileMenuOpen" class="p-1 rounded hover:bg-slate-800 transition-colors" aria-label="계정 메뉴">
                <ChevronDown :size="16" class="text-slate-300" />
              </button>

              <transition name="slide-down">
                <div v-if="profileMenuOpen" class="absolute right-0 top-full mt-2 w-48 bg-slate-900 border border-slate-800 rounded-lg py-1 shadow-lg z-50">
                  <button @click="goToProfile" class="w-full text-left px-3 py-2 text-sm text-slate-200 hover:bg-slate-800">마이페이지</button>
                  <button @click="handleLogout" class="w-full text-left px-3 py-2 text-sm text-rose-400 hover:bg-slate-800">로그아웃</button>
                </div>
              </transition>
            </div>
          </template>

          <template v-else>
            <button
              @click="handleLogin"
              class="px-4 py-2 rounded-full text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-400 transition-shadow shadow-sm"
            >
              로그인
            </button>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Code2, ClipboardList, ChevronDown } from 'lucide-vue-next'

import { useAuth } from '../composables/useAuth'

const { user, authChecked, refresh } = useAuth()

const visible = computed(() => {
  // Only hide header during OAuth redirect handler route
  const p = typeof window !== 'undefined' ? window.location.pathname : ''
  if (p && p.includes('/oauth2/redirect')) return false
  return true
})

const emit = defineEmits(['scroll'])

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const profileMenuOpen = ref(false)
const profileRef = ref(null)

const userInitial = computed(() => {
  const name = user.value?.name || user.value?.email || ''
  return name ? String(name).trim().charAt(0).toUpperCase() : 'U'
})

const goHome = () => { window.location.href = '/' }
const goToDashboard = () => { window.location.href = '/dashboard' }
const handleLogin = () => { window.location.href = `${API_BASE}/oauth2/authorization/google` }

const toggleProfileMenu = () => { profileMenuOpen.value = !profileMenuOpen.value }
const goToProfile = () => { profileMenuOpen.value = false; window.location.href = '/profile' }

const onDocClick = (e) => {
  const el = profileRef.value
  if (!el) return
  if (!el.contains(e.target)) profileMenuOpen.value = false
}

const handleLogout = async () => {
  try {
    await fetch(`${API_BASE}/logout`, { method: 'POST', credentials: 'include' })
  } catch (e) {
    console.error('Logout failed:', e)
  } finally {
    await refresh()
    window.location.href = '/'
  }
}

onMounted(() => {
  document.addEventListener('click', onDocClick)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})
</script>
