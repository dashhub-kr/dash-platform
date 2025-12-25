<template>
  <!-- Flex Container for Split View -->
  <div class="flex h-screen overflow-hidden bg-slate-50 font-sans">
    
    <!-- CONTEXT CARD MODE: When Drawer is Open - Show only selected problem -->
    <div 
      v-if="showDrawer && currentDrawerRecord"
      class="w-full md:w-[40%] bg-gradient-to-br from-slate-50 to-slate-100 border-r border-slate-200 flex flex-col"
    >
      <div class="flex-1 p-6 flex flex-col">
        <!-- Context Header -->
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
        
        <!-- Performance Metrics Grid -->
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
        
        <!-- Code Preview -->
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
        
        <!-- Close Button -->
        <button 
          @click="closeDrawer" 
          class="mt-4 w-full py-3 rounded-xl bg-slate-200 hover:bg-slate-300 text-slate-700 font-bold transition-colors flex items-center justify-center gap-2"
        >
          <X class="w-4 h-4" />
          분석 닫기
        </button>
      </div>
    </div>
    
    <!-- NORMAL MODE: Full Dashboard when Drawer is Closed -->
    <div 
      v-else
      class="w-full overflow-y-auto"
    >
      <div class="min-h-screen bg-slate-50 text-slate-800">
        <!-- Navbar / Header Area -->

        <div class="flex-1 flex justify-center p-4 md:p-8">
            <div class="flex gap-8 max-w-screen-xl w-full">
            
            <!-- 2. CENTRAL MAIN COLUMN -->
            <main class="flex-1 min-w-0 space-y-6">
                <!-- Header Section with Clean Metrics -->
                <div class="animate-fade-in-down">
                    <!-- Weekly Mission Section -->
                    <div class="mb-6">
                        <div v-if="targetMission" 
                            class="rounded-3xl p-6 shadow-none relative overflow-hidden transition-all duration-500 bg-white border border-slate-200"
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

                            <!-- Member Progress Section -->
                            <div v-if="targetMission.memberProgressList?.length > 0" class="mt-6 pt-4 border-t border-slate-100 flex flex-wrap items-center gap-4">
                                <div v-for="member in sortMembers(targetMission.memberProgressList)" :key="member.userId" 
                                    class="flex flex-col items-center gap-1 group relative cursor-help">
                                    
                                    <!-- Tooltip for Name -->
                                    <div class="absolute bottom-full mb-2 px-2 py-1 bg-black/80 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-20">
                                        {{ member.username }} {{ isMe(member.userId) ? '(나)' : '' }}
                                        <div class="absolute bottom-0 left-1/2 -translate-x-1/2 translate-y-1 w-2 h-2 bg-black/80 rotate-45"></div>
                                    </div>

                                    <!-- Avatar -->
                                    <img :src="getMemberProfileImage(member)" :alt="member.username"
                                        class="w-10 h-10 rounded-full object-cover border-2 transition-all relative z-10 bg-white"
                                        :class="[
                                            isMe(member.userId) 
                                                ? 'border-emerald-400 ring-2 ring-emerald-400/30' + (member.allCompleted ? ' shadow-[0_0_12px_rgba(52,211,153,0.6)]' : '')
                                                : member.allCompleted 
                                                ? 'border-orange-400 shadow-[0_0_12px_rgba(251,146,60,0.5)]' 
                                                : 'border-slate-200 opacity-80 grayscale-[0.0]'
                                        ]" />
                                    
                                    <!-- Status -->
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
                            <Map :size="20" />
                            <span class="font-medium">진행 중인 미션이 없어요!</span>
                        </div>
                    </div>
                </div>

                <!-- Timeline Section -->
                <div>
                    <div class="mb-4 flex items-center justify-between">
                        <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                            <Activity :size="20" class="text-brand-500 fill-brand-500"/>
                            Timeline
                        </h2>
                        <!-- Filter Tabs -->
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
                        <h3 class="text-lg font-bold text-slate-800 mb-1">기록된 모험이 없습니다</h3>
                        <p class="text-sm text-slate-400 mb-0 font-medium">
                            {{ selectedFilter === 'ALL' ? '첫 번째 알고리즘 문제를 해결하고 커밋해보세요!' : '해당 카테고리의 기록이 없습니다.' }}
                        </p>
                    </div>

                    <div v-else class="space-y-4 pb-20">
                        <template v-for="record in filteredRecords" :key="record.id">
                            <DashboardRecordCard 
                                :record="record"
                                :is-expanded="expandedRecordId === record.id"
                                @toggle-expand="handleToggleExpand"
                            />
                        </template>
                    </div>
                </div>
            </main>

            <!-- 3. RIGHT SIDEBAR COLUMN (Stats + Activity + Analysis Panel) -->
            <aside class="hidden xl:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]">
                
                <!-- 1. Stats Row (Moved here) -->
                <div class="bg-white rounded-2xl p-4 border border-slate-200 shadow-sm flex items-center justify-around">
                     <!-- Stat 1: Acorns -->
                     <div class="flex items-center gap-2 group cursor-pointer" title="Acorns" @click="goToPlayground">
                        <IconAcorn class="text-fox w-8 h-8" stroke-width="2.5" fill="currentColor" />
                        <span class="text-xl font-black text-slate-700">{{ studyData?.acornCount || 0 }}</span>
                    </div>

                    <div class="w-px h-8 bg-slate-100"></div>

                    <!-- Stat 3: Streak -->
                    <div class="flex items-center gap-2 group cursor-pointer" title="Streak">
                        <Flame class="w-7 h-7" :class="currentStreak > 0 ? 'text-rose-500 fill-rose-500 animate-pulse' : 'text-slate-300'" stroke-width="2.5" />
                        <span class="text-xl font-black text-slate-700">{{ currentStreak }}</span>
                    </div>
                    
                    <div class="w-px h-8 bg-slate-100"></div>

                    <!-- Stat 2: Submissions -->
                     <div class="flex items-center gap-2 group cursor-pointer" title="Submissions">
                        <Send class="w-6 h-6 text-sky-400 fill-sky-400" stroke-width="2.5" />
                        <span class="text-xl font-black text-slate-700">{{ records.length }}</span>
                    </div>
                </div>

                <!-- 2. Activity Log (Moved here) -->
                <div v-if="!loading" class="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                    <h3 class="font-black text-slate-700 text-sm mb-4">스터디 활동 로그</h3>
                    
                    <!-- Heatmap Visualization -->
                    <div class="">
                       <!-- Scroll wrapper -->
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

                <!-- 3. Analysis Sidebar (Context) -->
                <div class="flex-1 min-h-0 overflow-hidden rounded-2xl border border-slate-200 shadow-sm bg-white">
                     <AnalysisSidebar :record="activeAnalysisRecord" />
                </div>

                <div class="text-center text-[10px] text-slate-300 font-bold space-x-3 pb-2 uppercase tracking-wider">
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

    <!-- Inline AI Drawer (60% width when open) -->
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
      />
    </div>

  </div>
  <!-- End Split View Flex Container -->
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { dashboardApi } from '../api/dashboard';
import { studyApi } from '../api/study';
import { useAuth } from '../composables/useAuth';
import DashboardRecordCard from './dashboard/DashboardRecordCard.vue';
import AnalysisSidebar from './dashboard/AnalysisSidebar.vue';
import http from '../api/http';
import { aiApi } from '../api/ai';
import AlgorithmRadarChart from '../components/charts/AlgorithmRadarChart.vue';
import AiDrawer from '../components/AiDrawer.vue';
import IconAcorn from '../components/icons/IconAcorn.vue';
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
  Send
} from 'lucide-vue-next';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';
import { marked } from 'marked';

