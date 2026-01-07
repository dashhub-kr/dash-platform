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
            <component
              :is="item.locked ? 'div' : 'router-link'"
              v-for="item in group.items"
              :key="item.path"
              :to="!item.locked ? item.path : undefined"
              class="flex items-center gap-3 px-4 py-2 rounded-xl text-sm font-bold transition-all group relative overflow-hidden"
              :class="[
                  isActiveRoute(item.path) ? 'bg-slate-100' : 'hover:bg-slate-50',
                  item.locked ? 'opacity-50 cursor-not-allowed text-slate-400' : 'text-slate-500'
              ]"
            >
              <div 
                class="w-8 h-8 rounded-lg flex items-center justify-center shadow-sm transition-transform"
                :class="[
                    item.color,
                    item.locked ? 'grayscale opacity-70' : 'group-hover:scale-110'
                ]"
              >
                <component 
                  :is="item.icon" 
                  :size="18" 
                  class="text-white"
                  :stroke-width="2.5"
                />
              </div>
              <span class="tracking-wide text-sm font-bold" :class="{ 'text-slate-900': isActiveRoute(item.path) && !item.locked }">{{ item.label }}</span>
              
              <!-- Lock Overlay -->
              <div v-if="item.locked" class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
                  <Lock :size="14" />
              </div>
            </component>
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



   <!-- Study Application Modal -->
    <Teleport to="body">
        <div v-if="showApplicationModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="showApplicationModal = false"></div>
            
            <div class="relative bg-white rounded-3xl w-full max-w-md p-6 shadow-2xl animate-in fade-in zoom-in-95 duration-200">
                <h3 class="text-xl font-bold text-slate-800 mb-2">스터디 가입 신청</h3>
                
                <div v-if="loadingApp" class="py-12 flex justify-center">
                    <Loader2 class="animate-spin text-brand-500" :size="32"/>
                </div>
                
                <template v-else-if="selectedApp">
                    <div class="flex items-center gap-4 mb-6 p-4 bg-slate-50 rounded-2xl">
                        <img 
                            :src="selectedApp.applicant?.avatarUrl || '/images/profiles/default-profile.png'" 
                            class="w-12 h-12 rounded-full border border-slate-200 bg-white object-cover"
                        />
                        <div>
                            <div class="font-bold text-slate-800 text-lg">{{ selectedApp.applicant?.username || '알 수 없는 사용자' }}</div>
                            <div class="text-xs text-slate-400">가입 인사를 확인해주세요.</div>
                        </div>
                    </div>
                    
                    <div v-if="!isRejecting">
                         <div class="mb-8">
                             <div class="text-xs font-bold text-slate-400 uppercase mb-2">Message</div>
                             <div class="bg-slate-50 p-4 rounded-2xl text-slate-700 font-medium leading-relaxed whitespace-pre-wrap max-h-60 overflow-y-auto custom-scrollbar">
                                 {{ selectedApp.message }}
                             </div>
                        </div>

                        <div class="flex gap-3">
                            <button 
                                @click="handleApproveApp"
                                class="flex-1 py-3.5 rounded-xl font-bold text-white bg-brand-500 hover:bg-brand-600 transition-colors shadow-lg shadow-brand-500/30"
                            >
                                승인
                            </button>
                            <button 
                                @click="handleRejectClick"
                                class="flex-1 py-3.5 rounded-xl font-bold text-rose-500 bg-rose-50 hover:bg-rose-100 transition-colors"
                            >
                                거절
                            </button>
                        </div>
                    </div>

                    <div v-else class="animate-fade-in">
                        <div class="mb-6">
                            <label class="text-xs font-bold text-slate-400 uppercase mb-2 block">거절 사유</label>
                            <textarea
                                v-model="rejectReason"
                                class="w-full h-32 p-4 bg-slate-50 border-2 border-slate-100 rounded-xl focus:border-rose-500 focus:ring-0 transition-all font-medium text-slate-700 resize-none"
                                placeholder="거절 사유를 입력해주세요 (선택사항)"
                            ></textarea>
                        </div>
                        <div class="flex gap-3">
                            <button 
                                @click="handleCancelReject"
                                class="flex-1 py-3.5 rounded-xl font-bold text-slate-500 hover:bg-slate-100 transition-colors"
                            >
                                취소
                            </button>
                            <button 
                                @click="confirmReject"
                                class="flex-1 py-3.5 rounded-xl font-bold text-white bg-rose-500 hover:bg-rose-600 transition-colors shadow-lg shadow-rose-500/30"
                            >
                                거절하기
                            </button>
                        </div>
                    </div>
                </template>
                
                <div v-else class="py-10 text-center text-slate-400 font-medium bg-slate-50 rounded-2xl">
                    신청 정보를 찾을 수 없습니다.<br>
                    <span class="text-xs mt-1 block">이미 처리되었거나 삭제된 신청입니다.</span>
                    <button @click="showApplicationModal = false" class="mt-4 px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm font-bold text-slate-600 hover:bg-slate-50">닫기</button>
                </div>
            </div>
        </div>
    </Teleport>

