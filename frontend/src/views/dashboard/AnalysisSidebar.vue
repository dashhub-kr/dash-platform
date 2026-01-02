<template>
  <div class="flex flex-col h-full bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
    <div v-if="!record" class="flex-1 flex flex-col items-center justify-center text-slate-400 p-8 text-center">
      <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4">
        <Activity class="w-8 h-8 opacity-20" />
      </div>
      <p class="text-sm font-bold text-slate-500 mb-1">분석할 기록을 선택해주세요</p>
      <p class="text-xs">타임라인에서 항목을 클릭하면<br/>상세 분석이 여기에 표시됩니다.</p>
    </div>

    <template v-else>
        <!-- Tabs Header -->
        <div class="flex border-b border-slate-200 h-[48px] shrink-0 bg-slate-50">
            <button 
                v-for="tab in tabs" 
                :key="tab.id"
                @click="activeTab = tab.id"
                class="flex-1 flex items-center justify-center text-xs font-bold transition-colors relative"
                :class="activeTab === tab.id ? 'text-brand-600 bg-brand-50/50' : 'text-slate-500 hover:text-slate-700 hover:bg-slate-50'"
            >
                {{ tab.label }}
                <span v-if="activeTab === tab.id" class="absolute bottom-0 left-0 w-full h-0.5 bg-brand-600"></span>
            </button>
        </div>

        <!-- Tab Content -->
        <div class="flex-1 overflow-y-auto p-0 custom-scrollbar bg-slate-50">
            
            <!-- 1. OVERVIEW TAB -->
            <div v-if="activeTab === 'overview'" class="p-5 space-y-6 flex flex-col h-full">
                <div class="space-y-6 flex-1">
                    <!-- Structure: Variables & Functions -->
                    <div v-if="parsedVariables.length > 0 || parsedFunctions.length > 0" class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <h4 class="text-xs font-bold text-slate-800 mb-3 flex items-center gap-2">
                            <LayoutList :size="14" class="text-blue-500"/> 변수 및 함수
                        </h4>
                        
                        <!-- Variables -->
                        <div v-if="parsedVariables.length > 0" class="mb-4 last:mb-0">
                            <div class="text-[10px] font-bold text-slate-400 uppercase mb-2 ml-1">주요 변수</div>
                            <div class="space-y-2">
                                <div v-for="(item, idx) in parsedVariables" :key="'v-'+idx" 
                                        class="p-2 bg-blue-50 rounded border border-blue-100 hover:border-blue-300 transition-colors group cursor-pointer"
                                        @click="viewCodeLine(item.name)">
                                    <div class="font-mono text-[10px] font-bold text-blue-700 bg-blue-100 inline-block px-1 py-0.5 rounded mb-1">
                                        {{ item.name }}
                                    </div>
                                    <p class="text-xs text-slate-600 leading-snug">{{ item.role }}</p>
                                </div>
                            </div>
                        </div>

                        <!-- Functions -->
                        <div v-if="parsedFunctions.length > 0">
                            <div class="text-[10px] font-bold text-slate-400 uppercase mb-2 ml-1">주요 함수</div>
                            <div class="space-y-2">
                                <div v-for="(item, idx) in parsedFunctions" :key="'f-'+idx" 
                                        class="p-2 bg-purple-50 rounded border border-purple-100 hover:border-purple-300 transition-colors group cursor-pointer"
                                        @click="viewCodeLine(item.name)">
                                    <div class="font-mono text-[10px] font-bold text-purple-700 bg-purple-100 inline-block px-1 py-0.5 rounded mb-1">
                                        {{ item.name }}
                                    </div>
                                    <p class="text-xs text-slate-600 leading-snug">{{ item.role }}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Key Logic -->
                    <div v-if="parsedKeyBlocks.length > 0" class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <h4 class="text-xs font-bold text-slate-800 mb-3 flex items-center gap-2">
                            <Key :size="14" class="text-brand-500"/> 핵심 로직
                        </h4>
                        <div class="space-y-3">
                            <div v-for="(block, idx) in parsedKeyBlocks" :key="idx" 
                                class="bg-slate-50 border border-slate-200 rounded-lg p-3 hover:border-brand-300 transition-colors cursor-pointer group"
                                @click="emitScrollToLine(block.startLine, block.endLine)"
                            >
                                <h5 v-if="block.role" class="text-xs font-bold text-slate-800 mb-1">{{ block.role }}</h5>
                                <div v-else-if="block.startLine" class="text-[10px] text-slate-400 font-bold mb-1">
                                    Lines {{ block.startLine }} ~ {{ block.endLine }}
                                </div>
                                
                                <div class="mb-2 bg-white border border-slate-200 rounded p-1.5" v-if="block.code">
                                    <code class="font-mono text-[10px] text-slate-600 block truncate">{{ block.code.trim() }}</code>
                                </div>

                                <p class="text-[11px] text-slate-500 leading-relaxed">{{ block.explanation }}</p>
                            </div>
                        </div>
                    </div>

                    <!-- Summary -->
                    <div v-if="parsedSummary" class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <h4 class="text-xs font-bold text-slate-800 mb-3 flex items-center gap-2">
                            <BookOpen :size="14" class="text-brand-500"/> 요약
                        </h4>
                        <div class="text-sm text-slate-700 leading-relaxed" v-html="renderMarkdown(parsedSummary)"></div>
                    </div>

                    <!-- Core Idea (Intuition) -->
                    <div v-if="parsedIntuition" class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                        <h4 class="text-xs font-bold text-slate-800 mb-3 flex items-center gap-2">
                            <Lightbulb :size="14" class="text-amber-500"/> 핵심 아이디어
                        </h4>
                        <div class="text-sm text-slate-700 leading-relaxed whitespace-pre-line" v-html="renderMarkdown(parsedIntuition)"></div>
                    </div>

                    <!-- Trace Example -->
                    <div v-if="parsedTraceExample && parsedTraceExample.hasExample" class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                        <button @click="toggleTrace" class="w-full flex items-center justify-between p-4 bg-slate-50 hover:bg-slate-100 transition-colors">
                            <span class="text-xs font-bold text-slate-800 flex items-center gap-2">
                                <Footprints :size="14" class="text-emerald-500"/> 실행 흐름 예시
                            </span>
                            <ChevronDown :class="{ 'rotate-180': showTrace }" class="transition-transform duration-300 text-slate-400" :size="16"/>
                        </button>
                        <div v-if="showTrace" class="p-4 border-t border-slate-200 bg-white space-y-4 animate-slide-down">
                            <p v-if="parsedTraceExample.note" class="text-xs text-slate-500 bg-slate-50 p-2 rounded border border-slate-100">
                                {{ parsedTraceExample.note }}
                            </p>
                            <div class="space-y-3">
                                <div v-for="(step, idx) in parsedTraceExample.steps" :key="idx" class="flex gap-3">
                                        <div class="flex-col items-center flex">
                                            <div class="w-5 h-5 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center text-[10px] font-bold border border-emerald-200">
                                                {{ idx + 1 }}
                                            </div>
                                            <div v-if="idx < parsedTraceExample.steps.length - 1" class="w-0.5 flex-1 bg-emerald-100 my-1"></div>
                                        </div>
                                        <div class="text-xs text-slate-700 pt-0.5 leading-relaxed">
                                            {{ step }}
                                        </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 2. ANALYSIS TAB -->
            <div v-if="activeTab === 'analysis'" class="p-5 space-y-6">
                <!-- Complexity Cards -->
                <div class="space-y-3">
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
                    
                    <!-- Complexity Explanation Toggle -->
                    <div v-if="record.complexityExplanation">
                        <button 
                            @click="showComplexityExplanation = !showComplexityExplanation"
                            class="text-xs text-brand-500 hover:text-brand-600 font-bold flex items-center gap-1 mx-auto transition-colors"
                        >
                            <HelpCircle :size="12" /> 왜 이런 복잡도를 가지나요? <ChevronDown :size="12" class="transition-transform duration-300" :class="{ 'rotate-180': showComplexityExplanation }"/>
                        </button>
                        <div v-if="showComplexityExplanation" class="mt-3 bg-slate-50 p-3 rounded-lg border border-slate-200 animate-slide-down">
                            <div class="text-xs text-slate-600 leading-relaxed whitespace-pre-line" v-html="renderMarkdown(record.complexityExplanation)"></div>
                        </div>
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
                        <div v-if="record.refactorCode" class="rounded-lg overflow-hidden border border-slate-200 bg-white">
                            <div class="flex items-center justify-between px-3 py-1.5 bg-slate-100 border-b border-slate-200">
                                <span class="text-[10px] font-mono text-slate-500">{{ record.language || 'code' }}</span>
                                <button @click="copyToClipboard(record.refactorCode)" 
                                    class="text-[10px] text-slate-400 hover:text-brand-500 font-bold flex items-center gap-1 transition-colors">
                                    <Copy :size="10" /> 복사
                                </button>
                            </div>
                            <pre class="p-3 overflow-x-auto custom-scrollbar text-xs leading-relaxed"><code class="language-java" v-html="highlightCode(record.refactorCode, record.language)"></code></pre>
                        </div>
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
                    <template v-if="record.tag === 'MOCK_EXAM' || record.tag === 'TEST'">
                        <Trophy :size="32" class="mb-2 text-amber-500 opacity-80"/>
                        <p class="text-xs font-bold text-slate-600 mb-1">시험 통과!</p>
                        <p class="text-[10px]">문제 해결을 축하합니다. 훌륭한 성과입니다!</p>
                    </template>
                    <template v-else>
                        <CheckCircle2 :size="32" class="mb-2 text-emerald-500 opacity-50"/>
                        <p class="text-xs font-bold text-slate-600 mb-1">이미 해결된 문제입니다!</p>
                        <p class="text-[10px]">정답 코드는 반례 생성 기능을 사용할 수 없습니다.</p>
                    </template>
                </div>
                <div v-else class="flex flex-col h-full">
                    <div v-if="!aiData && !loadingAi" class="flex-1 flex flex-col items-center justify-center text-center">
                        <button @click="findCounterExample" class="px-5 py-2.5 bg-rose-500 hover:brightness-90 text-white rounded-lg font-bold text-xs shadow-lg shadow-rose-500/30 transition-all flex items-center gap-2">
                            <Bug :size="14"/> 반례 생성하기
                        </button>
                        <p class="text-[10px] text-slate-400 mt-3">AI가 코드를 분석하여 실패 원인을 찾아냅니다.</p>
                    </div>
                    
                    <div v-if="loadingAi" class="flex-1 flex flex-col items-center justify-center text-slate-400">
                        <Loader2 class="w-8 h-8 mb-2 animate-spin text-rose-500" />
                        <span class="text-xs">반례를 찾고 있습니다...</span>
                    </div>
                    
                    <div v-if="aiData" class="space-y-4 animate-fade-in-up">
                        <div class="bg-rose-500/5 border border-rose-500/20 rounded-xl p-4">
                            <div class="flex items-center justify-between mb-1">
                                <div class="text-[10px] font-bold text-slate-400 uppercase">입력 (Input)</div>
                                <button @click="copyToClipboard(aiData.input)" class="text-[10px] text-slate-400 hover:text-brand-500 font-bold flex items-center gap-1 transition-colors" title="클립보드에 복사">
                                    <Copy :size="10" /> 복사
                                </button>
                            </div>
                            <pre class="bg-white p-2 rounded border border-rose-500/20 text-xs font-mono text-slate-700 overflow-x-auto whitespace-pre-wrap min-h-[40px]">{{ aiData.input }}</pre>

                            <div class="flex items-center justify-between mt-3 mb-1">
                                <div class="text-[10px] font-bold text-slate-400 uppercase">예상 출력 (Expected)</div>
                                <button @click="copyToClipboard(aiData.expected)" class="text-[10px] text-slate-400 hover:text-brand-500 font-bold flex items-center gap-1 transition-colors" title="클립보드에 복사">
                                    <Copy :size="10" /> 복사
                                </button>
                            </div>
                            <pre class="bg-white p-2 rounded border border-rose-500/20 text-xs font-mono text-slate-700 overflow-x-auto whitespace-pre-wrap min-h-[40px]">{{ aiData.expected }}</pre>

                            <div class="flex items-center justify-between mt-3 mb-1">
                                <div class="text-[10px] font-bold text-slate-400 uppercase">내 코드 실행 결과 (Predicted)</div>
                                <button @click="copyToClipboard(aiData.predicted)" class="text-[10px] text-slate-400 hover:text-brand-500 font-bold flex items-center gap-1 transition-colors" title="클립보드에 복사">
                                    <Copy :size="10" /> 복사
                                </button>
                            </div>
                            <pre class="bg-white p-2 rounded border border-rose-500/20 text-xs font-mono text-red-600 bg-red-50/50 overflow-x-auto whitespace-pre-wrap min-h-[40px] border-red-100">{{ aiData.predicted }}</pre>
                        </div>
                        <div class="bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
                            <div class="text-xs font-bold text-brand-600 mb-2 flex items-center gap-2"><Lightbulb :size="14"/> 분석 결과</div>
                            <div class="text-xs text-slate-700 leading-relaxed" v-html="renderMarkdown(aiData.reason)"></div>
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
                        <p class="text-xs mb-4">코드에 대해 궁금한 점을 물어보세요!</p>
                        <div class="flex flex-wrap justify-center gap-2 max-w-[80%]">
                            <button @click="sendSuggestion('코드의 핵심 로직을 설명해줘')" class="px-3 py-1.5 bg-white border border-slate-200 rounded-full text-[10px] hover:border-brand-400 hover:text-brand-600 transition-colors shadow-sm">
                                <Brain :size="12" class="inline mr-1"/> 핵심 로직 설명
                            </button>
                            <button @click="sendSuggestion('시간 복잡도를 분석해줘')" class="px-3 py-1.5 bg-white border border-slate-200 rounded-full text-[10px] hover:border-brand-400 hover:text-brand-600 transition-colors shadow-sm">
                                <Zap :size="12" class="inline mr-1"/> 시간 복잡도 분석
                            </button>
                            <button @click="sendSuggestion('이 코드를 어떻게 개선할 수 있어?')" class="px-3 py-1.5 bg-white border border-slate-200 rounded-full text-[10px] hover:border-brand-400 hover:text-brand-600 transition-colors shadow-sm">
                                <Sparkles :size="12" class="inline mr-1"/> 코드 개선점
                            </button>
                        </div>
                    </div>
                    <div v-for="(msg, idx) in tutorMessages" :key="idx" 
                        class="flex gap-3" :class="msg.role === 'user' ? 'flex-row-reverse' : ''">
                        <div class="w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 border"
                            :class="msg.role === 'user' ? 'bg-brand-600 text-white border-brand-600' : 'bg-white text-emerald-600 border-slate-200'">
                            <span v-if="msg.role === 'user'" class="text-[10px]">나</span>
                            <Bot v-else :size="14" />
                        </div>
                        <div class="max-w-[85%] rounded-2xl p-3 text-xs shadow-sm leading-relaxed"
                            :class="msg.role === 'user' ? 'bg-brand-600 text-white rounded-tr-none' : 'bg-white border border-slate-200 text-slate-700 rounded-tl-none'">
                            <span v-if="msg.role === 'user'">{{ msg.content }}</span>
                            <div v-else-if="msg.isLoading">
                                <div class="flex items-center gap-2 text-slate-400">
                                    <Loader2 :size="14" class="animate-spin text-brand-500"/>
                                    <span class="text-[10px] font-bold animate-pulse">답변 생성 중...</span>
                                </div>
                            </div>
                            <div v-else>
                                <div v-html="renderMarkdown(msg.content)" class="prose prose-sm max-w-none prose-p:my-1 prose-code:before:content-none prose-code:after:content-none"></div>
                                
                                <div v-if="msg.encouragement" class="mt-3 p-2 bg-emerald-50 rounded text-emerald-700 italic border border-emerald-100 flex gap-2 items-start">
                                    <Lightbulb :size="12" class="mt-0.5 shrink-0"/>
                                    <span>{{ msg.encouragement }}</span>
                                </div>
                                
                                <div v-if="msg.concepts && msg.concepts.length > 0" class="mt-2 flex flex-wrap gap-1">
                                    <span v-for="c in msg.concepts" :key="c" class="px-1.5 py-0.5 bg-slate-100 text-slate-500 rounded text-[10px]">#{{c}}</span>
                                </div>

                                <div v-if="msg.suggestions && msg.suggestions.length > 0" class="mt-3 pt-3 border-t border-slate-100 flex flex-wrap gap-2 animate-fade-in">
                                    <button v-for="q in msg.suggestions" :key="q" @click="sendSuggestion(q)" 
                                        class="px-3 py-2 bg-brand-50 hover:bg-brand-100 text-brand-700 rounded-xl text-xs font-medium transition-colors flex items-start gap-2 border border-brand-200 shadow-sm w-full text-left">
                                        <MessageCircle :size="14" class="shrink-0 mt-0.5"/> 
                                        <span class="leading-relaxed">{{ q }}</span>
                                    </button>
                                </div>
                            </div>
                    </div>
                    </div>
                </div>
                    
                <div class="p-3 bg-white border-t border-slate-200 sticky bottom-0">
                    <div class="flex gap-2">
                        <input v-model="tutorInput" @keypress.enter="sendTutorMessage" :disabled="loadingTutorResponse"
                            placeholder="질문을 입력하세요..." class="flex-1 bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-xs focus:ring-1 focus:ring-brand-700 focus:outline-none"/>
                        <button @click="sendTutorMessage" :disabled="!tutorInput.trim() || loadingTutorResponse"
                                class="w-8 h-8 bg-brand-600 text-white rounded-lg flex items-center justify-center hover:bg-brand-500 disabled:opacity-50 disabled:cursor-not-allowed">
                            <Send :size="14" />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </template>
  </div>
