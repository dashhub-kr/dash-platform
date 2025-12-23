<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 relative overflow-hidden font-[Pretendard]">
    
    <!-- Background Decor -->
    <div class="absolute top-[-10%] left-[-5%] w-[40vw] h-[40vw] bg-purple-200/40 rounded-full blur-[100px] animate-blob mix-blend-multiply"></div>
    <div class="absolute bottom-[-10%] right-[-5%] w-[40vw] h-[40vw] bg-indigo-200/40 rounded-full blur-[100px] animate-blob animation-delay-2000 mix-blend-multiply"></div>

    <!-- Loading State -->
    <div v-if="loading" class="min-h-screen flex items-center justify-center relative z-10">
      <div class="text-center space-y-6">
        <div class="relative w-20 h-20 mx-auto">
          <div class="absolute inset-0 border-4 border-indigo-200 rounded-full animate-ping"></div>
          <div class="absolute inset-0 border-4 border-indigo-500 rounded-full animate-spin"></div>
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
    <div v-else class="max-w-6xl mx-auto py-12 space-y-12 relative z-10 animate-fade-in-up">
      
      <!-- 1. 현재 실력 분석 (재사용 컴포넌트) -->
      <section class="space-y-6">
        <h1 class="text-3xl font-extrabold text-slate-800 tracking-tight pl-2 border-l-4 border-indigo-500">현재 실력 분석</h1>
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
        <h1 class="text-3xl font-extrabold text-slate-800 tracking-tight pl-2 border-l-4 border-indigo-500">AI 종합 분석 리포트</h1>
        
        <div class="space-y-6">
          <!-- Top: Analysis Summary (Main Assessment) -->
          <div class="relative overflow-hidden rounded-[2rem] bg-indigo-900 shadow-xl shadow-indigo-900/10 text-white p-8 md:p-10">
             <!-- Background Effects -->
             <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20 brightness-100 contrast-150"></div>
             <div class="absolute top-0 right-0 w-96 h-96 bg-indigo-500 rounded-full blur-3xl opacity-20 -translate-y-1/2 translate-x-1/2"></div>
             
             <div class="relative z-10 flex flex-col md:flex-row gap-6 items-start">
               <div class="hidden md:flex p-3 bg-indigo-500/30 rounded-2xl backdrop-blur-md border border-indigo-400/30 flex-shrink-0">
                  <svg class="w-8 h-8 text-indigo-200" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
               </div>
               <div class="space-y-3 flex-1">
                 <h3 class="text-sm font-bold text-indigo-300 uppercase tracking-widest">Analysis Summary</h3>
                 <p class="text-lg md:text-xl text-indigo-50 leading-relaxed font-medium break-keep">
                   {{ analysisData?.aiAnalysis?.analysisSummary || 'AI가 회원님의 활동 이력을 분석하고 있습니다...' }}
                 </p>
               </div>
             </div>
          </div>

          <!-- Bottom: 3-Column Actionable Insights -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
             <!-- 1. Growth Prediction -->
             <div class="bg-white/60 backdrop-blur-xl border border-white/60 rounded-3xl p-6 shadow-lg shadow-indigo-900/5 hover:-translate-y-1 transition-transform duration-300 min-h-[160px] flex flex-col">
               <div class="flex items-center gap-3 mb-4">
                 <div class="w-10 h-10 rounded-full bg-violet-100 flex items-center justify-center text-violet-600">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
                 </div>
                 <h4 class="font-bold text-slate-700">성장 예측</h4>
               </div>
               <p class="text-slate-600 text-sm leading-relaxed font-medium">
                 {{ analysisData?.aiAnalysis?.growthPrediction || '문제 풀이 데이터가 충분하지 않습니다.' }}
               </p>
             </div>

             <!-- 2. Strategic Advice -->
             <div class="bg-indigo-50/60 backdrop-blur-xl border border-indigo-100 rounded-3xl p-6 shadow-lg shadow-indigo-900/5 hover:-translate-y-1 transition-transform duration-300 min-h-[160px] flex flex-col">
               <div class="flex items-center gap-3 mb-4">
                 <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                 </div>
                 <h4 class="font-bold text-indigo-900">전략적 조언</h4>
               </div>
               <p class="text-indigo-800 text-sm leading-relaxed font-medium">
                 {{ analysisData?.aiAnalysis?.strategicAdvice || '현재 단계에 맞는 학습 전략을 수립 중입니다.' }}
               </p>
             </div>

             <!-- 3. Efficiency Insight -->
             <div class="bg-white/60 backdrop-blur-xl border border-white/60 rounded-3xl p-6 shadow-lg shadow-indigo-900/5 hover:-translate-y-1 transition-transform duration-300 min-h-[160px] flex flex-col">
               <div class="flex items-center gap-3 mb-4">
                 <div class="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-600">
                   <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                 </div>
                 <h4 class="font-bold text-slate-700">효율성 분석</h4>
               </div>
               <p class="text-slate-600 text-sm leading-relaxed font-medium">
                 {{ analysisData?.aiAnalysis?.efficiencyAnalysis || '학습 효율성을 분석하기 위해 더 많은 데이터가 필요합니다.' }}
               </p>
             </div>
          </div>
        </div>
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
import AlgorithmRadarChart from '../../components/charts/AlgorithmRadarChart.vue';
import LearningRoadmap from '../../components/LearningRoadmap.vue';
import ClassBadgeGrid from '../../components/ClassBadgeGrid.vue';
import SkillAnalysisCard from '../../components/SkillAnalysisCard.vue';
// API
import { aiApi } from '../../api/ai';
import { useAuth } from '../../composables/useAuth';
import { onboardingApi } from '../../api/onboarding'; // To verify if user info needed again

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

const nextStep = () => {
  router.push('/onboarding/study');
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
