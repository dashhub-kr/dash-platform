<template>
  <!-- Flex Container for Split View -->
  <div class="flex h-screen overflow-hidden bg-slate-50">
    
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
            <span class="px-2 py-1 bg-indigo-100 text-indigo-700 text-xs font-bold rounded-lg">#{{ currentDrawerRecord.problemId }}</span>
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
            <div class="text-lg font-black text-indigo-600">{{ currentDrawerRecord.timeComplexity || '-' }}</div>
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

        <main class="container mx-auto px-6 py-8 pb-32">
          <!-- Header Section with Clean Metrics -->
          <div class="mb-8 animate-fade-in-down">
            <!-- Greeting Row -->
            <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
              <div>
                <h1 class="text-2xl font-bold text-slate-800">
                  안녕하세요, <span class="text-indigo-600">{{ user?.username || '탐험가' }}</span>님! 👋
                </h1>
                <p class="text-sm text-slate-500 mt-1">오늘도 알고리즘의 바다를 항해할 준비가 되셨나요?</p>
              </div>
              <!-- Streak Badge (only show if active) -->
              <div v-if="currentStreak > 0" class="flex items-center gap-2 px-3 py-1.5 bg-orange-50 border border-orange-100 text-orange-600 rounded-full text-sm font-medium">
                <span>🔥</span>
                <span>{{ currentStreak }}일 연속 활동 중</span>
              </div>
            </div>

            <!-- Clean Stats Row -->
            <div class="grid grid-cols-4 gap-3">
              <!-- Stat 1: Acorns -->
              <div 
                class="bg-white rounded-xl p-4 border border-slate-100 hover:border-amber-200 hover:shadow-sm transition-all cursor-pointer group"
                @click="goToPlayground"
              >
                <div class="flex items-center justify-between mb-2">
                  <span class="text-2xl">🌰</span>
                  <span class="text-xs font-medium text-slate-400 uppercase">Acorns</span>
                </div>
                <div class="text-2xl font-bold text-slate-800">{{ studyData?.acornCount || 0 }}</div>
              </div>

              <!-- Stat 2: Solutions -->
              <div class="bg-white rounded-xl p-4 border border-slate-100 hover:border-indigo-200 hover:shadow-sm transition-all">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-2xl">📊</span>
                  <span class="text-xs font-medium text-slate-400 uppercase">Solutions</span>
                </div>
                <div class="text-2xl font-bold text-slate-800">{{ records.length }}</div>
              </div>

              <!-- Stat 3: Streak -->
              <div class="bg-white rounded-xl p-4 border border-slate-100 hover:border-rose-200 hover:shadow-sm transition-all">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-2xl">🔥</span>
                  <span class="text-xs font-medium text-slate-400 uppercase">Streak</span>
                </div>
                <div class="text-2xl font-bold text-slate-800">{{ currentStreak }}<span class="text-sm font-normal text-slate-400 ml-1">일</span></div>
              </div>

              <!-- Stat 4: Members -->
              <div class="bg-white rounded-xl p-4 border border-slate-100 hover:border-emerald-200 hover:shadow-sm transition-all">
                <div class="flex items-center justify-between mb-2">
                  <span class="text-2xl">👥</span>
                  <span class="text-xs font-medium text-slate-400 uppercase">Members</span>
                </div>
                <div class="text-2xl font-bold text-slate-800">{{ studyData?.memberCount || 1 }}<span class="text-sm font-normal text-slate-400 ml-1">명</span></div>
              </div>
            </div>
          </div>



      <!-- Activity Section: Heatmap -->
      <div v-if="!loading" class="mb-10 animate-fade-in-up">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-5">
          
          <!-- Heatmap Card (Full Width) -->
          <div class="lg:col-span-12 bg-white rounded-2xl p-6 shadow-sm border border-slate-100">
            <div class="flex items-center justify-between mb-5">
              <h2 class="text-base font-bold text-slate-800 flex items-center gap-2">
                <TrendingUp class="text-indigo-500 w-5 h-5" />
                스터디 활동 로그
              </h2>
              <!-- Legend -->
              <div class="flex items-center gap-2 text-xs font-medium text-slate-400">
                <span>Less</span>
                <div class="flex gap-1">
                  <div class="w-3 h-3 rounded-sm bg-slate-100"></div>
                  <div class="w-3 h-3 rounded-sm bg-indigo-200"></div>
                  <div class="w-3 h-3 rounded-sm bg-indigo-400"></div>
                  <div class="w-3 h-3 rounded-sm bg-indigo-600"></div>
                  <div class="w-3 h-3 rounded-sm bg-indigo-800"></div>
                </div>
                <span>More</span>
              </div>
            </div>
            
            <div ref="heatmapScrollRef" class="overflow-x-auto pb-2 custom-scrollbar">
              <div class="min-w-max">
                <!-- Heatmap (Grass) -->
                <div class="flex gap-[3px]">
                  <div v-for="(week, wIdx) in heatmapWeeks" :key="wIdx" class="flex flex-col gap-[3px]">
                    <div 
                      v-for="(day, dIdx) in week" 
                      :key="dIdx"
                      class="w-3 h-3 rounded-[2px] transition-all relative cursor-pointer hover:ring-2 hover:ring-indigo-300 hover:z-10"
                      :class="day.colorClass"
                      @mouseenter="showTooltip($event, day)"
                      @mouseleave="hideTooltip"
                    >
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>

      <!-- Shared Fixed Tooltip -->
             <div v-if="tooltipData" 
                  class="fixed z-50 pointer-events-none transform -translate-x-1/2 -translate-y-full mb-2"
                  :style="{ left: tooltipPos.x + 'px', top: tooltipPos.y + 'px' }">
                  <div class="bg-slate-800 text-white text-xs rounded-lg py-2 px-3 shadow-xl flex flex-col items-center gap-1 mb-2">
                      <span class="font-bold text-slate-200">{{ tooltipData.dateFormatted }}</span>
                      <span class="font-bold">{{ tooltipData.count }} solutions</span>
                      <div v-if="tooltipData.count > 0" class="flex flex-wrap gap-1 max-w-[150px] justify-center mt-1 border-t border-slate-700 pt-1">
                          <span v-for="name in tooltipData.contributors" :key="name" class="text-[10px] bg-slate-700 px-1.5 py-0.5 rounded text-indigo-300">
                              {{ name }}
                          </span>
                      </div>
                  </div>
                  <!-- Arrow -->
                  <div class="w-2 h-2 bg-slate-800 transform rotate-45 absolute bottom-1 left-1/2 -translate-x-1/2 shadow-sm"></div>
             </div>

      <div v-if="records.length === 0 && !loading" class="flex flex-col items-center justify-center py-20 bg-white rounded-3xl border border-dashed border-slate-300">
        <div class="w-20 h-20 bg-indigo-50 rounded-full flex items-center justify-center mb-6">
          <Code2 :size="40" class="text-indigo-400" />
        </div>
        <h3 class="text-xl font-bold text-slate-800 mb-2">기록된 모험이 없습니다</h3>
        <p class="text-slate-500 mb-6">첫 번째 알고리즘 문제를 해결하고 커밋해보세요!</p>
        <button class="px-6 py-3 bg-slate-900 text-white rounded-xl font-bold hover:bg-slate-800 transition-colors">
          가이드 보기
        </button>
      </div>

      <div v-else class="grid grid-cols-1 gap-4">
        <!-- Record Card (Horizontal) -->
        <template v-for="record in records" :key="record.id">
        <div 
          class="group relative bg-white rounded-[24px] p-6 shadow-sm border border-slate-100 transition-all duration-300 hover:shadow-xl hover:-translate-y-1 overflow-hidden"
        >
          <!-- Status Indication Line (Left Border) -->
          <div 
             class="absolute left-0 top-0 bottom-0 w-1.5 transition-colors duration-300"
             :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-indigo-500' : 'bg-rose-500'"
          ></div>

          <div class="flex flex-col xl:flex-row gap-6 pl-2">
            
            <!-- Main Content Section -->
            <div class="flex-1 min-w-0">
                <!-- Header: badges & meta -->
                <div class="flex flex-wrap items-center gap-2 mb-3">
                    <a 
                      :href="`https://www.acmicpc.net/problem/${record.problemNumber}`" 
                      target="_blank"
                      class="flex items-center gap-1 text-[11px] font-black text-slate-500 bg-slate-100 px-2.5 py-1 rounded-lg hover:bg-indigo-50 hover:text-indigo-600 transition-colors uppercase tracking-wider"
                    >
                      #{{ record.problemNumber }}
                      <ExternalLink :size="10" />
                    </a>
                    
                    <span 
                        class="px-2.5 py-1 rounded-lg text-[11px] font-black uppercase tracking-wider"
                        :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-slate-100 text-slate-600' : 'bg-rose-100 text-rose-600'"
                    >
                        {{ record.language }}
                    </span>

                    <span v-if="!(record.runtimeMs > 0 && record.memoryKb > 0)" class="px-2.5 py-1 rounded-lg text-[11px] font-black bg-rose-500 text-white shadow-sm shadow-rose-200">
                        FAILED
                    </span>
                    
                    <div class="ml-auto text-[11px] font-medium text-slate-400 flex items-center gap-1.5 md:hidden">
                        {{ formatDate(record.committedAt).split('.').slice(0, 3).join('.') }}
                    </div>
                </div>

                <!-- Title -->
                <div class="flex items-start justify-between gap-4 mb-4">
                    <h3 class="text-xl md:text-2xl font-black text-slate-800 leading-tight group-hover:text-indigo-600 transition-colors cursor-pointer">
                        {{ record.title }}
                    </h3>
                </div>

                <!-- Footer: User & Date (Desktop) -->
                <div class="hidden md:flex items-center gap-3">
                     <div class="flex items-center gap-2 px-2.5 py-1.5 bg-slate-50 rounded-full border border-slate-100">
                        <div class="w-5 h-5 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-[10px] font-bold text-white shadow-sm">
                            {{ (record.username || '?').charAt(0).toUpperCase() }}
                        </div>
                        <span class="text-xs font-bold text-slate-600 pr-1">{{ record.username || 'Unknown' }}</span>
                     </div>
                     <span class="text-xs font-medium text-slate-400">{{ formatDate(record.committedAt) }}</span>
                </div>
            </div>

            <!-- Stats & Actions Section -->
            <div class="flex flex-col gap-4 xl:w-[480px] shrink-0">
                
                <!-- Stats Grid -->
                <div class="grid grid-cols-4 gap-2">
                    <!-- Runtime -->
                    <div class="flex flex-col items-center justify-center p-3 rounded-2xl bg-slate-50 border border-slate-100">
                         <Zap :size="16" class="mb-1" :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'text-amber-400' : 'text-slate-300'" />
                         <span class="text-sm font-black text-slate-700">{{ record.runtimeMs || '-' }}<span class="text-[10px] font-normal text-slate-400 ml-0.5">ms</span></span>
                    </div>
                    <!-- Memory -->
                    <div class="flex flex-col items-center justify-center p-3 rounded-2xl bg-slate-50 border border-slate-100">
                         <Database :size="16" class="mb-1" :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'text-blue-400' : 'text-slate-300'" />
                         <span class="text-sm font-black text-slate-700">{{ record.memoryKb ? Math.round(record.memoryKb / 1024) : '-' }}<span class="text-[10px] font-normal text-slate-400 ml-0.5">MB</span></span>
                    </div>
                     <!-- Logic -->
                    <div class="flex flex-col items-center justify-center p-3 rounded-2xl bg-slate-50 border border-slate-100">
                         <Activity :size="16" class="mb-1" :class="record.timeComplexity ? 'text-emerald-400' : 'text-slate-300'" />
                         <span class="text-xs font-black text-slate-700 truncate max-w-full px-1">{{ record.timeComplexity || '-' }}</span>
                    </div>
                    <!-- Score -->
                    <div class="flex flex-col items-center justify-center p-3 rounded-2xl bg-slate-50 border border-slate-100 relative overflow-hidden">
                         <Trophy :size="16" class="mb-1" :class="record.score ? 'text-purple-400' : 'text-slate-300'" />
                         <span class="text-sm font-black text-slate-700">{{ record.score || '-' }}</span>
                    </div>
                </div>

                <!-- Actions Buttons -->
                <div class="flex items-center gap-2">
                    <button 
                        @click.stop="requestReview(record)" 
                        class="flex-1 h-12 rounded-xl bg-slate-900 text-white font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-slate-200 hover:bg-slate-800 hover:shadow-xl hover:-translate-y-0.5 transition-all active:scale-95"
                    >
                        <Bot :size="18" />
                        <span>리뷰</span>
                    </button>
                    
                    <button 
                        v-if="!(record.runtimeMs > 0 && record.memoryKb > 0)"
                        @click.stop="requestCounterExample(record)" 
                        class="flex-1 h-12 rounded-xl bg-rose-50 text-rose-600 font-bold text-sm flex items-center justify-center gap-2 border border-rose-100 hover:bg-rose-100 hover:border-rose-200 transition-all active:scale-95"
                    >
                        <Bug :size="18" />
                        <span>반례</span>
                    </button>

                    <button 
                        @click.stop="requestTutor(record)" 
                        class="h-12 w-12 rounded-xl flex items-center justify-center border transition-all active:scale-95 bg-amber-50 border-amber-100 text-amber-600 hover:bg-amber-100"
                        title="AI 튜터에게 질문하기"
                    >
                        <Lightbulb :size="20" />
                    </button>
                </div>

            </div>
          </div>
          
          <!-- Expandable Code Section (Accordion) -->
          <Transition name="expand">
            <div v-if="expandedRecordId === record.id" class="mt-4 border-t border-slate-200 pt-4">
              <div class="flex items-center justify-between mb-3">
                <h4 class="text-sm font-bold text-slate-700 flex items-center gap-2">
                  <Code class="w-4 h-4" />
                  제출한 소스코드
                </h4>
                <button 
                  @click.stop="expandedRecordId = null" 
                  class="text-xs text-slate-500 hover:text-slate-700 underline"
                >
                  코드 숨기기
                </button>
              </div>
              
              <!-- Code Viewer (Light Theme) -->
              <div class="bg-white rounded-xl overflow-hidden border border-slate-200">
                <div class="px-4 py-2 bg-slate-50 text-slate-500 text-xs font-mono border-b border-slate-200 flex justify-between items-center">
                  <span>{{ record.language }}.{{ record.language === 'Java' ? 'java' : 'py' }}</span>
                  <span class="px-2 py-0.5 rounded bg-slate-100 text-slate-500">Read Only</span>
                </div>
                <div class="max-h-96 overflow-y-auto">
                  <pre class="m-0 p-6 text-sm font-mono leading-relaxed bg-white"><code class="hljs language-java text-slate-700" v-html="highlightCode(record.code || '// 코드를 불러올 수 없습니다.', record.language.toLowerCase())"></code></pre>
                </div>
              </div>
              
              <!-- TODO: 전체 화면으로 보기 버튼 (추후 구현) -->
            </div>
          </Transition>
        </div>
        </template>
      </div>
    </main>
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
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { dashboardApi } from '../api/dashboard';
import { studyApi } from '../api/study';
import { useAuth } from '../composables/useAuth';
import http from '../api/http';
import { aiApi } from '../api/ai';
import AlgorithmRadarChart from '../components/charts/AlgorithmRadarChart.vue';
import AiDrawer from '../components/AiDrawer.vue';
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
  Code,
  Youtube,
  Nut,
  Trophy,
  Activity,
  Copy,
  Check
} from 'lucide-vue-next';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';

