<template>
  <div>
    <!-- 콘텐츠 (5:7 비율) -->
    <div class="">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-5">
        
        <!-- Left Column: Radar Chart Only (5 cols) -->
        <div class="lg:col-span-5 flex flex-col gap-5">
          
          <!-- Radar Chart Card -->
          <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm">
            <h3 class="text-xs font-black text-slate-500 uppercase tracking-wide mb-4 flex items-center gap-2">
               <svg class="w-4 h-4 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 002 2h2a2 2 0 002-2z"></path></svg>
               알고리즘 역량
            </h3>
            <div class="flex items-center justify-center">
               <div class="w-full aspect-square max-w-[280px]">
                  <AlgorithmRadarChart :stats="chartTags" :max-tags="8" class="w-full h-full" />
               </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Learning Roadmap (7 cols) -->
        <div class="lg:col-span-7 bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
          <h3 class="text-xs font-black text-slate-500 uppercase tracking-wide mb-5 flex items-center gap-2">
             <svg class="w-4 h-4 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path></svg>
             맞춤형 학습 로드맵
          </h3>
          <LearningRoadmap :phases="phases" class="w-full" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import AlgorithmRadarChart from '@/components/charts/AlgorithmRadarChart.vue';
import LearningRoadmap from '@/components/LearningRoadmap.vue';

defineProps({
  tier: [Number, String],
  tierName: String,
  chartTags: {
    type: Array,
    default: () => []
  },
  keyStrength: String,
  primaryWeakness: String,
  phases: {
    type: Array,
    default: () => []
  }
});
</script>
