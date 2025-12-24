<template>
  <div class="study-analysis-container relative w-full min-h-screen bg-slate-50 font-[Pretendard]">
    <!-- 배경 효과 -->
    <div class="absolute inset-0 bg-gradient-to-br from-indigo-50 via-white to-emerald-50"></div>
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-200/30 rounded-full blur-3xl animate-pulse mix-blend-multiply"></div>
      <div class="absolute bottom-1/3 right-1/4 w-80 h-80 bg-emerald-200/30 rounded-full blur-3xl animate-pulse delay-1000 mix-blend-multiply"></div>
    </div>

    <div class="relative z-10 p-6 md:p-10 max-w-7xl mx-auto">
      
      <!-- 헤더 -->
      <div class="text-center mb-10">
        <h1 class="text-4xl md:text-5xl font-black text-slate-900 tracking-tight mb-2">팀 분석</h1>
        <p class="text-slate-500 text-lg">스터디 멤버들의 역량을 분석하고 커리큘럼을 제안합니다</p>
      </div>

      <!-- 로딩 상태 - 팀 분석 -->
      <div v-if="loadingAnalysis" class="flex flex-col items-center justify-center py-20">
        <div class="relative w-80 h-80 rounded-3xl overflow-hidden shadow-2xl mb-6">
          <video autoplay loop muted playsinline class="w-full h-full object-cover">
            <source src="/rec/team.mp4" type="video/mp4" />
          </video>
          <div class="absolute inset-0 bg-gradient-to-t from-black/30 to-transparent"></div>
        </div>
        <p class="text-slate-600 text-xl font-medium animate-pulse">팀 역량을 분석하고 있습니다...</p>
      </div>

      <!-- 분석 결과 -->
      <div v-else-if="analysis" class="space-y-10">
        
        <!-- 팀 개요 카드 -->
        <div class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-3xl p-8 shadow-xl">
          <div class="flex flex-wrap items-center justify-between gap-6 mb-8">
            <div>
              <h2 class="text-2xl font-bold text-slate-900 mb-1">팀 평균 티어</h2>
              <p class="text-slate-500">{{ getTierName(analysis.averageTier) }}</p>
            </div>
            <div class="flex items-center gap-4">
              <div class="text-center px-6 py-3 bg-slate-100 rounded-xl">
                <p class="text-3xl font-bold text-indigo-600">{{ analysis.memberStats.length }}</p>
                <p class="text-xs text-slate-500 font-medium">멤버 수</p>
              </div>
              <div class="text-center px-6 py-3 bg-emerald-50 rounded-xl">
                <p class="text-3xl font-bold text-emerald-600">{{ Math.round(analysis.averageTier) }}</p>
                <p class="text-xs text-slate-500 font-medium">Tier</p>
              </div>
            </div>
          </div>

          <!-- Radar Chart 영역 -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <!-- 차트 -->
            <div class="bg-slate-50 rounded-2xl p-6 flex flex-col items-center justify-center min-h-[350px]">
              <div class="w-full max-w-[300px] aspect-square">
                <Radar v-if="radarChartData" :data="radarChartData" :options="radarChartOptions" />
                <div v-else class="flex items-center justify-center h-full text-slate-400">
                  데이터가 부족합니다
                </div>
              </div>
              <p class="text-center text-xs text-slate-400 mt-3">
                팀원별 정규화 후 평균 (본인 최대=100%)
              </p>
            </div>

            <!-- 약점 및 범례 -->
            <div class="space-y-6">
              <!-- 상위 약점 -->
              <div class="bg-amber-50 border border-amber-200 rounded-2xl p-6">
                <h3 class="text-lg font-bold text-amber-800 mb-4 flex items-center gap-2">
                  <span>⚠️</span> 팀 약점 태그
                </h3>
                <div class="space-y-3">
                  <div v-for="weakness in analysis.topWeaknesses" :key="weakness.tagKey" 
                       class="flex items-center justify-between bg-white rounded-xl px-4 py-3">
                    <span class="font-medium text-slate-700">{{ getTagDisplayName(weakness.tagKey) }}</span>
                    <span class="text-amber-600 font-bold">{{ Math.round(weakness.averageRate) }}문제</span>
                  </div>
                </div>
              </div>

              <!-- 멤버 범례 -->
              <div class="bg-white rounded-2xl p-6 border border-slate-200">
                <h3 class="text-lg font-bold text-slate-800 mb-4">멤버별 정보</h3>
                <div class="space-y-2">
                  <div v-for="(member, idx) in analysis.memberStats.slice(0, 5)" :key="'legend-' + idx"
                       class="flex items-center gap-3">
                    <div class="w-4 h-4 rounded-full" :style="{ backgroundColor: memberColors[idx % memberColors.length] }"></div>
                    <span class="text-slate-700">{{ member.username }}</span>
                    <span class="text-xs text-slate-400 ml-auto">Tier {{ member.tier || 'N/A' }}</span>
                  </div>
                  <!-- 팀 평균 범례 -->
                  <div class="flex items-center gap-3 mt-3 pt-3 border-t border-slate-100">
                    <div class="w-4 h-4 rounded-full bg-indigo-500"></div>
                    <span class="text-slate-700 font-medium">팀 평균</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 커리큘럼 섹션 -->
        <div class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-3xl p-8 shadow-xl">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-slate-900">📚 추천 커리큘럼</h2>
            <button @click="loadCurriculum" 
                    class="px-6 py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-medium transition-all shadow-lg shadow-indigo-500/25"
                    :disabled="loadingCurriculum">
              {{ loadingCurriculum ? '생성 중...' : '커리큘럼 생성' }}
            </button>
          </div>

          <!-- 커리큘럼 로딩 -->
          <div v-if="loadingCurriculum" class="flex flex-col items-center py-16">
            <div class="relative w-64 h-64 rounded-2xl overflow-hidden shadow-xl mb-4">
              <video autoplay loop muted playsinline class="w-full h-full object-cover">
                <source src="/rec/problem-search.mp4" type="video/mp4" />
              </video>
            </div>
            <p class="text-slate-600 font-medium animate-pulse">팀에 맞는 문제를 찾고 있습니다...</p>
          </div>

          <!-- 커리큘럼 결과 -->
          <div v-else-if="curriculum.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <a v-for="problem in curriculum" :key="problem.problemId"
               :href="`https://www.acmicpc.net/problem/${problem.problemId}`"
               target="_blank"
               class="bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-xl p-5 transition-all hover:-translate-y-1 hover:shadow-lg group">
              <div class="flex items-center gap-3 mb-2">
                <span class="text-2xl font-bold text-indigo-600 group-hover:text-indigo-500">#{{ problem.problemId }}</span>
                <span v-if="problem.tags?.length" class="px-2 py-1 bg-indigo-100 text-indigo-700 text-xs font-medium rounded-md">{{ problem.tags[0] }}</span>
              </div>
              <p class="text-slate-800 font-medium truncate">{{ problem.title }}</p>
              <p class="text-slate-500 text-sm">Lv.{{ problem.level }}</p>
            </a>
          </div>

          <!-- 빈 상태 -->
          <div v-else class="text-center py-12 text-slate-400">
            <p>커리큘럼을 생성하려면 위 버튼을 클릭하세요</p>
          </div>
        </div>
      </div>

      <!-- 스터디 없음 상태 -->
      <div v-else class="flex flex-col items-center justify-center py-20 text-center">
        <p class="text-slate-500 text-xl mb-6">스터디에 가입해야 팀 분석을 이용할 수 있습니다.</p>
        <router-link to="/onboarding" class="px-8 py-4 bg-indigo-600 text-white rounded-xl font-bold shadow-lg">
          스터디 가입하기
        </router-link>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { Radar } from 'vue-chartjs';
import { Chart as ChartJS, RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend } from 'chart.js';

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

const loadingAnalysis = ref(true);
const loadingCurriculum = ref(false);
const analysis = ref(null);
const familyStats = ref([]);
const curriculum = ref([]);
const studyId = ref(null);

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
    
    if (studyId.value) {
      const [analysisRes, familyRes] = await Promise.all([
        axios.get(`/api/studies/${studyId.value}/analysis`),
        axios.get(`/api/studies/${studyId.value}/family-stats`)
      ]);
      analysis.value = analysisRes.data;
      familyStats.value = familyRes.data || [];
    }
  } catch (e) {
    console.error('팀 분석 로드 실패', e);
  } finally {
    loadingAnalysis.value = false;
  }
});

const getTagDisplayName = (tag) => familyDisplayNames[tag?.toUpperCase()] || tag;

const getTierName = (tier) => {
  if (!tier) return 'Unranked';
  const t = Math.round(tier);
  if (t >= 26) return 'Ruby';
  if (t >= 21) return 'Diamond';
  if (t >= 16) return 'Platinum';
  if (t >= 11) return 'Gold';
  if (t >= 6) return 'Silver';
  if (t >= 1) return 'Bronze';
  return 'Unranked';
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
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>
