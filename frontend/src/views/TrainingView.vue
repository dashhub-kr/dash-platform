<template>
  <div class="min-h-screen bg-slate-50 relative pb-20">
    <!-- 배경 효과 -->
    <div class="fixed inset-0 pointer-events-none overflow-hidden">
      <div class="absolute top-0 left-1/4 w-96 h-96 bg-indigo-200/20 rounded-full blur-3xl"></div>
      <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-rose-200/20 rounded-full blur-3xl"></div>
    </div>

    <!-- 헤더 영역 -->


    <div class="container mx-auto px-6 py-8 relative z-10">
      <!-- 탭 네비게이션 -->
      <div class="flex justify-center mb-8">
        <div class="flex gap-1 bg-white/50 backdrop-blur-sm p-1.5 rounded-2xl border border-white/60 shadow-sm">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="currentTab = tab.id"
            class="px-6 py-2.5 rounded-xl text-sm font-bold transition-all duration-200"
            :class="currentTab === tab.id ? 'bg-white text-indigo-600 shadow-sm ring-1 ring-black/5' : 'text-slate-500 hover:text-slate-700'"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>
      
      <!-- 1. 로드맵 (스킬 트리) -->
      <div v-if="currentTab === 'roadmap'" class="animate-fade-in-up">
        
        <!-- 히어로 카드 2개 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <!-- 오늘의 복습 -->
          <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center">
                <RefreshCw :size="24" class="text-indigo-600" />
              </div>
              <div>
                <span class="text-xs font-bold text-indigo-600 uppercase">Today's Review</span>
                <h3 class="text-lg font-bold text-slate-800">{{ dailyReview?.title || '복습할 문제가 없습니다' }}</h3>
              </div>
            </div>
            <p class="text-sm text-slate-500 mb-4 line-clamp-2">{{ dailyReview?.reason || '꾸준한 학습으로 실력을 키워보세요!' }}</p>
            <button 
              v-if="dailyReview"
              @click="openDailyReviewModal"
              class="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold transition-colors flex items-center justify-center gap-2"
            >
              <Play :size="16" />
              다시 풀기
            </button>
          </div>

          <!-- 오늘의 도전 -->
          <div class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-12 h-12 bg-rose-100 rounded-xl flex items-center justify-center">
                <Trophy :size="24" class="text-rose-500" />
              </div>
              <div>
                <span class="text-xs font-bold text-rose-500 uppercase">Daily Challenge</span>
                <h3 class="text-lg font-bold text-slate-800">{{ learningPath?.goalLevel || '목표 설정 중...' }}</h3>
              </div>
            </div>
            <p class="text-sm text-slate-500 mb-4">다음 단계로 나아가기 위한 도전입니다.</p>
            <button class="w-full py-3 border-2 border-slate-200 hover:border-rose-400 text-slate-700 hover:text-rose-500 rounded-xl font-bold transition-colors flex items-center justify-center gap-2">
              <Swords :size="16" />
              도전하기
            </button>
          </div>
        </div>

        <!-- 현재 실력 분석 (재사용 컴포넌트) -->
        <section class="mb-8">
          <SkillAnalysisCard 
            :tier="userTier"
            :tier-name="userTierName"
            :chart-tags="chartTags"
            :key-strength="learningPath?.aiAnalysis?.keyStrength"
            :primary-weakness="learningPath?.aiAnalysis?.primaryWeakness"
            :phases="learningPath?.aiAnalysis?.phases || []"
          />
        </section>

        <!-- AI 종합 분석 리포트 -->
        <section class="mb-8">
          <h2 class="text-xl font-bold text-slate-800 mb-5 flex items-center gap-2 pl-2 border-l-4 border-indigo-500">
            AI 종합 분석 리포트
          </h2>
          
          <div class="space-y-4">
            <!-- Analysis Summary Banner -->
            <div class="relative overflow-hidden rounded-2xl bg-indigo-900 shadow-lg text-white p-6">
               <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
               <div class="absolute top-0 right-0 w-64 h-64 bg-indigo-500 rounded-full blur-3xl opacity-20 -translate-y-1/2 translate-x-1/2"></div>
               
               <div class="relative z-10 flex gap-4 items-start">
                 <div class="hidden md:flex p-2.5 bg-indigo-500/30 rounded-xl backdrop-blur-md border border-indigo-400/30 flex-shrink-0">
                    <FileText :size="20" class="text-indigo-200" />
                 </div>
                 <div class="flex-1">
                   <h3 class="text-xs font-bold text-indigo-300 uppercase tracking-widest mb-2">Analysis Summary</h3>
                   <p class="text-base text-indigo-50 leading-relaxed font-medium">
                     {{ learningPath?.aiAnalysis?.analysisSummary || 'AI가 학습 데이터를 분석하고 있습니다...' }}
                   </p>
                 </div>
               </div>
            </div>

            <!-- 3-Column Insights -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
               <!-- Growth Prediction -->
               <div class="bg-white/80 border border-slate-200 rounded-xl p-4 min-h-[120px]">
                 <div class="flex items-center gap-2 mb-2">
                   <div class="w-8 h-8 rounded-full bg-violet-100 flex items-center justify-center text-violet-600">
                     <TrendingUp :size="16" />
                   </div>
                   <h4 class="font-bold text-slate-700 text-sm">성장 예측</h4>
                 </div>
                 <p class="text-slate-600 text-xs leading-relaxed">
                   {{ learningPath?.aiAnalysis?.growthPrediction || '데이터 분석 중...' }}
                 </p>
               </div>

               <!-- Strategic Advice -->
               <div class="bg-indigo-50/80 border border-indigo-200 rounded-xl p-4 min-h-[120px]">
                 <div class="flex items-center gap-2 mb-2">
                   <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600">
                     <Target :size="16" />
                   </div>
                   <h4 class="font-bold text-indigo-900 text-sm">전략적 조언</h4>
                 </div>
                 <p class="text-indigo-800 text-xs leading-relaxed">
                   {{ learningPath?.aiAnalysis?.strategicAdvice || '전략 수립 중...' }}
                 </p>
               </div>

               <!-- Efficiency Analysis -->
               <div class="bg-white/80 border border-slate-200 rounded-xl p-4 min-h-[120px]">
                 <div class="flex items-center gap-2 mb-2">
                   <div class="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center text-emerald-600">
                     <Activity :size="16" />
                   </div>
                   <h4 class="font-bold text-slate-700 text-sm">효율성 분석</h4>
                 </div>
                 <p class="text-slate-600 text-xs leading-relaxed">
                   {{ learningPath?.aiAnalysis?.efficiencyAnalysis || '효율성 분석 중...' }}
                 </p>
               </div>
            </div>
          </div>
        </section>
        
      </div>

      <!-- 2. 스킬 트리 -->
      <div v-if="currentTab === 'skilltree'" class="animate-fade-in-up">
        <SkillTreeView />
      </div>

    </div>
  </div>

  <!-- YouTube 임베드 모달 -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="selectedVideoId" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm" @click="closeVideoModal">
        <div class="relative w-full max-w-5xl aspect-video" @click.stop>
          <!-- 닫기 버튼 -->
          <button 
            @click="closeVideoModal"
            class="absolute -top-12 right-0 text-white/80 hover:text-white transition-colors flex items-center gap-2 text-sm font-medium"
          >
            <span>닫기</span>
            <div class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
          </button>
          
          <!-- YouTube Embed -->
          <iframe 
            :src="`https://www.youtube.com/embed/${selectedVideoId}?autoplay=1&rel=0`"
            class="w-full h-full rounded-2xl shadow-2xl"
            frameborder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowfullscreen
          ></iframe>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 강의 모달 (재사용 가능 컴포넌트) -->
  <LectureModal 
    :is-open="lectureModalOpen"
    :tag-name="lectureTagName"
    :tag-key="lectureTagKey"
    :boj-tag-id="lectureBojTagId"
    @close="closeLectureModal"
  />
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useRoute } from 'vue-router'; // useRoute 추가
import { 
    RefreshCw, Trophy, Swords, Play,
    Search, Loader2, Send, RotateCcw, LayoutGrid,
    Map as MapIcon, FileText, TrendingUp, Target, Activity, AlertTriangle, Zap
} from 'lucide-vue-next';
import AlgorithmRadarChart from '../components/charts/AlgorithmRadarChart.vue';
import LearningRoadmap from '../components/LearningRoadmap.vue';
import SkillTreeView from '../components/SkillTreeView.vue';
import SkillAnalysisCard from '../components/SkillAnalysisCard.vue';
import LectureModal from '../components/LectureModal.vue';
import { useAuth } from '../composables/useAuth';
import { aiApi } from '../api/ai';
import { marked } from 'marked';

