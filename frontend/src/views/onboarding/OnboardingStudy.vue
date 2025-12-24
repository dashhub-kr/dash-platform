<template>
  <div class="min-h-screen bg-white text-slate-800 p-6">

    <div class="max-w-4xl mx-auto py-8 space-y-8 relative z-10 animate-fade-in-up">
      <!-- Header -->
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-extrabold bg-gradient-to-r from-brand-600 to-cyan-500 bg-clip-text text-transparent">
          🤝 스터디 매칭
        </h1>
        <p class="text-slate-500 font-medium">함께 성장할 동료를 찾거나 새로운 여정을 시작하세요.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- Join Existing Study -->
        <div class="bg-white/80 backdrop-blur border border-white/60 rounded-3xl p-6 shadow-xl shadow-brand-500/10">
          <h2 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
            <BookOpen class="w-8 h-8 p-1.5 bg-brand-100 rounded-lg text-brand-600" />
            추천 스터디
          </h2>
          
          <div class="space-y-3 max-h-[350px] overflow-y-auto pr-2 custom-scrollbar">
            <div 
              v-for="study in studies" 
              :key="study.id"
              class="group p-4 rounded-2xl bg-slate-50 border border-slate-100 hover:border-brand-400 hover:bg-brand-50 transition-all cursor-pointer flex justify-between items-center"
              @click="joinStudy(study.id)"
            >
              <div>
                <h3 class="font-bold text-slate-800 group-hover:text-brand-600 transition-colors">{{ study.name }}</h3>
                <p class="text-sm text-slate-500 mt-1">
                   <span class="inline-flex items-center gap-1">
                    <Users :size="14"/> {{ study.memberCount || 0 }}명
                  </span>
                </p>
              </div>
              <div class="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center text-slate-400 group-hover:bg-brand-500 group-hover:text-white transition-all shadow-sm">
                <ChevronRight :size="20"/>
              </div>
            </div>
            
            <div v-if="studies.length === 0" class="p-8 text-center text-slate-500 bg-slate-50 rounded-2xl border border-dashed border-slate-300">
              <Search class="w-10 h-10 mx-auto mb-3 opacity-50"/>
              <p class="font-medium">참여 가능한 스터디가 없습니다.</p>
              <p class="text-sm text-slate-400 mt-1">새로운 스터디를 만들어보세요!</p>
            </div>
          </div>
        </div>

        <!-- Create New Study -->
        <div class="bg-white/80 backdrop-blur border border-white/60 rounded-3xl p-6 shadow-xl shadow-brand-500/10">
          <h2 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
            <Sparkles class="w-8 h-8 p-1.5 bg-emerald-100 rounded-lg text-emerald-600"/>
            새 스터디 만들기
          </h2>
          
          <form @submit.prevent="createStudy" class="space-y-5">
            <div class="space-y-2">
              <label class="text-sm font-bold text-slate-700 ml-1">스터디 이름</label>
              <input 
                v-model="newStudyName"
                type="text" 
                placeholder="예: 알고리즘 정복원정대"
                class="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-4 focus:ring-brand-500/20 focus:border-brand-500 transition-all font-medium"
                required
              />
            </div>
            
            <button 
              type="submit"
              class="w-full bg-gradient-to-r from-brand-600 to-brand-500 hover:from-brand-500 hover:to-brand-400 text-white font-bold py-4 rounded-2xl transition-all shadow-lg shadow-brand-500/30 hover:shadow-brand-500/40 hover:-translate-y-0.5 disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none flex items-center justify-center gap-2"
              :disabled="creating || !newStudyName"
            >
              <Loader2 v-if="creating" class="animate-spin h-5 w-5 text-white" />
              <Rocket v-else :size="20"/>
              <span>{{ creating ? '생성 중...' : '스터디 생성하기' }}</span>
            </button>
          </form>

          <div class="mt-6 bg-brand-50 border border-brand-100 rounded-2xl p-4">
            <h4 class="font-bold text-brand-700 mb-2 text-sm flex items-center gap-2"><Lightbulb :size="14"/> 스터디장이 되면?</h4>
            <ul class="space-y-1.5 text-sm text-brand-600">
              <li class="flex items-center gap-2">
                <span class="w-1.5 h-1.5 bg-brand-400 rounded-full"></span>
                스터디 방향성을 설정할 수 있어요.
              </li>
              <li class="flex items-center gap-2">
                <span class="w-1.5 h-1.5 bg-brand-400 rounded-full"></span>
                멤버 관리 권한을 가집니다.
              </li>
              <li class="flex items-center gap-2">
                <span class="w-1.5 h-1.5 bg-brand-400 rounded-full"></span>
                목표와 마일스톤을 정할 수 있어요.
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Skip Option -->
      <div class="text-center pt-4">
        <button 
          @click="skipStudy"
          class="text-slate-400 hover:text-slate-600 text-sm font-medium transition-colors"
        >
          나중에 스터디 찾기 →
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { studyApi } from '../../api/study';
import { BookOpen, Users, Search, Sparkles, Lightbulb, Rocket, ChevronRight, Loader2 } from 'lucide-vue-next';

const router = useRouter();
const studies = ref([]);
const newStudyName = ref('');
const creating = ref(false);

const loadStudies = async () => {
  try {
    const res = await studyApi.getStudies();
    studies.value = res.data;
  } catch (error) {
    console.error('Failed to load studies', error);
  }
};

const joinStudy = async (id) => {
  try {
    await studyApi.joinStudy(id);
    router.push('/onboarding/repo');
  } catch (error) {
    alert('스터디 가입에 실패했습니다.');
  }
};

const createStudy = async () => {
  if (!newStudyName.value) return;
  
  creating.value = true;
  try {
    await studyApi.createStudy(newStudyName.value);
    router.push('/onboarding/repo');
  } catch (error) {
    alert('스터디 생성에 실패했습니다.');
  } finally {
    creating.value = false;
  }
};

const skipStudy = () => {
  router.push('/onboarding/repo');
};

onMounted(() => {
  loadStudies();
});
</script>

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
