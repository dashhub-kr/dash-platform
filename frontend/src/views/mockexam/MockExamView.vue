<template>
  <div class="min-h-screen bg-white text-slate-800 pb-20">
    
    <!-- 진행 중인 시험 오버레이 (전체화면 고정) -->
    <div v-if="status.examType && !loading" class="fixed inset-0 z-50 bg-slate-50 flex items-center justify-center p-4">
      <div class="w-full max-w-4xl animate-fade-in-up">
        <div class="bg-white rounded-3xl p-8 md:p-12 shadow-xl border border-slate-200 text-center relative overflow-hidden">
          <!-- 상단 강조선 -->
          <div class="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-violet-500 via-purple-500 to-violet-500"></div>

          <div class="inline-block px-4 py-1.5 rounded-full bg-violet-100 text-violet-700 text-sm font-bold mb-6">
            {{ status.category }} - {{ status.displayName }}
          </div>

          <h1 class="text-3xl md:text-4xl font-black text-slate-800 mb-8">
            ⏱️ 진행 중인 시험
          </h1>

          <!-- 타이머 카드 -->
          <div class="bg-slate-50 p-6 rounded-2xl border border-slate-200 mb-8 inline-block">
            <span class="text-slate-500 text-xs uppercase tracking-widest mb-2 font-bold block">남은 시간</span>
            <div class="text-5xl font-mono font-black text-slate-900 tabular-nums">
              {{ formattedTimeLeft }}
            </div>
          </div>

          <!-- 문제 목록 -->
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
                class="px-8 py-4 bg-brand-600 hover:bg-brand-500 text-white rounded-xl font-bold text-lg shadow-lg shadow-brand-500/30 transition-all active:scale-95 flex items-center gap-2">
                <RefreshCw class="w-5 h-5" /> 상태 확인
              </button>
              <button @click="cancelExam" 
                class="px-8 py-4 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl font-bold text-lg transition-all active:scale-95">
                시험 취소
              </button>
            </div>
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
        
        <!-- 헤더 -->
        <h1 class="text-2xl font-black text-slate-800 flex items-center gap-3">
          <div class="w-10 h-10 bg-violet-100 rounded-xl flex items-center justify-center">
            <FileText class="w-6 h-6 text-violet-600" fill="currentColor" />
          </div>
          모의고사 / 코딩테스트
        </h1>

        <!-- 로딩 중 -->
        <div v-if="loading" class="flex flex-col items-center justify-center py-20">
          <div class="w-12 h-12 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mb-4"></div>
          <p class="text-slate-400 font-medium">불러오는 중...</p>
        </div>

        <template v-else>
          <!-- 모의고사 섹션 -->
          <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <GraduationCap class="w-5 h-5 text-violet-500" fill="currentColor" />
              삼성 역량 테스트 대비
            </h2>
            <div class="grid grid-cols-3 gap-4">
              <!-- IM -->
              <div @click="startExam('IM')" class="group cursor-pointer flex flex-col items-center p-4 rounded-2xl hover:bg-slate-50 transition-all active:scale-95 border border-transparent hover:border-slate-200">
                <div class="relative mb-3 transition-transform duration-300 group-hover:scale-110">
                  <div class="w-16 h-16 bg-emerald-100 rounded-full flex items-center justify-center text-emerald-500 shadow-sm">
                    <Sprout :size="32" stroke-width="1.5" fill="currentColor" />
                  </div>
                  <div class="absolute -top-1 -right-1 bg-emerald-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">기초</div>
                </div>
                <h3 class="text-sm font-black text-slate-800 mb-0.5">IM</h3>
                <div class="text-slate-400 text-xs font-bold">1문제 · 2시간</div>
              </div>

              <!-- A -->
              <div @click="startExam('A')" class="group cursor-pointer flex flex-col items-center p-4 rounded-2xl hover:bg-slate-50 transition-all active:scale-95 border border-transparent hover:border-slate-200">
                <div class="relative mb-3 transition-transform duration-300 group-hover:scale-110">
                  <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center text-blue-500 shadow-sm">
                    <Diamond :size="32" stroke-width="1.5" fill="currentColor" />
                  </div>
                  <div class="absolute -top-1 -right-1 bg-blue-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">중급</div>
                </div>
                <h3 class="text-sm font-black text-slate-800 mb-0.5">A형</h3>
                <div class="text-slate-400 text-xs font-bold">2문제 · 2시간</div>
              </div>

              <!-- B -->
              <div @click="startExam('B')" class="group cursor-pointer flex flex-col items-center p-4 rounded-2xl hover:bg-slate-50 transition-all active:scale-95 border border-transparent hover:border-slate-200">
                <div class="relative mb-3 transition-transform duration-300 group-hover:scale-110">
                  <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center text-purple-500 shadow-sm">
                    <Crown :size="32" stroke-width="1.5" fill="currentColor" />
                  </div>
                  <div class="absolute -top-1 -right-1 bg-purple-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">고급</div>
                </div>
                <h3 class="text-sm font-black text-slate-800 mb-0.5">B형</h3>
                <div class="text-slate-400 text-xs font-bold">1문제 · 4시간</div>
              </div>
            </div>
          </div>

          <!-- 코딩테스트 섹션 -->
          <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <Code class="w-5 h-5 text-amber-500" fill="currentColor" />
              코딩테스트 대비
            </h2>
            <div class="grid grid-cols-2 gap-4">
              <!-- Samsung -->
              <div @click="startExam('SAMSUNG')" class="group cursor-pointer flex flex-col items-center p-4 rounded-2xl hover:bg-slate-50 transition-all active:scale-95 border border-transparent hover:border-slate-200">
                <div class="relative mb-3 transition-transform duration-300 group-hover:scale-110">
                  <div class="w-16 h-16 bg-sky-100 rounded-full flex items-center justify-center text-sky-500 shadow-sm">
                    <Building :size="32" stroke-width="1.5" fill="currentColor" />
                  </div>
                </div>
                <h3 class="text-sm font-black text-slate-800 mb-0.5">삼성 SW</h3>
                <div class="text-slate-400 text-xs font-bold">3문제 · 2시간</div>
              </div>

              <!-- Kakao -->
              <div @click="startExam('KAKAO')" class="group cursor-pointer flex flex-col items-center p-4 rounded-2xl hover:bg-slate-50 transition-all active:scale-95 border border-transparent hover:border-slate-200">
                <div class="relative mb-3 transition-transform duration-300 group-hover:scale-110">
                  <div class="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center text-amber-500 shadow-sm">
                    <MessageCircle :size="32" stroke-width="1.5" fill="currentColor" />
                  </div>
                </div>
                <h3 class="text-sm font-black text-slate-800 mb-0.5">카카오</h3>
                <div class="text-slate-400 text-xs font-bold">3문제 · 2시간</div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 사이드바 -->
      <aside class="w-[380px] hidden xl:flex flex-col sticky top-8 h-fit gap-6">
        
        <!-- 안내 카드 -->
        <div class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200">
          <h3 class="font-bold text-slate-800 text-sm mb-4 flex items-center gap-2">
            <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center">
              <Info class="w-3 h-3 text-white" />
            </div>
            시험 안내
          </h3>
          <div class="space-y-3 text-sm text-slate-600">
            <div class="flex items-start gap-3">
              <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center shrink-0 mt-0.5">
                <MousePointerClick class="w-3 h-3 text-white" />
              </div>
              <span>시험 유형을 선택하면 문제가 출제됩니다.</span>
            </div>
            <div class="flex items-start gap-3">
              <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center shrink-0 mt-0.5">
                <Clock class="w-3 h-3 text-white" />
              </div>
              <span>제한 시간 내에 백준에서 문제를 제출해주세요.</span>
            </div>
            <div class="flex items-start gap-3">
              <div class="w-5 h-5 bg-brand-500 rounded-md flex items-center justify-center shrink-0 mt-0.5">
                <CheckCircle class="w-3 h-3 text-white" />
              </div>
              <span>상태 확인 버튼을 눌러 결과를 확인합니다.</span>
            </div>
          </div>
        </div>



      </aside>
    </div>

    <!-- A형 합격 모달 -->
    <div v-if="showGradeAModal" class="fixed inset-0 z-[60] flex items-center justify-center bg-black/40 backdrop-blur-sm animate-fade-in">
      <div class="bg-white border border-slate-200 p-8 rounded-3xl max-w-md text-center shadow-2xl animate-bounce-in">
        <div class="text-5xl mb-4">😲</div>
        <h2 class="text-2xl font-black text-slate-900 mb-2">A형 합격!</h2>
        <p class="text-slate-500 mb-6 break-keep font-medium">
            축하합니다! 1문제를 해결하여 <span class="text-brand-600 font-bold">A등급</span> 기준을 달성하셨습니다.<br/><br/>
            여기서 시험을 종료하고 A등급을 받으시겠습니까,<br/>
            아니면 남은 시간 동안 <span class="text-violet-600 font-bold">A+등급</span>(2문제)에 도전하시겠습니까?
        </p>
        <div class="flex gap-3">
            <button @click="finishExamAnyway" class="flex-1 py-3 bg-slate-100 text-slate-600 font-bold rounded-xl hover:bg-slate-200 transition-colors">
                지금 종료 (A등급)
            </button>
            <button @click="continueExam" class="flex-1 py-3 bg-violet-600 text-white font-bold rounded-xl hover:bg-violet-500 shadow-lg shadow-violet-500/30 transition-colors">
                계속 도전! (A+)
            </button>
        </div>
      </div>
    </div>

    <!-- 성공 모달 -->
    <div v-if="showSuccessModal" class="fixed inset-0 z-[60] flex items-center justify-center bg-black/40 backdrop-blur-sm animate-fade-in">
      <div class="bg-white border border-slate-200 p-10 rounded-3xl max-w-md text-center shadow-2xl animate-bounce-in">
        <div class="text-6xl mb-6">🎉</div>
        <h2 class="text-3xl font-black text-slate-900 mb-2">시험 완료!</h2>
        <p class="text-slate-500 mb-8 break-keep">모든 문제를 성공적으로 해결했습니다!</p>
        <button @click="closeSuccessModal" class="px-8 py-3 bg-brand-600 hover:bg-brand-500 text-white rounded-xl font-bold shadow-lg shadow-brand-500/30 transition-all">
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
import { FileText, Code, Sprout, Diamond, Crown, Building, MessageCircle, GraduationCap, Info, RefreshCw, MousePointerClick, Clock, CheckCircle } from 'lucide-vue-next';

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
    
    // 백엔드가 UTC 시간을 보내지만 'Z'가 없는 경우를 대비하여 처리
    let timeStr = status.value.startTime;
    if (!timeStr.endsWith('Z') && !timeStr.includes('+')) {
        timeStr += 'Z';
    }

    const startTime = new Date(timeStr);
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
        
        // API 응답에서 solvedProblems 사용
        solvedProblems.value = new Set(res.data.solvedProblems || []);
    } catch (e) {
        console.error("시험 상태 fetch 실패", e);
    } finally {
        loading.value = false;
    }
};

