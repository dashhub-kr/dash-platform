<script setup lang="ts">
import { AlertCircle, Shuffle, TrendingDown } from 'lucide-vue-next'

const problems = [
  {
    icon: Shuffle,
    title: "파편화된 기록",
    description: "백준, SWEA 등 여기저기 흩어진 풀이 이력",
    emoji: "🔀",
    color: "#FF9600"
  },
  {
    icon: AlertCircle,
    title: "피드백의 부재",
    description: "정답 처리는 받았지만, 내 코드의 효율성이나 반례를 알기 어려움",
    emoji: "❓",
    color: "#FFC800"
  },
  {
    icon: TrendingDown,
    title: "학습 방향성 상실",
    description: "수많은 문제 중 내 약점을 보완할 문제를 찾기 힘듦",
    emoji: "📉",
    color: "#FF4B4B"
  }
]
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative circles -->
    <div class="absolute top-20 right-20 w-64 h-64 bg-[#FF4B4B] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 left-20 w-80 h-80 bg-[#FFC800] opacity-5 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex h-full flex-col items-center justify-center px-8 py-16 max-w-7xl mx-auto">
      <div 
        class="text-center mb-16"
        v-motion
        :initial="{ y: -50, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { duration: 800 } }"
      >
        <div 
          class="inline-flex items-center gap-2 px-4 py-2 bg-[#FF4B4B] text-white rounded-xl mb-8 font-bold text-sm"
          v-motion
          :initial="{ scale: 0 }"
          :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
        >
          <AlertCircle class="w-4 h-4" :stroke-width="2.5" />
          Problem
        </div>

        <h2 class="text-5xl md:text-6xl font-black text-slate-800 mb-6 tracking-tight">
          열심히 풀고는 있는데,
        </h2>
        <p class="text-4xl md:text-5xl text-[#FF4B4B] font-bold tracking-tight">
          제가 정말 성장하고 있는 걸까요?
        </p>
      </div>

      <div class="grid md:grid-cols-3 gap-6 w-full">
        <div
          v-for="(problem, index) in problems"
          :key="index"
          class="relative group"
          v-motion
          :initial="{ y: 50, opacity: 0, scale: 0.9 }"
          :enter="{ y: 0, opacity: 1, scale: 1, transition: { delay: 300 + index * 150, duration: 600, type: 'spring' } }"
          :hover="{ y: -8, transition: { duration: 200 } }"
        >
          <div class="p-8 rounded-3xl bg-white border-2 border-slate-200 shadow-sm hover:shadow-md transition-all duration-300 h-full">
            <!-- Emoji icon -->
            <div class="mb-6">
              <div 
                class="inline-flex items-center justify-center w-16 h-16 rounded-2xl text-4xl"
                :style="{ backgroundColor: `${problem.color}20` }"
                v-motion
                :hover="{ rotate: [0, -10, 10, -10, 0], transition: { duration: 500 } }"
              >
                {{ problem.emoji }}
              </div>
            </div>
            
            <h3 class="text-2xl font-bold text-slate-800 mb-4 tracking-tight">{{ problem.title }}</h3>
            <p class="text-lg text-slate-600 leading-relaxed font-medium">{{ problem.description }}</p>
          </div>
        </div>
      </div>

      <div
        class="mt-16 text-center"
        v-motion
        :initial="{ scale: 0.9, opacity: 0 }"
        :enter="{ scale: 1, opacity: 1, transition: { delay: 1200, duration: 800 } }"
      >
        <div class="text-6xl mb-4">🤔</div>
        <p class="text-2xl text-slate-600 font-bold">이제 DASH가 해결하겠습니다!</p>
      </div>
    </div>
  </div>
</template>
