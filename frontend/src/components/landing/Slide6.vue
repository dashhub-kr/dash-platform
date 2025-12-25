<script setup lang="ts">
import { Users, Target, TrendingUp, Sparkles } from 'lucide-vue-next'

const teamMembers = [
  { name: '멤버 A', strength: '그래프', weakness: 'DP', level: 85, emoji: '👨‍💻' },
  { name: '멤버 B', strength: '구현', weakness: '그리디', level: 72, emoji: '👩‍💻' },
  { name: '멤버 C', strength: 'DP', weakness: '문자열', level: 68, emoji: '🧑‍💻' },
  { name: '멤버 D', strength: '그리디', weakness: '그래프', level: 90, emoji: '👨‍💼' },
]

const curriculumWeeks = [
  '1주차: DP 기초 (백준 1463, 9095, 11726)',
  '2주차: DP 심화 (백준 2579, 1149, 11053)',
  '3주차: DP 응용 (백준 12865, 9251)'
]
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 right-20 w-64 h-64 bg-[#2DD4BF] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 left-20 w-80 h-80 bg-[#3396F4] opacity-5 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex h-full items-center px-8 py-16 max-w-7xl mx-auto">
      <div class="w-full">
        <div 
          class="text-center mb-12"
          v-motion
          :initial="{ y: -50, opacity: 0 }"
          :enter="{ y: 0, opacity: 1, transition: { duration: 800 } }"
        >
          <div 
            class="inline-flex items-center gap-2 px-4 py-2 bg-[#2DD4BF] text-white rounded-xl mb-8 font-bold text-sm"
            v-motion
            :initial="{ scale: 0 }"
            :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
          >
            <Users class="w-4 h-4" :stroke-width="2.5" />
            Study Innovation
          </div>

          <h2 class="text-5xl md:text-6xl font-black text-slate-800 mb-4 tracking-tight">
            막막한 스터디 커리큘럼,
          </h2>
          <p class="text-4xl text-[#2DD4BF] font-bold tracking-tight">
            데이터가 정답을 알려줍니다 📊
          </p>
        </div>

        <div class="grid md:grid-cols-2 gap-8">
          <!-- Team Analysis -->
          <div
            class="space-y-4"
            v-motion
            :initial="{ x: -50, opacity: 0 }"
            :enter="{ x: 0, opacity: 1, transition: { delay: 300 } }"
          >
            <h3 class="text-2xl font-bold text-slate-800 mb-6 flex items-center gap-3">
              <Users class="w-8 h-8 text-[#2DD4BF]" :stroke-width="2.5" />
              팀 역량 분석
            </h3>

            <div
              v-for="(member, index) in teamMembers"
              :key="index"
              class="p-4 bg-white rounded-2xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ x: -30, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 500 + index * 100 } }"
            >
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-3">
                  <span class="text-2xl">{{ member.emoji }}</span>
                  <span class="text-lg font-bold text-slate-800">{{ member.name }}</span>
                </div>
                <span class="px-3 py-1 bg-[#3396F4] bg-opacity-10 rounded-xl text-[#3396F4] text-sm font-bold">
                  Lv. {{ member.level }}
                </span>
              </div>
              <div class="flex gap-4 text-sm font-medium">
                <div class="flex-1 px-3 py-2 bg-[#58CC02] bg-opacity-10 rounded-lg">
                  <span class="text-[#58CC02] font-bold">💪 </span>
                  <span class="text-slate-700">{{ member.strength }}</span>
                </div>
                <div class="flex-1 px-3 py-2 bg-[#FF4B4B] bg-opacity-10 rounded-lg">
                  <span class="text-[#FF4B4B] font-bold">📚 </span>
                  <span class="text-slate-700">{{ member.weakness }}</span>
                </div>
              </div>
            </div>

            <!-- Team Summary -->
            <div
              class="p-6 bg-[#2DD4BF] bg-opacity-10 rounded-3xl border-2 border-[#2DD4BF] mt-6"
              v-motion
              :initial="{ scale: 0.9, opacity: 0 }"
              :enter="{ scale: 1, opacity: 1, transition: { delay: 1200 } }"
            >
              <h4 class="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
                <TrendingUp class="w-6 h-6 text-[#2DD4BF]" :stroke-width="2.5" />
                팀 종합 리포트
              </h4>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <p class="text-slate-600 mb-1 font-medium text-sm">팀 평균 레벨</p>
                  <p class="text-3xl font-black text-[#2DD4BF]">78.8</p>
                </div>
                <div>
                  <p class="text-slate-600 mb-1 font-medium text-sm">공통 약점</p>
                  <p class="text-3xl font-black text-[#FF4B4B]">DP</p>
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
                <div class="w-12 h-12 bg-[#2DD4BF] bg-opacity-20 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Target class="w-6 h-6 text-[#2DD4BF]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">팀 역량 분석</h3>
                  <p class="text-lg text-slate-600 font-medium">스터디원 전체 데이터를 합산하여 그룹 전체의 취약점 도출</p>
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
                  <Sparkles class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">AI 커리큘럼 제안</h3>
                  <p class="text-lg text-slate-600 font-medium">팀 실력에 맞는 주차별 미션 문제 자동 추천</p>
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
                <div class="w-12 h-12 bg-[#58CC02] bg-opacity-10 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Users class="w-6 h-6 text-[#58CC02]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">원클릭 미션 관리</h3>
                  <p class="text-lg text-slate-600 font-medium">버튼 하나로 팀원들에게 과제 부여 및 달성도 확인</p>
                </div>
              </div>
            </div>

            <!-- Curriculum preview -->
            <div
              class="p-6 bg-white rounded-2xl border-2 border-[#3396F4] shadow-sm"
              v-motion
              :initial="{ y: 30, opacity: 0 }"
              :enter="{ y: 0, opacity: 1, transition: { delay: 1100 } }"
            >
              <h4 class="text-lg font-bold text-slate-800 mb-4">📚 추천 커리큘럼</h4>
              <div class="space-y-2 text-sm">
                <div 
                  v-for="(week, i) in curriculumWeeks" 
                  :key="i" 
                  class="flex items-center gap-2 text-slate-700 font-medium"
                >
                  <div class="w-6 h-6 rounded-full flex items-center justify-center text-white text-xs font-bold" :class="i < 2 ? 'bg-[#58CC02]' : 'bg-slate-300'">{{ i + 1 }}</div>
                  <span>{{ week }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
