<template>
  <div class="min-h-screen bg-white text-slate-800 selection:bg-brand-500/30 font-[Pretendard]">

    <main class="container mx-auto px-6 py-10 max-w-4xl">
      <!-- 뒤로가기 버튼 -->
      <button 
        @click="$router.push('/boards')" 
        class="flex items-center gap-2 text-slate-500 hover:text-brand-600 mb-6 transition-colors animate-fade-in-up"
      >
        <ArrowLeft :size="20" />
        목록으로
      </button>

      <!-- 게시글 내용 -->
      <article v-if="post" class="bg-white/80 border border-white/60 shadow-xl shadow-brand-500/5 backdrop-blur-md rounded-3xl p-8 mb-6 animate-fade-in-up delay-100">
        <!-- 헤더 -->
        <div class="border-b border-slate-100 pb-6 mb-6">
          <div class="flex items-center gap-3 mb-4">
            <span v-if="post.boardType === 'CODE_REVIEW'" class="px-3 py-1 text-xs font-bold rounded-full bg-emerald-100 text-emerald-700">코드리뷰</span>
            <span v-else class="px-3 py-1 text-xs font-bold rounded-full bg-slate-100 text-slate-600">일반</span>
          </div>
          <h1 class="text-3xl font-extrabold text-slate-900 mb-4 leading-tight">{{ post.title }}</h1>
          <div class="flex flex-wrap items-center gap-4 text-sm text-slate-500">
            <div class="flex items-center gap-2">
              <img v-if="post.authorProfileImageUrl" 
                   :src="post.authorProfileImageUrl" 
                   class="w-8 h-8 rounded-full object-cover border border-brand-100" />
              <div v-else class="w-8 h-8 rounded-full bg-brand-50 flex items-center justify-center text-brand-600 font-bold border border-brand-100">
                 {{ post.authorName?.charAt(0).toUpperCase() || 'U' }}
              </div>
              <span class="text-slate-700 font-medium">
                {{ post.authorName && !['Unknown', 'Unknown User'].includes(post.authorName) ? post.authorName : '탈퇴한 회원' }}
              </span>
              
              <!-- 차단 버튼 (관리자 전용) -->
              <button 
                v-if="isAdmin && post.authorName !== '탈퇴한 회원'"
                @click="blockUser"
                class="ml-2 px-2 py-0.5 text-[10px] font-bold text-rose-500 bg-rose-50 border border-rose-100 rounded hover:bg-rose-100 transition-colors flex items-center gap-1"
                title="회원 차단"
              >
                <ShieldBan :size="12" />
                BLOCK
              </button>
            </div>
            <span class="w-1 h-1 rounded-full bg-slate-300"></span>
            <span>{{ formatDate(post.createdAt) }}</span>
          </div>
        </div>

        <!-- 본문 -->
        <div class="prose max-w-none text-slate-700 leading-relaxed whitespace-pre-wrap mb-6">
          {{ post.content }}
        </div>

        <!-- 코드 뷰어 (코드 리뷰 게시글용) -->
        <div v-if="post.boardType === 'CODE_REVIEW' && algorithmRecord" class="mb-6">
          <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
            <Code2 :size="20" class="text-emerald-500" />
            코드
          </h3>
          <!-- CodeViewer는 보통 어둡게 유지됨. 기본 컴포넌트가 잘 작동한다고 가정. -->
          <CodeViewer
            :code="algorithmRecord.code"
            :language="algorithmRecord.language || 'java'"
            :filename="`${algorithmRecord.title}.${getExtension(algorithmRecord.language)}`"
            :comments="comments"
            @submit-comment="submitLineComment"
          />
        </div>


        <!-- 코드 로딩 상태 -->
        <div v-else-if="post.boardType === 'CODE_REVIEW' && loadingCode" class="mb-6 p-6 bg-slate-100 rounded-xl text-center text-slate-500">
          <div class="animate-pulse">코드를 불러오는 중...</div>
        </div>

        <!-- 추천 버튼 -->
        <div class="mt-8 pt-6 border-t border-slate-100 flex items-center gap-4">
          <button 
            @click="toggleLike"
            :class="[
              'flex items-center gap-2 px-6 py-3 rounded-xl font-bold transition-all shadow-sm',
              isLiked 
                ? 'bg-rose-50 text-rose-500 border border-rose-100' 
                : 'bg-white border border-slate-200 text-slate-600 hover:text-brand-600 hover:border-brand-200'
            ]"
          >
            <ThumbsUp :size="20" :fill="isLiked ? 'currentColor' : 'none'" />
            추천 {{ post.likeCount || 0 }}
          </button>
          <span class="text-slate-500 text-sm flex items-center gap-2">
            <MessageCircle :size="18" />
            댓글 {{ comments.length }}개
          </span>
        </div>
      </article>

      <div v-else-if="loading" class="text-center py-20 text-slate-500">
        게시글을 불러오는 중...
      </div>

      <!-- 댓글 섹션 -->
      <section v-if="post" class="bg-white/80 border border-white/60 shadow-xl shadow-brand-500/5 backdrop-blur-md rounded-3xl p-8 animate-fade-in-up delay-200">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <MessageCircle :size="22" />
          일반 댓글
        </h2>

        <!-- 댓글 폼 -->
        <div class="mb-8">
          <textarea
            v-model="newComment"
            placeholder="따뜻한 댓글을 남겨주세요..."
            class="w-full bg-white border border-slate-200 rounded-2xl p-4 text-slate-800 placeholder-slate-400 resize-none focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent shadow-inner transition-all"
            rows="3"
          ></textarea>
          <div class="flex justify-end mt-3">
            <button 
              @click="submitComment"
              :disabled="!newComment.trim()"
              class="px-6 py-2.5 bg-brand-600 hover:bg-brand-500 disabled:bg-slate-200 disabled:text-slate-400 text-white rounded-xl font-bold transition-all shadow-lg shadow-brand-500/20"
            >
              댓글 작성
            </button>
          </div>
        </div>

        <!-- 댓글 목록 (일반 댓글만 - 줄 번호 없음) -->
        <div v-if="generalComments.length === 0" class="text-center py-10 text-slate-400">
          아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
        </div>
        <div v-else class="space-y-4">
          <div 
            v-for="comment in generalComments" 
            :key="comment.id"
            class="bg-slate-50 border border-slate-100 rounded-2xl p-5"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <img v-if="comment.authorProfileImageUrl" 
                     :src="comment.authorProfileImageUrl" 
                     class="w-8 h-8 rounded-full object-cover shadow-sm border border-slate-100" />
                <div v-else class="w-8 h-8 rounded-full bg-white shadow-sm border border-slate-100 flex items-center justify-center text-slate-500 font-bold text-sm">
                  {{ comment.authorName?.charAt(0).toUpperCase() || 'U' }}
                </div>
                <span class="font-medium text-slate-700">
                    {{ comment.authorName && !['Unknown', 'Unknown User'].includes(comment.authorName) ? comment.authorName : '탈퇴한 회원' }}
                </span>
                <span class="text-xs text-slate-400">{{ formatDate(comment.createdAt) }}</span>
              </div>
              <button 
                @click="toggleCommentLike(comment)"
                :class="['flex items-center gap-1 text-sm transition-colors', comment.isLiked ? 'text-rose-500 font-bold' : 'text-slate-400 hover:text-rose-500']"
              >
                <ThumbsUp :size="14" :fill="comment.isLiked ? 'currentColor' : 'none'" />
                {{ comment.likeCount || 0 }}
              </button>
            </div>
            
            <!-- 댓글 내용 (수정 모드 아닐 때) -->
            <p v-if="!comment.isEditing" class="text-slate-700 whitespace-pre-wrap pl-11 mb-2">{{ comment.content }}</p>

            <!-- 댓글 수정 폼 (수정 모드) -->
            <div v-else class="pl-11 mb-4 mt-2">
                <textarea
                    v-model="comment.editContent"
                    class="w-full bg-white border border-brand-200 rounded-xl p-3 text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500 resize-none text-sm"
                    rows="3"
                ></textarea>
                <div class="flex justify-end gap-2 mt-2">
                    <button 
                        @click="cancelEditComment(comment)"
                        class="flex items-center gap-1 px-3 py-1.5 text-xs font-bold text-slate-500 hover:bg-slate-100 rounded-lg transition-colors"
                    >
                        <X :size="14" /> 취소
                    </button>
                    <button 
                        @click="saveEditComment(comment)"
                        class="flex items-center gap-1 px-3 py-1.5 text-xs font-bold text-white bg-brand-500 hover:bg-brand-600 rounded-lg transition-colors shadow-sm"
                    >
                        <Check :size="14" /> 저장
                    </button>
                </div>
            </div>

            <!-- 댓글 액션 버튼 (작성자 본인일 때만 표시) -->
            <div v-if="user && user.id === comment.userId && !comment.isEditing" class="flex justify-end gap-3 pl-11 mt-1">
                <button 
                    @click="editComment(comment)"
                    class="flex items-center gap-1 text-xs text-slate-400 hover:text-brand-600 transition-colors font-medium"
                >
                    <Pencil :size="12" /> 수정
                </button>
                <button 
                    @click="deleteComment(comment)"
                    class="flex items-center gap-1 text-xs text-slate-400 hover:text-rose-500 transition-colors font-medium"
                >
                    <Trash2 :size="12" /> 삭제
                </button>
            </div>

            <!-- 답글 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="mt-4 pl-11 border-l-2 border-slate-200 space-y-3">
              <div 
                v-for="reply in comment.replies" 
                :key="reply.id"
                class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm"
              >
                <div class="flex items-center gap-2 mb-2">
                  <img v-if="reply.authorProfileImageUrl" 
                       :src="reply.authorProfileImageUrl" 
                       class="w-6 h-6 rounded-full object-cover border border-slate-100" />
                  <div v-else class="w-6 h-6 rounded-full bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-400 font-bold text-[10px]">
                    {{ reply.authorName?.charAt(0).toUpperCase() || 'U' }}
                  </div>
                  <span class="font-medium text-slate-700 text-sm">
                    {{ reply.authorName && !['Unknown', 'Unknown User'].includes(reply.authorName) ? reply.authorName : '탈퇴한 회원' }}
                  </span>
                  <span class="text-xs text-slate-400">{{ formatDate(reply.createdAt) }}</span>
                </div>
                
                <!-- 대댓글 내용 및 수정 폼 -->
                <div v-if="!reply.isEditing">
                    <p class="text-slate-600 text-sm">{{ reply.content }}</p>
                </div>
                <div v-else class="mt-2 mb-2">
                    <textarea
                        v-model="reply.editContent"
                        class="w-full bg-slate-50 border border-slate-200 rounded-lg p-2 text-slate-700 focus:outline-none focus:ring-1 focus:ring-brand-500 resize-none text-xs"
                        rows="2"
                    ></textarea>
                     <div class="flex justify-end gap-2 mt-2">
                        <button 
                            @click="cancelEditComment(reply)"
                            class="flex items-center gap-1 px-2 py-1 text-[10px] font-bold text-slate-500 hover:bg-slate-200 rounded transition-colors"
                        >
                            <X :size="12" /> 취소
                        </button>
                        <button 
                            @click="saveEditComment(reply)"
                            class="flex items-center gap-1 px-2 py-1 text-[10px] font-bold text-white bg-brand-500 hover:bg-brand-600 rounded transition-colors shadow-sm"
                        >
                            <Check :size="12" /> 저장
                        </button>
                    </div>
                </div>

                <!-- 대댓글 액션 -->
                <div v-if="user && user.id === reply.userId && !reply.isEditing" class="flex justify-end gap-2 mt-1">
                    <button 
                        @click="editComment(reply)"
                        class="flex items-center gap-1 text-[10px] text-slate-400 hover:text-brand-600 transition-colors"
                    >
                        <Pencil :size="10" /> 수정
                    </button>
                    <button 
                        @click="deleteComment(reply)"
                        class="flex items-center gap-1 text-[10px] text-slate-400 hover:text-rose-500 transition-colors"
                    >
                        <Trash2 :size="10" /> 삭제
                    </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 작업 -->
      <div v-if="post && isAuthor" class="flex justify-end gap-3 mt-6 animate-fade-in-up delay-300">
        <button 
          @click="deletePost"
          class="px-4 py-2 rounded-xl text-rose-500 hover:bg-rose-50 transition-colors text-sm font-bold"
        >
          삭제
        </button>
        <button 
          @click="$router.push(`/boards/edit/${post.id}`)"
          class="px-5 py-2 rounded-xl bg-white text-slate-600 border border-slate-200 hover:border-brand-500 hover:text-brand-600 transition-all text-sm font-bold shadow-sm"
        >
          수정
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { ArrowLeft, ThumbsUp, MessageCircle, Code2, Pencil, Trash2, X, Check, UserX, ShieldBan } from 'lucide-vue-next';
import { boardApi, commentApi } from '@/api/board';
import { algorithmApi } from '@/api/algorithm';
import { useAuth } from '@/composables/useAuth';
import CodeViewer from '@/components/editor/CodeViewer.vue';

