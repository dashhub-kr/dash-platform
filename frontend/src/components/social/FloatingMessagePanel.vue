<template>
    <div class="fixed bottom-6 right-6 z-[50]">
        <!-- 메인 플로팅 버튼 -->
        <button 
            @click="toggle"
            class="w-14 h-14 bg-brand-500 hover:bg-brand-600 active:scale-95 text-white rounded-full shadow-lg shadow-brand-500/30 flex items-center justify-center transition-all duration-300 relative group"
        >
            <div class="relative">
                <MessageCircle :size="26" stroke-width="2.5" :class="{ 'scale-0 opacity-0': isOpen, 'scale-100 opacity-100': !isOpen }" class="transition-all duration-300 absolute inset-0 m-auto" />
                <X :size="26" stroke-width="2.5" :class="{ 'scale-100 opacity-100': isOpen, 'scale-0 opacity-0': !isOpen }" class="transition-all duration-300" />
            </div>
            
            <!-- 안읽음 뱃지 -->
            <div v-if="!isOpen && totalUnread > 0" class="absolute -top-1 -right-1 bg-rose-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full ring-2 ring-white animate-pulse">
                {{ totalUnread > 99 ? '99+' : totalUnread }}
            </div>
        </button>

        <!-- 패널 -->
        <Transition name="slide-up">
            <div v-if="isOpen" class="absolute bottom-16 right-0 w-[380px] h-[600px] bg-white rounded-2xl shadow-2xl border border-slate-100 flex flex-col overflow-hidden origin-bottom-right">
                
                <!-- 상단 헤더 -->
                <div class="h-14 shrink-0 bg-brand-500 px-4 flex items-center justify-between shadow-sm z-10">
                    <div class="flex items-center gap-2 text-white overflow-hidden">
                        <button v-if="viewMode !== 'list'" @click="goBack" class="p-1 hover:bg-white/20 rounded-full transition-colors">
                            <ChevronLeft :size="20" />
                        </button>
                        <h2 class="font-bold text-lg truncate">
                            {{ headerTitle }}
                        </h2>
                        <span v-if="viewMode === 'list' && totalUnread > 0" class="px-2 py-0.5 bg-white/20 rounded-full text-xs">{{ totalUnread }}</span>
                    </div>
                    <div class="flex items-center gap-1">
                        <button @click="openCreateModal" class="p-1.5 hover:bg-white/20 rounded-lg transition-colors" title="새 대화">
                            <Plus :size="16" class="text-white" />
                        </button>
                        <button @click="openFullView" class="p-1.5 hover:bg-white/20 rounded-lg transition-colors" title="전체보기">
                            <Maximize2 :size="16" class="text-white" />
                        </button>
                    </div>
                </div>

                <!-- 1. 대화 목록 뷰 -->
                <div v-if="viewMode === 'list'" class="flex-1 flex flex-col overflow-hidden bg-slate-50">
                    <div v-if="loading" class="flex-1 flex items-center justify-center">
                        <Loader2 class="animate-spin text-brand-500" />
                    </div>
                    
                    <div v-else-if="allConversations.length > 0" class="flex-1 overflow-y-auto">
                        <div 
                            v-for="conv in allConversations" 
                            :key="conv.key"
                            @click="conv.isGroup ? openGroupChat(conv) : openChat(conv)"
                            class="p-4 bg-white hover:bg-slate-50 border-b border-slate-50 cursor-pointer transition-colors relative"
                        >
                            <div class="flex gap-3">
                                <div class="relative shrink-0">
                                    <img :src="getAvatar(conv.isGroup ? null : conv.partnerAvatar)" class="w-12 h-12 rounded-full object-cover border border-slate-100" />
                                    <div v-if="conv.isGroup" class="absolute -bottom-1 -right-1 bg-indigo-500 text-white rounded-full p-0.5 border-2 border-white">
                                        <Users :size="10" />
                                    </div>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex items-center justify-between mb-0.5">
                                        <h3 class="font-bold text-slate-800 text-sm truncate flex items-center gap-1">
                                            {{ conv.name }}
                                            <span v-if="conv.isGroup" class="text-xs font-normal text-slate-500">({{ conv.memberCount }})</span>
                                        </h3>
                                        <span class="text-[10px] text-slate-400 shrink-0">{{ formatTime(conv.lastMessageTime) }}</span>
                                    </div>
                                    <p class="text-xs text-slate-500 truncate">{{ conv.lastMessagePreview || '대화가 없습니다.' }}</p>
                                </div>
                            </div>
                            <!-- Unread Badge -->
                            <div v-if="conv.unreadCount > 0" class="absolute right-4 bottom-4 min-w-[18px] h-[18px] bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1">
                                {{ conv.unreadCount }}
                            </div>
                        </div>
                    </div>

                    <div v-else class="flex-1 flex flex-col items-center justify-center text-slate-400 p-8">
                        <MessageCircle :size="32" class="mb-2 opacity-30" />
                        <p class="text-sm">아직 대화가 없어요</p>
                        <button @click="openCreateModal" class="mt-3 text-brand-500 text-sm font-bold hover:underline">
                            + 새 대화 시작하기
                        </button>
                    </div>
                </div>

                <!-- 2. 1:1 채팅 뷰 -->
                <div v-else-if="viewMode === 'chat'" class="flex-1 flex flex-col overflow-hidden bg-white">
                    <div 
                        ref="messagesContainer"
                        class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/50"
                    >
                        <div v-if="messagesLoading" class="flex justify-center py-4">
                            <Loader2 class="animate-spin text-slate-300" :size="20" />
                        </div>
                        
                        <template v-else>
                            <div v-for="(msg, index) in messages" :key="msg.id">
                                <!-- 날짜 구분선 -->
                                <div v-if="showDateSeparator(index)" class="flex items-center justify-center my-4">
                                    <span class="text-[10px] bg-slate-100 text-slate-500 px-3 py-1 rounded-full font-medium">
                                        {{ formatDate(msg.createdAt) }}
                                    </span>
                                </div>

                                <div 
                                    class="flex gap-2 max-w-[85%]"
                                    :class="msg.senderId === user.id ? 'ml-auto flex-row-reverse' : ''"
                                >
                                    <img 
                                        v-if="msg.senderId !== user.id" 
                                        :src="getAvatar(activeChat?.partnerAvatar, isPartnerDeleted)" 
                                        class="w-8 h-8 rounded-full self-start border border-slate-100" 
                                    />
                                    
                                    <div class="flex flex-col gap-1" :class="msg.senderId === user.id ? 'items-end' : 'items-start'">
                                        <div 
                                            class="px-3 py-2 rounded-2xl text-sm leading-relaxed shadow-sm break-keep"
                                            :class="msg.senderId === user.id ? 'bg-brand-500 text-white rounded-br-none' : 'bg-white border border-slate-100 text-slate-700 rounded-bl-none'"
                                        >
                                            {{ msg.content }}
                                        </div>
                                        <span class="text-[10px] text-slate-400 px-1">
                                            {{ formatMsgTime(msg.createdAt) }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </template>

                        <div v-if="isPartnerDeleted" class="flex justify-center my-4">
                            <div class="flex items-center gap-2 px-4 py-2 bg-slate-100 rounded-lg opacity-70">
                                <AlertCircle :size="14" class="text-slate-500" />
                                <span class="text-xs text-slate-500">상대방이 탈퇴하여 대화를 보낼 수 없습니다.</span>
                            </div>
                        </div>
                    </div>

                    <!-- 입력창 -->
                    <div class="p-3 bg-white border-t border-slate-100 shrink-0">
                        <form @submit.prevent="sendMessage" class="relative flex items-end gap-2 bg-slate-50 p-2 rounded-xl border border-slate-200 focus-within:border-brand-300 focus-within:ring-2 focus-within:ring-brand-100 transition-all">
                            <textarea 
                                v-model="newMessage"
                                @keydown.enter.prevent="sendMessage"
                                placeholder="메시지를 입력하세요..." 
                                class="flex-1 bg-transparent border-none focus:ring-0 text-sm p-1 max-h-20 resize-none placeholder:text-slate-400"
                                rows="1"
                                :disabled="sending || isPartnerDeleted"
                            ></textarea>
                            <button 
                                type="submit" 
                                :disabled="!newMessage.trim() || sending || isPartnerDeleted"
                                class="p-2 bg-brand-500 hover:bg-brand-600 active:scale-95 disabled:opacity-50 disabled:active:scale-100 text-white rounded-lg transition-all"
                            >
                                <Send :size="16" />
                            </button>
                        </form>
                    </div>
                </div>

                <!-- 3. 그룹 채팅 뷰 -->
                <div v-else-if="viewMode === 'groupChat'" class="flex-1 flex flex-col overflow-hidden bg-white">
                     <div 
                        ref="groupMessagesContainer"
                        class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/50"
                    >
                        <div v-for="msg in groupMessages" :key="msg.id" 
                            class="flex gap-2 max-w-[85%]"
                            :class="msg.senderId === user.id ? 'ml-auto flex-row-reverse' : ''"
                        >
                            <img 
                                v-if="msg.senderId !== user.id" 
                                :src="getAvatar(msg.senderAvatarUrl)" 
                                class="w-8 h-8 rounded-full self-start border border-slate-100" 
                                :title="msg.senderUsername"
                            />
                            
                            <div class="flex flex-col gap-1" :class="msg.senderId === user.id ? 'items-end' : 'items-start'">
                                <span v-if="msg.senderId !== user.id" class="text-[10px] text-slate-500 ml-1 truncate max-w-[100px]">{{ msg.senderUsername }}</span>
                                <div 
                                    class="px-3 py-2 rounded-2xl text-sm leading-relaxed shadow-sm break-keep"
                                    :class="msg.senderId === user.id ? 'bg-indigo-500 text-white rounded-br-none' : 'bg-white border border-slate-100 text-slate-700 rounded-bl-none'"
                                >
                                    {{ msg.content }}
                                </div>
                                <span class="text-[10px] text-slate-400 px-1">
                                    {{ formatMsgTime(msg.createdAt) }}
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- 입력창 (그룹) -->
                    <div class="p-3 bg-white border-t border-slate-100 shrink-0">
                        <form @submit.prevent="sendGroupMessage" class="relative flex items-end gap-2 bg-slate-50 p-2 rounded-xl border border-slate-200 focus-within:border-indigo-300 focus-within:ring-2 focus-within:ring-indigo-100 transition-all">
                            <textarea 
                                v-model="newGroupMessage"
                                @keydown.enter.prevent="sendGroupMessage"
                                placeholder="메시지를 입력하세요..." 
                                class="flex-1 bg-transparent border-none focus:ring-0 text-sm p-1 max-h-20 resize-none placeholder:text-slate-400"
                                rows="1"
                                :disabled="groupSending"
                            ></textarea>
                            <button 
                                type="submit" 
                                :disabled="!newGroupMessage.trim() || groupSending"
                                class="p-2 bg-indigo-500 hover:bg-indigo-600 active:scale-95 disabled:opacity-50 disabled:active:scale-100 text-white rounded-lg transition-all"
                            >
                                <Send :size="16" />
                            </button>
                        </form>
                    </div>
                </div>

            </div>
        </Transition>

        <!-- 새 대화 모달 (통합) -->
        <Transition name="fade">
            <div v-if="showCreateModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4" @click.self="showCreateModal = false">
                <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm"></div>
                <div class="relative bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden flex flex-col max-h-[600px]">
                    <div class="p-4 border-b border-slate-100 flex items-center justify-between shrink-0">
                        <h3 class="font-bold text-slate-800">새 대화 시작</h3>
                        <button @click="showCreateModal = false" class="p-1.5 hover:bg-slate-100 rounded-lg">
                            <X :size="18" class="text-slate-400" />
                        </button>
                    </div>

                    <div class="p-4 flex flex-col min-h-0 flex-1">
                        <!-- 검색 -->
                        <div class="mb-4 shrink-0">
                            <input 
                                v-model="friendSearch"
                                type="text" 
                                placeholder="이름으로 검색..."
                                class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:bg-white transition-all"
                            />
                        </div>

                        <div class="flex-1 overflow-y-auto min-h-0 space-y-6 pr-1">


                            <!-- 친구 목록 -->
                            <div>
                                <h4 class="text-xs font-bold text-slate-500 mb-2 px-1 flex justify-between items-center">
                                    친구 목록
                                    <span v-if="selectedFriendIds.length > 0" class="text-indigo-600">{{ selectedFriendIds.length }}명 선택됨</span>
                                </h4>
                                <div class="space-y-1">
                                    <div 
                                        v-for="friend in filteredFriends" 
                                        :key="friend.friend?.id" 
                                        @click="toggleFriendSelection(friend.friend?.id)"
                                        class="flex items-center gap-3 p-2 hover:bg-slate-50 rounded-xl cursor-pointer transition-colors"
                                        :class="{ 'bg-indigo-50/50': selectedFriendIds.includes(friend.friend?.id) }"
                                    >
                                        <div class="relative w-10 h-10 shrink-0">
                                            <img :src="getAvatar(friend.friend?.avatarUrl)" class="w-full h-full rounded-full object-cover border border-slate-200" />
                                            <div 
                                                v-if="selectedFriendIds.includes(friend.friend?.id)"
                                                class="absolute -bottom-1 -right-1 w-5 h-5 bg-indigo-500 rounded-full border-2 border-white flex items-center justify-center"
                                            >
                                                <Check :size="12" class="text-white" stroke-width="3" />
                                            </div>
                                        </div>
                                        <div class="flex-1 min-w-0">
                                            <div class="text-sm font-bold text-slate-700 truncate">{{ friend.friend?.username }}</div>
                                            <div class="text-xs text-slate-500 truncate">{{ friend.friend?.tier || 'Unrated' }}</div>
                                        </div>
                                        <div 
                                            class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors"
                                            :class="selectedFriendIds.includes(friend.friend?.id) ? 'border-indigo-500 bg-indigo-500' : 'border-slate-300'"
                                        >
                                            <Check v-if="selectedFriendIds.includes(friend.friend?.id)" :size="12" class="text-white" stroke-width="3" />
                                        </div>
                                    </div>
                                    <div v-if="filteredFriends.length === 0" class="py-10 text-center text-sm text-slate-400">
                                        {{ friendSearch ? '검색 결과가 없습니다' : '친구가 없습니다' }}
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 하단 액션 버튼 -->
                        <div class="pt-4 mt-auto border-t border-slate-100 shrink-0">
                            <button 
                                @click="handleStartChat"
                                :disabled="selectedFriendIds.length === 0 || creating"
                                class="w-full py-3.5 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-200 disabled:text-slate-400 text-white rounded-xl font-bold transition-all shadow-lg shadow-indigo-500/20 disabled:shadow-none flex items-center justify-center gap-2"
                            >
                                <Loader2 v-if="creating" class="animate-spin" :size="20" />
                                <span v-else>{{ startButtonText }}</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </Transition>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { MessageCircle, X, Maximize2, Loader2, ChevronLeft, Send, AlertCircle, Plus, Users, BookOpen, Check } from 'lucide-vue-next';
import { socialApi } from '@/api/social';
import { chatApi } from '@/api/chat';
import { userApi } from '@/api/user';
import { useAuth } from '@/composables/useAuth';
import { useFloatingChat } from '@/composables/useFloatingChat';

const router = useRouter();
const route = useRoute();
const { user } = useAuth();
const { isOpen, activeChat, toggle } = useFloatingChat();

const isAuthenticated = computed(() => !!user.value);

const loading = ref(false);
const conversations = ref([]);
const groupRooms = ref([]);

// Chat view state
const viewMode = ref('list'); // 'list', 'chat', 'groupChat'
const messages = ref([]);
const messagesLoading = ref(false);
const newMessage = ref('');
const sending = ref(false);
const messagesContainer = ref(null);
const isPartnerDeleted = ref(false);

// Group chat state
const activeGroupRoom = ref(null);
const groupMessages = ref([]);
const groupMessagesLoading = ref(false);
const newGroupMessage = ref('');
const groupSending = ref(false);
const showGroupMembers = ref(false);
const groupMessagesContainer = ref(null);

// Create modal state
const showCreateModal = ref(false);
const friends = ref([]);
const friendSearch = ref('');
const creating = ref(false);
const selectedFriendIds = ref([]);
const groupForm = ref({ name: '', memberIds: [] }); // 호환성 유지

let chatPollInterval = null;

// 헤더 타이틀
const headerTitle = computed(() => {
    if (viewMode.value === 'list') return '메시지';
    if (viewMode.value === 'chat') return activeChat.value?.partnerName;
    if (viewMode.value === 'groupChat') return activeGroupRoom.value?.name;
    return '메시지';
});

// 1:1 + 그룹 통합 목록
const allConversations = computed(() => {
    const dmList = conversations.value.map(c => ({
        key: `dm-${c.partnerId}`,
        isGroup: false,
        name: c.partnerName,
        partnerId: c.partnerId,
        partnerName: c.partnerName,
        partnerAvatar: c.partnerAvatar,
        lastMessagePreview: c.lastMessagePreview,
        lastMessageTime: c.lastMessageTime,
        unreadCount: c.unreadCount || 0
    }));

    const groupList = groupRooms.value.map(r => ({
        key: `group-${r.id}`,
        isGroup: true,
        id: r.id,
        name: r.name,
        type: r.type,
        memberCount: r.memberCount,
        lastMessagePreview: r.lastMessage,
        lastMessageTime: r.lastMessageAt,
        unreadCount: r.unreadCount || 0
    }));

    // 시간순 정렬
    return [...dmList, ...groupList].sort((a, b) => {
        const timeA = a.lastMessageTime ? new Date(a.lastMessageTime) : new Date(0);
        const timeB = b.lastMessageTime ? new Date(b.lastMessageTime) : new Date(0);
        return timeB - timeA;
    });
});

const totalUnread = computed(() => {
    return allConversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0);
});

