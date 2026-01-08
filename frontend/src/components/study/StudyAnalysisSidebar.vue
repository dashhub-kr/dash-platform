<template>
  <div class="space-y-6 w-full">
      <!-- 1. 팀 약점 태그 -->
      <div class="bg-amber-50 rounded-2xl p-6 border border-amber-100/50 shadow-sm">
        <h3 class="font-black text-amber-800 text-sm mb-4 flex items-center gap-2">
          <AlertTriangle class="w-5 h-5 text-fox" stroke-width="2.5" fill="currentColor" /> 팀 약점 태그
        </h3>
        <div class="space-y-2">
          <div v-for="weakness in analysis.topWeaknesses" :key="weakness.tagKey" 
               class="flex items-center justify-between bg-white rounded-xl px-4 py-3 border border-amber-100">
            <span class="font-bold text-slate-700">{{ getTagDisplayName(weakness.tagKey) }}</span>
            <span class="px-2 py-0.5 bg-amber-100 text-amber-700 font-bold text-xs rounded-lg">{{ Math.round(weakness.averageRate) }}문제</span>
          </div>
          <div v-if="!analysis.topWeaknesses?.length" class="text-center text-xs text-amber-400 py-4">
            약점 데이터가 부족합니다.
          </div>
        </div>
      </div>

      <!-- 2. 멤버 정보 및 범례 -->
      <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
        <h3 class="font-black text-slate-700 text-sm mb-4 flex items-center gap-2">
            <Users class="w-5 h-5 text-slate-400" stroke-width="2" /> 멤버별 정보
        </h3>
        <div class="space-y-2">
          <div v-for="(member, idx) in analysis.memberStats.slice(0, 5)" :key="'legend-' + idx"
               class="flex items-center gap-3 p-2 hover:bg-slate-50 rounded-xl transition-colors">
            <!-- 범례 색상 점 -->
            <div class="w-3.5 h-3.5 rounded-full ring-2 ring-white shadow-sm flex-shrink-0" :style="{ backgroundColor: memberColors[idx % memberColors.length] }"></div>
            
            <!-- 아바타 (GitHub 프로필) -->
            <div class="flex-shrink-0">
                <NicknameRenderer 
                    :username="member.username"
                    :avatar-url="member.avatarUrl"
                    avatar-class="w-8 h-8 border-2 border-white bg-slate-100 object-cover shadow-sm"
                    text-class="hidden"
                    :icon-size="16"
                />
            </div>

            <NicknameRenderer 
              :nickname="member.username" 
              :decorationClass="member.decorationClass"
              :show-avatar="false"
              class="flex-1 min-w-0 text-sm"
            />
            
            <!-- 티어 뱃지 -->
            <div class="flex items-center gap-1.5 flex-shrink-0 bg-slate-50 px-2.5 py-1 rounded-lg border border-slate-100">
              <img :src="getTierIconUrl(member.tier)" class="w-4 h-4 object-contain" />
              <span class="text-xs font-bold" :class="getTierColorClass(member.tier)">
                {{ getFormattedTierName(member.tier) }}
              </span>
            </div>
          </div>
          
          <!-- 팀 평균 범례 -->
          <div class="flex items-center gap-3 mt-3 pt-3 border-t border-slate-100 p-2">
            <div class="w-3.5 h-3.5 rounded-full bg-brand-500 ring-2 ring-white shadow-sm flex-shrink-0"></div>
            <div class="w-8 h-8 rounded-full bg-brand-50 flex items-center justify-center border-2 border-white">
                 <Users class="w-4 h-4 text-brand-500" stroke-width="2.5" />
            </div>
            <span class="text-sm font-bold text-slate-700 flex-1">팀 평균</span>
            
            <div class="flex items-center gap-1.5 flex-shrink-0 bg-brand-50 px-2.5 py-1 rounded-lg border border-brand-100" v-if="analysis.averageTier">
              <img :src="getTierIconUrl(analysis.averageTier)" class="w-4 h-4 object-contain" />
              <span class="text-xs font-bold" :class="getTierColorClass(analysis.averageTier)">
                {{ getFormattedTierName(analysis.averageTier) }}
              </span>
            </div>
          </div>
        </div>
      </div>
  </div>
</template>

<script setup>
import { AlertTriangle, Users } from 'lucide-vue-next';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';

const props = defineProps({
  analysis: {
    type: Object,
    required: true
  },
  memberColors: {
    type: Array,
    required: true
  }
});

const familyDisplayNames = {
  'IMPLEMENTATION': '구현',
  'DP': 'DP',
  'GRAPH': '그래프',
  'MATH': '수학',
  'GREEDY': '그리디',
  'STRING': '문자열',
  'DATA_STRUCTURE': '자료구조',
  'ADVANCED': '고급'
};

const getTagDisplayName = (tag) => familyDisplayNames[tag?.toUpperCase()] || tag;

const getTierIconUrl = (tier) => {
  const t = Math.round(tier || 0);
  return `https://static.solved.ac/tier_small/${t}.svg`;
};

const getFormattedTierName = (tier) => {
  if (!tier) return 'Unranked';
  const t = Math.round(tier);
  if (t === 0) return 'Unranked';
  
  const levels = ['Bronze', 'Silver', 'Gold', 'Platinum', 'Diamond', 'Ruby', 'Master'];
  const index = Math.ceil(t / 5) - 1;
  const levelName = levels[index] || 'Unknown';
  
  const subLevel = 5 - ((t - 1) % 5);
  return `${levelName} ${subLevel}`;
};

const getTierColorClass = (tier) => {
  const t = Math.round(tier || 0);
  if (t === 0) return 'text-slate-400';
  if (t <= 5) return 'text-orange-700'; 
  if (t <= 10) return 'text-slate-500'; 
  if (t <= 15) return 'text-yellow-600'; 
  if (t <= 20) return 'text-emerald-500'; 
  if (t <= 25) return 'text-sky-500'; 
  if (t <= 30) return 'text-rose-500'; 
  return 'text-purple-500'; 
};
</script>
