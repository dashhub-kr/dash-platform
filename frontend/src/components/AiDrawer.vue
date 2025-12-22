<template>
  <!-- Simple static drawer for inline split-view -->
  <div class="h-full flex flex-col bg-white shadow-2xl">
    <!-- Header -->
    <div class="px-4 py-6 sm:px-6 border-b border-slate-100 bg-white">
      <div class="flex items-start justify-between">
        <h2 class="text-xl font-black text-slate-800 flex items-center gap-3" id="slide-over-title">
          <div class="p-2 rounded-xl" :class="getHeaderIconClass(type)">
            <component :is="getHeaderIcon(type)" class="w-6 h-6" />
          </div>
          {{ title }}
        </h2>
        <div class="ml-3 flex h-7 items-center">
          <button type="button" class="rounded-full bg-white text-slate-400 hover:text-slate-500 hover:bg-slate-50 focus:outline-none p-2 transition-colors" @click="$emit('close')">
            <span class="sr-only">Close panel</span>
            <X class="w-6 h-6" />
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content (Scrollable) -->
    <div class="flex-1 overflow-y-auto px-4 sm:px-6 pb-10">

                
                <!-- LOADING STATE -->
                <div v-if="loading" class="flex flex-col items-center justify-center h-full py-20">
                    <div class="w-16 h-16 border-4 border-indigo-100 border-t-indigo-500 rounded-full animate-spin mb-6"></div>
                    <h4 class="text-lg font-bold text-slate-800">AI가 분석 중입니다</h4>
                    <p class="text-slate-500">잠시만 기다려주세요...</p>
                </div>

                <!-- EMPTY STATE (except for hint type) -->
                <div v-else-if="!data && type !== 'hint'" class="flex flex-col items-center justify-center h-full text-slate-500">
                    데이터가 없습니다. 다시 시도해주세요.
                </div>

                <!-- CONTENT: COUNTER EXAMPLE -->
                <div v-else-if="type === 'counter_example'" class="space-y-8 animate-fade-in">
                    
                    <!-- 1. Input Section (Terminal Style) -->
                    <div class="space-y-2">
                        <div class="flex items-center justify-between">
                            <label class="text-xs font-bold text-slate-500 uppercase tracking-wider">Counterexample Input</label>
                            <button 
                                @click="copyInput" 
                                class="flex items-center gap-1.5 text-xs font-bold text-indigo-500 hover:text-indigo-700 transition-colors px-2 py-1 rounded bg-indigo-50 hover:bg-indigo-100"
                            >
                                <component :is="copied ? Check : Copy" class="w-3.5 h-3.5" />
                                {{ copied ? 'Copied!' : 'Copy Input' }}
                            </button>
                        </div>
                        <div class="bg-slate-50 rounded-xl overflow-hidden border border-slate-200">
                            <div class="flex items-center px-4 py-2 bg-slate-100 border-b border-slate-200">
                                <div class="flex gap-1.5">
                                    <div class="w-3 h-3 rounded-full bg-red-400"></div>
                                    <div class="w-3 h-3 rounded-full bg-yellow-400"></div>
                                    <div class="w-3 h-3 rounded-full bg-green-400"></div>
                                </div>
                                <span class="ml-4 text-xs text-slate-500 font-mono">input.txt</span>
                            </div>
                            <pre class="p-4 text-sm font-mono text-slate-700 overflow-x-auto whitespace-pre-wrap bg-white">{{ data.input }}</pre>
                        </div>
                    </div>

                    <!-- 2. Diff View (Expected vs Predicted) -->
                    <div class="grid grid-cols-2 gap-4">
                        <!-- Predicted (Wrong) -->
                        <div class="space-y-2">
                            <label class="text-xs font-bold text-rose-500 uppercase tracking-wider flex items-center gap-1">
                                <XCircle class="w-4 h-4" /> Your Output
                            </label>
                            <div class="bg-rose-50 rounded-xl p-4 border border-rose-100 h-full font-mono text-sm text-slate-700 whitespace-pre-wrap shadow-sm">{{ data.predicted }}</div>
                        </div>

                        <!-- Expected (Correct) -->
                        <div class="space-y-2">
                            <label class="text-xs font-bold text-emerald-600 uppercase tracking-wider flex items-center gap-1">
                                <CheckCircle2 class="w-4 h-4" /> Expected Output
                            </label>
                            <div class="bg-emerald-50 rounded-xl p-4 border border-emerald-100 h-full font-mono text-sm text-slate-700 whitespace-pre-wrap shadow-sm">{{ data.expected }}</div>
                        </div>
                    </div>

                    <!-- 3. AI Tutor Explanation -->
                    <div class="bg-white rounded-2xl border border-indigo-100 shadow-lg p-6 relative overflow-hidden">
                        <div class="absolute top-0 left-0 w-1.5 h-full bg-gradient-to-b from-indigo-500 to-purple-500"></div>
                        <div class="flex items-start gap-4">
                            <div class="w-10 h-10 rounded-full bg-indigo-50 flex items-center justify-center shrink-0">
                                <Bot class="w-6 h-6 text-indigo-600" />
                            </div>
                            <div class="space-y-2">
                                <h4 class="font-bold text-slate-800">AI Tutor's Insight</h4>
                                <div class="text-slate-600/90 text-sm leading-relaxed prose prose-sm max-w-none" v-html="renderMarkdown(data.reason)"></div>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- CONTENT: HINT (Progressive Disclosure) -->
                <div v-else-if="type === 'hint'" class="space-y-6 animate-fade-in">
                    
                    <!-- Hint Level Selection (if no data yet) -->
                    <div v-if="!data" class="space-y-4">
                        <div class="text-center mb-6">
                            <h4 class="text-lg font-bold text-slate-800 mb-2">어떤 힌트가 필요하신가요?</h4>
                            <p class="text-sm text-slate-500">단계별로 힌트를 확인할 수 있습니다</p>
                        </div>

                        <!-- Level Cards -->
                        <div v-for="level in [1, 2, 3]" :key="level" 
                             class="group relative bg-white rounded-2xl p-6 border-2 border-slate-200 hover:border-amber-400 transition-all cursor-pointer hover:shadow-lg"
                             @click="$emit('request-hint-level', level)">
                            
                            <!-- Lock Icon Overlay -->
                            <div class="absolute top-4 right-4 w-10 h-10 rounded-full bg-amber-50 flex items-center justify-center group-hover:bg-amber-100 transition-colors">
                                <component :is="level === 1 ? Lightbulb : level === 2 ? Zap : Trophy" class="w-5 h-5 text-amber-600" />
                            </div>

                            <div class="pr-12">
                                <div class="flex items-center gap-3 mb-2">
                                    <span class="px-3 py-1 rounded-full bg-amber-100 text-amber-700 text-xs font-bold">Level {{ level }}</span>
                                    <span class="text-lg">{{ level === 1 ? '5🌰' : level === 2 ? '10🌰' : '15🌰' }}</span>
                                </div>
                                <h5 class="font-bold text-slate-800 mb-1">
                                    {{ level === 1 ? '알고리즘 유형' : level === 2 ? '접근 아이디어' : '상세 가이드' }}
                                </h5>
                                <p class="text-sm text-slate-600">
                                    {{ level === 1 ? '어떤 알고리즘을 써야 할까요?' : level === 2 ? '핵심 로직 힌트' : '코드 구조 및 구현 팁' }}
                                </p>
                            </div>

                            <div class="absolute inset-0 rounded-2xl bg-gradient-to-r from-amber-500 to-orange-500 opacity-0 group-hover:opacity-5 transition-opacity pointer-events-none"></div>
                        </div>
                    </div>

                    <!-- Hint Content (after selection) -->
                    <div v-else class="space-y-6 animate-fade-in-up">
                        <!-- Level Badge -->
                        <div class="flex items-center justify-between">
                            <div class="flex items-center gap-3">
                                <div class="w-12 h-12 rounded-full bg-amber-100 flex items-center justify-center">
                                    <Lightbulb class="w-6 h-6 text-amber-600" />
                                </div>
                                <div>
                                    <span class="block text-xs font-bold text-amber-600 uppercase tracking-wider">Hint Level {{ data.level }}</span>
                                    <span class="block text-sm text-slate-500">{{ data.level === 1 ? '5🌰 사용됨' : data.level === 2 ? '10🌰 사용됨' : '15🌰 사용됨' }}</span>
                                </div>
                            </div>
                            <button @click="$emit('back-to-selection')" class="text-sm text-slate-500 hover:text-slate-700 underline">
                                다른 레벨 선택
                            </button>
                        </div>

                        <!-- Hint Card -->
                        <div class="bg-gradient-to-br from-amber-50 to-orange-50 rounded-2xl p-8 border border-amber-100 shadow-lg">
                            <div class="prose prose-sm max-w-none">
                                <div class="text-slate-700 leading-relaxed text-base prose prose-sm max-w-none" v-html="renderMarkdown(data.hint)"></div>
                            </div>
                        </div>

                        <!-- Next Level Suggestion -->
                        <div v-if="data.level < 3" class="bg-white rounded-xl p-4 border border-slate-200 flex items-center justify-between">
                            <div class="flex items-center gap-3">
                                <div class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center">
                                    <ArrowRight class="w-4 h-4 text-slate-600" />
                                </div>
                                <span class="text-sm text-slate-600">더 자세한 힌트가 필요하신가요?</span>
                            </div>
                            <button 
                                @click="$emit('request-hint-level', data.level + 1)" 
                                class="px-4 py-2 bg-amber-500 text-white rounded-lg text-sm font-bold hover:bg-amber-600 transition-colors">
                                Level {{ data.level + 1 }} 보기
                            </button>
                        </div>
                    </div>

                </div>

                <!-- CONTENT: REVIEW (Analysis Only - No Code Panel) -->
                <div v-else-if="type === 'review'" class="space-y-6 animate-fade-in">
                    
                    <!-- Tabs -->
                    <div v-if="data" class="flex items-center border-b border-slate-200 bg-white -mx-4 sm:-mx-6 px-4 sm:px-6">
                        <button @click="activeTab = 'insight'" 
                            class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                            :class="activeTab === 'insight' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                            <LayoutGrid class="w-4 h-4" /> Insight
                        </button>
                        <button @click="activeTab = 'structure'" 
                            class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                            :class="activeTab === 'structure' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                            <Network class="w-4 h-4" /> Structure
                        </button>
                        <button @click="activeTab = 'feedback'" 
                            class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                            :class="activeTab === 'feedback' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                            <MessageSquare class="w-4 h-4" /> Feedback
                        </button>
                    </div>

                    <!-- Tab Content -->
                    <div v-if="data" class="space-y-6">
                        <!-- TAB 1: INSIGHT -->
                        <div v-if="activeTab === 'insight'" class="space-y-6 animate-fade-in">
                            <!-- Summary -->
                            <div class="bg-gradient-to-br from-indigo-50 to-white p-6 rounded-2xl border border-indigo-100 shadow-sm">
                                <h4 class="text-xs font-bold text-indigo-400 uppercase tracking-widest mb-3">Analysis Summary</h4>
                                <div class="prose prose-sm max-w-none text-slate-800 text-lg font-medium leading-relaxed" v-html="renderMarkdown(data.summary)"></div>
                            </div>
                            
                            <!-- Complexity Cards -->
                            <div class="grid grid-cols-2 gap-4">
                                <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex flex-col items-center justify-center text-center">
                                    <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Time Complexity</span>
                                    <div class="text-3xl font-black text-slate-800 mt-2">{{ data.complexity?.time || '-' }}</div>
                                </div>
                                <div class="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex flex-col items-center justify-center text-center">
                                    <span class="text-xs font-bold text-slate-400 uppercase tracking-wider">Space Complexity</span>
                                    <div class="text-3xl font-black text-slate-800 mt-2">{{ data.complexity?.space || '-' }}</div>
                                </div>
                            </div>

                            <!-- Problem & Intuition -->
                            <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="flex items-center gap-2 text-sm font-bold text-slate-800 mb-4">
                                    🎯 문제의 본질 & 직관
                                </h4>
                                <div class="bg-slate-50 p-4 rounded-xl text-slate-600 text-sm mb-4 prose prose-sm max-w-none" v-html="renderMarkdown(data.problem?.description || '')"></div>
                                <div v-if="data.algorithm?.intuition" 
                                    class="prose prose-sm prose-slate max-w-none text-slate-600" 
                                    v-html="renderMarkdown(data.algorithm.intuition)">
                                </div>
                            </div>
                        </div>

                        <!-- TAB 2: STRUCTURE -->
                        <div v-if="activeTab === 'structure'" class="space-y-6 animate-fade-in">
                            <!-- Code Structure Map -->
                            <div v-if="data.structure?.length" class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">🏗️ 코드 구조도</h4>
                                <div class="space-y-2">
                                    <div v-for="(item, idx) in data.structure" :key="idx" class="flex items-center gap-4 p-3 rounded-xl bg-slate-50 border border-slate-100">
                                        <div class="w-8 h-8 rounded-lg bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold text-xs shrink-0">
                                            {{ idx + 1 }}
                                        </div>
                                        <div class="flex-1">
                                            <div class="font-bold text-slate-800 text-sm prose prose-sm max-w-none" v-html="renderMarkdown(item.name)"></div>
                                            <div class="text-xs text-slate-500 prose prose-xs max-w-none" v-html="renderMarkdown(item.role)"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Trace -->
                            <div v-if="data.traceExample?.steps?.length" class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">🔍 실행 추적 (Trace)</h4>
                                <div class="bg-slate-900 rounded-xl p-4 mb-4 font-mono text-xs text-blue-300">
                                    Input: {{ data.traceExample.inputExample }}
                                </div>
                                <div class="space-y-4 pl-2">
                                    <div v-for="(step, idx) in data.traceExample.steps" :key="idx" class="flex gap-4 relative">
                                        <!-- Vertical Line -->
                                        <div v-if="idx !== data.traceExample.steps.length - 1" class="absolute left-[11px] top-6 bottom-[-20px] w-0.5 bg-slate-100"></div>
                                        
                                        <div class="w-6 h-6 rounded-full bg-blue-50 border-2 border-blue-100 flex items-center justify-center shrink-0 z-10">
                                            <div class="w-2 h-2 rounded-full bg-blue-400"></div>
                                        </div>
                                        <div class="text-sm text-slate-600 pt-0.5 prose prose-sm max-w-none" v-html="renderMarkdown(step)"></div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- TAB 3: FEEDBACK -->
                        <div v-if="activeTab === 'feedback'" class="space-y-5 animate-fade-in">
                            <!-- Complexity Detail -->
                            <div class="bg-white p-5 rounded-xl border border-slate-100">
                                <h4 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
                                    <span class="w-6 h-6 rounded-lg bg-slate-100 flex items-center justify-center text-sm">⏱️</span>
                                    복잡도 상세 분석
                                </h4>
                                <div v-if="data.complexity?.explanation" 
                                    class="prose prose-sm prose-slate max-w-none text-slate-600 leading-relaxed"
                                    v-html="renderMarkdown(data.complexity.explanation)">
                                </div>
                                <div v-else class="text-sm text-slate-400">
                                    복잡도 설명이 없습니다.
                                </div>
                            </div>

                            <!-- Pitfalls -->
                            <div v-if="data.pitfalls?.items?.length" class="bg-white p-5 rounded-xl border border-slate-100">
                                <h4 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
                                    <span class="w-6 h-6 rounded-lg bg-amber-50 flex items-center justify-center text-sm">⚠️</span>
                                    주의사항
                                </h4>
                                <ul class="space-y-2">
                                    <li v-for="(item, idx) in data.pitfalls.items" :key="idx" class="flex gap-3 text-sm text-slate-600 pl-1">
                                        <span class="text-amber-500 shrink-0 mt-0.5">•</span>
                                        <span class="prose prose-sm prose-slate max-w-none leading-relaxed" v-html="renderMarkdown(item)"></span>
                                    </li>
                                </ul>
                            </div>

                            <!-- Refactor -->
                            <div v-if="data.refactor?.code" class="bg-white p-5 rounded-xl border border-slate-100">
                                <h4 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
                                    <span class="w-6 h-6 rounded-lg bg-indigo-50 flex items-center justify-center text-sm">✨</span>
                                    리팩토링 제안
                                </h4>
                                <div class="prose prose-sm prose-slate max-w-none text-slate-600 mb-4 leading-relaxed" v-html="renderMarkdown(data.refactor.explanation)"></div>
                                <div class="bg-slate-50 rounded-lg overflow-hidden border border-slate-200">
                                    <div class="px-4 py-2 bg-slate-100 text-slate-500 text-xs font-mono border-b border-slate-200 flex justify-between items-center">
                                        <span>Refactored Code</span>
                                        <button @click="copyCode(data.refactor.code)" class="hover:text-slate-800 transition-colors">Copy</button>
                                    </div>
                                    <pre class="m-0 p-4 text-sm font-mono overflow-x-auto bg-white"><code class="hljs" v-html="highlightCode(data.refactor.code, 'java')"></code></pre>
                                </div>
                            </div>

                            <!-- No Content Fallback -->
                            <div v-if="!data.complexity?.explanation && !data.pitfalls?.items?.length && !data.refactor?.code" 
                                class="text-center py-10 text-slate-400">
                                <p>피드백 데이터가 없습니다.</p>
                            </div>
                        </div>


                    </div>
                 </div>

                <!-- CONTENT: OTHER (Placeholders for now) -->
                <div v-else class="text-center text-slate-400 py-10">
                    Content for {{ type }} will be implemented next.
                </div>

            </div>
        </div>
