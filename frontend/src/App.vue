<template>
  <MobileRestrictionView v-if="isMobile" />
  <template v-else>
    <Sidebar @scroll="scrollToSection" />
    <div class="bg-slate-50 min-h-screen transition-all duration-300" :class="{ 'md:ml-64': isSidebarVisible }">
        <router-view />
    </div>
  </template>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "@/composables/useAuth";
import Sidebar from "./components/layout/Sidebar.vue";
import MobileRestrictionView from "@/views/MobileRestrictionView.vue";

const route = useRoute();
const { user } = useAuth();
const isMobile = ref(false);

const checkMobile = () => {
  if (typeof window !== "undefined") {
    isMobile.value = window.innerWidth < 768;
  }
};

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});

const isSidebarVisible = computed(() => {
  if (typeof window === "undefined") return false;
  if (route.path && route.path.includes("/oauth2/redirect")) return false;
  if (route.path && route.path.startsWith("/onboarding")) return false;
  if (route.path === '/' && !user.value) return false;
  return true;
});

const scrollToSection = (id) => {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
};
</script>

<style>
/* 전역 앱 스타일 설정 영역 */
</style>
