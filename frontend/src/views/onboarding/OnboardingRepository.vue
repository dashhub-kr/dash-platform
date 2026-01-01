<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50 text-slate-800 p-6">

    <div class="max-w-md w-full space-y-8 animate-fade-in-up relative z-10">
      <div class="text-center space-y-2">
        <h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">저장소 설정</h1>
        <p class="text-slate-500 font-medium">마지막 단계입니다! 백준 풀이를 저장할 깃허브 저장소를 연결하세요.</p>
      </div>

      <div class="bg-white/80 border border-white/60 rounded-3xl p-8 shadow-xl shadow-brand-500/10 backdrop-blur-md space-y-6">
        <!-- Extension Detected Badge -->
        <div v-if="extensionDetected" class="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 flex items-center gap-3">
          <div class="w-10 h-10 bg-emerald-100 rounded-full flex items-center justify-center">
            <Puzzle :size="20" class="text-emerald-600" fill="currentColor" />
          </div>
          <div>
            <div class="font-bold text-emerald-800 text-sm">BaekjoonHub Dash 익스텐션 감지됨</div>
            <div class="text-emerald-600 text-xs">저장소가 자동으로 선택되었습니다. 변경하려면 검색하세요.</div>
          </div>
        </div>

        <div class="bg-brand-50 border border-brand-100 rounded-2xl p-5 space-y-3">
          <h3 class="text-sm font-bold text-brand-900 uppercase tracking-wider flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-brand-500"></span>
            체크리스트
          </h3>
          <ol class="list-decimal list-inside text-sm text-slate-600 space-y-2 marker:font-bold marker:text-brand-500 font-medium">
            <li :class="extensionDetected ? 'line-through text-slate-400' : ''">
                 <a href="https://chromewebstore.google.com/detail/kimjgflahdmnlhilmojcoaechlgkokhc?utm_source=item-share-cb" target="_blank" class="font-bold text-brand-600 hover:text-brand-500 hover:underline transition-colors" :class="extensionDetected ? 'text-slate-400' : ''">BaekjoonHub Dash</a> 익스텐션을 설치합니다.
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
                class="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-4 focus:ring-brand-500/20 focus:border-brand-500 transition-all font-medium"
                :disabled="saving || success"
                @input="onSearchInput"
              />
              <Loader2 v-if="searching" class="absolute right-4 top-1/2 -translate-y-1/2 animate-spin h-5 w-5 text-slate-400" />
            </div>
          </label>

            <!-- Search Results -->
            <div 
              v-for="repo in repositories" 
              :key="repo.fullName"
              @click="selectRepository(repo)"
              class="bg-white hover:bg-brand-50 border border-slate-200 hover:border-brand-300 rounded-2xl p-4 cursor-pointer transition-all group shadow-sm hover:shadow-md"
            >
              <div class="flex items-center justify-between gap-3">
                <div class="flex items-center gap-3 flex-1 min-w-0">
                  <div class="w-10 h-10 bg-slate-100 group-hover:bg-white rounded-xl flex items-center justify-center transition-colors border border-slate-200 group-hover:border-brand-200 flex-shrink-0">
                    <Folder :size="20" class="text-slate-400 group-hover:text-brand-600" fill="currentColor" />
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="font-bold text-slate-800 group-hover:text-brand-700 transition-colors truncate text-sm md:text-base leading-tight">{{ repo.fullName }}</div>
                    <div class="text-xs text-slate-500 truncate mt-0.5">{{ repo.description || '설명 없음' }}</div>
                  </div>
                </div>
                <span v-if="repo.isPrivate" class="flex-shrink-0 text-[10px] bg-amber-50 text-amber-600 border border-amber-200 px-2 py-1 rounded-lg font-bold flex items-center gap-1">
                    <Lock :size="12" fill="currentColor" />
                    Private
                </span>
              </div>
            </div>

          <!-- No Results -->
          <div v-if="searchQuery && !searching && repositories.length === 0 && !selectedRepo" class="text-center py-6 text-slate-500">
            <p class="font-medium">일치하는 저장소가 없습니다.</p>
            <p class="text-sm mt-1">다른 이름으로 검색해보세요.</p>
          </div>

          <!-- Selected Repository Card -->
          <div v-if="selectedRepo" class="relative">
            <div class="bg-gradient-to-br from-emerald-50 to-white border border-emerald-300 rounded-2xl p-5 shadow-lg shadow-emerald-100 ring-2 ring-emerald-50">
              <div class="flex items-start gap-3">
                <div class="w-10 h-10 bg-emerald-100 rounded-xl flex items-center justify-center flex-shrink-0 mt-0.5">
                  <Folder :size="20" class="text-emerald-600" fill="currentColor" />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-2 min-w-0">
                         <div class="font-bold text-slate-800 truncate text-sm md:text-base">{{ selectedRepo.fullName }}</div>
                         <span v-if="selectedRepo.isPrivate" class="flex-shrink-0 text-[10px] bg-amber-50 text-amber-600 border border-amber-200 px-1.5 py-0.5 rounded font-bold flex items-center gap-0.5">
                            <Lock :size="12" fill="currentColor" />
                         </span>
                    </div>
                    <div class="text-emerald-500 flex-shrink-0">
                      <Check :size="24" stroke-width="3" />
                    </div>
                  </div>
                  <div class="text-sm text-slate-500 mt-1 truncate">{{ selectedRepo.description || '설명 없음' }}</div>
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
              class="w-full px-4 py-4 rounded-2xl bg-gradient-to-r from-brand-600 to-brand-500 hover:from-brand-500 hover:to-brand-400 transition-all text-white font-bold shadow-lg shadow-brand-500/30 hover:shadow-brand-500/40 hover:-translate-y-0.5 disabled:opacity-60 disabled:cursor-not-allowed disabled:transform-none"
              :disabled="saving || success || !selectedRepo"
            >
              {{ saving ? "연결 중..." : (selectedRepo ? "저장소 연결하고 시작하기" : "위에서 저장소를 선택해주세요") }}
            </button>

          </div>
        </form>

        <div v-if="success" class="flex items-center gap-3 p-4 bg-emerald-50 border border-emerald-100 rounded-2xl animate-in zoom-in-95 duration-300">
           <div class="p-2 bg-emerald-100 rounded-full">
              <CheckCircle2 :size="24" class="text-emerald-600" fill="currentColor" />
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
import { onboardingApi } from "@/api/onboarding";
import { Puzzle, Loader2, Folder, Lock, Check, CheckCircle2 } from 'lucide-vue-next';

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

const emit = defineEmits(['finish']);

const scheduleRedirect = () => {
  setTimeout(() => {
    emit('finish');
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
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
