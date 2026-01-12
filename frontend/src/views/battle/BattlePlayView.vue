<template>
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">
      <div class="min-h-screen bg-slate-50 pb-20">
        
        <!-- Loading -->
        <div v-if="loading" class="fixed inset-0 flex items-center justify-center bg-slate-50">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-violet-200 border-t-violet-600 rounded-full animate-spin mb-4 mx-auto"></div>
            <p class="text-slate-400 font-medium">배틀 정보를 불러오는 중...</p>
          </div>
        </div>

        <!-- Main Layout -->
        <div v-else class="flex justify-center p-4 md:p-8">
          <div class="flex gap-6 max-w-screen-xl w-full">
            
            <!-- Main Content (Left) -->
            <div class="flex-1 min-w-0">
              
              <!-- Header -->
              <div class="bg-gradient-to-r from-violet-500 to-purple-600 rounded-3xl p-6 text-white mb-6 shadow-xl">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-3">
                    <Swords class="w-7 h-7" />
                    <h1 class="text-2xl font-black">
                      {{ battle?.type === 'DEFENSE' ? '디펜스' : '모의고사' }} 배틀
                    </h1>
                  </div>
                  <div class="text-right">
                    <div class="text-3xl font-mono font-black tabular-nums">
                      {{ formattedTimeLeft }}
                    </div>
                    <div class="text-white/70 text-sm">남은 시간</div>
                  </div>
                </div>
              </div>

              <!-- Problem Card -->
              <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 mb-6">
                <h2 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                  <FileText class="w-5 h-5 text-violet-500" />
                  출제 문제
                </h2>

                <div v-if="problems.length === 0" class="text-center py-8 text-slate-400">
                  문제가 없습니다
                </div>

                <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <a
                    v-for="(problem, idx) in problems"
                    :key="problem.id"
                    :href="`https://www.acmicpc.net/problem/${problem.id}`"
                    target="_blank"
                    class="group flex items-center gap-4 p-4 rounded-xl border transition-all"
                    :class="isSolved(problem.id) 
                      ? 'bg-green-50 border-green-200' 
                      : 'bg-slate-50 border-slate-200 hover:bg-slate-100'"
                  >
                    <div class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-lg"
                         :class="isSolved(problem.id) ? 'bg-green-500 text-white' : 'bg-slate-200 text-slate-600'">
                      {{ isSolved(problem.id) ? '✓' : idx + 1 }}
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="font-semibold text-slate-800 group-hover:text-violet-600 transition-colors">
                        #{{ problem.id }}
                      </div>
                      <div class="text-xs text-slate-400">클릭하여 백준에서 풀기 ↗</div>
                    </div>
                  </a>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex gap-4">
                <button
                  @click="refreshStatus"
                  :disabled="refreshing"
                  class="flex-1 py-4 bg-gradient-to-r from-violet-500 to-purple-600 text-white font-bold rounded-xl
                         hover:from-violet-600 hover:to-purple-700 disabled:opacity-50 transition-all active:scale-95
                         flex items-center justify-center gap-2"
                >
                  <RefreshCw class="w-5 h-5" :class="{ 'animate-spin': refreshing }" />
                  {{ refreshing ? '확인 중...' : '상태 확인' }}
                </button>
                <button
                  @click="forfeit"
                  class="px-6 py-4 bg-slate-100 text-slate-600 font-bold rounded-xl
                         hover:bg-slate-200 transition-all active:scale-95"
                >
                  포기하기
                </button>
              </div>

              <p class="text-center text-slate-400 text-sm mt-4">
                문제를 백준에서 풀고 제출한 후 "상태 확인" 버튼을 눌러주세요!
              </p>
            </div>

            <!-- Sidebar (Right) - Participants -->
            <aside class="w-80 shrink-0 hidden xl:block">
              <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 sticky top-8">
                <h2 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                  <Trophy class="w-5 h-5 text-amber-500" />
                  실시간 순위
                </h2>

                <div class="space-y-3">
                  <div
                    v-for="(p, idx) in sortedParticipants"
                    :key="p.id"
                    class="flex items-center gap-3 p-3 rounded-xl transition-colors"
                    :class="p.userId === user?.id ? 'bg-violet-50 ring-2 ring-violet-200' : 'bg-slate-50'"
                  >
                    <!-- Rank -->
                    <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm"
                         :class="getRankClass(idx)">
                      {{ idx + 1 }}
                    </div>
                    
                    <!-- Avatar -->
                    <img
                      :src="p.userAvatar || '/default-avatar.png'"
                      class="w-10 h-10 rounded-full object-cover"
                    />
                    
                    <!-- Info -->
                    <div class="flex-1 min-w-0">
                      <div class="font-semibold text-slate-800 truncate text-sm">
                        {{ p.userName }}
                        <span v-if="p.userId === user?.id" class="text-violet-600">(나)</span>
                      </div>
                      <div class="text-xs text-slate-400">
                        {{ getParticipantStatusText(p) }}
                      </div>
                    </div>
                    
                    <!-- Score -->
                    <div class="text-right">
                      <div class="font-bold text-slate-800">{{ p.score || 0 }}</div>
                      <div class="text-xs text-slate-400">점</div>
                    </div>
                  </div>
                </div>

                <div class="mt-4 pt-4 border-t border-slate-100 text-center text-xs text-slate-400">
                  5초마다 자동 갱신됩니다
                </div>
              </div>
            </aside>
          </div>
        </div>

        <!-- Battle Complete Modal -->
        <Teleport to="body">
          <div v-if="showResultModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div class="absolute inset-0 bg-black/50 backdrop-blur-sm"></div>
            <div class="relative bg-white rounded-3xl p-8 max-w-md w-full text-center animate-bounce-in">
              <!-- 승리/패배 분기 -->
              <div v-if="isWinner" class="text-6xl mb-4">🎉</div>
              <div v-else class="text-6xl mb-4">💪</div>
              
              <h2 class="text-2xl font-black text-slate-800 mb-2">
                {{ isWinner ? '축하합니다! 승리!' : '아쉽네요!' }}
              </h2>
              <p class="text-slate-500 mb-6">
                {{ isWinner ? '멋진 실력이에요!' : '다음엔 꼭 이겨봐요!' }}
              </p>
              
              <!-- Results -->
              <div class="space-y-2 mb-6">
                <div v-for="(p, idx) in sortedParticipants" :key="p.id"
                     class="flex items-center gap-3 p-3 rounded-xl transition-all"
                     :class="[
                       idx === 0 ? 'bg-amber-50 ring-2 ring-amber-300' : 'bg-slate-50',
                       p.userId === user?.id ? 'ring-2 ring-violet-300' : ''
                     ]">
                  <div class="text-2xl">{{ idx === 0 ? '🥇' : idx === 1 ? '🥈' : '🥉' }}</div>
                  <div class="flex-1 text-left">
                    <div class="font-semibold">
                      {{ p.userName }}
                      <span v-if="p.userId === user?.id" class="text-violet-600 text-sm">(나)</span>
                    </div>
                    <div class="text-xs text-slate-400">
                      {{ p.problemsSolved || 0 }}문제 · {{ formatTime(p.totalTimeSeconds) }}
                    </div>
                  </div>
                  <div class="text-right">
                    <div class="font-bold text-lg">{{ p.score || 0 }}</div>
                    <div class="text-xs text-slate-400">점</div>
                  </div>
                </div>
              </div>

              <button
                @click="goBack"
                class="w-full py-3 bg-gradient-to-r from-violet-500 to-purple-600 text-white font-bold rounded-xl
                       hover:from-violet-600 hover:to-purple-700 transition-all active:scale-95"
              >
                확인
              </button>
            </div>
          </div>
        </Teleport>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Swords, FileText, RefreshCw, Trophy } from 'lucide-vue-next';
