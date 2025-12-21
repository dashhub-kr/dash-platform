<template>
  <div class="min-h-screen bg-[#f0f9ff] text-slate-800 selection:bg-indigo-500/30 font-[Pretendard]">

    <main class="container mx-auto px-6 py-10 max-w-4xl">
      <div class="mb-8 animate-fade-in-up">
        <button 
          @click="$router.back()" 
          class="flex items-center gap-2 text-slate-500 hover:text-indigo-600 mb-4 transition-colors"
        >
          <ArrowLeft :size="20" />
          돌아가기
        </button>
        <h1 class="text-3xl font-extrabold text-slate-900">{{ isEdit ? '글 수정하기' : '새 글 작성하기' }}</h1>
      </div>

      <!-- Main Layout -->
      <div :class="isCodeReviewMode ? 'grid grid-cols-1 lg:grid-cols-12 gap-8 items-start' : 'max-w-3xl mx-auto'">
        
        <!-- LEFT COLUMN: Code Viewer (Only in Code Review Mode) -->
        <div v-if="isCodeReviewMode" class="lg:col-span-7 order-2 lg:order-1 animate-fade-in-up">
           <div class="sticky top-24 space-y-4">
              <!-- Helper Text -->
              <div class="bg-white/50 backdrop-blur border border-slate-200 p-4 rounded-xl flex items-start gap-3 shadow-sm">
                 <div class="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                    <Code2 :size="20" />
                 </div>
                 <div>
                    <h3 class="font-bold text-slate-800 text-sm">코드 리뷰 모드</h3>
                    <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                       좌측의 코드를 보며 리뷰를 작성하세요. <br/>
                       코드 라인을 클릭하면 <span class="text-indigo-600 font-bold">인라인 댓글</span>을 남길 수 있습니다.
                    </p>
                 </div>
              </div>

              <!-- The Viewer -->
              <div v-if="selectedRecord" class="shadow-2xl shadow-slate-200/50 rounded-xl overflow-hidden border border-slate-200/60">
                <CodeViewer
                  :code="selectedRecord.code"
                  :language="selectedRecord.language || 'java'"
                  :filename="`${selectedRecord.title}.${getExtension(selectedRecord.language)}`"
                  :comments="draftComments"
                  @submit-comment="submitLineComment"
                  class="bg-[#1e293b]" 
                  style="max-height: 75vh; height: 75vh;" 
                />
              </div>
              <div v-else class="h-[500px] bg-slate-100 rounded-xl border-2 border-dashed border-slate-300 flex items-center justify-center text-slate-400">
                  <p>상단에서 풀이를 선택해주세요</p>
              </div>
           </div>
        </div>

        <!-- RIGHT COLUMN: Form inputs (Or Center if General) -->
        <div :class="[
          isCodeReviewMode ? 'lg:col-span-5 order-1 lg:order-2' : 'w-full',
          'space-y-6 animate-fade-in-up delay-100'
        ]">
          
          <!-- Board Type & Record Select (Always visible, but styled differently) -->
          <div class="bg-white/80 backdrop-blur-md border border-white/60 shadow-xl shadow-indigo-500/5 rounded-2xl p-6">
             <div class="mb-6">
                <label class="block text-sm font-bold text-slate-700 mb-2">게시글 유형</label>
                <div class="flex gap-4 p-1 bg-slate-100/50 rounded-xl border border-slate-200/50">
                  <label class="flex-1 relative cursor-pointer group">
                    <input type="radio" v-model="form.boardType" value="GENERAL" class="peer sr-only" />
                    <div class="flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-bold text-slate-500 transition-all peer-checked:bg-white peer-checked:text-slate-800 peer-checked:shadow-sm group-hover:text-slate-700">
                      일반
                    </div>
                  </label>
                  <label class="flex-1 relative cursor-pointer group">
                    <input type="radio" v-model="form.boardType" value="CODE_REVIEW" class="peer sr-only" />
                    <div class="flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-bold text-slate-500 transition-all peer-checked:bg-white peer-checked:text-emerald-600 peer-checked:shadow-sm group-hover:text-emerald-500">
                      <Code2 :size="16" />
                      코드리뷰
                    </div>
                  </label>
                </div>
             </div>

             <!-- Algorithm Record Select -->
             <div v-if="form.boardType === 'CODE_REVIEW'">
                <label class="block text-sm font-bold text-slate-700 mb-2">풀이 선택</label>
                <div v-if="loadingRecords" class="py-3 text-sm text-slate-500 flex items-center gap-2">
                   <div class="w-4 h-4 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
                   불러오는 중...
                </div>
                <div v-else-if="studyRecords.length === 0" class="text-sm text-slate-400 py-2">
                   내가 작성한 풀이가 없습니다.
                </div>
                <select
                  v-else
                  v-model="form.algorithmRecordId"
                  class="w-full bg-white border border-slate-200 rounded-xl px-4 py-3 text-slate-800 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all shadow-sm cursor-pointer hover:bg-slate-50"
                  :class="{'ring-2 ring-emerald-500 border-transparent': form.algorithmRecordId}"
                >
                  <option :value="null">리뷰할 풀이를 선택하세요</option>
                  <option v-for="record in studyRecords" :key="record.id" :value="record.id">
                    [#{{ record.problemNumber }}] {{ record.title }} ({{ record.language }})
                  </option>
                </select>
             </div>
          </div>

          <!-- The Actual Writing Form -->
          <form @submit.prevent="handleSubmit" class="bg-white/80 backdrop-blur-md border border-white/60 shadow-xl shadow-indigo-500/5 rounded-2xl p-6 space-y-6">
            <!-- Title -->
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-2">제목</label>
              <input
                v-model="form.title"
                type="text"
                placeholder="제목을 입력하세요"
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              />
            </div>

            <!-- Content -->
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-2">내용</label>
              <textarea
                v-model="form.content"
                placeholder="코드에 대한 전반적인 리뷰나 질문을 작성해주세요."
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all resize-none leading-relaxed"
                :rows="isCodeReviewMode ? 15 : 12"
              ></textarea>
            </div>

            <!-- Submit Buttons -->
            <div class="flex justify-end gap-3 pt-2">
              <button 
                @click="$router.push('/boards')"
                class="px-5 py-2.5 rounded-xl font-bold text-slate-500 hover:bg-slate-100 transition-colors"
              >
                취소
              </button>
              <button 
                type="submit"
                :disabled="submitting || !isValid"
                class="px-8 py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-300 disabled:text-slate-500 text-white rounded-xl font-bold transition-all shadow-lg shadow-indigo-500/30 transform active:scale-95 flex items-center gap-2"
              >
                <div v-if="submitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                <span>{{ isEdit ? '수정 완료' : '작성 완료' }}</span>
              </button>
            </div>
          </form>
        </div>
      
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ArrowLeft, Code2 } from 'lucide-vue-next';
import { boardApi } from '../api/board';
import { algorithmApi } from '../api/algorithm';
import { useAuth } from '../composables/useAuth';
import CodeViewer from '../components/CodeViewer.vue';

