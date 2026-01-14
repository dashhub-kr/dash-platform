<template>
    <div class="bg-white rounded-2xl border border-slate-200 p-6 hover:border-slate-300 transition-colors w-full">
        <!-- 헤더: 유저 정보 + 시간 -->
        <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
                <img :src="getAvatar(item.userAvatar)" class="w-10 h-10 rounded-full border border-slate-200"/>
                <div>
                    <div class="flex items-center gap-2">
                        <span class="font-bold text-slate-800" :class="item.userDecorationClass">{{ item.userName }}</span>
                        <TierBadge v-if="item.userTier" :tier="item.userTier" size="xs" />
                    </div>
                    <span class="text-xs text-slate-400">{{ formatTimeAgo(item.createdAt) }}</span>
                </div>
            </div>
            <div 
                class="w-8 h-8 rounded-lg flex items-center justify-center"
                :class="typeIconBg"
            >
                <component :is="typeIcon" :size="16" class="text-white" />
            </div>
        </div>

        <!-- 콘텐츠: 타입별 -->
        <!-- SOLVED -->
        <div v-if="item.type === 'SOLVED'">
            <p class="text-slate-600">
                <a :href="getProblemLink(item.problemId, item.platform)" target="_blank" class="font-bold text-slate-800 hover:text-brand-600 hover:underline transition-colors">#{{ item.problemId }} {{ item.problemTitle }}</a> 문제를 풀었어요! 🎉
            </p>
        </div>

        <!-- STREAK -->
        <div v-else-if="item.type === 'STREAK'" class="flex items-center gap-2">
            <div class="text-3xl">🔥</div>
            <div>
                <p class="text-slate-800 font-bold">연속 {{ item.streakDays }}일 달성!</p>
                <p class="text-sm text-slate-500">꾸준히 문제를 풀고 있어요</p>
            </div>
        </div>

        <!-- BATTLE -->
        <div v-else-if="item.type === 'BATTLE'" class="space-y-3">
            <div class="flex items-center gap-3 p-3 bg-gradient-to-r from-violet-50 to-pink-50 rounded-xl">
                <div class="text-2xl">⚔️</div>
                <div>
                    <p class="text-slate-800 font-bold">{{ item.battleType === 'MOCK_EXAM' ? '모의고사' : '디펜스' }} 배틀 완료!</p>
                    <p class="text-sm text-slate-500">
                        {{ item.isWinner ? '🏆 승리!' : item.isDraw ? '🤝 무승부' : '도전 완료' }}
                    </p>
                </div>
            </div>
            <button 
                @click="$emit('view-battle', item)"
                class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-violet-100 hover:bg-violet-200 text-violet-700 rounded-lg text-sm font-bold transition-colors"
            >
                <Trophy :size="14" />
                결과 보기
            </button>
        </div>

        <!-- CHALLENGE_RECEIVED (받은 도전장) -->
        <div v-else-if="item.type === 'CHALLENGE_RECEIVED'" class="space-y-3">
            <div class="flex items-center gap-3 p-3 bg-gradient-to-r from-amber-50 to-orange-50 rounded-xl">
                <div class="text-2xl">🏅</div>
                <div>
                    <p class="text-slate-800 font-bold">도전장이 도착했어요!</p>
                    <p class="text-sm text-slate-500">{{ item.battleType === 'MOCK_EXAM' ? '모의고사' : '디펜스' }} · {{ item.problemCount }}문제</p>
                </div>
            </div>
            <div class="flex gap-2">
                <button 
                    @click="$emit('accept-challenge', item)"
                    class="inline-flex items-center gap-1.5 px-4 py-2 bg-brand-500 hover:bg-brand-600 text-white rounded-xl text-sm font-bold transition-colors"
                >
                    수락하기
                </button>
                <button 
                    @click="$emit('decline-challenge', item)"
                    class="inline-flex items-center gap-1.5 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-xl text-sm font-bold transition-colors"
                >
                    거절
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue';
import { Code, Flame, Swords, Trophy } from 'lucide-vue-next';
import TierBadge from '@/components/common/TierBadge.vue';

const props = defineProps({
    item: {
        type: Object,
        required: true
    }
});

defineEmits(['view-battle', 'accept-challenge', 'decline-challenge']);

const getAvatar = (url) => {
    if (url && !url.includes('dicebear')) return url;
    return '/images/profiles/default-profile.png';
};

const typeIcon = computed(() => {
    switch (props.item.type) {
        case 'SOLVED': return Code;
        case 'STREAK': return Flame;
        case 'BATTLE': 
        case 'CHALLENGE_RECEIVED': return Swords;
        default: return Code;
    }
});

const typeIconBg = computed(() => {
    switch (props.item.type) {
        case 'SOLVED': return 'bg-emerald-500';
        case 'STREAK': return 'bg-orange-500';
        case 'BATTLE': 
        case 'CHALLENGE_RECEIVED': return 'bg-violet-500';
        default: return 'bg-slate-500';
    }
});

const formatTimeAgo = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return '방금 전';
    if (minutes < 60) return `${minutes}분 전`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}시간 전`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}일 전`;
    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
};

const getProblemLink = (problemId, platform) => {
    const p = platform?.toLowerCase();
    if (p === 'swea') {
        // SWEA는 고유 ID가 필요하므로 검색 페이지로 연결
        return `https://swexpertacademy.com/main/searchAll/searchMore.do?category=CODE&pageIndex=1&keyword=${problemId}`;
    }
    // 기본: 백준
    return `https://www.acmicpc.net/problem/${problemId}`;
};
</script>
