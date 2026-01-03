<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 flex flex-col items-center justify-center p-6 relative overflow-hidden">
    
    <!-- Background Elements -->
    <div class="absolute top-0 left-1/4 w-[1000px] h-[1000px] bg-indigo-50/50 rounded-full blur-[150px] animate-pulse-slow pointer-events-none"></div>

    <!-- Loading State -->
    <transition name="fade" mode="out-in">
      <div v-if="loading" class="flex flex-col items-center justify-center space-y-8 z-10 text-center">
        <div class="relative w-32 h-32">
          <!-- Rings -->
          <div class="absolute inset-0 border-8 border-slate-100 rounded-full"></div>
          <div class="absolute inset-0 border-8 border-brand-500 rounded-full border-t-transparent animate-spin"></div>
          <!-- Icon -->
          <div class="absolute inset-0 flex items-center justify-center">
             <BrainCircuit class="w-12 h-12 text-brand-500 animate-pulse" />
          </div>
        </div>
        <div>
           <h2 class="text-3xl font-black text-slate-800 mb-2">AI 정밀 분석 중</h2>
           <p class="text-slate-500 text-lg font-medium typing-effect">
              {{ loadingMessage }}
           </p>
        </div>
      </div>

      <!-- Analysis Result -->
      <div v-else class="w-full max-w-7xl relative z-10">
        
        <!-- Header -->
        <div class="flex flex-col md:flex-row items-center justify-between mb-8 gap-4 animate-slide-down">
          <div>
            <div class="inline-flex items-center gap-2 px-3 py-1 bg-white border border-slate-200 rounded-full shadow-sm mb-3">
               <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
               <span class="text-xs font-bold text-slate-600 tracking-wide">ANALYSIS COMPLETE</span>
            </div>
            <h1 class="text-4xl font-black text-slate-900 tracking-tight">
              <span class="text-brand-600">{{ userName }}</span>님의 분석 리포트
            </h1>
          </div>
          <button 
            @click="emit('next')"
            class="group flex items-center gap-3 px-8 py-4 bg-slate-900 text-white rounded-2xl font-bold text-lg shadow-xl shadow-slate-900/10 hover:shadow-slate-900/20 hover:scale-105 transition-all duration-300"
          >
            <span>스터디 추천받기</span>
            <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
          </button>
        </div>

        <!-- Dashboard Grid -->
        <div class="grid grid-cols-1 md:grid-cols-12 gap-6 animate-fade-in-up">
          
          <!-- 1. Key Metrics (Top Left) -->
          <div class="md:col-span-4 grid grid-rows-2 gap-6">
             <!-- Estimated Time -->
             <div class="bg-white rounded-3xl p-8 border border-slate-100 shadow-sm hover:shadow-md transition-all relative overflow-hidden group">
                <div class="absolute right-[-20%] bottom-[-20%] w-48 h-48 bg-brand-50 rounded-full group-hover:scale-110 transition-transform duration-700"></div>
                <div class="relative z-10">
                   <h3 class="text-xs font-bold text-slate-400 uppercase tracking-widest mb-4">GOAL ESTIMATION</h3>
                   <div class="flex items-baseline gap-2">
                      <span class="text-6xl font-black text-slate-900 tracking-tighter">
                         {{ analysisData?.aiAnalysis?.estimatedDaysToGoal || '30' }}
                      </span>
                      <span class="text-xl font-bold text-slate-400">Days</span>
                   </div>
                   <p class="text-sm text-slate-500 mt-2 font-medium">목표 달성까지 예상 소요 시간</p>
                </div>
             </div>

             <!-- Weekly Goal -->
             <div class="bg-gradient-to-br from-indigo-600 to-indigo-700 rounded-3xl p-8 shadow-lg shadow-indigo-500/20 text-white relative overflow-hidden">
                <Target class="absolute top-6 right-6 w-8 h-8 text-white/20" />
                <h3 class="text-xs font-bold text-indigo-200 uppercase tracking-widest mb-4">WEEKLY FOCUS</h3>
                <p class="text-2xl font-bold leading-snug">
                   {{ analysisData?.aiAnalysis?.weeklyGoal || '매일 1문제 챌린지' }}
                </p>
                <div class="mt-4 inline-flex px-3 py-1 bg-white/20 backdrop-blur rounded-lg text-sm font-semibold">
                   🔥 {{ analysisData?.aiAnalysis?.difficultySuggestion || 'Gold V 이상' }}
                </div>
             </div>
          </div>

          <!-- 2. Radar Chart (Middle) -->
          <div class="md:col-span-4 bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex flex-col items-center justify-center relative">
             <h3 class="absolute top-6 left-8 text-xs font-bold text-slate-400 uppercase tracking-widest">SKILL RADAR</h3>
             <div class="w-full h-full max-h-[300px] p-4">
                <AlgorithmRadarChart :stats="chartTags" :max-tags="6" class="w-full h-full" />
             </div>
             <div class="flex flex-wrap justify-center gap-2 mt-2">
               <span v-for="tag in analysisData?.aiAnalysis?.strengthTags?.slice(0, 3)" :key="tag" class="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-1 rounded-md">
                 + {{ tag.tagName }}
               </span>
             </div>
          </div>

          <!-- 3. AI Insights (Right) -->
          <div class="md:col-span-4 bg-white rounded-3xl p-8 border border-slate-100 shadow-sm flex flex-col">
             <div class="flex items-center gap-3 mb-6">
                <div class="w-10 h-10 bg-violet-100 rounded-xl flex items-center justify-center">
                   <Sparkles class="w-5 h-5 text-violet-600" />
                </div>
                <div>
                   <h3 class="text-xs font-bold text-slate-400 uppercase tracking-widest">AI INSIGHTS</h3>
                   <div class="text-sm font-bold text-slate-800">성장 분석 요약</div>
                </div>
             </div>
             
             <div class="flex-1 space-y-6">
                <div>
                   <h4 class="text-sm font-bold text-slate-900 mb-2">💡 전략적 조언</h4>
                   <p class="text-sm text-slate-600 leading-relaxed bg-slate-50 p-4 rounded-2xl border border-slate-100">
                      {{ analysisData?.aiAnalysis?.strategicAdvice || '데이터가 부족하여 일반적인 조언을 제공합니다. 꾸준히 문제를 풀어보세요.' }}
                   </p>
                </div>
                
                <div>
                   <h4 class="text-sm font-bold text-slate-900 mb-2">💪 강점 & 약점</h4>
                   <div class="space-y-3">
                      <div class="flex items-start gap-3">
                         <div class="w-1 h-full bg-emerald-400 rounded-full opacity-50"></div>
                         <p class="text-xs text-slate-600 leading-relaxed">
                            {{ analysisData?.aiAnalysis?.growthPrediction || '성장 가능성이 매우 높습니다.' }}
                         </p>
                      </div>
                   </div>
                </div>
             </div>
          </div>

          <!-- 4. Tags Banner (Bottom) -->
          <div class="md:col-span-12 bg-white rounded-3xl p-6 border border-slate-100 shadow-sm flex items-center justify-between gap-6 overflow-hidden relative">
             <div class="absolute left-0 top-0 w-2 h-full bg-gradient-to-b from-brand-500 to-indigo-500"></div>
             
             <div class="flex-1 overflow-hidden">
                <h3 class="text-xs font-bold text-slate-400 uppercase tracking-widest mb-2 ml-2">RECOMMENDED TAGS</h3>
                <div class="flex flex-wrap gap-2">
                   <span 
                      v-for="(tag, idx) in analysisData?.aiAnalysis?.recommendedTags" 
                      :key="idx"
                      class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm font-bold text-slate-700 hover:bg-slate-100 transition-colors cursor-default"
                   >
                      #{{ tag }}
                   </span>
                   <span v-if="!analysisData?.aiAnalysis?.recommendedTags?.length" class="text-sm text-slate-400 px-2">
                      추천 태그를 분석하고 있습니다...
                   </span>
                </div>
             </div>

             <div class="hidden md:block text-right pr-4">
                <p class="text-sm font-medium text-slate-900">
                   "{{ analysisData?.aiAnalysis?.motivationMessage || '시작이 반입니다!' }}"
                </p>
                <p class="text-xs text-slate-400 mt-1">DashHub AI Coach</p>
             </div>
          </div>

        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { aiApi } from '@/api/ai';
