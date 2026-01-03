<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 flex items-center justify-center relative overflow-hidden">
    
    <!-- Decorative -->
    <div class="absolute inset-0 bg-grid-slate-100 [mask-image:linear-gradient(0deg,white,rgba(255,255,255,0.6))] pointer-events-none"></div>

    <div class="max-w-xl w-full relative z-10 animate-fade-in-up">
      
      <div class="text-center mb-8">
        <span class="inline-block px-3 py-1 bg-brand-50 text-brand-600 rounded-full text-xs font-bold tracking-wider mb-3">STEP 05</span>
        <h1 class="text-3xl font-black text-slate-900 tracking-tight mb-2">
          저장소 확인
        </h1>
        <p class="text-slate-500 font-medium">
          익스텐션(백준허브)에 설정된 저장소를 감지합니다.
        </p>
      </div>

      <div class="bg-white/90 backdrop-blur-xl border border-white/60 rounded-3xl p-8 shadow-2xl space-y-6">
        
        <!-- State: Detecting -->
        <div v-if="detecting" class="py-8 text-center">
           <div class="relative w-16 h-16 mx-auto mb-4">
              <div class="absolute inset-0 bg-brand-100 rounded-full animate-ping opacity-75"></div>
              <div class="relative bg-brand-500 rounded-full w-16 h-16 flex items-center justify-center text-white">
                 <Search class="w-8 h-8 animate-pulse" />
              </div>
           </div>
           <h3 class="font-bold text-slate-800 text-lg">저장소 감지 중...</h3>
           <p class="text-sm text-slate-500 mt-1">익스텐션 설정을 확인하고 있습니다.</p>
        </div>

        <!-- State: Detected & Confirm -->
        <div v-else-if="detectedRepo" class="space-y-6 animate-scale-in">
           <div class="bg-emerald-50 border border-emerald-200 rounded-2xl p-6 text-center">
              <div class="w-12 h-12 bg-white rounded-full flex items-center justify-center mx-auto mb-3 text-emerald-500 shadow-sm">
                 <CheckCircle2 class="w-8 h-8" />
              </div>
              <p class="text-sm font-bold text-emerald-600 mb-1">저장소를 찾았습니다!</p>
              <h3 class="text-2xl font-black text-slate-900 break-all">
                 {{ detectedRepo.fullName }}
              </h3>
              <p class="text-xs text-slate-400 mt-2">{{ detectedRepo.description || '설명 없음' }}</p>
           </div>
           
           <div class="space-y-3">
              <button 
                @click="confirmRepo" 
                class="w-full py-4 bg-brand-600 hover:bg-brand-500 text-white font-bold rounded-2xl shadow-xl shadow-brand-500/20 hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2"
                :disabled="saving"
              >
                 <Loader2 v-if="saving" class="animate-spin" />
                 <span>네, 이 저장소가 맞습니다</span>
              </button>
              
              <button 
                @click="redetect"
                class="w-full py-3 text-brand-600 hover:text-brand-700 font-bold text-sm transition-colors flex items-center justify-center gap-2"
              >
                 <RotateCcw class="w-4 h-4" /> 설정 다시 확인하기 (재탐지)
              </button>
           </div>
        </div>

        <!-- State: Manual Search Removed / Guide -->
        <div v-else class="space-y-6 animate-fade-in text-center py-6">
           <div class="w-16 h-16 bg-amber-50 rounded-full flex items-center justify-center mx-auto mb-4 text-amber-500">
               <AlertTriangle class="w-8 h-8" />
           </div>
           <div>
               <p class="text-sm text-amber-600 font-bold mb-1">익스텐션 설정을 찾을 수 없습니다</p>
               <h3 class="text-lg font-bold text-slate-800">브라우저 우측 상단의 익스텐션을 확인해주세요</h3>
           </div>
           
           <div class="bg-slate-50 p-4 rounded-xl text-left text-sm text-slate-600 space-y-2 border border-slate-100">
               <p>1. Chrome 익스텐션 목록에서 <span class="font-bold text-slate-800">DASH Hub</span>를 클릭하세요.</p>
               <p>2. <span class="font-bold text-slate-800">Authenticate</span> 버튼을 눌러 인증을 완료하세요.</p>
               <p>3. <span class="font-bold text-slate-800">Repository</span>를 선택/연결하세요.</p>
               <p>4. 아래 버튼을 눌러 다시 탐지하세요.</p>
           </div>
           
           <button 
             @click="redetect"
             class="w-full py-3 bg-slate-900 text-white font-bold rounded-xl shadow-lg hover:bg-slate-800 transition-all flex items-center justify-center gap-2"
           >
              <RotateCcw class="w-4 h-4" /> 설정 다시 탐지하기
           </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { onboardingApi } from '@/api/onboarding';
