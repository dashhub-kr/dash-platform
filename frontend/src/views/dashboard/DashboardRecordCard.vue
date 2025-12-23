<template>
  <div 
    class="group relative bg-white rounded-xl shadow-sm border border-slate-100 transition-all duration-300 overflow-hidden"
    :class="{ 'hover:shadow-md hover:-translate-y-0.5': !expanded, 'shadow-xl ring-1 ring-slate-200': expanded }"
  >
    <!-- Header / Collapsed View -->
    <div class="flex flex-col xl:flex-row gap-6 p-5 cursor-pointer" @click="toggleExpand">
        
      <!-- Main Content Section -->
      <div class="flex-1 min-w-0">
          <!-- Header: badges & meta -->
          <div class="flex flex-wrap items-center gap-2 mb-2">
              <a 
                :href="`https://www.acmicpc.net/problem/${record.problemNumber}`" 
                target="_blank"
                @click.stop
                class="flex items-center gap-1 text-[11px] font-black text-slate-500 bg-slate-100 px-2.5 py-1 rounded-lg hover:bg-slate-200 hover:text-indigo-600 transition-colors uppercase tracking-wider"
              >
                #{{ record.problemNumber }}
                <ExternalLink :size="10" />
              </a>
              
              <span 
                  class="px-2.5 py-1 rounded-lg text-[11px] font-black uppercase tracking-wider"
                  :class="(record.runtimeMs > 0 && record.memoryKb > 0) ? 'bg-slate-100 text-slate-600' : 'bg-red-100 text-red-600'"
              >
                  {{ record.language }}
              </span>

              <!-- Explicit PASSED/FAILED badge -->
              <span v-if="record.runtimeMs > 0 && record.memoryKb > 0" class="px-2.5 py-1 rounded-lg text-[11px] font-black bg-emerald-100 text-emerald-600">
                  PASSED
              </span>
              <span v-else class="px-2.5 py-1 rounded-lg text-[11px] font-black bg-red-100 text-red-600">
                  FAILED
              </span>

              <div class="ml-auto text-[11px] font-medium text-slate-400 flex items-center gap-1.5 md:hidden">
                  {{ formatDate(record.committedAt) }}
              </div>
          </div>

          <!-- Title -->
          <div class="flex items-start justify-between gap-4 mb-2">
              <h3 class="text-lg md:text-xl font-bold text-slate-800 leading-tight group-hover:text-indigo-600 transition-colors">
                  {{ record.title }}
              </h3>
               <div v-if="!expanded" class="text-slate-400">
                  <ChevronDown :size="20" />
               </div>
               <div v-else class="text-indigo-500">
                  <ChevronUp :size="20" />
               </div>
          </div>

          <!-- Footer: User & Date (Desktop) -->
          <div class="hidden md:flex items-center gap-3">
               <div class="flex items-center gap-2 px-2.5 py-1 bg-slate-50 rounded-full border border-slate-100">
                  <div class="w-5 h-5 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-[10px] font-bold text-white shadow-sm">
                      {{ (record.username || '?').charAt(0).toUpperCase() }}
                  </div>
                  <span class="text-xs font-bold text-slate-600 pr-1">{{ record.username || 'Unknown' }}</span>
               </div>
               <span class="text-xs font-medium text-slate-400">{{ formatDate(record.committedAt) }}</span>
          </div>
      </div>
    </div>

    <!-- Expanded View -->
    <div v-if="expanded" class="border-t border-slate-100 animate-slide-down bg-slate-50/50">
        <div class="flex flex-col h-[600px]">
            
            <!-- CODE VIEWER (Full Width) -->
            <div class="flex-1 overflow-hidden bg-slate-900 relative flex flex-col">
                <div class="p-4 bg-slate-800 border-b border-slate-700 flex justify-between items-center text-slate-300 text-xs font-bold font-mono">
                    <span>CODE</span>
                    <div class="flex gap-2">
                       <button @click.stop="findCounterExample" class="flex items-center gap-1 px-3 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded-lg transition-colors">
                           <Bug :size="14"/> 반례찾기
                       </button>
                       <button @click.stop="askTutor" class="flex items-center gap-1 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg transition-colors">
                           <Bot :size="14"/> 질문하기
                       </button>
                    </div>
                </div>
                <div class="flex-1 overflow-auto custom-scrollbar">
                     <CodeViewer
                        :code="record.code"
                        :language="record.language || 'java'"
                        :filename="`${record.title}.${getExtension(record.language)}`"
                        :comments="comments"
                        @submit-comment="submitLineComment"
                        :readOnly="false" 
                      />
                </div>
                
                <!-- Performance Metrics Bar -->
                <div class="px-4 py-3 bg-slate-800 border-t border-slate-700 flex flex-wrap gap-4 text-xs font-mono text-slate-400">
                    <div class="flex items-center gap-2">
                        <span class="font-bold text-slate-500">실행시간:</span>
                        <span class="text-emerald-400">{{ record.runtimeMs || '-' }} ms</span>
                    </div>
                    <div class="flex items-center gap-2">
                        <span class="font-bold text-slate-500">메모리:</span>
                        <span class="text-blue-400">{{ record.memoryKb ? Math.round(record.memoryKb / 1024) : '-' }} MB</span>
                    </div>
                     <div class="flex items-center gap-2">
                        <span class="font-bold text-slate-500">시간복잡도:</span>
                        <span class="text-amber-400">{{ record.timeComplexity || '-' }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                        <span class="font-bold text-slate-500">공간복잡도:</span>
                        <span class="text-purple-400">{{ record.spaceComplexity || '-' }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { ExternalLink, ChevronDown, ChevronUp, Bot, Bug, Send, Loader2 } from 'lucide-vue-next';
import CodeViewer from '../../components/CodeViewer.vue';
import { boardApi, commentApi } from '../../api/board';

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['find-counter-example', 'ask-tutor']);

const expanded = ref(false);
const loadingBoard = ref(false);
const board = ref(null);
const comments = ref([]);
// const newComment = ref(''); // Removed

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
            // Load comments
            const commentsRes = await commentApi.findByBoardId(board.value.id);
            comments.value = commentsRes.data || [];
        }
    } catch (e) {
        console.error("Failed to load review info", e);
    } finally {
        loadingBoard.value = false;
    }
};

