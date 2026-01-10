<template>
  <!-- Main Layout Wrapper matching DashboardView -->
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">
      <div class="min-h-screen bg-white pb-20">
    <!-- Main Layout Container -->
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full items-start">
        
        <!-- LEFT COLUMN: Main Content (로드맵 중심) -->
        <main class="flex-1 min-w-0 space-y-8 animate-in slide-in-from-left duration-500">
          
          <!-- Page Header (외부) -->
          <!-- Page Header (외부) -->
          <div class="mb-8">
            <div class="flex items-center gap-3 mb-2">
              <School class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
              <h1 class="text-xl font-black text-slate-800">로드맵</h1>
            </div>
            <p class="text-slate-500 font-medium">나에게 맞는 학습 경로를 따라가보세요</p>
          </div>
          

          
          <!-- 승인 대기 배너 -->
          <div v-if="isPendingApproval" class="bg-amber-50 border border-amber-200 rounded-2xl p-4 flex items-start gap-3 shadow-sm animate-pulse">
            <div class="bg-amber-100 p-2 rounded-lg text-amber-600">
                <Clock class="w-5 h-5" stroke-width="2.5" />
            </div>
            <div>
                <h3 class="font-bold text-amber-800 text-sm mb-1">
                    <span class="font-black">[{{ pendingStudyName }}]</span> 스터디 승인 대기 중입니다
                </h3>
                <p class="text-xs text-amber-700 leading-relaxed font-medium">
                    스터디장의 승인을 기다리고 있습니다. 승인이 완료되면 해당 스터디의 대시보드와 미션 기능을 이용할 수 있습니다.<br/>
                    그 동안 <span class="font-bold underline">맞춤형 로드맵</span>을 통해 개인 학습을 진행해보세요!
                </p>
            </div>
          </div>

          <section class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <svg class="w-5 h-5 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path></svg>
              맞춤형 학습 로드맵
            </h2>
            <LearningRoadmap :phases="learningPath?.aiAnalysis?.phases || []" class="w-full" />
          </section>

          <!-- AI 요약 (간결하게) -->
          <section v-if="learningPath?.aiAnalysis?.analysisSummary" class="bg-brand-50 rounded-2xl p-6 border border-brand-100">
            <div class="flex items-start gap-3">
              <div class="w-8 h-8 bg-brand-100 rounded-lg flex items-center justify-center shrink-0">
                <Sparkles class="w-4 h-4 text-brand-600" stroke-width="2.5" />
              </div>
              <p class="text-sm text-slate-700 leading-relaxed font-medium">
                {{ learningPath.aiAnalysis.analysisSummary }}
              </p>
            </div>
          </section>

          <!-- 주간 목표 + 추천 태그 -->
          <section class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- 주간 목표 -->
            <div v-if="learningPath?.aiAnalysis?.weeklyGoal" class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
              <div class="text-[10px] font-black text-brand-600 uppercase tracking-wider mb-2">이번 주 목표</div>
              <p class="text-sm font-bold text-slate-800">{{ learningPath.aiAnalysis.weeklyGoal }}</p>
              <div v-if="learningPath?.aiAnalysis?.estimatedDaysToGoal" class="mt-3 pt-3 border-t border-slate-100 flex items-center gap-2">
                <span class="text-2xl font-black text-brand-600">{{ learningPath.aiAnalysis.estimatedDaysToGoal }}</span>
                <span class="text-[10px] font-black text-slate-400 uppercase">Days to Goal</span>
              </div>
            </div>

            <!-- 추천 태그 -->
            <div v-if="learningPath?.aiAnalysis?.recommendedTags?.length" class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
              <div class="text-[10px] font-black text-slate-500 uppercase tracking-wider mb-3">집중 추천 태그</div>
              <div class="flex flex-wrap gap-2">
                <span 
                  v-for="(tag, idx) in learningPath.aiAnalysis.recommendedTags" 
                  :key="idx"
                  class="px-2.5 py-1 bg-brand-50 text-brand-700 text-xs font-bold rounded-lg"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </section>
        </main>

        <!-- 오른쪽 칼럼: 사이드바 -->
        <aside class="hidden xl:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-fit">
          
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

          <!-- 2. 레이더 차트 (간결) -->
          <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
            <h3 class="text-[10px] font-black text-slate-500 uppercase tracking-wide mb-3 flex items-center gap-1">
              <BarChart3 class="w-3.5 h-3.5 text-brand-500" stroke-width="2.5" />
              알고리즘 역량
            </h3>
            <div class="flex items-center justify-center">
              <div class="w-full aspect-square max-w-[200px]">
                <AlgorithmRadarChart :stats="chartTags" :max-tags="6" class="w-full h-full" />
              </div>
            </div>
          </div>

          <!-- 3. 오늘의 복습 -->
          <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-200">
            <div class="flex items-center gap-3 mb-2">
              <div class="w-9 h-9 bg-brand-50 text-brand-600 rounded-lg flex items-center justify-center shrink-0">
                <RefreshCw :size="18" stroke-width="2.5" fill="currentColor" />
              </div>
              <div class="flex-1 min-w-0">
                <span class="text-[10px] font-black text-brand-600 uppercase tracking-wide">Today's Review</span>
                <h3 class="text-sm font-bold text-slate-800 truncate">{{ dailyReview?.title || '복습할 문제 없음' }}</h3>
              </div>
            </div>
            <button 
              v-if="dailyReview"
              @click="openDailyReviewModal"
              class="w-full py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-lg font-bold text-sm transition-all flex items-center justify-center gap-2"
            >
              <Play :size="12" fill="currentColor" />
              다시 풀기
            </button>
          </div>

          <!-- 4. 오늘의 도전 -->
          <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-200">
            <div class="flex items-center gap-3 mb-2">
              <div class="w-9 h-9 bg-rose-50 text-rose-500 rounded-lg flex items-center justify-center shrink-0">
                <Trophy :size="18" stroke-width="2.5" fill="currentColor" />
              </div>
              <div class="flex-1 min-w-0">
                <span class="text-[10px] font-black text-rose-500 uppercase tracking-wide">Daily Challenge</span>
                <h3 class="text-sm font-bold text-slate-800 truncate">{{ learningPath?.dailyChallenge?.title || learningPath?.goalLevel || '목표 설정 중' }}</h3>
              </div>
            </div>
            <button 
              @click="startChallenge"
              class="w-full py-2 bg-rose-50 hover:bg-rose-100 text-rose-600 rounded-lg font-bold text-sm transition-all flex items-center justify-center gap-2"
            >
              <Swords :size="12" fill="currentColor" />
              도전하기
            </button>
          </div>

          <!-- 5. 취약점 알림 -->
          <div v-if="learningPath?.aiAnalysis?.primaryWeakness" class="bg-amber-50 rounded-2xl p-4 border border-amber-100/50">
            <h3 class="font-bold text-amber-800 text-xs mb-2 flex items-center gap-1.5">
              <AlertTriangle class="w-4 h-4 text-fox" stroke-width="2.5" /> 집중 필요
            </h3>
            <p class="text-xs text-amber-700 leading-relaxed">{{ learningPath.aiAnalysis.primaryWeakness }}</p>
          </div>
        </aside>
      </div>
    </div>

    <!-- 강의 모달 -->
    <LectureModal 
      :is-open="lectureModalOpen"
      :tag-name="lectureTagName"
      :tag-key="lectureTagKey"
      :boj-tag-id="lectureBojTagId"
      @close="closeLectureModal"
    />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router'; // Updated