// 내 스터디 채팅방 목록
const myStudyRooms = computed(() => {
    return groupRooms.value.filter(room => room.type === 'STUDY');
});

const filteredFriends = computed(() => {
    if (!friendSearch.value) return friends.value;
    const q = friendSearch.value.toLowerCase();
    return friends.value.filter(f => f.friend?.username?.toLowerCase().includes(q));
});

// 버튼 텍스트
const startButtonText = computed(() => {
    const count = selectedFriendIds.value.length;
    if (count === 0) return '대화 상대를 선택해주세요';
    if (count === 1) return '1:1 대화 시작';
    return `${count}명과 그룹 대화 시작`;
});

// Watch isOpen
watch(isOpen, (newVal) => {
    if (newVal) {
        if (!activeChat.value && !activeGroupRoom.value) {
            viewMode.value = 'list';
            loadAllConversations();
        }
    } else {
        stopChatPolling();
    }
});

// Watch activeChat for 1:1
watch(activeChat, async (newVal) => {
    if (newVal) {
        viewMode.value = 'chat';
        messages.value = [];
        isPartnerDeleted.value = false;
        
        try {
            const res = await userApi.getById(newVal.partnerId);
            if (res.data.isDeleted) isPartnerDeleted.value = true;
        } catch (e) {
            console.error("Failed to check partner status", e);
        }

        fetchMessages();
        startChatPolling();
    }
});