// ... (other imports)

const { user } = useAuth();
const records = ref([]);
const studyData = ref(null);
const acornLogs = ref([]);
const loading = ref(true);
const heatmapWeeks = ref([]);
const heatmapScrollRef = ref(null);
const tagStats = ref([]);
const topTagName = computed(() => tagStats.value.length > 0 ? tagStats.value[0].tagKey : '');
const totalSolvedCount = computed(() => tagStats.value.reduce((acc, curr) => acc + (curr.solved || 0), 0));

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
const expandedRecordId = ref(null);

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
      const recordsRes = await dashboardApi.getRecords();
      records.value = recordsRes.data;
      
      // Fetch Tag Stats for Radar Chart
      const statsRes = await aiApi.getTagStats(user.value.id || 1, 6); // Mock ID 1 if null
      tagStats.value = statsRes.data || [];
  } catch(e) {
    console.error('Data Load Error:', e);
  }
  
  if (user.value?.studyId) {
      try {
          // Fetch Study Data
          const studyRes = await studyApi.get(user.value.studyId);
          studyData.value = studyRes.data;

          // Fetch Acorn Logs for Ticker
          const logsRes = await studyApi.getAcornLogs(user.value.studyId);
          acornLogs.value = logsRes.data || [];
      } catch (e) {
          console.error("Study data error", e);
      }
  }

  try {
    const heatmapRes = await dashboardApi.getHeatmap();
    processHeatmap(heatmapRes.data || []);
    // 최신 날짜(우측)로 스크롤
    setTimeout(() => {
      if (heatmapScrollRef.value) {
        heatmapScrollRef.value.scrollLeft = heatmapScrollRef.value.scrollWidth;
      }
    }, 100);
  } catch(e) {
    console.error('Heatmap error:', e);
    processHeatmap([]);
  }
  
  loading.value = false;
});