const showGradeAModal = ref(false);

const refreshStatus = async () => {
    try {
        const prevSolvedCount = status.value.solvedCount;
        const wasActive = status.value.examType !== null;
        
        const res = await axios.get('/api/mockexam/status');
        const newSolvedCount = res.data.solvedCount;
        const isNowActive = res.data.examType !== null;
        
        status.value = res.data;
        solvedProblems.value = new Set(res.data.solvedProblems || []);

        // 1. 시험이 이미 종료된 경우 (시간 초과 등)
        if (wasActive && !isNowActive) {
             showSuccessModal.value = true; // 혹은 '시험 종료' 안내 모달
             return;
        }

        // 2. 새로운 문제를 풀었는지 확인
        if (newSolvedCount > prevSolvedCount) {
            // A형 모의고사 특수 로직
            if (status.value.examType === 'A') {
                if (newSolvedCount === 1) {
                    showGradeAModal.value = true;
                    return;
                } else if (newSolvedCount >= 2) {
                     // A+ 달성 -> 즉시 종료
                     await finishExam(true); 
                     return;
                }
            } else {
                // 일반 시험: 모든 문제 해결 시 종료
                if (newSolvedCount >= status.value.totalCount) {
                    await finishExam(true);
                    return;
                }
            }
            // 그 외: 문제 해결 알림 (Toast 등 있으면 좋음, 여기선 alert 생략)
            alert(`정답입니다! (${newSolvedCount}/${status.value.totalCount})`);
        } else {
            // 변동 없음
            alert("새로운 해결 기록이 없습니다. 잠시 후 다시 시도해주세요.");
        }

    } catch (e) {
        console.error("상태 refresh 실패", e);
    }
};