// State
const { user } = useAuth();
const currentTab = ref('roadmap');
const isSimulating = ref(false);
const selectedVideoId = ref(null); // YouTube 모달용

// Lecture Modal State
const lectureModalOpen = ref(false);
const lectureTagName = ref('');
const lectureTagKey = ref('');
const lectureBojTagId = ref('');

const tabs = [
    { id: 'roadmap', label: '⛳️ 로드맵' },
    { id: 'skilltree', label: '🌳 스킬 트리' }
];

// 1. Roadmap Data
const learningPath = ref(null);
const dailyReview = ref(null);
const allTagStats = ref([]);

// User Tier (from auth or learning path)
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

// Chart Tags for Radar Chart (familyStats based, like OnboardingAnalysis)
const familyStats = ref([]);
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

  // 2. Fallback: Use Tags from Learning Path
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

// 2. Videos Data
const searchKeyword = ref('');
const recommendedKeywords = ref(['Dynamic Programming', 'BFS', 'Dijkstra', 'Greedy']);
const videos = ref([]);
const recommendedProblems = ref([]); // 추천 문제 상태 추가

const route = useRoute(); // 라우트 인스턴스

// URL 쿼리 파라미터 처리 (초기 로드 + URL 변경 감지)
const handleQueryParams = () => {
    if (route.query.tab) {
        currentTab.value = route.query.tab;
    }
    
    if (route.query.q) {
        if (currentTab.value !== 'videos') currentTab.value = 'videos';
        // 이미 같은 검색어면 재검색 방지, 단 태그변경시 재검색
        if (searchKeyword.value !== route.query.q || currentTagKey.value !== route.query.tagKey) {
            // tagKey, bojId가 있으면 함께 전달
            searchVideos(route.query.q, route.query.tagKey, route.query.bojId);
        }
    }
};

