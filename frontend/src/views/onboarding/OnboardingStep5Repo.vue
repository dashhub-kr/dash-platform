<template>
  <div class="min-h-screen bg-slate-50 text-slate-800 p-6 flex items-center justify-center relative overflow-hidden">
    
    <!-- Decorative -->
    <div class="absolute inset-0 bg-grid-slate-100 [mask-image:linear-gradient(0deg,white,rgba(255,255,255,0.6))] pointer-events-none"></div>

    <div class="max-w-xl w-full relative z-10 animate-fade-in-up">
      
      <div class="text-center mb-8">
        <span class="inline-block px-3 py-1 bg-brand-50 text-brand-600 rounded-full text-xs font-bold tracking-wider mb-3">STEP 05</span>
        <h1 class="text-3xl font-black text-slate-900 tracking-tight mb-2">
          저장소 확인
        </h1>
        <p class="text-slate-500 font-medium">
          익스텐션(백준허브)에 설정된 저장소를 감지합니다.
        </p>
      </div>

      <div class="bg-white/90 backdrop-blur-xl border border-white/60 rounded-3xl p-8 shadow-2xl space-y-6">
        
        <!-- State: Detecting -->
        <div v-if="detecting" class="py-8 text-center">
           <div class="relative w-16 h-16 mx-auto mb-4">
              <div class="absolute inset-0 bg-brand-100 rounded-full animate-ping opacity-75"></div>
              <div class="relative bg-brand-500 rounded-full w-16 h-16 flex items-center justify-center text-white">
                 <Search class="w-8 h-8 animate-pulse" />
              </div>
           </div>
           <h3 class="font-bold text-slate-800 text-lg">저장소 감지 중...</h3>
           <p class="text-sm text-slate-500 mt-1">익스텐션 설정을 확인하고 있습니다.</p>
        </div>

        <!-- State: Detected & Confirm -->
        <div v-else-if="detectedRepo" class="space-y-6 animate-scale-in">
           <div class="bg-emerald-50 border border-emerald-200 rounded-2xl p-6 text-center">
              <div class="w-12 h-12 bg-white rounded-full flex items-center justify-center mx-auto mb-3 text-emerald-500 shadow-sm">
                 <CheckCircle2 class="w-8 h-8" />
              </div>
              <p class="text-sm font-bold text-emerald-600 mb-1">저장소를 찾았습니다!</p>
              <h3 class="text-2xl font-black text-slate-900 break-all">
                 {{ detectedRepo.fullName }}
              </h3>
              <p class="text-xs text-slate-400 mt-2">{{ detectedRepo.description || '설명 없음' }}</p>
           </div>
           
           <div class="space-y-3">
              <button 
                @click="confirmRepo" 
                class="w-full py-4 bg-brand-600 hover:bg-brand-500 text-white font-bold rounded-2xl shadow-xl shadow-brand-500/20 hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2"
                :disabled="saving"
              >
                 <Loader2 v-if="saving" class="animate-spin" />
                 <span>네, 이 저장소가 맞습니다</span>
              </button>
              
              <button 
                @click="startManualSearch"
                class="w-full py-3 text-slate-400 hover:text-slate-600 font-bold text-sm transition-colors"
              >
                 아니요, 직접 검색할게요
              </button>
           </div>
        </div>

        <!-- State: Manual Search (Fallback) -->
        <div v-else class="space-y-6 animate-fade-in">
           <div class="text-center">
               <p class="text-sm text-amber-500 font-bold mb-2">⚠ 저장소를 자동으로 찾지 못했습니다.</p>
               <h3 class="text-lg font-bold text-slate-800">직접 검색해서 선택해주세요</h3>
           </div>
           
           <!-- Search Input -->
           <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="예: username/repository"
                class="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 pl-11 font-medium focus:bg-white focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all outline-none"
                @input="onSearchInput"
              />
              <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
              <Loader2 v-if="searching" class="absolute right-4 top-1/2 -translate-y-1/2 w-4 h-4 text-brand-500 animate-spin" />
           </div>

           <!-- Search Results -->
           <div v-if="repositories.length > 0" class="max-h-60 overflow-y-auto custom-scrollbar space-y-2">
              <div 
                v-for="repo in repositories" 
                :key="repo.fullName"
                @click="selectRepo(repo)"
                class="p-3 bg-white border border-slate-100 rounded-xl hover:border-brand-300 hover:shadow-md transition-all cursor-pointer flex items-center justify-between group"
              >
                 <div class="min-w-0">
                    <div class="font-bold text-slate-800 group-hover:text-brand-700 truncate text-sm">{{ repo.fullName }}</div>
                    <div class="text-[10px] text-slate-400 truncate">{{ repo.description }}</div>
                 </div>
                 <div v-if="repo.isPrivate" class="px-1.5 py-0.5 bg-amber-50 text-amber-600 rounded text-[10px] font-bold">Private</div>
              </div>
           </div>
           
           <!-- Selected (Manual) -->
           <div v-if="selectedRepo" class="bg-brand-50 border border-brand-200 p-4 rounded-xl flex items-center justify-between">
              <div class="font-bold text-brand-800 truncate pr-2">{{ selectedRepo.fullName }}</div>
              <button @click="confirmRepo" class="px-4 py-2 bg-brand-600 text-white rounded-lg text-sm font-bold shadow-md hover:bg-brand-500 shrink-0">
                 선택
              </button>
           </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onboardingApi } from '@/api/onboarding';