const loadAllConversations = async () => {
    if (!isAuthenticated.value) return;
    loading.value = true;
    try {
        // DM 목록 로드
        try {
            const dmRes = await socialApi.getConversations();
            conversations.value = dmRes.data;
        } catch (e) {
            console.error('Failed to load DM conversations:', e);
            conversations.value = [];
        }

        // 그룹 채팅방 목록 로드
        try {
            const groupRes = await chatApi.getRooms();
            groupRooms.value = groupRes.data;
        } catch (e) {
            console.error('Failed to load group rooms:', e);
            groupRooms.value = [];
        }
    } finally {
        loading.value = false;
    }
};

const openChat = (conv) => {
    activeChat.value = {
        partnerId: conv.partnerId,
        partnerName: conv.partnerName,
        partnerAvatar: conv.partnerAvatar,
        partnerDecoration: ''
    };
};

const openGroupChat = async (room) => {
    viewMode.value = 'groupChat';
    activeGroupRoom.value = room;
    showGroupMembers.value = false;
    groupMessagesLoading.value = true;

    try {
        const [detailRes, msgRes] = await Promise.all([
            chatApi.getRoom(room.id),
            chatApi.getMessages(room.id)
        ]);
        activeGroupRoom.value = { ...room, ...detailRes.data };
        groupMessages.value = msgRes.data.reverse();
        await chatApi.markAsRead(room.id);
        nextTick(() => scrollToBottomGroup());
        startGroupPolling();
    } catch (e) {
        console.error(e);
    } finally {
        groupMessagesLoading.value = false;
    }
};

