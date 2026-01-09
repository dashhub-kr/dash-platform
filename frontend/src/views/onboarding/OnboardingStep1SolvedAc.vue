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
        <form @submit.prevent="handleButtonClick" class="space-y-6">
          
          <!-- Input Field -->
          <div class="space-y-2">
            <label class="text-sm font-bold text-slate-700 ml-1 flex items-center justify-between">
              <span>Solved.ac Handle</span>
              <span v-if="handle" class="text-xs font-medium" :class="verificationSuccess ? 'text-emerald-500' : (confirmed ? 'text-blue-500' : 'text-slate-400')">
                {{ verificationSuccess ? '확인되었습니다' : (confirmed ? '인증 대기중...' : '검색 중...') }}
              </span>
            </label>
            <div class="relative group">
              <input
                v-model="handle"
                type="text"
                placeholder="백준 핸들 입력"
                class="w-full bg-white border-2 border-slate-100 rounded-2xl pl-5 pr-12 py-4 text-lg font-bold text-slate-800 placeholder:text-slate-300 placeholder:font-medium focus:outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 transition-all duration-300 shadow-sm"
                :class="{'border-emerald-400 focus:border-emerald-500 focus:ring-emerald-500/10': verificationSuccess}"
                :disabled="loading || confirmed"
                @input="onHandleInput"
                required
              />
              <div class="absolute right-4 top-1/2 -translate-y-1/2 transition-all duration-300 pointer-events-none">
                <Loader2 v-if="verifying" class="w-6 h-6 text-brand-500 animate-spin" />
                <CheckCircle2 v-else-if="verificationSuccess" class="w-6 h-6 text-emerald-500 animate-scale-in" />
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

                <!-- Verification Guide (Only when confirmed) -->
                <transition name="fade">
                  <div v-if="confirmed && !verificationSuccess" class="mt-4 pt-4 border-t border-slate-100 space-y-3">
                    <div class="p-4 bg-slate-50 rounded-xl border border-slate-200">
                        <h4 class="text-sm font-bold text-slate-700 mb-2 flex items-center gap-1.5">
                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                            본인 인증이 필요합니다
                        </h4>
                        <p class="text-xs text-slate-500 leading-relaxed mb-3">
                            타인 도용 방지를 위해, 
                            <a :href="`https://solved.ac/profile/${handle}`" target="_blank" class="text-blue-500 hover:underline font-bold">Solved.ac</a>
                            로그인 후 '프로필 편집' 버튼을 눌러 자기소개란에 아래 코드를 입력해주세요.
                        </p>
                        
                        <div class="flex items-center gap-2">
                             <div 
                                @click="copyVerificationCode"
                                class="flex-1 flex items-center justify-between bg-white border border-slate-200 rounded-lg p-2.5 cursor-pointer hover:border-emerald-500 hover:text-emerald-600 transition-all group"
                            >
                                <code class="font-mono text-sm font-bold text-slate-700 group-hover:text-emerald-600">DashHub:{{ user?.username }}</code>
                                <Copy class="w-4 h-4 text-slate-400 group-hover:text-emerald-500" />
                            </div>
                            
                            <a 
                                :href="`https://solved.ac/profile/${handle}`" 
                                target="_blank"
                                class="p-2.5 bg-white border border-slate-200 rounded-lg text-slate-400 hover:text-blue-500 hover:border-blue-200 transition-colors"
                                title="Solved.ac 프로필로 이동"
                            >
                                <ExternalLink class="w-5 h-5" />
                            </a>
                        </div>
                         <p class="text-[10px] text-slate-400 mt-2 text-right">
                            * 설정 저장 후 아래 버튼을 눌러주세요.
                        </p>
                    </div>
                  </div>
                </transition>

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
                <p class="text-sm font-bold text-rose-600">
                    {{ verifyError.includes('존재하지') ? '계정을 찾을 수 없어요' : '인증에 실패했습니다' }}
                </p>
                <p class="text-xs text-rose-400 mt-1">{{ verifyError }}</p>
                <a 
                    v-if="confirmed && verifyError && !verifyError.includes('존재하지')"
                    :href="`https://solved.ac/profile/${handle}`"
                    target="_blank"
                    class="inline-flex items-center gap-1 text-xs font-bold text-rose-600 underline mt-2 hover:text-rose-700"
                >
                    내 프로필 확인하기 <ExternalLink class="w-3 h-3" />
                </a>
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
              {{ verificationSuccess ? '분석 시작하기' : (verifyError ? '다시 인증하기' : '인증하기') }}
              <ArrowRight v-if="verificationSuccess" class="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              <RotateCcw v-else-if="verifyError" class="w-5 h-5 group-hover:-rotate-180 transition-transform" />
              <CheckCircle2 v-else class="w-5 h-5 group-hover:scale-110 transition-transform" />
            </span>
          </button>

          <!-- Retry Helper (Only when error) -->
          <button 
            v-if="verifyError" 
            type="button" 
            @click="resetConfirmation"
            class="w-full text-center text-xs text-slate-400 hover:text-rose-500 underline transition-colors"
          >
            아이디를 잘못 입력하셨나요? 다시 검색하기
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
import { Loader2, Search, CheckCircle2, ArrowRight, X, AlertCircle, Puzzle, Copy, ExternalLink, RotateCcw } from 'lucide-vue-next';

