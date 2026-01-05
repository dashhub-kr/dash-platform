<template>
  <!-- 분할 뷰를 위한 Flex 컨테이너 -->
  <div class="flex h-screen overflow-hidden bg-white font-sans">
    
    <!-- 컨텍스트 카드 모드: 드로어가 열려 있을 때 - 선택된 문제만 표시 -->
    <div 
      v-if="showDrawer && currentDrawerRecord"
      class="w-full md:w-[40%] bg-gradient-to-br from-slate-50 to-slate-100 border-r border-slate-200 flex flex-col"
    >
      <div class="flex-1 p-6 flex flex-col">
        <!-- 컨텍스트 헤더 -->
        <div class="mb-6">
          <div class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">분석 중인 문제</div>
          <div class="flex items-center gap-2 mb-3">
            <span class="px-2 py-1 bg-brand-100 text-brand-700 text-xs font-bold rounded-lg">#{{ currentDrawerRecord.problemId }}</span>
            <span class="px-2 py-1 bg-slate-200 text-slate-600 text-xs font-bold rounded-lg">{{ currentDrawerRecord.language }}</span>
            <span v-if="currentDrawerRecord.result === 'FAIL'" class="px-2 py-1 bg-red-100 text-red-600 text-xs font-bold rounded-lg">FAILED</span>
            <span v-else class="px-2 py-1 bg-emerald-100 text-emerald-600 text-xs font-bold rounded-lg">PASSED</span>
          </div>
          <h2 class="text-xl font-black text-slate-800 leading-tight">{{ currentDrawerRecord.problemTitle }}</h2>
          <p class="text-sm text-slate-500 mt-1">{{ formatDate(currentDrawerRecord.createdAt) }}</p>
        </div>
        
        <!-- 성능 지표 그리드 -->
        <div class="grid grid-cols-2 gap-3 mb-6">
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">실행 시간</div>
            <div class="text-lg font-black text-slate-800">{{ currentDrawerRecord.executionTime || '-' }}<span class="text-sm font-normal text-slate-500">ms</span></div>
          </div>
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">메모리</div>
            <div class="text-lg font-black text-slate-800">{{ currentDrawerRecord.memory || '-' }}<span class="text-sm font-normal text-slate-500">MB</span></div>
          </div>
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">시간 복잡도</div>
            <div class="text-lg font-black text-brand-600">{{ currentDrawerRecord.timeComplexity || '-' }}</div>
          </div>
          <div class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm text-center">
            <div class="text-xs font-bold text-slate-400 uppercase mb-1">점수</div>
            <div class="text-lg font-black text-slate-800">{{ currentDrawerRecord.score || '-' }}</div>
          </div>
        </div>
        
        <!-- 코드 미리보기 -->
        <div class="flex-1 bg-white rounded-xl overflow-hidden flex flex-col border border-slate-200">
          <div class="px-4 py-2 bg-slate-50 text-slate-500 text-xs font-mono flex justify-between items-center border-b border-slate-200">
            <span>{{ currentDrawerRecord.language }}.{{ getFileExtension(currentDrawerRecord.language) }}</span>
            <span class="text-slate-400">제출 코드</span>
          </div>
          <div class="flex-1 overflow-y-auto p-4">
            <pre class="text-sm font-mono leading-relaxed"><code class="text-slate-700" v-html="highlightCode(currentDrawerRecord.code?.substring(0, 800) || '// 코드 없음', currentDrawerRecord.language)"></code></pre>
            <div v-if="currentDrawerRecord.code?.length > 800" class="text-center mt-4">
              <span class="text-slate-400 text-xs">... 코드 더 보기는 AI 분석에서</span>
            </div>
          </div>
        </div>
        
        <!-- 닫기 버튼 -->
        <button 
          @click="closeDrawer" 
          class="mt-4 w-full py-3 rounded-xl bg-slate-200 hover:bg-slate-300 text-slate-700 font-bold transition-colors flex items-center justify-center gap-2"
        >
          <X class="w-4 h-4" />
          분석 닫기
        </button>
      </div>
    </div>
    
    <!-- 일반 모드: 드로어가 닫혀 있을 때 전체 대시보드 -->
    <div 
      v-else
      class="w-full overflow-y-auto"
      @click="collapseExpandedCard"
    >
      <div class="min-h-screen bg-white text-slate-800">
        <!-- 네비게이션/헤더 영역 -->
        
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

        <div class="flex-1 flex justify-center p-4 md:p-8">
            <div class="flex gap-8 max-w-screen-xl w-full">
            
            <!-- 2. 중앙 메인 컬럼 -->
            <main class="flex-1 min-w-0 space-y-6">
                <!-- 깔끔한 지표가 있는 헤더 섹션 -->
                <div class="animate-fade-in-down">
                    <!-- 승인 대기 배너 -->
                    <div v-if="isPendingApproval" class="bg-amber-50 border border-amber-200 rounded-2xl p-4 flex items-start gap-3 shadow-sm animate-pulse mb-6">
                        <div class="bg-amber-100 p-2 rounded-lg text-amber-600">
                            <Clock class="w-5 h-5" stroke-width="2.5" />
                        </div>
                        <div>
                            <h3 class="font-bold text-amber-800 text-sm mb-1">
                                <span class="font-black">[{{ pendingStudyName }}]</span> 스터디 승인 대기 중입니다
                            </h3>
                            <p class="text-xs text-amber-700 leading-relaxed font-medium">
                                현재 개인 연구실(Personal Study)을 이용 중이며, 스터디장의 승인을 기다리고 있습니다.<br/>
                                승인이 완료되면 해당 스터디의 대시보드로 자동 전환됩니다.
                            </p>
                        </div>
                    </div>

                    <!-- 주간 미션 섹션 -->
                    <div class="mb-6">
                        <div v-if="targetMission" 
                            class="rounded-3xl p-6 shadow-none relative transition-all duration-500 bg-white border border-slate-200"
                            :class="getMissionThemeClass(targetMission)">
                            
                            <div class="relative z-10 flex flex-col items-start gap-4">
                                <div>
                                    <div class="flex items-center gap-2 mb-2">
                                        <span class="px-2 py-0.5 bg-slate-100 rounded-lg text-xs font-bold uppercase tracking-wider text-slate-400">
                                            #{{ targetMission.week }}
                                        </span>
                                        <span class="flex items-center gap-1 text-xs font-bold text-slate-500">
                                            <Calendar :size="12" />
                                            ~ {{ formatMissionDate(targetMission.deadline) }}
                                        </span>
                                        <span v-if="isMissionUrgent(targetMission) && !isMissionCompleted(targetMission)" class="px-2 py-0.5 bg-rose-100 text-rose-600 rounded text-xs font-bold animate-pulse">
                                            마감 임박
                                        </span>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <h2 class="text-3xl font-black text-slate-800 mb-1 tracking-tight">{{ targetMission.title }}</h2>
                                        <span v-if="isMissionCompleted(targetMission)" class="bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded-lg text-xs font-bold flex items-center gap-1">
                                            <Check :size="12" /> 해결 완료!
                                        </span>
                                    </div>
                                </div>
                                
                                <div class="flex flex-wrap gap-2 w-full">
                                    <a 
                                        v-for="problemId in targetMission.problemIds" 
                                        :key="problemId"
                                        :href="getProblemLink(problemId)" 
                                        target="_blank"
                                        class="flex items-center gap-2 px-6 py-3.5 rounded-2xl font-black text-sm transition-all shadow-none group flex-1 justify-center border-2"
                                        :class="isProblemSolved(problemId) 
                                            ? 'bg-emerald-500 border-emerald-500 text-white hover:bg-emerald-600 hover:border-emerald-600' 
                                            : 'bg-white border-brand-200 text-brand-600 hover:bg-brand-50 hover:border-brand-300 hover:scale-[1.02]'"
                                    >
                                        <span class="flex items-center gap-2">
                                            {{ problemId }}번
                                            <Check v-if="isProblemSolved(problemId)" :size="16" stroke-width="3" />
                                        </span>
                                        <ExternalLink v-if="!isProblemSolved(problemId)" :size="14" class="opacity-50 group-hover:opacity-100" />
                                    </a>
                                </div>
                            </div>

                            <!-- 멤버 진행 상황 섹션 -->
                            <div v-if="targetMission.memberProgressList?.length > 0" class="mt-6 pt-4 border-t border-slate-100 flex flex-wrap items-center gap-4">
                                <div v-for="member in sortMembers(targetMission.memberProgressList)" :key="member.userId" 
                                    class="flex flex-col items-center gap-1 group relative cursor-help">
                                    
                                    <!-- 이름 툴팁 -->
                                    <div class="absolute bottom-full mb-2 px-2 py-1 bg-black/80 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-20">
                                        {{ member.username }} {{ isMe(member.userId) ? '(나)' : '' }}
                                        <div class="absolute bottom-0 left-1/2 -translate-x-1/2 translate-y-1 w-2 h-2 bg-black/80 rotate-45"></div>
                                    </div>

                                    <!-- 아바타 -->
                                    <UserX v-if="member.username === '탈퇴한 회원'" :size="40" class="w-10 h-10 rounded-full border-2 border-slate-200 bg-white p-2 text-slate-400" />
                                    <img v-else :src="getMemberProfileImage(member)" :alt="member.username"
                                        class="w-10 h-10 rounded-full object-cover border-2 transition-all relative z-10 bg-white"
                                        :class="[
                                            isMe(member.userId) 
                                                ? 'border-emerald-400 ring-2 ring-emerald-400/30' + (member.allCompleted ? ' shadow-[0_0_12px_rgba(52,211,153,0.6)]' : '')
                                                : member.allCompleted 
                                                ? 'border-orange-400 shadow-[0_0_12px_rgba(251,146,60,0.5)]' 
                                                : 'border-slate-200 opacity-80 grayscale-[0.0]'
                                        ]" />
                                    
                                    <!-- 상태 -->
                                    <div class="flex items-center gap-0.5 mt-0.5">
                                        <Flame :size="13" 
                                            class="transition-all"
                                            :class="member.allCompleted ? 'fill-orange-400 text-orange-400 animate-pulse' : 'text-slate-300/40'" />
                                        <span v-if="!member.allCompleted" class="text-[11px] font-bold text-slate-400">
                                            {{ member.completedCount }}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <div v-if="!targetMission" class="bg-white rounded-3xl p-6 shadow-sm flex items-center justify-center gap-3 text-slate-400 border border-slate-200">
                            <MapIcon :size="20" />
                            <span class="font-medium">진행 중인 미션이 없어요!</span>
                        </div>
                    </div>
                </div>

                <!-- 타임라인 섹션 -->
                <div>
                    <div class="mb-4 flex items-center justify-between">
                        <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                            <Activity :size="20" class="text-brand-500 fill-brand-500"/>
                            타임라인
                        </h2>
                        <!-- 필터 탭 -->
                        <div class="flex p-1 bg-slate-200/50 rounded-xl font-bold">
                            <button 
                                v-for="filter in ['ALL', 'MISSION', 'MOCK_EXAM', 'DEFENSE', 'GENERAL']" 
                                :key="filter"
                                @click="selectedFilter = filter"
                                class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all"
                                :class="selectedFilter === filter ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-400 hover:text-slate-600'"
                            >
                                {{ filter === 'ALL' ? '전체' : filter === 'MOCK_EXAM' ? '모의고사' : filter === 'MISSION' ? '과제' : filter === 'DEFENSE' ? '디펜스' : '일반' }}
                            </button>
                        </div>
                    </div>

                    <div v-if="filteredRecords.length === 0 && !loading" class="flex flex-col items-center justify-center py-20 bg-white rounded-3xl border-2 border-dashed border-slate-200">
                        <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4">
                            <Code2 :size="32" class="text-slate-300" />
                        </div>
                        <h3 class="text-lg font-bold text-slate-800 mb-1">알고리즘 숲이 텅 비어있습니다</h3>
                        <p class="text-sm text-slate-400 mb-0 font-medium">
                            {{ selectedFilter === 'ALL' ? '첫 번째 알고리즘 문제를 해결하고 커밋해보세요!' : '해당 카테고리의 기록이 없습니다.' }}
                        </p>
                    </div>

                    <div v-else class="space-y-4 pb-20">
                        <template v-for="record in filteredRecords" :key="record.id">
                            <DashboardRecordCard 
                                :ref="el => { if (expandedRecordId === record.id) activeCardRef = el }"
                                :record="record"
                                :is-expanded="expandedRecordId === record.id"
                                @toggle-expand="handleToggleExpand"
                            />
                        </template>
                    </div>
                </div>
            </main>

            <!-- 3. 우측 사이드바 컬럼 (통계 + 활동 + 분석 패널) -->
            <aside class="hidden xl:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]">
                
                <!-- 1. 통계 열 (이동됨) -->
                <div class="bg-white rounded-2xl p-4 border border-slate-200 shadow-sm flex items-center justify-around">
                     <!-- 통계 1: 도토리 -->
                     <div class="flex items-center gap-2 group cursor-pointer" title="Acorns" @click="goToPlayground">
                        <IconAcorn class="text-fox w-8 h-8" stroke-width="2.5" fill="currentColor" />
                        <span class="text-xl font-black text-slate-700">{{ (studyData?.acornCount || 0).toLocaleString() }}</span>
                    </div>

                    <div class="w-px h-8 bg-slate-100"></div>

                    <!-- 통계 3: 스트릭 -->
                    <div class="flex items-center gap-2 group cursor-pointer" title="Streak">
                        <Flame class="w-7 h-7" :class="currentStreak > 0 ? 'text-rose-500 fill-rose-500 animate-pulse' : 'text-slate-300'" stroke-width="2.5" />
                        <span class="text-xl font-black text-slate-700">{{ currentStreak.toLocaleString() }}</span>
                    </div>
                    
                    <div class="w-px h-8 bg-slate-100"></div>

                    <!-- 통계 2: 제출 수 -->
                     <div class="flex items-center gap-2 group cursor-pointer" title="Submissions">
                        <Send class="w-6 h-6 text-sky-400 fill-sky-400" stroke-width="2.5" />
                        <span class="text-xl font-black text-slate-700">{{ records.length.toLocaleString() }}</span>
                    </div>
                </div>

                <!-- 2. 활동 로그 (이동됨) -->
                <div v-if="!loading" class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                    <h3 class="font-black text-slate-700 text-sm mb-4">스터디 활동 로그</h3>
                    
                    <!-- 히트맵 시각화 -->
                    <div class="relative">
                       <!-- 스크롤 래퍼 -->
                       <div ref="heatmapScrollRef" class="overflow-x-auto pb-2 custom-scrollbar no-scrollbar" style="direction: rtl;">
                           <div class="flex gap-[3px] justify-end min-w-max" style="direction: ltr;">
                               <div v-for="(week, wIdx) in heatmapWeeks" :key="wIdx" class="flex flex-col gap-[3px]">
                                   <div v-for="(day, dIdx) in week" :key="dIdx"
                                        class="w-2.5 h-2.5 rounded-[2px] transition-all relative cursor-pointer hover:ring-2 hover:ring-brand-400 hover:ring-offset-1"
                                        :class="day.colorClass"
                                        @mouseenter="showHeatmapTooltip($event, day)"
                                        @mouseleave="hideHeatmapTooltip">
                                   </div>
                               </div>
                           </div>
                       </div>
                       
                       <!-- 커스텀 툴팁 -->
                       <Teleport to="body">
                           <Transition name="fade">
                               <div 
                                   v-if="heatmapTooltip.visible"
                                   class="fixed z-[9999] bg-slate-900/95 backdrop-blur-sm text-white text-xs rounded-xl p-3 shadow-2xl pointer-events-none min-w-[180px] max-w-[240px]"
                                   :style="heatmapTooltipStyle"
                               >
                                   <!-- 날짜 헤더 -->
                                   <div class="flex items-center justify-between mb-2 pb-2 border-b border-slate-700">
                                       <span class="font-bold text-sm">{{ heatmapTooltip.date }}</span>
                                       <span class="bg-brand-500/20 text-brand-300 px-2 py-0.5 rounded-full text-[10px] font-bold">
                                           {{ heatmapTooltip.count }}건
                                       </span>
                                   </div>
                                   
                                   <!-- 기여자 목록 -->
                                   <div v-if="heatmapTooltip.contributors?.length > 0" class="space-y-2">
                                       <div v-for="c in heatmapTooltip.contributors" :key="c.userId || c.username" 
                                            class="flex items-center gap-2 bg-slate-800/50 rounded-lg p-1.5 pr-3">
                                           <!-- 프로필 이미지 -->
                                           <UserX v-if="c.username === '탈퇴한 회원'" :size="16" class="w-6 h-6 rounded-full border border-slate-600 bg-slate-700 p-1 text-slate-400" />
                                           <img v-else
                                               :src="c.avatarUrl || getDefaultProfileImage(c.userId)" 
                                               :alt="c.username"
                                               class="w-6 h-6 rounded-full object-cover border border-slate-600 bg-slate-700"
                                           />
                                           <!-- 이름 & 카운트 -->
                                           <div class="flex-1 min-w-0">
                                               <div class="font-medium text-white truncate">{{ c.username }}</div>
                                           </div>
                                           <div class="text-brand-400 font-bold text-sm">{{ c.count }}건</div>
                                       </div>
                                   </div>
                                   <div v-else class="text-slate-400 text-center py-2">
                                       <span class="text-lg">😴</span>
                                       <div class="mt-1">활동 없음</div>
                                   </div>
                               </div>
                           </Transition>
                       </Teleport>
                    </div>
                </div>

                <!-- 3. 분석 사이드바 (컨텍스트) -->
                <div class="flex-1 min-h-0 overflow-hidden rounded-2xl border border-slate-200 shadow-sm bg-white" @click.stop>
                     <AnalysisSidebar :record="activeAnalysisRecord" @scroll-to-line="handleScrollToLine" @acorn-used="handleAcornUsed" />
                </div>

                <div class="text-center text-[10px] text-slate-300 font-bold space-x-3 pb-2 uppercase tracking-wider">
