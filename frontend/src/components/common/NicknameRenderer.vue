<template>
    <div class="flex items-center gap-2" :class="containerClass">
        <!-- Avatar -->
        <template v-if="showAvatar">
            <img v-if="isUnknown" 
                src="https://avatars.githubusercontent.com/u/0" 
                class="rounded-full object-cover border border-slate-200 grayscale opacity-60" 
                :class="avatarClass"
            />
            <img v-else-if="avatarUrl" 
                :src="avatarUrl" 
                class="rounded-full object-cover border border-brand-100" 
                :class="avatarClass"
            />
            <div v-else 
                class="rounded-full bg-brand-50 flex items-center justify-center text-brand-600 font-bold border border-brand-100"
                :class="avatarClass"
            >
                {{ nickname?.charAt(0).toUpperCase() || username?.charAt(0).toUpperCase() || 'U' }}
            </div>
        </template>

        <!-- Username / Nickname -->
        <span 
            v-if="showText"
            class="font-medium truncate" 
            :class="[
                textClass, 
                enableDecoration ? decorationClass : '',
                isUnknown ? 'text-slate-400' : 'text-slate-700',
                { 'cursor-pointer hover:underline hover:text-brand-600': clickable && !isUnknown }
            ]"
            @click="handleClick"
        >
            {{ displayName }}
        </span>
    </div>
</template>

<script setup>
import { computed } from 'vue';
import { UserX } from 'lucide-vue-next';
import { useUserProfileModal } from '@/composables/useUserProfileModal';

const props = defineProps({
    nickname: { type: String, default: '' }, 
    username: { type: String, default: '' }, 
    userId: { type: Number, default: null }, // NEW
    avatarUrl: { type: String, default: null },
    role: { type: String, default: null },
    decorationClass: { type: String, default: null }, 
    verified: { type: Boolean, default: false },
    
    showAvatar: { type: Boolean, default: true },
    showText: { type: Boolean, default: true },
    clickable: { type: Boolean, default: false }, // NEW
    avatarClass: { type: String, default: 'w-6 h-6' },
    textClass: { type: String, default: 'text-xs' },
    containerClass: { type: String, default: '' },
    iconSize: { type: Number, default: 14 },
    isDeleted: { type: Boolean, default: false },
    enableDecoration: { type: Boolean, default: false } // NEW
});

const { open } = useUserProfileModal();

const handleClick = (e) => {
    if (props.clickable && !isUnknown.value && props.userId) {
        e.stopPropagation();
        open({
            userId: props.userId,
            nickname: props.nickname,
            username: props.username,
            avatarUrl: props.avatarUrl,
            role: props.role,
            decorationClass: props.decorationClass,
            isDeleted: props.isDeleted
            // email is not usually passed here, can be fetched if needed or just omitted
        });
    }
};

const effectiveNickname = computed(() => props.nickname || props.username);

const isUnknown = computed(() => {
    if (props.isDeleted) return true;
    const name = effectiveNickname.value;
    return !name || ['Unknown', 'Unknown User', '탈퇴한 회원'].includes(name);
});

const displayName = computed(() => {
    return isUnknown.value ? '탈퇴한 회원' : effectiveNickname.value;
});


</script>

<style scoped>
</style>