const goBack = () => {
    viewMode.value = 'list';
    activeChat.value = null;
    activeGroupRoom.value = null;
    isPartnerDeleted.value = false;
    showGroupMembers.value = false;
    stopChatPolling();
    loadAllConversations();
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
    if (!newMessage.value.trim() || sending.value || !activeChat.value || isPartnerDeleted.value) return;
    
    const content = newMessage.value;
    newMessage.value = '';
    sending.value = true;
    
    try {
        await socialApi.sendMessage(activeChat.value.partnerId, content);
        await fetchMessages();
        scrollToBottom();
    } catch (e) {
        newMessage.value = content;
        console.error(e);
    } finally {
        sending.value = false;
    }
};

const sendGroupMessage = async () => {
    if (!newGroupMessage.value.trim() || groupSending.value || !activeGroupRoom.value) return;

    const content = newGroupMessage.value;
    newGroupMessage.value = '';
    groupSending.value = true;

    try {
        const res = await chatApi.sendMessage(activeGroupRoom.value.id, content);
        groupMessages.value.push(res.data);
        nextTick(() => scrollToBottomGroup());
    } catch (e) {
        newGroupMessage.value = content;
        console.error(e);
        alert('메시지 전송 실패');
    } finally {
        groupSending.value = false;
    }
};