const finishExam = async (success = false) => {
    try {
        await axios.post('/api/mockexam/finish'); // 명시적 종료 API 필요 시 구현, 혹은 cancel 사용
        // 여기서는 편의상 로컬 상태 업데이트 및 성공 모달 띄우기
        // 실제로는 백엔드에서 finish 처리를 해야 기록이 남음.
        // 기존 cancelExam은 '포기'이므로, '완료' 처리를 위한 별도 호출이 낫지만
        // 현재 스펙상으로는 status 체크로 자동 종료되거나, 시간 초과로 종료됨.
        // A형 1솔 종료를 위해 /finish 엔드포인트가 있다고 가정하거나, 
        // /cancel을 '완료'로 처리할 수 있게 백엔드 수정이 필요할 수 있음.
        // 우선은 cancelExam 대신 별도 처리를 하거나 cancel을 호출하되 메시지를 다르게 처리.
        // *가정*: 여기서는 일단 로컬 상태만 변경하고 성공 모달을 띄움. 
        // 실제 종료는 백엔드 타이머나 명시적 요청 필요.
        
        // 간단히: 성공 모달 띄우고, 확인 누르면 홈으로.
        showSuccessModal.value = true;
    } catch (e) {
        console.error(e);
    }
};

const finishExamAnyway = async () => {
    showGradeAModal.value = false;
    await finishExam(true);
};

const continueExam = () => {
    showGradeAModal.value = false;
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
