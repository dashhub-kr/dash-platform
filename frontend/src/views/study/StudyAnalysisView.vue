<template>
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">

    <!-- 메인 레이아웃 컨테이너 -->
    <div class="flex justify-center p-4 md:p-8 min-h-screen pb-20">
      
      <!-- Loading State -->
      <div v-if="loadingAnalysis" class="flex flex-col items-center justify-center py-40 w-full">
        <div class="w-16 h-16 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mb-6"></div>
        <p class="text-slate-600 text-xl font-medium animate-pulse">팀 역량을 분석하고 있습니다...</p>
      </div>

      <!-- 콘텐츠 상태 -->
      <div v-else-if="analysis" class="flex gap-8 max-w-screen-xl w-full items-start">
          
          <!-- 왼쪽 칼럼: 메인 콘텐츠 -->
          <main class="flex-1 min-w-0 space-y-8 animate-in slide-in-from-left duration-500">
             
             <!-- Page Header -->
             <div class="mb-8">
               <div class="flex items-center gap-3 mb-2">
                 <PieChart class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
                 <h1 class="text-xl font-black text-slate-800">팀 분석</h1>
               </div>
               <p class="text-slate-500 font-medium">팀에 맞는 추천 문제를 등록해보세요</p>
             </div>
             

             
             <div class="bg-white border border-slate-200 shadow-sm rounded-3xl p-6">
                 <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
                   <Activity class="w-5 h-5 text-brand-500" stroke-width="2.5" fill="currentColor" /> 팀 스킬 분석
                 </h2>
                 <div class="flex flex-col items-center justify-center">
                    <div class="w-full max-w-[400px] aspect-square relative z-0">
                      <Radar v-if="radarChartData" :data="radarChartData" :options="radarChartOptions" />
                      <div v-else class="flex items-center justify-center h-full text-slate-400">
                        데이터가 부족합니다
                      </div>
                    </div>
                    <p class="text-center text-xs text-slate-400 mt-6 bg-slate-50 px-3 py-1 rounded-full">
                      팀원별 정규화 후 평균 (본인 최대=100%)
                    </p>
                 </div>
             </div>
  
              <!-- 2. 추천 커리큘럼 -->
              <div class="bg-white border border-slate-200 rounded-3xl p-6 shadow-sm">
                <div class="flex items-center justify-between mb-6">
                  <h2 class="text-lg font-bold text-slate-800 flex items-center gap-2">
                      <BookOpen class="w-5 h-5 text-brand-500" stroke-width="2.5" fill="currentColor" /> 팀 문제 추천 (커리큘럼)
                  </h2>
                  <div v-if="loadingCurriculum" class="flex items-center gap-2 text-brand-600">
                     <Loader2 class="w-4 h-4 animate-spin" />
                     <span class="text-xs font-bold">맞춤 커리큘럼 생성 중...</span>
                  </div>
                </div>
  
                <!-- 커리큘럼 로딩 -->
                <div v-if="loadingCurriculum" class="flex flex-col items-center py-16 animate-fade-in">
                  <div class="w-10 h-10 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mb-4"></div>
                  <p class="text-slate-600 font-bold text-sm animate-pulse">팀에 맞는 문제를 찾고 있습니다...</p>
                </div>
  
                <!-- 커리큘럼 결과 -->
                <div v-else-if="curriculum.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  <div v-for="problem in curriculum" :key="problem.problemId"
                       class="bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-xl p-5 transition-all hover:-translate-y-1 hover:shadow-lg group relative">
                    <a :href="`https://www.acmicpc.net/problem/${problem.problemId}`"
                       target="_blank"
                       class="block">
                      <div class="flex items-center gap-2 mb-2 min-w-0">
                        <span class="text-xl font-black text-brand-600 group-hover:text-brand-500 flex-shrink-0">#{{ problem.problemId }}</span>
                        <span v-if="problem.tags?.length" class="px-2 py-0.5 bg-brand-100 text-brand-700 text-[10px] font-bold rounded-md truncate max-w-[120px]">{{ problem.tags[0] }}</span>
                      </div>
                      <p class="text-slate-800 font-bold truncate text-sm mb-1">{{ problem.title }}</p>
                      <div class="flex items-center gap-1.5 bg-slate-50 px-2.5 py-1 rounded-lg border border-slate-100 w-fit">
                           <img :src="getTierIconUrl(problem.level)" class="w-3.5 h-3.5 object-contain" />
                           <span class="text-xs font-bold" :class="getTierColorClass(problem.level)">
                                {{ getFormattedTierName(problem.level) }}
                           </span>
                      </div>

                       <!-- 이미 푼 멤버 표시 -->
                       <div v-if="problem.solvedMembers && problem.solvedMembers.length > 0" 
                            class="mt-3 flex items-center gap-2 cursor-pointer group/members"
                            @click.prevent.stop="toggleMemberPopup(problem.problemId, $event)">
                            <div class="flex -space-x-1.5">
                               <template v-for="member in problem.solvedMembers.slice(0, 5)" :key="member.id">
                                   <div class="relative group/avatar">
                                       <img :src="member.avatarUrl" :alt="member.username" class="w-5 h-5 rounded-full border border-white ring-1 ring-emerald-50 bg-slate-100 object-cover" />
                                       <div class="absolute left-1/2 -translate-x-1/2 -top-8 bg-slate-800 text-white text-[10px] font-bold px-2 py-1 rounded-lg opacity-0 group-hover/avatar:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-50">{{ member.username }}</div>
                                   </div>
                               </template>
                               <div v-if="problem.solvedMembers.length > 5" class="w-5 h-5 rounded-full bg-emerald-50 border border-white flex items-center justify-center text-[8px] font-bold text-emerald-600">
                                   +{{ problem.solvedMembers.length - 5 }}
                               </div>
                           </div>
                           <span class="text-[10px] text-emerald-600 font-bold bg-emerald-50 px-1.5 py-0.5 rounded-md">멘토 가능</span>
                      </div>
                    </a>
                    <!-- 팀장 전용: 미션 등록 버튼 -->
                    <button v-if="isLeader"
                            @click.prevent.stop="registerAsMission(problem)"
                            class="absolute top-3 right-3 px-2 py-1 bg-brand-500 hover:bg-brand-600 text-white text-[10px] font-bold rounded-lg shadow-md transition-all opacity-0 group-hover:opacity-100 flex items-center gap-1">
                      <Pin class="w-3 h-3" /> 미션 등록
                    </button>
                  </div>
                </div>
  
                <!-- 빈 상태 -->
                <div v-else class="text-center py-12 text-slate-400 text-sm">
                  <p>데이터가 부족하여 커리큘럼을 생성할 수 없습니다.</p>
                </div>
              </div>
          </main>
  
          <!-- 오른쪽 칼럼: 사이드바 -->
          <aside class="hidden lg:flex w-[380px] shrink-0 sticky top-8 h-fit">
              <StudyAnalysisSidebar :analysis="analysis" :memberColors="memberColors" />
          </aside>
      </div>

      <!-- 스터디 없음 상태 -->
      <div v-else class="flex flex-col items-center justify-center py-40 text-center w-full">
        <p class="text-slate-500 text-xl mb-6">스터디에 가입해야 팀 분석을 이용할 수 있습니다.</p>
        <router-link to="/onboarding" class="px-8 py-4 bg-brand-600 text-white rounded-2xl font-bold shadow-lg shadow-brand-200 hover:shadow-brand-300 transition-all hover:-translate-y-1">
          스터디 가입하기
        </router-link>
      </div>

    </div>
    <!-- 미션 생성 모달 -->
    <StudyMissionCreateModal
        :isOpen="showCreateModal"
        :studyId="studyId"
        :missions="missions"
        :initialProblemIds="modalProblemIds"
        :initialTitle="modalTitle"
        @close="closeModal"
        @refresh="loadMissions"
    />
  </div>
    <!-- Member Popup Teleport -->
    <Teleport to="body">
       <div v-if="currentPopupProblem" @click.stop 
            class="fixed bg-white text-slate-800 rounded-2xl z-[9999] min-w-[200px] max-h-[300px] overflow-y-auto shadow-2xl border border-slate-100 p-3 animate-in fade-in zoom-in-95 duration-200" 
            :style="{ 
                top: `${popupPosition.top}px`, 
                left: `${popupPosition.left}px`,
                transform: 'translate(-50%, -100%) translateY(-10px)'
            }">
          <div class="text-slate-400 text-[10px] uppercase tracking-wider mb-2 pb-2 border-b border-slate-100 font-bold flex justify-between items-center">
            <span>해결한 멤버</span>
            <span class="text-slate-900">{{ currentPopupProblem.solvedMembers.length }}명</span>
          </div>
          <div v-for="member in currentPopupProblem.solvedMembers" :key="member.id" class="flex items-center gap-3 py-2 border-b border-slate-50 last:border-0 hover:bg-slate-50 px-2 rounded-lg transition-colors">
             <NicknameRenderer :username="member.username" :avatar-url="member.avatarUrl" avatar-class="w-8 h-8 ring-2 ring-white shadow-sm" text-class="text-sm font-bold text-slate-700" :show-avatar="true" />
          </div>
       </div>
    </Teleport>
    </div>

