<template>
  <div class="w-full h-full bg-slate-50 flex items-center justify-center overflow-hidden relative">
      <!-- 배경 효과 -->
      <div class="absolute inset-0 bg-gradient-to-b from-slate-50 to-slate-100/50 z-0"></div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-violet-500/5 rounded-full blur-[120px] animate-pulse-slow"></div>

      <div class="relative z-10 max-w-6xl mx-auto px-6 w-full flex flex-col-reverse md:flex-row items-center gap-16">
          
          <!-- 그래픽 영역 (코드 에디터) -->
          <div class="flex-1 w-full relative group perspective-1000 animate-in slide-in-from-bottom duration-700">
              <div class="relative bg-white rounded-xl border border-slate-200 shadow-2xl overflow-hidden transform transition-transform duration-500 group-hover:rotate-y-2 group-hover:rotate-x-2">
                  <!-- 에디터 헤더 -->
                  <div class="flex items-center justify-between px-4 py-3 bg-slate-50 border-b border-slate-200">
                      <div class="flex gap-2">
                          <div class="w-3 h-3 rounded-full bg-rose-400"></div>
                          <div class="w-3 h-3 rounded-full bg-amber-400"></div>
                          <div class="w-3 h-3 rounded-full bg-emerald-400"></div>
                      </div>
                      <span class="text-xs text-slate-500 font-mono">Solution.java</span>
                  </div>
                  
                  <!-- 에디터 바디 -->
                  <div class="p-6 font-mono text-sm leading-relaxed relative bg-white">
                      <div class="text-slate-300 select-none absolute left-4 top-6 flex flex-col gap-1 text-right w-6">
                        <span>1</span><span>2</span><span>3</span><span>4</span><span>5</span><span>6</span>
                      </div>
                      <div class="pl-12">
                          <p><span class="text-violet-600">public</span> <span class="text-rose-500">void</span> solve() {</p>
                          <p class="pl-4"><span class="text-slate-400">// O(N^2) Logic detected</span></p>
                          <p class="pl-4"><span class="text-violet-600">for</span> (<span class="text-amber-600">int</span> i=0; i&lt;n; i++) {</p>
                          <p class="pl-8"><span class="text-violet-600">for</span> (<span class="text-amber-600">int</span> j=0; j&lt;n; j++) {</p>
                          <p class="pl-12 text-slate-500">process(i, j);</p>
                          <p class="pl-8">}</p>
                          <p class="pl-4">}</p>
                          <p>}</p>
                      </div>

                       <!-- AI 분석 오버레이 -->
                       <div class="absolute top-16 right-8 w-64 bg-white/90 backdrop-blur-md rounded-xl border border-violet-100 p-4 shadow-xl animate-in fade-in zoom-in duration-500 delay-500">
                           <div class="flex items-center gap-2 mb-3">
                               <Sparkles class="w-4 h-4 text-violet-500" />
                               <span class="text-xs font-bold text-violet-700">AI Analysis</span>
                           </div>
                           <div class="space-y-2">
                               <div class="flex justify-between items-center text-xs">
                                   <span class="text-slate-500">Time Complexity</span>
                                   <span class="text-rose-500 font-bold">O(N²)</span>
                                </div>
                               <div class="w-full bg-slate-100 h-1 rounded-full overflow-hidden">
                                   <div class="bg-rose-500 w-[80%] h-full"></div>
                               </div>
                               <p class="text-[10px] text-slate-500 border-l-2 border-violet-400 pl-2 mt-2">
                                  이중 반복문으로 인해 입력 크기가 커질 경우 시간 초과가 발생할 수 있습니다.
                               </p>
                           </div>
                       </div>
                  </div>
              </div>

              <!-- 뒷배경 글로우 -->
              <div class="absolute -inset-1 bg-violet-400 rounded-2xl blur-2xl opacity-10 group-hover:opacity-20 transition-opacity -z-10"></div>
          </div>

          <!-- 텍스트 영역 -->
          <div class="flex-1 space-y-6 animate-in slide-in-from-right duration-700 delay-200">
              <div class="inline-flex items-center gap-2 px-3 py-1 bg-violet-100 border border-violet-200 rounded-full text-violet-600 text-xs font-bold uppercase tracking-wider">
                  <Bot class="w-4 h-4" />
                  AI Coaching
              </div>
              
              <h2 class="text-4xl md:text-5xl font-black text-slate-900 leading-tight">
                내 코드를 꿰뚫어보는<br/>
                <span class="text-transparent bg-clip-text bg-gradient-to-r from-violet-600 to-fuchsia-600">AI 코치</span>
              </h2>
              
              <p class="text-slate-600 text-lg leading-relaxed">
                  단순한 정답 여부를 넘어,<br/>
                  <span class="text-slate-900 font-bold">시간/공간 복잡도</span>부터 <span class="text-slate-900 font-bold">구현 의도</span>까지.<br/>
                  DashHub AI가 당신의 코드를 정밀하게 진단합니다.
              </p>

              <ul class="space-y-4 pt-4">
                  <li v-for="(item, idx) in features" :key="idx" class="flex items-start gap-3">
                      <div class="mt-1 w-5 h-5 rounded-full bg-violet-100 flex items-center justify-center shrink-0">
                          <Check class="w-3 h-3 text-violet-600" />
                      </div>
                      <span class="text-slate-600">{{ item }}</span>
                  </li>
              </ul>
          </div>

      </div>
  </div>
</template>

<script setup>
import { Sparkles, Bot, Check } from 'lucide-vue-next';

const features = [
  'Big-O 표기법 기반 복잡도 자동 계산',
  '코드 라인별 상세 피드백 제공',
  '알고리즘 패턴(DP, DFS 등) 자동 분류'
];
</script>

<style scoped>
.perspective-1000 {
  perspective: 1000px;
}
.rotate-y-2 {
  transform: rotateY(-2deg);
}
.rotate-x-2 {
  transform: rotateX(2deg);
}
.animate-spin-slow {
    animation: spin 3s linear infinite;
}
.animate-pulse-slow {
   animation: pulse 8s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style>