<!-- ... (skip intermediate lines) -->
<!-- Replace AiDrawer part separately if contiguous block is too large or risky -->
                    <span class="hover:text-slate-400 cursor-pointer transition-colors">INFO</span>
                    <span>&bull;</span>
                    <span class="hover:text-slate-400 cursor-pointer transition-colors">STUDY</span>
                    <span>&bull;</span>
                    <span class="hover:text-slate-400 cursor-pointer transition-colors">PRIVACY</span>
                </div>

            </aside>

            </div>
        </div>
    </div>
  </div>

    <!-- 인라인 AI 드로어 (열렸을 때 60% 너비) -->
    <div 
      v-if="showDrawer"
      class="w-full md:w-[60%] border-l border-slate-200 transition-all duration-300 ease-in-out bg-white"
    >
      <AiDrawer 
        :is-visible="true"
        :type="drawerType"
        :title="drawerTitle"
        :loading="drawerLoading"
        :data="drawerData"
        :code="currentRecordCode"
        :record-id="currentDrawerRecord?.id"
        :user-id="user?.id"
        :solve-status="currentDrawerRecord?.result !== 'FAIL' ? 'solved' : 'wrong'"
        :wrong-reason="currentDrawerRecord?.result"
        :problem-number="currentDrawerRecord?.problemNumber"
        :problem-title="currentDrawerRecord?.title"
        @close="closeDrawer"
        @acorn-used="handleAcornUsed"
      />
    </div>

  </div>
  <!-- 분할 뷰 Flex 컨테이너 종료 -->
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { dashboardApi } from '@/api/dashboard';
import { studyApi } from '@/api/study';
import { useAuth } from '@/composables/useAuth';
import DashboardRecordCard from './DashboardRecordCard.vue';
import AnalysisSidebar from './AnalysisSidebar.vue';
import http from '@/api/http';
import { aiApi } from '@/api/ai';
import AlgorithmRadarChart from '@/components/charts/AlgorithmRadarChart.vue';
import AiDrawer from '@/components/ai/AiDrawer.vue';
import IconAcorn from '@/components/icons/IconAcorn.vue';
import { 
  Bot, 
  Lightbulb, 
  Bug, 
  Cpu, 
  Zap, 
  Database, 
  Binary, 
  LayoutGrid, 
  Network, 
  MessageSquare, 
  X,
  Code2,
  Youtube,
  Nut,
  Trophy,
  Activity,
  Copy,
  Check,
  ExternalLink,
  Calendar,
  Map as MapIcon,
  TrendingUp,
  School,
  Flame,
  Users,
  BarChart2,
  Hexagon,
  Send,
  UserX,
  Clock // Added
} from 'lucide-vue-next';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';
import { marked } from 'marked';

