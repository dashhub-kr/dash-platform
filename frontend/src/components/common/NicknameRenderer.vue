<template>
  <span 
    class="inline-flex items-center gap-1.5 transition-all duration-300"
    :class="computedClasses"
  >
    <!-- Decoration styling is applied via class prop passed to this span -->
    <span :class="decorationClass">
      {{ isUnknown ? '탈퇴한 회원' : nickname }}
    </span>
    
    <!-- Admin Badge (Optional, mostly handled by effect but can be explicit) -->
    <span 
      v-if="role === 'ROLE_ADMIN'" 
      class="inline-flex items-center px-1 py-0.5 rounded text-[10px] bg-slate-800 text-yellow-400 font-bold leading-none shadow-sm"
    >
      ADMIN
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  nickname: {
    type: String,
    required: true,
  },
  role: {
    type: String,
    default: 'ROLE_USER',
  },
  decorationClass: {
    type: String,
    default: null,
  },
  verified: {
    type: Boolean,
    default: false
  }
});

const isUnknown = computed(() => {
  return ['Unknown', 'Unknown User'].includes(props.nickname);
});

const computedClasses = computed(() => {
  return [
    // Add generalized classes if needed, but mainly we use the decorationClass on the inner span
  ];
});
</script>

<style scoped>
/* Scoped styles if needed, but effects.css should be global */
</style>
