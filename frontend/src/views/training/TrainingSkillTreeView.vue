<template>
  <!-- Main Layout Wrapper matching DashboardView -->
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">
      <div class="min-h-screen bg-white pb-20">
    <!-- Main Layout Container -->
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full items-start">
        
        <!-- 왼쪽 칼럼: 메인 콘텐츠 -->
        <main class="flex-1 min-w-0 space-y-8 animate-in slide-in-from-left duration-500">
          
          <!-- Page Header (외부) -->
          <div class="flex items-center gap-3">
            <Network class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
            <h1 class="text-xl font-black text-slate-800">스킬 트리</h1>
          </div>

          <!-- 스킬 트리 컴포넌트 -->
          <SkillTreeView />
        </main>

        <!-- 오른쪽 칼럼: 사이드바 -->
        <aside class="hidden lg:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-fit">
          
          <!-- 1. 통계 열 (UserQuickStats) -->
          <UserQuickStats 
            :items="[
              { 
                image: `https://static.solved.ac/tier_small/${userTier}.svg`,
                value: userTierName,
                tooltip: 'Current Tier',
                textClass: 'text-sm'
              },
              { 
                icon: CheckCircle, 
                value: (user?.solvedCount || 0).toLocaleString(), 
                tooltip: 'Solved Problems',
                iconClass: 'text-leaf',
                fill: 'currentColor'
              }
            ]"
          />

          <!-- 2. 범례 (간결 & 아름다움) -->
          <div class="bg-slate-50 rounded-2xl p-4 border border-slate-100">
            <div class="text-[10px] font-black text-slate-400 uppercase tracking-wider mb-3">레벨 가이드</div>
            
            <!-- 진행도 뱃지들 -->
            <div class="flex items-center gap-2 mb-3">
              <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-amber-100 rounded-lg">
                <div class="w-3 h-3 rounded-full bg-amber-400 shadow-inner"></div>
                <span class="text-[11px] font-bold text-amber-700">마스터</span>
              </div>
              <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-emerald-100 rounded-lg">
                <div class="w-3 h-3 rounded-full bg-leaf shadow-inner"></div>
                <span class="text-[11px] font-bold text-emerald-700">학습중</span>
              </div>
              <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-100 rounded-lg">
                <div class="w-3 h-3 rounded-full bg-slate-300 shadow-inner"></div>
                <span class="text-[11px] font-bold text-slate-500">미진행</span>
              </div>
            </div>

            <!-- 중요도 구분선 + 뱃지 -->
            <div class="flex items-center gap-2 pt-3 border-t border-slate-200">
              <span class="w-6 h-6 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-white text-[10px] font-black flex items-center justify-center shadow-sm">S</span>
              <span class="w-6 h-6 rounded-full bg-gradient-to-br from-purple-400 to-violet-500 text-white text-[10px] font-black flex items-center justify-center shadow-sm">A</span>
              <span class="w-6 h-6 rounded-full bg-gradient-to-br from-sky-400 to-blue-500 text-white text-[10px] font-black flex items-center justify-center shadow-sm">B</span>
              <span class="w-6 h-6 rounded-full bg-gradient-to-br from-teal-400 to-emerald-500 text-white text-[10px] font-black flex items-center justify-center shadow-sm">C</span>
              <span class="text-[10px] text-slate-400 ml-1">= 중요도</span>
            </div>
          </div>

          <!-- 3. Quick Stats (3단계) -->
          <div class="bg-brand-50 rounded-2xl p-6 border border-brand-100/50 shadow-sm">
            <h3 class="font-black text-brand-800 text-sm mb-4 flex items-center gap-2">
              <TrendingUp class="w-5 h-5 text-brand-500" stroke-width="2.5" fill="currentColor" /> 학습 현황
            </h3>
            <div class="grid grid-cols-3 gap-4">
              <div class="text-center">
                <div class="text-2xl font-black text-orange-500">{{ masterCount }}</div>
                <div class="text-xs font-bold text-orange-500">마스터</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-black text-emerald-600">{{ learningCount }}</div>
                <div class="text-xs font-bold text-emerald-500">학습중</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-black text-slate-400">{{ lockedCount }}</div>
                <div class="text-xs font-bold text-slate-400">미진행</div>
              </div>
            </div>
          </div>

          <!-- 4. 빠른 액션 -->
          <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
            <h3 class="font-black text-slate-700 text-sm mb-4 flex items-center gap-2">
              <Zap class="w-5 h-5 text-bee" stroke-width="2.5" fill="currentColor" /> 빠른 액션
            </h3>
            <div class="space-y-2">
              <button @click="goToRoadmap" class="w-full py-3 bg-brand-50 hover:bg-brand-100 text-brand-700 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2">
                <Map class="w-4 h-4" /> 로드맵 보기
              </button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { CheckCircle, Info, TrendingUp, Zap, Map, Sword, Network } from 'lucide-vue-next';
import SkillTreeView from '@/components/skill/SkillTreeView.vue';
import { useAuth } from '@/composables/useAuth';
import { tagApi } from '@/api/tags';
import UserQuickStats from '@/components/common/UserQuickStats.vue';

const router = useRouter();
const { user } = useAuth();

// 스킬 트리 통계 (API에서 가져옴)
const skillTreeData = ref(null);

// 사용자 티어
const TIER_NAMES = [
  "Unrated",
  "Bronze V", "Bronze IV", "Bronze III", "Bronze II", "Bronze I",
  "Silver V", "Silver IV", "Silver III", "Silver II", "Silver I",
  "Gold V", "Gold IV", "Gold III", "Gold II", "Gold I",
  "Platinum V", "Platinum IV", "Platinum III", "Platinum II", "Platinum I",
  "Diamond V", "Diamond IV", "Diamond III", "Diamond II", "Diamond I",
  "Ruby V", "Ruby IV", "Ruby III", "Ruby II", "Ruby I",
  "Master"
];

const userTier = computed(() => user.value?.solvedacTier || 0);
const userTierName = computed(() => {
  const tier = userTier.value;
  return tier >= 0 && tier < TIER_NAMES.length ? TIER_NAMES[tier] : 'Unrated';
});

// 가져온 데이터로 계산된 스킬 통계
const allChildTags = computed(() => {
  if (!skillTreeData.value?.families) return [];
  return skillTreeData.value.families.flatMap(f => f.children || []);
});

// 마스터: ★4~5 (MASTER, EXPERT)
const masterCount = computed(() => 
  allChildTags.value.filter(t => ['MASTER', 'EXPERT'].includes(t.masteryLevel)).length
);

// 학습중: ★1~3 (ADVANCED, INTERMEDIATE, BEGINNER)
const learningCount = computed(() => 
  allChildTags.value.filter(t => ['ADVANCED', 'INTERMEDIATE', 'BEGINNER'].includes(t.masteryLevel)).length
);

// 미진행: ★0 (NONE or null)
const lockedCount = computed(() => 
  allChildTags.value.filter(t => !t.masteryLevel || t.masteryLevel === 'NONE').length
);

const goToRoadmap = () => {
  router.push('/training/roadmap');
};

const loadSkillTreeData = async () => {
  try {
    const userId = user.value?.id || 1;
    const res = await tagApi.getSkillTree(userId);
    skillTreeData.value = res.data;
  } catch (e) {
    console.error('Failed to load skill tree stats:', e);
  }
};

onMounted(() => {
  if (user.value) {
    loadSkillTreeData();
  }
});
</script>
