<template>
  <div class="min-h-screen bg-white text-slate-800">

    <div class="relative z-10 p-6 md:p-10 max-w-4xl mx-auto">
      


      <!-- 로딩 -->
      <div v-if="loading" class="text-center py-20 text-slate-500 animate-pulse text-xl">
        스터디 목록을 불러오는 중...
      </div>

      <!-- Top 3 시상대 -->
      <div v-else-if="studies.length > 0" class="mb-12">
        <div class="flex flex-wrap justify-center items-end gap-4 md:gap-6">
          <!-- 2위 -->
          <div v-if="studies[1]" class="flex flex-col items-center order-1 md:order-1">
            <img src="/models/2nd.png" alt="2nd" class="w-24 h-24 md:w-32 md:h-32 object-contain drop-shadow-lg" />
            <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-t-2xl p-4 text-center shadow-lg min-w-[140px] h-24 flex flex-col justify-center">
              <p class="text-base font-bold text-slate-800 truncate">{{ studies[1].name }}</p>
              <p class="text-xs text-slate-400 mt-1">MVP</p>
              <p class="text-sm font-bold text-brand-600 truncate">{{ studies[1].mvpUsername || '-' }}</p>
            </div>
          </div>
          
          <!-- 1위 -->
          <div v-if="studies[0]" class="flex flex-col items-center order-0 md:order-2">
            <img src="/models/1st.png" alt="1st" class="w-32 h-32 md:w-44 md:h-44 object-contain drop-shadow-xl animate-bounce-slow" />
            <div class="bg-gradient-to-br from-amber-50 to-yellow-100 backdrop-blur-xl border border-amber-300 rounded-t-2xl p-5 text-center shadow-xl min-w-[160px] h-32 flex flex-col justify-center">
              <p class="text-lg font-bold text-slate-800 truncate">{{ studies[0].name }}</p>
              <p class="text-xs text-slate-500 mt-1">MVP</p>
              <p class="text-base font-bold text-amber-600 truncate">{{ studies[0].mvpUsername || '-' }}</p>
            </div>
          </div>
          
          <!-- 3위 -->
          <div v-if="studies[2]" class="flex flex-col items-center order-2 md:order-3">
            <img src="/models/3rd.png" alt="3rd" class="w-20 h-20 md:w-28 md:h-28 object-contain drop-shadow-lg" />
            <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-t-2xl p-4 text-center shadow-lg min-w-[130px] h-20 flex flex-col justify-center">
              <p class="text-sm font-bold text-slate-800 truncate">{{ studies[2].name }}</p>
              <p class="text-xs text-slate-400">MVP</p>
              <p class="text-sm font-bold text-orange-600 truncate">{{ studies[2].mvpUsername || '-' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 전체 랭킹 목록 -->
      <div v-if="studies.length > 0" class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-3xl overflow-hidden shadow-xl">
        <div class="px-6 py-4 bg-slate-50 border-b border-slate-200">
          <h2 class="text-lg font-bold text-slate-700">전체 순위</h2>
        </div>
        <div class="divide-y divide-slate-100">
          <div v-for="(study, idx) in studies" :key="study.id"
               class="flex items-center gap-4 px-6 py-4 hover:bg-slate-50 transition-colors">
            <!-- 순위 -->
            <div class="w-12 h-12 flex items-center justify-center">
              <span v-if="idx === 0" class="text-2xl font-black text-amber-500">1</span>
              <span v-else-if="idx === 1" class="text-2xl font-bold text-slate-400">2</span>
              <span v-else-if="idx === 2" class="text-2xl font-bold text-orange-400">3</span>
              <span v-else class="text-2xl font-bold text-slate-300">{{ idx + 1 }}</span>
            </div>
            
            <!-- 스터디 정보 -->
            <div class="flex-1 min-w-0">
              <p class="font-bold text-slate-800 truncate">{{ study.name }}</p>
              <div class="flex items-center gap-3 text-sm text-slate-500">
                <span>{{ study.memberCount }}명</span>
                <span class="text-slate-300">|</span>
                <span :class="getTierClass(study.tierBadge)">{{ study.tierBadge }}</span>
              </div>
            </div>
            
            <!-- 통계 -->
            <div class="text-right">
              <p class="text-xl font-bold text-brand-600">{{ study.totalSolved }}</p>
              <p class="text-xs text-slate-400">문제 풀이</p>
            </div>
            
            <!-- 가입 버튼 -->
            <button @click="joinStudy(study.id)"
                    class="px-4 py-2 bg-brand-600 hover:bg-brand-500 text-white text-sm font-medium rounded-lg transition-colors">
              가입
            </button>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else-if="!loading" class="text-center py-20 text-slate-400 text-xl">
        등록된 스터디가 없습니다
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const loading = ref(true);
const studies = ref([]);

onMounted(async () => {
  try {
    const res = await axios.get('/api/studies');
    studies.value = res.data;
  } catch (e) {
    console.error('스터디 목록 로드 실패', e);
  } finally {
    loading.value = false;
  }
});

const getTierClass = (tierBadge) => {
  const classes = {
    Ruby: 'text-rose-500 font-bold',
    Diamond: 'text-cyan-500 font-bold',
    Platinum: 'text-teal-500 font-bold',
    Gold: 'text-amber-500 font-bold',
    Silver: 'text-slate-400 font-bold',
    Bronze: 'text-orange-600 font-bold',
  };
  return classes[tierBadge] || 'text-slate-400';
};

const joinStudy = async (studyId) => {
  try {
    await axios.post(`/api/studies/${studyId}/join`);
    alert('스터디에 가입되었습니다!');
  } catch (e) {
    console.error('가입 실패', e);
    alert('가입에 실패했습니다.');
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

@keyframes bounce-slow {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}
.animate-bounce-slow {
  animation: bounce-slow 2s ease-in-out infinite;
}
</style>
