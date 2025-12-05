<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500/30">
    
    <main class="container mx-auto px-6 py-10">
      <!-- Welcome Section -->
      <section class="mb-12">
        <h1 class="text-3xl md:text-4xl font-bold mb-4 animate-fade-in-up">
          안녕하세요, <span class="text-indigo-400">{{ user?.username || '사용자' }}</span>님!
        </h1>
        <p class="text-slate-400 animate-fade-in-up delay-100">
          오늘도 코딩하기 좋은 날입니다. 학습 현황을 확인해보세요.
        </p>
      </section>

      <!-- Stats Overview -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12 animate-fade-in-up delay-200">
        <!-- Solved Count -->
        <div class="bg-slate-900/50 border border-white/10 rounded-2xl p-6 hover:bg-slate-900 transition-colors group">
          <div class="flex items-center gap-4 mb-4">
            <div class="p-3 bg-indigo-500/20 rounded-xl text-indigo-400 group-hover:scale-110 transition-transform">
              <CheckCircle :size="24" />
            </div>
            <h3 class="text-lg font-semibold text-slate-200">해결한 문제</h3>
          </div>
          <p class="text-4xl font-bold text-white mb-2">{{ solvedCount }}</p>
          <p class="text-sm text-slate-500">이번 주 +{{ weeklySolvedCount }} 문제</p>
        </div>

        <!-- Streak -->
        <div class="bg-slate-900/50 border border-white/10 rounded-2xl p-6 hover:bg-slate-900 transition-colors group">
          <div class="flex items-center gap-4 mb-4">
            <div class="p-3 bg-green-500/20 rounded-xl text-green-400 group-hover:scale-110 transition-transform">
              <Flame :size="24" />
            </div>
            <h3 class="text-lg font-semibold text-slate-200">연속 학습일</h3>
          </div>
          <p class="text-4xl font-bold text-white mb-2">{{ streakDays }}일</p>
          <p class="text-sm text-slate-500">뜨겁게 불태우고 계시네요!</p>
        </div>

        <!-- Point/Tier -->
        <div class="bg-slate-900/50 border border-white/10 rounded-2xl p-6 hover:bg-slate-900 transition-colors group">
          <div class="flex items-center gap-4 mb-4">
            <div class="p-3 bg-yellow-500/20 rounded-xl text-yellow-400 group-hover:scale-110 transition-transform">
              <Trophy :size="24" />
            </div>
            <h3 class="text-lg font-semibold text-slate-200">나의 티어</h3>
          </div>
          <p class="text-4xl font-bold text-white mb-2">Gold 1</p>
          <p class="text-sm text-slate-500">Platinum까지 120점 남음</p>
        </div>
      </section>

      <!-- Quick Actions & Recent Activity -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 animate-fade-in-up delay-300">
        <!-- Quick Actions -->
        <section class="lg:col-span-2">
          <h2 class="text-xl font-bold mb-6 flex items-center gap-2">
            <LayoutDashboard :size="24" class="text-indigo-400" />
            빠른 이동
          </h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <button @click="$router.push('/boards')" class="p-6 bg-slate-800/50 border border-white/5 rounded-xl hover:bg-slate-800 hover:border-indigo-500/50 transition-all text-left group">
              <div class="flex justify-between items-start mb-4">
                <MessageSquare :size="28" class="text-slate-400 group-hover:text-indigo-400 transition-colors" />
                <ArrowRight :size="20" class="text-slate-600 group-hover:translate-x-1 transition-transform" />
              </div>
              <h3 class="text-lg font-semibold text-white mb-1">게시판</h3>
              <p class="text-sm text-slate-400">스터디원들과 정보를 공유하세요.</p>
            </button>

             <button @click="$router.push('/profile')" class="p-6 bg-slate-800/50 border border-white/5 rounded-xl hover:bg-slate-800 hover:border-indigo-500/50 transition-all text-left group">
              <div class="flex justify-between items-start mb-4">
                <User :size="28" class="text-slate-400 group-hover:text-indigo-400 transition-colors" />
                <ArrowRight :size="20" class="text-slate-600 group-hover:translate-x-1 transition-transform" />
              </div>
              <h3 class="text-lg font-semibold text-white mb-1">마이페이지</h3>
              <p class="text-sm text-slate-400">내 정보와 설정을 관리하세요.</p>
            </button>
          </div>
        </section>

        <!-- Notification / Tip -->
        <section>
          <h2 class="text-xl font-bold mb-6 flex items-center gap-2">
            <Bell :size="24" class="text-yellow-400" />
            알림
          </h2>
          <div class="bg-gradient-to-br from-indigo-900/20 to-slate-900 border border-indigo-500/20 rounded-2xl p-6">
            <div class="mb-4">
              <span class="bg-indigo-500/20 text-indigo-300 text-xs font-bold px-2 py-1 rounded">Tip</span>
            </div>
            <h3 class="text-lg font-semibold text-white mb-2">알고리즘 기록 연동</h3>
            <p class="text-slate-400 text-sm mb-4 leading-relaxed">
              GitHub 저장소를 등록하면 문제 풀이 기록이 자동으로 대시보드에 반영됩니다.
            </p>
            <button class="w-full py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-sm font-semibold transition-colors">
              저장소 연결하기
            </button>
          </div>
        </section>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '../composables/useAuth';
import { 
  CheckCircle, 
  Flame, 
  Trophy, 
  LayoutDashboard, 
  MessageSquare, 
  User, 
  ArrowRight,
  Bell
} from 'lucide-vue-next';
import { algorithmApi } from '../api/algorithm';

const router = useRouter();
const { user } = useAuth();
const solvedCount = ref(0);
const weeklySolvedCount = ref(0);
const streakDays = ref(0);

// Fetch stats on mount
onMounted(async () => {
    try {
        // Mock data or fetch real data
        const records = await algorithmApi.findAll();
        solvedCount.value = records.data ? records.data.length : 0;
        
        // Simple mock logic for other stats
        weeklySolvedCount.value = 3; 
        streakDays.value = 12; 
    } catch (e) {
        console.error("Failed to load dashboard stats", e);
    }
});
</script>

<style scoped>
.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

.delay-100 { animation-delay: 0.1s; }
.delay-200 { animation-delay: 0.2s; }
.delay-300 { animation-delay: 0.3s; }

@keyframes fade-in-up {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
