<template>
  <div class="min-h-screen bg-slate-50 text-slate-800">
    <!-- Navbar / Header Area -->



    <main class="container mx-auto px-6 py-8">
      <!-- Header Section -->
      <div class="mb-10 animate-fade-in-down">
        <h1 class="text-3xl font-extrabold text-slate-900 mb-2">
          안녕하세요, <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">{{ user?.username || '탐험가' }}님!</span> 👋
        </h1>
        <p class="text-slate-500">오늘도 알고리즘의 바다를 항해할 준비가 되셨나요?</p>
      </div>

      <!-- Content Grid -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div v-for="i in 6" :key="i" class="h-64 rounded-3xl bg-white shadow-sm border border-slate-100 animate-pulse"></div>
      </div>

      <!-- Stats & Sidebar Area -->
      <div v-if="!loading" class="mb-10 animate-fade-in-up flex flex-col xl:flex-row items-start justify-center gap-6">
        
        <!-- Heatmap Card -->
        <div class="bg-white rounded-3xl p-8 shadow-sm border border-slate-100 relative w-fit max-w-full">
             <div class="flex items-center justify-between mb-6 gap-8">
                <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2 whitespace-nowrap">
                    <TrendingUp class="text-indigo-500" />
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
             
             <div class="overflow-x-auto pb-2 custom-scrollbar">
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

        <!-- Sidebar: Acorn & Ticker -->
        <div class="flex flex-col gap-4 min-w-[220px] shrink-0">
           <!-- Badge -->
           <div v-if="studyData" class="bg-white px-6 py-5 rounded-3xl shadow-sm border border-slate-100 flex flex-col items-center justify-center gap-2 cursor-pointer hover:bg-slate-50 transition-colors group" @click="openAcornHistory">
                <div class="w-12 h-12 rounded-2xl bg-amber-100 group-hover:bg-amber-200 flex items-center justify-center text-3xl shadow-sm transition-colors ring-4 ring-white">🌰</div>
                <div class="flex flex-col leading-none text-center">
                     <span class="text-xs font-bold text-amber-600 uppercase tracking-wider mb-1">Study Acorns</span>
                     <span class="text-3xl font-black text-slate-800">{{ studyData.acornCount }}</span>
                </div>
           </div>

           <!-- Recent Logs List -->
           <div v-if="acornLogs.length > 0" class="bg-white rounded-3xl p-5 shadow-sm border border-slate-100">
                <h3 class="text-xs font-bold text-slate-400 mb-4 uppercase tracking-wider flex items-center gap-2 pb-2 border-b border-slate-100">
                    <Activity :size="12" /> Recent Activity
                </h3>
                <div class="flex flex-col gap-3">
                     <div v-for="log in acornLogs.slice(0, 5)" :key="log.id" class="flex items-center justify-between text-sm">
                         <span class="font-bold text-slate-700 truncate max-w-[100px]">{{ log.username }}</span>
                         <span class="font-bold font-mono" :class="log.amount > 0 ? 'text-green-500' : 'text-red-500'">
                             {{ log.amount > 0 ? '+' : '' }}{{ log.amount }}
                         </span>
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
                @click.stop="requestReview(record)" 
                class="flex-1 md:flex-none flex items-center justify-center gap-2 px-6 py-3 rounded-xl font-bold text-sm transition-colors bg-slate-800 text-white hover:bg-slate-700 shadow-md hover:shadow-lg"
            >
                <Bot :size="18" />
                코드 리뷰
            </button>
            <button 
                @click.stop="requestHint(record)" 
                :disabled="record.runtimeMs > 0 && record.memoryKb > 0"
                class="flex-1 md:flex-none flex items-center justify-center gap-2 px-6 py-3 rounded-xl font-bold text-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                :class="!(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-amber-50 text-amber-600 hover:bg-amber-100' : 'bg-slate-100 text-slate-400'"
            >
                <Lightbulb :size="18" />
                힌트
            </button>
          </div>
        </div>
        </template>
      </div>
    </main>

    <!-- Trendy Modal -->
    <!-- Full Screen Modal (Split View) -->
    <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center p-0 md:p-4">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" @click="closeModal"></div>
      
      <div class="bg-white w-full h-full md:h-[95vh] md:w-[95vw] md:rounded-[32px] overflow-hidden relative shadow-2xl flex flex-col animate-pop-in">
        
        <!-- Modal Header -->
        <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-white z-20 shrink-0">
           <h3 class="text-xl font-black text-slate-800 flex items-center gap-3">
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

        <!-- Split View Content -->
        <div class="flex-1 flex flex-col md:flex-row overflow-hidden">
            
            <!-- Left Panel: Source Code -->
            <div v-if="modalType === 'review'" class="flex w-full md:w-[45%] bg-[#282c34] flex-col border-b md:border-b-0 md:border-r border-slate-200 h-[200px] md:h-auto shrink-0">
                <div class="px-4 py-2 bg-[#21252b] text-slate-400 text-xs font-mono border-b border-[#181a1f] flex justify-between items-center shrink-0">
                    <span>Source Code.java</span>
                    <span class="px-2 py-0.5 rounded bg-white/10 text-slate-300">Read Only</span>
                </div>
                <div class="flex-1 overflow-y-auto custom-scrollbar p-0">
                    <pre class="m-0 p-6 text-sm font-mono leading-relaxed"><code class="hljs language-java" v-html="highlightCodeContent(currentRecordCode || '// 코드를 불러올 수 없습니다.', 'java')"></code></pre>
                </div>
            </div>

            <!-- Right Panel: Analysis & Tabs -->
            <div class="flex-1 flex flex-col bg-slate-50 w-full md:w-[55%] relative h-full overflow-hidden">
                <!-- ... existing right panel content ... -->
                
                <!-- Loading State -->
                <div v-if="modalLoading" class="absolute inset-0 z-30 flex flex-col items-center justify-center bg-white/80 backdrop-blur">
                     <div class="w-16 h-16 border-4 border-indigo-100 border-t-indigo-500 rounded-full animate-spin mb-6"></div>
                     <h4 class="text-lg font-bold text-slate-800">AI가 코드를 정밀 분석중입니다</h4>
                     <p class="text-slate-500">잠시만 기다려주세요...</p>
                </div>

                <!-- Tabs (Only for Review) -->
                <div v-if="modalType === 'review' && modalData" class="flex items-center px-6 border-b border-slate-200 bg-white sticky top-0 z-10">
                    <button @click="activeTab = 'insight'" 
                        class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                        :class="activeTab === 'insight' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                        <LayoutGrid :size="16" /> Insight
                    </button>
                    <button @click="activeTab = 'structure'" 
                        class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                        :class="activeTab === 'structure' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                        <Network :size="16" /> Structure
                    </button>
                    <button @click="activeTab = 'feedback'" 
                        class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                        :class="activeTab === 'feedback' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                        <MessageSquare :size="16" /> Feedback
                    </button>
                </div>

                <!-- Content Area -->
                <div class="flex-1 overflow-y-auto p-6 custom-scrollbar">
                    
                    <!-- REVIEW CONTENT -->
                    <div v-if="modalType === 'review' && modalData">
                        
                        <!-- TAB 1: INSIGHT -->
                        <div v-if="activeTab === 'insight'" class="space-y-6 animate-fade-in">
                             <!-- Summary -->
                             <div class="bg-gradient-to-br from-indigo-50 to-white p-6 rounded-2xl border border-indigo-100 shadow-sm">
                                <h4 class="text-xs font-bold text-indigo-400 uppercase tracking-widest mb-3">Analysis Summary</h4>
                                <p class="text-slate-800 text-lg font-medium leading-relaxed">{{ modalData.summary }}</p>
                             </div>
                             
                             <!-- Complexity Cards -->
                             <div class="grid grid-cols-2 gap-4">
                                 <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex flex-col items-center justify-center text-center">
                                     <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Time Complexity</span>
                                     <div class="text-3xl font-black text-slate-800 mt-2">{{ modalData.complexity?.time || '-' }}</div>
                                 </div>
                                 <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex flex-col items-center justify-center text-center">
                                     <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Space Complexity</span>
                                     <div class="text-3xl font-black text-slate-800 mt-2">{{ modalData.complexity?.space || '-' }}</div>
                                 </div>
                             </div>

                             <!-- Problem & Intuition -->
                             <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="flex items-center gap-2 text-sm font-bold text-slate-800 mb-4">
                                    🎯 문제의 본질 & 직관
                                </h4>
                                <div class="bg-slate-50 p-4 rounded-xl text-slate-600 text-sm mb-4">
                                    {{ modalData.problem?.description }}
                                </div>
                                <div v-if="modalData.algorithm?.intuition" 
                                     class="prose prose-sm prose-slate max-w-none text-slate-600" 
                                     v-html="renderMarkdown(modalData.algorithm.intuition)">
                                </div>
                             </div>
                        </div>

                        <!-- TAB 2: STRUCTURE -->
                        <div v-if="activeTab === 'structure'" class="space-y-6 animate-fade-in">
                            <!-- Code Structure Map -->
                            <div v-if="modalData.structure?.length" class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">🏗️ 코드 구조도</h4>
                                <div class="space-y-2">
                                    <div v-for="(item, idx) in modalData.structure" :key="idx" class="flex items-center gap-4 p-3 rounded-xl bg-slate-50 border border-slate-100">
                                        <div class="w-8 h-8 rounded-lg bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold text-xs shrink-0">
                                            {{ idx + 1 }}
                                        </div>
                                        <div class="flex-1">
                                            <div class="font-bold text-slate-800 text-sm">{{ item.name }}</div>
                                            <div class="text-xs text-slate-500">{{ item.role }}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Trace -->
                            <div v-if="modalData.traceExample?.steps?.length" class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">🔍 실행 추적 (Trace)</h4>
                                <div class="bg-slate-900 rounded-xl p-4 mb-4 font-mono text-xs text-blue-300">
                                    Input: {{ modalData.traceExample.inputExample }}
                                </div>
                                <div class="space-y-4 pl-2">
                                    <div v-for="(step, idx) in modalData.traceExample.steps" :key="idx" class="flex gap-4 relative">
                                        <!-- Vertical Line -->
                                        <div v-if="idx !== modalData.traceExample.steps.length - 1" class="absolute left-[11px] top-6 bottom-[-20px] w-0.5 bg-slate-100"></div>
                                        
                                        <div class="w-6 h-6 rounded-full bg-blue-50 border-2 border-blue-100 flex items-center justify-center shrink-0 z-10">
                                            <div class="w-2 h-2 rounded-full bg-blue-400"></div>
                                        </div>
                                        <div class="text-sm text-slate-600 pt-0.5">{{ step }}</div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- TAB 3: FEEDBACK -->
                        <div v-if="activeTab === 'feedback'" class="space-y-6 animate-fade-in">
                            <!-- Complexity Detail -->
                            <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="text-sm font-bold text-slate-800 mb-4">⏱️ 복잡도 상세 분석</h4>
                                <div v-if="modalData.complexity?.explanation" 
                                     class="prose prose-sm prose-slate max-w-none text-slate-600 bg-slate-50 p-4 rounded-xl"
                                     v-html="renderMarkdown(modalData.complexity.explanation)">
                                </div>
                            </div>

                            <!-- Pitfalls -->
                            <div v-if="modalData.pitfalls?.items?.length" class="bg-red-50 p-6 rounded-2xl border border-red-100">
                                <h4 class="text-red-800 font-bold mb-4 flex items-center gap-2">⚠️ 주의사항 (Pitfalls)</h4>
                                <ul class="space-y-2">
                                    <li v-for="(item, idx) in modalData.pitfalls.items" :key="idx" class="flex gap-3 text-red-700 text-sm bg-white/50 p-3 rounded-xl">
                                        <span>•</span>
                                        <span>{{ item }}</span>
                                    </li>
                                </ul>
                            </div>

                            <!-- Refactor -->
                            <div v-if="modalData.refactor?.code" class="bg-emerald-50 p-6 rounded-2xl border border-emerald-100">
                                <h4 class="text-emerald-800 font-bold mb-4 flex items-center gap-2">✨ 리팩토링 제안</h4>
                                <div class="prose prose-sm max-w-none text-emerald-800 mb-4" v-html="renderMarkdown(modalData.refactor.explanation)"></div>
                                <div class="bg-[#282c34] rounded-xl overflow-hidden shadow-sm">
                                    <div class="px-4 py-2 bg-[#21252b] text-slate-400 text-xs font-mono border-b border-[#181a1f] flex justify-between">
                                        <span>Refactored.java</span>
                                        <button @click="copyCode(modalData.refactor.code)" class="hover:text-white">Copy</button>
                                    </div>
                                    <pre class="m-0 p-4 text-sm font-mono text-emerald-300 overflow-x-auto"><code>{{ modalData.refactor.code }}</code></pre>
                                </div>
                            </div>
                        </div>

                    </div>
                    
                    <!-- HINT CONTENT (Legacy Support) -->
                    <div v-else-if="modalType === 'hint'">
                         <!-- Reuse existing hint UI here or simplify -->
                         <div v-if="!modalData" class="py-10 text-center">
                            <h4 class="font-bold text-slate-800 text-lg mb-4">어떤 힌트가 필요하신가요?</h4>
                            <div class="grid gap-3">
                                 <button @click="requestHintWithLevel(1)" class="p-4 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 text-left">
                                     <div class="font-bold text-slate-800">1단계: 알고리즘 유형 (5🌰)</div>
                                     <div class="text-xs text-slate-500">어떤 알고리즘을 써야 할까요?</div>
                                 </button>
                                 <button @click="requestHintWithLevel(2)" class="p-4 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 text-left">
                                     <div class="font-bold text-slate-800">2단계: 접근 아이디어 (10🌰)</div>
                                     <div class="text-xs text-slate-500">핵심 로직 힌트</div>
                                 </button>
                                 <button @click="requestHintWithLevel(3)" class="p-4 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 text-left">
                                     <div class="font-bold text-slate-800">3단계: 상세 가이드 (15🌰)</div>
                                     <div class="text-xs text-slate-500">코드 구조 및 구현 팁</div>
                                 </button>
                            </div>
                         </div>
                         <div v-else class="p-6 bg-amber-50 rounded-2xl border border-amber-100">
                             <h4 class="font-black text-amber-600 text-xl mb-4">Hint Level {{ modalData.level }}</h4>
                             <p class="whitespace-pre-wrap text-slate-700 leading-relaxed">{{ modalData.hint }}</p>
                             <button @click="modalData = null" class="mt-6 text-sm underline text-slate-500 hover:text-slate-800">Back</button>
                         </div>
                    </div>

                    <!-- ACORN CONTENT (Legacy Support) -->
                    <div v-else-if="modalType === 'acorn'">
                        <!-- Reuse existing acorn UI -->
                         <div v-for="log in modalData" :key="log.id" class="flex justify-between p-4 border-b">
                            <span>{{ log.reason }}</span>
                            <span class="font-bold">{{ log.amount }}</span>
                         </div>
                    </div>

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
  Activity,
  Network
} from 'lucide-vue-next';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css';

