<template>
  <div ref="rootRef" class="code-viewer bg-white border border-slate-200 rounded-xl relative">
    <!-- Header -->
    <div class="px-4 py-2 bg-slate-50 text-slate-500 text-xs font-mono border-b border-slate-200 flex justify-between items-center select-none rounded-t-xl">
      <span class="font-bold text-slate-600">{{ filename }}</span>
      <div class="flex items-center gap-3">
        <!-- Author Filter -->
        <select v-if="uniqueAuthors.length > 0" v-model="selectedAuthorFilter" class="text-[10px] bg-white border border-slate-200 rounded px-2 py-1 focus:outline-none focus:ring-1 focus:ring-brand-400">
          <option value="">모든 댓글</option>
          <option v-for="author in uniqueAuthors" :key="author" :value="author">{{ author }}</option>
        </select>
        <!-- Expand/Collapse All -->
        <button v-if="hasAnyComments" @click="toggleAllComments" class="flex items-center gap-1 text-[10px] font-bold text-brand-600 hover:text-brand-800 transition-colors">
          <span>{{ allCommentsExpanded ? '모두 접기' : '모두 펼치기' }}</span>
        </button>
        <span class="px-2 py-0.5 rounded bg-slate-200 text-slate-600 uppercase font-bold tracking-wider text-[10px]">{{ language }}</span>
        <button @click="copyCode" class="hover:text-slate-800 transition-colors flex items-center gap-1 group">
          <Copy :size="12" class="group-hover:scale-110 transition-transform"/>
          복사
        </button>
      </div>
    </div>
    
    <!-- Code Content -->
    <div class="overflow-x-auto custom-scrollbar bg-white rounded-b-xl">
      <table class="w-full border-collapse">
        <tbody>
          <template v-for="(line, index) in codeLines" :key="index">
            <!-- Code Line Row -->
            <tr 
              class="group transition-colors duration-200"
              :class="{ 
                'hover:bg-slate-50': !effectiveHighlightedLines.has(index + 1),
                'bg-brand-100 text-slate-900': selectedHighlightLines.has(index + 1),
                'bg-brand-50/50': !selectedHighlightLines.has(index + 1) && hoverHighlightLines.has(index + 1) 
              }"
              :data-line-number="index + 1"
            >
              <!-- Line Number (Sticky) -->
              <td 
                class="w-10 text-right pr-2 py-0.5 text-slate-400 select-none font-mono text-sm border-r border-slate-100 bg-slate-50 sticky left-0 z-10"
                :class="{'text-slate-600 font-bold': selectedLine === index + 1, 'bg-brand-100 text-brand-600 font-bold': effectiveHighlightedLines.has(index + 1)}"
              >
                {{ index + 1 }}
              </td>
              
              <!-- Code -->
              <td 
                class="pl-2 pr-4 py-0.5 font-mono text-sm whitespace-pre text-slate-700 relative cursor-pointer"
                @click="toggleLine(index + 1)"
                @mouseenter="handleLineHover(index + 1, $event)"
                @mouseleave="handleLineLeave"
              >
                <code v-html="highlightLine(line)"></code>
              </td>
              
              <!-- Comment Badge (Right side indicator) -->
              <td class="w-16 text-center py-0.5 align-middle">
                <!-- Collapsed: Show stacked avatars -->
                <div v-if="filteredCommentsByLine[index + 1]?.length > 0 && !isLineExpanded(index + 1)" 
                     class="flex items-center justify-center cursor-pointer" 
                     @click="toggleLine(index + 1)">
                  <div class="flex -space-x-2">
                    <template v-for="(comment, cidx) in filteredCommentsByLine[index + 1].slice(0, 2)" :key="cidx">
                      <img v-if="comment.authorProfileImageUrl" 
                           :src="comment.authorProfileImageUrl" 
                           :title="comment.authorName"
                           class="w-5 h-5 rounded-full border-2 border-white shadow-sm object-cover" />
                      <div v-else
                           class="w-5 h-5 rounded-full bg-brand-100 border-2 border-white flex items-center justify-center text-[8px] font-bold text-brand-600 shadow-sm"
                           :title="comment.authorName">
                        {{ comment.authorName?.charAt(0).toUpperCase() || 'U' }}
                      </div>
                    </template>
                    <div v-if="filteredCommentsByLine[index + 1].length > 2" 
                         class="w-5 h-5 rounded-full bg-slate-200 border-2 border-white flex items-center justify-center text-[8px] font-bold text-slate-600 shadow-sm">
                      +{{ filteredCommentsByLine[index + 1].length - 2 }}
                    </div>
                  </div>
                </div>
                <!-- Expanded: Show message icon -->
                <button 
                  v-else-if="filteredCommentsByLine[index + 1]?.length > 0"
                  @click="toggleLine(index + 1)"
                  class="text-brand-500 hover:text-brand-600 transition-colors"
                  title="댓글 접기"
                >
                  <MessageSquare :size="14" />
                </button>
                <!-- No comments: Show add button on hover -->
                <button
                   v-else
                   @click="toggleLine(index + 1)"
                   class="text-slate-300 hover:text-slate-500 opacity-0 group-hover:opacity-100 transition-all font-bold text-lg leading-none"
                   title="댓글 작성"
                >
                  +
                </button>
              </td>
            </tr>

            <!-- Inline Comment Row -->
            <tr v-if="isLineExpanded(index + 1) || (allCommentsExpanded && filteredCommentsByLine[index + 1]?.length > 0)" class="bg-slate-50">
              <td class="border-r border-slate-100 bg-slate-50/50"></td>
              <td colspan="2" class="px-4 py-2 border-b border-slate-100 border-t border-slate-100">
                <!-- Existing Comments (Slimmer) -->
                <div v-if="filteredCommentsByLine[index + 1]?.length > 0" class="space-y-2 mb-3">
                  <div 
                    v-for="comment in filteredCommentsByLine[index + 1]" 
                    :key="comment.id"
                    class="flex gap-2 items-start animate-fade-in"
                  >
                    <img v-if="comment.authorProfileImageUrl" 
                         :src="comment.authorProfileImageUrl" 
                         class="w-5 h-5 rounded-full flex-shrink-0 border border-brand-200 object-cover" />
                    <div v-else class="w-5 h-5 rounded-full bg-brand-100 flex-shrink-0 flex items-center justify-center text-brand-600 text-[9px] font-bold border border-brand-200">
                       {{ comment.authorName?.charAt(0).toUpperCase() || 'U' }}
                    </div>
                    <div class="flex-1 min-w-0">
                       <div class="flex items-center gap-2">
                         <span class="font-bold text-slate-700 text-[11px]">{{ comment.authorName }}</span>
                         <span class="text-[9px] text-slate-400">{{ formatDate(comment.createdAt) }}</span>
                       </div>
                       <p class="text-slate-600 text-xs leading-relaxed">{{ comment.content }}</p>
                    </div>
                  </div>
                </div>

                <!-- Separator between existing comments and new input -->
                <div v-if="filteredCommentsByLine[index + 1]?.length > 0 && expandedLine === index + 1" class="border-t border-dashed border-slate-200 my-2"></div>

                <!-- New Comment Form (Only if explicitly toggled/selected) -->
                <div v-if="expandedLine === index + 1" class="flex items-center gap-2 pl-7 animate-fade-in">
                    <input
                      v-model="newCommentContent"
                      placeholder="이 라인에 대한 리뷰..."
                      class="flex-1 bg-white border border-slate-200 rounded-lg px-3 py-1.5 text-slate-700 placeholder-slate-400 text-xs focus:outline-none focus:border-brand-400 focus:ring-1 focus:ring-brand-400/50 transition-all"
                      @keyup.enter.stop="submitLineComment(index + 1)"
                    />
                    <button 
                      @click="expandedLine = null"
                      class="px-2 py-1 rounded text-[10px] font-bold text-slate-500 hover:bg-slate-100 transition-colors"
                    >
                      취소
                    </button>
                    <button 
                      @click="submitLineComment(index + 1)"
                      :disabled="!newCommentContent.trim()"
                      class="px-2 py-1 rounded text-[10px] font-bold text-white bg-brand-600 hover:bg-brand-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      등록
                    </button>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <!-- AI Annotation Tooltip (Absolute, non-teleported) -->
    <div v-if="hoveredLine && keyBlocksByLine[hoveredLine]?.length > 0"
         class="absolute z-[40] bg-white text-slate-800 rounded-2xl shadow-2xl p-5 max-w-md pointer-events-none border border-brand-200"
         :style="{ left: tooltipPosition.x + 'px', top: tooltipPosition.y + 'px', transform: 'translateY(-100%) translateY(-12px)' }">
      <div class="flex items-center gap-2 mb-3 text-brand-600 font-bold text-sm">
        <span>💡</span>
        <span>AI 코드 설명</span>
      </div>
      <div v-for="(block, idx) in keyBlocksByLine[hoveredLine]" :key="idx" class="space-y-3">
        <div v-if="block.code" class="bg-slate-100 rounded-lg p-3 text-xs font-mono text-slate-700 border border-slate-200">
          {{ block.code }}
        </div>
        <p class="text-sm text-slate-600 leading-relaxed">{{ block.explanation }}</p>
      </div>
      <!-- Down Arrow (Bottom) -->
      <div class="absolute bottom-0 left-6 translate-y-[95%] text-brand-200 drop-shadow-sm">
           <div class="w-0 h-0 border-l-[8px] border-r-[8px] border-t-[8px] border-l-transparent border-r-transparent border-t-brand-200"></div>
           <div class="w-0 h-0 border-l-[6px] border-r-[6px] border-t-[6px] border-l-transparent border-r-transparent border-t-white absolute bottom-[2px] left-[-6px]"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Copy, MessageSquare } from 'lucide-vue-next';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';

