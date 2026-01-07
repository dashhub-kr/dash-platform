<template>
  <div class="min-h-screen bg-white font-sans pb-20">
    
    <!-- 관전 모드 배너 -->
    <div v-if="isObserving" class="bg-slate-900 text-white px-4 py-3 flex items-center justify-between sticky top-0 z-50 shadow-md">
        <div class="flex items-center gap-2 font-bold">
            <div class="w-2 h-2 rounded-full bg-red-500 animate-pulse"></div>
            <span>관리자 관전 모드</span>
            <span class="text-slate-400 text-sm font-normal mx-2">|</span>
            <span class="text-brand-300">{{ studyData?.name || 'Loading...' }}</span>
        </div>
        <button @click="exitObservation" class="text-xs bg-white/10 hover:bg-white/20 px-3 py-1.5 rounded-lg transition-colors font-bold flex items-center gap-1">
            <X :size="14" /> 나가기
        </button>
    </div>

    <!-- 2열 레이아웃을 위한 중앙 컨테이너 -->
    <div class="flex justify-center p-4 md:p-8">
        <div class="flex gap-8 max-w-screen-xl w-full">
            
            <!-- 왼쪽 칼럼: 메인 콘텐츠 (미션) -->
            <main class="flex-1 min-w-0">
                <!-- 헤더 섹션 -->
                <div class="flex items-center justify-between mb-8">
                    <h1 class="text-2xl font-black text-slate-800 tracking-tight flex items-center gap-2">
                        <MapIcon :size="28" class="text-brand-500 fill-brand-500" />
                        미션
                    </h1>
                    
                    <button v-if="isLeader" @click="openCreateModal"
                            class="px-5 py-2.5 bg-brand-500 hover:bg-brand-600 text-white rounded-xl font-bold text-sm transition-all flex items-center gap-2 shadow-sm hover:translate-y-0.5 active:translate-y-1">
                    <Plus :size="18" stroke-width="3" />
                    <span>새 미션</span>
                    </button>
                </div>

                <!-- 로딩 상태 -->
                <div v-if="loading" class="flex flex-col items-center justify-center py-20 space-y-4">
                    <div class="w-12 h-12 border-4 border-slate-200 border-t-brand rounded-full animate-spin"></div>
                    <p class="text-slate-400 font-bold animate-pulse">로딩 중입니다...</p>
                </div>

                <!-- 빈 상태 -->
                <div v-else-if="missions.length === 0" class="flex flex-col items-center justify-center py-20 text-center bg-white rounded-3xl border border-dashed border-slate-200">
                        <div class="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mb-6 text-slate-300">
                            <Inbox :size="48" stroke-width="1.5" />
                        </div>
                        <h3 class="text-xl font-bold text-slate-700 mb-2">등록된 미션이 없어요</h3>
                        <p class="text-slate-400 max-w-sm mx-auto mb-8 font-medium">스터디원들과 함께 풀고 싶은 문제가 있다면<br/>첫 번째 미션을 만들어보세요!</p>
                        <button v-if="isLeader" @click="showCreateModal = true"
                            class="px-8 py-4 bg-brand-500 hover:bg-brand-600 text-white rounded-2xl font-bold text-lg shadow-xl shadow-brand-200 transition-all hover:-translate-y-1">
                        첫 미션 시작하기
                        </button>
                </div>

                <!-- 미션 목록 -->
                <div v-else class="space-y-6">
                    <div v-for="mission in missions" :key="mission.id"
                        class="bg-white rounded-3xl p-6 md:p-8 shadow-sm transition-all hover:bg-slate-50 border border-slate-200 group relative overflow-hidden"
                        :class="{ 'opacity-70 grayscale': mission.status === 'COMPLETED' }"
                    >
                        <!-- 상태 뱃지 (절대 위치) -->
                        <div v-if="mission.status === 'COMPLETED'" class="absolute -right-12 top-8 bg-slate-100 text-slate-400 font-black text-xs py-1 w-40 text-center rotate-45 shadow-sm border border-slate-200">
                            COMPLETED
                        </div>

                            <!-- 절대 위치: 리더 액션 버튼 (카드 우측 상단) -->
                            <div v-if="isLeader && mission.status !== 'COMPLETED'" class="absolute top-6 right-6 flex items-center gap-1 z-10 bg-white/80 backdrop-blur-sm p-1 rounded-xl border border-slate-100 shadow-sm opacity-0 group-hover:opacity-100 transition-all duration-300 translate-y-2 group-hover:translate-y-0">
                                <button 
                                    @click.stop="openEditModal(mission)"
                                    class="p-2 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded-lg transition-colors"
                                    title="미션 수정">
                                    <Pencil class="w-4 h-4" />
                                </button>
                                <button 
                                    @click.stop="confirmEndMission(mission.id)"
                                    class="p-2 text-slate-400 hover:text-orange-500 hover:bg-orange-50 rounded-lg transition-colors"
                                    title="미션 종료">
                                    <CheckCircle2 class="w-4 h-4" />
                                </button>
                            </div>
                            <div v-else-if="isLeader && mission.status === 'COMPLETED'" class="absolute top-6 right-6 z-10 opacity-0 group-hover:opacity-100 transition-opacity">
                                <button 
                                    @click.stop="confirmDeleteMission(mission.id)"
                                    class="flex items-center gap-1 px-3 py-1.5 bg-red-50 text-red-500 hover:bg-red-100 rounded-lg text-xs font-bold transition-colors"
                                    title="미션 삭제">
                                    <Trash2 class="w-3.5 h-3.5" />
                                    <span>기록 삭제</span>
                                </button>
                            </div>

                        <div class="flex flex-col md:flex-row gap-6">
                            <!-- 왼쪽: 정보 -->
                            <div class="flex-1 flex flex-col">
                                <div class="flex items-center gap-2 mb-2">
                                    <!-- 주차 뱃지 -->
                                    <span class="px-2.5 py-1 rounded-lg bg-slate-100 text-slate-600 text-xs font-extrabold uppercase tracking-wide">
                                        Week {{ mission.week }}
                                    </span>
                                    <!-- AI 뱃지 -->
                                    <span v-if="mission.sourceType === 'AI_RECOMMENDED'" class="px-2.5 py-1 rounded-lg bg-indigo-50 text-indigo-600 text-xs font-bold flex items-center gap-1">
                                        <Brain :size="12" /> AI 추천
                                    </span>
                                </div>

                                <h3 class="text-2xl font-black text-slate-800 mb-3 leading-tight group-hover:text-brand-600 transition-colors">
                                    {{ mission.title }}
                                </h3>
                                
                                <div class="flex items-center gap-3 mb-6">
                                    <!-- 마감일 -->
                                    <div class="flex items-center gap-1.5 text-slate-500 text-xs font-bold bg-slate-50 px-3 py-1.5 rounded-full border border-slate-100">
                                        <Calendar :size="14" />
                                        <span>{{ formatDate(mission.deadline) }} 마감</span>
                                    </div>
                                    
                                    <!-- D-Day 뱃지 -->
                                    <div v-if="getDaysLeft(mission.deadline) >= 0" 
                                         class="flex items-center gap-1 text-xs font-bold px-2.5 py-1.5 rounded-full border"
                                         :class="getDaysLeft(mission.deadline) <= 3 
                                            ? 'bg-orange-50 text-orange-600 border-orange-100 animate-pulse' 
                                            : 'bg-emerald-50 text-emerald-600 border-emerald-100'">
                                        <Flame :size="12" fill="currentColor" />
                                        <span>{{ getDaysLeft(mission.deadline) === 0 ? '오늘 마감' : `D-${getDaysLeft(mission.deadline)}` }}</span>
                                    </div>
                                    <span v-else class="text-slate-400 text-xs font-bold bg-slate-100 px-2.5 py-1.5 rounded-full">
                                        종료됨
                                    </span>
                                </div>

                                <!-- 문제 칩 -->
                                <div class="flex flex-wrap gap-2 mt-auto">
                                    <a v-for="(problemId, idx) in mission.problemIds" :key="problemId"
                                        :href="`https://www.acmicpc.net/problem/${problemId}`" target="_blank"
                                        class="px-3 py-1.5 rounded-xl font-bold text-sm transition-all flex items-center gap-2 hover:-translate-y-0.5"
                                        :class="idx < mission.solvedCount 
                                            ? 'bg-emerald-500 text-white shadow-md shadow-emerald-200' 
                                            : 'bg-white text-slate-500 border border-slate-200 hover:border-brand-300 hover:text-brand-600'"
                                    >
                                        <span class="font-mono">{{ problemId }}</span>
                                        <Check v-if="idx < mission.solvedCount" :size="14" stroke-width="3" />
                                    </a>
                                </div>
                            </div>

                            <!-- 오른쪽: 레이스 트랙 -->
                            <div class="w-full md:w-1/3 bg-slate-50 rounded-2xl p-4 self-stretch flex flex-col justify-center border border-slate-100">
                                <div class="flex justify-between items-center mb-4">
                                    <span class="text-xs font-black text-slate-400 uppercase tracking-widest">Team Race</span>
                                    <span class="text-xs font-bold text-slate-500 bg-white px-2 py-1 rounded-lg shadow-sm border border-slate-100">
                                        {{ Math.round((mission.memberProgressList?.reduce((acc, cur) => acc + cur.completedCount, 0) / (Math.max(mission.memberProgressList?.length * Math.max(mission.totalProblems, 1), 1))) * 100) }}% 달성
                                    </span>
                                </div>

                                <div class="relative h-3 bg-slate-200 rounded-full mb-8 mx-2 overflow-visible">
                                    <!-- 진행률 채움 바 -->
                                    <div class="absolute top-0 left-0 h-full bg-brand-200 rounded-full transition-all duration-500 ease-out"
                                         :style="{ width: `${Math.round((mission.memberProgressList?.reduce((acc, cur) => acc + cur.completedCount, 0) / (Math.max(mission.memberProgressList?.length * Math.max(mission.totalProblems, 1), 1))) * 100)}%` }">
                                    </div>
                                    <!-- 참여자 (러너) -->
                                    <template v-for="(members, progress) in getGroupedMembers(mission.memberProgressList)" :key="progress">
                                        <div 
                                            class="absolute top-1/2 -translate-y-1/2 group/runner-group z-10 w-8 h-8"
                                            :style="{ left: `calc(${progress}% - 14px)` }"
                                        >
                                            <div v-for="(member, idx) in getSortedMembersForGroup(members)" 
                                                :key="member.userId"
                                                class="absolute top-0 left-0 transition-all duration-300 ease-out"
                                                :style="{ 
                                                    zIndex: member.userId === currentUserId ? 50 : (40 - idx),
                                                    transform: `translateX(${idx * 12}px)`
                                                }">
                                                
                                                <!-- 아바타 컨테이너 -->
                                                <div class="relative transition-transform duration-300 transform group-hover/runner-group:scale-110 group/avatar"
                                                    :class="idx > 0 ? 'group-hover/runner-group:opacity-80' : ''">
                                                    
                                                    <NicknameRenderer 
                                                        :username="member.username"
                                                        :avatar-url="member.avatarUrl"
                                                        avatar-class="w-8 h-8 flex-shrink-0 border-2 border-white shadow-sm bg-white"
                                                        :class="[member.userId === currentUserId ? 'ring-2 ring-brand/30' : '', 'rounded-full']"
                                                        text-class="hidden"
                                                        :icon-size="16"
                                                    />

                                                    <!-- 완료 깃발 -->
                                                    <div v-if="member.completedCount === member.totalProblems" class="absolute -top-3 -right-2 text-rose-500 drop-shadow-md animate-bounce z-50">
                                                        <Flag :size="16" fill="currentColor" />
                                                    </div>

                                                    <!-- 툴팁 -->
                                                    <div class="absolute left-1/2 -translate-x-1/2 -top-8 bg-slate-800 text-white text-[10px] font-bold px-2 py-1 rounded-lg opacity-0 group-hover/avatar:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-[60]">
                                                        {{ member.username }}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </template>
                                </div>
                                
                                <!-- 범례 -->
                                <div class="flex flex-wrap gap-2 mt-auto pt-2">
                                    <div v-for="member in mission.memberProgressList?.slice(0, 3)" :key="'legend-'+member.userId" 
                                        class="flex items-center gap-1.5 text-[10px] font-bold text-slate-400 bg-white px-2 py-1 rounded-lg border border-slate-100">
                                        <div class="w-1.5 h-1.5 rounded-full" :class="member.completedCount === member.totalProblems ? 'bg-emerald-500' : 'bg-slate-300'"></div>
                                        <NicknameRenderer 
                                            :username="member.username"
                                            :show-avatar="false"
                                            text-class="text-[10px]"
                                        />
                                    </div>
                                    <div v-if="mission.memberProgressList?.length > 3" class="text-[10px] font-bold text-slate-300 px-1 py-1">
                                        +{{ mission.memberProgressList.length - 3 }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            <!-- 오른쪽 칼럼: 통계 및 활동 -->
            <aside class="hidden xl:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]">
                
                <!-- 1. 통계 열 -->
                <div class="bg-white rounded-2xl p-4 border border-slate-200 shadow-sm flex items-center justify-around">
                     <!-- 통계 1: 도토리 -->
                     <div class="flex items-center gap-2 group cursor-pointer" title="Acorns">
                        <IconAcorn class="text-fox w-8 h-8" stroke-width="2.5" fill="currentColor" />
                        <span class="text-xl font-black text-slate-700">{{ studyData?.acornCount || 0 }}</span>
                    </div>

                    <div class="w-px h-8 bg-slate-100"></div>

                    <!-- 통계 2: 스트릭 -->
                    <div class="flex items-center gap-2 group cursor-pointer" title="Streak">
                        <Flame class="w-7 h-7" :class="currentStreak > 0 ? 'text-rose-500 fill-rose-500 animate-pulse' : 'text-slate-300'" stroke-width="2.5" />
                        <span class="text-xl font-black text-slate-700">{{ currentStreak }}</span>
                    </div>
                    
                    <div class="w-px h-8 bg-slate-100"></div>

                    <!-- Stat 3: Submissions -->
                     <div class="flex items-center gap-2 group cursor-pointer" title="Submissions">
                        <Send class="w-6 h-6 text-sky-400 fill-sky-400" stroke-width="2.5" />
                        <span class="text-xl font-black text-slate-700">{{ recordCount }}</span>
                    </div>
                </div>

                <!-- 2. Activity Log -->
                <div v-if="!loading && heatmapWeeks.length > 0" class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                    <h3 class="font-black text-slate-700 text-sm mb-4">스터디 활동 로그</h3>
                    
                    <!-- 히트맵 시각화 -->
                    <div class="">
                       <!-- 스크롤 래퍼 -->
                       <div ref="heatmapScrollRef" class="overflow-x-auto pb-2 custom-scrollbar no-scrollbar" style="direction: rtl;">
                           <div class="flex gap-[3px] justify-end min-w-max" style="direction: ltr;">
                               <div v-for="(week, wIdx) in heatmapWeeks" :key="wIdx" class="flex flex-col gap-[3px]">
                                   <div v-for="(day, dIdx) in week" :key="dIdx"
                                        class="w-2.5 h-2.5 rounded-[2px] transition-all relative"
                                        :class="day.colorClass"
                                        :title="`${day.dateFormatted}: ${day.count} solved`">
                                   </div>
                               </div>
                           </div>
                       </div>
                    </div>
                </div>
                <!-- 3. 동적 미션 상세 (사이드바) -->
                <!-- 현재 활성 미션 고정 -->
                <StudyMissionSidebarDetail 
                    v-if="selectedMission"
                    :mission="selectedMission"
                    :currentUserId="currentUserId"
                    :studyId="studyId"
                    :isLeader="isLeader"
                    @refresh="loadMissions"
                    @open-add-modal="openAddModalForMission"
                />
            </aside>

        </div>
    </div>

    <!-- 모달 -->
    <StudyMissionCreateModal
      :isOpen="showCreateModal"
      :studyId="studyId"
      :missions="missions"
      :initialProblemIds="modalProblemIds"
      :initialTitle="modalTitle"
      :preSelectedMissionId="preSelectedMissionId"
      :forceNew="forceNewMission"
      :isEditMode="isEditMode"
      :missionId="editingMissionId"
      :initialDeadline="modalDeadline"
      :initialWeek="modalWeek"
      @close="closeModal"
      @refresh="loadMissions"
    />

  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import StudyMissionCreateModal from '@/components/study/StudyMissionCreateModal.vue';
import StudyMissionSidebarDetail from '@/components/study/StudyMissionSidebarDetail.vue';
import BaseIconBadge from '@/components/common/BaseIconBadge.vue';
import IconAcorn from '@/components/icons/IconAcorn.vue';
import { dashboardApi } from '@/api/dashboard';
import { studyApi } from '@/api/study';
import { 
    Plus, 
    Inbox, 
    Calendar,
    Brain,
    Flame,
    Flag,
    Map as MapIcon,
    Send,
    Pencil,
    Trash2,
    CheckCircle2,
    Check,
    X
} from 'lucide-vue-next';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';

// 관리자 관전 모드 Props
const props = defineProps({
    studyId: {
        type: Number,
        default: null
    }
});

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const missions = ref([]);
const studyId = ref(null);
const currentUserId = ref(null);
const showCreateModal = ref(false);
const isLeader = ref(false);

const isObserving = computed(() => !!props.studyId);

const exitObservation = () => {
    router.push('/study/ranking');
};

// 통계 데이터
const studyData = ref(null);
const recordCount = ref(0);
const heatmapWeeks = ref([]);
const heatmapScrollRef = ref(null);

// ... (existing refs)
const modalProblemIds = ref('');
const modalTitle = ref('');
const preSelectedMissionId = ref(null);
const forceNewMission = ref(false);

const isEditMode = ref(false);
const editingMissionId = ref(null);
const modalDeadline = ref('');
const modalWeek = ref(null);

// 사이드바 선택 상태
const selectedMission = ref(null);

// ... (existing helper methods: processHeatmap, currentStreak) 
// (For brevity, assuming they are unchanged, but I must provide full replacement chunk if they are inside the block)
// I will just replace the setup block start up to onMounted to insert my logic.

// ... (Helper methods omitted from this block, assuming they are below)

const processHeatmap = (data) => {
    try {
        const activityMap = new Map();
        (data || []).forEach(item => {
             if(item?.date) activityMap.set(item.date, item);
        });

        const weeks = [];
        const today = new Date();
        const end = new Date(today);
        const start = new Date(end);
        start.setDate(start.getDate() - (52 * 7) + 1);

        let current = new Date(start);
        let currentWeek = [];

        for (let i = 0; i < 52 * 7; i++) {
             const dateStr = current.toISOString().split('T')[0];
             const activity = activityMap.get(dateStr);
             
             // --- 참여 로직 시작 ---
             const totalMembers = studyData.value?.memberCount || 1;
             const contributors = Array.isArray(activity?.contributors) ? activity.contributors : [];
             const activeCount = contributors.length;
             const totalSolvedCount = activity?.count || 0;
             
             let participationRate = 0;
             if (totalMembers > 0) {
                 participationRate = activeCount / totalMembers;
             }
             
             let colorClass = 'bg-slate-100';
             if (participationRate > 0) colorClass = 'bg-brand-200';
             if (participationRate >= 0.25) colorClass = 'bg-brand-400';
             if (participationRate >= 0.50) colorClass = 'bg-brand-500';
             if (participationRate >= 0.75) colorClass = 'bg-brand-600';
             if (participationRate >= 1.0) colorClass = 'bg-brand-800';
             // --- 참여 로직 끝 ---

             currentWeek.push({
                 date: dateStr,
                 dateFormatted: current.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }),
                 count: totalSolvedCount,
                 colorClass: colorClass
             });

             if (currentWeek.length === 7) {
                 weeks.push(currentWeek);
                 currentWeek = [];
             }
             current.setDate(current.getDate() + 1);
        }
        if (currentWeek.length > 0) weeks.push(currentWeek);
        heatmapWeeks.value = weeks;
    } catch(err) {
        console.error("Heatmap Gen Error", err);
    }
};

