<template>
  <div class="skill-tree-wrapper">
    <!-- 로딩 -->
    <div v-if="loading" class="flex items-center justify-center py-16 bg-white rounded-2xl border border-slate-200">
      <div class="text-center">
        <div class="w-10 h-10 mx-auto mb-3 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
        <p class="text-slate-500 text-sm">스킬 트리 로딩 중...</p>
      </div>
    </div>

    <!-- 스킬 트리 -->
    <div v-else class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      
      <!-- 헤더 -->
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
        <div class="flex items-center gap-3">
          <span class="text-2xl">⚔️</span>
          <h3 class="text-lg font-bold text-slate-800">알고리즘 스킬트리</h3>
        </div>
        <div class="flex gap-4 text-xs">
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
            <span class="text-slate-600">마스터 {{ skillTree?.masteredTags || 0 }}</span>
          </span>
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full bg-amber-500"></span>
            <span class="text-slate-600">학습중 {{ skillTree?.learningTags || 0 }}</span>
          </span>
          <span class="flex items-center gap-1.5">
            <span class="w-2.5 h-2.5 rounded-full bg-slate-300"></span>
            <span class="text-slate-500">미개방 {{ skillTree?.lockedTags || 0 }}</span>
          </span>
        </div>
      </div>
      
      <!-- 스킬 그리드 -->
      <div class="p-6">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div 
            v-for="family in sortedFamilies" 
            :key="family.key"
            class="group cursor-pointer"
            @click="selectFamily(family)"
          >
            <div 
              class="relative p-4 rounded-xl border-2 transition-all hover:shadow-md"
              :class="[
                selectedFamily?.key === family.key 
                  ? 'bg-indigo-50 border-indigo-400 shadow-md' 
                  : 'bg-white border-slate-200 hover:border-indigo-300',
                getCompletionClass(family.progressPercent)
              ]"
            >
              <!-- 아이콘 + 진행도 -->
              <div class="flex items-center justify-between mb-3">
                <span class="text-3xl">{{ getFamilyIcon(family.key) }}</span>
                <div class="flex flex-col items-end">
                  <span class="text-xl font-bold" :class="getProgressColor(family.progressPercent)">
                    {{ Math.round(family.progressPercent || 0) }}%
                  </span>
                </div>
              </div>
              
              <!-- 이름 -->
              <div class="text-sm font-bold text-slate-800 mb-2">{{ family.name }}</div>
              
              <!-- 진행 바 -->
              <div class="h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div 
                  class="h-full rounded-full transition-all"
                  :class="getProgressBarColor(family.progressPercent)"
                  :style="{ width: (family.progressPercent || 0) + '%' }"
                ></div>
              </div>
              
              <!-- 하위 태그 수 -->
              <div class="mt-2 text-xs text-slate-400">
                {{ family.children?.length || 0 }}개 스킬
              </div>
              
              <!-- 마스터 뱃지 -->
              <div v-if="family.progressPercent >= 80" class="absolute -top-2 -right-2 text-lg">🏆</div>
            </div>
          </div>
        </div>
        
        <!-- 선택된 Family의 하위 태그 -->
        <Transition name="expand">
          <div v-if="selectedFamily" class="mt-6 pt-6 border-t border-slate-200">
            <!-- 헤더 -->
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-3">
                <span class="text-2xl">{{ getFamilyIcon(selectedFamily.key) }}</span>
                <span class="font-bold text-slate-800 text-lg">{{ selectedFamily.name }}</span>
                <span class="text-xs text-slate-400">({{ selectedFamily.children?.length || 0 }}개)</span>
              </div>
              <button @click.stop="selectedFamily = null" class="text-slate-400 hover:text-slate-600 text-xl">✕</button>
            </div>
            
            <!-- 하위 태그 그리드 -->
            <div class="grid grid-cols-3 md:grid-cols-6 gap-3">
              <div 
                v-for="tag in sortedTags" 
                :key="tag.key"
                class="group cursor-pointer"
                @click="onTagClick(tag)"
              >
                <div 
                  class="relative p-3 rounded-xl border-2 text-center transition-all hover:shadow-md"
                  :class="getSubNodeClass(tag)"
                >
                  <!-- 숫자 -->
                  <div class="text-lg font-bold text-slate-700 mb-1">{{ tag.solved || 0 }}</div>
                  
                  <!-- 티어 뱃지 -->
                  <span 
                    class="inline-block text-[10px] font-bold px-1.5 py-0.5 rounded"
                    :class="getTierBadge(tag.tier)"
                  >
                    {{ tag.tier }}
                  </span>
                  
                  <!-- 이름 -->
                  <div class="mt-2 text-[10px] text-slate-500 truncate">{{ tag.name }}</div>
                  
                  <!-- 마스터 뱃지 -->
                  <div v-if="tag.masteryLevel === 'MASTER'" class="absolute -top-1 -right-1 text-sm">🏆</div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { tagApi } from '@/api/tags';
