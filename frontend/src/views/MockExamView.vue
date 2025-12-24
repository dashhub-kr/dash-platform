<template>
  <div class="mockexam-container relative w-full h-screen overflow-hidden bg-slate-50 font-[Pretendard]">
    <!-- Background Effects -->
    <div class="absolute inset-0 bg-gradient-to-br from-violet-50 via-white to-slate-100"></div>
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
       <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-violet-200/40 rounded-full blur-3xl animate-pulse mix-blend-multiply"></div>
       <div class="absolute bottom-1/3 right-1/4 w-80 h-80 bg-amber-200/40 rounded-full blur-3xl animate-pulse delay-1000 mix-blend-multiply"></div>
    </div>

    <div class="relative z-10 flex flex-col items-center justify-center h-full px-4">
      
      <!-- Loading State -->
      <div v-if="loading" class="text-slate-900 text-2xl font-bold animate-pulse">
        불러오는 중...
      </div>

      <!-- Active Exam View -->
      <div v-else-if="status.examType" class="w-full max-w-5xl animate-fade-in-up">
        <div class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-3xl p-8 md:p-12 shadow-2xl text-center relative overflow-hidden ring-1 ring-black/5">
            <!-- Glow behind -->
            <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full h-1 bg-gradient-to-r from-transparent via-violet-500 to-transparent shadow-[0_0_20px_rgba(139,92,246,0.5)]"></div>

            <div class="inline-block px-4 py-1 rounded-full bg-slate-100 text-violet-600 text-sm font-bold mb-6 border border-slate-200">
                {{ status.category }} - {{ status.displayName }}
            </div>

            <h1 class="text-4xl md:text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-slate-900 to-slate-600 mb-8 drop-shadow-sm">
                진행 중인 시험
            </h1>

            <!-- Timer Card -->
            <div class="bg-slate-50 p-6 rounded-2xl border border-slate-200 mb-8">
                <span class="text-slate-500 text-sm uppercase tracking-widest mb-2 font-semibold block">남은 시간</span>
                <div class="text-5xl font-mono font-bold text-slate-900 tabular-nums tracking-tight">
                    {{ formattedTimeLeft }}
                </div>
            </div>

            <!-- Problem List -->
            <div class="mb-10">
                <h3 class="text-lg font-bold text-slate-700 mb-4">출제 문제 ({{ status.solvedCount }}/{{ status.totalCount }})</h3>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <a v-for="(problem, index) in status.problems" :key="problem"
                       :href="`https://www.acmicpc.net/problem/${problem}`" target="_blank"
                       class="bg-white p-4 rounded-xl border-2 transition-all hover:-translate-y-1 shadow-sm"
                       :class="isProblemSolved(problem) 
                         ? 'border-emerald-300 bg-emerald-50' 
                         : 'border-slate-200 hover:border-violet-300'">
                        <div class="text-sm text-slate-500 mb-1">문제 {{ index + 1 }}</div>
                        <div class="text-2xl font-bold" 
                             :class="isProblemSolved(problem) ? 'text-emerald-600' : 'text-slate-800'">
                            #{{ problem }}
                        </div>
                        <div v-if="isProblemSolved(problem)" class="text-emerald-600 text-sm font-bold mt-1">✓ 완료</div>
                        <div v-else class="text-slate-400 text-xs mt-1">백준에서 풀기 ↗</div>
                    </a>
                </div>
            </div>

            <div class="flex flex-col items-center gap-4">
               <div class="flex gap-4">
                   <button @click="refreshStatus" 
                    class="px-8 py-4 bg-violet-600 hover:bg-violet-500 text-white rounded-xl font-bold text-lg shadow-lg shadow-violet-500/30 transition-all active:scale-95 flex items-center gap-2">
                     <span>🔄 상태 확인</span>
                   </button>
                   <button @click="cancelExam" 
                    class="px-8 py-4 bg-slate-200 hover:bg-slate-300 text-slate-700 rounded-xl font-bold text-lg transition-all active:scale-95">
                     시험 취소
                   </button>
               </div>
               <p class="text-slate-500 text-sm max-w-md leading-relaxed break-keep">
                   문제를 해결하고 깃허브에 푸시하세요.<br/>
                   시스템이 웹훅을 통해 자동으로 성공 여부를 확인합니다.
               </p>
            </div>
        </div>
      </div>

      <!-- Selection View -->
      <div v-else class="w-full max-w-6xl animate-fade-in py-8 px-4">
        <div class="text-center mb-12">
            <h1 class="text-4xl md:text-6xl font-black text-slate-900 mb-4 tracking-tight drop-shadow-sm">
                모의고사 / 코딩테스트
            </h1>
            <p class="text-slate-500 text-lg md:text-xl font-medium">
                역량 수준에 맞는 시험을 선택하세요. 이미 푼 문제는 제외됩니다.
            </p>
        </div>

        <!-- 모의고사 Section -->
        <div class="mb-12">
            <h2 class="text-2xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                <span class="w-8 h-8 bg-violet-100 rounded-lg flex items-center justify-center text-violet-600">📝</span>
                모의고사
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 overflow-hidden p-1">
                <!-- IM -->
                <div @click="startExam('IM')" class="group relative cursor-pointer">
                    <div class="absolute inset-0 bg-gradient-to-b from-white to-slate-50 rounded-2xl transform transition-transform duration-300 group-hover:scale-[1.02] group-hover:shadow-xl border border-slate-200 shadow-lg"></div>
                    <div class="relative p-6 flex flex-col items-center">
                        <div class="w-16 h-16 mb-4 bg-slate-100 rounded-2xl flex items-center justify-center text-3xl">🌱</div>
                        <h3 class="text-xl font-bold text-slate-800 mb-1">IM 모의고사</h3>
                        <p class="text-slate-500 text-sm text-center mb-4">삼성 IM 등급 대비</p>
                        <div class="flex gap-3 text-xs text-slate-400">
                            <span class="bg-slate-100 px-2 py-1 rounded">1문제</span>
                            <span class="bg-slate-100 px-2 py-1 rounded">2시간</span>
                        </div>
                    </div>
                </div>

                <!-- A -->
                <div @click="startExam('A')" class="group relative cursor-pointer">
                    <div class="absolute inset-0 bg-gradient-to-b from-blue-50 to-white rounded-2xl transform transition-transform duration-300 group-hover:scale-[1.02] group-hover:shadow-xl border border-blue-200 shadow-lg"></div>
                    <div class="relative p-6 flex flex-col items-center">
                        <div class="w-16 h-16 mb-4 bg-blue-100 rounded-2xl flex items-center justify-center text-3xl">🔷</div>
                        <h3 class="text-xl font-bold text-blue-900 mb-1">A형 모의고사</h3>
                        <p class="text-blue-600/70 text-sm text-center mb-4">1문제=A, 2문제=A+</p>
                        <div class="flex gap-3 text-xs text-blue-400">
                            <span class="bg-blue-50 px-2 py-1 rounded">2문제</span>
                            <span class="bg-blue-50 px-2 py-1 rounded">2시간</span>
                        </div>
                    </div>
                </div>

                <!-- B -->
                <div @click="startExam('B')" class="group relative cursor-pointer">
                    <div class="absolute inset-0 bg-gradient-to-b from-purple-50 to-white rounded-2xl transform transition-transform duration-300 group-hover:scale-[1.02] group-hover:shadow-xl border border-purple-200 shadow-lg"></div>
                    <div class="relative p-6 flex flex-col items-center">
                        <div class="w-16 h-16 mb-4 bg-purple-100 rounded-2xl flex items-center justify-center text-3xl">💎</div>
                        <h3 class="text-xl font-bold text-purple-900 mb-1">B형 모의고사</h3>
                        <p class="text-purple-600/70 text-sm text-center mb-4">고급 알고리즘 대비</p>
                        <div class="flex gap-3 text-xs text-purple-400">
                            <span class="bg-purple-50 px-2 py-1 rounded">1문제</span>
                            <span class="bg-purple-50 px-2 py-1 rounded">4시간</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 코딩테스트 Section -->
        <div class="mb-8">
            <h2 class="text-2xl font-bold text-slate-800 mb-6 flex items-center gap-2">
                <span class="w-8 h-8 bg-amber-100 rounded-lg flex items-center justify-center text-amber-600">💻</span>
                코딩테스트
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 overflow-hidden p-1">
                <!-- Samsung -->
                <div @click="startExam('SAMSUNG')" class="group relative cursor-pointer">
                    <div class="absolute inset-0 bg-gradient-to-b from-sky-50 to-white rounded-2xl transform transition-transform duration-300 group-hover:scale-[1.02] group-hover:shadow-xl border border-sky-200 shadow-lg"></div>
                    <div class="relative p-6 flex flex-col items-center">
                        <div class="w-16 h-16 mb-4 bg-sky-100 rounded-2xl flex items-center justify-center text-3xl">🏢</div>
                        <h3 class="text-xl font-bold text-sky-900 mb-1">삼성 코딩테스트</h3>
                        <p class="text-sky-600/70 text-sm text-center mb-4">삼성 SW 역량테스트 기출</p>
                        <div class="flex gap-3 text-xs text-sky-400">
                            <span class="bg-sky-50 px-2 py-1 rounded">3문제</span>
                            <span class="bg-sky-50 px-2 py-1 rounded">2시간</span>
                        </div>
                    </div>
                </div>

                <!-- Kakao -->
                <div @click="startExam('KAKAO')" class="group relative cursor-pointer">
                    <div class="absolute inset-0 bg-gradient-to-b from-amber-50 to-yellow-50 rounded-2xl transform transition-transform duration-300 group-hover:scale-[1.02] group-hover:shadow-xl border border-amber-200 shadow-lg"></div>
                    <div class="relative p-6 flex flex-col items-center">
                        <div class="w-16 h-16 mb-4 bg-amber-100 rounded-2xl flex items-center justify-center text-3xl">🟡</div>
                        <h3 class="text-xl font-bold text-amber-900 mb-1">카카오 코딩테스트</h3>
                        <p class="text-amber-700/70 text-sm text-center mb-4">카카오 코딩테스트 대비</p>
                        <div class="flex gap-3 text-xs text-amber-500">
                            <span class="bg-amber-50 px-2 py-1 rounded">3문제</span>
                            <span class="bg-amber-50 px-2 py-1 rounded">2시간</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <button @click="$router.push('/')" class="mt-8 text-slate-400 hover:text-slate-900 transition-colors flex items-center justify-center w-full gap-2">
            <span>⬅ 대시보드로 돌아가기</span>
        </button>
      </div>

    </div>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm animate-fade-in">
        <div class="bg-white border border-slate-200 p-10 rounded-3xl max-w-md text-center shadow-2xl animate-bounce-in">
            <div class="text-6xl mb-6 animate-pulse">🎉</div>
            <h2 class="text-4xl font-black text-slate-900 mb-2">시험 완료!</h2>
            <p class="text-slate-500 mb-8 break-keep">모든 문제를 성공적으로 해결했습니다!</p>
            <div class="flex justify-center gap-4">
                <button @click="closeSuccessModal" class="px-6 py-3 bg-violet-600 hover:bg-violet-500 text-white rounded-xl font-bold shadow-lg shadow-violet-500/30 transition-all">
                    확인
                </button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const loading = ref(true);
