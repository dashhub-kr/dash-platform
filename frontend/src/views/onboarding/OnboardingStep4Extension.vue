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
        
        <!-- Step Illustrations -->
        <div class="flex flex-col md:flex-row items-center gap-6 mb-8 relative z-10">
           
           <!-- Step A -->
           <div class="flex-1 bg-slate-50 rounded-2xl p-5 border border-slate-100 text-center group hover:bg-white hover:shadow-lg transition-all duration-300">
              <div class="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center mx-auto mb-3 text-2xl group-hover:scale-110 transition-transform">
                🧩
              </div>
              <h3 class="font-bold text-slate-800 mb-1">익스텐션 설치</h3>
              <p class="text-xs text-slate-500">Chrome Web Store에서<br>설치 버튼 클릭</p>
           </div>

           <ArrowRight class="hidden md:block text-slate-300 w-6 h-6" />
           <ArrowDown class="md:hidden text-slate-300 w-6 h-6" />

           <!-- Step B -->
           <div class="flex-1 bg-slate-50 rounded-2xl p-5 border border-slate-100 text-center group hover:bg-white hover:shadow-lg transition-all duration-300">
              <div class="w-12 h-12 bg-white rounded-xl shadow-sm flex items-center justify-center mx-auto mb-3 text-2xl group-hover:scale-110 transition-transform">
                🔑
              </div>
              <h3 class="font-bold text-slate-800 mb-1">인증 및 설정</h3>
              <p class="text-xs text-slate-500">설치된 퍼즐 조각 아이콘 클릭<br>→ 깃허브 로그인</p>
           </div>
        </div>

        <!-- CTA Button -->
        <a 
          href="https://chromewebstore.google.com/detail/kimjgflahdmnlhilmojcoaechlgkokhc?utm_source=item-share-cb" 
          target="_blank"
          @click="onInstallClick"
          class="block w-full py-4 bg-slate-900 hover:bg-slate-800 text-white font-bold text-center rounded-2xl text-lg shadow-xl shadow-slate-900/10 hover:shadow-slate-900/20 hover:-translate-y-1 transition-all mb-4"
        >
           <span class="flex items-center justify-center gap-2">
             <Chrome class="w-5 h-5" /> Chrome Web Store 방문하기
           </span>
        </a>

        <!-- Confirmation & Detection State -->
        <div v-if="installClicked" class="text-center animate-fade-in space-y-4 pt-4 border-t border-slate-100">
           
           <!-- State: Detecting or Incomplete -->
           <div v-if="detecting || (!isHookLinked)" class="py-2">
               <!-- Case 1: Installing... -->
               <div v-if="!isInstalled" class="flex flex-col items-center justify-center gap-2 text-slate-500 mb-2">
                   <div class="flex items-center gap-2">
                       <Loader2 class="w-5 h-5 animate-spin text-brand-500" />
                       <span class="font-bold text-sm">익스텐션 설치 확인 중...</span>
                   </div>
                   <p class="text-xs text-slate-400">설치가 완료되면 자동으로 감지합니다.</p>
               </div>
               
               <!-- Case 2: Installed but No Repo Linked -->
               <div v-else-if="isInstalled && !isHookLinked" class="flex flex-col items-center justify-center gap-2 text-amber-600 mb-2 animate-pulse">
                   <div class="flex items-center gap-2">
                       <Loader2 class="w-5 h-5 animate-spin text-amber-500" />
                       <span class="font-bold text-sm">저장소 연결 확인 중...</span>
                   </div>
                   <p class="text-xs text-amber-600 font-bold bg-amber-50 px-3 py-1 rounded-lg">
                     익스텐션 팝업을 열어 저장소를 연결해주세요!
                   </p>
               </div>
           </div>

           <!-- State: All Detected (Success) -->
           <div v-else-if="isInstalled && isHookLinked" class="py-2 animate-scale-in">
               <div class="flex items-center justify-center gap-2 text-emerald-600 mb-2">
                   <CheckCircle2 class="w-6 h-6" />
                   <span class="font-bold text-lg">설치 및 연결 완료!</span>
               </div>
               <button 
                 @click="emit('next')"
                 class="px-8 py-3 bg-brand-600 hover:bg-brand-500 text-white font-bold rounded-xl shadow-lg shadow-brand-500/20 transition-all hover:scale-105"
               >
                 다음 단계로
               </button>
           </div>

           <!-- Fallback / Retry -->
           <div v-else class="text-xs text-slate-400 pb-2">
               <span class="block mb-2">감지가 되지 않으시나요?</span>
               <button @click="checkExtension" class="underline hover:text-slate-600">다시 확인하기</button>
           </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ArrowRight, ArrowDown, Chrome, Loader2, CheckCircle2 } from 'lucide-vue-next';

// Emits 정의
const emit = defineEmits(['next']);

const installClicked = ref(false); // "감지 중..." UI를 표시하는 데 사용
const detecting = ref(false);
const isInstalled = ref(false);
const isHookLinked = ref(false); // 저장소 연결 여부

const onInstallClick = () => {
  installClicked.value = true;
  detecting.value = true;
  checkExtension(); // 클릭 시 즉시 확인
};

// 엄격한 이중 검증 로직 (Strict Dual Verification Logic)
const checkExtension = () => {
    const dataEl = document.getElementById('DashHub-dash-data');
    
    // 1. 설치 확인
    if (dataEl && dataEl.getAttribute('data-extension-installed') === 'true') {
        isInstalled.value = true;
        
        // 2. 저장소 연결(Hook) 확인
        const hook = dataEl.getAttribute('data-hook');
        if (hook && hook.length > 0) {
            isHookLinked.value = true;
            onAllDetected(); // 모두 완료
        } else {
            isHookLinked.value = false; // 설치는 됐으나 연결 안됨
        }
    } else {
        // 설치도 안됨
        isInstalled.value = false;
        isHookLinked.value = false;
    }
    
    if (!isInstalled.value || !isHookLinked.value) {
        // 아직 완료되지 않았으면 재요청
        window.dispatchEvent(new CustomEvent('DashHub-dash-request'));
    }
};

const onAllDetected = () => {
    isInstalled.value = true;
    isHookLinked.value = true;
    detecting.value = false;
};

// 익스텐션 응답 이벤트 리스너
const onExtensionReady = (e) => {
    if (e.detail && e.detail.extensionInstalled) {
        isInstalled.value = true;
        if (e.detail.hook) {
            isHookLinked.value = true;
            onAllDetected();
        } else {
            isHookLinked.value = false;
        }
    }
};

onMounted(() => {
    window.addEventListener('DashHub-dash-ready', onExtensionReady);
    
    // 주기적 확인 (Polling) - 모두 감지되면 중지
    const interval = setInterval(() => {
        if (!isInstalled.value || !isHookLinked.value) {
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