</template>

<script setup>
import { ref, watch, computed, nextTick } from 'vue';
import { ExternalLink, ChevronDown, Bot, Bug, Send, Loader2, Activity, LayoutList, Lightbulb, Tag, MessageSquare, Wand2, CheckCircle2, BookOpen, Footprints, HelpCircle, Trophy, Copy, Key, Sparkles, MessageCircle, Brain, Zap } from 'lucide-vue-next';
import { aiApi } from '@/api/ai'; 
import { useAuth } from '@/composables/useAuth';
import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';

// Configure marked for proper inline code parsing
marked.setOptions({
    breaks: true,
    gfm: true
});

const props = defineProps({
  record: { type: Object, default: null }
});

const emit = defineEmits(['scroll-to-line', 'acorn-used']);

const { user } = useAuth();

// TABS
const activeTab = ref('overview');
const tabs = [
    { id: 'overview', label: '개요' },
    { id: 'analysis', label: '분석' },
    { id: 'counter', label: '반례' },
    { id: 'tutor', label: '튜터' }
];

// STATE
const showTrace = ref(false);
const showComplexityExplanation = ref(false);
const loadingAi = ref(false);
const aiData = ref(null);
const tutorInput = ref('');
const tutorMessages = ref([]);
const loadingTutorResponse = ref(false);
const chatContainer = ref(null);

