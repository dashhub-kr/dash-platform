<script setup lang="ts">
import { MessageSquare, Code, Trophy, Users } from 'lucide-vue-next'

const leaderboard = [
  { rank: 1, name: 'coder_pro', points: 2850, icon: '🥇' },
  { rank: 2, name: 'algorithm_master', points: 2340, icon: '🥈' },
  { rank: 3, name: 'dev_enthusiast', points: 1920, icon: '🥉' },
]

const rewards = [
  { amount: '+50', desc: '답변 채택', color: 'text-[#58CC02]' },
  { amount: '+20', desc: '질문 등록', color: 'text-[#3396F4]' },
  { amount: '+10', desc: '댓글 작성', color: 'text-[#FF9600]' },
]
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 left-20 w-64 h-64 bg-[#2DD4BF] opacity-5 rounded-full blur-3xl"></div>
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
            class="inline-flex items-center gap-2 px-4 py-2 bg-[#2DD4BF] text-white rounded-xl mb-8 font-bold text-sm"
            v-motion
            :initial="{ scale: 0 }"
            :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
          >
            <Users class="w-4 h-4" :stroke-width="2.5" />
            Community
          </div>

          <h2 class="text-5xl md:text-6xl font-black text-slate-800 mb-4 tracking-tight">
            우리 스터디를 넘어,
          </h2>
          <p class="text-4xl text-[#2DD4BF] font-bold tracking-tight">
            전국 개발자의 지식을 공유하세요 🌐
          </p>
        </div>

        <div class="grid md:grid-cols-2 gap-8">
          <!-- Community Interface -->
          <div
            class="space-y-6"
            v-motion
            :initial="{ x: -50, opacity: 0 }"
            :enter="{ x: 0, opacity: 1, transition: { delay: 300 } }"
          >
            <!-- Code with inline comments -->
            <div class="p-6 bg-white rounded-3xl border-2 border-slate-200 shadow-sm">
              <div class="flex items-center gap-3 mb-4 pb-3 border-b-2 border-slate-100">
                <Code class="w-6 h-6 text-[#2DD4BF]" :stroke-width="2.5" />
                <h3 class="text-xl font-bold text-slate-800">인라인 코멘트</h3>
              </div>

              <div class="font-mono text-sm space-y-3 mb-4">
                <div class="flex items-start gap-3">
                  <span class="text-slate-400 select-none">1</span>
                  <span class="text-[#FF4B4B]">def</span>
                  <span class="text-[#3396F4] flex-1">binary_search(arr, target):</span>
                </div>
                
                <div class="relative">
                  <div class="flex items-start gap-3">
                    <span class="text-slate-400 select-none">2</span>
                    <span class="text-slate-700 flex-1 pl-4">left, right = 0, len(arr) - 1</span>
                  </div>
                  
                  <!-- Inline comment -->
                  <div
                    class="ml-8 mt-2 p-3 bg-[#2DD4BF] bg-opacity-10 rounded-xl border-l-4 border-[#2DD4BF]"
                    v-motion
                    :initial="{ x: -20, opacity: 0 }"
                    :enter="{ x: 0, opacity: 1, transition: { delay: 700 } }"
                  >
                    <div class="flex items-start gap-2">
                      <div class="w-6 h-6 bg-[#2DD4BF] rounded-full flex items-center justify-center text-white text-xs flex-shrink-0 font-bold">
                        A
                      </div>
                      <div class="flex-1">
                        <p class="text-xs text-[#2DD4BF] mb-1 font-bold">@user_a · 2시간 전</p>
                        <p class="text-sm text-slate-700 font-medium">right를 len(arr)로 설정하면 어떤 차이가 있나요?</p>
                      </div>
                    </div>
                  </div>

                  <div
                    class="ml-12 mt-2 p-3 bg-[#3396F4] bg-opacity-10 rounded-xl border-l-4 border-[#3396F4]"
                    v-motion
                    :initial="{ x: -20, opacity: 0 }"
                    :enter="{ x: 0, opacity: 1, transition: { delay: 900 } }"
                  >
                    <div class="flex items-start gap-2">
                      <div class="w-6 h-6 bg-[#3396F4] rounded-full flex items-center justify-center text-white text-xs flex-shrink-0 font-bold">
                        B
                      </div>
                      <div class="flex-1">
                        <p class="text-xs text-[#3396F4] mb-1 font-bold">@user_b · 1시간 전</p>
                        <p class="text-sm text-slate-700 font-medium">범위를 [left, right)로 설정하는 방식입니다! 👍</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <button class="w-full py-3 bg-[#2DD4BF] bg-opacity-10 hover:bg-[#2DD4BF] hover:bg-opacity-20 border-2 border-[#2DD4BF] rounded-xl text-[#2DD4BF] font-bold transition-all">
                + 코멘트 작성하기
              </button>
            </div>

            <!-- Leaderboard -->
            <div
              class="p-6 bg-white rounded-3xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ y: 30, opacity: 0 }"
              :enter="{ y: 0, opacity: 1, transition: { delay: 1100 } }"
            >
              <div class="flex items-center gap-3 mb-4">
                <Trophy class="w-6 h-6 text-[#FFC800]" :stroke-width="2.5" />
                <h3 class="text-xl font-bold text-slate-800">이번 주 리더보드</h3>
              </div>

              <div class="space-y-3">
                <div
                  v-for="(user, index) in leaderboard"
                  :key="user.rank"
                  class="flex items-center gap-4 p-3 bg-slate-50 rounded-xl border-2 border-slate-200"
                  v-motion
                  :initial="{ x: -30, opacity: 0 }"
                  :enter="{ x: 0, opacity: 1, transition: { delay: 1300 + index * 100 } }"
                >
                  <span class="text-2xl">{{ user.icon }}</span>
                  <div class="flex-1">
                    <p class="text-slate-800 font-bold">{{ user.name }}</p>
                    <p class="text-sm text-slate-600 font-medium">{{ user.points }} 도토리 🌰</p>
                  </div>
                  <div class="text-[#2DD4BF] font-black text-xl">#{{ user.rank }}</div>
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
                  <MessageSquare class="w-6 h-6 text-[#2DD4BF]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">인라인 코멘트</h3>
                  <p class="text-lg text-slate-600 font-medium">특정 코드 라인에 직접 질문/답변 작성</p>
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
                  <Code class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">전국 게시판</h3>
                  <p class="text-lg text-slate-600 font-medium">컬러 스크립트 기반 가독성 높은 코드 공유</p>
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
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">리더보드 & 보상</h3>
                  <p class="text-lg text-slate-600 font-medium">활동 보상 '도토리' 적립 및 실시간 랭킹 시스템</p>
                </div>
              </div>
            </div>

            <!-- Rewards Card -->
            <div
              class="p-6 bg-white rounded-3xl border-2 border-[#FFC800] shadow-sm"
              v-motion
              :initial="{ y: 30, opacity: 0 }"
              :enter="{ y: 0, opacity: 1, transition: { delay: 1100 } }"
            >
              <h4 class="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
                🌰 나의 도토리
              </h4>
              <div class="flex items-center justify-between mb-4">
                <div>
                  <p class="text-4xl font-black text-[#FF9600]">1,250</p>
                  <p class="text-sm text-slate-600 mt-1 font-medium">보유 중</p>
                </div>
                <div
                  class="text-6xl"
                  v-motion
                  :enter="{ rotate: [0, 10, -10, 0], transition: { repeat: Infinity, duration: 2000, repeatDelay: 1000 } }"
                >
                  🌰
                </div>
              </div>
              <div class="grid grid-cols-3 gap-2 text-xs">
                <div v-for="(reward, index) in rewards" :key="index" class="p-2 bg-slate-50 rounded-lg text-center border border-slate-200">
                  <p class="font-black" :class="reward.color">{{ reward.amount }}</p>
                  <p class="text-slate-600 font-medium">{{ reward.desc }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
