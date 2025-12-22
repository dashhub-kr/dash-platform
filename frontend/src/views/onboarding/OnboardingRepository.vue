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
        <!-- Extension Detected Badge -->
        <div v-if="extensionDetected" class="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 flex items-center gap-3">
          <div class="w-10 h-10 bg-emerald-100 rounded-full flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-emerald-600" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
          </div>
          <div>
            <div class="font-bold text-emerald-800 text-sm">BaekjoonHub Dash 익스텐션 감지됨</div>
            <div class="text-emerald-600 text-xs">저장소가 자동으로 선택되었습니다. 변경하려면 검색하세요.</div>
          </div>
        </div>

        <div class="bg-indigo-50 border border-indigo-100 rounded-2xl p-5 space-y-3">
          <h3 class="text-sm font-bold text-indigo-900 uppercase tracking-wider flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-indigo-500"></span>
            체크리스트
          </h3>
          <ol class="list-decimal list-inside text-sm text-slate-600 space-y-2 marker:font-bold marker:text-indigo-500 font-medium">
            <li :class="extensionDetected ? 'line-through text-slate-400' : ''">
                 <a href="https://chromewebstore.google.com/detail/kimjgflahdmnlhilmojcoaechlgkokhc?utm_source=item-share-cb" target="_blank" class="font-bold text-indigo-600 hover:text-indigo-500 hover:underline transition-colors" :class="extensionDetected ? 'text-slate-400' : ''">BaekjoonHub Dash</a> 익스텐션을 설치합니다.
                 <span v-if="extensionDetected" class="text-emerald-500 ml-1">✓</span>
            </li>
            <li>연결할 저장소 이름을 검색합니다.</li>
            <li>검색 결과에서 저장소를 선택합니다.</li>
          </ol>
        </div>

        <form class="space-y-6" @submit.prevent="submitRepository">
          <label class="block text-left space-y-2">
            <span class="text-sm font-bold text-slate-700 ml-1">저장소 검색</span>
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="저장소 이름을 입력하세요"
                class="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-4 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium"
                :disabled="saving || success"
                @input="onSearchInput"
              />
              <svg v-if="searching" class="absolute right-4 top-1/2 -translate-y-1/2 animate-spin h-5 w-5 text-slate-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </div>
          </label>

          <!-- Search Results -->
          <div v-if="repositories.length > 0 && !selectedRepo" class="space-y-2 max-h-64 overflow-y-auto">
            <div 
              v-for="repo in repositories" 
              :key="repo.fullName"
              @click="selectRepository(repo)"
              class="bg-slate-50 hover:bg-indigo-50 border border-slate-200 hover:border-indigo-300 rounded-xl p-4 cursor-pointer transition-all group"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 bg-slate-200 group-hover:bg-indigo-200 rounded-lg flex items-center justify-center transition-colors">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-slate-500 group-hover:text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                    </svg>
                  </div>
                  <div>
                    <div class="font-bold text-slate-800 group-hover:text-indigo-700 transition-colors">{{ repo.fullName }}</div>
                    <div class="text-sm text-slate-500 line-clamp-1">{{ repo.description || '설명 없음' }}</div>
                  </div>
                </div>
                <span v-if="repo.isPrivate" class="text-xs bg-slate-200 text-slate-600 px-2 py-0.5 rounded-full font-medium">🔒 Private</span>
              </div>
            </div>
          </div>

          <!-- No Results -->
          <div v-else-if="searchQuery && !searching && repositories.length === 0 && !selectedRepo" class="text-center py-6 text-slate-500">
            <p class="font-medium">일치하는 저장소가 없습니다.</p>
            <p class="text-sm mt-1">다른 이름으로 검색해보세요.</p>
          </div>

          <!-- Selected Repository Card -->
          <div v-if="selectedRepo" class="relative">
            <div class="bg-gradient-to-br from-emerald-50 to-white border-2 border-emerald-400 rounded-2xl p-5 shadow-lg shadow-emerald-100">
              <div class="flex items-start gap-3">
                <div class="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                  </svg>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between gap-2">
                    <div class="font-bold text-slate-800 truncate">{{ selectedRepo.fullName }}</div>
                    <div class="text-emerald-500 flex-shrink-0">
                      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 6 9 17l-5-5"/>
                      </svg>
                    </div>
                  </div>
                  <div class="text-sm text-slate-500 mt-1 line-clamp-2">{{ selectedRepo.description || '설명 없음' }}</div>
                </div>
              </div>
              <button 
                type="button"
                @click="clearSelection"
                class="w-full mt-4 py-2 text-slate-500 hover:text-slate-700 font-medium transition-colors text-sm border-t border-slate-200 pt-3"
              >
                다른 저장소 선택
              </button>
            </div>
          </div>

          <p v-if="submitError" class="text-sm text-rose-500 font-medium whitespace-pre-line bg-rose-50 px-4 py-3 rounded-xl border border-rose-100">
            ⚠ {{ submitError }}
          </p>

          <div class="flex flex-col gap-3">
            <button
              type="submit"
              class="w-full px-4 py-4 rounded-2xl bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 transition-all text-white font-bold shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/40 hover:-translate-y-0.5 disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none"
              :disabled="saving || success || !selectedRepo"
            >
              {{ saving ? "연결 중..." : (selectedRepo ? "저장소 연결하고 시작하기" : "위에서 저장소를 선택해주세요") }}
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
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { onboardingApi } from "../../api/onboarding";

