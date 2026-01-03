<template>
  <div class="relative w-full h-full">
    <!-- Loading Screen (Initial Check) -->
    <div v-if="checkingStatus" class="min-h-screen flex items-center justify-center bg-slate-50">
       <!-- Simple Loading Spinner -->
       <div class="w-10 h-10 border-4 border-slate-200 border-t-brand-500 rounded-full animate-spin"></div>
    </div>

    <!-- 단계별 화면 (Step Views) -->
    <transition name="fade-slide" mode="out-in">
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

// 새로운 단계 컴포넌트 임포트
import Step1SolvedAc from './OnboardingStep1SolvedAc.vue';
import Step2Analysis from './OnboardingStep2Analysis.vue';
import Step3Study from './OnboardingStep3Study.vue';
import Step4Extension from './OnboardingStep4Extension.vue';
import Step5Repo from './OnboardingStep5Repo.vue';
import Step6Guide from './OnboardingStep6Guide.vue';

const router = useRouter();
const { user, refresh } = useAuth();

const checkingStatus = ref(true);
const currentStepIndex = ref(0);

// 단계 매핑 (Step Mapping)
const steps = [
  'solvedac',   // 0
  'analysis',   // 1
  'study',      // 2
  'extension',  // 3
  'repo',       // 4
  'guide'       // 5
];

const currentStepComponent = computed(() => {
  switch (steps[currentStepIndex.value]) {
    case 'solvedac': return Step1SolvedAc;
    case 'analysis': return Step2Analysis;
    case 'study': return Step3Study;
    case 'extension': return Step4Extension;
    case 'repo': return Step5Repo;
    case 'guide': return Step6Guide;
    default: return Step1SolvedAc;
  }
});

const determineInitialStep = () => {
  if (!user.value) {
    currentStepIndex.value = 0;
    return;
  }

  const hasSolvedac = user.value.solvedacId || user.value.solvedacHandle;
  const hasStudy = !!user.value.studyId;
  const hasRepo = !!user.value.repositoryName;

  // 1. Solved.ac 확인
  if (!hasSolvedac) {
    currentStepIndex.value = 0;
    return;
  }

  // 2. 스터디 확인
  if (!hasStudy) {
    // Solved.ac는 있지만 스터디가 없는 경우, 분석(1) -> 스터디(2) 허용
    // 스터디가 없을 때마다 사용자가 다시 돌아오면 분석을 보여주고 싶은 경우:
    currentStepIndex.value = 1; 
    return;
  }

  // 3. 리포지토리 확인
  if (!hasRepo) {
    // 스터디는 있지만 리포지토리가 없는 경우.
    // 익스텐션 가이드(3)를 보여줄까요 아니면 리포지토리(4)를 보여줄까요?
    // 안전하고 도움이 되도록 익스텐션 가이드(3)를 보여줍니다.
    currentStepIndex.value = 3;
    return;
  }

  // 4. 모두 완료 -> 가이드(5) 또는 대시보드
  // 사용자가 여기 도착했지만 이미 완료된 경우, 가이드를 보여주거나 리다이렉트할까요?
  // 루프를 피하기 위해 대시보드로 리다이렉트하거나, 요청된 경우 가이드로 이동합니다.
  // 하지만 보통 OnboardingView는 가드되어 있습니다. 가드된 경우 강제로 머무르지 않는 한 리다이렉트될 수 있습니다.
  // 멋진 마무리를 위해 가이드로 가거나 대시보드로 이동합니다.
  router.replace('/dashboard');
};

onMounted(async () => {
  await refresh();
  determineInitialStep();
  // Brief delay to prevent flicker if component swaps fast
  setTimeout(() => {
    checkingStatus.value = false;
  }, 300);
});

// 외부 상태 변경 감지 (예: 페이지에 있는 동안 스터디 승인됨)
watch(() => user.value?.studyId, (newId, oldId) => {
    // 사용자가 스터디 단계(2)에 멈춰있다가 승인된 경우
    if (currentStepIndex.value === 2 && !oldId && newId) {
        nextStep(); // 익스텐션 단계로 이동
    }
});

const nextStep = () => {
  if (currentStepIndex.value < steps.length - 1) {
    currentStepIndex.value++;
  } else {
    finishOnboarding();
  }
};

const finishOnboarding = async (repoName) => {
  // repoName이 전달된 경우(Step 5에서), 로컬 상태를 낙관적으로 업데이트
  if (repoName && user.value) {
      user.value.repositoryName = repoName;
  }
  
  // Step 5에서 호출하는 경우, 단계가 하나 더 남았습니다: 가이드(5)
  // 잠시만요, currentStepIndex 로직은 5(Guide)까지 처리합니다.
  // Step 5 Repo가 'finish'를 호출하면 repoName과 함께 호출합니까? 
  // Step5Repo 내부를 보면 Step6Guide로 가고 싶어 합니다.
  
  // 따라서 Step 5의 'finish' 이벤트는 실제로 'nextStep' 로직을 트리거하여 가이드로 가야 합니다.
  // 아니면 'finishOnboarding'이 최종 리다이렉트를 처리합니까?
  
  // 로직 수정:
  // Step 5 (Repo) -> 'finish' 방출 -> 부모가 처리.
  // 'repo' 단계(인덱스 4)에 있다면, 'finish'는 'guide'(인덱스 5)로 가는 것을 의미합니다.
  if (steps[currentStepIndex.value] === 'repo') {
      if (repoName && user.value) user.value.repositoryName = repoName;
      currentStepIndex.value++; // 가이드로 이동
      return;
  }

  // Step 6 (Guide) -> 'finish' 방출 -> 라우터로 리다이렉트
  if (steps[currentStepIndex.value] === 'guide') {
      router.replace('/dashboard');
  }
};
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>
