<template>
  <div class="study-mission-container relative w-full min-h-screen bg-slate-50 font-[Pretendard]">
    <!-- 배경 효과 -->
    <div class="absolute inset-0 bg-gradient-to-br from-emerald-50 via-white to-indigo-50"></div>
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
      <div class="absolute top-1/4 right-1/4 w-96 h-96 bg-emerald-200/30 rounded-full blur-3xl animate-pulse mix-blend-multiply"></div>
      <div class="absolute bottom-1/4 left-1/4 w-80 h-80 bg-indigo-200/30 rounded-full blur-3xl animate-pulse delay-1000 mix-blend-multiply"></div>
    </div>

    <div class="relative z-10 p-6 md:p-10 max-w-6xl mx-auto">
      
      <!-- 헤더 -->
      <div class="flex flex-wrap items-center justify-between gap-4 mb-10">
        <div>
          <h1 class="text-4xl font-black text-slate-900 tracking-tight mb-1">주차별 미션</h1>
          <p class="text-slate-500">스터디 과제를 관리하고 진행 현황을 확인하세요</p>
        </div>
        <button @click="showCreateModal = true"
                class="px-6 py-3 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl font-bold shadow-lg shadow-emerald-500/25 transition-all">
          + 미션 생성
        </button>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" class="text-center py-20 text-slate-500 animate-pulse text-xl">
        미션 목록을 불러오는 중...
      </div>

      <!-- 미션 목록 -->
      <div v-else-if="missions.length > 0" class="space-y-6">
        <div v-for="mission in missions" :key="mission.id"
             class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-2xl p-6 shadow-lg hover:shadow-xl transition-shadow">
          <div class="flex flex-wrap items-start justify-between gap-4 mb-4">
            <div>
              <div class="flex items-center gap-3 mb-2">
                <span class="px-3 py-1 bg-emerald-100 text-emerald-700 font-bold rounded-lg text-sm">
                  Week {{ mission.week }}
                </span>
                <span v-if="mission.sourceType === 'AI_RECOMMENDED'" 
                      class="px-3 py-1 bg-indigo-100 text-indigo-700 font-medium rounded-lg text-sm">
                  🤖 AI 추천
                </span>
              </div>
              <h3 class="text-xl font-bold text-slate-900">{{ mission.title }}</h3>
            </div>
            <div class="text-right">
              <p class="text-sm text-slate-500 mb-1">마감일</p>
              <p class="font-medium text-slate-700">{{ formatDate(mission.deadline) }}</p>
            </div>
          </div>

          <!-- 문제 목록 -->
          <div class="flex flex-wrap gap-2 mb-6">
            <a v-for="(problemId, idx) in mission.problemIds" :key="problemId"
               :href="`https://www.acmicpc.net/problem/${problemId}`"
               target="_blank"
               class="px-4 py-2 rounded-lg font-medium text-sm transition-all"
               :class="idx < mission.solvedCount 
                 ? 'bg-emerald-100 text-emerald-700 line-through' 
                 : 'bg-slate-100 text-slate-600 hover:bg-slate-200'">
              #{{ problemId }}
            </a>
          </div>

          <!-- 팀원별 진행 레이스 (달리기) -->
          <div class="mt-6 pt-6 border-t border-slate-100">
            <h4 class="text-sm font-bold text-slate-500 mb-4 flex items-center gap-2">
              <span>🏃 팀원 진행 레이스</span>
            </h4>
            
            <div class="space-y-4">
              <div v-for="member in mission.memberProgressList" :key="member.userId" class="relative">
                <!-- 트랙 -->
                <div class="h-2 w-full bg-slate-100 rounded-full relative overflow-visible mt-6 mb-2">
                   <!-- 내 트랙 하이라이트 -->
                   <div v-if="member.userId === currentUserId" 
                        class="absolute inset-0 bg-indigo-50/50 rounded-full -m-1"></div>
                </div>

                <!-- 러너 (Emoji) -->
                <div class="absolute top-0 left-0 w-full h-8 pointer-events-none" style="top: -4px;">
                   <div class="absolute transform -translate-x-1/2 transition-all duration-700 ease-out flex flex-col items-center"
                        :style="{ left: `${(member.completedCount / Math.max(member.totalProblems, 1)) * 100}%` }">
                      <span class="text-2xl filter drop-shadow-md z-10">
                        {{ member.allCompleted ? '🚩' : '🏃' }}
                      </span>
                      <!-- 이름표 -->
                      <span class="text-xs font-bold mt-1 px-2 py-0.5 rounded-full whitespace-nowrap shadow-sm border"
                            :class="member.userId === currentUserId ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-slate-600 border-slate-200'">
                        {{ member.username }}
                      </span>
                      <!-- 퍼센트 -->
                      <span class="text-[10px] font-medium text-slate-400 mt-0.5">
                        {{ member.completedCount }}/{{ member.totalProblems }}
                      </span>
                   </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else class="text-center py-20">
        <p class="text-slate-400 text-xl mb-6">아직 등록된 미션이 없습니다</p>
        <button @click="showCreateModal = true"
                class="px-8 py-4 bg-emerald-600 text-white rounded-xl font-bold shadow-lg">
          첫 미션 만들기
        </button>
      </div>

    </div>

    <!-- 미션 생성 Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
      <div class="bg-white rounded-3xl w-full max-w-lg shadow-2xl overflow-hidden">
        <!-- Modal 헤더 -->
        <div class="bg-gradient-to-r from-emerald-500 to-teal-500 p-6">
          <h2 class="text-2xl font-bold text-white">🎯 새 미션 만들기</h2>
          <p class="text-emerald-100 text-sm mt-1">스터디원들을 위한 주차별 과제를 등록하세요</p>
        </div>
        
        <div class="p-6 space-y-5">
          <!-- 주차 및 제목 Row -->
          <div class="grid grid-cols-4 gap-4">
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">주차</label>
              <input v-model.number="newMission.week" type="number" min="1"
                     class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-center font-bold text-lg transition-all"
                     placeholder="1" />
            </div>
            <div class="col-span-3">
              <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">미션 제목</label>
              <input v-model="newMission.title" type="text"
                     class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all"
                     placeholder="예: DP 기초 다지기" />
            </div>
          </div>
          
          <!-- 문제 번호 -->
          <div>
            <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">📝 문제 번호</label>
            <input v-model="problemIdsInput" type="text"
                   class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all"
                   placeholder="1234, 5678, 9012 (쉼표로 구분)" />
            <p class="text-xs text-slate-400 mt-2">백준 문제 번호를 쉼표로 구분하여 입력하세요</p>
          </div>
          
          <!-- 마감일 -->
          <div>
            <label class="block text-xs font-bold text-slate-500 mb-2 uppercase tracking-wider">⏰ 마감일</label>
            <input v-model="newMission.deadline" type="date"
                   class="w-full px-4 py-3 bg-slate-50 border-2 border-slate-200 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all" />
          </div>
        </div>
        
        <!-- Modal 푸터 -->
        <div class="flex gap-3 p-6 bg-slate-50 border-t border-slate-100">
          <button @click="showCreateModal = false"
                  class="flex-1 py-3 border-2 border-slate-200 text-slate-600 rounded-xl font-bold hover:bg-slate-100 transition-all">
            취소
          </button>
          <button @click="createMission"
                  class="flex-1 py-3 bg-gradient-to-r from-emerald-500 to-teal-500 text-white rounded-xl font-bold hover:from-emerald-600 hover:to-teal-600 shadow-lg shadow-emerald-500/25 transition-all">
            ✨ 미션 생성
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const loading = ref(true);
const missions = ref([]);
const studyId = ref(null);
const currentUserId = ref(null);
const showCreateModal = ref(false);

const newMission = ref({
  week: 1,
  title: '',
  deadline: ''
});
const problemIdsInput = ref('');

onMounted(async () => {
  try {
    const userRes = await axios.get('/api/users/me');
    studyId.value = userRes.data.studyId;
    currentUserId.value = userRes.data.id;
    
    if (studyId.value) {
      await loadMissions();
    }
  } catch (e) {
    console.error('미션 로드 실패', e);
  } finally {
    loading.value = false;
  }
});

const loadMissions = async () => {
  const res = await axios.get(`/api/studies/${studyId.value}/missions`);
  missions.value = res.data;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('ko-KR');
};

const createMission = async () => {
  try {
    const problemIds = problemIdsInput.value
      .split(',')
      .map(s => parseInt(s.trim()))
      .filter(n => !isNaN(n));
    
    await axios.post(`/api/studies/${studyId.value}/missions`, {
      week: newMission.value.week,
      title: newMission.value.title,
      problemIds,
      deadline: newMission.value.deadline
    });
    
    showCreateModal.value = false;
    newMission.value = { week: 1, title: '', deadline: '' };
    problemIdsInput.value = '';
    await loadMissions();
  } catch (e) {
    console.error('미션 생성 실패', e);
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>