const router = useRouter();
const searchQuery = ref("");
const repositories = ref([]);
const selectedRepo = ref(null);
const submitError = ref(null);
const saving = ref(false);
const success = ref(false);
const searching = ref(false);
const extensionDetected = ref(false);

let debounceTimer = null;

// BaekjoonHub Dash 익스텐션에서 repo 값을 읽어 기본값으로 설정
const handleExtensionData = async (repoUrl) => {
  if (!repoUrl) return;
  
  // URL 형식에서 owner/repo 추출 (예: "https://github.com/canit0221/code-kata" -> "canit0221/code-kata")
  let repoFullName = repoUrl;
  if (repoUrl.includes("github.com/")) {
    repoFullName = repoUrl.split("github.com/")[1];
  }
  
  if (repoFullName && repoFullName.includes("/")) {
    extensionDetected.value = true;
    searchQuery.value = repoFullName.split("/")[1]; // repo 이름으로 검색
    
    // 해당 저장소 검색 및 자동 선택
    try {
      const res = await onboardingApi.searchRepositories(searchQuery.value);
      const repos = res.data || [];
      // 정확히 일치하는 저장소 찾기
      const matchedRepo = repos.find(r => r.fullName === repoFullName);
      if (matchedRepo) {
        selectedRepo.value = matchedRepo;
      } else if (repos.length > 0) {
        // 정확히 일치하지 않으면 목록 표시
        repositories.value = repos;
      }
    } catch (err) {
      console.error("Failed to auto-search repository:", err);
    }
  }
};

onMounted(() => {
  // 방법 1: DOM에서 익스텐션 데이터 읽기 (Content Script가 주입)
  const dataElement = document.getElementById('baekjoonhub-dash-data');
  if (dataElement && dataElement.dataset.repo) {
    handleExtensionData(dataElement.dataset.repo);
  }
  
  // 방법 2: CustomEvent 리스닝 (Content Script가 발송)
  window.addEventListener('baekjoonhub-dash-ready', (e) => {
    if (e.detail && e.detail.repo && !extensionDetected.value) {
      handleExtensionData(e.detail.repo);
    }
  });
  
  // 익스텐션에 데이터 요청 (이미 로드된 경우 대비)
  window.dispatchEvent(new CustomEvent('baekjoonhub-dash-request'));
});

const scheduleRedirect = () => {
  setTimeout(() => {
    window.location.href = "/dashboard";
  }, 1500);
};

const onSearchInput = () => {
  selectedRepo.value = null;
  submitError.value = null;
  
  clearTimeout(debounceTimer);
  if (searchQuery.value.trim().length >= 1) {
    debounceTimer = setTimeout(() => {
      searchRepositories();
    }, 300);
  } else {
    repositories.value = [];
  }
};

const searchRepositories = async () => {
  searching.value = true;
  try {
    const res = await onboardingApi.searchRepositories(searchQuery.value.trim());
    repositories.value = res.data || [];
  } catch (err) {
    console.error("Failed to search repositories:", err);
    submitError.value = "저장소 목록을 불러오는데 실패했습니다.";
    repositories.value = [];
  } finally {
    searching.value = false;
  }
};

const selectRepository = (repo) => {
  selectedRepo.value = repo;
  repositories.value = [];
};

const clearSelection = () => {
  selectedRepo.value = null;
  searchQuery.value = "";
};

const submitRepository = async () => {
  if (!selectedRepo.value) {
    submitError.value = "저장소를 선택해주세요.";
    return;
  }

  saving.value = true;
  submitError.value = null;
  try {
    const res = await onboardingApi.submitRepository(selectedRepo.value.fullName);
    const data = res.data;
    
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

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