const profileImages = [
    // '/profile/bag.png',
    // '/profile/proud.png',
    // '/profile/smart.png',
    // '/profile/smile.png'
];

const getMemberProfileImage = (member) => {
    if (member.avatarUrl) return member.avatarUrl;
    const id = member.userId || 0;
    const index = id % profileImages.length;
    return profileImages[index];
};

// ... (other imports)

// ... (other imports)

const props = defineProps({
    studyId: {
        type: Number,
        default: null
    }
});

const { user } = useAuth();
const router = useRouter();
const records = ref([]);
const studyData = ref(null);
const loading = ref(true);
const heatmapWeeks = ref([]);
const heatmapScrollRef = ref(null);

const isObserving = computed(() => !!props.studyId);
const currentStudyId = computed(() => props.studyId || user.value?.studyId);

const isPendingApproval = computed(() => !!user.value?.pendingStudyName);
const pendingStudyName = computed(() => user.value?.pendingStudyName);

const exitObservation = () => {
    router.push('/study/ranking');
};

const goToPlayground = () => {
    router.push('/playground');
};

// 히트맵 툴팁 상태
const heatmapTooltip = ref({
    visible: false,
    x: 0,
    y: 0,
    date: '',
    count: 0,
    contributors: []
});

const showHeatmapTooltip = (event, day) => {
    const rect = event.target.getBoundingClientRect();
    heatmapTooltip.value = {
        visible: true,
        x: rect.left + rect.width / 2,
        y: rect.top - 8,
        date: day.dateFormatted,
        count: day.count || 0,
        contributors: day.contributors || []
    };
};