import { useAuth } from '@/composables/useAuth';
import { battleApi } from '@/api/battle';

const route = useRoute();
const router = useRouter();
const { user } = useAuth();

const battle = ref(null);
const loading = ref(true);
const refreshing = ref(false);
const showResultModal = ref(false);
const now = ref(new Date());

let pollInterval = null;
let timerInterval = null;

const battleId = computed(() => route.params.id);

// 문제 목록 (JSON 파싱)
const problems = computed(() => {
  if (!battle.value?.problemIds) return [];
  try {
    const ids = JSON.parse(battle.value.problemIds);
    return ids.map(id => ({ id }));
  } catch {
    return [];
  }
});

// 내 참가 정보
const myParticipant = computed(() => 
  battle.value?.participants?.find(p => p.userId === user.value?.id)
);

// 승패 판정: 문제 수 > 시간 순 정렬
const sortedParticipants = computed(() => {
  if (!battle.value?.participants) return [];
  return [...battle.value.participants]
    .filter(p => p.status !== 'DECLINED')
    .sort((a, b) => {
      // 1순위: 문제 수 (많을수록 높은 순위)
      if ((b.problemsSolved || 0) !== (a.problemsSolved || 0)) {
        return (b.problemsSolved || 0) - (a.problemsSolved || 0);
      }
      // 2순위: 시간 (짧을수록 높은 순위)
      return (a.totalTimeSeconds || 0) - (b.totalTimeSeconds || 0);
    });
});

