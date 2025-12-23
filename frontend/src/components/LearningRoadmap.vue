<template>
  <div class="learning-roadmap bg-white/50 backdrop-blur border border-white/60 rounded-3xl p-6 shadow-xl shadow-indigo-500/5 relative overflow-hidden">
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
                ? 'bg-indigo-600 border-indigo-100 shadow-[0_0_0_4px_rgba(79,70,229,0.2)] scale-110' 
                : 'bg-white border-slate-300'
            ]"
          >
            <span v-if="idx < 0" class="text-emerald-500">✓</span> <!-- Completed logic can be added later -->
            <div v-if="idx > 0" class="w-2 h-2 rounded-full bg-slate-300"></div>
          </div>
        </div>

        <!-- Content -->
        <div class="flex-1 pt-1">
          <div class="flex flex-wrap items-center gap-2 mb-1">
            <h4 
              class="font-bold text-base transition-colors"
              :class="idx === 0 ? 'text-indigo-700' : 'text-slate-600'"
            >
              Phase {{ idx + 1 }}: {{ phase.title }}
            </h4>
            <span 
              v-if="idx === 0" 
              class="px-2 py-0.5 bg-indigo-100 text-indigo-700 text-[10px] font-bold uppercase rounded-full tracking-wide"
            >
              Current
            </span>
          </div>
          
          <div class="text-xs font-medium text-slate-400 mb-2">{{ phase.duration }} · {{ phase.focus }}</div>
          
          <!-- Expandable Details (Only for active phase) -->
          <div v-if="idx === 0" class="bg-indigo-50/50 rounded-xl p-3 border border-indigo-100/50 mt-2 space-y-2 animate-fade-in">
            <div v-if="phase.goals && phase.goals.length" class="space-y-1">
              <div class="text-[10px] text-indigo-400 font-bold uppercase">Goals</div>
              <ul class="space-y-1">
                <li v-for="(goal, gIdx) in phase.goals" :key="gIdx" class="text-sm text-slate-700 flex items-start gap-1.5">
                  <span class="text-indigo-500 mt-1">•</span>{{ goal }}
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
