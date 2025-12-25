<template>
  <Sidebar @scroll="scrollToSection" />
  <div class="bg-slate-50 min-h-screen transition-all duration-300" :class="{ 'md:ml-64': isSidebarVisible }">
      <router-view />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "./composables/useAuth";
import Sidebar from "./components/Sidebar.vue";

const route = useRoute();
const { user } = useAuth();

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
