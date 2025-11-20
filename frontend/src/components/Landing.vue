
<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-indigo-500/30">

    <div class="fixed inset-0 z-0 pointer-events-none">
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-[1000px] h-[600px] bg-indigo-600/20 rounded-full blur-[120px] opacity-50 mix-blend-screen"></div>
      <div class="absolute bottom-0 right-0 w-[800px] h-[600px] bg-blue-600/10 rounded-full blur-[100px] opacity-30"></div>
      <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
      <div class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:24px_24px]"></div>
    </div>

    <header class="border-b border-white/10 bg-slate-950/50 backdrop-blur-md sticky top-0 z-50">
      <div class="container mx-auto px-6 h-16 flex items-center justify-between relative">
        <div class="flex items-center gap-4 flex-shrink-0">
          <div class="flex items-center gap-2 group cursor-pointer flex-shrink-0" @click="goHome">
          <div class="p-1.5 rounded-lg bg-indigo-500/10 group-hover:bg-indigo-500/20 transition-colors">
            <Code2 :size="24" class="text-indigo-400" />
          </div>
          <span class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">DASH</span>
          </div>

          <!-- 고정 너비(nav)로 중앙에 배치: 좌/우 컨텐츠 변화에 영향받지 않음 -->
          <nav class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 hidden sm:flex w-[420px] justify-center items-center gap-6" :class="user ? 'opacity-0 pointer-events-none' : ''">
            <button @click.prevent="scrollToSection('hero')" class="text-sm text-slate-300 hover:text-white transition text-center">홈</button>
            <button @click.prevent="scrollToSection('features')" class="text-sm text-slate-300 hover:text-white transition text-center">주요 기능</button>
            <button @click.prevent="scrollToSection('core')" class="text-sm text-slate-300 hover:text-white transition text-center">핵심 기능</button>
          </nav>

        </div>

        <div class="flex items-center gap-4 flex-shrink-0 min-w-[240px] justify-end">
          <template v-if="user">
            <div class="flex items-center gap-3 min-w-0 relative" ref="profileRef">
              <div class="flex items-center gap-3 min-w-0">
                <div class="w-9 h-9 rounded-full bg-indigo-600/20 flex items-center justify-center text-indigo-300 font-semibold">{{ userInitial }}</div>
                <div class="flex flex-col leading-tight min-w-0">
                  <span class="text-xs text-slate-300">반갑습니다,</span>
                  <span class="text-sm text-white font-semibold max-w-[140px] sm:max-w-[180px] block truncate">{{ user.name || user.email }}</span>
                </div>
              </div>

              <!-- 삼각형(chevron) 버튼: 클릭으로 메뉴 토글 -->
              <button @click.stop="toggleProfileMenu" :aria-expanded="profileMenuOpen" class="ml-2 p-1 rounded hover:bg-white/5 transition-colors" aria-label="계정 메뉴">
                <ChevronDown :size="16" class="text-slate-300" />
              </button>

              <!-- 드롭다운: 헤더 내부에 절대 포지션으로 렌더 (메인 콘텐츠를 밀지 않음) -->
              <transition name="slide-down">
                <div v-if="profileMenuOpen" class="absolute right-0 top-full mt-2 w-40 bg-slate-900/95 border border-white/10 rounded-lg py-1 shadow-lg z-50">
                  <button @click="goToProfile" class="w-full text-left px-3 py-2 text-sm hover:bg-white/5">마이페이지</button>
                  <button @click="handleLogout" class="w-full text-left px-3 py-2 text-sm text-rose-400 hover:bg-white/5">로그아웃</button>
                </div>
              </transition>
            </div>
          </template>
          
          <template v-else>
            <button 
              @click="handleLogin"
              class="px-4 py-2 rounded-full text-sm font-medium bg-white/5 hover:bg-white/10 border border-white/10 transition-all hover:scale-105 active:scale-95"
            >
              로그인
            </button>
          </template>
        </div>
      </div>
    </header>

    <!-- (대체) 드롭다운은 헤더 내부에 절대 포지션으로 렌더되어 메인 컨텐츠를 밀지 않습니다 -->

    <main class="relative z-10 flex-1">
      <template v-if="!user">
      
      <section id="hero" class="container mx-auto px-6 pt-20 pb-16 text-center relative">
        <h1 class="text-5xl md:text-7xl font-bold tracking-tight mb-6 text-balance animate-fade-in-up">
          알고리즘 스터디, <br />
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 via-blue-400 to-cyan-400">
            '백준 제출'만 하세요.
          </span>
        </h1>
        <p class="text-lg md:text-xl text-slate-400 max-w-2xl mx-auto mb-10 text-pretty animate-fade-in-up" style="animation-delay: 0.2s">
          공통 목표 선정, 설명을 위한 주석, 형식에 맞는 업로드...<br class="hidden md:block" />
          당신의 <span class="text-indigo-400 font-semibold">실패한 코드</span>조차 노력의 증거로 남겨드립니다.
        </p>
        
          <div class="flex flex-col sm:flex-row gap-4 justify-center items-center animate-fade-in-up" style="animation-delay: 0.3s">
          <button
            @click="handleLogin"
            class="group relative inline-flex h-12 items-center justify-center overflow-hidden rounded-full bg-indigo-600 px-8 font-medium text-white transition-all duration-300 hover:bg-indigo-700 hover:ring-4 hover:ring-indigo-500/30 hover:scale-105"
          >
            <span class="mr-2"><Github :size="20" /></span>
            GitHub로 3초 만에 시작하기
            <div class="absolute inset-0 flex h-full w-full justify-center [transform:skew(-12deg)_translateX(-100%)] group-hover:duration-1000 group-hover:[transform:skew(-12deg)_translateX(100%)]">
              <div class="relative h-full w-8 bg-white/20"></div>
            </div>
          </button>
        </div>
      </section>

      <section id="features" class="container mx-auto px-6 py-20 relative max-w-7xl">
        <div class="text-center mb-16 max-w-3xl mx-auto">
          <h2 class="text-4xl font-bold mb-4 text-white">Dash가 해결하는 3가지 문제</h2>
          <p class="text-lg text-slate-400">스터디원들의 고질적인 페인 포인트를 시원하게 해결했습니다.</p>
        </div>

        <div class="flex flex-col gap-12">

          <div class="bg-white/[0.03] border border-white/10 rounded-3xl p-8 md:p-10 flex flex-col gap-10">
            <div class="text-center md:text-left flex flex-col md:flex-row gap-6 items-center md:items-start border-b border-white/5 pb-8">
              <div class="p-4 rounded-2xl bg-red-500/10 text-red-400 flex-shrink-0">
                <UploadCloud :size="40" />
              </div>
              <div class="flex-1">
                <h3 class="text-2xl font-bold text-white mb-2">Uploader (업로더)</h3>
                <p class="text-xl font-medium text-slate-300 mb-2">
                  "문제 풀기도 벅찬데, 패키지 만들고 파일명 바꾸고... 너무 귀찮아요."
                </p>
                <p class="text-slate-400">
                  그냥 <span class="text-red-400 font-bold">백준에서 'Main'으로 풀고 제출하세요!<br/></span>  
                  <span class="text-white font-bold underline decoration-indigo-500/50">파일 구조를 자동 변환</span>하고, 
                  <span class="text-white font-bold underline decoration-red-500/50">실패한 기록</span>까지 잔디로 남겨드립니다.
                </p>
              </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div class="bg-slate-900/50 rounded-2xl border border-white/10 p-6 relative overflow-hidden">
                    <div class="absolute top-0 right-0 bg-indigo-500/20 text-indigo-300 text-[10px] px-2 py-1 rounded-bl-lg font-bold border-l border-b border-indigo-500/20">
                        Auto Refactoring
                    </div>
                    <div class="flex flex-col gap-4">
                        <div class="flex items-center gap-3 opacity-60">
                            <FileCode :size="24" class="text-slate-400" />
                            <div class="flex-1">
                                <p class="text-xs text-slate-500 mb-0.5">My Local IDE</p>
                                <p class="text-sm text-slate-300 font-mono bg-slate-800 px-2 py-1 rounded w-fit">class Main { ... }</p>
                            </div>
                        </div>
                        <div class="flex justify-center relative h-8">
                            <div class="absolute left-1/2 -translate-x-1/2 h-full w-0.5 bg-gradient-to-b from-slate-700 to-indigo-500"></div>
                            <ArrowDown :size="20" class="text-indigo-400 absolute bottom-0 bg-slate-900 rounded-full" />
                        </div>
                        <div class="flex items-center gap-3">
                            <FolderTree :size="24" class="text-indigo-400" />
                            <div class="flex-1">
                                <p class="text-xs text-indigo-300 mb-0.5">GitHub Repository (Auto)</p>
                                <div class="bg-indigo-950/30 border border-indigo-500/30 rounded p-2 font-mono text-xs space-y-1">
                                    <p class="text-slate-400">📂 src/ssafy/week04/</p>
                                    <p class="text-green-300 pl-4">📄 BOJ_1000.java</p>
                                    <p class="text-slate-500 pl-4 text-[10px]">// package com.ssafy...</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="bg-slate-900/50 rounded-2xl border border-white/10 p-6 relative overflow-hidden">
                    <div class="absolute top-0 right-0 bg-red-500/20 text-red-300 text-[10px] px-2 py-1 rounded-bl-lg font-bold border-l border-b border-red-500/20">
                        Proof of Effort
                    </div>
                    <div class="flex flex-col justify-between h-full gap-4">
                          <div class="flex justify-between items-center">
                             <div class="flex items-center gap-2">
                                <div class="w-2 h-2 rounded-full bg-red-500 animate-pulse"></div>
                                <span class="text-sm text-red-300 font-bold">틀렸습니다 (시간 초과)</span>
                             </div>
                             <span class="text-xs text-slate-500">오늘, 3시간 소요</span>
                          </div>
                          
                          <div class="bg-slate-800/50 rounded-lg p-3 flex items-center gap-3 border border-white/5">
                             <div class="bg-green-500/20 p-1.5 rounded-full">
                                 <GitCommit :size="16" class="text-green-400" />
                             </div>
                             <div class="flex-1">
                                 <p class="text-xs text-slate-300 font-mono">feat: solve BOJ_2580 (Attempt 1)</p>
                                 <p class="text-[10px] text-slate-500">Dash Bot committed 1 min ago</p>
                             </div>
                          </div>

                          <div>
                             <p class="text-[10px] text-slate-400 mb-1">Contribution Graph</p>
                             <div class="flex gap-1">
                                 <div class="w-4 h-4 rounded bg-slate-800"></div>
                                 <div class="w-4 h-4 rounded bg-green-700"></div>
                                 <div class="w-4 h-4 rounded bg-yellow-600 border border-yellow-500/50 shadow-[0_0_10px_rgba(234,179,8,0.3)]" title="실패했지만 기록됨!"></div>
                                 <div class="w-4 h-4 rounded bg-slate-800"></div>
                                 <div class="w-4 h-4 rounded bg-slate-800"></div>
                             </div>
                          </div>
                    </div>
                </div>

            </div>
          </div>


          <div class="bg-white/[0.03] border border-white/10 rounded-3xl p-8 md:p-10 flex flex-col gap-10">
            <div class="text-center md:text-left flex flex-col md:flex-row gap-6 items-center md:items-start border-b border-white/5 pb-8">
              <div class="p-4 rounded-2xl bg-green-500/10 text-green-400 flex-shrink-0">
                <Bot :size="40" />
              </div>
              <div>
                <h3 class="text-2xl font-bold text-white mb-2">Reviewer (리뷰어)</h3>
                <p class="text-xl font-medium text-slate-300 mb-2">"주석 없는 코드를 이해하느라 시간을 다 써요."</p>
                <p class="text-slate-400">
                  속 시원하게, <span class="text-green-400 font-bold">AI가 달아줄게요!<br/></span> 
                  코드를 읽지 않아도 풀이의 핵심을 3초 만에 파악하세요.
                </p>
              </div>
            </div>

            <div class="w-full grid grid-cols-1 md:grid-cols-[1fr_auto_1fr] gap-6 items-center">
                <div class="bg-slate-900 rounded-xl p-6 border border-white/10 relative min-h-[160px] flex flex-col justify-center">
                    <div class="absolute -top-3 left-4 bg-slate-800 px-2 py-0.5 text-xs text-slate-400 rounded">Before</div>
                    <div class="space-y-2 opacity-40 blur-[1px]">
                        <div class="h-2 bg-slate-600 rounded w-3/4"></div>
                        <div class="h-2 bg-slate-600 rounded w-full"></div>
                        <div class="h-2 bg-slate-600 rounded w-1/2"></div>
                    </div>
                    <div class="mt-4 text-center text-red-400 text-sm font-medium flex justify-center items-center gap-2">
                        <Search :size="16" /> 분석에 10분 소요
                    </div>
                </div>

                <ArrowRight :size="32" class="text-slate-600 mx-auto rotate-90 md:rotate-0" />

                <div class="bg-green-950/20 rounded-xl p-6 border border-green-500/20 relative min-h-[160px] flex flex-col justify-center">
                    <div class="absolute -top-3 left-4 bg-green-600 px-2 py-0.5 text-xs text-white rounded shadow-lg">Dash AI</div>
                    <div class="flex flex-wrap gap-2 mb-3">
                        <span class="px-2 py-1 rounded bg-blue-500/20 text-blue-300 text-xs font-mono">#DP</span>
                        <span class="px-2 py-1 rounded bg-purple-500/20 text-purple-300 text-xs font-mono">#O(N)</span>
                    </div>
                    <p class="text-sm text-slate-200 leading-relaxed">
                        "점화식을 이용한 <span class="text-green-300 font-bold">동적 계획법</span> 풀이입니다. <br/>
                        메모이제이션으로 중복 계산을 제거했습니다."
                    </p>
                </div>
            </div>
          </div>


          <div class="bg-white/[0.03] border border-white/10 rounded-3xl p-8 md:p-10 flex flex-col gap-10">
            <div class="text-center md:text-left flex flex-col md:flex-row gap-6 items-center md:items-start border-b border-white/5 pb-8">
              <div class="p-4 rounded-2xl bg-blue-500/10 text-blue-400 flex-shrink-0">
                <ClipboardList :size="40" />
              </div>
              <div>
                <h3 class="text-2xl font-bold text-white mb-2">Lead (스터디장)</h3>
                <p class="text-xl font-medium text-slate-300 mb-2">
                  "매주 문제를 정하는 것도, 레벨 차이 조율도 힘들어요."
                </p>
                <p class="text-slate-400">
                  정확한 분석으로, <span class="text-blue-400 font-bold">AI가 공통 목표를 잡아줘요!<br/></span> 
                  팀원들의 Solved.ac 티어와 취약점을 분석해, 이번 주 최적의 문제를 추천합니다.
                </p>
              </div>
            </div>

            <div class="w-full grid grid-cols-1 md:grid-cols-[1fr_auto_1.2fr] gap-6 items-center">
                <div class="bg-slate-900 rounded-xl p-6 border border-white/10 relative min-h-[200px] flex flex-col justify-center">
                    <div class="absolute top-3 left-4 text-xs text-slate-500">💬 스터디 단톡방</div>
                    <div class="space-y-3 mt-4">
                        <div class="flex gap-2">
                            <div class="w-6 h-6 rounded-full bg-slate-700"></div>
                            <div class="bg-slate-800 p-2 rounded-r-lg rounded-bl-lg text-xs text-slate-300">
                                이번 주 뭐 풀까요? 골드?
                            </div>
                        </div>
                        <div class="flex gap-2 flex-row-reverse">
                            <div class="w-6 h-6 rounded-full bg-indigo-900"></div>
                            <div class="bg-indigo-900/50 p-2 rounded-l-lg rounded-br-lg text-xs text-slate-300">
                                아직 실버도 어려워요.. 😓
                            </div>
                        </div>
                        <div class="flex justify-center mt-2">
                           <span class="text-[10px] text-red-400 bg-red-950/50 px-2 py-1 rounded-full">결정 장애...</span>
                        </div>
                    </div>
                </div>

                <div class="flex justify-center text-slate-500">
                    <ArrowRight :size="32" class="hidden md:block" />
                    <ArrowDown :size="32" class="block md:hidden" />
                </div>

                <div class="bg-blue-950/20 rounded-xl border border-blue-500/20 overflow-hidden flex flex-col min-h-[200px]">
                    <div class="p-4 border-b border-blue-500/10 bg-blue-900/10 flex justify-between items-center">
                        <span class="text-xs font-bold text-blue-300 flex items-center gap-1">
                            <PieChart :size="12" /> Team Status
                        </span>
                        <span class="text-[10px] text-red-300 bg-red-500/10 px-1.5 py-0.5 rounded">DP 취약 ⚠️</span>
                    </div>
                    <div class="p-5 flex-1 flex flex-col justify-center items-center text-center">
                       <h4 class="text-white font-bold text-base mb-1">Week 4: DP 기초 다지기</h4>
                       <p class="text-xs text-slate-400 mb-3">팀원 평균 티어(실버1) 맞춤<br/>필수 문제 5선 추천</p>
                       <button class="text-xs bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-lg transition-colors w-full shadow-lg shadow-blue-500/20">
                           커리큘럼 적용하기
                       </button>
                    </div>
                </div>
            </div>
          </div>

        </div>
      </section>

      <section id="core" class="container mx-auto px-6 py-24 border-t border-white/5">
        <div class="text-center mb-16">
          <h2 class="text-4xl font-bold mb-4 text-white">Dash의 핵심 기능</h2>
          <p class="text-lg text-slate-400">코딩에만 집중하세요. 나머지는 Dash가 알아서 합니다.</p>
        </div>

        <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
          <div 
            v-for="(feature, index) in detailedFeatures" 
            :key="feature.title"
            class="group relative p-6 rounded-2xl border border-white/5 bg-white/[0.03] hover:bg-white/[0.06] transition-all duration-300 hover:-translate-y-1"
          >
            <div class="absolute inset-0 rounded-2xl bg-gradient-to-br from-indigo-500/5 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
            <div class="relative z-10">
              <div class="h-12 w-12 rounded-lg bg-indigo-500/10 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform duration-300">
                <component :is="feature.icon" :size="24" class="text-indigo-400" />
              </div>
              <h3 class="text-xl font-semibold mb-2 text-slate-200">{{ feature.title }}</h3>
              <p class="text-slate-400 text-sm leading-relaxed">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="container mx-auto px-6 py-24 border-t border-white/5">
        <div class="relative rounded-3xl bg-gradient-to-b from-indigo-900/20 to-slate-900/50 border border-indigo-500/20 p-12 overflow-hidden text-center">
          <div class="absolute top-0 left-1/2 -translate-x-1/2 w-1/2 h-1/2 bg-indigo-500/20 blur-[100px] rounded-full"></div>
            <div class="relative z-10 max-w-2xl mx-auto">
            <h2 class="text-3xl md:text-4xl font-bold mb-6 text-white">
              코딩에만 집중하세요.<br />나머지는 Dash가 합니다.
            </h2>
            
            <button
              @click="handleLogin"
              class="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-full bg-white text-slate-950 hover:bg-slate-200 transition-all font-bold text-lg hover:shadow-[0_0_20px_rgba(255,255,255,0.3)]"
            >
              <Github :size="20" />
              지금 무료로 Dash 시작하기
            </button>
          </div>
        </div>
      </section>


      </template>

      <template v-else>
        <section class="container mx-auto px-6 py-20">
          <div class="max-w-3xl mx-auto bg-gradient-to-br from-slate-900/60 to-slate-800/30 border border-white/6 rounded-3xl p-12 shadow-xl text-center">
            <div class="flex flex-col items-center gap-4">
              <div class="bg-indigo-700/20 p-4 rounded-full">
                <ClipboardList :size="48" class="text-indigo-300" />
              </div>
              <h2 class="text-3xl md:text-4xl font-extrabold text-white">대시보드</h2>
              <p class="text-slate-300 max-w-xl">가입된 스터디가 없습니다.</p>

              <div class="mt-6 flex gap-4 justify-center">
                <button class="px-5 py-3 rounded-full bg-indigo-600 text-white font-semibold shadow-lg hover:bg-indigo-500 transition" @click="goToDashboard">스터디 찾기</button>
                <button @click="goToDashboard" class="px-5 py-3 rounded-full bg-white/5 text-white border border-white/10 hover:bg-white/10 transition">새 스터디 만들기</button>
              </div>

              <p class="text-xs text-slate-500 mt-4">Tip: GitHub 연동 후 자동 커밋으로 활동이 기록됩니다.</p>
            </div>
          </div>
        </section>
      </template>
    </main>

    <footer class="border-t border-white/5 py-8 bg-slate-950">
      <div class="container mx-auto px-6 text-center text-sm text-slate-500">
        © 2025 DASH. All rights reserved.
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { 
  Code2, Github, UploadCloud, Bot, ClipboardList, 
  ArrowRight, ArrowDown, Search, PieChart, 
  FileCode, FolderTree, GitCommit 
  , ChevronDown
} from 'lucide-vue-next'

