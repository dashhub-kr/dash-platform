<template>
  <aside 
    v-if="visible"
    class="fixed left-0 top-0 h-screen w-64 bg-white border-r border-slate-200 flex flex-col z-50 transition-all duration-300 hidden md:flex"
  >
    <!-- 로고 영역 -->
    <div class="px-8 py-5">
      <div 
        class="cursor-pointer font-['Outfit'] text-2xl flex items-center select-none gap-px"
        @click="goHome"
      >
        <span class="font-black text-brand-600 tracking-tighter">Dash</span>
        <span class="font-bold text-slate-700 tracking-tight">Hub</span>
      </div>
    </div>

    <!-- 네비게이션 메뉴 -->
    <nav class="flex-1 px-4 space-y-2 overflow-y-auto custom-scrollbar">
      <template v-if="user">
        <div v-for="(group, gIdx) in navGroups" :key="gIdx">
          <div v-if="gIdx > 0" class="h-px bg-slate-100 my-1 mx-2"></div>
          
          <h3 class="px-4 mb-1 text-xs font-black text-slate-400 uppercase tracking-wider">{{ group.title }}</h3>
          
          <div class="space-y-1">
            <router-link
              v-for="item in group.items"
              :key="item.path"
              :to="item.path"
              class="flex items-center gap-3 px-4 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-50 transition-all group"
              :class="isActiveRoute(item.path) ? 'bg-slate-100' : ''"
            >
              <div 
                class="w-8 h-8 rounded-lg flex items-center justify-center shadow-sm transition-transform group-hover:scale-110"
                :class="item.color"
              >
                <component 
                  :is="item.icon" 
                  :size="18" 
                  class="text-white"
                  :stroke-width="2.5"
                />
              </div>
              <span class="tracking-wide text-sm font-bold text-slate-600" :class="{ 'text-slate-900': isActiveRoute(item.path) }">{{ item.label }}</span>
            </router-link>
          </div>
        </div>
      </template>
      
      <div v-else class="px-4 py-4 text-center">
         <p class="text-slate-400 text-xs mb-4">로그인이 필요합니다</p>
         <button
            @click="handleLogin"
            class="w-full flex items-center justify-center gap-2 px-5 py-3 rounded-xl text-sm font-bold text-white bg-slate-900 hover:bg-slate-800 transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5"
          >
            <Github :size="18" />
            <span>로그인</span>
          </button>
      </div>
    </nav>

    <!-- 하단 액션 영역 -->
    <div v-if="user" class="p-3 border-t border-slate-100 space-y-2">
       <!-- 알림 -->
       <div class="relative" ref="notificationRef">
          <button 
            @click="toggleNotifications"
            class="w-full flex items-center gap-4 px-4 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 transition-colors"
            :class="{ 'text-slate-800 bg-slate-100': notificationsOpen }"
          >
            <div class="relative">
              <Bell :size="24" :stroke-width="2.5" />
              <span 
                v-if="unreadCount > 0"
                class="absolute -top-1 -right-1 w-3 h-3 bg-rose-500 rounded-full border-2 border-white"
              ></span>
            </div>
            <span class="tracking-wide uppercase text-xs">알림</span>
          </button>

          <!-- 알림 드롭다운 (위쪽으로) -->
          <transition name="slide-up">
            <div v-if="notificationsOpen" class="absolute left-full bottom-0 ml-4 w-80 bg-white border border-slate-100 rounded-2xl shadow-xl z-50 overflow-hidden flex flex-col max-h-[480px]">
               <div class="px-4 py-3 border-b border-slate-100 bg-slate-50 flex justify-between items-center shrink-0">
                <div class="flex items-center gap-2">
                    <h3 class="font-bold text-slate-800 text-sm">알림</h3>
                    <span class="text-xs font-bold text-brand-500 bg-brand-50 px-1.5 py-0.5 rounded-md" v-if="unreadCount > 0">{{ unreadCount }}</span>
                </div>
                <div class="flex items-center gap-2">
                    <button @click="handleMarkAllRead" class="text-xs font-bold text-slate-500 hover:text-brand-600 transition-colors" v-if="unreadCount > 0">모두 읽음</button>
                    <button @click="notificationsOpen = false" class="text-slate-400 hover:text-slate-600"><X :size="16"/></button>
                </div>
              </div>
              
              <div v-if="notifications.length === 0" class="p-8 text-center text-slate-400">
                <div class="flex justify-center mb-2"><Bell :size="24" class="opacity-20"/></div>
                <p class="text-xs">새로운 알림이 없습니다</p>
              </div>
              
              <div v-else class="overflow-y-auto custom-scrollbar flex-1">
                <div 
                    v-for="notif in notifications" 
                    :key="notif.id" 
                    class="p-4 border-b border-slate-50 last:border-b-0 hover:bg-slate-50 transition-colors cursor-pointer relative group"
                    :class="{ 'bg-slate-50 opacity-75': notif.isRead, 'bg-white border-l-4 border-l-brand-500': !notif.isRead }"
                    @click="handleNotificationClick(notif)"
                >
                  <div class="flex gap-3">
                    <!-- 아이콘 -->
                    <div class="mt-0.5 shrink-0">
                        <div v-if="notif.type === 'SOS'" class="w-8 h-8 rounded-full bg-rose-100 flex items-center justify-center text-rose-500"><AlertCircle :size="16" /></div>
                        <div v-else-if="notif.type === 'SOLVED'" class="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-500"><CheckCircle2 :size="16" /></div>
                        <div v-else-if="notif.type === 'MISSION'" class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-500"><Target :size="16" /></div>
                        <div v-else-if="notif.type === 'MISSION_COMPLETED'" class="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center text-amber-500"><Trophy :size="16" /></div>
                        <div v-else-if="notif.type === 'STUDY_REQUEST'" class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-500"><UserCircle :size="16" /></div>
                        <div v-else-if="notif.type === 'STUDY_RESULT'" class="w-8 h-8 rounded-full bg-sky-100 flex items-center justify-center text-sky-500"><School :size="16" /></div>
                        <div v-else-if="notif.type === 'COMMENT'" class="w-8 h-8 rounded-full bg-violet-100 flex items-center justify-center text-violet-500"><MessageCircle :size="16" /></div>
                        <div v-else class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500"><Bell :size="16" /></div>
                    </div>
                    
                    <div class="flex-1 min-w-0">
                      <p class="text-sm text-slate-800 leading-snug break-keep" :class="{ 'font-bold': !notif.isRead }">
                        {{ notif.content }}
                      </p>
                      <p class="text-[11px] text-slate-400 mt-1 font-medium">{{ formatTimeAgo(notif.createdAt) }}</p>
                    </div>
                    
                    <div v-if="!notif.isRead" class="mt-1.5 shrink-0 w-2 h-2 rounded-full bg-brand-500"></div>
                  </div>
                </div>
              </div>
            </div>
          </transition>
       </div>

       <!-- 프로필 / 더보기 -->
       <div class="relative" ref="profileRef">
          <button 
            @click="toggleProfileMenu"
            class="w-full flex items-center gap-4 px-4 py-2 rounded-xl text-sm font-bold text-slate-500 hover:bg-slate-100 transition-colors"
            :class="{ 'text-slate-800 bg-slate-100 ring-2 ring-slate-200': profileMenuOpen }"
          >
            <img 
                :src="userProfileImage" 
                class="w-7 h-7 rounded-full object-cover bg-brand-100 border border-slate-200"
                :alt="user.username"
            />
            <span class="tracking-wide uppercase text-xs truncate flex-1 text-left">더보기</span>
          </button>

          <!-- 프로필 메뉴 드롭다운 (위쪽으로) -->
          <transition name="slide-up">
            <div
              v-if="profileMenuOpen"
              class="absolute left-full bottom-0 ml-4 w-56 bg-white border border-slate-100 rounded-2xl py-2 shadow-xl z-50 overflow-hidden"
            >
              <div class="px-4 py-3 border-b border-slate-50 mb-1">
                  <p class="font-bold text-slate-800 text-sm truncate">{{ user.username }}</p>
                  <p class="text-xs text-slate-400 truncate">{{ user.email }}</p>
              </div>
              <button
                @click="goToProfile"
                class="w-full text-left px-4 py-2.5 text-sm font-bold text-slate-600 hover:bg-slate-50 hover:text-brand-600 transition-colors flex items-center gap-3"
              >
                <UserCircle :size="18" /> 마이페이지
              </button>
              <button
                @click="handleLogout"
                class="w-full text-left px-4 py-2.5 text-sm font-bold text-rose-500 hover:bg-rose-50 transition-colors flex items-center gap-3"
              >
                <LogOut :size="18" /> 로그아웃
              </button>
            </div>
          </transition>
       </div>
    </div>
  </aside>

  <!-- 모바일 헤더 (작은 화면에서만 표시) -->
  <div v-if="visible" class="md:hidden fixed top-0 left-0 w-full h-16 bg-white border-b border-slate-200 z-50 flex items-center justify-between px-4">
      <div class="flex items-center gap-px font-['Outfit'] text-xl select-none" @click="goHome">
        <span class="font-black text-brand-600 tracking-tighter">Dash</span>
        <span class="font-bold text-slate-700 tracking-tight">Hub</span>
      </div>
      <button @click="mobileMenuOpen = !mobileMenuOpen" class="p-2 text-slate-600">
          <Menu :size="24" />
      </button>
  </div>
   <!-- Simple Mobile Menu Overlay -->
   <div v-if="mobileMenuOpen && visible" class="md:hidden fixed inset-0 bg-white z-[60] p-6 animate-fade-in">
       <div class="flex justify-between items-center mb-8">
           <div class="font-['Outfit'] text-xl flex items-center select-none gap-px">
               <span class="font-black text-brand-600 tracking-tighter">Dash</span>
               <span class="font-bold text-slate-700 tracking-tight">Hub</span>
           </div>
           <button @click="mobileMenuOpen = false"><X :size="24" class="text-slate-400"/></button>
       </div>
       <nav class="space-y-4">
           <router-link v-for="item in navItems" :key="item.path" :to="item.path" @click="mobileMenuOpen = false"
               class="block text-lg font-bold text-slate-600 py-2 border-b border-slate-100">
               {{ item.label }}
           </router-link>
       </nav>
   </div>

