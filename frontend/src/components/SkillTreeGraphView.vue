<template>
  <div class="skill-graph-container h-full w-full bg-slate-50 relative">
    <!-- 로딩 -->
    <div v-if="loading" class="flex items-center justify-center h-full">
      <div class="text-slate-400">그래프 로딩 중...</div>
    </div>

    <!-- Vue Flow 그래프 -->
    <VueFlow 
      v-else
      :nodes="nodes" 
      :edges="computedEdges"
      :default-viewport="{ zoom: 0.5, x: 0, y: 0 }"
      :min-zoom="0.1"
      :max-zoom="4"
      class="skill-flow"
      @pane-click="onPaneClick"
      @pane-ready="onPaneReady"
    >
      <Background />
      <Controls />
      
      <!-- 커스텀 노드 -->
      <template #node-skill="{ data }">
          <!-- 태그 이름 (솔리드 컬러 + 흰 글씨) -->
          <div 
            class="skill-node px-5 py-3 rounded-xl text-center cursor-pointer transition-all flex items-center justify-center min-w-[100px] shadow-sm hover:shadow-md hover:scale-105"
            :class="getNodeClass(data)"
            @click.stop="onNodeClick(data)"
          >
            <div class="text-sm font-bold whitespace-pre-wrap break-words line-clamp-2 leading-tight max-w-[120px]" :class="getNodeTextClass(data)">
              {{ data.label }}
            </div>
          </div>
      </template>
    </VueFlow>
    
    <!-- 범례 -->
    <div v-if="selectedNodeId" class="absolute bottom-4 left-4 bg-white/90 backdrop-blur-sm p-3 rounded-lg shadow-lg text-xs">
      <div class="font-bold mb-2">{{ selectedNodeLabel }}</div>
      <div class="flex gap-4 font-medium text-xs">
        <span class="flex items-center gap-1 text-emerald-600"><span class="w-2.5 h-2.5 bg-emerald-500 rounded-full"></span> 선수: {{ prerequisiteIds.length }}</span>
        <span class="flex items-center gap-1 text-slate-500"><span class="w-2.5 h-2.5 bg-slate-500 rounded-full"></span> 이후: {{ dependentIds.length }}</span>
      </div>
      <div class="mt-2 text-slate-400">다른 곳 클릭하여 해제</div>
    </div>
    <!-- 초기화 버튼 -->
    <button 
      @click="restoreOriginalLayout"
      class="absolute top-4 right-4 p-3 bg-white text-slate-600 rounded-full shadow-lg hover:bg-slate-50 transition-all z-20 group"
      title="화면 초기화"
    >
      <RotateCcw class="w-5 h-5 group-hover:-rotate-180 transition-transform duration-500" />
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { RotateCcw } from 'lucide-vue-next';
import { VueFlow, useVueFlow } from '@vue-flow/core';
import { Background } from '@vue-flow/background';
import { Controls } from '@vue-flow/controls';
import '@vue-flow/core/dist/style.css';
import '@vue-flow/core/dist/theme-default.css';
import * as dagre from 'dagre';
import { tagApi } from '@/api/tags';
import { useAuth } from '@/composables/useAuth';
import { calculateAllPositions } from '@/data/skillTreePositions';

const { user } = useAuth();
const { fitView } = useVueFlow();
const loading = ref(true);
const graphData = ref(null);
const selectedNodeId = ref(null);

const onPaneReady = (instance) => {
  instance.fitView({ padding: 0.2, duration: 0 });
};

// 선택된 노드의 라벨
const selectedNodeLabel = computed(() => {
  if (!selectedNodeId.value || !graphData.value?.nodes) return '';
  const node = graphData.value.nodes.find(n => n.id === selectedNodeId.value);
  return node?.label || selectedNodeId.value;
});



const isPrerequisite = (id) => prerequisiteIds.value.includes(id);
const isDependent = (id) => dependentIds.value.includes(id);
const isSelected = (id) => id === selectedNodeId.value;
const isRelated = (id) => isSelected(id) || isPrerequisite(id) || isDependent(id);

// 수동 배치 좌표 사용 (지하철 노선도 스타일)
const layoutPositions = computed(() => {
  if (!graphData.value?.nodes) return {};
  return calculateAllPositions(graphData.value.nodes);
});