import { Search, Loader2, CheckCircle2 } from 'lucide-vue-next';

const emit = defineEmits(['finish']);

const detecting = ref(true);
const detectedRepo = ref(null);
const searching = ref(false);
const saving = ref(false);
const searchQuery = ref('');
const repositories = ref([]);
const selectedRepo = ref(null);

let debounceTimer = null;

// Fake detection for UX demo + Real logic
const detectRepository = async () => {
    // 1. Check DOM (injected by extension)
    let repoName = null;
    const dataEl = document.getElementById('baekjoonhub-dash-data');
    if (dataEl?.dataset.repo) {
       repoName = dataEl.dataset.repo;
    }
    
    // If not found, listen for event (with timeout)
    if (!repoName) {
        // Wait for event...
        // For logic simplicity, if not found immediately or via short polling, fallback.
    }

    // UX Delay
    await new Promise(r => setTimeout(r, 1500));
    
    // Simulate finding one if repoName exists, or if we want to show detection success for demo (if user has repo)
    // For now, let's assume we proceed to manual if nothing is found.
    // However, if we found it:
    if (repoName) {
        // Extract "owner/repo"
        if (repoName.includes('github.com/')) repoName = repoName.split('github.com/')[1];
        
        detectedRepo.value = {
            fullName: repoName,
            description: 'Extension detected repository'
        };
    }
    
    detecting.value = false;
};

// Handle manual search
const onSearchInput = () => {
    clearTimeout(debounceTimer);
    if (searchQuery.value.length < 2) return;
    
    searching.value = true;
    debounceTimer = setTimeout(async () => {
        try {
            const res = await onboardingApi.searchRepositories(searchQuery.value);
            repositories.value = res.data || [];
        } catch (e) {
            console.error(e);
        } finally {
            searching.value = false;
        }
    }, 500);
};

const selectRepo = (repo) => {
    selectedRepo.value = repo;
};

const startManualSearch = () => {
    detectedRepo.value = null;
    detecting.value = false;
};

const confirmRepo = async () => {
    const repo = detectedRepo.value || selectedRepo.value;
    if (!repo) return;
    
    saving.value = true;
    try {
        await onboardingApi.submitRepository(repo.fullName);
        // Success
        emit('finish', repo.fullName);
    } catch (e) {
        alert('저장소 연결 실패. 다시 시도해주세요.');
        saving.value = false;
    }
};

onMounted(() => {
    // Listen for real extension data
    window.addEventListener('baekjoonhub-dash-ready', (e) => {
        if (detecting.value && e.detail?.repo) {
            const repoUrl = e.detail.repo;
            const repoName = repoUrl.includes('github.com/') ? repoUrl.split('github.com/')[1] : repoUrl;
            detectedRepo.value = { fullName: repoName, description: 'Detected from Extension' };
            detecting.value = false;
        }
    });
    // Trigger detection
    window.dispatchEvent(new CustomEvent('baekjoonhub-dash-request'));
    
    detectRepository();
});
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}
@keyframes fadeInUp { to { opacity: 1; transform: translateY(0); } }

.animate-scale-in {
  animation: scaleIn 0.3s ease-out forwards;
}
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
</style>
