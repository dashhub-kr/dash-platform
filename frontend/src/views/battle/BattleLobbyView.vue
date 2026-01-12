<template>
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">
      <div class="min-h-screen bg-slate-50 pb-20">
        
        <!-- Main Content -->
        <div class="flex justify-center p-4 md:p-8">
          <div class="w-full max-w-2xl">
            
            <!-- Loading -->
            <div v-if="loading" class="flex flex-col items-center justify-center py-20">
              <div class="w-12 h-12 border-4 border-violet-200 border-t-violet-600 rounded-full animate-spin mb-4"></div>
              <p class="text-slate-400 font-medium">불러오는 중...</p>
            </div>

            <!-- Battle Not Found -->
            <div v-else-if="!battle" class="text-center py-20">
              <div class="text-6xl mb-4">😢</div>
              <h2 class="text-xl font-bold text-slate-800 mb-2">배틀을 찾을 수 없습니다</h2>
              <router-link to="/defense" class="text-violet-600 hover:underline">
                돌아가기
              </router-link>
            </div>

            <!-- Battle Lobby -->
            <template v-else>
              <!-- Header Card -->
              <div class="bg-gradient-to-r from-violet-500 to-purple-600 rounded-3xl p-6 md:p-8 text-white mb-6 shadow-xl">
                <div class="flex items-center gap-3 mb-2">
                  <Swords class="w-8 h-8" />
                  <h1 class="text-2xl md:text-3xl font-black">
                    {{ battle.type === 'DEFENSE' ? '디펜스' : '모의고사' }} 배틀
                  </h1>
                </div>
                <p class="text-white/80">
                  {{ getStatusMessage() }}
                </p>
              </div>

              <!-- Status Badge -->
              <div class="flex justify-center mb-6">
                <BaseIconBadge 
                  :icon="getStatusIcon" 
                  :text="getStatusText" 
                  :color="getStatusColor" 
                  size="lg"
                />
              </div>

              <!-- Participants -->
              <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 mb-6">
                <h2 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                  <Users class="w-5 h-5 text-violet-500" />
                  참가자 ({{ acceptedCount }}/{{ battle.participants?.length || 0 }}명 준비)
                </h2>
                
                <div class="space-y-3">
                  <div
                    v-for="p in battle.participants"
                    :key="p.id"
                    class="flex items-center gap-4 p-4 rounded-xl transition-colors"
                    :class="getParticipantBgClass(p.status)"
                  >
                    <!-- Avatar -->
                    <div class="relative">
                      <img
                        :src="p.userAvatar || '/default-avatar.png'"
                        class="w-12 h-12 rounded-full object-cover ring-2"
                        :class="getAvatarRingClass(p.status)"
                      />
                      <div
                        v-if="p.userId === battle.creatorId"
                        class="absolute -top-1 -right-1 w-5 h-5 bg-amber-400 rounded-full flex items-center justify-center"
                        title="방장"
                      >
                        <Crown class="w-3 h-3 text-white" />
                      </div>
                    </div>
                    
                    <!-- Info -->
                    <div class="flex-1 min-w-0">
                      <div class="font-semibold text-slate-800 truncate">
                        {{ p.userName }}
                        <span v-if="p.userId === user?.id" class="text-violet-600">(나)</span>
                      </div>
                      <div class="text-xs text-slate-400">{{ getTierLabel(p.userTier) }}</div>
                    </div>
                    
                    <!-- Status -->
                    <div :class="getStatusClass(p.status)">
                      {{ getStatusLabel(p.status) }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex flex-col gap-3">
                <!-- 초대받은 사용자: 수락/거절 버튼 -->
                <template v-if="myParticipant?.status === 'INVITED'">
                  <button
                    @click="acceptInvite"
                    :disabled="actionLoading"
                    class="w-full py-4 bg-gradient-to-r from-green-500 to-emerald-600 text-white font-bold rounded-xl
                           hover:from-green-600 hover:to-emerald-700 disabled:opacity-50 transition-all active:scale-95
                           flex items-center justify-center gap-2"
                  >
                    <Check class="w-5 h-5" />
                    참가하기
                  </button>
                  <button
                    @click="declineInvite"
                    :disabled="actionLoading"
                    class="w-full py-4 bg-slate-100 text-slate-600 font-bold rounded-xl
                           hover:bg-slate-200 disabled:opacity-50 transition-all active:scale-95
                           flex items-center justify-center gap-2"
                  >
                    <X class="w-5 h-5" />
                    거절하기
                  </button>
                </template>

                <!-- 방장: 시작 버튼 -->
                <template v-else-if="isCreator && battle.status === 'PENDING'">
                  <button
                    @click="startBattle"
                    :disabled="acceptedCount < 2 || actionLoading"
                    class="w-full py-4 bg-gradient-to-r from-violet-500 to-purple-600 text-white font-bold rounded-xl
                           hover:from-violet-600 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all active:scale-95
                           flex items-center justify-center gap-2"
                  >
                    <Play class="w-5 h-5" />
                    {{ acceptedCount < 2 ? '2명 이상 수락 필요' : '배틀 시작!' }}
                  </button>
                </template>

                <!-- 배틀 진행 중 -->
                <template v-else-if="battle.status === 'IN_PROGRESS'">
                  <button
                    @click="goToPlay"
                    class="w-full py-4 bg-gradient-to-r from-violet-500 to-purple-600 text-white font-bold rounded-xl
                           hover:from-violet-600 hover:to-purple-700 transition-all active:scale-95
                           flex items-center justify-center gap-2"
                  >
                    <Swords class="w-5 h-5" />
                    배틀 참가하기
                  </button>
                </template>

                <!-- 대기 중 메시지 -->
                <div v-else-if="myParticipant?.status === 'ACCEPTED'" class="flex flex-col items-center py-6 text-slate-500">
                  <div class="mb-3 animate-pulse">
                    <BaseIconBadge :icon="Hourglass" color="orange" size="xl" />
                  </div>
                  <span class="font-bold text-slate-400">다른 참가자를 기다리는 중...</span>
                </div>
              </div>

            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Swords, Users, Crown, Check, X, Play, Clock, Hourglass, AlertCircle } from 'lucide-vue-next';