// 노드 생성 (Ref로 변경하여 직접 수정 가능하게 함)
const nodes = ref([]);

watch(graphData, (newData) => {
  if (!newData?.nodes) {
    nodes.value = [];
    return;
  }
  
  // 연결된 엣지가 없는 고립 노드 필터링
  const connectedNodeIds = new Set();
  if (newData.edges) {
    newData.edges.forEach(edge => {
      connectedNodeIds.add(edge.source);
      connectedNodeIds.add(edge.target);
    });
  }

  nodes.value = newData.nodes
    .filter(node => connectedNodeIds.has(node.id))
    .map(node => ({
      id: node.id,
      type: 'skill',
      position: layoutPositions.value[node.id] || { x: 0, y: 0 },
      data: {
        ...node,
        label: node.label || node.id
      }
    }));
}, { immediate: true });

const computedEdges = computed(() => {
  if (!graphData.value?.edges) return [];
  
  // 전체 노드 관계 맵 생성 (재귀 탐색용)
  const edgeMap = {
    parents: {}, // dependent -> [prerequisites]
    children: {} // prerequisite -> [dependents]
  };
  
  graphData.value.edges.forEach(edge => {
    if (!edgeMap.parents[edge.target]) edgeMap.parents[edge.target] = [];
    edgeMap.parents[edge.target].push(edge.source);
    
    if (!edgeMap.children[edge.source]) edgeMap.children[edge.source] = [];
    edgeMap.children[edge.source].push(edge.target);
  });

  return graphData.value.edges.map((edge, idx) => {
    let isHighlighted = false;
    let isPrereqPath = false;

    if (selectedNodeId.value) {
      // 1. 이 엣지가 선수 관계 경로에 포함되는지 확인
      // (Target이 선택된 노드이거나, Target이 선수 목록에 포함됨 AND Source도 선수 목록에 포함됨)
      const targetIsPrereq = prerequisiteIds.value.includes(edge.target) || edge.target === selectedNodeId.value;
      const sourceIsPrereq = prerequisiteIds.value.includes(edge.source);
      
      if (targetIsPrereq && sourceIsPrereq) {
        isHighlighted = true;
        isPrereqPath = true;
      }

      // 2. 이 엣지가 후행 관계 경로에 포함되는지 확인
      // (Source가 선택된 노드이거나, Source가 후행 목록에 포함됨 AND Target도 후행 목록에 포함됨)
      const sourceIsDep = dependentIds.value.includes(edge.source) || edge.source === selectedNodeId.value;
      const targetIsDep = dependentIds.value.includes(edge.target);
      
      if (sourceIsDep && targetIsDep) {
        isHighlighted = true;
        isPrereqPath = false;
      }
    }
    
    return {
      id: `e-${idx}`,
      source: edge.source,
      target: edge.target,
      animated: true,
      style: {
        stroke: isHighlighted 
          ? (isPrereqPath ? '#10b981' : '#475569') // 선수=녹색 강조, 의존=기본 회색 유지
          : (selectedNodeId.value ? '#e2e8f0' : '#475569'), // 선택 안된 거: 연한 회색, 기본: 진한 회색
        strokeWidth: (isHighlighted && isPrereqPath) ? 2 : 1.5,
        opacity: selectedNodeId.value && !isHighlighted ? 0.1 : (isPrereqPath ? 0.8 : 0.3) // 의존/기본 라인 투명도 0.3으로 낮춤 (은은하게)
      }
    };
  });
});

// 재귀적으로 모든 선수/후행 태그 찾기
const findAllRelated = (startNodeId, direction) => {
  if (!graphData.value?.edges) return [];
  
  const related = new Set();
  const queue = [startNodeId];
  const visited = new Set([startNodeId]);
  
  // 엣지 맵핑
  const map = {};
  graphData.value.edges.forEach(edge => {
    const key = direction === 'prereq' ? edge.target : edge.source; // prereq 찾으려면 target이 key
    const val = direction === 'prereq' ? edge.source : edge.target;
    
    if (!map[key]) map[key] = [];
    map[key].push(val);
  });
  
  while (queue.length > 0) {
    const current = queue.shift();
    const neighbors = map[current] || [];
    
    neighbors.forEach(next => {
      if (!visited.has(next)) {
        visited.add(next);
        related.add(next);
        queue.push(next);
      }
    });
  }
  
  return Array.from(related);
};

