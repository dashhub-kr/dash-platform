<template>
    <Teleport to="body">
        <!-- 그룹 채팅방 목록 패널 -->
        <Transition name="slide-up">
            <div 
                v-if="isOpen" 
                class="fixed bottom-0 right-0 md:absolute md:bottom-20 md:right-6 w-full md:w-[380px] h-[85vh] md:h-[550px] bg-white md:rounded-2xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col z-50"
            >
                <!-- 헤더 -->
                <div class="flex items-center justify-between p-4 border-b border-slate-100 bg-gradient-to-r from-indigo-600 to-purple-600 text-white shrink-0">
                    <div class="flex items-center gap-2">
                        <Users :size="20" />
                        <span class="font-bold">그룹 채팅</span>
                    </div>
                    <div class="flex items-center gap-1">
                        <button @click="openCreateModal" class="p-2 hover:bg-white/20 rounded-lg transition-colors" title="새 채팅방">
                            <Plus :size="18" />
                        </button>
                        <button @click="close" class="p-2 hover:bg-white/20 rounded-lg transition-colors">
                            <X :size="18" />
                        </button>
                    </div>
                </div>

                <!-- 채팅방 목록 또는 채팅 뷰 -->
                <template v-if="!activeRoom">
                    <!-- 채팅방 목록 -->
                    <div class="flex-1 overflow-y-auto">
                        <div v-if="loading" class="flex justify-center py-10">
                            <Loader2 class="animate-spin text-indigo-500" />
                        </div>
                        <div v-else-if="rooms.length === 0" class="text-center py-10 text-slate-400">
                            <Users :size="32" class="mx-auto mb-2 opacity-30" />
                            <p class="text-sm">채팅방이 없어요</p>
                            <button @click="openCreateModal" class="mt-3 text-indigo-500 text-sm font-bold hover:underline">
                                + 새 채팅방 만들기
                            </button>
                        </div>
                        <div v-else>
                            <div 
                                v-for="room in rooms" 
                                :key="room.id" 
                                @click="openRoom(room)"
                                class="flex items-center gap-3 p-4 hover:bg-slate-50 cursor-pointer transition-colors border-b border-slate-50 last:border-b-0"
                            >
                                <div class="w-11 h-11 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-white font-bold">
                                    {{ room.memberCount }}
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex items-center gap-2">
                                        <span class="text-sm font-bold text-slate-700 truncate">{{ room.name }}</span>
                                        <span v-if="room.type === 'STUDY'" class="text-[10px] px-1.5 py-0.5 bg-indigo-100 text-indigo-600 rounded font-bold">스터디</span>
                                    </div>
                                    <p class="text-xs text-slate-500 truncate mt-0.5">{{ room.lastMessage || '메시지 없음' }}</p>
                                </div>
                                <div class="flex flex-col items-end gap-1">
                                    <span v-if="room.lastMessageAt" class="text-[10px] text-slate-400">{{ formatTime(room.lastMessageAt) }}</span>
                                    <span v-if="room.unreadCount > 0" class="min-w-[18px] h-[18px] bg-rose-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1">
                                        {{ room.unreadCount > 99 ? '99+' : room.unreadCount }}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>

                <!-- 채팅 뷰 -->
                <template v-else>
                    <!-- 채팅방 헤더 -->
                    <div class="flex items-center justify-between p-3 border-b border-slate-100 bg-slate-50 shrink-0">
                        <div class="flex items-center gap-3">
                            <button @click="activeRoom = null" class="p-1.5 hover:bg-slate-200 rounded-lg transition-colors text-slate-600">
                                <ChevronLeft :size="20" />
                            </button>
                            <div>
                                <h3 class="text-sm font-bold text-slate-700">{{ activeRoom.name }}</h3>
                                <p class="text-xs text-slate-400">{{ activeRoom.members?.length || 0 }}명 참여 중</p>
                            </div>
                        </div>
                        <button @click="showMemberList = !showMemberList" class="p-2 hover:bg-slate-200 rounded-lg transition-colors text-slate-500">
                            <Users :size="16" />
                        </button>
                    </div>

                    <!-- 멤버 목록 (토글) -->
                    <Transition name="slide-down">
                        <div v-if="showMemberList" class="p-3 bg-slate-50 border-b border-slate-100">
                            <div class="flex flex-wrap gap-2">
                                <div v-for="member in activeRoom.members" :key="member.userId" class="flex items-center gap-1.5 bg-white px-2 py-1 rounded-full border border-slate-200">
                                    <img :src="getAvatar(member.avatarUrl)" class="w-5 h-5 rounded-full" />
                                    <span class="text-xs font-medium text-slate-600">{{ member.username }}</span>
                                </div>
                            </div>
                        </div>
                    </Transition>

                    <!-- 메시지 목록 -->
                    <div class="flex-1 overflow-y-auto p-3 space-y-3 bg-slate-50" ref="messagesContainer">
                        <div v-if="messagesLoading" class="flex justify-center py-8">
                            <Loader2 class="animate-spin text-indigo-500" />
                        </div>
                        <template v-else>
                            <template v-for="(msg, index) in messages" :key="msg.id">
                                <!-- 내 메시지 -->
                                <div v-if="msg.isMe" class="flex justify-end">
                                    <div class="flex flex-col items-end max-w-[75%]">
                                        <div class="bg-indigo-500 text-white px-3 py-2 rounded-2xl rounded-br-sm text-sm whitespace-pre-wrap break-words">
                                            {{ msg.content }}
                                        </div>
                                        <span class="text-[9px] text-slate-400 mt-0.5 px-1">{{ formatMsgTime(msg.createdAt) }}</span>
                                    </div>
                                </div>
                                <!-- 상대방 메시지 -->
                                <div v-else class="flex gap-2">
                                    <img :src="getAvatar(msg.senderAvatarUrl)" class="w-8 h-8 rounded-full shrink-0" />
                                    <div class="flex flex-col max-w-[75%]">
                                        <span class="text-[10px] text-slate-500 font-medium mb-0.5">{{ msg.senderUsername }}</span>
                                        <div class="bg-white border border-slate-200 px-3 py-2 rounded-2xl rounded-tl-sm text-sm text-slate-700 whitespace-pre-wrap break-words">
                                            {{ msg.content }}
                                        </div>
                                        <span class="text-[9px] text-slate-400 mt-0.5 px-1">{{ formatMsgTime(msg.createdAt) }}</span>
                                    </div>
                                </div>
                            </template>
                        </template>
                    </div>

                    <!-- 입력 영역 -->
                    <div class="p-3 border-t border-slate-100 bg-white shrink-0">
                        <div class="flex items-end gap-2">
                            <textarea
                                v-model="newMessage"
                                @keydown.enter.exact.prevent="sendMessage"
                                placeholder="메시지 입력..."
                                class="flex-1 px-3 py-2 border border-slate-200 rounded-xl text-sm resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-400 max-h-24"
                                rows="1"
                            ></textarea>
                            <button 
                                @click="sendMessage"
                                :disabled="!newMessage.trim() || sending"
                                class="p-2.5 bg-indigo-500 text-white rounded-xl hover:bg-indigo-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                <Send :size="18" />
                            </button>
                        </div>
                    </div>
                </template>
            </div>
        </Transition>

        <!-- 채팅방 생성 모달 -->
        <Transition name="fade">
            <div v-if="showCreateModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4" @click.self="showCreateModal = false">
                <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm"></div>
                <div class="relative bg-white rounded-2xl w-full max-w-md shadow-2xl overflow-hidden">
                    <div class="p-4 border-b border-slate-100 flex items-center justify-between">
                        <h3 class="font-bold text-slate-800">새 그룹 채팅방</h3>
                        <button @click="showCreateModal = false" class="p-1.5 hover:bg-slate-100 rounded-lg">
                            <X :size="18" class="text-slate-400" />
                        </button>
                    </div>
                    <div class="p-4 space-y-4">
                        <div>
                            <label class="text-xs font-bold text-slate-600 mb-1.5 block">채팅방 이름</label>
                            <input 
                                v-model="createForm.name" 
                                type="text" 
                                placeholder="예: 알고리즘 스터디 잡담"
                                class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-400"
                            />
                        </div>
                        <div>
                            <label class="text-xs font-bold text-slate-600 mb-1.5 block">초대할 친구</label>
                            <div class="max-h-48 overflow-y-auto border border-slate-200 rounded-lg">
                                <div 
                                    v-for="friend in friends" 
                                    :key="friend.id" 
                                    @click="toggleFriend(friend.friend.id)"
                                    class="flex items-center gap-3 p-3 hover:bg-slate-50 cursor-pointer border-b border-slate-100 last:border-b-0"
                                >
                                    <input 
                                        type="checkbox" 
                                        :checked="createForm.memberIds.includes(friend.friend.id)"
                                        class="w-4 h-4 text-indigo-500 rounded border-slate-300"
                                    />
                                    <img :src="getAvatar(friend.friend?.avatarUrl)" class="w-8 h-8 rounded-full" />
                                    <span class="text-sm font-medium text-slate-700">{{ friend.friend?.username }}</span>
                                </div>
                                <div v-if="friends.length === 0" class="p-4 text-center text-sm text-slate-400">
                                    친구가 없습니다
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="p-4 border-t border-slate-100 flex justify-end gap-2">
                        <button @click="showCreateModal = false" class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg">
                            취소
                        </button>
                        <button 
                            @click="createRoom"
                            :disabled="!createForm.name.trim() || creating"
                            class="px-4 py-2 text-sm font-bold text-white bg-indigo-500 hover:bg-indigo-600 disabled:opacity-50 rounded-lg"
                        >
                            {{ creating ? '생성 중...' : '만들기' }}
                        </button>
                    </div>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { chatApi } from '@/api/chat';
