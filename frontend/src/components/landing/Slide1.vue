<script setup lang="ts">
import { Code, Sparkles } from 'lucide-vue-next'

const shapes = ['◆', '●', '■', '▲']
const colors = ['#58CC02', '#3396F4', '#FFC800', '#2DD4BF', '#FF9600']

// Generate random shapes data
const floatingShapes = Array.from({ length: 8 }, (_, i) => ({
  id: i,
  shape: shapes[i % shapes.length],
  color: colors[i % colors.length],
  initial: {
    x: Math.random() * (typeof window !== 'undefined' ? window.innerWidth : 1000),
    y: Math.random() * (typeof window !== 'undefined' ? window.innerHeight : 800),
    opacity: 0.1,
    rotate: 0
  },
  enter: {
    y: Math.random() * (typeof window !== 'undefined' ? window.innerHeight : 800),
    rotate: 360,
    transition: {
      duration: (15 + Math.random() * 10) * 1000,
      repeat: Infinity,
      ease: "linear"
    }
  }
}))

const badges = [
  { text: '자동 기록', color: 'bg-[#58CC02] text-white' },
  { text: 'AI 코칭', color: 'bg-[#3396F4] text-white' },
  { text: '실전 모의고사', color: 'bg-[#FF9600] text-white' },
]

const handleLogin = () => {
  window.location.href = `/oauth2/authorization/github`
}
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Background Pattern -->
    <div class="absolute inset-0 opacity-5">
      <div class="absolute inset-0" :style="{
        backgroundImage: `url(&quot;data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%233396F4' fill-opacity='0.4'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E&quot;)`
      }"></div>
    </div>

    <!-- Floating Shapes -->
    <div
      v-for="item in floatingShapes"
      :key="item.id"
      class="absolute text-6xl opacity-10"
      :style="{ color: item.color }"
      v-motion
      :initial="item.initial"
      :enter="item.enter"
    >
      {{ item.shape }}
    </div>

    <!-- Main Content -->
    <div class="relative z-10 flex h-full flex-col items-center justify-center px-8 text-center">
      <!-- Logo -->
      <div
        class="mb-12"
        v-motion
        :initial="{ scale: 0, rotate: -180 }"
        :enter="{ scale: 1, rotate: 0, transition: { duration: 800, type: 'spring' } }"
      >
        <div class="inline-flex items-center gap-4 px-8 py-6 bg-white rounded-3xl shadow-md border-4 border-[#3396F4]">
          <Code class="w-16 h-16 text-[#3396F4]" :stroke-width="2.5" />
          <div
            v-motion
            :enter="{ rotate: [0, 10, -10, 0], transition: { duration: 2000, repeat: Infinity, repeatDelay: 1000 } }"
          >
            <Sparkles class="w-12 h-12 text-[#FFC800]" :stroke-width="2.5" />
          </div>
        </div>
      </div>

      <!-- Text -->
      <div 
        class="mb-4"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 300, duration: 800 } }"
      >
        <h1 class="text-8xl md:text-9xl font-black text-[#3396F4] mb-4 tracking-tight" style="font-weight: 900">
          DASH
        </h1>
      </div>

      <p
        class="text-3xl md:text-4xl text-slate-800 mb-3 font-bold tracking-tight"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 500, duration: 800 } }"
      >
        기록을 넘어 성장을 설계하다
      </p>

      <p
        class="text-2xl md:text-3xl text-slate-600 mb-2 font-medium"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 600, duration: 800 } }"
      >
        알고리즘 통합 플랫폼
      </p>

      <div
        class="mt-8 text-xl md:text-2xl text-slate-500 max-w-3xl font-medium"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 800, duration: 800 } }"
      >
        데이터 기반 맞춤형 분석부터 AI 코칭, 실전 모의고사까지
      </div>

      <!-- Button -->
      <button
        @click="handleLogin"
        class="mt-16 px-10 py-4 bg-[#3396F4] hover:bg-[#2B7FD4] text-white text-xl font-bold rounded-2xl shadow-md transition-colors"
        v-motion
        :initial="{ scale: 0, opacity: 0 }"
        :enter="{ scale: 1, opacity: 1, transition: { delay: 1000, duration: 500, type: 'spring' } }"
        :hover="{ scale: 1.05 }"
        :tapped="{ scale: 0.95 }"
      >
        시작하기 →
      </button>

      <!-- Badges -->
      <div
        class="mt-12 flex flex-wrap gap-3 justify-center"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 1200, duration: 800 } }"
      >
        <div
          v-for="(badge, i) in badges"
          :key="i"
          class="px-4 py-2 rounded-xl text-sm font-bold shadow-sm"
          :class="badge.color"
          v-motion
          :initial="{ scale: 0 }"
          :enter="{ scale: 1, transition: { delay: 1300 + i * 100, type: 'spring' } }"
        >
          {{ badge.text }}
        </div>
      </div>
    </div>
  </div>
</template>
