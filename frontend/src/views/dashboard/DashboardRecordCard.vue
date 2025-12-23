<template>
  <div 
    class="group relative bg-white rounded-xl shadow-sm overflow-hidden transition-all duration-300 border-2"
    :class="{ 'hover:shadow-md hover:-translate-y-0.5': !expanded, 'shadow-xl': expanded, ...cardBorderClass }"
  >
    <!-- STATUS HEADER BAR -->
    <div :class="statusHeaderClass" class="px-4 py-2 flex items-center gap-2 text-sm font-bold">
      <span>{{ isPassed ? '✅' : '❌' }}</span>
      <span>{{ isPassed ? 'PASSED' : 'FAILED' }}</span>
      <span v-if="taskTypeLabel" class="ml-1">[{{ taskTypeLabel }}]</span>
      <span v-if="record.tag === 'DEFENSE' && defenseStreak > 0" class="ml-auto text-xs">🔥 {{ defenseStreak}}연승</span>
      <div v-if="!expanded" class="ml-auto text-xs opacity-50">{{ formatDate(record.committedAt) }}</div>
    </div>

    <!-- Header / Collapsed View -->
    <div class="flex flex-col xl:flex-row gap-6 p-5 cursor-pointer" @click="toggleExpand">
      <!-- Main Content Section -->
      <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between gap-4 mb-2">
              <div class="flex flex-col">
                  <!-- Badges -->
                  <div class="flex flex-wrap items-center gap-2 mb-2">
                    <span 
                        class="px-2.5 py-1 rounded-lg text-[11px] font-black uppercase tracking-wider bg-slate-100 text-slate-600"
                    >
                        #{{ record.problemNumber }}
                    </span>
                    <span 
                        class="px-2.5 py-1 rounded-lg text-[11px] font-black uppercase tracking-wider"
                        :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-slate-100 text-slate-600' : 'bg-red-100 text-red-600'"
                    >
                        {{ record.language }}
                    </span>
                    <!-- Pattern Tags (Korean only) -->
                    <span v-for="pattern in extractPatterns(record.patterns)" :key="pattern"
                          class="px-2 py-0.5 bg-indigo-50 text-indigo-600 text-[10px] font-bold rounded border border-indigo-100">
                        {{ extractKorean(pattern) }}
                    </span>
                  </div>
                  <!-- Title -->
                  <h3 class="text-lg md:text-xl font-bold text-slate-800 leading-tight group-hover:text-indigo-600 transition-colors">
                      {{ record.title }}
                  </h3>
              </div>
               <div class="text-slate-400">
                  <ChevronDown v-if="!expanded" :size="20" />
                  <ChevronUp v-else :size="20" class="text-indigo-500" />
               </div>
          </div>
      </div>
    </div>

    <!-- Expanded View: Code Viewer Only (Side panels teleported) -->
    <div v-if="expanded" class="border-t border-slate-100 animate-slide-down bg-slate-50 relative">
        <div class="flex flex-col">
            
            <!-- CENTER PANEL: Code Viewer (Full Width) -->
            <div class="bg-slate-900 flex flex-col min-w-0">
                <div class="h-[48px] bg-slate-800 flex items-center px-4 justify-between border-b border-slate-700 sticky top-16 z-20 shadow-md">
                    <span class="text-xs font-bold text-slate-300">SOURCE CODE</span>
                    <div class="flex items-center gap-3 text-[11px] text-slate-400">
                        <span v-if="record.runtimeMs > 0">{{ record.runtimeMs }}ms</span>
                        <span v-if="record.memoryKb > 0">{{ Math.round(record.memoryKb / 1024) }}MB</span>
                    </div>
                </div>
                <div class="relative">
                     <CodeViewer
                        ref="codeViewerRef"
                        :code="record.code"
                        :language="record.language || 'java'"
                        :filename="`${record.title}.${getExtension(record.language)}`"
                        :comments="comments"
                        :keyBlocks="parsedKeyBlocks"
                        @submit-comment="submitLineComment"
                        :readOnly="false" 
                      />
                </div>
            </div>

            <!-- TELEPORTED LEFT PANEL: Context & KeyBlocks (Fixed to Left) -->
            <Teleport to="body">
                <div v-if="expanded" class="hidden xl:flex flex-col fixed top-[6.5rem] left-8 w-[580px] h-[calc(100vh-8rem)] bg-white rounded-xl border border-slate-200 shadow-xl overflow-hidden z-50 animate-fade-in-left">
                    <div class="p-3 bg-white border-b border-slate-200 font-bold text-xs text-slate-600 flex items-center gap-2 h-[48px] shrink-0 bg-slate-50">
                        <LayoutList :size="14" /> 구조 및 핵심
                    </div>
                    <div class="flex-1 overflow-y-auto p-3 custom-scrollbar space-y-4">
                        <!-- Key Blocks -->
                        <div v-if="parsedKeyBlocks.length > 0" class="space-y-3">
                            <div v-for="(block, idx) in parsedKeyBlocks" :key="idx" 
                                class="bg-white border border-slate-200 rounded-lg p-3 shadow-sm hover:border-indigo-300 transition-colors cursor-pointer group"
                                @click="scrollToLine(block.startLine)"
                            >
                                <div class="flex items-center justify-between mb-2">
                                    <span class="text-[10px] font-bold text-indigo-600 opacity-0 group-hover:opacity-100 transition-opacity">이동</span>
                                </div>
                                <h5 class="text-xs font-bold text-slate-800 mb-1">{{ block.role || `Lines ${block.startLine} ~ ${block.endLine}` }}</h5>
                                <p class="text-[11px] text-slate-500 leading-relaxed">{{ block.explanation }}</p>
                            </div>
                        </div>
                        <div v-else class="text-center py-10 text-slate-400">
                            <Loader2 class="w-6 h-6 mx-auto mb-2 opacity-50 animate-spin" v-if="loadingBoard"/>
                            <span class="text-xs">{{ loadingBoard ? '분석 중...' : '핵심 블록 정보 없음' }}</span>
                        </div>
                    </div>
                </div>
            </Teleport>

            <!-- TELEPORTED RIGHT PANEL: Tabbed Insights (Fixed to Right) -->
            <Teleport to="body">
                <div v-if="expanded" class="hidden xl:flex flex-col fixed top-[6.5rem] right-8 w-[580px] h-[calc(100vh-8rem)] bg-white rounded-xl border border-slate-200 shadow-xl overflow-hidden z-50 animate-fade-in-right">
                    <!-- Tabs Header -->
                    <div class="flex border-b border-slate-200 h-[48px] shrink-0 bg-slate-50">
                        <button 
                            v-for="tab in tabs" 
                            :key="tab.id"
                            @click="activeTab = tab.id"
                            class="flex-1 flex items-center justify-center text-xs font-bold transition-colors relative"
                            :class="activeTab === tab.id ? 'text-indigo-600 bg-indigo-50/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-50'"
                        >
                            {{ tab.label }}
                            <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 w-full h-0.5 bg-indigo-600"></div>
                        </button>
                    </div>

                    <!-- Tab Content -->
                    <div class="flex-1 overflow-y-auto p-0 custom-scrollbar bg-slate-50">
                        
                        <!-- 1. OVERVIEW TAB -->
                        <div v-if="activeTab === 'overview'" class="p-5 space-y-6">
                            <!-- Core Idea -->
                            <div v-if="record.algorithmIntuition" class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                                <h4 class="text-xs font-bold text-slate-800 mb-3 flex items-center gap-2">
                                    <Lightbulb :size="14" class="text-amber-500"/> 핵심 아이디어
                                </h4>
                                <p class="text-sm text-slate-700 leading-relaxed whitespace-pre-line">{{ record.algorithmIntuition }}</p>
                            </div>
                            
                            <!-- Pattern Tags Detail -->
                            <div v-if="extractPatterns(record.patterns).length > 0" class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                                <h4 class="text-xs font-bold text-slate-800 mb-3 flex items-center gap-2">
                                    <Tag :size="14" class="text-indigo-500"/> 알고리즘 & 자료구조
                                </h4>
                                <div class="flex flex-wrap gap-2">
                                    <span v-for="pattern in extractPatterns(record.patterns)" :key="pattern"
                                          class="px-2.5 py-1 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-lg border border-indigo-100">
                                        {{ extractKorean(pattern) }}
                                    </span>
                                </div>
                            </div>

                            <!-- Comments Preview -->
                            <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                                <h4 class="text-xs font-bold text-slate-800 mb-3 flex items-center gap-2">
                                    <MessageSquare :size="14" class="text-slate-400"/> 댓글 ({{ comments.length }})
                                </h4>
                                <div  v-for="comment in comments" :key="comment.id" class="text-xs text-slate-600 bg-slate-50 p-2 rounded mb-2 last:mb-0">
                                    <span class="font-bold">{{ comment.author || 'User' }}:</span> {{ comment.content }}
                                </div>
                                <div v-if="comments.length === 0" class="text-center text-xs text-slate-400 py-2">
                                    등록된 댓글이 없습니다.
                                </div>
                            </div>
                        </div>

                        <!-- 2. ANALYSIS TAB -->
                        <div v-if="activeTab === 'analysis'" class="p-5 space-y-6">
                            <!-- Complexity Cards -->
                            <div class="grid grid-cols-2 gap-3">
                                <div class="bg-white p-3 rounded-xl border border-slate-200 shadow-sm text-center">
                                    <div class="text-[10px] text-slate-400 font-bold uppercase mb-1">시간 복잡도</div>
                                    <div class="text-sm font-black text-slate-800 font-mono">{{ record.timeComplexity || '-' }}</div>
                                </div>
                                <div class="bg-white p-3 rounded-xl border border-slate-200 shadow-sm text-center">
                                    <div class="text-[10px] text-slate-400 font-bold uppercase mb-1">공간 복잡도</div>
                                    <div class="text-sm font-black text-slate-800 font-mono">{{ record.spaceComplexity || '-' }}</div>
                                </div>
                            </div>

                            <!-- Pitfalls -->
                            <div v-if="parsedPitfalls.length > 0" class="space-y-3">
                                <h4 class="text-xs font-bold text-rose-600 flex items-center gap-2">
                                    <Bug :size="14"/> 주의사항
                                </h4>
                                <ul class="space-y-2">
                                    <li v-for="(pitfall, idx) in parsedPitfalls" :key="idx" 
                                        class="bg-rose-50 p-3 rounded-lg border border-rose-100 text-xs text-slate-700 leading-relaxed">
                                        <span v-html="renderMarkdown(pitfall)" class="prose prose-sm max-w-none"></span>
                                    </li>
                                </ul>
                            </div>

                            <!-- Refactoring -->
                            <div v-if="record.refactorProvided" class="space-y-3">
                                <h4 class="text-xs font-bold text-emerald-600 flex items-center gap-2">
                                    <Wand2 :size="14"/> 리팩토링 제안
                                </h4>
                                <div class="bg-emerald-50 p-3 rounded-lg border border-emerald-100">
                                    <div class="text-xs text-slate-700 leading-relaxed mb-3" v-html="renderMarkdown(record.refactorExplanation)"></div>
                                    <pre v-if="record.refactorCode" class="bg-slate-800 text-slate-300 p-2 rounded text-[10px] overflow-x-auto custom-scrollbar font-mono">{{ record.refactorCode }}</pre>
                                </div>
                            </div>
                            <div v-if="!hasAnyAnalysis" class="text-center py-10 text-slate-400">
                                <Activity class="w-8 h-8 mx-auto mb-2 opacity-20" />
                                <span class="text-xs">분석 데이터가 없습니다</span>
                            </div>
                        </div>

                        <!-- 3. COUNTER TAB -->
                        <div v-if="activeTab === 'counter'" class="p-5 flex flex-col h-full">
                            <div v-if="isPassed" class="flex flex-col items-center justify-center flex-1 text-center p-6 text-slate-400 bg-slate-100 rounded-xl border border-dashed border-slate-300">
                                <CheckCircle2 :size="32" class="mb-2 text-emerald-500 opacity-50"/>
                                <p class="text-xs font-bold text-slate-600 mb-1">이미 해결된 문제입니다!</p>
                                <p class="text-[10px]">정답 코드는 반례 생성 기능을 사용할 수 없습니다.</p>
                            </div>
                            <div v-else class="flex flex-col h-full">
                                <div v-if="!aiData && !loadingAi" class="flex-1 flex flex-col items-center justify-center text-center">
                                    <button @click="findCounterExample" class="px-5 py-2.5 bg-rose-500 hover:bg-rose-600 text-white rounded-lg font-bold text-xs shadow-lg shadow-rose-200 transition-all flex items-center gap-2">
                                        <Bug :size="14"/> 반례 생성하기
                                    </button>
                                    <p class="text-[10px] text-slate-400 mt-3">AI가 코드를 분석하여 실패 원인을 찾아냅니다.</p>
                                </div>
                                
                                <div v-if="loadingAi" class="flex-1 flex flex-col items-center justify-center text-slate-400">
                                    <Loader2 class="w-8 h-8 mb-2 animate-spin text-rose-500" />
                                    <span class="text-xs">반례를 찾고 있습니다...</span>
                                </div>
                                
                                <div v-if="aiData" class="space-y-4 animate-fade-in-up">
                                    <div class="bg-rose-50 border border-red-100 rounded-xl p-4">
                                        <div class="text-[10px] font-bold text-slate-400 mb-1 uppercase">입력 (Input)</div>
                                        <pre class="bg-white p-2 rounded border border-rose-100 text-xs font-mono text-slate-700 overflow-x-auto">{{ aiData.input }}</pre>
                                        <div class="mt-3 text-[10px] font-bold text-slate-400 mb-1 uppercase">예상 출력 (Expected)</div>
                                        <pre class="bg-white p-2 rounded border border-rose-100 text-xs font-mono text-slate-700 overflow-x-auto">{{ aiData.expectedOutput }}</pre>
                                    </div>
                                    <div class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
                                        <div class="text-xs font-bold text-indigo-600 mb-2">💡 분석 결과</div>
                                        <div class="text-xs text-slate-700 leading-relaxed" v-html="renderMarkdown(aiData.explanation)"></div>
                                    </div>
                                    <button @click="aiData = null" class="w-full py-2 bg-slate-100 text-slate-600 rounded-lg text-xs font-bold hover:bg-slate-200">
                                        다시 찾기
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- 4. TUTOR TAB -->
                        <div v-if="activeTab === 'tutor'" class="flex flex-col h-full relative">
                            <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar" ref="chatContainer">
                                <div v-if="tutorMessages.length === 0" class="flex flex-col items-center justify-center h-full text-slate-400 text-center opacity-70">
                                    <Bot :size="32" class="mb-2"/>
                                    <p class="text-xs">코드에 대해 궁금한 점을 물어보세요!</p>
                                </div>
                                <div v-for="(msg, idx) in tutorMessages" :key="idx" 
                                    class="flex gap-3" :class="msg.role === 'user' ? 'flex-row-reverse' : ''">
                                    <div class="w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 border"
                                        :class="msg.role === 'user' ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-emerald-600 border-slate-200'">
                                        <span v-if="msg.role === 'user'" class="text-[10px]">나</span>
                                        <Bot v-else :size="14" />
                                    </div>
                                    <div class="max-w-[85%] rounded-2xl p-3 text-xs shadow-sm leading-relaxed"
                                        :class="msg.role === 'user' ? 'bg-indigo-600 text-white rounded-tr-none' : 'bg-white border border-slate-200 text-slate-700 rounded-tl-none'">
                                        <span v-if="msg.role === 'user'">{{ msg.content }}</span>
                                        <div v-else v-html="renderMarkdown(msg.content)" class="prose prose-sm max-w-none prose-p:my-1 prose-strong:text-emerald-700"></div>
                                    </div>
                                </div>
                                <div v-if="loadingTutorResponse" class="flex gap-3">
                                    <div class="w-7 h-7 rounded-full bg-white border border-slate-200 flex items-center justify-center text-emerald-600"><Bot :size="14"/></div>
                                    <div class="bg-white border border-slate-200 px-4 py-2.5 rounded-2xl rounded-tl-none"><Loader2 :size="14" class="animate-spin text-slate-400"/></div>
                                </div>
                            </div>
                            <div class="p-3 bg-white border-t border-slate-200 sticky bottom-0">
                                <div class="flex gap-2">
                                    <input v-model="tutorInput" @keypress.enter="sendTutorMessage" :disabled="loadingTutorResponse"
                                        placeholder="질문을 입력하세요..." class="flex-1 bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-xs focus:ring-1 focus:ring-indigo-500 focus:outline-none"/>
                                    <button @click="sendTutorMessage" :disabled="!tutorInput.trim() || loadingTutorResponse"
                                            class="w-8 h-8 bg-indigo-600 text-white rounded-lg flex items-center justify-center hover:bg-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed">
                                        <Send :size="14" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </Teleport>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue';