const status = ref({
    examType: null,
    displayName: null,
    category: null,
    problems: [],
    startTime: null,
    timeLimitHours: 0,
    solvedCount: 0,
    totalCount: 0
});
const showSuccessModal = ref(false);
const timerInterval = ref(null);
const now = ref(new Date());
const solvedProblems = ref(new Set());

const formattedTimeLeft = computed(() => {
    if (!status.value.startTime) return "00:00:00";
    
    const startTime = new Date(status.value.startTime);
    const endTime = new Date(startTime.getTime() + status.value.timeLimitHours * 60 * 60 * 1000);
    const diff = endTime - now.value;

    if (diff <= 0) return "00:00:00";

    const h = Math.floor(diff / 1000 / 60 / 60);
    const m = Math.floor((diff / 1000 / 60) % 60);
    const s = Math.floor((diff / 1000) % 60);

    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
});

const isProblemSolved = (problemId) => {
    return solvedProblems.value.has(problemId);
};

onMounted(async () => {
    await fetchStatus();
    timerInterval.value = setInterval(() => {
        now.value = new Date();
    }, 1000);
});

onUnmounted(() => {
    if (timerInterval.value) clearInterval(timerInterval.value);
});

const fetchStatus = async () => {
    loading.value = true;
    try {
        const res = await axios.get('/api/mockexam/status');
        status.value = res.data;
        
        // Use solvedProblems from API response
        solvedProblems.value = new Set(res.data.solvedProblems || []);
    } catch (e) {
        console.error("시험 상태 fetch 실패", e);
    } finally {
        loading.value = false;
    }
};

