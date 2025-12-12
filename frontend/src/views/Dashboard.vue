<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500/30">
    <main class="container mx-auto px-6 py-10">
      <!-- Header -->
      <section class="mb-12 flex justify-between items-end">
        <div>
          <h1 class="text-3xl md:text-4xl font-bold mb-2 animate-fade-in-up">
            <span class="text-indigo-400">스터디</span> 대시보드
          </h1>
          <p class="text-slate-400 animate-fade-in-up delay-100">
            팀원들의 최근 알고리즘 풀이 현황입니다.
          </p>
        </div>
        <div class="flex gap-2 animate-fade-in-up delay-200">
            <button @click="$router.push('/simcity')" class="px-4 py-2 rounded-lg bg-green-600/20 text-green-400 hover:bg-green-600/30 transition-colors text-sm font-medium flex items-center gap-2">
                <LayoutGrid :size="16" />
                심시티
            </button>
            <button @click="$router.push('/youtube')" class="px-4 py-2 rounded-lg bg-red-600/20 text-red-400 hover:bg-red-600/30 transition-colors text-sm font-medium flex items-center gap-2">
                <Youtube :size="16" />
                영상 학습
            </button>
        </div>
      </section>

      <!-- Algorithm Cards Grid -->
      <section v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="i in 6" :key="i" class="h-48 rounded-2xl bg-slate-900/50 border border-white/5 animate-pulse"></div>
      </section>

      <section v-else-if="records.length === 0" class="text-center py-20 bg-slate-900/30 rounded-3xl border border-white/5 border-dashed">
        <div class="w-16 h-16 bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4 text-slate-500">
            <Code2 :size="32" />
        </div>
        <h3 class="text-lg font-semibold text-slate-300 mb-2">아직 기록이 없습니다</h3>
        <p class="text-slate-500">문제를 풀고 깃허브에 커밋해보세요!</p>
      </section>

      <section v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
            v-for="record in records" 
            :key="record.id" 
            class="group bg-slate-900/50 border border-white/10 hover:border-indigo-500/50 rounded-2xl p-6 transition-all hover:bg-slate-900 hover:shadow-xl hover:shadow-indigo-500/10 flex flex-col relative overflow-hidden"
        >
          <!-- Background Glow -->
          <div class="absolute -top-10 -right-10 w-32 h-32 bg-indigo-500/10 rounded-full blur-3xl group-hover:bg-indigo-500/20 transition-all"></div>

          <!-- Header: User & Meta -->
          <div class="flex justify-between items-start mb-4 relative z-10">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full bg-indigo-500/20 border border-indigo-500/30 flex items-center justify-center text-indigo-300 font-bold shrink-0">
                {{ (record.repositoryName || 'U').substring(0, 1).toUpperCase() }}
              </div>
              <div class="overflow-hidden">
                <p class="text-sm font-medium text-slate-200 truncate">{{ record.repositoryName || 'Unknown User' }}</p>
                <p class="text-xs text-slate-500">{{ formatDate(record.committedAt) }}</p>
              </div>
            </div>
            <div class="px-2 py-1 rounded bg-slate-800 text-xs font-mono text-slate-400 border border-white/5">
                {{ record.language }}
            </div>
          </div>

          <!-- Content: Problem Title -->
          <div class="mb-4 relative z-10 flex-grow">
            <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-bold text-slate-500">#{{ record.problemNumber }}</span>
                <a :href="`https://www.acmicpc.net/problem/${record.problemNumber}`" target="_blank" class="text-xs text-indigo-400 hover:underline flex items-center gap-0.5">
                    문제 보기 <ExternalLink :size="10" />
                </a>
            </div>
            <h3 class="text-lg font-bold text-white group-hover:text-indigo-300 transition-colors line-clamp-2">
                {{ record.title }}
            </h3>
          </div>

          <!-- Metrics -->
          <div class="grid grid-cols-2 gap-2 mb-4 relative z-10">
            <div class="bg-slate-950/50 rounded-lg p-2 flex flex-col items-center border border-white/5">
                <Zap :size="14" class="text-yellow-400 mb-1" />
                <span class="text-xs text-slate-400">{{ record.runtimeMs }} ms</span>
            </div>
            <div class="bg-slate-950/50 rounded-lg p-2 flex flex-col items-center border border-white/5">
                <Database :size="14" class="text-blue-400 mb-1" />
                <span class="text-xs text-slate-400">{{ record.memoryKb }} KB</span>
            </div>
          </div>

          <!-- Actions (AI) -->
          <div class="grid grid-cols-2 gap-2 relative z-10 mt-auto">
            <button 
                @click="requestReview(record)" 
                class="flex items-center justify-center gap-2 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-semibold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="processing === record.id"
            >
                <Bot :size="16" />
                <span v-if="processing === record.id">분석 중...</span>
                <span v-else>AI 리뷰</span>
            </button>
            <button 
                @click="requestHint(record)" 
                class="flex items-center justify-center gap-2 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm font-medium transition-colors border border-white/10"
            >
                <Lightbulb :size="16" class="text-yellow-400" />
                힌트
            </button>
          </div>
        </div>
      </section>

      <!-- AI Modal -->
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="closeModal"></div>
        <div class="bg-slate-900 border border-white/10 rounded-2xl w-full max-w-2xl max-h-[80vh] overflow-y-auto relative shadow-2xl animate-fade-in-up">
            <div class="sticky top-0 bg-slate-900/95 backdrop-blur border-b border-white/10 p-4 flex justify-between items-center z-10">
                <h3 class="text-xl font-bold text-white flex items-center gap-2">
                    <Bot v-if="modalType === 'review'" class="text-indigo-400" />
                    <Lightbulb v-else class="text-yellow-400" />
                    {{ modalTitle }}
                </h3>
                <button @click="closeModal" class="p-1 rounded-full hover:bg-white/10 text-slate-400">
                    <X :size="20" />
                </button>
            </div>
            <div class="p-6">
                <!-- Loading State -->
                <div v-if="modalLoading" class="flex flex-col items-center justify-center py-10">
                    <div class="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mb-4"></div>
                    <p class="text-slate-400 animate-pulse">AI가 열심히 분석하고 있습니다...</p>
                </div>

                <!-- Review Content -->
                <div v-else-if="modalType === 'review' && modalData" class="space-y-6">
                    <div class="p-4 bg-indigo-500/10 border border-indigo-500/20 rounded-xl">
                        <h4 class="font-bold text-indigo-300 mb-2">분석 요약</h4>
                        <p class="text-slate-300 text-sm leading-relaxed">{{ modalData.summary }}</p>
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                         <div class="p-4 bg-slate-800/50 rounded-xl">
                            <h4 class="font-semibold text-slate-300 mb-1 text-sm">시간 복잡도</h4>
                            <p class="text-lg font-mono text-white">{{ modalData.complexity?.time || 'N/A' }}</p>
                        </div>
                        <div class="p-4 bg-slate-800/50 rounded-xl">
                            <h4 class="font-semibold text-slate-300 mb-1 text-sm">공간 복잡도</h4>
                            <p class="text-lg font-mono text-white">{{ modalData.complexity?.space || 'N/A' }}</p>
                        </div>
                    </div>

                     <div>
                        <h4 class="font-bold text-slate-200 mb-2 flex items-center gap-2">
                             <TrendingUp :size="18" class="text-green-400" /> 개선할 점
                        </h4>
                        <ul class="list-disc list-inside space-y-1 text-sm text-slate-300 bg-slate-800/30 p-4 rounded-xl">
                            <li v-for="(item, idx) in (modalData.pitfalls?.improvements || ['완벽합니다!'])" :key="idx">
                                {{ item }}
                            </li>
                        </ul>
                    </div>
                </div>

                <!-- Hint Content -->
                <div v-else-if="modalType === 'hint' && modalData" class="space-y-6">
                     <div class="p-6 bg-yellow-500/10 border border-yellow-500/20 rounded-xl text-center">
                        <Lightbulb :size="32" class="text-yellow-400 mx-auto mb-3" />
                        <h4 class="font-bold text-yellow-300 mb-2 text-lg">Level {{ modalData.level }} 힌트</h4>
                        <p class="text-slate-200 text-lg font-medium leading-relaxed">"{{ modalData.hint }}"</p>
                    </div>

                    <div>
                        <h4 class="font-semibold text-slate-300 mb-2 text-sm">관련 개념</h4>
                        <div class="flex flex-wrap gap-2">
                            <span v-for="tag in (modalData.relatedConcepts || [])" :key="tag" class="px-3 py-1 rounded-full bg-slate-800 text-slate-300 text-xs border border-white/10">
                                {{ tag }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { dashboardApi } from '../api/dashboard';
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
  Youtube
} from 'lucide-vue-next';

