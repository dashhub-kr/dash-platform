<template>
    <div class="min-h-screen bg-white text-slate-800 pb-20">
        <!-- 메인 레이아웃 컨테이너 -->
        <div class="flex justify-center p-4 md:p-8">
            <div class="flex gap-8 max-w-screen-xl w-full items-start">

                <!-- 왼쪽 컬럼: 메인 콘텐츠 -->
                <main class="flex-1 min-w-0 space-y-6">
                    <!-- Header -->
                    <div class="mb-8">
                        <h1 class="text-2xl font-black text-slate-800 flex items-center gap-3 mb-2">
                            <div class="w-10 h-10 bg-pink-500 rounded-xl flex items-center justify-center">
                                <Users class="w-6 h-6 text-white" :stroke-width="2.5" />
                            </div>
                            소셜
                        </h1>
                        <p class="text-slate-500 ml-[52px]">친구들과 함께 공부하고 소통해보세요</p>
                    </div>

                    <!-- Tab Switcher -->
                    <div class="flex bg-slate-100 p-1 rounded-xl font-bold w-fit">
                        <button 
                            v-for="tab in mainTabs" 
                            :key="tab.id"
                            @click="activeTab = tab.id"
                            class="px-5 py-2.5 rounded-lg text-sm transition-all"
                            :class="activeTab === tab.id ? 'bg-white text-brand-600 shadow-sm' : 'text-slate-400 hover:text-slate-600'"
                        >
                            {{ tab.label }}
                            <span v-if="tab.showBadge && tab.count > 0" class="ml-1 px-1.5 py-0.5 bg-rose-500 text-white text-[10px] rounded-full">{{ tab.count }}</span>
                        </button>
                    </div>

                    <!-- Content -->
                    <div class="bg-white rounded-3xl border border-slate-200 shadow-sm min-h-[500px] p-6">
                        
                        <!-- Tab: Messages (쪽지함) with Inline Chat -->
                        <div v-if="activeTab === 'messages'">
                            <!-- 대화 목록 뷰 -->
                            <div v-if="chatViewMode === 'list'" class="space-y-4">
                                <div v-if="loadingConversations" class="flex justify-center py-20"><Loader2 class="animate-spin text-brand-500"/></div>
                                <div v-else-if="conversations.length === 0" class="text-center py-20 text-slate-400">
                                    <MessageCircle :size="48" class="mx-auto mb-4 opacity-20"/>
                                    <p>아직 주고받은 쪽지가 없어요.</p>
                                </div>
                                <div v-else class="space-y-3">
                                    <div v-for="conv in conversations" :key="conv.partnerId" 
                                         @click="openInlineChat(conv)"
                                         class="flex items-center justify-between p-4 rounded-2xl border border-slate-100 hover:bg-brand-50 hover:border-brand-200 cursor-pointer transition-all group">
                                        <div class="flex items-center gap-4 flex-1 min-w-0">
                                            <div class="relative">
                                                <img :src="getAvatar(conv.partnerAvatar)" 
                                                     class="w-12 h-12 rounded-full border border-slate-200 bg-white object-cover"/>
                                                <div v-if="conv.unreadCount > 0" class="absolute -top-1 -right-1 min-w-[18px] h-[18px] bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1">
                                                    {{ conv.unreadCount > 9 ? '9+' : conv.unreadCount }}
                                                </div>
                                            </div>
                                            <div class="flex-1 min-w-0">
                                                <div class="flex items-center gap-2">
                                                    <NicknameRenderer 
                                                        :nickname="conv.partnerName" 
                                                        :decorationClass="conv.partnerDecorationClass"
                                                        :show-avatar="false"
                                                        class="text-base"
                                                    />
                                                    <span class="text-xs text-slate-400">{{ formatTime(conv.lastMessageTime) }}</span>
                                                </div>
                                                <p class="text-sm text-slate-500 truncate">{{ conv.lastMessagePreview || '메시지 없음' }}</p>
                                            </div>
                                        </div>
                                        <ChevronRight :size="20" class="text-slate-300 group-hover:text-brand-500 transition-colors" />
                                    </div>
                                </div>
                            </div>

                            <!-- 인라인 채팅 뷰 -->
                            <div v-else class="flex flex-col h-[600px]">
                                <!-- 채팅 헤더 -->
                                <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
                                    <button @click="goBackToList" class="p-2 hover:bg-slate-100 rounded-lg transition-colors text-slate-500">
                                        <ChevronLeft :size="20" />
                                    </button>
                                    <img :src="getAvatar(activeChat?.partnerAvatar)" class="w-10 h-10 rounded-full border border-slate-200"/>
                                    <div>
                                        <NicknameRenderer 
                                            :nickname="activeChat?.partnerName" 
                                            :decorationClass="activeChat?.partnerDecoration"
                                            :show-avatar="false"
                                            class="text-base font-bold"
                                        />
                                    </div>
                                </div>

                                <!-- 메시지 목록 -->
                                <div class="flex-1 overflow-y-auto py-4 space-y-3" ref="messagesContainer">
                                    <div v-if="messagesLoading" class="flex justify-center py-10">
                                        <Loader2 class="animate-spin text-brand-500" />
                                    </div>
                                    <template v-else>
                                        <template v-for="(msg, index) in messages" :key="msg.id">
                                            <!-- Date Separator -->
                                            <div v-if="showDateSeparator(index)" class="w-full flex justify-center my-4">
                                                <span class="text-[10px] font-bold text-slate-500 bg-slate-100 px-3 py-1 rounded-full">
                                                    {{ formatDate(msg.createdAt) }}
                                                </span>
                                            </div>

                                            <!-- Message Item -->
                                            <div 
                                                class="flex flex-col"
                                                :class="msg.isMine ? 'items-end' : 'items-start'"
                                            >
                                                <div 
                                                    class="max-w-[70%] px-4 py-2.5 rounded-2xl text-sm shadow-sm leading-relaxed whitespace-pre-wrap"
                                                    :class="msg.isMine ? 'bg-brand-500 text-white rounded-tr-sm' : 'bg-slate-100 text-slate-700 rounded-tl-sm'"
                                                >
                                                    {{ msg.content }}
                                                </div>
                                                <span class="text-[10px] text-slate-400 mt-1 px-1">{{ formatMsgTime(msg.createdAt) }}</span>
                                            </div>
                                        </template>
                                    </template>
                                </div>

                                <!-- 입력 영역 -->
                                <div class="pt-4 border-t border-slate-100">
                                    <form @submit.prevent="sendMessage" class="flex items-center gap-2">
                                        <input 
                                            v-model="newMessage" 
                                            type="text" 
                                            placeholder="메시지를 입력하세요..." 
                                            class="flex-1 px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 transition-all text-sm"
                                            :disabled="sending"
                                        />
                                        <button 
                                            type="submit" 
                                            class="p-3 bg-brand-500 text-white rounded-xl hover:bg-brand-600 disabled:opacity-50 transition-all"
                                            :disabled="!newMessage.trim() || sending"
                                        >
                                            <Send :size="20" />
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <!-- Tab: Friends -->
                        <div v-if="activeTab === 'friends'" class="space-y-4">
                            <div v-if="loading" class="flex justify-center py-20"><Loader2 class="animate-spin text-brand-500"/></div>
                            <div v-else-if="friends.length === 0" class="text-center py-20 text-slate-400">
                                <Users :size="48" class="mx-auto mb-4 opacity-20"/>
                                <p>아직 친구가 없어요. 오른쪽에서 친구를 찾아보세요!</p>
                            </div>
                            <div v-else class="space-y-3">
                                <div v-for="item in friends" :key="item.id" 
                                     class="flex items-center justify-between p-4 rounded-2xl border border-slate-100 hover:bg-slate-50 hover:border-slate-200 transition-all group">
                                    <div class="flex items-center gap-4">
                                        <img :src="(item.friend.avatarUrl && !item.friend.avatarUrl.includes('dicebear')) ? item.friend.avatarUrl : '/images/profiles/default-profile.png'" 
                                             class="w-12 h-12 rounded-full border-2 border-white shadow-sm bg-white object-cover"/>
                                        <div class="flex-1 min-w-0">
                                            <NicknameRenderer 
                                                :nickname="item.friend.username" 
                                                :decorationClass="item.friend.equippedDecorationClass"
                                                :role="item.friend.role"
                                                :show-avatar="false"
                                                class="text-base font-semibold"
                                            />
                                            <div class="text-xs text-slate-400 truncate mt-0.5">{{ item.friend.email }}</div>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-2">
                                         <button @click="openDM(item.friend.id, item.friend.username, item.friend.avatarUrl, item.friend.equippedDecorationClass)" 
                                                 class="p-2.5 bg-brand-500 text-white rounded-xl hover:bg-brand-600 transition-colors" title="쪽지 보내기">
                                            <MessageCircle :size="18"/>
                                         </button>
                                         <button @click="deleteFriend(item.friend.id)" 
                                                 class="p-2.5 bg-slate-100 text-slate-400 rounded-xl hover:bg-rose-100 hover:text-rose-500 transition-colors" title="친구 삭제">
                                            <UserMinus :size="18"/>
                                         </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Tab: Requests -->
                        <div v-if="activeTab === 'requests'" class="space-y-4">
                            <div v-if="loading" class="flex justify-center py-20"><Loader2 class="animate-spin text-brand-500"/></div>
                            <div v-else-if="requests.length === 0" class="text-center py-20 text-slate-400">
                                <Bell :size="48" class="mx-auto mb-4 opacity-20"/>
                                <p>받은 친구 요청이 없습니다.</p>
                            </div>
                            <div v-else class="space-y-3">
                                <div v-for="req in requests" :key="req.id" class="flex items-center justify-between p-4 rounded-2xl border border-slate-100 hover:bg-slate-50">
                                    <div class="flex items-center gap-4">
                                         <img :src="(req.friend.avatarUrl && !req.friend.avatarUrl.includes('dicebear')) ? req.friend.avatarUrl : '/images/profiles/default-profile.png'" class="w-10 h-10 rounded-full border border-slate-200 bg-white object-cover"/>
                                         <div>
                                            <NicknameRenderer 
                                                :nickname="req.friend.username" 
                                                :decorationClass="req.friend.equippedDecorationClass" 
                                                :role="req.friend.role"
                                                :show-avatar="false"
                                                class="text-base"
                                            />
                                            <div class="text-xs text-slate-400">님이 친구 신청을 보냈습니다.</div>
                                         </div>
                                    </div>
                                    <div class="flex gap-2">
                                        <button @click="acceptRequest(req.id)" class="px-4 py-2 bg-brand-500 text-white rounded-xl text-sm font-bold hover:bg-brand-600 transition-colors">수락</button>
                                        <button @click="rejectRequest(req.id)" class="px-4 py-2 bg-slate-200 text-slate-600 rounded-xl text-sm font-bold hover:bg-slate-300 transition-colors">거절</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>

                <!-- 오른쪽 컬럼: 사이드바 (친구 검색) -->
                <aside class="hidden lg:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-fit">
                    <!-- 친구 검색 -->
                    <div class="bg-white rounded-3xl border border-slate-200 shadow-sm p-6">
                        <h2 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                            <div class="w-8 h-8 bg-violet-500 rounded-lg flex items-center justify-center">
                                <Search :size="16" class="text-white" />
                            </div>
                            친구 찾기
                        </h2>
                        <div class="relative mb-4">
                            <Search :size="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
                            <input 
                                v-model="searchQuery" 
                                @keyup.enter="handleSearch"
                                type="text" 
                                placeholder="이메일 또는 닉네임" 
                                class="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-200 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 transition-all font-medium text-slate-700 bg-slate-50 focus:bg-white"
                            />
                        </div>
                        <button 
                            @click="handleSearch"
                            class="w-full py-3 bg-slate-900 text-white rounded-xl font-bold hover:bg-brand-600 transition-colors"
                        >
                            검색
                        </button>

                        <!-- 검색 결과 -->
                        <div v-if="searchLoading" class="flex justify-center py-6"><Loader2 class="animate-spin text-brand-500"/></div>
                        <div v-else-if="searchResults" class="mt-4 space-y-3 max-h-[400px] overflow-y-auto">
                            <div v-if="searchResults.length === 0" class="text-center py-6 text-slate-400 text-sm">
                                검색 결과가 없습니다.
                            </div>
                            <div v-for="user in searchResults" :key="user.id" class="flex items-center justify-between p-3 rounded-xl border border-slate-100 hover:bg-slate-50 transition-colors">
                                <div class="flex items-center gap-3 flex-1 min-w-0">
                                    <img :src="(user.avatarUrl && !user.avatarUrl.includes('dicebear')) ? user.avatarUrl : '/images/profiles/default-profile.png'" class="w-9 h-9 rounded-full border border-slate-200 bg-white object-cover"/>
                                    <div class="min-w-0">
                                        <NicknameRenderer 
                                            :nickname="user.username" 
                                            :decorationClass="user.equippedDecorationClass"
                                            :role="user.role"
                                            :show-avatar="false"
                                            class="text-sm"
                                        />
                                        <div class="text-[10px] text-slate-400 truncate">{{ user.email }}</div>
                                    </div>
                                </div>
                                <div v-if="user.friendshipStatus === 'ACCEPTED'" class="px-2 py-1 bg-slate-100 text-slate-400 text-[10px] font-bold rounded-md flex items-center gap-1">
                                    <Users :size="12"/> 친구
                                </div>
                                <button
                                    v-else-if="user.friendshipStatus === 'PENDING' || user.requested"
                                    disabled
                                    class="px-2 py-1 bg-slate-200 text-slate-500 text-[10px] font-bold rounded-md cursor-not-allowed flex items-center gap-1"
                                >
                                    <CheckCircle2 :size="12"/> 요청됨
                                </button>
                                <button 
                                    v-else
                                    @click="sendRequest(user)" 
                                    class="px-2 py-1 bg-slate-900 text-white text-[10px] font-bold rounded-md hover:bg-brand-600 transition-colors flex items-center gap-1"
                                >
                                    <UserPlus :size="12"/> 신청
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- 가이드 -->
                    <div class="bg-gradient-to-br from-pink-50 to-violet-50 rounded-3xl p-6 border border-pink-100">
                        <h3 class="font-bold text-slate-700 text-sm mb-2 flex items-center gap-2">
                            <div class="w-6 h-6 bg-pink-500 rounded-lg flex items-center justify-center text-white">
                                <MessageCircle :size="12" />
                            </div>
                            친구와 함께해요
                        </h3>
                        <p class="text-xs text-slate-500 leading-relaxed">
                            같은 문제를 풀거나 서로의 코드를 리뷰하며 함께 성장할 수 있어요!
                        </p>
                    </div>
                </aside>

            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, computed, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { socialApi } from '@/api/social';
