<template>
  <div
    class="min-h-screen bg-slate-50 text-slate-800 flex flex-col selection:bg-indigo-500/30 font-[Pretendard]"
  >
    <!-- 배경: Light Mode Gradients -->
    <div class="fixed inset-0 z-0 pointer-events-none overflow-hidden">
      <!-- Top Left Gradient -->
      <div
        class="absolute top-[-10%] left-[-10%] w-[50vw] h-[50vw] bg-indigo-200/40 rounded-full blur-[120px] mix-blend-multiply opacity-70 animate-blob"
      ></div>
      <!-- Bottom Right Gradient -->
      <div
        class="absolute bottom-[-10%] right-[-10%] w-[50vw] h-[50vw] bg-purple-200/40 rounded-full blur-[120px] mix-blend-multiply opacity-70 animate-blob animation-delay-2000"
      ></div>
      <!-- Center Accent -->
      <div
        class="absolute top-[30%] left-[30%] w-[40vw] h-[40vw] bg-pink-200/40 rounded-full blur-[120px] mix-blend-multiply opacity-70 animate-blob animation-delay-4000"
      ></div>
      
      <!-- Grid Pattern -->
      <div
        class="absolute inset-0 bg-[linear-gradient(to_right,#8080800a_1px,transparent_1px),linear-gradient(to_bottom,#8080800a_1px,transparent_1px)] bg-[size:24px_24px]"
      ></div>
    </div>

    <!-- 내용 -->
    <main class="relative z-10 flex-1">
      <template v-if="authChecked && !user">
        <section
          id="hero"
          class="container mx-auto px-6 pt-32 pb-24 text-center relative max-w-5xl"
        >
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 border border-indigo-100 text-indigo-600 text-sm font-bold mb-6 animate-fade-in-up">
             <span class="relative flex h-2 w-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-indigo-500"></span>
              </span>
             GitHub 연동으로 3초 만에 시작
          </div>

          <h1
            class="text-6xl md:text-7xl font-black tracking-tight mb-8 text-slate-900 leading-tight animate-fade-in-up delay-100"
          >
            알고리즘 스터디, <br />
            <span
              class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600"
            >
              '제출'만 하세요.
            </span>
          </h1>
          
          <p
            class="text-xl md:text-2xl text-slate-500 max-w-2xl mx-auto mb-12 text-pretty font-medium animate-fade-in-up delay-200"
          >
            번거로운 파일 관리, 리팩토링, 코드 리뷰...<br/>
            이제 <span class="text-indigo-600 font-bold bg-indigo-50 px-1 rounded">Dash</span>가 전부 자동으로 처리해드립니다.
          </p>

          <div
            class="flex flex-col sm:flex-row gap-5 justify-center items-center animate-fade-in-up delay-300"
          >
            <button
              @click="handleLogin"
              class="group relative inline-flex h-14 items-center justify-center overflow-hidden rounded-2xl bg-slate-900 px-10 font-bold text-white transition-all duration-300 hover:bg-slate-800 hover:shadow-xl hover:shadow-indigo-500/20 hover:-translate-y-1"
            >
              <span class="mr-3"><Github :size="24" /></span>
              <span class="text-lg">GitHub로 시작하기</span>
              <div
                class="absolute inset-0 flex h-full w-full justify-center [transform:skew(-12deg)_translateX(-100%)] group-hover:duration-1000 group-hover:[transform:skew(-12deg)_translateX(100%)]"
              >
                <div class="relative h-full w-8 bg-white/20"></div>
              </div>
            </button>
             <button
              @click="scrollToSection('features')"
              class="inline-flex h-14 items-center justify-center rounded-2xl bg-white px-8 font-bold text-slate-600 border border-slate-200 hover:bg-slate-50 hover:border-slate-300 transition-all duration-300"
            >
              기능 둘러보기
            </button>
          </div>
        </section>

        <section
          id="features"
          class="container mx-auto px-6 py-24 relative max-w-7xl"
        >
          <div class="text-center mb-20 max-w-3xl mx-auto">
            <h2 class="text-4xl font-extrabold mb-6 text-slate-900">
              Dash가 해결하는 <span class="underline decoration-wavy decoration-indigo-300 underline-offset-4">3가지 문제</span>
            </h2>
            <p class="text-xl text-slate-500">
               스터디원들의 '귀차니즘'을 기술적으로 해결했습니다.
            </p>
          </div>

          <div class="flex flex-col gap-12">
            <!-- Feature 1: Uploader -->
            <div
              class="group reveal bg-white rounded-[40px] p-8 md:p-12 shadow-sm border border-slate-100 hover:shadow-2xl transition-all duration-500 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center"
            >
               <div class="order-2 lg:order-1">
                  <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-50 text-red-600 font-bold text-sm mb-6">
                    <UploadCloud :size="16" />
                    Uploader
                  </div>
                  <h3 class="text-3xl font-bold text-slate-900 mb-4 leading-tight">
                    "문제 풀기도 바쁜데,<br/> 패키지 관리는 너무 귀찮아요."
                  </h3>
                  <p class="text-lg text-slate-500 mb-8 leading-relaxed">
                    그냥 백준에서 <code class="bg-slate-100 px-1.5 py-0.5 rounded text-red-500 font-bold">Main</code>으로 풀고 제출하세요.
                    Dash가 자동으로 <span class="text-slate-900 font-bold underline decoration-indigo-200">파일 구조를 변환</span>하고,
                    실패한 기록까지 잔디로 심어드립니다.
                  </p>
               </div>
               <div class="order-1 lg:order-2 relative group-hover:scale-[1.02] transition-transform duration-500">
                  <div class="absolute -inset-4 bg-gradient-to-r from-slate-100 to-slate-50 rounded-[40px] -z-10 transform rotate-2"></div>
                  <div class="bg-white rounded-3xl shadow-xl border border-slate-200 overflow-hidden relative">
                     <!-- Fake Browser Header -->
                     <div class="h-10 bg-slate-50 border-b border-slate-100 flex items-center px-4 gap-2">
                        <div class="flex gap-1.5">
                           <div class="w-3 h-3 rounded-full bg-red-400"></div>
                           <div class="w-3 h-3 rounded-full bg-amber-400"></div>
                           <div class="w-3 h-3 rounded-full bg-green-400"></div>
                        </div>
                        <div class="flex-1 text-center text-xs font-mono text-slate-400">Main.java - IntelliJ IDEA</div>
                     </div>
                     <div class="p-6 font-mono text-sm bg-slate-900 text-slate-300">
                        <p><span class="text-amber-400">public class</span> <span class="text-white">Main</span> {</p>
                        <p class="pl-4"><span class="text-amber-400">public static void</span> <span class="text-blue-300">main</span>(String[] args) {</p>
                        <p class="pl-8 text-slate-500">// 제발 이번엔 맞자...</p>
                        <p class="pl-8"><span class="text-white">solve();</span></p>
                        <p class="pl-4">}</p>
                        <p>}</p>
                     </div>
                     <!-- Transform Arrow -->
                     <div class="absolute inset-0 flex items-center justify-center bg-slate-900/10 backdrop-blur-[1px] opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                        <div class="bg-white p-4 rounded-full shadow-lg transform translate-y-4 group-hover:translate-y-0 transition-all duration-300">
                           <ArrowDown :size="32" class="text-indigo-600" />
                        </div>
                     </div>
                  </div>
               </div>
            </div>

            <!-- Feature 2: Reviewer -->
            <div
              class="group reveal bg-white rounded-[40px] p-8 md:p-12 shadow-sm border border-slate-100 hover:shadow-2xl transition-all duration-500 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center"
            >
               <div class="relative group-hover:scale-[1.02] transition-transform duration-500">
                  <div class="absolute -inset-4 bg-gradient-to-l from-indigo-50 to-purple-50 rounded-[40px] -z-10 transform -rotate-2"></div>
                   <div class="bg-white rounded-3xl shadow-xl border border-slate-200 p-6 relative overflow-hidden">
                       <div class="absolute top-0 left-0 bg-green-500 text-white text-[10px] font-bold px-3 py-1 rounded-br-xl">Dash AI Analysis</div>
                       <div class="flex gap-4 mt-4">
                           <div class="w-12 h-12 rounded-2xl bg-green-100 flex items-center justify-center shrink-0">
                               <Bot :size="24" class="text-green-600" />
                           </div>
                           <div class="space-y-2">
                               <div class="h-4 bg-slate-100 rounded w-48"></div>
                               <div class="h-3 bg-slate-50 rounded w-full"></div>
                               <div class="h-3 bg-slate-50 rounded w-5/6"></div>
                           </div>
                       </div>
                       <div class="mt-6 bg-slate-50 rounded-xl p-4 border border-slate-100">
                           <p class="text-sm text-slate-600 leading-relaxed font-medium">
                               "이 풀이는 <span class="bg-green-100 text-green-700 font-bold px-1 rounded">DP(동적 계획법)</span>를 사용하여 O(N) 시간 복잡도로 <br/>
                               효율적으로 해결했습니다. 메모이제이션 활용이 훌륭하네요! 👍"
                           </p>
                       </div>
                   </div>
               </div>
               <div>
                  <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-green-50 text-green-600 font-bold text-sm mb-6">
                    <Bot :size="16" />
                    AI Reviewer
                  </div>
                  <h3 class="text-3xl font-bold text-slate-900 mb-4 leading-tight">
                    "주석 없는 코드는<br/> 읽기 힘들어요."
                  </h3>
                  <p class="text-lg text-slate-500 mb-8 leading-relaxed">
                    속 시원하게, <span class="text-slate-900 font-bold underline decoration-green-300">AI가 대신 읽어드립니다.</span><br/>
                    알고리즘 분류, 시간 복잡도, 풀이 핵심을 3초 만에 요약해서 보여줍니다.
                    코드 리뷰 시간을 획기적으로 줄여보세요.
                  </p>
               </div>
            </div>

            <!-- Feature 3: Captain -->
             <div
              class="group reveal bg-white rounded-[40px] p-8 md:p-12 shadow-sm border border-slate-100 hover:shadow-2xl transition-all duration-500 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center"
            >
               <div class="order-2 lg:order-1">
                  <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-50 text-blue-600 font-bold text-sm mb-6">
                    <ClipboardList :size="16" />
                    Study Lead
                  </div>
                  <h3 class="text-3xl font-bold text-slate-900 mb-4 leading-tight">
                    "매주 문제 고르는 게<br/> 제일 큰 일이에요."
                  </h3>
                  <p class="text-lg text-slate-500 mb-8 leading-relaxed">
                    팀원들의 Solved.ac 티어와 취약 유형을 분석해,<br/>
                    이번 주에 풀어야 할 <span class="text-slate-900 font-bold underline decoration-blue-300">최적의 문제를 추천</span>합니다.
                    이제 스터디장은 결정만 하세요.
                  </p>
               </div>
               <div class="order-1 lg:order-2 relative group-hover:scale-[1.02] transition-transform duration-500">
                  <div class="absolute -inset-4 bg-gradient-to-tr from-blue-50 to-cyan-50 rounded-[40px] -z-10 transform rotate-1"></div>
                  <div class="bg-white rounded-3xl shadow-xl border border-slate-200 p-6 flex flex-col gap-4">
                      <div class="flex justify-between items-center border-b border-slate-100 pb-4">
                          <span class="font-bold text-slate-800">Weekly Recommendation</span>
                          <span class="text-xs bg-red-100 text-red-500 font-bold px-2 py-1 rounded-full">취약점 보완</span>
                      </div>
                      <div class="flex items-center gap-4 p-3 bg-slate-50 rounded-2xl border border-slate-100">
                          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-400 to-amber-600 flex items-center justify-center text-white font-bold shadow-lg shadow-amber-500/30">G4</div>
                          <div class="flex-1">
                              <h4 class="font-bold text-slate-800">평범한 배낭</h4>
                              <p class="text-xs text-slate-400">#DP #Knapsack</p>
                          </div>
                      </div>
                      <div class="flex items-center gap-4 p-3 bg-slate-50 rounded-2xl border border-slate-100 opacity-60">
                           <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-slate-300 to-slate-400 flex items-center justify-center text-white font-bold">S1</div>
                           <div class="flex-1">
                               <h4 class="font-bold text-slate-800">정수 삼각형</h4>
                               <p class="text-xs text-slate-400">#DP</p>
                           </div>
                      </div>
                  </div>
               </div>
            </div>

          </div>
        </section>

        <!-- Bento Grid Features Overview -->
        <section
          id="core"
          class="container mx-auto px-6 py-24"
        >
          <div class="text-center mb-16">
            <h2 class="text-3xl font-bold mb-4 text-slate-900">그 외에도 강력한 기능들</h2>
            <p class="text-lg text-slate-500">
              코딩에만 집중하세요. 나머지는 Dash가 알아서 합니다.
            </p>
          </div>

          <div
            class="grid md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto"
          >
            <div
              v-for="(feature, index) in detailedFeatures"
              :key="feature.title"
              class="group relative p-8 rounded-3xl bg-white border border-slate-200 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
            >
              <!-- Icon -->
              <div
                class="w-12 h-12 rounded-2xl bg-indigo-50 flex items-center justify-center mb-6 group-hover:bg-indigo-600 transition-colors duration-300"
              >
                 <component :is="feature.icon" :size="24" class="text-indigo-600 group-hover:text-white transition-colors duration-300" />
              </div>
              
              <h3 class="text-xl font-bold mb-3 text-slate-800">
                {{ feature.title }}
              </h3>
              <p class="text-slate-500 text-sm leading-relaxed">
                {{ feature.description }}
              </p>
            </div>
          </div>
        </section>

        <!-- CTA -->
        <section class="container mx-auto px-6 py-24 pb-32">
          <div
            class="relative rounded-[48px] bg-slate-900 overflow-hidden text-center p-12 md:p-24 shadow-2xl"
          >
            <!-- Fancy Background elements within CTA -->
            <div class="absolute top-0 right-0 w-64 h-64 bg-indigo-500 rounded-full blur-[80px] opacity-30"></div>
            <div class="absolute bottom-0 left-0 w-64 h-64 bg-purple-500 rounded-full blur-[80px] opacity-30"></div>
            
            <div class="relative z-10 max-w-3xl mx-auto">
              <h2 class="text-4xl md:text-5xl font-black mb-8 text-white leading-tight">
                준비되셨나요? <br/>
                <span class="text-indigo-400">Dash</span>로 멋진 항해를 시작하세요.
              </h2>
              <p class="text-slate-400 text-lg mb-10">
                 지금 바로 무료로 시작해보세요. 팀원 초대는 언제든지 가능합니다.
              </p>

              <button
                @click="handleLogin"
                class="inline-flex items-center justify-center gap-2 px-10 py-5 rounded-2xl bg-white text-slate-900 hover:bg-indigo-50 transition-all font-bold text-lg shadow-lg hover:shadow-white/20 hover:scale-105"
              >
                <Github :size="20" />
                지금 Dash 시작하기
              </button>
            </div>
          </div>
        </section>
      </template>
    </main>
    
    <footer class="border-t border-slate-200 py-12 bg-white">
      <div class="container mx-auto px-6 text-center">
        <div class="text-2xl font-black text-slate-900 mb-6">Dash</div>
        <div class="text-sm text-slate-400">
          © 2025 DASH. All rights reserved. <br/>
          Designed for developers, by developers.
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import {
  ref,
  computed,
  onMounted,
  onBeforeUnmount,
  watch,
  nextTick,
} from "vue";
import {
  Code2,
  Github,
  UploadCloud,
  Bot,
  ClipboardList,
  ArrowRight,
  ArrowDown,
  Search,
  PieChart,
  FileCode,
  FolderTree,
  GitCommit,
} from "lucide-vue-next";