import { socialApi } from '@/api/social';
import { Users, Plus, X, ChevronLeft, Send, Loader2 } from 'lucide-vue-next';

const props = defineProps({
    isOpen: Boolean
});

const emit = defineEmits(['close']);

// State
const loading = ref(false);
const rooms = ref([]);
const activeRoom = ref(null);
const messages = ref([]);
const messagesLoading = ref(false);
const newMessage = ref('');
const sending = ref(false);
const showMemberList = ref(false);
const messagesContainer = ref(null);

// 채팅방 생성
const showCreateModal = ref(false);
const creating = ref(false);
const friends = ref([]);
const createForm = ref({
    name: '',
    memberIds: []
});

// Methods
const close = () => emit('close');

const getAvatar = (url) => {
    if (url && !url.includes('dicebear')) return url;
    return '/images/profiles/default-profile.png';
};

const formatTime = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) return '방금';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}분 전`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}시간 전`;
    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
};

const formatMsgTime = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
};

const loadRooms = async () => {
    loading.value = true;
    try {
        const res = await chatApi.getRooms();
        rooms.value = res.data;
    } catch (e) {
        console.error('Failed to load rooms', e);
    } finally {
        loading.value = false;
    }
};

const openRoom = async (room) => {
    activeRoom.value = room;
    messagesLoading.value = true;
    showMemberList.value = false;
    
    try {
        // 채팅방 상세 정보 (멤버 포함)
        const detailRes = await chatApi.getRoom(room.id);
        activeRoom.value = { ...room, ...detailRes.data };
        
        // 메시지 로드
        const msgRes = await chatApi.getMessages(room.id);
        messages.value = msgRes.data.reverse(); // 시간순 정렬
        
        // 읽음 처리
        await chatApi.markAsRead(room.id);
        
        nextTick(() => scrollToBottom());
    } catch (e) {
        console.error('Failed to load room', e);
    } finally {
        messagesLoading.value = false;
    }
};