const router = useRouter();

const goToPlayground = () => {
    router.push('/playground');
};

const processHeatmap = (data) => {
    // 1. Map data for quick lookup
    const activityMap = new Map();
    data.forEach(item => {
        activityMap.set(item.date, item);
    });

    // 2. Generate last 365 days (approx 52 weeks)
    const weeks = [];
    const today = new Date();
    // Align to Saturday of this week to fill to the right
    const end = new Date(today);
    // Go back 52 weeks * 7 days
    const start = new Date(end);
    start.setDate(start.getDate() - (52 * 7) + 1); 
    
    // Adjust start to be Sunday
    // while(start.getDay() !== 0) start.setDate(start.getDate() - 1);

    let current = new Date(start);
    let currentWeek = [];

    // Loop until we cover enough days
    for (let i = 0; i < 52 * 7; i++) {
        const dateStr = current.toISOString().split('T')[0];
        const activity = activityMap.get(dateStr);
        const count = activity ? activity.count : 0;
        
        // Determine color
        let colorClass = 'bg-slate-100';
        if (count > 0) colorClass = 'bg-indigo-200';
        if (count >= 3) colorClass = 'bg-indigo-400';
        if (count >= 6) colorClass = 'bg-indigo-600';
        if (count >= 9) colorClass = 'bg-indigo-800';

        currentWeek.push({
            date: dateStr,
            dateFormatted: current.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }),
            count: count,
            contributors: activity ? activity.contributors : [],
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

import { marked } from 'marked';

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

const adaptAnalysisData = (result) => {
    if (result.fullResponse) {
        try {
            return JSON.parse(result.fullResponse);
        } catch (e) {
            console.error("JSON Parse failed, falling back to flat fields", e);
        }
    }

    // Fallback Adaptor for Flat DTO
    return {
        summary: result.summary,
        complexity: {
            time: result.timeComplexity || '-',
            space: result.spaceComplexity || '-',
            explanation: result.complexityExplanation
        },
        algorithm: {
            intuition: result.algorithmIntuition,
            patterns: parsePatterns(result.patterns)
        },
        structure: [],
        traceExample: { steps: [] },
        pitfalls: {
            items: parsePatterns(result.pitfalls),
            improvements: parsePatterns(result.improvements)
        },
        refactor: {
            code: result.refactorCode,
            explanation: result.refactorExplanation
        }
    };
};

const requestReview = async (record) => {
    // Set current record for Context Card
    currentDrawerRecord.value = record;
    expandedRecordId.value = record.id;
    
    drawerType.value = 'review';
    drawerTitle.value = `AI 코드 분석 · ${record.title}`;
    drawerLoading.value = true;
    showDrawer.value = true;
    drawerData.value = null;
    currentRecordCode.value = record.code || '';

    try {
        const response = await http.get(`/ai/review/${record.id}`);
        console.log("Analysis Result:", response.data); // Debug
        drawerData.value = adaptAnalysisData(response.data);
    } catch (error) {
        console.error("Failed to fetch review:", error);
        const status = error.response?.status || 'Unknown';
        const msg = error.response?.data?.message || error.message;
        drawerData.value = { 
            summary: `분석 데이터를 불러오는 데 실패했습니다. (Error ${status}: ${msg})`,
            complexity: { time: '-', space: '-', explanation: '서버 연결 확인 필요' }
        };
    } finally {
        drawerLoading.value = false;
    }
};






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
        'javascript': 'js',
        'cpp': 'cpp',
        'c': 'c',
        'kotlin': 'kt',
        'swift': 'swift',
        'go': 'go',
        'rust': 'rs',
        'typescript': 'ts'
    };
    return extensions[language?.toLowerCase()] || 'txt';
};

