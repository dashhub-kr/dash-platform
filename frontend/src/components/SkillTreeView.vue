<template>
  <div class="skill-tree-wrapper">
    <!-- 로딩 -->
    <div v-if="loading" class="flex items-center justify-center py-16 bg-white rounded-3xl border border-slate-200">
      <div class="text-center">
        <div class="w-12 h-12 mx-auto mb-3 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
        <p class="text-slate-500 text-sm">스킬 트리 로딩 중...</p>
      </div>
    </div>

    <!-- RPG 스킬 트리 -->
    <div v-else class="skill-tree-container relative bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 rounded-2xl p-6 shadow-2xl overflow-hidden">
      <!-- 배경 효과 -->
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full bg-gradient-radial from-indigo-500/30 to-transparent"></div>
      </div>
      
      <!-- 헤더 -->
      <div class="relative text-center mb-6">
        <h3 class="text-lg font-bold text-amber-400 tracking-wide flex items-center justify-center gap-2">
          <span>⚔️</span>
          <span>알고리즘 스킬트리</span>
          <span>⚔️</span>
        </h3>
        <div class="mt-2 flex justify-center gap-4 text-xs">
          <span class="text-emerald-400">● 마스터 {{ skillTree?.masteredTags || 0 }}</span>
          <span class="text-amber-400">● 학습중 {{ skillTree?.learningTags || 0 }}</span>
          <span class="text-slate-500">● 미개방 {{ skillTree?.lockedTags || 0 }}</span>
        </div>
        <div class="mt-2 flex justify-center gap-3 text-xs">
          <span class="px-2 py-0.5 rounded bg-amber-400 text-slate-900 font-bold">S</span>
          <span class="px-2 py-0.5 rounded bg-purple-400 text-white font-bold">A</span>
          <span class="px-2 py-0.5 rounded bg-sky-400 text-white font-bold">B</span>
        </div>
      </div>

      <!-- 트리 영역 -->
      <div class="relative mx-auto" style="width: 720px; height: 420px;">
        <!-- SVG 연결선 레이어 (노드 아래) -->
        <svg class="absolute inset-0 w-full h-full" style="z-index: 1;">
          <!-- 루트 → 8개 Family 연결선 -->
          <g v-for="(family, idx) in sortedFamilies" :key="'line-' + idx">
            <line 
              :x1="360" :y1="50"
              :x2="nodePositions[idx].x" :y2="nodePositions[idx].y"
              :stroke="getLineColor(family.progressPercent)"
              stroke-width="3"
              stroke-linecap="round"
              class="transition-all duration-300"
            />
            <!-- 글로우 효과 -->
            <line 
              :x1="360" :y1="50"
              :x2="nodePositions[idx].x" :y2="nodePositions[idx].y"
              :stroke="getLineColor(family.progressPercent)"
              stroke-width="6"
              stroke-linecap="round"
              opacity="0.3"
              class="transition-all duration-300"
            />
          </g>
        </svg>

        <!-- 노드 레이어 (연결선 위) -->
        <div class="absolute inset-0" style="z-index: 2;">
          <!-- 루트 노드 -->
          <div 
            class="absolute transform -translate-x-1/2 -translate-y-1/2 cursor-pointer group"
            style="left: 360px; top: 50px;"
          >
            <div class="relative">
              <!-- 외곽 글로우 -->
              <div class="absolute inset-0 bg-amber-400 rounded-full blur-md opacity-50 group-hover:opacity-80 transition-opacity"></div>
              <!-- 노드 -->
              <div class="relative w-16 h-16 bg-gradient-to-br from-amber-400 via-orange-500 to-rose-500 rounded-full flex items-center justify-center text-3xl border-4 border-amber-300 shadow-lg group-hover:scale-110 transition-transform">
                👑
              </div>
            </div>
          </div>

          <!-- Family 노드들 -->
          <div 
            v-for="(family, idx) in sortedFamilies" 
            :key="family.key"
            class="absolute transform -translate-x-1/2 -translate-y-1/2 cursor-pointer group"
            :style="{ left: nodePositions[idx].x + 'px', top: nodePositions[idx].y + 'px' }"
            @click="selectFamily(family)"
          >
            <div class="relative">
              <!-- 선택 글로우 -->
              <div 
                v-if="selectedFamily?.key === family.key"
                class="absolute -inset-2 bg-indigo-500 rounded-xl blur-md opacity-70"
              ></div>
              <!-- 외곽 글로우 (진행도 기반) -->
              <div 
                v-else
                class="absolute inset-0 rounded-xl blur-sm opacity-40 transition-opacity group-hover:opacity-60"
                :class="getNodeGlowClass(family.progressPercent)"
              ></div>
              
              <!-- 노드 -->
              <div 
                class="relative w-14 h-14 rounded-xl flex items-center justify-center text-2xl border-2 shadow-lg transition-all group-hover:scale-110"
                :class="getNodeBgClass(family)"
              >
                {{ getFamilyIcon(family.key) }}
              </div>
              
              <!-- 진행도 표시 -->
              <div class="absolute -bottom-1 -right-1 w-6 h-6 rounded-full bg-slate-800 border-2 border-slate-600 flex items-center justify-center">
                <span class="text-[8px] font-bold text-white">{{ Math.round(family.progressPercent || 0) }}</span>
              </div>

              <!-- 이름 -->
              <div class="absolute -bottom-6 left-1/2 -translate-x-1/2 whitespace-nowrap text-[10px] font-medium text-slate-300">
                {{ family.name }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 선택된 Family의 하위 태그 -->
      <Transition name="expand">
        <div v-if="selectedFamily" class="relative mt-12 pt-6 border-t border-slate-700">
          <!-- 연결선 애니메이션 -->
          <div class="absolute top-0 left-1/2 -translate-x-1/2 w-px h-6 bg-gradient-to-b from-indigo-500 to-transparent"></div>
          
          <!-- 헤더 -->
          <div class="text-center mb-4">
            <div class="inline-flex items-center gap-2 px-4 py-2 bg-slate-800 rounded-full border border-slate-600">
              <span class="text-xl">{{ getFamilyIcon(selectedFamily.key) }}</span>
              <span class="font-bold text-white">{{ selectedFamily.name }}</span>
              <span class="text-xs text-slate-400">({{ selectedFamily.children?.length || 0 }}개)</span>
              <button @click.stop="selectedFamily = null" class="ml-2 text-slate-400 hover:text-white">✕</button>
            </div>
          </div>
          
          <!-- 하위 태그 (SVG 연결 + 노드) -->
          <div class="relative mx-auto overflow-x-auto" :style="{ width: '100%', maxWidth: getSubTreeWidth + 'px', height: '150px' }">
            <!-- SVG 연결선 -->
            <svg class="absolute inset-0 w-full h-full" style="z-index: 1;">
              <line 
                v-for="(tag, idx) in sortedTags" 
                :key="'subline-' + idx"
                :x1="getSubTreeWidth / 2" :y1="0"
                :x2="getSubTagX(idx)" :y2="60"
                :stroke="getLineColor(tag.progressPercent)"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>

            <!-- 하위 태그 노드들 -->
            <div 
              v-for="(tag, idx) in sortedTags" 
              :key="tag.key"
              class="absolute transform -translate-x-1/2 cursor-pointer group"
              :style="{ left: getSubTagX(idx) + 'px', top: '50px' }"
              @click="onTagClick(tag)"
            >
              <div class="relative">
                <!-- S/A 티어 글로우 -->
                <div 
                  v-if="isSATier(tag.tier)"
                  class="absolute -inset-1 rounded-lg blur-sm"
                  :class="tag.tier === 'S' ? 'bg-amber-400 opacity-50' : 'bg-purple-400 opacity-40'"
                ></div>
                
                <!-- 노드 -->
                <div 
                  class="relative w-12 h-12 rounded-lg flex flex-col items-center justify-center border-2 shadow-lg transition-all group-hover:scale-110"
                  :class="getSubNodeClass(tag)"
                >
                  <span class="text-xs font-bold text-white">{{ tag.solved || 0 }}</span>
                  <span 
                    class="text-[8px] font-bold px-1 rounded mt-0.5"
                    :class="getTierBadge(tag.tier)"
                  >
                    {{ tag.tier }}
                  </span>
                </div>
                
                <!-- 이름 -->
                <div class="w-14 text-center mt-1 text-[9px] text-slate-400 truncate">
                  {{ tag.name }}
                </div>

                <!-- 마스터 뱃지 -->
                <div v-if="tag.masteryLevel === 'MASTER'" class="absolute -top-1 -right-1 text-xs">🏆</div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
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

// 8개 Family 노드의 고정 위치 (원형 배치 - 겹침 방지)
const nodePositions = [
  { x: 80, y: 170 },   // 구현 (좌측 상단)
  { x: 170, y: 260 },  // DP (좌측)
  { x: 280, y: 320 },  // 그래프 (좌측 하단)
  { x: 440, y: 320 },  // 수학 (우측 하단)
  { x: 550, y: 260 },  // 그리디 (우측)
  { x: 640, y: 170 },  // 문자열 (우측 상단)
  { x: 540, y: 100 },  // 자료구조 (상단 우측)
  { x: 180, y: 100 },  // 고급 (상단 좌측)
];

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

// orderIndex 순서대로 정렬된 families (1-8번 순서)
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

// 하위 태그 레이아웃
const getSubTreeWidth = computed(() => Math.max(sortedTags.value.length * 80, 400));
const getSubTagX = (idx) => {
  const total = sortedTags.value.length;
  const spacing = getSubTreeWidth.value / (total + 1);
  return spacing * (idx + 1);
};

// 스타일 함수들
const getLineColor = (percent) => {
  if (percent >= 70) return '#10b981';
  if (percent >= 30) return '#f59e0b';
  if (percent > 0) return '#64748b';
  return '#334155';
};

const getNodeGlowClass = (percent) => {
  if (percent >= 70) return 'bg-emerald-500';
  if (percent >= 30) return 'bg-amber-500';
  return 'bg-slate-600';
};

const getNodeBgClass = (family) => {
  const selected = selectedFamily.value?.key === family.key;
  if (selected) return 'bg-gradient-to-br from-indigo-500 to-purple-600 border-indigo-400';
  if (family.progressPercent >= 70) return 'bg-gradient-to-br from-emerald-600 to-emerald-800 border-emerald-500';
  if (family.progressPercent >= 30) return 'bg-gradient-to-br from-amber-600 to-orange-700 border-amber-500';
  return 'bg-slate-700 border-slate-600 hover:border-slate-500';
};

const isSATier = (tier) => tier === 'S' || tier === 'A';

const getSubNodeClass = (tag) => {
  if (tag.tier === 'S') return 'bg-gradient-to-br from-amber-500 to-rose-600 border-amber-400';
  if (tag.tier === 'A') return 'bg-gradient-to-br from-purple-500 to-indigo-600 border-purple-400';
  if (tag.tier === 'B') return 'bg-gradient-to-br from-sky-500 to-blue-600 border-sky-400';
  if (tag.tier === 'C') return 'bg-gradient-to-br from-teal-500 to-cyan-600 border-teal-400';
  if (tag.progressPercent >= 70) return 'bg-gradient-to-br from-emerald-600 to-emerald-800 border-emerald-500';
  return 'bg-slate-700 border-slate-600';
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