const route = useRoute();
const router = useRouter();
const { user } = useAuth();

const post = ref(null);
const loading = ref(true);
const loadingCode = ref(false);
const comments = ref([]);
const newComment = ref('');
const isLiked = ref(false);
const algorithmRecord = ref(null);

const isAuthor = computed(() => {
    if (!post.value || !user.value) return false;
    return post.value.userId === user.value.id;
});

// 일반 댓글 (줄 번호 없음)
const generalComments = computed(() => {
    return comments.value.filter(c => !c.lineNumber);
});

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString();
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

onMounted(async () => {
    try {
        const res = await boardApi.findById(route.params.id);
        post.value = res.data;
        isLiked.value = res.data.isLiked || false;

        // 댓글 로드
        const commentsRes = await commentApi.findByBoardId(route.params.id);
        comments.value = commentsRes.data || [];

        // 코드 리뷰인 경우 알고리즘 기록 로드
        if (post.value.boardType === 'CODE_REVIEW' && post.value.algorithmRecordId) {
            loadingCode.value = true;
            try {
                const recordRes = await algorithmApi.findById(post.value.algorithmRecordId);
                algorithmRecord.value = recordRes.data;
            } catch (e) {
                console.error("Failed to fetch algorithm record", e);
            } finally {
                loadingCode.value = false;
            }
        }
    } catch (e) {
        console.error("Failed to fetch post", e);
        alert("게시글을 찾을 수 없습니다.");
        router.push('/boards');
    } finally {
        loading.value = false;
    }
});

