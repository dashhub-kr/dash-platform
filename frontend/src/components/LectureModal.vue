<template>
  <!-- 강의 + 문제 모달 -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="isOpen" class="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm" @click="close">
        <div class="bg-white rounded-3xl shadow-2xl w-full max-w-5xl max-h-[90vh] overflow-hidden flex flex-col" @click.stop>
          <!-- 헤더 -->
          <div class="flex items-center justify-between p-6 border-b border-slate-100 bg-slate-50">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-rose-100 flex items-center justify-center text-rose-600">
                <Youtube :size="20" />
              </div>
              <div>
                <h3 class="text-lg font-bold text-slate-800">{{ tagName }} 강의</h3>
                <p class="text-sm text-slate-500">영상을 클릭하면 바로 재생됩니다</p>
              </div>
            </div>
            <button @click="close" class="text-slate-400 hover:text-slate-600 transition-colors">
              <X :size="24" />
            </button>
          </div>
          
          <!-- 콘텐츠 -->
          <div class="flex-1 overflow-y-auto p-6 space-y-6">
            <!-- 로딩 -->
            <div v-if="loading" class="flex items-center justify-center py-12">
              <div class="text-center">
                <div class="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
                <p class="text-slate-500 text-sm">강의 검색 중...</p>
              </div>
            </div>
            
            <template v-else>
              <!-- 추천 문제 -->
              <section v-if="problems.length > 0">
                <h4 class="font-bold text-slate-700 flex items-center gap-2 mb-3">
                  <Code :size="16" class="text-indigo-500" />
                  이 태그와 함께 풀면 좋은 문제
                </h4>
                <div class="grid grid-cols-4 gap-3">
                  <div 
                    v-for="prob in problems.slice(0, 3)" 
                    :key="prob.problemId"
                    class="bg-slate-50 rounded-xl p-4 hover:bg-indigo-50 transition-colors cursor-pointer border border-slate-100 hover:border-indigo-200"
                    @click="goToProblem(prob.problemId)"
                  >
                    <div class="flex items-center gap-2 mb-2">
                      <img :src="`https://static.solved.ac/tier_small/${prob.level}.svg`" class="w-5 h-5" />
                      <span class="text-xs font-bold text-slate-500">#{{ prob.problemId }}</span>
                      <span v-if="prob.essential" class="ml-auto text-[10px] font-bold text-indigo-600 bg-indigo-100 px-1.5 py-0.5 rounded">필수</span>
                    </div>
                    <h5 class="font-medium text-slate-800 text-sm truncate">{{ prob.title }}</h5>
                  </div>
                  
                  <!-- 더 보기 카드 -->
                  <div 
                    @click="goToMoreProblems"
                    class="bg-indigo-50 rounded-xl p-4 hover:bg-indigo-100 transition-colors cursor-pointer border-2 border-dashed border-indigo-200 hover:border-indigo-400 flex flex-col items-center justify-center gap-2"
                  >
                    <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600">
                      <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                      </svg>
                    </div>
                    <span class="text-sm font-bold text-indigo-600">더 보기</span>
                  </div>
                </div>
              </section>
              
              <!-- 영상 목록 -->
              <section v-if="videos.length > 0">
                <h4 class="font-bold text-slate-700 mb-3 flex items-center gap-2">
                  <Youtube :size="16" class="text-rose-500" />
                  관련 강의 ({{ videos.length }}개)
                </h4>
                <div class="grid grid-cols-2 gap-4">
                  <div 
                    v-for="video in videos" 
                    :key="video.videoId"
                    class="group cursor-pointer rounded-xl overflow-hidden bg-slate-50 border border-slate-100 hover:border-rose-200 transition-all hover:shadow-md"
                    @click="playVideo(video.videoId)"
                  >
                    <div class="aspect-video relative overflow-hidden">
                      <img :src="video.thumbnailUrl" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
                      <div class="absolute inset-0 bg-black/20 group-hover:bg-black/40 transition-colors flex items-center justify-center">
                        <div class="w-12 h-12 rounded-full bg-white/90 flex items-center justify-center text-rose-500 opacity-0 group-hover:opacity-100 transition-opacity">
                          <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                        </div>
                      </div>
                    </div>
                    <div class="p-3">
                      <h5 class="font-medium text-slate-800 text-sm line-clamp-2">{{ video.title }}</h5>
                      <p class="text-xs text-slate-500 mt-1">{{ video.channelTitle }}</p>
                    </div>
                  </div>
                </div>
              </section>
              
              <div v-if="videos.length === 0 && problems.length === 0" class="text-center py-12 text-slate-500">
                검색 결과가 없습니다.
              </div>
            </template>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
  
  <!-- YouTube 재생 모달 -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="selectedVideoId" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm" @click="selectedVideoId = null">
        <div class="relative w-full max-w-5xl aspect-video" @click.stop>
          <button 
            @click="selectedVideoId = null"
            class="absolute -top-12 right-0 text-white/80 hover:text-white transition-colors flex items-center gap-2 text-sm font-medium"
          >
            <span>닫기</span>
            <div class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-colors">
              <X :size="20" />
            </div>
          </button>
          <iframe 
            :src="`https://www.youtube.com/embed/${selectedVideoId}?autoplay=1&rel=0`"
            class="w-full h-full rounded-2xl shadow-2xl"
            frameborder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowfullscreen
          ></iframe>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue';
