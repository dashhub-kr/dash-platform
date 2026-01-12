<template>
  <div class="relative w-full h-full overflow-hidden bg-slate-50 flex items-center justify-center font-['Pretendard']">
    
    <div class="absolute inset-0 z-0">
        <div class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px]"></div>
        <div class="absolute inset-0 bg-[linear-gradient(to_right,#80808020_1px,transparent_1px),linear-gradient(to_bottom,#80808020_1px,transparent_1px)] bg-[size:96px_96px]"></div>
        
        <div class="absolute inset-0 bg-gradient-to-b from-slate-50/0 via-slate-50/80 to-slate-50"></div>
    </div>

    <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute -top-[30%] -right-[10%] w-[1000px] h-[1000px] bg-indigo-200/20 rounded-full blur-[120px] mix-blend-multiply animate-pulse-slow"></div>
        <div class="absolute top-[20%] -left-[20%] w-[900px] h-[900px] bg-brand-200/20 rounded-full blur-[120px] mix-blend-multiply animate-pulse-slow delay-1000"></div>
    </div>

    <div class="relative z-10 flex flex-col items-center justify-center p-6 text-center max-w-6xl mx-auto space-y-12">
      
      <div class="animate-in fade-in slide-in-from-bottom-4 duration-700 delay-100">
        <span class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white border border-slate-200 text-slate-600 text-xs font-bold tracking-wider uppercase shadow-sm selection:bg-transparent">
          <span class="w-2 h-2 rounded-full bg-brand-500 animate-pulse"></span>
          Automated Algorithm Study Platform
        </span>
      </div>

      <h1 class="text-6xl md:text-8xl font-black text-slate-900 tracking-tighter leading-tight animate-in fade-in slide-in-from-bottom-8 duration-700 delay-200 selection:bg-brand-100">
        <span class="text-slate-900">개발자 성장</span>의 모든 순간에,<br />
        <span class="relative inline-block text-brand-600">
            DashHub
            <svg class="absolute -bottom-2 left-0 w-full h-3 text-brand-200/50 -z-10" viewBox="0 0 100 10" preserveAspectRatio="none">
                <path d="M0 5 Q 50 10 100 5" stroke="currentColor" stroke-width="8" fill="none" />
            </svg>
        </span>
      </h1>

      <p class="text-xl md:text-2xl text-slate-500 font-medium max-w-3xl leading-relaxed animate-in fade-in slide-in-from-bottom-8 duration-700 delay-300 selection:bg-brand-100">
        기록, 분석, 피드백까지.<br class="block md:hidden"/>
        단 3초면 끝나는 <span class="text-slate-900 font-bold decoration-4 decoration-brand-200 underline-offset-4">자동화된 알고리즘 스터디</span>를 경험하세요.
      </p>

      <div class="flex flex-col sm:flex-row gap-5 pt-8 animate-in fade-in slide-in-from-bottom-8 duration-700 delay-500 w-full justify-center">
         <button @click="handleLogin" 
                 class="group relative px-9 py-5 bg-slate-900 text-white rounded-2xl font-bold text-lg shadow-2xl shadow-slate-900/20 hover:shadow-slate-900/40 hover:-translate-y-1 transition-all duration-300 overflow-hidden min-w-[220px]">
            <span class="relative z-10 flex items-center justify-center gap-3">
               GitHub로 시작하기
               <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </span>
            <div class="absolute inset-0 bg-gradient-to-tr from-white/0 via-white/10 to-white/0 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
         </button>
         
         <button @click="emit('next')"
                 class="group px-9 py-5 bg-white text-slate-700 border border-slate-200 rounded-2xl font-bold text-lg shadow-lg shadow-slate-200/50 hover:bg-slate-50 hover:border-slate-300 transition-all hover:-translate-y-1 min-w-[180px]">
            <span class="flex items-center justify-center gap-2">
                더 알아보기
            </span>
         </button>
      </div>

    </div>

    <div class="absolute bottom-12 left-1/2 -translate-x-1/2 z-10 flex flex-col items-center gap-3 animate-bounce">
       <span class="text-[10px] font-extrabold text-slate-400 tracking-[0.3em] uppercase opacity-70">Scroll</span>
       <ChevronDown class="w-5 h-5 text-slate-400 opacity-70" />
    </div>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ArrowRight, ChevronDown } from 'lucide-vue-next';

const router = useRouter();
const emit = defineEmits(['next']);

const handleLogin = () => {
    window.location.href = '/oauth2/authorization/github';
};
</script>

<style scoped>
.animate-pulse-slow {
   animation: pulse 8s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.05); }
}
</style>