import { useAuth } from '@/composables/useAuth';
import { battleApi } from '@/api/battle';
import BaseIconBadge from '@/components/common/BaseIconBadge.vue';

const route = useRoute();
const router = useRouter();
const { user } = useAuth();

const battle = ref(null);
const loading = ref(true);
const actionLoading = ref(false);
let pollInterval = null;

const battleId = computed(() => route.params.id);

const myParticipant = computed(() => 
  battle.value?.participants?.find(p => p.userId === user.value?.id)
);

const isCreator = computed(() => battle.value?.creatorId === user.value?.id);

const acceptedCount = computed(() => 
  battle.value?.participants?.filter(p => p.status === 'ACCEPTED').length || 0
);

const getStatusColor = computed(() => {
  switch (battle.value?.status) {
    case 'PENDING': return 'orange';
    case 'IN_PROGRESS': return 'brand';
    case 'COMPLETED': return 'slate';
    case 'CANCELLED': return 'rose';
    default: return 'slate';
  }
});

const getStatusText = computed(() => {
  switch (battle.value?.status) {
    case 'PENDING': return '대기 중';
    case 'IN_PROGRESS': return '진행 중';
    case 'COMPLETED': return '완료';
    case 'CANCELLED': return '취소됨';
    default: return '';
  }
});

const getStatusIcon = computed(() => {
  switch (battle.value?.status) {
    case 'PENDING': return Hourglass;
    case 'IN_PROGRESS': return Swords;
    case 'COMPLETED': return Check;
    case 'CANCELLED': return X;
    default: return AlertCircle;
  }
});

// const statusBadgeClass = computed ... (removed)

const fetchBattle = async () => {
  try {
    const res = await battleApi.getBattle(battleId.value);
    battle.value = res.data;
    
    // 배틀이 시작되면 자동 이동
    if (battle.value.status === 'IN_PROGRESS' && myParticipant.value?.status !== 'DECLINED') {
      router.push(`/battle/${battleId.value}/play`);
    }
  } catch (e) {
    console.error('Failed to fetch battle:', e);
    battle.value = null;
  } finally {
    loading.value = false;
  }
};