const detailedFeatures = ref([
  {
    icon: UploadCloud,
    title: "완전 자동화",
    description:
      "백준 문제 제출 시 성공/실패 여부 상관없이 코드를 자동으로 GitHub에 커밋하고 Dash 대시보드를 갱신합니다.",
  },
  {
    icon: Bot,
    title: "AI 자동 요약",
    description:
      "제출된 코드를 Dash AI가 분석하여 풀이의 핵심 요약, 시간/공간 복잡도, 사용 알고리즘 태그를 자동으로 생성합니다.",
  },
  {
    icon: FolderTree,
    title: "깔끔한 코드 관리",
    description:
      "IDE에서 Main으로 풀어도 괜찮습니다. 자동으로 패키지와 파일명을 스터디 규칙에 맞게 변환하여 저장합니다.",
  },
  {
    icon: PieChart,
    title: "데이터 기반 목표",
    description:
      "Solved.ac 연동 데이터를 기반으로 스터디원들의 취약한 유형을 분석하고, 효과적인 공통 학습 목표를 추천합니다.",
  },
  {
    icon: Search,
    title: "통합 검색",
    description:
      "스터디원들이 제출한 모든 풀이와 AI가 요약한 내용을 키워드(문제 번호, 태그, 풀이 내용)로 즉시 검색할 수 있습니다.",
  },
  {
    icon: GitCommit,
    title: "노력의 기록",
    description:
      "실패한 제출도 사라지지 않습니다. 당신의 고민과 시도를 잔디(Contribution)와 대시보드에 시각적으로 남겨드립니다.",
  },
]);