// 선택된 노드의 선수/의존 태그 목록 (재귀)
const prerequisiteIds = computed(() => {
  if (!selectedNodeId.value) return [];
  return findAllRelated(selectedNodeId.value, 'prereq');
});

const dependentIds = computed(() => {
  if (!selectedNodeId.value) return [];
  return findAllRelated(selectedNodeId.value, 'dependent');
});

// 헬퍼 함수들
const getMaxStars = (tier) => {
  if (tier === 'S') return 5;
  if (tier === 'A') return 4;
  if (tier === 'B') return 3;
  return 2;
};

const getFilledStars = (masteryLevel) => {
  const levels = { 'NONE': 0, 'BEGINNER': 1, 'INTERMEDIATE': 2, 'ADVANCED': 3, 'EXPERT': 4, 'MASTER': 5 };
  return levels[masteryLevel] || 0;
};

// 패밀리별 색상 (DESIGN_SYSTEM.md 참조 - 솔리드 배경 + 흰 글씨)
const familyColors = {
  1: { bg: 'bg-rose-500', text: 'text-white', ring: 'ring-rose-400', line: '#f43f5e' },           // 구현
  2: { bg: 'bg-violet-500', text: 'text-white', ring: 'ring-violet-400', line: '#8b5cf6' },       // DP
  3: { bg: 'bg-sky-500', text: 'text-white', ring: 'ring-sky-400', line: '#0ea5e9' },             // 그래프
  4: { bg: 'bg-[#58CC02]', text: 'text-white', ring: 'ring-emerald-400', line: '#58CC02' },       // 탐색 (Leaf)
  5: { bg: 'bg-amber-500', text: 'text-white', ring: 'ring-amber-400', line: '#f59e0b' },         // 자료구조
  6: { bg: 'bg-pink-500', text: 'text-white', ring: 'ring-pink-400', line: '#ec4899' },           // 수학
  7: { bg: 'bg-[#2DD4BF]', text: 'text-white', ring: 'ring-teal-400', line: '#2DD4BF' },          // 문자열 (Beetle)
  8: { bg: 'bg-[#FF9600]', text: 'text-white', ring: 'ring-orange-400', line: '#FF9600' },        // 기하 (Fox)
  default: { bg: 'bg-slate-400', text: 'text-white', ring: 'ring-slate-300', line: '#64748b' }
};

const getFamilyColor = (familyId) => {
  return familyColors[familyId] || familyColors.default;
};

// 노드 텍스트 색상
const getNodeTextClass = (data) => {
  const id = data.id;
  
  // 관련 없는 노드 (선택 상태일 때)
  if (selectedNodeId.value && !isRelated(id)) {
    return 'text-slate-300';
  }
  
  // 선택된 노드
  if (isSelected(id)) {
    return 'text-brand-700';
  }
  
  const familyColor = getFamilyColor(data.familyId);
  return familyColor.text;
};

const getNodeClass = (data) => {
  const filled = getFilledStars(data.masteryLevel);
  const id = data.id;
  const familyColor = getFamilyColor(data.familyId);
  
  // 선택된 노드
  if (isSelected(id)) {
    return 'bg-white ring-2 ring-brand-500 shadow-lg';
  }
  
  // 선수 태그
  if (selectedNodeId.value && isPrerequisite(id)) {
    return `${familyColor.bg} shadow-md ring-2 ${familyColor.ring}`;
  }
  
  // 의존 태그
  if (selectedNodeId.value && isDependent(id)) {
    return `${familyColor.bg} shadow-md ring-2 ${familyColor.ring} opacity-50`;
  }
  
  // 관련 없는 노드 (선택 상태일 때)
  if (selectedNodeId.value && !isRelated(id)) {
    return 'bg-slate-100 opacity-20';
  }
  
  // 마스터 노드 (★4~5) - 금테 추가
  if (filled >= 4) {
    return `${familyColor.bg} shadow-lg ring-2 ring-amber-400`;
  }
  
  // 기본 상태
  return `${familyColor.bg}`;
};

