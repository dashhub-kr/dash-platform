<template>
  <div class="skill-tree-wrapper">
    <!-- 로딩 -->
    <div v-if="loading" class="flex items-center justify-center py-16 bg-white rounded-2xl border border-slate-200">
      <div class="text-center">
        <div class="w-10 h-10 mx-auto mb-3 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
        <p class="text-slate-500 text-sm">스킬 트리 로딩 중...</p>
      </div>
    </div>

    <!-- 스킬 트리 -->
    <div v-else class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      
      <!-- 헤더 (그래프 모드일 때는 숨김) -->
      <div v-if="viewMode === 'card'" class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
        <div class="flex items-center gap-3">
          <span class="text-2xl">⚔️</span>
          <h3 class="text-lg font-bold text-slate-800">알고리즘 스킬트리</h3>
        </div>
        <div class="flex gap-4 text-xs">
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
            <span class="text-slate-600">전문가+ {{ expertPlusTags }}</span>
          </span>
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
            <span class="text-slate-600">숙련 {{ advancedTags }}</span>
          </span>
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full bg-sky-500"></span>
            <span class="text-slate-600">학습중 {{ learningTags }}</span>
          </span>
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full bg-slate-300"></span>
            <span class="text-slate-500">미진행 {{ lockedTags }}</span>
          </span>
        </div>
      </div>
      
      <!-- 그래프 뷰 (전체화면 오버레이) -->
      <div v-if="viewMode === 'graph'" class="fixed inset-0 top-[64px] z-50 bg-slate-50 flex flex-col">
        <div class="flex-1 w-full h-full relative">
            <SkillTreeGraphView />

            <!-- 닫기 버튼 (Floating) -->
            <button 
                @click="viewMode = 'card'"
                class="absolute bottom-8 left-1/2 -translate-x-1/2 w-14 h-14 bg-white rounded-full shadow-lg border border-slate-200 flex items-center justify-center text-slate-600 hover:text-slate-900 hover:scale-110 transition-all z-50"
            >
                <X :size="32" stroke-width="2.5" />
            </button>
        </div>
      </div>
      
      <!-- 카드 뷰 (기존 스킬 그리드) -->
      <div v-else class="p-6 relative min-h-[600px]">
        <!-- 그래프 모드 진입 버튼 (Floating) -->
        <button 
            @click="viewMode = 'graph'"
            class="fixed bottom-8 left-1/2 -translate-x-1/2 w-14 h-14 bg-indigo-600 rounded-full shadow-lg hover:shadow-xl shadow-indigo-500/30 flex items-center justify-center text-white hover:bg-indigo-700 hover:scale-110 transition-all z-40"
        >
            <Network :size="32" stroke-width="2.5" />
        </button>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div 
            v-for="family in sortedFamilies" 
            :key="family.key"
            class="group cursor-pointer"
            @click="selectFamily(family)"
          >
            <div 
              class="relative p-4 rounded-xl border-2 transition-all hover:shadow-md"
              :class="[
                selectedFamily?.key === family.key 
                  ? 'bg-indigo-50 border-indigo-400 shadow-md' 
                  : 'bg-white border-slate-200 hover:border-indigo-300',
                getCompletionClass(family.progressPercent)
              ]"
            >
              <!-- 아이콘 + 별 표시 -->
              <div class="flex items-center justify-between mb-3">
                <span class="text-3xl">{{ getFamilyIcon(family.key) }}</span>
                <div class="flex gap-0.5">
                  <span v-for="i in 5" :key="i" class="text-base" :class="i <= getFilledStars(family.masteryLevel) ? 'text-amber-400' : 'text-slate-300'">★</span>
                </div>
              </div>
              
              <!-- 이름 -->
              <div class="text-sm font-bold text-slate-800 mb-2">{{ family.name }}</div>
              
              <!-- 마스터리 레벨 텍스트 -->
              <div class="text-xs text-slate-500 mb-2">
                {{ getFamilyMasteryLabel(family.masteryLevel) }}
              </div>
              
              <!-- 하위 태그 수 -->
              <div class="text-xs text-slate-400">
                {{ family.children?.length || 0 }}개 스킬 · {{ family.solved || 0 }}문제
              </div>
              
              <!-- 마스터 뱃지 -->
              <div v-if="getFilledStars(family.masteryLevel) >= 4" class="absolute -top-2 -right-2 text-lg">🏆</div>
            </div>
          </div>
        </div>
        
        <!-- 선택된 Family의 하위 태그 -->
        <Transition name="expand">
          <div v-if="selectedFamily" class="mt-6 pt-6 border-t border-slate-200">
            <!-- 헤더 -->
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-3">
                <span class="text-2xl">{{ getFamilyIcon(selectedFamily.key) }}</span>
                <span class="font-bold text-slate-800 text-lg">{{ selectedFamily.name }}</span>
                <span class="text-xs text-slate-400">({{ selectedFamily.children?.length || 0 }}개)</span>
              </div>
              <button @click.stop="selectedFamily = null" class="text-slate-400 hover:text-slate-600 text-xl">✕</button>
            </div>
            
            <!-- 하위 태그 그리드 (크기 증가) -->
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div 
                v-for="tag in sortedTags" 
                :key="tag.key"
                class="group cursor-pointer"
                @click="onTagClick(tag)"
              >
                <div 
                  class="relative p-5 rounded-xl border-2 text-center transition-all hover:shadow-lg"
                  :class="getEnhancedSubNodeClass(tag)"
                >
                  <!-- 추천 뱃지 (더 크게 + 글로우) -->
                  <div v-if="isRecommendedTag(tag)" class="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-1 bg-gradient-to-r from-rose-500 to-pink-500 text-white text-xs font-bold rounded-full shadow-lg animate-bounce">
                    🎯 추천
                  </div>
                  
                  <!-- 마스터 뱃지 (4개 이상) - 왼쪽 상단 -->
                  <div v-if="getFilledStars(tag.masteryLevel) >= 4" class="absolute -top-2 -left-2 text-xl drop-shadow-md">🏆</div>
                  
                  <!-- 별 표시 (크기 증가) -->
                  <div class="flex justify-center gap-1 text-xl">
                    <span 
                      v-for="i in getMaxStars(tag.tier)" 
                      :key="i" 
                      :class="getStarClass(tag.tier, i, getFilledStars(tag.masteryLevel))"
                    >★</span>
                  </div>
                  
                  <!-- 이름 -->
                  <div class="mt-3 text-sm font-medium truncate" :class="getFilledStars(tag.masteryLevel) >= 4 ? 'text-amber-700' : 'text-slate-600'">{{ tag.name }}</div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
    <!-- 액션 모달 -->
    <div v-if="selectedActionTag" class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm" @click="closeActionModal">
      <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full relative transform transition-all scale-100" @click.stop>
        <!-- 닫기 버튼 -->
        <button 
          @click="closeActionModal"
          class="absolute top-4 right-4 text-slate-400 hover:text-slate-600 transition-colors"
        >
          <X :size="24" />
        </button>

        <!-- 타이틀 -->
        <div class="text-center mb-8">
          <div class="text-3xl mb-2">🚀</div>
          <h3 class="text-2xl font-bold text-slate-800">{{ selectedActionTag.name }}</h3>
          <p class="text-slate-500 mt-1">어떤 학습을 진행하시겠습니까?</p>
        </div>

        <!-- 액션 버튼들 -->
        <div class="grid grid-cols-2 gap-4">
          <button 
            @click="solveProblem(selectedActionTag)"
            class="flex flex-col items-center justify-center gap-3 p-6 rounded-xl bg-indigo-50 border-2 border-indigo-100 hover:border-indigo-500 hover:bg-indigo-100 transition-all group"
          >
            <div class="w-12 h-12 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center group-hover:bg-indigo-500 group-hover:text-white transition-colors">
              <Code :size="24" stroke-width="2.5" />
            </div>
            <span class="font-bold text-indigo-900">문제 풀기</span>
          </button>

          <button 
            @click="openLecture(selectedActionTag)"
            class="flex flex-col items-center justify-center gap-3 p-6 rounded-xl bg-rose-50 border-2 border-rose-100 hover:border-rose-500 hover:bg-rose-100 transition-all group"
          >
            <div class="w-12 h-12 rounded-full bg-rose-100 text-rose-600 flex items-center justify-center group-hover:bg-rose-500 group-hover:text-white transition-colors">
              <Youtube :size="24" stroke-width="2.5" />
            </div>
            <span class="font-bold text-rose-900">강의 영상</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 강의 모달 (재사용 가능 컴포넌트) -->
    <LectureModal 
      :is-open="lectureModalOpen"
      :tag-name="lectureTag?.name"
      :tag-key="lectureTag?.key"
      :boj-tag-id="lectureTag?.bojTagId"
      @close="closeLectureModal"
    />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { tagApi } from '@/api/tags';
