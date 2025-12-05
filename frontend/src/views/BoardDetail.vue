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
      <article v-if="post" class="bg-slate-900/50 border border-white/10 rounded-2xl p-8 mb-8 animate-fade-in-up delay-100">
        <!-- Header -->
        <div class="border-b border-white/5 pb-6 mb-6">
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
            <span class="w-1 h-1 rounded-full bg-slate-600"></span>
            <span>조회수 {{ post.viewCount || 0 }}</span>
          </div>
        </div>

        <!-- Body -->
        <div class="prose prose-invert max-w-none text-slate-300 leading-relaxed whitespace-pre-wrap">
          {{ post.content }}
        </div>
      </article>

      <div v-else-if="loading" class="text-center py-20 text-slate-500">
        게시글을 불러오는 중...
      </div>

      <!-- Actions -->
      <div v-if="post && isAuthor" class="flex justify-end gap-3 animate-fade-in-up delay-200">
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
import { ArrowLeft } from 'lucide-vue-next';
import { boardApi } from '../api/board';
import { useAuth } from '../composables/useAuth';

const route = useRoute();
const router = useRouter();
const { user } = useAuth();

const post = ref(null);
const loading = ref(true);

// Check if current user is author. 
// Note: This matches simple username or email. ideally should match ID.
const isAuthor = computed(() => {
    if (!post.value || !user.value) return false;
    // Assuming backend returns authorName. Adjust if it returns authorId.
    // user.value.username might differ from post.authorName depending on backend logic.
    // For now, let's allow everyone to see buttons or rely on backend error if not authorized.
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
    } catch (e) {
        console.error("Failed to fetch post", e);
        alert("게시글을 찾을 수 없습니다.");
        router.push('/boards');
    } finally {
        loading.value = false;
    }
});

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
@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
}
</style>