// Props 및 Emits
const emit = defineEmits(['next']);

// 상태 (State)
const { user } = useAuth();
// ...

const copyVerificationCode = () => {
    const code = `DashHub:${user.value.username}`;
    navigator.clipboard.writeText(code).then(() => {
        // Toast notification would be better but alert is consistent with profile view
        alert("인증 코드가 복사되었습니다!\nSolved.ac 로그인 > '프로필 편집'을 통해 붙여넣어주세요.");
    });
};

const handle = ref('');
const loading = ref(false);
const verifying = ref(false);
const verifiedUser = ref(null);
const verifyError = ref(null);
const confirmed = ref(false);
const verificationSuccess = ref(false);

let debounceTimer = null;

// 메서드 (Methods)
const onHandleInput = () => {
  verifiedUser.value = null;
  verifyError.value = null;
  confirmed.value = false;
  verificationSuccess.value = false;
  
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
  verificationSuccess.value = false;
  verifiedUser.value = null;
  verifyError.value = null;
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

const processVerification = async () => {
  if (!handle.value || !confirmed.value) return;
  
  console.log("Starting verification for:", handle.value);
  loading.value = true;
  verifyError.value = null;

  try {
    // 프로필 이미지 선택 로직 개선
    // 사용자가 이미 프로필 이미지를 가지고 있다면 그것을 사용, 없다면 기본 이미지
    // 단, Solved.ac 연동 시에는 Solved.ac 이미지가 우선순위가 높을 수 있으나,
    // 현재 API는 registerSolvedac에 profileImage를 보내면 그것으로 User Avatar를 업데이트함.
    // 여기서는 "User의 현재 Avatar가 없으면 Default를 보낸다"는 로직 유지.
    let profileImage = user.value?.avatarUrl || null;
    
    // 만약 user.avatarUrl도 없고, null을 보내면 백엔드에서 처리가 안될 수도 있으니 기본값 설정
    if (!profileImage) {
        profileImage = '/images/profiles/default-profile.png';
    }
    
    console.log("Sending verification request with image:", profileImage);
    await onboardingApi.registerSolvedac(handle.value, profileImage);
    
    console.log("Verification successful");
    // 인증 성공 처리
    verificationSuccess.value = true;
    loading.value = false;
    
  } catch (error) {
    console.error("Verification failed:", error);
    const msg = error.response?.data?.message || '연동 중 문제가 발생했습니다. 다시 시도해주세요.';
    if (msg.includes("Bio")) {
         verifyError.value = "인증에 실패했습니다. 자기소개를 확인해주세요.";
    } else {
         verifyError.value = msg;
    }
    loading.value = false;
    verificationSuccess.value = false;
  }
};

const handleButtonClick = async () => {
    try {
        if (verificationSuccess.value) {
            console.log("Emitting next");
            emit('next');
        } else if (verifyError.value) {
            // 실패 후 재시도: 에러만 초기화하고 가이드 화면 유지 (재검증은 사용자가 다시 버튼 누르면 수행)
            verifyError.value = null;
        } else {
            await processVerification();
        }
    } catch (e) {
        console.error("Error in button handler:", e);
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
