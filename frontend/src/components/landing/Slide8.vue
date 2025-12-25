<script setup lang="ts">
import { Timer, Target, TrendingUp, Award } from 'lucide-vue-next'
import { ref, onMounted, onUnmounted } from 'vue'

const timerKey = ref(0)
let interval: any

onMounted(() => {
  interval = setInterval(() => {
    timerKey.value++
  }, 1000)
})

onUnmounted(() => {
  clearInterval(interval)
})
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 right-20 w-64 h-64 bg-[#FF9600] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 left-20 w-80 h-80 bg-[#FF4B4B] opacity-5 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex h-full items-center px-8 py-16 max-w-7xl mx-auto">
      <div class="w-full">
        <div 
          class="text-center mb-12"
          v-motion
          :initial="{ y: -50, opacity: 0 }"
          :enter="{ y: 0, opacity: 1, transition: { duration: 800 } }"
        >
          <div 
            class="inline-flex items-center gap-2 px-4 py-2 bg-[#FF9600] text-white rounded-xl mb-8 font-bold text-sm"
            v-motion
            :initial="{ scale: 0 }"
            :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
          >
            <Timer class="w-4 h-4" :stroke-width="2.5" />
            Real Practice
          </div>

          <h2 class="text-5xl md:text-6xl font-black mb-4 tracking-tight">
            <span class="text-[#FF9600]">
              코딩테스트 당일의
            </span>
            <br />
            <span class="text-slate-800">긴장감까지 연습합니다</span>
          </h2>
          <p class="text-2xl text-slate-700 font-bold">
            실전 감각 극대화: 모의고사 & 랜덤디펜스 ⏱️
          </p>
        </div>

        <div class="grid md:grid-cols-2 gap-8">
          <!-- Mock Test Interface -->
          <div
            class="space-y-6"
            v-motion
            :initial="{ x: -50, opacity: 0 }"
            :enter="{ x: 0, opacity: 1, transition: { delay: 300 } }"
          >
            <!-- Timer Display -->
            <div class="p-8 bg-white rounded-3xl border-2 border-slate-200 shadow-sm">
              <div class="text-center mb-6">
                <div
                  class="inline-flex items-center justify-center w-32 h-32 bg-[#FF9600] rounded-full mb-4"
                  v-motion
                  :enter="{ scale: [1, 1.05, 1], transition: { repeat: Infinity, duration: 1000 } }"
                >
                  <Timer class="w-16 h-16 text-white" :stroke-width="2.5" />
                </div>
                <div
                  :key="timerKey"
                  class="text-5xl font-black text-slate-800 font-mono"
                  v-motion
                  :initial="{ scale: 1.2, opacity: 0 }"
                  :enter="{ scale: 1, opacity: 1 }"
                >
                  45:23
                </div>
                <p class="text-slate-600 mt-2 font-bold">남은 시간</p>
              </div>

              <div class="space-y-3">
                <div
                  class="p-4 bg-[#58CC02] bg-opacity-10 rounded-xl border-2 border-[#58CC02]"
                  v-motion
                  :initial="{ x: -20, opacity: 0 }"
                  :enter="{ x: 0, opacity: 1, transition: { delay: 500 } }"
                >
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-slate-800 font-bold">문제 1</span>
                    <span class="px-3 py-1 bg-[#58CC02] text-white text-sm rounded-lg font-bold">완료</span>
                  </div>
                  <p class="text-sm text-slate-600 font-medium">소요 시간: 12분 34초</p>
                </div>

                <div
                  class="p-4 bg-[#FF9600] bg-opacity-10 rounded-xl border-2 border-[#FF9600]"
                  v-motion
                  :initial="{ x: -20, opacity: 0 }"
                  :enter="{ x: 0, opacity: 1, transition: { delay: 700 } }"
                >
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-slate-800 font-bold">문제 2</span>
                    <span class="px-3 py-1 bg-[#FF9600] text-white text-sm rounded-lg font-bold">진행중</span>
                  </div>
                  <p class="text-sm text-slate-600 font-medium">현재 시간: 14분 46초</p>
                </div>

                <div class="p-4 bg-slate-100 rounded-xl border-2 border-slate-200 opacity-50">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-slate-600 font-bold">문제 3</span>
                    <span class="px-3 py-1 bg-slate-300 text-slate-600 text-sm rounded-lg font-bold">대기</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Difficulty Cards -->
            <div class="grid grid-cols-3 gap-3">
              <div
                v-for="(tier, index) in [
                  { level: 'Silver', color: 'bg-slate-400', icon: '🥈' },
                  { level: 'Gold', color: 'bg-[#FFC800]', icon: '🥇' },
                  { level: 'Platinum', color: 'bg-[#2DD4BF]', icon: '💎' },
                ]"
                :key="tier.level"
                class="p-4 rounded-2xl text-white text-center cursor-pointer shadow-sm hover:shadow-md transition-all"
                :class="tier.color"
                v-motion
                :initial="{ y: 30, opacity: 0 }"
                :enter="{ y: 0, opacity: 1, transition: { delay: 700 + index * 100, type: 'spring' } }"
                :hover="{ scale: 1.05 }"
              >
                <div class="text-3xl mb-2">{{ tier.icon }}</div>
                <p class="text-sm font-bold">{{ tier.level }}</p>
              </div>
            </div>
          </div>

          <!-- Features and Stats -->
          <div class="flex flex-col justify-center space-y-6">
            <div
              class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ x: 50, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 500 } }"
            >
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-[#FF9600] bg-opacity-20 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Target class="w-6 h-6 text-[#FF9600]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">기업 기출 환경</h3>
                  <p class="text-lg text-slate-600 font-medium">삼성(IM/A/B), 카카오 등 실제 환경 및 타이머 적용</p>
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
                <div class="w-12 h-12 bg-[#3396F4] bg-opacity-10 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <TrendingUp class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">정밀 분석</h3>
                  <p class="text-lg text-slate-600 font-medium">정답 여부뿐만 아니라 '풀이 소요 시간' 추적</p>
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
                <div class="w-12 h-12 bg-[#FF4B4B] bg-opacity-10 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Award class="w-6 h-6 text-[#FF4B4B]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">랜덤 디펜스</h3>
                  <p class="text-lg text-slate-600 font-medium">연승 기록 저장 및 공유로 실전 대응력 강화</p>
                </div>
              </div>
            </div>

            <!-- Stats Card -->
            <div
              class="p-6 bg-white rounded-3xl border-2 border-[#FF9600] shadow-sm"
              v-motion
              :initial="{ y: 30, opacity: 0 }"
              :enter="{ y: 0, opacity: 1, transition: { delay: 1100 } }"
            >
              <h4 class="text-lg font-bold text-slate-800 mb-4">📊 나의 실전 기록</h4>
              <div class="grid grid-cols-2 gap-4">
                <div class="text-center">
                  <p class="text-3xl font-black text-[#3396F4] mb-1">15회</p>
                  <p class="text-sm text-slate-600 font-medium">모의고사 응시</p>
                </div>
                <div class="text-center">
                  <p class="text-3xl font-black text-[#58CC02] mb-1">87%</p>
                  <p class="text-sm text-slate-600 font-medium">평균 정답률</p>
                </div>
                <div class="text-center">
                  <p class="text-3xl font-black text-[#FFC800] mb-1">8연승</p>
                  <p class="text-sm text-slate-600 font-medium">랜덤디펜스 최고</p>
                </div>
                <div class="text-center">
                  <p class="text-3xl font-black text-[#FF9600] mb-1">23분</p>
                  <p class="text-sm text-slate-600 font-medium">평균 풀이시간</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
