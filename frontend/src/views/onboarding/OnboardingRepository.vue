<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50 text-slate-800 p-6 relative overflow-hidden font-[Pretendard]">
    
    <!-- Background Decor -->
    <div class="absolute top-[-10%] right-[10%] w-[40vw] h-[40vw] bg-yellow-200/40 rounded-full blur-[100px] animate-blob mix-blend-multiply"></div>
    <div class="absolute bottom-[-10%] left-[10%] w-[40vw] h-[40vw] bg-emerald-200/40 rounded-full blur-[100px] animate-blob animation-delay-2000 mix-blend-multiply"></div>

    <div class="max-w-md w-full space-y-8 animate-fade-in-up relative z-10">
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">저장소 설정</h1>
        <p class="text-slate-500 font-medium">마지막 단계입니다! 백준 풀이를 저장할 깃허브 저장소를 연결하세요.</p>
      </div>

      <div class="bg-white/80 border border-white/60 rounded-3xl p-8 shadow-xl shadow-indigo-500/10 backdrop-blur-md space-y-6">
        <div class="bg-indigo-50 border border-indigo-100 rounded-2xl p-5 space-y-3">
          <h3 class="text-sm font-bold text-indigo-900 uppercase tracking-wider flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-indigo-500"></span>
            체크리스트
          </h3>
          <ol class="list-decimal list-inside text-sm text-slate-600 space-y-2 marker:font-bold marker:text-indigo-500 font-medium">
            <li>
                 <a href="https://chromewebstore.google.com/detail/kimjgflahdmnlhilmojcoaechlgkokhc?utm_source=item-share-cb" target="_blank" class="font-bold text-indigo-600 hover:text-indigo-500 hover:underline transition-colors">BaekjoonHub Dash</a> 익스텐션을 설치합니다.
            </li>
            <li>연결할 저장소 이름을 입력합니다.</li>
            <li>Dash가 자동으로 Webhook을 설정합니다.</li>
          </ol>
        </div>

        <form class="space-y-6" @submit.prevent="submitRepository">
          <label class="block text-left space-y-2">
            <span class="text-sm font-bold text-slate-700 ml-1">깃허브 저장소</span>
            <input
              v-model="repositoryName"
              type="text"
              placeholder="owner/repository"
              class="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-4 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-mono font-medium"
              :disabled="saving || success"
            />
          </label>

          <p v-if="submitError" class="text-sm text-rose-500 font-medium whitespace-pre-line bg-rose-50 px-4 py-3 rounded-xl border border-rose-100">
            ⚠ {{ submitError }}
          </p>

          <div class="flex flex-col gap-3">
            <button
              type="submit"
              class="w-full px-4 py-4 rounded-2xl bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 transition-all text-white font-bold shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/40 hover:-translate-y-0.5 disabled:opacity-60"
              :disabled="saving || success"
            >
              {{ saving ? "저장 중..." : "저장소 연결하고 시작하기" }}
            </button>
            <button
              type="button"
              class="w-full px-4 py-3 rounded-2xl bg-slate-100 hover:bg-slate-200 transition-colors text-slate-600 font-bold hover:text-slate-800"
              @click="skipOnboarding"
              :disabled="saving"
            >
              나중에 설정할게요
            </button>
          </div>
        </form>

        <div v-if="success" class="flex items-center gap-3 p-4 bg-emerald-50 border border-emerald-100 rounded-2xl animate-in zoom-in-95 duration-300">
           <div class="p-2 bg-emerald-100 rounded-full">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" />
             </svg>
           </div>
           <span class="text-sm font-bold text-emerald-700">설정이 완료되었습니다!<br>잠시 후 대시보드로 이동합니다.</span>
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
