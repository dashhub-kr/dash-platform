<template>
  <!-- Main Layout Wrapper matching DashboardView -->
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">
      <div class="min-h-screen bg-white pb-20">
    
    <!-- 활성화된 디펜스 오버레이 (전체화면 고정) -->
    <div v-if="status.defenseProblemId && !loading" class="fixed inset-0 z-50 bg-slate-50 flex items-center justify-center p-4">
      <div class="w-full max-w-3xl animate-fade-in-up">
        <div class="bg-white rounded-3xl p-8 md:p-12 shadow-xl border border-slate-200 text-center relative overflow-hidden">
          <!-- 상단 강조선 -->
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-brand-500 via-sky-500 to-brand-500"></div>

          <div class="inline-block px-4 py-1.5 rounded-full text-sm font-bold mb-6"
               :class="status.defenseType === 'GOLD' ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-700'">
            {{ status.defenseType === 'GOLD' ? '🟡 골드 등급' : '⚪ 실버 등급' }} 디펜스
          </div>

          <h1 class="text-3xl md:text-4xl font-black text-slate-800 mb-8">
            ⚔️ 진행 중인 미션
          </h1>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-10">
            <!-- 타이머 카드 -->
            <div class="bg-slate-50 p-6 rounded-2xl border border-slate-200 flex flex-col items-center justify-center">
              <span class="text-slate-500 text-xs uppercase tracking-widest mb-2 font-bold">남은 시간</span>
              <div class="text-4xl font-mono font-black text-slate-900 tabular-nums">
                {{ formattedTimeLeft }}
              </div>
            </div>

            <!-- 문제 카드 -->
            <a :href="`https://www.acmicpc.net/problem/${status.defenseProblemId}`" target="_blank" 
               class="bg-slate-50 p-6 rounded-2xl border border-slate-200 flex flex-col items-center justify-center group hover:bg-slate-100 transition-all hover:-translate-y-1 cursor-pointer">
              <span class="text-slate-500 text-xs uppercase tracking-widest mb-2 font-bold">목표 문제</span>
              <div class="text-4xl font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                #{{ status.defenseProblemId }}
              </div>
              <span class="mt-2 text-xs text-slate-400 group-hover:text-slate-600">백준에서 풀기 ↗</span>
            </a>
          </div>

          <div class="flex flex-col items-center gap-4">
            <button @click="refreshStatus" 
              class="px-8 py-4 bg-brand-600 hover:bg-brand-500 text-white rounded-xl font-bold text-lg shadow-lg shadow-brand-500/30 transition-all active:scale-95 flex items-center gap-2">
              <RefreshCw class="w-5 h-5" /> 상태 확인
            </button>
            <p class="text-slate-500 text-sm max-w-md leading-relaxed break-keep">
              문제를 제출하고 상태 확인 버튼을 눌러주세요! <br/>
              
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 메인 레이아웃 컨테이너 (선택 화면) -->
    <div v-else class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full items-start">
      
      <!-- 메인 피드 -->
      <div class="flex-1 min-w-0 space-y-8">
        
        <!-- Header -->
        <div class="mb-8">
          <div class="flex items-center gap-3 mb-2">
            <Shield class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
            <h1 class="text-xl font-black text-slate-800">랜덤 디펜스</h1>
          </div>
          <p class="text-slate-500 font-medium">무작위 문제를 해결하며 최다 연승에 도전해보세요</p>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="flex flex-col items-center justify-center py-20">
          <div class="w-12 h-12 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mb-4"></div>
          <p class="text-slate-400 font-medium">불러오는 중...</p>
        </div>

        <template v-else>
          <!-- 디펜스 카드 -->
          <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <Sword class="w-5 h-5 text-slate-500" fill="currentColor" />
              등급 선택
            </h2>
            <div class="grid grid-cols-2 gap-6">
              <!-- Silver Card -->
              <div class="group flex flex-col items-center p-6 rounded-2xl border border-transparent hover:border-slate-200 transition-all">
                <div class="flex flex-col items-center w-full">
                  <div class="relative mb-4 transition-transform duration-500 group-hover:scale-110 group-hover:-rotate-3">
                    <img src="/images/defense/silver.png" alt="Silver Defense" class="w-32 h-32 object-contain drop-shadow-lg" />
                  </div>
                  <h3 class="text-xl font-black text-slate-800 mb-1">실버 등급</h3>
                  <p class="text-slate-400 text-sm font-bold mb-3">기본 알고리즘 역량</p>
                  <div class="flex items-center gap-2 bg-slate-100 px-4 py-1.5 rounded-full mb-4">
                    <span class="text-lg font-black text-slate-800">{{ status.silverStreak }}</span>
                    <span class="text-slate-500 text-xs font-bold uppercase">연승중</span>
                  </div>
                </div>
                <div class="flex gap-2 w-full">
                  <button
                    @click="startDefense('SILVER')"
                    class="flex-1 py-2.5 px-3 bg-slate-100 text-slate-700 font-bold rounded-xl
                           hover:bg-slate-200 transition-all active:scale-95 text-sm flex items-center justify-center gap-1"
                  >
                    <Sword class="w-4 h-4" />
                    혼자하기
                  </button>
                  <button
                    @click.stop="openBattleModal('SILVER')"
                    class="flex-1 py-2.5 px-3 bg-violet-100 text-violet-700 font-bold rounded-xl
                           hover:bg-violet-200 transition-all active:scale-95 text-sm flex items-center justify-center gap-1"
                  >
                    <Users class="w-4 h-4" />
                    함께하기
                  </button>
                </div>
              </div>

              <!-- Gold Card -->
              <div class="group flex flex-col items-center p-6 rounded-2xl border border-transparent hover:border-amber-200 transition-all">
                <div class="flex flex-col items-center w-full">
                  <div class="relative mb-4 transition-transform duration-500 group-hover:scale-110 group-hover:rotate-3">
                    <img src="/images/defense/gold.png" alt="Gold Defense" class="w-32 h-32 object-contain drop-shadow-lg" />
                  </div>
                  <h3 class="text-xl font-black text-amber-800 mb-1">골드 등급</h3>
                  <p class="text-amber-700/60 text-sm font-bold mb-3">심화 알고리즘 역량</p>
                  <div class="flex items-center gap-2 bg-amber-100 px-4 py-1.5 rounded-full mb-4">
                    <span class="text-lg font-black text-amber-900">{{ status.goldStreak }}</span>
                    <span class="text-amber-700 text-xs font-bold uppercase">연승중</span>
                  </div>
                </div>
                <div class="flex gap-2 w-full">
                  <button
                    @click="startDefense('GOLD')"
                    class="flex-1 py-2.5 px-3 bg-amber-50 text-amber-700 font-bold rounded-xl
                           hover:bg-amber-100 transition-all active:scale-95 text-sm flex items-center justify-center gap-1"
                  >
                    <Sword class="w-4 h-4" />
                    혼자하기
                  </button>
                  <button
                    @click.stop="openBattleModal('GOLD')"
                    class="flex-1 py-2.5 px-3 bg-amber-100 text-amber-700 font-bold rounded-xl
                           hover:bg-amber-200 transition-all active:scale-95 text-sm flex items-center justify-center gap-1"
                  >
                    <Users class="w-4 h-4" />
                    함께하기
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 사이드바 -->
      <aside class="w-[380px] shrink-0 hidden xl:flex flex-col gap-6 sticky top-8 h-fit">
        
        <!-- 연승 기록 -->
        <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200">
          <h3 class="font-bold text-slate-800 text-sm mb-4 flex items-center gap-2">
            <Flame class="w-5 h-5 text-orange-500" fill="currentColor" />
            최고 연승 기록
          </h3>
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-slate-600 font-medium">실버</span>
              <span class="font-black text-slate-800">{{ status.maxSilverStreak || 0 }}연승</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-amber-700 font-medium">골드</span>
              <span class="font-black text-amber-800">{{ status.maxGoldStreak || 0 }}연승</span>
            </div>
          </div>
        </div>

        <!-- 규칙 안내 -->
        <div class="bg-gradient-to-br from-brand-50 to-sky-50 rounded-3xl p-6 border border-brand-100">
          <h3 class="font-bold text-brand-800 text-sm mb-4 flex items-center gap-2">
            <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center">
              <ClipboardList class="w-3 h-3 text-white" />
            </div>
            디펜스 규칙
          </h3>
          <div class="space-y-3 text-sm text-slate-600">
            <div class="flex items-start gap-3">
              <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center shrink-0 mt-0.5">
                <Target class="w-3 h-3 text-white" />
              </div>
              <span>랜덤 문제가 1개 출제됩니다.</span>
            </div>
            <div class="flex items-start gap-3">
              <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center shrink-0 mt-0.5">
                <Clock class="w-3 h-3 text-white" />
              </div>
              <span>제한 시간 내에 풀어야 합니다.</span>
            </div>
            <div class="flex items-start gap-3">
              <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center shrink-0 mt-0.5">
                <Zap class="w-3 h-3 text-white" />
              </div>
              <span>성공하면 연승, 실패하면 초기화!</span>
            </div>
          </div>
        </div>

      </aside>
    </div>
    </div>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="fixed inset-0 z-[60] flex items-center justify-center bg-black/40 backdrop-blur-sm animate-fade-in">
      <div class="bg-white border border-slate-200 p-10 rounded-3xl max-w-md text-center shadow-2xl animate-bounce-in">
        <div class="text-6xl mb-6">🎉</div>
        <h2 class="text-3xl font-black text-slate-900 mb-2">디펜스 성공!</h2>
        <p class="text-slate-500 mb-8 break-keep">연승 기록을 갱신했습니다. 계속 도전하세요!</p>
        <div class="flex justify-center gap-4">
          <button @click="showSuccessModal = false" class="px-6 py-3 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl font-bold transition-all">
            닫기
          </button>
          <button @click="resetAndPlayAgain" class="px-6 py-3 bg-brand-600 hover:bg-brand-500 text-white rounded-xl font-bold shadow-lg shadow-brand-500/30 transition-all">
            다음 도전 ➡
          </button>
        </div>
      </div>
    </div>
      </div>
    </div>
  </div>

  <!-- Battle Invite Modal -->
  <BattleInviteModal
    :isOpen="showBattleModal"
    battleType="DEFENSE"
    :battleSubType="selectedDefenseType"
    @close="showBattleModal = false"
  />
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { Shield, Sword, Flame, RefreshCw, ClipboardList, Target, Clock, Zap, Users } from 'lucide-vue-next';
import BattleInviteModal from '@/components/battle/BattleInviteModal.vue';