import { useAuth } from "../composables/useAuth";
import { authApi } from "../api/auth";

const { user, authChecked } = useAuth();

let revealObserver = null;
function initReveal() {
  const els = document.querySelectorAll(".reveal");
  if (!els || els.length === 0) return;

  if (revealObserver) return;

  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("reveal-visible");
          revealObserver.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.1 }
  );

  els.forEach((el, i) => {
    el.style.transitionDelay = `${i * 100}ms`;
    revealObserver.observe(el);
  });
}

watch(
  authChecked,
  (val) => {
    if (val) {
      if (user.value) {
        window.location.href = "/dashboard";
      } else {
        nextTick(() => initReveal());
      }
    }
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  if (revealObserver) {
    revealObserver.disconnect();
    revealObserver = null;
  }
});

const scrollToSection = (id) => {
  const el = document.getElementById(id);
  if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
};

const handleLogin = () => {
  window.location.href = `/oauth2/authorization/github`;
};
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard/dist/web/static/pretendard.css');

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}

@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
}

.delay-100 { animation-delay: 0.1s; }
.delay-200 { animation-delay: 0.2s; }
.delay-300 { animation-delay: 0.3s; }

.reveal {
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 1s cubic-bezier(0.16, 1, 0.3, 1), transform 1s cubic-bezier(0.16, 1, 0.3, 1);
}

.reveal-visible {
  opacity: 1;
  transform: translateY(0);
}
</style>
