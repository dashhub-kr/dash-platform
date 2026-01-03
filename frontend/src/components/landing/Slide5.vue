<template>
  <div class="w-full h-full bg-slate-900 flex items-center justify-center overflow-hidden relative">
      <!-- 배경 효과 (다크 테마 - Slide 1과의 수미상관) -->
      <div class="absolute inset-0 bg-gradient-to-t from-black via-slate-900 to-slate-900 z-0"></div>
      <div class="absolute top-0 w-full h-px bg-gradient-to-r from-transparent via-white/10 to-transparent"></div>
      
      <!-- 배경 글로우 -->
      <div class="absolute bottom-0 left-1/2 -translate-x-1/2 w-[800px] h-[500px] bg-brand-600/20 rounded-full blur-[120px] pointer-events-none animate-pulse-slow"></div>

      <div class="relative z-10 max-w-5xl mx-auto px-6 w-full text-center space-y-12">
          
          <!-- 상단 텍스트 -->
          <div class="space-y-6 animate-in fade-in slide-in-from-bottom-8 duration-1000">
              <h2 class="text-5xl md:text-7xl font-black text-white tracking-tight leading-1">
                  준비되셨나요?<br/>
                  <span class="text-white underline decoration-wavy decoration-brand-500/50 underline-offset-8 decoration-4"><span class="text-brand-500">성장</span>은 지금부터입니다.</span>
              </h2>
              <p class="text-xl text-slate-400 font-medium max-w-2xl mx-auto">
                  GitHub 계정으로 3초 만에 시작하세요.<br>
                  당신의 성향에 딱 맞는 <strong>최적의 스터디</strong>가 기다리고 있습니다.
              </p>
          </div>

          <!-- 카드 섹션 (가입 -> 매칭) -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl mx-auto">
               <!-- 카드 1: 깃허브 가입 -->
               <div class="group relative bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl p-8 hover:bg-white/10 transition-all duration-300 animate-in slide-in-from-bottom-8 duration-700 delay-200">
                    <div class="absolute -top-6 left-1/2 -translate-x-1/2 w-12 h-12 bg-slate-800 border-4 border-slate-900 rounded-full flex items-center justify-center shadow-lg">
                        <Github class="w-6 h-6 text-white" />
                    </div>
                    <h3 class="mt-4 text-xl font-bold text-white mb-2">3초 무설치 가입</h3>
                    <p class="text-sm text-slate-400">
                        별도 회원가입 없이,<br/>
                        GitHub 계정 그대로 시작하세요.
                    </p>
               </div>

               <!-- 카드 2: 스터디 매칭 -->
               <div class="group relative bg-gradient-to-br from-brand-500/10 to-purple-500/10 backdrop-blur-sm border border-brand-500/20 rounded-2xl p-8 hover:border-brand-500/40 transition-all duration-300 animate-in slide-in-from-bottom-8 duration-700 delay-400">
                    <div class="absolute -top-6 left-1/2 -translate-x-1/2 w-12 h-12 bg-slate-800 border-4 border-slate-900 rounded-full flex items-center justify-center shadow-lg">
                        <Users class="w-6 h-6 text-brand-400" />
                    </div>
                    <h3 class="mt-4 text-xl font-bold text-white mb-2">스터디 자동 매칭</h3>
                    <p class="text-sm text-slate-400">
                        나와 비슷한 목표를 가진<br/>
                        동료들을 찾아드립니다.
                    </p>
               </div>
          </div>

          <!-- 최종 CTA -->
          <div class="pt-8 animate-in fade-in slide-in-from-bottom-8 duration-1000 delay-500">
              <button @click="handleLogin" class="relative group px-12 py-5 bg-white text-slate-900 rounded-full font-black text-xl shadow-[0_0_40px_-10px_rgba(255,255,255,0.3)] hover:scale-105 hover:shadow-[0_0_60px_-10px_rgba(255,255,255,0.5)] transition-all duration-300 overflow-hidden">
                <span class="relative z-10 flex items-center gap-3">
                    <Github class="w-6 h-6" />
                    GitHub로 바로 시작하기
                </span>
                <div class="absolute inset-0 bg-gradient-to-r from-transparent via-brand-200/50 to-transparent translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700"></div>
              </button>
              <p class="mt-4 text-xs text-slate-500">
                  * 초기 설정 비용 0원 • 언제든 탈퇴 가능
              </p>
          </div>

      </div>
  </div>
</template>

<script setup>
import { Github, Users } from 'lucide-vue-next';
import { useRouter } from 'vue-router';

const router = useRouter();

const handleLogin = () => {
    window.location.href = '/oauth2/authorization/github';
};
</script>

<style scoped>
.animate-pulse-slow {
   animation: pulse 6s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