const currentRecord = ref(null);

const requestTutor = (record) => {
    currentDrawerRecord.value = record;  // Set for Context Card
    currentRecord.value = record;
    drawerType.value = 'tutor';
    drawerTitle.value = `AI 튜터 · ${record.title}`;
    showDrawer.value = true;
    drawerData.value = null;
};

const requestCounterExample = async (record) => {
    currentDrawerRecord.value = record;  // Set for Context Card
    drawerType.value = 'counter_example';
    drawerTitle.value = `AI 반례 분석 · ${record.title}`;
    showDrawer.value = true;
    drawerData.value = null;
    drawerLoading.value = true;
    
    try {
        const res = await aiApi.generateCounterExample({
            problemNumber: String(record.problemNumber),
            code: record.code,
            language: record.language
        });
        drawerData.value = res.data;
    } catch (e) {
        console.error("Failed to generate counter example", e);
        alert("반례 생성에 실패했습니다.");
        showDrawer.value = false;
    } finally {
        drawerLoading.value = false;
    }
};

const closeDrawer = () => {
    showDrawer.value = false;
    drawerData.value = null;
    currentDrawerRecord.value = null;  // Clear Context Card
    
    // Collapse accordion if closing review drawer
    if (drawerType.value === 'review') {
        expandedRecordId.value = null;
    }
};