const profileImages = [
    '/profile/bag.png',
    '/profile/proud.png',
    '/profile/smart.png',
    '/profile/smile.png'
];

const getMemberProfileImage = (member) => {
    if (member.avatarUrl) return member.avatarUrl;
    const id = member.userId || 0;
    const index = id % profileImages.length;
    return profileImages[index];
};

// ... (other imports)

const { user } = useAuth();
const records = ref([]);
const studyData = ref(null);
const loading = ref(true);
const heatmapWeeks = ref([]);
const heatmapScrollRef = ref(null);

const selectedFilter = ref('ALL');
const filteredRecords = computed(() => {
    if (selectedFilter.value === 'ALL') return records.value;
    return records.value.filter(r => r.tag === selectedFilter.value);
});

// Reset expansion when changing filters
watch(selectedFilter, () => {
    expandedRecordId.value = null;
});

const expandedRecordId = ref(null);
const activeAnalysisRecord = ref(null);

const handleToggleExpand = (recordId) => {
    console.log('Dashboard: toggle-expand received for', recordId);
    if (expandedRecordId.value === recordId) {
        console.log('Dashboard: Collapsing record');
        expandedRecordId.value = null;
        activeAnalysisRecord.value = null;
    } else {
        console.log('Dashboard: Expanding record');
        expandedRecordId.value = recordId;
        const found = records.value.find(r => r.id == recordId);
        console.log('Dashboard: Found record?', found);
        activeAnalysisRecord.value = found;
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

// Check if a specific problem is solved BY ME (raw version for computed)
const isProblemSolvedRaw = (problemId) => {
    return records.value.some(r => 
        r.problemNumber == problemId && 
        r.userId === user.value?.id &&
        (r.result === 'SUCCESS' || r.result === 'PASSED' || (r.runtimeMs && r.runtimeMs > 0))
    );
};

// Check if a specific problem is solved BY ME (not by other team members)
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
             
             // --- Participation Logic Start ---
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
             // --- Participation Logic End ---

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


// Computed: Current streak (consecutive days with activity)
const currentStreak = computed(() => {
    if (!heatmapWeeks.value.length) return 0;
    
    // Flatten weeks to days array and reverse to start from today
    const allDays = heatmapWeeks.value.flat().reverse();
    
    let streak = 0;
    const today = new Date().toISOString().split('T')[0];
    
    for (const day of allDays) {
        // Skip future dates
        if (day.date > today) continue;
        
        if (day.count > 0) {
            streak++;
        } else if (streak > 0 || day.date === today) {
            // If today has no activity, streak is 0
            // If we had activity before and now hit a zero, stop counting
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

// Drawer State
const showDrawer = ref(false);
const drawerType = ref('');
const drawerTitle = ref('');
const drawerData = ref(null);
const drawerLoading = ref(false);
const currentDrawerRecord = ref(null);  // Record being analyzed in Context Card

// Accordion state for code view
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



onMounted(async () => {
  try {
      // 1. Fetch User Records
      try {
          const recordsRes = await dashboardApi.getRecords();
          records.value = recordsRes.data;
      } catch(e) {
          console.error('Records Load Error:', e);
      }
      
      // 2. Fetch Tag Stats
      try {
          const statsRes = await aiApi.getTagStats(user.value?.id || 1, 6);
          tagStats.value = statsRes.data || [];
      } catch(e) {
          console.error('TagStats Load Error:', e);
      }
      
      // 3. Fetch Study Data (if applicable)
      if (user.value?.studyId) {
          try {
              const studyRes = await studyApi.get(user.value.studyId);
              studyData.value = studyRes.data;

              const logsRes = await studyApi.getAcornLogs(user.value.studyId);
              acornLogs.value = logsRes.data || [];

              const missionsRes = await studyApi.getMissions(user.value.studyId);
              missions.value = missionsRes.data || [];
          } catch (e) {
              console.error("Study Data Load Error", e);
          }
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

const router = useRouter();

const goToPlayground = () => {
    router.push('/playground');
};



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
