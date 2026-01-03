<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 flex flex-col items-center justify-center relative overflow-hidden">
    
    <!-- Decorative Background -->
    <div class="absolute bottom-0 right-0 w-[800px] h-[800px] bg-emerald-50/50 rounded-full blur-[120px] pointer-events-none"></div>

    <div class="w-full max-w-6xl relative z-10 animate-fade-in">
      
      <!-- Header -->
      <div v-if="!isExploring" class="text-center mb-12 space-y-4">
        <span class="inline-block px-3 py-1 bg-brand-50 text-brand-600 rounded-full text-xs font-bold tracking-wider">STEP 03</span>
        <h1 class="text-4xl md:text-5xl font-black text-slate-900 tracking-tight">
          함께 성장할 <span class="text-brand-600">숲</span>을 찾으세요
        </h1>
        <p class="text-lg text-slate-500 font-medium max-w-2xl mx-auto">
          혼자서는 막막한 알고리즘, 동료들과 함께라면 더 멀리 갈 수 있습니다.<br>
          기존 스터디에 참여하거나 새로운 스터디를 만들어보세요.
        </p>
      </div>

      <!-- Main Options (Split View) -->
      <div v-if="!isExploring && !creating" class="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
        
        <!-- Option 1: Create Study -->
        <div 
          class="group relative bg-white/70 backdrop-blur-md border border-white rounded-3xl p-8 shadow-xl shadow-brand-500/5 hover:-translate-y-2 hover:shadow-brand-500/15 transition-all duration-300 cursor-pointer overflow-hidden"
          @click="showCreateForm"
        >
          <div class="absolute inset-0 bg-gradient-to-br from-brand-500/0 via-brand-500/0 to-brand-500/5 group-hover:to-brand-500/10 transition-colors"></div>
          
          <div class="relative z-10 flex flex-col h-full">
            <div class="w-16 h-16 bg-brand-100 rounded-2xl flex items-center justify-center mb-6 shadow-inner">
               <span class="text-3xl">🌱</span>
            </div>
            <h3 class="text-2xl font-bold text-slate-900 mb-2">새 스터디 만들기</h3>
            <p class="text-slate-500 font-medium mb-8">
              직접 리더가 되어 나만의 숲을 가꿔보세요.<br>
              스터디 규칙과 방향성을 직접 설정할 수 있습니다.
            </p>
            <div class="mt-auto">
               <span class="inline-flex items-center text-brand-600 font-bold group-hover:gap-2 transition-all">
                 생성하기 <ArrowRight class="w-4 h-4 ml-1" />
               </span>
            </div>
          </div>
        </div>

        <!-- Option 2: Explore Studies -->
        <div 
          class="group relative bg-white/70 backdrop-blur-md border border-white rounded-3xl p-8 shadow-xl shadow-cyan-500/5 hover:-translate-y-2 hover:shadow-cyan-500/15 transition-all duration-300 cursor-pointer overflow-hidden"
          @click="startExploring"
        >
          <div class="absolute inset-0 bg-gradient-to-br from-cyan-500/0 via-cyan-500/0 to-cyan-500/5 group-hover:to-cyan-500/10 transition-colors"></div>
          
          <div class="relative z-10 flex flex-col h-full">
            <div class="w-16 h-16 bg-cyan-100 rounded-2xl flex items-center justify-center mb-6 shadow-inner">
               <span class="text-3xl">🔭</span>
            </div>
            <h3 class="text-2xl font-bold text-slate-900 mb-2">스터디 찾아보기</h3>
            <p class="text-slate-500 font-medium mb-8">
              이미 활성화된 스터디에 합류하세요.<br>
              다양한 주제와 난이도의 스터디가 기다리고 있습니다.
            </p>
            <div class="mt-auto">
               <span class="inline-flex items-center text-cyan-600 font-bold group-hover:gap-2 transition-all">
                 탐색하기 <ArrowRight class="w-4 h-4 ml-1" />
               </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Form Overlay -->
      <transition name="fade">
        <div v-if="creating" class="max-w-md mx-auto">
           <div class="bg-white rounded-3xl p-8 shadow-2xl border border-slate-100">
              <button @click="cancelCreate" class="text-sm text-slate-400 font-bold hover:text-slate-600 mb-6 flex items-center gap-1">
                 <ArrowLeft class="w-4 h-4" /> 뒤로가기
              </button>
              
              <h2 class="text-2xl font-black text-slate-900 mb-6">스터디 생성</h2>
              
              <form @submit.prevent="submitCreate" class="space-y-5">
                 <div class="space-y-1">
                    <label class="text-sm font-bold text-slate-700 ml-1">스터디 이름</label>
                    <input 
                      v-model="newStudyName"
                      type="text" 
                      placeholder="예: 아침 9시 알고리즘"
                      class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 font-medium focus:bg-white focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all outline-none"
                      required
                    />
                 </div>
                 <div class="space-y-1">
                    <label class="text-sm font-bold text-slate-700 ml-1">한 줄 소개</label>
                    <input 
                      v-model="newDescription"
                      type="text" 
                      placeholder="예: 골드 달성까지 함께 달려요!"
                      class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 font-medium focus:bg-white focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all outline-none"
                    />
                 </div>
                 
                 <button 
                  type="submit"
                  class="w-full bg-slate-900 text-white font-bold py-4 rounded-xl shadow-lg shadow-slate-900/10 hover:shadow-slate-900/20 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center justify-center gap-2 mt-4"
                  :disabled="loading"
                 >
                    <Loader2 v-if="loading" class="animate-spin w-5 h-5" />
                    <span>생성하고 시작하기</span>
                 </button>
              </form>
           </div>
        </div>
      </transition>

      <!-- Explorer View -->
      <transition name="slide-up">
        <div v-if="isExploring" class="relative h-[80vh] flex flex-col">
            <div class="flex items-center justify-between mb-6 px-4">
               <button @click="backToMain" class="flex items-center gap-2 text-slate-500 hover:text-slate-900 font-bold transition-colors bg-white px-4 py-2 rounded-xl shadow-sm border border-slate-100">
                   <ArrowLeft class="w-4 h-4" /> 처음으로
               </button>
            </div>
            
            <div class="bg-white/80 backdrop-blur-xl border border-white/60 rounded-3xl p-6 shadow-2xl overflow-hidden flex-1 relative">
                <!-- Use existing StudyExplorer but customized if needed via props -->
                <StudyExplorer :is-onboarding="true" @apply-success="onStudyJoined" />
            </div>
        </div>
      </transition>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ArrowLeft, ArrowRight, Loader2 } from 'lucide-vue-next';
