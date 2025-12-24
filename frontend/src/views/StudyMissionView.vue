<template>
  <div class="min-h-screen bg-white text-slate-700 font-sans pb-20">
    
    <div class="max-w-4xl mx-auto px-6 py-10">
      
      <!-- Header Section -->
      <div class="flex items-center justify-between mb-8">
        <h1 class="text-2xl font-black text-slate-800 tracking-tight">
          미션 로드맵
        </h1>
        
        <button v-if="isLeader" @click="showCreateModal = true"
                class="px-5 py-2.5 bg-brand-500 hover:bg-brand-600 text-white rounded-xl font-bold text-sm transition-all flex items-center gap-2 shadow-sm hover:translate-y-0.5 active:translate-y-1">
          <plus-icon class="w-4 h-4" stroke-width="3" />
          <span>새 미션</span>
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20 space-y-4">
        <div class="w-12 h-12 border-4 border-slate-200 border-t-brand rounded-full animate-spin"></div>
        <p class="text-slate-400 font-bold animate-pulse">로딩 중입니다...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="missions.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-24 h-24 bg-slate-100 rounded-full flex items-center justify-center mb-6 text-slate-400">
                <Inbox :size="48" stroke-width="1.5" />
            </div>
            <h3 class="text-xl font-bold text-slate-700 mb-2">등록된 미션이 없어요</h3>
            <p class="text-slate-400 max-w-sm mx-auto mb-8">스터디원들과 함께 풀고 싶은 문제가 있다면 첫 번째 미션을 만들어보세요!</p>
            <button v-if="isLeader" @click="showCreateModal = true"
                class="px-8 py-4 bg-brand-500 hover:bg-brand-600 text-white rounded-2xl font-bold text-lg shadow-xl shadow-brand-200 transition-all hover:-translate-y-1">
            첫 미션 시작하기
            </button>
      </div>

      <!-- Missions List -->
      <div v-else class="space-y-8">
        <div v-for="mission in missions" :key="mission.id"
             @click="openDetailDrawer(mission)"
             class="bg-white rounded-3xl p-6 md:p-8 shadow-sm cursor-pointer transition-all hover:bg-slate-50 group relative overflow-hidden"
             :class="{ 'opacity-70 grayscale': mission.status === 'COMPLETED' }"
        >
             <!-- Status Badge (Absolute) -->
             <div v-if="mission.status === 'COMPLETED'" class="absolute -right-12 top-8 bg-slate-200 text-slate-500 font-black text-xs py-1 w-40 text-center rotate-45 shadow-sm">
                COMPLETED
             </div>

            <div class="flex flex-col md:flex-row gap-6">
                <!-- Left: Info -->
                <div class="flex-1">
                    <div class="flex items-center gap-2 mb-3">
                         <!-- Week Badge -->
                        <BaseIconBadge 
                            :icon="Calendar"
                            :text="`Week ${mission.week}`"
                            :color="mission.status === 'COMPLETED' ? 'slate' : 'yellow'"
                            size="sm"
                        />
                         <!-- AI Badge -->
                         <BaseIconBadge 
                             v-if="mission.sourceType === 'AI_RECOMMENDED'"
                             :icon="Brain"
                             text="AI 추천"
                             color="brand"
                             size="sm"
                        />
                    </div>

                    <h3 class="text-2xl font-black text-slate-700 mb-2 group-hover:text-brand transition-colors">
                        {{ mission.title }}
                    </h3>
                    
                    <div class="flex items-center gap-4 text-slate-400 text-sm font-bold mb-6">
                        <span>{{ formatDate(mission.deadline) }} 까지</span>
                        
                        <!-- D-Day Badge -->
                        <BaseIconBadge 
                            v-if="getDaysLeft(mission.deadline) >= 0"
                            :icon="Flame"
                            :text="getDaysLeft(mission.deadline) === 0 ? '오늘 마감' : `D-${getDaysLeft(mission.deadline)}`"
                            color="orange"
                            size="sm"
                        />
                         <span v-else class="text-slate-300 bg-slate-100 px-2 py-0.5 rounded-lg text-xs font-bold">
                            종료됨
                        </span>
                    </div>

                    <!-- Problem Chips -->
                     <div class="flex flex-wrap gap-2">
                        <div v-for="(problemId, idx) in mission.problemIds" :key="problemId"
                            class="px-3 py-1.5 rounded-xl font-bold text-sm transition-all flex items-center gap-1.5"
                            :class="idx < mission.solvedCount 
                                ? 'bg-leaf/10 text-leaf' 
                                : 'bg-slate-100 text-slate-400'"
                        >
                           <div class="w-2 h-2 rounded-full" :class="idx < mission.solvedCount ? 'bg-leaf' : 'bg-slate-300'"></div>
                           <span>{{ problemId }}</span>
                        </div>
                    </div>
                </div>

                <!-- Right: Race Track -->
                <div class="w-full md:w-1/3 bg-slate-50 rounded-2xl p-4 self-stretch flex flex-col justify-center">
                    <div class="flex justify-between items-center mb-4">
                         <span class="text-xs font-black text-slate-400 uppercase tracking-widest">Team Race</span>
                         <span class="text-xs font-bold text-slate-500 bg-white px-2 py-1 rounded-lg">
                            {{ Math.round((mission.memberProgressList?.reduce((acc, cur) => acc + cur.completedCount, 0) / (mission.memberProgressList?.length * Math.max(mission.totalProblems, 1))) * 100) }}% 달성
                         </span>
                    </div>

                    <div class="relative h-4 bg-slate-200 rounded-full mb-8 mx-2">
                        <!-- Runners -->
                        <div v-for="(members, progress) in getGroupedMembers(mission.memberProgressList)" 
                             :key="progress"
                             class="absolute top-1/2 -translate-y-1/2 group/runner-group z-10 w-8 h-8"
                             :style="{ left: `calc(${progress}% - 14px)` }">

                            <div v-for="(member, idx) in getSortedMembersForGroup(members)" 
                                :key="member.userId"
                                class="absolute top-0 left-0 transition-all duration-300 ease-out"
                                :style="{ 
                                    zIndex: member.userId === currentUserId ? 50 : (40 - idx),
                                    '--tx': `${idx * 18}px`
                                }">
                                
                                <!-- Avatar Container -->
                                <div class="relative transition-transform duration-300 transform group-hover/runner-group:translate-x-[var(--tx)] group/avatar"
                                     :class="idx > 0 ? 'group-hover/runner-group:opacity-80' : ''">
                                    
                                    <img v-if="member.avatarUrl" :src="member.avatarUrl" 
                                         class="w-8 h-8 rounded-full border-2 border-white object-cover shadow-sm bg-white"
                                         :class="member.userId === currentUserId ? 'ring-2 ring-brand/30' : ''" />
                                    <div v-else class="w-8 h-8 rounded-full border-2 border-white bg-slate-300 flex items-center justify-center text-[10px] font-bold text-white uppercase shadow-sm"
                                         :style="{ backgroundColor: stringToColor(member.username) }">
                                        {{ member.username?.substring(0, 2) }}
                                    </div>

                                    <!-- Flag for Completion -->
                                    <div v-if="member.completedCount === member.totalProblems" class="absolute -top-2 -right-1 text-brand drop-shadow-md animate-bounce z-50">
                                        <Flag :size="16" fill="currentColor" />
                                    </div>

                                    <!-- Tooltip -->
                                    <div class="absolute left-1/2 -translate-x-1/2 -top-8 bg-slate-800 text-white text-[10px] font-bold px-2 py-1 rounded-lg opacity-0 group-hover/avatar:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-[60]">
                                        {{ member.username }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Legend -->
                    <div class="flex flex-wrap gap-2 mt-auto">
                        <div v-for="member in mission.memberProgressList?.slice(0, 3)" :key="'legend-'+member.userId" 
                             class="flex items-center gap-1.5 text-[10px] font-bold text-slate-400 bg-white px-2 py-1 rounded-lg border border-slate-100">
                             <div class="w-1.5 h-1.5 rounded-full" :class="member.completedCount === member.totalProblems ? 'bg-leaf' : 'bg-slate-300'"></div>
                             {{ member.username }}
                        </div>
                        <div v-if="mission.memberProgressList?.length > 3" class="text-[10px] font-bold text-slate-300 px-1 py-1">
                            +{{ mission.memberProgressList.length - 3 }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
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
import BaseIconBadge from '@/components/common/BaseIconBadge.vue';
import { Plus as PlusIcon, 
    Inbox, 
    Calendar,
    Brain,
    Flame,
    Flag,
    Info
} from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const missions = ref([]);
const studyId = ref(null);
const currentUserId = ref(null);
const showCreateModal = ref(false);
const isLeader = ref(false);

const modalProblemIds = ref('');
const modalTitle = ref('');
const preSelectedMissionId = ref(null);

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
      if (route.query.problemIds && isLeader.value) {
        modalProblemIds.value = route.query.problemIds;
        modalTitle.value = route.query.title || '';
        showCreateModal.value = true;
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
  try {
    const res = await axios.get(`/api/studies/${studyId.value}/missions`);
    missions.value = res.data;
  } catch (e) {
    console.error("Fetch missions failed", e);
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}/${date.getDate()}`;
};

const getDaysLeft = (deadline) => {
    const today = new Date();
    today.setHours(0,0,0,0);
    const end = new Date(deadline);
    end.setHours(0,0,0,0);
    const diff = end - today;
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
};

const closeModal = () => {
  showCreateModal.value = false;
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

const getGroupedMembers = (memberList) => {
  if (!memberList) return {};
  const groups = {};
  memberList.forEach(m => {
    const total = Math.max(m.totalProblems, 1);
    const progress = Math.min(Math.floor((m.completedCount / total) * 100), 100);
    if (!groups[progress]) groups[progress] = [];
    groups[progress].push(m);
  });
  return groups;
};

const getSortedMembersForGroup = (members) => {
  return [...members].sort((a, b) => {
    if (a.userId === currentUserId.value) return -1;
    if (b.userId === currentUserId.value) return 1;
    return 0;
  });
};

const stringToColor = (str) => {
    if (!str) return '#cbd5e1';
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    const c = (hash & 0x00ffffff).toString(16).toUpperCase();
    return '#' + '00000'.substring(0, 6 - c.length) + c;
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>

