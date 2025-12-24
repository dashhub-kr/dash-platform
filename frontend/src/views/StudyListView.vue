<template>
  <div class="min-h-screen bg-slate-50 font-[Pretendard] pb-20">
    <!-- 헤더 영역 -->
    <div class="bg-white border-b border-slate-200">
      <div class="max-w-7xl mx-auto px-6 py-12 text-center">
        <h1 class="text-4xl font-extrabold text-slate-900 mb-3 tracking-tight">스터디 둘러보기</h1>
        <p class="text-slate-500 text-lg">함께 성장할 동료들을 찾아보세요</p>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-6 py-10">
      
      <!-- 로딩 -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="w-12 h-12 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mb-4"></div>
        <p class="text-slate-400 font-medium animate-pulse">스터디 목록을 불러오는 중...</p>
      </div>

      <div v-else>
        <!-- Top 3 섹션 (데이터가 있을 때만) -->
        <div v-if="studies.length > 0" class="mb-16">
          <div class="flex items-center gap-2 mb-6 justify-center">
            <Trophy class="w-6 h-6 text-brand-500" fill="currentColor" />
            <h2 class="text-xl font-bold text-slate-800">Honor Board</h2>
          </div>
          
          <div class="flex flex-wrap justify-center items-end gap-6">
            <!-- 2위 -->
            <div v-if="studies[1]" class="flex flex-col items-center order-1 relative group cursor-pointer hover:-translate-y-2 transition-transform">
              <div class="absolute -top-8 text-4xl font-black text-slate-200 italic -z-10 group-hover:text-amber-100 transition-colors">2</div>
              <img src="/models/2nd.png" alt="2nd" class="w-24 h-24 md:w-28 md:h-28 object-contain drop-shadow-lg mb-3" />
              <div class="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-md min-w-[160px] relative">
                <div class="absolute -top-3 left-1/2 -translate-x-1/2 bg-slate-100 px-2 py-0.5 rounded-full text-[10px] font-bold text-slate-500">
                  RANK 2
                </div>
                <p class="font-bold text-slate-800 truncate mb-1">{{ studies[1].name }}</p>
                <div class="text-xs text-orange-500 font-bold flex items-center justify-center gap-1">
                  <Flame class="w-3 h-3" fill="currentColor" /> {{ studies[1].streak || 0 }}일
                </div>
              </div>
            </div>
            
            <!-- 1위 -->
            <div v-if="studies[0]" class="flex flex-col items-center order-0 md:order-1 relative group cursor-pointer hover:-translate-y-2 transition-transform z-10">
              <div class="absolute -top-12 text-6xl font-black text-amber-100 italic -z-10 bg-amber-50 rounded-full w-32 h-32 flex items-center justify-center blur-xl opacity-50"></div>
              <img src="/models/1st.png" alt="1st" class="w-32 h-32 md:w-40 md:h-40 object-contain drop-shadow-xl mb-4 animate-bounce-slow" />
              <div class="bg-gradient-to-br from-white to-amber-50 border border-amber-200 rounded-2xl p-5 text-center shadow-xl min-w-[200px] relative">
                 <div class="absolute -top-3 left-1/2 -translate-x-1/2 bg-amber-100 text-amber-700 px-3 py-0.5 rounded-full text-xs font-bold border border-amber-200">
                  RANK 1
                </div>
                <p class="text-lg font-bold text-slate-800 truncate mb-1">{{ studies[0].name }}</p>
                <div class="text-sm text-orange-600 font-bold flex items-center justify-center gap-1">
                  <Flame class="w-4 h-4" fill="currentColor" /> {{ studies[0].streak || 0 }}일 연속
                </div>
              </div>
            </div>
            
            <!-- 3위 -->
            <div v-if="studies[2]" class="flex flex-col items-center order-2 relative group cursor-pointer hover:-translate-y-2 transition-transform">
              <div class="absolute -top-8 text-4xl font-black text-slate-200 italic -z-10 group-hover:text-amber-100 transition-colors">3</div>
              <img src="/models/3rd.png" alt="3rd" class="w-20 h-20 md:w-24 md:h-24 object-contain drop-shadow-lg mb-3" />
               <div class="bg-white border border-slate-200 rounded-2xl p-4 text-center shadow-md min-w-[140px] relative">
                <div class="absolute -top-3 left-1/2 -translate-x-1/2 bg-slate-100 px-2 py-0.5 rounded-full text-[10px] font-bold text-slate-500">
                  RANK 3
                </div>
                <p class="text-sm font-bold text-slate-800 truncate mb-1">{{ studies[2].name }}</p>
                <div class="text-xs text-orange-500 font-bold flex items-center justify-center gap-1">
                  <Flame class="w-3 h-3" fill="currentColor" /> {{ studies[2].streak || 0 }}일
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 스터디 리스트 -->
        <div v-if="studies.length > 0">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-slate-800 flex items-center gap-2">
              <Search class="w-5 h-5 text-slate-400" fill="currentColor" />
              활동 중인 스터디
              <span class="text-base font-normal text-slate-400 ml-1">총 {{ studies.length }}개</span>
            </h2>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div v-for="(study, idx) in studies" :key="study.id" 
                 class="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all group relative overflow-hidden flex flex-col h-full">
              
              <!-- 랭킹 배지 (순서) -->
               <div class="absolute top-4 right-4 text-4xl font-black text-slate-100 italic pointer-events-none group-hover:text-brand-50 transition-colors">
                  {{ idx + 1 }}
               </div>

              <!-- 상단 정보 -->
              <div class="relative z-10 mb-4">
                 <div class="flex items-center gap-2 mb-3">
                   <span class="px-2.5 py-1 bg-slate-100 text-slate-600 text-xs font-bold rounded-lg border border-slate-200">
                    알고리즘
                   </span>
                   <span class="flex items-center gap-1 text-xs font-medium text-slate-500">
                      <Users class="w-3 h-3" /> {{ study.memberCount }}명
                   </span>
                 </div>
                 
                 <h3 class="text-xl font-bold text-slate-900 mb-2 truncate pr-8 group-hover:text-brand-600 transition-colors">
                   {{ study.name }}
                 </h3>
                 <p class="text-slate-500 text-sm line-clamp-2 h-10 leading-relaxed">
                   {{ study.description || '스터디 소개가 없습니다.' }}
                 </p>
              </div>

              <!-- 통계 정보 -->
              <div class="grid grid-cols-2 gap-3 mb-6 relative z-10">
                 <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100">
                    <div class="text-[10px] text-slate-400 font-bold mb-1 uppercase tracking-wider">Streak</div>
                    <div class="text-orange-600 font-bold flex items-center gap-1.5">
                       <Flame class="w-4 h-4 text-orange-500 fill-orange-100" /> {{ study.streak || 0 }}일
                    </div>
                 </div>
                 <div class="bg-slate-50 rounded-2xl p-3 border border-slate-100">
                    <div class="text-[10px] text-slate-400 font-bold mb-1 uppercase tracking-wider">Activity</div>
                    <div class="text-slate-700 font-bold flex items-center gap-1.5">
                       <Activity class="w-4 h-4 text-slate-400" />
                       {{ (study.averageSubmissionRate || 0).toFixed(1) }}문제
                    </div>
                 </div>
              </div>

               <!-- 하단 버튼 -->
               <div class="mt-auto relative z-10">
                  <!-- 내 스터디 -->
                  <div v-if="user?.studyId === study.id" 
                       class="w-full py-3 bg-brand-50 text-brand-600 font-bold rounded-xl text-center border border-brand-100 flex items-center justify-center gap-2">
                       <span class="w-2 h-2 rounded-full bg-brand-500 animate-pulse"></span>
                       참여 중
                  </div>

                  <!-- 가입 불가 (다른 스터디 참여 중) -->
                  <button v-else-if="user?.studyId" disabled
                          class="w-full py-3 bg-slate-100 text-slate-400 font-bold rounded-xl cursor-not-allowed border border-slate-200">
                     가입 불가
                  </button>

                  <!-- 신청하기 -->
                  <button v-else 
                          @click="openApplyModal(study)"
                          class="w-full py-3 bg-slate-900 hover:bg-brand-600 text-white font-bold rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 group-hover:scale-[1.02]">
                     가입 신청 <ArrowRight class="w-4 h-4 opacity-50 group-hover:opacity-100 transition-opacity" />
                  </button>
               </div>
            </div>
          </div>
        </div>

        <!-- 빈 상태 -->
        <div v-else class="text-center py-24 bg-white rounded-3xl border border-slate-200 shadow-sm">
          <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4">
             <Search class="w-8 h-8 text-slate-300" />
          </div>
          <p class="text-slate-500 font-medium text-lg mb-6">등록된 스터디가 없습니다</p>
          <button @click="$router.back()" class="px-6 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 font-bold rounded-xl transition-colors">
            뒤로가기
          </button>
        </div>
      </div>
    </div>

    <!-- 가입 신청 모달 -->
    <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" @click="closeModal"></div>
      <div class="bg-white rounded-3xl p-8 w-full max-w-lg relative z-10 shadow-2xl animate-fade-in-up transform transition-all scale-100">
        
        <div class="text-center mb-8">
           <div class="w-16 h-16 bg-brand-50 rounded-full flex items-center justify-center mx-auto mb-4 border-4 border-white shadow-lg">
              <Send class="w-8 h-8 text-brand-600 -ml-1 translate-y-0.5" />
           </div>
           <h3 class="text-2xl font-bold text-slate-900 mb-2">가입 신청 보내기</h3>
           <p class="text-slate-500">
             <span class="font-bold text-brand-600 bg-brand-50 px-2 py-0.5 rounded-md">{{ selectedStudy?.name }}</span> 
             스터디장에게 메시지를 남겨주세요
           </p>
        </div>
        
        <div class="space-y-4 mb-8">
           <label class="block text-sm font-bold text-slate-700">자기소개 및 각오</label>
           <textarea 
             v-model="applyMessage"
             class="w-full h-40 bg-slate-50 border border-slate-200 rounded-2xl p-5 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 resize-none font-medium transition-all"
             placeholder="안녕하세요! 매일 1문제씩 꾸준히 풀고 싶어서 신청합니다."
           ></textarea>
        </div>

        <div class="flex gap-3">
          <button @click="closeModal" class="flex-1 py-3.5 bg-white border border-slate-200 text-slate-600 font-bold rounded-xl hover:bg-slate-50 hover:text-slate-800 transition-colors">
             취소
          </button>
          <button @click="submitApplication" 
                  :disabled="applying || !applyMessage.trim()"
                  class="flex-1 py-3.5 bg-brand-600 text-white font-bold rounded-xl hover:bg-brand-700 transition-all shadow-lg shadow-brand-200 disabled:opacity-50 disabled:shadow-none flex items-center justify-center gap-2">
             <div v-if="applying" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
             <span v-else>신청하기</span>
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
import { Trophy, Flame, Users, Search, Activity, ArrowRight, Send } from 'lucide-vue-next';

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
    
    // 이미 신청한 경우 등 에러 처리
    if (e.response && e.response.status === 409) {
        alert('이미 가입 신청을 보냈거나, 가입된 상태입니다.');
    } else {
        alert('신청 중 오류가 발생했습니다.');
    }
  } finally {
    applying.value = false;
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

@keyframes bounce-slow {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.animate-bounce-slow {
  animation: bounce-slow 3s ease-in-out infinite;
}
</style>