const originalPositions = ref({}); // 원래 위치 저장
const getHierarchicalLayout = (nodes, edges) => {
  const g = new dagre.graphlib.Graph();
  g.setGraph({ rankdir: 'TB', nodesep: 100, ranksep: 120 }); // 위에서 아래로
  g.setDefaultEdgeLabel(() => ({}));

  nodes.forEach(node => {
    g.setNode(node.id, { width: 140, height: 60 }); // 노드 크기 근사치
  });

  edges.forEach(edge => {
    g.setEdge(edge.source, edge.target);
  });

  dagre.layout(g);

  return nodes.map(node => {
    const nodeWithPosition = g.node(node.id);
    return {
      ...node,
      position: { x: nodeWithPosition.x, y: nodeWithPosition.y }
    };
  });
};

const onNodeClick = (data) => {
  // 이미 선택된 노드 다시 클릭 시 해제 (원복)
  if (selectedNodeId.value === data.id) {
    selectedNodeId.value = null;
    restoreOriginalLayout();
    return;
  }

  selectedNodeId.value = data.id;

  // 1. 관련 노드 찾기
  const relatedNodeIds = [
    data.id,
    ...findAllRelated(data.id, 'prereq'),
    ...findAllRelated(data.id, 'dependent')
  ];

  // 2. 현재 상태(전체 맵) 저장 (최초 진입 시에만)
  if (Object.keys(originalPositions.value).length === 0 && nodes.value.length > 0) {
    nodes.value.forEach(n => {
      originalPositions.value[n.id] = { ...n.position };
    });
  }

  // 3. 서브그래프 구성
  const subNodes = nodes.value.filter(n => relatedNodeIds.includes(n.id));
  const subEdges = computedEdges.value.filter(e => 
    relatedNodeIds.includes(e.source) && relatedNodeIds.includes(e.target)
  );

  // 4. 계층형 레이아웃 계산
  const targetLayout = getHierarchicalLayout(subNodes, subEdges);

  // 5. 노드 위치 업데이트 (애니메이션)
  nodes.value = nodes.value.map(n => {
    const newPos = targetLayout.find(tn => tn.id === n.id);
    if (newPos) {
      return { ...n, position: newPos.position, hidden: false, style: { opacity: 1 } }; // 관련 노드는 새 위치로
    } else {
       // 관련 없는 노드는 멀리 보내거나 숨김 (여기서는 위치 유지하되 흐리게 처리되어 있음)
       // 투명도/숨김 처리는 템플릿의 클래스 바인딩으로 처리되므로 위치는 기존 유지 또는 숨김
       return { ...n, hidden: false }; 
    }
  });
  
  // 6. 줌 & 팬 (약간의 딜레이 후 적용하여 위치 이동 애니메이션과 조화)
  nextTick(() => {
    fitView({ 
      nodes: relatedNodeIds, // 관련 노드들만 포함해서 줌
      padding: 0.2, 
      duration: 1000 
    });
  });
};

const restoreOriginalLayout = () => {
  selectedNodeId.value = null; // 선택 해제
  if (Object.keys(originalPositions.value).length === 0) return;

  // 원래 위치로 복귀
  nodes.value = nodes.value.map(n => ({
    ...n,
    position: originalPositions.value[n.id] || n.position,
    hidden: false
  }));

  // 전체 뷰로 리셋
  nextTick(() => {
    fitView({ padding: 0.2, duration: 1000 });
  });
  
  // 저장된 위치 초기화는 하지 않음 (계속 재사용)
};

const onPaneClick = () => {
  if (selectedNodeId.value) {
    selectedNodeId.value = null;
    restoreOriginalLayout();
  }
};

const fetchGraphData = async () => {
  if (!user.value?.id) return;
  
  try {
    loading.value = true;
    const response = await tagApi.getSkillGraph(user.value.id);
    graphData.value = response.data;
  } catch (error) {
    console.error('Failed to fetch skill graph:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchGraphData();
});
</script>

<style scoped>
.skill-flow {
  width: 100%;
  height: 100%;
}

.skill-node {
  min-width: 80px;
}

/* Vue Flow 커스텀 스타일 */
:deep(.vue-flow__node) {
  background: transparent;
  border: none;
  padding: 0;
}

:deep(.vue-flow__edge-path) {
  stroke-linecap: round;
}
</style>
