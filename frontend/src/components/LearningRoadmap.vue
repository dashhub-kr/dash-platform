<template>
  <div class="learning-roadmap bg-white/50 backdrop-blur border border-white/60 rounded-3xl p-6 shadow-xl shadow-brand-500/5 relative overflow-hidden">
    <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wide mb-6">학습 로드맵</h3>

    <!-- Timeline -->
    <div class="relative space-y-0">
      <!-- Vertical Line -->
      <div class="absolute left-4 top-2 bottom-6 w-0.5 bg-slate-200"></div>

      <div 
        v-for="(phase, idx) in phases" 
        :key="idx" 
        class="relative flex gap-6 pb-8 last:pb-0"
      >
        <!-- Node -->
        <div class="relative z-10 flex-shrink-0">
          <div 
            class="w-8 h-8 rounded-full flex items-center justify-center border-4 transition-all duration-300"
            :class="[
              idx === 0 
                ? 'bg-brand-600 border-brand-100 shadow-[0_0_0_4px_rgba(79,70,229,0.2)] scale-110' 
                : 'bg-white border-slate-300'
            ]"
          >
            <span v-if="idx < 0" class="text-emerald-500">✓</span>
            <div v-if="idx > 0" class="w-2 h-2 rounded-full bg-slate-300"></div>
          </div>
        </div>

        <!-- Content -->
        <div class="flex-1 pt-1">
          <div class="flex flex-wrap items-center gap-2 mb-1">
            <h4 
              class="font-bold text-base transition-colors"
              :class="idx === 0 ? 'text-brand-700' : 'text-slate-600'"
            >
              {{ phase.phase || idx + 1 }}단계: {{ phase.title }}
            </h4>
            <span 
              v-if="idx === 0" 
              class="px-2 py-0.5 bg-brand-100 text-brand-700 text-[10px] font-bold uppercase rounded-full tracking-wide"
            >
              Current
            </span>
          </div>
          
          <!-- Duration & Focus Tags -->
          <div class="flex flex-wrap items-center gap-2 text-xs font-medium text-slate-400 mb-2">
            <span v-if="phase.durationDays" class="flex items-center gap-1">
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
              {{ phase.durationDays }}일
            </span>
            <span v-else-if="phase.duration">{{ phase.duration }}</span>
            
            <span v-if="phase.targetProblems" class="flex items-center gap-1">
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
              </svg>
              {{ phase.targetProblems }}문제
            </span>
            
            <span v-if="phase.difficultyRange" class="text-slate-500">
              · {{ phase.difficultyRange }}
            </span>
          </div>
          
          <!-- Focus Tags -->
          <div v-if="phase.focusTags && phase.focusTags.length" class="flex flex-wrap gap-1.5 mb-2">
            <span 
              v-for="(tag, tIdx) in phase.focusTags" 
              :key="tIdx"
              class="px-2 py-0.5 bg-brand-100 text-brand-600 text-[10px] font-semibold rounded-full"
            >
              {{ tag }}
            </span>
          </div>
          <!-- Legacy: focus string -->
          <div v-else-if="phase.focus" class="text-xs text-slate-400 mb-2">{{ phase.focus }}</div>
          
          <!-- Expandable Details (Only for active phase) -->
          <div v-if="idx === 0" class="bg-brand-50/50 rounded-xl p-3 border border-brand-100/50 mt-2 space-y-2 animate-fade-in">
            <div v-if="phase.goals && phase.goals.length" class="space-y-1">
              <div class="text-[10px] text-brand-400 font-bold uppercase">Goals</div>
              <ul class="space-y-1">
                <li v-for="(goal, gIdx) in phase.goals" :key="gIdx" class="text-sm text-slate-700 flex items-start gap-1.5">
                  <span class="text-brand-500 mt-1">•</span>{{ goal }}
                </li>
              </ul>
            </div>
            
            <div v-if="phase.milestones && phase.milestones.length" class="space-y-1">
              <div class="text-[10px] text-emerald-500 font-bold uppercase">Milestones</div>
              <ul class="space-y-1">
                <li v-for="(milestone, mIdx) in phase.milestones" :key="mIdx" class="text-sm text-slate-700 flex items-start gap-1.5">
                  <span class="text-emerald-500 mt-1">✓</span>{{ milestone }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  phases: {
    type: Array,
    default: () => []
  }
});
</script>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
