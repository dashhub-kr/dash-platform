<template>
  <div class="min-h-screen bg-[#f0f9ff] text-slate-800 selection:bg-indigo-500/30 font-[Pretendard]">

    <main class="container mx-auto px-6 py-10 max-w-5xl">
      <!-- Back Button -->
      <button 
        @click="$router.push('/boards')" 
        class="flex items-center gap-2 text-slate-500 hover:text-indigo-600 mb-6 transition-colors animate-fade-in-up"
      >
        <ArrowLeft :size="20" />
        목록으로
      </button>

      <!-- Post Content -->
      <article v-if="post" class="bg-white/80 border border-white/60 shadow-xl shadow-indigo-500/5 backdrop-blur-md rounded-3xl p-8 mb-6 animate-fade-in-up delay-100">
        <!-- Header -->
        <div class="border-b border-slate-100 pb-6 mb-6">
          <div class="flex items-center gap-3 mb-4">
            <span v-if="post.boardType === 'CODE_REVIEW'" class="px-3 py-1 text-xs font-bold rounded-full bg-emerald-100 text-emerald-700">코드리뷰</span>
            <span v-else class="px-3 py-1 text-xs font-bold rounded-full bg-slate-100 text-slate-600">일반</span>
          </div>
          <h1 class="text-3xl font-extrabold text-slate-900 mb-4 leading-tight">{{ post.title }}</h1>
          <div class="flex flex-wrap items-center gap-4 text-sm text-slate-500">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-full bg-indigo-50 flex items-center justify-center text-indigo-600 font-bold border border-indigo-100">
                 {{ post.authorName?.charAt(0).toUpperCase() || 'U' }}
              </div>
              <span class="text-slate-700 font-medium">{{ post.authorName || '익명' }}</span>
            </div>
            <span class="w-1 h-1 rounded-full bg-slate-300"></span>
            <span>{{ formatDate(post.createdAt) }}</span>
          </div>
        </div>

        <!-- Body -->
        <div class="prose max-w-none text-slate-700 leading-relaxed whitespace-pre-wrap mb-6">
          {{ post.content }}
        </div>

        <!-- Code Viewer (for CODE_REVIEW posts) -->
        <div v-if="post.boardType === 'CODE_REVIEW' && algorithmRecord" class="mb-6">
          <h3 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
            <Code2 :size="20" class="text-emerald-500" />
            코드
          </h3>
          <!-- CodeViewer stays dark usually, or we can check its implementation. Assuming default component works fine. -->
          <CodeViewer
            :code="algorithmRecord.code"
            :language="algorithmRecord.language || 'java'"
            :filename="`${algorithmRecord.title}.${getExtension(algorithmRecord.language)}`"
            :comments="comments"
            @submit-comment="submitLineComment"
          />
        </div>


        <!-- Code loading state -->
        <div v-else-if="post.boardType === 'CODE_REVIEW' && loadingCode" class="mb-6 p-6 bg-slate-100 rounded-xl text-center text-slate-500">
          <div class="animate-pulse">코드를 불러오는 중...</div>
        </div>

        <!-- Like Button -->
        <div class="mt-8 pt-6 border-t border-slate-100 flex items-center gap-4">
          <button 
            @click="toggleLike"
            :class="[
              'flex items-center gap-2 px-6 py-3 rounded-xl font-bold transition-all shadow-sm',
              isLiked 
                ? 'bg-rose-50 text-rose-500 border border-rose-100' 
                : 'bg-white border border-slate-200 text-slate-600 hover:text-indigo-600 hover:border-indigo-200'
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

      <!-- Comments Section -->
      <section v-if="post" class="bg-white/80 border border-white/60 shadow-xl shadow-indigo-500/5 backdrop-blur-md rounded-3xl p-8 animate-fade-in-up delay-200">
        <h2 class="text-xl font-bold text-slate-800 mb-6 flex items-center gap-2">
          <MessageCircle :size="22" />
          일반 댓글
        </h2>

        <!-- Comment Form -->
        <div class="mb-8">
          <textarea
            v-model="newComment"
            placeholder="따뜻한 댓글을 남겨주세요..."
            class="w-full bg-white border border-slate-200 rounded-2xl p-4 text-slate-800 placeholder-slate-400 resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent shadow-inner transition-all"
            rows="3"
          ></textarea>
          <div class="flex justify-end mt-3">
            <button 
              @click="submitComment"
              :disabled="!newComment.trim()"
              class="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-200 disabled:text-slate-400 text-white rounded-xl font-bold transition-all shadow-lg shadow-indigo-500/20"
            >
              댓글 작성
            </button>
          </div>
        </div>

        <!-- Comments List (General comments only - no lineNumber) -->
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
                <div class="w-8 h-8 rounded-full bg-white shadow-sm border border-slate-100 flex items-center justify-center text-slate-500 font-bold text-sm">
                  {{ comment.authorName?.charAt(0).toUpperCase() || 'U' }}
                </div>
                <span class="font-medium text-slate-700">{{ comment.authorName }}</span>
                <span class="text-xs text-slate-400">{{ formatDate(comment.createdAt) }}</span>
              </div>
              <button 
                @click="toggleCommentLike(comment)"
                class="flex items-center gap-1 text-sm text-slate-400 hover:text-rose-500 transition-colors"
              >
                <ThumbsUp :size="14" />
                {{ comment.likeCount || 0 }}
              </button>
            </div>
            <p class="text-slate-700 whitespace-pre-wrap pl-11">{{ comment.content }}</p>

            <!-- Replies -->
            <div v-if="comment.replies && comment.replies.length > 0" class="mt-4 pl-11 border-l-2 border-slate-200 space-y-3">
              <div 
                v-for="reply in comment.replies" 
                :key="reply.id"
                class="bg-white rounded-xl p-4 border border-slate-100 shadow-sm"
              >
                <div class="flex items-center gap-2 mb-2">
                  <span class="font-medium text-slate-700 text-sm">{{ reply.authorName }}</span>
                  <span class="text-xs text-slate-400">{{ formatDate(reply.createdAt) }}</span>
                </div>
                <p class="text-slate-600 text-sm">{{ reply.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Actions -->
      <div v-if="post && isAuthor" class="flex justify-end gap-3 mt-6 animate-fade-in-up delay-300">
        <button 
          @click="deletePost"
          class="px-4 py-2 rounded-xl text-rose-500 hover:bg-rose-50 transition-colors text-sm font-bold"
        >
          삭제
        </button>
        <button 
          @click="$router.push(`/boards/edit/${post.id}`)"
          class="px-5 py-2 rounded-xl bg-white text-slate-600 border border-slate-200 hover:border-indigo-500 hover:text-indigo-600 transition-all text-sm font-bold shadow-sm"
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
import { ArrowLeft, ThumbsUp, MessageCircle, Code2 } from 'lucide-vue-next';
import { boardApi, commentApi } from '../api/board';
import { algorithmApi } from '../api/algorithm';
import { useAuth } from '../composables/useAuth';
import CodeViewer from '../components/CodeViewer.vue';

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
    return true; 
    return true; 
});

// General comments (no lineNumber)
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

        // Load comments
        const commentsRes = await commentApi.findByBoardId(route.params.id);
        comments.value = commentsRes.data || [];

        // Load algorithm record if CODE_REVIEW
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
        await commentApi.like(comment.id);
        comment.likeCount = (comment.likeCount || 0) + 1;
    } catch (e) {
        console.error("Comment like failed", e);
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
