<template>
  <div class="min-h-screen bg-white text-slate-800 overflow-hidden">

    <div class="relative z-10 flex flex-col items-center justify-center h-full px-4">
      
      <!-- Loading State -->
      <div v-if="loading" class="text-slate-900 text-2xl font-bold animate-pulse">
        탐색 중...
      </div>

      <!-- Active Defense View -->
      <div v-else-if="status.defenseProblemId" class="w-full max-w-4xl animate-fade-in-up">
        <div class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-3xl p-8 md:p-12 shadow-2xl text-center relative overflow-hidden ring-1 ring-black/5">
            <!-- Glow behind -->
            <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full h-1 bg-gradient-to-r from-transparent via-brand-500 to-transparent shadow-[0_0_20px_rgba(99,102,241,0.5)]"></div>

            <div class="inline-block px-4 py-1 rounded-full bg-slate-100 text-brand-600 text-sm font-bold mb-6 border border-slate-200">
                {{ status.defenseType === 'GOLD' ? '🟡 골드 등급' : '⚪ 실버 등급' }} 디펜스
            </div>

            <h1 class="text-5xl md:text-6xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-slate-900 to-slate-600 mb-8 drop-shadow-sm">
                진행 중인 미션
            </h1>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-10">
                <!-- Timer Card -->
                <div class="bg-slate-50 p-6 rounded-2xl border border-slate-200 flex flex-col items-center justify-center group hover:bg-slate-100 transition-colors shadow-sm">
                    <span class="text-slate-500 text-sm uppercase tracking-widest mb-2 font-semibold">남은 시간</span>
                    <div class="text-5xl font-mono font-bold text-slate-900 tabular-nums tracking-tight group-hover:text-brand-600 transition-colors">
                        {{ formattedTimeLeft }}
                    </div>
                </div>

                <!-- Problem Card -->
                <a :href="`https://www.acmicpc.net/problem/${status.defenseProblemId}`" target="_blank" class="bg-slate-50 p-6 rounded-2xl border border-slate-200 flex flex-col items-center justify-center group hover:bg-slate-100 transition-all hover:-translate-y-1 cursor-pointer shadow-sm">
                    <span class="text-slate-500 text-sm uppercase tracking-widest mb-2 font-semibold">목표 문제</span>
                    <div class="text-5xl font-bold text-slate-900 group-hover:text-emerald-600 transition-colors">
                        #{{ status.defenseProblemId }}
                    </div>
                    <span class="mt-2 text-xs text-slate-500 group-hover:text-slate-800">백준에서 풀기 ↗</span>
                </a>
            </div>

            <div class="flex flex-col items-center gap-4">
               <button @click="refreshStatus" 
                class="px-8 py-4 bg-brand-600 hover:bg-brand-500 text-white rounded-xl font-bold text-lg shadow-lg shadow-brand-500/30 transition-all active:scale-95 flex items-center gap-2">
                 <span>🔄 상태 확인</span>
               </button>
               <p class="text-slate-500 text-sm max-w-md leading-relaxed break-keep">
                   문제를 해결하고 깃허브에 푸시하세요.<br/>
                   시스템이 웹훅을 통해 자동으로 성공 여부를 확인합니다.
               </p>
            </div>
        </div>
      </div>

      <!-- Selection View -->
      <div v-else class="w-full max-w-4xl animate-fade-in">
        <div class="text-center mb-12">
            <h1 class="text-4xl md:text-6xl font-black text-slate-900 mb-4 tracking-tight drop-shadow-sm">
                랜덤 디펜스
            </h1>
            <p class="text-slate-500 text-lg md:text-xl font-medium">
                난이도를 선택해 도전하세요. 연승을 지켜내야 합니다.
            </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 md:gap-12 px-4">
            <!-- Silver Card -->
            <!-- Silver Card -->
            <div @click="startDefense('SILVER')" class="group cursor-pointer flex flex-col items-center p-6 hover:bg-slate-50 rounded-[2rem] transition-all active:scale-95">
                <div class="relative mb-6 transition-transform duration-500 group-hover:scale-110 group-hover:-rotate-3">
                    <img src="/defense/silver-def.png" alt="Silver Defense" class="w-48 h-48 object-contain drop-shadow-2xl" />
                </div>
                <h2 class="text-3xl font-black text-slate-800 mb-2 font-[Outfit]">실버 등급</h2>
                <p class="text-slate-400 mb-6 text-center text-sm font-bold break-keep">기본적인 알고리즘 역량 테스트</p>
                <div class="mt-auto flex items-center gap-3 bg-slate-100 px-6 py-2 rounded-full">
                    <span class="text-slate-500 text-xs font-black uppercase">연승</span>
                    <span class="text-xl font-black text-slate-800">{{ status.silverStreak }}</span>
                </div>
            </div>

            <!-- Gold Card -->
            <!-- Gold Card -->
            <div @click="startDefense('GOLD')" class="group cursor-pointer flex flex-col items-center p-6 hover:bg-slate-50 rounded-[2rem] transition-all active:scale-95">
                <div class="relative mb-6 transition-transform duration-500 group-hover:scale-110 group-hover:rotate-3">
                    <img src="/defense/gold-def.png" alt="Gold Defense" class="w-48 h-48 object-contain drop-shadow-2xl" />
                </div>
                <h2 class="text-3xl font-black text-amber-900 mb-2 font-[Outfit]">골드 등급</h2>
                <p class="text-amber-800/60 mb-6 text-center text-sm font-bold break-keep">심화 알고리즘 문제해결 능력</p>
                <div class="mt-auto flex items-center gap-3 bg-amber-50 px-6 py-2 rounded-full">
                     <span class="text-amber-700 text-xs font-black uppercase">연승</span>
                    <span class="text-xl font-black text-amber-900">{{ status.goldStreak }}</span>
                </div>
            </div>
        </div>

        <button @click="$router.push('/')" class="mt-12 text-slate-400 hover:text-slate-900 transition-colors flex items-center justify-center w-full gap-2">
            <span>⬅ 대시보드로 돌아가기</span>
        </button>
      </div>

    </div>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm animate-fade-in">
        <div class="bg-white border border-slate-200 p-10 rounded-3xl max-w-md text-center shadow-2xl animate-bounce-in">
            <div class="text-6xl mb-6 animate-pulse">🎉</div>
            <h2 class="text-4xl font-black text-slate-900 mb-2">디펜스 성공!</h2>
            <p class="text-slate-500 mb-8 break-keep">연승 기록을 갱신하고 있습니다. 계속 도전하세요!</p>
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
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
// Assuming axios is globally configured or imported from a helper. 
// If not, I'll try to import from axios directly, assuming user has it.
import axios from 'axios';

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
        
        // Check if just succeeded
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
