<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="close"></div>
      
      <!-- Modal -->
      <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden animate-fade-in-up">
        <!-- Header -->
        <div class="bg-gradient-to-r from-violet-500 to-purple-600 px-6 py-5">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <Swords class="w-6 h-6 text-white" />
              <h2 class="text-xl font-bold text-white">배틀 초대</h2>
            </div>
            <button @click="close" class="text-white/70 hover:text-white transition-colors">
              <X class="w-6 h-6" />
            </button>
          </div>
          <p class="text-white/80 text-sm mt-1">
            {{ battleType === 'DEFENSE' ? '디펜스' : '모의고사' }} 배틀에 초대할 친구를 선택하세요
          </p>
        </div>

        <!-- Tab Navigation -->
        <div class="flex border-b border-slate-200">
          <button
            @click="activeTab = 'friends'"
            :class="[
              'flex-1 py-3 text-sm font-semibold transition-colors',
              activeTab === 'friends' 
                ? 'text-violet-600 border-b-2 border-violet-600' 
                : 'text-slate-500 hover:text-slate-700'
            ]"
          >
            <Users class="w-4 h-4 inline-block mr-1" />
            친구 ({{ friends.length }})
          </button>
          <button
            v-if="hasStudy"
            @click="activeTab = 'study'"
            :class="[
              'flex-1 py-3 text-sm font-semibold transition-colors',
              activeTab === 'study' 
                ? 'text-violet-600 border-b-2 border-violet-600' 
                : 'text-slate-500 hover:text-slate-700'
            ]"
          >
            <BookOpen class="w-4 h-4 inline-block mr-1" />
            스터디 ({{ studyMembers.length }})
          </button>
        </div>

        <!-- Content -->
        <div class="p-6 max-h-80 overflow-y-auto">
          <!-- Loading -->
          <div v-if="loading" class="flex justify-center py-8">
            <div class="w-8 h-8 border-4 border-violet-200 border-t-violet-600 rounded-full animate-spin"></div>
          </div>

          <!-- Friends List -->
          <div v-else-if="activeTab === 'friends'" class="space-y-2">
            <div v-if="friends.length === 0" class="text-center py-8 text-slate-400">
              친구가 없습니다
            </div>
            <label
              v-for="friendItem in friends"
              :key="friendItem.friend.id"
              class="flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 cursor-pointer transition-colors"
              :class="{ 'bg-violet-50 ring-2 ring-violet-200': isSelected(friendItem.friend.id) }"
            >
              <input
                type="checkbox"
                :checked="isSelected(friendItem.friend.id)"
                @change="toggleSelection(friendItem.friend.id)"
                :disabled="!isSelected(friendItem.friend.id) && selectedIds.length >= 3"
                class="w-5 h-5 text-violet-600 rounded focus:ring-violet-500"
              />
              <img 
                :src="friendItem.friend.avatarUrl || 'https://api.dicebear.com/7.x/initials/svg?seed=' + friendItem.friend.username" 
                class="w-10 h-10 rounded-full object-cover"
                @error="e => e.target.src = 'https://api.dicebear.com/7.x/initials/svg?seed=' + friendItem.friend.username"
              />
              <div class="flex-1 min-w-0">
                <div class="font-semibold text-slate-800 truncate">{{ friendItem.friend.username }}</div>
                <div class="text-xs text-slate-400">{{ getTierLabel(friendItem.friend.solvedacTier) }}</div>
              </div>
            </label>
          </div>

          <!-- Study Members List -->
          <div v-else-if="activeTab === 'study'" class="space-y-2">
            <div v-if="studyMembers.length === 0" class="text-center py-8 text-slate-400">
              스터디 멤버가 없습니다
            </div>
            <label
              v-for="member in studyMembers"
              :key="member.id"
              class="flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 cursor-pointer transition-colors"
              :class="{ 'bg-violet-50 ring-2 ring-violet-200': isSelected(member.id) }"
            >
              <input
                type="checkbox"
                :checked="isSelected(member.id)"
                @change="toggleSelection(member.id)"
                :disabled="!isSelected(member.id) && selectedIds.length >= 3"
                class="w-5 h-5 text-violet-600 rounded focus:ring-violet-500"
              />
              <img 
                :src="member.avatarUrl || 'https://api.dicebear.com/7.x/initials/svg?seed=' + member.username" 
                class="w-10 h-10 rounded-full object-cover"
                @error="e => e.target.src = 'https://api.dicebear.com/7.x/initials/svg?seed=' + member.username"
              />
              <div class="flex-1 min-w-0">
                <div class="font-semibold text-slate-800 truncate">{{ member.username }}</div>
                <div class="text-xs text-slate-400">{{ getTierLabel(member.solvedacTier) }}</div>
              </div>
            </label>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 bg-slate-50 border-t border-slate-200">
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm text-slate-500">
              {{ selectedIds.length }}/3명 선택됨
            </span>
            <span v-if="selectedIds.length >= 3" class="text-xs text-amber-600 font-medium">
              최대 인원 도달
            </span>
          </div>
          <button
            @click="sendInvite"
            :disabled="selectedIds.length === 0 || submitting"
            class="w-full py-3 px-4 bg-gradient-to-r from-violet-500 to-purple-600 text-white font-bold rounded-xl
                   hover:from-violet-600 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed
                   transition-all active:scale-95 flex items-center justify-center gap-2"
          >
            <template v-if="submitting">
              <div class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              초대 중...
            </template>
            <template v-else>
              <Send class="w-5 h-5" />
              초대 보내기
            </template>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Swords, X, Users, BookOpen, Send } from 'lucide-vue-next';
