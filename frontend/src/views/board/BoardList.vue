<template>
  <!-- Main Layout Wrapper matching DashboardView -->
  <div class="flex h-screen overflow-hidden bg-white font-['Pretendard']">
    <div class="w-full overflow-y-auto [scrollbar-gutter:stable]">
      <div class="min-h-screen bg-white pb-20">
    <!-- Main Layout Container -->
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full items-start">

        <!-- 왼쪽 컬럼: 메인 콘텐츠 -->
        <main class="flex-1 min-w-0 space-y-6">

          <!-- Header -->
          <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
             <div>
                <div class="flex items-center gap-3 mb-2">
                   <MessageSquare class="w-7 h-7 text-brand-500" stroke-width="2.5" fill="currentColor" />
                   <h1 class="text-xl font-black text-slate-800">전국 게시판</h1>
                </div>
                <p class="text-slate-500 font-medium">자유롭게 질문하고 정보를 공유해보세요</p>
             </div>
             <button
               @click="$router.push('/boards/write')"
               class="flex items-center gap-2 px-5 py-2.5 bg-brand-600 hover:bg-brand-500 text-white rounded-xl font-bold transition-all shadow-md shadow-brand-200 hover:-translate-y-0.5 shrink-0"
             >
               <PenSquare :size="18" />
               글쓰기
             </button>
          </div>



          <!-- 게시판 목록 -->
          <div class="bg-white/80 border border-white/60 rounded-3xl overflow-hidden shadow-xl shadow-slate-200/50 backdrop-blur-md animate-fade-in-up delay-100">
            <!-- 목록 헤더 -->
            <div class="grid grid-cols-[3.5rem_1fr_8rem_6rem_4rem] px-8 py-5 bg-slate-50 border-b border-slate-100 text-sm font-bold text-slate-500 uppercase tracking-wider">
              <div class="text-center">번호</div>
              <div class="pl-2">제목</div>
              <div class="text-left pl-2">작성자</div>
              <div class="text-left pl-2">추천/댓글</div>
              <div class="text-center">작성일</div>
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
                class="grid grid-cols-[3.5rem_1fr_8rem_6rem_4rem] px-8 py-5 border-b border-slate-100 hover:bg-brand-50/30 cursor-pointer transition-colors group items-center"
              >
                <div class="text-center text-slate-400 font-mono text-sm group-hover:text-brand-500 transition-colors">{{ post.id }}</div>
                <div class="pl-2 pr-4 flex items-center gap-2 min-w-0">
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
                <div class="text-left text-sm font-medium text-slate-600 flex items-center justify-start gap-2 pl-2">
                  <NicknameRenderer 
                       :username="post.authorName"
                       :avatar-url="post.authorProfileImageUrl"
                       :role="post.authorRole"
                       :user-id="post.userId"
                       :clickable="true"
                       :is-deleted="post.authorIsDeleted"
                       :enable-decoration="true"
                       :decoration-class="post.authorDecorationClass"
                       avatar-class="w-6 h-6 border border-slate-200"
                       text-class="font-medium text-slate-600 block truncate max-w-[6rem]"
                       :icon-size="16"
                  />
                </div>
                <div class="text-left text-sm text-slate-500 flex items-center justify-start gap-3 pl-2">
                  <span class="flex items-center gap-1.5" title="추천">
                    <div class="w-5 h-5 bg-rose-500 rounded-full flex items-center justify-center text-white shadow-sm shadow-rose-200">
                        <ThumbsUp :size="11" stroke-width="3" />
                    </div>
                    <span class="font-bold text-rose-500">{{ post.likeCount || 0 }}</span>
                  </span>
                  <span class="flex items-center gap-1.5" title="댓글">
                    <div class="w-5 h-5 bg-brand-500 rounded-full flex items-center justify-center text-white shadow-sm shadow-brand-200">
                        <MessageCircle :size="11" stroke-width="3" />
                    </div>
                    <span class="font-bold text-brand-500">{{ post.commentCount || 0 }}</span>
                  </span>
                </div>
                <div class="text-center text-xs text-slate-400 font-medium">
                  {{ formatDate(post.createdAt) }}
                </div>
              </div>
            </template>
          </div>
        </main>

        <!-- 오른쪽 컬럼: 사이드바 (인기글 + 검색) -->
        <aside class="hidden lg:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-fit">
          <!-- 인기글 섹션 (간소화된 스타일) -->
          <div v-if="popularPosts.length > 0 && !searchProblemNumber" class="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 animate-fade-in-up">
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-lg font-bold text-slate-800 flex items-center gap-2">
                <Flame class="w-5 h-5 text-orange-500" fill="currentColor" />
                인기글
              </h2>
              <span class="text-xs font-medium text-slate-400">Top 3</span>
            </div>
            <div class="flex flex-col gap-2">
              <div 
                v-for="(post, idx) in popularPosts.slice(0, 3)" 
                :key="'popular-' + post.id"
                @click="$router.push(`/boards/${post.id}`)"
                class="group relative bg-white border border-slate-100 rounded-xl p-3 transition-all hover:border-orange-200 hover:shadow-md cursor-pointer"
                :class="{
                  'bg-gradient-to-br from-orange-50/50 to-white border-orange-200/60': idx === 0,
                  'bg-gradient-to-br from-slate-50/50 to-white': idx === 1,
                  'bg-gradient-to-br from-amber-50/50 to-white border-amber-200/60': idx === 2
                }"
              >
                <div class="flex items-center gap-2 mb-2">
                  <span v-if="post.problemNumber" class="px-2.5 py-1 bg-white/80 backdrop-blur-sm border border-orange-200 text-orange-700 text-xs font-bold rounded-lg shadow-sm">P{{ post.problemNumber }}</span>
                  <span v-else class="px-2.5 py-1 bg-white/80 backdrop-blur-sm border border-slate-200 text-slate-500 text-xs font-bold rounded-lg shadow-sm">자유</span>
                  <div class="flex items-center gap-1.5 ml-auto">
                    <div class="w-5 h-5 bg-rose-500 rounded-full flex items-center justify-center text-white shadow-sm shadow-rose-200">
                       <ThumbsUp :size="11" stroke-width="3" />
                    </div>
                  </div>
                  
                  <!-- 추천수 -->
                  <div class="flex items-center gap-1 text-xs">
                    <ThumbsUp :size="12" class="text-rose-500" />
                    <span class="font-bold text-rose-500">{{ post.likeCount || 0 }}</span>
                  </div>
                </div>
                
                <!-- 1위 왕관 -->
                <Flame v-if="idx === 0" class="w-4 h-4 text-orange-400 absolute top-0 right-2 -translate-y-1/2 drop-shadow-sm" fill="currentColor" />
              </div>
            </div>
          </div>


          <!-- 검색 UI -->
          <div class="bg-slate-50/90 backdrop-blur-md p-6 rounded-3xl border border-slate-100 shadow-sm">
            <label class="block text-sm font-bold text-slate-500 mb-3 ml-1 flex items-center justify-between">
               <span>문제 번호 검색</span>
               <span v-if="searchProblemNumber" class="text-brand-600 cursor-pointer hover:underline" @click="clearSearch">
                  전체 목록 보기
               </span>
            </label>
            <div class="relative">
              <input 
                v-model="searchProblemNumber"
                @keyup.enter="searchPosts"
                type="number"
                placeholder="예: 1234"
                class="w-full bg-white border border-slate-200 rounded-2xl pl-12 pr-4 py-4 font-medium text-slate-800 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all shadow-sm placeholder:text-slate-400"
              />
              <Search class="w-5 h-5 text-slate-400 absolute left-4 top-1/2 -translate-y-1/2" />
            </div>
          </div>
          
          <!-- 가이드 정보 -->
          <div class="bg-gradient-to-br from-slate-50 to-brand-50/20 rounded-3xl p-6 border border-slate-100">
            <h3 class="font-bold text-slate-700 text-sm mb-2 flex items-center gap-2">
              <div class="w-6 h-6 bg-brand-500 rounded-lg flex items-center justify-center text-white shadow-sm">
                <Lightbulb :size="14" stroke-width="2.5" />
              </div>
              게시판 이용 가이드
            </h3>
            <p class="text-xs text-slate-500 leading-relaxed word-keep-all">
              커뮤니티 가이드라인을 위반하는 게시물은 제재될 수 있습니다.
            </p>
          </div>
        </aside>

      </div>
    </div>
  </div>
  </div>
</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { PenSquare, Inbox, ThumbsUp, MessageCircle, Search, Flame, Lightbulb, Code2, UserX, MessageSquare } from 'lucide-vue-next';
import NicknameRenderer from '@/components/common/NicknameRenderer.vue';
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
    const date = new Date(dateString);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}.${day}`;
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

</style>
