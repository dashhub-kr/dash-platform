<template>
  <div class="code-viewer bg-[#282c34] rounded-xl overflow-hidden">
    <!-- Header -->
    <div class="px-4 py-2 bg-[#21252b] text-slate-400 text-xs font-mono border-b border-[#181a1f] flex justify-between items-center">
      <span>{{ filename }}</span>
      <div class="flex items-center gap-3">
        <span class="px-2 py-0.5 rounded bg-white/10 text-slate-300 uppercase">{{ language }}</span>
        <button @click="copyCode" class="hover:text-white transition-colors flex items-center gap-1">
          <Copy :size="14" />
          복사
        </button>
      </div>
    </div>
    
    <!-- Code Content -->
    <div class="overflow-auto max-h-[500px] custom-scrollbar">
      <table class="w-full">
        <tbody>
          <tr 
            v-for="(line, index) in codeLines" 
            :key="index"
            @click="handleLineClick(index + 1)"
            :class="[
              'group transition-colors',
              selectedLine === index + 1 ? 'bg-indigo-500/20' : 'hover:bg-white/5',
              lineComments[index + 1] > 0 ? 'cursor-pointer' : ''
            ]"
          >
            <!-- Line Number -->
            <td class="w-12 text-right pr-4 py-0.5 text-slate-600 select-none font-mono text-sm border-r border-[#181a1f] sticky left-0 bg-[#21252b]">
              {{ index + 1 }}
            </td>
            
            <!-- Code -->
            <td class="pl-4 pr-4 py-0.5 font-mono text-sm whitespace-pre text-slate-300">
              <code v-html="highlightLine(line)"></code>
            </td>
            
            <!-- Comment Badge -->
            <td class="w-12 text-right pr-4 py-0.5">
              <span 
                v-if="lineComments[index + 1] > 0"
                class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-400 text-xs font-bold cursor-pointer hover:bg-indigo-500/30 transition-colors"
              >
                <MessageSquare :size="12" />
                {{ lineComments[index + 1] }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Line Comment Panel (Inline) -->
    <div 
      v-if="selectedLine && showCommentPanel" 
      class="border-t border-[#181a1f] bg-[#21252b] p-4"
    >
      <div class="flex items-center justify-between mb-3">
        <h4 class="text-sm font-bold text-white flex items-center gap-2">
          <MessageSquare :size="16" class="text-indigo-400" />
          라인 {{ selectedLine }} 댓글
        </h4>
        <button @click="closeCommentPanel" class="text-slate-400 hover:text-white">
          <X :size="18" />
        </button>
      </div>
      
      <!-- Selected Line Preview -->
      <div class="mb-4 p-3 bg-[#282c34] rounded-lg border border-white/10">
        <code class="text-sm font-mono text-slate-300" v-html="highlightLine(codeLines[selectedLine - 1])"></code>
      </div>
      
      <!-- Comments for this line -->
      <div v-if="lineCommentsData.length > 0" class="space-y-3 mb-4 max-h-40 overflow-y-auto">
        <div 
          v-for="comment in lineCommentsData" 
          :key="comment.id"
          class="p-3 bg-[#282c34] rounded-lg"
        >
          <div class="flex items-center gap-2 mb-2">
            <span class="font-medium text-slate-200 text-sm">{{ comment.authorName }}</span>
            <span class="text-xs text-slate-500">{{ formatDate(comment.createdAt) }}</span>
          </div>
          <p class="text-slate-400 text-sm">{{ comment.content }}</p>
        </div>
      </div>
      <div v-else class="text-center py-4 text-slate-500 text-sm">
        이 라인에 댓글이 없습니다.
      </div>
      
      <!-- New Comment Form -->
      <div class="flex gap-2">
        <input
          v-model="newLineComment"
          type="text"
          placeholder="라인 댓글 작성..."
          class="flex-1 bg-[#282c34] border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
          @keyup.enter="submitLineComment"
        />
        <button 
          @click="submitLineComment"
          :disabled="!newLineComment.trim()"
          class="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg text-sm font-bold transition-all"
        >
          작성
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { Copy, MessageSquare, X } from 'lucide-vue-next';
import hljs from 'highlight.js';
import 'highlight.js/styles/atom-one-dark.css';

const props = defineProps({
  code: {
    type: String,
    required: true
  },
  language: {
    type: String,
    default: 'java'
  },
  filename: {
    type: String,
    default: 'Source Code'
  },
  lineComments: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['line-click', 'submit-comment']);

const selectedLine = ref(null);
const showCommentPanel = ref(false);
const newLineComment = ref('');
const lineCommentsData = ref([]);

const codeLines = computed(() => {
  return props.code ? props.code.split('\n') : [];
});

const highlightLine = (line) => {
  if (!line) return '';
  try {
    return hljs.highlight(line, { language: props.language }).value;
  } catch (e) {
    return line;
  }
};

const handleLineClick = (lineNumber) => {
  selectedLine.value = lineNumber;
  showCommentPanel.value = true;
  emit('line-click', { lineNumber });
};

const closeCommentPanel = () => {
  showCommentPanel.value = false;
  selectedLine.value = null;
};

const copyCode = () => {
  navigator.clipboard.writeText(props.code);
  alert('코드가 복사되었습니다.');
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString();
};

const submitLineComment = () => {
  if (!newLineComment.value.trim() || !selectedLine.value) return;
  emit('submit-comment', { 
    lineNumber: selectedLine.value, 
    content: newLineComment.value 
  });
  newLineComment.value = '';
};

// Watch for line comments data from parent
watch(() => props.lineComments, () => {
  // Parent can update lineCommentsData via this watch
}, { deep: true });

// Expose method to update line comments
defineExpose({
  setLineCommentsData: (comments) => {
    lineCommentsData.value = comments;
  }
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #21252b;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #4b5563;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}
</style>