import { useAuth } from '@/composables/useAuth';
import SkillTreeGraphView from '@/components/SkillTreeGraphView.vue';
import LectureModal from '@/components/LectureModal.vue';
import { X, Network } from 'lucide-vue-next';

const router = useRouter();
const { user } = useAuth();
const loading = ref(true);
const skillTree = ref(null);
const selectedFamily = ref(null);
const viewMode = ref('card'); // 'card' or 'graph'

// Lecture Modal State
const lectureModalOpen = ref(false);
const lectureTag = ref(null);

// Family 아이콘
const familyIcons = {
  'IMPLEMENTATION': '⚡',
  'DP': '🧩',
  'GRAPH': '🕸️',
  'MATH': '🔢',
  'GREEDY': '🎯',
  'STRING': '📝',
  'DATA_STRUCTURE': '📦',
  'ADVANCED': '🚀'
};

// 사용자 티어
const userTier = computed(() => user.value?.solvedacTier || 1);

// 추천 태그 (선택된 Family 내에서 가장 낮은 진행률, 동률시 S > A > B > C 우선)
const TIER_PRIORITY = { 'S': 0, 'A': 1, 'B': 2, 'C': 3 };

const recommendedTagKey = computed(() => {
  if (!selectedFamily.value?.children?.length) return null;
  const learningTags = selectedFamily.value.children.filter(
    t => t.solved > 0 && t.masteryLevel !== 'MASTER'
  );
  if (learningTags.length === 0) {
    // 아직 안 푼 태그 중 S티어 우선으로 첫 번째 권장
    const unstarted = selectedFamily.value.children
      .filter(t => t.solved === 0)
      .sort((a, b) => (TIER_PRIORITY[a.tier] ?? 4) - (TIER_PRIORITY[b.tier] ?? 4));
    return unstarted.length > 0 ? unstarted[0].key : null;
  }
  // 가장 낮은 진행률 + S티어 우선
  const sorted = [...learningTags].sort((a, b) => {
    const progressDiff = (a.progressPercent || 0) - (b.progressPercent || 0);
    if (progressDiff !== 0) return progressDiff;
    return (TIER_PRIORITY[a.tier] ?? 4) - (TIER_PRIORITY[b.tier] ?? 4);
  });
  return sorted[0].key;
});