import { ExternalLink, ChevronDown, ChevronUp, Bot, Bug, Send, Loader2, Activity, LayoutList, Lightbulb, Tag, MessageSquare, Wand2, CheckCircle2 } from 'lucide-vue-next';
import CodeViewer from '../../components/CodeViewer.vue';
import { boardApi, commentApi } from '../../api/board';
import { aiApi } from '../../api/ai'; 
import { useAuth } from '../../composables/useAuth';
import { marked } from 'marked';

const props = defineProps({
  record: { type: Object, required: true }
});

const emit = defineEmits(['find-counter-example', 'ask-tutor']);

const { user } = useAuth();

const expanded = ref(false);
const loadingBoard = ref(false);
const board = ref(null);
const comments = ref([]);

// TABS
const activeTab = ref('overview');
const tabs = [
    { id: 'overview', label: '개요' },
    { id: 'analysis', label: '분석' },
    { id: 'counter', label: '반례' },
    { id: 'tutor', label: '튜터' }
];

// AI & Chat State
const loadingAi = ref(false);
const aiData = ref(null);
const tutorInput = ref('');
const tutorMessages = ref([]);
const loadingTutorResponse = ref(false);
const chatContainer = ref(null);
const codeViewerRef = ref(null);

// Methods
const toggleExpand = async () => {
    expanded.value = !expanded.value;
    if (expanded.value && !board.value) {
        await loadBoardAndComments();
    }
};