import { Loader2, Users, Bell, Search, UserPlus, MessageCircle, UserMinus, CheckCircle2, ChevronRight, ChevronLeft, Send } from 'lucide-vue-next';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';
import { useDirectMessageModal } from '@/composables/useDirectMessageModal';

const route = useRoute();
const activeTab = ref('messages');
const loading = ref(false);

const friends = ref([]);
const requests = ref([]);

// 메인 탭 (검색 제외 - 사이드바로 이동)
const mainTabs = computed(() => [
    { id: 'messages', label: '쪽지함', count: conversations.value.filter(c => c.unreadCount > 0).length, showBadge: true },
    { id: 'friends', label: '내 친구', count: 0, showBadge: false },
    { id: 'requests', label: '친구 요청', count: requests.value.length, showBadge: true },
]);

// 검색 (Search)
const searchQuery = ref('');
const searchLoading = ref(false);
const searchResults = ref(null);

// 쪽지함 (Conversations)
const conversations = ref([]);
const loadingConversations = ref(false);

// 인라인 채팅 상태
const chatViewMode = ref('list'); // 'list' or 'chat'
const activeChat = ref(null);
const messages = ref([]);
const messagesLoading = ref(false);
const newMessage = ref('');
const sending = ref(false);
const messagesContainer = ref(null);
let chatPollInterval = null;