</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute, useRouter } from 'vue-router';
// Imports update
import { 
    Github, Shield, LayoutGrid, MessageSquare, School, FileText, 
    PieChart, Target, Trophy, Bell, UserCircle, LogOut, X, Menu, Network, Compass,
    CheckCircle2, AlertCircle, MessageCircle, HelpCircle, Star, Loader2, Lock, Users
} from "lucide-vue-next";

import { useAuth } from "@/composables/useAuth";
import { authApi } from "@/api/auth";
import { getNotifications, markAsRead, markAllAsRead, updateNotification } from "@/api/notification";
import { studyApi } from "@/api/study";

const emits = defineEmits(['scroll']);

const { user, logout, refresh } = useAuth();
const route = useRoute();
const router = useRouter();
const mobileMenuOpen = ref(false);

const visible = computed(() => {
  if (route.path && route.path.includes("/oauth2/redirect")) return false;
  if (route.path && route.path.startsWith("/onboarding")) return false;
  if (route.path === '/' && !user.value) return false;
  return true;
});

const navGroups = computed(() => {
    const hasStudy = user.value && user.value.studyId;
    return [
  {
    title: '팀 스페이스', // Renamed from '스터디'
    items: [
      { label: '대시보드', path: '/dashboard', icon: LayoutGrid, color: 'bg-sky-500' },
      { label: '팀 미션', path: '/study/missions', icon: Target, color: 'bg-rose-500' }, // Renamed from '미션'
      { label: '팀 분석', path: '/study/analysis', icon: PieChart, color: 'bg-emerald-500' }, // Renamed from '분석'
    ]
  },
  {
    title: '퍼스널 트레이닝', // New Group
    items: [
      { label: '랜덤 디펜스', path: '/defense', icon: Shield, color: 'bg-slate-500' }, // Renamed & Moved to top (Action)
      { label: '모의고사', path: '/mockexam', icon: FileText, color: 'bg-blue-500' }, // Moved to 2nd (Action)
      { label: '스킬 트리', path: '/training/skilltree', icon: Network, color: 'bg-indigo-500' }, // Analysis/Status
      { label: '로드맵', path: '/training/roadmap', icon: School, color: 'bg-amber-500' }, // Planning (Ending)
    ]
  },
  {
    title: '커뮤니티',
    items: [
      { label: '전국 게시판', path: '/boards', icon: MessageSquare, color: 'bg-violet-500' }, // Reordered: Board first/second? User said "Explorer -> Board -> Social" order is weird. usually Board is main community.
      { label: '스터디 라운지', path: '/study/ranking', icon: Compass, color: 'bg-yellow-500' }, // Renamed from '스터디 탐험'.
      { label: '소셜', path: '/social', icon: Users, color: 'bg-pink-500' },
    ]
  },
  ...(user.value?.role === 'ROLE_ADMIN' ? [{
    title: '관리자',
    items: [
       { label: '대시보드', path: '/admin', icon: Shield, color: 'bg-slate-900' },
    ]
  }] : [])
]});

const navItems = computed(() => navGroups.value.flatMap(g => g.items));

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

// 브라우저 알림 권한 요청
const requestNotificationPermission = async () => {
    if (!("Notification" in window)) return;
    if (Notification.permission === "default") {
        await Notification.requestPermission();
    }
};

const latestNotificationId = ref(null);
const isFirstLoad = ref(true);

const fetchNotifications = async () => {
  if (!user.value) return;
  try {
    const data = await getNotifications();
    
    // 신규 알림 체크 및 브라우저 알림 발송
    if (!isFirstLoad.value && data.length > 0 && Notification.permission === "granted") {
        const newNotifications = data.filter(n => !latestNotificationId.value || n.id > latestNotificationId.value);
        newNotifications.forEach(n => {
            if (!n.isRead) {
                new Notification("DashHub Notification", {
                    body: n.content,
                    icon: '/favicon.ico' // 아이콘 경로
                });
            }
        });
    }

    notifications.value = data;
    if (data.length > 0) {
        // 가장 최신 ID 저장 (data는 보통 최신순 정렬됨을 가정)
        const maxId = Math.max(...data.map(n => n.id));
        if (!latestNotificationId.value || maxId > latestNotificationId.value) {
            latestNotificationId.value = maxId;
        }
    }
    isFirstLoad.value = false;
  } catch (e) {
    console.error('Failed to fetch notifications', e);
    // 401 에러(인증 만료) 시 폴링 중단하여 불필요한 호출 방지
    if (e.response && e.response.status === 401) {
        stopPolling();
    }
  }
};