const scrollToBottom = () => {
    nextTick(() => {
        if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
        }
    });
};

const scrollToBottomGroup = () => {
    nextTick(() => {
        if (groupMessagesContainer.value) {
            groupMessagesContainer.value.scrollTop = groupMessagesContainer.value.scrollHeight;
        }
    });
};

const fetchGroupMessages = async () => {
    if (!activeGroupRoom.value) return;
    try {
        const res = await chatApi.getMessages(activeGroupRoom.value.id);
        if (res.data.length !== groupMessages.value.length) {
            groupMessages.value = res.data.reverse();
            nextTick(() => scrollToBottomGroup());
        }
    } catch (e) {
        console.error(e);
    }
};

const startChatPolling = () => {
    stopChatPolling();
    chatPollInterval = setInterval(() => {
        if (viewMode.value === 'chat' && activeChat.value) {
            fetchMessages();
        } else if (viewMode.value === 'groupChat' && activeGroupRoom.value) {
            fetchGroupMessages();
        }
    }, 3000);
};

const startGroupPolling = () => {
    startChatPolling();
};

const stopChatPolling = () => {
    if (chatPollInterval) {
        clearInterval(chatPollInterval);
        chatPollInterval = null;
    }
};

// ============================================
// New Create Modal Functions
// ============================================