const refreshStatus = async () => {
    try {
        const prevSolvedCount = status.value.solvedCount;
        const wasActive = status.value.examType !== null;
        
        const res = await axios.get('/api/mockexam/status');
        const newSolvedCount = res.data.solvedCount;
        const isNowActive = res.data.examType !== null;
        
        // 시험이 방금 완료되었는지 확인
        if (wasActive && !isNowActive && newSolvedCount > prevSolvedCount) {
             showSuccessModal.value = true;
        }
        
        status.value = res.data;
        solvedProblems.value = new Set(res.data.solvedProblems || []);
    } catch (e) {
        console.error("상태 refresh 실패", e);
    }
};

const startExam = async (examType) => {
    const examLabels = {
        'IM': 'IM 모의고사',
        'A': 'A형 모의고사',
        'B': 'B형 모의고사',
        'SAMSUNG': '삼성 코딩테스트',
        'KAKAO': '카카오 코딩테스트'
    };
    
    if (!confirm(`${examLabels[examType]}를 시작하시겠습니까?`)) return;
    
    try {
        await axios.post('/api/mockexam/start', { examType });
        await fetchStatus();
    } catch (e) {
        alert("시작 실패: " + (e.response?.data?.message || e.message));
    }
};

const cancelExam = async () => {
    if (!confirm("시험을 취소하시겠습니까? 진행 상황이 초기화됩니다.")) return;
    
    try {
        await axios.post('/api/mockexam/cancel');
        await fetchStatus();
    } catch (e) {
        console.error("시험 취소 실패", e);
    }
};

const closeSuccessModal = () => {
    showSuccessModal.value = false;
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

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