import StudyExplorer from '@/components/study/StudyExplorer.vue'; // Reuse existing component
import axios from 'axios';
import { useAuth } from '@/composables/useAuth';

const emit = defineEmits(['next']);
const { refresh } = useAuth();

const isExploring = ref(false);
const creating = ref(false);
const loading = ref(false);

const newStudyName = ref('');
const newDescription = ref('');

const showCreateForm = () => {
  creating.value = true;
};

const cancelCreate = () => {
  creating.value = false;
  newStudyName.value = '';
  newDescription.value = '';
};

const startExploring = () => {
  isExploring.value = true;
};

const backToMain = () => {
  isExploring.value = false;
};

const submitCreate = async () => {
  if (!newStudyName.value) return;
  
  loading.value = true;
  try {
    const res = await axios.post('/api/studies', { 
        name: newStudyName.value, 
        description: newDescription.value 
    });
    
    // Refresh auth user to get studyId
    await refresh();
    emit('next');
  } catch (error) {
    alert('스터디 생성 실패: ' + (error.response?.data?.message || '오류가 발생했습니다.'));
  } finally {
    loading.value = false;
  }
};

const onStudyJoined = async () => {
  // StudyExplorer handles the API call. We just need to wait a bit and move on.
  // Ideally StudyExplorer emits when application is done.
  // If it requires approval, users might need to wait.
  // For MVP/Renewal, assuming auto-join or simple application -> Next step might be blocked until approval?
  // Let's assume for now valid joins allow proceeding or we handle "Pending" state in this component if we had time.
  // Based on proposal: "Step 3: ... (Study Check) -> Step 4"
  // If user *applied* but not *approved*, they technically haven't finished this step if Step 4 requires a Study ID for the repo to link to?
  // Actually, repo linking (Step 5) needs a Study ID? Usually yes for "Study Dashboard".
  // So we accept "Pending" as "Done with Step 3" BUT Step 4 Guide might say "Wait for approval"?
  // Or we just proceed. Let's proceed.
  
  await refresh();
  emit('next');
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}

/* Transitions */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