const openCreateModal = async () => {
    showCreateModal.value = true;
    selectedFriendIds.value = [];
    friendSearch.value = '';
    
    // 친구 목록 로드
    try {
        const res = await socialApi.getFriends();
        friends.value = res.data;
    } catch (e) {
        console.error(e);
    }
    
    // 스터디 목록 업데이트를 위해 채팅방 목록도 새로고침
    try {
        const groupRes = await chatApi.getRooms();
        groupRooms.value = groupRes.data;
    } catch (e) {
        // 무시
    }
};

const toggleFriendSelection = (friendId) => {
    const index = selectedFriendIds.value.indexOf(friendId);
    if (index === -1) {
        selectedFriendIds.value.push(friendId);
    } else {
        selectedFriendIds.value.splice(index, 1);
    }
};

const openStudyRoom = (room) => {
    openGroupChat(room);
    showCreateModal.value = false;
};

const handleStartChat = async () => {
    if (selectedFriendIds.value.length === 0) return;
    
    creating.value = true;
    try {
        if (selectedFriendIds.value.length === 1) {
            // 1:1 대화
            const friendId = selectedFriendIds.value[0];
            const friend = friends.value.find(f => f.friend?.id === friendId)?.friend;
            if (friend) {
                startDMWithFriend(friend);
            }
        } else {
            // 그룹 대화
            // 이름 자동 생성: "나, 친구1, 친구2..."
            const selectedFriends = friends.value
                .filter(f => selectedFriendIds.value.includes(f.friend?.id))
                .map(f => f.friend?.username);
            
            // 내 이름도 포함하거나, 그냥 친구들 이름만 나열
            const roomName = [user.value.username, ...selectedFriends].join(', ');
            // 너무 길면 자르기
            const finalName = roomName.length > 20 ? roomName.substring(0, 20) + '...' : roomName;
            
            await createGroupRoom(finalName, selectedFriendIds.value);
        }
    } catch (e) {
        console.error(e);
        alert('대화방 생성 실패: ' + e.message);
    } finally {
        creating.value = false;
    }
};

