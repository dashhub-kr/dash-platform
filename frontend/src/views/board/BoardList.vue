<template>
  <div class="min-h-screen bg-white text-slate-800">

    <!-- 메인 레이아웃 컨테이너 -->
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full items-start">

        <!-- 왼쪽 컬럼: 메인 콘텐츠 -->
        <main class="flex-1 min-w-0 space-y-6">

          <!-- 검색 바 -->
          <div class="animate-fade-in-up sticky top-6 z-40 bg-white/90 backdrop-blur-md p-1 -mx-1 rounded-2xl">
            <div class="flex gap-3">
              <div class="flex-1 relative">
                <Search :size="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
                <input 
                  v-model="searchProblemNumber"
                  @keyup.enter="searchPosts"
                  type="number"
                  placeholder="문제 번호로 검색 (예: 1234)"
                  class="w-full pl-12 pr-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-700 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-400 focus:border-transparent shadow-sm"
                />
              </div>
              <button @click="searchPosts" class="px-6 py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl font-bold transition-colors shadow-sm">
                검색
              </button>
              <button v-if="searchProblemNumber" @click="clearSearch" class="px-4 py-3 text-slate-500 hover:text-slate-700 transition-colors">
                초기화
              </button>
              <div class="w-px h-full bg-slate-200 mx-1"></div>
              <button
                @click="$router.push('/boards/write')"
                class="flex items-center gap-2 px-5 py-3 bg-brand-600 hover:bg-brand-500 text-white rounded-xl font-bold transition-all shadow-md shadow-brand-200 hover:-translate-y-0.5 shrink-0"
              >
                <PenSquare :size="18" />
                글쓰기
              </button>
            </div>
          </div>

          <!-- 게시판 목록 -->
          <div class="bg-white/80 border border-white/60 rounded-3xl overflow-hidden shadow-xl shadow-slate-200/50 backdrop-blur-md animate-fade-in-up delay-100">
            <!-- 목록 헤더 -->
            <div class="grid grid-cols-12 px-8 py-5 bg-slate-50 border-b border-slate-100 text-sm font-bold text-slate-500 uppercase tracking-wider">
              <div class="col-span-1 text-center">번호</div>
              <div class="col-span-5 pl-2">제목</div>
              <div class="col-span-2 text-center">작성자</div>
              <div class="col-span-2 text-center">추천/댓글</div>
              <div class="col-span-2 text-center">작성일</div>
            </div>

            <!-- 목록 아이템 -->
            <template v-if="loading">
              <div class="p-16 text-center text-slate-400 font-medium">
                <div class="animate-pulse flex flex-col items-center">
                  <div class="h-4 bg-slate-200 rounded w-1/4 mb-4"></div>
                  <div class="h-4 bg-slate-200 rounded w-1/2"></div>
                </div>
              </div>
            </template>
            <template v-else-if="filteredPosts.length === 0">
              <div class="p-20 text-center flex flex-col items-center justify-center">
                <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-6 text-slate-300">
                    <Inbox :size="32" />
                </div>
                <h3 class="text-xl font-bold text-slate-700 mb-2">{{ searchProblemNumber ? '검색 결과가 없습니다' : '작성된 글이 없습니다' }}</h3>
                <p class="text-slate-500">{{ searchProblemNumber ? '다른 문제 번호로 검색해보세요.' : '첫 번째 글을 작성해보세요!' }}</p>
              </div>
            </template>
            <template v-else>
              <div
                v-for="post in filteredPosts"
                :key="post.id"
                @click="$router.push(`/boards/${post.id}`)"
                :class="{'bg-slate-50/50 hover:!bg-slate-100/50': post.authorRole === 'ROLE_ADMIN'}"
                class="grid grid-cols-12 px-8 py-5 border-b border-slate-100 hover:bg-brand-50/30 cursor-pointer transition-colors group items-center"
              >
                <div class="col-span-1 text-center text-slate-400 font-mono text-sm group-hover:text-brand-500 transition-colors">{{ post.id }}</div>
                <div class="col-span-5 pl-2 pr-4 flex items-center gap-2">
                  <span v-if="post.boardType === 'CODE_REVIEW'" class="shrink-0 flex items-center justify-center w-6 h-6 rounded-full bg-emerald-100 text-emerald-700 border border-emerald-200/50 shadow-sm" title="Code Review">
                    <Code2 :size="14" stroke-width="2.5" />
                  </span>
                  <span v-if="post.problemNumber" class="shrink-0 px-2.5 py-1 text-[11px] font-bold rounded-full bg-blue-50 text-blue-600 whitespace-nowrap border border-blue-200 shadow-sm flex items-center h-fit">
                    P{{ post.problemNumber }}
                  </span>
                  <h3 class="text-slate-800 font-bold text-lg group-hover:text-brand-600 transition-colors truncate leading-normal">
                    {{ post.title }}
                  </h3>
                </div>
                <div class="col-span-2 text-center text-sm font-medium text-slate-600 flex items-center justify-center gap-2">
                  <UserX v-if="['Unknown User', 'Unknown'].includes(post.authorName)" :size="16" class="w-6 h-6 rounded-full border border-slate-200 bg-slate-50 p-1 text-slate-400" />
                  <img v-else-if="post.authorProfileImageUrl" :src="post.authorProfileImageUrl" class="w-6 h-6 rounded-full border border-slate-200" />
                  <div class="flex flex-col items-start">
                       <span v-if="post.studyName" class="text-[10px] text-brand-500 font-bold hidden sm:inline">[{{ post.studyName }}]</span>
                       <span class="flex items-center gap-1 min-w-0 max-w-[120px]">
                          <span 
                              class="truncate block max-w-[80px] sm:max-w-[100px]"
                              :class="{ 'admin-shining-text': post.authorRole === 'ROLE_ADMIN' }"
                          >
                              {{ ['Unknown User', 'Unknown'].includes(post.authorName) ? '탈퇴한 회원' : post.authorName }}
                          </span>
                       </span>
                  </div>
                </div>
                <div class="col-span-2 text-center text-sm text-slate-500 flex items-center justify-center gap-3">
                  <span class="flex items-center gap-1.5" title="추천">
                    <div class="w-6 h-6 bg-rose-500 rounded-full flex items-center justify-center text-white shadow-sm shadow-rose-200">
                        <ThumbsUp :size="12" stroke-width="3" />
                    </div>
                    <span class="font-bold text-rose-500">{{ post.likeCount || 0 }}</span>
                  </span>
                  <span class="flex items-center gap-1.5" title="댓글">
                    <div class="w-6 h-6 bg-brand-500 rounded-full flex items-center justify-center text-white shadow-sm shadow-brand-200">
                        <MessageCircle :size="12" stroke-width="3" />
                    </div>
                    <span class="font-bold text-brand-500">{{ post.commentCount || 0 }}</span>
                  </span>
                </div>
                <div class="col-span-2 text-center text-xs text-slate-400 font-medium">
                  {{ formatDate(post.createdAt) }}
                </div>
              </div>
            </template>
          </div>
        </main>

        <!-- 오른쪽 컬럼: 사이드바 (인기글) -->
        <aside class="hidden xl:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]">
          <!-- 인기글 섹션 -->
          <div v-if="popularPosts.length > 0 && !searchProblemNumber" class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 animate-fade-in-up">
            <h2 class="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
              <div class="w-8 h-8 bg-orange-500 rounded-lg flex items-center justify-center text-white shadow-sm shadow-orange-200">
                <Flame :size="18" fill="currentColor" class="text-white" />
              </div>
              인기글
            </h2>
            <div class="flex flex-col gap-4">
              <div 
                v-for="post in popularPosts.slice(0, 3)" 
                :key="'popular-' + post.id"
                @click="$router.push(`/boards/${post.id}`)"
                class="bg-gradient-to-br from-orange-50 to-amber-50 border border-orange-200 rounded-2xl p-5 cursor-pointer hover:shadow-lg hover:-translate-y-1 transition-all"
              >
                <div class="flex items-center gap-2 mb-2">
                  <span v-if="post.problemNumber" class="px-2.5 py-1 bg-white/80 backdrop-blur-sm border border-orange-200 text-orange-700 text-xs font-bold rounded-lg shadow-sm">P{{ post.problemNumber }}</span>
                  <span v-else class="px-2.5 py-1 bg-white/80 backdrop-blur-sm border border-slate-200 text-slate-500 text-xs font-bold rounded-lg shadow-sm">자유</span>
                  <div class="flex items-center gap-1.5 ml-auto">
                    <div class="w-6 h-6 bg-rose-500 rounded-full flex items-center justify-center text-white shadow-sm shadow-rose-200">
                       <ThumbsUp :size="12" stroke-width="3" />
                    </div>
                    <span class="text-rose-600 text-sm font-bold">{{ post.likeCount }}</span>
                  </div>
                </div>
                <h3 class="font-bold text-slate-800 truncate mb-1">{{ post.title }}</h3>
                  <div class="flex items-center gap-2 text-xs text-slate-500">
                    <UserX v-if="['Unknown User', 'Unknown'].includes(post.authorName)" :size="14" class="w-5 h-5 rounded-full border border-orange-200 bg-orange-50 p-1 text-orange-400" />
                    <template v-else>
                        <span v-if="post.studyName" class="text-brand-600 font-bold hidden xl:inline">[{{ post.studyName }}]</span>
                        <span 
                            class="truncate max-w-[100px]"
                            :class="{ 'admin-shining-text': post.authorRole === 'ROLE_ADMIN' }"
                        >
                            {{ ['Unknown User', 'Unknown'].includes(post.authorName) ? '탈퇴한 회원' : post.authorName }}
                        </span>
                    </template>
                  </div>
              </div>
            </div>
          </div>
          
          <!-- 추가 정보 또는 플레이스홀더 -->
          <div class="bg-gradient-to-br from-slate-50 to-brand-50/20 rounded-3xl p-6 border border-slate-100">
            <h3 class="font-bold text-slate-700 text-sm mb-2 flex items-center gap-2">
              <div class="w-6 h-6 bg-brand-500 rounded-lg flex items-center justify-center text-white shadow-sm">
                <Lightbulb :size="14" stroke-width="2.5" />
              </div>
              게시판 이용 가이드
            </h3>
            <p class="text-xs text-slate-500 leading-relaxed">
              자유롭게 질문하고 정보를 공유해보세요.
            </p>
          </div>
        </aside>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { PenSquare, Inbox, ThumbsUp, MessageCircle, Search, Flame, Lightbulb, Code2, UserX } from 'lucide-vue-next';
