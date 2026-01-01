<template>
  <div class="min-h-screen bg-white text-slate-800 p-6">

    <!-- Loading State -->
    <div v-if="loading" class="min-h-screen flex items-center justify-center relative z-10">
      <div class="text-center space-y-6">
        <div class="relative w-20 h-20 mx-auto">
          <div class="absolute inset-0 border-4 border-brand-200 rounded-full animate-ping"></div>
          <div class="absolute inset-0 border-4 border-brand-500 rounded-full animate-spin"></div>
          <div class="absolute inset-3 bg-white rounded-full flex items-center justify-center shadow-lg">
            <span class="text-2xl">🧠</span>
          </div>
        </div>
        <div>
          <h2 class="text-xl font-bold text-slate-800">AI 분석 중...</h2>
          <p class="text-slate-500 text-sm mt-1">알고리즘 실력을 분석하고 있습니다</p>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div v-else class="max-w-4xl mx-auto py-12 space-y-12 relative z-10 animate-fade-in-up">
      
      <!-- 1. 현재 실력 분석 (재사용 컴포넌트) -->
      <section class="space-y-6">
        <h1 class="text-3xl font-extrabold text-slate-800 tracking-tight pl-2 border-l-4 border-brand-500">현재 실력 분석</h1>
        <SkillAnalysisCard 
          :tier="userContext?.tier"
          :tier-name="userContext?.tierName"
          :chart-tags="chartTags"
          :key-strength="analysisData?.aiAnalysis?.keyStrength"
          :primary-weakness="analysisData?.aiAnalysis?.primaryWeakness"
          :phases="analysisData?.aiAnalysis?.phases || []"
        />
      </section>

      <!-- 3. Bottom Section: Assessment & Motivation -->
      <section class="space-y-8 pb-12">
        <h1 class="text-3xl font-extrabold text-slate-800 tracking-tight pl-2 border-l-4 border-brand-500">AI 종합 분석 리포트</h1>
        
        <AiAnalysisReport :ai-analysis="analysisData?.aiAnalysis" />
      </section>

      <!-- Next Button -->
      <!-- ... --> 
      <!-- (Keep existing button code but ensure it's outside sections if needed, or just append) -->
      <div class="text-center py-16">
        <button 
          @click="nextStep"
          class="group relative inline-flex items-center justify-center px-8 py-4 font-bold text-white transition-all duration-200 bg-slate-900 rounded-2xl hover:bg-slate-800 hover:scale-105 shadow-xl hover:shadow-2xl focus:outline-none ring-offset-2 focus:ring-2 ring-slate-900"
        >
          <span>나에게 맞는 스터디 찾기</span>
          <svg class="w-5 h-5 ml-2 transition-transform duration-200 group-hover:translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6"></path>
          </svg>
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
// Components
import AlgorithmRadarChart from '@/components/charts/AlgorithmRadarChart.vue';
import LearningRoadmap from '@/components/skill/LearningRoadmap.vue';
import ClassBadgeGrid from '@/components/study/ClassBadgeGrid.vue';
import SkillAnalysisCard from '@/components/skill/SkillAnalysisCard.vue';
import AiAnalysisReport from '@/components/ai/AiAnalysisReport.vue';
// API
import { aiApi } from '@/api/ai';
import { useAuth } from '@/composables/useAuth';
import { onboardingApi } from '@/api/onboarding'; // To verify if user info needed again

const router = useRouter();
const { user, refresh } = useAuth();

const loading = ref(true);
const analysisData = ref(null);
const familyStats = ref([]);
const classStats = ref([]);
const userContext = ref(null);

const chartTags = computed(() => {
  // 1. If Family Stats exist (Ideal), use them
  if (familyStats.value && familyStats.value.length > 0) {
    return familyStats.value.map(stat => ({
      tagKey: stat.familyKey?.toLowerCase(),
      solved: stat.solved || 0,
      total: stat.total || 0,
      label: stat.familyName === '다이나믹 프로그래밍' ? 'DP' : stat.familyName
    }));
  }

  // 2. Fallback: Use Tags from AI Analysis (Like TrainingView)
  if (analysisData.value) {
      const strong = analysisData.value.strengthTags || [];
      const weak = analysisData.value.weaknessTags || [];
      const merged = [...strong, ...weak];
      
      // Remove duplicates by tagKey
      const uniqueMap = new Map();
      merged.forEach(t => uniqueMap.set(t.tagKey, t));
      
      return Array.from(uniqueMap.values()).map(t => ({
          tagKey: t.tagKey,
          solved: t.solved,
          total: t.total,
          label: t.tagName // Use tagName from AI response
      }));
  }
  
  return [];
});

// Solved.ac Tier Names
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

const getTierName = (tier) => {
  if (tier >= 0 && tier < TIER_NAMES.length) {
    return TIER_NAMES[tier];
  }
  return 'Unrated';
};

onMounted(async () => {
  try {
    // 1. Force refresh user state to get latest handle (crucial after onboarding step)
    await refresh();
    
    // 2. Parallel Fetching with fresh user data
    const userId = user.value?.id;
    const handle = user.value?.solvedacHandle;
    
    // Log for debugging (optional)
    if (!handle) console.warn("No Solved.ac handle found after refresh");

    const [learningPathRes, familyRes, classRes, userRes] = await Promise.allSettled([
      userId ? aiApi.getLearningPath(userId) : Promise.resolve({ data: null }),
      userId ? aiApi.getFamilyStats(userId) : Promise.resolve({ data: [] }),
      userId ? aiApi.getClassStats(userId) : Promise.resolve({ data: [] }),
      handle ? onboardingApi.verifySolvedac(handle) : Promise.reject('No handle')
    ]);
    
    if (learningPathRes.status === 'fulfilled') {
      analysisData.value = learningPathRes.value.data;
    }
    if (familyRes.status === 'fulfilled') {
      familyStats.value = familyRes.value.data || [];
    }
    if (classRes.status === 'fulfilled') {
      classStats.value = classRes.value.data || [];
    }

    if (userRes.status === 'fulfilled') {
        userContext.value = userRes.value.data;
    } else if (user.value?.solvedacTier) {
        // Use stored tier from auth context if verify fails or wasn't needed
        userContext.value = {
            tierName: getTierName(user.value.solvedacTier),
            tier: user.value.solvedacTier
        };
    } else {
        // Fallback
        userContext.value = { 
            tierName: 'Unrated',
            tier: 0
        };
    }

  } catch (e) {
    console.error('Analysis error:', e);
  } finally {
    // Add artificial delay for smoother UX if too fast
    setTimeout(() => {
        loading.value = false;
    }, 800);
  }
});

const emit = defineEmits(['next']);

const nextStep = () => {
  emit('next');
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

.animate-blob {
  animation: blob 10s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
  to { opacity: 1; transform: translateY(0); } /* Fixed typo in keyframes? */
}
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
</style>