// Reset when record changes
watch(() => props.record, (newRecord) => {
    if (newRecord) {
        activeTab.value = 'overview';
        tutorMessages.value = [];
        showTrace.value = false;
        showComplexityExplanation.value = false;
        
        // Initialize AI Data if exists
        if (newRecord.counterExampleInput) {
            aiData.value = {
                input: newRecord.counterExampleInput,
                expected: newRecord.counterExampleExpected,
                predicted: newRecord.counterExamplePredicted,
                reason: newRecord.counterExampleReason
            };
        } else {
            aiData.value = null;
        }
    } else {
        aiData.value = null;
    }
});

// Computed Properties (Parsing)
const parsedFullResponse = computed(() => {
    if (!props.record?.fullResponse) return null;
    try { return JSON.parse(props.record.fullResponse); } catch { return null; }
});

const parsedStructure = computed(() => {
    if (!parsedFullResponse.value) return [];
    return Array.isArray(parsedFullResponse.value.structure) ? parsedFullResponse.value.structure : [];
});

const parsedVariables = computed(() => parsedStructure.value.filter(item => !item.type || item.type === 'variable' || item.type === 'constant'));
const parsedFunctions = computed(() => parsedStructure.value.filter(item => item.type === 'function' || item.type === 'class'));

const parsedKeyBlocks = computed(() => {
    if (!props.record?.keyBlocks) return [];
    try { return Array.isArray(JSON.parse(props.record.keyBlocks)) ? JSON.parse(props.record.keyBlocks) : []; } catch { return []; }
});

