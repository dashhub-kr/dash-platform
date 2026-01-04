<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 flex items-center justify-center relative overflow-hidden">
    
    <!-- Decorative background -->
    <div class="absolute top-0 right-0 w-[500px] h-[500px] bg-sky-100 rounded-full blur-[100px] animate-blob mix-blend-multiply opacity-70"></div>
    <div class="absolute bottom-0 left-0 w-[500px] h-[500px] bg-brand-100 rounded-full blur-[100px] animate-blob animation-delay-2000 mix-blend-multiply opacity-70"></div>

    <div class="max-w-2xl w-full relative z-10 animate-fade-in-up">
      
      <div class="text-center mb-8">
        <span class="inline-block px-3 py-1 bg-brand-50 text-brand-600 rounded-full text-xs font-bold tracking-wider mb-3">STEP 04</span>
        <h1 class="text-3xl md:text-4xl font-black text-slate-900 tracking-tight mb-3">
          자동 기록을 시작해볼까요?
        </h1>
        <p class="text-slate-500 text-lg font-medium">
          문제를 풀면 자동으로 커밋해주는 <strong>DashHub Extension</strong>이 필요합니다.
        </p>
      </div>

      <div class="bg-white/80 backdrop-blur-xl border border-white rounded-3xl p-8 shadow-2xl relative overflow-hidden">
        
        <!-- Main Checklist Area -->
        <div class="relative z-10 space-y-6">

           <!-- 3-Step Checklist -->
           <div class="space-y-3 text-left w-full bg-slate-50 p-6 rounded-2xl border border-slate-100">
               <!-- Step 1: Install -->
               <div class="flex flex-col gap-2">
                   <div class="flex items-center justify-between">
                       <div class="flex items-center gap-3">
                           <div :class="isInstalled ? 'bg-emerald-500 text-white' : 'bg-slate-200 text-slate-400'" class="w-8 h-8 rounded-full flex items-center justify-center transition-colors shrink-0">
                               <CheckCircle2 v-if="isInstalled" class="w-5 h-5" />
                               <span v-else class="text-sm font-bold">1</span>
                           </div>
                           <div>
                               <span :class="isInstalled ? 'text-slate-800 font-bold' : 'text-slate-500'" class="text-base">익스텐션 설치</span>
                               <p v-if="!isInstalled" class="text-xs text-slate-400 mt-0.5">Chrome Web Store에서 설치해주세요</p>
                           </div>
                       </div>
                       
                       <!-- Install Button -->
                       <div v-if="!isInstalled" class="flex items-center gap-2">
                           <button 
                             @click="reloadPage"
                             class="px-3 py-1.5 bg-white border border-slate-200 text-slate-600 text-[10px] font-bold rounded-lg hover:bg-slate-50 transition-colors flex items-center gap-1"
                           >
                             <RotateCcw class="w-3 h-3" /> 새로고침
                           </button>
                           <a 
                              href="https://chromewebstore.google.com/detail/kimjgflahdmnlhilmojcoaechlgkokhc?utm_source=item-share-cb" 
                              target="_blank"
                              class="px-3 py-1.5 bg-slate-900 text-white text-[10px] font-bold rounded-lg hover:bg-slate-700 transition-colors flex items-center gap-1"
                           >
                             설치하기
                           </a>
                       </div>
                   </div>
                   <!-- Stylish Hint -->
                   <div v-if="!isInstalled" class="ml-11 bg-slate-100 rounded-lg p-3 text-[11px] text-slate-500 leading-relaxed flex items-start gap-2">
                      <Info class="w-3.5 h-3.5 mt-0.5 shrink-0 text-slate-400" />
                      <span>설치가 완료되면 <strong>새로고침</strong> 버튼을 눌러주세요.<br>자동으로 설치가 감지됩니다.</span>
                   </div>
               </div>

               <!-- Step 2: Login -->
               <div class="flex flex-col gap-2 pt-2">
                   <div class="flex items-center justify-between">
                       <div class="flex items-center gap-3">
                           <div :class="isLoggedIn ? 'bg-emerald-500 text-white' : 'bg-slate-200 text-slate-400'" class="w-8 h-8 rounded-full flex items-center justify-center transition-colors shrink-0">
                               <CheckCircle2 v-if="isLoggedIn" class="w-5 h-5" />
                               <span v-else class="text-sm font-bold">2</span>
                           </div>
                           <div>
                               <span :class="isLoggedIn ? 'text-slate-800 font-bold' : 'text-slate-500'" class="text-base">GitHub 인증</span>
                               <p v-if="isInstalled && !isLoggedIn" class="text-xs text-amber-600 font-bold mt-0.5 animate-pulse">
                                   로그인이 필요합니다
                               </p>
                           </div>
                       </div>
                       <!-- Open Extension Button -->
                       <button 
                         v-if="isInstalled && !isLoggedIn"
                         @click="openExtensionHome"
                         class="px-3 py-1.5 bg-slate-900 text-white text-[10px] font-bold rounded-lg hover:bg-slate-700 transition-colors"
                       >
                         인증하기
                       </button>
                   </div>
               </div>

               <!-- Step 3: Link -->
               <div class="flex flex-col gap-2 pt-2">
                   <div class="flex items-center justify-between">
                       <div class="flex items-center gap-3">
                           <div :class="isHookLinked ? 'bg-emerald-500 text-white' : 'bg-slate-200 text-slate-400'" class="w-8 h-8 rounded-full flex items-center justify-center transition-colors shrink-0">
                               <CheckCircle2 v-if="isHookLinked" class="w-5 h-5" />
                               <span v-else class="text-sm font-bold">3</span>
                           </div>
                           <div>
                               <span :class="isHookLinked ? 'text-slate-800 font-bold' : 'text-slate-500'" class="text-base">저장소 연결</span>
                               <p v-if="isLoggedIn && !isHookLinked" class="text-xs text-amber-600 font-bold mt-0.5 animate-pulse">
                                   저장소를 선택해주세요
                               </p>
                           </div>
                       </div>
                       <!-- Open Extension Button -->
                       <button 
                         v-if="isLoggedIn && !isHookLinked"
                         @click="openExtensionHome"
                         class="px-3 py-1.5 bg-slate-900 text-white text-[10px] font-bold rounded-lg hover:bg-slate-700 transition-colors"
                       >
                         설정하기
                       </button>
                   </div>
               </div>
           </div>

           <!-- Success Action -->
           <div v-if="isHookLinked" class="animate-scale-in pt-2">
               <button 
                 @click="emit('next')"
                 class="w-full py-3 bg-brand-600 hover:bg-brand-500 text-white font-bold rounded-xl shadow-lg shadow-brand-500/20 transition-all hover:scale-105 flex items-center justify-center gap-2"
               >
                 <span>다음 단계로</span>
                 <ArrowRight class="w-4 h-4" />
               </button>
           </div>
           
           <!-- Retry Manual : Removed as requested -->
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ArrowRight, Chrome, Loader2, CheckCircle2, RotateCcw, Info } from 'lucide-vue-next';