const isRecommendedTag = (tag) => tag.key === recommendedTagKey.value;

// 헤더 통계 (전체 하위 태그 기준)
const allChildTags = computed(() => {
  if (!skillTree.value?.families) return [];
  return skillTree.value.families.flatMap(f => f.children || []);
});

const expertPlusTags = computed(() => 
  allChildTags.value.filter(t => ['MASTER', 'EXPERT'].includes(t.masteryLevel)).length
);

const advancedTags = computed(() => 
  allChildTags.value.filter(t => ['ADVANCED', 'INTERMEDIATE'].includes(t.masteryLevel)).length
);

const learningTags = computed(() => 
  allChildTags.value.filter(t => t.masteryLevel === 'BEGINNER').length
);

const lockedTags = computed(() => 
  allChildTags.value.filter(t => !t.masteryLevel || t.masteryLevel === 'NONE').length
);

// orderIndex 순서대로 정렬
const sortedFamilies = computed(() => {
  if (!skillTree.value?.families) return [];
  return [...skillTree.value.families].sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0));
});

const getFamilyIcon = (key) => familyIcons[key] || '📌';

// 정렬된 태그 (S/A 먼저)
const sortedTags = computed(() => {
  if (!selectedFamily.value?.children) return [];
  return [...selectedFamily.value.children].sort((a, b) => {
    const order = { 'S': 0, 'A': 1, 'B': 2, 'C': 3 };
    return (order[a.tier] ?? 4) - (order[b.tier] ?? 4);
  });
});