import { 
    RefreshCw, Trophy, Swords, Play, AlertTriangle, CheckCircle, BarChart3, Sparkles, Clock, School
} from 'lucide-vue-next';
import LearningRoadmap from '@/components/skill/LearningRoadmap.vue';
import AlgorithmRadarChart from '@/components/charts/AlgorithmRadarChart.vue';
import LectureModal from '@/components/lecture/LectureModal.vue';
import { useAuth } from '@/composables/useAuth';
import { aiApi } from '@/api/ai';
import UserQuickStats from '@/components/common/UserQuickStats.vue';

const router = useRouter();
const route = useRoute(); // ADDED
const { user } = useAuth();

const isPendingApproval = computed(() => !!user.value?.pendingStudyName);
const pendingStudyName = computed(() => user.value?.pendingStudyName);

// 상태
const lectureModalOpen = ref(false);
const lectureTagName = ref('');
const lectureTagKey = ref('');
const lectureBojTagId = ref('');

// 로드맵 데이터
const learningPath = ref(null);
const dailyReview = ref(null);
const familyStats = ref([]);

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

// 차트 태그
const chartTags = computed(() => {
  if (familyStats.value && familyStats.value.length > 0) {
    return familyStats.value.map(stat => ({
      tagKey: stat.familyKey?.toLowerCase(),
      solved: stat.solved || 0,
      total: stat.total || 0,
      label: stat.familyName === '다이나믹 프로그래밍' ? 'DP' : stat.familyName
    }));
  }
  if (learningPath.value) {
    const strong = learningPath.value.strengthTags || [];
    const weak = learningPath.value.weaknessTags || [];
    const merged = [...strong, ...weak];
    const uniqueMap = new Map();
    merged.forEach(t => {
      if (!uniqueMap.has(t.tagKey)) {
        uniqueMap.set(t.tagKey, {
          tagKey: t.tagKey,
          solved: t.solved || 0,
          total: t.total || 0,
          label: t.tagName === '다이나믹 프로그래밍' ? 'DP' : t.tagName
        });
      }
    });
    return Array.from(uniqueMap.values());
  }
  return [];
});

