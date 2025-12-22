<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 relative overflow-hidden font-[Pretendard]">
    
    <!-- Background Decor -->
    <div class="absolute top-[-10%] left-[-5%] w-[40vw] h-[40vw] bg-purple-200/40 rounded-full blur-[100px] animate-blob mix-blend-multiply"></div>
    <div class="absolute bottom-[-10%] right-[-5%] w-[40vw] h-[40vw] bg-indigo-200/40 rounded-full blur-[100px] animate-blob animation-delay-2000 mix-blend-multiply"></div>

    <!-- Loading State -->
    <div v-if="loading" class="min-h-screen flex items-center justify-center">
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
    <div v-else class="max-w-5xl mx-auto py-8 space-y-8 relative z-10 animate-fade-in-up">
      
      <!-- Header -->
      <header class="text-center space-y-2">
        <h1 class="text-3xl font-extrabold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
          🎯 실력 분석 리포트
        </h1>
        <p class="text-slate-500">AI가 분석한 당신만의 학습 전략</p>
      </header>

      <!-- Stats Overview Row -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="bg-white/80 backdrop-blur border border-white/60 rounded-2xl p-5 text-center shadow-sm">
          <div class="text-3xl font-extrabold text-indigo-600">{{ totalSolved }}</div>
          <div class="text-sm text-slate-500 mt-1">해결한 문제</div>
        </div>
        <div class="bg-white/80 backdrop-blur border border-white/60 rounded-2xl p-5 text-center shadow-sm">
          <div class="text-3xl font-extrabold text-emerald-600">{{ topTags.length }}</div>
          <div class="text-sm text-slate-500 mt-1">학습한 태그</div>
        </div>
        <div class="bg-white/80 backdrop-blur border border-white/60 rounded-2xl p-5 text-center shadow-sm">
          <div class="text-3xl font-extrabold text-amber-600">{{ highestClass }}</div>
          <div class="text-sm text-slate-500 mt-1">최고 클래스</div>
        </div>
        <div class="bg-white/80 backdrop-blur border border-white/60 rounded-2xl p-5 text-center shadow-sm">
          <div class="text-3xl font-extrabold text-rose-600">{{ analysisData?.aiAnalysis?.phases?.length || 0 }}</div>
          <div class="text-sm text-slate-500 mt-1">학습 단계</div>
        </div>
      </div>

      <!-- Main Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        <!-- Left Column: Radar Chart + Strengths -->
        <div class="lg:col-span-1 space-y-6">
          <!-- Radar Chart -->
          <div class="bg-white/80 backdrop-blur border border-white/60 rounded-3xl p-6 shadow-xl shadow-indigo-500/10">
            <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wide mb-4">알고리즘 역량 분포</h3>
            <div class="aspect-square">
              <AlgorithmRadarChart :stats="topTags" :max-tags="6" />
            </div>
          </div>

          <!-- Strength & Weakness -->
          <div class="space-y-3">
            <div class="bg-emerald-50 border border-emerald-200 rounded-2xl p-4">
              <div class="flex items-center gap-2 text-emerald-600 text-sm font-bold mb-1">
                <span>💪</span> 핵심 강점
              </div>
              <div class="text-slate-700 font-medium">{{ analysisData?.aiAnalysis?.keyStrength || '-' }}</div>
            </div>
            <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4">
              <div class="flex items-center gap-2 text-amber-600 text-sm font-bold mb-1">
                <span>🎯</span> 집중 영역
              </div>
              <div class="text-slate-700 font-medium">{{ analysisData?.aiAnalysis?.primaryWeakness || '-' }}</div>
            </div>
          </div>
        </div>

        <!-- Right Column: AI Analysis + Phases -->
        <div class="lg:col-span-2 space-y-6">
          <!-- Overall Assessment -->
          <div class="bg-white/80 backdrop-blur border border-white/60 rounded-3xl p-6 shadow-xl shadow-indigo-500/10">
            <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wide mb-3">📊 AI 종합 분석</h3>
            <p class="text-slate-700 leading-relaxed">{{ analysisData?.aiAnalysis?.overallAssessment }}</p>
            <div v-if="analysisData?.aiAnalysis?.personalizedAdvice" class="mt-4 pt-4 border-t border-slate-100">
              <div class="text-sm text-indigo-600 font-bold mb-1">💡 맞춤 조언</div>
              <p class="text-slate-600 text-sm">{{ analysisData?.aiAnalysis?.personalizedAdvice }}</p>
            </div>
          </div>

          <!-- Learning Phases -->
          <div class="bg-white/80 backdrop-blur border border-white/60 rounded-3xl p-6 shadow-xl shadow-indigo-500/10">
            <h3 class="text-sm font-bold text-slate-500 uppercase tracking-wide mb-4">📚 학습 로드맵</h3>
            <div class="space-y-4">
              <div 
                v-for="(phase, idx) in analysisData?.aiAnalysis?.phases || []" 
                :key="idx"
                class="flex gap-4"
              >
                <!-- Phase indicator -->
                <div class="flex flex-col items-center">
                  <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold text-sm shadow-lg">
                    {{ phase.phase || idx + 1 }}
                  </div>
                  <div v-if="idx < (analysisData?.aiAnalysis?.phases?.length || 0) - 1" class="w-0.5 flex-1 bg-slate-200 my-2"></div>
                </div>
                
                <!-- Phase content -->
                <div class="flex-1 pb-4">
                  <div class="flex flex-wrap items-center gap-3 mb-2">
                    <h4 class="font-bold text-slate-800">{{ phase.title }}</h4>
                    <span class="px-2 py-0.5 bg-slate-100 text-slate-600 rounded text-xs font-medium">{{ phase.duration }}</span>
                  </div>
                  <p v-if="phase.focus" class="text-slate-500 text-sm mb-3">{{ phase.focus }}</p>
                  
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                    <div v-if="phase.goals?.length" class="space-y-1">
                      <div class="text-xs text-slate-400 uppercase font-bold">목표</div>
                      <ul class="space-y-1">
                        <li v-for="(goal, gIdx) in phase.goals.slice(0, 2)" :key="gIdx" class="flex items-start gap-2 text-slate-600">
                          <span class="text-emerald-500 mt-0.5">•</span>{{ goal }}
                        </li>
                      </ul>
                    </div>
                    <div v-if="phase.milestones?.length" class="space-y-1">
                      <div class="text-xs text-slate-400 uppercase font-bold">마일스톤</div>
                      <ul class="space-y-1">
                        <li v-for="(m, mIdx) in phase.milestones.slice(0, 2)" :key="mIdx" class="flex items-start gap-2 text-slate-600">
                          <span class="text-amber-500 mt-0.5">★</span>{{ m }}
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Motivational Message -->
      <div v-if="analysisData?.motivationalMessage" class="bg-gradient-to-r from-indigo-500 to-purple-600 rounded-3xl p-8 text-center text-white shadow-xl">
        <p class="text-xl font-medium leading-relaxed">"{{ analysisData.motivationalMessage }}"</p>
      </div>

      <!-- Next Button -->
      <div class="text-center pt-4 pb-8">
        <button 
          @click="nextStep"
          class="px-10 py-4 bg-slate-900 text-white font-bold rounded-2xl hover:bg-slate-800 hover:scale-105 transition-all shadow-lg hover:shadow-xl"
        >
          나에게 맞는 스터디 찾기 →
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
// Import the new component
import AlgorithmRadarChart from '../../components/charts/AlgorithmRadarChart.vue';
import { aiApi } from '../../api/ai';
import { useAuth } from '../../composables/useAuth';

