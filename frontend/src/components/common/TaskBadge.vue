<template>
  <span 
    class="flex items-center gap-1 px-2 py-0.5 rounded-lg text-xs font-medium"
    :class="badgeClass"
  >
    <component v-if="iconComponent" :is="iconComponent" :size="size" />
    <slot>{{ label }}</slot>
  </span>
</template>

<script setup>
import { computed } from 'vue';
import { BookOpen, Shield, Clock, Code2 } from 'lucide-vue-next';

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => ['MISSION', 'DEFENSE', 'MOCK_EXAM', 'GENERAL', 'FREE'].includes(value)
  },
  size: {
    type: Number,
    default: 12
  }
});

const badgeConfig = {
  MISSION: {
    label: '과제',
    icon: BookOpen,
    class: 'text-brand-700 bg-brand-100'
  },
  DEFENSE: {
    label: '디펜스',
    icon: Shield,
    class: 'text-indigo-600 bg-indigo-50'
  },
  MOCK_EXAM: {
    label: '모의고사',
    icon: Clock,
    class: 'text-orange-600 bg-orange-50'
  },
  GENERAL: {
    label: '자유',
    icon: Code2,
    class: 'text-slate-500 bg-slate-100'
  },
  FREE: { // Alias for GENERAL
    label: '자유',
    icon: Code2,
    class: 'text-slate-500 bg-slate-100'
  }
};

const currentConfig = computed(() => badgeConfig[props.type] || badgeConfig.GENERAL);

const badgeClass = computed(() => currentConfig.value.class);
const iconComponent = computed(() => currentConfig.value.icon);
const label = computed(() => currentConfig.value.label);
</script>