</template>

<script setup>
import { ref } from 'vue';
import { 
  X, Bot, Bug, Lightbulb, Copy, Check,
  XCircle, CheckCircle2, Zap, Trophy, ArrowRight,
  LayoutGrid, Network, MessageSquare
} from 'lucide-vue-next';
import hljs from 'highlight.js/lib/core';
import java from 'highlight.js/lib/languages/java';
import 'highlight.js/styles/github.css';
import {marked} from 'marked';

hljs.registerLanguage('java', java);

const props = defineProps({
  isVisible: Boolean,
  type: String, // 'counter_example', 'review', 'hint'
  title: String,
  loading: Boolean,
  data: Object,
  code: String // For review mode
});

const emit = defineEmits(['close']);

const copied = ref(false);
const activeTab = ref('insight');

const getHeaderIcon = (type) => {
    switch(type) {
        case 'review': return Bot;
        case 'counter_example': return Bug;
        case 'hint': return Lightbulb;
        default: return Bot;
    }
};

const getHeaderIconClass = (type) => {
    switch(type) {
        case 'review': return 'bg-indigo-100 text-indigo-600';
        case 'counter_example': return 'bg-rose-100 text-rose-600';
        case 'hint': return 'bg-amber-100 text-amber-600';
        default: return 'bg-slate-100 text-slate-600';
    }
};