const hideHeatmapTooltip = () => {
    heatmapTooltip.value.visible = false;
};

// 빈 공간 클릭 시 확장된 카드 접기
const collapseExpandedCard = (event) => {
    // Don't collapse if clicking inside a card (handled by stopPropagation on cards)
    if (expandedRecordId.value !== null) {
        expandedRecordId.value = null;
        activeAnalysisRecord.value = null;
        activeCardRef.value = null;
    }
};

// 툴팁 위치 계산 (오버플로우 방지)
const heatmapTooltipStyle = computed(() => {
    const tooltip = heatmapTooltip.value;
    const tooltipWidth = 200;
    const tooltipHeight = 150;
    const padding = 16;
    
    let left = tooltip.x;
    let top = tooltip.y - tooltipHeight - 8;
    
    // 가로 범위 유지
    if (left - tooltipWidth / 2 < padding) {
        left = tooltipWidth / 2 + padding;
    } else if (left + tooltipWidth / 2 > window.innerWidth - padding) {
        left = window.innerWidth - tooltipWidth / 2 - padding;
    }
    
    // 툴팁이 화면 위로 넘치면 아래에 표시
    if (top < padding) {
        top = tooltip.y + 20;
    }
    
    return {
        left: left + 'px',
        top: top + 'px',
        transform: 'translateX(-50%)'
    };
});

