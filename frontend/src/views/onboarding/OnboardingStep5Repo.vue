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
          대시허브 익스텐션에 설정된 저장소를 감지하고,<br>
          GitHub App을 통해 실시간 동기화를 연결합니다.
        </p>
      </div>

      <div class="bg-white/90 backdrop-blur-xl border border-white/60 rounded-3xl p-8 shadow-2xl space-y-6">
        
        <!-- State: Detecting -->
        <div v-if="detecting" class="py-8 text-center space-y-6">
           <div class="relative w-16 h-16 mx-auto">
              <div class="absolute inset-0 bg-brand-100 rounded-full animate-ping opacity-75"></div>
              <div class="relative bg-brand-500 rounded-full w-16 h-16 flex items-center justify-center text-white">
                 <Search class="w-8 h-8 animate-pulse" />
              </div>
           </div>
           <div>
              <h3 class="font-bold text-slate-800 text-lg">저장소 감지 중...</h3>
              <p class="text-sm text-slate-500 mt-1">익스텐션 설정을 확인하고 있습니다.</p>
           </div>

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

            <!-- GitHub App Installation (Mandatory Final Step) -->
            <div class="bg-blue-50 border-2 border-blue-200 rounded-2xl p-6 space-y-4 shadow-sm relative overflow-visible">
               <!-- Decorative badge -->
               <div class="absolute -top-3 -right-3 bg-blue-500 text-white text-[10px] font-black px-3 py-1 rotate-12 shadow-md z-20 rounded-sm">FINAL</div>
               
               <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-white rounded-full flex items-center justify-center text-blue-600 shadow-sm shrink-0">
                     <Info class="w-6 h-6" />
                  </div>
                  <div>
                     <h4 class="font-black text-blue-900 leading-tight">실시간 잔디 전송 활성화 (필수)</h4>
                     <p class="text-[11px] text-blue-600 font-medium">정확한 동기화를 위해 GitHub App 설치가 반드시 필요합니다.</p>
                  </div>
               </div>
               
               <div class="bg-white/60 p-4 rounded-xl space-y-2">
                  <p class="text-xs text-slate-600 leading-relaxed font-medium">
                     1. 아래 버튼을 눌러 <span class="font-bold text-slate-900">DashHub App</span>을 설치하세요.<br>
                     2. <span class="font-bold text-slate-900">'All repositories'</span> 혹은 <span class="font-bold text-slate-900">감지된 저장소</span>를 선택하세요.<br>
                     3. 설치 완료 후 아래 <span class="font-bold text-slate-900">[연동 완료]</span> 버튼을 눌러주세요.
                  </p>
               </div>

               <a 
                 :href="isAppInstalled ? '#' : `https://github.com/apps/${githubAppName}/installations/new`" 
                 target="_blank"
                 class="w-full py-3 font-black rounded-xl transition-all shadow-md flex items-center justify-center gap-2"
                 :class="isAppInstalled 
                    ? 'bg-blue-600 text-white cursor-default pointer-events-none' 
                    : 'bg-blue-600 hover:bg-blue-700 text-white hover:scale-[1.02] active:scale-[0.98]'"
               >
                  <Chrome class="w-4 h-4" />
                  <span>{{ isAppInstalled ? 'GitHub App 설치됨' : 'GitHub App 설치/권한 추가' }}</span>
               </a>
            </div>
           
           <div class="space-y-4 pt-2">
              <!-- Polling State / Result -->
              <button 
                @click="confirmRepo" 
                class="w-full py-4 text-white font-bold rounded-2xl shadow-xl transition-all flex items-center justify-center gap-2"
                 :class="isAppInstalled 
                    ? 'bg-slate-900 hover:bg-slate-800 shadow-slate-900/20 hover:-translate-y-0.5' 
                    : pollingTimedOut 
                      ? 'bg-amber-500 hover:bg-amber-400 shadow-amber-500/10' 
                      : 'bg-slate-300 cursor-not-allowed opacity-70'"
                :disabled="saving || (!isAppInstalled && !pollingTimedOut)"
              >
                 <template v-if="saving">
                   <Loader2 class="animate-spin w-5 h-5" />
                   <span>완료 처리 중...</span>
                 </template>
                  <template v-else-if="isAppInstalled">
                    <CheckCircle2 class="w-5 h-5" />
                    <span>연동이 완료되었습니다</span>
                  </template>
                 <template v-else-if="pollingTimedOut">
                   <AlertTriangle class="w-5 h-5" />
                   <span>설치를 찾지 못했습니다</span>
                 </template>
                 <template v-else>
                   <Loader2 class="animate-spin w-5 h-5" />
                   <span>App 승인을 기다리고 있습니다...</span>
                 </template>
              </button>

              <!-- Timeout Guidance -->
              <p v-if="pollingTimedOut" class="text-xs text-center text-amber-600 font-medium animate-fade-in">
                 <span class="font-bold">재탐지</span> 시도 후에도 감지되지 않는다면, <br>
                 GitHub App 설치가 정상적으로 완료되었는지 다시 확인해주세요.
              </p>
              
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
               <p>1. Chrome 익스텐션 목록에서 <span class="font-bold text-slate-800">DashHub</span>를 클릭하세요.</p>
               <p>2. <span class="font-bold text-slate-800">Authenticate</span> 버튼을 눌러 인증을 완료하세요.</p>
               <p>3. <span class="font-bold text-slate-800">Repository</span>를 선택/연결하세요.</p>
                <div class="pt-2">
                   <p class="font-bold text-brand-600 mb-1">4. GitHub App 설치 (필수)</p>
                   <a 
                     :href="`https://github.com/apps/${githubAppName}/installations/new`" 
                     target="_blank"
                     class="inline-flex items-center gap-2 text-xs font-extrabold text-white bg-slate-900 px-3 py-2 rounded-lg hover:bg-slate-700 transition-colors shadow-sm"
                   >
                     <Chrome class="w-3.5 h-3.5" /> App 설치하러 가기
                   </a>
                </div>
                <p class="pt-1 text-[10px] text-slate-400 leading-tight">설치가 완료되면 아래 '다시 탐지하기' 버튼을 눌러주세요.</p>
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
import { Search, Loader2, CheckCircle2, RotateCcw, AlertTriangle, Info, Chrome } from 'lucide-vue-next';