import { Search, Loader2, CheckCircle2, RotateCcw, AlertTriangle } from 'lucide-vue-next';

const emit = defineEmits(['finish']);

const detecting = ref(true);
const detectedRepo = ref(null);
const saving = ref(false);

let debounceTimer = null;
let pollInterval = null;

// 엄격한 감지 로직 (Strict Detection Logic)
const detectRepository = async () => {
    detecting.value = true;
    
    // 1. DOM 확인 (Strict: data-hook 우선)
    // 익스텐션이 주입하는 데이터:
    // data-repo: "repo-name" (단순 이름)
    // data-hook: "user/repo-name" (실제 훅 경로 - 설정 완료 증거)
    const dataEl = document.getElementById('DashHub-dash-data');
    if (dataEl) {
       const hook = dataEl.getAttribute('data-hook');
       const repo = dataEl.getAttribute('data-repo');
       
       if (hook) {
           onRepoDetected(hook, 'Extension Hook Detected');
           return;
       } else if (repo) {
           // 리포지토리 이름으로 폴백 (덜 엄격하지만, 의미는 있음)
           // 단순 이름인 경우 사용자에게 좀 더 신중한 확인을 요구할 수도 있음
           onRepoDetected(repo, 'Extension Settings Detected');
           return;
       }
    }
    
    // 2. 요청 발송 (Dispatch Request)
    window.dispatchEvent(new CustomEvent('DashHub-dash-request'));
};

const onRepoDetected = (repoName, desc) => {
    // "user/repo" 형태의 훅 문자열이거나 전체 URL인 경우 파싱
    // "repo" 단순 이름인 경우 나중에 백엔드 검증에 의존
    let fullName = repoName;
    if (fullName.includes('github.com/')) {
        fullName = fullName.split('github.com/')[1];
    }
    
    detectedRepo.value = {
        fullName: fullName,
        description: desc
    };
    detecting.value = false;
    
    if (pollInterval) clearInterval(pollInterval);
};

const redetect = () => {
    detectedRepo.value = null;
    detecting.value = true;
    
    // 재시도 시 DOM 데이터 초기화 시도 (필요한 경우)
    const dataEl = document.getElementById('DashHub-dash-data');
    if (dataEl) {
        dataEl.removeAttribute('data-hook');
        dataEl.removeAttribute('data-repo');
    }

    setTimeout(() => {
        detectRepository();
        // 다시 폴링 시작
        startPolling();
    }, 500);
};

const startPolling = () => {
    if (pollInterval) clearInterval(pollInterval);
    let attempt = 0;
    pollInterval = setInterval(() => {
        attempt++;
        if (attempt > 10) { 
             clearInterval(pollInterval);
             if (detecting.value) {
                 detecting.value = false; // 타임아웃
             }
        }
        detectRepository();
    }, 1000);
};

const confirmRepo = async () => {
    const repo = detectedRepo.value;
    if (!repo) return;
    
    saving.value = true;
    try {
        await onboardingApi.submitRepository(repo.fullName);
        // 성공
        emit('finish', repo.fullName);
    } catch (e) {
        alert('저장소 연결 실패. 다시 시도해주세요.');
        saving.value = false;
    }
};

onMounted(() => {
    // 실제 익스텐션 데이터 수신
    window.addEventListener('DashHub-dash-ready', (e) => {
        // 우선순위: hook > repo
        const hook = e.detail?.hook;
        const repo = e.detail?.repo;
        
        if (detecting.value) {
            if (hook) {
                 onRepoDetected(hook, 'Extension Hook Verified');
            } else if (repo) {
                 onRepoDetected(repo, 'Extension Repo Detected');
            }
        }
    });

    detectRepository();
    
    startPolling();
});


onUnmounted(() => {
    if (pollInterval) clearInterval(pollInterval);
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}
@keyframes fadeInUp { to { opacity: 1; transform: translateY(0); } }

.animate-scale-in {
  animation: scaleIn 0.3s ease-out forwards;
}
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
</style>