// 사용자 ID 기반 기본 프로필 이미지
const getDefaultProfileImage = (userId) => {
    const id = userId || 0;
    const index = id % profileImages.length;
    return profileImages[index];
};

const selectedFilter = ref('ALL');
const filteredRecords = computed(() => {
    if (selectedFilter.value === 'ALL') return records.value;
    return records.value.filter(r => r.tag === selectedFilter.value);
});

// 필터 변경 시 확장 초기화
watch(selectedFilter, () => {
    expandedRecordId.value = null;
});

const expandedRecordId = ref(null);
const activeAnalysisRecord = ref(null);
const activeCardRef = ref(null);

const handleToggleExpand = (recordId) => {
    console.log('Dashboard: toggle-expand received for', recordId);
    if (expandedRecordId.value === recordId) {
        console.log('Dashboard: Collapsing record');
        expandedRecordId.value = null;
        activeAnalysisRecord.value = null;
        activeCardRef.value = null;
    } else {
        console.log('Dashboard: Expanding record');
        expandedRecordId.value = recordId;
        const found = records.value.find(r => r.id == recordId);
        console.log('Dashboard: Found record?', found);
        activeAnalysisRecord.value = found;
    }
};

const handleScrollToLine = ({ start, end }) => {
    console.log('Dashboard: handleScrollToLine', start, end, 'activeCardRef:', !!activeCardRef.value);
    if (activeCardRef.value && activeCardRef.value.scrollToLine) {
        activeCardRef.value.scrollToLine(start, end);
    }
};

