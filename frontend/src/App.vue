<template>
    <Sidebar @scroll="scrollToSection" />
    <div class="bg-slate-50 min-h-screen transition-all duration-300 pb-16 md:pb-0" :class="mainContentClass">
        <router-view />
    </div>

    <!-- Global Modals -->
    <UserProfileModal />
    <DirectMessageModal 
      v-if="isDMOpen && dmPartnerInfo"
      :partner-id="dmPartnerInfo.partnerId"
      :partner-name="dmPartnerInfo.partnerName"
      :partner-avatar="dmPartnerInfo.partnerAvatar"
      :partner-decoration="dmPartnerInfo.partnerDecoration"
      @close="closeDM"
    />

    <!-- Floating Message Panel (Instagram-style) -->
    <FloatingMessagePanel />
</template>


<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import { useAuth } from "@/composables/useAuth";
import Sidebar from "./components/layout/Sidebar.vue";

// Global Modals
import UserProfileModal from "@/components/social/UserProfileModal.vue";
import DirectMessageModal from "@/components/social/DirectMessageModal.vue";
import FloatingMessagePanel from "@/components/social/FloatingMessagePanel.vue";
import { useDirectMessageModal } from "@/composables/useDirectMessageModal";

const route = useRoute();
const { user } = useAuth();

const { isOpen: isDMOpen, partnerInfo: dmPartnerInfo, close: closeDM } = useDirectMessageModal();

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