const parsedSummary = computed(() => parsedFullResponse.value?.summary || null);
const parsedTraceExample = computed(() => parsedFullResponse.value?.traceExample || null);
const parsedIntuition = computed(() => parsedFullResponse.value?.algorithm?.intuition || props.record?.algorithmIntuition || null);

const parsedPitfalls = computed(() => {
    if (!parsedFullResponse.value || !parsedFullResponse.value.pitfalls) return [];
    return Array.isArray(parsedFullResponse.value.pitfalls) ? parsedFullResponse.value.pitfalls : [];
});

const hasAnyAnalysis = computed(() => {
    return props.record?.timeComplexity || props.record?.spaceComplexity || parsedPitfalls.value.length > 0 || props.record?.refactorProvided;
});

const isPassed = computed(() => {
    if (!props.record) return false;
    return props.record.result === 'SUCCESS' || props.record.result === 'PASSED' || (props.record.runtimeMs && props.record.runtimeMs > 0);
});

// ACTIONS
const toggleTrace = () => showTrace.value = !showTrace.value;

const emitScrollToLine = (start, end) => {
    emit('scroll-to-line', { start, end });
};

const viewCodeLine = (name) => {
    if (!name || !props.record?.code) return;
    const lines = props.record.code.split('\n');
    
    // Handle comma-separated names like "N, K" - search for each part
    const searchTerms = name.split(',').map(s => s.trim()).filter(s => s.length > 0);
    const primaryTerm = searchTerms[0]; // Use first term for search
    
    const index = lines.findIndex(line => line.includes(primaryTerm));
    if (index !== -1) emitScrollToLine(index + 1);
};