const acornLogs = ref([]);
const missions = ref([]);

// 마감되지 않은 + 모든 팀원이 완료하지 않은 미션만 표시
const activeMissions = computed(() => {
    if (!missions.value || missions.value.length === 0) return [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    return missions.value
        .filter(m => {
            const deadline = new Date(m.deadline);
            deadline.setHours(23, 59, 59, 999);
            const notExpired = deadline >= today;
            
            // 모든 팀원이 완료했는지 확인 (memberProgressList가 있는 경우)
            const allMembersCompleted = m.memberProgressList?.length > 0 && 
                m.memberProgressList.every(member => member.allCompleted);
            
            return notExpired && !allMembersCompleted;
        })
        .sort((a, b) => a.week - b.week);  // 낮은 주차부터 표시
});

// 화면에 표시할 단일 미션 (가장 우선순위 높은 것)
const targetMission = computed(() => {
    return activeMissions.value.length > 0 ? activeMissions.value[0] : null;
});

const tagStats = ref([]);
const topTagName = computed(() => tagStats.value.length > 0 ? tagStats.value[0].tagKey : '');
const totalSolvedCount = computed(() => tagStats.value.reduce((acc, curr) => acc + (curr.solved || 0), 0));

// 특정 문제가 나에 의해 해결되었는지 확인 (computed용 raw 버전)
const isProblemSolvedRaw = (problemId) => {
    return records.value.some(r => 
        r.problemNumber == problemId && 
        r.userId === user.value?.id &&
        (r.result === 'SUCCESS' || r.result === 'PASSED' || (r.runtimeMs && r.runtimeMs > 0))
    );
};

// 특정 문제가 나에 의해 해결되었는지 확인 (팀원 미포함)
const isProblemSolved = (problemId) => {
    return isProblemSolvedRaw(problemId);
};

// 특정 미션이 완료되었는지 확인
const isMissionCompleted = (mission) => {
    if (!mission) return false;
    return mission.problemIds.every(pid => isProblemSolved(pid));
};

// 특정 미션이 마감 임박인지 확인 (3일 이내)
const isMissionUrgent = (mission) => {
    if (!mission || isMissionCompleted(mission)) return false;
    const now = new Date();
    const deadline = new Date(mission.deadline);
    const diffTime = deadline - now;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); 
    return diffDays <= 3 && diffDays >= 0;
};