const loadLearningPath = async () => {
  try {
    if (!user.value) return;
    
    const [learningPathRes, familyRes] = await Promise.all([
      aiApi.getLearningPath(user.value.id),
      aiApi.getFamilyStats(user.value.id).catch(() => ({ data: [] }))
    ]);
    
    learningPath.value = learningPathRes.data;
    familyStats.value = familyRes.data || [];
    
    // 오늘의 복습 설정
    if (learningPathRes.data.weaknessTags?.length > 0) {
      const worstTag = learningPathRes.data.weaknessTags[0];
      const winRate = worstTag.total > 0 ? Math.round((worstTag.solved / worstTag.total) * 100) : 0;
      const tierStart = userTier.value || 1;
      const tierEnd = Math.min(tierStart + 4, 30);
      const tierRange = Array.from({ length: tierEnd - tierStart + 1 }, (_, i) => tierStart + i).join('%2C');
      const bojTagId = worstTag.bojTagId;
      const link = bojTagId 
        ? `https://www.acmicpc.net/problemset?sort=ac_desc&submit=pac%2Cfa%2Cus&tier=${tierRange}&algo=${bojTagId}&algo_if=and`
        : `https://solved.ac/problems/tags/${worstTag.tagKey}`;
      
      dailyReview.value = {
        title: `${worstTag.tagName} 집중 공략`,
        tagKey: worstTag.tagKey,
        bojTagId: bojTagId,
        link,
        reason: `정답률 ${winRate}%로 가장 낮습니다.`
      };
    }
  } catch (e) {
    console.error("Failed to load learning path:", e);
  }
};

const startChallenge = () => {
  if (learningPath.value?.dailyChallenge?.link) {
    window.open(learningPath.value.dailyChallenge.link, '_blank');
  }
};

const openDailyReviewModal = () => {
  if (!dailyReview.value) return;
  lectureTagName.value = dailyReview.value.title?.replace('집중 공략', '').trim() || 'binary_search';
  lectureTagKey.value = dailyReview.value.tagKey || 'binary_search';
  lectureBojTagId.value = dailyReview.value.bojTagId || '';
  lectureModalOpen.value = true;
};

const closeLectureModal = () => {
  lectureModalOpen.value = false;
};

onMounted(async () => {
  if (user.value) {
    await loadLearningPath();
  }
});

watch(user, async (newUser) => {
  if (newUser) {
    await loadLearningPath();
  }
});
</script>
