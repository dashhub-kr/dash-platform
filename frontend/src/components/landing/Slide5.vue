<script setup lang="ts">
import { ArrowRight, Github, Mail, Globe } from 'lucide-vue-next'

const socialIcons = [
  { emoji: '🌐', color: '#3396F4', link: '#' },
  { emoji: '📱', color: '#58CC02', link: '#' },
  { emoji: '💬', color: '#2DD4BF', link: '#' },
  { emoji: '📧', color: '#FFC800', link: '#' }
]

const sparkles = Array.from({ length: 6 }, (_, i) => ({
  id: i,
  initial: { x: 0, y: 0, scale: 0, opacity: 0 },
  enter: {
    x: (Math.random() - 0.5) * 400,
    y: (Math.random() - 0.5) * 400,
    scale: [0, 1, 0],
    opacity: [0, 1, 0],
    transition: {
      duration: 2000,
      delay: 1500 + i * 200,
      repeat: Infinity,
      repeatDelay: 3000,
    }
  }
}))

const handleLogin = () => {
  window.location.href = `/oauth2/authorization/github`
}
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Animated gradient orbs -->
    <div class="absolute inset-0">
      <div
        class="absolute top-1/4 left-1/4 w-96 h-96 bg-[#3396F4] opacity-10 rounded-full blur-3xl"
        v-motion
        :enter="{
          scale: [1, 1.2, 1],
          x: [0, 50, 0],
          y: [0, -30, 0],
          transition: {
            duration: 10000,
            repeat: Infinity,
            repeatType: 'reverse',
          }
        }"
      />
      <div
        class="absolute bottom-1/4 right-1/4 w-96 h-96 bg-[#58CC02] opacity-10 rounded-full blur-3xl"
        v-motion
        :enter="{
          scale: [1, 1.3, 1],
          x: [0, -50, 0],
          y: [0, 50, 0],
          transition: {
            duration: 12000,
            repeat: Infinity,
            repeatType: 'reverse',
          }
        }"
      />
    </div>

    <div class="relative z-10 flex h-full flex-col items-center justify-center px-8 text-center">
      <!-- Logo/Icon -->
      <div
        class="mb-12"
        v-motion
        :initial="{ scale: 0, rotate: -180 }"
        :enter="{ scale: 1, rotate: 0, transition: { duration: 1000, ease: 'easeOut', type: 'spring' } }"
      >
        <div class="inline-flex items-center justify-center p-8 bg-white rounded-3xl shadow-md border-4 border-[#3396F4]">
          <div class="text-7xl">📊</div>
        </div>
      </div>

      <!-- Main Message -->
      <h1
        class="text-6xl md:text-7xl font-black mb-6 max-w-5xl tracking-tight"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 300, duration: 800 } }"
      >
        <span class="text-[#3396F4]">
          당신의 다음 커밋이
        </span>
        <br />
        <span class="text-slate-800">
          성장의 증거가 됩니다
        </span>
      </h1>

      <p
        class="text-3xl text-slate-700 mb-12 font-bold"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 500, duration: 800 } }"
      >
        지금 DASH에서 분석을 시작하세요 ✨
      </p>

      <!-- CTA Button -->
      <button
        @click="handleLogin"
        class="group px-12 py-5 bg-[#3396F4] hover:bg-[#2B7FD4] rounded-2xl text-white text-2xl font-bold shadow-md hover:shadow-lg transition-all flex items-center gap-3"
        v-motion
        :initial="{ scale: 0, opacity: 0 }"
        :enter="{ scale: 1, opacity: 1, transition: { delay: 700, duration: 500, type: 'spring' } }"
        :hover="{ scale: 1.05 }"
        :tapped="{ scale: 0.95 }"
      >
        시작하기
        <div
          v-motion
          :enter="{ x: [0, 5, 0], transition: { repeat: Infinity, duration: 1500 } }"
        >
          <ArrowRight class="w-7 h-7" :stroke-width="2.5" />
        </div>
      </button>

      <!-- Service URL -->
      <div
        class="mt-16 p-8 bg-white rounded-3xl border-2 border-slate-200 shadow-sm"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 900, duration: 800 } }"
      >
        <div class="flex items-center gap-3 justify-center mb-6">
          <Globe class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
          <span class="text-2xl text-[#3396F4] font-black font-mono">www.dash-algo.com</span>
        </div>
        
        <div class="flex items-center justify-center gap-8 text-slate-600">
          <a
            href="#"
            class="flex items-center gap-2 hover:text-[#3396F4] transition-colors font-medium"
            v-motion
            :hover="{ scale: 1.1 }"
          >
            <Mail class="w-5 h-5" :stroke-width="2" />
            <span>contact@dash.com</span>
          </a>
          
          <a
            href="#"
            class="flex items-center gap-2 hover:text-[#3396F4] transition-colors font-medium"
            v-motion
            :hover="{ scale: 1.1 }"
          >
            <Github class="w-5 h-5" :stroke-width="2" />
            <span>https://github.com/utact/dash</span>
          </a>
        </div>
      </div>

      <!-- Social Icons -->
      <div
        class="mt-8 flex gap-4"
        v-motion
        :initial="{ y: 30, opacity: 0 }"
        :enter="{ y: 0, opacity: 1, transition: { delay: 1100, duration: 800 } }"
      >
        <div
          v-for="(item, index) in socialIcons"
          :key="index"
          class="w-14 h-14 bg-white rounded-2xl flex items-center justify-center border-2 border-slate-200 shadow-sm hover:shadow-md cursor-pointer transition-all"
          :style="{ borderColor: item.color + '40' }"
          v-motion
          :hover="{ scale: 1.2, rotate: 10 }"
          :tapped="{ scale: 0.9 }"
        >
          <span class="text-2xl">{{ item.emoji }}</span>
        </div>
      </div>

      <!-- Sparkle effects -->
      <div
        v-for="s in sparkles"
        :key="s.id"
        class="absolute text-4xl"
        v-motion
        :initial="s.initial"
        :enter="s.enter"
      >
        ✨
      </div>
    </div>
  </div>
</template>
