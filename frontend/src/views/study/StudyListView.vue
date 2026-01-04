<template>
  <div class="min-h-screen bg-white text-slate-800 pb-20">
    <!-- 메인 레이아웃 컨테이너 -->
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full">
        
        <!-- 왼쪽 컬럼: 메인 콘텐츠 -->
        <main class="flex-1 min-w-0 space-y-6">          
          <StudyExplorer />
        </main>

        <!-- 오른쪽 컬럼: Honor Board (사이드바) -->
        <aside class="hidden xl:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]">
            <div class="bg-white rounded-3xl border border-slate-200 shadow-sm p-6">
                <div class="flex items-center justify-between mb-6">
                    <div class="flex items-center gap-2">
                        <Trophy class="w-5 h-5 text-brand-500" fill="currentColor" />
                        <h2 class="text-lg font-bold text-slate-800">Honor Board</h2>
                    </div>
                    <span class="text-xs font-medium text-slate-400">Weekly Top 3</span>
                </div>

                <div v-if="studies.length > 0" class="flex flex-col gap-3">
                    <div v-for="(study, idx) in studies.slice(0, 3)" :key="'rank-'+study.id" 
                         class="group relative bg-white border border-slate-100 rounded-2xl p-4 transition-all hover:border-brand-200 hover:shadow-md cursor-pointer"
                         :class="{
                           'bg-gradient-to-br from-amber-50/50 to-white border-amber-200/60': idx === 0,
                           'bg-gradient-to-br from-slate-50/50 to-white border-slate-200/60': idx === 1,
                           'bg-gradient-to-br from-orange-50/50 to-white border-orange-200/60': idx === 2
                         }"
                    >
                        <div class="flex items-center gap-3">
                            <!-- 순위 뱃지 -->
                            <div class="flex-shrink-0 w-8 h-8 flex items-center justify-center rounded-lg font-black text-sm shadow-sm"
                                 :class="{
                                   'bg-amber-100 text-amber-600': idx === 0,
                                   'bg-slate-100 text-slate-500': idx === 1,
                                   'bg-orange-100 text-orange-600': idx === 2,
                                   'bg-slate-50 text-slate-400': idx > 2
                                 }">
                                {{ idx + 1 }}
                            </div>
                            
                            <!-- 콘텐츠 -->
                            <div class="flex-1 min-w-0">
                                <div class="flex items-center justify-between">
                                    <h3 class="font-bold text-slate-800 text-sm truncate mb-0.5 group-hover:text-brand-600 transition-colors">
                                        {{ study.name }}
                                    </h3>
                                    <!-- 관리자 관전 버튼 -->
                                    <button v-if="isAdmin" @click.stop="observeStudy(study.id)" 
                                            class="text-[10px] bg-slate-800 text-white px-1.5 py-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity">
                                        관전
                                    </button>
                                </div>
                                <div class="flex items-center gap-2 text-xs text-slate-500">
                                   <span class="flex items-center gap-0.5" :class="{'text-orange-500 font-bold': idx === 0}">
                                      <Flame :size="12" fill="currentColor" /> {{ study.streak || 0 }}일
                                   </span>
                                   <span class="w-0.5 h-2 bg-slate-200 rounded-full"></span>
                                   <span class="truncate">멤버 {{ study.memberCount }}명</span>
                                </div>
                            </div>

                             <!-- 1위를 위한 왕관 아이콘 -->
                             <Trophy v-if="idx === 0" class="w-5 h-5 text-amber-400 absolute top-0 right-3 -translate-y-1/2 drop-shadow-sm filter" fill="currentColor" />
                        </div>
                    </div>
                </div>
                
                <div v-else class="text-center py-10 text-slate-400">
                    <Trophy class="w-12 h-12 mx-auto mb-2 opacity-20" />
                    <p class="text-sm">랭킹 데이터가 없습니다</p>
                </div>
            </div>
        </aside>

      </div>
    
      <!-- Honor Board 로직 -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { Trophy, Flame } from 'lucide-vue-next';
import StudyExplorer from '@/components/study/StudyExplorer.vue';
import { useAuth } from '@/composables/useAuth';

const studies = ref([]);
const { user } = useAuth();
const router = useRouter();

const isAdmin = computed(() => {
    return user.value?.role === 'ROLE_ADMIN' || user.value?.role === 'ADMIN'; 
});

const observeStudy = (studyId) => {
    router.push(`/admin/study/${studyId}/dashboard`);
};

onMounted(async () => {
  try {
    const res = await axios.get('/api/studies');
    studies.value = res.data;
  } catch (e) {
    console.error('스터디 목록 로드 실패', e);
  }
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
</style>
