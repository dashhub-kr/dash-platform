<template>
  <div class="space-y-4">
    <!-- Analysis Summary Card -->
    <div class="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm">
      <div class="flex items-start gap-4">
        <div class="w-10 h-10 bg-brand-50 rounded-xl flex items-center justify-center shrink-0">
          <Sparkles class="w-5 h-5 text-brand-500" stroke-width="2.5" fill="currentColor" />
        </div>
        <p class="text-sm text-slate-600 leading-relaxed font-medium flex-1">
          {{ aiAnalysis?.analysisSummary || '분석 데이터를 불러오는 중...' }}
        </p>
      </div>
    </div>

    <!-- 2-Column Stats -->
    <div class="grid grid-cols-2 gap-4">
      <!-- Weekly Goal -->
      <div class="bg-brand-50 rounded-2xl p-5 border border-brand-100">
        <div class="text-[10px] font-black text-brand-600 uppercase tracking-wider mb-2">이번 주 목표</div>
        <p class="text-sm font-bold text-slate-800 leading-snug">
          {{ aiAnalysis?.weeklyGoal || '목표 생성 중...' }}
        </p>
        <p v-if="aiAnalysis?.difficultySuggestion" class="text-xs text-brand-500 mt-2 font-medium">
          권장: {{ aiAnalysis.difficultySuggestion }}
        </p>
      </div>
      
      <!-- Estimated Days -->
      <div class="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm flex flex-col justify-center items-center">
        <div class="text-3xl font-black text-brand-600">
          {{ aiAnalysis?.estimatedDaysToGoal || '-' }}
        </div>
        <div class="text-[10px] font-black text-slate-400 uppercase tracking-wider">Days to Goal</div>
      </div>
    </div>

    <!-- Recommended Tags -->
    <div v-if="aiAnalysis?.recommendedTags?.length" class="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm">
      <div class="text-[10px] font-black text-slate-500 uppercase tracking-wider mb-3">집중 추천 태그</div>
      <div class="flex flex-wrap gap-2">
        <span 
          v-for="(tag, idx) in aiAnalysis.recommendedTags" 
          :key="idx"
          class="px-2.5 py-1 bg-brand-50 text-brand-700 text-xs font-bold rounded-lg"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- Growth + Strategic Insight -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm">
        <div class="flex items-center gap-2 text-violet-600 mb-3">
          <TrendingUp class="w-4 h-4" stroke-width="2.5" />
          <span class="font-bold text-xs uppercase">성장 예측</span>
        </div>
        <p class="text-sm text-slate-600 leading-relaxed">
          {{ aiAnalysis?.growthPrediction || '데이터 분석 중...' }}
        </p>
        <p v-if="aiAnalysis?.strategicAdvice" class="text-xs text-brand-600 mt-3 pt-3 border-t border-slate-100 font-medium">
          💡 {{ aiAnalysis.strategicAdvice }}
        </p>
      </div>

      <!-- Motivation -->
      <div class="bg-emerald-50 rounded-2xl p-5 border border-emerald-100 flex items-center">
        <p class="text-sm text-emerald-700 leading-relaxed font-medium">
          {{ aiAnalysis?.motivationMessage || aiAnalysis?.efficiencyAnalysis || '꾸준히 노력하면 목표에 도달할 수 있어요!' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Sparkles, TrendingUp } from 'lucide-vue-next';

defineProps({
  aiAnalysis: {
    type: Object,
    default: () => ({})
  }
});
</script>