const router = useRouter();
const { user } = useAuth();
const loading = ref(true);
const analysisData = ref(null);
const tagStats = ref([]);
const classStats = ref([]);

// Computed
const topTags = computed(() => tagStats.value.slice(0, 6));
const totalSolved = computed(() => analysisData.value?.solvedCount || 0);
const highestClass = computed(() => {
  const completedClasses = classStats.value.filter(c => c.essentialSolved > 0);
  const maxClass = completedClasses.reduce((max, c) => c.classNumber > (max?.classNumber || 0) ? c : max, null);
  return maxClass ? `Class ${maxClass.classNumber}` : '-';
});

// Radar Chart logic moved to component

onMounted(async () => {
  const userId = user.value?.id || 1;
  
  try {
    const [learningPathRes, tagStatsRes, classStatsRes] = await Promise.allSettled([
      aiApi.getLearningPath(userId),
      aiApi.getTagStats(userId, 6),
      aiApi.getClassStats(userId)
    ]);
    
    if (learningPathRes.status === 'fulfilled') {
      analysisData.value = learningPathRes.value.data;
    }
    if (tagStatsRes.status === 'fulfilled') {
      tagStats.value = tagStatsRes.value.data || [];
    }
    if (classStatsRes.status === 'fulfilled') {
      classStats.value = classStatsRes.value.data || [];
    }
  } catch (e) {
    console.error('Analysis error:', e);
  } finally {
    loading.value = false;
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
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
</style>