const ensureBoardExists = async () => {
    if (board.value) return board.value;

    try {
        // Create new Code Review Board
        const res = await boardApi.create({
            title: `Code Review: ${props.record.title}`,
            content: `Code review for ${props.record.title} (${props.record.problemNumber})`,
            boardType: 'CODE_REVIEW',
            algorithmRecordId: props.record.id
        });
        board.value = res.data;
        return board.value;
    } catch (e) {
        console.error("Failed to create review board", e);
        throw e;
    }
};

const submitLineComment = async ({ lineNumber, content }) => {
    try {
        const targetBoard = await ensureBoardExists();
        const res = await commentApi.create(targetBoard.id, { 
            content, 
            lineNumber 
        });
        comments.value.push(res.data);
    } catch (e) {
        alert("리뷰 저장 실패");
    }
};

// General comment logic removed

const findCounterExample = () => {
    emit('find-counter-example', props.record);
};

const askTutor = () => {
    emit('ask-tutor', props.record);
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString();
};

const formatDateShort = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getMonth()+1}/${date.getDate()} ${date.getHours()}:${date.getMinutes()}`;
};

const getExtension = (lang) => {
    const map = {
        'java': 'java',
        'python': 'py',
        'javascript': 'js',
        'cpp': 'cpp',
        'c': 'c'
    };
    return map[lang?.toLowerCase()] || 'txt';
};
</script>

<style scoped>
.animate-slide-down {
  animation: slide-down 0.3s ease-out forwards;
}
@keyframes slide-down {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}
</style>
