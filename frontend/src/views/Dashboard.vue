<template>
  <div class="min-h-screen bg-slate-50 text-slate-800">
    <!-- Navbar / Header Area -->



    <main class="container mx-auto px-6 py-8">
      <!-- Welcome Section -->
      <div class="mb-10">
        <h1 class="text-3xl font-extrabold text-slate-900 mb-2">
          안녕하세요, <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">탐험가님!</span> 👋
        </h1>
        <p class="text-slate-500">오늘도 알고리즘의 바다를 항해할 준비가 되셨나요?</p>
      </div>

      <!-- Content Grid -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div v-for="i in 6" :key="i" class="h-64 rounded-3xl bg-white shadow-sm border border-slate-100 animate-pulse"></div>
      </div>

      <!-- Stats Overview -->
      <div v-if="!loading" class="mb-10 animate-fade-in-up">
        <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-100 relative overflow-hidden">
             <div class="flex items-center justify-between mb-6">
                <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
                    <TrendingUp class="text-indigo-500" />
                    스터디 활동 로그
                </h2>
                
                <div v-if="studyData" class="flex items-center gap-3 bg-amber-50 px-4 py-2 rounded-xl border border-amber-100 cursor-pointer hover:bg-amber-100 transition-colors" @click="openAcornHistory">
                     <div class="w-8 h-8 rounded-full bg-amber-200 flex items-center justify-center text-xl shadow-sm">🌰</div>
                     <div class="flex flex-col leading-none">
                         <span class="text-xs font-bold text-amber-600 uppercase">ACORNS</span>
                         <span class="text-lg font-black text-slate-800">{{ studyData.acornCount }}</span>
                     </div>
                </div>
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
             
             <!-- Heatmap (Grass) -->
             <div class="overflow-x-auto pb-2">
                <div class="flex gap-[3px] min-w-max">
                    <div v-for="(week, wIdx) in heatmapWeeks" :key="wIdx" class="flex flex-col gap-[3px]">
                        <div 
                            v-for="(day, dIdx) in week" 
                            :key="dIdx"
                            class="w-3 h-3 rounded-[2px] transition-all relative group cursor-pointer"
                            :class="day.colorClass"
                        >
                            <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 hidden group-hover:block z-20 min-w-max pointer-events-none">
                                <div class="bg-slate-800 text-white text-xs rounded-lg py-2 px-3 shadow-xl flex flex-col items-center gap-1">
                                    <span class="font-bold text-slate-200">{{ day.dateFormatted }}</span>
                                    <span class="font-bold">{{ day.count }} solutions</span>
                                    <div v-if="day.count > 0" class="flex flex-wrap gap-1 max-w-[150px] justify-center mt-1 border-t border-slate-700 pt-1">
                                        <span v-for="name in day.contributors" :key="name" class="text-[10px] bg-slate-700 px-1.5 py-0.5 rounded text-indigo-300">
                                            {{ name }}
                                        </span>
                                    </div>
                                </div>
                                <div class="w-2 h-2 bg-slate-800 transform rotate-45 absolute -bottom-1 left-1/2 -translate-x-1/2"></div>
                            </div>
                        </div>
                    </div>
                </div>
             </div>
        </div>
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
        <div 
          v-for="record in records" 
          :key="record.id" 
          class="group relative rounded-3xl p-6 shadow-sm border transition-all duration-300 flex flex-col md:flex-row items-center gap-6 hover:shadow-xl hover:-translate-y-1"
          :class="[
            (record.runtimeMs > 0 && record.memoryKb > 0) 
              ? 'bg-white border-slate-100 hover:border-indigo-200' 
              : 'bg-red-50/30 border-red-100 hover:border-red-300 hover:bg-red-50/50'
          ]"
        >
          <!-- Left: Info -->
          <div class="flex-1 w-full md:w-auto text-left">
            <div class="flex items-center gap-2 mb-2">
               <a 
                 :href="`https://www.acmicpc.net/problem/${record.problemNumber}`" 
                 target="_blank"
                 class="flex items-center gap-1 text-xs font-bold text-indigo-500 bg-indigo-50 px-2 py-1 rounded hover:bg-indigo-100 transition-colors"
               >
                 #{{ record.problemNumber }}
                 <ExternalLink :size="10" />
               </a>
               <span class="px-2 py-0.5 rounded-full text-xs font-bold bg-slate-100 text-slate-600 border border-slate-200 uppercase tracking-wider">
                 {{ record.language }}
               </span>
               <span v-if="!(record.runtimeMs > 0 && record.memoryKb > 0)" class="px-2 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-600 border border-red-200 uppercase tracking-wider">
                 FAILED
               </span>
            </div>
            <h3 class="text-xl font-bold text-slate-800 leading-snug truncate group-hover:text-indigo-600 transition-colors">
              {{ record.title }}
            </h3>
            <div class="flex items-center gap-2 mt-2">
                 <div class="flex items-center gap-1.5 px-2 py-1 bg-slate-100 rounded-md">
                    <div class="w-5 h-5 rounded-full bg-indigo-100 flex items-center justify-center text-[10px] font-bold text-indigo-600">
                        {{ (record.username || '?').charAt(0).toUpperCase() }}
                    </div>
                    <span class="text-xs font-bold text-slate-600">{{ record.username || 'Unknown' }}</span>
                 </div>
                 <div class="text-xs text-slate-400 flex items-center gap-1">
                   {{ formatDate(record.committedAt) }}
                 </div>
            </div>
          </div>

          <!-- Middle: Stats & Analysis -->
          <div class="flex items-center gap-3 w-full md:w-auto shrink-0 flex-wrap justify-center md:justify-start">
            <!-- Runtime -->
            <div class="flex-1 md:flex-none rounded-2xl px-5 py-3 flex items-center justify-center gap-2 border min-w-[100px]"
                 :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-slate-50 border-slate-100' : 'bg-red-50 border-red-100'">
              <Zap :size="18" :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'text-amber-400' : 'text-red-400'" />
              <span class="text-sm font-bold" :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'text-slate-700' : 'text-red-700'">{{ record.runtimeMs }}ms</span>
            </div>
            
            <!-- Memory -->
            <div class="flex-1 md:flex-none rounded-2xl px-5 py-3 flex items-center justify-center gap-2 border min-w-[100px]"
                 :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-slate-50 border-slate-100' : 'bg-red-50 border-red-100'">
              <Database :size="18" :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'text-blue-400' : 'text-red-400'" />
              <span class="text-sm font-bold" :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'text-slate-700' : 'text-red-700'">{{ record.memoryKb }}KB</span>
            </div>

            <!-- Analysis: Score -->
            <div v-if="record.score" class="flex-1 md:flex-none rounded-2xl px-5 py-3 flex items-center justify-center gap-2 border min-w-[80px] bg-purple-50 border-purple-100">
              <Trophy :size="18" class="text-purple-500" />
              <span class="text-sm font-bold text-purple-700">{{ record.score }}점</span>
            </div>
            
            <!-- Analysis: Complexity -->
            <div v-if="record.timeComplexity" class="flex-1 md:flex-none rounded-2xl px-5 py-3 flex items-center justify-center gap-2 border min-w-[100px] bg-sky-50 border-sky-100">
               <Activity :size="18" class="text-sky-500" />
               <span class="text-sm font-bold text-sky-700">{{ record.timeComplexity }}</span>
            </div>
          </div>

          <!-- Right: Actions -->
          <div class="flex items-center gap-2 w-full md:w-auto shrink-0">
             <button 
                @click="toggleCode(record.id)" 
                class="flex-1 md:flex-none flex items-center justify-center gap-2 px-6 py-3 rounded-xl font-bold text-sm transition-colors"
                :class="expandedCodeId === record.id ? 'bg-slate-800 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
            >
                <Code2 :size="18" />
                <span v-if="expandedCodeId === record.id">숨기기</span>
                <span v-else>코드</span>
            </button>
            <button 
                @click="requestHint(record)" 
                class="flex-1 md:flex-none flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-amber-50 text-amber-600 font-bold text-sm hover:bg-amber-100 transition-colors"
            >
                <Lightbulb :size="18" />
                hints
            </button>
          </div>
        </div>
        
        <!-- Expanded Code & Detailed Analysis Section -->
        <div v-if="expandedCodeId === record.id" class="mt-[-20px] mb-4 bg-slate-50 rounded-b-3xl border-x border-b border-slate-100 p-6 animate-fade-in mx-4">
             <!-- Analysis Badge List -->
             <div v-if="record.patterns" class="flex flex-wrap gap-2 mb-4">
                 <span v-for="tag in parsePatterns(record.patterns)" :key="tag" class="px-3 py-1 bg-white border border-slate-200 rounded-full text-xs font-bold text-slate-600 shadow-sm">
                   #{{ tag }}
                 </span>
                 <span v-if="record.complexityExplanation" class="px-3 py-1 bg-sky-100 text-sky-700 rounded-full text-xs font-bold shadow-sm">
                   💡 {{ record.complexityExplanation }}
                 </span>
             </div>

             <!-- Code Block -->
             <div class="bg-slate-900 rounded-2xl overflow-hidden shadow-inner relative group">
                 <div class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button @click="copyCode(record.code)" class="text-xs bg-white/10 text-white px-2 py-1 rounded hover:bg-white/20">Copy</button>
                 </div>
                 <pre class="p-6 text-sm font-mono text-green-400 overflow-x-auto selection:bg-green-900 selection:text-white"><code>{{ record.code || '// No code available' }}</code></pre>
             </div>
        </div>
      </div>
    </main>

    <!-- Trendy Modal -->
    <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" @click="closeModal"></div>
      <div class="bg-white rounded-[32px] w-full max-w-2xl max-h-[85vh] overflow-hidden relative shadow-2xl flex flex-col animate-pop-in">
        
        <!-- Modal Header -->
        <div class="px-8 py-6 border-b border-slate-100 flex justify-between items-center bg-white/80 backdrop-blur z-10 sticky top-0">
           <h3 class="text-2xl font-black text-slate-800 flex items-center gap-3">
             <div class="p-2 rounded-xl" :class="modalType === 'review' ? 'bg-indigo-100 text-indigo-600' : 'bg-amber-100 text-amber-600'">
                <Bot v-if="modalType === 'review'" :size="24" />
                <Lightbulb v-else :size="24" />
             </div>
             {{ modalTitle }}
           </h3>
           <button @click="closeModal" class="p-2 rounded-full hover:bg-slate-100 text-slate-400 transition-colors">
             <X :size="24" />
           </button>
        </div>

        <!-- Modal Content -->
        <div class="p-8 overflow-y-auto custom-scrollbar">
            <!-- Loading -->
            <div v-if="modalLoading" class="py-20 flex flex-col items-center justify-center text-center">
                 <div class="w-16 h-16 border-4 border-indigo-100 border-t-indigo-500 rounded-full animate-spin mb-6"></div>
                 <h4 class="text-lg font-bold text-slate-800">AI가 분석중입니다</h4>
                 <p class="text-slate-500">잠시만 기다려주세요...</p>
            </div>

            <!-- Review Data -->
            <div v-else-if="modalType === 'review' && modalData" class="space-y-6">
                 <!-- Summary -->
                 <div class="bg-indigo-50/50 p-6 rounded-3xl border border-indigo-100">
                    <h4 class="text-sm font-bold text-indigo-900 uppercase tracking-wide mb-3">📝 분석 요약</h4>
                    <p class="text-slate-700 leading-relaxed">{{ modalData.summary }}</p>
                 </div>

                 <!-- Problem Info -->
                 <div v-if="modalData.problem" class="bg-slate-50 p-5 rounded-2xl border border-slate-100">
                    <h4 class="text-sm font-bold text-slate-800 uppercase tracking-wide mb-3">🎯 문제 분석</h4>
                    <p class="text-slate-600 mb-2">{{ modalData.problem.description }}</p>
                    <div v-if="modalData.problem.isGuess" class="text-xs text-amber-600 bg-amber-50 px-2 py-1 rounded inline-block">
                      ⚠️ {{ modalData.problem.guessReason }}
                    </div>
                 </div>

                 <!-- Algorithm Patterns -->
                 <div v-if="modalData.algorithm?.patterns?.length" class="bg-purple-50 p-5 rounded-2xl border border-purple-100">
                    <h4 class="text-sm font-bold text-purple-800 uppercase tracking-wide mb-3">🧩 알고리즘 패턴</h4>
                    <div class="flex flex-wrap gap-2 mb-3">
                      <span v-for="(pattern, idx) in modalData.algorithm.patterns" :key="idx" 
                            class="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-sm font-medium">
                        {{ pattern }}
                      </span>
                    </div>
                    <p v-if="modalData.algorithm.intuition" class="text-slate-600 text-sm">{{ modalData.algorithm.intuition }}</p>
                 </div>

                 <!-- Complexity -->
                 <div class="grid grid-cols-2 gap-4">
                     <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
                         <span class="text-xs font-bold text-slate-400 uppercase">⏱️ Time</span>
                         <div class="text-xl font-black text-slate-800 mt-1">{{ modalData.complexity?.time || '-' }}</div>
                     </div>
                     <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm">
                         <span class="text-xs font-bold text-slate-400 uppercase">💾 Space</span>
                         <div class="text-xl font-black text-slate-800 mt-1">{{ modalData.complexity?.space || '-' }}</div>
                     </div>
                 </div>

                 <!-- Key Code Blocks -->
                 <div v-if="modalData.keyBlocks?.length">
                     <h4 class="flex items-center gap-2 text-lg font-bold text-slate-800 mb-4">
                         💡 핵심 코드 블록
                     </h4>
                     <div class="space-y-3">
                         <div v-for="(block, idx) in modalData.keyBlocks" :key="idx" class="bg-slate-900 rounded-xl overflow-hidden">
                             <pre class="p-4 text-sm text-green-400 overflow-x-auto"><code>{{ block.code }}</code></pre>
                             <div class="px-4 pb-3 text-slate-400 text-sm">{{ block.explanation }}</div>
                         </div>
                     </div>
                 </div>

                 <!-- Pitfalls -->
                 <div v-if="modalData.pitfalls?.items?.length">
                     <h4 class="flex items-center gap-2 text-lg font-bold text-red-600 mb-4">
                         ⚠️ 주의할 점
                     </h4>
                     <ul class="space-y-2">
                         <li v-for="(item, idx) in modalData.pitfalls.items" :key="idx" class="flex items-start gap-3 bg-red-50 p-3 rounded-xl border border-red-100">
                             <div class="w-1.5 h-1.5 rounded-full bg-red-400 mt-2 shrink-0"></div>
                             <span class="text-slate-700 text-sm">{{ item }}</span>
                         </li>
                     </ul>
                 </div>

                 <!-- Improvements -->
                 <div v-if="modalData.pitfalls?.improvements?.length">
                     <h4 class="flex items-center gap-2 text-lg font-bold text-slate-800 mb-4">
                         <TrendingUp class="text-green-500" /> 개선 포인트
                     </h4>
                     <ul class="space-y-2">
                         <li v-for="(item, idx) in modalData.pitfalls.improvements" :key="idx" class="flex items-start gap-3 bg-green-50 p-3 rounded-xl border border-green-100">
                             <div class="w-1.5 h-1.5 rounded-full bg-green-400 mt-2 shrink-0"></div>
                             <span class="text-slate-700 text-sm">{{ item }}</span>
                         </li>
                     </ul>
                 </div>
            </div>

            <!-- Hint Selection -->
            <div v-else-if="modalType === 'hint' && !modalData" class="space-y-4">
                 <div class="text-center mb-6">
                     <p class="text-slate-600">
                         도토리를 사용하여 힌트를 확인할 수 있습니다.<br>
                         높은 단계일수록 더 강력한 힌트가 제공됩니다.
                     </p>
                 </div>
                 
                 <div class="grid grid-cols-1 gap-3">
                     <button @click="requestHintWithLevel(1)" 
                             class="flex items-center justify-between p-4 rounded-xl border-2 border-slate-100 hover:border-amber-300 hover:bg-amber-50 transition-all group"
                             :disabled="modalLoading">
                         <div class="flex flex-col text-left">
                             <span class="font-bold text-slate-800">Level 1. 알고리즘 유형</span>
                             <span class="text-xs text-slate-500">어떤 알고리즘을 써야 할까요?</span>
                         </div>
                         <div class="flex items-center gap-1 bg-amber-100 px-3 py-1.5 rounded-lg text-amber-700 font-bold text-sm">
                             <span>🌰</span> 5 
                         </div>
                     </button>

                     <button @click="requestHintWithLevel(2)" 
                             class="flex items-center justify-between p-4 rounded-xl border-2 border-slate-100 hover:border-amber-300 hover:bg-amber-50 transition-all group"
                             :disabled="modalLoading">
                         <div class="flex flex-col text-left">
                             <span class="font-bold text-slate-800">Level 2. 접근 방법</span>
                             <span class="text-xs text-slate-500">문제 풀이의 핵심 아이디어는?</span>
                         </div>
                         <div class="flex items-center gap-1 bg-amber-100 px-3 py-1.5 rounded-lg text-amber-700 font-bold text-sm">
                             <span>🌰</span> 10 
                         </div>
                     </button>

                     <button @click="requestHintWithLevel(3)" 
                             class="flex items-center justify-between p-4 rounded-xl border-2 border-slate-100 hover:border-amber-300 hover:bg-amber-50 transition-all group"
                             :disabled="modalLoading">
                         <div class="flex flex-col text-left">
                             <span class="font-bold text-slate-800">Level 3. 상세 가이드</span>
                             <span class="text-xs text-slate-500">코드 구조와 구현 팁까지!</span>
                         </div>
                         <div class="flex items-center gap-1 bg-amber-100 px-3 py-1.5 rounded-lg text-amber-700 font-bold text-sm">
                             <span>🌰</span> 15 
                         </div>
                     </button>
                 </div>
            </div>

            <!-- Hint Data Display -->
            <div v-else-if="modalType === 'hint' && modalData" class="space-y-8">
                <div class="text-center py-6">
                    <div class="inline-block p-4 rounded-full bg-amber-100 text-amber-500 mb-4">
                        <Lightbulb :size="48" />
                    </div>
                    <h4 class="text-2xl font-bold text-slate-800 mb-4">Hint Level {{ modalData.level }}</h4>
                    <div class="bg-amber-50 p-6 rounded-2xl text-left border border-amber-100">
                        <p class="text-lg text-slate-700 font-medium leading-relaxed whitespace-pre-wrap">
                            {{ modalData.hint }}
                        </p>
                    </div>
                </div>
                <div class="text-center">
                    <button @click="modalData = null" class="text-slate-500 hover:text-slate-800 text-sm font-medium underline">
                        다른 힌트 보기
                    </button>
                </div>
            </div>

            <!-- Acorn History -->
            <div v-else-if="modalType === 'acorn'" class="space-y-4">
                 <div v-if="!modalData || modalData.length === 0" class="text-center py-10 text-slate-400">
                     내역이 없습니다.
                 </div>
                 <div v-else v-for="log in modalData" :key="log.id" class="flex items-center justify-between p-4 bg-slate-50 rounded-2xl border border-slate-100">
                     <div class="flex items-center gap-3">
                         <div class="w-10 h-10 rounded-full flex items-center justify-center text-lg shadow-sm"
                              :class="log.amount > 0 ? 'bg-amber-100 text-amber-600' : 'bg-slate-200 text-slate-500'">
                             {{ log.amount > 0 ? '🌰' : '💸' }}
                         </div>
                         <div>
                             <p class="font-bold text-slate-800">{{ log.reason }}</p>
                             <div class="flex items-center gap-2 text-xs text-slate-400 font-medium">
                                 <span>{{ log.username }}</span>
                                 <span>•</span>
                                 <span>{{ formatDate(log.createdAt) }}</span>
                             </div>
                         </div>
                     </div>
                     <span class="font-black text-lg" :class="log.amount > 0 ? 'text-amber-500' : 'text-slate-400'">
                         {{ log.amount > 0 ? '+' : '' }}{{ log.amount }}
                     </span>
                 </div>
            </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { dashboardApi } from '../api/dashboard';
