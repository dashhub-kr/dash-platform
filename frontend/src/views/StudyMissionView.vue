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

          <!-- 팀원별 진행 레이스 (New Shared Gauge) -->
          <div class="mt-6 pt-6 border-t border-slate-100">
            <h4 class="text-sm font-bold text-slate-500 mb-4 flex items-center gap-2">
              <span>🏃 팀원 진행 현황</span>
            </h4>
            
            <div class="relative h-14 w-full mt-8 mb-2">
                <!-- Progress Track -->
                <div class="absolute top-[28px] left-0 right-0 h-3 bg-slate-100 rounded-full overflow-hidden shadow-inner">
                    <div class="w-full h-full opacity-30 bg-[linear-gradient(90deg,transparent_99%,rgba(0,0,0,0.05)_100%)] bg-[length:20%_100%]"></div>
                </div>
                
                <!-- Goal Flag (Right End) -->
                <div class="absolute top-[18px] -right-2 z-10 bg-white p-1.5 rounded-full border border-slate-200 shadow-sm">
                    <Trophy class="w-4 h-4 text-yellow-500" />
                </div>

                <!-- Member Markers -->
                <div v-for="member in sortMembers(mission.memberProgressList)" :key="member.userId" 
                     class="absolute top-0 transform -translate-x-1/2 transition-all duration-700 ease-out z-20 group"
                     :style="{ left: getMemberProgressPercent(member, mission) + '%' }"
                     :class="{'z-30': isMe(member.userId)}">
                     
                     <div class="flex flex-col items-center cursor-help">
                        <!-- Avatar -->
                        <div class="relative">
                            <img :src="getMemberProfileImage(member)" :alt="member.username"
                                 class="w-8 h-8 rounded-full object-cover border-2 shadow-md transition-transform hover:scale-110 bg-white"
                                 :class="[
                                   isMe(member.userId) 
                                     ? 'border-emerald-500 ring-2 ring-emerald-500/30' 
                                     : member.allCompleted 
                                       ? 'border-orange-500' 
                                       : 'border-slate-200 grayscale-[0.3]'
                                 ]" />
                             
                             <!-- Completed Badge -->
                             <div v-if="member.allCompleted" class="absolute -top-1 -right-1 w-3.5 h-3.5 bg-orange-500 rounded-full flex items-center justify-center border border-white shadow-sm animate-bounce">
                                 <Check class="w-2 h-2 text-white" stroke-width="4" />
                             </div>
                        </div>

                        <!-- Arrow -->
                        <div class="w-0 h-0 border-l-[5px] border-l-transparent border-r-[5px] border-r-transparent border-t-[6px] transition-colors mt-0.5 drop-shadow-sm"
                             :class="isMe(member.userId) ? 'border-t-emerald-500' : 'border-t-slate-300'"></div>

                        <!-- Tooltip -->
                        <div class="absolute bottom-full mb-1 px-2 py-1 bg-slate-800 text-white text-[10px] rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none shadow-xl flex flex-col items-center gap-0.5 z-50">
                            <span class="font-bold text-indigo-200">{{ member.username }} {{ isMe(member.userId) ? '(나)' : '' }}</span>
                            <span class="font-mono">{{ member.completedCount }} / {{ mission.problemIds.length }}</span>
                            <!-- Tooltip Arrow -->
                            <div class="absolute -bottom-1 left-1/2 -translate-x-1/2 w-2 h-2 bg-slate-800 rotate-45"></div>
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
import { Trophy, Check, Flame } from 'lucide-vue-next';

const profileImages = [
    '/profile/bag.png',
    '/profile/proud.png',
    '/profile/smart.png',
    '/profile/smile.png'
];

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

// --- Helper Functions for Mission Gauge ---
const isMe = (userId) => currentUserId.value === userId;

const getMemberProfileImage = (member) => {
    if (member.avatarUrl) return member.avatarUrl;
    const id = member.userId || 0;
    const index = id % profileImages.length;
    return profileImages[index];
};

const getMemberProgressPercent = (member, mission) => {
    if (!mission || !mission.problemIds || mission.problemIds.length === 0) return 0;
    const total = mission.problemIds.length;
    const completed = member.completedCount || 0;
    
    if (member.allCompleted) return 100;
    return Math.min(100, Math.max(0, (completed / total) * 100));
};

const sortMembers = (members) => {
    if (!members) return [];
    return [...members].sort((a, b) => {
        if (a.userId === currentUserId.value) return -1;
        if (b.userId === currentUserId.value) return 1;
        return a.username.localeCompare(b.username);
    });
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>