// Study Application Modal
const showApplicationModal = ref(false);
const selectedApp = ref(null);
const loadingApp = ref(false);

const isRejecting = ref(false);
const rejectReason = ref('');

const openApplicationModal = async (applicationId) => {
    showApplicationModal.value = true;
    loadingApp.value = true;
    selectedApp.value = null;
    isRejecting.value = false;
    rejectReason.value = '';
    try {
        const res = await studyApi.getApplication(applicationId);
        selectedApp.value = res.data;
    } catch (e) {
        console.error(e);
        // alert("신청 정보를 불러오지 못했습니다.");
        // showApplicationModal.value = false;
    } finally {
        loadingApp.value = false;
    }
};

const handleApproveApp = async () => {
    if (!selectedApp.value) return;
    if (!confirm("가입 신청을 승인하시겠습니까?")) return;
    try {
        await studyApi.approveApplication(selectedApp.value.id);
        alert("승인되었습니다.");
        
        // Find corresponding notification to update
        const relatedNotif = notifications.value.find(n => n.relatedId === selectedApp.value.id && n.type === 'STUDY_REQUEST');
        if (relatedNotif) {
             const newContent = `'${selectedApp.value.applicant?.username}'님의 가입 신청을 승인했습니다.`;
             await updateNotification(relatedNotif.id, newContent, 'STUDY_RESULT');
             // Local update
             relatedNotif.content = newContent;
             relatedNotif.type = 'STUDY_RESULT';
             relatedNotif.isRead = true;
        } else {
             fetchNotifications();
        }
        showApplicationModal.value = false;
    } catch (e) {
        console.error(e);
        alert(e.response?.data?.message || "승인에 실패했습니다.");
    }
};

const handleRejectClick = () => {
    isRejecting.value = true;
};

const handleCancelReject = () => {
    isRejecting.value = false;
    rejectReason.value = '';
};

const confirmReject = async () => {
    if (!selectedApp.value) return;
    try {
        await studyApi.rejectApplication(selectedApp.value.id, rejectReason.value);
        alert("거절되었습니다.");
        
        // Find corresponding notification to update
        const relatedNotif = notifications.value.find(n => n.relatedId === selectedApp.value.id && n.type === 'STUDY_REQUEST');
        if (relatedNotif) {
             const newContent = `'${selectedApp.value.applicant?.username}'님의 가입 신청을 거절했습니다.`;
             await updateNotification(relatedNotif.id, newContent, 'STUDY_RESULT');
             // Local update
             relatedNotif.content = newContent;
             relatedNotif.type = 'STUDY_RESULT';
             relatedNotif.isRead = true;
        } else {
             fetchNotifications();
        }
        showApplicationModal.value = false;
    } catch (e) {
        console.error(e);
        alert(e.response?.data?.message || "거절에 실패했습니다.");
    }
};

const handleNotificationClick = async (notification) => {
  if (!notification.isRead) {
    try {
      await markAsRead(notification.id);
      notification.isRead = true;
    } catch (e) {
      console.error(e);
    }
  }
  
  if (notification.type === 'STUDY_REQUEST' && notification.relatedId) {
      notificationsOpen.value = false;
      openApplicationModal(notification.relatedId);
      return;
  }
  
  if (notification.type === 'FRIEND_REQUEST') {
      notificationsOpen.value = false;
      router.push('/social?tab=requests');
      return;
  }

  notificationsOpen.value = false;
  
  if (notification.type === 'DIRECT_MESSAGE') {
      notificationsOpen.value = false;
      if (notification.relatedId) {
          router.push(`/social?tab=friends&partnerId=${notification.relatedId}`);
      } else if (notification.url) {
          router.push(notification.url.replace('/social/messages', '/social').replace('?partnerId=', '?tab=friends&partnerId='));
      }
      return;
  }
  
  if (notification.type === 'STUDY_RESULT') {
      try {
          await refresh(); // 스터디 정보 업데이트 (가입 승인 시 user.studyId 갱신)
      } catch (e) { console.error('세션 갱신 실패', e); }
  }

  if (notification.type === 'FRIEND_ACCEPTED') {
      router.push('/social?tab=friends');
      return;
  }

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
    requestNotificationPermission(); // 권한 요청
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
// Removed duplicate useAuth call
// ...
const handleLogout = async () => {
  if (!confirm("로그아웃 하시겠습니까?")) return;
  await logout();
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