import { useAuth } from '@/composables/useAuth';
import { socialApi } from '@/api/social';
import { studyApi } from '@/api/study';
import { battleApi } from '@/api/battle';

const props = defineProps({
  isOpen: Boolean,
  battleType: {
    type: String,
    default: 'DEFENSE', // 'DEFENSE' or 'MOCK_EXAM'
  },
  battleSubType: {
    type: String,
    default: null, // 디펜스: 'SILVER'/'GOLD', 모의고사: 'IM'/'A'/'B' 등
  },
});

const emit = defineEmits(['close', 'created']);

const router = useRouter();
const { user } = useAuth();

const activeTab = ref('friends');
const friends = ref([]);
const studyMembers = ref([]);
const selectedIds = ref([]);
const loading = ref(false);
const submitting = ref(false);

const hasStudy = computed(() => !!user.value?.studyId);

// 데이터 로드
const loadData = async () => {
  if (!props.isOpen) return;
  
  loading.value = true;
  try {
    // 친구 목록
    const friendsRes = await socialApi.getFriends();
    friends.value = friendsRes.data || [];
    
    // 스터디 멤버 (스터디 있을 경우)
    if (hasStudy.value) {
      const membersRes = await studyApi.getMembers(user.value.studyId);
      // 자기 자신 제외
      studyMembers.value = (membersRes.data || []).filter(m => m.id !== user.value.id);
    }
  } catch (e) {
    console.error('Failed to load data:', e);
  } finally {
    loading.value = false;
  }
};

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    selectedIds.value = [];
    loadData();
  }
});

onMounted(() => {
  if (props.isOpen) loadData();
});

const isSelected = (id) => selectedIds.value.includes(id);

const toggleSelection = (id) => {
  const idx = selectedIds.value.indexOf(id);
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1);
  } else if (selectedIds.value.length < 3) {
    selectedIds.value.push(id);
  }
};

const getTierLabel = (tier) => {
  if (!tier || tier === 0) return '언랭크';
  const tiers = ['', 'Bronze V', 'Bronze IV', 'Bronze III', 'Bronze II', 'Bronze I',
    'Silver V', 'Silver IV', 'Silver III', 'Silver II', 'Silver I',
    'Gold V', 'Gold IV', 'Gold III', 'Gold II', 'Gold I',
    'Platinum V', 'Platinum IV', 'Platinum III', 'Platinum II', 'Platinum I',
    'Diamond V', 'Diamond IV', 'Diamond III', 'Diamond II', 'Diamond I',
    'Ruby V', 'Ruby IV', 'Ruby III', 'Ruby II', 'Ruby I', 'Master'];
  return tiers[tier] || '언랭크';
};

const close = () => {
  emit('close');
};

const sendInvite = async () => {
  if (selectedIds.value.length === 0 || submitting.value) return;
  
  submitting.value = true;
  try {
    const res = await battleApi.createBattle(
      props.battleType,
      props.battleSubType,
      null, // problemIds - 배틀 시작 시 선정
      selectedIds.value
    );
    
    emit('created', res.data);
    close();
    
    // 배틀 대기실로 이동
    router.push(`/battle/${res.data.id}`);
  } catch (e) {
    console.error('Failed to create battle:', e);
    alert('배틀 생성에 실패했습니다.');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
