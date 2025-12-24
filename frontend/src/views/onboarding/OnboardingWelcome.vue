<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50 text-slate-800 p-6">

    <div class="max-w-md w-full space-y-8 animate-fade-in-up relative z-10">
      <div class="text-center space-y-2">
        <h1 class="text-4xl font-extrabold tracking-tight bg-gradient-to-r from-brand-600 to-cyan-500 bg-clip-text text-transparent">
          Solved.ac 연동
        </h1>
        <p class="text-slate-500 font-medium">
          알고리즘 실력을 분석하기 위해 아이디를 입력해주세요.
        </p>
      </div>

      <div class="bg-white/80 border border-white/60 rounded-3xl p-8 shadow-xl shadow-brand-500/10 backdrop-blur-md">
        <form @submit.prevent="submitHandle" class="space-y-6">
          <div class="space-y-2">
            <label class="text-sm font-bold text-slate-700 ml-1">Solved.ac ID</label>
            <input
              v-model="handle"
              type="text"
              placeholder="백준 핸들을 입력하세요"
              class="w-full bg-white border border-slate-200 rounded-2xl px-5 py-4 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-4 focus:ring-brand-500/20 focus:border-brand-500 transition-all font-medium"
              :disabled="loading || confirmed"
              @input="onHandleInput"
              required
            />
          </div>

          <!-- Verification Status -->
          <div v-if="verifying" class="flex items-center justify-center gap-2 text-slate-500 py-4">
            <Loader2 class="animate-spin h-5 w-5" />
            <span class="font-medium">아이디 확인 중...</span>
          </div>

          <!-- User Preview Card -->
          <div v-else-if="verifiedUser && !verifyError" class="relative">
            <div 
              class="bg-gradient-to-br from-slate-50 to-white border rounded-2xl p-5 transition-all"
              :class="confirmed ? 'border-emerald-400 shadow-lg shadow-emerald-100 ring-2 ring-emerald-50' : 'border-slate-200 shadow-sm'"
            >
              <div class="flex items-center gap-4">
                <!-- Profile Image -->
                <div class="w-16 h-16 rounded-full bg-gradient-to-br from-brand-500 to-purple-600 flex items-center justify-center text-2xl font-bold text-white shadow-lg overflow-hidden">
                  <img 
                    :src="verifiedUser.profileImageUrl || '/land/default_profile.png'" 
                    :alt="verifiedUser.handle"
                    class="w-full h-full object-cover"
                    @error="$event.target.src = '/land/default_profile.png'"
                  />
                </div>
                
                <!-- User Info -->
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <span class="font-bold text-lg text-slate-800">{{ verifiedUser.handle }}</span>
                    <span 
                      class="text-xs font-bold px-2 py-0.5 rounded-full"
                      :class="getTierBadgeClass(verifiedUser.tier)"
                    >
                      {{ verifiedUser.tierName }}
                    </span>
                  </div>
                  <div class="text-sm text-slate-500 mt-1 flex items-center gap-3">
                    <span class="flex items-center gap-1"><Puzzle :size="14"/> {{ verifiedUser.solvedCount || 0 }}문제 해결</span>
                    <span v-if="verifiedUser.classLevel" class="flex items-center gap-1"><BookOpen :size="14"/> Class {{ verifiedUser.classLevel }}</span>
                  </div>
                </div>

                <!-- Confirmed Badge -->
                <div v-if="confirmed" class="text-emerald-500">
                  <CheckCircle2 :size="28" />
                </div>
              </div>

              <!-- Confirm Button -->
              <button 
                v-if="!confirmed"
                type="button"
                @click="confirmUser"
                class="w-full mt-4 py-3 bg-emerald-500 hover:bg-emerald-600 text-white font-bold rounded-xl transition-all shadow-md shadow-emerald-200 hover:shadow-lg hover:-translate-y-0.5"
              >
                ✓ 이 계정이 맞습니다
              </button>

              <!-- Change Button -->
              <button 
                v-else
                type="button"
                @click="resetConfirmation"
                class="w-full mt-4 py-2 text-slate-500 hover:text-slate-700 font-medium transition-colors text-sm"
              >
                다른 계정으로 변경
              </button>
            </div>
          </div>

          <!-- Error State -->
          <div v-else-if="verifyError" class="bg-red-50 border border-red-200 rounded-2xl p-4 text-center">
            <p class="text-red-600 font-medium">{{ verifyError }}</p>
            <p class="text-red-400 text-sm mt-1">핸들을 다시 확인해주세요.</p>
          </div>

          <button
            type="submit"
            class="w-full bg-gradient-to-r from-brand-600 to-brand-500 hover:from-brand-500 hover:to-brand-400 text-white font-bold py-4 rounded-2xl transition-all shadow-lg shadow-brand-500/30 hover:shadow-brand-500/40 hover:-translate-y-0.5 disabled:opacity-70 disabled:cursor-not-allowed disabled:transform-none flex items-center justify-center gap-2"
            :disabled="loading || !confirmed"
          >
            <Loader2 v-if="loading" class="animate-spin h-5 w-5 text-white" />
            <span class="flex items-center gap-2" v-else>
                {{ confirmed ? '다음으로' : '위에서 계정을 확인해주세요' }} <ArrowRight v-if="confirmed" :size="20"/>
            </span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { onboardingApi } from '../../api/onboarding';
