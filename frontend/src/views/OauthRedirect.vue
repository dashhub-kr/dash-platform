<template>
  <div
    class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-950 to-slate-900 text-slate-100 p-6"
  >
    <div
      class="max-w-md w-full bg-white/5 backdrop-blur-xl border border-white/10 rounded-2xl p-8 text-center shadow-2xl transform transition-all duration-500"
    >
      <div
        v-if="loading"
        class="flex flex-col items-center justify-center py-6 space-y-4"
      >
        <div class="relative w-16 h-16">
          <div
            class="absolute inset-0 border-4 border-indigo-500/30 rounded-full"
          ></div>
          <div
            class="absolute inset-0 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"
          ></div>
        </div>
        <h2 class="text-lg font-medium text-slate-200 animate-pulse">
          로그인 정보를 확인하고 있습니다...
        </h2>
      </div>

      <div
        v-else-if="error"
        class="flex flex-col items-center justify-center py-4 space-y-4"
      >
        <div
          class="w-14 h-14 rounded-full bg-red-500/20 flex items-center justify-center text-red-400 mb-2"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-8 w-8"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-red-400">로그인 실패</h2>
        <p class="text-sm text-slate-400">{{ error }}</p>
        <button
          @click="goHome"
          class="mt-4 px-6 py-2 rounded-full bg-slate-700 hover:bg-slate-600 transition-colors text-white text-sm"
        >
          홈으로 돌아가기
        </button>
      </div>

      <div
        v-else-if="user"
        class="flex flex-col items-center justify-center py-6 space-y-4"
      >
        <div
          class="w-16 h-16 rounded-full bg-green-500/20 flex items-center justify-center text-green-400 mb-2 scale-100 animate-[bounce_1s_ease-in-out_1]"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-8 w-8"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="3"
              d="M5 13l4 4L19 7"
            />
          </svg>
        </div>

        <div>
          <h2 class="text-2xl font-bold text-white mb-1">반갑습니다!</h2>
          <p class="text-slate-300">
            <span class="font-semibold text-indigo-400">{{
              user.username || user.email
            }}</span
            >님으로 로그인되었습니다.
          </p>
        </div>

        <p class="text-xs text-slate-500 mt-4">
          잠시 후 메인 페이지로 이동합니다...
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

const loading = ref(true);
const error = ref(null);
const user = ref(null);

onMounted(async () => {
  try {
    const [res] = await Promise.all([
      fetch(`/api/users/me`, { credentials: "include" }),
      new Promise((resolve) => setTimeout(resolve, 600)),
    ]);

    if (!res.ok) {
      throw new Error(`사용자 정보 조회 실패 (${res.status})`);
    }
    user.value = await res.json();

    setTimeout(() => {
      window.location.href = "/";
    }, 1200);
  } catch (err) {
    error.value = err.message || "로그인 처리 중 오류가 발생했습니다.";
  } finally {
    loading.value = false;
  }
});

const goHome = () => {
  window.location.href = "/";
};
</script>

<style scoped>
/* 스피너 등 추가적인 CSS 영역 */
</style>
