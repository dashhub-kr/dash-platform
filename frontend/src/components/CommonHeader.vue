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
            <img src="/icons/dash.png" alt="DASH" class="h-10 object-contain group-hover:scale-105 transition-transform" />
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
        <nav
          v-else-if="authChecked && user"
          class="hidden sm:flex items-center gap-1 pointer-events-auto"
        >
          <!-- Dashboard -->
          <router-link
            to="/dashboard"
            class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-indigo-600 transition-all whitespace-nowrap"
            active-class="bg-indigo-50 text-indigo-600"
          >
            <LayoutGrid :size="18" />
            <span>대시보드</span>
          </router-link>

          <!-- Divider -->
          <div class="w-px h-6 bg-slate-200 mx-2"></div>

          <!-- Group 1: Study -->
          <div class="flex items-center gap-1">
            <router-link
              to="/study/missions"
              class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-emerald-600 transition-all whitespace-nowrap"
              active-class="bg-emerald-50 text-emerald-600"
            >
              <Target :size="18" />
              <span>미션</span>
            </router-link>
            <router-link
              to="/study/analysis"
              class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-teal-600 transition-all whitespace-nowrap"
              active-class="bg-teal-50 text-teal-600"
            >
              <PieChart :size="18" />
              <span>분석</span>
            </router-link>
          </div>

          <!-- Divider -->
          <div class="w-px h-6 bg-slate-200 mx-2"></div>

          <!-- Group 2: Growth -->
          <div class="flex items-center gap-1">
            <router-link
              to="/training"
              class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-indigo-600 transition-all whitespace-nowrap"
              active-class="bg-indigo-50 text-indigo-600"
            >
              <School :size="18" />
              <span>트레이닝</span>
            </router-link>
            <router-link
              to="/mockexam"
              class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-amber-600 transition-all whitespace-nowrap"
              active-class="bg-amber-50 text-amber-600"
            >
              <FileText :size="18" />
              <span>모의고사</span>
            </router-link>
            <router-link
              to="/defense"
              class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-rose-600 transition-all whitespace-nowrap"
              active-class="bg-rose-50 text-rose-600"
            >
              <Shield :size="18" />
              <span>디펜스</span>
            </router-link>
          </div>

          <!-- Divider -->
          <div class="w-px h-6 bg-slate-200 mx-2"></div>

          <!-- Group 3: Social -->
          <div class="flex items-center gap-1">
            <router-link
              to="/study/ranking"
              class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-amber-500 transition-all whitespace-nowrap"
              active-class="bg-amber-50 text-amber-500"
            >
              <Trophy :size="18" />
              <span>랭킹</span>
            </router-link>
            <router-link
              to="/boards"
              class="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 hover:text-indigo-600 transition-all whitespace-nowrap"
              active-class="bg-indigo-50 text-indigo-600"
            >
              <MessageSquare :size="18" />
              <span>게시판</span>
            </router-link>
          </div>
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
              <!-- 프로필 아바타 -->
              <img 
                :src="userProfileImage" 
                class="w-9 h-9 rounded-full object-cover bg-indigo-100 cursor-pointer hover:ring-2 hover:ring-indigo-100 transition-all"
                @click.stop="toggleProfileMenu"
                :alt="user.username || user.email"
              />

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
import { Github, Shield, LayoutGrid, MessageSquare, School, FileText, PieChart, Target, Trophy } from "lucide-vue-next";

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

const profileImages = [
  '/profile/bag.png',
  '/profile/proud.png',
  '/profile/smart.png',
  '/profile/smile.png'
];

const userProfileImage = computed(() => {
  if (!user.value) return '';
  const id = user.value.id || 0;
  // Deterministic selection based on ID
  const index = id % profileImages.length;
  return profileImages[index];
});

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
    // 인메모리 상태를 초기화하고 랜딩 페이지로 이동
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

/* 네비게이션 커스텀 툴팁 */
.nav-tooltip {
  position: relative;
}
.nav-tooltip::after {
  content: attr(data-tooltip);
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(4px);
  padding: 6px 12px;
  background: #1e293b;
  color: white;
  font-size: 12px;
  font-weight: 500;
  border-radius: 8px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s ease;
  pointer-events: none;
  z-index: 50;
}
.nav-tooltip::before {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-bottom-color: #1e293b;
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s ease;
}
.nav-tooltip:hover::after,
.nav-tooltip:hover::before {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(8px);
}
.nav-tooltip:hover::before {
  transform: translateX(-50%) translateY(4px);
}
</style>
