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
                class="absolute bottom-0 right-0 w-[360px] bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col"
                :style="{ height: viewMode === 'chat' ? '520px' : 'auto' }"
            >
                <!-- 헤더 - 대화 목록 -->
                <div v-if="viewMode === 'list'" class="flex items-center justify-between p-4 border-b border-slate-100 bg-brand-600 text-white shrink-0">
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

                <!-- 헤더 - 채팅 뷰 -->
                <div v-else class="flex items-center justify-between p-3 border-b border-slate-100 bg-slate-50 shrink-0">
                    <div class="flex items-center gap-3">
                        <button @click="goBack" class="p-1.5 hover:bg-slate-200 rounded-lg transition-colors text-slate-600">
                            <ChevronLeft :size="20" />
                        </button>
                        <img 
                            :src="getAvatar(activeChat?.partnerAvatar)" 
                            class="w-9 h-9 rounded-full border border-slate-200 bg-white object-cover"
                        />
                        <span class="font-bold text-slate-800 text-sm">{{ activeChat?.partnerName }}</span>
                    </div>
                    <div class="flex items-center gap-1">
                        <button @click="openFullView" class="p-1.5 hover:bg-slate-200 rounded-lg transition-colors text-slate-500" title="전체보기">
                            <Maximize2 :size="16" />
                        </button>
                        <button @click="togglePanel" class="p-1.5 hover:bg-slate-200 rounded-lg transition-colors text-slate-500" title="닫기">
                            <X :size="16" />
                        </button>
                    </div>
                </div>

                <!-- 대화 목록 뷰 -->
                <div v-if="viewMode === 'list'" class="max-h-[400px] overflow-y-auto">
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

                <!-- 채팅 뷰 -->
                <template v-else>
                    <!-- 메시지 목록 -->
                    <div class="flex-1 overflow-y-auto p-3 space-y-3 bg-slate-50" ref="messagesContainer">
                        <div v-if="messagesLoading" class="flex justify-center py-8">
                            <Loader2 class="animate-spin text-brand-500" />
                        </div>
                        <template v-else>
                            <template v-for="(msg, index) in messages" :key="msg.id">
                                <!-- Date Separator -->
                                <div v-if="showDateSeparator(index)" class="w-full flex justify-center my-4">
                                    <span class="text-[10px] font-bold text-slate-500 bg-slate-200/80 px-3 py-1 rounded-full">
                                        {{ formatDate(msg.createdAt) }}
                                    </span>
                                </div>

                                <!-- Message Item -->
                                <div 
                                    class="flex flex-col"
                                    :class="msg.isMine ? 'items-end' : 'items-start'"
                                >
                                    <div 
                                        class="max-w-[75%] px-3 py-2 rounded-2xl text-sm shadow-sm leading-relaxed whitespace-pre-wrap"
                                        :class="msg.isMine ? 'bg-brand-500 text-white rounded-tr-sm' : 'bg-white text-slate-700 border border-slate-200 rounded-tl-sm'"
                                    >
                                        {{ msg.content }}
                                    </div>
                                    <span class="text-[9px] text-slate-400 mt-0.5 px-1">{{ formatMsgTime(msg.createdAt) }}</span>
                                </div>
                            </template>
                        </template>
                    </div>

                    <!-- 입력 영역 -->
                    <div class="p-3 bg-white border-t border-slate-100 shrink-0">
                        <form @submit.prevent="sendMessage" class="flex items-center gap-2">
                            <input 
                                v-model="newMessage" 
                                type="text" 
                                placeholder="메시지 입력..." 
                                class="flex-1 px-3 py-2.5 bg-slate-100 border-0 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500/30 transition-all text-sm"
                                :disabled="sending"
                            />
                            <button 
                                type="submit" 
                                class="p-2.5 bg-brand-500 text-white rounded-xl hover:bg-brand-600 disabled:opacity-50 transition-all"
                                :disabled="!newMessage.trim() || sending"
                            >
                                <Send :size="18" />
                            </button>
                        </form>
                    </div>
                </template>
            </div>
        </Transition>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessageCircle, X, Maximize2, Loader2, ChevronLeft, Send } from 'lucide-vue-next';