// ... (keep props and other vars)
const props = defineProps({
  code: { type: String, default: '' },
  language: { type: String, default: 'java' },
  filename: { type: String, default: '' },
  comments: { type: Array, default: () => [] },
  keyBlocks: { type: Array, default: () => [] },
  readOnly: { type: Boolean, default: false }
});

const emit = defineEmits(['submit-comment']);

const rootRef = ref(null);
const selectedAuthorFilter = ref('');
const allCommentsExpanded = ref(false);
const expandedLine = ref(null);
const selectedLine = ref(null);
const selectedHighlightLines = ref(new Set());
const hoverHighlightLines = ref(new Set());
const hoveredLine = ref(null);
const rafId = ref(null);
const tooltipPosition = ref({ x: 0, y: 0 });
const newCommentContent = ref('');

const codeLines = computed(() => {
    return props.code ? props.code.split('\n') : [];
});

const keyBlocksByLine = computed(() => {
    const map = {};
    if (!props.keyBlocks) return map;
    
    props.keyBlocks.forEach(block => {
        // Find start line if code matches
        if (block.code) {
           const lines = codeLines.value;
           const targetCode = block.code.trim();
           for(let i=0; i<lines.length; i++) {
               if(lines[i].includes(targetCode)) {
                   if (!map[i+1]) map[i+1] = [];
                   map[i+1].push({...block, startLine: i+1});
               }
           }
        }
    });
    return map;
});

