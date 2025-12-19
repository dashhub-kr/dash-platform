<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50 text-slate-800 p-6 relative overflow-hidden font-[Pretendard]">
    
    <!-- Background Decor -->
    <div class="absolute top-[-10%] right-[-5%] w-[40vw] h-[40vw] bg-indigo-200/40 rounded-full blur-[100px] animate-blob mix-blend-multiply"></div>
    <div class="absolute bottom-[-10%] left-[-5%] w-[40vw] h-[40vw] bg-sky-200/40 rounded-full blur-[100px] animate-blob animation-delay-2000 mix-blend-multiply"></div>

    <div class="max-w-md w-full space-y-8 animate-fade-in-up relative z-10">
      <div class="text-center space-y-2">
        <h1 class="text-4xl font-extrabold tracking-tight bg-gradient-to-r from-indigo-600 to-cyan-500 bg-clip-text text-transparent">
          Solved.ac 연동
        </h1>
        <p class="text-slate-500 font-medium">
          알고리즘 실력을 분석하기 위해 아이디를 입력해주세요.
        </p>
      </div>

      <div class="bg-white/80 border border-white/60 rounded-3xl p-8 shadow-xl shadow-indigo-500/10 backdrop-blur-md">
        <form @submit.prevent="submitHandle" class="space-y-6">
          <div class="space-y-2">
            <label class="text-sm font-bold text-slate-700 ml-1">Solved.ac ID</label>
            <input
              v-model="handle"
              type="text"
              placeholder="백준 핸들을 입력하세요"
              class="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-4 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium"
              :disabled="loading"
              required
            />
          </div>

          <button
            type="submit"
            class="w-full bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 text-white font-bold py-4 rounded-2xl transition-all shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/40 hover:-translate-y-0.5 disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none flex items-center justify-center gap-2"
            :disabled="loading || !handle"
          >
            <svg v-if="loading" class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>{{ loading ? '연동 중...' : '다음으로' }}</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { onboardingApi } from '../../api/onboarding';

const router = useRouter();
const handle = ref('');
const loading = ref(false);

const submitHandle = async () => {
  if (!handle.value) return;
  
  loading.value = true;
  try {
    await onboardingApi.registerSolvedac(handle.value);
    router.push('/onboarding/analysis');
  } catch (error) {
    console.error(error);
    alert('아이디 연동에 실패했습니다. 올바른 아이디인지 확인해주세요.');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

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
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
</style>
