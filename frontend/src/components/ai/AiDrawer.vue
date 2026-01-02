<template>
  <!-- 인라인 분할 보기를 위한 간단한 정적 드로어 -->
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

    <!-- 메인 콘텐츠 (스크롤 가능) -->
    <div class="flex-1 overflow-y-auto px-4 sm:px-6 pb-10">

                
                <!-- 로딩 상태 -->
                <div v-if="loading" class="flex flex-col items-center justify-center h-full py-20">
                    <div class="w-16 h-16 border-4 border-brand-100 border-t-brand-500 rounded-full animate-spin mb-6"></div>
                    <h4 class="text-lg font-bold text-slate-800">AI가 분석 중입니다</h4>
                    <p class="text-slate-500">잠시만 기다려주세요...</p>
                </div>

                <!-- 빈 상태 (힌트 타입 제외) -->
                <div v-else-if="!data && type !== 'tutor'" class="flex flex-col items-center justify-center h-full text-slate-500">
                    데이터가 없습니다. 다시 시도해주세요.
                </div>

                <!-- 콘텐츠: 반례 -->
                <div v-else-if="type === 'counter_example'" class="space-y-8 animate-fade-in">
                    
                    <!-- 1. 입력 섹션 (터미널 스타일) -->
                    <div class="space-y-2">
                        <div class="flex items-center justify-between">
                            <label class="text-xs font-bold text-slate-500 uppercase tracking-wider">Counterexample Input</label>
                            <button 
                                @click="copyInput" 
                                class="flex items-center gap-1.5 text-xs font-bold text-brand-500 hover:text-brand-700 transition-colors px-2 py-1 rounded bg-brand-50 hover:bg-brand-100"
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

                    <!-- 2. 비교 보기 (예상 vs 예측) -->
                    <div class="grid grid-cols-2 gap-4">
                        <!-- 예측값 (오답) -->
                        <div class="space-y-2">
                            <label class="text-xs font-bold text-rose-500 uppercase tracking-wider flex items-center gap-1">
                                <XCircle class="w-4 h-4" /> Your Output
                            </label>
                            <div class="bg-rose-50 rounded-xl p-4 border border-rose-100 h-full font-mono text-sm text-slate-700 whitespace-pre-wrap shadow-sm">{{ data.predicted }}</div>
                        </div>

                        <!-- 예상값 (정답) -->
                        <div class="space-y-2">
                            <label class="text-xs font-bold text-emerald-600 uppercase tracking-wider flex items-center gap-1">
                                <CheckCircle2 class="w-4 h-4" /> Expected Output
                            </label>
                            <div class="bg-emerald-50 rounded-xl p-4 border border-emerald-100 h-full font-mono text-sm text-slate-700 whitespace-pre-wrap shadow-sm">{{ data.expected }}</div>
                        </div>
                    </div>

                    <!-- 3. AI 튜터 설명 -->
                    <div class="bg-white rounded-2xl border border-brand-100 shadow-lg p-6 relative overflow-hidden">
                        <div class="absolute top-0 left-0 w-1.5 h-full bg-gradient-to-b from-brand-500 to-purple-500"></div>
                        <div class="flex items-start gap-4">
                            <div class="w-10 h-10 rounded-full bg-brand-50 flex items-center justify-center shrink-0">
                                <Bot class="w-6 h-6 text-brand-600" />
                            </div>
                            <div class="space-y-2">
                                <h4 class="font-bold text-slate-800">AI Tutor's Insight</h4>
                                <div class="text-slate-600/90 text-sm leading-relaxed prose prose-sm max-w-none" v-html="renderMarkdown(data.reason)"></div>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- 콘텐츠: 힌트 (채팅 기반) -->
                <div v-else-if="type === 'tutor'" class="space-y-6 animate-fade-in">
                    
                    <!-- 헤더 -->
                    <div class="flex items-center gap-3 mb-4">
                        <div class="w-12 h-12 rounded-full bg-gradient-to-br from-brand-500 to-purple-600 flex items-center justify-center shadow-lg">
                            <MessageSquare class="w-6 h-6 text-white" />
                        </div>
                        <div>
                            <h4 class="font-bold text-slate-800 text-lg">💡 AI 튜터에게 물어보세요</h4>
                            <p class="text-sm text-slate-500">질문당 도토리 <span class="text-amber-500 font-bold">3개</span>가 사용됩니다.</p>
                        </div>
                    </div>

                    <!-- 채팅 메시지 -->
                    <div class="space-y-3 mb-4 max-h-[400px] overflow-y-auto" ref="chatContainer">
                        <div v-for="(msg, idx) in chatMessages" :key="idx"
                             class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
                            <div class="max-w-[85%] px-4 py-3 rounded-2xl text-sm"
                                 :class="msg.role === 'user' 
                                     ? 'bg-brand-500 text-white rounded-br-sm' 
                                     : 'bg-white border border-slate-200 text-slate-700 rounded-bl-sm shadow-sm'">
                                <div v-html="renderMarkdown(msg.content)"></div>
                            </div>
                        </div>
                        <!-- 로딩 표시기 -->
                        <div v-if="chatLoading" class="flex justify-start">
                            <div class="bg-white border border-slate-200 px-4 py-3 rounded-2xl rounded-bl-sm shadow-sm flex items-center gap-3">
                                <div class="flex gap-1.5">
                                    <div class="w-2 h-2 bg-brand-400 rounded-full animate-bounce"></div>
                                    <div class="w-2 h-2 bg-brand-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                                    <div class="w-2 h-2 bg-brand-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
                                </div>
                                <span class="text-xs text-slate-400 font-medium">AI가 답변을 생성중입니다...</span>
                            </div>
                        </div>
                    </div>

                    <!-- 빠른 답장 -->
                    <div v-if="quickReplies.length > 0 && !chatLoading" class="flex flex-wrap gap-2 mb-4">
                        <button v-for="(reply, idx) in quickReplies" :key="idx"
                                @click="sendChatMessage(reply)"
                                class="px-3 py-2 bg-brand-50 border border-brand-200 text-brand-700 rounded-xl text-xs font-medium hover:bg-brand-100 transition-colors flex items-start gap-2 shadow-sm w-full text-left">
                                <MessageSquare class="w-3.5 h-3.5 shrink-0 mt-0.5" />
                                <span class="leading-relaxed">{{ reply }}</span>
                        </button>
                    </div>

                    <!-- 채팅 입력 -->
                    <div class="flex gap-2 sticky bottom-0 bg-white pt-2">
                        <input 
                            v-model="chatInput"
                            @keyup.enter="sendChatMessage(chatInput)"
                            :disabled="chatLoading"
                            type="text" 
                            placeholder="힌트가 필요한 부분을 질문해보세요..."
                            class="flex-1 px-4 py-3 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                        />
                        <button 
                            @click="sendChatMessage(chatInput)"
                            :disabled="!chatInput.trim() || chatLoading"
                            class="px-5 py-3 bg-gradient-to-r from-brand-500 to-purple-600 text-white rounded-xl font-bold text-sm hover:from-brand-600 hover:to-purple-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-md">
                            전송
                        </button>
                    </div>

                </div>

                <!-- 콘텐츠: 리뷰 (분석 전용 - 코드 패널 없음) -->
                <div v-else-if="type === 'review'" class="space-y-6 animate-fade-in">
                    
                    <!-- 탭 -->
                    <div v-if="data" class="flex items-center border-b border-slate-200 bg-white -mx-4 sm:-mx-6 px-4 sm:px-6">
                        <button @click="activeTab = 'insight'" 
                            class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                            :class="activeTab === 'insight' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                            <LayoutGrid class="w-4 h-4" /> Insight
                        </button>
                        <button @click="activeTab = 'structure'" 
                            class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                            :class="activeTab === 'structure' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                            <Network class="w-4 h-4" /> Structure
                        </button>
                        <button @click="activeTab = 'feedback'" 
                            class="px-4 py-4 text-sm font-bold border-b-2 transition-colors flex items-center gap-2"
                            :class="activeTab === 'feedback' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-500 hover:text-slate-800'">
                            <MessageSquare class="w-4 h-4" /> Feedback
                        </button>
                    </div>

                    <!-- 탭 콘텐츠 -->
                    <div v-if="data" class="space-y-6">
                        <!-- 탭 1: 통찰 -->
                        <div v-if="activeTab === 'insight'" class="space-y-6 animate-fade-in">
                            <!-- 요약 -->
                            <div class="bg-gradient-to-br from-brand-50 to-white p-6 rounded-2xl border border-brand-100 shadow-sm">
                                <h4 class="text-xs font-bold text-brand-400 uppercase tracking-widest mb-3">Analysis Summary</h4>
                                <div class="prose prose-sm max-w-none text-slate-800 text-lg font-medium leading-relaxed" v-html="renderMarkdown(data.summary)"></div>
                            </div>
                            
                            <!-- 복잡도 카드 -->
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

                            <!-- 문제 & 직관 -->
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

                        <!-- 탭 2: 구조 -->
                        <div v-if="activeTab === 'structure'" class="space-y-6 animate-fade-in">
                            <!-- 코드 구조 맵 -->
                            <div v-if="data.structure?.length" class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">🏗️ 코드 구조도</h4>
                                <div class="space-y-2">
                                    <div v-for="(item, idx) in data.structure" :key="idx" class="flex items-center gap-4 p-3 rounded-xl bg-slate-50 border border-slate-100">
                                        <div class="w-8 h-8 rounded-lg bg-brand-100 flex items-center justify-center text-brand-600 font-bold text-xs shrink-0">
                                            {{ idx + 1 }}
                                        </div>
                                        <div class="flex-1">
                                            <div class="font-bold text-slate-800 text-sm prose prose-sm max-w-none" v-html="renderMarkdown(item.name)"></div>
                                            <div class="text-xs text-slate-500 prose prose-xs max-w-none" v-html="renderMarkdown(item.role)"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- 추적 -->
                            <div v-if="data.traceExample?.steps?.length" class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                                <h4 class="text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">🔍 실행 추적 (Trace)</h4>
                                <div class="bg-slate-900 rounded-xl p-4 mb-4 font-mono text-xs text-blue-300">
                                    Input: {{ data.traceExample.inputExample }}
                                </div>
                                <div class="space-y-4 pl-2">
                                    <div v-for="(step, idx) in data.traceExample.steps" :key="idx" class="flex gap-4 relative">
                                        <!-- 수직선 -->
                                        <div v-if="idx !== data.traceExample.steps.length - 1" class="absolute left-[11px] top-6 bottom-[-20px] w-0.5 bg-slate-100"></div>
                                        
                                        <div class="w-6 h-6 rounded-full bg-blue-50 border-2 border-blue-100 flex items-center justify-center shrink-0 z-10">
                                            <div class="w-2 h-2 rounded-full bg-blue-400"></div>
                                        </div>
                                        <div class="text-sm text-slate-600 pt-0.5 prose prose-sm max-w-none" v-html="renderMarkdown(step)"></div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 탭 3: 피드백 -->
                        <div v-if="activeTab === 'feedback'" class="space-y-5 animate-fade-in">
                            <!-- 복잡도 상세 -->
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

                            <!-- 함정 -->
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

                            <!-- 리팩토링 -->
                            <div v-if="data.refactor?.code" class="bg-white p-5 rounded-xl border border-slate-100">
                                <h4 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
                                    <span class="w-6 h-6 rounded-lg bg-brand-50 flex items-center justify-center text-sm">✨</span>
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

                            <!-- 콘텐츠 없음 대체 -->
                            <div v-if="!data.complexity?.explanation && !data.pitfalls?.items?.length && !data.refactor?.code" 
                                class="text-center py-10 text-slate-400">
                                <p>피드백 데이터가 없습니다.</p>
                            </div>
                        </div>


                    </div>
                 </div>

                <!-- 콘텐츠: 기타 (현재 자리 표시자) -->
                <div v-else class="text-center text-slate-400 py-10">
                    Content for {{ type }} will be implemented next.
                </div>

            </div>
        </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import { 
  X, Bot, Bug, Lightbulb, Copy, Check,
  XCircle, CheckCircle2, Zap, Trophy, ArrowRight,
  LayoutGrid, Network, MessageSquare
} from 'lucide-vue-next';
import hljs from 'highlight.js/lib/core';
import java from 'highlight.js/lib/languages/java';
import 'highlight.js/styles/github.css';
import {marked} from 'marked';
import { aiApi } from '@/api/ai';