// 미션별 테마 클래스
const getMissionThemeClass = (mission) => {
    return 'bg-white';
};

// 멤버 리스트 정렬: 본인 우선, 그 외 이름순
const sortMembers = (members) => {
    if (!members) return [];
    return [...members].sort((a, b) => {
        if (a.userId === user.value?.id) return -1;
        if (b.userId === user.value?.id) return 1;
        return a.username.localeCompare(b.username);
    });
};

// 본인인지 확인
const isMe = (userId) => {
    return user.value?.id === userId;
};

const processHeatmap = (data) => {
    console.log('processHeatmap called with:', data?.length); // Debug
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
                 contributors: contributors,
                 colorClass: colorClass
             });

             if (currentWeek.length === 7) {
                 weeks.push(currentWeek);
                 currentWeek = [];
             }
             current.setDate(current.getDate() + 1);
        }
        if (currentWeek.length > 0) weeks.push(currentWeek);
        console.log('Heatmap Generated:', weeks.length, 'weeks'); // Debug
        heatmapWeeks.value = weeks;
    } catch(err) {
        console.error("Heatmap Gen Error", err);
    }
};

const currentWeekCount = computed(() => {
    if (!heatmapWeeks.value.length) return 0;
    const lastWeek = heatmapWeeks.value[heatmapWeeks.value.length - 1];
    return lastWeek.reduce((acc, day) => acc + (day.count || 0), 0);
});

const heatmapResData = ref([]);

watch(() => studyData.value, () => {
    // Re-process heatmap when study data (member count) loads
    if (heatmapResData.value) {
        processHeatmap(heatmapResData.value);
    }
});


// Computed: 현재 스트릭 (연속 활동 일수)
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

const showModal = ref(false);
const copiedInput = ref(false);

const copyToClipboard = async (text) => {
    try {
        await navigator.clipboard.writeText(text);
        copiedInput.value = true;
        setTimeout(() => {
            copiedInput.value = false;
        }, 2000);
    } catch (err) {
        console.error('Failed to copy text: ', err);
    }
};
const modalType = ref('');
const modalTitle = ref('');
const modalData = ref(null);
const modalLoading = ref(false);
const activeTab = ref('insight');