const detailedFeatures = ref([
  { icon: UploadCloud, title: '완전 자동화', description: '백준 문제 제출 시 성공/실패 여부 상관없이 코드를 자동으로 GitHub에 커밋하고 Dash 대시보드를 갱신합니다.' },
  { icon: Bot, title: 'AI 자동 요약', description: '제출된 코드를 Dash AI가 분석하여 풀이의 핵심 요약, 시간/공간 복잡도, 사용 알고리즘 태그를 자동으로 생성합니다.' },
  { icon: FolderTree, title: '깔끔한 코드 관리', description: 'IDE에서 Main으로 풀어도 괜찮습니다. 자동으로 패키지와 파일명을 스터디 규칙에 맞게 변환하여 저장합니다.' },
  { icon: PieChart, title: '데이터 기반 목표', description: 'Solved.ac 연동 데이터를 기반으로 스터디원들의 취약한 유형을 분석하고, 효과적인 공통 학습 목표를 추천합니다.' },
  { icon: Search, title: '통합 검색', description: '스터디원들이 제출한 모든 풀이와 AI가 요약한 내용을 키워드(문제 번호, 태그, 풀이 내용)로 즉시 검색할 수 있습니다.' },
  { icon: GitCommit, title: '노력의 기록', description: '실패한 제출도 사라지지 않습니다. 당신의 고민과 시도를 잔디(Contribution)와 대시보드에 시각적으로 남겨드립니다.' }
])

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const user = ref(null)
const profileMenuOpen = ref(false)
const profileRef = ref(null)

