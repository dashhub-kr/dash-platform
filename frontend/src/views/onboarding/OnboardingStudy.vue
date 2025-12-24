<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
// import { studyApi } from '../../api/study'; // Assuming this exists or using axios
import axios from 'axios';
import { studyApi } from '../../api/study';

const router = useRouter();
const newStudyName = ref('');
const newBenefit = ref('');
const creating = ref(false);
const pendingApplication = ref(null);
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await studyApi.getMyApplication();
    if (res.status === 200 && res.data) {
      pendingApplication.value = res.data;
    }
  } catch (e) {
    // console.log("No pending application");
  } finally {
    loading.value = false;
  }
});

const goExplore = () => {
  router.push('/study/ranking');
};

const cancelApp = async () => {
  if (!confirm('가입 신청을 취소하시겠습니까?')) return;
  try {
    await studyApi.cancelApplication(pendingApplication.value.id);
    pendingApplication.value = null;
    alert('취소되었습니다. 다른 스터디를 찾아보세요!');
  } catch (e) {
    alert('취소 실패');
  }
};

const createStudy = async () => {
  if (!newStudyName.value) return;
  
  creating.value = true;
  try {
    const res = await axios.post('/api/studies', { name: newStudyName.value, description: newBenefit.value });
    // Success -> Navigate
    router.push('/onboarding/repo');
  } catch (error) {
    alert('스터디 생성 실패: ' + (error.response?.status === 401 ? '로그인이 필요합니다' : '오류가 발생했습니다'));
  } finally {
    creating.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 relative overflow-hidden font-[Pretendard] flex items-center justify-center">
    
    <!-- Background Decor -->
    <div class="absolute top-[-10%] right-[-5%] w-[40vw] h-[40vw] bg-cyan-200/40 rounded-full blur-[100px] animate-blob mix-blend-multiply"></div>
    <div class="absolute bottom-[-10%] left-[-5%] w-[40vw] h-[40vw] bg-indigo-200/40 rounded-full blur-[100px] animate-blob animation-delay-2000 mix-blend-multiply"></div>

    <div v-if="!loading" class="max-w-5xl w-full relative z-10 animate-fade-in-up">
      
      <!-- Pending State -->
      <div v-if="pendingApplication" class="max-w-xl mx-auto text-center bg-white/80 backdrop-blur rounded-3xl p-10 shadow-2xl border border-white/60">
         <div class="w-20 h-20 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-6 text-4xl animate-bounce">
            ⏳
         </div>
         <h2 class="text-3xl font-bold text-slate-900 mb-4">가입 승인 대기 중</h2>
         <p class="text-slate-500 mb-8 text-lg">
            스터디장의 승인을 기다리고 있습니다.<br>
            조금만 기다려주세요!
         </p>
         
         <div class="bg-indigo-50 rounded-xl p-6 mb-8 text-left">
            <div class="text-xs font-bold text-indigo-500 uppercase tracking-wider mb-2">신청 메시지</div>
            <p class="text-slate-700 font-medium">"{{ pendingApplication.message }}"</p>
            <div class="text-xs text-slate-400 mt-4 text-right">{{ new Date(pendingApplication.createdAt).toLocaleDateString() }} 신청</div>
         </div>

         <div class="flex flex-col gap-3">
            <button @click="goExplore" class="w-full py-3.5 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl transition-all">
               다른 스터디 둘러보기
            </button>
            <button @click="cancelApp" class="w-full py-3.5 bg-white border border-slate-200 hover:bg-red-50 hover:text-red-500 text-slate-500 font-bold rounded-xl transition-all">
               신청 취소하기
            </button>
         </div>
      </div>

      <!-- Normal State -->
      <div v-else>
          <!-- Header -->
          <div class="text-center mb-12">
            <h1 class="text-4xl font-black bg-gradient-to-r from-indigo-600 to-cyan-500 bg-clip-text text-transparent mb-4">
              여정의 시작
            </h1>
            <p class="text-slate-500 text-lg">혼자 가면 빨리 가지만, 함께 가면 멀리 갑니다.<br>어떤 방식으로 참여하시겠습니까?</p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <!-- Create New Study -->
            <div class="group relative bg-white/80 backdrop-blur border border-white/60 rounded-3xl p-8 shadow-xl shadow-indigo-500/10 hover:shadow-indigo-500/20 transition-all hover:-translate-y-1">
              <div class="absolute inset-0 bg-gradient-to-br from-indigo-50/50 to-transparent rounded-3xl opacity-0 group-hover:opacity-100 transition-opacity"></div>
              <div class="relative z-10">
                <div class="w-16 h-16 bg-indigo-100 rounded-2xl flex items-center justify-center text-3xl mb-6">✨</div>
                <h2 class="text-2xl font-bold text-slate-900 mb-2">새 스터디 만들기</h2>
                <p class="text-slate-500 mb-8 h-12">나만의 스터디를 만들고<br>리더가 되어 팀을 이끌어보세요.</p>
                
                <form @submit.prevent="createStudy" class="space-y-4">
                  <div>
                    <input 
                      v-model="newStudyName"
                      type="text" 
                      placeholder="스터디 이름"
                      class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium"
                      required
                    />
                  </div>
                  <div>
                     <input 
                      v-model="newBenefit"
                      type="text" 
                      placeholder="한 줄 소개 (선택)"
                      class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium"
                    />
                  </div>
                  <button 
                    type="submit"
                    class="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-3.5 rounded-xl transition-all flex items-center justify-center gap-2 mt-2"
                    :disabled="creating || !newStudyName"
                  >
                    <span v-if="creating" class="animate-spin">⌛</span>
                    <span>스터디 생성하기</span>
                  </button>
                </form>
              </div>
            </div>

            <!-- Find Study -->
            <div class="group relative bg-white/80 backdrop-blur border border-white/60 rounded-3xl p-8 shadow-xl shadow-cyan-500/10 hover:shadow-cyan-500/20 transition-all hover:-translate-y-1 cursor-pointer"
                 @click="goExplore">
              <div class="absolute inset-0 bg-gradient-to-br from-cyan-50/50 to-transparent rounded-3xl opacity-0 group-hover:opacity-100 transition-opacity"></div>
              <div class="relative z-10 h-full flex flex-col">
                <div class="w-16 h-16 bg-cyan-100 rounded-2xl flex items-center justify-center text-3xl mb-6">🔭</div>
                <h2 class="text-2xl font-bold text-slate-900 mb-2">스터디 찾아보기</h2>
                <p class="text-slate-500 mb-8">이미 활동 중인 스터디를 찾아<br>동료가 되어보세요.</p>
                
                <div class="mt-auto">
                  <div class="w-full bg-cyan-500 hover:bg-cyan-400 text-white font-bold py-3.5 rounded-xl transition-all flex items-center justify-center gap-2">
                    <span>둘러보러 가기 →</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

.animate-blob {
  animation: blob 10s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}
</style>
