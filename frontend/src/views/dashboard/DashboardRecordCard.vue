<template>
  <div 
    class="group relative bg-white rounded-3xl shadow-sm transition-all duration-300"
    :class="{ 'hover:shadow-md hover:-translate-y-0.5': !isExpanded, 'shadow-md': isExpanded, ...cardBorderClass }"
    @click.stop
  >
    <!-- 상태 헤더 바 -->
    <div :class="statusHeaderClass" class="px-5 py-3 flex items-center gap-2 text-sm font-bold rounded-t-3xl cursor-pointer" @click="toggleExpand">
      <span v-if="record.tag === 'DEFENSE'">
        <Shield v-if="isPassed" :size="16" class="inline" />
        <ShieldAlert v-else :size="16" class="inline" />
      </span>
      <span v-else>
        <Check v-if="isPassed" :size="16" class="inline" />
        <X v-else :size="16" class="inline" />
      </span>
      
      <span v-if="record.tag === 'DEFENSE'">{{ isPassed ? '방어성공' : '방어실패' }}</span>
      <span v-else>{{ isPassed ? 'PASSED' : 'FAILED' }}</span>

      <span v-if="taskTypeLabel" class="ml-1 opacity-70 font-medium text-[10px]">[{{ taskTypeLabel }}]</span>

      <!-- 디펜스 연속 성공 뱃지 -->
      <div v-if="record.tag === 'DEFENSE' && defenseStreak > 0" 
           class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-gradient-to-r from-orange-500 via-amber-500 to-yellow-500 text-white shadow-sm shadow-orange-200 border border-orange-400/50 mx-1">
          <Flame class="w-3 h-3 animate-pulse" fill="currentColor" />
          <span class="text-[9px] font-black tracking-wider">{{ defenseStreak }} 연승!</span>
      </div>

      <!-- 소요 시간 뱃지 (디펜스/모의고사용) -->
      <div v-if="record.elapsedTimeSeconds && (record.tag === 'DEFENSE' || record.tag === 'MOCK_EXAM')" 
           class="flex items-center gap-1 px-2 py-0.5 rounded-full bg-blue-100 text-blue-700 border border-blue-200">
          <Clock :size="10" />
          <span class="text-[9px] font-bold">{{ formatElapsedTime(record.elapsedTimeSeconds) }}</span>
      </div>
      
      <!-- 오른쪽: 이름/날짜 -->
      <div class="ml-auto flex items-center gap-3">
          <!-- 제출자 이름 & 날짜 -->
          <div class="flex items-center gap-2 text-xs opacity-50">
              <span class="font-bold border-r border-slate-400/30 pr-2">{{ record.username || '탈퇴한 회원' }}</span>
              <span>{{ formatDate(record.committedAt) }}</span>
          </div>
      </div>
    </div>

    <!-- 헤더 / 접힌 뷰 -->
    <div class="flex flex-col xl:flex-row gap-6 p-6 cursor-pointer" @click="toggleExpand">
      <!-- 메인 콘텐츠 섹션 -->
      <div class="flex-1 min-w-0">
          <div class="flex items-start justify-between gap-4 mb-2">
              <div class="flex flex-col">
                  <!-- 뱃지 -->
                  <div class="flex flex-wrap items-center gap-2 mb-2">
                    <span 
                        class="px-3 py-1.5 rounded-xl text-[11px] font-black uppercase tracking-wider bg-slate-50 border-2 border-slate-100 text-slate-500"
                    >
                        #{{ record.problemNumber }}
                    </span>
                    <span 
                        class="px-3 py-1.5 rounded-xl text-[11px] font-black uppercase tracking-wider border-2"
                        :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-slate-50 border-slate-100 text-slate-600' : 'bg-red-50 border-red-100 text-red-600'"
                    >
                        {{ record.language }}
                    </span>
                    <!-- 패턴 태그 -->
                    <span v-for="pattern in extractPatterns(record.patterns)" :key="pattern"
                          class="px-3 py-1.5 bg-brand-50 text-brand-600 text-[10px] font-bold rounded-xl border-2 border-brand-100">
                        {{ pattern }}
                    </span>
                  </div>
                  <!-- 제목 -->
                  <h3 class="text-lg md:text-xl font-bold text-slate-800 leading-tight group-hover:text-brand-600 transition-colors flex items-center gap-2">
                      <span v-if="platformBadge" class="text-slate-400 text-sm font-normal">
                          [{{ platformBadge }}]
                      </span>
                      {{ record.title }}
                  </h3>
              </div>
               <div class="text-slate-400">
                  <ChevronDown v-if="!isExpanded" :size="20" />
                  <ChevronUp v-else :size="20" class="text-brand-500" />
               </div>
          </div>
      </div>
    </div>

    <!-- 확장 뷰: 코드 뷰어 전용 -->
    <div v-if="isExpanded" class="animate-slide-down bg-slate-50 relative">
        <div class="flex flex-col">
            
            <!-- 중앙 패널: 코드 뷰어 (전체 너비) -->
            <div class="rounded-xl overflow-hidden">
                <CodeViewer
                    ref="codeViewerRef"
                    :code="record.code"
                    :language="record.language || 'java'"
                    :filename="`${record.title}.${getExtension(record.language)}`"
                    :comments="comments"
                    :keyBlocks="combinedHighlights"
                    @submit-comment="submitLineComment"
                    :readOnly="false" 
                />
            </div>

            </div>
        </div>

  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue';
