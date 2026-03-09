<template>
  <div
    class="group relative rounded-3xl bg-white shadow-sm transition-all duration-300 hover:-translate-y-0.5 hover:shadow-md"
    :class="{ 'shadow-md': isExpanded, 'ring-2 ring-brand-500/20': record.tag === 'MISSION' }"
    @click.stop
  >
    <div
      :class="statusHeaderClass"
      class="flex cursor-pointer items-center gap-3 rounded-t-3xl px-5 py-3 text-sm font-bold"
      @click="toggleExpand"
    >
      <div class="flex items-center gap-1.5">
        <CheckCircle2 v-if="isPassed" :size="18" class="fill-current" />
        <X v-else :size="18" />
        <span>{{ isPassed ? 'SUCCESS' : 'FAILED' }}</span>
      </div>

      <div class="mx-1 h-3 w-px bg-current opacity-20"></div>

      <TaskBadge :type="record.tag || 'GENERAL'" />

      <div class="ml-2 flex items-center gap-2">
        <div
          v-if="record.tag === 'DEFENSE' && defenseStreak > 0"
          class="flex items-center gap-1 rounded border border-orange-100 bg-orange-50 px-1.5 py-0.5 text-[10px] font-bold text-orange-600"
        >
          <Flame :size="10" class="fill-orange-500" /> {{ defenseStreak }}연속
        </div>
        <div
          v-if="record.elapsedTimeSeconds && (record.tag === 'DEFENSE' || record.tag === 'MOCK_EXAM')"
          class="flex items-center gap-1 rounded bg-slate-100 px-1.5 py-0.5 text-[10px] text-slate-500"
        >
          <Clock :size="10" /> {{ formatElapsedTime(record.elapsedTimeSeconds) }}
        </div>
      </div>

      <div class="ml-auto flex items-center gap-3">
        <div class="flex items-center gap-1.5 rounded-full border border-slate-100 bg-slate-50 px-2 py-1">
          <NicknameRenderer
            :username="record.username"
            :show-avatar="false"
            text-class="text-xs font-medium text-slate-600"
          />
        </div>
        <div class="flex items-center gap-2 text-xs font-medium opacity-60">
          <span>{{ formatDate(record.createdAt) }}</span>
        </div>
      </div>
    </div>

    <div class="flex cursor-pointer flex-col gap-6 p-6 xl:flex-row" @click="toggleExpand">
      <div class="min-w-0 flex-1">
        <div class="mb-2 flex items-start justify-between gap-4">
          <div class="flex flex-col">
            <div class="mb-2 flex flex-wrap items-center gap-2">
              <span class="rounded-lg border border-slate-200 bg-slate-100 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider text-slate-500">
                #{{ record.problemNumber }}
              </span>
              <span
                class="rounded-lg border px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider"
                :class="isPassed ? 'border-slate-200 bg-white text-slate-600' : 'border-rose-100 bg-rose-50 text-rose-600'"
              >
                {{ record.language }}
              </span>
              <span
                v-if="platformBadge"
                class="rounded-lg border px-2.5 py-1 text-[10px] font-bold"
                :class="platformBadgeClass"
              >
                {{ platformBadge }}
              </span>
              <span
                class="rounded-lg border px-2.5 py-1 text-[10px] font-bold"
                :class="hasAnyAnalysis ? 'border-emerald-100 bg-emerald-50 text-emerald-600' : 'border-amber-100 bg-amber-50 text-amber-600'"
              >
                {{ hasAnyAnalysis ? '분석 완료' : '클릭 시 AI 분석 생성' }}
              </span>
            </div>

            <h3 class="flex items-center gap-2 text-lg font-bold leading-tight text-slate-800 transition-colors group-hover:text-brand-600 md:text-xl">
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

    <div v-if="isExpanded" class="relative overflow-hidden rounded-b-3xl border-t border-slate-100 bg-slate-50 animate-slide-down">
      <div class="flex flex-col">
        <div class="overflow-hidden rounded-xl">
          <CodeViewer
            ref="codeViewerRef"
            :code="record.code"
            :language="record.language || 'java'"
            :filename="`${record.title}.${getExtension(record.language)}`"
            :comments="comments"
            :key-blocks="combinedHighlights"
            :read-only="false"
            @submit-comment="submitLineComment"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue';
import { CheckCircle2, ChevronDown, ChevronUp, Clock, Flame, Key, X } from 'lucide-vue-next';
import CodeViewer from '@/components/editor/CodeViewer.vue';
import TaskBadge from '@/components/common/TaskBadge.vue';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';
import { boardApi, commentApi } from '@/api/board';

const props = defineProps({
  record: { type: Object, required: true },
  isExpanded: { type: Boolean, default: false }
});

const emit = defineEmits(['toggle-expand']);

const board = ref(null);
const comments = ref([]);
const loadingBoard = ref(false);
const codeViewerRef = ref(null);

const record = computed(() => props.record);

watch(
  () => props.isExpanded,
  async (expanded) => {
    if (expanded && !board.value) {
      await loadBoardAndComments();
    }
  }
);

const toggleExpand = () => {
  emit('toggle-expand', props.record.id);
};