const submitLineComment = async ({ lineNumber, content }) => {
    try {
        const res = await commentApi.create(post.value.id, { 
            content, 
            lineNumber 
        });
        comments.value.push(res.data);
    } catch (e) {
        console.error("Line comment submit failed", e);
        alert("댓글 작성에 실패했습니다.");
    }
};

const toggleLike = async () => {
    try {
        if (isLiked.value) {
            const res = await boardApi.unlike(post.value.id);
            post.value.likeCount = res.data?.likeCount ?? (post.value.likeCount - 1);
            isLiked.value = false;
        } else {
            const res = await boardApi.like(post.value.id);
            post.value.likeCount = res.data?.likeCount ?? (post.value.likeCount + 1);
            isLiked.value = true;
        }
    } catch (e) {
        console.error("Like toggle failed", e.response?.data || e);
    }
};

const toggleCommentLike = async (comment) => {
    try {
        if (comment.isLiked) {
            await commentApi.unlike(comment.id);
            comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1);
            comment.isLiked = false;
        } else {
            await commentApi.like(comment.id);
            comment.likeCount = (comment.likeCount || 0) + 1;
            comment.isLiked = true;
        }
    } catch (e) {
        console.error("Comment like failed", e);
    }
};

// 댓글 수정 모드 진입
const editComment = (comment) => {
    comment.isEditing = true;
    comment.editContent = comment.content;
};