const openModal = (type, title) => {
    modalType.value = type;
    modalTitle.value = title;
    showModal.value = true;
    modalData.value = null;
};

const closeModal = () => {
    showModal.value = false;
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

* {
  font-family: 'Pretendard', sans-serif;
}

.btn-primary-soft {
  @apply flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-50 text-indigo-600 font-bold hover:bg-indigo-100 transition-colors;
}

.btn-secondary-soft {
  @apply flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-100 text-slate-600 font-bold hover:bg-slate-200 transition-colors;
}

.animate-pop-in {
  animation: popIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: scale(0.95);
}

@keyframes popIn {
  to { opacity: 1; transform: scale(1); }
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: #94a3b8;
}

/* Expand Transition for Accordion */
.expand-enter-active,
.expand-leave-active {
  transition: max-height 0.4s ease-in-out, opacity 0.3s ease-in-out;
  overflow: hidden;
}

.expand-enter-from {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to {
  max-height: 1000px; /* Sufficient height for code viewer */
  opacity: 1;
}

.expand-leave-from {
  max-height: 1000px;
  opacity: 1;
}

.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.5s ease;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(100%);
}
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-100%);
}

.mask-linear {
    mask-image: linear-gradient(to bottom, transparent 0%, black 10%, black 90%, transparent 100%);
    -webkit-mask-image: linear-gradient(to bottom, transparent 0%, black 10%, black 90%, transparent 100%);
}