// 쪽지 (DM) - 모달용 (친구 목록에서 사용)
const { open: openGlobalDM } = useDirectMessageModal();

const loadData = async () => {
    loading.value = true;
    try {
        const [friendsRes, requestsRes] = await Promise.all([
            socialApi.getFriends(),
            socialApi.getReceivedRequests()
        ]);
        friends.value = friendsRes.data;
        requests.value = requestsRes.data;
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

const loadConversations = async () => {
    loadingConversations.value = true;
    try {
        const res = await socialApi.getConversations();
        conversations.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        loadingConversations.value = false;
    }
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
        const days = Math.floor(diff / oneDay);
        return `${days}일 전`;
    } else {
        return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
    }
};

// 인라인 채팅 함수들
const getAvatar = (url) => {
    if (url && !url.includes('dicebear')) return url;
    return '/images/profiles/default-profile.png';
};

const openInlineChat = (conv) => {
    activeChat.value = {
        partnerId: conv.partnerId,
        partnerName: conv.partnerName,
        partnerAvatar: conv.partnerAvatar,
        partnerDecoration: conv.partnerDecorationClass || ''
    };
    chatViewMode.value = 'chat';
    messages.value = [];
    fetchMessages();
    startChatPolling();
};

const goBackToList = () => {
    chatViewMode.value = 'list';
    activeChat.value = null;
    stopChatPolling();
    loadConversations();
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

const handleSearch = async () => {
    if (!searchQuery.value.trim()) return;
    searchLoading.value = true;
    try {
        const res = await socialApi.searchUsers(searchQuery.value);
        searchResults.value = res.data;
    } catch(e) {
        console.error(e);
    } finally {
        searchLoading.value = false;
    }
};

const sendRequest = async (user) => {
    if (!confirm('친구 신청을 보내시겠습니까?')) return;
    try {
        await socialApi.sendFriendRequest(user.id);
        user.requested = true;
        alert('친구 신청을 보냈습니다.');
    } catch(e) {
        alert(e.response?.data?.message || '실패했습니다.');
    }
};

const acceptRequest = async (requestId) => {
    try {
        await socialApi.acceptFriendRequest(requestId);
        loadData();
    } catch(e) {
        console.error(e);
    }
};

const rejectRequest = async (requestId) => {
    if (!confirm('거절하시겠습니까?')) return;
    try {
        await socialApi.rejectFriendRequest(requestId);
        loadData();
    } catch(e) {
        console.error(e);
    }
};

const deleteFriend = async (friendId) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
        await socialApi.deleteFriend(friendId);
        loadData();
    } catch(e) {
        console.error(e);
    }
};

