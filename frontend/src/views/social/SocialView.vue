<template>
    <div class="p-8 max-w-7xl mx-auto space-y-6">
        <!-- Header -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
            <div>
                <div class="flex items-center gap-3 mb-2">
                    <Users class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
                    <h1 class="text-xl font-black text-slate-800">소셜</h1>
                </div>
                <!-- <h1 class="text-3xl font-black text-slate-800 tracking-tight mb-2">소셜</h1> -->
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

                <!-- 메인: 친구 피드 -->
                <main class="flex-1 min-w-0 space-y-6">
                    <!-- Header -->
                    <div class="mb-6">
                        <h1 class="text-2xl font-black text-slate-800 flex items-center gap-3 mb-2">
                            <div class="w-10 h-10 bg-pink-500 rounded-xl flex items-center justify-center">
                                <Users class="w-6 h-6 text-white" :stroke-width="2.5" />
                            </div>
                            소셜
                        </h1>
                        <p class="text-slate-500 ml-[52px]">친구들의 활동을 확인하고 함께 성장해요</p>
                    </div>

                    <!-- 피드 영역 -->
                    <div class="space-y-4">
                        <!-- 로딩 -->
                        <div v-if="feedLoading && feedItems.length === 0" class="flex justify-center py-20">
                            <Loader2 class="animate-spin text-brand-500" :size="32" />
                        </div>

                        <!-- 빈 피드 -->
                        <div v-else-if="feedItems.length === 0" class="text-center py-20">
                            <div class="w-16 h-16 bg-slate-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
                                <Rss :size="32" class="text-slate-300" />
                            </div>
                            <p class="text-slate-400 font-medium mb-2">아직 친구 활동이 없어요</p>
                            <p class="text-sm text-slate-400">친구를 추가하고 활동을 확인해보세요!</p>
                        </div>

                        <!-- 피드 아이템 목록 -->
                        <template v-else>
                            <FeedItem 
                                v-for="item in feedItems" 
                                :key="item.id" 
                                :item="item"
                                @view-battle="handleViewBattle"
                            />
                        </template>

                        <!-- 무한 스크롤 트리거 -->
                        <div v-if="hasMore" ref="loadMoreTrigger" class="flex justify-center py-8">
                            <Loader2 class="animate-spin text-slate-400" :size="24" />
                        </div>
                    </div>
                </main>

                <!-- 우측 사이드바: 친구 관리 -->
                <aside class="hidden lg:flex w-[360px] shrink-0 flex-col gap-5 sticky top-8 h-fit">
                    
                    <!-- 친구 찾기 + 친구 목록 통합 -->
                    <div class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
                        <!-- 검색 헤더 -->
                        <div class="p-5 border-b border-slate-100">
                            <h2 class="text-sm font-black text-slate-800 uppercase tracking-wide mb-3 flex items-center gap-2">
                                <Search :size="14" class="text-slate-400" />
                                친구 찾기
                            </h2>
                            <div class="flex gap-2">
                                <input 
                                    v-model="searchQuery" 
                                    @keyup.enter="handleSearch"
                                    type="text" 
                                    placeholder="이메일 또는 닉네임" 
                                    class="flex-1 px-3 py-2.5 rounded-xl border border-slate-200 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 transition-all font-medium text-slate-700 bg-slate-50 focus:bg-white text-sm"
                                />
                                <button 
                                    @click="handleSearch"
                                    class="px-4 py-2.5 bg-slate-900 text-white rounded-xl font-bold hover:bg-brand-600 transition-colors text-sm"
                                >
                                    검색
                                </button>
                            </div>

                            <!-- 검색 결과 -->
                            <div v-if="searchLoading" class="flex justify-center py-4 mt-3">
                                <Loader2 class="animate-spin text-brand-500" :size="20" />
                            </div>
                            <div v-else-if="searchResults && searchResults.length > 0" class="mt-3 space-y-2 max-h-[200px] overflow-y-auto">
                                <div v-for="user in searchResults" :key="user.id" class="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 hover:bg-slate-100 transition-colors">
                                    <div class="flex items-center gap-2.5 min-w-0">
                                        <img :src="getAvatar(user.avatarUrl)" class="w-8 h-8 rounded-full border border-slate-200"/>
                                        <div class="min-w-0">
                                            <div class="flex items-center gap-1">
                                                <span class="text-sm font-bold text-slate-700 truncate">{{ user.username }}</span>
                                                <TierBadge v-if="user.solvedacTier" :tier="user.solvedacTier" size="xs" :show-roman="false" />
                                            </div>
                                        </div>
                                    </div>
                                    <button 
                                        v-if="user.friendshipStatus !== 'ACCEPTED'"
                                        @click="sendRequest(user)" 
                                        :disabled="user.friendshipStatus === 'PENDING' || user.requested"
                                        class="px-2.5 py-1 text-[10px] font-bold rounded-lg transition-colors"
                                        :class="user.friendshipStatus === 'PENDING' || user.requested 
                                            ? 'bg-slate-200 text-slate-400 cursor-not-allowed' 
                                            : 'bg-brand-500 text-white hover:bg-brand-600'"
                                    >
                                        {{ user.friendshipStatus === 'PENDING' || user.requested ? '요청됨' : '친구 추가' }}
                                    </button>
                                    <span v-else class="text-[10px] font-bold text-slate-400 bg-slate-100 px-2 py-1 rounded-lg">친구</span>
                                </div>
                            </div>
                            <div v-else-if="searchResults && searchResults.length === 0" class="mt-3 text-center py-3 text-slate-400 text-sm">
                                검색 결과가 없습니다
                            </div>
                        </div>

                        <!-- 친구 요청 -->
                        <div v-if="requests.length > 0" class="p-5 border-b border-slate-100 bg-rose-50/50">
                            <h3 class="text-xs font-black text-rose-600 uppercase tracking-wide mb-3 flex items-center gap-2">
                                <Bell :size="12" />
                                친구 요청 ({{ requests.length }})
                            </h3>
                            <div class="space-y-2">
                                <div v-for="req in requests" :key="req.id" class="flex items-center justify-between p-2.5 rounded-xl bg-white border border-rose-100">
                                    <div class="flex items-center gap-2.5 min-w-0">
                                        <img :src="getAvatar(req.requester.avatarUrl)" class="w-8 h-8 rounded-full border border-slate-200"/>
                                        <span class="text-sm font-bold text-slate-700 truncate">{{ req.requester.username }}</span>
                                    </div>
                                    <div class="flex items-center gap-1.5">
                                        <button @click="acceptRequest(req.id)" class="p-1.5 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-colors">
                                            <Check :size="14" />
                                        </button>
                                        <button @click="rejectRequest(req.id)" class="p-1.5 bg-slate-200 text-slate-500 rounded-lg hover:bg-rose-100 hover:text-rose-500 transition-colors">
                                            <X :size="14" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 내 친구 목록 -->
                        <div class="p-5">
                            <h3 class="text-xs font-black text-slate-500 uppercase tracking-wide mb-3 flex items-center gap-2">
                                <Users :size="12" />
                                내 친구 ({{ friends.length }})
                            </h3>
                            <div v-if="friendsLoading" class="flex justify-center py-4">
                                <Loader2 class="animate-spin text-brand-500" :size="20" />
                            </div>
                            <div v-else-if="friends.length === 0" class="text-center py-4 text-slate-400 text-sm">
                                아직 친구가 없어요
                            </div>
                            <div v-else class="space-y-1.5 max-h-[300px] overflow-y-auto">
                                <div 
                                    v-for="item in friends" 
                                    :key="item.id" 
                                    class="flex items-center justify-between p-2.5 rounded-xl hover:bg-slate-50 transition-colors group cursor-pointer"
                                    @click="openUserProfile(item.friend)"
                                >
                                    <div class="flex items-center gap-2.5 min-w-0">
                                        <img 
                                            :src="item.friend.isDeleted ? 'https://avatars.githubusercontent.com/u/0' : getAvatar(item.friend.avatarUrl)" 
                                            class="w-8 h-8 rounded-full border border-slate-200 object-cover"
                                            :class="{ 'grayscale opacity-60': item.friend.isDeleted }"
                                        />
                                        <div class="min-w-0">
                                            <div class="flex items-center gap-1">
                                                <span 
                                                    class="text-sm font-bold truncate"
                                                    :class="item.friend.isDeleted ? 'text-slate-400' : 'text-slate-700'"
                                                >
                                                    {{ item.friend.isDeleted ? '탈퇴한 회원' : item.friend.username }}
                                                </span>
                                                <TierBadge v-if="item.friend.solvedacTier && !item.friend.isDeleted" :tier="item.friend.solvedacTier" size="xs" :show-roman="false" />
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div class="flex items-center gap-1">
                                         <button 
                                            v-if="!item.friend.isDeleted"
                                            @click.stop="openDM(item.friend)" 
                                            class="p-1.5 text-slate-400 hover:text-brand-500 hover:bg-brand-50 rounded-lg transition-colors opacity-0 group-hover:opacity-100"
                                            title="쪽지 보내기"
                                        >
                                            <MessageCircle :size="16" />
                                        </button>
                                        <span v-else class="text-[10px] text-slate-300 px-2">탈퇴</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                </aside>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { socialApi } from '@/api/social';