const loadBoardAndComments = async () => {
  loadingBoard.value = true;
  try {
    const boardResponse = await boardApi.findByRecordId(props.record.id);
    if (boardResponse.status === 204 || !boardResponse.data) {
      board.value = null;
      comments.value = [];
      return;
    }

    board.value = boardResponse.data;
    const commentsResponse = await commentApi.findByBoardId(board.value.id);
    comments.value = commentsResponse.data || [];
  } catch (error) {
    console.error('Failed to load review info', error);
  } finally {
    loadingBoard.value = false;
  }
};

const ensureBoardExists = async () => {
  if (board.value) return board.value;

  const response = await boardApi.create({
    title: props.record.title,
    content: `Code review for ${props.record.title} (${props.record.problemNumber})`,
    boardType: 'CODE_REVIEW',
    visibility: 'PRIVATE',
    algorithmRecordId: props.record.id
  });

  board.value = response.data;
  return board.value;
};

const submitLineComment = async ({ lineNumber, content }) => {
  try {
    const targetBoard = await ensureBoardExists();
    const response = await commentApi.create(targetBoard.id, { content, lineNumber });
    comments.value.push(response.data);
  } catch (error) {
    console.error('Failed to submit comment', error);
  }
};

const scrollToLine = (lineNumber, endLine = null) => {
  if (codeViewerRef.value && lineNumber) {
    codeViewerRef.value.scrollToLine(Number(lineNumber), endLine ? Number(endLine) : null);
  }
};

const formatDate = (value) => {
  if (!value) return '';
  return new Date(value).toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const formatElapsedTime = (seconds) => {
  if (!seconds || seconds < 0) return '';
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;

  if (minutes === 0) return `${remainingSeconds}초`;
  if (remainingSeconds === 0) return `${minutes}분`;
  return `${minutes}분 ${remainingSeconds}초`;
};

const getExtension = (language) => ({
  java: 'java',
  python: 'py',
  cpp: 'cpp',
  c: 'c',
  javascript: 'js'
}[language?.toLowerCase()] || 'txt');

const parsedFullResponse = computed(() => {
  if (!props.record.fullResponse) return null;
  try {
    return JSON.parse(props.record.fullResponse);
  } catch {
    return null;
  }
});

const parsedStructure = computed(() => {
  const structure = parsedFullResponse.value?.structure;
  return Array.isArray(structure) ? structure : [];
});

const parsedKeyBlocks = computed(() => {
  if (!props.record.keyBlocks) return [];
  try {
    const parsed = JSON.parse(props.record.keyBlocks);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
});

const parsedSummary = computed(() => parsedFullResponse.value?.summary || null);
const parsedIntuition = computed(() => parsedFullResponse.value?.algorithm?.intuition || props.record.algorithmIntuition || null);

const parsedPitfalls = computed(() => {
  if (!props.record.pitfalls) return [];
  try {
    const parsed = JSON.parse(props.record.pitfalls);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
});

const combinedHighlights = computed(() => {
  const structureHighlights = parsedStructure.value.map((item) => ({
    ...item,
    code: item.name,
    explanation: `[구조] ${item.role}`
  }));

  const blockHighlights = parsedKeyBlocks.value.map((block) => ({
    ...block,
    code: block.code,
    explanation: `[로직] ${block.explanation}`
  }));

  return [...structureHighlights, ...blockHighlights];
});

const hasAnyAnalysis = computed(() => Boolean(
  props.record.timeComplexity ||
  props.record.spaceComplexity ||
  props.record.fullResponse ||
  parsedPitfalls.value.length > 0 ||
  props.record.refactorProvided
));

const isPassed = computed(() => {
  const runtime = props.record.runtimeMs;
  const memory = props.record.memoryKb;
  return props.record.result === 'SUCCESS' || props.record.result === 'PASSED' || (runtime !== null && runtime !== undefined && runtime !== -1 && memory > 0);
});

const defenseStreak = computed(() => props.record.defenseStreak || 0);

const platformBadge = computed(() => {
  const platform = props.record.platform?.toLowerCase();
  if (platform === 'baekjoon' || platform === 'boj') return 'BOJ';
  if (platform === 'swea') return 'SWEA';
  if (platform === 'programmers' || platform === 'pgs') return 'PGS';
  return null;
});

const platformBadgeClass = computed(() => {
  if (platformBadge.value === 'BOJ') return 'bg-blue-50 text-blue-600 border-blue-100';
  if (platformBadge.value === 'SWEA') return 'bg-cyan-50 text-cyan-600 border-cyan-100';
  if (platformBadge.value === 'PGS') return 'bg-slate-800 text-white border-slate-700';
  return 'bg-slate-50 text-slate-500 border-slate-200';
});

const statusHeaderClass = computed(() => (
  isPassed.value
    ? 'border-b border-emerald-100 bg-emerald-50 text-emerald-700'
    : 'border-b border-rose-100 bg-rose-50 text-rose-700'
));

defineExpose({ scrollToLine });
</script>

<style scoped>
.animate-slide-down { animation: slide-down 0.3s ease-out forwards; }
@keyframes slide-down { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>