const router = useRouter();
const loading = ref(true);
const status = ref({
    defenseType: null,
    defenseProblemId: null,
    defenseStartTime: null,
    silverStreak: 0,
    goldStreak: 0,
    maxSilverStreak: 0,
    maxGoldStreak: 0
});
const showSuccessModal = ref(false);
const timerInterval = ref(null);
const now = ref(new Date());

const formattedTimeLeft = computed(() => {
    if (!status.value.defenseStartTime) return "00:00:00.00";
    
    const startTime = new Date(status.value.defenseStartTime);
    const endTime = new Date(startTime.getTime() + 60 * 60 * 1000); // 1 hour
    const diff = endTime - now.value;

    if (diff <= 0) return "00:00:00.00";

    const m = Math.floor((diff / 1000 / 60) % 60);
    const s = Math.floor((diff / 1000) % 60);
    const ms = Math.floor((diff % 1000) / 10); // 2 digits

    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}.${ms.toString().padStart(2, '0')}`;
});

onMounted(async () => {
    await fetchStatus();
    timerInterval.value = setInterval(() => {
        now.value = new Date();
    }, 50); // High refresh for ms
});

onUnmounted(() => {
    if (timerInterval.value) clearInterval(timerInterval.value);
});

const fetchStatus = async () => {
    loading.value = true;
    try {
        const res = await axios.get('/api/defense/status');
        status.value = res.data;
    } catch (e) {
        console.error("Failed to fetch defense status", e);
    } finally {
        loading.value = false;
    }
};

const refreshStatus = async () => {
    try {
        const prevId = status.value.defenseProblemId;
        const res = await axios.get('/api/defense/status');
        const nextId = res.data.defenseProblemId;
        
        // 막 성공했는지 확인
        if (prevId && !nextId && (res.data.silverStreak > status.value.silverStreak || res.data.goldStreak > status.value.goldStreak)) {
             showSuccessModal.value = true;
        }
        
        status.value = res.data;
    } catch (e) {
        console.error("Failed to refresh status", e);
    }
};

const startDefense = async (type) => {
    const typeLabel = type === 'GOLD' ? '골드' : '실버';
    if (!confirm(`${typeLabel} 등급 디펜스를 시작하시겠습니까?`)) return;
    try {
        await axios.post('/api/defense/start', { type });
        await fetchStatus();
    } catch (e) {
        alert("시작 실패: " + (e.response?.data?.message || e.message));
    }
};

const resetAndPlayAgain = () => {
    showSuccessModal.value = false;
    // Just close modal, user sees selection screen
};

// Battle Modal
const showBattleModal = ref(false);
const selectedDefenseType = ref(null);

const openBattleModal = (type) => {
    selectedDefenseType.value = type;
    showBattleModal.value = true;
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;700;900&display=swap');

.animate-fade-in-up {
    animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.animate-fade-in {
    animation: fadeIn 1s ease-out forwards;
}

.animate-bounce-in {
    animation: bounceIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards;
}

@keyframes fadeInUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

@keyframes bounceIn {
    0% { transform: scale(0.8); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
}
</style>