import { useAuth } from '@/composables/useAuth';
import AlgorithmRadarChart from '@/components/charts/AlgorithmRadarChart.vue';
import { BrainCircuit, ArrowRight, Target, Sparkles } from 'lucide-vue-next';

// Props & Emits
const emit = defineEmits(['next']);

// State
const { user, refresh } = useAuth();
const loading = ref(true);
const analysisData = ref(null);
const familyStats = ref([]);
const loadingMessage = ref('데이터를 불러오는 중입니다...');
const loadingMessages = [
  '백준 데이터를 분석하고 있습니다...',
  '약점 유형을 파악하고 있습니다...',
  '맞춤형 커리큘럼을 생성하고 있습니다...',
  '거의 다 되었습니다!'
];

// Computed
const userName = computed(() => user.value?.solvedacHandle || '사용자');

const chartTags = computed(() => {
  if (familyStats.value?.length > 0) {
    return familyStats.value.map(stat => ({
      tagKey: stat.familyKey?.toLowerCase(),
      solved: stat.solved || 0,
      total: stat.total || 0,
      label: stat.familyName
    }));
  }
  
  if (analysisData.value) {
      const merged = [
          ...(analysisData.value.strengthTags || []), 
          ...(analysisData.value.weaknessTags || [])
      ];
      const unique = new Map(merged.map(t => [t.tagKey, t]));
      
      return Array.from(unique.values()).map(t => ({
          tagKey: t.tagKey,
          solved: t.solved,
          total: t.total,
          label: t.tagName
      }));
  }
  return [];
});

onMounted(async () => {
  // Loading Text Animation
  let msgIdx = 0;
  const interval = setInterval(() => {
     msgIdx = (msgIdx + 1) % loadingMessages.length;
     loadingMessage.value = loadingMessages[msgIdx];
  }, 2000);

  try {
    await refresh();
    const userId = user.value?.id;
    if (!userId) throw new Error("No User ID");

    // Fetch Data
    const [pathRes, familyRes] = await Promise.allSettled([
       aiApi.getLearningPath(userId),
       aiApi.getFamilyStats(userId)
    ]);

    if (pathRes.status === 'fulfilled') analysisData.value = pathRes.value.data;
    if (familyRes.status === 'fulfilled') familyStats.value = familyRes.value.data;

  } catch (e) {
    console.error(e);
  } finally {
    clearInterval(interval);
    // Minimum loading time for UX
    setTimeout(() => {
       loading.value = false;
    }, 1500);
  }
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-pulse-slow {
  animation: pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.animate-slide-down {
  animation: slideDown 0.8s ease-out forwards;
  opacity: 0;
  transform: translateY(-20px);
}

.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out 0.2s forwards;
  opacity: 0;
  transform: translateY(30px);
}

@keyframes slideDown {
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}

.typing-effect:after {
  content: '...';
  animation: typing 1.5s infinite;
}

@keyframes typing {
  0% { content: ''; }
  25% { content: '.'; }
  50% { content: '..'; }
  75% { content: '...'; }
}

/* Transition Classes */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
