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
        <button v-if="isLeader" @click="showCreateModal = true"
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
             @click="openDetailDrawer(mission)"
             class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-2xl p-6 shadow-lg hover:shadow-xl transition-all cursor-pointer group active:scale-[0.99]"
             :class="{ 'opacity-70 grayscale bg-slate-50': mission.status === 'COMPLETED' }">
            <div class="flex flex-wrap items-start justify-between gap-4 mb-4">
            <div>
              <div class="flex items-center gap-3 mb-2">
                <span class="px-3 py-1 font-bold rounded-lg text-sm"
                      :class="mission.status === 'COMPLETED' ? 'bg-slate-200 text-slate-500' : 'bg-emerald-100 text-emerald-700'">
                  Week {{ mission.week }}
                </span>
                <span v-if="mission.status === 'COMPLETED'" class="px-3 py-1 bg-slate-100 text-slate-400 font-bold rounded-lg text-sm border border-slate-200">
                  🚫 종료됨
                </span>
                <span v-else-if="mission.sourceType === 'AI_RECOMMENDED'" 
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
        <button v-if="isLeader" @click="showCreateModal = true"
                class="px-8 py-4 bg-emerald-600 text-white rounded-xl font-bold shadow-lg">
          첫 미션 만들기
        </button>
      </div>

    </div>

    <!-- 미션 생성 Modal -->
    <StudyMissionCreateModal
      :isOpen="showCreateModal"
      :studyId="studyId"
      :missions="missions"
      :initialProblemIds="modalProblemIds"
      :initialTitle="modalTitle"
      :preSelectedMissionId="preSelectedMissionId"
      @close="closeModal"
      @refresh="loadMissions"
    />

    <!-- 미션 상세 Drawer -->
    <StudyMissionDetailDrawer
      :isOpen="showDetailDrawer"
      :mission="selectedMission"
      :studyId="studyId"
      :isLeader="isLeader"
      :currentUserId="currentUserId"
      @close="closeDetailDrawer"
      @refresh="loadMissions"
      @open-add-modal="openAddModalForMission"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import StudyMissionCreateModal from '@/components/StudyMissionCreateModal.vue';
import StudyMissionDetailDrawer from '@/components/StudyMissionDetailDrawer.vue';

const route = useRoute();
const router = useRouter(); // 쿼리 제거용

const loading = ref(true);
const missions = ref([]);
const studyId = ref(null);
const currentUserId = ref(null);
const showCreateModal = ref(false);
const isLeader = ref(false);

const modalProblemIds = ref('');
const modalTitle = ref('');
const preSelectedMissionId = ref(null);

// Drawer 상태
const showDetailDrawer = ref(false);
const selectedMission = ref(null);

onMounted(async () => {
  try {
    const userRes = await axios.get('/api/users/me');
    studyId.value = userRes.data.studyId;
    currentUserId.value = userRes.data.id;
    isLeader.value = userRes.data.isStudyLeader || false;
    
    if (studyId.value) {
      await loadMissions();
      
      // 쿼리 파라미터로 전달된 문제 정보가 있으면 모달 자동 열기
      if (route.query.problemIds && isLeader.value) {
        modalProblemIds.value = route.query.problemIds;
        modalTitle.value = route.query.title || '';
        showCreateModal.value = true;
        
        // 쿼리 파라미터 제거 (지저분하므로)
        router.replace({ query: null });
      }
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
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}. ${month}. ${day}.`;
};

const closeModal = () => {
  showCreateModal.value = false;
  // 모달 닫을 때 데이터 초기화
  modalProblemIds.value = '';
  modalTitle.value = '';
  preSelectedMissionId.value = null;
};

const openDetailDrawer = (mission) => {
  selectedMission.value = mission;
  showDetailDrawer.value = true;
};

const closeDetailDrawer = () => {
  showDetailDrawer.value = false;
  selectedMission.value = null;
};

const openAddModalForMission = (missionId) => {
  preSelectedMissionId.value = missionId;
  showCreateModal.value = true;
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>