// 스타일 함수들
const getProgressColor = (percent) => {
  if (percent >= 70) return 'text-emerald-400';
  if (percent >= 30) return 'text-amber-400';
  return 'text-slate-400';
};

const getProgressBarColor = (percent) => {
  if (percent >= 70) return 'bg-emerald-500';
  if (percent >= 30) return 'bg-amber-500';
  return 'bg-slate-500';
};

const getCompletionClass = (percent) => {
  if (percent >= 70) return 'ring-1 ring-emerald-500/30';
  return '';
};

const getSubNodeClass = (tag) => {
  if (tag.tier === 'S') return 'bg-amber-50 border-amber-400';
  if (tag.tier === 'A') return 'bg-purple-50 border-purple-400';
  if (tag.tier === 'B') return 'bg-sky-50 border-sky-400';
  if (tag.progressPercent >= 70) return 'bg-emerald-50 border-emerald-400';
  return 'bg-slate-50 border-slate-200';
};

// 강화된 태그 카드 스타일 (별 4개 이상시 특별 효과)
const getEnhancedSubNodeClass = (tag) => {
  const filled = getFilledStars(tag.masteryLevel);
  
  // 4개 이상 채움: 골드 그라데이션 + 글로우
  if (filled >= 4) {
    return 'bg-gradient-to-br from-amber-50 to-yellow-100 border-amber-400 shadow-lg shadow-amber-200/50 ring-2 ring-amber-300/30';
  }
  
  // 기본 스타일 - 뉴트럴 색상
  return 'bg-white border-slate-200 hover:border-slate-300';
};

// 티어 뱃지 클래스
const getTierBadgeClass = (tier) => {
  if (tier === 'S') return 'bg-gradient-to-br from-amber-400 to-orange-500 text-white';
  if (tier === 'A') return 'bg-gradient-to-br from-purple-400 to-violet-500 text-white';
  if (tier === 'B') return 'bg-gradient-to-br from-sky-400 to-blue-500 text-white';
  return 'bg-slate-400 text-white';
};

// 별 색상 (통일 골드 색상)
const getStarClass = (tier, starIndex, filledCount) => {
  if (starIndex > filledCount) return 'text-slate-300';
  return 'text-amber-400 drop-shadow-sm';
};

const getTierBadge = (tier) => {
  if (tier === 'S') return 'bg-amber-400 text-slate-900';
  if (tier === 'A') return 'bg-purple-400 text-white';
  if (tier === 'B') return 'bg-sky-400 text-white';
  if (tier === 'C') return 'bg-teal-400 text-white';
  return 'bg-slate-600 text-slate-300';
};

// 별 표시 함수들
const getMaxStars = (tier) => {
  if (tier === 'S') return 5;
  if (tier === 'A') return 4;
  if (tier === 'B') return 3;
  return 2; // C 또는 기타
};

const getFilledStars = (masteryLevel) => {
  const levels = { 'NONE': 0, 'BEGINNER': 1, 'INTERMEDIATE': 2, 'ADVANCED': 3, 'EXPERT': 4, 'MASTER': 5 };
  return levels[masteryLevel] || 0;
};

const getFamilyMasteryLabel = (masteryLevel) => {
  const labels = {
    'NONE': '시작 전',
    'BEGINNER': '입문',
    'INTERMEDIATE': '학습중',
    'ADVANCED': '숙련',
    'EXPERT': '전문가',
    'MASTER': '마스터'
  };
  return labels[masteryLevel] || '시작 전';
};

