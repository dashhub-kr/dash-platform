<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-indigo-500/30">

    <main class="container mx-auto px-6 py-10 max-w-2xl">
      <div class="mb-8 animate-fade-in-up">
        <h1 class="text-3xl font-bold mb-2">마이페이지</h1>
        <p class="text-slate-400">내 정보를 확인하고 수정할 수 있습니다.</p>
      </div>

      <div class="bg-slate-900/50 border border-white/10 rounded-2xl p-8 animate-fade-in-up delay-100">
        <div class="flex items-center gap-6 mb-8 pb-8 border-b border-white/5">
          <div class="w-20 h-20 rounded-full bg-indigo-600/20 flex items-center justify-center text-3xl font-bold text-indigo-400">
            {{ userInitial }}
          </div>
          <div>
            <h2 class="text-xl font-bold text-white">{{ userData.username }}</h2>
            <p class="text-slate-400">{{ userData.email }}</p>
          </div>
        </div>

        <form @submit.prevent="handleUpdate" class="space-y-6">
          <div class="space-y-2">
            <label for="username" class="text-sm font-medium text-slate-300">이름</label>
            <input
              id="username"
              v-model="userData.username"
              type="text"
              class="w-full bg-slate-800/50 border border-white/10 rounded-lg px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-transparent transition-all"
            />
          </div>

           <div class="space-y-2">
            <label for="email" class="text-sm font-medium text-slate-300">이메일</label>
            <input
              id="email"
              v-model="userData.email"
              type="email"
              disabled
              class="w-full bg-slate-800/20 border border-white/5 rounded-lg px-4 py-3 text-slate-500 cursor-not-allowed"
            />
            <p class="text-xs text-slate-500">이메일은 변경할 수 없습니다.</p>
          </div>

          <div class="pt-4 flex justify-end">
            <button
              type="submit"
              :disabled="updating"
              class="px-6 py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg font-bold shadow-lg shadow-indigo-500/20 transition-all hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              <div v-if="updating" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              저장하기
            </button>
          </div>
        </form>

        <div class="mt-8 pt-8 border-t border-white/5">
            <h3 class="text-red-400 font-bold mb-4">위험 구역</h3>
            <button 
                @click="handleDelete"
                class="px-4 py-2 border border-red-500/30 text-red-400 rounded-lg hover:bg-red-500/10 transition-colors text-sm"
            >
                회원 탈퇴
            </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { userApi } from '../api/user';
import { useAuth } from '../composables/useAuth';

const router = useRouter();
const { refresh, user } = useAuth(); // Assuming useAuth has refresh to update session user state implies getting fresh data

const userData = ref({
    id: null,
    username: '',
    email: ''
});
const updating = ref(false);

const userInitial = computed(() => {
    return userData.value.username ? userData.value.username.charAt(0).toUpperCase() : 'U';
});

onMounted(async () => {
    try {
        const res = await userApi.getMyProfile();
        userData.value = res.data;
    } catch (e) {
        console.error("Failed to load profile", e);
    }
});

const handleUpdate = async () => {
    updating.value = true;
    try {
        await userApi.update(userData.value.id, { 
            username: userData.value.username,
            email: userData.value.email 
        });
        alert("정보가 수정되었습니다.");
        // Should refresh global auth state if useAuth stores user info
        window.location.reload(); 
    } catch (e) {
        console.error("Update failed", e);
        alert("수정에 실패했습니다.");
    } finally {
        updating.value = false;
    }
};

const handleDelete = async () => {
    if(!confirm("정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) return;
    try {
        await userApi.delete(userData.value.id);
        alert("탈퇴 처리되었습니다.");
        window.location.href = "/";
    } catch (e) {
        console.error("Delete failed", e);
        alert("실패했습니다.");
    }
};
</script>

<style scoped>
.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}
.delay-100 { animation-delay: 0.1s; }
@keyframes fade-in-up {
  to { opacity: 1; transform: translateY(0); }
}
</style>