import { socialApi } from '@/api/social';
import { useAuth } from '@/composables/useAuth';

const router = useRouter();
const route = useRoute();
const { user } = useAuth();

// user가 있으면 인증됨
const isAuthenticated = computed(() => !!user.value);

const isOpen = ref(false);
const loading = ref(false);
const conversations = ref([]);

// Chat view state
const viewMode = ref('list'); // 'list' or 'chat'
const activeChat = ref(null);
const messages = ref([]);
const messagesLoading = ref(false);
const newMessage = ref('');
const sending = ref(false);
const messagesContainer = ref(null);

let chatPollInterval = null;

const totalUnread = computed(() => {
    return conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0);
});

const togglePanel = () => {
    isOpen.value = !isOpen.value;
    if (isOpen.value) {
        viewMode.value = 'list';
        loadConversations();
    } else {
        stopChatPolling();
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
    activeChat.value = {
        partnerId: conv.partnerId,
        partnerName: conv.partnerName,
        partnerAvatar: conv.partnerAvatar,
        partnerDecoration: conv.partnerDecorationClass || ''
    };
    viewMode.value = 'chat';
    messages.value = [];
    fetchMessages();
    startChatPolling();
};

const goBack = () => {
    viewMode.value = 'list';
    activeChat.value = null;
    stopChatPolling();
    loadConversations(); // Refresh unread counts
};

const fetchMessages = async () => {
    if (!activeChat.value) return;
    if (messages.value.length === 0) messagesLoading.value = true;
    try {
        const res = await socialApi.getConversation(activeChat.value.partnerId);
        messages.value = res.data;
        if (messagesLoading.value) scrollToBottom();
    } catch (e) {
        console.error(e);
    } finally {
        messagesLoading.value = false;
    }
};

const sendMessage = async () => {
    if (!newMessage.value.trim() || sending.value || !activeChat.value) return;
    
    const content = newMessage.value;
    newMessage.value = '';
    sending.value = true;
    
    try {
        await socialApi.sendMessage(activeChat.value.partnerId, content);
        await fetchMessages();
        scrollToBottom();
    } catch (e) {
        console.error(e);
        newMessage.value = content;
        alert('전송 실패');
    } finally {
        sending.value = false;
    }
};

const scrollToBottom = () => {
    nextTick(() => {
        if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
        }
    });
};

const startChatPolling = () => {
    stopChatPolling();
    chatPollInterval = setInterval(fetchMessages, 3000);
};

const stopChatPolling = () => {
    if (chatPollInterval) {
        clearInterval(chatPollInterval);
        chatPollInterval = null;
    }
};

const openFullView = () => {
    router.push('/social?tab=messages');
    isOpen.value = false;
    stopChatPolling();
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

const formatMsgTime = (iso) => {
    const d = new Date(iso);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const formatDate = (iso) => {
    const d = new Date(iso);
    return d.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
};

const showDateSeparator = (index) => {
    if (index === 0) return true;
    const currentMsgDate = new Date(messages.value[index].createdAt).toLocaleDateString();
    const prevMsgDate = new Date(messages.value[index - 1].createdAt).toLocaleDateString();
    return currentMsgDate !== prevMsgDate;
};

// 주기적으로 대화 목록 새로고침 (1분마다)
let refreshInterval = null;
onMounted(() => {
    if (isAuthenticated.value) {
        loadConversations();
        refreshInterval = setInterval(() => {
            if (viewMode.value === 'list') loadConversations();
        }, 60000);
    }
});

onUnmounted(() => {
    if (refreshInterval) clearInterval(refreshInterval);
    stopChatPolling();
});

// 라우트 변경 시 패널 닫기
watch(() => route.path, () => {
    isOpen.value = false;
    stopChatPolling();
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