import { boardApi } from '@/api/board';

const router = useRouter();
const posts = ref([]);
const popularPosts = ref([]);
const loading = ref(true);
const searchProblemNumber = ref('');

const filteredPosts = computed(() => {
    if (!searchProblemNumber.value) return posts.value;
    const num = parseInt(searchProblemNumber.value);
    return posts.value.filter(p => p.problemNumber === num);
});

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString();
};

const searchPosts = async () => {
    // Filter is done client-side via computed
};

const clearSearch = () => {
    searchProblemNumber.value = '';
};

const fetchPopularPosts = async () => {
    try {
        const response = await boardApi.findPopular(5);
        popularPosts.value = response.data || [];
    } catch (e) {
        console.error("Failed to fetch popular posts", e);
    }
};

onMounted(async () => {
    try {
        const [postsRes] = await Promise.all([
            boardApi.findAll(),
            fetchPopularPosts()
        ]);
        posts.value = postsRes.data || [];
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

.admin-shining-text {
  background: linear-gradient(to right, #6366f1 20%, #a855f7 40%, #ec4899 60%, #6366f1 80%);
  background-size: 200% auto;
  color: #000;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shine 3s linear infinite;
  font-weight: 900;
}

@keyframes shine {
  to {
    background-position: 200% center;
  }
}
</style>