const goToProblem = (problemNumber, link) => {
    if (link) window.open(link, '_blank');
    else if (problemNumber) window.open(`https://www.acmicpc.net/problem/${problemNumber}`, '_blank');
};

const currentTagKey = ref(''); // 현재 추천된 태그 키 저장
const currentBojId = ref('');  // 현재 백준 태그 ID 저장

const goToMoreProblems = () => {
    if (!user.value) return;
    
    const tierStart = userTier.value || 1;
    // 범위: 내 티어 ~ +4 (넓게 탐색)
    const tierEnd = Math.min(tierStart + 4, 30); 
    
    if (currentBojId.value) {
        // 1. BOJ Tag ID가 있으면 백준 문제집으로 이동 (사용자가 원한 방식)
        const tierRange = Array.from({ length: tierEnd - tierStart + 1 }, (_, i) => tierStart + i).join('%2C');
        const url = `https://www.acmicpc.net/problemset?sort=ac_desc&submit=pac%2Cfa%2Cus&tier=${tierRange}&algo=${currentBojId.value}&algo_if=and`;
        window.open(url, '_blank');
    } else if (currentTagKey.value) {
        // 2. 없으면 Solved.ac 검색 (fallback)
        const query = `*tag:${currentTagKey.value} tier:${tierStart}..${tierEnd} -s@${user.value.username}`;
        window.open(`https://solved.ac/search?query=${encodeURIComponent(query)}`, '_blank');
    }
};

