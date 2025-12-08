<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500/30">
    <main class="container mx-auto px-6 py-10">
      <!-- Header -->
      <div class="mb-8 animate-fade-in-up">
        <h1 class="text-3xl font-bold mb-4 flex items-center gap-3">
          <Youtube :size="32" class="text-red-500" />
          영상 학습
        </h1>
        <p class="text-slate-400">관심 있는 키워드로 학습 영상을 검색해보세요.</p>
      </div>

      <!-- Search Bar -->
      <div class="mb-12 animate-fade-in-up delay-100">
        <form @submit.prevent="handleSearch" class="relative max-w-2xl">
          <input
            v-model="keyword"
            type="text"
            placeholder="검색어를 입력하세요 (예: Spring Boot, Vue.js)"
            class="w-full bg-slate-900/50 border border-white/10 rounded-xl px-6 py-4 pr-16 text-lg text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-red-500/50 focus:border-transparent transition-all shadow-lg shadow-black/20"
          />
          <button
            type="submit"
            :disabled="loading"
            class="absolute right-3 top-1/2 -translate-y-1/2 p-2 bg-red-600 hover:bg-red-500 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <div v-if="loading" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
            <Search v-else :size="20" />
          </button>
        </form>
      </div>

      <!-- Results Grid -->
      <section v-if="hasSearched" class="animate-fade-in-up delay-200">
        <div v-if="videos.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <article
            v-for="video in videos"
            :key="video.videoId"
            class="group bg-slate-900/50 border border-white/10 rounded-xl overflow-hidden hover:bg-slate-900 hover:border-red-500/30 transition-all hover:-translate-y-1 shadow-lg cursor-pointer"
            @click="openVideo(video.videoId)"
          >
            <!-- Thumbnail -->
            <div class="relative aspect-video overflow-hidden">
              <img :src="video.thumbnailUrl" :alt="video.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
              <div class="absolute inset-0 bg-black/20 group-hover:bg-black/0 transition-colors"></div>
              <div class="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                <div class="w-12 h-12 bg-red-600/90 rounded-full flex items-center justify-center shadow-lg backdrop-blur-sm">
                  <Play :size="24" class="text-white ml-1" />
                </div>
              </div>
            </div>

            <!-- Content -->
            <div class="p-5">
              <h3 class="font-bold text-lg mb-2 line-clamp-2 leading-snug group-hover:text-red-400 transition-colors" v-html="video.title"></h3>
              <div class="flex items-center gap-2 text-sm text-slate-400 mb-3">
                <span class="font-medium text-slate-300">{{ video.channelTitle }}</span>
                <span class="w-1 h-1 rounded-full bg-slate-600"></span>
                <span>{{ formatDate(video.publishedAt) }}</span>
              </div>
              <p class="text-sm text-slate-500 line-clamp-2">{{ video.description }}</p>
            </div>
          </article>
        </div>
        
        <!-- Empty State -->
        <div v-else class="text-center py-20 text-slate-500">
          <p>검색 결과가 없습니다.</p>
        </div>
      </section>

      <!-- Initial State -->
      <section v-else class="text-center py-20 text-slate-600 animate-fade-in-up delay-200">
        <div class="inline-flex p-4 bg-slate-900/50 rounded-full mb-4">
          <Youtube :size="48" class="text-slate-700" />
        </div>
        <p class="text-lg">검색어를 입력하고 학습 영상을 찾아보세요.</p>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Youtube, Search, Play } from 'lucide-vue-next';
import { youtubeApi } from '../api/youtube';

const keyword = ref('');
const videos = ref([]);
const loading = ref(false);
const hasSearched = ref(false);

const handleSearch = async () => {
    if (!keyword.value.trim()) return;
    
    loading.value = true;
    hasSearched.value = true;
    try {
        const res = await youtubeApi.search(keyword.value);
        videos.value = res.data;
    } catch (e) {
        console.error("Youtube Search Failed", e);
        alert("검색 중 오류가 발생했습니다.");
    } finally {
        loading.value = false;
    }
};

const openVideo = (videoId) => {
    window.open(`https://www.youtube.com/watch?v=${videoId}`, '_blank');
};

const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
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
