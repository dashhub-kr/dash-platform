<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-950 text-slate-100 p-6">
    <div class="max-w-md w-full space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-bold text-white">저장소 설정</h1>
        <p class="text-slate-400">마지막 단계입니다! 백준 풀이를 저장할 깃허브 저장소를 연결하세요.</p>
      </div>

      <div class="bg-white/5 border border-white/10 rounded-2xl p-6 shadow-2xl backdrop-blur-sm space-y-6">
        <div class="bg-indigo-500/10 border border-indigo-500/20 rounded-xl p-4 space-y-3">
          <h3 class="text-sm font-semibold text-indigo-300">체크리스트</h3>
          <ol class="list-decimal list-inside text-sm text-slate-300 space-y-1">
            <li>
                 <a href="https://chromewebstore.google.com/detail/kimjgflahdmnlhilmojcoaechlgkokhc?utm_source=item-share-cb" target="_blank" class="font-medium text-indigo-300 underline underline-offset-2 hover:text-indigo-200 transition-colors">BaekjoonHub Dash</a> 익스텐션을 설치합니다.
            </li>
            <li>연결할 저장소 이름을 입력합니다.</li>
            <li>Dash가 자동으로 Webhook을 설정합니다.</li>
          </ol>
        </div>

        <form class="space-y-4" @submit.prevent="submitRepository">
          <label class="block text-left">
            <span class="text-sm font-semibold text-slate-200">깃허브 저장소</span>
            <input
              v-model="repositoryName"
              type="text"
              placeholder="owner/repository"
              class="mt-2 w-full rounded-xl bg-slate-900/60 border border-white/10 px-4 py-3 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-500 font-mono"
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
              {{ saving ? "저장 중..." : "저장소 연결하고 시작하기" }}
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

        <div v-if="success" class="flex items-center gap-3 p-4 bg-green-500/10 border border-green-500/20 rounded-xl">
           <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
           </svg>
           <span class="text-sm text-green-300">설정이 완료되었습니다! 잠시 후 대시보드로 이동합니다.</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { authApi } from "../../api/auth";
import { onboardingApi } from "../../api/onboarding";

const router = useRouter();
const repositoryName = ref("");
const submitError = ref(null);
const saving = ref(false);
const success = ref(false);

const scheduleRedirect = () => {
  setTimeout(() => {
    window.location.href = "/dashboard";
  }, 1500);
};

const skipOnboarding = () => {
  window.location.href = "/dashboard";
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
      submitError.value = "웹훅이 아직 활성화되지 않았습니다.\n잠시 후 다시 시도하거나 관리자에게 문의해주세요.";
    }
  } catch (err) {
    submitError.value = err.message || "저장소 정보를 저장하지 못했습니다.";
  } finally {
    saving.value = false;
  }
};
</script>