import { Youtube, X, Code } from 'lucide-vue-next';
import { youtubeApi } from '@/api/youtube';
import { problemApi } from '@/api/problem';
import { useAuth } from '@/composables/useAuth';

const props = defineProps({
  isOpen: Boolean,
  tagName: String,
  tagKey: String,
  bojTagId: [String, Number]
});

const emit = defineEmits(['close']);

const { user } = useAuth();
const loading = ref(false);
const videos = ref([]);
const problems = ref([]);
const selectedVideoId = ref(null);

// Computed user tier
const userTier = () => user.value?.solvedacTier || 1;

// Watch for modal open to fetch data
watch(() => props.isOpen, async (isOpen) => {
  if (isOpen && props.tagName) {
    loading.value = true;
    videos.value = [];
    problems.value = [];
    
    try {
      const query = `${props.tagName} 알고리즘 강의`;
      const [videoRes, probRes] = await Promise.all([
        youtubeApi.search(query),
        props.tagKey ? problemApi.getRecommendations(props.tagKey, userTier()) : Promise.resolve({ data: [] })
      ]);
      videos.value = videoRes.data || [];
      problems.value = probRes.data || [];
    } catch (e) {
      console.error('Failed to load lecture data:', e);
    } finally {
      loading.value = false;
    }
  }
});

const close = () => {
  emit('close');
};

const playVideo = (videoId) => {
  selectedVideoId.value = videoId;
};

const goToProblem = (problemId) => {
  window.open(`https://www.acmicpc.net/problem/${problemId}`, '_blank');
};

const goToMoreProblems = () => {
  const tierStart = userTier();
  const tierEnd = Math.min(tierStart + 4, 30);
  
  if (props.bojTagId) {
    const tierRange = Array.from({ length: tierEnd - tierStart + 1 }, (_, i) => tierStart + i).join('%2C');
    const url = `https://www.acmicpc.net/problemset?sort=ac_desc&submit=pac%2Cfa%2Cus&tier=${tierRange}&algo=${props.bojTagId}&algo_if=and`;
    window.open(url, '_blank');
  } else if (props.tagKey) {
    const query = `*tag:${props.tagKey} tier:${tierStart}..${tierEnd} -s@${user.value?.solvedacHandle || ''}`;
    window.open(`https://solved.ac/search?query=${encodeURIComponent(query)}`, '_blank');
  }
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