const emit = defineEmits(['finish']);

const githubAppName = import.meta.env.VITE_GITHUB_APP_NAME;

const detecting = ref(true);
const detectedRepo = ref(null);
const saving = ref(false);

const isAppInstalled = ref(false);
const detectingApp = ref(false);
const pollingTimedOut = ref(false);
let appPollInterval = null;

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
    
    // 리포지토리 감지 후 앱 설치 상태 폴링 시작
    startAppInstallationPolling();
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

    stopAppInstallationPolling();
    isAppInstalled.value = false;
    pollingTimedOut.value = false;
    detecting.value = true;

    setTimeout(() => {
        detectRepository();
        // 다시 폴링 시작
        startPolling();
    }, 500);
};

const stopAppInstallationPolling = () => {
    detectingApp.value = false;
    if (appPollInterval) {
        clearInterval(appPollInterval);
        appPollInterval = null;
    }
};

const startAppInstallationPolling = () => {
    stopAppInstallationPolling();
    
    detectingApp.value = true;
    let attempts = 0;
    
    // 초기 1회 즉시 실행
    checkAppStatus();

    appPollInterval = setInterval(() => {
        if (isAppInstalled.value) {
            stopAppInstallationPolling();
            return;
        }
        attempts++;
        if (attempts > 40) { // 1.5초 * 40 = 60초 폴링 후 멈춤
            stopAppInstallationPolling();
            if (!isAppInstalled.value) {
                pollingTimedOut.value = true;
            }
            return;
        }
        checkAppStatus();
    }, 1500);
};

const checkAppStatus = async () => {
    if (!detectedRepo.value?.fullName) return;
    
    try {
        const response = await onboardingApi.checkAppInstallation(detectedRepo.value.fullName);
        if (response.data === true) {
            isAppInstalled.value = true;
            detectingApp.value = false;
            if (appPollInterval) clearInterval(appPollInterval);
        }
    } catch (e) {
        // 백그라운드 폴링 실패는 조용히 무시 (네트워크 일시적 에러 등)
        console.warn('Polling app status failed', e);
    }
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
    if (!repo || !isAppInstalled.value) return;
    
    saving.value = true;
    try {
        await onboardingApi.submitRepository(repo.fullName);
        // 성공
        emit('finish', repo.fullName);
    } catch (e) {
        if (e.response?.data?.code === 'GITHUB_APP_NOT_INSTALLED') {
            alert('DashHub GitHub App 설치가 확인되지 않았습니다.\n안내에 따라 앱을 설치하고 권한을 부여한 뒤 다시 시도해주세요.');
        } else {
            alert('저장소 연결 실패. 다시 시도해주세요.');
        }
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
