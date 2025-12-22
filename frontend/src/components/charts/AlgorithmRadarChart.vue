<template>
  <div class="algorithm-radar-chart">
    <div class="aspect-square relative">
      <Radar v-if="chartData" :data="chartData" :options="chartOptions" />
      <div v-else class="flex items-center justify-center h-full text-slate-400 text-sm">
        데이터가 부족합니다
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Radar } from 'vue-chartjs';
import { Chart as ChartJS, RadialLinearScale, PointElement, LineElement, Filler, Tooltip } from 'chart.js';

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip);

const props = defineProps({
  stats: {
    type: Array, // [{ tagKey: 'dp', solved: 10, ... }]
    default: () => []
  },
  maxTags: {
    type: Number,
    default: 6
  }
});

const topTags = computed(() => {
    if (!props.stats || props.stats.length === 0) return [];
    // Sort by solved count desc
    return [...props.stats]
        .sort((a, b) => (b.solved || 0) - (a.solved || 0))
        .slice(0, props.maxTags);
});

const chartData = computed(() => {
  if (topTags.value.length === 0) return null;
  
  const labels = topTags.value.map(t => {
    const tagMap = {
      'implementation': '구현',
      'dp': 'DP',
      'greedy': '그리디',
      'graphs': '그래프',
      'math': '수학',
      'data_structures': '자료구조',
      'string': '문자열',
      'binary_search': '이분탐색',
      'bfs': 'BFS',
      'dfs': 'DFS',
      'simulation': '시뮬레이션',
      'sorting': '정렬',
      'bruteforcing': '완전탐색',
      'trees': '트리'
    };
    return tagMap[t.tagKey] || t.tagKey;
  });
  
  const maxSolved = Math.max(...topTags.value.map(t => t.solved || 0), 1);
  const data = topTags.value.map(t => Math.round(((t.solved || 0) / maxSolved) * 100));
  
  // Close the loop for radar chart by repeating first element if needed? 
  // Chart.js handles it automatically usually.

  return {
    labels,
    datasets: [{
      label: '역량 분포',
      data,
      backgroundColor: 'rgba(99, 102, 241, 0.15)', // Indigo-500 low opacity
      borderColor: 'rgba(99, 102, 241, 0.8)',
      borderWidth: 2,
      pointBackgroundColor: 'rgba(99, 102, 241, 1)',
      pointBorderColor: '#fff',
      pointBorderWidth: 2,
      pointRadius: 4,
      pointHoverRadius: 6
    }]
  };
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx) => `상대적 숙련도: ${ctx.raw}%`
      },
      backgroundColor: 'rgba(15, 23, 42, 0.9)', // Slate-900
      padding: 10,
      cornerRadius: 8,
      displayColors: false
    }
  },
  scales: {
    r: {
      beginAtZero: true,
      max: 100,
      ticks: {
        stepSize: 25,
        display: false, // Hide numeric ticks for cleaner look
      },
      grid: {
        color: 'rgba(148, 163, 184, 0.1)', // Slate-400 very low opacity
        circular: true
      },
      angleLines: {
        color: 'rgba(148, 163, 184, 0.1)'
      },
      pointLabels: {
        color: 'rgba(51, 65, 85, 0.8)', // Slate-700
        font: { 
            family: "'Pretendard', sans-serif",
            size: 11, 
            weight: '600' 
        }
      }
    }
  }
};
</script>

<style scoped>
.algorithm-radar-chart {
    width: 100%;
}
</style>
