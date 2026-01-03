<template>
  <div class="min-h-screen bg-slate-900 text-white p-6 flex items-center justify-center relative overflow-hidden">
    
    <!-- Dark Mode Background -->
    <div class="absolute inset-0 bg-slate-900">
        <div class="absolute top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-slate-800 via-slate-900 to-slate-900 opacity-50"></div>
    </div>
    
    <div class="max-w-4xl w-full relative z-10 animate-fade-in text-center">
      
      <div class="mb-12">
        <h1 class="text-4xl md:text-5xl font-black mb-6 leading-tight">
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-brand-400 to-cyan-400">연결이 완료되었습니다!</span><br>
          이제 알고리즘에만 집중하세요.
        </h1>
        <p class="text-slate-400 text-lg">
          나머지는 DashHub가 알아서 기록하고 분석해드립니다.
        </p>
      </div>

      <!-- Conceptual Flow Diagram -->
      <div class="flex flex-col md:flex-row items-center justify-center gap-8 mb-16 relative">
         <!-- Connector Line (Desktop) -->
         <div class="hidden md:block absolute top-1/2 left-0 w-full h-1 bg-gradient-to-r from-slate-800 via-brand-500/50 to-slate-800 -z-10 transform -translate-y-1/2"></div>

         <!-- Node 1: BOJ -->
         <div class="relative group">
            <div class="w-24 h-24 bg-slate-800 rounded-3xl border border-slate-700 flex items-center justify-center shadow-2xl relative z-10 group-hover:-translate-y-2 transition-transform duration-300">
               <span class="text-4xl">💻</span>
            </div>
            <div class="mt-4 font-bold text-slate-300">문제 풀이</div>
            <p class="text-xs text-slate-500 mt-1">백준 / SWEA</p>
         </div>

         <!-- Arrow -->
         <ArrowRight class="w-8 h-8 text-brand-500 animate-pulse hidden md:block" />
         <ArrowDown class="w-8 h-8 text-brand-500 animate-pulse md:hidden" />

         <!-- Node 2: GitHub (Auto) -->
         <div class="relative group">
            <div class="absolute -inset-1 bg-gradient-to-r from-brand-500 to-cyan-500 rounded-3xl blur opacity-30 group-hover:opacity-60 transition-opacity"></div>
            <div class="w-24 h-24 bg-slate-900 rounded-3xl border border-slate-700 flex items-center justify-center shadow-2xl relative z-10 group-hover:-translate-y-2 transition-transform duration-300">
               <Github class="w-10 h-10 text-white" />
            </div>
            <div class="mt-4 font-bold text-transparent bg-clip-text bg-gradient-to-r from-brand-400 to-cyan-400">자동 커밋</div>
            <p class="text-xs text-slate-500 mt-1">Extension</p>
         </div>

         <!-- Arrow -->
         <ArrowRight class="w-8 h-8 text-cyan-500 animate-pulse delay-150 hidden md:block" />
         <ArrowDown class="w-8 h-8 text-cyan-500 animate-pulse delay-150 md:hidden" />

         <!-- Node 3: DashHub -->
         <div class="relative group">
            <div class="w-24 h-24 bg-slate-800 rounded-3xl border border-slate-700 flex items-center justify-center shadow-2xl relative z-10 group-hover:-translate-y-2 transition-transform duration-300">
               <BarChart3 class="w-10 h-10 text-brand-400" />
            </div>
            <div class="mt-4 font-bold text-slate-300">자동 분석</div>
            <p class="text-xs text-slate-500 mt-1">DashHub</p>
         </div>
      </div>

      <button 
        @click="emit('finish')"
        class="px-10 py-4 bg-white text-slate-900 rounded-2xl font-black text-xl hover:scale-105 hover:shadow-[0_0_30px_rgba(255,255,255,0.3)] transition-all duration-300"
      >
        대시보드로 입장하기
      </button>

    </div>
  </div>
</template>

<script setup>
import { ArrowRight, ArrowDown, Github, BarChart3 } from 'lucide-vue-next';
const emit = defineEmits(['finish']);
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-fade-in {
  animation: fadeIn 1s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
