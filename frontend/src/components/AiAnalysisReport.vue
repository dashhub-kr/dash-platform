<template>
  <div class="space-y-4">
    <!-- Analysis Summary Banner -->
    <div class="relative overflow-hidden rounded-2xl bg-brand-900 shadow-lg text-white p-6">
      <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
      <div class="absolute top-0 right-0 w-64 h-64 bg-brand-500 rounded-full blur-3xl opacity-20 -translate-y-1/2 translate-x-1/2"></div>
      
      <div class="relative z-10">
        <p class="text-base text-brand-50 leading-relaxed font-medium">
          {{ aiAnalysis?.analysisSummary || '분석 데이터를 불러오는 중...' }}
        </p>
      </div>
    </div>

    <!-- Weekly Goal & Quick Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <!-- Weekly Goal -->
      <div class="md:col-span-2 bg-gradient-to-r from-brand-500 to-purple-500 rounded-xl p-5 text-white shadow-md">
        <div class="text-xs font-bold text-white/70 uppercase tracking-wider mb-2">이번 주 목표</div>
        <p class="text-lg font-bold leading-snug">
          {{ aiAnalysis?.weeklyGoal || '목표 생성 중...' }}
        </p>
        <p v-if="aiAnalysis?.difficultySuggestion" class="text-white/70 text-sm mt-2">
          권장 난이도: {{ aiAnalysis.difficultySuggestion }}
        </p>
      </div>
      
      <!-- Estimated Days -->
      <div class="bg-white border border-slate-200 rounded-xl p-4 flex flex-col justify-center items-center shadow-sm">
        <div class="text-4xl font-black text-brand-600">
          {{ aiAnalysis?.estimatedDaysToGoal || '-' }}
        </div>
        <div class="text-xs font-semibold text-slate-500 uppercase">Days to Goal</div>
      </div>
    </div>

    <!-- Recommended Tags -->
    <div v-if="aiAnalysis?.recommendedTags?.length" class="bg-slate-50 border border-slate-200 rounded-xl p-4">
      <div class="text-xs font-bold text-slate-500 uppercase mb-3">집중 추천 태그</div>
      <div class="flex flex-wrap gap-2">
        <span 
          v-for="(tag, idx) in aiAnalysis.recommendedTags" 
          :key="idx"
          class="px-3 py-1.5 bg-brand-100 text-brand-700 text-sm font-semibold rounded-full"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- 2-Column Insights (헤더 최소화) -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- Growth + Strategic combined feel -->
      <div class="bg-white border border-slate-200 rounded-xl p-5 space-y-3">
        <div class="flex items-center gap-2 text-violet-600">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
          </svg>
          <span class="font-bold text-sm">성장 예측</span>
        </div>
        <p class="text-slate-600 text-sm leading-relaxed">
          {{ aiAnalysis?.growthPrediction || '데이터 분석 중...' }}
        </p>
        <p v-if="aiAnalysis?.strategicAdvice" class="text-brand-600 text-sm leading-relaxed border-t border-slate-100 pt-3">
          💡 {{ aiAnalysis.strategicAdvice }}
        </p>
      </div>

      <!-- Motivation (헤더 없이 자연스럽게) -->
      <div class="bg-gradient-to-br from-emerald-50 to-teal-50 border border-emerald-200 rounded-xl p-5 flex items-center">
        <p class="text-emerald-700 text-sm leading-relaxed font-medium">
          {{ aiAnalysis?.motivationMessage || aiAnalysis?.efficiencyAnalysis || '꾸준히 노력하면 목표에 도달할 수 있어요!' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  aiAnalysis: {
    type: Object,
    default: () => ({})
  }
});
</script>
