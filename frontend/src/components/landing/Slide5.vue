<template>
  <div class="w-full h-full bg-slate-50 flex items-center justify-center overflow-hidden relative font-['Pretendard']">
      
      <div class="absolute inset-0 z-0 pointer-events-none">
          <div class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px]"></div>
          <div class="absolute inset-0 bg-gradient-to-t from-slate-50 via-slate-50/80 to-slate-50/20"></div>
      </div>
      
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-brand-200/20 rounded-full blur-[100px] mix-blend-multiply animate-pulse-slow"></div>
      </div>

      <div class="relative z-10 max-w-4xl mx-auto px-6 w-full text-center flex flex-col items-center justify-center min-h-[60vh] space-y-12">
          
          <div class="space-y-8 animate-in fade-in slide-in-from-bottom-8 duration-1000">
              <h2 class="text-5xl md:text-7xl font-black text-slate-900 tracking-tighter leading-tight">
                  <span class="block text-slate-400 text-2xl md:text-3xl font-bold mb-4 tracking-normal uppercase">Ready to Dive?</span>
                  성장은 <span class="text-brand-600 relative inline-block">지금부터<svg class="absolute -bottom-2 left-0 w-full h-4 text-brand-200 -z-10" viewBox="0 0 100 10" preserveAspectRatio="none"><path d="M0 5 Q 50 10 100 5" stroke="currentColor" stroke-width="8" fill="none" opacity="0.5" /></svg></span>입니다.
              </h2>
          </div>

          <div class="animate-in fade-in zoom-in duration-1000 delay-300">
              <button @click="handleLogin" 
                      class="relative group px-12 py-6 bg-slate-900 text-white rounded-full font-bold text-2xl shadow-2xl shadow-slate-900/30 hover:shadow-slate-900/50 hover:-translate-y-2 transition-all duration-300 overflow-hidden min-w-[320px]">
                <span class="relative z-10 flex items-center justify-center gap-3">
                    <Github class="w-8 h-8" />
                    GitHub로 시작하기
                </span>
                <div class="absolute inset-0 bg-gradient-to-tr from-white/0 via-white/10 to-white/0 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
              </button>
              <p class="mt-8 text-slate-500 font-medium">
                  3초 만에 시작하는 데이터 기반 성장
              </p>
          </div>

      </div>
  </div>
</template>

<script setup>
import { Github } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

const router = useRouter();

const handleLogin = () => {
    window.location.href = '/oauth2/authorization/github';
};
</script>

<style scoped>
.animate-pulse-slow {
   animation: pulse 6s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}
</style>