import { Loader2, Users, Search, Bell, MessageCircle, Rss, Check, X } from 'lucide-vue-next';
import TierBadge from '@/components/common/TierBadge.vue';
import FeedItem from '@/components/social/FeedItem.vue';
import { useFloatingChat } from '@/composables/useFloatingChat';
import { useUserProfileModal } from '@/composables/useUserProfileModal';

const route = useRoute();
const router = useRouter();
const { openChat: openGlobalDM } = useFloatingChat();
const { open: openProfile } = useUserProfileModal();

// 피드 상태
const feedItems = ref([]);
const feedLoading = ref(false);
const hasMore = ref(true);
const page = ref(0);

// 친구 상태
const friends = ref([]);
const friendsLoading = ref(false);
const requests = ref([]);

// 검색 상태
const searchQuery = ref('');
const searchLoading = ref(false);
const searchResults = ref(null);

// 헬퍼
const getAvatar = (url) => {
    if (url && !url.includes('dicebear')) return url;
    return '/images/profiles/default-profile.png';
};

// 피드 로드
const loadFeed = async (reset = false) => {
    if (reset) {
        page.value = 0;
        feedItems.value = [];
        hasMore.value = true;
    }
    
    feedLoading.value = true;
    try {
        const res = await socialApi.getFeed(page.value);
        const items = res.data.content || res.data || [];
        
        if (items.length === 0) {
            hasMore.value = false;
        } else {
            feedItems.value = [...feedItems.value, ...items];
            page.value++;
        }
    } catch (e) {
        console.error('Failed to load feed:', e);
        // API 아직 없으면 mock 데이터
        if (page.value === 0) {
            feedItems.value = [
                { id: 1, type: 'SOLVED', userName: '김코딩', userAvatar: null, userTier: 15, problemId: 1234, problemTitle: '토마토', createdAt: new Date().toISOString() },
                { id: 2, type: 'STREAK', userName: '박알고', userAvatar: null, userTier: 12, streakDays: 7, createdAt: new Date().toISOString() },
            ];
        }
        hasMore.value = false;
    } finally {
        feedLoading.value = false;
    }
};