// Emits 정의
const emit = defineEmits(['next']);

const navigating = ref(false);
const detecting = ref(true); // Default to true to start detecting immediately

const isInstalled = ref(false);
const isLoggedIn = ref(false);
const isHookLinked = ref(false); // 저장소 연결 여부
const extensionId = ref(null);

const reloadPage = () => {
    window.location.reload();
};

const openExtensionHome = () => {
    if (extensionId.value) {
        window.open(`chrome-extension://${extensionId.value}/welcome.html`, '_blank');
    } else {
        alert('익스텐션 ID를 찾을 수 없습니다. 새로고침 후 다시 시도해주세요.');
    }
};

// 엄격한 이중 검증 로직 (Strict Dual Verification Logic)
const checkExtension = () => {
    const dataEl = document.getElementById('DashHub-dash-data');
    
    // 1. 설치 확인
    if (dataEl && dataEl.getAttribute('data-extension-installed') === 'true') {
        isInstalled.value = true;
        extensionId.value = dataEl.getAttribute('data-extension-id');
        
        // 2. 로그인(Username) 확인
        const username = dataEl.getAttribute('data-username');
        if (username && username.length > 0) {
            isLoggedIn.value = true;
            
            // 3. 저장소 연결(Hook) 확인
            const hook = dataEl.getAttribute('data-hook');
            if (hook && hook.length > 0) {
                isHookLinked.value = true;
                onAllDetected(); // 모두 완료
            } else {
                isHookLinked.value = false;
            }
        } else {
            isLoggedIn.value = false;
            isHookLinked.value = false;
        }

    } else {
        // 설치도 안됨
        isInstalled.value = false;
        isLoggedIn.value = false;
        isHookLinked.value = false;
    }
    
    if (!isInstalled.value || !isHookLinked.value) {
        // 아직 완료되지 않았으면 재요청
        window.dispatchEvent(new CustomEvent('DashHub-dash-request'));
    }
};

const onAllDetected = () => {
    isInstalled.value = true;
    isLoggedIn.value = true;
    isHookLinked.value = true;
    detecting.value = false;
};

// 익스텐션 응답 이벤트 리스너
const onExtensionReady = (e) => {
    if (e.detail && e.detail.extensionInstalled) {
        isInstalled.value = true;
        extensionId.value = e.detail.extensionId;
        
        if (e.detail.username) {
            isLoggedIn.value = true;
            
            if (e.detail.hook) {
                isHookLinked.value = true;
                onAllDetected();
            } else {
                isHookLinked.value = false;
            }
        } else {
            isLoggedIn.value = false;
            isHookLinked.value = false;
        }
    }
};

onMounted(() => {
    window.addEventListener('DashHub-dash-ready', onExtensionReady);
    
    // 주기적 확인 (Polling) - 모두 감지되면 중지
    const interval = setInterval(() => {
        if (!isInstalled.value || !isLoggedIn.value || !isHookLinked.value) {
           checkExtension();
        }
    }, 1000);
    
    onUnmounted(() => {
        clearInterval(interval);
        window.removeEventListener('DashHub-dash-ready', onExtensionReady);
    });
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-blob {
  animation: blob 10s infinite;
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