const router = useRouter();
const route = useRoute();
const { user } = useAuth();

const isEdit = computed(() => !!route.params.id);
const submitting = ref(false);
const loadingRecords = ref(false);
const studyRecords = ref([]);
const codeViewerRef = ref(null);
const draftComments = ref([]); // Stores { lineNumber, content, authorName, createdAt, id }

const form = ref({
    title: '',
    content: '',
    boardType: 'GENERAL',
    algorithmRecordId: null
});

const isCodeReviewMode = computed(() => form.value.boardType === 'CODE_REVIEW');

const isValid = computed(() => {
    if (!form.value.title.trim() || !form.value.content.trim()) return false;
    if (isCodeReviewMode.value && !form.value.algorithmRecordId) return false;
    return true;
});

// Selected record object (computed from ID)
const selectedRecord = computed(() => {
    return studyRecords.value.find(r => r.id === form.value.algorithmRecordId) || null;
});

// Helpers
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

// Watch for boardType changes to load study records
watch(() => form.value.boardType, async (newType) => {
    if (newType === 'CODE_REVIEW' && user.value?.studyId) {
        await loadStudyRecords();
    }
});

const loadStudyRecords = async () => {
    if (!user.value?.id) return;
    loadingRecords.value = true;
    try {
        const res = await algorithmApi.findByUserId(user.value.id);
        studyRecords.value = res.data || [];
    } catch (e) {
        console.error("Failed to load user's algorithm records", e);
        studyRecords.value = [];
    } finally {
        loadingRecords.value = false;
    }
};

const submitLineComment = ({ lineNumber, content }) => {
    // Add to local draft state
    const newComment = {
        id: Date.now(), // Temporary ID
        lineNumber,
        content,
        authorName: user.value?.username || 'Me',
        createdAt: new Date()
    };
    draftComments.value.push(newComment);
};

onMounted(async () => {
    if (isEdit.value) {
        try {
            const res = await boardApi.findById(route.params.id);
            if (res.data) {
                form.value = {
                    title: res.data.title,
                    content: res.data.content,
                    boardType: res.data.boardType || 'GENERAL',
                    algorithmRecordId: res.data.algorithmRecordId || null
                };
                // Load records if CODE_REVIEW
                if (form.value.boardType === 'CODE_REVIEW') {
                    await loadStudyRecords();
                }
            }
        } catch (e) {
            console.error("Failed to load post", e);
            alert("게시글을 불러오는 데 실패했습니다.");
            router.push('/boards');
        }
    }
});


const handleSubmit = async () => {
    submitting.value = true;
    try {
        const payload = {
            ...form.value,
            userId: user.value?.id
        };
        // Remove algorithmRecordId if GENERAL type
        if (payload.boardType === 'GENERAL') {
            payload.algorithmRecordId = null;
        } else {
            // Include initial comments for CODE_REVIEW
            payload.initialComments = draftComments.value.map(c => ({
                lineNumber: c.lineNumber,
                content: c.content
            }));
        }

        if (isEdit.value) {
            await boardApi.update(route.params.id, payload);
            alert("게시글이 수정되었습니다.");
        } else {
            await boardApi.create(payload);
            alert("게시글이 등록되었습니다.");
        }
        router.push('/boards');
    } catch (e) {
        console.error("Failed to submit", e);
        alert("처리에 실패했습니다.");
    } finally {
        submitting.value = false;
    }
};
</script>

<style scoped>
.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}
.delay-100 { animation-delay: 0.1s; }
@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
}
</style>
