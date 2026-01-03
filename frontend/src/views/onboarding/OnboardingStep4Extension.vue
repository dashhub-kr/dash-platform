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

        <!-- Confirmation -->
        <div v-if="installClicked" class="text-center animate-fade-in space-y-4 pt-4 border-t border-slate-100">
           <p class="text-sm text-slate-600 font-bold">
             설치와 설정을 마치셨나요?
           </p>
           <button 
             @click="emit('next')"
             class="px-8 py-3 bg-brand-500 hover:bg-brand-600 text-white font-bold rounded-xl shadow-lg shadow-brand-500/20 transition-all hover:scale-105"
           >
             네, 완료했습니다!
           </button>
           <p class="text-xs text-slate-400 mt-2">
             * 설치가 완료되면 자동으로 감지합니다.
           </p>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ArrowRight, ArrowDown, Chrome } from 'lucide-vue-next';

const emit = defineEmits(['next']);
const installClicked = ref(false);

const onInstallClick = () => {
  installClicked.value = true;
};

// Auto-detection logic (Optional UX enhancement)
// Listen for extension content script message if available
const checkExtension = (e) => {
    // If we receive a message from the extension (injected script), we know it's installed.
    // However, the extension might only inject on refresh or specific pages.
    // This is a "Nice to have".
    console.log("Extension detected!", e.detail);
    emit('next');
};

onMounted(() => {
    window.addEventListener('baekjoonhub-dash-ready', checkExtension);
    // Poll or dispatch event to ask extension "Are you there?"
    // The content script should listen to this and reply.
    const interval = setInterval(() => {
        window.dispatchEvent(new CustomEvent('baekjoonhub-dash-request'));
    }, 1000);
    
    onUnmounted(() => {
        clearInterval(interval);
        window.removeEventListener('baekjoonhub-dash-ready', checkExtension);
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
