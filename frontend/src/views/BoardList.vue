<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500/30">

    <main class="container mx-auto px-6 py-10 max-w-5xl">
      <!-- Page Header -->
      <div class="flex justify-between items-center mb-8 animate-fade-in-up">
        <div>
          <h1 class="text-3xl font-bold mb-2">게시판</h1>
          <p class="text-slate-400">스터디원들과 자유롭게 이야기를 나눠보세요.</p>
        </div>
        <button
          @click="$router.push('/boards/write')"
          class="flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg font-semibold transition-all hover:scale-105 shadow-lg shadow-indigo-500/20"
        >
          <PenSquare :size="18" />
          글쓰기
        </button>
      </div>

      <!-- Board List -->
      <div class="bg-slate-900/50 border border-white/10 rounded-2xl overflow-hidden animate-fade-in-up delay-100">
        <!-- List Header -->
        <div class="grid grid-cols-12 px-6 py-4 bg-slate-900 border-b border-white/5 text-sm font-medium text-slate-400">
          <div class="col-span-1 text-center">번호</div>
          <div class="col-span-7">제목</div>
          <div class="col-span-2 text-center">작성자</div>
          <div class="col-span-2 text-center">작성일</div>
        </div>

        <!-- List Items -->
        <template v-if="loading">
          <div class="p-8 text-center text-slate-500">
            로딩 중...
          </div>
        </template>
        <template v-else-if="posts.length === 0">
          <div class="p-12 text-center text-slate-500 flex flex-col items-center">
            <inbox :size="48" class="mb-4 opacity-50" />
            <p>아직 작성된 글이 없습니다.</p>
          </div>
        </template>
        <template v-else>
          <div
            v-for="post in posts"
            :key="post.id"
            @click="$router.push(`/boards/${post.id}`)"
            class="grid grid-cols-12 px-6 py-4 border-b border-white/5 hover:bg-white/[0.02] cursor-pointer transition-colors group items-center"
          >
            <div class="col-span-1 text-center text-slate-500 text-sm">{{ post.id }}</div>
            <div class="col-span-7 pr-4">
              <h3 class="text-slate-200 font-medium group-hover:text-indigo-400 transition-colors truncate">
                {{ post.title }}
              </h3>
            </div>
            <div class="col-span-2 text-center text-sm text-slate-400">
              {{ post.authorName || '익명' }}
            </div>
            <div class="col-span-2 text-center text-xs text-slate-500 font-mono">
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
import { PenSquare, Inbox } from 'lucide-vue-next';
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