// 무한 스크롤 상태
const loadMoreTrigger = ref(null);
let observer = null;

// 친구 데이터 로드
const loadFriends = async () => {
    friendsLoading.value = true;
    try {
        const [friendsRes, requestsRes] = await Promise.all([
            socialApi.getFriends(),
            socialApi.getReceivedRequests()
        ]);
        friends.value = friendsRes.data;
        requests.value = requestsRes.data;
    } catch (e) {
        console.error('Failed to load friends:', e);
    } finally {
        friendsLoading.value = false;
    }
};

// 검색
const handleSearch = async () => {
    if (!searchQuery.value.trim()) return;
    searchLoading.value = true;
    try {
        const res = await socialApi.searchUsers(searchQuery.value);
        searchResults.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        searchLoading.value = false;
    }
};

// 친구 요청
const sendRequest = async (user) => {
    try {
        await socialApi.sendFriendRequest(user.id);
        user.requested = true;
    } catch (e) {
        alert(e.response?.data?.message || '요청 실패');
    }
};

const acceptRequest = async (requestId) => {
    try {
        await socialApi.acceptFriendRequest(requestId);
        loadFriends();
    } catch (e) {
        console.error(e);
    }
};

const rejectRequest = async (requestId) => {
    try {
        await socialApi.rejectFriendRequest(requestId);
        loadFriends();
    } catch (e) {
        console.error(e);
    }
};

