<template>
  <div class="min-h-screen bg-white text-slate-800 selection:bg-brand-500/30 font-[Pretendard]">

    <main class="container mx-auto px-6 py-10 max-w-4xl">
      <div class="flex items-center justify-between mb-6 animate-fade-in-up">
        <button 
          @click="$router.back()" 
          class="flex items-center gap-2 text-slate-500 hover:text-brand-600 transition-colors font-medium"
        >
          <ArrowLeft :size="20" />
          돌아가기
        </button>
        <h1 class="text-xl font-bold text-slate-800">{{ isEdit ? '글 수정' : '새 글 작성' }}</h1>
      </div>

      <!-- 메인 레이아웃 -->
      <div :class="isCodeReviewMode ? 'grid grid-cols-1 lg:grid-cols-12 gap-8 items-start' : 'max-w-3xl mx-auto'">
        
        <!-- 왼쪽 컬럼: 코드 뷰어 (코드 리뷰 모드에서만) -->
        <div v-if="isCodeReviewMode" class="lg:col-span-7 order-2 lg:order-1 animate-fade-in-up">
           <div class="sticky top-24 space-y-4">
              <!-- 도움말 텍스트 -->
              <div class="bg-white/50 backdrop-blur border border-slate-200 p-4 rounded-xl flex items-start gap-3 shadow-sm">
                 <div class="p-2 bg-brand-50 text-brand-600 rounded-lg">
                    <Code2 :size="20" />
                 </div>
                 <div>
                    <h3 class="font-bold text-slate-800 text-sm">코드 리뷰 모드</h3>
                    <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                       좌측의 코드를 보며 리뷰를 작성하세요. <br/>
                       코드 라인을 클릭하면 <span class="text-brand-600 font-bold">인라인 댓글</span>을 남길 수 있습니다.
                    </p>
                 </div>
              </div>

              <!-- 뷰어 -->
              <div v-if="selectedRecord" class="shadow-2xl shadow-slate-200/50 rounded-xl overflow-hidden border border-slate-200/60">
                <CodeViewer
                  :code="selectedRecord.code"
                  :language="selectedRecord.language || 'java'"
                  :filename="`${selectedRecord.title}.${getExtension(selectedRecord.language)}`"
                  :comments="draftComments"
                  :read-only="true"
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

        <!-- 오른쪽 컬럼: 폼 입력 (일반 모드일 때는 중앙) -->
        <div :class="[
          isCodeReviewMode ? 'lg:col-span-5 order-1 lg:order-2' : 'w-full',
          'space-y-6 animate-fade-in-up delay-100'
        ]">
          
          <!-- 게시판 유형 & 기록 선택 (항상 표시하지만 스타일 다름) -->
          <div v-if="!isEdit || isCodeReviewMode" class="bg-white/80 backdrop-blur-md border border-white/60 shadow-xl shadow-brand-500/5 rounded-2xl p-6">
             <div class="mb-6" v-if="!isEdit">
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

             <!-- 알고리즘 기록 선택 -->
             <div v-if="form.boardType === 'CODE_REVIEW'">
                <label class="block text-sm font-bold text-slate-700 mb-2">풀이 선택</label>
                
                <!-- 수정 모드일 때는 읽기 전용으로 표시 -->
                <div v-if="isEdit && selectedRecord" class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-slate-600 font-medium">
                    [#{{ selectedRecord.problemNumber }}] {{ selectedRecord.title }} ({{ selectedRecord.language }})
                    <span class="text-xs text-slate-400 ml-2">* 수정 시 변경 불가</span>
                </div>

                <div v-else>
                    <div v-if="loadingRecords" class="py-3 text-sm text-slate-500 flex items-center gap-2">
                        <div class="w-4 h-4 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
                        불러오는 중...
                    </div>
                    <div v-else-if="studyRecords.length === 0" class="text-sm text-slate-400 py-2">
                        내가 작성한 풀이가 없습니다.
                    </div>
                    
                    <div v-else class="space-y-2">
                        <!-- 검색 입력 -->
                        <div class="relative">
                            <input 
                                v-model="searchQuery" 
                                type="text"
                                placeholder="문제 번호 또는 제목으로 검색..."
                                class="w-full pl-9 pr-4 py-2 bg-white border border-slate-200 rounded-lg text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
                            />
                            <Search :size="16" class="absolute left-3 top-2.5 text-slate-400" />
                        </div>

                        <!-- 검색 결과 리스트 (최대 높이 제한) -->
                        <div class="max-h-60 overflow-y-auto border border-slate-200 rounded-xl bg-white shadow-sm custom-scrollbar">
                           <template v-if="filteredRecords.length > 0">
                                <div 
                                    v-for="record in filteredRecords" 
                                    :key="record.id" 
                                    @click="selectRecord(record)"
                                    class="px-4 py-3 flex items-center justify-between cursor-pointer hover:bg-slate-50 transition-colors border-b border-slate-100 last:border-0"
                                    :class="{'bg-emerald-50 hover:bg-emerald-100': form.algorithmRecordId === record.id}"
                                >
                                    <div class="flex items-center gap-3">
                                        <span class="px-2 py-0.5 bg-slate-100 text-slate-500 text-xs font-bold rounded">#{{ record.problemNumber }}</span>
                                        <div class="flex flex-col">
                                            <span class="text-sm font-bold text-slate-700" :class="{'text-emerald-700': form.algorithmRecordId === record.id}">{{ record.title }}</span>
                                            <span class="text-xs text-slate-400">{{ record.language }} · {{ formatDate(record.createdAt || record.committedAt) }}</span>
                                        </div>
                                    </div>
                                    <div v-if="form.algorithmRecordId === record.id" class="text-emerald-600">
                                        <CheckCircle2 :size="18" class="fill-current" />
                                    </div>
                                </div>
                           </template>
                           <div v-else class="p-4 text-center text-sm text-slate-400">
                               검색 결과가 없습니다.
                           </div>
                        </div>
                    </div>
                </div>
             </div>
          </div>

          <!-- 실제 작성 폼 -->
          <form @submit.prevent="handleSubmit" class="bg-white/80 backdrop-blur-md border border-white/60 shadow-xl shadow-brand-500/5 rounded-2xl p-6 space-y-6">
            <!-- 제목 -->
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-2">제목</label>
              <input
                v-model="form.title"
                type="text"
                placeholder="제목을 입력하세요"
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition-all"
              />
            </div>

            <!-- 내용 -->
            <div>
              <label class="block text-sm font-bold text-slate-700 mb-2">내용</label>
              <textarea
                v-model="form.content"
                placeholder="코드에 대한 전반적인 리뷰나 질문을 작성해주세요."
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition-all resize-none leading-relaxed"
                :rows="isCodeReviewMode ? 15 : 12"
              ></textarea>
            </div>

            <!-- 제출 버튼 -->
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
                class="px-8 py-2.5 bg-brand-600 hover:bg-brand-500 disabled:bg-slate-300 disabled:text-slate-500 text-white rounded-xl font-bold transition-all shadow-lg shadow-brand-500/30 transform active:scale-95 flex items-center gap-2"
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
import { ArrowLeft, Code2, Search, CheckCircle2 } from 'lucide-vue-next';
import { boardApi } from '@/api/board';
import { algorithmApi } from '@/api/algorithm';
import { useAuth } from '@/composables/useAuth';
import CodeViewer from '@/components/editor/CodeViewer.vue';

const router = useRouter();
const route = useRoute();
const { user } = useAuth();

const isEdit = computed(() => !!route.params.id);
const submitting = ref(false);
const loadingRecords = ref(false);
const studyRecords = ref([]);
const searchQuery = ref('');
const codeViewerRef = ref(null);
const draftComments = ref([]); // { lineNumber, content, authorName, createdAt, id } 저장

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

// 선택된 기록 객체 (ID로 계산됨)
const selectedRecord = computed(() => {
    return studyRecords.value.find(r => r.id === form.value.algorithmRecordId) || null;
});

const filteredRecords = computed(() => {
    if (!searchQuery.value) return studyRecords.value;
    const q = searchQuery.value.toLowerCase();
    return studyRecords.value.filter(r => 
        r.problemNumber.toLowerCase().includes(q) || 
        r.title.toLowerCase().includes(q)
    );
});

// 헬퍼
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

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString();
};