/* ===== ULTRA Compact Mode - 20년차의 섬세한 최적화 ===== */
/* 50% 너비에서도 아름답고 깔끔한 UI */

/* 헤더를 한 줄로 압축 */
.compact-mode h1 {
  font-size: 1.25rem !important;  /* 더 작게 */
  line-height: 1.5rem !important;
  margin-bottom: 0 !important;
}

.compact-mode h1 + p {
  display: none !important;  /* 서브 텍스트 숨기기 */
}

/* 모든 heading 크기 축소 */
.compact-mode h2 {
  font-size: 0.875rem !important;
  line-height: 1.25rem !important;
  font-weight: 700 !important;
}

.compact-mode h3, .compact-mode h4 {
  font-size: 0.75rem !important;
  font-weight: 600 !important;
}

/* 본문 텍스트 최소화 */
.compact-mode p, .compact-mode span:not(.text-6xl):not(.text-3xl) {
  font-size: 0.6875rem !important;
  line-height: 1rem !important;
}

/* ===== Spacing - Ultra Tight ===== */
.compact-mode .mb-10 {
  margin-bottom: 0.5rem !important;  /* 더 줄임 */
}

.compact-mode .mb-8 {
  margin-bottom: 0.375rem !important;
}

.compact-mode .mb-6 {
  margin-bottom: 0.25rem !important;
}

.compact-mode .mb-4 {
  margin-bottom: 0.25rem !important;
}

.compact-mode .mb-2 {
  margin-bottom: 0.125rem !important;
}

/* Card padding - 더 aggressive */
.compact-mode .p-8 {
  padding: 0.5rem !important;
}

.compact-mode .p-6 {
  padding: 0.5rem !important;
}