import { ExternalLink, ChevronDown, ChevronUp, Bot, Bug, Send, Loader2, Activity, LayoutList, Lightbulb, Tag, MessageSquare, Wand2, CheckCircle2, BookOpen, Footprints, HelpCircle, Trophy, Clock, Check, X, Shield, ShieldAlert, Package, Key, Flame, Sparkles, MessageCircle, Copy } from 'lucide-vue-next';
import CodeViewer from '@/components/editor/CodeViewer.vue';
import { boardApi, commentApi } from '@/api/board';
import { aiApi } from '@/api/ai'; 
import { useAuth } from '@/composables/useAuth';
import { marked } from 'marked';

const props = defineProps({
  record: { type: Object, required: true },
  isExpanded: { type: Boolean, default: false }
});

const emit = defineEmits(['find-counter-example', 'ask-tutor', 'toggle-expand']);

const { user } = useAuth();

const loadingBoard = ref(false);
const board = ref(null);
const comments = ref([]);

// 탭
const activeTab = ref('overview');
const tabs = [
    { id: 'overview', label: '개요' },
    { id: 'analysis', label: '분석' },
    { id: 'counter', label: '반례' },
    { id: 'tutor', label: '튜터' }
];

// AI & 채팅 상태
const loadingAi = ref(false);
const aiData = ref(props.record.counterExampleInput ? {
    input: props.record.counterExampleInput,
    expected: props.record.counterExampleExpected,
    predicted: props.record.counterExamplePredicted,
    reason: props.record.counterExampleReason
} : null);
const tutorInput = ref('');
const tutorMessages = ref([]);
const loadingTutorResponse = ref(false);
const chatContainer = ref(null);
const codeViewerRef = ref(null);
const commentsScrollContainer = ref(null);

// 개요 탭 상태
const showTrace = ref(false);
const overviewCommentInput = ref('');
// 분석 탭 상태
const showComplexityExplanation = ref(false);

const toggleTrace = () => {
    showTrace.value = !showTrace.value;
};

const submitOverviewComment = async () => {
    if (!overviewCommentInput.value.trim()) return;
    await submitLineComment({ lineNumber: null, content: overviewCommentInput.value.trim() });
    overviewCommentInput.value = '';
};

// 메서드
const toggleExpand = () => {
    console.log('Card: Emitting toggle-expand for', props.record.id);
    emit('toggle-expand', props.record.id);
};

// 데이터 로드를 위한 확장 감지
watch(() => props.isExpanded, async (newVal) => {
    if (newVal && !board.value) {
        await loadBoardAndComments();
    }
});

