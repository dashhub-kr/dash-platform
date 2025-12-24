<template>
  <div>
    <!-- 콘텐츠 (5:7 비율) -->
    <div class="">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-5">
        
        <!-- Left Column: Tier + Radar + Strength/Weakness (5 cols) -->
        <div class="lg:col-span-5 flex flex-col gap-5">
          
          <!-- Tier Card (Compact) -->
          <div class="bg-gradient-to-br from-brand-50 to-purple-50 border border-brand-100 rounded-2xl p-5 relative overflow-hidden group">
            <div class="absolute inset-0 bg-gradient-to-br from-brand-50/50 to-purple-50/50 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <div class="relative z-10 flex items-center gap-5">
              <div class="relative flex-shrink-0">
                <div class="absolute inset-0 bg-brand-500/20 blur-xl rounded-full group-hover:bg-brand-500/30 transition-all duration-500"></div>
                <img 
                  v-if="tier"
                  :src="`https://static.solved.ac/tier_small/${tier}.svg`" 
                  class="w-16 h-16 relative drop-shadow-2xl transform group-hover:scale-105 transition-transform duration-300"
                  alt="Tier Badge"
                />
              </div>
              <div class="space-y-0.5">
                <div class="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                  현재 티어
                </div>
                <div class="text-2xl font-black text-brand-900">{{ tierName || 'Unranked' }}</div>
              </div>
            </div>
          </div>

          <!-- Radar Chart Card -->
          <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5 flex-1">
            <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wide mb-4 flex items-center gap-2">
               <svg class="w-4 h-4 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 002 2h2a2 2 0 002-2z"></path></svg>
               알고리즘 역량
            </h3>
            <div class="flex items-center justify-center">
               <div class="w-full aspect-square max-w-[280px]">
                  <AlgorithmRadarChart :stats="chartTags" :max-tags="8" class="w-full h-full" />
               </div>
            </div>
          </div>

          <!-- Strength & Weakness Cards -->
          <div class="grid grid-cols-2 gap-4">
            <!-- Strength Card -->
            <div class="group bg-emerald-50 border border-emerald-200 rounded-xl p-4 hover:bg-emerald-100/50 transition-all duration-300">
              <div class="flex items-center gap-2 mb-2">
                <div class="w-7 h-7 rounded-lg bg-emerald-100 flex items-center justify-center text-emerald-600">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                </div>
                <span class="text-xs font-bold text-emerald-700 uppercase">강점</span>
              </div>
              <div class="text-slate-700 text-sm font-medium leading-relaxed">
                {{ keyStrength || '-' }}
              </div>
            </div>

            <!-- Weakness Card -->
            <div class="group bg-rose-50 border border-rose-200 rounded-xl p-4 hover:bg-rose-100/50 transition-all duration-300">
              <div class="flex items-center gap-2 mb-2">
                 <div class="w-7 h-7 rounded-lg bg-rose-100 flex items-center justify-center text-rose-500">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                 </div>
                <span class="text-xs font-bold text-rose-600 uppercase">약점</span>
              </div>
              <div class="text-slate-700 text-sm font-medium leading-relaxed">
                {{ primaryWeakness || '-' }}
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Learning Roadmap (7 cols) -->
        <div class="lg:col-span-7 bg-slate-50 border border-slate-100 rounded-2xl p-6">
          <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wide mb-5 flex items-center gap-2">
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