const selectFamily = (family) => {
  selectedFamily.value = selectedFamily.value?.key === family.key ? null : family;
};



// --- 액션 모달 로직 ---
const selectedActionTag = ref(null);

const onTagClick = (tag) => {
  console.log('Tag Clicked:', tag.name);
  openLecture(tag); // 바로 강의 모달 띄우기
};

const closeActionModal = () => {
  selectedActionTag.value = null;
};

const openLecture = async (tag) => {
  closeActionModal();
  lectureTag.value = tag;
  lectureModalOpen.value = true;
  lectureLoading.value = true;
  lectureVideos.value = [];
  lectureProblems.value = [];
  
  try {
    const query = `${tag.name} 알고리즘 강의`;
    const [videoRes, probRes] = await Promise.all([
      youtubeApi.search(query),
      problemApi.getRecommendations(tag.key, userTier.value || 1)
    ]);
    lectureVideos.value = videoRes.data || [];
    lectureProblems.value = probRes.data || [];
  } catch (e) {
    console.error('Failed to load lecture data:', e);
  } finally {
    lectureLoading.value = false;
  }
};

const closeLectureModal = () => {
  lectureModalOpen.value = false;
  lectureTag.value = null;
};

const playVideo = (videoId) => {
  selectedVideoId.value = videoId;
};

const goToProblemModal = (problemId) => {
  window.open(`https://www.acmicpc.net/problem/${problemId}`, '_blank');
};

const goToMoreProblemsModal = () => {
  const tierStart = userTier.value || 1;
  const tierEnd = Math.min(tierStart + 4, 30);
  
  if (lectureTag.value?.bojTagId) {
    const tierRange = Array.from({ length: tierEnd - tierStart + 1 }, (_, i) => tierStart + i).join('%2C');
    const url = `https://www.acmicpc.net/problemset?sort=ac_desc&submit=pac%2Cfa%2Cus&tier=${tierRange}&algo=${lectureTag.value.bojTagId}&algo_if=and`;
    window.open(url, '_blank');
  } else if (lectureTag.value?.key) {
    const query = `*tag:${lectureTag.value.key} tier:${tierStart}..${tierEnd} -s@${user.value?.solvedacHandle || ''}`;
    window.open(`https://solved.ac/search?query=${encodeURIComponent(query)}`, '_blank');
  }
};

// 기존 onTagClick 로직을 재사용 (solveProblem)
const solveProblem = (tag) => {
  const tierStart = userTier.value || 1;
  const tierEnd = Math.min(tierStart + 4, 30);
  
  if (tag.bojTagId) {
    const tierRange = Array.from({ length: tierEnd - tierStart + 1 }, (_, i) => tierStart + i).join('%2C');
    const url = `https://www.acmicpc.net/problemset?sort=ac_desc&submit=pac%2Cfa%2Cus&tier=${tierRange}&algo=${tag.bojTagId}&algo_if=and`;
    window.open(url, '_blank');
  } else {
    // Solved.ac fallback (tagKey 기반 검색) -> 여기도 필터링 적용 가능하면 좋으나 일단 기존 유지 혹은 업데이트
    // 사용자 요청인 "로드맵의 todays review와 완전히 동일하게"를 따르자면 Solved.ac 검색도 userTier 조건이 들어가야 함.
    const query = `*tag:${tag.key} tier:${tierStart}..${tierEnd} -s@${user.value?.solvedacHandle || ''}`;
    window.open(`https://solved.ac/search?query=${encodeURIComponent(query)}`, '_blank');
  }
  closeActionModal();
};

const fetchSkillTree = async () => {
  try {
    loading.value = true;
    const userId = user?.id || 1;
    const res = await tagApi.getSkillTree(userId);
    skillTree.value = res.data;
  } catch (error) {
    console.error('Failed to load skill tree:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => fetchSkillTree());
</script>

<style scoped>
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Modal fade transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Line clamp utility */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
