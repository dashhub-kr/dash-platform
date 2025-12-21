<template>
  <div class="code-viewer bg-[#282c34] rounded-xl overflow-hidden shadow-inner border border-slate-800">
    <!-- Header -->
    <div class="px-4 py-2 bg-[#21252b] text-slate-400 text-xs font-mono border-b border-[#181a1f] flex justify-between items-center select-none">
      <span class="font-bold text-slate-500">{{ filename }}</span>
      <div class="flex items-center gap-3">
        <span class="px-2 py-0.5 rounded bg-white/5 text-slate-500 uppercase font-bold tracking-wider text-[10px]">{{ language }}</span>
        <button @click="copyCode" class="hover:text-white transition-colors flex items-center gap-1 group">
          <Copy :size="12" class="group-hover:scale-110 transition-transform"/>
          복사
        </button>
      </div>
    </div>
    
    <!-- Code Content -->
    <div class="overflow-auto max-h-[600px] custom-scrollbar bg-[#282c34]">
      <table class="w-full border-collapse">
        <tbody>
          <template v-for="(line, index) in codeLines" :key="index">
            <!-- Code Line Row -->
            <tr 
              class="group hover:bg-[#2c313c] transition-colors"
            >
              <!-- Line Number -->
              <td 
                class="w-12 text-right pr-4 py-0.5 text-slate-600 select-none font-mono text-sm border-r border-[#181a1f] bg-[#21252b]"
                :class="{'text-slate-400 font-bold': selectedLine === index + 1}"
              >
                {{ index + 1 }}
              </td>
              
              <!-- Code -->
              <td 
                class="pl-4 pr-4 py-0.5 font-mono text-sm whitespace-pre text-slate-300 relative cursor-pointer"
                @click="toggleLine(index + 1)"
              >
                <div class="absolute left-0 top-0 bottom-0 w-1 bg-indigo-500 opacity-0 group-hover:opacity-100 transition-opacity"></div>
                <code v-html="highlightLine(line)"></code>
              </td>
              
              <!-- Comment Badge (Right side indicator) -->
              <td class="w-10 text-center py-0.5 align-middle">
                <button 
                  v-if="commentsByLine[index + 1]?.length > 0"
                  @click="toggleLine(index + 1)"
                  class="text-indigo-400 hover:text-indigo-300 transition-colors"
                  title="댓글 보기"
                >
                  <MessageSquare :size="14" />
                </button>
                <button
                   v-else
                   @click="toggleLine(index + 1)"
                   class="text-slate-600 hover:text-slate-400 opacity-0 group-hover:opacity-100 transition-all font-bold text-lg leading-none"
                   title="댓글 작성"
                >
                  +
                </button>
              </td>
            </tr>

            <!-- Inline Comment Row -->
            <tr v-if="expandedLines.has(index + 1) || commentsByLine[index + 1]?.length > 0" class="bg-[#21252b]">
              <td class="border-r border-[#181a1f] bg-[#21252b]"></td>
              <td colspan="2" class="p-4 border-b border-[#181a1f] border-t border-[#181a1f] shadow-[inset_0_2px_4px_rgba(0,0,0,0.1)]">
                <!-- Existing Comments -->
                <div v-if="commentsByLine[index + 1]?.length > 0" class="space-y-3 mb-4">
                  <div 
                    v-for="comment in commentsByLine[index + 1]" 
                    :key="comment.id"
                    class="flex gap-3 animate-fade-in"
                  >
                    <div class="w-8 h-8 rounded-full bg-indigo-900/50 flex-shrink-0 flex items-center justify-center text-indigo-200 text-xs font-bold border border-indigo-500/20">
                       {{ comment.authorName?.charAt(0).toUpperCase() || 'U' }}
                    </div>
                    <div class="bg-[#2c313c] rounded-bl-xl rounded-r-xl p-3 border border-white/5 flex-1 shadow-sm">
                       <div class="flex items-center gap-2 mb-1">
                         <span class="font-bold text-slate-300 text-xs">{{ comment.authorName }}</span>
                         <span class="text-[10px] text-slate-500">{{ formatDate(comment.createdAt) }}</span>
                       </div>
                       <p class="text-slate-300 text-sm leading-relaxed">{{ comment.content }}</p>
                    </div>
                  </div>
                </div>

                <!-- New Comment Form (Only if explicitely toggled/selected) -->
                <div v-if="expandedLines.has(index + 1)" class="flex gap-3 pl-11 animate-fade-in">
                    <div class="flex-1">
                      <textarea
                        v-model="newCommentContent"
                        placeholder="이 라인에 대한 리뷰를 남겨주세요..."
                        class="w-full bg-[#1e2227] border border-slate-700 rounded-lg p-3 text-slate-200 placeholder-slate-600 text-sm focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500/50 transition-all resize-none mb-2"
                        rows="2"
                        @keyup.ctrl.enter="submitLineComment(index + 1)"
                      ></textarea>
                      <div class="flex justify-end gap-2">
                        <button 
                          @click="expandedLines.delete(index + 1)"
                          class="px-3 py-1.5 rounded-lg text-xs font-bold text-slate-500 hover:bg-slate-700 transition-colors"
                        >
                          취소
                        </button>
                        <button 
                          @click="submitLineComment(index + 1)"
                          :disabled="!newCommentContent.trim()"
                          class="px-3 py-1.5 rounded-lg text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shadow-lg shadow-indigo-500/20"
                        >
                          댓글 등록
                        </button>
                      </div>
                    </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Copy, MessageSquare } from 'lucide-vue-next';
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
  comments: { // Changed from lineComments count object to full array
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['submit-comment']);

const expandedLines = ref(new Set()); // Set of line numbers where the form is open
const newCommentContent = ref('');

const codeLines = computed(() => {
  return props.code ? props.code.split('\n') : [];
});

const commentsByLine = computed(() => {
    const map = {};
    props.comments.forEach(c => {
        if (!c.lineNumber) return;
        if (!map[c.lineNumber]) map[c.lineNumber] = [];
        map[c.lineNumber].push(c);
    });
    return map;
});

const highlightLine = (line) => {
  if (!line && line !== '') return '';
  // Empty line handling
  if (line.trim() === '') return '&nbsp;';
  
  try {
    return hljs.highlight(line, { language: props.language }).value;
  } catch (e) {
    return line;
  }
};

const toggleLine = (lineNumber) => {
    if (expandedLines.value.has(lineNumber)) {
        expandedLines.value.delete(lineNumber);
    } else {
        // Close others? GitHub allows multiple open. Let's allow multiple.
        expandedLines.value.add(lineNumber);
        // Clear input when opening new? Ideally we persist input per line but for simplicity global ref
        newCommentContent.value = '';
    }
};

const copyCode = () => {
  navigator.clipboard.writeText(props.code);
  alert('코드가 복사되었습니다.');
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString();
};

const submitLineComment = (lineNumber) => {
  if (!newCommentContent.value.trim()) return;
  
  emit('submit-comment', { 
    lineNumber, 
    content: newCommentContent.value 
  });
  
  newCommentContent.value = '';
  expandedLines.value.delete(lineNumber);
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #21252b;
  border-left: 1px solid #181a1f;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #4b5563;
  border-radius: 5px;
  border: 2px solid #21252b;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fade-in 0.2s ease-out forwards;
}
</style>
