<template>
  <div class="relative w-full h-full overflow-hidden bg-slate-900 flex items-center justify-center">
    <!-- 배경 이미지 & 오버레이 -->
    <div class="absolute inset-0 z-0">
      <img src="/images/background/storage.png" alt="DashHub Background" class="w-full h-full object-cover opacity-50 scale-105 animate-slow-zoom" />
      <div class="absolute inset-0 bg-gradient-to-b from-slate-900/60 via-slate-900/80 to-slate-900"></div>
      
      <!-- 장식용 그라디언트 오브브 (Blur Blobs) -->
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-brand-500/20 rounded-full blur-[100px] animate-pulse-slow"></div>
      <div class="absolute bottom-1/4 right-1/4 w-80 h-80 bg-leaf/10 rounded-full blur-[80px] animate-pulse-slow delay-1000"></div>
    </div>

    <!-- 메인 콘텐츠 -->
    <div class="relative z-10 flex flex-col items-center justify-center p-6 text-center max-w-4xl mx-auto space-y-8">
      
      <!-- 상단 뱃지 -->
      <div class="animate-in fade-in slide-in-from-bottom-4 duration-700 delay-100">
        <span class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full border border-white/10 bg-white/5 backdrop-blur-md text-brand-200 text-sm font-bold shadow-lg shadow-brand-500/10 hover:bg-white/10 transition-colors cursor-default">
          <span class="w-2 h-2 rounded-full bg-brand-400 animate-pulse"></span>
          알고리즘 스터디 자동화 플랫폼
        </span>
      </div>

      <!-- 헤드라인 -->
      <h1 class="text-5xl md:text-7xl font-black text-white tracking-tight leading-tight animate-in fade-in slide-in-from-bottom-8 duration-700 delay-200 drop-shadow-2xl">
        개발자 성장의 모든 순간에,<br />
        <span class="text-brand-500">Dash</span><span class="text-white">Hub</span>
      </h1>

      <!-- 서브텍스트 -->
      <p class="text-lg md:text-xl text-slate-300 font-medium max-w-2xl leading-relaxed animate-in fade-in slide-in-from-bottom-8 duration-700 delay-300">
        기록, 분석, 피드백까지.<br class="block md:hidden"/>
        단 3초면 끝나는 <span class="text-white font-bold">자동화된 알고리즘 스터디</span>를 경험하세요.
      </p>

      <!-- CTA 버튼 -->
      <div class="flex flex-col sm:flex-row gap-4 pt-4 animate-in fade-in slide-in-from-bottom-8 duration-700 delay-500">
         <button @click="window.location.href = '/oauth2/authorization/github'" 
                 class="group relative px-8 py-4 bg-white text-slate-900 rounded-2xl font-black text-lg shadow-xl shadow-brand-500/20 hover:scale-105 hover:shadow-brand-500/40 transition-all duration-300 overflow-hidden">
            <span class="relative z-10 flex items-center justify-center gap-2">
               GitHub로 시작하기
               <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </span>
            <div class="absolute inset-0 bg-gradient-to-r from-brand-500/0 via-brand-500/10 to-brand-500/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-1000 ease-in-out"></div>
         </button>
         
         <button @click="emit('next')"
                 class="px-8 py-4 bg-white/5 text-white border border-white/10 rounded-2xl font-bold text-lg hover:bg-white/10 backdrop-blur-sm transition-all hover:scale-105">
            더 알아보기
         </button>
      </div>

    </div>

    <!-- 하단 스크롤 유도 -->
    <div class="absolute bottom-12 left-1/2 -translate-x-1/2 z-10 flex flex-col items-center gap-2 animate-bounce opacity-50">
       <span class="text-xs font-bold text-slate-400 tracking-widest uppercase">Scroll</span>
       <ChevronDown class="w-5 h-5 text-white" />
    </div>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ArrowRight, ChevronDown } from 'lucide-vue-next';

const router = useRouter();
const emit = defineEmits(['next']);
</script>

<style scoped>
.animate-slow-zoom {
  animation: zoom 20s infinite alternate ease-in-out;
}
.animate-pulse-slow {
   animation: pulse 8s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes zoom {
  from { transform: scale(1.05); }
  to { transform: scale(1.15); }
}
</style>
