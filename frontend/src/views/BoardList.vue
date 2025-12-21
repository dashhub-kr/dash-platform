<template>
  <div class="min-h-screen bg-[#f0f9ff] text-slate-800 font-[Pretendard] relative overflow-hidden">
    
    <!-- Background Decor -->
    <div class="absolute top-0 right-0 w-[500px] h-[500px] bg-slate-200/50 rounded-full blur-[120px] mix-blend-multiply opacity-50 z-0 pointer-events-none"></div>

    <main class="container mx-auto px-6 py-10 max-w-5xl relative z-10">
      <!-- Page Header -->
      <div class="flex justify-between items-end mb-10 animate-fade-in-up">
        <div>
          <h1 class="text-4xl font-extrabold mb-3 tracking-tight text-slate-900">게시판</h1>
          <p class="text-slate-500 font-medium text-lg">스터디원들과 자유롭게 지식을 공유하세요.</p>
        </div>
        <button
          @click="$router.push('/boards/write')"
          class="flex items-center gap-2 px-6 py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-2xl font-bold transition-all hover:-translate-y-1 shadow-lg shadow-indigo-500/20 active:scale-95"
        >
          <PenSquare :size="20" />
          글쓰기
        </button>
      </div>

      <!-- Board List -->
      <div class="bg-white/80 border border-white/60 rounded-3xl overflow-hidden shadow-xl shadow-slate-200/50 backdrop-blur-md animate-fade-in-up delay-100">
        <!-- List Header -->
        <div class="grid grid-cols-12 px-8 py-5 bg-slate-50 border-b border-slate-100 text-sm font-bold text-slate-500 uppercase tracking-wider">
          <div class="col-span-1 text-center">번호</div>
          <div class="col-span-5 pl-2">제목</div>
          <div class="col-span-2 text-center">작성자</div>
          <div class="col-span-2 text-center">추천/댓글</div>
          <div class="col-span-2 text-center">작성일</div>
        </div>

        <!-- List Items -->
        <template v-if="loading">
          <div class="p-16 text-center text-slate-400 font-medium">
            <div class="animate-pulse flex flex-col items-center">
              <div class="h-4 bg-slate-200 rounded w-1/4 mb-4"></div>
              <div class="h-4 bg-slate-200 rounded w-1/2"></div>
            </div>
          </div>
        </template>
        <template v-else-if="posts.length === 0">
          <div class="p-20 text-center flex flex-col items-center justify-center">
            <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-6 text-slate-300">
                <Inbox :size="32" />
            </div>
            <h3 class="text-xl font-bold text-slate-700 mb-2">작성된 글이 없습니다</h3>
            <p class="text-slate-500">첫 번째 글을 작성해보세요!</p>
          </div>
        </template>
        <template v-else>
          <div
            v-for="post in posts"
            :key="post.id"
            @click="$router.push(`/boards/${post.id}`)"
            class="grid grid-cols-12 px-8 py-5 border-b border-slate-100 hover:bg-indigo-50/30 cursor-pointer transition-colors group items-center"
          >
            <div class="col-span-1 text-center text-slate-400 font-mono text-sm group-hover:text-indigo-500 transition-colors">{{ post.id }}</div>
            <div class="col-span-5 pl-2 pr-4 flex items-center gap-2">
              <span v-if="post.boardType === 'CODE_REVIEW'" class="px-2 py-0.5 text-xs font-bold rounded-full bg-emerald-100 text-emerald-700">코드리뷰</span>
              <h3 class="text-slate-800 font-bold text-lg group-hover:text-indigo-600 transition-colors truncate">
                {{ post.title }}
              </h3>
            </div>
            <div class="col-span-2 text-center text-sm font-medium text-slate-600">
              {{ post.authorName || '익명' }}
            </div>
            <div class="col-span-2 text-center text-sm text-slate-500 flex items-center justify-center gap-3">
              <span class="flex items-center gap-1" title="추천">
                <ThumbsUp :size="14" class="text-rose-400" />
                {{ post.likeCount || 0 }}
              </span>
              <span class="flex items-center gap-1" title="댓글">
                <MessageCircle :size="14" class="text-indigo-400" />
                {{ post.commentCount || 0 }}
              </span>
            </div>
            <div class="col-span-2 text-center text-xs text-slate-400 font-medium">
              {{ formatDate(post.createdAt) }}
            </div>
          </div>
        </template>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { PenSquare, Inbox, ThumbsUp, MessageCircle } from 'lucide-vue-next';
import { boardApi } from '../api/board';

const router = useRouter();
const posts = ref([]);
const loading = ref(true);

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString();
};

onMounted(async () => {
    try {
        const response = await boardApi.findAll();
        posts.value = response.data || [];
    } catch (e) {
        console.error("Failed to fetch posts", e);
    } finally {
        loading.value = false;
    }
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}
.delay-100 { animation-delay: 0.1s; }

@keyframes fade-in-up {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