const loadBoardAndComments = async () => {
    loadingBoard.value = true;
    try {
        const res = await boardApi.findByRecordId(props.record.id);
        if (res.status === 204 || !res.data) {
            board.value = null;
            comments.value = [];
        } else {
            board.value = res.data;
            const commentsRes = await commentApi.findByBoardId(board.value.id);
            comments.value = commentsRes.data || [];
        }
    } catch (e) {
        console.error("Failed to load review info", e);
    } finally {
        loadingBoard.value = false;
    }
};

const scrollToLine = (lineNumber) => {
    // This requires CodeViewer to expose a scrollToLine method or we handle it via scrollIntoView
    // For now, we can try to select the element if possible, or assume CodeViewer is doing it.
    // If CodeViewer component exposes a method, we can call it. 
    // Assuming we might add `scrollToLine` to CodeViewer later or simply use the DOM.
    const codeLine = document.querySelector(`[data-line-number="${lineNumber}"]`);
    if(codeLine) codeLine.scrollIntoView({ behavior: 'smooth', block: 'center' });
};

const ensureBoardExists = async () => {
    if (board.value) return board.value;
    try {
        const res = await boardApi.create({
            title: `Code Review: ${props.record.title}`,
            content: `Code review for ${props.record.title} (${props.record.problemNumber})`,
            boardType: 'CODE_REVIEW',
            algorithmRecordId: props.record.id
        });
        board.value = res.data;
        return board.value;
    } catch (e) { 
        console.error("Failed to create review board", e); throw e; 
    }
};