// 새 댓글 추가 시 자동 스크롤
watch(comments, () => {
    nextTick(() => {
        if (commentsScrollContainer.value) {
            commentsScrollContainer.value.scrollTop = commentsScrollContainer.value.scrollHeight;
        }
    });
}, { deep: true });

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

const scrollToLine = (lineNumber, endLine = null) => {
    console.log("Dashboard: scrollToLine", lineNumber, endLine, "Ref:", !!codeViewerRef.value);
    if (codeViewerRef.value && lineNumber) {
        codeViewerRef.value.scrollToLine(Number(lineNumber), endLine ? Number(endLine) : null);
    }
};

const scrollToStructure = (name) => {
    if (!name || !props.record.code) return;
    const lines = props.record.code.split('\n');
    const target = name.trim();
    // Simple search: find first line containing the name
    // Could be improved with regex or exact match, but contains is usually sufficient for navigation
    const index = lines.findIndex(line => line.includes(target));
    if (index !== -1) {
        scrollToLine(index + 1);
    }
};

const ensureBoardExists = async () => {
    if (board.value) return board.value;
    try {
        const res = await boardApi.create({
            title: `${props.record.title}`,
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

// AI 로직
const findCounterExample = async () => {
    loadingAi.value = true;
    try {
        const res = await aiApi.generateCounterExample({
            recordId: props.record.id,
            problemNumber: String(props.record.problemNumber),
            code: props.record.code,
            language: props.record.language,
            platform: props.record.platform,
            problemTitle: props.record.title
        });
        aiData.value = res.data;
    } catch (e) {
        console.error("Failed to find counter example", e);
        alert("반례 생성에 실패했습니다.");
    } finally {
        loadingAi.value = false;
    }
};

const copyToClipboard = async (text) => {
    if (!text) return;
    try {
        await navigator.clipboard.writeText(text);
    // 추후 토스트 알림 추가 가능
    } catch (err) {
        console.error('Failed to copy text: ', err);
    }
};

const sendTutorMessage = async () => {
    if (!tutorInput.value.trim()) return;
    const userMsg = tutorInput.value;
    tutorMessages.value.push({ role: 'user', content: userMsg });
    tutorInput.value = '';
    loadingTutorResponse.value = true;
    
    // 자동 스크롤
    nextTick(() => { if(chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });

    // 로딩 플레이스홀더 추가
    tutorMessages.value.push({ role: 'assistant', isLoading: true });
    nextTick(() => { if(chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });

    // 현재 메시지와 로딩은 히스토리에서 제외
    const history = tutorMessages.value.slice(0, -2).map(m => ({ 
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
        
        // 로딩 플레이스홀더 제거
        tutorMessages.value.pop();

        tutorMessages.value.push({ 
            role: 'assistant', 
            content: res.data.reply || res.data.answer || "답변을 생성할 수 없습니다.",
            suggestions: res.data.followUpQuestions,
            concepts: res.data.relatedConcepts,
            encouragement: res.data.encouragement
        });
    } catch (e) {
        tutorMessages.value.pop(); // Remove placeholder
        console.error("Tutor chat failed", e);
        tutorMessages.value.push({ role: 'assistant', content: "죄송합니다, 답변을 생성하는 중 오류가 발생했습니다." });
    } finally {
        loadingTutorResponse.value = false;
        nextTick(() => { if(chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });
    }
};


const sendSuggestion = (q) => {
    tutorInput.value = q;
    sendTutorMessage();
};

// 헬퍼 함수
const formatDate = (d) => d ? new Date(d).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) : '';
const getExtension = (l) => ({'java':'java','python':'py','cpp':'cpp','c':'c','javascript':'js'}[l?.toLowerCase()] || 'txt');

const formatElapsedTime = (seconds) => {
    if (!seconds || seconds < 0) return '';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    if (mins === 0) return `${secs}초`;
    if (secs === 0) return `${mins}분`;
    return `${mins}분 ${secs}초`;
};

const extractPatterns = (json) => {
    if (!json) return [];
    try { 
        const parsed = Array.isArray(JSON.parse(json)) ? JSON.parse(json) : [];
        // Flatten: If a pattern string contains commas, split it.
        return parsed.flatMap(p => p.split(',').map(s => s.trim()).filter(s => s.length > 0)); 
    } catch { return []; }
};

const renderMarkdown = (text) => text ? marked.parse(text) : '';

// Computed 속성
const parsedKeyBlocks = computed(() => {
    if (!props.record.keyBlocks) return [];
    try { return Array.isArray(JSON.parse(props.record.keyBlocks)) ? JSON.parse(props.record.keyBlocks) : []; } catch { return []; }
});

const parsedStructure = computed(() => {
    if (!props.record.fullResponse) return [];
    try {
        const full = JSON.parse(props.record.fullResponse);
        return Array.isArray(full.structure) ? full.structure : [];
    } catch { return []; }
});

// Type별 분리 - 변수 (variable, constant)
const parsedVariables = computed(() => {
    return parsedStructure.value.filter(item => 
        !item.type || item.type === 'variable' || item.type === 'constant'
    );
});

// Type별 분리 - 함수 (function, class)
const parsedFunctions = computed(() => {
    return parsedStructure.value.filter(item => 
        item.type === 'function' || item.type === 'class'
    );
});



const parsedFullResponse = computed(() => {
    if (!props.record.fullResponse) return null;
    try { return JSON.parse(props.record.fullResponse); } catch { return null; }
});

const parsedSummary = computed(() => parsedFullResponse.value?.summary || null);

const parsedTraceExample = computed(() => parsedFullResponse.value?.traceExample || null);

const parsedIntuition = computed(() => {
    return parsedFullResponse.value?.algorithm?.intuition || props.record.algorithmIntuition || null;
});

const latestSuggestions = computed(() => {
    const lastMsg = tutorMessages.value.slice().reverse().find(m => m.role === 'assistant');
    return lastMsg && lastMsg.suggestions ? lastMsg.suggestions : [];
});

const combinedHighlights = computed(() => {
    const structure = parsedStructure.value.map(item => ({
        ...item,
        code: item.name,
        explanation: `[구조] ${item.role}`
    }));
    const keyBlocks = parsedKeyBlocks.value.map(block => ({
        ...block,
        code: block.code,
        explanation: `[로직] ${block.explanation}`
    }));
    return [...structure, ...keyBlocks];
});

const parsedPitfalls = computed(() => {
    if (!props.record.pitfalls) return [];
    try { return JSON.parse(props.record.pitfalls); } catch (e) { return []; }
});

const hasAnyAnalysis = computed(() => props.record.timeComplexity || parsedPitfalls.value.length > 0 || props.record.refactorProvided);
const isPassed = computed(() => props.record.result === 'SUCCESS' || props.record.result === 'PASSED' || (props.record.runtimeMs > 0 && props.record.memoryKb > 0));

const taskTypeLabel = computed(() => ({'MISSION':'과제','MOCK_EXAM':'모의고사','DEFENSE':'디펜스','GENERAL':'일반'}[props.record.tag]));
const defenseStreak = computed(() => props.record.defenseStreak || 0);

const platformBadge = computed(() => {
    const p = props.record.platform?.toUpperCase();
    if (p === 'BAEKJOON') return 'BOJ';
    if (p === 'SWEA') return 'SWEA';
    if (p === 'PROGRAMMERS') return 'PGS';
    return null;
});

const statusHeaderClass = computed(() => {
    // Defense: Indigo theme (Premium, Shield-like)
    if (props.record.tag === 'DEFENSE') return isPassed.value ? 'bg-brand-50 text-brand-700 border-b border-brand-100' : 'bg-red-50 text-red-700 border-b border-red-100';
    // General: Emerald/Red theme
    return isPassed.value ? 'bg-emerald-50 text-emerald-700 border-b border-emerald-100' : 'bg-red-50 text-red-700 border-b border-red-100';
});

const cardBorderClass = computed(() => {
    // Borderless mode: no ring effects
    return {};
});

defineExpose({ scrollToLine });
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
