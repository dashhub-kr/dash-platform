<script setup lang="ts">
import { Brain, MessageSquare, AlertTriangle, Code } from 'lucide-vue-next'
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 left-20 w-64 h-64 bg-[#3396F4] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 right-20 w-80 h-80 bg-[#FF9600] opacity-5 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex h-full items-center px-8 py-16 max-w-7xl mx-auto">
      <div class="w-full">
        <div 
          class="text-center mb-12"
          v-motion
          :initial="{ y: -50, opacity: 0 }"
          :enter="{ y: 0, opacity: 1, transition: { duration: 800 } }"
        >
          <div 
            class="inline-flex items-center gap-2 px-4 py-2 bg-[#3396F4] text-white rounded-xl mb-8 font-bold text-sm"
            v-motion
            :initial="{ scale: 0 }"
            :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
          >
            <Brain class="w-4 h-4" :stroke-width="2.5" />
            AI-Powered
          </div>

          <h2 class="text-5xl md:text-6xl font-black text-slate-800 mb-4 tracking-tight">
            <span class="text-[#3396F4]">
              AI가 리뷰어의 눈으로
            </span>
            <br />당신의 코드를 해부합니다
          </h2>

          <p class="text-2xl text-slate-700 font-bold">
            지능형 피드백: AI 코드 리뷰 & 튜터 🤖
          </p>
        </div>

        <div class="grid md:grid-cols-2 gap-8">
          <!-- Code editor mockup -->
          <div
            class="relative"
            v-motion
            :initial="{ x: -50, opacity: 0 }"
            :enter="{ x: 0, opacity: 1, transition: { delay: 300, duration: 800 } }"
          >
            <div class="p-6 bg-white rounded-3xl border-2 border-slate-200 shadow-sm">
              <div class="flex items-center gap-2 mb-4 pb-3 border-b-2 border-slate-100">
                <div class="flex gap-1.5">
                  <div class="w-3 h-3 bg-[#FF4B4B] rounded-full"></div>
                  <div class="w-3 h-3 bg-[#FFC800] rounded-full"></div>
                  <div class="w-3 h-3 bg-[#58CC02] rounded-full"></div>
                </div>
                <span class="text-sm text-slate-600 ml-3 font-bold">solution.py</span>
              </div>

              <div class="font-mono text-sm space-y-2 mb-4">
                <div class="text-slate-500"><span class="select-none">1 </span><span class="text-[#FF4B4B]">def</span> <span class="text-[#3396F4]">binary_search</span>(<span class="text-[#FF9600]">arr, target</span>):</div>
                <div class="text-slate-500"><span class="select-none">2   </span><span class="text-slate-700">left, right = 0, len(arr) - 1</span></div>
                <div class="text-slate-500 relative">
                  <span class="select-none">3   </span><span class="text-[#FF4B4B]">for</span> <span class="text-slate-700">i in</span> <span class="text-[#3396F4]">range</span>(<span class="text-[#3396F4]">len</span>(<span class="text-[#FF9600]">arr</span>)):
                  <div
                    class="absolute -right-2 top-1 w-2 h-2 bg-[#FFC800] rounded-full"
                    v-motion
                    :initial="{ scale: 0 }"
                    :enter="{ scale: 1, transition: { delay: 1000 } }"
                  ></div>
                </div>
                <div class="text-slate-500"><span class="select-none">4     </span><span class="text-slate-700">result.append(arr[i] * 2)</span></div>
                <div class="text-slate-500"><span class="select-none">5   </span><span class="text-[#FF4B4B]">return</span> <span class="text-slate-700">result</span></div>
              </div>

              <!-- AI comment bubbles -->
              <div
                class="mt-4 p-4 bg-[#3396F4] bg-opacity-10 rounded-2xl border-2 border-[#3396F4] relative"
                v-motion
                :initial="{ x: -20, opacity: 0 }"
                :enter="{ x: 0, opacity: 1, transition: { delay: 1200 } }"
              >
                <div class="flex items-start gap-3">
                  <div class="w-8 h-8 bg-[#3396F4] rounded-full flex items-center justify-center flex-shrink-0">
                    <Brain class="w-5 h-5 text-white" :stroke-width="2.5" />
                  </div>
                  <div class="flex-1">
                    <p class="text-sm font-bold text-[#3396F4] mb-1">💡 AI 리뷰</p>
                    <p class="text-sm text-slate-700 font-medium mb-2">리스트 컴프리헨션을 사용하면 더 효율적입니다:</p>
                    <code class="block p-2 bg-slate-100 rounded-lg text-xs text-[#58CC02] font-bold">
                      return [x * 2 for x in arr]
                    </code>
                  </div>
                </div>
              </div>

              <div
                class="mt-3 p-4 bg-[#FF9600] bg-opacity-10 rounded-2xl border-2 border-[#FF9600]"
                v-motion
                :initial="{ x: -20, opacity: 0 }"
                :enter="{ x: 0, opacity: 1, transition: { delay: 1500 } }"
              >
                <div class="flex items-start gap-3">
                  <AlertTriangle class="w-6 h-6 text-[#FF9600] flex-shrink-0" :stroke-width="2.5" />
                  <div class="flex-1">
                    <p class="text-sm font-bold text-[#FF9600] mb-1">⚠️ 반례 탐색</p>
                    <p class="text-sm text-slate-700 font-medium">빈 배열이 입력될 경우를 고려하세요</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Features -->
          <div class="flex flex-col justify-center space-y-6">
            <div
              class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm hover:shadow-md transition-all"
              v-motion
              :initial="{ x: 50, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 500 } }"
            >
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-[#3396F4] bg-opacity-10 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Code class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">라인별 해석</h3>
                  <p class="text-lg text-slate-600 font-medium">복잡한 로직을 AI가 알기 쉽게 설명</p>
                </div>
              </div>
            </div>

            <div
              class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm hover:shadow-md transition-all"
              v-motion
              :initial="{ x: 50, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 700 } }"
            >
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-[#FF9600] bg-opacity-20 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <AlertTriangle class="w-6 h-6 text-[#FF9600]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">반례 탐색</h3>
                  <p class="text-lg text-slate-600 font-medium">놓치기 쉬운 엣지 케이스와 반례 자동 제안</p>
                </div>
              </div>
            </div>

            <div
              class="p-6 bg-white rounded-2xl border-2 border-slate-200 shadow-sm hover:shadow-md transition-all"
              v-motion
              :initial="{ x: 50, opacity: 0 }"
              :enter="{ x: 0, opacity: 1, transition: { delay: 900 } }"
            >
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-[#2DD4BF] bg-opacity-20 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <MessageSquare class="w-6 h-6 text-[#2DD4BF]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-2xl font-bold text-slate-800 mb-2 tracking-tight">AI 튜터</h3>
                  <p class="text-lg text-slate-600 font-medium">막히는 순간, 실시간 질의응답으로 힌트 획득</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