const copyInput = async () => {
    if (!props.data?.input) return;
    try {
        await navigator.clipboard.writeText(props.data.input);
        copied.value = true;
        setTimeout(() => copied.value = false, 2000);
    } catch (err) {
        console.error('Copy failed', err);
    }
};

const highlightCode = (code, language) => {
    try {
        return hljs.highlight(code, { language }).value;
    } catch (err) {
        return hljs.highlightAuto(code).value;
    }
};

const renderMarkdown = (text) => {
    if (!text) return '';
    try {
        // Convert single-quoted code variables to backtick format before markdown processing
        // Matches patterns like 'variable', 'a', 'cnt' etc. and converts to `variable`
        let processed = text.replace(/'([a-zA-Z_][a-zA-Z0-9_]*(?:\s*[+\-*/=<>!&|]+\s*[a-zA-Z0-9_]+)?)'/g, '`$1`');
        // Also handle patterns like 'c >= b + 2' with operators
        processed = processed.replace(/'([^']{1,50})'/g, (match, inner) => {
            // Only convert if it looks like code (has operators or is a variable name)
            if (/^[a-zA-Z_]/.test(inner) || /[+\-*/=<>!&|]/.test(inner)) {
                return '`' + inner + '`';
            }
            return match;
        });
        return marked(processed);
    } catch (err) {
        return text;
    }
};

const copyCode = async (code) => {
    try {
        await navigator.clipboard.writeText(code);
    } catch (err) {
        console.error('Copy failed', err);
    }
};

</script>

<style scoped>
/* Custom Scrollbar for the drawer content */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}

/* Notion-style inline code */
:deep(code:not(pre code)) {
  background-color: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
  font-weight: 500;
}

/* Dark mode support for code in dark backgrounds */
.bg-slate-900 :deep(code:not(pre code)),
.bg-\[#282c34\] :deep(code:not(pre code)) {
  background-color: rgba(96, 165, 250, 0.2);
  color: #93c5fd;
}
</style>