const openDM = (id, name, avatar, decoration) => {
    openGlobalDM({
        partnerId: id,
        partnerName: name,
        partnerAvatar: avatar,
        partnerDecoration: decoration || ''
    });
};

// 라우트 쿼리 핸들러 (예: 알림 클릭 시)
const checkQueryForDM = async () => {
    const pid = route.query.partnerId;
    if (pid) {
        const partnerId = parseInt(pid);
        const friend = friends.value.find(f => f.friend.id === partnerId);
        if (friend) {
            openGlobalDM({
                partnerId: friend.friend.id,
                partnerName: friend.friend.username,
                partnerAvatar: friend.friend.avatarUrl,
                partnerDecoration: friend.friend.equippedDecorationClass || ''
            });
        } else {
            openGlobalDM({
                partnerId: partnerId,
                partnerName: 'User',
                partnerAvatar: null,
                partnerDecoration: ''
            });
        }
    }
};

watch(() => route.query.tab, (val) => {
    if (val && ['messages', 'friends', 'requests'].includes(val)) {
        activeTab.value = val;
    }
}, { immediate: true });

watch(() => route.query.partnerId, async (val) => {
    if (val) {
        if (friends.value.length === 0 && !loading.value) {
            await loadData();
        }
        checkQueryForDM();
    }
}, { immediate: true });

onMounted(async () => {
    await Promise.all([loadData(), loadConversations()]);
    checkQueryForDM(); 
});

watch(activeTab, () => {
    // 탭 변경 시 인라인 채팅 초기화
    if (chatViewMode.value === 'chat') {
        goBackToList();
    }
    
    if (activeTab.value === 'messages') {
        loadConversations();
    } else {
        loadData();
    }
});

onBeforeUnmount(() => {
    stopChatPolling();
});
</script>
