<script setup lang="ts">
import { Gamepad2, Trophy, Target, Video } from 'lucide-vue-next'

const nodes = [
  { id: 1, x: 50, y: 20, label: '정렬', status: 'complete', emoji: '⭐' },
  { id: 2, x: 20, y: 40, label: '그리디', status: 'complete', emoji: '⭐' },
  { id: 3, x: 50, y: 60, label: 'DP', status: 'current', emoji: '🎯' },
  { id: 4, x: 80, y: 40, label: '그래프', status: 'complete', emoji: '⭐' },
  { id: 5, x: 20, y: 80, label: 'DFS', status: 'locked', emoji: '🔒' },
  { id: 6, x: 80, y: 80, label: 'BFS', status: 'locked', emoji: '🔒' },
]

const connections = [
  { from: 1, to: 2 }, { from: 1, to: 4 },
  { from: 2, to: 3 }, { from: 4, to: 3 },
  { from: 3, to: 5 }, { from: 3, to: 6 },
]

const statusColors: Record<string, string> = {
  complete: 'bg-[#58CC02] border-[#58CC02]',
  current: 'bg-[#FFC800] border-[#FFC800]',
  locked: 'bg-slate-300 border-slate-300',
}

const getLineProps = (conn: { from: number, to: number }) => {
  const fromNode = nodes.find(n => n.id === conn.from)
  const toNode = nodes.find(n => n.id === conn.to)
  if (!fromNode || !toNode) return { x1: '0%', y1: '0%', x2: '0%', y2: '0%' }
  return {
    x1: `${fromNode.x}%`,
    y1: `${fromNode.y}%`,
    x2: `${toNode.x}%`,
    y2: `${toNode.y}%`
  }
}
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 left-20 w-64 h-64 bg-[#FFC800] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 right-20 w-80 h-80 bg-[#3396F4] opacity-5 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex h-full items-center px-8 py-16 max-w-7xl mx-auto">
      <div class="w-full">
        <div 
          class="text-center mb-12"
          v-motion
          :initial="{ y: -50, opacity: 0 }"
          :enter="{ y: 0, opacity: 1, transition: { duration: 800 } }"
        >
          <div 
            class="inline-flex items-center gap-2 px-4 py-2 bg-[#FFC800] text-slate-800 rounded-xl mb-8 font-bold text-sm"
            v-motion
            :initial="{ scale: 0 }"
            :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
          >
            <Gamepad2 class="w-4 h-4" :stroke-width="2.5" />
            Training Center
          </div>

          <h2 class="text-5xl md:text-6xl font-black mb-4 tracking-tight">
            <span class="text-[#FFC800]">
              RPG 게임을 하듯 즐겁게,
            </span>
          </h2>
          <p class="text-3xl text-slate-800 font-bold">
            알고리즘 스킬 트리 정복 🎮
          </p>
        </div>

        <div class="grid md:grid-cols-2 gap-12">
          <!-- Skill Tree Visualization -->
          <div
            class="relative"
            v-motion
            :initial="{ x: -50, opacity: 0 }"
            :enter="{ x: 0, opacity: 1, transition: { delay: 300 } }"
          >
            <div class="p-8 bg-white rounded-3xl border-2 border-slate-200 shadow-sm relative overflow-hidden">
              <h3 class="text-2xl font-bold text-slate-800 mb-8 text-center">알고리즘 스킬 트리</h3>
              
              <!-- SVG for connections -->
              <svg class="absolute inset-0 w-full h-full pointer-events-none">
                <line
                  v-for="(conn, i) in connections"
                  :key="i"
                  v-bind="getLineProps(conn)"
                  stroke="#E2E8F0"
                  stroke-width="3"
                  v-motion
                  :initial="{ pathLength: 0 }"
                  :enter="{ pathLength: 1, transition: { delay: 500 + i * 100, duration: 500 } }"
                />
              </svg>

              <!-- Nodes -->
              <div class="relative h-96">
                <div
                  v-for="(node, index) in nodes"
                  :key="node.id"
                  class="absolute"
                  :style="{
                    left: `${node.x}%`,
                    top: `${node.y}%`,
                    transform: 'translate(-50%, -50%)',
                  }"
                  v-motion
                  :initial="{ scale: 0, opacity: 0 }"
                  :enter="{ scale: 1, opacity: 1, transition: { delay: 700 + index * 100, type: 'spring' } }"
                  :hover="{ scale: 1.1 }"
                >
                  <div class="w-20 h-20 rounded-2xl border-4 flex flex-col items-center justify-center shadow-md cursor-pointer" :class="statusColors[node.status]">
                    <div class="text-2xl mb-1">{{ node.emoji }}</div>
                    <span class="text-xs text-white font-bold">{{ node.label }}</span>
                  </div>
                </div>
              </div>

              <div class="mt-6 flex justify-center gap-4 text-sm font-bold">
                <div class="flex items-center gap-2">
                  <div class="w-3 h-3 bg-[#58CC02] rounded"></div>
                  <span class="text-slate-700">완료</span>
                </div>
                <div class="flex items-center gap-2">
                  <div class="w-3 h-3 bg-[#FFC800] rounded"></div>
                  <span class="text-slate-700">진행중</span>
                </div>
                <div class="flex items-center gap-2">
                  <div class="w-3 h-3 bg-slate-300 rounded"></div>
                  <span class="text-slate-700">잠김</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Features -->
          <div class="flex flex-col justify-center space-y-6">
            <div
              class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ x: 50, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 500 } }"
            >
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-[#3396F4] bg-opacity-10 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Target class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">노드-그래프 스킬 트리</h3>
                  <p class="text-lg text-slate-600 font-medium">알고리즘 계통도를 시각화하여 정복 상태 표시</p>
                </div>
              </div>
            </div>

            <div
              class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ x: 50, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 700 } }"
            >
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-[#FF9600] bg-opacity-20 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Video class="w-6 h-6 text-[#FF9600]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">개인 맞춤 추천</h3>
                  <p class="text-lg text-slate-600 font-medium">약점 태그 기반 문제 + 보완 학습용 영상 매칭</p>
                </div>
              </div>
            </div>

            <div
              class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ x: 50, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 900 } }"
            >
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-[#FFC800] bg-opacity-30 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Trophy class="w-6 h-6 text-[#FF9600]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">마스터 시스템</h3>
                  <p class="text-lg text-slate-600 font-medium">각 노드를 해결할 때마다 별 획득 및 다음 단계 오픈</p>
                </div>
              </div>
            </div>

            <!-- Progress card -->
            <div
              class="p-6 bg-[#FFC800] bg-opacity-10 rounded-3xl border-2 border-[#FFC800] shadow-sm"
              v-motion
              :initial="{ y: 30, opacity: 0 }"
              :enter="{ y: 0, opacity: 1, transition: { delay: 1100 } }"
            >
              <h4 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                <Trophy class="w-6 h-6 text-[#FF9600]" :stroke-width="2.5" />
                현재 진행 상황
              </h4>
              <div class="space-y-3">
                <div>
                  <div class="flex justify-between text-sm mb-2 font-bold">
                    <span class="text-slate-700">전체 진행도</span>
                    <span class="text-[#3396F4]">60%</span>
                  </div>
                  <div class="w-full bg-slate-200 rounded-full h-3 overflow-hidden">
                    <div
                      class="h-full bg-[#58CC02]"
                      v-motion
                      :initial="{ width: '0%' }"
                      :enter="{ width: '60%', transition: { delay: 1300, duration: 1000 } }"
                    />
                  </div>
                </div>
                <div class="flex justify-between text-sm font-bold">
                  <span class="text-slate-700">획득한 별</span>
                  <span class="text-[#FFC800]">12 ⭐</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