</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { Radar } from 'vue-chartjs';
import { Chart as ChartJS, RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend } from 'chart.js';
import StudyMissionCreateModal from '@/components/study/StudyMissionCreateModal.vue';
import StudyAnalysisSidebar from '@/components/study/StudyAnalysisSidebar.vue';
import { BookOpen, AlertTriangle, Pin, Users, BookMarked, Activity, PieChart, Loader2 } from 'lucide-vue-next';

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

const router = useRouter();

const loadingAnalysis = ref(true);
const loadingCurriculum = ref(false);
const analysis = ref(null);
const familyStats = ref([]);
const curriculum = ref([]);
const studyId = ref(null);
const isLeader = ref(false);

// 팝업 상태
const openPopupProblemId = ref(null);
const popupPosition = ref({ top: 0, left: 0 });

const toggleMemberPopup = (problemId, event) => {
    if (openPopupProblemId.value === problemId) {
        openPopupProblemId.value = null;
    } else {
        const rect = event.currentTarget.getBoundingClientRect();
        popupPosition.value = {
            top: rect.top, // Fixed layout uses viewport coordinates, scrollY is not needed
            left: rect.left + rect.width / 2
        };
        openPopupProblemId.value = problemId;
    }
};

const closeMemberPopup = () => {
    openPopupProblemId.value = null;
};