// 드로어 상태
const showDrawer = ref(false);
const drawerType = ref('');
const drawerTitle = ref('');
const drawerData = ref(null);
const drawerLoading = ref(false);
const currentDrawerRecord = ref(null);  // 컨텍스트 카드에서 분석 중인 기록

// 코드 뷰를 위한 아코디언 상태
// const expandedRecordId = ref(null); // Removed

const tooltipData = ref(null);
const tooltipPos = ref({ x: 0, y: 0 });

const showTooltip = (event, day) => {
    const rect = event.target.getBoundingClientRect();
    tooltipPos.value = {
        x: rect.left + rect.width / 2,
        y: rect.top
    };
    tooltipData.value = day;
};

const hideTooltip = () => {
    tooltipData.value = null;
};



const fetchStudyData = async () => {
    if (!currentStudyId.value) return;
    try {
        const studyRes = await studyApi.get(currentStudyId.value);
        studyData.value = studyRes.data;

        const logsRes = await studyApi.getAcornLogs(currentStudyId.value);
        acornLogs.value = logsRes.data || [];

        const missionsRes = await studyApi.getMissions(currentStudyId.value);
        missions.value = missionsRes.data || [];
        
        // 히트맵 데이터 처리
        // Admin observation: use specific study ID if provided
        const heatmapRes = await dashboardApi.getHeatmap(props.studyId);
        heatmapResData.value = heatmapRes.data || [];
        processHeatmap(heatmapResData.value);
    } catch(e) {
        console.error('Study Load Error:', e);
    }
};

const handleAcornUsed = () => {
    console.log('Acorn used, refreshing study data...');
    fetchStudyData();
};

onMounted(async () => {
  try {
      // 1. 사용자 기록 가져오기
      try {
          const recordsRes = await dashboardApi.getRecords();
          records.value = recordsRes.data;
      } catch(e) {
          console.error('Records Load Error:', e);
      }
      
      // 2. 태그 통계 가져오기
      try {
          const statsRes = await aiApi.getTagStats(user.value?.id || 1, 6);
          tagStats.value = statsRes.data || [];
      } catch(e) {
          console.error('TagStats Load Error:', e);
      }
      
      // 3. 스터디 데이터 가져오기 (해당되는 경우)
      if (user.value?.studyId) {
          await fetchStudyData();
      }

      // 4. Fetch Heatmap
      try {
        const heatmapRes = await dashboardApi.getHeatmap();
        heatmapResData.value = heatmapRes.data || [];
        processHeatmap(heatmapResData.value);
        nextTick(() => {
          if (heatmapScrollRef.value) {
            heatmapScrollRef.value.scrollLeft = heatmapScrollRef.value.scrollWidth;
          }
        });
      } catch(e) {
        console.error('Heatmap Load Error:', e);
        processHeatmap([]);
      }
  } catch (globalError) {
      console.error("Critical Dashboard Error:", globalError);
  } finally {
      loading.value = false;
  }
});

const getProblemLink = (problemId) => `https://www.acmicpc.net/problem/${problemId}`;





const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
};

const formatMissionDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}. ${month}. ${day}.`;
};

// Markdown Renderer

// Markdown Renderer
const renderMarkdown = (text) => {
    if (!text) return '';
    return marked.parse(text);
};

const highlightCodeContent = (code, lang = 'java') => {
    if (!code) return '';
    try {
        return hljs.highlight(code, { language: lang }).value;
    } catch (e) {
        try {
             return hljs.highlightAuto(code).value;
        } catch (e2) {
             return code;
        }
    }
};

const copyCode = (code) => {
    navigator.clipboard.writeText(code);
    alert('코드가 복사되었습니다.');
};

const currentRecordCode = ref('');

const parsePatterns = (jsonString) => {
    if (!jsonString) return [];
    try {
        const parsed = JSON.parse(jsonString);
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        console.error("Failed to parse patterns", e);
        return [];
    }
};

const highlightCode = (code, language) => {
    try {
        return hljs.highlight(code, { language }).value;
    } catch (err) {
        return hljs.highlightAuto(code).value;
    }
};

// Helper to get file extension from language
const getFileExtension = (language) => {
    const extensions = {
        'java': 'java',
        'python': 'py',
        'cpp': 'cpp',
        'c++': 'cpp',
        'c': 'c',
        'javascript': 'js',
        'typescript': 'ts'
    };
    return extensions[language?.toLowerCase()] || 'txt';
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
