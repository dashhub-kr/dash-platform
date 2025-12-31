<script setup lang="ts">
import { Sparkles, User, Zap } from 'lucide-vue-next'
import { computed } from 'vue'

const data = [
  { subject: '그리디', A: 85, fullMark: 100 },
  { subject: '그래프', A: 70, fullMark: 100 },
  { subject: 'DP', A: 45, fullMark: 100 },
  { subject: '구현', A: 90, fullMark: 100 },
  { subject: '문자열', A: 75, fullMark: 100 },
  { subject: '정렬', A: 80, fullMark: 100 },
]

const radius = 120
const centerX = 200
const centerY = 200
const angleStep = (Math.PI * 2) / data.length

const getPoint = (value: number, index: number) => {
  const angle = index * angleStep - Math.PI / 2
  const x = centerX + (value / 100) * radius * Math.cos(angle)
  const y = centerY + (value / 100) * radius * Math.sin(angle)
  return `${x},${y}`
}

const radarPoints = computed(() => {
  return data.map((d, i) => getPoint(d.A, i)).join(' ')
})

const gridLevels = [25, 50, 75, 100]

const getGridPolygon = (level: number) => {
  return data.map((_, i) => getPoint(level, i)).join(' ')
}
</script>

<template>
  <div class="relative h-full w-full overflow-hidden bg-[#F8FAFC]">
    <!-- Decorative elements -->
    <div class="absolute top-20 left-20 w-64 h-64 bg-[#3396F4] opacity-5 rounded-full blur-3xl"></div>
    <div class="absolute bottom-20 right-20 w-80 h-80 bg-[#58CC02] opacity-5 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex h-full items-center px-8 py-16 max-w-7xl mx-auto">
      <div class="grid md:grid-cols-2 gap-16 w-full">
        <!-- Left side - Text content -->
        <div class="flex flex-col justify-center">
          <div
            v-motion
            :initial="{ x: -50, opacity: 0 }"
            :enter="{ x: 0, opacity: 1, transition: { duration: 800 } }"
          >
            <div 
              class="inline-flex items-center gap-2 px-4 py-2 bg-[#3396F4] text-white rounded-xl mb-8 font-bold text-sm"
              v-motion
              :initial="{ scale: 0 }"
              :enter="{ scale: 1, transition: { delay: 200, type: 'spring' } }"
            >
              <Zap class="w-4 h-4" :stroke-width="2.5" />
              Hero Feature
            </div>
            
            <h2 class="text-5xl md:text-6xl font-black text-slate-800 mb-6 leading-tight tracking-tight">
              퀵 온보딩:<br />
              <span class="text-[#3396F4]">
                ID 하나로 시작
              </span>
            </h2>
            
            <p class="text-2xl text-slate-700 mb-8 font-bold">
              당신의 'Solved.ac' ID는<br />
              당신의 역량을 알고 있습니다 ✨
            </p>

            <div class="space-y-6">
              <div
                class="flex items-start gap-4"
                v-motion
                :initial="{ x: -30, opacity: 0 }"
                :enter="{ x: 0, opacity: 1, transition: { delay: 300 } }"
              >
                <div class="flex-shrink-0 w-12 h-12 bg-[#3396F4] bg-opacity-10 rounded-2xl flex items-center justify-center">
                  <User class="w-6 h-6 text-[#3396F4]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-xl font-bold text-slate-800 mb-1">아이디 입력 즉시 스캔</h3>
                  <p class="text-slate-600 font-medium">지난 풀이 이력을 자동으로 분석</p>
                </div>
              </div>

              <div
                class="flex items-start gap-4"
                v-motion
                :initial="{ x: -30, opacity: 0 }"
                :enter="{ x: 0, opacity: 1, transition: { delay: 500 } }"
              >
                <div class="flex-shrink-0 w-12 h-12 bg-[#FFC800] bg-opacity-20 rounded-2xl flex items-center justify-center">
                  <Sparkles class="w-6 h-6 text-[#FF9600]" :stroke-width="2.5" />
                </div>
                <div>
                  <h3 class="text-xl font-bold text-slate-800 mb-1">AI 리포트 생성</h3>
                  <p class="text-slate-600 font-medium">강점 태그(예: 그리디), 약점 태그(예: DP) 자동 분류</p>
                </div>
              </div>
            </div>

            <!-- ID Input mock -->
            <div
              class="mt-10 p-6 bg-white rounded-3xl border-2 border-slate-200 shadow-sm"
              v-motion
              :initial="{ y: 30, opacity: 0 }"
              :enter="{ y: 0, opacity: 1, transition: { delay: 700 } }"
            >
              <label class="block text-sm font-bold text-slate-700 mb-3">Solved.ac ID</label>
              <div class="flex gap-3">
                <input 
                  type="text" 
                  placeholder="your_id"
                  class="flex-1 px-4 py-3 bg-slate-50 rounded-xl border-2 border-slate-200 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:border-[#3396F4] transition-colors font-medium"
                />
                <button class="px-6 py-3 bg-[#3396F4] hover:bg-[#2B7FD4] rounded-xl text-white font-bold shadow-sm hover:shadow-md transition-all">
                  분석 시작
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Right side - Radar chart -->
        <div
          class="flex items-center justify-center"
          v-motion
          :initial="{ x: 50, opacity: 0, scale: 0.9 }"
          :enter="{ x: 0, opacity: 1, scale: 1, transition: { duration: 800, delay: 300 } }"
        >
          <div class="relative w-full">
            <div class="p-8 bg-white rounded-3xl border-2 border-slate-200 shadow-sm">
              <div class="flex items-center gap-3 mb-6">
                <div class="text-3xl">📊</div>
                <h3 className="text-2xl font-bold text-slate-800">역량 분석 결과</h3>
              </div>
              
              <!-- Custom SVG Radar Chart -->
              <div class="flex justify-center">
                <svg width="400" height="400" viewBox="0 0 400 400">
                  <!-- Grid -->
                  <g v-for="level in gridLevels" :key="level">
                    <polygon :points="getGridPolygon(level)" fill="none" stroke="#E2E8F0" :stroke-width="1" />
                  </g>
                  
                  <!-- Axes -->
                  <line 
                    v-for="(_, i) in data" 
                    :key="'line'+i"
                    :x1="centerX" 
                    :y1="centerY" 
                    :x2="centerX + radius * Math.cos(i * angleStep - Math.PI/2)" 
                    :y2="centerY + radius * Math.sin(i * angleStep - Math.PI/2)" 
                    stroke="#E2E8F0" 
                    stroke-width="1" 
                  />

                  <!-- Data Area -->
                  <polygon :points="radarPoints" fill="#3396F4" fill-opacity="0.5" stroke="#3396F4" stroke-width="3" />
                  
                  <!-- Labels -->
                  <text 
                    v-for="(d, i) in data" 
                    :key="'text'+i"
                    :x="centerX + (radius + 25) * Math.cos(i * angleStep - Math.PI/2)" 
                    :y="centerY + (radius + 25) * Math.sin(i * angleStep - Math.PI/2)" 
                    text-anchor="middle" 
                    dominant-baseline="middle"
                    fill="#334155"
                    font-size="14"
                    font-weight="bold"
                  >
                    {{ d.subject }}
                  </text>
                </svg>
              </div>
              
              <div class="mt-6 grid grid-cols-2 gap-4">
                <div class="p-4 bg-[#58CC02] bg-opacity-10 rounded-2xl border-2 border-[#58CC02]">
                  <p className="text-sm text-[#58CC02] font-bold mb-1">💪 강점</p>
                  <p className="text-lg font-bold text-slate-800">구현, 그리디</p>
                </div>
                <div class="p-4 bg-[#FF4B4B] bg-opacity-10 rounded-2xl border-2 border-[#FF4B4B]">
                  <p className="text-sm text-[#FF4B4B] font-bold mb-1">📚 약점</p>
                  <p className="text-lg font-bold text-slate-800">DP</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
