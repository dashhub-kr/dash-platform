<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500/30">

    <main class="container mx-auto px-6 py-10 max-w-4xl">
      <!-- Back Button -->
      <button 
        @click="$router.push('/boards')" 
        class="flex items-center gap-2 text-slate-400 hover:text-white mb-6 transition-colors animate-fade-in-up"
      >
        <ArrowLeft :size="20" />
        목록으로
      </button>

      <!-- Post Content -->
      <article v-if="post" class="bg-slate-900/50 border border-white/10 rounded-2xl p-8 mb-6 animate-fade-in-up delay-100">
        <!-- Header -->
        <div class="border-b border-white/5 pb-6 mb-6">
          <div class="flex items-center gap-3 mb-4">
            <span v-if="post.boardType === 'CODE_REVIEW'" class="px-3 py-1 text-xs font-bold rounded-full bg-emerald-500/20 text-emerald-400">코드리뷰</span>
            <span v-else class="px-3 py-1 text-xs font-bold rounded-full bg-slate-700 text-slate-300">일반</span>
          </div>
          <h1 class="text-3xl font-bold text-white mb-4 leading-tight">{{ post.title }}</h1>
          <div class="flex flex-wrap items-center gap-4 text-sm text-slate-400">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-full bg-indigo-500/20 flex items-center justify-center text-indigo-400 font-bold">
                 {{ post.authorName?.charAt(0).toUpperCase() || 'U' }}
              </div>
              <span class="text-slate-300">{{ post.authorName || '익명' }}</span>
            </div>
            <span class="w-1 h-1 rounded-full bg-slate-600"></span>
            <span>{{ formatDate(post.createdAt) }}</span>
          </div>
        </div>

        <!-- Body -->
        <div class="prose prose-invert max-w-none text-slate-300 leading-relaxed whitespace-pre-wrap">
          {{ post.content }}
        </div>

        <!-- Like Button -->
        <div class="mt-8 pt-6 border-t border-white/5 flex items-center gap-4">
          <button 
            @click="toggleLike"
            :class="[
              'flex items-center gap-2 px-6 py-3 rounded-xl font-bold transition-all',
              isLiked 
                ? 'bg-rose-500/20 text-rose-400 hover:bg-rose-500/30' 
                : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
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
      <section v-if="post" class="bg-slate-900/50 border border-white/10 rounded-2xl p-8 animate-fade-in-up delay-200">
        <h2 class="text-xl font-bold text-white mb-6 flex items-center gap-2">
          <MessageCircle :size="22" />
          댓글
        </h2>

        <!-- Comment Form -->
        <div class="mb-8">
          <textarea
            v-model="newComment"
            placeholder="댓글을 입력하세요..."
            class="w-full bg-slate-800 border border-white/10 rounded-xl p-4 text-slate-200 placeholder-slate-500 resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500/50"
            rows="3"
          ></textarea>
          <div class="flex justify-end mt-3">
            <button 
              @click="submitComment"
              :disabled="!newComment.trim()"
              class="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-700 disabled:text-slate-500 text-white rounded-lg font-bold transition-all"
            >
              댓글 작성
            </button>
          </div>
        </div>

        <!-- Comments List -->
        <div v-if="comments.length === 0" class="text-center py-10 text-slate-500">
          아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
        </div>
        <div v-else class="space-y-4">
          <div 
            v-for="comment in comments" 
            :key="comment.id"
            class="bg-slate-800/50 rounded-xl p-5"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-slate-700 flex items-center justify-center text-slate-400 font-bold text-sm">
                  {{ comment.authorName?.charAt(0).toUpperCase() || 'U' }}
                </div>
                <span class="font-medium text-slate-200">{{ comment.authorName }}</span>
                <span class="text-xs text-slate-500">{{ formatDate(comment.createdAt) }}</span>
              </div>
              <button 
                @click="toggleCommentLike(comment)"
                class="flex items-center gap-1 text-sm text-slate-400 hover:text-rose-400 transition-colors"
              >
                <ThumbsUp :size="14" />
                {{ comment.likeCount || 0 }}
              </button>
            </div>
            <p class="text-slate-300 whitespace-pre-wrap">{{ comment.content }}</p>

            <!-- Replies -->
            <div v-if="comment.replies && comment.replies.length > 0" class="mt-4 pl-6 border-l-2 border-slate-700 space-y-3">
              <div 
                v-for="reply in comment.replies" 
                :key="reply.id"
                class="bg-slate-900/50 rounded-lg p-4"
              >
                <div class="flex items-center gap-2 mb-2">
                  <span class="font-medium text-slate-300 text-sm">{{ reply.authorName }}</span>
                  <span class="text-xs text-slate-500">{{ formatDate(reply.createdAt) }}</span>
                </div>
                <p class="text-slate-400 text-sm">{{ reply.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Actions -->
      <div v-if="post && isAuthor" class="flex justify-end gap-3 mt-6 animate-fade-in-up delay-300">
        <button 
          @click="deletePost"
          class="px-4 py-2 rounded-lg text-rose-400 hover:bg-rose-950/30 transition-colors text-sm font-medium"
        >
          삭제
        </button>
        <button 
          @click="$router.push(`/boards/edit/${post.id}`)"
          class="px-5 py-2 rounded-lg bg-slate-800 text-slate-200 hover:bg-slate-700 transition-colors text-sm font-medium"
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
import { ArrowLeft, ThumbsUp, MessageCircle } from 'lucide-vue-next';
import { boardApi, commentApi } from '../api/board';
import { useAuth } from '../composables/useAuth';

const route = useRoute();
const router = useRouter();
const { user } = useAuth();

const post = ref(null);
const loading = ref(true);
const comments = ref([]);
const newComment = ref('');
const isLiked = ref(false);

const isAuthor = computed(() => {
    if (!post.value || !user.value) return false;
    return true; 
});

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString();
};

onMounted(async () => {
    try {
        const res = await boardApi.findById(route.params.id);
        post.value = res.data;
        
        // Load comments
        const commentsRes = await commentApi.findByBoardId(route.params.id);
        comments.value = commentsRes.data || [];
    } catch (e) {
        console.error("Failed to fetch post", e);
        alert("게시글을 찾을 수 없습니다.");
        router.push('/boards');
    } finally {
        loading.value = false;
    }
});

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
        console.error("Like toggle failed", e);
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
