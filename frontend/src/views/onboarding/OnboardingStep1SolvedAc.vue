<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50 text-slate-800 p-6 relative overflow-hidden">
    
    <!-- Background Decoration -->
    <div class="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] bg-brand-500/10 rounded-full blur-[120px] animate-blob"></div>
    <div class="absolute bottom-[-20%] right-[-10%] w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-[100px] animate-blob animation-delay-2000"></div>

    <div class="max-w-md w-full space-y-8 animate-fade-in-up relative z-10">
      
      <!-- Header -->
      <div class="text-center space-y-2">
        <span class="inline-block px-3 py-1 bg-brand-50 text-brand-600 rounded-full text-xs font-bold tracking-wider mb-2">STEP 01</span>
        <h1 class="text-4xl font-black tracking-tight text-slate-900">
          누구신가요?
        </h1>
        <p class="text-slate-500 text-lg font-medium leading-relaxed">
          Solved.ac 아이디를 알려주세요.<br>
          당신의 알고리즘 여정을 분석해드릴게요.
        </p>
      </div>

      <!-- Main Card -->
      <div class="bg-white/70 backdrop-blur-xl border border-white/50 rounded-3xl p-8 shadow-2xl shadow-brand-500/5 ring-1 ring-black/5 transition-all duration-500 hover:shadow-brand-500/10">
        <form @submit.prevent="submitHandle" class="space-y-6">
          
          <!-- Input Field -->
          <div class="space-y-2">
            <label class="text-sm font-bold text-slate-700 ml-1 flex items-center justify-between">
              <span>Solved.ac Handle</span>
              <span v-if="handle" class="text-xs font-medium" :class="verifiedUser ? 'text-emerald-500' : 'text-slate-400'">
                {{ verifiedUser ? '확인되었습니다' : '검색 중...' }}
              </span>
            </label>
            <div class="relative group">
              <input
                v-model="handle"
                type="text"
                placeholder="백준 핸들 입력"
                class="w-full bg-white border-2 border-slate-100 rounded-2xl pl-5 pr-12 py-4 text-lg font-bold text-slate-800 placeholder:text-slate-300 placeholder:font-medium focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all duration-300 shadow-sm"
                :class="{'border-emerald-400 focus:border-emerald-500 focus:ring-emerald-500/10': confirmed}"
                :disabled="loading || confirmed"
                @input="onHandleInput"
                required
              />
              <div class="absolute right-4 top-1/2 -translate-y-1/2 transition-all duration-300 pointer-events-none">
                <Loader2 v-if="verifying" class="w-6 h-6 text-brand-500 animate-spin" />
                <CheckCircle2 v-else-if="confirmed" class="w-6 h-6 text-emerald-500 animate-scale-in" />
                <Search v-else class="w-6 h-6 text-slate-300 group-focus-within:text-brand-400" />
              </div>
            </div>
          </div>

          <!-- User Preview Card (Conditional) -->
          <transition name="expand">
            <div v-if="verifiedUser && !verifyError" class="overflow-hidden">
              <div 
                class="relative bg-gradient-to-br from-white to-slate-50 border rounded-2xl p-5 transition-all duration-300 group cursor-pointer"
                :class="confirmed ? 'border-emerald-500 ring-2 ring-emerald-500/10 shadow-lg shadow-emerald-500/10' : 'border-slate-200 hover:border-brand-300'"
                @click="!confirmed && confirmUser()"
              >
                <!-- Selection Indicator -->
                <div v-if="!confirmed" class="absolute inset-0 bg-brand-500/0 group-hover:bg-brand-500/5 transition-colors duration-300 rounded-2xl"></div>
                
                <div class="flex items-center gap-5 relative z-10">
                  <!-- Avatar -->
                  <div class="relative">
                    <div class="w-16 h-16 rounded-2xl overflow-hidden border-2 border-white shadow-md">
                      <img 
                        :src="verifiedUser.profileImageUrl || '/images/profiles/default-profile.png'" 
                        class="w-full h-full object-cover"
                        alt="Profile"
                        @error="$event.target.src='/images/profiles/default-profile.png'"
                      />
                    </div>
                    <!-- Tier Badge (Absolute) -->
                    <div class="absolute -bottom-2 -right-2 bg-white rounded-lg shadow-sm border border-slate-100 p-0.5">
                       <img :src="getTierImage(verifiedUser.tier)" class="w-6 h-6" :alt="verifiedUser.tierName" />
                    </div>
                  </div>
                  
                  <!-- Info -->
                  <div class="flex-1 min-w-0">
                    <h3 class="font-black text-xl text-slate-900 truncate">{{ verifiedUser.handle }}</h3>
                    <div class="flex items-center gap-3 text-sm font-medium text-slate-500 mt-0.5">
                      <span class="flex items-center gap-1">
                        <Puzzle class="w-3.5 h-3.5" />
                        {{ verifiedUser.solvedCount.toLocaleString() }}
                      </span>
                      <span v-if="verifiedUser.classLevel" class="flex items-center gap-1 px-1.5 py-0.5 rounded-md bg-slate-100 text-slate-600 text-xs">
                        Class {{ verifiedUser.classLevel }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Action Hint -->
                <div v-if="!confirmed" class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs font-bold text-slate-400 group-hover:text-brand-600 transition-colors">
                  <span>이 계정이 맞나요?</span>
                  <span class="flex items-center gap-1">클릭해서 선택 <ArrowRight class="w-3 h-3" /></span>
                </div>

                <!-- Change Button (Only when confirmed) -->
                <button 
                  v-if="confirmed"
                  type="button"
                  @click.stop="resetConfirmation"
                  class="absolute top-2 right-2 p-2 text-slate-300 hover:text-rose-500 transition-colors rounded-xl hover:bg-rose-50"
                  title="다른 계정으로 변경"
                >
                  <X class="w-4 h-4" />
                </button>
              </div>
            </div>
          </transition>

          <!-- Error State -->
          <transition name="fade">
            <div v-if="verifyError" class="bg-rose-50/50 border border-rose-100 rounded-2xl p-4 flex items-start gap-3">
              <AlertCircle class="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
              <div>
                <p class="text-sm font-bold text-rose-600">계정을 찾을 수 없어요</p>
                <p class="text-xs text-rose-400 mt-1">아이디를 다시 한 번 확인해주세요.</p>
              </div>
            </div>
          </transition>

          <!-- Submit Button -->
          <button
            type="submit"
            class="w-full bg-slate-900 hover:bg-slate-800 text-white font-bold text-lg py-4 rounded-2xl transition-all shadow-xl shadow-slate-900/10 hover:shadow-slate-900/20 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none flex items-center justify-center gap-2 group"
            :disabled="loading || !confirmed"
          >
            <Loader2 v-if="loading" class="animate-spin h-5 w-5 text-slate-400" />
            <span v-else class="flex items-center gap-2">
              분석 시작하기
              <ArrowRight class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </span>
          </button>

        </form>
      </div>
      
      <!-- Footer Note -->
      <p class="text-center text-xs text-slate-400 font-medium">
        Solved.ac 계정이 없으신가요? <br>
        <a href="https://solved.ac" target="_blank" class="text-brand-500 hover:underline">여기서 가입</a>할 수 있습니다.
      </p>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { onboardingApi } from '@/api/onboarding';
import { useAuth } from '@/composables/useAuth';
import { Loader2, Search, CheckCircle2, ArrowRight, X, AlertCircle, Puzzle } from 'lucide-vue-next';

// Props 및 Emits
const emit = defineEmits(['next']);

// 상태 (State)
const { user } = useAuth();
const handle = ref('');
const loading = ref(false);
const verifying = ref(false);
const verifiedUser = ref(null);
const verifyError = ref(null);
const confirmed = ref(false);

let debounceTimer = null;

// 메서드 (Methods)
const onHandleInput = () => {
  verifiedUser.value = null;
  verifyError.value = null;
  confirmed.value = false;
  
  clearTimeout(debounceTimer);
  if (handle.value.trim().length >= 2) {
    verifying.value = true;
    debounceTimer = setTimeout(() => {
      verifyHandle();
    }, 600);
  } else {
    verifying.value = false;
  }
};

const verifyHandle = async () => {
  if (!handle.value.trim()) {
    verifying.value = false;
    return;
  }
  
  try {
    const res = await onboardingApi.verifySolvedac(handle.value.trim());
    verifiedUser.value = res.data;
  } catch (error) {
    // console.error(error);
    verifyError.value = '존재하지 않는 아이디입니다.';
    verifiedUser.value = null;
  } finally {
    verifying.value = false;
  }
};

const confirmUser = () => {
  confirmed.value = true;
};

const resetConfirmation = () => {
  confirmed.value = false;
  verifiedUser.value = null;
  handle.value = '';
};

// 이미지 경로 매핑 대신 SVG 활용도 가능하지만, 일단 기존 로직 참고하여 아이콘/이미지 사용
// 여기서는 간단히 Tier 번호를 받아 이미지 URL을 생성한다고 가정
// 만약 이미지가 없다면 뱃지 컴포넌트 사용 권장. 여기선 예시로 svg 아이콘으로 대체 가능하나, 
// 사용자 요청이 'Senior Design'이므로 가능한 시각적 요소를 풍부하게.
const getTierImage = (tier) => {
    // 실제 구현시엔 tier 숫자에 맞는 이미지 경로 반환
    // 예: https://static.solved.ac/tier_small/1.svg
    if (!tier) return 'https://static.solved.ac/tier_small/0.svg';
    return `https://static.solved.ac/tier_small/${tier}.svg`;
};

const submitHandle = async () => {
  if (!handle.value || !confirmed.value) return;
  
  loading.value = true;
  try {
    let profileImage = null;
    if (!user.value?.avatarUrl) {
        profileImage = '/images/profiles/default-profile.png';
    }
    
    await onboardingApi.registerSolvedac(handle.value, profileImage);
    
    // 다음 단계로 이동
    emit('next');
    
  } catch (error) {
    console.error(error);
    verifyError.value = '연동 중 문제가 발생했습니다. 다시 시도해주세요.';
    loading.value = false;
  }
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');
* { font-family: 'Pretendard', sans-serif; }

.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(30px);
}

@keyframes fadeInUp {
  to { opacity: 1; transform: translateY(0); }
}

.animate-blob {
  animation: blob 10s infinite;
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

.animate-scale-in {
  animation: scaleIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes scaleIn {
  from { transform: scale(0); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

/* Transitions */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  max-height: 200px;
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-10px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