</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from 'vue-router';
import { 
    Github, Shield, LayoutGrid, MessageSquare, School, FileText, 
    PieChart, Target, Trophy, Bell, UserCircle, LogOut, X, Menu, Network, Compass,
    CheckCircle2, AlertCircle, MessageCircle, HelpCircle, Star
} from "lucide-vue-next";

import { useAuth } from "@/composables/useAuth";
import { authApi } from "@/api/auth";
import { getNotifications, markAsRead, markAllAsRead } from "@/api/notification";

const emits = defineEmits(['scroll']);

const { user, authChecked } = useAuth();
const route = useRoute();
const router = useRouter();
const mobileMenuOpen = ref(false);

const visible = computed(() => {
  if (route.path && route.path.includes("/oauth2/redirect")) return false;
  if (route.path && route.path.startsWith("/onboarding")) return false;
  if (route.path === '/' && !user.value) return false;
  return true;
});

const navGroups = [
  {
    title: '스터디',
    items: [
      { label: '대시보드', path: '/dashboard', icon: LayoutGrid, color: 'bg-sky-500' },
      { label: '미션', path: '/study/missions', icon: Target, color: 'bg-rose-500' },
      { label: '분석', path: '/study/analysis', icon: PieChart, color: 'bg-emerald-500' },
    ]
  },
  {
    title: '트레이닝',
    items: [
      { label: '로드맵', path: '/training/roadmap', icon: School, color: 'bg-amber-500' },
      { label: '스킬 트리', path: '/training/skilltree', icon: Network, color: 'bg-indigo-500' },
    ]
  },
  {
    title: '문제',
    items: [
      { label: '모의고사', path: '/mockexam', icon: FileText, color: 'bg-blue-500' },
      { label: '디펜스', path: '/defense', icon: Shield, color: 'bg-slate-500' },
    ]
  },
  {
    title: '커뮤니티',
    items: [
      { label: '둘러보기', path: '/study/ranking', icon: Compass, color: 'bg-yellow-500' },
      { label: '게시판', path: '/boards', icon: MessageSquare, color: 'bg-violet-500' },
    ]
  }
];