const currentPopupProblem = computed(() => {
    if (!openPopupProblemId.value) return null;
    return curriculum.value.find(p => p.problemId === openPopupProblemId.value);
});

// 외부 클릭 감지
onMounted(() => {
    document.addEventListener('click', closeMemberPopup);
});
onUnmounted(() => {
    document.removeEventListener('click', closeMemberPopup);
});

// 모달 관련 상태
const showCreateModal = ref(false);
const modalProblemIds = ref('');
const modalTitle = ref('');
const missions = ref([]); // 미션 목록 (모달용)

// 패밀리 표시 이름 맵핑
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

const memberColors = [
  '#ef4444', // red
  '#22c55e', // green
  '#f59e0b', // amber
  '#8b5cf6', // violet
  '#06b6d4', // cyan
];

// 레이더차트용 레이블 (패밀리 기준)
const chartLabels = computed(() => {
  if (familyStats.value.length === 0) return [];
  return familyStats.value.map(stat => 
    familyDisplayNames[stat.familyKey] || stat.familyName || stat.familyKey
  );
});

// 레이더차트 데이터 - 멤버별 오버레이 + 팀 평균
const radarChartData = computed(() => {
  if (familyStats.value.length === 0 || !analysis.value?.memberStats) return null;
  
  const labels = chartLabels.value;
  const familyKeys = familyStats.value.map(s => s.familyKey?.toLowerCase());
  
  // 멤버별 정규화 데이터 계산
  const normalizedMembers = [];
  const datasets = [];
  
  analysis.value.memberStats.slice(0, 5).forEach((member, idx) => {
    const rawData = familyKeys.map(key => member.tagSolved?.[key] || 0);
    const maxValue = Math.max(...rawData, 1);
    const normalizedData = rawData.map(v => Math.round(v / maxValue * 100));
    normalizedMembers.push(normalizedData);
    
    const color = memberColors[idx % memberColors.length];
    datasets.push({
      label: member.username,
      data: normalizedData,
      backgroundColor: `${color}08`,  // 매우 연한 배경
      borderColor: `${color}50`,      // 연한 테두리
      borderWidth: 1.5,
      pointBackgroundColor: color,
      pointBorderColor: '#fff',
      pointBorderWidth: 1,
      pointRadius: 2,
      pointHoverRadius: 4
    });
  });
  
  // 팀 평균 (정규화된 값들의 평균)
  const teamAvg = familyKeys.map((_, i) => {
    const sum = normalizedMembers.reduce((acc, m) => acc + m[i], 0);
    return Math.round(sum / normalizedMembers.length);
  });
  
  datasets.push({
    label: '팀 평균',
    data: teamAvg,
    backgroundColor: 'rgba(99, 102, 241, 0.15)',
    borderColor: 'rgba(99, 102, 241, 0.9)',
    borderWidth: 2.5,
    pointBackgroundColor: '#6366f1',
    pointBorderColor: '#fff',
    pointBorderWidth: 2,
    pointRadius: 4,
    pointHoverRadius: 6
  });
  
  return { labels, datasets };
});