// ... (other imports)

const { user } = useAuth();
const records = ref([]);
const studyData = ref(null);
const acornLogs = ref([]);
const loading = ref(true);
const heatmapWeeks = ref([]);
const showModal = ref(false);
const modalType = ref('');
const modalTitle = ref('');
const modalData = ref(null);
const modalLoading = ref(false);
const activeTab = ref('insight');

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
  } catch(e) {
    console.error('Records error:', e);
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
    modalType.value = 'review';
    modalTitle.value = 'AI 코드 분석';
    modalLoading.value = true;
    showModal.value = true;
    modalData.value = null;
    currentRecordCode.value = record.code || '';
    activeTab.value = 'insight';

    try {
        const response = await http.get(`/ai/review/${record.id}`);
        console.log("Analysis Result:", response.data); // Debug
        modalData.value = adaptAnalysisData(response.data);
    } catch (error) {
        console.error("Failed to fetch review:", error);
        const status = error.response?.status || 'Unknown';
        const msg = error.response?.data?.message || error.message;
        modalData.value = { 
            summary: `분석 데이터를 불러오는 데 실패했습니다. (Error ${status}: ${msg})`,
            complexity: { time: '-', space: '-', explanation: '서버 연결 확인 필요' }
        };
    } finally {
        modalLoading.value = false;
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
</style>
