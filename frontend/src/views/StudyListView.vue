<template>
  <div class="min-h-screen bg-white text-slate-800 pb-20">
    <!-- Main Layout Container -->
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full">
        
        <!-- LEFT COLUMN: Main Content -->
        <main class="flex-1 min-w-0 space-y-6">          
          <StudyExplorer />
        </main>

        <!-- RIGHT COLUMN: Honor Board (Sidebar) -->
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
                            <!-- Rank Badge -->
                            <div class="flex-shrink-0 w-8 h-8 flex items-center justify-center rounded-lg font-black text-sm shadow-sm"
                                 :class="{
                                   'bg-amber-100 text-amber-600': idx === 0,
                                   'bg-slate-100 text-slate-500': idx === 1,
                                   'bg-orange-100 text-orange-600': idx === 2,
                                   'bg-slate-50 text-slate-400': idx > 2
                                 }">
                                {{ idx + 1 }}
                            </div>
                            
                            <!-- Content -->
                            <div class="flex-1 min-w-0">
                                <h3 class="font-bold text-slate-800 text-sm truncate mb-0.5 group-hover:text-brand-600 transition-colors">
                                    {{ study.name }}
                                </h3>
                                <div class="flex items-center gap-2 text-xs text-slate-500">
                                   <span class="flex items-center gap-0.5" :class="{'text-orange-500 font-bold': idx === 0}">
                                      <Flame :size="12" fill="currentColor" /> {{ study.streak || 0 }}일
                                   </span>
                                   <span class="w-0.5 h-2 bg-slate-200 rounded-full"></span>
                                   <span class="truncate">멤버 {{ study.memberCount }}명</span>
                                </div>
                            </div>

                             <!-- Crown Icon for 1st Place -->
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
    
      <!-- Confirmation Modal logic was moved to StudyExplorer, but Honor Board acts independently for now -->
      <!-- If Honor Board needs to open apply modal, we might need shared state or event bus, but for now assuming Honor Board relies on study navigation or is just display -->
      <!-- Actually, original StudyListView didn't have actions in Honor Board, just display. -->
      <!-- Wait, original Honor Board was just looping studies. It didn't have apply button. It just showed stats. -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { Trophy, Flame } from 'lucide-vue-next';
import StudyExplorer from '../components/StudyExplorer.vue';

const studies = ref([]);

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
