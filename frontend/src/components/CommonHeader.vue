<template>
  <header
    v-if="visible"
    class="border-b border-slate-200 bg-white/70 backdrop-blur-md sticky top-0 z-50 transition-all duration-300"
  >
    <!-- 좌측 -->
    <div class="container mx-auto px-6 h-16 relative">
      <div class="absolute left-0 inset-y-0 flex items-center">
        <div class="flex items-center gap-4 justify-start">
          <div
            class="flex items-center gap-2 group cursor-pointer"
            @click="goHome"
            aria-label="홈으로 이동"
          >
            <img src="/icons/icon-128.png" alt="Dash Logo" class="w-8 h-8 group-hover:scale-105 transition-transform" />
            <span
              class="text-xl font-bold text-slate-800 tracking-tight group-hover:text-indigo-600 transition-colors"
              >Dash</span
            >
          </div>
        </div>
      </div>

      <!-- 가운데 -->
      <div
        class="absolute left-1/2 top-0 transform -translate-x-1/2 h-full flex items-center justify-center pointer-events-none"
      >
        <!-- 비로그인 시 랜딩 페이지 네비게이션 -->
        <nav
          v-if="authChecked && !user"
          class="hidden sm:flex items-center gap-6 pointer-events-auto"
        >
          <button
            @click.prevent="$emit('scroll', 'hero')"
            class="text-sm font-bold text-slate-500 hover:text-indigo-600 transition-colors"
          >
            홈
          </button>
          <button
            @click.prevent="$emit('scroll', 'features')"
            class="text-sm font-bold text-slate-500 hover:text-indigo-600 transition-colors"
          >
            주요 기능
          </button>
          <button
            @click.prevent="$emit('scroll', 'core')"
            class="text-sm font-bold text-slate-500 hover:text-indigo-600 transition-colors"
          >
            핵심 기능
          </button>
        </nav>

        <!-- 로그인 시 앱 네비게이션 -->
        <!-- 로그인 시 앱 네비게이션 -->
        <nav
          v-else-if="authChecked && user"
          class="hidden sm:flex items-center gap-6 pointer-events-auto"
        >
          <router-link
            to="/map"
            class="flex items-center text-slate-500 hover:text-indigo-600 transition-colors"
            active-class="text-indigo-600"
            title="월드 맵"
          >
            <Map :size="24" />
          </router-link>
          <router-link
            to="/simcity"
            class="flex items-center text-slate-500 hover:text-indigo-600 transition-colors"
            active-class="text-indigo-600"
            title="심시티"
          >
            <LayoutGrid :size="24" />
          </router-link>
        </nav>
      </div>

      <!-- 우측 -->
      <div class="absolute right-0 inset-y-0 flex items-center">
        <div class="flex items-center gap-4 justify-end">
          <template v-if="user">
            <div
              class="flex items-center gap-3 min-w-0 relative"
              ref="profileRef"
            >
              <!-- Profile Avatar Only -->
              <div 
                class="w-9 h-9 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold text-sm cursor-pointer hover:ring-2 hover:ring-indigo-100 transition-all"
                @click.stop="toggleProfileMenu"
                :title="user.username || user.email"
              >
                {{ userInitial }}
              </div>

              <transition name="slide-down">
                <div
                  v-if="profileMenuOpen"
                  class="absolute right-0 top-full mt-2 w-48 bg-white border border-slate-100 rounded-2xl py-2 shadow-xl z-50 overflow-hidden"
                >
                  <button
                    @click="goToProfile"
                    class="w-full text-left px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-indigo-600 transition-colors"
                  >
                    마이페이지
                  </button>
                  <button
                    @click="handleLogout"
                    class="w-full text-left px-4 py-2.5 text-sm font-medium text-rose-500 hover:bg-rose-50 transition-colors"
                  >
                    로그아웃
                  </button>
                </div>
              </transition>
            </div>
          </template>

          <template v-else>
            <button
              @click="handleLogin"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold text-white bg-slate-900 hover:bg-slate-800 transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5"
            >
              <Github :size="16" />
              로그인
            </button>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { Github, Map, LayoutGrid } from "lucide-vue-next";

import { useAuth } from "../composables/useAuth";
import { authApi } from "../api/auth";

const { user, authChecked, refresh } = useAuth();

const visible = computed(() => {
  const p = typeof window !== "undefined" ? window.location.pathname : "";
  if (p && p.includes("/oauth2/redirect")) return false;
  return true;
});

const emit = defineEmits(["scroll"]);

const profileMenuOpen = ref(false);
const profileRef = ref(null);

const userInitial = computed(() => {
  const name = user.value?.username || user.value?.email || "";
  return name ? String(name).trim().charAt(0).toUpperCase() : "U";
});

const goHome = () => {
  window.location.href = "/";
};
const handleLogin = () => {
  window.location.href = `/oauth2/authorization/github`;
};

const toggleProfileMenu = () => {
  profileMenuOpen.value = !profileMenuOpen.value;
};
const goToProfile = () => {
  profileMenuOpen.value = false;
  window.location.href = "/profile";
};

const onDocClick = (e) => {
  const el = profileRef.value;
  if (!el) return;
  if (!el.contains(e.target)) profileMenuOpen.value = false;
};

const handleLogout = async () => {
  if (!confirm("로그아웃 하시겠습니까?")) return;
  try {
    await authApi.logout();
  } catch (e) {
    console.error("Logout API failed, forcing local logout:", e);
  } finally {
    // Force a full reload to clear any in-memory state
    window.location.href = "/";
  }
};

onMounted(() => {
  document.addEventListener("click", onDocClick);
});
onBeforeUnmount(() => {
  document.removeEventListener("click", onDocClick);
});
</script>

<style scoped>
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}
.slide-down-enter-to,
.slide-down-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
</style>
