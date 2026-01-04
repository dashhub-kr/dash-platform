<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 flex items-center justify-center relative overflow-hidden">
    
    <!-- Background Decoration -->
    <div class="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] bg-brand-500/10 rounded-full blur-[120px] animate-blob"></div>
    <div class="absolute bottom-[-20%] right-[-10%] w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-[100px] animate-blob animation-delay-2000"></div>
    
    <div class="max-w-4xl w-full relative z-10 animate-fade-in text-center">
      
      <div class="mb-12">
        <h1 class="text-4xl md:text-5xl font-black mb-6 leading-tight text-slate-900">
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-brand-600 to-cyan-600">연결이 완료되었습니다!</span><br>
          이제 알고리즘에만 집중하세요.
        </h1>
        <p class="text-slate-500 text-lg font-medium">
          나머지는 DashHub가 알아서 기록하고 분석해드립니다.
        </p>
      </div>

      <!-- Conceptual Flow Diagram -->
      <div class="flex flex-col md:flex-row items-center justify-center gap-8 mb-16 relative">
         <!-- Connector Line (Desktop) -->
         <div class="hidden md:block absolute top-1/2 left-0 w-full h-1 bg-gradient-to-r from-slate-200 via-brand-500/50 to-slate-200 -z-10 transform -translate-y-1/2"></div>

         <!-- Node 1: BOJ -->
         <div class="relative group">
            <div class="w-24 h-24 bg-white rounded-3xl border border-slate-200 flex items-center justify-center shadow-xl shadow-slate-200/50 relative z-10 group-hover:-translate-y-2 transition-transform duration-300">
               <span class="text-4xl">💻</span>
            </div>
            <div class="mt-4 font-bold text-slate-700">문제 풀이</div>
            <p class="text-xs text-slate-400 mt-1">백준 / SWEA</p>
         </div>

         <!-- Arrow -->
         <ArrowRight class="w-8 h-8 text-brand-500 animate-pulse hidden md:block" />
         <ArrowDown class="w-8 h-8 text-brand-500 animate-pulse md:hidden" />

         <!-- Node 2: GitHub (Auto) -->
         <div class="relative group">
            <div class="absolute -inset-1 bg-gradient-to-r from-brand-500 to-cyan-500 rounded-3xl blur opacity-20 group-hover:opacity-40 transition-opacity"></div>
            <div class="w-24 h-24 bg-slate-900 rounded-3xl border border-slate-800 flex items-center justify-center shadow-xl relative z-10 group-hover:-translate-y-2 transition-transform duration-300">
               <Github class="w-10 h-10 text-white" />
            </div>
            <div class="mt-4 font-bold text-transparent bg-clip-text bg-gradient-to-r from-brand-600 to-cyan-600">자동 커밋</div>
            <p class="text-xs text-slate-400 mt-1">Extension</p>
         </div>

         <!-- Arrow -->
         <ArrowRight class="w-8 h-8 text-cyan-500 animate-pulse delay-150 hidden md:block" />
         <ArrowDown class="w-8 h-8 text-cyan-500 animate-pulse delay-150 md:hidden" />

         <!-- Node 3: DashHub -->
         <div class="relative group">
            <div class="w-24 h-24 bg-white rounded-3xl border border-slate-200 flex items-center justify-center shadow-xl shadow-slate-200/50 relative z-10 group-hover:-translate-y-2 transition-transform duration-300">
               <BarChart3 class="w-10 h-10 text-brand-500" />
            </div>
            <div class="mt-4 font-bold text-slate-700">자동 분석</div>
            <p class="text-xs text-slate-400 mt-1">DashHub</p>
         </div>
      </div>

      <button 
        @click="emit('finish')"
        class="px-10 py-4 bg-slate-900 text-white rounded-2xl font-bold text-xl hover:scale-105 hover:bg-slate-800 hover:shadow-xl hover:shadow-slate-900/20 transition-all duration-300"
      >
        대시보드로 입장하기
      </button>

    </div>
  </div>
</template>

<script setup>
import { ArrowRight, ArrowDown, Github, BarChart3 } from 'lucide-vue-next';
// Emits 정의
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

.animate-blob {
  animation: blob 10s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
</style>
