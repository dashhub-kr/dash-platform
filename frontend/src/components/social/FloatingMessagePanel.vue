<template>
    <!-- 로그인한 사용자에게만 표시 -->
    <div v-if="isAuthenticated" class="fixed bottom-6 right-6 z-50">
        <!-- 플로팅 버튼 -->
        <Transition name="scale">
            <button
                v-if="!isOpen"
                @click="togglePanel"
                class="w-14 h-14 bg-brand-600 hover:bg-brand-500 text-white rounded-full shadow-lg hover:shadow-xl transition-all duration-200 flex items-center justify-center group hover:scale-105"
            >
                <MessageCircle :size="24" class="group-hover:scale-110 transition-transform" />
                <!-- 안읽음 뱃지 -->
                <div 
                    v-if="totalUnread > 0" 
                    class="absolute -top-1 -right-1 min-w-[22px] h-[22px] bg-rose-500 text-white text-xs font-bold rounded-full flex items-center justify-center px-1 shadow-md"
                >
                    {{ totalUnread > 99 ? '99+' : totalUnread }}
                </div>
            </button>
        </Transition>

        <!-- 확장 패널 -->
        <Transition name="slide-up">
            <div 
                v-if="isOpen"
                class="absolute bottom-0 right-0 w-[360px] bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden"
            >
                <!-- 헤더 -->
                <div class="flex items-center justify-between p-4 border-b border-slate-100 bg-brand-600 text-white">
                    <div class="flex items-center gap-2">
                        <MessageCircle :size="20" />
                        <span class="font-bold">메시지</span>
                        <span v-if="totalUnread > 0" class="px-2 py-0.5 bg-white/20 rounded-full text-xs">{{ totalUnread }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                        <button @click="openFullView" class="p-1.5 hover:bg-white/20 rounded-lg transition-colors" title="전체보기">
                            <Maximize2 :size="16" />
                        </button>
                        <button @click="togglePanel" class="p-1.5 hover:bg-white/20 rounded-lg transition-colors" title="닫기">
                            <X :size="16" />
                        </button>
                    </div>
                </div>

                <!-- 대화 목록 -->
                <div class="max-h-[400px] overflow-y-auto">
                    <div v-if="loading" class="flex justify-center py-10">
                        <Loader2 class="animate-spin text-brand-500" />
                    </div>
                    <div v-else-if="conversations.length === 0" class="text-center py-10 text-slate-400">
                        <MessageCircle :size="32" class="mx-auto mb-2 opacity-30" />
                        <p class="text-sm">아직 대화가 없어요</p>
                    </div>
                    <div v-else>
                        <div 
                            v-for="conv in conversations" 
                            :key="conv.partnerId" 
                            @click="openChat(conv)"
                            class="flex items-center gap-3 p-4 hover:bg-slate-50 cursor-pointer transition-colors border-b border-slate-50 last:border-b-0"
                        >
                            <div class="relative">
                                <img 
                                    :src="getAvatar(conv.partnerAvatar)" 
                                    class="w-11 h-11 rounded-full border border-slate-200 bg-white object-cover"
                                />
                                <div 
                                    v-if="conv.unreadCount > 0" 
                                    class="absolute -top-1 -right-1 min-w-[16px] h-[16px] bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1"
                                >
                                    {{ conv.unreadCount > 9 ? '9+' : conv.unreadCount }}
                                </div>
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="flex items-center justify-between">
                                    <span class="font-semibold text-slate-800 text-sm truncate">{{ conv.partnerName }}</span>
                                    <span class="text-[10px] text-slate-400 shrink-0 ml-2">{{ formatTime(conv.lastMessageTime) }}</span>
                                </div>
                                <p class="text-xs text-slate-500 truncate mt-0.5">{{ conv.lastMessagePreview || '메시지 없음' }}</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 하단 -->
                <div class="p-3 border-t border-slate-100 bg-slate-50">
                    <button 
                        @click="openFullView"
                        class="w-full py-2 text-sm text-brand-600 font-bold hover:bg-brand-50 rounded-lg transition-colors"
                    >
                        소셜 탭에서 더보기
                    </button>
                </div>
            </div>
        </Transition>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessageCircle, X, Maximize2, Loader2 } from 'lucide-vue-next';
import { socialApi } from '@/api/social';
import { useAuth } from '@/composables/useAuth';
import { useDirectMessageModal } from '@/composables/useDirectMessageModal';

const router = useRouter();
const route = useRoute();
const { user } = useAuth();
const { open: openDM } = useDirectMessageModal();

// user가 있으면 인증됨
const isAuthenticated = computed(() => !!user.value);

const isOpen = ref(false);
const loading = ref(false);
const conversations = ref([]);

const totalUnread = computed(() => {
    return conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0);
});

const togglePanel = () => {
    isOpen.value = !isOpen.value;
    if (isOpen.value) {
        loadConversations();
    }
};

const loadConversations = async () => {
    if (!isAuthenticated.value) return;
    loading.value = true;
    try {
        const res = await socialApi.getConversations();
        conversations.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const openChat = (conv) => {
    openDM({
        partnerId: conv.partnerId,
        partnerName: conv.partnerName,
        partnerAvatar: conv.partnerAvatar,
        partnerDecoration: conv.partnerDecorationClass || ''
    });
    isOpen.value = false;
};

const openFullView = () => {
    router.push('/social?tab=messages');
    isOpen.value = false;
};

const getAvatar = (url) => {
    if (url && !url.includes('dicebear')) return url;
    return '/images/profiles/default-profile.png';
};

const formatTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;
    const oneDay = 24 * 60 * 60 * 1000;
    
    if (diff < oneDay) {
        return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    } else if (diff < 7 * oneDay) {
        return `${Math.floor(diff / oneDay)}일 전`;
    } else {
        return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
    }
};

// 주기적으로 대화 목록 새로고침 (1분마다)
let refreshInterval = null;
onMounted(() => {
    if (isAuthenticated.value) {
        loadConversations();
        refreshInterval = setInterval(loadConversations, 60000);
    }
});

onUnmounted(() => {
    if (refreshInterval) clearInterval(refreshInterval);
});

// 라우트 변경 시 패널 닫기
watch(() => route.path, () => {
    isOpen.value = false;
});

// 인증 상태 변경 시 대화 목록 로드
watch(isAuthenticated, (val) => {
    if (val) {
        loadConversations();
    } else {
        conversations.value = [];
    }
});
</script>

<style scoped>
.scale-enter-active,
.scale-leave-active {
    transition: all 0.2s ease;
}
.scale-enter-from,
.scale-leave-to {
    opacity: 0;
    transform: scale(0.8);
}

.slide-up-enter-active,
.slide-up-leave-active {
    transition: all 0.25s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
}
</style>
