<template>
  <header
    v-if="visible"
    class="border-b border-white/10 bg-slate-950 sticky top-0 z-50"
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
            <img
              src="/icons/icon-32.png"
              alt="DASH logo"
              class="w-7 h-7 object-contain drop-shadow-lg"
            />
            <span
              class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400"
              >DASH</span
            >
          </div>
        </div>
      </div>

      <!-- 가운데 -->
      <div
        class="absolute left-1/2 top-0 transform -translate-x-1/2 h-full flex items-center justify-center pointer-events-none"
      >
        <nav
          v-if="authChecked && !user"
          class="hidden sm:flex items-center gap-6 pointer-events-auto"
        >
          <button
            @click.prevent="$emit('scroll', 'hero')"
            class="text-sm text-slate-300 hover:text-white transition text-center"
          >
            홈
          </button>
          <button
            @click.prevent="$emit('scroll', 'features')"
            class="text-sm text-slate-300 hover:text-white transition text-center"
          >
            주요 기능
          </button>
          <button
            @click.prevent="$emit('scroll', 'core')"
            class="text-sm text-slate-300 hover:text-white transition text-center"
          >
            핵심 기능
          </button>
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
              <div class="flex items-center gap-3 min-w-0">
                <div
                  class="w-9 h-9 rounded-full bg-indigo-600/20 flex items-center justify-center text-indigo-300 font-semibold"
                >
                  {{ userInitial }}
                </div>
                <div class="flex flex-col leading-tight min-w-0">
                  <span class="text-xs text-slate-300">반갑습니다,</span>
                  <span
                    class="text-sm text-white font-semibold max-w-[140px] sm:max-w-[180px] block truncate"
                    >{{ user.username || user.email }}</span
                  >
                </div>
              </div>

              <button
                @click.stop="toggleProfileMenu"
                :aria-expanded="profileMenuOpen"
                class="p-1 rounded hover:bg-slate-800 transition-colors"
                aria-label="계정 메뉴"
              >
                <ChevronDown :size="16" class="text-slate-300" />
              </button>

              <transition name="slide-down">
                <div
                  v-if="profileMenuOpen"
                  class="absolute right-0 top-full mt-2 w-48 bg-slate-900 border border-slate-800 rounded-lg py-1 shadow-lg z-50"
                >
                  <button
                    @click="goToProfile"
                    class="w-full text-left px-3 py-2 text-sm text-slate-200 hover:bg-slate-800"
                  >
                    마이페이지
                  </button>
                  <button
                    @click="handleLogout"
                    class="w-full text-left px-3 py-2 text-sm text-rose-400 hover:bg-slate-800"
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
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { ChevronDown } from "lucide-vue-next";

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
  window.location.href = `/oauth2/authorization/github`; // This is OAuth entry, keep as simple link
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
  try {
    await authApi.logout();
  } catch (e) {
    console.error("Logout failed:", e);
  } finally {
    await refresh();
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