import { studyApi } from '../api/study';
import { useAuth } from '../composables/useAuth';
import http from '../api/http';
import { 
  Bot, 
  Lightbulb, 
  Zap, 
  Database, 
  ExternalLink,
  Code2,
  X,
  TrendingUp,
  LayoutGrid,
  Youtube,
  Nut,
  Trophy,
  Activity
} from 'lucide-vue-next';

// ... (other imports)

const { user } = useAuth();
const records = ref([]);
const studyData = ref(null);
const acornLogs = ref([]);
const loading = ref(true);
// ... (other refs)

onMounted(async () => {
  try {
    const recordsRes = await dashboardApi.getRecords();
    records.value = recordsRes.data;
  } catch(e) {
    console.error('Records error:', e);
  }
  
  if (user.value?.studyId) {
      try {
          const studyRes = await studyApi.get(user.value.studyId);
          studyData.value = studyRes.data;
      } catch (e) {
          console.error("Study fetch error", e);
      }
  }

  try {
    const heatmapRes = await dashboardApi.getHeatmap();
    processHeatmap(heatmapRes.data || []);
  } catch(e) {
    console.error('Heatmap error:', e);
    processHeatmap([]);
  }
  
  loading.value = false;
});

const openAcornHistory = async () => {
    openModal('acorn', '도토리 활동 내역');
    modalLoading.value = true;
    try {
        if (user.value?.studyId) {
            const res = await studyApi.getAcornLogs(user.value.studyId);
            modalData.value = res.data;
        }
    } catch (e) {
        modalData.value = [];
    } finally {
        modalLoading.value = false;
    }
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

const requestReview = async (record) => {
    // Deprecated: Review is now automatic.
    // Keeping function structure if called by legacy, but UI shouldn't call it.
    console.warn("Manual review is deprecated.");
};


const expandedCodeId = ref(null);

const toggleCode = (id) => {
    if (expandedCodeId.value === id) {
        expandedCodeId.value = null;
    } else {
        expandedCodeId.value = id;
    }
};

const copyCode = (text) => {
    navigator.clipboard.writeText(text).then(() => {
        // Optional: Toast notification
        console.log("Copied to clipboard");
    });
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

const currentRecord = ref(null);

const requestHint = (record) => {
    currentRecord.value = record;
    openModal('hint', '스마트 힌트 (도토리 사용)');
};

const requestHintWithLevel = async (level) => {
    if (!currentRecord.value) return;
    
    modalLoading.value = true;
    try {
        const res = await http.post('/ai/hint', {
            userId: currentRecord.value.userId, 
            problemNumber: String(currentRecord.value.problemNumber),
            problemTitle: currentRecord.value.title,
            level: level
        });
        modalData.value = { ...res.data, level };
        
        // Refresh study data to update acorn count
        if (user.value?.studyId) {
            const studyRes = await studyApi.get(user.value.studyId);
            studyData.value = studyRes.data;
        }
    } catch(e) {
        alert('도토리가 부족하거나 오류가 발생했습니다.');
        console.error(e);
    } finally {
        modalLoading.value = false;
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
</style>
