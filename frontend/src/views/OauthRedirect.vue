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

      <div v-else-if="user" class="flex flex-col py-6 space-y-6">
        <div v-if="isSignUp" class="text-left space-y-6">
          <div class="space-y-2">
            <h2 class="text-2xl font-bold text-white">
              첫 가입을 환영합니다, {{ user.username || user.email }}님!
            </h2>
            <p class="text-sm text-slate-300 leading-relaxed">
              깃허브 익스텐션을 설치하고, 저장소를 등록해 주세요.<br />
              저장이 완료되면 대시보드로 안내해 드릴게요.
            </p>
          </div>

          <div
            class="bg-slate-900/40 border border-white/10 rounded-xl p-4 space-y-3"
          >
            <h3 class="text-sm font-semibold text-indigo-300">
              온보딩 체크리스트
            </h3>
            <ol
              class="list-decimal list-inside text-sm text-slate-300 space-y-1"
            >
              <li>
                마켓에서
                <span class="font-medium text-indigo-300">BaekjoonHub Dash</span
                >를 설치합니다.
              </li>
              <li>Webhook을 설정할 리포지토리를 입력합니다.</li>
              <li>등록 후에는 즉시 웹훅 생성 프로세스를 시작합니다.</li>
            </ol>
          </div>

          <form class="space-y-4" @submit.prevent="submitRepository">
            <label class="block text-left">
              <span class="text-sm font-semibold text-slate-200"
                >깃허브 저장소</span
              >
              <input
                v-model="repositoryName"
                type="text"
                placeholder="owner/repository"
                class="mt-2 w-full rounded-xl bg-slate-900/60 border border-white/10 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                :disabled="saving || success"
              />
            </label>

            <p v-if="submitError" class="text-sm text-red-400 whitespace-pre-line">
              {{ submitError }}
            </p>

            <div class="flex flex-col gap-2">
              <button
                type="submit"
                class="w-full px-4 py-3 rounded-xl bg-indigo-500 hover:bg-indigo-400 transition-colors text-white text-sm font-semibold disabled:opacity-60"
                :disabled="saving || success"
              >
                {{ saving ? "저장 중..." : "저장소 연결하기" }}
              </button>
              <button
                type="button"
                class="w-full px-4 py-3 rounded-xl bg-slate-800/70 hover:bg-slate-700 transition-colors text-slate-200 text-sm"
                @click="skipOnboarding"
                :disabled="saving"
              >
                나중에 설정할게요
              </button>
            </div>
          </form>

          <div
            v-if="success"
            class="rounded-xl bg-green-500/10 border border-green-500/30 p-4 text-sm text-green-300"
          >
            곧 대시보드로 이동합니다...
          </div>
        </div>

        <div v-else class="flex flex-col items-center justify-center space-y-4">
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

          <div class="text-center">
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { authApi } from "../api/auth";
import { onboardingApi } from "../api/onboarding";

const loading = ref(true);
const error = ref(null);
const user = ref(null);
const flowType = ref(null);
const repositoryName = ref("");
const submitError = ref(null);
const saving = ref(false);
const success = ref(false);
const isSignUp = computed(() => flowType.value === "SIGN_UP");

const scheduleRedirect = () => {
  setTimeout(() => {
    window.location.href = "/";
  }, 1200);
};

onMounted(async () => {
  try {
    // Artificial delay tailored to match original UX
    const [res] = await Promise.all([
      authApi.getSession(),
      new Promise((resolve) => setTimeout(resolve, 600)),
    ]);

    const data = res.data;
    user.value = data.user;
    flowType.value = data.flowType;

    if (data.flowType === "LOGIN") {
      scheduleRedirect();
    }
  } catch (err) {
    error.value = err.message || "로그인 처리 중 오류가 발생했습니다.";
  } finally {
    loading.value = false;
  }
});

const goHome = () => {
  window.location.href = "/";
};

const skipOnboarding = () => {
  goHome();
};

const submitRepository = async () => {
  if (!repositoryName.value.trim()) {
    submitError.value = "저장소 이름을 입력해주세요.";
    return;
  }

  saving.value = true;
  submitError.value = null;
  try {
    const res = await onboardingApi.submitRepository(repositoryName.value.trim());

    const data = res.data;
    repositoryName.value = data.repositoryName;
    success.value = !!data.webhookConfigured;

    if (success.value) {
      scheduleRedirect();
    } else {
      submitError.value =
        "웹훅이 아직 활성화되지 않았습니다.\n잠시 후 다시 시도하거나 관리자에게 문의해주세요.";
    }
  } catch (err) {
    submitError.value = err.message || "저장소 정보를 저장하지 못했습니다.";
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
/* 스피너 등 추가적인 CSS 영역 */
</style>