const effectiveHighlightedLines = computed(() => {
    const set = new Set(selectedHighlightLines.value);
    keyBlocksByLine.value && Object.keys(keyBlocksByLine.value).forEach(line => set.add(Number(line)));
    return set;
});

const uniqueAuthors = computed(() => {
    if (!props.comments) return [];
    const authors = new Set(props.comments.map(c => c.authorName).filter(Boolean));
    return Array.from(authors);
});

const filteredCommentsByLine = computed(() => {
    const map = {};
    if (!props.comments) return map;
    
    props.comments.forEach(comment => {
        if (selectedAuthorFilter.value && comment.authorName !== selectedAuthorFilter.value) return;
        
        const line = comment.lineNumber;
        if (line) {
            if (!map[line]) map[line] = [];
            map[line].push(comment);
        }
    });
    return map;
});

const hasAnyComments = computed(() => {
    return props.comments && props.comments.length > 0;
});


const highlightLine = (line) => {
    try {
        return hljs.highlight(line || ' ', { language: props.language }).value;
    } catch (e) {
        return line;
    }
};

const toggleLine = (lineNumber) => {
    if (expandedLine.value === lineNumber) {
        expandedLine.value = null;
    } else {
        expandedLine.value = lineNumber;
    }
};

const toggleAllComments = () => {
    allCommentsExpanded.value = !allCommentsExpanded.value;
};