// 레이더차트 옵션
const radarChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.dataset.label}: ${ctx.raw}%`
      },
      backgroundColor: 'rgba(15, 23, 42, 0.9)',
      padding: 10,
      cornerRadius: 8
    }
  },
  scales: {
    r: {
      beginAtZero: true,
      ticks: {
        stepSize: 20,
        display: false
      },
      grid: {
        color: 'rgba(148, 163, 184, 0.15)',
        circular: true
      },
      angleLines: {
        color: 'rgba(148, 163, 184, 0.15)'
      },
      pointLabels: {
        color: 'rgba(51, 65, 85, 0.8)',
        font: { 
          family: "'Pretendard', sans-serif",
          size: 11, 
          weight: '600' 
        }
      }
    }
  }
}));

onMounted(async () => {
  try {
    const userRes = await axios.get('/api/users/me');
    studyId.value = userRes.data.studyId;
    isLeader.value = userRes.data.isStudyLeader || false;
    
    if (studyId.value) {
      const [analysisRes, familyRes] = await Promise.all([
        axios.get(`/api/studies/${studyId.value}/analysis`),
        axios.get(`/api/studies/${studyId.value}/family-stats`)
      ]);
      analysis.value = analysisRes.data;
      familyStats.value = familyRes.data || [];
      
      // 커리큘럼 및 미션 목록 로드
      loadCurriculum();
      loadMissions();
    }
  } catch (e) {
    console.error('팀 분석 로드 실패', e);
  } finally {
    loadingAnalysis.value = false;
  }
});

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
  return `${levelName} ${subLevel}`; // e.g., Gold III
};

const getTierColorClass = (tier) => {
  const t = Math.round(tier || 0);
  if (t === 0) return 'text-slate-400';
  if (t <= 5) return 'text-orange-700'; // Bronze
  if (t <= 10) return 'text-slate-500'; // Silver
  if (t <= 15) return 'text-yellow-600'; // Gold
  if (t <= 20) return 'text-emerald-500'; // Platinum
  if (t <= 25) return 'text-sky-500'; // Diamond
  if (t <= 30) return 'text-rose-500'; // Ruby
  return 'text-purple-500'; // Master
};

const loadCurriculum = async () => {
  if (!studyId.value) return;
  
  loadingCurriculum.value = true;
  
  try {
    const res = await axios.get(`/api/studies/${studyId.value}/curriculum`);
    curriculum.value = res.data || [];
  } catch (e) {
    console.error('커리큘럼 로드 실패', e);
  } finally {
    loadingCurriculum.value = false;
  }
};

const loadMissions = async () => {
  if (!studyId.value) return;
  try {
    const res = await axios.get(`/api/studies/${studyId.value}/missions`);
    missions.value = res.data;
  } catch (e) {
    console.error('미션 목록 로드 실패', e);
  }
};

// 미션 등록 모달 열기
const registerAsMission = (problem) => {
  modalProblemIds.value = problem.problemId;
  modalTitle.value = problem.tags?.[0] ? `${problem.tags[0]} 연습` : `문제 #${problem.problemId}`;
  showCreateModal.value = true;
};

const closeModal = () => {
    showCreateModal.value = false;
    modalProblemIds.value = '';
    modalTitle.value = '';
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>