const navItems = computed(() => navGroups.flatMap(g => g.items));

const isActiveRoute = (itemPath) => {
  const currentPath = route.path;
  if (currentPath === itemPath) return true;
  if (currentPath.startsWith(itemPath + '/')) return true;
  return false;
};

// 프로필 및 알림
const profileMenuOpen = ref(false);
const profileRef = ref(null);
const notificationsOpen = ref(false);
const notificationRef = ref(null);
const notifications = ref([]);
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length);

const toggleNotifications = () => {
    notificationsOpen.value = !notificationsOpen.value;
    if (notificationsOpen.value) {
        profileMenuOpen.value = false;
        fetchNotifications();
    }
};

const toggleProfileMenu = () => {
    profileMenuOpen.value = !profileMenuOpen.value;
    if (profileMenuOpen.value) notificationsOpen.value = false;
};

const fetchNotifications = async () => {
  if (!user.value) return;
  try {
    const data = await getNotifications();
    notifications.value = data;
  } catch (e) {
    console.error('Failed to fetch notifications', e);
  }
};

const handleNotificationClick = async (notification) => {
  if (!notification.isRead) {
    try {
      await markAsRead(notification.id);
      notification.isRead = true;
      // notifications.value = notifications.value.map(n => n.id === notification.id ? { ...n, isRead: true } : n);
    } catch (e) {
      console.error(e);
    }
  }
  
  notificationsOpen.value = false;
  if (notification.url) {
    router.push(notification.url);
  }
};

