<script setup lang="ts">
import { Github, Chrome, Zap, CheckCircle } from 'lucide-vue-next'
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 right-20 w-64 h-64 bg-[#58CC02] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 left-20 w-80 h-80 bg-[#2DD4BF] opacity-5 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex h-full items-center px-8 py-16 max-w-7xl mx-auto">
      <div class="grid md:grid-cols-2 gap-16 w-full">
        <!-- Left side - Extension visual -->
        <div 
          class="flex items-center justify-center"
          v-motion
          :initial="{ x: -50, opacity: 0 }"
          :enter="{ x: 0, opacity: 1, transition: { duration: 800 } }"
        >
          <div class="relative w-full">
            <!-- Chrome Extension mockup -->
            <div class="p-8 bg-white rounded-3xl border-2 border-slate-200 shadow-sm">
              <div class="flex items-center gap-4 mb-8 pb-6 border-b-2 border-slate-100">
                <div class="w-14 h-14 bg-[#58CC02] bg-opacity-10 rounded-2xl flex items-center justify-center">
                  <Chrome class="w-8 h-8 text-[#58CC02]" :stroke-width="2.5" />
                </div>
                <div>
                  <h4 class="text-xl font-bold text-slate-800">DashHub Extension</h4>
                  <p class="text-sm text-slate-600 font-medium">자동 기록 시스템</p>
                </div>
              </div>

              <div class="space-y-4 mb-8">
                <div
                  class="flex items-center gap-3 p-4 bg-[#58CC02] bg-opacity-10 rounded-xl border-2 border-[#58CC02]"
                  v-motion
                  :initial="{ x: -20, opacity: 0 }"
                  :enter="{ x: 0, opacity: 1, transition: { delay: 500 } }"
                >
                  <CheckCircle class="w-6 h-6 text-[#58CC02]" :stroke-width="2.5" />
                  <span class="text-slate-800 font-bold">백준 1000번 정답! 🎉</span>
                </div>

                <div
                  class="flex items-center gap-3 p-4 bg-[#3396F4] bg-opacity-10 rounded-xl border-2 border-[#3396F4]"
                  v-motion
                  :initial="{ x: -20, opacity: 0 }"
                  :enter="{ x: 0, opacity: 1, transition: { delay: 700 } }"
                >
                  <Github class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
                  <span class="text-slate-800 font-bold">GitHub 커밋 완료 ✅</span>
                </div>

                <div
                  class="flex items-center gap-3 p-4 bg-[#FFC800] bg-opacity-20 rounded-xl border-2 border-[#FFC800]"
                  v-motion
                  :initial="{ x: -20, opacity: 0 }"
                  :enter="{ x: 0, opacity: 1, transition: { delay: 900 } }"
                >
                  <Zap class="w-6 h-6 text-[#FF9600]" :stroke-width="2.5" />
                  <span class="text-slate-800 font-bold">서버 동기화 완료 ⚡</span>
                </div>
              </div>

              <button class="w-full py-4 bg-[#58CC02] hover:bg-[#4CAD02] rounded-xl text-white font-bold shadow-sm hover:shadow-md transition-all">
                확장 프로그램 설치
              </button>
            </div>

            <!-- GitHub contribution graph mockup -->
            <div
              class="mt-6 p-5 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ y: 20, opacity: 0 }"
              :enter="{ y: 0, opacity: 1, transition: { delay: 1100 } }"
            >
              <div class="flex items-center gap-2 mb-4">
                <Github class="w-5 h-5 text-slate-700" :stroke-width="2" />
                <p class="text-sm text-slate-700 font-bold">자동 커밋 현황</p>
              </div>
              <div class="grid grid-cols-12 gap-1.5">
                <div
                  v-for="(_, i) in 48"
                  :key="i"
                  class="w-full aspect-square rounded"
                  :class="[
                     'bg-slate-100', 'bg-[#58CC02] opacity-20', 'bg-[#58CC02] opacity-40', 'bg-[#58CC02] opacity-70', 'bg-[#58CC02]'
                  ][Math.floor(Math.random() * 5)]" 
                  v-motion
                  :initial="{ scale: 0 }"
                  :enter="{ scale: 1, transition: { delay: 1200 + i * 10 } }"
                >
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right side - Text content -->
        <div class="flex flex-col justify-center">
          <div
            v-motion
            :initial="{ x: 50, opacity: 0 }"
            :enter="{ x: 0, opacity: 1, transition: { duration: 800 } }"
          >
            <div 
              class="inline-flex items-center gap-2 px-4 py-2 bg-[#58CC02] text-white rounded-xl mb-8 font-bold text-sm"
              v-motion
              :initial="{ scale: 0 }"
              :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
            >
              <Zap class="w-4 h-4" :stroke-width="2.5" />
              Zero-Touch
            </div>

            <h2 class="text-5xl md:text-6xl font-black text-slate-800 mb-6 leading-tight tracking-tight">
              제출만 하세요.<br />
              <span class="text-[#58CC02]">
                기록은 DASH가
              </span>
            </h2>

            <p class="text-2xl text-slate-700 mb-10 font-bold">
              DashHub 익스텐션 🚀
            </p>

            <div class="space-y-6">
              <div
                class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
                v-motion
                :initial="{ x: 30, opacity: 0 }"
                :enter="{ x: 0, opacity: 1, transition: { delay: 300 } }"
              >
                <div class="flex items-center gap-3 mb-2">
                  <div class="w-2 h-2 bg-[#58CC02] rounded-full"></div>
                  <h3 class="text-xl font-bold text-slate-800">확장 프로그램 연동</h3>
                </div>
                <p class="text-slate-600 pl-5 font-medium">백준/SWEA 제출 결과 실시간 감지</p>
              </div>

              <div
                class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
                v-motion
                :initial="{ x: 30, opacity: 0 }"
                :enter="{ x: 0, opacity: 1, transition: { delay: 500 } }"
              >
                <div class="flex items-center gap-3 mb-2">
                  <div class="w-2 h-2 bg-[#3396F4] rounded-full"></div>
                  <h3 class="text-xl font-bold text-slate-800">자동 GitHub 커밋</h3>
                </div>
                <p class="text-slate-600 pl-5 font-medium">정해진 포맷에 맞춰 개인 리포지토리에 저장</p>
              </div>

              <div
                class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
                v-motion
                :initial="{ x: 30, opacity: 0 }"
                :enter="{ x: 0, opacity: 1, transition: { delay: 700 } }"
              >
                <div class="flex items-center gap-3 mb-2">
                  <div class="w-2 h-2 bg-[#FF9600] rounded-full"></div>
                  <h3 class="text-xl font-bold text-slate-800">간편 웹훅</h3>
                </div>
                <p class="text-slate-600 pl-5 font-medium">클릭 한 번으로 백엔드 서버와 동기화 설정 완료</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
