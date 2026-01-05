<template>
    <div class="p-8 max-w-7xl mx-auto space-y-6">
        <!-- Header -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
                <h1 class="text-3xl font-black text-slate-800 tracking-tight mb-2">소셜</h1>
                <p class="text-slate-500 font-medium">친구들과 함께 공부하고 소통해보세요</p>
            </div>
            
            <div class="flex bg-slate-100 p-1 rounded-xl font-bold">
                <button 
                    v-for="tab in tabs" 
                    :key="tab.id"
                    @click="activeTab = tab.id"
                    class="px-5 py-2.5 rounded-lg text-sm transition-all"
                    :class="activeTab === tab.id ? 'bg-white text-brand-600 shadow-sm' : 'text-slate-400 hover:text-slate-600'"
                >
                    {{ tab.label }}
                    <span v-if="tab.count > 0" class="ml-1 px-1.5 py-0.5 bg-rose-500 text-white text-[10px] rounded-full">{{ tab.count }}</span>
                </button>
            </div>
        </div>

        <!-- Content -->
        <div class="bg-white rounded-3xl border border-slate-200 shadow-sm min-h-[600px] p-6 relative">
            
            <!-- Tab: Friends -->
            <div v-if="activeTab === 'friends'" class="space-y-4">
                <div v-if="loading" class="flex justify-center py-20"><Loader2 class="animate-spin text-brand-500"/></div>
                <div v-else-if="friends.length === 0" class="text-center py-20 text-slate-400">
                    <Users :size="48" class="mx-auto mb-4 opacity-20"/>
                    <p>아직 친구가 없어요. '친구 찾기' 탭에서 새로운 친구를 만들어보세요!</p>
                </div>
                <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div v-for="item in friends" :key="item.id" class="p-4 rounded-2xl border border-slate-100 bg-slate-50 hover:bg-white hover:border-brand-200 hover:shadow-md transition-all group">
                        <div class="flex items-center gap-4">
                            <img :src="item.friend.avatarUrl || '/images/profiles/default-profile.png'" class="w-12 h-12 rounded-full border border-slate-200 bg-white object-cover"/>
                            <div class="flex-1 min-w-0">
                                <div class="font-bold text-slate-800 truncate">{{ item.friend.username }}</div>
                                <div class="text-xs text-slate-400 truncate">{{ item.friend.email }}</div>
                            </div>
                        </div>
                        <div class="mt-4 flex gap-2">
                             <button @click="openDM(item.friend.id, item.friend.username, item.friend.avatarUrl)" class="flex-1 py-2 bg-brand-500 text-white rounded-xl text-sm font-bold hover:bg-brand-600 transition-colors flex items-center justify-center gap-2">
                                <MessageCircle :size="16"/> 쪽지
                             </button>
                             <button @click="deleteFriend(item.friend.id)" class="px-3 py-2 bg-slate-200 text-slate-500 rounded-xl hover:bg-rose-100 hover:text-rose-500 transition-colors">
                                <UserMinus :size="16"/>
                             </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Tab: Requests -->
            <div v-if="activeTab === 'requests'" class="space-y-4">
                <div v-if="loading" class="flex justify-center py-20"><Loader2 class="animate-spin text-brand-500"/></div>
                <div v-else-if="requests.length === 0" class="text-center py-20 text-slate-400">
                    <div class="flex justify-center mb-2"><Bell :size="48" class="opacity-20"/></div>
                    <p>받은 친구 요청이 없습니다.</p>
                </div>
                <div v-else class="space-y-3">
                    <div v-for="req in requests" :key="req.id" class="flex items-center justify-between p-4 rounded-2xl border border-slate-100 hover:bg-slate-50">
                        <div class="flex items-center gap-4">
                             <img :src="req.friend.avatarUrl || '/images/profiles/default-profile.png'" class="w-10 h-10 rounded-full border border-slate-200 bg-white object-cover"/>
                             <div>
                                <div class="font-bold text-slate-800">{{ req.friend.username }}</div>
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

            <!-- Tab: Search -->
            <div v-if="activeTab === 'search'" class="space-y-6">
                <div class="relative">
                    <Search :size="20" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"/>
                    <input 
                        v-model="searchQuery" 
                        @keyup.enter="handleSearch"
                        type="text" 
                        placeholder="이메일 또는 닉네임으로 검색하세요" 
                        class="w-full pl-12 pr-4 py-4 rounded-2xl border-2 border-slate-100 focus:border-brand-500 focus:ring-0 transition-all font-bold text-slate-700 bg-slate-50 focus:bg-white"
                    />
                    <button 
                        @click="handleSearch"
                        class="absolute right-2 top-2 bottom-2 px-6 bg-brand-500 text-white rounded-xl font-bold hover:bg-brand-600 transition-colors"
                    >
                        검색
                    </button>
                </div>

                <div v-if="searchLoading" class="flex justify-center py-10"><Loader2 class="animate-spin text-brand-500"/></div>
                <div v-else-if="searchResults">
                     <div v-if="searchResults.length === 0" class="text-center py-10 text-slate-400">
                        검색 결과가 없습니다.
                     </div>
                <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div v-for="user in searchResults" :key="user.id" class="flex items-center justify-between p-4 rounded-2xl border border-slate-100 hover:bg-slate-50 transition-colors">
                            <div class="flex items-center gap-3">
                                <img :src="user.avatarUrl || '/images/profiles/default-profile.png'" class="w-10 h-10 rounded-full border border-slate-200 bg-white object-cover"/>
                                <div>
                                    <div class="font-bold text-slate-800">{{ user.username }}</div>
                                    <div class="text-xs text-slate-400">{{ user.email }}</div>
                                </div>
                            </div>
                            <div v-if="user.friendshipStatus === 'ACCEPTED'" class="px-3 py-1.5 bg-slate-100 text-slate-400 text-xs font-bold rounded-lg flex items-center gap-1 cursor-default">
                                <Users :size="14"/> 친구
                            </div>
                            <button
                                v-else-if="user.friendshipStatus === 'PENDING' || user.requested"
                                disabled
                                class="px-3 py-1.5 bg-slate-200 text-slate-500 text-xs font-bold rounded-lg cursor-not-allowed flex items-center gap-1"
                            >
                                <CheckCircle2 :size="14"/> 요청됨
                            </button>
                            <button 
                                v-else
                                @click="sendRequest(user)" 
                                class="px-3 py-1.5 bg-slate-900 text-white text-xs font-bold rounded-lg hover:bg-brand-600 transition-colors flex items-center gap-1"
                            >
                                <UserPlus :size="14"/> 친구 신청
                            </button>
                        </div>
                     </div>
                </div>
            </div>

        </div>

        <!-- DM Modal -->
        <DirectMessageModal 
            v-if="showDMModal" 
            :partner-id="dmPartnerId" 
            :partner-name="dmPartnerName"
            :partner-avatar="dmPartnerAvatar"
            @close="showDMModal = false"
        />

    </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { socialApi } from '@/api/social';
