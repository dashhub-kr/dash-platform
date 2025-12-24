<template>
  <div class="study-list-container relative w-full min-h-screen bg-slate-50 font-[Pretendard]">
    <!-- 배경 효과 -->
    <div class="absolute inset-0 bg-gradient-to-br from-amber-50 via-white to-indigo-50"></div>
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
      <div class="absolute top-1/4 left-1/4 w-96 h-96 bg-amber-200/30 rounded-full blur-3xl animate-pulse mix-blend-multiply"></div>
      <div class="absolute bottom-1/3 right-1/4 w-80 h-80 bg-indigo-200/30 rounded-full blur-3xl animate-pulse delay-1000 mix-blend-multiply"></div>
    </div>

    <div class="relative z-10 p-6 md:p-10 max-w-6xl mx-auto">
      
      <!-- 헤더 -->
      <div class="text-center mb-12">
        <h1 class="text-4xl md:text-5xl font-black text-slate-900 tracking-tight mb-2">스터디 둘러보기</h1>
        <p class="text-slate-500 text-lg">활발하게 활동 중인 스터디를 찾아보세요</p>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" class="text-center py-20 text-slate-500 animate-pulse text-xl">
        스터디 목록을 불러오는 중...
      </div>

      <!-- Top 3 시상대 (유지 or 수정, 여기선 유지하되 데이터 연동 확인) -->
      <div v-else-if="studies.length > 0" class="mb-12">
         <!-- ... Top 3 UI Code ... (Simplify for brevity in this replacement if unchanged, but I must provide full content if replacing block) -->
         <!-- I will keep the Top 3 but add streak info maybe? Or keeps as is for visual appeal -->
         <!-- To save tokens/complexity, I'll rely on the existing Top 3 structure but just ensure variables match -->
         <div class="flex flex-wrap justify-center items-end gap-4 md:gap-6">
          <!-- 2위 -->
          <div v-if="studies[1]" class="flex flex-col items-center order-1 md:order-1">
            <img src="/models/2nd.png" alt="2nd" class="w-24 h-24 md:w-32 md:h-32 object-contain drop-shadow-lg" />
            <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-t-2xl p-4 text-center shadow-lg min-w-[140px] h-24 flex flex-col justify-center">
              <p class="text-base font-bold text-slate-800 truncate">{{ studies[1].name }}</p>
              <div class="flex justify-center items-center gap-1 text-xs text-orange-500 font-bold mt-1">
                 <span>🔥 {{ studies[1].streak || 0 }}일</span>
              </div>
            </div>
          </div>
          
          <!-- 1위 -->
          <div v-if="studies[0]" class="flex flex-col items-center order-0 md:order-2">
            <img src="/models/1st.png" alt="1st" class="w-32 h-32 md:w-44 md:h-44 object-contain drop-shadow-xl animate-bounce-slow" />
            <div class="bg-gradient-to-br from-amber-50 to-yellow-100 backdrop-blur-xl border border-amber-300 rounded-t-2xl p-5 text-center shadow-xl min-w-[160px] h-32 flex flex-col justify-center">
              <p class="text-lg font-bold text-slate-800 truncate">{{ studies[0].name }}</p>
              <div class="flex justify-center items-center gap-1 text-sm text-orange-600 font-bold mt-1">
                 <span>🔥 {{ studies[0].streak || 0 }}일 연속</span>
              </div>
            </div>
          </div>
          
          <!-- 3위 -->
          <div v-if="studies[2]" class="flex flex-col items-center order-2 md:order-3">
            <img src="/models/3rd.png" alt="3rd" class="w-20 h-20 md:w-28 md:h-28 object-contain drop-shadow-lg" />
            <div class="bg-white/90 backdrop-blur-xl border border-slate-200 rounded-t-2xl p-4 text-center shadow-lg min-w-[130px] h-20 flex flex-col justify-center">
              <p class="text-sm font-bold text-slate-800 truncate">{{ studies[2].name }}</p>
              <div class="flex justify-center items-center gap-1 text-xs text-orange-500 font-bold mt-1">
                 <span>🔥 {{ studies[2].streak || 0 }}일</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 탐색 목록 -->
      <div v-if="studies.length > 0" class="bg-white/80 backdrop-blur-xl border border-white/50 rounded-3xl overflow-hidden shadow-xl">
        <div class="px-6 py-4 bg-slate-50 border-b border-slate-200 flex justify-between items-center">
          <h2 class="text-lg font-bold text-slate-700">활동 중인 스터디</h2>
          <div class="text-sm text-slate-400">총 {{ studies.length }}개</div>
        </div>
        <div class="divide-y divide-slate-100">
          <div v-for="(study, idx) in studies" :key="study.id"
               class="flex items-center gap-4 px-6 py-5 hover:bg-slate-50 transition-colors group">
            
            <!-- 랭킹 배지 -->
            <div class="w-10 h-10 flex-shrink-0 flex items-center justify-center font-black text-slate-300 italic text-xl">
              {{ idx + 1 }}
            </div>
            
            <!-- 스터디 정보 -->
            <div class="flex-1 min-w-0 grid grid-cols-12 gap-4 items-center">
              <div class="col-span-12 md:col-span-5">
                <p class="font-bold text-slate-800 text-lg truncate group-hover:text-indigo-600 transition-colors">{{ study.name }}</p>
                <div class="flex items-center gap-3 text-sm text-slate-500 mt-1">
                  <span class="flex items-center gap-1">👥 {{ study.memberCount }}명</span>
                  <span class="text-slate-300">|</span>
                  <span :class="getTierClass(study.tierBadge)">{{ study.tierBadge }} Avg</span>
                </div>
                <p v-if="study.description" class="text-xs text-slate-400 mt-1 truncate">{{ study.description }}</p>
              </div>

              <!-- 지표 -->
              <div class="col-span-6 md:col-span-3">
                 <div class="text-xs text-slate-400 mb-1">최근 활동 (주간 인당)</div>
                 <div class="font-bold text-slate-700">{{ (study.averageSubmissionRate || 0).toFixed(1) }} 문제</div>
              </div>

              <div class="col-span-6 md:col-span-2">
                 <div class="text-xs text-slate-400 mb-1">스트릭</div>
                 <div class="font-bold text-orange-500 flex items-center gap-1">
                   🔥 {{ study.streak || 0 }}일
                 </div>
              </div>

              <!-- 버튼 -->
              <div class="col-span-12 md:col-span-2 text-right">
                <button v-if="!user?.studyId" 
                        @click="openApplyModal(study)"
                        class="w-full md:w-auto px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-bold rounded-xl transition-all shadow-md shadow-indigo-200">
                  신청하기
                </button>
                <span v-else-if="user.studyId === study.id" class="text-sm font-bold text-indigo-600 bg-indigo-50 px-3 py-1 rounded-lg">
                  내 스터디
                </span>
                <button v-else disabled class="w-full md:w-auto px-4 py-2 bg-slate-100 text-slate-400 text-sm font-bold rounded-xl cursor-not-allowed">
                  가입 불가
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-else-if="!loading" class="text-center py-20 text-slate-400 text-xl">
        등록된 스터디가 없습니다
      </div>

    </div>

    <!-- 가입 신청 모달 -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="closeModal"></div>
      <div class="bg-white rounded-3xl p-6 w-full max-w-md relative z-10 shadow-2xl animate-fade-in-up">
        <h3 class="text-xl font-bold text-slate-800 mb-2">🚀 스터디 가입 신청</h3>
        <p class="text-slate-500 text-sm mb-4">
          <span class="font-bold text-indigo-600">{{ selectedStudy?.name }}</span> 스터디에 합류하고 싶으신가요?<br>
          스터디장에게 간단한 메시지를 남겨주세요.
        </p>
        
        <textarea 
          v-model="applyMessage"
          class="w-full h-32 bg-slate-50 border border-slate-200 rounded-xl p-4 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20 resize-none mb-4 font-medium"
          placeholder="자기소개나 각오를 적어주세요!"
        ></textarea>

        <div class="flex gap-3">
          <button @click="closeModal" class="flex-1 py-3 bg-slate-100 text-slate-600 font-bold rounded-xl hover:bg-slate-200 transition-colors">취소</button>
          <button @click="submitApplication" 
                  :disabled="applying || !applyMessage"
                  class="flex-1 py-3 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-500 transition-colors disabled:opacity-50">
             {{ applying ? '전송 중...' : '신청서 보내기' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useAuth } from '../composables/useAuth';

const { user } = useAuth();
const loading = ref(true);
const studies = ref([]);
const showModal = ref(false);
const selectedStudy = ref(null);
const applyMessage = ref('');
const applying = ref(false);

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

const openApplyModal = (study) => {
  selectedStudy.value = study;
  applyMessage.value = '';
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  selectedStudy.value = null;
};

const submitApplication = async () => {
  if (!selectedStudy.value) return;
  
  applying.value = true;
  try {
    await axios.post(`/api/studies/${selectedStudy.value.id}/apply`, {
      message: applyMessage.value
    });
    alert('가입 신청이 전송되었습니다! 스터디장의 승인을 기다려주세요.');
    closeModal();
  } catch (e) {
    console.error('신청 실패', e);
    alert('이미 신청했거나 오류가 발생했습니다.');
  } finally {
    applying.value = false;
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