const isLineExpanded = (lineNumber) => {
    return expandedLine.value === lineNumber;
};
const updateTooltipPosition = () => {
    if (!hoveredLine.value || !rootRef.value) return;
    
    // Check if we need to use a specific start line from the block
    const blocks = keyBlocksByLine.value[hoveredLine.value];
    let targetLineNumber = hoveredLine.value;
    
    if (blocks && blocks.length > 0 && blocks[0].startLine) {
        targetLineNumber = blocks[0].startLine;
    }

    let targetRect = null;
    const rootRect = rootRef.value.getBoundingClientRect();
    
    const tr = rootRef.value.querySelector(`tr[data-line-number="${targetLineNumber}"]`);
    if (tr) {
        const codeTd = tr.querySelector('td:nth-child(2)');
        if (codeTd) {
            targetRect = codeTd.getBoundingClientRect();
        } else {
            targetRect = tr.getBoundingClientRect();
        }
    }

    if (targetRect) {
        // Calculate position RELATIVE to the root container
        tooltipPosition.value = {
            x: targetRect.left - rootRect.left,
            y: targetRect.top - rootRect.top
        };
    }
    
    rafId.value = requestAnimationFrame(updateTooltipPosition);
};

const handleLineHover = (lineNumber, event) => {
    const lineContent = codeLines.value[lineNumber - 1];
    if (!lineContent) return;
    
    const trimmed = lineContent.trim();
    if (trimmed === '') return;
    if (/^[}\])];]+$/.test(trimmed)) return;

    const blocks = keyBlocksByLine.value[lineNumber];
    if (blocks && blocks.length > 0) {
        hoveredLine.value = lineNumber;
        
        const relatedLines = new Set();
        relatedLines.add(lineNumber);
        blocks.forEach(block => {
             for (const [lineNumStr, lineBlocks] of Object.entries(keyBlocksByLine.value)) {
                 if (lineBlocks.includes(block)) {
                     relatedLines.add(Number(lineNumStr));
                 }
             }
        });
        hoverHighlightLines.value = relatedLines;

        // Start tracking position
        if (rafId.value) cancelAnimationFrame(rafId.value);
        updateTooltipPosition();
    }
};

const handleLineLeave = () => {
    hoveredLine.value = null;
    hoverHighlightLines.value = new Set();
    if (rafId.value) {
        cancelAnimationFrame(rafId.value);
        rafId.value = null;
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
    if (!newCommentContent.value.trim()) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    emit('submit-comment', {
        lineNumber,
        content: newCommentContent.value.trim()
    });

    newCommentContent.value = '';
    // Keep the comment row open after submit so user can see their comment
};
const scrollToLine = (lineNumber, endLine = null) => {
    // 1. Logic to expand/select the line visually
    const start = Number(lineNumber);
    const end = endLine ? Number(endLine) : null;

    selectedLine.value = start;
    // expandedLine은 설정하지 않음 - 댓글창 자동 열기 방지
    
    const linesToHighlight = new Set();
    if (end && end >= start) {
        for (let i = start; i <= end; i++) {
            linesToHighlight.add(i);
        }
    } else {
        linesToHighlight.add(start);
    }
    selectedHighlightLines.value = linesToHighlight;

    // 2. DOM Scroll
    setTimeout(() => {
        const lineEl = document.querySelector(`tr[data-line-number="${start}"]`);
        if (lineEl) {
            lineEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }, 0);
};

defineExpose({ scrollToLine });
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f8fafc;
  border-left: 1px solid #e2e8f0;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 5px;
  border: 2px solid #f8fafc;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fade-in 0.2s ease-out forwards;
}
</style>