const submitLineComment = async ({ lineNumber, content }) => {
    try {
        const targetBoard = await ensureBoardExists();
        const res = await commentApi.create(targetBoard.id, { content, lineNumber });
        comments.value.push(res.data);
    } catch (e) { console.error("Failed to submit comment", e); }
};

// AI Logic
const findCounterExample = async () => {
    loadingAi.value = true;
    try {
        const res = await aiApi.generateCounterExample({
            problemNumber: String(props.record.problemNumber),
            code: props.record.code,
            language: props.record.language
        });
        aiData.value = res.data;
    } catch (e) {
        console.error("Failed to find counter example", e);
        alert("반례 생성에 실패했습니다.");
    } finally {
        loadingAi.value = false;
    }
};

const sendTutorMessage = async () => {
    if (!tutorInput.value.trim()) return;
    const userMsg = tutorInput.value;
    tutorMessages.value.push({ role: 'user', content: userMsg });
    tutorInput.value = '';
    loadingTutorResponse.value = true;
    
    // Auto-scroll
    nextTick(() => { if(chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });

    // History excludes the current message which is sent in 'message' field
    const history = tutorMessages.value.slice(0, -1).map(m => ({ 
        role: m.role === 'user' ? 'user' : 'assistant', 
        content: m.content 
    }));

    try {
        const res = await aiApi.tutorChat({
             userId: user.value?.id,
             recordId: props.record.id,
             message: userMsg,
             solveStatus: isPassed.value ? 'solved' : 'wrong',
             wrongReason: !isPassed.value ? '틀렸습니다' : null,
             history: history
        });
        tutorMessages.value.push({ role: 'assistant', content: res.data.reply || res.data.answer || "답변을 생성할 수 없습니다." });
    } catch (e) {
        console.error(e);
        tutorMessages.value.push({ role: 'assistant', content: "AI 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요." });
    } finally {
        loadingTutorResponse.value = false;
        nextTick(() => { if(chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });
    }
};

// Helpers
const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '';
const getExtension = (l) => ({'java':'java','python':'py','cpp':'cpp','c':'c','javascript':'js'}[l?.toLowerCase()] || 'txt');
const extractKorean = (text) => { const match = text?.match(/[가-힣\s]+/); return match ? match[0].trim() : text; };
const extractPatterns = (json) => {
    if (!json) return [];
    try { return Array.isArray(JSON.parse(json)) ? JSON.parse(json) : []; } catch { return []; }
};
const renderMarkdown = (text) => text ? marked.parse(text) : '';

// Computed
const parsedKeyBlocks = computed(() => {
    if (!props.record.keyBlocks) return [];
    try { return JSON.parse(props.record.keyBlocks); } catch (e) { return [];}
});

const parsedPitfalls = computed(() => {
    if (!props.record.pitfalls) return [];
    try { return JSON.parse(props.record.pitfalls); } catch (e) { return []; }
});

const hasAnyAnalysis = computed(() => props.record.timeComplexity || parsedPitfalls.value.length > 0 || props.record.refactorProvided);
const isPassed = computed(() => props.record.result === 'SUCCESS' || props.record.result === 'PASSED' || (props.record.runtimeMs > 0 && props.record.memoryKb > 0));

const taskTypeLabel = computed(() => ({'MISSION':'과제','MOCK_EXAM':'모의고사','DEFENSE':'디펜스','GENERAL':'일반'}[props.record.tag]));
const defenseStreak = ref(0);

const statusHeaderClass = computed(() => {
    if (props.record.tag === 'DEFENSE') return isPassed.value ? 'bg-indigo-50 text-indigo-700 border-b border-indigo-100' : 'bg-red-50 text-red-700 border-b border-red-100';
    return isPassed.value ? 'bg-emerald-50 text-emerald-700 border-b border-emerald-100' : 'bg-red-50 text-red-700 border-b border-red-100';
});

const cardBorderClass = computed(() => {
    if(!expanded.value) { // Default hover colors for collapsed state
        return { 'hover:border-indigo-300': props.record.tag === 'DEFENSE', 'hover:border-emerald-300': isPassed.value, 'hover:border-red-300': !isPassed.value };
    }
    // Expanded state borders
    if (props.record.tag === 'DEFENSE') return { 'border-indigo-200': isPassed.value, 'border-red-200': !isPassed.value };
    return { 'border-emerald-200': isPassed.value, 'border-red-200': !isPassed.value };
});
</script>

<style scoped>
.animate-slide-down { animation: slide-down 0.3s ease-out forwards; }
@keyframes slide-down { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.custom-scrollbar::-webkit-scrollbar { width: 4px; height: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 2px; }

.animate-fade-in-left { animation: fade-in-left 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes fade-in-left { from { opacity: 0; transform: translateX(-20px); } to { opacity: 1; transform: translateX(0); } }

.animate-fade-in-right { animation: fade-in-right 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards; }
@keyframes fade-in-right { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
</style>