hljs.registerLanguage('java', java);

const props = defineProps({
  isVisible: Boolean,
  type: String, // 'counter_example', 'review', 'tutor'
  title: String,
  loading: Boolean,
  data: Object,
  code: String, // 리뷰 모드용
  recordId: Number,      // 알고리즘 기록 ID (DB 조회용)
  userId: Number,        // 사용자 ID
  solveStatus: String,   // "solved" | "wrong"
  wrongReason: String,   // 틀린 이유 (시간초과, 틀렸습니다 등)
  problemNumber: String, // 힌트 채팅용 (대체)
  problemTitle: String   // 힌트 채팅용 (대체)
});

const emit = defineEmits(['close', 'acorn-used']);

const copied = ref(false);
const activeTab = ref('insight');

// 힌트 채팅 상태
const chatMessages = ref([]);
const chatInput = ref('');
const chatLoading = ref(false);
const quickReplies = ref(['🤔 이 문제 어떻게 접근하지?', '💡 알고리즘 유형이 뭐야?', '🐛 왜 틀렸을까?']);

// 드로어가 닫히거나 힌트가 변경될 때 채팅 초기화
watch([() => props.isVisible, () => props.solveStatus], ([visible, status]) => {
    if (!visible) {
        chatMessages.value = [];
        chatInput.value = '';
        return;
    }
    
    // 풀이 상태에 따라 초기 빠른 답장 설정
    if (status === 'solved') {
        quickReplies.value = [
            '⚡ 시간 복잡도를 더 줄일 수 있나요?', 
            '🛠 이 코드의 개선점을 알려주세요.',
            '🧩 다른 방식의 풀이도 있을까요?'
        ];
    } else {
        quickReplies.value = [
             '🤔 이 문제 어떻게 접근해야 해?', 
             '💡 어떤 알고리즘을 써야 할까?', 
             '🐛 왜 틀렸는지 힌트 좀 줘!'
        ];
    }
}, { immediate: true });

