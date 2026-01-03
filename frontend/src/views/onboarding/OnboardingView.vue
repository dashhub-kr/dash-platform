<template>
  <div class="relative w-full h-full">
    <!-- Loading Screen (Initial Check) -->
    <div v-if="checkingStatus" class="min-h-screen flex items-center justify-center bg-slate-50">
       <!-- Simple Loading Spinner -->
       <div class="w-10 h-10 border-4 border-slate-200 border-t-brand-500 rounded-full animate-spin"></div>
    </div>

    <!-- Step Views -->
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

// Import New Steps
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

// Step Mapping
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

  // 1. Solved.ac Check
  if (!hasSolvedac) {
    currentStepIndex.value = 0;
    return;
  }

  // 2. Study Check
  if (!hasStudy) {
    // If has Solved.ac but no study, allow Analysis (1) -> Study (2)
    // If we want to show Analysis every time the user comes back here without a study:
    currentStepIndex.value = 1; 
    return;
  }

  // 3. Repo Check
  if (!hasRepo) {
    // Has Study but no Repo.
    // Should we show Extension Guide (3) or Repo (4)?
    // Let's show Extension Guide (3) to be safe/helpful.
    currentStepIndex.value = 3;
    return;
  }

  // 4. All Done -> Guide (5) or Dashboard
  // If user lands here but is done, maybe show Guide or redirect?
  // Let's redirect to Dashboard to avoid loop, or Guide if requested?
  // But usually OnboardingView is guarded. If guarded, it might redirect unless we force stay.
  // Let's just go to Guide for a nice finish or Dashboard.
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

// Watch for external state changes (e.g. Study Approved while on page)
watch(() => user.value?.studyId, (newId, oldId) => {
    // If user was stuck on Study Step (2) and got approved
    if (currentStepIndex.value === 2 && !oldId && newId) {
        nextStep(); // Move to Extension
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
  // If repoName is passed (from Step 5), update local state optimistically
  if (repoName && user.value) {
      user.value.repositoryName = repoName;
  }
  
  // If calling from Step 5, we have one more step: Guide (5)
  // Wait, currentStepIndex logic handles up to index 5 (Guide).
  // Step 5 Repo calls 'finish' with repoName? No, Step 5 should call 'next' or emit 'finish' to go to next step?
  // Let's check Step5Repo.vue emits 'finish'.
  // Inside Step5Repo, we want to go to Step6Guide.
  
  // So Step 5 'finish' event should actually trigger 'nextStep' logic to go to Guide?
  // Or 'finishOnboarding' handles the final redirect?
  
  // Logic Fix:
  // Step 5 (Repo) -> Emit 'finish' -> Parent handles.
  // If we are at 'repo' step (index 4), 'finish' means go to 'guide' (index 5).
  if (steps[currentStepIndex.value] === 'repo') {
      if (repoName && user.value) user.value.repositoryName = repoName;
      currentStepIndex.value++; // Go to Guide
      return;
  }

  // Step 6 (Guide) -> Emit 'finish' -> Redirect via Router
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
