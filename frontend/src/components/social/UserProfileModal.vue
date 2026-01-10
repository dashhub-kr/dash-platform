<template>
    <div v-if="isOpen && targetUserInfo" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" @click="close"></div>
        
        <div class="relative bg-white rounded-3xl w-full max-w-sm shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200 p-6 text-center">
            
            <button @click="close" class="absolute top-4 right-4 p-2 text-slate-400 hover:text-slate-600 bg-slate-50 rounded-full hover:bg-slate-100 transition-colors">
                <X :size="20"/>
            </button>

            <!-- Profile Info -->
            <div class="flex flex-col items-center mb-6">
                <div class="relative mb-4">
                    <img 
                        :src="targetUserInfo.avatarUrl || '/images/profiles/default-profile.png'" 
                        class="w-24 h-24 rounded-full border-4 border-slate-50 shadow-md object-cover bg-white"
                        :alt="targetUserInfo.nickname"
                    />
                    <div v-if="targetUserInfo.role === 'ROLE_ADMIN'" class="absolute -bottom-1 -right-1 bg-amber-500 text-white p-1.5 rounded-full border-2 border-white shadow-sm" title="Admin">
                        <Crown :size="16" fill="currentColor"/>
                    </div>
                </div>
                
                <h2 class="text-xl font-black text-slate-800 mb-1 flex items-center gap-2 justify-center">
                    {{ targetUserInfo.nickname }}
                    <span v-if="!targetUserInfo.nickname && targetUserInfo.username">(@{{ targetUserInfo.username }})</span>
                </h2>
                
                <div v-if="targetUserInfo.decorationClass" class="mb-2">
                    <span class="px-3 py-1 bg-gradient-to-r from-brand-50 to-indigo-50 text-brand-600 text-xs font-bold rounded-full border border-brand-100 shadow-sm">
                        {{ targetUserInfo.decorationClass }}
                    </span>
                </div>
                
                <p v-if="targetUserInfo.email" class="text-xs text-slate-400 font-medium">{{ targetUserInfo.email }}</p>
            </div>

            <!-- Loading State -->
            <div v-if="loading" class="py-4">
                <Loader2 class="animate-spin w-6 h-6 text-brand-500 mx-auto" />
            </div>

            <!-- Actions -->
            <div v-else class="space-y-3">
                <!-- Case: Me -->
                <div v-if="isMe" class="p-3 bg-slate-50 text-slate-500 rounded-xl text-sm font-bold">
                    나의 프로필입니다
                </div>

                <!-- Case: Friend -->
                <template v-else-if="friendshipStatus === 'ACCEPTED'">
                    <button 
                        @click="openDM" 
                        class="w-full py-3 bg-brand-500 text-white rounded-xl text-sm font-bold hover:bg-brand-600 transition-colors flex items-center justify-center gap-2 shadow-lg shadow-brand-500/20"
                    >
                        <MessageCircle :size="18"/> 쪽지 보내기
                    </button>
                    <div class="text-xs text-brand-600 font-bold flex items-center justify-center gap-1 mt-2">
                        <Users :size="14"/> 서로 친구입니다
                    </div>
                </template>

                <!-- Case: Pending / Strings attached -->
                <button 
                    v-else-if="friendshipStatus === 'PENDING' || requested"
                    disabled
                    class="w-full py-3 bg-slate-100 text-slate-400 rounded-xl text-sm font-bold cursor-not-allowed flex items-center justify-center gap-2"
                >
                    <CheckCircle2 :size="18"/> 친구 요청 보냄
                </button>

                <!-- Case: Not Friend -->
                <button 
                    v-else
                    @click="sendFriendRequest"
                    class="w-full py-3 bg-slate-900 text-white rounded-xl text-sm font-bold hover:bg-slate-800 transition-colors flex items-center justify-center gap-2 shadow-lg shadow-slate-900/10"
                >
                    <UserPlus :size="18"/> 친구 신청
                </button>
            </div>

        </div>
    </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { useUserProfileModal } from '@/composables/useUserProfileModal';
import { useDirectMessageModal } from '@/composables/useDirectMessageModal';
import { useAuth } from '@/composables/useAuth';
import { socialApi } from '@/api/social';
import { X, Crown, MessageCircle, UserPlus, CheckCircle2, Users, Loader2 } from 'lucide-vue-next';

const { isOpen, targetUserInfo, close } = useUserProfileModal();
const { open: openDirectMessage } = useDirectMessageModal();
const { user: currentUser } = useAuth();

const loading = ref(false);
const friendshipStatus = ref(null);
const requested = ref(false);

const isMe = computed(() => {
    return currentUser.value && targetUserInfo.value && currentUser.value.id === targetUserInfo.value.userId;
});

// Fetch friendship status when modal opens
watch([isOpen, targetUserInfo], async ([newOpen, newUserInfo]) => {
    if (newOpen && newUserInfo && !isMe.value) {
        // 이미 친구 상태 정보가 있으면 API 호출 스킵
        if (newUserInfo.friendshipStatus) {
            friendshipStatus.value = newUserInfo.friendshipStatus;
            return;
        }

        // Init state
        loading.value = true;
        friendshipStatus.value = null;
        requested.value = false;

        try {
            // Check via Search API using nickname or username
            // Note: Ideally backend should provide a checkStatus API by ID. 
            // Using search as a workaround if direct check isn't available, 
            // BUT board/comment writers might not have nickname exposed correctly or duplicates?
            // Actually, best way is to try to 'get friends' and find them, OR assuming I can search by ID? NO.
            // Let's rely on search for now as planned.
            const query = newUserInfo.nickname || newUserInfo.username;
            if (query) {
                const res = await socialApi.searchUsers(query);
                // Find exact match by ID if possible, otherwise by name
                const match = res.data.find(u => u.id === newUserInfo.userId || u.nickname === query || u.username === query);
                if (match) {
                    friendshipStatus.value = match.friendshipStatus;
                }
            }
        } catch (e) {
            console.error("Failed to check friendship status", e);
        } finally {
            loading.value = false;
        }
    }
});

const sendFriendRequest = async () => {
    if (!confirm('친구 신청을 보내시겠습니까?')) return;
    try {
        await socialApi.sendFriendRequest(targetUserInfo.value.userId);
        requested.value = true;
        alert('친구 신청을 보냈습니다.');
    } catch(e) {
        alert(e.response?.data?.message || '실패했습니다.');
    }
};

const openDM = () => {
    // Map targetUserInfo to format expected by DirectMessageModal
    // DirectMessageModal props: partnerId, partnerName, partnerAvatar, partnerDecoration
    const partner = {
        partnerId: targetUserInfo.value.userId,
        partnerName: targetUserInfo.value.nickname || targetUserInfo.value.username,
        partnerAvatar: targetUserInfo.value.avatarUrl,
        partnerDecoration: targetUserInfo.value.decorationClass
    };
    
    close(); // Close profile modal
    openDirectMessage(partner); // Open DM modal
};

</script>