const userInitial = computed(() => {
  const name = user.value?.name || user.value?.email || ''
  return name ? String(name).trim().charAt(0).toUpperCase() : 'U'
})

const scrollToSection = (id) => {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const goHome = () => { window.location.href = '/' }
const goToDashboard = () => { window.location.href = '/dashboard' }
const handleLogin = () => { window.location.href = `${API_BASE}/oauth2/authorization/google` }

const toggleProfileMenu = (evt) => {
  profileMenuOpen.value = !profileMenuOpen.value
}

const goToProfile = () => {
  profileMenuOpen.value = false
  window.location.href = '/profile'
}

const onDocClick = (e) => {
  const el = profileRef.value
  if (!el) return
  if (!el.contains(e.target)) profileMenuOpen.value = false
}

const handleLogout = async () => {
  try {
    await fetch(`${API_BASE}/logout`, { method: 'POST', credentials: 'include' })
  } catch (e) {
    console.error('Logout failed:', e)
  } finally {
    window.location.href = '/' 
  }
}

onMounted(async () => {
  try {
    const res = await fetch(`${API_BASE}/api/me`, { credentials: 'include' })
    if (res.ok) user.value = await res.json()
  } catch (e) {
    // not authenticated
  }
  document.addEventListener('click', onDocClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})
</script>

<style scoped>
@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
}

/* slide-down transition for header dropdown */
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}
.slide-down-enter-to,
.slide-down-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}
.slide-down-enter-active,
.slide-down-leave-active {
  transition: transform 180ms cubic-bezier(.16,1,.3,1), opacity 160ms ease;
}
</style>