// 댓글 수정 취소
const cancelEditComment = (comment) => {
    comment.isEditing = false;
    comment.editContent = '';
};

// 댓글 수정 저장
const saveEditComment = async (comment) => {
    if (!comment.editContent || !comment.editContent.trim()) {
        alert("내용을 입력해주세요.");
        return;
    }
    
    try {
        const res = await commentApi.update(post.value.id, comment.id, { 
            content: comment.editContent 
        });
        
        // 업데이트된 내용 반영
        comment.content = res.data.content;
        comment.updatedAt = res.data.updatedAt;
        comment.isEditing = false;
    } catch (e) {
        console.error("Comment update failed", e);
        alert("댓글 수정에 실패했습니다.");
    }
};

// 댓글 삭제
const deleteComment = async (comment) => {
    if (!confirm("댓글을 삭제하시겠습니까?")) return;
    
    try {
        await commentApi.delete(post.value.id, comment.id);
        comments.value = comments.value.filter(c => c.id !== comment.id);
    } catch (e) {
        console.error("Comment delete failed", e);
        alert("댓글 삭제에 실패했습니다.");
    }
};

const submitComment = async () => {
    if (!newComment.value.trim()) return;
    try {
        const res = await commentApi.create(post.value.id, { content: newComment.value });
        comments.value.push(res.data);
        newComment.value = '';
    } catch (e) {
        console.error("Comment submit failed", e);
        alert("댓글 작성에 실패했습니다.");
    }
};

const deletePost = async () => {
    if(!confirm("정말로 삭제하시겠습니까?")) return;
    try {
        await boardApi.delete(post.value.id);
        alert("삭제되었습니다.");
        router.push('/boards');
    } catch (e) {
        console.error("Delete failed", e);
        alert("삭제에 실패했습니다.");
    }
};

const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN');

const blockUser = async () => {
    if (!confirm(`'${post.value.authorName}' 회원을 정말로 차단하시겠습니까?`)) return;
    try {
        await axios.post(`/api/admin/users/${post.value.userId}/block`);
        alert("회원이 차단되었습니다.");
    } catch (e) {
        console.error("Block failed", e);
        alert("차단에 실패했습니다.");
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
.delay-200 { animation-delay: 0.2s; }
.delay-300 { animation-delay: 0.3s; }
@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
}
</style>