// 내 순위 계산
const myRank = computed(() => {
  const idx = sortedParticipants.value.findIndex(p => p.userId === user.value?.id);
  return idx >= 0 ? idx + 1 : null;
});

// 승리 여부
const isWinner = computed(() => myRank.value === 1);

// 시간 포맷 헬퍼
const formatTime = (seconds) => {
  if (!seconds) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
};

// 남은 시간 계산
const formattedTimeLeft = computed(() => {
  if (!battle.value?.startedAt) return '00:00:00';
  
  const startTime = new Date(battle.value.startedAt);
  const duration = battle.value.type === 'DEFENSE' ? 60 * 60 * 1000 : 2 * 60 * 60 * 1000; // 1시간 or 2시간
  const endTime = new Date(startTime.getTime() + duration);
  const diff = endTime - now.value;
  
  if (diff <= 0) return '00:00:00';
  
  const h = Math.floor(diff / 1000 / 60 / 60);
  const m = Math.floor((diff / 1000 / 60) % 60);
  const s = Math.floor((diff / 1000) % 60);
  
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
});

const isSolved = (problemId) => {
  if (!myParticipant.value?.solvedProblemIds) return false;
  try {
    const solvedIds = JSON.parse(myParticipant.value.solvedProblemIds);
    return solvedIds.includes(problemId);
  } catch {
    return false;
  }
};

const fetchBattle = async () => {
  try {
    const res = await battleApi.getBattle(battleId.value);
    battle.value = res.data;
    
    // 배틀 완료 체크
    if (battle.value.status === 'COMPLETED') {
      showResultModal.value = true;
      stopPolling();
    }
  } catch (e) {
    console.error('Failed to fetch battle:', e);
  } finally {
    loading.value = false;
  }
};

const refreshStatus = async () => {
  refreshing.value = true;
  try {
    await fetchBattle();
  } finally {
    refreshing.value = false;
  }
};

const forfeit = async () => {
  if (!confirm('정말 포기하시겠습니까?')) return;
  
  try {
    await battleApi.submitResult(battleId.value, 0, 0, 0);
    router.push('/defense');
  } catch (e) {
    console.error('Failed to forfeit:', e);
  }
};

const goBack = () => {
  router.push('/defense');
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

const startTimer = () => {
  timerInterval = setInterval(() => {
    now.value = new Date();
  }, 1000);
};

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
};

onMounted(() => {
  fetchBattle();
  startPolling();
  startTimer();
});

onUnmounted(() => {
  stopPolling();
  stopTimer();
});

const getRankClass = (idx) => {
  switch (idx) {
    case 0: return 'bg-amber-400 text-white';
    case 1: return 'bg-slate-400 text-white';
    case 2: return 'bg-orange-400 text-white';
    default: return 'bg-slate-200 text-slate-600';
  }
};

const getParticipantStatusText = (p) => {
  if (p.status === 'COMPLETED') return '완료';
  if (p.status === 'IN_PROGRESS') return `${p.problemsSolved || 0}문제 풀이 중`;
  return p.status;
};
</script>

<style scoped>
.animate-bounce-in {
  animation: bounceIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards;
}

@keyframes bounceIn {
  0% { transform: scale(0.8); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
</style>