// 액션
const openDM = (friend) => {
    if (friend.isDeleted) return; // 탈퇴 회원 클릭 방지
    openGlobalDM({
        partnerId: friend.id,
        partnerName: friend.username,
        partnerAvatar: friend.avatarUrl,
        partnerDecoration: friend.equippedDecorationClass || '',
        partnerIsDeleted: friend.isDeleted || false
    });
};

const openUserProfile = (user) => {
    // UserProfileModal이 기대하는 포맷으로 변환
    openProfile({
        userId: user.id,
        username: user.username, // 혹은 nickname
        nickname: user.nickname || user.username,
        email: user.email,
        avatarUrl: user.avatarUrl,
        solvedacTier: user.solvedacTier,
        decorationClass: user.equippedDecorationClass, // 필드명 확인 필요
        friendshipStatus: 'ACCEPTED', // 친구 목록에서 클릭했으므로 항상 ACCEPTED
        isDeleted: user.isDeleted || false 
    });
};

const handleViewBattle = (item) => {
    router.push(`/battle/${item.battleId}`);
};

// 라우트 쿼리 핸들러 (예: 알림 클릭 시) - HEAD에서 가져옴
const checkQueryForDM = async () => {
    const pid = route.query.partnerId;
    if (pid) {
        const partnerId = parseInt(pid);
        
        // 1. 친구 목록 로드 대기 (이미 로드되었겠지만 확실히)
        if (friends.value.length === 0) await loadFriends();

        // 2. 친구 목록에서 찾아보기
        const friendItem = friends.value.find(f => f.friend.id === partnerId);
        
        if (friendItem) {
            // 친구인 경우 DM 모달 열기
            openDM(friendItem.friend);
        } else {
            // 친구가 아닌 경우 (혹은 탈퇴)
             alert('현재 친구 목록에 없는 사용자이거나 정보를 불러올 수 없습니다.');
        }

        // 쿼리 파라미터 제거하여 URL 정리
        router.replace({ query: { ...route.query, partnerId: undefined } });
    }
};

onMounted(async () => {
    loadFeed(true);
    await loadFriends(); // 친구 목록 먼저 로드
    checkQueryForDM();   // 그 다음 쿼리 체크
    
    // 무한 스크롤 옵저버
    observer = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && !feedLoading.value && hasMore.value) {
            loadFeed();
        }
    }, { rootMargin: '200px' });
});

// 트리거 요소 감시
watch(loadMoreTrigger, (el) => {
    if (el && observer) observer.observe(el);
});

onBeforeUnmount(() => {
    if (observer) observer.disconnect();
});
</script>
