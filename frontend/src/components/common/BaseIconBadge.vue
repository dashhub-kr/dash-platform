<template>
  <div 
    class="flex items-center justify-center rounded-xl transition-all"
    :class="[
      sizeClasses[size].container,
      colorClasses[color] || colorClasses.slate,
      text ? 'px-3 gap-1.5' : ''
    ]"
  >
    <component :is="icon" :size="sizeClasses[size].icon" fill="currentColor" />
    <span v-if="text" class="font-black pt-[1px] tracking-tight">{{ text }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  icon: {
    type: Object,
    required: true
  },
  text: {
    type: [String, Number],
    default: ''
  },
  color: {
    type: String, // 'brand', 'rose', 'orange', 'emerald', 'yellow', 'slate'
    default: 'slate'
  },
  size: {
    type: String, // 'sm', 'md', 'lg'
    default: 'md'
  }
});

const sizeClasses = {
  sm: { container: 'h-8 px-2 text-xs', icon: 14 },
  md: { container: 'h-10 px-3 text-sm', icon: 18 },
  lg: { container: 'h-12 px-4 text-base', icon: 24 },
  xl: { container: 'h-16 px-6 text-xl', icon: 32 }
};

const colorClasses = {
  brand: 'bg-brand-100 text-brand-600',
  emerald: 'bg-leaf/10 text-leaf', // Leaf (커스텀 색상 사용)
  green: 'bg-leaf/10 text-leaf',
  orange: 'bg-fox/10 text-fox', // Fox
  fox: 'bg-fox/10 text-fox', // Fox Alias
  yellow: 'bg-bee/10 text-bee', // Bee
  bee: 'bg-bee/10 text-bee', // Bee Alias
  rose: 'bg-rose-100 text-rose-600',
  indigo: 'bg-brand-100 text-brand-600', // 브랜드 색상으로 대체
  slate: 'bg-slate-100 text-slate-500'
};
</script>