const createGroupRoom = async (name, memberIds) => {
    try {
        const res = await chatApi.createRoom(name, memberIds);
        const newRoom = res.data;
        
        // 목록 새로고침
        await loadAllConversations();
        
        // 바로 입장
        openGroupChat(newRoom);
        showCreateModal.value = false;
    } catch (e) {
        console.error(e);
        throw e;
    }
};

const startDMWithFriend = (friend) => {
    // 1. 이미 존재하는 대화방인지 확인 (로컬 목록에서)
    const existingConv = conversations.value.find(c => c.partnerId === friend.id);
    
    if (existingConv) {
        // 이미 있으면 열기
        openChat(existingConv);
    } else {
        // 없으면 새 activeChat 설정 (메시지 보내면 생성됨)
        activeChat.value = {
            partnerId: friend.id,
            partnerName: friend.username,
            partnerAvatar: friend.avatarUrl,
            partnerDecoration: ''
        };
    }
    showCreateModal.value = false;
};

// Utilities
const openFullView = () => {
    router.push('/social?tab=messages');
    isOpen.value = false;
    stopChatPolling();
};

const getAvatar = (url, isDeleted = false) => {
    if (isDeleted) return 'https://avatars.githubusercontent.com/u/0';
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

// Lifecycle Hooks
let refreshInterval = null;
onMounted(() => {
    if (isAuthenticated.value) {
        loadAllConversations();
        refreshInterval = setInterval(() => {
            if (viewMode.value === 'list') loadAllConversations();
        }, 60000);
    }
});

onUnmounted(() => {
    if (refreshInterval) clearInterval(refreshInterval);
    stopChatPolling();
});

watch(() => route.path, () => {
    isOpen.value = false;
    stopChatPolling();
});

watch(isAuthenticated, (val) => {
    if (val) loadAllConversations();
    else {
        conversations.value = [];
        groupRooms.value = [];
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

.slide-down-enter-active,
.slide-down-leave-active {
    transition: all 0.2s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
    max-height: 0;
    opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>