const selectRecord = (record) => {
    form.value.algorithmRecordId = record.id;
    searchQuery.value = ''; // 선택 후 검색어 초기화 (선택 사항)
};

// 게시판 유형 변경 감지하여 스터디 기록 로드
watch(() => form.value.boardType, async (newType) => {
    if (newType === 'CODE_REVIEW') {
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
    // 로컬 초안 상태에 추가
    const newComment = {
        id: Date.now(), // 임시 ID
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
                // 코드 리뷰인 경우 기록 로드
                if (form.value.boardType === 'CODE_REVIEW') {
                    // 수정 모드에서는 해당 기록을 단건 조회하여 리스트에 추가 (내 기록이 아닐 수 있으므로)
                    if (form.value.algorithmRecordId) {
                         try {
                             const recordRes = await algorithmApi.findById(form.value.algorithmRecordId);
                             studyRecords.value = [recordRes.data];
                         } catch (e) {
                             console.error("Failed to load attached record", e);
                         }
                    } else {
                        await loadStudyRecords();
                    }
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
        // 일반 유형인 경우 algorithmRecordId 제거
        if (payload.boardType === 'GENERAL') {
            payload.algorithmRecordId = null;
        } else {
            // 코드 리뷰를 위한 초기 댓글 포함
            payload.initialComments = draftComments.value.map(c => ({
                lineNumber: c.lineNumber,
                content: c.content
            }));
        }

        if (isEdit.value) {
            await boardApi.update(route.params.id, payload);
            alert("게시글이 수정되었습니다.");
            router.push(`/boards/${route.params.id}`);
        } else {
            const res = await boardApi.create(payload);
            alert("게시글이 등록되었습니다.");
            if (res.data && res.data.id) {
                router.push(`/boards/${res.data.id}`);
            } else {
                router.push('/boards');
            }
        }
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

.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: #f1f5f9;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: #cbd5e1;
    border-radius: 3px;
}
</style>
