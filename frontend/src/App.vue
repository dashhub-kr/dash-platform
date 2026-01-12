<template>
    <Sidebar @scroll="scrollToSection" />
    <div class="bg-slate-50 min-h-screen transition-all duration-300 pb-16 md:pb-0" :class="mainContentClass">
        <router-view />
    </div>

    <!-- Global Modals -->
    <UserProfileModal />

    <!-- Global Floating Chat Button -->
    <FloatingMessagePanel v-if="user" />

    <!-- Admin Floating Button -->
    <router-link
      v-if="user?.role === 'ROLE_ADMIN'"
      to="/admin"
      class="fixed bottom-24 right-6 z-50 p-4 bg-slate-900 text-white rounded-full shadow-lg shadow-slate-900/40 hover:scale-110 active:scale-95 transition-all text-sm font-bold flex items-center group"
    >
        <Shield :size="24" stroke-width="2.5" />
        <span class="max-w-0 overflow-hidden whitespace-nowrap group-hover:max-w-xs group-hover:ml-2 transition-all duration-300">관리자</span>
    </router-link>
</template>


<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "@/composables/useAuth";
import Sidebar from "./components/layout/Sidebar.vue";
import MobileRestrictionView from "@/views/MobileRestrictionView.vue";
import { Shield } from 'lucide-vue-next';

// Global Modals
import UserProfileModal from "@/components/social/UserProfileModal.vue";
import FloatingMessagePanel from "@/components/social/FloatingMessagePanel.vue";

const route = useRoute();
const { user } = useAuth();

const isSidebarVisible = computed(() => {
  if (typeof window === "undefined") return false;
  if (route.path && route.path.includes("/oauth2/redirect")) return false;
  if (route.path && route.path.startsWith("/onboarding")) return false;
  if (route.path === '/' && !user.value) return false;
  return true;
});

// Responsive margin: xl=full sidebar, lg/md=icon sidebar, mobile=no sidebar
const mainContentClass = computed(() => {
  if (!isSidebarVisible.value) return '';
  return 'xl:ml-64 lg:ml-20 md:ml-20';
});

const scrollToSection = (id) => {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
};
</script>

<style>
/* 전역 앱 스타일 설정 영역 */
</style>