const loadLearningPath = async () => {
    try {
        if (!user.value) return;
        
        // Load learning path and family stats in parallel
        const [learningPathRes, familyRes] = await Promise.all([
            aiApi.getLearningPath(user.value.id),
            aiApi.getFamilyStats(user.value.id).catch(() => ({ data: [] }))
        ]);
        
        learningPath.value = learningPathRes.data;
        familyStats.value = familyRes.data || [];
        
        if (learningPathRes.data.weaknessTags?.length > 0) {
            recommendedKeywords.value = learningPathRes.data.weaknessTags.map(t => t.tagName);
        }

        const weak = learningPathRes.data.weaknessTags || [];
        const strong = learningPathRes.data.strengthTags || [];
        const map = new Map();
        [...weak, ...strong].forEach(t => map.set(t.tagKey, t));
        allTagStats.value = Array.from(map.values());

        if (learningPathRes.data.weaknessTags?.length > 0) {
            const worstTag = learningPathRes.data.weaknessTags[0];
            const winRate = worstTag.total > 0 ? Math.round((worstTag.solved / worstTag.total) * 100) : 0;
            
            // Build tier range: userTier ~ userTier+4 (capped at 30)
            const tierStart = userTier.value || 1;
            const tierEnd = Math.min(tierStart + 4, 30);
            const tierRange = Array.from({ length: tierEnd - tierStart + 1 }, (_, i) => tierStart + i).join('%2C');
            
            // Build 백준 problemset URL with tier filtering
            const bojTagId = worstTag.bojTagId;
            const link = bojTagId 
                ? `https://www.acmicpc.net/problemset?sort=ac_desc&submit=pac%2Cfa%2Cus&tier=${tierRange}&algo=${bojTagId}&algo_if=and`
                : `https://solved.ac/problems/tags/${worstTag.tagKey}`;
            
            dailyReview.value = {
                title: `${worstTag.tagName} 집중 공략`,
                problemNumber: '', 
                link,
                reason: `정답률 ${winRate}%로 가장 낮습니다.`
            };
        } else if (res.data.classStats?.length > 0) {
            const processingClass = res.data.classStats.find(c => c.essentialSolved < c.essentials);
            if (processingClass) {
                dailyReview.value = {
                    title: `Class ${processingClass.classNumber} 정복`,
                    link: `https://solved.ac/class/${processingClass.classNumber}`,
                    reason: `${processingClass.essentials - processingClass.essentialSolved}문제 남음`
                };
            }
        }
    } catch (e) {
        console.error("Failed to load learning path:", e);
    }
};

onMounted(async () => {
    handleQueryParams();
    if (user.value) {
        await loadLearningPath();
    }
});

watch(user, async (newUser) => {
    if (newUser) {
        await loadLearningPath();
    }
});

watch(() => route.query, () => {
    handleQueryParams();
});

const searchVideos = async (keyword, explicitTagKey = null, explicitBojId = null) => {
    currentTab.value = 'videos';
    searchKeyword.value = keyword;
    isLoadingVideos.value = true;
    recommendedProblems.value = []; 

    try {
        const youtubeQuery = keyword.includes('알고리즘') ? keyword : keyword + " 알고리즘 강의";
        const videoRes = await youtubeApi.search(youtubeQuery);
        videos.value = videoRes.data;

        let tag = explicitTagKey;
        if (!tag) {
             tag = keyword.replace(' 알고리즘', '').trim();
        }
        
        if (tag) {
            currentTagKey.value = tag; 
            currentBojId.value = explicitBojId; // BoJ ID 저장
            
            const tier = userTier.value || 1;
            const probRes = await problemApi.getRecommendations(tag, tier);
            recommendedProblems.value = probRes.data;
        } else {
            currentTagKey.value = '';
            currentBojId.value = '';
        }

    } catch (e) {
        console.error("Search failed:", e);
    } finally {
        isLoadingVideos.value = false;
    }
};

const openVideo = (videoId) => {
    selectedVideoId.value = videoId;
};

const closeVideoModal = () => {
    selectedVideoId.value = null;
};

// Lecture Modal 함수
const openDailyReviewModal = () => {
    if (!dailyReview.value) return;
    // dailyReview의 태그 정보로 모달 열기
    lectureTagName.value = dailyReview.value.title?.replace('집중 공략', '').replace(' ', '') || dailyReview.value.tag || 'binary_search';
    lectureTagKey.value = dailyReview.value.tagKey || dailyReview.value.tag || 'binary_search';
    lectureBojTagId.value = dailyReview.value.bojTagId || '';
    lectureModalOpen.value = true;
};

const closeLectureModal = () => {
    lectureModalOpen.value = false;
};



const renderMarkdown = (text) => marked(text || '');

const formatDate = (isoString) => {
    if (!isoString) return '';
    return new Date(isoString).toLocaleDateString();
};
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
    display: none;
}
.no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

/* YouTube 모달 페이드 트랜지션 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>