const currentStreak = computed(() => {
    if (!heatmapWeeks.value.length) return 0;
    
    // 주간 데이터를 날짜 배열로 평탄화하고 오늘부터 시작하도록 역순 정렬
    const allDays = heatmapWeeks.value.flat().reverse();
    
    let streak = 0;
    const today = new Date().toISOString().split('T')[0];
    
    for (const day of allDays) {
        // 미래 날짜 건너뛰기
        if (day.date > today) continue;
        
        if (day.count > 0) {
            streak++;
        } else if (streak > 0 || day.date === today) {
            // 오늘 활동이 없으면 스트릭 0
            // 이전에 활동이 있었는데 0을 만나면 카운팅 중단
            break;
        }
    }
    
    return streak;
});


onMounted(async () => {
  try {
    const userRes = await axios.get('/api/users/me');
    
    // 관리자 관전 모드인 경우 prop을 사용. 
    // 그렇지 않으면 userRes의 studyId를 사용 (기존 로직 유지)
    if (props.studyId) {
        studyId.value = props.studyId;
    } else {
        studyId.value = userRes.data.studyId;
    }
    
    // currentUserId 명시적 업데이트
    currentUserId.value = userRes.data.id;
    isLeader.value = userRes.data.isStudyLeader || false;
    
    // 관전 모드일 경우 리더 권한 시각적으로 제거 (백엔드 보안 처리됨, UI용)
    if (isObserving.value) {
        isLeader.value = false; 
    }
    
    if (studyId.value) {
      await loadMissions();

      // 통계 가져오기
      const statsRes = await studyApi.get(studyId.value);
      studyData.value = statsRes.data;

      // 관전 모드 시 해당 스터디의 기록을 조회해야 함
      // dashboardApi.getRecords에 studyId를 전달하도록 수정됨
      const recordsRes = await dashboardApi.getRecords(studyId.value);
      recordCount.value = recordsRes.data?.length || 0;

      // 히트맵 처리
      try {
        const heatmapRes = await dashboardApi.getHeatmap(studyId.value);
        processHeatmap(heatmapRes.data || []);
      } catch (e) {
        console.error('Heatmap Load Error:', e);
        processHeatmap([]);
      }

      nextTick(() => {
          if (heatmapScrollRef.value) {
            heatmapScrollRef.value.scrollLeft = heatmapScrollRef.value.scrollWidth;
          }
      });

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
    
    // 사이드바에 고정할 최신 활성 미션(완료되지 않은) 찾기
    // 미션이 주차별 내림차순(또는 ID 내림차순)으로 정렬되어 있다고 가정
    const activeMission = missions.value.find(m => m.status !== 'COMPLETED');
    // 활성 미션이 없으면(모두 완료됨), 최신 미션을 표시하거나 null 처리
    // 사용자는 "현재 진행 중인 미션"을 원하므로, 모두 완료된 경우 일단 최신을 기본값으로 하되, 활성을 우선함.
    selectedMission.value = activeMission || missions.value[0] || null;

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


const openCreateModal = () => {
    forceNewMission.value = true;
    isEditMode.value = false;
    modalProblemIds.value = '';
    modalTitle.value = '';
    modalDeadline.value = '';
    modalWeek.value = null;
    preSelectedMissionId.value = null;
    showCreateModal.value = true;
};

const openEditModal = (mission) => {
    isEditMode.value = true;
    // 백엔드는 PATCH 요청에 missionId가 필요함
    editingMissionId.value = mission.id;
    
    // 초기값 채우기
    modalTitle.value = mission.title;
    modalDeadline.value = mission.deadline;
    modalWeek.value = mission.week;
    
    // 문제 번호 배열을 콤마로 구분된 문자열로 변환하여 전달
    if (mission.problemIds && mission.problemIds.length > 0) {
        modalProblemIds.value = mission.problemIds.join(', ');
    } else {
        modalProblemIds.value = '';
    }
    
    showCreateModal.value = true;
};

const confirmDeleteMission = async (missionId) => {
    if (!confirm('정말로 이 미션을 삭제하시겠습니까? 삭제된 미션은 복구할 수 없으며, 팀원들의 제출 기록도 함께 삭제됩니다.')) {
        return;
    }
    
    try {
        await axios.delete(`/api/studies/${studyId.value}/missions/${missionId}`);
        await loadMissions();
        if (selectedMission.value?.id === missionId) selectedMission.value = null;
    } catch (e) {
        console.error('미션 삭제 실패', e);
        alert('미션 삭제 중 오류가 발생했습니다.');
    }
};

const confirmEndMission = async (missionId) => {
    if (!confirm('미션을 종료하시겠습니까? 종료된 미션은 더 이상 제출할 수 없습니다.')) {
        return;
    }

    try {
        await axios.patch(`/api/studies/${studyId.value}/missions/${missionId}/status`, {
            status: 'COMPLETED'
        });
        await loadMissions();
    } catch (e) {
        console.error('미션 종료 실패', e);
        alert('미션 종료 중 오류가 발생했습니다.');
    }
};

const closeModal = () => {
  showCreateModal.value = false;
  modalProblemIds.value = '';
  modalTitle.value = '';
  preSelectedMissionId.value = null;
  forceNewMission.value = false;
};

// 드로어 여는 대신 사이드바용 미션 선택
const openDetailDrawer = (mission) => {
  selectedMission.value = mission;
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
.no-scrollbar::-webkit-scrollbar {
    display: none;
}
.no-scrollbar {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>