import { Loader2, Users, Bell, Search, UserPlus, MessageCircle, UserMinus, CheckCircle2 } from 'lucide-vue-next';
import DirectMessageModal from '@/components/social/DirectMessageModal.vue';

const route = useRoute();
const activeTab = ref('friends');
const loading = ref(false);

const friends = ref([]);
const requests = ref([]);
const tabs = ref([
    { id: 'friends', label: '내 친구', count: 0 },
    { id: 'requests', label: '친구 요청', count: 0 },
    { id: 'search', label: '친구 찾기', count: 0 },
]);

// 검색 (Search)
const searchQuery = ref('');
const searchLoading = ref(false);
const searchResults = ref(null);

// 쪽지 (DM)
const showDMModal = ref(false);
const dmPartnerId = ref(null);
const dmPartnerName = ref('');
const dmPartnerAvatar = ref('');

const loadData = async () => {
    loading.value = true;
    try {
        const [friendsRes, requestsRes] = await Promise.all([
            socialApi.getFriends(),
            socialApi.getReceivedRequests()
        ]);
        friends.value = friendsRes.data;
        requests.value = requestsRes.data;
        
        // 카운트 업데이트
        tabs.value[0].count = friends.value.length;
        tabs.value[1].count = requests.value.length;
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
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

const isMyFriend = (userId) => {
    return friends.value.some(f => f.friend.id === userId);
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
        loadData(); // 새로고침
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

const openDM = (id, name, avatar) => {
    dmPartnerId.value = id;
    dmPartnerName.value = name;
    dmPartnerAvatar.value = avatar;
    showDMModal.value = true;
};

// 라우트 쿼리 핸들러 (예: 알림 클릭 시)
watch(() => route.query.tab, (val) => {
    if (val) activeTab.value = val;
}, { immediate: true });

watch(() => route.query.partnerId, (val) => {
    if (val) {
        // 친구 목록에서 이름을 찾거나 사용자 정보를 가져옴 (간소화: ID로 열기)
        dmPartnerId.value = parseInt(val);
        // 로드된 친구 목록에서 이름 찾기 시도
        const friend = friends.value.find(f => f.friend.id === parseInt(val));
        dmPartnerName.value = friend ? friend.friend.username : 'Friend'; 
        dmPartnerAvatar.value = friend ? friend.friend.avatarUrl : '';
        showDMModal.value = true;
    }
}, { immediate: true });

onMounted(() => {
    loadData();
});

watch(activeTab, () => {
    if (activeTab.value !== 'search') loadData();
});

</script>