const scrollToBottom = async () => {
    await nextTick();
    if (chatContainer.value) {
        chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
};

// 채팅 메시지가 추가되면 스크롤
watch(() => chatMessages.value.length, scrollToBottom);

// 로딩 상태가 변하면 스크롤 (로딩 시작/끝)
watch(chatLoading, scrollToBottom);

const sendChatMessage = async (message) => {
    if (!message?.trim() || chatLoading.value) return;
    
    const trimmedMessage = message.trim();
    chatInput.value = '';
    
    // 사용자 메시지 추가
    chatMessages.value.push({ role: 'user', content: trimmedMessage });
    chatLoading.value = true;
    
    try {
        // AI 튜터 대화 요청
        const response = await aiApi.tutorChat({
            userId: props.userId,
            recordId: props.recordId,
            message: trimmedMessage,
            solveStatus: props.solveStatus || 'wrong',
            wrongReason: props.wrongReason,
            history: chatMessages.value.slice(0, -1) // 마지막 user 메시지 제외
        });
        
        // 어시스턴트 메시지 추가
        chatMessages.value.push({ role: 'assistant', content: response.data.reply });
        
        emit('acorn-used');
        
        // 후속 질문으로 빠른 답장 업데이트
        if (response.data.followUpQuestions?.length > 0) {
            quickReplies.value = response.data.followUpQuestions.slice(0, 3);
        } else {
            quickReplies.value = [];
        }
    } catch (error) {
        console.error('Hint chat failed:', error);
        
        // Handle insufficient acorns specifically
        const errorMsg = error.response?.data?.message || '';
        if (errorMsg.includes('Not enough acorns')) {
            chatMessages.value.push({
                role: 'assistant',
                content: '도토리가 부족해 응답을 생성할 수 없습니다.'
            });
        } else {
            chatMessages.value.push({ 
                role: 'assistant', 
                content: '죄송해요, 일시적인 오류가 발생했어요. 다시 시도해주세요.' 
            });
        }
    } finally {
        chatLoading.value = false;
    }
};

const getHeaderIcon = (type) => {
    switch(type) {
        case 'review': return Bot;
        case 'counter_example': return Bug;
        case 'tutor': return Lightbulb;
        default: return Bot;
    }
};

const getHeaderIconClass = (type) => {
    switch(type) {
        case 'review': return 'bg-brand-100 text-brand-600';
        case 'counter_example': return 'bg-rose-100 text-rose-600';
        case 'tutor': return 'bg-amber-100 text-amber-600';
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
        // 마크다운 처리 전 작은따옴표 코드 변수를 백틱 형식으로 변환
        // 'variable', 'a', 'cnt' 등의 패턴과 일치하며 `variable`로 변환
        let processed = text.replace(/'([a-zA-Z_][a-zA-Z0-9_]*(?:\s*[+\-*/=<>!&|]+\s*[a-zA-Z0-9_]+)?)'/g, '`$1`');
        // 연산자가 있는 'c >= b + 2' 등의 패턴도 처리
        processed = processed.replace(/'([^']{1,50})'/g, (match, inner) => {
            // 코드로 보이는 경우(연산자가 있거나 변수명인 경우)에만 변환
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
/* 드로어 콘텐츠용 커스텀 스크롤바 */
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

/* 기본 인라인 코드 스타일 (AI 응답용 - 흰색/밝은 배경) */
:deep(code:not(pre code)) {
  background-color: rgba(99, 102, 241, 0.1);
  color: #4338ca;
  padding: 0.15em 0.4em;
  border-radius: 4px;
  font-size: 0.85em;
  font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
  font-weight: 500;
}

/* 사용자 메시지의 인라인 코드 (인디고/파란색 배경) */
.bg-brand-500 :deep(code:not(pre code)) {
  background-color: rgba(255, 255, 255, 0.25);
  color: #fff;
}

/* 인디고 빠른 답장의 인라인 코드 */
.bg-brand-50 :deep(code:not(pre code)),
.border-brand-200 :deep(code:not(pre code)) {
  background-color: rgba(99, 102, 241, 0.15);
  color: #4338ca;
}
</style>
