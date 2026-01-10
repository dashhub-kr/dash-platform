<template>
    <div 
        class="inline-flex items-center gap-1 font-bold"
        :class="[sizeClasses, !compact && 'px-2 py-0.5 rounded-lg', tierBgClass]"
    >
        <span :class="tierTextClass">{{ tierName }}</span>
        <span v-if="!compact && showRoman" class="opacity-75">{{ tierRoman }}</span>
    </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
    tier: {
        type: Number,
        default: 0
    },
    compact: {
        type: Boolean,
        default: false
    },
    showRoman: {
        type: Boolean,
        default: true
    },
    size: {
        type: String,
        default: 'sm' // 'xs', 'sm', 'md'
    }
});

// Tier 데이터 (Solved.ac 기준)
// 0: Unrated, 1-5: Bronze, 6-10: Silver, 11-15: Gold, 16-20: Platinum, 21-25: Diamond, 26-30: Ruby

const tierData = computed(() => {
    const t = props.tier || 0;
    
    if (t === 0) return { name: 'Unrated', color: 'slate', level: 0 };
    if (t <= 5) return { name: 'Bronze', color: 'amber', level: 6 - t };
    if (t <= 10) return { name: 'Silver', color: 'slate', level: 11 - t };
    if (t <= 15) return { name: 'Gold', color: 'yellow', level: 16 - t };
    if (t <= 20) return { name: 'Platinum', color: 'teal', level: 21 - t };
    if (t <= 25) return { name: 'Diamond', color: 'sky', level: 26 - t };
    if (t <= 30) return { name: 'Ruby', color: 'rose', level: 31 - t };
    return { name: 'Master', color: 'red', level: 0 };
});

const tierName = computed(() => tierData.value.name);

const tierRoman = computed(() => {
    const romans = ['', 'I', 'II', 'III', 'IV', 'V'];
    return romans[tierData.value.level] || '';
});

const tierBgClass = computed(() => {
    const colorMap = {
        slate: 'bg-slate-100',
        amber: 'bg-amber-100',
        yellow: 'bg-yellow-100',
        teal: 'bg-teal-100',
        sky: 'bg-sky-100',
        rose: 'bg-rose-100',
        red: 'bg-red-100'
    };
    return props.compact ? '' : colorMap[tierData.value.color] || 'bg-slate-100';
});

const tierTextClass = computed(() => {
    const colorMap = {
        slate: 'text-slate-600',
        amber: 'text-amber-700',
        yellow: 'text-yellow-700',
        teal: 'text-teal-700',
        sky: 'text-sky-700',
        rose: 'text-rose-600',
        red: 'text-red-600'
    };
    return colorMap[tierData.value.color] || 'text-slate-600';
});

const sizeClasses = computed(() => {
    const sizes = {
        xs: 'text-[10px]',
        sm: 'text-xs',
        md: 'text-sm'
    };
    return sizes[props.size] || sizes.sm;
});
</script>