const sendMessage = async () => {
    if (!newMessage.value.trim() || sending.value || !activeRoom.value) return;
    
    sending.value = true;
    const content = newMessage.value.trim();
    newMessage.value = '';
    
    try {
        const res = await chatApi.sendMessage(activeRoom.value.id, content);
        messages.value.push(res.data);
        nextTick(() => scrollToBottom());
    } catch (e) {
        console.error('Failed to send message', e);
        newMessage.value = content; // 복원
    } finally {
        sending.value = false;
    }
};

const scrollToBottom = () => {
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
};

const openCreateModal = async () => {
    showCreateModal.value = true;
    createForm.value = { name: '', memberIds: [] };
    
    try {
        const res = await socialApi.getFriends();
        friends.value = res.data;
    } catch (e) {
        console.error('Failed to load friends', e);
    }
};

const toggleFriend = (friendId) => {
    const idx = createForm.value.memberIds.indexOf(friendId);
    if (idx > -1) {
        createForm.value.memberIds.splice(idx, 1);
    } else {
        createForm.value.memberIds.push(friendId);
    }
};

const createRoom = async () => {
    if (!createForm.value.name.trim() || creating.value) return;
    
    creating.value = true;
    try {
        await chatApi.createRoom(createForm.value.name, createForm.value.memberIds);
        showCreateModal.value = false;
        await loadRooms();
    } catch (e) {
        alert(e.response?.data?.message || '채팅방 생성에 실패했습니다.');
    } finally {
        creating.value = false;
    }
};

// 폴링 (5초마다)
let pollInterval = null;

watch(() => props.isOpen, (newVal) => {
    if (newVal) {
        loadRooms();
        pollInterval = setInterval(() => {
            if (activeRoom.value) {
                // 현재 채팅방의 새 메시지 확인
                chatApi.getMessages(activeRoom.value.id, 0, 1).then(res => {
                    if (res.data.length > 0 && res.data[0].id !== messages.value[messages.value.length - 1]?.id) {
                        // 새 메시지가 있으면 전체 다시 로드
                        chatApi.getMessages(activeRoom.value.id).then(msgRes => {
                            messages.value = msgRes.data.reverse();
                            nextTick(() => scrollToBottom());
                        });
                    }
                });
            } else {
                loadRooms();
            }
        }, 5000);
    } else {
        if (pollInterval) clearInterval(pollInterval);
        activeRoom.value = null;
    }
});

onMounted(() => {
    if (props.isOpen) {
        loadRooms();
    }
});
</script>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
    transition: all 0.3s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
    transform: translateY(100%);
    opacity: 0;
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