const startPolling = () => {
  pollInterval = setInterval(fetchBattle, 5000);
};

const stopPolling = () => {
  if (pollInterval) {
    clearInterval(pollInterval);
    pollInterval = null;
  }
};

onMounted(() => {
  fetchBattle();
  startPolling();
});

onUnmounted(() => {
  stopPolling();
});

const acceptInvite = async () => {
  actionLoading.value = true;
  try {
    await battleApi.acceptBattle(battleId.value);
    await fetchBattle();
  } catch (e) {
    console.error('Failed to accept:', e);
    alert('수락에 실패했습니다.');
  } finally {
    actionLoading.value = false;
  }
};

const declineInvite = async () => {
  actionLoading.value = true;
  try {
    await battleApi.declineBattle(battleId.value);
    router.push('/defense');
  } catch (e) {
    console.error('Failed to decline:', e);
    alert('거절에 실패했습니다.');
  } finally {
    actionLoading.value = false;
  }
};

const startBattle = async () => {
  actionLoading.value = true;
  try {
    await battleApi.startBattle(battleId.value);
    router.push(`/battle/${battleId.value}/play`);
  } catch (e) {
    console.error('Failed to start:', e);
    alert('시작에 실패했습니다.');
  } finally {
    actionLoading.value = false;
  }
};

const goToPlay = () => {
  router.push(`/battle/${battleId.value}/play`);
};

const getStatusMessage = () => {
  switch (battle.value?.status) {
    case 'PENDING': return '참가자를 기다리는 중입니다';
    case 'IN_PROGRESS': return '배틀이 진행 중입니다!';
    case 'COMPLETED': return '배틀이 종료되었습니다';
    case 'CANCELLED': return '배틀이 취소되었습니다';
    default: return '';
  }
};



const getParticipantBgClass = (status) => {
  switch (status) {
    case 'ACCEPTED': return 'bg-green-50';
    case 'DECLINED': return 'bg-red-50';
    default: return 'bg-slate-50';
  }
};

const getAvatarRingClass = (status) => {
  switch (status) {
    case 'ACCEPTED': return 'ring-green-400';
    case 'DECLINED': return 'ring-red-400';
    default: return 'ring-slate-300';
  }
};

const getStatusClass = (status) => {
  const base = 'px-3 py-1 rounded-full text-xs font-bold';
  switch (status) {
    case 'ACCEPTED': return `${base} bg-green-100 text-green-700`;
    case 'DECLINED': return `${base} bg-red-100 text-red-700`;
    case 'IN_PROGRESS': return `${base} bg-violet-100 text-violet-700`;
    case 'COMPLETED': return `${base} bg-slate-100 text-slate-700`;
    default: return `${base} bg-amber-100 text-amber-700`;
  }
};

const getStatusLabel = (status) => {
  switch (status) {
    case 'INVITED': return '대기중';
    case 'ACCEPTED': return '준비완료';
    case 'DECLINED': return '거절함';
    case 'IN_PROGRESS': return '진행중';
    case 'COMPLETED': return '완료';
    default: return status;
  }
};

const getTierLabel = (tier) => {
  if (!tier || tier === 0) return '언랭크';
  const tiers = ['', 'Bronze V', 'Bronze IV', 'Bronze III', 'Bronze II', 'Bronze I',
    'Silver V', 'Silver IV', 'Silver III', 'Silver II', 'Silver I',
    'Gold V', 'Gold IV', 'Gold III', 'Gold II', 'Gold I',
    'Platinum V', 'Platinum IV', 'Platinum III', 'Platinum II', 'Platinum I',
    'Diamond V', 'Diamond IV', 'Diamond III', 'Diamond II', 'Diamond I',
    'Ruby V', 'Ruby IV', 'Ruby III', 'Ruby II', 'Ruby I', 'Master'];
  return tiers[tier] || '언랭크';
};
</script>
