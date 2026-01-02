<template>
  <div class="relative w-full h-full">
    <!-- Loading State -->
    <div v-if="loading" class="min-h-screen flex items-center justify-center bg-slate-50">
      <div class="text-center">
        <div class="w-12 h-12 border-4 border-brand-200 border-t-brand-600 rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-slate-500 font-medium">온보딩 상태 확인 중...</p>
      </div>
    </div>

    <transition v-else name="fade-slide" mode="out-in">
      <component 
        :is="currentStepComponent" 
        @next="nextStep"
        @finish="finishOnboarding"
      />
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import OnboardingWelcome from './OnboardingWelcome.vue';
import OnboardingAnalysis from './OnboardingAnalysis.vue';
import OnboardingStudy from './OnboardingStudy.vue';
import OnboardingRepository from './OnboardingRepository.vue';

const router = useRouter();
const { user, refresh } = useAuth();
const loading = ref(true);
const steps = ['welcome', 'analysis', 'study', 'repo'];
const currentStepIndex = ref(0);
const hasNavigatedManually = ref(false); // 사용자가 '다음'을 클릭했는지 추적

const currentStepComponent = computed(() => {
  switch (steps[currentStepIndex.value]) {
    case 'welcome': return OnboardingWelcome;
    case 'analysis': return OnboardingAnalysis;
    case 'study': return OnboardingStudy;
    case 'repo': return OnboardingRepository;
    default: return OnboardingWelcome;
  }
});

// 초기 마운트 시에만 호출되어 백엔드 상태 복원
const determineInitialStep = () => {
  if (!user.value) {
    currentStepIndex.value = 0; // welcome
    return;
  }

  // Solved.ac 연동 여부 확인 (solvedacId 또는 solvedacHandle 확인)
  const hasSolvedac = user.value.solvedacId || user.value.solvedacHandle;
  const hasStudy = !!user.value.studyId;
  const hasRepo = !!user.value.repositoryName;

  // 사용자가 모든 단계를 완료한 경우 -> 대시보드로 리다이렉트
  if (hasSolvedac && hasStudy && hasRepo) {
    router.replace('/dashboard');
    return;
  }

  // solvedac 연동 안됨 -> 환영 단계
  if (!hasSolvedac) {
    currentStepIndex.value = 0; // welcome
    return;
  }

  // solvedac 있음, study 없음 -> 분석 단계 (새로고침 시 분석 페이지 보여주기)
  if (!hasStudy) {
    currentStepIndex.value = 1; // analysis
    return;
  }

  // study 있음, repo 없음 -> 저장소 단계
  if (!hasRepo) {
    currentStepIndex.value = 3; // repo
    return;
  }
};

onMounted(async () => {
  await refresh();
  determineInitialStep();
  loading.value = false;
});

// 오직 승인(studyId 업데이트) 감지를 위해서만 사용자 변경 감시
// 이는 RESET이 아니라 ADVANCE(진행)만 해야 함
watch(() => user.value?.studyId, (newStudyId, oldStudyId) => {
  if (!loading.value && !oldStudyId && newStudyId) {
    // 사용자가 스터디 승인을 받음 -> 저장소 단계로 진행
    currentStepIndex.value = 3;
  }
});

const nextStep = () => {
  hasNavigatedManually.value = true;
  
  // Current Step: Welcome (0) -> Next: Analysis (1)
  // Current Step: Analysis (1) -> Next: Study (2) OR Repo (3) if study done
  // Current Step: Study (2) -> Next: Repo (3)
  
  if (currentStepIndex.value < steps.length - 1) {
    // If moving from Analysis (1) to Study (2), check if study is already done
    if (currentStepIndex.value === 1 && user.value?.studyId) {
      currentStepIndex.value = 3; // Skip Study, go to Repo
    } else {
      currentStepIndex.value++;
    }
  }
};

const finishOnboarding = async (repoName) => {
  await refresh();
  
  // Guard Bypass: If backend is slow to update, optimistically set the repo name locally
  // so the Router Guard allows access to Dashboard.
  if (user.value && !user.value.repositoryName && repoName) {
    user.value.repositoryName = repoName;
  }
  
  router.replace('/dashboard');
};
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