const handleMarkAllRead = async () => {
    try {
        await markAllAsRead();
        notifications.value.forEach(n => n.isRead = true);
    } catch (e) {
        console.error(e);
    }
}

// 폴링 로직
let pollingInterval = null;
const startPolling = () => {
  stopPolling();
  if (user.value) {
    fetchNotifications();
    pollingInterval = setInterval(fetchNotifications, 15000); // 15초 간격
  }
};
const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval);
    pollingInterval = null;
  }
};

watch(user, (val) => {
  if (val) startPolling();
  else stopPolling();
}, { immediate: true });

onBeforeUnmount(() => stopPolling());

// 사용자 프로필
const userProfileImage = computed(() => {
  if (!user.value) return '';
  if (user.value.avatarUrl) return user.value.avatarUrl;
  return '/images/profiles/default-profile.png';
});

// 액션
const goHome = () => window.location.href = "/";
const handleLogin = () => window.location.href = `/oauth2/authorization/github`;
const goToProfile = () => {
    profileMenuOpen.value = false;
    window.location.href = "/profile";
};
const handleLogout = async () => {
  if (!confirm("로그아웃 하시겠습니까?")) return;
  try { await authApi.logout(); } catch (e) { console.error(e); }
  finally { window.location.href = "/"; }
};

const formatTimeAgo = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  if (minutes < 60) return `${minutes}분 전`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}시간 전`;
  return `${Math.floor(hours / 24)}일 전`;
};

const onDocClick = (e) => {
  if (profileRef.value && !profileRef.value.contains(e.target)) profileMenuOpen.value = false;
  if (notificationRef.value && !notificationRef.value.contains(e.target)) notificationsOpen.value = false;
};

onMounted(() => document.addEventListener("click", onDocClick));
onBeforeUnmount(() => document.removeEventListener("click", onDocClick));


</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@500;700;900&display=swap');
</style>