import { useAuth } from '../../composables/useAuth';
import { Loader2, Puzzle, BookOpen, Check, ArrowRight, CheckCircle2 } from 'lucide-vue-next';

const router = useRouter();
const { user } = useAuth();

const handle = ref('');
const loading = ref(false);
const verifying = ref(false);
const verifiedUser = ref(null);
const verifyError = ref(null);
const confirmed = ref(false);

let debounceTimer = null;

const onHandleInput = () => {
  // Reset states
  verifiedUser.value = null;
  verifyError.value = null;
  confirmed.value = false;
  
  // Debounce verification
  clearTimeout(debounceTimer);
  if (handle.value.trim().length >= 2) {
    debounceTimer = setTimeout(() => {
      verifyHandle();
    }, 500);
  }
};

const verifyHandle = async () => {
  if (!handle.value.trim()) return;
  
  verifying.value = true;
  verifyError.value = null;
  
  try {
    const res = await onboardingApi.verifySolvedac(handle.value.trim());
    verifiedUser.value = res.data;
  } catch (error) {
    console.error(error);
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

const getTierBadgeClass = (tier) => {
  if (!tier || tier === 0) return 'bg-slate-100 text-slate-600';
  if (tier <= 5) return 'bg-amber-700/20 text-amber-800'; // Bronze
  if (tier <= 10) return 'bg-slate-300/50 text-slate-700'; // Silver
  if (tier <= 15) return 'bg-yellow-400/30 text-yellow-700'; // Gold
  if (tier <= 20) return 'bg-cyan-400/30 text-cyan-700'; // Platinum
  if (tier <= 25) return 'bg-blue-400/30 text-blue-700'; // Diamond
  if (tier <= 30) return 'bg-rose-400/30 text-rose-700'; // Ruby
  return 'bg-purple-400/30 text-purple-700'; // Master
};

const submitHandle = async () => {
  if (!handle.value || !confirmed.value) return;
  
  loading.value = true;
  try {
    let profileImage = null;

    // GitHub 아바타가 없고, 기존 프로필 이미지도 없는 경우에만 랜덤 이미지 사용
    if (!user.value?.avatarUrl) {
         const profileImages = [
          '/profile/bag.png',
          '/profile/proud.png',
          '/profile/smart.png',
          '/profile/smile.png'
        ];
        profileImage = profileImages[Math.floor(Math.random() * profileImages.length)];
    }
    
    // profileImage가 null이면 백엔드에서 기존(GitHub) 아바타를 유지함
    await onboardingApi.registerSolvedac(handle.value, profileImage);
    router.push('/onboarding/analysis');
  } catch (error) {
    console.error(error);
    alert('아이디 연동에 실패했습니다. 올바른 아이디인지 확인해주세요.');
  } finally {
    loading.value = false;
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
