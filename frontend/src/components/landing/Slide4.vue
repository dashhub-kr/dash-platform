<template>
  <div class="w-full h-full bg-slate-50 flex items-center justify-center overflow-hidden relative">
      <!-- 배경 장식 (라이트 테마 유지) -->
      <div class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px] pointer-events-none"></div>
      <div class="absolute top-0 left-0 w-full h-32 bg-gradient-to-b from-white to-transparent z-10"></div>
      <div class="absolute bottom-0 left-0 w-full h-32 bg-gradient-to-t from-slate-50 to-transparent z-10"></div>

      <div class="relative z-10 max-w-7xl mx-auto px-6 w-full flex flex-col md:flex-row items-center gap-16">
          
          <!-- 텍스트 영역 (왼쪽) -->
          <div class="flex-1 space-y-8 animate-in slide-in-from-left duration-700">
              <div class="inline-flex items-center gap-2 px-3 py-1 bg-blue-50 border border-blue-100 rounded-full text-blue-600 text-xs font-bold uppercase tracking-wider">
                  <Database class="w-4 h-4" />
                  Data Integration
              </div>

              <h2 class="text-4xl md:text-5xl font-black text-slate-900 leading-tight">
                아이디 입력 하나로<br/>
                <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">모든 분석이 끝납니다.</span>
              </h2>
              
              <p class="text-lg text-slate-600 leading-relaxed">
                  Solved.ac 계정만 입력하세요.<br/>
                  <span class="text-slate-900 font-bold">취약 유형 분석</span>부터 <span class="text-slate-900 font-bold">맞춤형 실전 모의고사</span>까지.<br/>
                  DashHub가 당신에게 딱 맞는 성장의 길을 제시합니다.
              </p>

              <!-- 입력 필드 데모 -->
              <div class="relative max-w-md p-1 bg-gradient-to-r from-slate-200 to-slate-100 rounded-2xl shadow-lg transform -rotate-1">
                <div class="bg-white rounded-xl p-2 flex items-center gap-3">
                    <div class="pl-4 text-slate-400 font-mono text-sm">Solved.ac/profile/</div>
                    <div class="flex-1 font-bold text-slate-800">funczun</div>
                    <button class="px-4 py-2 bg-blue-600 text-white text-sm font-bold rounded-lg shadow-md shadow-blue-500/30">
                        분석하기
                    </button>
                </div>
              </div>

          </div>

          <!-- 그래픽 영역 (오른쪽 - 대시보드 & 코딩환경) -->
          <div class="flex-1 w-full relative h-[500px] perspective-1000">
                <!-- 카드 1: 분석 리포트 -->
                <div class="absolute top-10 left-0 w-3/4 bg-white rounded-2xl shadow-xl border border-slate-100 p-6 z-20 animate-in slide-in-from-right duration-700 delay-100 transform rotate-y-n10 hover:rotate-y-0 transition-transform">
                    <div class="flex items-center justify-between mb-4">
                        <h4 class="font-bold text-slate-800 flex items-center gap-2">
                            <PieChart class="w-5 h-5 text-blue-500" />
                            취약 유형 분석
                        </h4>
                        <span class="text-xs text-slate-400">전체 상위 5%</span>
                    </div>
                    
                    <!-- 레이더 차트 (실제 컴포넌트) -->
                    <div class="relative w-full aspect-video bg-slate-50 rounded-xl overflow-hidden flex items-center justify-center p-2">
                        <AlgorithmRadarChart :stats="mockStats" :maxTags="6" />
                    </div>

                    <!-- 팝업 뱃지 (이동됨) -->
                    <div class="absolute -top-3 -right-3 bg-emerald-500 text-white text-xs font-bold px-3 py-1 rounded-full shadow-lg animate-bounce delay-700 z-30">
                        분석 완료!
                    </div>
                </div>

                <!-- 카드 2: 실전 코딩 환경 -->
                <div class="absolute bottom-10 right-0 w-3/4 bg-slate-900 rounded-2xl shadow-2xl border border-slate-800 p-4 z-30 animate-in slide-in-from-bottom duration-700 delay-300 transform translate-x-4">
                    <!-- 탭 -->
                    <div class="flex items-center gap-4 mb-3 border-b border-slate-700 pb-2">
                        <div class="flex gap-1.5">
                            <div class="w-3 h-3 rounded-full bg-red-500"></div>
                            <div class="w-3 h-3 rounded-full bg-yellow-500"></div>
                        </div>
                        <span class="text-xs text-slate-400 font-mono">Mock_Test_Env.js</span>
                    </div>
                    <!-- 코드 -->
                    <div class="space-y-2 font-mono text-xs">
                        <div class="flex gap-2">
                             <span class="text-slate-600">1</span>
                             <span class="text-purple-400">function</span> <span class="text-blue-400">solution</span><span class="text-slate-300">(input) {</span>
                        </div>
                        <div class="flex gap-2">
                             <span class="text-slate-600">2</span>
                             <span class="text-slate-500 pl-4">// 실전과 동일한 환경 제공</span>
                        </div>
                        <div class="flex gap-2">
                             <span class="text-slate-600">3</span>
                             <span class="text-slate-300 pl-4">const result = input.map(...)</span>
                        </div>
                        <div class="flex gap-2">
                             <span class="text-slate-600">4</span>
                             <span class="text-purple-400 pl-4">return</span> <span class="text-slate-300">result;</span>
                        </div>
                        <div class="flex gap-2">
                             <span class="text-slate-600">5</span>
                             <span class="text-slate-300">}</span>
                        </div>
                    </div>
                    <div class="mt-4 flex justify-between items-center">
                         <span class="text-xs text-green-400">● Test Passing</span>
                         <button class="px-3 py-1 bg-blue-600 text-white text-xs rounded hover:bg-blue-500">Submit</button>
                    </div>
                </div>

                <!-- 연결선 -->
                <div class="absolute top-1/2 left-1/2 w-32 h-32 border-t-2 border-r-2 border-dashed border-blue-300 rounded-tr-3xl -z-10 animate-pulse"></div>
          </div>

      </div>
  </div>
</template>

<script setup>
import { Database, PieChart } from 'lucide-vue-next';
import AlgorithmRadarChart from '@/components/charts/AlgorithmRadarChart.vue';

const mockStats = [
    { tagKey: 'dp', solved: 40 },
    { tagKey: 'graphs', solved: 20 }, // 취약하게 설정 (상대적)
    { tagKey: 'math', solved: 85 },
    { tagKey: 'implementation', solved: 90 },
    { tagKey: 'data_structures', solved: 75 },
    { tagKey: 'sorting', solved: 60 }
];
</script>

<style scoped>
.perspective-1000 {
  perspective: 1000px;
}
.rotate-y-n10 {
  transform: rotateY(-10deg);
}
</style>