import { useAuth } from '@/composables/useAuth';

const { user } = useAuth();
const loading = ref(true);
const skillTree = ref(null);
const selectedFamily = ref(null);

// Family 아이콘
const familyIcons = {
  'IMPLEMENTATION': '⚡',
  'DP': '🧩',
  'GRAPH': '🕸️',
  'MATH': '🔢',
  'GREEDY': '🎯',
  'STRING': '📝',
  'DATA_STRUCTURE': '📦',
  'ADVANCED': '🚀'
};

// orderIndex 순서대로 정렬
const sortedFamilies = computed(() => {
  if (!skillTree.value?.families) return [];
  return [...skillTree.value.families].sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0));
});

const getFamilyIcon = (key) => familyIcons[key] || '📌';

// 정렬된 태그 (S/A 먼저)
const sortedTags = computed(() => {
  if (!selectedFamily.value?.children) return [];
  return [...selectedFamily.value.children].sort((a, b) => {
    const order = { 'S': 0, 'A': 1, 'B': 2, 'C': 3 };
    return (order[a.tier] ?? 4) - (order[b.tier] ?? 4);
  });
});

// 스타일 함수들
const getProgressColor = (percent) => {
  if (percent >= 70) return 'text-emerald-400';
  if (percent >= 30) return 'text-amber-400';
  return 'text-slate-400';
};

const getProgressBarColor = (percent) => {
  if (percent >= 70) return 'bg-emerald-500';
  if (percent >= 30) return 'bg-amber-500';
  return 'bg-slate-500';
};

const getCompletionClass = (percent) => {
  if (percent >= 70) return 'ring-1 ring-emerald-500/30';
  return '';
};

const getSubNodeClass = (tag) => {
  if (tag.tier === 'S') return 'bg-amber-50 border-amber-400';
  if (tag.tier === 'A') return 'bg-purple-50 border-purple-400';
  if (tag.tier === 'B') return 'bg-sky-50 border-sky-400';
  if (tag.progressPercent >= 70) return 'bg-emerald-50 border-emerald-400';
  return 'bg-slate-50 border-slate-200';
};

const getTierBadge = (tier) => {
  if (tier === 'S') return 'bg-amber-400 text-slate-900';
  if (tier === 'A') return 'bg-purple-400 text-white';
  if (tier === 'B') return 'bg-sky-400 text-white';
  if (tier === 'C') return 'bg-teal-400 text-white';
  return 'bg-slate-600 text-slate-300';
};

const selectFamily = (family) => {
  selectedFamily.value = selectedFamily.value?.key === family.key ? null : family;
};

const onTagClick = (tag) => {
  console.log('Skill:', tag);
  // TODO: 상세 모달 또는 문제 추천
};

const fetchSkillTree = async () => {
  try {
    loading.value = true;
    const userId = user?.id || 1;
    const res = await tagApi.getSkillTree(userId);
    skillTree.value = res.data;
  } catch (error) {
    console.error('Failed to load skill tree:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => fetchSkillTree());
</script>

<style scoped>
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