.compact-mode .p-5, .compact-mode .p-4 {
  padding: 0.375rem !important;
}

.compact-mode .px-8 {
  padding-left: 0.5rem !important;
  padding-right: 0.5rem !important;
}

.compact-mode .py-6 {
  padding-top: 0.5rem !important;
  padding-bottom: 0.5rem !important;
}

/* Gap - 최소화 */
.compact-mode .gap-8 {
  gap: 0.375rem !important;
}

.compact-mode .gap-6 {
  gap: 0.25rem !important;
}

.compact-mode .gap-4 {
  gap: 0.25rem !important;
}

.compact-mode .gap-3 {
  gap: 0.125rem !important;
}

.compact-mode .gap-2 {
  gap: 0.125rem !important;
}

/* ===== Heatmap - 숨기기 또는 최소화 ===== */
.compact-mode .bg-white.rounded-3xl.p-8.shadow-sm {
  display: none !important;  /* Heatmap 카드 완전히 숨김 */
}

/* Stats flex를 single column으로 */
.compact-mode .xl\:flex-row {
  flex-direction: column !important;
  gap: 0.25rem !important;
}

/* Grid 강제 single column */
.compact-mode .grid-cols-3,
.compact-mode .grid-cols-2,
.compact-mode .md\:grid-cols-3,
.compact-mode .md\:grid-cols-2 {
  grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
}

/* ===== Icon & Visual Elements ===== */
.compact-mode svg {
  width: 0.75rem !important;
  height: 0.75rem !important;
}

/* Large number styling */
.compact-mode .text-6xl {
  font-size: 1.25rem !important;
  line-height: 1.5rem !important;
}

.compact-mode .text-3xl {
  font-size: 1rem !important;
  line-height: 1.25rem !important;
}

.compact-mode .text-2xl {
  font-size: 0.875rem !important;
}

.compact-mode .text-xl {
  font-size: 0.75rem !important;
}

.compact-mode .text-lg {
  font-size: 0.6875rem !important;
}

/* ===== Button & Interactive ===== */
.compact-mode button {
  padding: 0.25rem 0.5rem !important;
  font-size: 0.625rem !important;
  line-height: 1rem !important;
}

.compact-mode .text-xs {
  font-size: 0.5rem !important;
}

/* ===== Borders & Shapes ===== */
.compact-mode .rounded-3xl {
  border-radius: 0.5rem !important;
}

.compact-mode .rounded-2xl {
  border-radius: 0.375rem !important;
}

.compact-mode .rounded-xl {
  border-radius: 0.25rem !important;
}

/* ===== Code Viewer ===== */
.compact-mode pre {
  font-size: 0. 625rem !important;
  padding: 0.375rem !important;
  line-height: 1.1 !important;
}

.compact-mode code {
  font-size: 0.625rem !important;
}

/* ===== Shadows - Minimal ===== */
.compact-mode .shadow-lg,
.compact-mode .shadow-xl,
.compact-mode .shadow-2xl {
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.05) !important;
}

.compact-mode .shadow-sm {
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.03) !important;
}

/* ===== Recent Activity - Compact ===== */
.compact-mode [class*="RECENT"] {
  max-height: 150px !important;
  overflow-y: auto !important;
}

/* ===== Hide less important elements ===== */
.compact-mode .animate-fade-in-down > p {
  display: none !important;  /* 인사말 아래 설명 숨김 */
}

/* Acorns 카드 더 작게 */
.compact-mode [class*="ACORN"] {
  padding: 0.5rem !important;
}

/* ===== Algorithm Cards Optimization ===== */
.compact-mode [class*="algorithm-card"] {
  padding: 0.5rem !important;
}

/* Badge 크기 */
.compact-mode .px-2 {
  padding-left: 0.25rem !important;
  padding-right: 0.25rem !important;
}

.compact-mode .py-1 {
  padding-top: 0.125rem !important;
  padding-bottom: 0.125rem !important;
}

/* ===== Ultra compact for specific elements ===== */
.compact-mode [class*="Activity"] svg,
.compact-mode [class*="TrendingUp"] {
  width: 0.625rem !important;
  height: 0.625rem !important;
}

/* Tight line heights everywhere */
.compact-mode * {
  line-height: 1.3 !important;
}

/* Remove extra top padding from main container */
.compact-mode main {
  padding-top: 0.5rem !important;
}


</style>
