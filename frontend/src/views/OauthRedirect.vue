<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50 relative overflow-hidden">
    <!-- Decorative Background Elements -->
    <div class="absolute top-[-10%] left-[-10%] w-[500px] h-[500px] rounded-full bg-indigo-200/30 blur-3xl animate-pulse-slow"></div>
    <div class="absolute bottom-[-10%] right-[-10%] w-[500px] h-[500px] rounded-full bg-purple-200/30 blur-3xl animate-pulse-slow animation-delay-1000"></div>

    <div class="relative z-10 w-full max-w-md p-8">
      
      <!-- Loading State -->
      <div v-if="!error" class="flex flex-col items-center text-center space-y-8 animate-fade-in-up">
        <div class="relative w-24 h-24">
           <!-- Outer Ring -->
           <div class="absolute inset-0 rounded-full border-4 border-slate-100"></div>
           <!-- Spinning Segment -->
           <div class="absolute inset-0 rounded-full border-4 border-indigo-500 border-t-transparent animate-spin"></div>
           <!-- Inner Icon -->
           <div class="absolute inset-0 flex items-center justify-center text-indigo-500">
             <Compass :size="40" class="animate-pulse" />
           </div>
        </div>

        <div class="space-y-2">
            <h2 class="text-2xl font-black text-slate-800">
              <span v-if="isSignUp">새로운 모험을 준비하고 있어요!</span>
              <span v-else>알고리즘의 바다로 이동 중...</span>
            </h2>
            <p class="text-slate-500 font-medium">잠시만 기다려주세요, 선장님!</p>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="bg-white rounded-3xl p-8 shadow-xl border border-red-100 text-center animate-shake">
        <div class="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4 text-red-500">
            <Ghost :size="32" />
        </div>
        <h2 class="text-xl font-bold text-slate-800 mb-2">항해를 시작할 수 없습니다</h2>
        <p class="text-sm text-slate-500 mb-6">{{ error }}</p>
        <button 
          @click="goHome" 
          class="w-full py-3.5 rounded-xl bg-slate-900 text-white font-bold hover:bg-slate-800 transition-all active:scale-95 shadow-lg shadow-slate-200"
        >
          홈으로 돌아가기
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { authApi } from "../api/auth";
import { Compass, Ghost } from "lucide-vue-next";

const router = useRouter();
const error = ref(null);
const flowType = ref(null);
const isSignUp = computed(() => flowType.value === "SIGN_UP");

const goHome = () => {
  window.location.href = "/";
};

onMounted(async () => {
  try {
    const res = await authApi.getSession();
    const data = res.data;
    flowType.value = data.flowType;

    setTimeout(() => {
        if (data.flowType === "SIGN_UP") {
            router.push("/onboarding/welcome");
        } else {
            window.location.href = "/";
        }
    }, 1500); // Slightly longer delay to show off the animation
    
  } catch (err) {
    error.value = err.message || "로그인 처리 중 오류가 발생했습니다.";
  }
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

* {
  font-family: 'Pretendard', sans-serif;
}

.animate-pulse-slow {
    animation: pulse 4s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.animation-delay-1000 {
    animation-delay: 1000ms;
}

.animate-fade-in-up {
    animation: fadeInUp 0.6s ease-out forwards;
    opacity: 0;
    transform: translateY(20px);
}

@keyframes fadeInUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.animate-shake {
    animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