// AI & UTILS
const renderMarkdown = (text) => {
    if (!text) return '';
    
    return text
        // Escape HTML first
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        // Handle code blocks (triple backticks)
        .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
        // Handle inline code (single backticks)
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        // Handle bold
        .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
        // Handle italic
        .replace(/\*([^*]+)\*/g, '<em>$1</em>')
        // Handle line breaks
        .replace(/\n/g, '<br>');
};
const copyToClipboard = async (text) => {
    if (!text) return;
    try { await navigator.clipboard.writeText(text); } catch (err) { console.error('Failed to copy', err); }
};

const highlightCode = (code, language) => {
    if (!code) return '';
    try {
        const lang = language?.toLowerCase() || 'java';
        return hljs.highlight(code, { language: lang }).value;
    } catch (err) {
        try {
            return hljs.highlightAuto(code).value;
        } catch (e) {
            return code;
        }
    }
};

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

const sendTutorMessage = async () => {
    if (!tutorInput.value.trim()) return;
    const userMsg = tutorInput.value;
    tutorMessages.value.push({ role: 'user', content: userMsg });
    tutorInput.value = '';
    
    // Add temporary loading message
    const loadingMsg = { role: 'assistant', content: '', isLoading: true };
    tutorMessages.value.push(loadingMsg);
    loadingTutorResponse.value = true;
    
    // Auto-scroll
    nextTick(() => { if(chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });

    // History excludes current & loading
    // We already pushed userMsg and loadingMsg, so slice -2 to get history before this turn
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

        // Remove loading message
        tutorMessages.value.pop();

        tutorMessages.value.push({ 
            role: 'assistant', 
            content: res.data.reply || res.data.answer || "답변을 생성할 수 없습니다.",
            suggestions: res.data.followUpQuestions,
            concepts: res.data.relatedConcepts,
            encouragement: res.data.encouragement
        });
        
        emit('acorn-used');
    } catch (e) {
        console.error("Tutor chat failed", e);
        
        // Remove loading message
        tutorMessages.value.pop();
        
        const errorMsg = e.response?.data?.message || '';
        if (errorMsg.includes('Not enough acorns')) {
             tutorMessages.value.push({ role: 'assistant', content: "도토리가 부족해 응답을 생성할 수 없습니다." });
        } else {
             tutorMessages.value.push({ role: 'assistant', content: "죄송합니다, 답변을 생성하는 중 오류가 발생했습니다." });
        }
    } finally {
        loadingTutorResponse.value = false;
        nextTick(() => { if(chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight; });
    }
};

const sendSuggestion = (q) => {
    tutorInput.value = q;
    sendTutorMessage();
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

/* Inline code styling for markdown rendered content */
:deep(.prose code:not(pre code)),
:deep(code:not(pre code)) {
  background-color: #f1f5f9;
  color: #0f172a;
  padding: 0.15rem 0.4rem;
  border-radius: 0.25rem;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 0.85em;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  word-break: break-word;
}

/* Slightly darker background for code in tutor chat bubbles */
:deep(.bg-white code:not(pre code)) {
  background-color: #e2e8f0;
  border-color: #cbd5e1;
}
</style>