const records = ref([]);
const loading = ref(true);

const processing = ref(null);
const showModal = ref(false);
const modalType = ref(''); // 'review' | 'hint'
const modalTitle = ref('');
const modalLoading = ref(false);
const modalData = ref(null);

onMounted(async () => {
  try {
    const res = await dashboardApi.getRecords();
    records.value = res.data;
  } catch (e) {
    console.error("Failed to load records", e);
  } finally {
    loading.value = false;
  }
});

const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};

const requestReview = async (record) => {
    openModal('review', 'AI 코드 리뷰');
    modalLoading.value = true;
    try {
        // 백엔드 사양에 맞춰 코드 리뷰 요청 페이로드를 구성합니다.
        // 백엔드는 algorithmRecordId, code, language, problemNumber를 예상합니다.
        // 클라이언트는 POST /api/ai/review 로 다음 바디를 보냅니다:
        // { "algorithmRecordId": ..., "code": ..., "language": ..., "problemNumber": ... }
        
        // 백엔드 컨트롤러 구현을 확인하여 올바르게 호출합니다.
        // 표준 POST /api/ai/review 를 가정합니다.
        const res = await http.post('/ai/review', {
            algorithmRecordId: record.id,
            code: record.code,
            language: record.language,
            problemNumber: String(record.problemNumber)
        });
        modalData.value = res.data;
    } catch (e) {
        console.error(e);
        modalData.value = { summary: '코드를 분석하는 도중 오류가 발생했습니다.' };
    } finally {
        modalLoading.value = false;
    }
};

const requestHint = async (record) => {
    openModal('hint', '맞춤형 힌트');
    modalLoading.value = true;
     try {
        const res = await http.post('/ai/hint', {
            userId: record.userId, 
            problemNumber: String(record.problemNumber),
            problemTitle: record.title,
            level: 1
        });
        modalData.value = res.data;
    } catch (e) {
        console.error(e);
         modalData.value = { hint: '힌트를 불러올 수 없습니다.', level: 1 };
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
.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

.delay-100 { animation-delay: 0.1s; }
.delay-200 { animation-delay: 0.2s; }

@keyframes fade-in-up {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
