<template>
    <div class="flex items-center gap-2" :class="containerClass">
        <!-- Avatar -->
        <template v-if="showAvatar">
            <div v-if="isUnknown" class="rounded-full flex items-center justify-center text-slate-400 bg-slate-100 border border-slate-200" :class="avatarClass">
                <UserX :size="iconSize" />
            </div>
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
            class="font-medium truncate" 
            :class="[
                textClass, 
                decorationClass,
                isUnknown ? 'text-slate-400' : 'text-slate-700',
                { 'admin-shining-text': isAdmin }
            ]"
        >
            {{ displayName }}
        </span>
    </div>
</template>

<script setup>
import { computed } from 'vue';
import { UserX } from 'lucide-vue-next';

const props = defineProps({
    nickname: { type: String, default: '' }, 
    username: { type: String, default: '' }, 
    avatarUrl: { type: String, default: null },
    role: { type: String, default: null },
    decorationClass: { type: String, default: null }, 
    verified: { type: Boolean, default: false },
    
    showAvatar: { type: Boolean, default: true },
    avatarClass: { type: String, default: 'w-6 h-6' },
    textClass: { type: String, default: 'text-xs' },
    containerClass: { type: String, default: '' },
    iconSize: { type: Number, default: 14 }
});

const effectiveNickname = computed(() => props.nickname || props.username);

const isUnknown = computed(() => {
    const name = effectiveNickname.value;
    return !name || ['Unknown', 'Unknown User', '탈퇴한 회원'].includes(name);
});

const displayName = computed(() => {
    return isUnknown.value ? '탈퇴한 회원' : effectiveNickname.value;
});

const isAdmin = computed(() => props.role === 'ROLE_ADMIN');
</script>

<style scoped>
.admin-shining-text {
  background: linear-gradient(90deg, #d97706, #fbbf24, #d97706);
  background-size: 200% auto;
  color: transparent;
  -webkit-background-clip: text;
  background-clip: text;
  animation: shine 3s linear infinite;
  font-weight: 800;
}

@keyframes shine {
  to {
    background-position: 200% center;
  }
}
</style>
