<script setup lang="ts">
import { Zap, TrendingUp, Trophy, CheckCircle } from 'lucide-vue-next'

const benefits = [
  {
    icon: Zap,
    title: "학습 효율성",
    description: "자동 기록과 AI 피드백으로 공부 시간 2배 활용",
    stat: "200%",
    color: "#FFC800",
    emoji: "⚡"
  },
  {
    icon: TrendingUp,
    title: "동기 부여",
    description: "시각화된 성장 지표와 재화 시스템",
    stat: "+85%",
    color: "#58CC02",
    emoji: "📈"
  },
  {
    icon: Trophy,
    title: "취업 경쟁력",
    description: "데이터로 입증된 기출 실전 감각",
    stat: "실전",
    color: "#3396F4",
    emoji: "🏆"
  }
]

const features = [
  { text: "Solved.ac ID로 즉시 역량 분석", color: "#3396F4" },
  { text: "자동 GitHub 커밋 & 기록 관리", color: "#58CC02" },
  { text: "AI 코드 리뷰 & 실시간 튜터링", color: "#3396F4" },
  { text: "데이터 기반 팀 커리큘럼 생성", color: "#2DD4BF" },
  { text: "RPG 스타일 알고리즘 스킬 트리", color: "#FFC800" },
  { text: "실전 환경 모의고사 & 랜덤디펜스", color: "#FF9600" },
  { text: "인라인 코멘트 커뮤니티 시스템", color: "#2DD4BF" },
  { text: "보상형 리더보드 & 도토리 시스템", color: "#FFC800" }
]

const particles = Array.from({ length: 20 }, (_, i) => ({
  id: i,
  char: ['⭐', '🎯', '🚀', '💎', '✨'][Math.floor(Math.random() * 5)],
  initial: {
    x: Math.random() * (typeof window !== 'undefined' ? window.innerWidth : 1000),
    y: (typeof window !== 'undefined' ? window.innerHeight : 800) + 50,
    scale: 0,
  },
  enter: {
    y: -50,
    scale: [0, 1, 0],
    transition: {
      duration: (3 + Math.random() * 2) * 1000,
      delay: Math.random() * 2000,
      repeat: Infinity,
      repeatDelay: Math.random() * 3000,
    }
  }
}))
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 left-20 w-64 h-64 bg-[#58CC02] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 right-20 w-80 h-80 bg-[#3396F4] opacity-5 rounded-full blur-3xl"></div>

    <!-- Animated success particles -->
    <div
      v-for="p in particles"
      :key="p.id"
      class="absolute text-3xl"
      v-motion
      :initial="p.initial"
      :enter="p.enter"
    >
      {{ p.char }}
    </div>

    <div class="relative z-10 flex h-full items-center px-8 py-16 max-w-7xl mx-auto">
      <div class="w-full">
        <div 
          class="text-center mb-16"
          v-motion
          :initial="{ y: -50, opacity: 0 }"
          :enter="{ y: 0, opacity: 1, transition: { duration: 800 } }"
        >
          <div 
            class="inline-flex items-center gap-2 px-4 py-2 bg-[#58CC02] text-white rounded-xl mb-8 font-bold text-sm"
            v-motion
            :initial="{ scale: 0 }"
            :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
          >
            <CheckCircle class="w-4 h-4" :stroke-width="2.5" />
            Summary
          </div>

          <h2 class="text-6xl md:text-7xl font-black mb-6 tracking-tight">
            <span class="text-[#3396F4]">
              DASH와 함께라면
            </span>
          </h2>
          <p class="text-4xl text-slate-800 font-bold">
            알고리즘은 더 이상 '장벽'이 아닙니다 🚀
          </p>
        </div>

        <!-- Benefits Grid -->
        <div class="grid md:grid-cols-3 gap-8 mb-12">
          <div
            v-for="(benefit, index) in benefits"
            :key="index"
            class="relative group"
            v-motion
            :initial="{ y: 50, opacity: 0, scale: 0.9 }"
            :enter="{ y: 0, opacity: 1, scale: 1, transition: { delay: 300 + index * 200, duration: 800, type: 'spring' } }"
            :hover="{ y: -8, transition: { duration: 200 } }"
          >
            <div class="p-8 bg-white rounded-3xl border-2 border-slate-200 shadow-sm hover:shadow-md transition-all duration-300 h-full">
              <div
                class="inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-6 text-4xl"
                :style="{ backgroundColor: `${benefit.color}20` }"
                v-motion
                :initial="{ scale: 0, rotate: -180 }"
                :enter="{ scale: 1, rotate: 0, transition: { delay: 500 + index * 200, duration: 600, type: 'spring' } }"
              >
                {{ benefit.emoji }}
              </div>

              <div
                class="text-5xl font-black mb-4"
                :style="{ color: benefit.color }"
                v-motion
                :initial="{ scale: 0 }"
                :enter="{ scale: 1, transition: { delay: 700 + index * 200 } }"
              >
                {{ benefit.stat }}
              </div>

              <h3 class="text-2xl font-bold text-slate-800 mb-3 tracking-tight">{{ benefit.title }}</h3>
              <p class="text-lg text-slate-600 leading-relaxed font-medium">{{ benefit.description }}</p>
            </div>
          </div>
        </div>

        <!-- Key Features Summary -->
        <div
          class="p-8 bg-white rounded-3xl border-2 border-slate-200 shadow-sm"
          v-motion
          :initial="{ y: 50, opacity: 0 }"
          :enter="{ y: 0, opacity: 1, transition: { delay: 1200, duration: 800 } }"
        >
          <h3 class="text-2xl font-bold text-slate-800 mb-6 text-center">핵심 기능 요약 ✨</h3>
          
          <div class="grid md:grid-cols-2 gap-4">
            <div
              v-for="(feature, index) in features"
              :key="index"
              class="flex items-center gap-3 p-4 bg-slate-50 rounded-xl border border-slate-200"
              v-motion
              :initial="{ x: index % 2 === 0 ? -30 : 30, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 1400 + index * 100 } }"
            >
              <CheckCircle 
                class="w-6 h-6 flex-shrink-0" 
                :stroke-width="2.5"
                :style="{ color: feature.color }"
              />
              <span class="text-slate-800 font-medium">{{ feature.text }}</span>
            </div>
          </div>
        </div>

        <!-- Bottom tagline -->
        <div
          class="text-center mt-12"
          v-motion
          :initial="{ scale: 0.8, opacity: 0 }"
          :enter="{ scale: 1, opacity: 1, transition: { delay: 2000, duration: 800 } }"
        >
          <p class="text-2xl text-slate-600 font-bold">
            성장의 모든 단계를 DASH가 함께합니다 💪
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
